
/**
 * AITracking handles the artificial intelligence of tracking the player. Each
 * enemy that chases the player has an object of this class.
 * @author Patrick Owen
 */
public class AITracking extends AIBasic
{
	//Constant controls
	private double maxSpeed;
	private double agility;
	
	private double minPreferredDistance;
	private double maxPreferredDistance;
	private double blockedPreferredDistance;
	private double distanceChangeFrequency;
	
	private double maxStrafeSpeed;
	private double strafeChangeFrequency;
	
	private double minPreferredHeight;
	private double maxPreferredHeight;
	private double heightChangeFrequency;
	
	//Non-constant controls
	private double preferredDistance;
	private double strafeSpeed;
	private double preferredHeight;
	
	/**
	 * Constructs and AITracking object.
	 * @param controller The controller object.
	 * @param gameMap The map where the enemy is.
	 * @param e The parent enemy.
	 */
	public AITracking(Controller controller, GameMap gameMap, Enemy e)
	{
		super(controller, gameMap, e);
		
		preferredDistance = minPreferredDistance;
		strafeSpeed = 0;
	}
	
	/**
	 * Moves the enemy in the appropriate way based on the AI
	 * @param dt Time step in seconds.
	 */
	public void performAI(double dt)
	{
		if (enemy instanceof Damageable)
		{
			radius = ((Damageable)enemy).getRadius();
			height = ((Damageable)enemy).getHeight();
		}
		
		x = enemy.getX(); y = enemy.getY(); z = enemy.getZ();
		xV = enemy.getXV(); yV = enemy.getYV(); zV = enemy.getZV();
		
		handleMovement(dt);
		handleCollisionChecking(dt);
		
		enemy.setPosition(x, y, z);
		enemy.setVelocity(xV, yV, zV);
	}
	
	/**
	 * Sets the parameters for the tracking AI
	 */
	public void setControls(double setMaxSpeed, double setAgility, double setMinPreferredDistance,
			double setMaxPreferredDistance, double setBlockedPreferredDistance,
			double setDistanceChangeFrequency, double setMaxStrafeSpeed, double setStrafeChangeFrequency,
			double setMinPreferredHeight, double setMaxPreferredHeight, double setHeightChangeFrequency)
	{
		maxSpeed = setMaxSpeed;
		agility = setAgility;
		
		minPreferredDistance = setMinPreferredDistance;
		maxPreferredDistance = setMaxPreferredDistance;
		blockedPreferredDistance = setBlockedPreferredDistance;
		distanceChangeFrequency = setDistanceChangeFrequency;
		
		maxStrafeSpeed = setMaxStrafeSpeed;
		strafeChangeFrequency = setStrafeChangeFrequency;
		
		minPreferredHeight = setMinPreferredHeight;
		maxPreferredHeight = setMaxPreferredHeight;
		heightChangeFrequency = setHeightChangeFrequency;
	}
	
	//Handles the AI-decided movement of the turret; pays no attention to terrain
	private void handleMovement(double dt)
	{
		Player player = map.getPlayer();
		if (!player.isDead())
		{
			/*
			 * ALGORITHM 4:
			 * Set the intended speed directly towards or away from the player to reach the preferred distance.
			 * Add the intended speed by an amount perpendicular to the direction to the player to strafe around the player.
			 * Multiply the speed goal by the right amount to make the enemy move at its maximum speed.
			 * Modify the strafe speed, preferred distance, and preferred height randomly to add variety in enemy movement.
			 * Modify the actual enemy speed to move towards its intended speed (based on its agility).
			 */
			double xDiff = player.getX()-x, yDiff = player.getY()-y, zDiff = player.getZ()-z;
			
			double xVGoal = 0, yVGoal = 0, zVGoal = 0;
			
			//Track player
			double hDist = Math.sqrt(xDiff*xDiff + yDiff*yDiff);
			if (canGoToPlayer())
			{
				xVGoal += (xDiff/hDist)*(hDist-preferredDistance); yVGoal = (yDiff/hDist)*(hDist-preferredDistance);
				zVGoal += zDiff+preferredHeight;
			}
			else
			{
				xVGoal += (xDiff/hDist)*(hDist-blockedPreferredDistance); yVGoal = (yDiff/hDist)*(hDist-blockedPreferredDistance);
				zVGoal += zDiff+preferredHeight;
			}
			
			//Strafe
			xVGoal += yDiff*strafeSpeed; yVGoal += -xDiff*strafeSpeed;
			
			double speedGoal = Math.sqrt(xVGoal*xVGoal + yVGoal*yVGoal + zVGoal*zVGoal);
			if (speedGoal != 0)
			{
				xVGoal *= maxSpeed/speedGoal;
				yVGoal *= maxSpeed/speedGoal;
				zVGoal *= maxSpeed/speedGoal;
			}
			
			if (Math.random() < strafeChangeFrequency*dt)
			{
				strafeSpeed = 2*maxStrafeSpeed*Math.random()-maxStrafeSpeed;
			}
			
			if (Math.random() < distanceChangeFrequency*dt)
			{
				preferredDistance = (maxPreferredDistance-minPreferredDistance)*Math.random()+minPreferredDistance;
			}
			
			if (Math.random() < heightChangeFrequency*dt)
			{
				preferredHeight = (maxPreferredHeight-minPreferredHeight)*Math.random()+minPreferredHeight;
			}
			
			xV += agility*(xVGoal-xV)*dt;
			yV += agility*(yVGoal-yV)*dt;
			zV += agility*(zVGoal-zV)*dt;
		}
		else
		{
			xV -= agility*xV*dt;
			yV -= agility*yV*dt;
			zV -= agility*zV*dt;
		}
	}
	
	private boolean canGoToPlayer()
	{
		Player p = map.getPlayer();
		double px = (p.getX())-(x);
		double py = (p.getY())-(y);
		double pz = (p.getZ())-(z)+preferredHeight;
		double pdist = Math.sqrt(px*px+py*py+pz*pz);
		if (pdist < 1) return true;
		
		px /= pdist; py /= pdist; pz /= pdist;
		
		Collision col = map.getCollision();
		
		double t = col.getPlayerCollision(x,y,z,px,py,pz,radius,height);
		if (t == 1) return true;
		
		if (-px*col.getNormalX() - py*col.getNormalY() - pz*col.getNormalZ() < 0.2)
			return true;
		
		return false;
	}
	
	//Moves the turret and makes sure it does not go through a wall
	private void handleCollisionChecking(double dt)
	{
		double remaining = 1; //Amount of distance remaining to travel
		boolean normalsReceived = false; //Checks whether there is the possibility of two simultaneous collisions.
		double nx=0, ny=0, nz=0;
		
		for (int i=0; i<5; i++) //Cannot repeat this forever
		{
			double t = map.getCollision().getPlayerCollision(x,y,z,xV*dt*remaining,yV*dt*remaining,zV*dt*remaining,radius,height);
			boolean stopped = (t < 0.001);
			
			if (!stopped)
			{
				x += xV*dt*t*remaining;
				y += yV*dt*t*remaining;
				z += zV*dt*t*remaining;
			}
			
			if (t == 1) break;
			
			double m = -map.getCollision().getNormalX()*xV - map.getCollision().getNormalY()*yV - map.getCollision().getNormalZ()*zV;
			xV += m*map.getCollision().getNormalX(); yV += m*map.getCollision().getNormalY(); zV += m*map.getCollision().getNormalZ();
			
			//Deal with being in a corner of some sort
			if (normalsReceived && stopped)
			{
				m = -map.getCollision().getNormalX()*nx - map.getCollision().getNormalY()*ny - map.getCollision().getNormalZ()*nz;
				nx += m*map.getCollision().getNormalX(); ny += m*map.getCollision().getNormalY(); nz += m*map.getCollision().getNormalZ();
				double dist = Math.sqrt(nx*nx + ny*ny + nz*nz);
				if (dist != 0)
				{
					nx /= dist; ny /= dist; nz /= dist;
					m = -nx*xV - ny*yV - nz*zV;
					xV += m*nx; yV += m*ny; zV += m*nz;
				}
			}
			
			nx = map.getCollision().getNormalX(); ny = map.getCollision().getNormalY(); nz = map.getCollision().getNormalZ(); normalsReceived = true;
			
			remaining *= 1-t;
			
			if (i == 4)
			{
			    xV = 0;
			    yV = 0;
			    zV = 0;
			}
		}
	}
}
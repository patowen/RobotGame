import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Handles the controls and situation of the player. This class represents the player of the game.
 * @author Patrick Owen
 */
public class Player extends Entity implements Damageable
{
	private InputHandler input;
	
	private double horizontalDir, verticalDir;
	
	private boolean inAir; //Flag that stores whether the player is in the air or on the ground.
	private double floorNormX, floorNormY, floorNormZ;
	
	private double hp;
	private boolean isDead;
	
	//Constants
	private double radius, height;
	private double eyeHeight;
	private double terrainTolerance;
	private double friction, airResistance;
	private double groundAcceleration, airAcceleration;
	private double maxSpeed;
	private double jumpSpeed;
	private double stepDownHeight;
	private double maxHp;
	
	private Weapon currentWeapon;
	private Weapon[] weapons;
	
	/**
	 * Initializes the player with default parameters.
	 * @param controller The active Controller object.
	 * @param world Level where the player is placed.
	 */
	public Player(Controller controller, World world)
	{
		super(controller, world);
		
		input = c.getInputHandler();
		w = world;
		inAir = true;
		
		weapons = new Weapon[3];
		weapons[0] = new PlasmaRifle(c, w);
		weapons[1] = new PlasmaSword(c, w);
		weapons[2] = new PlasmaLauncher(c, w);
		
		for (int i=0; i<weapons.length; i++)
			weapons[i].setPlayer(this);
		
		currentWeapon = weapons[0];
		
		terrainTolerance = 0.5;
		radius = 0.2;
		height = 1;
		eyeHeight = 0.8;
		friction = 20;
		airResistance = 0.2;
		groundAcceleration = 20;
		airAcceleration = 2.5;
		maxSpeed = 4;
		jumpSpeed = 4;
		stepDownHeight = 0.2;
		maxHp = 50;
		
		hp = maxHp;
		isDead = false;
	}
	
	/**
	 * Draws the player
	 * @param gl
	 */
	public void draw(GL2 gl)
	{
		if (!isDead)
		{
			if (!isLocal)
			{
				GLUT glut = new GLUT();
				gl.glPushMatrix();
				gl.glTranslated(x, y, z);
				glut.glutSolidCylinder(radius, height, 12, 1);
				gl.glPopMatrix();
			}
			currentWeapon.draw(gl);
		}
	}
	
	/**
	 * Draws transparent parts of the player, called after
	 * all draw methods are called
	 * @param gl
	 */
	public void draw2(GL2 gl)
	{
		if(!isDead)
			currentWeapon.draw2(gl);
	}
	
	public boolean isGhost()
	{
		return !isActive && isDead;
	}
	
	/**
	 * Applies damage to the player and knocks him back.
	 * @param amount The amount of health loss the enemy inflicts.
	 * @param x
	 * @param y
	 * @param z The direction the blast <i>comes from</i>. Should be a unit vector.
	 * @param knockBack The speed that the player is knocked back.
	 * @param absolute Whether all momentum should be stopped and replaced by the knockback.
	 * If set to false, the knockback is added to the current momentum.
	 */
	public void applyDamage(double amount, double x, double y, double z, double knockBack, boolean absolute)
	{
		if (absolute)
		{
			xV = -knockBack*x;
			yV = -knockBack*y;
			zV = -knockBack*z;
		}
		else
		{
			xV += -knockBack*x;
			yV += -knockBack*y;
			zV += -knockBack*z;
		}
		
		inAir = true;
		
		hp -= amount;
		
		//Calculate hit mark angle
		double temp = x*Math.cos(horizontalDir) + y*Math.sin(horizontalDir);
		y = -x*Math.sin(horizontalDir) + y*Math.cos(horizontalDir);
		x = temp;
		
		temp = x*Math.cos(verticalDir) + z*Math.sin(verticalDir);
		z = -x*Math.sin(verticalDir) + z*Math.cos(verticalDir);
		x = temp;
		
		if (isLocal)
			c.getHUD().addHitMark(Math.atan2(z, -y));
	}
	
	/**
	 * Heals the player, often called after each wave as a bonus and to give no incentive for the player
	 * to wait before destroying the last enemy.
	 */
	public void heal()
	{
		hp = Math.min(maxHp, hp+5);
	}
	
	/**
	 * Returns the maximum health.
	 */
	public double getMaxHP()
	{
		return maxHp;
	}
	
	/**
	 * Returns the current health.
	 */
	public double getHP()
	{
		return hp;
	}
	
	/**
	 * Returns the radius of the player.
	 */
	public double getRadius()
	{
		return radius;
	}
	
	/**
	 * Returns the height of the player.
	 */
	public double getHeight()
	{
		return height;
	}
	
	/**
	 * Returns the currently equipped weapon
	 */
	public Weapon getCurrentWeapon()
	{
		return currentWeapon;
	}
	
	/**
	 * Returns the weapon list
	 */
	public Weapon[] getWeapons()
	{
		return weapons;
	}
	
	/**
	 * Initializes extra data for the player.<br/>
	 * 0: Horizontal facing direction<br/>
	 * 1: Vertical facing direction
	 */
	public void initializeExtraData(int index, double data)
	{
		switch (index)
		{
		case 0:
			horizontalDir = data;
		case 1:
			verticalDir = data;
		}
	}
	
	public int getAmountExtraData()
	{
		return 2;
	}
	
	/**
	 * Returns whether the player is dead.
	 */
	public boolean isDead()
	{
		return isDead;
	}
	
	/**
	 * Runs a step of the game and moves the player according to the game physics.
	 * @param dt Time step in seconds
	 */
	public void step(double dt)
	{
		super.step(dt);
		
		if (!isDead)
		{
			handleLooking();
			handleJumping(dt);
			handleMovement(dt);
			handleCollisionChecking(dt);
			
			handleWeapons(dt);
			
			c.getSoundHandler().setListenerPosition(x, y, z+eyeHeight);
			c.getSoundHandler().setListenerOrientation(Math.cos(horizontalDir)*Math.cos(verticalDir), Math.sin(horizontalDir)*Math.cos(verticalDir), eyeHeight+Math.sin(verticalDir),
					-Math.cos(horizontalDir)*Math.sin(verticalDir), -Math.sin(horizontalDir)*Math.sin(verticalDir), Math.cos(verticalDir));
			
			//Allow slow recovery
//			hp += dt*0.2;
//			if (hp > maxHp) hp = maxHp;
			
			//Handle death
			if (hp <= 0)
			{
				EntityExplosion explosion = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
				explosion.setPosition(x, y, z+height/2);
				explosion.setDuration(1);
				explosion.setRadius(0.2f);
				explosion.setFinalRadius(2);
				explosion.setDuration(1);
				explosion.setColor(1, 0.5f, 0.25f);
				w.create(explosion);
				isDead = true;
			}
		}
	}
	
	/**
	 * Adjusts the ModelView matrix to make the scene viewed from the eyes of the player.
	 */
	public void viewFirstPerson(GL gl)
	{
		GLU glu = new GLU();
		glu.gluLookAt(x, y, z+eyeHeight, x+Math.cos(horizontalDir)*Math.cos(verticalDir), y+Math.sin(horizontalDir)*Math.cos(verticalDir), z+eyeHeight+Math.sin(verticalDir),
				-Math.cos(horizontalDir)*Math.sin(verticalDir), -Math.sin(horizontalDir)*Math.sin(verticalDir), Math.cos(verticalDir));
	}
	
	/**
	 * Adjusts the ModelView matrix to make the player viewed at a reasonable distance
	 */
	public void viewThirdPerson(GL gl)
	{
		GLU glu = new GLU();
		glu.gluLookAt(x-5*Math.cos(horizontalDir), y+5*Math.sin(horizontalDir), z+3, x, y, z+height/2, 0, 0, 1);
	}
	
	//Handles using all the player's weapons.
	private void handleWeapons(double dt)
	{
		if (isLocal)
		{
			if (input.getKey(InputHandler.WEAPON1) && currentWeapon != weapons[0])
				currentWeapon = weapons[0];
			if (input.getKey(InputHandler.WEAPON2) && currentWeapon != weapons[1])
				currentWeapon = weapons[1];
			if (input.getKey(InputHandler.WEAPON3) && currentWeapon != weapons[2])
				currentWeapon = weapons[2];
		}
		for(Weapon w : weapons)
			w.recharge(dt);
		currentWeapon.setPosition(x, y, z+eyeHeight, horizontalDir, verticalDir);
		currentWeapon.step(dt);
	}
	
	
	//Adjusts the player's view angle with the mouse.
	private void handleLooking()
	{
		if (this == w.getPlayer())
		{
			input.readMouse();
			
			double mx = -input.getMouseX();
			double my = -input.getMouseY();
			mx = mx*45;
			my = my*45;
			
			horizontalDir += mx;
			verticalDir += my;
			
			if (verticalDir > Math.PI/2) verticalDir = Math.PI/2;
			if (verticalDir < -Math.PI/2) verticalDir = -Math.PI/2;
		}
	}
	
	//Handles the situations that can cause the player to leave the ground.
	private void handleJumping(double dt)
	{
		/*
		 * ALGORITHM 1a:
		 * If jump key is pressed and is on the ground
		 *    Jump by changing the vertical velocity and leave the ground (updating the inAir variable).
		 * Check how far down the closest piece of land is (by checking for collision in a downward direction) and store the result in zLand
		 * If the player is in the air (already known, no land directly under the player, or the land under the player is too steep)
		 *    Update vertical velocity by the gravity
		 *    Leave the ground (updating the inAir variable)
		 */
		if (isLocal && input.getKeyPressed(InputHandler.JUMP) && !inAir)
		{
			inAir = true;
			zV += jumpSpeed;
		}
		
		double t = w.getCollision().getPlayerCollision(x, y, z, 0, 0, -(zV+8)*dt, radius, height);
		double zLand = z-(zV+8)*dt*t;
		floorNormX = w.getCollision().getNormalX(); floorNormY = w.getCollision().getNormalY(); floorNormZ = w.getCollision().getNormalZ();
		
		if (inAir || z > zLand + 0.05 || floorNormZ < terrainTolerance)
		{
			zV -= w.getGravity()*dt;
			inAir = true;
		}
		
		//z_prev = z;
	}
	
	//Handles changes in movement speed from friction and keyboard controls.
	private void handleMovement(double dt)
	{
		double acceleration;
		
		if (inAir) acceleration = airAcceleration;
		else acceleration = groundAcceleration;
		
		boolean upKey = input.getKey(InputHandler.UP); //W
		boolean downKey = input.getKey(InputHandler.DOWN); //S
		boolean leftKey = input.getKey(InputHandler.LEFT); //A
		boolean rightKey = input.getKey(InputHandler.RIGHT); //D
		
		boolean moving;
		
		//Ignore keys that cancel each other out
		if (isLocal)
		{
			if (upKey && downKey) {upKey = false; downKey = false;}
			if (leftKey && rightKey) {leftKey = false; rightKey = false;}
			
			//Figure out whether the user is pressing any substantial keys.
			if (leftKey || upKey || rightKey || downKey) moving = true;
			else moving = false;
		}
		else
		{
			moving = false;
		}
		
		/*
		 * ALGORITHM 1b:
		 * If the player is pressing any keys that should move the user
		 *    Smoothly change the speed of the player based on which key combination is being pressed.
		 * Otherwise
		 *    Slow the player down using air friction if the player is in the air or through quick
		 *      stopping if the player is on the ground.
		 */
		if (moving)
		{
			if (downKey && leftKey)
				changeSpeed(dt, -0.707,0.707,acceleration);
			else if (downKey && rightKey)
				changeSpeed(dt, -0.707,-0.707,acceleration);
			else if (upKey && rightKey)
				changeSpeed(dt, 0.707,-0.707,acceleration);
			else if (upKey && leftKey)
				changeSpeed(dt, 0.707,0.707,acceleration);
			
			else if (downKey)
				changeSpeed(dt, -1,0,acceleration);
			else if (upKey)
				changeSpeed(dt, 1,0,acceleration);
			else if (leftKey)
				changeSpeed(dt, 0,1,acceleration);
			else if (rightKey)
				changeSpeed(dt, 0,-1,acceleration);
		}
		else
		{
			if (inAir)
			{
				xV = xV - xV*airResistance*dt;
				yV = yV - yV*airResistance*dt;
				zV = zV - zV*airResistance*dt;
			}
			else
				changeSpeed(dt, 0,0,friction);
		}
	}
	
	//Handles all player movement that involves collision with the environment.
	private void handleCollisionChecking(double dt)
	{
		double remaining = 1; //Amount of distance remaining to travel
		boolean normalsReceived = false; //Checks whether there is the possibility of two simultaneous collisions.
		double nx=0, ny=0, nz=0;
		
		/*
		 * ALGORITHM 1c:
		 * Loop the following
		 *    See how far the player can go in its current direction. If it cannot go far, note that it is stopped.
		 *    If the player is not stopped, move the player appropriately.
		 *    If the player moved its full distance, the algorithm is done.
		 *    
		 *    Otherwise, do the following:
		 *    Update the velocity to be parallel to the wall that stopped the player.
		 *    If the player was unable to move after the velocity update, update the velocity to be parallel to the last two walls that stopped the player.
		 *      (The code that handles this is under "Deal with being in an acute angled corner." Without this, the player would stick on such corners and fail to wall slide.
		 *    If the player collided onto level enough ground, update the inAir flag accordingly.
		 *    Repeat to allow wall sliding to take place.
		 */
		for (int i=0; i<5; i+=1) //Cannot repeat this forever
		{
			double t = w.getCollision().getPlayerCollision(x,y,z,xV*dt*remaining,yV*dt*remaining,zV*dt*remaining,radius,height);
			boolean stopped = (t < 0.001);
			
			if (!stopped)
			{
				x += xV*dt*t*remaining;
				y += yV*dt*t*remaining;
				z += zV*dt*t*remaining;
			}
			
			if (t == 1) break;
			
			double m = -w.getCollision().getNormalX()*xV - w.getCollision().getNormalY()*yV - w.getCollision().getNormalZ()*zV;
			xV += m*w.getCollision().getNormalX(); yV += m*w.getCollision().getNormalY(); zV += m*w.getCollision().getNormalZ();
			
			//Deal with being in an acute angled corner.
			if (normalsReceived && stopped)
			{
				m = -w.getCollision().getNormalX()*nx - w.getCollision().getNormalY()*ny - w.getCollision().getNormalZ()*nz;
				nx += m*w.getCollision().getNormalX(); ny += m*w.getCollision().getNormalY(); nz += m*w.getCollision().getNormalZ();
				double dist = Math.sqrt(sqr(nx) + sqr(ny) + sqr(nz));
				if (dist != 0)
				{
					nx /= dist; ny /= dist; nz /= dist;
					m = -nx*xV - ny*yV - nz*zV;
					xV += m*nx; yV += m*ny; zV += m*nz;
				}
			}
			
			nx = w.getCollision().getNormalX(); ny = w.getCollision().getNormalY(); nz = w.getCollision().getNormalZ(); normalsReceived = true;
			
			//Landing
			if (nz > terrainTolerance && inAir)
			    inAir = false;
			
			remaining *= 1-t;
			
			if (i == 4)
			{
			    xV = 0;
			    yV = 0;
			    zV = 0;
			}
		}
		
		//Cling onto surfaces. If there is a floor under the player close enough to the player, set the player's ground to this surface.
		if (!inAir)
		{
			double t = w.getCollision().getPlayerCollision(x,y,z,0,0,-stepDownHeight,radius,height);
			if (t != 1)
				if (w.getCollision().getNormalZ() >= terrainTolerance)
				{
					z -= t*stepDownHeight;
					floorNormX = w.getCollision().getNormalX();
					floorNormY = w.getCollision().getNormalY();
					floorNormZ = w.getCollision().getNormalZ();
				}
		}
	}
	
	//Returns x^2 (for convenience)
	private double sqr(double x)
	{
		return x*x;
	}
	
	//Changes the speed based on the current surface's normal vector and the intended direction of travel (relative to the player's facing direction).
	/*
	 * ALGORITHM 1d (used in 1b):
	 * Set xGoal and yGoal to the proper speed and direction assuming the ground is flat.
	 * Set zGoal to the proper amount given the slope of the ground.
	 * Multiply xGoal, yGoal, and zGoal to the proper amount to prevent steep slopes from speeding up the player.
	 * 
	 * Move the velocity vector <xV, yV, zV> towards the goal velocity vector <xGoal, yGoal, zGoal> by:
	 * Setting the velocity vector to the goal velocity vector if the velocity vector is close enough.
	 * Adding the velocity vector by a vector that is pointing from the velocity vector to the goal velocity vector and has a magnitude of acceleration otherwise.
	 */
	private void changeSpeed(double dt, double xVel, double yVel, double acceleration)
	{
		//xGoal, yGoal, and zGoal are the intended xV, yV, and zV.
		double xGoal = xVel*maxSpeed;
		double yGoal = yVel*maxSpeed;
		double zGoal;
		
		//Rotate the intended direction vector by the direction the player is facing.
		double temp = xGoal*Math.cos(horizontalDir)-yGoal*Math.sin(horizontalDir);
		yGoal = xGoal*Math.sin(horizontalDir)+yGoal*Math.cos(horizontalDir);
		xGoal = temp;
		
		if (inAir)
			zGoal = 0;
		else
		{
			zGoal = (-xGoal*floorNormX - yGoal*floorNormY) / floorNormZ;
			if (xGoal != 0 || yGoal != 0)
			{
				double mult = Math.sqrt((xGoal*xGoal + yGoal*yGoal) / (xGoal*xGoal + yGoal*yGoal + zGoal*zGoal));
				xGoal *= mult; yGoal *= mult; zGoal *= mult; acceleration *= mult;
			}
		}
		
		double dist = sqr(xGoal-xV) + sqr(yGoal-yV) + sqr(zGoal-zV);
		if (dist <= sqr(acceleration*dt))
		{
			xV = xGoal;
			yV = yGoal;
			zV = zGoal;
		}
		else
		{
			dist = Math.sqrt(dist);
			xV += acceleration*dt*(xGoal-xV)/dist;
			yV += acceleration*dt*(yGoal-yV)/dist;
			zV += acceleration*dt*(zGoal-zV)/dist;
		}
	}
}

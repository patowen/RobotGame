import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Crowd Control Enemy. Fires Plasmabolts from two guns that have negative knockback,
 * drawing entities towards the explosion. Does very low damage, but can slow down the 
 * player and draw other enemies towards them.
 * @author Patrick Owen
 */
public class EnemyGrappler extends Enemy implements Damageable
{
	//Constants
	private double radius;
	private double height;
	private double shotHeight;
	private double shotDistance;
	private double shotSpeed;
	private double shotDelay;
	private double range;
	
	private double shotX;
	private double shotY;
	private double shotZ;
	
	//Controls
	private double horizontalDir;
	private double verticalDir;
	private double charge;
	
	private AITracking ai;
	private boolean flip;
	private double speedspin = 0;//Increased spin of gun arms prior to firing
	private double spin = 0;//Constant spin of gun arms
	
	/**
	 * Creates a new EnemyTurret.
	 * @param controller The active Controller object.
	 * @param world The world where the EnemyTurret is placed.
	 */
	public EnemyGrappler(Controller controller, World world)
	{
		super(controller, world);
		
		radius = 0.5;
		height = 1;
		
		horizontalDir = 3*Math.PI/2;
		verticalDir = 0;
		xV = 0;
		yV = 0;
		zV = 0;
		shotX = .15;
		shotY = 0;
		shotZ = -.25;
		flip = true;
		
		shotDelay = 2;
		shotHeight = 0.4;
		shotDistance = 0.4;
		shotSpeed = 100;
		range = 100;
		
		charge = shotDelay;
		
		ai = new AITracking(c, w, this);
		ai.setControls(5, 1, 6, 9, 6, 1, 1, 1, 1, 3, 1);
		
		hp = 5;
	}
	
	public double getRadius()
	{
		return radius;
	}
	
	public double getHeight()
	{
		return height;
	}
	
	/**
	 * Shoots at the player if he is within range.
	 */
	public void step(double dt)
	{
		super.step(dt);
		handleShooting(dt);
		handleLooking(dt);
		ai.performAI(dt);
		
		if (hp <= 0)
		{
			c.addScore(1000);
			delete();
		}
	}
	
	/**
	 * Modifies the shotX, shotY, and shotZ values so that the 
	 * weapon switches positions each shot
	 */
	private void handleShotOrigin()
	{
		if(flip)
			shotY = .475;
		else
			shotY = -.475;
		flip = !flip;
	}
	
	//Handles the turret firing its bullets
	private void handleShooting(double dt)
	{
		charge -= dt;
		
		if (w.getPlayer().isDead()) return;
		
		if (charge < 0 && noFriendlyFire())
		{
			handleShotOrigin();
			EntityPlasmaBolt beam = (EntityPlasmaBolt)c.createEntity(w, EI.EntityPlasmaBolt);
			double xDir = Math.cos(horizontalDir)*Math.cos(verticalDir),
					yDir = Math.sin(horizontalDir)*Math.cos(verticalDir), zDir = Math.sin(verticalDir);
			
			double xDisp = shotX*Math.cos(verticalDir) - shotZ*Math.sin(verticalDir);
			double zDisp = shotX*Math.sin(verticalDir) + shotZ*Math.cos(verticalDir);
			
			double yDisp = xDisp*Math.sin(horizontalDir) - shotY*Math.cos(horizontalDir);
			xDisp = xDisp*Math.cos(horizontalDir) + shotY*Math.sin(horizontalDir);
			
			double t = w.getCollision().getBulletCollision(x, y, z, xDir*shotDistance+xDisp, yDir*shotDistance+yDisp, zDir*shotDistance+shotHeight+zDisp);
			
			if(t == 1)
			{
				beam.setPosition(x+xDir*shotDistance+xDisp, y+yDir*shotDistance+yDisp, z+zDir*shotDistance+shotHeight+zDisp);
				beam.setVelocity(shotSpeed*xDir, shotSpeed*yDir, shotSpeed*zDir);
				beam.setRange(3);
				beam.setColor(.75f, 0.75f, 1f);
				beam.setDamage(.25, -20);
				beam.setOwner(this);
				
				c.getSoundHandler().playSound(0, x, y, z);
				w.create(beam);
				
				speedspin = 0;
				charge = shotDelay;
			}
		}
	}
	
	//Handles the turret pointing at the player (plus an amount to account for velocity and bullet travel time)
	private void handleLooking(double dt)
	{
		/*
		 * ALGORITHM 5:
		 * Find the difference between the center of the turret and the center of the player.
		 * Add the amount by the player's velocity vector times the amount of time it takes for the bullet to reach the player.
		 * Based on coordinate differences, find the horizontal and vertical directions from the turret to the player.
		 * Modify the horizontal and vertical directions of the turret smoothly to face the player.
		 */
		Player player = w.getPlayer();
		if (player.isDead()) return;
		double xDiff = player.getX()-x, yDiff = player.getY()-y, zDiff = player.getZ()+player.getHeight()/2-z-height/2;
		
		//Account for player velocity
		double xV = player.getXV(), yV = player.getYV(), zV = player.getZV();
		double pDist = Math.sqrt(xDiff*xDiff + yDiff*yDiff + zDiff*zDiff);
		xDiff += xV*pDist/shotSpeed; yDiff += yV*pDist/shotSpeed; zDiff += zV*pDist/shotSpeed;
		
		double xyDiff = Math.sqrt(xDiff*xDiff + yDiff*yDiff);
		
		double playerDir = Math.atan2(yDiff, xDiff);
		double playerVDir = Math.atan2(zDiff, xyDiff);
		
		double dirDifference = playerDir-horizontalDir;
		double vDirDifference = playerVDir-verticalDir;
		
		while (dirDifference < -Math.PI) dirDifference += 2*Math.PI;
		while (dirDifference > Math.PI) dirDifference -= 2*Math.PI; 
		
		//Modify the horizontal and vertical directions of the turret smoothly to face the player.
		if (dirDifference > 0)
			horizontalDir += Math.min(1*dt, dirDifference);
		else
			horizontalDir -= Math.min(1*dt, -dirDifference);
		
		if (vDirDifference > 0)
			verticalDir += Math.min(1*dt, vDirDifference);
		else
			verticalDir -= Math.min(1*dt, -vDirDifference);
	}
	
	//Tests to make sure the turret is not shooting another enemy.
	private boolean noFriendlyFire()
	{
		double xd = range*Math.cos(horizontalDir)*Math.cos(verticalDir),
				yd = range*Math.sin(horizontalDir)*Math.cos(verticalDir), zd = range*Math.sin(verticalDir);
		
		double t = w.getCollision().getBulletCollision(x, y, z+shotHeight, xd, yd, zd);
		double t2 = 1; //Bullet distance traveled before first detected collision
		Damageable entityToDamage = null;
		
		for (Entity entity : w.getEntities())
		{
			if (entity == this) continue;
			if (!(entity instanceof Damageable) || entity == this) continue;
			
			Damageable e = (Damageable) entity;
			
			//tTest must be less than t2 to update it.
			double tTest = w.getCollision().getEntityBulletCollision(x, y, z+shotHeight, xd*t, yd*t, zd*t, e);
			
			if (tTest < t2)
			{
				entityToDamage = e;
				t2 = tTest;
			}
		}
		
		if (t2 == 1) return true;
		
		if (entityToDamage instanceof Player) return true;
		
		return false;
	}
	
	public void draw(GL2 gl)
	{
		GLUT glut = new GLUT();
		speedspin+=(shotDelay-charge)/shotDelay*20;
		spin++;
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0.6f,0.7f,0.9f,1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0.4f,0.4f,0.4f,1}, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 8);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,0,0,1}, 0);
		
		gl.glPushMatrix();
		
		gl.glTranslated(x, y, z+height*.65);
		gl.glRotated(horizontalDir*180/Math.PI + 180, 0, 0, 1);
		gl.glRotated(verticalDir*180/Math.PI, 0, 1, 0);
		
		glut.glutSolidSphere(radius/1.5, 24, 16);
		gl.glTranslated(.1, 0, 0);
		
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0.5f,0.5f,0.6f,1}, 0);
		
		gl.glPushMatrix();
		gl.glRotated(-45, 1, 0, 0);
		gl.glTranslated(0, radius/2.4, 0);
		ModelSawHook.draw(gl);
		gl.glTranslated(-.55, .46, 0);
		gl.glRotated(spin, 1, 0, 0);
		if(flip)
			gl.glRotated(speedspin, 1, 0, 0);
		ModelPlasmaLauncherBig.draw(gl);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glRotated(-135, 1, 0, 0);
		gl.glTranslated(0, radius/2.4, 0);
		ModelSawHook.draw(gl);
		gl.glTranslated(-.55, .46, 0);
		gl.glRotated(-spin, 1, 0, 0);
		if(!flip)
			gl.glRotated(-speedspin, 1, 0, 0);
		ModelPlasmaLauncherBig.draw(gl);
		gl.glPopMatrix();
		
		gl.glPopMatrix();
		
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0,0,0,1}, 0);
	}
	
	
	//Apply damage and knockback. EnemyGrappler is highly resistant to knockback effects
	public void applyDamage(double amount, double x, double y, double z, double knockBack, boolean absolute)
	{
		hp -= amount;
		xV -= .4*knockBack*x/radius; yV -= .4*knockBack*y/radius; zV -= .4*knockBack*z/radius;
	}
}

package patowen95.robotgame.entity.enemy;
import javax.media.opengl.GL2;

import patowen95.robotgame.Controller;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.Damageable;
import patowen95.robotgame.entity.EI;
import patowen95.robotgame.entity.Entity;
import patowen95.robotgame.entity.EntityPlayer;
import patowen95.robotgame.entity.EntityPlayerBasic;
import patowen95.robotgame.entity.ai.AITracking;
import patowen95.robotgame.entity.weapon.EntityBullet;
import patowen95.robotgame.model.ModelTurret;

/**
 * The tracking enemy: it floats at a preferred distance from the player and shoots at him/her, tracking
 * velocity as well as position.
 * @author Patrick Owen
 */
public class EnemyTracking extends Enemy implements Damageable
{
	//Constants
	private double radius;
	private double height;
	private double shotHeight;
	private double shotDistance;
	private double shotSpeed;
	private double shotDelay;
	private double range;
	
	//Controls
	private double horizontalDir;
	private double verticalDir;
	private double charge;
	
	private AITracking ai;
	
	/**
	 * Creates a new EnemyTurret.
	 * @param controller The active Controller object.
	 * @param world The world where the EnemyTurret is placed.
	 */
	public EnemyTracking(Controller controller, World world)
	{
		super(controller, world);
		
		radius = 0.5;
		height = 0.8;
		
		horizontalDir = 3*Math.PI/2;
		verticalDir = 0;
		xV = 0;
		yV = 0;
		zV = 0;
		
		shotDelay = 1.0/2;
		shotHeight = 0.4;
		shotDistance = 0.4;
		shotSpeed = 20;
		range = 100;
		
		charge = shotDelay;
		
		ai = new AITracking(c, w, this);
		ai.setControls(5, 1, 3, 9, 6, 1, 1, 1, 1, 3, 1);
		
		hp = 2;
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
			c.addScore(500);
			delete();
		}
	}
	
	//Handles the turret firing its bullets
	private void handleShooting(double dt)
	{
		charge -= dt;
		
		if (w.getPlayer().isDead()) return;
		
		if (isLocal && charge < 0 && noFriendlyFire())
		{
			EntityBullet bullet = (EntityBullet)c.createEntity(w, EI.EntityBullet);
			double xDir = Math.cos(horizontalDir)*Math.cos(verticalDir),
					yDir = Math.sin(horizontalDir)*Math.cos(verticalDir), zDir = Math.sin(verticalDir);
			bullet.setPosition(x+xDir*shotDistance, y+yDir*shotDistance, z+zDir*shotDistance+shotHeight);
			bullet.setVelocity(shotSpeed*xDir, shotSpeed*yDir, shotSpeed*zDir);
			bullet.setDamage(1, 4);
			bullet.setColor(1, 0.5f, 0f);
			bullet.setOwner(this);
			
			c.getSoundHandler().playSound(0, x, y, z);
			w.create(bullet);
			
			charge = shotDelay;
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
		EntityPlayerBasic player = w.getPlayer();
		if (player.isDead()) return;
		double xDiff = player.getTargetX()-x, yDiff = player.getTargetY()-y, zDiff = player.getTargetZ()-z-height/2;
		
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
		
		if (entityToDamage instanceof EntityPlayer) return true;
		
		return false;
	}
	
	public void draw(GL2 gl)
	{
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0.9f,0.8f,0.8f,1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0.4f,0.4f,0.4f,1}, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 8);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,0,0,1}, 0);
		
		gl.glPushMatrix();
		
		gl.glTranslated(x, y, z+shotHeight);
		gl.glRotated(horizontalDir*180/Math.PI + 180, 0, 0, 1);
		gl.glRotated(verticalDir*180/Math.PI, 0, 1, 0);
		
		ModelTurret.draw(gl);
		
		gl.glPopMatrix();
		
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0,0,0,1}, 0);
	}

	public void applyDamage(double amount, double x, double y, double z, double knockBack, boolean absolute)
	{
		hp -= amount;
		xV -= 4*knockBack*x/radius; yV -= 4*knockBack*y/radius; zV -= 4*knockBack*z/radius;
	}
}

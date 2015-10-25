package patowen95.robotgame.entity.enemy;
import com.jogamp.opengl.GL2;

import patowen95.robotgame.Controller;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.Collidable;
import patowen95.robotgame.entity.Damageable;
import patowen95.robotgame.entity.EI;
import patowen95.robotgame.entity.Entity;
import patowen95.robotgame.entity.Player;
import patowen95.robotgame.entity.weapon.EntityBullet;
import patowen95.robotgame.model.ModelTurret;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * The turret enemy: it aims and shoots at the player.
 * @author Patrick Owen
 * @author Michael Ekstrom
 */
public class EnemyTurret extends Enemy implements Collidable, Damageable
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
	
	/**
	 * Creates a new EnemyTurret.
	 * @param controller The active Controller object.
	 * @param world The world where the EnemyTurret is placed.
	 */
	public EnemyTurret(Controller controller, World world)
	{
		super(controller, world);
		
		radius = 0.5;
		height = 0.8;
		
		horizontalDir = 3*Math.PI/2;
		verticalDir = 0;
		
		shotDelay = 1.0/2;
		shotHeight = 0.5;
		shotDistance = 0.4;
		shotSpeed = 20;
		range = 100;
		
		charge = shotDelay;
		
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
	
	public void step(double dt)
	{
		super.step(dt);
		
		charge -= dt;
		
		if (isLocal && charge < 0 && noFriendlyFire())
		{
			EntityBullet bullet = (EntityBullet)c.createEntity(w, EI.EntityBullet);
			double xDir = Math.cos(horizontalDir)*Math.cos(verticalDir),
					yDir = Math.sin(horizontalDir)*Math.cos(verticalDir), zDir = Math.sin(verticalDir);
			bullet.setPosition(x+xDir*shotDistance, y+yDir*shotDistance, z+zDir*shotDistance+shotHeight);
			bullet.setVelocity(shotSpeed*xDir, shotSpeed*yDir, shotSpeed*zDir);
			bullet.setDamage(2, 4);
			bullet.setColor(1, 0.8f, 0.6f);
			bullet.setOwner(this);
			
			c.getSoundHandler().playSound(0, x, y, z);
			w.create(bullet);
			
			charge = shotDelay;
		}
		
		Player player = w.getPlayer();
		double xDiff = player.getX()-x, yDiff = player.getY()-y, zDiff = player.getZ()-z;
		double xyDiff = Math.sqrt(xDiff*xDiff + yDiff*yDiff);
		
		double playerDir = Math.atan2(yDiff, xDiff);
		double playerVDir = Math.atan2(zDiff, xyDiff);
		
		double dirDifference = playerDir-horizontalDir;
		double vDirDifference = playerVDir-verticalDir;
		
		while (dirDifference < -Math.PI) dirDifference += 2*Math.PI;
		while (dirDifference > Math.PI) dirDifference -= 2*Math.PI; 
		
		if (dirDifference > 0)
			horizontalDir += Math.min(dt, dirDifference);
		else
			horizontalDir -= Math.min(dt, -dirDifference);
		
		if (vDirDifference > 0)
			verticalDir += Math.min(dt, vDirDifference);
		else
			verticalDir -= Math.min(dt, -vDirDifference);
		
		if (hp <= 0)
		{
			c.addScore(100);
			delete();
		}
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
		
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {1,1,1,1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,0,0,1}, 0);
		
		gl.glPushMatrix();
		gl.glTranslated(x, y, z);
		glut.glutSolidCylinder(0.05, 0.5, 12, 1);
		
		gl.glTranslated(0, 0, 0.5);
		gl.glRotated(horizontalDir*180/Math.PI + 180, 0, 0, 1);
		gl.glRotated(verticalDir*180/Math.PI, 0, 1, 0);
		
		ModelTurret.draw(gl);
		
		gl.glPopMatrix();
	}

	public void applyDamage(double amount, double x, double y, double z, double knockBack, boolean absolute)
	{
		hp -= amount;
	}
}

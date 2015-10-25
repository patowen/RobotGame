package patowen95.robotgame.entity.weapon;
import com.jogamp.opengl.GL2;

import patowen95.robotgame.Collision;
import patowen95.robotgame.Controller;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.Damageable;
import patowen95.robotgame.entity.EI;
import patowen95.robotgame.entity.Entity;
import patowen95.robotgame.entity.effect.EntityExplosion;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * This class will holds a missile entity that explodes on contact with
 * anything and damages nearby entities
 * @author Patrick Owen
 * @author Michael Ekstrom
 */
public class EntityRocket extends Entity implements Damageable
{
	private double radius;
	private double height;
	
	private double fireFrequency; //Frequency that the EnemyRocket exhausts an EntityExplosion
	private double fireTime; //Keeps track of time between explosion exhaust
	private double acceleration;
	private double friction;
	
	private double range;
	private double damage;
	private double maxDamage;
	private double knockback;
	
	private Damageable target;
	private Entity owner;
	
	private boolean isDestroyed;
	
	/**
	 * Creates a new EnemyRocket object. 
	 * @param c Controller object, passed to superclass
	 * @param world World object, passed to superclass
	 */
	public EntityRocket(Controller c, World world)
	{
		super(c, world);
		
		radius = .15;
		height = .3;
		fireFrequency = .05;
		fireTime = 0;
		acceleration = 15;
		friction = 1.5;
		
		range = 4;
		damage = 16;
		maxDamage = 9;
		knockback = 20;
		
		isDestroyed = false;
	}
	
	/**
	 * Sets the target that the rocket is aiming for to the specified entity.
	 * @param newTarget
	 */
	public void setTarget(Damageable newTarget)
	{
		target = newTarget;
	}
	
	/**
	 * Handles the exhaust trail
	 */
	public void handleExhaust(double dt)
	{
		fireTime -=dt;
		if (fireTime <=0)
		{
			fireTime = fireFrequency;
			EntityExplosion exhaust = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
			exhaust.setPosition(x, y, z + height/2);
			exhaust.setColor(.1f, 0f, 0f);
			exhaust.setRadius(1.1*radius);
			exhaust.setFinalRadius(2*radius);
			exhaust.setDuration(.25);
			w.create(exhaust);
		}
	}

	/**
	 * Sets the owner of the rocket. The owner will not be hit by the rocket until a certain timeout
	 * @param newOwner
	 */
	public void setOwner(Entity newOwner)
	{
		owner = newOwner;
	}
	
	/**
	 * called to remove the EntityRocket. No score should be given.
	 */
	public void destroy(boolean applyDamage)
	{
		if (isDestroyed) return;
		
		isDestroyed = true;
		
		EntityExplosion blast = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
		blast.setColor(.75f, .25f, .1f);
		blast.setDuration(.5);
		blast.setPosition(x, y, z + radius/2);
		blast.setRadius(radius);
		blast.setFinalRadius(range);
		w.create(blast);
		
		if (applyDamage)
		{
			for (Entity entity : w.getEntities())
			{
				if (entity == owner || entity == this) continue;
				if (!(entity instanceof Damageable)) continue;
				
				Damageable e = (Damageable) entity;
				
				double xDiff = x-e.getX();
				double yDiff = y-e.getY();
				double zDiff = z+height/2-e.getZ()-e.getHeight()/2;
				double distSqr = xDiff*xDiff + yDiff*yDiff + zDiff*zDiff;
				
				if (distSqr < range*range)
				{
					double dist = Math.sqrt(distSqr);
					double totalDamage = damage/distSqr;
					double totalKB = knockback/distSqr;
					
					if (totalDamage > maxDamage) totalDamage = maxDamage;
					if(Math.abs(totalKB) > Math.abs(knockback)) totalKB = knockback;
					e.applyDamage(totalDamage, xDiff/dist, yDiff/dist, zDiff/dist, totalKB, false);
				}
			}
		}
		
		delete();
	}
	
	/**
	 * Modifies the entity appropriately. 
	 * @param dt Length of the step
	 */
	public void step(double dt)
	{		
		handleExhaust(dt);
		handleMovement(dt);
		handleCollision(dt);
	}
	
	//Accelerates the rocket appropriately and handles hitting the wall.
	private void handleMovement(double dt)
	{
		if (target != null)
		{
			double xDiff = target.getX()-x;
			double yDiff = target.getY()-y;
			double zDiff = (target.getZ()+target.getHeight()/2)-(z+height/2);
			double diff = Math.sqrt(xDiff*xDiff + yDiff*yDiff + zDiff*zDiff);
			
			if (diff != 0)
			{
				xDiff /= diff; yDiff /= diff; zDiff /= diff;
				xV -= friction*xV*dt; yV -= friction*yV*dt; zV -= friction*zV*dt;
				xV += acceleration*xDiff*dt; yV += acceleration*yDiff*dt; zV += acceleration*zDiff*dt;
			}
		}
	}
	
	//Handles the rocket making contact with anything
	private void handleCollision(double dt)
	{
		super.step(dt);
		Collision col = w.getCollision();
		
		double t = col.getPlayerCollision(x, y, z, xV*dt, yV*dt, zV*dt, radius, height);
		double t2 = 1; //Bullet distance traveled before first detected collision
		Damageable entityToDamage = null;
		
		for (Entity entity : w.getEntities())
		{
			if (entity == owner || entity == this) continue;
			if (!(entity instanceof Damageable)) continue;
			
			Damageable e = (Damageable) entity;
			
			//tTest must be less than t2 to update it.
			double tTest = col.getEntityBulletCollision(x, y, z, xV*t*dt, yV*t*dt, zV*t*dt, e);
			
			if (tTest < t2)
			{
				entityToDamage = e;
				t2 = tTest;
			}
		}
		
		//Rocket hits entity.
		if (entityToDamage != null)
		{
//			double totalVel = Math.sqrt(xV*xV + yV*yV + zV*zV);
//			entityToDamage.applyDamage(damage, -xV/totalVel, -yV/totalVel, -zV/totalVel, knockBack, false);
		}
		
		//Update rocket location
		x += xV*t*t2*dt;
		y += yV*t*t2*dt;
		z += zV*t*t2*dt;
		
		//Rocket hits anything
		if (t2 < 1 || t < 1)
			destroy(true);
	}
	
	public void draw(GL2 gl)
	{
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0.6f,0.6f,0.6f,1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0.8f,0.8f,0.8f,1}, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 8);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,0,0,1}, 0);
		
		gl.glPushMatrix();
		
		gl.glTranslated(x, y, z+height/2);
		
		GLUT glut = new GLUT();
		glut.glutSolidSphere(radius, 24, 8);
		
		gl.glPopMatrix();
		
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0,0,0,1}, 0);
	}
	
	public void draw2(GL2 gl)
	{
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0,0,0,(float)(0.25+0.1*Math.random())}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {.5f,.25f,0,1}, 0);
		
		gl.glPushMatrix();
		
		gl.glTranslated(x, y, z+height/2);
		
		GLUT glut = new GLUT();
		glut.glutSolidSphere(radius+.02, 24, 8);
		
		gl.glPopMatrix();
	}

	public double getRadius()
	{
		return radius;
	}

	public double getHeight()
	{
		return height;
	}

	public void applyDamage(double amount, double x, double y, double z, double knockBack, boolean absolute)
	{
		destroy(false);
	}
}

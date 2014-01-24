import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * The PlasmaBolt weapon. Fires an EntityPlasmaBolt that explodes upon contact with anything
 * @author Patrick Owen
 * @author Michael Ekstrom
 */
public class EntityPlasmaBolt extends Entity
{
	private Entity owner;
	
	private double radius, radius2;
	private double damage;
	private double knockback;
	private double range;
	private double maxDamage;
	private double fireFrequency; //Frequency that the EnemyRocket exhausts an EntityExplosion
	private double fireTime; //Keeps track of time between explosion exhaust
	
	private boolean isDestroyed;
	
	private float[] color;
	
	/**
	 * Creates a new EntityBullet.
	 * @param controller The active Controller object.
	 * @param world The world where the EntityBullet is placed.
	 */
	public EntityPlasmaBolt(Controller controller, World world)
	{
		super(controller, world);
		
		radius = 0.1; radius2 = 0.2;
		damage = 5;
		knockback = 5;
		xV = 0;
		yV = 0;
		zV = 0;
		
		color = new float[4];
		isDestroyed = false;
		range = 6;
		maxDamage = damage;
	}
	
	/**
	 * Sets the owner of the bullet, which determines which entity cannot
	 * be damaged by the bullet.
	 * @param e The specified owner
	 */
	public void setOwner(Entity e)
	{
		owner = e;
	}
	
	/**
	 * Sets the damage and knock back of the bullet.
	 * @param newDamage damage
	 * @param newKnockBack knock back
	 */
	public void setDamage(double newDamage, double newKnockBack)
	{
		damage = newDamage;
		maxDamage = damage;
		knockback = newKnockBack;
	}
	
	/**
	 * Sets the range of the blast of the PlasmaBolt
	 * @param newRange range
	 */
	public void setRange(double newRange)
	{
		range = newRange;
	}
	
	/**
	 * Sets the color of the bullet
	 * @param red
	 * @param green
	 * @param blue RGB color (Each value 0-1)
	 */
	public void setColor(float red, float green, float blue)
	{
		color[0] = red; color[1] = green; color[2] = blue;
	}
	
	public void step(double dt)
	{
		super.step(dt);
		handleExhaust(dt);
		
		double t = w.getCollision().getBulletCollision(x, y, z, xV*dt, yV*dt, zV*dt);
		double t2 = 1; //Bullet distance traveled before first detected collision
		Damageable entityToDamage = null;
		
		for (Entity entity : w.getEntities())
		{
			if (entity == owner) continue;
			if (!(entity instanceof Damageable)) continue;
			
			Damageable e = (Damageable) entity;
			
			//tTest must be less than t2 to update it.
			double tTest = w.getCollision().getEntityBulletCollision(x, y, z, xV*t*dt, yV*t*dt, zV*t*dt, e);
			
			if (tTest < t2)
			{
				entityToDamage = e;
				t2 = tTest;
			}
		}
		
		//Bullet hits entity.
		if (entityToDamage != null)
		{
			double totalVel = Math.sqrt(xV*xV + yV*yV + zV*zV);
			entityToDamage.applyDamage(damage, -xV/totalVel, -yV/totalVel, -zV/totalVel, knockback, false);
		}
		
		//Update bullet location
		x += xV*t*t2*dt;
		y += yV*t*t2*dt;
		z += zV*t*t2*dt;
		
		//Bullet hits something.
		if (t2 < 1 || t < 1)
		{
			destroy();
		}
	}
	
	/**
	 * called to remove the EntityRocket. No score should be given.
	 */
	public void destroy()
	{
		if (isDestroyed) return;
		
		isDestroyed = true;
		
		EntityFade blast = (EntityFade)c.createEntity(w, EI.EntityFade);
		blast.setColor(color[0], color[1], color[2]);
		blast.setDuration(1);
		blast.setPosition(x, y, z + radius/2);
		blast.setRadius(range);
		w.create(blast);
		
		for (Entity entity : w.getEntities())
		{
			if (entity == this) continue;
			if (!(entity instanceof Damageable)) continue;
			
			Damageable e = (Damageable) entity;
			
			double xDiff = x-e.getX();
			double yDiff = y-e.getY();
			double zDiff = z+radius/2-e.getZ()-e.getRadius()/2;
			double distSqr = xDiff*xDiff + yDiff*yDiff + zDiff*zDiff;
			
			if (distSqr < range*range)
			{
				double dist = Math.sqrt(distSqr);
				double totalDamage = damage/distSqr;
				double totalKB = knockback/distSqr;
				
				if(Math.abs(totalKB) > Math.abs(knockback)) totalKB = knockback;
				if (totalDamage > maxDamage) totalDamage = maxDamage;
				e.applyDamage(totalDamage, xDiff/dist, yDiff/dist, zDiff/dist, totalKB, false);
			}
		}
		
		delete();
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
			EntityFade trail = (EntityFade)c.createEntity(w, EI.EntityFade);
			trail.setPosition(x, y, z);
			trail.setColor(color[0], color[1], color[2]);
			trail.setRadius(radius2);
			trail.setDuration(.25);
			w.create(trail);
		}
	}
	
	public void draw(GL2 gl)
	{
		GLUT glut = new GLUT();
		
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0,0,0,1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, color, 0);
		
		gl.glPushMatrix();
		gl.glTranslated(x, y, z);
		glut.glutSolidSphere(radius, 8, 8);
		gl.glPopMatrix();
	}
	
	public void draw2(GL2 gl)
	{
		GLUT glut = new GLUT();
		
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0,0,0,0.3f}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, color, 0);
		
		gl.glPushMatrix();
		gl.glTranslated(x, y, z);
		glut.glutSolidSphere(radius2, 24, 8);
		gl.glPopMatrix();
	}
}

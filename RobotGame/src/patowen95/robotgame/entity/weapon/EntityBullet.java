package patowen95.robotgame.entity.weapon;
import com.jogamp.opengl.GL2;

import patowen95.robotgame.Controller;
import patowen95.robotgame.NetworkPacket;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.Damageable;
import patowen95.robotgame.entity.EI;
import patowen95.robotgame.entity.Entity;
import patowen95.robotgame.entity.effect.EntityExplosion;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * The bullet entity - travels in a straight line until it hits a wall or another entity.
 * Its collision mask is a point.
 * @author Patrick
 */
public class EntityBullet extends Entity
{
	private Entity owner;
	
	private double radius, radius2;
	private double damage;
	private double knockBack;
	
	private float[] color;
	
	/**
	 * Creates a new EntityBullet.
	 * @param controller The active Controller object.
	 * @param world The world where the EntityBullet is placed.
	 */
	public EntityBullet(Controller controller, World world)
	{
		super(controller, world);
		
		radius = 0.05; radius2 = 0.1;
		damage = 0;
		knockBack = 0;
		xV = 0;
		yV = 0;
		zV = 0;
		
		color = new float[4];
		color[3] = 1;
	}
	
	protected void readState(NetworkPacket data)
	{
		super.readState(data);
		
		radius = data.getDouble();
		radius2 = data.getDouble();
		damage = data.getDouble();
		knockBack = data.getDouble();
		color[0] = data.getFloat();
		color[1] = data.getFloat();
		color[2] = data.getFloat();
	}
	
	protected void writeState(NetworkPacket data)
	{
		super.writeState(data);
		
		data.addDoubles(radius, radius2, damage, knockBack);
		data.addFloats(color[0], color[1], color[2]);
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
		knockBack = newKnockBack;
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
		
		if (isLocal)
		{
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
				entityToDamage.applyDamage(damage, -xV/totalVel, -yV/totalVel, -zV/totalVel, knockBack, false);
			}
			
			//Update bullet location
			x += xV*t*t2*dt;
			y += yV*t*t2*dt;
			z += zV*t*t2*dt;
			
			//Bullet hits something.
			if (t2 < 1 || t < 1)
			{
				delete();
				
				if (entityToDamage == null)
					explodeOnWall();
				else if (!(entityToDamage == w.getPlayer()))
					explodeOnEntity();
			}
		}
	}
	
	//Creates a small spark-like explosion to make it obvious that the bullet hit the wall.
	private void explodeOnWall()
	{
		EntityExplosion blast = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
		blast.setPosition(x, y, z);
		blast.setRadius(radius);
		blast.setFinalRadius(radius*4);
		blast.setDuration(.1);
		blast.setColor(color[0], color[1], color[2]);
		w.create(blast);
		c.getSoundHandler().playSound(2, x, y, z);
	}
	
	//Creates a larger explosion to make it look like an entity was damaged.
	private void explodeOnEntity()
	{
		EntityExplosion blast = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
		blast.setPosition(x, y, z);
		blast.setRadius(radius);
		blast.setFinalRadius(radius*10);
		blast.setDuration(.25);
		blast.setColor(1, .5f, .25f);
		c.getSoundHandler().playSound(1, x, y, z);
		w.create(blast);
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

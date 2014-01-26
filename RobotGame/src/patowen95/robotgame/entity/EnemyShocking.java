package patowen95.robotgame.entity;
import javax.media.opengl.GL2;

import patowen95.robotgame.Controller;
import patowen95.robotgame.World;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * It tracks the player and shocks him if he gets too close.
 * @author Patrick Owen
 */
public class EnemyShocking extends Enemy implements Damageable
{
	//Constants
	/**
	 * Dimensions of the EnemyShocking.
	 */
	protected double radius, height;
	private double shotDelay;
	
	//Controls
	/**
	 * AI object that lets the enemy track the player.
	 */
	protected AITracking ai;
	private double charge;
	
	//Determines how large and opaque the transparent sphere around the enemy
	//is
	private double anger;
	
	/**
	 * Creates a new EnemyShocking.
	 * @param controller The active Controller object.
	 * @param world The world where the EnemyTurret is placed.
	 */
	public EnemyShocking(Controller controller, World world)
	{
		super(controller, world);
		
		radius = 0.2;
		height = 0.4;
		
		ai = new AITracking(c, w, this);
		ai.setControls(7, 2, 0, 0, 6, 1, 0.4, 1, 0, 0, 1);
		
		shotDelay = 1.0/2;
		
		charge = shotDelay;
		anger = 0;
		
		hp = 1;
	}
	
	/**
	 * Sets the radius of the shocking enemy to the specified value and modifies
	 * the height to fit.
	 * @param r Radius
	 */
	public void setRadius(double r)
	{
		radius = r;
		height = r*2;
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
	 * Shocks the player if he is within range.
	 */
	public void step(double dt)
	{
		super.step(dt);
		
		anger -= 2*dt;
		if (anger<0)
		{
			anger = 0;
		}
		
		if (isLocal)
			handleShocking(dt);
		ai.performAI(dt);
		
		if (hp <= 0)
			destroy();
	}
	
	public void destroy()
	{
		c.addScore(200);
		delete();
	}
	
	//Handles the enemy shocking the player
	private void handleShocking(double dt)
	{		
		charge -= dt;
		
		if (charge < 0)
		{
			for (Entity e : w.getEntities())
			{
				if (!(e instanceof Player))
					continue;
				Player player = (Player)e;
				if (player.isGhost()) continue;
				double xDiff = player.getX()-x, yDiff = player.getY()-y, zDiff = player.getZ()+player.getHeight()/2 - z - height/2;
				
				if (xDiff*xDiff + yDiff*yDiff + zDiff*zDiff < radius+1)
				{
					player.applyDamage(2, -xDiff, -yDiff, -zDiff, 8, false);
					xV -= 16*xDiff; yV -= 16*yDiff; zV -= 16*zDiff;
					charge = shotDelay;
					anger = 1;
				}
			}
		}
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
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0,0,0,(float)(0.1+0.1*Math.random()+0.6*anger)}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,1,1,1}, 0);
		
		gl.glPushMatrix();
		
		gl.glTranslated(x, y, z+height/2);
		
		GLUT glut = new GLUT();
		glut.glutSolidSphere(radius+0.05+0.2*anger, 24, 8);
		
		gl.glPopMatrix();
	}
	
	public void applyDamage(double amount, double x, double y, double z, double knockBack, boolean absolute)
	{
		hp -= amount;
		xV -= 4*knockBack*x/radius; yV -= 4*knockBack*y/radius; zV -= 4*knockBack*z/radius;
	}
}

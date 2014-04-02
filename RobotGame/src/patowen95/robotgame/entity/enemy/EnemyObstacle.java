package patowen95.robotgame.entity.enemy;
import javax.media.opengl.GL2;

import patowen95.robotgame.Controller;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.Damageable;
import patowen95.robotgame.entity.EntityPlayer;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * The obstacle enemy: it shocks the player if the player gets too close.
 * @author Patrick Owen
 */
public class EnemyObstacle extends Enemy implements Damageable
{
	//Constants
	private double radius;
	private double height;
	
	/**
	 * Creates a new EnemyObstacle.
	 * @param controller The active Controller object.
	 * @param world The world where the EnemyObstacle is placed.
	 */
	public EnemyObstacle(Controller controller, World world)
	{
		super(controller, world);
		
		radius = 0.3;
		height = 0.8;
		
		hp = 5;
	}
	
	public void step(double dt)
	{
		super.step(dt);
		
//		EntityPlayer player = w.getPlayer();
//		double pX = player.getX(), pY = player.getY(), pZ = player.getZ(), pR = player.getRadius(), pH = player.getHeight();
//		
//		if (z+height >= pZ && z <= pZ+pH) //Check for z being in bounds
//		{
//			if ((x-pX)*(x-pX) + (y-pY)*(y-pY) < radius+pR) //Check for x and y being in bounds
//			{
//				double xFrom, yFrom, zFrom, fromMag;
//				
//				xFrom = x-pX; yFrom = y-pY; zFrom = (z+height/2)-(pZ+pH/2);
//				fromMag = Math.sqrt(xFrom*xFrom + yFrom*yFrom + zFrom*zFrom);
//				xFrom /= fromMag; yFrom /= fromMag; zFrom /= fromMag;
//				
//				player.applyDamage(1, xFrom, yFrom, zFrom, 4, true);
//			}
//		}
//		
//		if (hp <= 0) delete();
	}
	
	public void draw(GL2 gl)
	{
		GLUT glut = new GLUT();
		
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0.7f,0.7f,0.7f,1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,0.7f,0.7f,1}, 0);
		
		gl.glPushMatrix();
		gl.glTranslated(x, y, z);
		glut.glutSolidCylinder(radius, height-radius, 24, 8);
		gl.glTranslated(0, 0, height-radius);
		glut.glutSolidSphere(radius, 24, 8);
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
		hp -= amount;
	}
}

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * This class will handle and create the graphics for a non-damaging explosion effect based on size and duration parameters
 * @author Michael Ekstrom
 */
public class EntityFade extends Entity
{
	private double radius;
	private double duration;
	private double life;//Time this has existed
	
	private float[] color;
	
	//Initializes a new EntityExplosion object
	public EntityFade(Controller controller, World world)
	{
		super(controller, world);
		radius = 0.0;
		duration = 5;
		life = duration;
		
		color = new float[4];
		color[3] = 1;
		
	}
	
	/**
	 * Sets the current radius of the explosion
	 */
	public void setRadius(double r)
	{
		radius = r;
	}
	
	
	/**
	 * Sets the time the explosion will persist
	 */
	public void setDuration(double d)
	{
		duration = d;
		life = duration;
	}
	
	/**
	 * Sets the color of the explosion
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
		life-=dt;
		if (life <= 0)
			delete();
	}
	
	public void draw2(GL2 gl)
	{
		GLUT glut = new GLUT();
		
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0,0,0,(float)(life/duration*(Math.random()+1)/2)}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, color, 0);
		
		gl.glPushMatrix();
		gl.glTranslated(x, y, z);
		glut.glutSolidSphere(radius*(Math.random()+.5)/1.5, 16, 16);
		gl.glPopMatrix();
	}
}

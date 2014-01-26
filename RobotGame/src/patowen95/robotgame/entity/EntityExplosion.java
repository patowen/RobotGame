package patowen95.robotgame.entity;
import javax.media.opengl.GL2;

import patowen95.robotgame.Controller;
import patowen95.robotgame.NetworkPacket;
import patowen95.robotgame.World;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * This class will handle and create the graphics for a non-damaging explosion effect based on size and duration parameters
 * @author Michael Ekstrom
 */
public class EntityExplosion extends Entity
{
	private double radius, finalRadius;
	private double duration;
	private double radiusStep;
	
	private float[] color;
	
	//Initializes a new EntityExplosion object
	public EntityExplosion(Controller controller, World world)
	{
		super(controller, world);
		radius = 0.0; 
		finalRadius = 1.0;
		duration = 5;
		radiusStep = (finalRadius-radius)/duration;
		
		color = new float[4];
		color[3] = 1;
	}
	
	protected void readState(NetworkPacket data)
	{
		super.readState(data);
		
		radius = data.getDouble();
		finalRadius = data.getDouble();
		duration = data.getDouble();
		radiusStep = data.getDouble();
		color[0] = data.getFloat();
		color[1] = data.getFloat();
		color[2] = data.getFloat();
	}
	
	protected void writeState(NetworkPacket data)
	{
		super.writeState(data);
		
		data.addDoubles(radius, finalRadius, duration, radiusStep);
		data.addFloats(color[0], color[1], color[2]);
	}
	
	/**
	 * Sets the current radius of the explosion
	 */
	public void setRadius(double r)
	{
		radius = r;
		radiusStep = (finalRadius-radius)/duration;
	}
	
	
	/**
	 * Sets the final radius of the explosion
	 */
	public void setFinalRadius(double fr)
	{
		finalRadius = fr;
		radiusStep = (finalRadius-radius)/duration;
	}
	
	/**
	 * Sets the time the explosion will persist
	 */
	public void setDuration(double d)
	{
		duration = d;
		radiusStep = (finalRadius-radius)/duration;
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
		radius+=radiusStep*dt;
		if ((radiusStep > 0) == (radius >= finalRadius))
			delete();
	}
	
	public void draw2(GL2 gl)
	{
		GLUT glut = new GLUT();
		
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0,0,0,0.3f}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, color, 0);
		
		gl.glPushMatrix();
		gl.glTranslated(x, y, z);
		glut.glutSolidSphere(radius*.25, 8, 8);
		glut.glutSolidSphere(radius*.5, 8, 8);
		glut.glutSolidSphere(radius * .75, 24, 8);
		glut.glutSolidSphere(radius, 24, 8);
		gl.glPopMatrix();
	}
}

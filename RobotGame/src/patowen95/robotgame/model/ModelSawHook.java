package patowen95.robotgame.model;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;


/**
 * This class holds static methods that initialize and draws a hook.
 * @author Michael Ekstrom
 * @author Patrick Owen
 */
public class ModelSawHook
{
	
	//Hook constants
	private static int hstacks;
	private static int hslices;
	private static float hbigrad;
	private static float hsmallrad;
	private static float harc;
	
	private static FloatBuffer hookpts;
	private static FloatBuffer hooknormals;
	
	
	/*
	 * The normals are generated by using the gradient vector at a given point on the ellipse or sphere.
	 */
	
	/**
	 * Initializes the hook model and allows all classes to use it
	 * @param gl
	 */
	public static void init(GL2 gl)
	{
		/*
		 * ALGORITHM 3:
		 * Create float buffers to store vertices and normals (for lighting).
		 * The model itself is generated along the confines of a sphere, using spherical coordinates.
		 * After a certain phiCutoff value, the ellipse ends and the model starts adding points in a concave sphere to create the gun's muzzle.
		 */
		
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		
		
		hstacks = 20;
		hslices = 20;
		hbigrad = .5f;
		hsmallrad = .1f;
		harc = .75f;
		
		hookpts = Buffers.newDirectFloatBuffer(6*hstacks*hslices);
		hooknormals = Buffers.newDirectFloatBuffer(6*hstacks*hslices);
		
		
		for(int i = 0; i < hstacks; i++)
		{
			double ttheta = i* Math.PI/hstacks*harc; //theta around the torus semicircle
			
			for(int j = 0; j < hslices; j++)
			{
				double htheta = 2*j*Math.PI/hslices;//theta around center of hook
				storePointHook(ttheta, htheta, gl);
				storePointHook(ttheta + Math.PI/hstacks, htheta, gl);
				
			}
		}
	}
	
	//Creates and stores a point for the hook
	private static void storePointHook(double ttheta, double htheta, GL2 gl)
	{
		double small = (ttheta/(harc*1.025)-Math.PI)/-Math.PI * hsmallrad;
		float x = (float)((hbigrad+small*Math.cos(htheta))*Math.cos(ttheta));
		float y = (float)((hbigrad+small*Math.cos(htheta))*Math.sin(ttheta));
		float z = (float)(small*Math.sin(htheta));
		
		hookpts.put(x);
		hookpts.put(y);
		hookpts.put(z);
		
		small = hsmallrad;
		x = (float)((hbigrad+small*Math.cos(htheta))*Math.cos(ttheta));
		y = (float)((hbigrad+small*Math.cos(htheta))*Math.sin(ttheta));
		z = (float)(small*Math.sin(htheta));
		
		double temp = Math.sqrt(x*x + y*y);
		float xnorm = (float)(-2*(hbigrad - temp)*x/temp);
		float ynorm = (float)(-2*(hbigrad - temp)*y/temp);
		float znorm = 2*z;
		hooknormals.put(xnorm);
		hooknormals.put(ynorm);
		hooknormals.put(znorm);
	}
	
	
	/**
	 * Draws the hook model with the coordinate axes centered on the base
	 * @param gl
	 */
	public static void draw(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		
		gl.glTranslated(-hbigrad, 0, 0);
		
		hookpts.rewind();
		hooknormals.rewind();
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, hookpts);
		gl.glNormalPointer(GL.GL_FLOAT, 0, hooknormals);
		gl.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, 2*hslices*hstacks);
		
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glPopMatrix();
	}
	
}
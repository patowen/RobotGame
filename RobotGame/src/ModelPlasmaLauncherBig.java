import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;


/**
 * This class holds static methods that initialize and draw the plasma launcher model,
 * used by player entities.
 * @author Michael Ekstrom
 * @author Patrick Owen
 */
public class ModelPlasmaLauncherBig
{
	//Gun Barrel constants
	private static int stacks;
	private static int orbStacks;
	private static int slices;
	private static float length;
	private static float width;
	private static double phiCutoff;
	
	private static FloatBuffer xyzpts;
	private static FloatBuffer normals;
	
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
	 * Initializes the gun model and allows all gun objects to use them.
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
		
		stacks = 20; orbStacks = 10;
		slices = 20; phiCutoff = .775;
		
		length = 1f;//length of the ellipse
		width = 0.3f;//maximum width of the ellipse
		
		xyzpts = Buffers.newDirectFloatBuffer(6*(stacks+orbStacks)*(slices+1));
		normals = Buffers.newDirectFloatBuffer(6*(stacks+orbStacks)*(slices+1));
		
		//Outer shell
		for(int i=0; i<stacks; i++)
		{
			double phi = i*Math.PI*phiCutoff/stacks;
			
			for (int j=0; j<=slices; j++)
			{							
				double theta = j*2*Math.PI/slices;

				storePointOuter(phi+Math.PI*phiCutoff/stacks, theta, gl);
				storePointOuter(phi, theta, gl);
					
			}//end for loop: slices
		}//end for loop: stacks
		
		//Inner cavity
		double phiEnd = Math.PI*phiCutoff;
		double center = Math.cos(phiEnd) * length/2;
		double radius = Math.sin(phiEnd) * width/2;
		
		for (int i = orbStacks; i > 0; i--)
		{
			double phi = i*Math.PI/orbStacks/2;
			
			for (int j=0; j<=slices; j++)
			{							
				double theta = j*2*Math.PI/slices;

				storePointInner(phi-Math.PI/orbStacks/2, theta, center, radius, gl);
				storePointInner(phi, theta, center, radius, gl);
					
			}//end for loop: slices
		}
		
		hstacks = 20;
		hslices = 20;
		hbigrad = .225f;
		hsmallrad = .05f;
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
	
	// Creates and stores a point for the gun. The normals are generated by using the gradient vector at a given point on the ellipse.
	private static void storePointOuter(double phi, double theta, GL2 gl)
	{
		float x = (float)Math.cos(phi) * (length/2);
		float radius = (float) Math.sin(phi) * (width/2);
		float y = (float)Math.sin(theta) * radius;
		float z = (float)Math.cos(theta) * radius;
		
		//Find the gradient vector.
		float nx = 2 * x / (length/2 * length/2);
		float ny = 2 * y / (width/2 * width/2);
		float nz = 2 * z / (width/2 * width/2);
		
		float n = (float)Math.sqrt(nx*nx + ny*ny + nz*nz);
		nx /= n; ny /= n; nz /= n;
		
		xyzpts.put(x);
		xyzpts.put(y);
		xyzpts.put(z);
		normals.put(nx);
		normals.put(ny);
		normals.put(nz);
	}//end storePoint
	
	// Creates and stores a point for the gun
	private static void storePointInner(double phi, double theta, double orbCenter, double orbRadius, GL gl)
	{
		float x = (float) (orbRadius*Math.cos(phi));
		float radius = (float) (orbRadius*Math.sin(phi));
		float y = (float)Math.sin(theta) * radius;
		float z = (float)Math.cos(theta) * radius;
		
		float nx = -x;
		float ny = -y;
		float nz = -z;
		
		x += orbCenter;
		
		float n = (float)Math.sqrt(nx*nx + ny*ny + nz*nz);
		nx /= n; ny /= n; nz /= n;
		
		xyzpts.put(x);
		xyzpts.put(y);
		xyzpts.put(z);
		normals.put(nx);
		normals.put(ny);
		normals.put(nz);
	}//end storePoint
	
	/**
	 * Draws the gun model facing in the standard direction (x-positive,
	 * z-positive is up)
	 * @param gl
	 */
	public static void draw(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		
		xyzpts.rewind();
		normals.rewind();
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, xyzpts);
		gl.glNormalPointer(GL2.GL_FLOAT, 0, normals);
		gl.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, 2 * (stacks+orbStacks) * (slices+1));
		
		gl.glRotated(220, 1, 0, 0);
		gl.glTranslated(-.425, 0, 0);
		drawHook(gl);
		gl.glRotated(120, 1, 0, 0);
		drawHook(gl);
		gl.glRotated(120, 1, 0, 0);
		drawHook(gl);
		
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glPopMatrix();
	}
	
	/**
	 * Draws a hook with the FloatBuffer. Assumes float and normal client states
	 * are enabled
	 * @param gl
	 */
	public static void drawHook(GL2 gl)
	{
		hookpts.rewind();
		hooknormals.rewind();
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, hookpts);
		gl.glNormalPointer(GL.GL_FLOAT, 0, hooknormals);
		gl.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, 2*hslices*hstacks);
	}
}

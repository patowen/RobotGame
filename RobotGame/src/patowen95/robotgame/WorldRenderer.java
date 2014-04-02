package patowen95.robotgame;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;

public class WorldRenderer
{
	private Controller c;
	
	private int numVertices; //Amount of visual data
	private ArrayList<Double> vX, vY, vZ; //Vertex location
	private ArrayList<Double> nX, nY, nZ; //Normal
	private ArrayList<Texture> tex; //Texture
	private ArrayList<Double> tX, tY; //Texture coordinates
	
	private int numTriangles;
	private ArrayList<Integer> v1, v2, v3;
	
	public WorldRenderer(Controller controller)
	{
		c = controller;
		
		//Initialize all data arrays		
		numVertices = 0;
		vX = new ArrayList<Double>(); vY = new ArrayList<Double>(); vZ = new ArrayList<Double>();
		nX = new ArrayList<Double>(); nY = new ArrayList<Double>(); nZ = new ArrayList<Double>();
		
		tex = new ArrayList<Texture>();
		
		tX = new ArrayList<Double>(); tY = new ArrayList<Double>();
		
		v1 = new ArrayList<Integer>(); v2 = new ArrayList<Integer>(); v3 = new ArrayList<Integer>();
	}
	
	/**
	 * Uses drawing data to draw the world
	 * @param gl
	 */
	public void draw(GL2 gl)
	{		
		//Set material properties to what is set for the world
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {1, 1, 1, 1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] {0, 0, 0, 1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, new float[] {0, 0, 0, 1}, 0);
		
		//These variables prevent a texture from being disabled and immediately enabled
		Texture currentTexture = null;
		boolean textureUsed = false;
		
		for (int i=0; i<numTriangles; i++)
		{
			int vert1 = v1.get(i);
			int vert2 = v2.get(i);
			int vert3 = v3.get(i);
			
			if (currentTexture != tex.get(vert1))
			{
				if (textureUsed) currentTexture.disable(gl);
				currentTexture = tex.get(vert1);
				currentTexture.enable(gl);
				currentTexture.bind(gl);
				textureUsed = true;
			}
			
			gl.glBegin(GL2.GL_TRIANGLES);
			
			gl.glNormal3d(nX.get(vert1), nY.get(vert1), nZ.get(vert1));
			gl.glTexCoord2d(tX.get(vert1), tY.get(vert1));
			gl.glVertex3d(vX.get(vert1), vY.get(vert1), vZ.get(vert1));
			
			gl.glNormal3d(nX.get(vert2), nY.get(vert2), nZ.get(vert2));
			gl.glTexCoord2d(tX.get(vert2), tY.get(vert2));
			gl.glVertex3d(vX.get(vert2), vY.get(vert2), vZ.get(vert2));
			
			gl.glNormal3d(nX.get(vert3), nY.get(vert3), nZ.get(vert3));
			gl.glTexCoord2d(tX.get(vert3), tY.get(vert3));
			gl.glVertex3d(vX.get(vert3), vY.get(vert3), vZ.get(vert3));
			
			gl.glEnd();
		}
		
		if (textureUsed)
			currentTexture.disable(gl);
	}
	
	public void addVertex(double x, double y, double z, double nx, double ny, double nz, int texture, double tx, double ty)
	{
		vX.add(x); vY.add(y); vZ.add(z);
		nX.add(nx); nY.add(ny); nZ.add(nz);
		tex.add(c.getTexture(texture));
		tX.add(tx); tY.add(ty);
		numVertices++;
	}
	
	public void addTriangle(int vert1, int vert2, int vert3)
	{
		v1.add(vert1);
		v2.add(vert2);
		v3.add(vert3);
		numTriangles++;
	}
	
	public void addTriangle(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, int texture, double tx1, double ty1, double tx2, double ty2, double tx3, double ty3)
	{
		//Set the normal
		double vectX1 = x2 - x1; double vectX2 = x3 - x1;
		double vectY1 = y2 - y1; double vectY2 = y3 - y1;
		double vectZ1 = z2 - z1; double vectZ2 = z3 - z1;
		
		double normX = vectY1*vectZ2 - vectZ1*vectY2;
		double normY = vectZ1*vectX2 - vectX1*vectZ2;
		double normZ = vectX1*vectY2 - vectY1*vectX2;
		
		double normMag = Math.sqrt(normX*normX + normY*normY + normZ*normZ);
		
		normX /= normMag; normY /= normMag; normZ /= normMag;
		
		vX.add(x1); vY.add(y1); vZ.add(z1);
		nX.add(normX); nY.add(normY); nZ.add(normZ);
		tX.add(tx1); tY.add(ty1);
		tex.add(c.getTexture(texture));
		v1.add(numVertices ++);
		
		vX.add(x2); vY.add(y2); vZ.add(z2);
		nX.add(normX); nY.add(normY); nZ.add(normZ);
		tX.add(tx2); tY.add(ty2);
		tex.add(c.getTexture(texture));
		v2.add(numVertices ++);
		
		vX.add(x3); vY.add(y3); vZ.add(z3);
		nX.add(normX); nY.add(normY); nZ.add(normZ);
		tX.add(tx3); tY.add(ty3);
		tex.add(c.getTexture(texture));
		v3.add(numVertices ++);
		
		numTriangles ++;
	}
}

package patowen95.robotgame.utilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

import com.jogamp.opengl.util.texture.Texture;

public class WorldExporter
{
	private BufferedOutputStream stream;
	private HashMap<Integer, String> alias;
	
	private int numVertices; //Amount of visual data
	private ArrayList<Double> vX, vY, vZ; //Visual Vertex location
//	private ArrayList<Double> nX, nY, nZ; //Normal
//	private ArrayList<Texture> tex; //Texture
//	private ArrayList<Double> tX, tY; //Texture coordinates
	
	public WorldExporter(File file) throws FileNotFoundException
	{
		stream = new BufferedOutputStream(new FileOutputStream(file));
		numVertices = 0;
		vX = new ArrayList<Double>();
		vY = new ArrayList<Double>();
		vZ = new ArrayList<Double>();
	}
	
	public int addVertex(double x, double y, double z, double nx, double ny, double nz, int tex, double tx, double ty) throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(69).order(ByteOrder.LITTLE_ENDIAN);
		buf.put((byte)1);
		buf.putDouble(x).putDouble(y).putDouble(z);
		buf.putDouble(nx).putDouble(ny).putDouble(nz);
		buf.putInt(tex);
		buf.putDouble(tx).putDouble(ty);
		stream.write(buf.array());
		
		vX.add(x); vY.add(y); vZ.add(z);
		return numVertices++;
	}
	
	public void addTriangle(int t1, int t2, int t3) throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN);
		buf.put((byte)2);
		buf.putInt(t1).putInt(t2).putInt(t3);
		stream.write(buf.array());
	}
	
	public void addPoly(int... vertices) throws IOException
	{
		for (int i=1; i<vertices.length-1; i++)
			addTriangle(vertices[0], vertices[i], vertices[i+1]);
	}
	
	public void addPolyWall(int... vertices) throws IOException
	{
		for (int i=1; i<vertices.length-1; i++)
			addWall(vertices[0], vertices[i], vertices[i+1]);
	}
	
	public void addSolidTriangle(int t1, int t2, int t3) throws IOException
	{
		addTriangle(t1, t2, t3);
		addWall(t1, t2, t3);
	}
	
	public void addSolidPoly(int... vertices) throws IOException
	{
		addPoly(vertices);
		addPolyWall(vertices);
	}
	
	public void addWall(int v1, int v2, int v3) throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(73).order(ByteOrder.LITTLE_ENDIAN);
		buf.put((byte)3);
		buf.putDouble(vX.get(v1)).putDouble(vY.get(v1)).putDouble(vZ.get(v1));
		buf.putDouble(vX.get(v2)).putDouble(vY.get(v2)).putDouble(vZ.get(v2));
		buf.putDouble(vX.get(v3)).putDouble(vY.get(v3)).putDouble(vZ.get(v3));
		stream.write(buf.array());
	}
	
	public void addWall(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate(73).order(ByteOrder.LITTLE_ENDIAN);
		buf.put((byte)3);
		buf.putDouble(x1).putDouble(y1).putDouble(z1);
		buf.putDouble(x2).putDouble(y2).putDouble(z2);
		buf.putDouble(x3).putDouble(y3).putDouble(z3);
		stream.write(buf.array());
	}
	
	public void close() throws IOException
	{
		stream.write(-1);
		stream.close();
	}
	
	//Extra methods for ease of use (if the ones above weren't enough)
	public void addTriangle(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, int texture, double tx1, double ty1, double tx2, double ty2, double tx3, double ty3, boolean solid) throws IOException
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
		
		int v1 = addVertex(x1, y1, z1, normX, normY, normZ, texture, tx1, ty1);
		int v2 = addVertex(x2, y2, z2, normX, normY, normZ, texture, tx2, ty2);
		int v3 = addVertex(x3, y3, z3, normX, normY, normZ, texture, tx3, ty3);
		addTriangle(v1, v2, v3);
		if (solid)
			addWall(v1, v2, v3);
	}
	
	public void addQuad(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, int texture, double tx1, double ty1, double tx2, double ty2, double tx3, double ty3, double tx4, double ty4, boolean solid) throws IOException
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
		
		int v1 = addVertex(x1, y1, z1, normX, normY, normZ, texture, tx1, ty1);
		int v2 = addVertex(x2, y2, z2, normX, normY, normZ, texture, tx2, ty2);
		int v3 = addVertex(x3, y3, z3, normX, normY, normZ, texture, tx3, ty3);
		int v4 = addVertex(x4, y4, z4, normX, normY, normZ, texture, tx4, ty4);
		addTriangle(v1, v2, v3);
		addTriangle(v1, v3, v4);
		if (solid)
		{
			addWall(v1, v2, v3);
			addWall(v1, v3, v4);
		}
	}
}

package patowen95.robotgame.utilities;

import java.io.File;
import java.io.IOException;

public class MapArenaGenerator
{
	public void generateMap()
	{
		try
		{
			WorldExporter e = new WorldExporter(new File("maps/arena.map"));
			int tex = 0;
			
			//Ramps
			double edge = 15*Math.sin(Math.PI/3);
			double scale = 0.5;
			e.addQuad(2.5*scale, 8*scale, 0, 2.5*scale, edge*scale, 4*scale, -2.5*scale, edge*scale, 4*scale, -2.5*scale, 8*scale, 0, tex, 5, 0, 5, 6, 0, 6, 0, 0, true);
			e.addTriangle(2.5*scale, 8*scale, 0, 2.5*scale, edge*scale, 0, 2.5*scale, edge*scale, 4*scale, tex, 0, 0, 6, 0, 6, 5, true);
			e.addTriangle(-2.5*scale, 8*scale, 0, -2.5*scale, edge*scale, 4*scale, -2.5*scale, edge*scale, 0, tex, 0, 0, 6, 5, 6, 0, true);
			
			e.addQuad(-2.5*scale, -8*scale, 0, -2.5*scale, -edge*scale, 4*scale, 2.5*scale, -edge*scale, 4*scale, 2.5*scale, -8*scale, 0, tex, 5, 0, 5, 6, 0, 6, 0, 0, true);
			e.addTriangle(-2.5*scale, -8*scale, 0, -2.5*scale, -edge*scale, 0, -2.5*scale, -edge*scale, 4*scale, tex, 0, 0, 6, 0, 6, 5, true);
			e.addTriangle(2.5*scale, -8*scale, 0, 2.5*scale, -edge*scale, 4*scale, 2.5*scale, -edge*scale, 0, tex, 0, 0, 6, 5, 6, 0, true);
			
			//Walls and roof
			for (int i=0; i<6; i++)
			{
				double theta1 = i*Math.PI/3;
				double c1 = Math.cos(theta1);
				double s1 = Math.sin(theta1);
				double theta2 = (i+1)*Math.PI/3;
				double c2 = Math.cos(theta2);
				double s2 = Math.sin(theta2);
				e.addQuad(15*c1*scale, 15*s1*scale, 0, 15*c1*scale, 15*s1*scale, 4*scale, 15*c2*scale, 15*s2*scale, 4*scale, 15*c2*scale, 15*s2*scale, 0, tex, 0, 0, 0, 4, 15, 4, 15, 0, true);
				e.addQuad(20*c1*scale, 20*s1*scale, 4*scale, 20*c1*scale, 20*s1*scale, 10*scale, 20*c2*scale, 20*s2*scale, 10*scale, 20*c2*scale, 20*s2*scale, 4*scale, tex, 0, 0, 0, 6, 20, 6, 20, 0, true);
				e.addTriangle(0, 0, 15*scale, 20*c2*scale, 20*s2*scale, 10*scale, 20*c1*scale, 20*s1*scale, 10*scale, tex, 10, 20, 20, 0, 0, 0, true);
			}
			
			//Lower Floor
			int[] hex = new int[6];
			for (int i=0; i<6; i++)
			{
				double theta = i*Math.PI/3;
				double c = Math.cos(theta);
				double s = Math.sin(theta);
				hex[i] = e.addVertex(15*c*scale, 15*s*scale, 0, 0, 0, 1, tex, 15*c, 15*s);
			}
			e.addSolidPoly(hex);
			
			//Upper Floor
			int[] hex1 = new int[6], hex2 = new int[6];
			for (int i=0; i<6; i++)
			{
				double theta = i*Math.PI/3;
				double c = Math.cos(theta);
				double s = Math.sin(theta);
				hex1[i] = e.addVertex(15*c*scale, 15*s*scale, 4*scale, 0, 0, 1, tex, 15*c, 15*s);
				hex2[i] = e.addVertex(20*c*scale, 20*s*scale, 4*scale, 0, 0, 1, tex, 20*c, 20*s);
			}
			for (int i=0; i<6; i++)
			{
				int i2 = i+1;
				if (i2 == 6) i2 = 0;
				e.addSolidPoly(hex1[i], hex2[i], hex2[i2], hex1[i2]);
			}
			
			e.close();
			System.out.println("Success");
		}
		catch (IOException e)
		{
			System.out.println("Error occurred while generating map.");
		}
	}
}

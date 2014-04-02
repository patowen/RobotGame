package patowen95.robotgame;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import patowen95.robotgame.entity.EI;
import patowen95.robotgame.entity.Entity;

public class WorldImporter
{
	private Controller c;
	private World w;
	private File f;
	
	private HashMap<Integer, String> alias;
	
	public WorldImporter(Controller controller, World world, File file)
	{
		w = world;
		f = file;
		
		alias = new HashMap<Integer, String>(64);
	}
	
	public void importWorld()
	{
		ImportStream input;
		
		try
		{
			input = new ImportStream(new BufferedInputStream(new FileInputStream(f)));
			readMap(input);
			input.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out.println("Error reading map");
			e.printStackTrace();
		}
	}
	
	private void readMap(ImportStream input) throws IOException
	{
		int b = input.getByte();
		while ((byte)b != -1)
		{
			switch (b)
			{
			case 0: //Reading info, such as alias
				addAlias(input);
				break;
			case 1: //Vertex placement
				addVertex(input);
				break;
			case 2: //Triangle placement
				addTriangle(input);
				break;
			case 3: //Collision wall placement
				addWall(input);
				break;
			case 4: //System placement
				addSystem(input);
				break;
			case 5: //Entity placement
				addEntity(input);
				break;
			default:
				System.out.println("Undefined");
				break;
			}
			b = input.getByte();
		}
		System.out.println("done");
	}
	
	private void addAlias(ImportStream input) throws IOException
	{
		alias.put(input.getInt(), input.getString());
	}
	
	private void addVertex(ImportStream input) throws IOException
	{
		w.getWorldRenderer().addVertex(input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
				input.getInt(), input.getDouble(), input.getDouble());
	}
	
	private void addTriangle(ImportStream input) throws IOException
	{
		w.getWorldRenderer().addTriangle(input.getInt(), input.getInt(), input.getInt());
	}
	
	private void addWall(ImportStream input) throws IOException
	{
		w.getCollision().addWall(input.getDouble(), input.getDouble(), input.getDouble(),
				input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble());
	}
	
	private void addEntity(ImportStream input) throws IOException
	{
		int type = input.getInt();
		Entity e = EI.createEntity(c, w, type);
		e.initializeData(input);
	}
	
	private void addSystem(ImportStream input) throws IOException
	{
		//TODO Implement
	}
}

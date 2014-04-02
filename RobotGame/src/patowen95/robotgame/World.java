package patowen95.robotgame;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import javax.media.opengl.GL2;

import patowen95.robotgame.entity.EI;
import patowen95.robotgame.entity.Entity;
import patowen95.robotgame.entity.EntityCreationListener;
import patowen95.robotgame.entity.EntityPlayer;
import patowen95.robotgame.entity.EntityPlayerBasic;
import patowen95.robotgame.menu.MainMenu;

/**
 * Stores visual and physical data for a 3D level.
 * @author Patrick Owen
 */
public class World
{
	//Allows data to be saved between levels
	private Controller c;
	
	//Collision data, each synchronized with each other
	private Collision collision;
	
	//Rendering data, for showing the walls of the World
	private WorldRenderer renderer;
	
	//Entity data
	private EntityPlayerBasic player;
	private ArrayList<Entity> entities;
	private ArrayList<Entity> deletionQueue;
	private ArrayList<Entity> creationQueue;
	
	private HashMap<Identification, Entity> entityMap;
	private HashSet<Identification> antiEntitySet;
	private ArrayBlockingQueue<Identification> antiEntityQueue;
	private int nextEntityID;
	
	//Entity Creation Listener objects
	private ArrayList<EntityCreationListener> eclisteners;
	
	//Death
	private double deathWait;
	private double deathDuration;
	
	private double gravity;
	
	//Spawn data
	private ArrayList<Double> spawnX;
	private ArrayList<Double> spawnY;
	private ArrayList<Double> spawnZ;
	
	//Spawning waves
//	private static final int NUM_DIFFICULTIES = 6;
//	private double difficulty;
//	private SpawningWave currentWave;
//	private ArrayList<ArrayList<SpawningWave>> waves; //Spawning waves; the outer array list stores single-difficulty sets.
	
	/**
	 * Creates a game level given data available in a file.
	 * @param controller The active Controller object
	 * @param fName The name of the file containing the data.
	 */
	public World(Controller controller, File fName)
	{
		c = controller;
		
		collision = new Collision(this);
		renderer = new WorldRenderer(c);
		
		entities = new ArrayList<Entity>();
		deletionQueue = new ArrayList<Entity>();
		creationQueue = new ArrayList<Entity>();
		
		eclisteners = new ArrayList<EntityCreationListener>();
		
		entityMap = new HashMap<Identification, Entity>(1024);
		antiEntitySet = new HashSet<Identification>(64);
		antiEntityQueue = new ArrayBlockingQueue<Identification>(64);
		nextEntityID = 0;
		
		gravity = 10;
		
		deathDuration = 0;
		deathWait = 2;
		
		spawnX = new ArrayList<Double>();
		spawnY = new ArrayList<Double>();
		spawnZ = new ArrayList<Double>();
		
//		waves = new ArrayList<ArrayList<SpawningWave>>();
//		for (int i=0; i<NUM_DIFFICULTIES; i++)
//			waves.add(new ArrayList<SpawningWave>());
//		
//		difficulty = 0;
//		currentWave = null;
		
//		FileReader reader = new FileReader();
//		reader.readData(fName);
		
		WorldImporter importer = new WorldImporter(c, this, new File("maps/arena.map"));
		importer.importWorld();
		
		player = (EntityPlayer)EI.createEntity(c, this, EI.Player);
		player.setPosition(0, 0, 0.001);
		entities.add(player);
	}
	
	/**
	 * Resets the level. Currently broken because the player is removed.
	 */
	public void resetLevel()
	{		
		for (Entity e : entities)
		{
			e.delete();
		}
		
		entities.clear();
		deletionQueue.clear();
		creationQueue.clear();
		entityMap.clear();
		nextEntityID = 0;
		
		deathDuration = 0;
		
		spawnX = new ArrayList<Double>();
		spawnY = new ArrayList<Double>();
		spawnZ = new ArrayList<Double>();
		
//		difficulty = 0;
//		currentWave = null;
	}
	
	/**
	 * Returns the EntityPlayerBasic object
	 */
	public EntityPlayerBasic getPlayer()
	{
		return player;
	}
	
	/**
	 * Returns an ArrayList of the entities held by the World.
	 * Do not modify this ArrayList.
	 */
	public ArrayList<Entity> getEntities()
	{
		return entities;
	}
	
	public Entity getEntity(int owner, int id)
	{
		return entityMap.get(new Identification(owner, id));
	}
	
	/**
	 * Adds a new ECL object to the World's ArrayList
	 * @param ecl
	 */
	public void addEntityCreationListener(EntityCreationListener ecl)
	{
		eclisteners.add(ecl);
	}
	
	/**
	 * Returns the current gravity of the world
	 */
	public double getGravity()
	{
		return gravity;
	}
	
	/**
	 * Requests deletion of the specified entity.
	 * @param e Specified entity.
	 */
	public void delete(Entity e)
	{
		Identification identify = new Identification(e.getOwner(), e.getID());
		
		deletionQueue.add(e);
		entityMap.remove(identify);
	}
	
	public void delete(int owner, int id)
	{
		Identification identify = new Identification(owner, id);
		
		Entity e = getEntity(owner, id);
		if (e != null)
		{
			deletionQueue.add(e);
			entityMap.remove(identify);
		}
		
		antiEntitySet.add(identify);
	}
	
	public void enqueueTimedIdentification(int owner, int id)
	{
		if (antiEntityQueue.remainingCapacity() == 0)
		{
			antiEntitySet.remove(antiEntityQueue.remove());
		}
		antiEntityQueue.add(new Identification(owner, id));
	}
	
	/**
	 * Requests creation of the specified entity.
	 * @param e Specified entity.
	 */
	public void create(Entity e)
	{
		Identification identify = new Identification(e.getOwner(), e.getID());
		if (!antiEntitySet.contains(identify))
		{
			creationQueue.add(e);
			entityMap.put(new Identification(e.getOwner(), e.getID()), e);
		}
		
		for(EntityCreationListener ecl: eclisteners)
			ecl.entityCreated(e);
	}
	
	public int generateEntityID()
	{
		return nextEntityID++;
	}
	
	/**
	 * Returns the x-position of the specified spawn point.
	 */
	public double getSpawnX(int spawnPoint)
	{
		return spawnX.get(spawnPoint);
	}
	
	/**
	 * Returns the y-position of the specified spawn point.
	 */
	public double getSpawnY(int spawnPoint)
	{
		return spawnY.get(spawnPoint);
	}
	
	/**
	 * Returns the z-position of the specified spawn point.
	 */
	public double getSpawnZ(int spawnPoint)
	{
		return spawnZ.get(spawnPoint);
	}
	
	/**
	 * Runs a step of the level.
	 * @param dt Time step in seconds.
	 */
	public void step(double dt)
	{		
//		handleSpawning(dt);
		handleDeath(dt);
		
		for (Entity e : entities)
		{
			e.step(dt);
		}
		
		if (c.isMultiplayer() && c.isServer())
		{
			for (Entity e : entities)
			{
				e.stepSendSignals();
			}
		}
		
		for (Entity e : deletionQueue)
		{
			entities.remove(e);
		}
		
		for (Entity e : creationQueue)
		{
			if (!antiEntitySet.contains(new Identification(e.getOwner(), e.getID())))
				entities.add(e);
		}
		
		deletionQueue.clear();
		creationQueue.clear();
	}
	
	/**
	 * Quits the level after a certain amount of time by notifying Controller after
	 * the player has been dead for the proper amount of time.
	 */
	public void handleDeath(double dt)
	{
		if (player.isDead())
		{
			deathDuration += dt;
			if (deathDuration >= deathWait)
			{
				if (c.isMultiplayer())
					resetLevel();
				else
				{
					c.setCurrentMenu(new MainMenu(c));
					c.resetScore();
				}
			}
		}
	}
	
//	/**
//	 * Handles the spawn waves and switches waves in the best way for gameplay (easy to medium to hard, ...)
//	 * @param dt The time step
//	 */
//	public void handleSpawning(double dt)
//	{
//		if (currentWave == null || (!currentWave.isRunning() && noEnemies()))
//		{
//			player.heal();
//			
//			int waveDifficulty = (int)difficulty;
//			if (waveDifficulty >= NUM_DIFFICULTIES) waveDifficulty = NUM_DIFFICULTIES - 1;
//			ArrayList<SpawningWave> waveList = waves.get(waveDifficulty);
//			
//			int index = (int)(Math.random()*waveList.size());
//			currentWave = waveList.get(index);
//			currentWave.start();
//			
//			difficulty += 0.25;
//		}
//		
//		currentWave.step(dt);
//	}
//	
//	//Checks whether all the enemies have been destroyed by the player
//	private boolean noEnemies()
//	{
//		for (Entity e : entities)
//		{
//			if (e instanceof Enemy) return false;
//		}
//		return true;
//	}
	
	/**
	 * Uses drawing data to draw the world
	 * @param gl
	 */
	public void draw(GL2 gl)
	{
		//Set the view
		if (player.isDead())
			player.viewThirdPerson(gl);
		else
			player.viewFirstPerson(gl);
		
		//Draw the lights
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {0.31f, 0, 0.95f, 0}, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[] {0.4f, 0.4f, 0.4f, 0}, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[] {0.4f, 0.4f, 0.4f, 0}, 0);
		
		renderer.draw(gl);
		
		//Draw entities
		for (Entity e : entities)
		{
			if (!e.isGhost())
				e.draw(gl);
		}
		
		for (Entity e : entities)
		{
			if (!e.isGhost())
				e.draw2(gl);
		}
	}
	
	/**
	 * Returns the Collision object that handles collisions in this world.
	 */
	public Collision getCollision()
	{
		return collision;
	}
	
	public WorldRenderer getWorldRenderer()
	{
		return renderer;
	}
	
	private class Identification
	{
		public final int owner;
		public final int id;
		public final int hashCode;
		
		public Identification(int owner, int id)
		{
			this.owner = owner;
			this.id = id;
			hashCode = owner*29 + id;
		}
		
		public int hashCode()
		{
			return hashCode;
		}
		
		public boolean equals(Object o)
		{
			Identification i = (Identification)o;
			return (i.owner == owner && i.id == id);
		}
	}
	
	//Handles parsing the map data file for level creation.
	private class FileReader
	{
		private Scanner data;
		private String fileInfo;
		
		//Reads a file for map data and creates the map out of it.
		public void readData(File file)
		{			
			//Open and start reading the file
			try
			{
				data = new Scanner(file);
			}
			catch (FileNotFoundException e)
			{
				throw new IllegalArgumentException("Specified file cannot be found/read");
			}
			
			//Parse the data
			while (data.hasNextLine())
			{
				fileInfo = data.nextLine().trim();
				if (fileInfo.equals("")) continue;
				if (fileInfo.charAt(0) == '#') continue;
				int command = getInt();
				
				//1- Entity placement (id, x, y, z)
				if (command == 1)
				{
					Entity e = c.createEntity(World.this, getInt());
					
//					if (e instanceof Player)
//						player = (Player)e;
					
					if (!c.isMultiplayer() || c.isServer() || e instanceof EntityPlayer)
					{
						if (!(e instanceof EntityPlayer))
						{
							//Move the entity to the proper location.
							e.setPosition(getDouble(), getDouble(), getDouble()+0.0001);
							
	//						int extra = e.getAmountExtraData();
	//						for (int i=0; i<extra; i++)
	//							e.initializeExtraData(i, getDouble());
							
							entities.add(e);
							entityMap.put(new Identification(e.getOwner(), e.getID()), e);
						}
					}
				}
				
				//2- Surface placement
				else if (command == 2)
				{
					placeSurface();
				}
				
				//3- Spawn point placement
				else if (command == 3)
				{
					spawnX.add(getDouble());
					spawnY.add(getDouble());
					spawnZ.add(getDouble());
				}
				
				//4- Spawn waves
				else if (command == 4)
				{
					addWave(getInt());
				}
			}
		}
		
		//Places a surface based on information in the file.
		public void placeSurface()
		{
			int shape = getInt();

			if (shape == 0)
			{						
				double[] pos = new double[12];
				double[] texPos = new double[8];
				
				for (int i=0; i<12; i+=1)
                	pos[i] = getDouble();
				int texture = getInt();
				for (int i=0; i<8; i+=1)
                	texPos[i] = getDouble();
				
				//Collision
				collision.addWall(pos[0], pos[1], pos[2], pos[3], pos[4], pos[5], pos[6], pos[7], pos[8]);
				collision.addWall(pos[0], pos[1], pos[2], pos[6], pos[7], pos[8], pos[9], pos[10], pos[11]);					
				
				//Drawing
				renderer.addTriangle(pos[0], pos[1], pos[2], pos[3], pos[4], pos[5], pos[6], pos[7], pos[8], texture, texPos[0], texPos[1], texPos[2], texPos[3], texPos[4], texPos[5]);
				renderer.addTriangle(pos[0], pos[1], pos[2], pos[6], pos[7], pos[8], pos[9], pos[10], pos[11], texture, texPos[0], texPos[1], texPos[4], texPos[5], texPos[6], texPos[7]);
			}
			else if (shape == 1)
			{
				double[] pos = new double[9];
				double[] texPos = new double[6];
				
				for (int i=0; i<9; i+=1)
                	pos[i] = getDouble();
				int texture = getInt();
				for (int i=0; i<6; i+=1)
                	texPos[i] = getDouble();
				
				//Collision
				collision.addWall(pos[0], pos[1], pos[2], pos[3], pos[4], pos[5], pos[6], pos[7], pos[8]);
				
				//Drawing
				renderer.addTriangle(pos[0], pos[1], pos[2], pos[3], pos[4], pos[5], pos[6], pos[7], pos[8], texture, texPos[0], texPos[1], texPos[2], texPos[3], texPos[4], texPos[5]);
			}
		}
		
		//Adds a spawning wave based on the file.
		public void addWave(int difficulty)
		{
			SpawningWave wave = new SpawningWave(c, World.this);
			
			//Parse the data
			while (data.hasNextLine())
			{
				fileInfo = data.nextLine().trim();
				if (fileInfo.equals("")) continue;
				if (fileInfo.charAt(0) == '#') continue;
				int command = getInt();
				
				//0- End
				if (command == 0) break;
				
				//1- Spawning
				if (command == 1)
					wave.addSpawn(getInt(), getInt());
				
				//2- Delay
				if (command == 2)
					wave.addDelay(getDouble());
			}
			
//			waves.get(difficulty).add(wave);
		}
		
		//Retrieves an integer from the file info.
		public int getInt()
		{
			int pos = 0;
			while (pos < fileInfo.length() && fileInfo.charAt(pos) != ' ')
				pos += 1;
			
			int result = Integer.parseInt(fileInfo.substring(0, pos));
			
			fileInfo = fileInfo.substring(pos).trim();
			
			return result;
		}
		
		//Retrieves a double from the file info.
		public double getDouble()
		{
			int pos = 0;
			while (pos < fileInfo.length() && fileInfo.charAt(pos) != ' ')
				pos += 1;
			
			double result = Double.parseDouble(fileInfo.substring(0, pos));
			
			fileInfo = fileInfo.substring(pos).trim();
			
			return result;
		}
	}
}

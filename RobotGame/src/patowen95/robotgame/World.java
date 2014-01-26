package patowen95.robotgame;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import javax.media.opengl.GL2;

import patowen95.robotgame.entity.Entity;
import patowen95.robotgame.entity.EntityCreationListener;
import patowen95.robotgame.entity.Player;
import patowen95.robotgame.entity.enemy.Enemy;
import patowen95.robotgame.menu.MainMenu;

import com.jogamp.opengl.util.texture.Texture;

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
	
	//File data
	private File mapFile;
	
	//Entity data
	private Player player;
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
	private static final int NUM_DIFFICULTIES = 6;
	private double difficulty;
	private SpawningWave currentWave;
	private ArrayList<ArrayList<SpawningWave>> waves; //Spawning waves; the outer array list stores single-difficulty sets.
	
	//Drawing data, each synchronized with each other
	private int drawData; //Amount of visual data
	private ArrayList<Double> vX1, vY1, vZ1, vX2, vY2, vZ2, vX3, vY3, vZ3; //Vertex location
	private ArrayList<Double> nX, nY, nZ; //Normal
	private ArrayList<Texture> tex; //Texture
	private ArrayList<Double> tX1, tY1, tX2, tY2, tX3, tY3; //Texture coordinates
	
	/**
	 * Creates a game level given data available in a file.
	 * @param controller The active Controller object
	 * @param fName The name of the file containing the data.
	 */
	public World(Controller controller, File fName)
	{
		c = controller;
		mapFile = fName;
		
		collision = new Collision(this);
		
		//Initialize all data arrays		
		drawData = 0;
		vX1 = new ArrayList<Double>(); vY1 = new ArrayList<Double>(); vZ1 = new ArrayList<Double>();
		vX2 = new ArrayList<Double>(); vY2 = new ArrayList<Double>(); vZ2 = new ArrayList<Double>();
		vX3 = new ArrayList<Double>(); vY3 = new ArrayList<Double>(); vZ3 = new ArrayList<Double>();
		
		nX = new ArrayList<Double>(); nY = new ArrayList<Double>(); nZ = new ArrayList<Double>();
		
		tex = new ArrayList<Texture>();
		
		tX1 = new ArrayList<Double>(); tY1 = new ArrayList<Double>();
		tX2 = new ArrayList<Double>(); tY2 = new ArrayList<Double>();
		tX3 = new ArrayList<Double>(); tY3 = new ArrayList<Double>();
		
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
		
		waves = new ArrayList<ArrayList<SpawningWave>>();
		for (int i=0; i<NUM_DIFFICULTIES; i++)
			waves.add(new ArrayList<SpawningWave>());
		
		difficulty = 0;
		currentWave = null;
		
		FileReader reader = new FileReader();
		reader.readData(fName);
	}
	
	/**
	 * Generates the level again, rereading the file and resetting
	 * all data.
	 */
	public void resetLevel()
	{
		collision = new Collision(this);
		
		//Initialize all data arrays		
		drawData = 0;
		vX1 = new ArrayList<Double>(); vY1 = new ArrayList<Double>(); vZ1 = new ArrayList<Double>();
		vX2 = new ArrayList<Double>(); vY2 = new ArrayList<Double>(); vZ2 = new ArrayList<Double>();
		vX3 = new ArrayList<Double>(); vY3 = new ArrayList<Double>(); vZ3 = new ArrayList<Double>();
		
		nX = new ArrayList<Double>(); nY = new ArrayList<Double>(); nZ = new ArrayList<Double>();
		
		tex = new ArrayList<Texture>();
		
		tX1 = new ArrayList<Double>(); tY1 = new ArrayList<Double>();
		tX2 = new ArrayList<Double>(); tY2 = new ArrayList<Double>();
		tX3 = new ArrayList<Double>(); tY3 = new ArrayList<Double>();
		
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
		
		waves = new ArrayList<ArrayList<SpawningWave>>();
		for (int i=0; i<NUM_DIFFICULTIES; i++)
			waves.add(new ArrayList<SpawningWave>());
		
		difficulty = 0;
		currentWave = null;
		
		FileReader reader = new FileReader();
		reader.readData(mapFile);
	}
	
	/**
	 * Returns the Player object
	 */
	public Player getPlayer()
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
		handleSpawning(dt);
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
	
	/**
	 * Handles the spawn waves and switches waves in the best way for gameplay (easy to medium to hard, ...)
	 * @param dt The time step
	 */
	public void handleSpawning(double dt)
	{
		if (currentWave == null || (!currentWave.isRunning() && noEnemies()))
		{
			player.heal();
			
			int waveDifficulty = (int)difficulty;
			if (waveDifficulty >= NUM_DIFFICULTIES) waveDifficulty = NUM_DIFFICULTIES - 1;
			ArrayList<SpawningWave> waveList = waves.get(waveDifficulty);
			
			int index = (int)(Math.random()*waveList.size());
			currentWave = waveList.get(index);
			currentWave.start();
			
			difficulty += 0.25;
		}
		
		currentWave.step(dt);
	}
	
	//Checks whether all the enemies have been destroyed by the player
	private boolean noEnemies()
	{
		for (Entity e : entities)
		{
			if (e instanceof Enemy) return false;
		}
		return true;
	}
	
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
		
		//Set material properties to what is set for the world
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {1, 1, 1, 1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] {0, 0, 0, 1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, new float[] {0, 0, 0, 1}, 0);
		
		//These variables prevent a texture from being disabled and immediately enabled
		Texture currentTexture = null;
		boolean textureUsed = false;
		
		for (int i=0; i<drawData; i++)
		{
			if (currentTexture != tex.get(i))
			{
				if (textureUsed) currentTexture.disable(gl);
				currentTexture = tex.get(i);
				currentTexture.enable(gl);
				currentTexture.bind(gl);
				textureUsed = true;
			}
			
			gl.glBegin(GL2.GL_TRIANGLES);
			
			gl.glNormal3d(nX.get(i), nY.get(i), nZ.get(i));
			gl.glTexCoord2d(tX1.get(i), tY1.get(i));
			gl.glVertex3d(vX1.get(i), vY1.get(i), vZ1.get(i));
			gl.glTexCoord2d(tX2.get(i), tY2.get(i));
			gl.glVertex3d(vX2.get(i), vY2.get(i), vZ2.get(i));
			gl.glTexCoord2d(tX3.get(i), tY3.get(i));
			gl.glVertex3d(vX3.get(i), vY3.get(i), vZ3.get(i));
			
			gl.glEnd();
		}
		
		if (textureUsed)
			currentTexture.disable(gl);
		
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
	 * Adds a normal to the ArrayList of normals given the last element of the other ArrayLists.
	 */
	private void setDrawingNormal()
	{
		double vectX1 = vX2.get(drawData-1) - vX1.get(drawData-1); double vectX2 = vX3.get(drawData-1) - vX1.get(drawData-1);
		double vectY1 = vY2.get(drawData-1) - vY1.get(drawData-1); double vectY2 = vY3.get(drawData-1) - vY1.get(drawData-1);
		double vectZ1 = vZ2.get(drawData-1) - vZ1.get(drawData-1); double vectZ2 = vZ3.get(drawData-1) - vZ1.get(drawData-1);
		
		double normX = vectY1*vectZ2 - vectZ1*vectY2;
		double normY = vectZ1*vectX2 - vectX1*vectZ2;
		double normZ = vectX1*vectY2 - vectY1*vectX2;
		
		double normMag = Math.sqrt(normX*normX + normY*normY + normZ*normZ);
		
		nX.add(normX/normMag); nY.add(normY/normMag); nZ.add(normZ/normMag);
	}
	
	/**
	 * Returns the Collision object that handles collisions in this world.
	 */
	public Collision getCollision()
	{
		return collision;
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
					
					if (e instanceof Player)
						player = (Player)e;
					
					if (!c.isMultiplayer() || c.isServer() || e instanceof Player)
					{						
						//Move the entity to the proper location.
						e.setPosition(getDouble(), getDouble(), getDouble()+0.0001);
						
						int extra = e.getAmountExtraData();
						for (int i=0; i<extra; i++)
							e.initializeExtraData(i, getDouble());
						
						entities.add(e);
						entityMap.put(new Identification(e.getOwner(), e.getID()), e);
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
				vX1.add(pos[0]); vY1.add(pos[1]); vZ1.add(pos[2]);
				vX2.add(pos[3]); vY2.add(pos[4]); vZ2.add(pos[5]);
				vX3.add(pos[6]); vY3.add(pos[7]); vZ3.add(pos[8]);
				drawData ++;
				
				setDrawingNormal();
				
				vX1.add(pos[0]); vY1.add(pos[1]); vZ1.add(pos[2]);
				vX2.add(pos[6]); vY2.add(pos[7]); vZ2.add(pos[8]);
				vX3.add(pos[9]); vY3.add(pos[10]); vZ3.add(pos[11]);
				drawData ++;
				
				setDrawingNormal();
				
				tex.add(c.getTexture(texture));
				tex.add(c.getTexture(texture));
				
				tX1.add(texPos[0]); tY1.add(texPos[1]);
				tX2.add(texPos[2]); tY2.add(texPos[3]);
				tX3.add(texPos[4]); tY3.add(texPos[5]);
				
				tX1.add(texPos[0]); tY1.add(texPos[1]);
				tX2.add(texPos[4]); tY2.add(texPos[5]);
				tX3.add(texPos[6]); tY3.add(texPos[7]);
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
				vX1.add(pos[0]); vY1.add(pos[1]); vZ1.add(pos[2]);
				vX2.add(pos[3]); vY2.add(pos[4]); vZ2.add(pos[5]);
				vX3.add(pos[6]); vY3.add(pos[7]); vZ3.add(pos[8]);
				drawData ++;
				
				setDrawingNormal();
				
				tex.add(c.getTexture(texture));
				
				tX1.add(texPos[0]); tY1.add(texPos[1]);
				tX2.add(texPos[2]); tY2.add(texPos[3]);
				tX3.add(texPos[4]); tY3.add(texPos[5]);
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
			
			waves.get(difficulty).add(wave);
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

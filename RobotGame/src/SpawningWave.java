import java.util.ArrayList;


/**
 * Handles the spawning of one enemy or multiple enemies and has several options for how the spawning should
 * proceed.
 * @author Patrick Owen
 */
public class SpawningWave
{
	private Controller c;
	private World w;
	
	private boolean isRunning;
	
	private ArrayList<Integer> entity; //Index of entity to spawn, -1 if delay instead
	private ArrayList<Integer> spawnPoint; //Which spawn point to use, synchronized with entity (0 if irrelevant)
	private ArrayList<Double> delay; //How long a delay there should be, synchronized with entity (0 if irrelevant)
	
	//waitingList and waitLength make sure that the enemy to spawn waits until the spawn animation is complete.
	private ArrayList<Entity> waitingList;
	private ArrayList<Double> waitLength;
	
	private int currentIndex; //Which index of the ArrayLists is active
	private double timeLeft; //Used to count the delay
	
	/**
	 * Constructs the SpawningWave class.
	 * @param controller The Controller object of the game.
	 * @param world The World that the SpawningWave acts on.
	 */
	public SpawningWave(Controller controller, World world)
	{
		c = controller;
		w = world;
		
		isRunning = false;
		
		entity = new ArrayList<Integer>();
		spawnPoint = new ArrayList<Integer>();
		delay = new ArrayList<Double>();
		
		waitingList = new ArrayList<Entity>();
		waitLength = new ArrayList<Double>();
		
		currentIndex = 0;
		timeLeft = 0;
	}
	
	/**
	 * Adds the spawning of the specified entity to the list of instructions.
	 * @param e The entity to spawn.
	 * @param s The index of the spawn point of the entity.
	 */
	public void addSpawn(int e, int s)
	{
		entity.add(e);
		spawnPoint.add(s);
		delay.add(0.0);
	}
	
	/**
	 * Adds the specified delay to the list of instructions.
	 * @param seconds Length of the delay.
	 */
	public void addDelay(double seconds)
	{
		entity.add(-1);
		spawnPoint.add(0);
		delay.add(seconds);
	}
	
	/**
	 * Returns whether the SpawningWave is in progress spawning entities.
	 */
	public boolean isRunning()
	{
		return isRunning;
	}
	
	/**
	 * Allows the SpawningWave to start spawning entities.
	 */
	public void start()
	{
		currentIndex = 0;
		isRunning = true;
	}
	
	/**
	 * Runs a frame of the SpawningWave and determines if it needs to spawn more entities.
	 * This method only needs to be called if the wave is active, which can be determined with
	 * isRunning
	 * @param dt Time step
	 */
	public void step(double dt)
	{
		if (isRunning)
		{
			//Handle waves
			if (!c.isMultiplayer() || c.isServer())
			{
				timeLeft -= dt;
				
				if (timeLeft <= 0)
				{
					while (currentIndex < entity.size() && entity.get(currentIndex) != -1)
					{
						Entity e = c.createEntity(w, entity.get(currentIndex));
						
						int spawn = spawnPoint.get(currentIndex);
						e.setPosition(w.getSpawnX(spawn), w.getSpawnY(spawn), w.getSpawnZ(spawn));
						double height = 0, radius = 0;
						if (e instanceof Damageable)
						{
							radius = ((Damageable)e).getRadius();
							height = ((Damageable)e).getHeight()/2;
						}
						
						EntityExplosion spark = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
						spark.setColor(0, 1, 1);
						spark.setDuration(0.5);
						spark.setRadius(2+1.5*radius);
						spark.setFinalRadius(0);
						spark.setPosition(e.getX(), e.getY(), e.getZ()+height);
						w.create(spark);
						waitingList.add(e);
						waitLength.add(0.3);
						
						currentIndex++;
					}
					
					if (currentIndex >= entity.size())
					{
						if (waitingList.isEmpty())
							isRunning = false;
					}
					else
					{
						timeLeft = delay.get(currentIndex);
						currentIndex++;
					}
				}
			}
			
			//Handle the waitingList
			for (int i=0; i<waitingList.size(); i++)
			{
				double d = waitLength.get(i);
				d -= dt;
				if (d < 0)
				{
					waitLength.remove(i);
					w.create(waitingList.get(i));
					waitingList.remove(i);
					i--;
				}
				else
				{
					waitLength.set(i, d);
				}
			}
		}
	}
}

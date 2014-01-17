import java.util.ArrayList;


/**
 * This class will keep track of newly added entities in each
 * step of the program. It will be handled by the GameMap.
 * Assuming it is referenced once per step, it should never return
 * the same entity twice.
 * @author Michael Ekstrom
 */

public class EntityCreationBuffer
{
	private ArrayList<Entity> list = new ArrayList<Entity>();
	private ArrayList<Damageable> damageable = new ArrayList<Damageable>();
	
	/**
	 * Constructor
	 */
	public EntityCreationBuffer()
	{
		
	}
	
	/**
	 * Adds a new entity to the main list and all other lists
	 * for which they apply
	 */
	public void put(Entity e)
	{
		list.add(e);
		if(e instanceof Damageable)
			damageable.add((Damageable) e);
	}
	
	/**
	 * Returns the list of newly created entities
	 * @return List of new entities
	 */
	public ArrayList<Entity> getList()
	{
		return list;
	}
	
	/**
	 * Returns all new entities that implement Damageable
	 * @return List of Damageable Entities
	 */
	public ArrayList<Damageable> getDamageable()
	{
		return damageable;
	}
	
	/**
	 * Clears all entities from all lists
	 */
	public void clearList()
	{
		list.clear();
		damageable.clear();
	}
}

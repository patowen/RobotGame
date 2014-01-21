/**
 * This interface will be implemented by any class that needs to
 * detect newly created entities. This is necessary for classes like
 * PlasmaSword, which have effects that cross multiple frames.
 * @author Michael Ekstrom
 *
 */

public interface EntityCreationListener
{
	/**
	 * This will be called by World's create(Entity e) method. World
	 * will iterate thorugh and call this method with that entity for all
	 * EntityCreationListeners given to it by World's addEntityCreationListener
	 * class.
	 * @param e New Entity
	 */
	public void entityCreated(Entity e);
}
/**
 * Superclass for all enemies in the game.
 * @author Patrick Owen
 */
public class Enemy extends Entity
{
	protected double hp;
	
	/**
	 * Creates a new Enemy
	 * @param controller The active Controller object.
	 * @param gameMap The map where the Enemy is placed.
	 */
	public Enemy(Controller controller, GameMap gameMap)
	{
		super(controller, gameMap);
	}
	
	/**
	 * This method is called to add the appropriate number of points,
	 * create a necessary explosion, and destroy the enemy.
	 */
	public void destroy()
	{
		delete();
	}
}
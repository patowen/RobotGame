package patowen95.robotgame.entity.ai;
import patowen95.robotgame.Controller;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.enemy.Enemy;


/**
 * AIBasic is a superclass for all classes that handle artificial intelligence.
 * It holds information common to all these classes.
 * @author Patrick Owen
 */
public class AIBasic
{
	/**
	 * The main Controller object of the game.
	 */
	protected Controller c;
	
	/**
	 * The world where the enemy who holds the AI object is.
	 */
	protected World w;
	
	/**
	 * The enemy controlled by the AI
	 */
	protected Enemy enemy;
	
	/**
	 * Location and dimensions of the holder of the AI object, must be set manually.
	 */
	protected double x, y, z, radius, height;
	
	/**
	 * Velocity of the holder of the AI object, must be set manually.
	 */
	protected double xV, yV, zV;
	
	/**
	 * Constructs an AIBasic object tied to an enemy.
	 * @param controller The controller object.
	 * @param world The world where the enemy is.
	 * @param e The parent enemy.
	 */
	public AIBasic(Controller controller, World world, Enemy e)
	{
		c = controller;
		w = world;
		enemy = e;
	}
}

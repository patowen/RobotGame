import javax.media.opengl.GL2;


/**
 * Superclass for all player-wielded weapons in the game.
 * @author Michael Ekstrom
 * @author Patrick Washington
 */


public class Weapon
{
	protected Controller c;
	protected GameMap map;
	protected Player player;
	protected InputHandler input;
	
	protected double shotDelay;
	protected double charge;
	
	protected double x, y, z;
	protected double horizontalDir, verticalDir;
	
	/**
	 * Constructs a Weapon object.
	 * @param controller The Controller object.
	 * @param gameMap The map where the player is.
	 * @param p The owner player.
	 */
	public Weapon(Controller controller, GameMap gameMap, Player p)
	{
		c = controller;
		map = gameMap;
		player = p;
		input = c.getInputHandler();
		
	}
	
	/**
	 * Handles the operations of the weapon. This method should be called every frame.
	 * @param dt
	 */
	public void step(double dt)
	{
		handleFiring(dt);
	}
	
	/**
	 * Sets the position of the player's eye according to the weapon
	 * @param xPos x
	 * @param yPos y
	 * @param zPos z
	 * @param hDir Horizontal direction
	 * @param vDir Vertical direction
	 */
	public void setPosition(double xPos, double yPos, double zPos, double hDir, double vDir)
	{
		x = xPos; y = yPos; z = zPos;
		horizontalDir = hDir; verticalDir = vDir;
	}
	
	//Handles firing the weapon
	private void handleFiring(double dt)
	{
		
	}
	
	/**
	 * Draws the weapon
	 * @param gl
	 */
	public void draw(GL2 gl)
	{
		
	}
	
	/**
	 * Draws transparent parts of the weapon, called after
	 * all draw methods are called
	 * @param gl
	 */
	public void draw2(GL2 gl)
	{
		
	}
}


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
	
	protected double maxEnergy;//Maximum energy of weapon. Default 100
	protected double energy;//Current energy available to the weapon
	protected double energyRegen;//Rate at which the energy regenerates per second, multiply by dt
	protected double energyUse;//Amount of energy the weapon takes per shot
	
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
		maxEnergy = 100;
		energy = maxEnergy;
		energyRegen = 2.5;
	}
	
	//returns the current energy of the weapon
	public double getEnergy()
	{
		return energy;
	}

	//returns the current energy of the weapon
	public double getMaxEnergy()
	{
		return maxEnergy;
	}
	
	//returns the current energy of the weapon
	public double getEnergyUse()
	{
		return energyUse;
	}
	
	/**
	 * Handles the operations of the weapon. This method should be called every frame.
	 * @param dt
	 */
	public void step(double dt)
	{
		
	}
	
	//Recharges the energy of the weapon
	public void recharge(double dt)
	{
		if (energy + energyRegen*dt < maxEnergy)
		{
			if (maxEnergy - energy < energyRegen * dt)
				energy = maxEnergy;
			else
				energy+=energyRegen * dt;
		}	
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
	@SuppressWarnings("unused")
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


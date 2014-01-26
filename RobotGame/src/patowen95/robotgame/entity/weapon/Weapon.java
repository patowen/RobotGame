package patowen95.robotgame.entity.weapon;
import javax.media.opengl.GL2;

import patowen95.robotgame.Controller;
import patowen95.robotgame.InputHandler;
import patowen95.robotgame.NetworkPacket;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.Player;


/**
 * Superclass for all player-wielded weapons in the game.
 * @author Michael Ekstrom
 * @author Patrick Washington
 */


public class Weapon
{
	protected Controller c;
	protected World w;
	
	protected Player player;
	protected InputHandler input;
	
	protected String name;
	
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
	 * @param world The world where the player is.
	 * @param p The owner player.
	 */
	public Weapon(Controller controller, World world)
	{
		c = controller;
		w = world;
		
		input = c.getInputHandler();
		maxEnergy = 100;
		energy = maxEnergy;
		energyRegen = 2.5;
	}
	
	public void readState(NetworkPacket data)
	{
		energy = data.getDouble();
	}
	
	public void writeState(NetworkPacket data)
	{
		data.addDouble(energy);
	}
	
	public void setPlayer(Player p)
	{
		player = p;
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
	
	//returns the name of the weapon
	public String getName()
	{
		return name;
	}
	
	/**
	 * Handles the operations of the weapon. This method should be called every frame.
	 * @param dt
	 */
	public void step(double dt)
	{
		if (player.isLocal())
			handleFiring(dt);
	}
	
	//Recharges the energy of the weapon. This is handled entirely by the Player class
	public void recharge(double dt)
	{
		if (energy < maxEnergy)
		{
			energy+=energyRegen * dt;
			if (energy > maxEnergy)
				energy = maxEnergy;
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
	protected void handleFiring(double dt)
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


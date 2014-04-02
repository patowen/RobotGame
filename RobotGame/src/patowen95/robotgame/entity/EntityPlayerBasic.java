package patowen95.robotgame.entity;
import javax.media.opengl.GL;

import patowen95.robotgame.Controller;
import patowen95.robotgame.InputHandler;
import patowen95.robotgame.NetworkPacket;
import patowen95.robotgame.World;

/**
 * Handles the controls and situation of the player. This class represents the player of the game.
 * @author Patrick Owen
 */
public abstract class EntityPlayerBasic extends Entity
{
	protected InputHandler input;
	
	protected boolean isDead;
	
	/**
	 * Initializes the player with default parameters.
	 * @param controller The active Controller object.
	 * @param world Level where the player is placed.
	 */
	public EntityPlayerBasic(Controller controller, World world)
	{
		super(controller, world);
		
		input = c.getInputHandler();
		w = world;
		
		isDead = false;
	}
	
	public abstract double getTargetX();
	
	public abstract double getTargetY();
	
	public abstract double getTargetZ();
	
	protected void readState(NetworkPacket data)
	{
		super.readState(data);
		
		isDead = data.getByte() != 0;
	}
	
	protected void writeState(NetworkPacket data)
	{
		super.writeState(data);
		
		data.addByte(isDead?1:0);
	}
	
	public boolean isGhost()
	{
		return !isActive && isDead;
	}
	
	/**
	 * Heals the player, often called after each wave as a bonus and to give no incentive for the player
	 * to wait before destroying the last enemy.
	 */
	public abstract void heal();
	
	/**
	 * Returns whether the player is dead.
	 */
	public boolean isDead()
	{
		return isDead;
	}
	
	/**
	 * Adjusts the ModelView matrix to make the scene viewed from the eyes of the player.
	 */
	public abstract void viewFirstPerson(GL gl);
	
	/**
	 * Adjusts the ModelView matrix to make the player viewed at a reasonable distance
	 */
	public abstract void viewThirdPerson(GL gl);
}

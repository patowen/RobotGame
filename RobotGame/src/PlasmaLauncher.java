
/**
 * Handles the controls and the firing of the plasma launcher, a 
 * powerful AoE attack that deals heavy damage
 * @author Patrick Owen
 * @author Michael Ekstrom
 */
public class PlasmaLauncher extends Weapon
{
	
	private double shotX, shotY, shotZ; //Location of shot relative to player
	
	/**
	 * Constructs a PlasmaRifle object.
	 * @param controller The Controller object.
	 * @param gameMap The map where the player is.
	 * @param p The owner player.
	 */
	public PlasmaLauncher(Controller controller, GameMap gameMap, Player p)
	{
		super(controller, gameMap, p);
		name = "Ion Cannon";
		
		shotDelay = .5;
		shotX = 0.8; shotY = 0.04; shotZ = -0.03;
		charge = 0;
		energyUse = 33;
	}	
	
	//Handles firing the rifle. 
	protected void handleFiring(double dt)
	{
		//Fire if the mouse button is pressed.
		if (input.getMouseButton(InputHandler.FIRE) && charge <= 0 && energy >= energyUse)
		{
			EntityPlasmaBolt bolt = new EntityPlasmaBolt(c, map);
			bolt.setColor(.2f, .8f, 1f);
			double xDir = Math.cos(horizontalDir)*Math.cos(verticalDir),
					yDir = Math.sin(horizontalDir)*Math.cos(verticalDir), zDir = Math.sin(verticalDir);
			
			double xDisp = shotX*Math.cos(verticalDir) - shotZ*Math.sin(verticalDir);
			double zDisp = shotX*Math.sin(verticalDir) + shotZ*Math.cos(verticalDir);
			
			double yDisp = xDisp*Math.sin(horizontalDir) - shotY*Math.cos(horizontalDir);
			xDisp = xDisp*Math.cos(horizontalDir) + shotY*Math.sin(horizontalDir);
			
			//t is to prevent bullets from spawning through walls.
			
			double t = map.getCollision().getBulletCollision(x, y, z, xDisp, yDisp, zDisp);
			
			if (t == 1)
			{
				//Create the bullet
				energy -= energyUse;
				bolt.setPosition(x+xDisp, y+yDisp, z+zDisp);
				int vel = 30;
				
				bolt.setVelocity(vel*xDir, vel*yDir, vel*zDir);
				bolt.setOwner(player);
				
				map.create(bolt);
				
				charge = shotDelay;
				
				//Laser gun sound
				c.getSoundHandler().playSound(0);
			}
		}
		
		//Weapon recharging
		charge -= dt;
	}
}

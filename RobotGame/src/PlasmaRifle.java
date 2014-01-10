
/**
 * Handles the controls and the firing of the main game weapon, the plasma
 * rifle, which fires standard-strength bullets four times per second
 * @author Patrick Owen
 * @author Michael Ekstrom
 */
public class PlasmaRifle extends Weapon
{
	
	private double shotX, shotY, shotZ; //Location of shot relative to player
	
	/**
	 * Constructs a PlasmaRifle object.
	 * @param controller The Controller object.
	 * @param gameMap The map where the player is.
	 * @param p The owner player.
	 */
	public PlasmaRifle(Controller controller, GameMap gameMap, Player p)
	{
		super(controller, gameMap, p);
		
		shotDelay = 0.25;
		shotX = 0.8; shotY = 0.04; shotZ = -0.03;
		charge = 0;
	}
	
	/**
	 * Handles the operations of the weapon. This method should be called every frame.
	 * @param dt
	 */
	public void step(double dt)
	{
		handleFiring(dt);
	}
	
	
	//Handles firing the rifle. 
	private void handleFiring(double dt)
	{
		//Fire if the mouse button is pressed.
		if (input.getMouseButton(InputHandler.FIRE) && charge <= 0)
		{
			EntityBullet bullet = new EntityBullet(c, map);
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
				bullet.setPosition(x+xDisp, y+yDisp, z+zDisp);
				int vel = 60;
				
				bullet.setVelocity(vel*xDir, vel*yDir, vel*zDir);
				bullet.setDamage(1, 2);
				bullet.setColor(0, 1, 0);
				
				bullet.setOwner(player);
				
				map.create(bullet);
				
				charge = shotDelay;
				
				//Laser gun sound
				c.getSoundHandler().playSound(0);
			}
		}
		
		//Weapon recharging
		charge -= dt;
	}
}

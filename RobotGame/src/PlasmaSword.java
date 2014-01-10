import java.util.ArrayList;


/**
 * Handles the controls and the firing of the Plasma Sword, a melee weapon that damages all entites
 * in an arc in front of the player
 * @author Michael Ekstrom
 * @author Patrick Owen
 */
public class PlasmaSword extends Weapon
{
	private ArrayList<Damageable> hit = new ArrayList<Damageable>();//List of entities the sword has already hit
	
	private double shotDelay;
	private double charge; //How much time before the next slash
	
	private double arclength;//Relative length of the arc
	private double bladelength;//length of the sword
	private double knockback;//knockback speed of things hit by sword
	private double damage;//Damage done by a single sword hit
	private double ht, vt;//Horizontal and vertical theta adjustments
	private double hstep, vstep; //Amount horizontal and vertical theta change each step
	private int steps;//Number of bullets
	private int currentstep;//Current bullet sword is at
	
	/**
	 * Constructs a PlasmaSword object.
	 * @param controller The Controller object.
	 * @param gameMap The map where the player is.
	 * @param p The owner player.
	 */
	public PlasmaSword(Controller controller, GameMap gameMap, Player p)
	{
		super(controller, gameMap, p);
		
		shotDelay = .25;
		charge = 0;
		steps = 10;
		bladelength = 2;
		knockback = 10;
		damage = 2;
		arclength = .4;
		
	}
	
	/**
	 * Handles the operations of the weapon. This method should be called every frame.
	 * @param dt
	 */
	public void step(double dt)
	{
		handleFiring(dt);
	}
	
	//Handles sword swing
	private void handleFiring(double dt)
	{
		//Fire if the mouse button is pressed.
		if (input.getMouseButton(InputHandler.FIRE) && charge <= 0)
		{
			double randtheta = 6.2831 * Math.random();//6.2831 = 2*pi
			currentstep = 0;
			ht = Math.cos(randtheta);
			vt = Math.sin(randtheta);
			ht*=arclength; 
			vt*=arclength;
			hstep = 2*ht/steps;
			vstep = 2*vt/steps;
			charge = shotDelay;
			//Laser gun sound
			hit.clear();
			
		}
		if(currentstep < steps)/*If currently in sword swing*/
		{
			//c.getSoundHandler().playSound(0);
			//Go to current place in sword's arc
			currentstep++;
			horizontalDir+= ht;
			verticalDir+=vt;
			ht-=hstep;
			vt-=vstep;
			
			EntityExplosion boom = new EntityExplosion(c, map);
			boom.setRadius(.045);
			boom.setFinalRadius(.05);
			boom.setDuration(.1);
			boom.setColor(0, 1f, .8f);
			
			
			double xDir = Math.cos(horizontalDir)*Math.cos(verticalDir),
					yDir = Math.sin(horizontalDir)*Math.cos(verticalDir), zDir = Math.sin(verticalDir);
			
			
			double t = map.getCollision().getBulletCollision(x, y, z, bladelength*xDir, bladelength*yDir, bladelength*zDir);
			double t2 = 1; //Bullet distance traveled before first detected collision
			Damageable entityToDamage = null;
			
			boom.setPosition(x+bladelength*xDir*t, y+bladelength*yDir*t, z+bladelength*zDir*t);
			map.create(boom);
			
			for (Entity entity : map.getEntities())
			{
				if (entity == player) continue;
				if (!(entity instanceof Damageable)) continue;
				
				Damageable e = (Damageable) entity;
				
				//tTest must be less than t2 to update it.
				double tTest = map.getCollision().getEntityBulletCollision(x, y, z, bladelength*xDir*t, bladelength*yDir*t, bladelength*zDir*t,
						e.getXPrevious(), e.getYPrevious(), e.getZPrevious(),
						e.getX()-e.getXPrevious(), e.getY()-e.getYPrevious(), e.getZ() - e.getZPrevious(), e.getRadius(), e.getHeight());
				
				if (tTest < t2)
				{
					entityToDamage = e;
					for(Damageable d: hit)
					{
						if(e == d)
						{
							entityToDamage = null;
						}
					}
					hit.add(e);
				}
			}
			
			//Bullet hits entity.
			if (entityToDamage != null)
			{
				double totalVel = Math.sqrt(xDir*xDir + yDir*yDir + zDir*zDir);
				entityToDamage.applyDamage(damage, -xDir/totalVel, -yDir/totalVel, -zDir/totalVel, knockback, false);
				boom.setFinalRadius(.5);
				boom.setDuration(.25);
				boom.setColor(.5f, .5f, 1f);
				map.create(boom);
			}
		}
		
		//Weapon recharging
		charge -= dt;
	}
}

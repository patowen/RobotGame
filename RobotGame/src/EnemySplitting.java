/**
 * This enemy is similar in behavior to EnemyShocking, except that it divides when hit into two smaller entities
 * @author Michael Ekstrom
 */
public class EnemySplitting extends EnemyShocking
{
	private double stage;
	
	/**
	 * Standard constructor, uses 8 as a default size
	 * @param c
	 * @param gm
	 */
	public EnemySplitting(Controller c, GameMap gm)
	{
		this(c, gm, 3.5);
	}
	
	/**
	 * Creates a new EnemySplitting object
	 * @param c Controller controller object
	 * @param gm GameMap object
	 * @param s number of times the enemy and its heirs can split
	 */
	public EnemySplitting(Controller c, GameMap gm, double s)
	{
		super(c, gm);
		stage = s;
		setRadius(radius*s);
		double maxSpeed;
		if (stage == 3.5)
		{
			maxSpeed = 3.9;
		}
		else if (stage < 1.5)
		{
			maxSpeed = 7;
		}
		else
		{
			maxSpeed = 4;
		}
		ai.setControls(maxSpeed, 2, 0, 0, 6, 1, 0.4, 1, 0, 0, 1);
	}
	
	public void step(double dt)
	{
		super.step(dt);
		
	}
	
	public void destroy()
	{
		if (stage > 1)
		{
			EnemySplitting temp1 = new EnemySplitting(c, map, stage*.7);
			EnemySplitting temp2 = new EnemySplitting(c, map, stage*.7);
			temp1.setPosition(x, y, z);
			temp2.setPosition(x, y, z);
			Player target = c.getPlayer();
			double xDir = target.getX() - x;
			double yDir = target.getY() - y;
			double mag = Math.sqrt(xDir*xDir + yDir*yDir);
			xDir/=mag; yDir/=mag; 
			double vel = 2;
			temp1.setVelocity(xDir*vel, -yDir*vel, 0);
			temp2.setVelocity(-xDir*vel, yDir*vel, 0);
			
			map.create(temp1);
			map.create(temp2);
		}
		else
		{
			c.addScore(50);
		}
		
		delete();
	}
}
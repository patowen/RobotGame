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
	 * @param world
	 */
	public EnemySplitting(Controller c, World world)
	{
		super(c, world);
		setStage(3.5);
	}
	
	public void setStage(double stage)
	{
		this.stage = stage;
		setRadius(0.2*stage);
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
			EnemySplitting temp1 = (EnemySplitting)c.createEntity(w, EI.EnemySplitting);
			EnemySplitting temp2 = (EnemySplitting)c.createEntity(w, EI.EnemySplitting);
			temp1.setPosition(x, y, z);
			temp2.setPosition(x, y, z);
			temp1.setStage(stage*.7);
			temp2.setStage(stage*.7);
			Player target = w.getPlayer();
			double xDir = target.getX() - x;
			double yDir = target.getY() - y;
			double mag = Math.sqrt(xDir*xDir + yDir*yDir);
			xDir/=mag; yDir/=mag; 
			double vel = 2;
			temp1.setVelocity(xDir*vel, -yDir*vel, 0);
			temp2.setVelocity(-xDir*vel, yDir*vel, 0);
			
			w.create(temp1);
			w.create(temp2);
		}
		else
		{
			c.addScore(50);
		}
		
		delete();
	}
}

package patowen95.robotgame.entity.enemy;
import patowen95.robotgame.Controller;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.EI;

/**
 * This enemy is similar in behavior to EnemyShocking, except that it divides when hit into two smaller entities
 * @author Michael Ekstrom
 */
public class EnemySplitting extends EnemyShocking
{
	private double stage;
	private double xDir = 0;
	private double yDir = 0;
	
	/**
	 * Standard constructor, uses 8 as a default size
	 * @param c
	 * @param world
	 */
	public EnemySplitting(Controller c, World world)
	{
		super(c, world);
		setStage(1/.7/.7);
	}
	
	public void setStage(double stage)
	{
		this.stage = stage;
		setRadius(0.2*stage);
		double maxSpeed;
		
		if (stage > 1.6)
		{
			maxSpeed = 4;
		}
		else if (stage > 1.3)
		{
			maxSpeed = 5;
		}
		else
		{
			maxSpeed = 7;
		}
		
		ai.setControls(maxSpeed, 2, 0, 0, 6, 1, 0.4, 1, 0, 0, 1);
	}
	
	public void step(double dt)
	{
		super.step(dt);
		
	}
	
	public void applyDamage(double amount, double x, double y, double z, double knockBack, boolean absolute)
	{
		super.applyDamage(amount, x, y, z, knockBack, absolute);
		xDir = x; yDir = y;
	}
	
	public void destroy()
	{
		if (isLocal)
		{
			if (stage > 1.1)
			{
				EnemySplitting temp1 = (EnemySplitting)c.createEntity(w, EI.EnemySplitting);
				EnemySplitting temp2 = (EnemySplitting)c.createEntity(w, EI.EnemySplitting);
				temp1.setStage(stage*.7);
				temp2.setStage(stage*.7);
				temp1.setPosition(x, y, z+height/2 - temp1.getHeight()/2);
				temp2.setPosition(x, y, z+height/2 - temp2.getHeight()/2);
				
				double mag = Math.sqrt(xDir*xDir + yDir*yDir);
				xDir/=mag; yDir/=mag; 
				double vel = 4;
				temp1.setVelocity(xV + yDir*vel, yV - xDir*vel, 0);
				temp2.setVelocity(xV - yDir*vel, yV + xDir*vel, 0);
				
				w.create(temp1);
				w.create(temp2);
			}
			else
			{
				c.addScore(50);
			}
		}
		
		delete();
	}
}

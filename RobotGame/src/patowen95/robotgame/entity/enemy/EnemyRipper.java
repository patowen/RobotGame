package patowen95.robotgame.entity.enemy;
import com.jogamp.opengl.GL2;

import patowen95.robotgame.Controller;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.Damageable;
import patowen95.robotgame.entity.Entity;
import patowen95.robotgame.entity.Player;
import patowen95.robotgame.entity.ai.AITracking;
import patowen95.robotgame.model.ModelSawHook;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Berserk Rusher enemy. Finds the player, then pauses midair. It then speeds up and 
 * flings itself at the player. If it colides, it launches the player skyward. 
 * @author Michael Ekstrom
 * @author Patrick Owen
 */
public class EnemyRipper extends Enemy implements Damageable
{
	//Constants
	private double radius;
	private double height;
	private double range;
	
	//Controls
	private double horizontalDir;
	private double verticalDir;
	
	//Controls for various attack states
	private double resttime;//Amount of time between rushes
	private double exhaustion;//Delay before next rush
	private boolean angry;//True if rushing, false if not
	private double spintime;//Time spent paused before charging
	private double spin;//Current amount of time spent spinning
	private boolean spinning;//True if spinning up, false if not
	private double temper;//Duration of rush attack
	private double rage;//Current time spent in rush attack
	private AITracking ai;//Currently equipped AI
	private AITracking calmAI;//Keeps distance but follows player
	private AITracking angryAI;//Quickly closes with the player
	
	private double rotation;//Used to rotate entity around
	
	/**
	 * Creates a new EnemyRipper.
	 * @param controller The active Controller object.
	 * @param world The world where the EnemyRipper is placed.
	 */
	public EnemyRipper(Controller controller, World world)
	{
		super(controller, world);
		
		radius = 1;
		height = 1;
		
		horizontalDir = 3*Math.PI/2;
		verticalDir = 0;
		xV = 0;
		yV = 0;
		zV = 0;
		
		range = 100;
		
		angryAI = new AITracking(c, w, this);
		angryAI.setControls(50, .75, 0, 0, 6, 1, 0, 0, 0, 0, 1);
		
		calmAI = new AITracking(c, w, this);
		calmAI.setControls(5, 1, 8, 10, 6, 1, 10, 1, 1, 3, 1);
		
		resttime = 3 + Math.random();
		spintime = 1;
		temper = 3;
		exhaustion = resttime;
		spin = 0;
		rage = 0;
		spinning = false;
		angry = false;
		ai = calmAI;
		
		hp = 3;
	}
	
	public double getRadius()
	{
		return radius;
	}
	
	public double getHeight()
	{
		return height;
	}
	
	/**
	 * Handle all actions of a single frame
	 */
	public void step(double dt)
	{
		super.step(dt);
		handleLooking(dt);
		handleCharge(dt);
		handleShocking(dt);
		if(!spinning)
			ai.performAI(dt);
		
		if (hp <= 0)
		{
			c.addScore(750);
			delete();
		}
	}
	
	//Modifies variables based on the entity's stage of attack
	private void handleCharge(double dt)
	{
		if(spin >= spintime)
			spinning = false;
		if(spinning)
			spin += dt;
		if(!angry)
			exhaustion -= dt;
		else
			rage-=dt;
		
		if(angry && rage <= 0)
		{
			angry = false;
			exhaustion = resttime;
			ai = calmAI;
			spin = 0;
		}
			
		if(!angry && exhaustion < 0 && lineOfSight())
		{
			angry = true;
			spinning = true;
			rage = temper;
			ai = angryAI;
		}
	}
	
	//Handles the enemy hitting the player
	private void handleShocking(double dt)
	{	
		if (angry)
		{
			for (Entity e : w.getEntities())
			{
				if (!(e instanceof Player))
					continue;
				Player player = (Player)e;
				if (player.isGhost()) continue;
				double xDiff = player.getX()-x, yDiff = player.getY()-y, zDiff = player.getZ()+player.getHeight()/2 - z - height/2;
				
				if (xDiff*xDiff + yDiff*yDiff + zDiff*zDiff < radius+1)
				{
					player.applyDamage(2, -xDiff/2, -yDiff/2, -1, 8, false);
					xV -= 32*xDiff; yV -= 32*yDiff; zV -= 32*zDiff;
					
					angry = false;
					exhaustion = resttime;
					ai = calmAI;
					spin = 0;
				}
			}
		}
	}
	
	//Handles the turret pointing at the player (plus an amount to account for velocity and bullet travel time)
	private void handleLooking(double dt)
	{
		/*
		 * ALGORITHM 5:
		 * Find the difference between the center of the turret and the center of the player.
		 * Add the amount by the player's velocity vector times the amount of time it takes for the bullet to reach the player.
		 * Based on coordinate differences, find the horizontal and vertical directions from the turret to the player.
		 * Modify the horizontal and vertical directions of the turret smoothly to face the player.
		 */
		Player player = w.getPlayer();
		if (player.isDead()) return;
		double xDiff = player.getX()-x, yDiff = player.getY()-y, zDiff = player.getZ()+player.getHeight()/2-z-height/2;
		
		double xyDiff = Math.sqrt(xDiff*xDiff + yDiff*yDiff);
		
		double playerDir = Math.atan2(yDiff, xDiff);
		double playerVDir = Math.atan2(zDiff, xyDiff);
		
		double dirDifference = playerDir-horizontalDir;
		double vDirDifference = playerVDir-verticalDir;
		
		while (dirDifference < -Math.PI) dirDifference += 2*Math.PI;
		while (dirDifference > Math.PI) dirDifference -= 2*Math.PI; 
		
		//Modify the horizontal and vertical directions of the turret smoothly to face the player.
		if (dirDifference > 0)
			horizontalDir += dirDifference;
		else
			horizontalDir -= -dirDifference;
		
		if (vDirDifference > 0)
			verticalDir += vDirDifference;
		else
			verticalDir -= -vDirDifference;
	}
	
	//Tests to see if the Ripper has line of sight on the player
	private boolean lineOfSight()
	{
		double xd = range*Math.cos(horizontalDir)*Math.cos(verticalDir),
				yd = range*Math.sin(horizontalDir)*Math.cos(verticalDir), zd = range*Math.sin(verticalDir);
		
		double t = w.getCollision().getBulletCollision(x, y, z+height/2, xd, yd, zd);
		double t2 = 1; //Bullet distance traveled before first detected collision
		Damageable entityToDamage = null;
		
		for (Entity entity : w.getEntities())
		{
			if (entity == this) continue;
			if (!(entity instanceof Damageable) || entity == this) continue;
			
			Damageable e = (Damageable) entity;
			
			//tTest must be less than t2 to update it.
			double tTest = w.getCollision().getEntityBulletCollision(x, y, z+height/2, xd*t, yd*t, zd*t, e);
			
			if (tTest < t2)
			{
				entityToDamage = e;
				t2 = tTest;
			}
		}
		
		if (t2 == 1) return false;
		
		if (entityToDamage instanceof Player) return true;
		
		return false;
	}
	
	//Draws the solid parts of the ripper
	public void draw(GL2 gl)
	{		
		rotation+=1+20*spin;
		GLUT glut = new GLUT();
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0.5f,0.4f,0.4f,1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0.8f,0.8f,0.8f,1}, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 8);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,0,0,1}, 0);
		
		gl.glPushMatrix();
		
		gl.glTranslated(x, y, z+height/2);
		glut.glutSolidSphere(radius/2.5, 10, 10);
		gl.glRotated(rotation, 0, 0, 1);
		
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {.5f,0.3f,0.3f,1}, 0);
		gl.glTranslated(0, 0, .05);
		for(int i = 0; i < 6; i++)
		{
			if(i%2==0)
				gl.glTranslated(0, 0, -.1);
			else
				gl.glTranslated(0, 0, .1);
			ModelSawHook.draw(gl);
			gl.glRotated(60, 0, 0, 1);
		}
		
		
		gl.glPopMatrix();
		
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0,0,0,1}, 0);
	}
	
	//applies knockback to the ripper.
	public void applyDamage(double amount, double x, double y, double z, double knockBack, boolean absolute)
	{
		hp -= amount;
		xV -= 4*knockBack*x/radius; yV -= 4*knockBack*y/radius; zV -= 4*knockBack*z/radius;
	}
}

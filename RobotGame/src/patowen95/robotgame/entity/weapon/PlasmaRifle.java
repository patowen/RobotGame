package patowen95.robotgame.entity.weapon;
import javax.media.opengl.GL2;

import patowen95.robotgame.Controller;
import patowen95.robotgame.InputHandler;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.EI;
import patowen95.robotgame.model.ModelPlasmaRifle;

import com.jogamp.opengl.util.gl2.GLUT;


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
	 * @param world The world where the player is.
	 * @param p The owner player.
	 */
	public PlasmaRifle(Controller controller, World world)
	{
		super(controller, world);
		name = "Plasma Rifle";
		
		shotDelay = 0.25;
		shotX = .8; shotY = .05; shotZ = -.05;
		charge = 0;
		energyUse = 2;
	}
	
	//Handles firing the rifle. 
	protected void handleFiring(double dt)
	{
		//Fire if the mouse button is pressed.
		if (input.getMouseButton(InputHandler.FIRE) && charge <= 0 && energy >= energyUse)
		{
			EntityBullet bullet = (EntityBullet)c.createEntity(w, EI.EntityBullet);
			double xDir = Math.cos(horizontalDir)*Math.cos(verticalDir),
					yDir = Math.sin(horizontalDir)*Math.cos(verticalDir), zDir = Math.sin(verticalDir);
			
			double xDisp = shotX*Math.cos(verticalDir) - shotZ*Math.sin(verticalDir);
			double zDisp = shotX*Math.sin(verticalDir) + shotZ*Math.cos(verticalDir);
			
			double yDisp = xDisp*Math.sin(horizontalDir) - shotY*Math.cos(horizontalDir);
			xDisp = xDisp*Math.cos(horizontalDir) + shotY*Math.sin(horizontalDir);
			
			//t is to prevent bullets from spawning through walls.
			
			double t = w.getCollision().getBulletCollision(x, y, z, xDisp, yDisp, zDisp);
			
			if (t == 1)
			{
				//Create the bullet
				energy -= energyUse;
				bullet.setPosition(x+xDisp, y+yDisp, z+zDisp);
				int vel = 60;
				
				bullet.setVelocity(vel*xDir, vel*yDir, vel*zDir);
				bullet.setDamage(1, 1);//damage, knockback
				bullet.setColor(0, 1, 0);
				
				bullet.setOwner(player);
				
				w.create(bullet);
				
				charge = shotDelay;
				
				//Laser gun sound
				c.getSoundHandler().playSound(0);
			}
		}
		
		//Weapon recharging
		charge -= dt;
	}
	
	//Draws the gun
	public void draw(GL2 gl)
	{
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {.7f, .8f, .8f,1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,0,0,1}, 0);

		double xDisp = shotX*Math.cos(verticalDir) - shotZ*Math.sin(verticalDir);
		double zDisp = shotX*Math.sin(verticalDir) + shotZ*Math.cos(verticalDir);
		
		double yDisp = xDisp*Math.sin(horizontalDir) - shotY*Math.cos(horizontalDir);
		xDisp = xDisp*Math.cos(horizontalDir) + shotY*Math.sin(horizontalDir);
		
		gl.glPushMatrix();
		gl.glTranslated(x+ xDisp, y + yDisp, z+ zDisp);
		gl.glRotated(horizontalDir*180/Math.PI + 180, 0, 0, 1);
		gl.glRotated(verticalDir*180/Math.PI + 90, 0, 1, 0);
		gl.glTranslated(0,  0, .6);
		gl.glRotated(270, 0, 1, 0);
		
		ModelPlasmaRifle.draw(gl);
			
		gl.glPopMatrix();
	}
	
	//draws the transparent parts of the gun
	public void draw2(GL2 gl)
	{
		GLUT glut = new GLUT();
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0, 0, 0, (float)(.3 * Math.random())}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0f,1f,0,1}, 0);

		double xDisp = shotX*Math.cos(verticalDir) - shotZ*Math.sin(verticalDir);
		double zDisp = shotX*Math.sin(verticalDir) + shotZ*Math.cos(verticalDir);
		
		double yDisp = xDisp*Math.sin(horizontalDir) - shotY*Math.cos(horizontalDir);
		xDisp = xDisp*Math.cos(horizontalDir) + shotY*Math.sin(horizontalDir);
		
		gl.glPushMatrix();
		gl.glTranslated(x+ xDisp, y + yDisp, z+ zDisp);
		gl.glRotated(horizontalDir*180/Math.PI + 180, 0, 0, 1);
		gl.glRotated(verticalDir*180/Math.PI + 90, 0, 1, 0);
		gl.glTranslated(0, 0, .5);
		glut.glutSolidSphere(.02*Math.min(energy, energyUse)/energyUse, 24, 8);
		
		gl.glPopMatrix();
	}
}

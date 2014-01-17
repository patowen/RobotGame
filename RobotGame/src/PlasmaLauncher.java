import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;


/**
 * Handles the controls and the firing of the plasma launcher, a 
 * powerful AoE attack that deals heavy damage
 * @author Patrick Owen
 * @author Michael Ekstrom
 */
public class PlasmaLauncher extends Weapon
{
	private double shotX, shotY, shotZ; //Location of shot relative to player
	private double spin;//Used to rotate the weapon
	
	/**
	 * Constructs a PlasmaRifle object.
	 * @param controller The Controller object.
	 * @param world The world where the player is.
	 * @param p The owner player.
	 */
	public PlasmaLauncher(Controller controller, World world)
	{
		super(controller, world);
		name = "Ion Cannon";
		
		shotDelay = .5;
		shotX = 0.8; shotY = 0.05; shotZ = -0.05;
		charge = -1;//This compensates for spin of gun. Does not affect performance
		energyUse = 33;
		spin = 0;
	}	
	
	//Handles firing the rifle. 
	protected void handleFiring(double dt)
	{
		//Fire if the mouse button is pressed.
		if (input.getMouseButton(InputHandler.FIRE) && charge <= 0 && energy >= energyUse)
		{
			EntityPlasmaBolt bolt = (EntityPlasmaBolt)c.createEntity(w, EI.EntityPlasmaBolt);
			bolt.setColor(.2f, .8f, 1f);
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
				bolt.setPosition(x+xDisp, y+yDisp, z+zDisp);
				int vel = 30;
				
				bolt.setVelocity(vel*xDir, vel*yDir, vel*zDir);
				bolt.setOwner(player);
				
				w.create(bolt);
				
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
		spin += 1+ Math.max(charge+1, 0)*5;
		
		//Gamejolt.com
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
		gl.glTranslated(0,  0, .5);
		gl.glRotated(spin, 0, 0, 1);
		gl.glTranslated(0,  0, .1);
		gl.glRotated(270, 0, 1, 0);
		
		ModelPlasmaLauncher.draw(gl);
		
		gl.glPopMatrix();
	}
	
	//draws the transparent parts of the gun
	public void draw2(GL2 gl)
	{
		GLUT glut = new GLUT();
		double radius = .0275*Math.min(energy, energyUse)/energyUse;
		double sin = Math.sin(3*charge)*radius*.7;
		double cos = Math.cos(3*charge)*radius*.7;
		//Color
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0, 0, 0, .5f}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0f,.8f,1f,1f}, 0);

		double xDisp = shotX*Math.cos(verticalDir) - shotZ*Math.sin(verticalDir);
		double zDisp = shotX*Math.sin(verticalDir) + shotZ*Math.cos(verticalDir);
		
		double yDisp = xDisp*Math.sin(horizontalDir) - shotY*Math.cos(horizontalDir);
		xDisp = xDisp*Math.cos(horizontalDir) + shotY*Math.sin(horizontalDir);
		
		gl.glPushMatrix();
		gl.glTranslated(x+ xDisp, y + yDisp, z+ zDisp);
		gl.glRotated(horizontalDir*180/Math.PI + 180, 0, 0, 1);
		gl.glRotated(verticalDir*180/Math.PI + 90, 0, 1, 0);
		gl.glTranslated(0, 0, .5);
		gl.glRotated(spin, 0, 0, 1);
		
		gl.glPushMatrix();
		gl.glTranslated(sin, 0, cos);
		glut.glutSolidSphere(radius/4, 10, 10);
		gl.glTranslated(-sin, cos, sin-cos);
		glut.glutSolidSphere(radius/4, 10, 10);
		gl.glTranslated(cos, sin-cos, -sin);
		glut.glutSolidSphere(radius/4, 10, 10);
		gl.glPopMatrix();
		
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0, 0, 0, (float)(.3 * Math.random())}, 0);
		glut.glutSolidSphere(radius, 24, 8);
		
		gl.glPopMatrix();
	}
}

import java.util.ArrayList;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;


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
	 * @param world The world where the player is.
	 * @param p The owner player.
	 */
	public PlasmaSword(Controller controller, World world)
	{
		super(controller, world);
		name = "Laser Sword";
		
		shotDelay = .4;
		charge = 0;
		steps = 15;
		currentstep = steps;
		bladelength = 3;
		knockback = 10;
		damage = 2;
		arclength = .4;
		energyUse = 5;
	}
	
	protected void readState(NetworkPacket data)
	{
		super.readState(data);
		
		ht = data.getDouble();
		vt = data.getDouble();
		currentstep = data.getInt();
	}
	
	protected void writeState(NetworkPacket data)
	{
		super.writeState(data);
		
		data.addDoubles(ht, vt);
		data.addInt(currentstep);
	}
	
	/**
	 * Calls Weapon's setPosition but slightly decrements Z so that the sword is swung below eye level
	 */
	public void setPosition(double xPos, double yPos, double zPos, double hDir, double vDir)
	{
		super.setPosition(xPos, yPos, zPos, hDir, vDir);
		z-=.1;
		verticalDir+=.05;
	}
	
	//Handles sword swing
	protected void handleFiring(double dt)
	{
		if (!player.isLocal())
			return;
		
		//Fire if the mouse button is pressed.
		if (input.getMouseButton(InputHandler.FIRE) && charge <= 0 && energy >= energyUse)
		{
			energy -= energyUse;
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
			
			double xDir = Math.cos(horizontalDir)*Math.cos(verticalDir),
					yDir = Math.sin(horizontalDir)*Math.cos(verticalDir), zDir = Math.sin(verticalDir);
			
			
			double t = w.getCollision().getBulletCollision(x, y, z, bladelength*xDir, bladelength*yDir, bladelength*zDir);
			double t2 = 1; //Bullet distance traveled before first detected collision
			Damageable entityToDamage = null;
			
			
			
			if (t < t2)
			{
				EntityFade spark = (EntityFade)c.createEntity(w, EI.EntityFade);
				spark.setRadius(.045);
				spark.setDuration(.25);
				spark.setColor(.25f, .5f, 1f);
				spark.setPosition(x+bladelength*xDir*t, y+bladelength*yDir*t, z+bladelength*zDir*t);
				w.create(spark);
			}
			
			double tTest = 1;
			for (Entity entity : w.getEntities())
			{
				if (entity == player) continue;
				if (!(entity instanceof Damageable)) continue;
				
				Damageable e = (Damageable) entity;
				
				//tTest must be less than t2 to update it.
				tTest = w.getCollision().getEntityBulletCollision(x, y, z, bladelength*xDir*t, bladelength*yDir*t, bladelength*zDir*t, e);
				
				
				if (tTest < 1)
				{
					entityToDamage = e;
					for(Damageable d: hit)
					{
						if(e == d)
						{
							entityToDamage = null;
						}
					}
					//Bullet hits entity.
					if (entityToDamage != null)
					{
						double totalVel = Math.sqrt(xDir*xDir + yDir*yDir + zDir*zDir);
						entityToDamage.applyDamage(damage, -xDir/totalVel, -yDir/totalVel, -zDir/totalVel, knockback, false);
						EntityExplosion boom = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
						boom.setPosition(x+bladelength*xDir*tTest, y+bladelength*yDir*tTest, z+bladelength*zDir*tTest);
						boom.setFinalRadius(.2);
						boom.setDuration(.25);
						boom.setColor(1f, .5f, .25f);
						c.getSoundHandler().playSound(1, x+bladelength*xDir*tTest, y+bladelength*yDir*tTest, z+bladelength*zDir*tTest);
						w.create(boom);
					}
					hit.add(e);
				}
			}
		}
		
		//Weapon recharging
		charge -= dt;
	}
	
	/**
	 * Draws the interior and handle of the sword
	 * @param gl
	 */
	public void draw(GL2 gl)
	{		
		if(currentstep < steps)
		{
			GLUT glut = new GLUT();
			
			if (!player.isLocal())
			{
				horizontalDir += ht;
				verticalDir += vt;
			}
			
			//Color
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {1,1,1,1}, 0);
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,0,0,1}, 0);
			
			
			double xDir = Math.cos(horizontalDir)*Math.cos(verticalDir),
					yDir = Math.sin(horizontalDir)*Math.cos(verticalDir), zDir = Math.sin(verticalDir);
			
			
			
			gl.glPushMatrix();
			gl.glTranslated(x+bladelength*xDir, y+bladelength*yDir, z+bladelength*zDir);
			gl.glRotated(horizontalDir*180/Math.PI + 180, 0, 0, 1);
			gl.glRotated(verticalDir*180/Math.PI + 90, 0, 1, 0);
			glut.glutSolidCylinder(.01, bladelength-.25, 12, 1);
			gl.glTranslated(0, 0, bladelength-.25);
			
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {.35f,.35f,.35f,1}, 0);
			glut.glutSolidCylinder(.03, .04, 12, 1);
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {.45f,.45f,.45f,1}, 0);
			glut.glutSolidCylinder(.0125, .2, 12, 1);
			
			gl.glPopMatrix();
		}
	}
	
	/**
	 * Draws transparent parts of the sword, called after
	 * all draw methods are called
	 * @param gl
	 */
	public void draw2(GL2 gl)
	{
		if(currentstep < steps)
		{
			GLUT glut = new GLUT();
			
			//Color
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {.25f,.5f,1,.5f}, 0);
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,0,0,1}, 0);
			
			
			double xDir = Math.cos(horizontalDir)*Math.cos(verticalDir),
					yDir = Math.sin(horizontalDir)*Math.cos(verticalDir), zDir = Math.sin(verticalDir);
			
			gl.glPushMatrix();
			gl.glTranslated(x+bladelength*xDir, y+bladelength*yDir, z+bladelength*zDir);
			gl.glRotated(horizontalDir*180/Math.PI + 180, 0, 0, 1);
			gl.glRotated(verticalDir*180/Math.PI + 90, 0, 1, 0);
			glut.glutSolidCylinder(.025, bladelength-.25, 12, 1);
			
			gl.glPopMatrix();
		}
	}
}

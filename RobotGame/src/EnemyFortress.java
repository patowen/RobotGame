import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * This class contains an enemy that will shoot EntityRockets at the player
 * @author Michael Ekstrom
 * @author Patrick Owen
 */

public class EnemyFortress extends Enemy implements Damageable
{
	//Constants
	private double radius;
	private double height;
	private double shotHeight;
	private double shotDistance;
	private double shotSpeed;
	private double shotDelay;
	
	//Controls
	private double horizontalDir;
	private double verticalDir;
	private double charge;
	private int side;
	
	private AITracking ai;
	
	/**
	 * Creates a new EnemyFortress
	 * @param controller The active Controller object.
	 * @param world The world where the EnemyFortress is placed.
	 */
	public EnemyFortress(Controller controller, World world)
	{
		super(controller, world);
		
		radius = 0.8;
		height = 1.5;
		
		horizontalDir = 3*Math.PI/2;
		verticalDir = 0;
		xV = 0;
		yV = 0;
		zV = 0;
		
		shotDelay = 2;
		shotHeight = 0.4;
		shotDistance = 1;
		shotSpeed = 8;
		
		charge = shotDelay;
		
		ai = new AITracking(c, w, this);
		ai.setControls(3, 1, 8, 9, 6, 1, 1, 1, 1, 3, 1);
		
		hp = 10;
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
	 * Shoots at the player if he is within range.
	 */
	public void step(double dt)
	{
		super.step(dt);
		
		handleShooting(dt);
		handleLooking(dt);
		ai.performAI(dt);
		
		if (hp <= 0)
		{
			c.addScore(2000);
			deathThroes();
			delete();
		}
	}
	
	//Handles the turret firing its bullets
	private void handleShooting(double dt)
	{
		charge -= dt;
		
		if (w.getPlayer().isDead()) return;
		
		if (charge < 0)
		{
			side++;
			if (side > 3)
				side = 0;
			double boost = Math.PI/2 * side;
			
			double xDir = Math.cos(horizontalDir+boost)*Math.cos(verticalDir),
					yDir = Math.sin(horizontalDir+boost)*Math.cos(verticalDir), zDir = Math.sin(verticalDir);
			
			EntityRocket rocket = (EntityRocket)c.createEntity(w, EI.EntityRocket);
			
			if (w.getCollision().getPlayerCollision
					(x, y, z+height/2, xDir*shotDistance, yDir*shotDistance, zDir*shotDistance, rocket.getRadius(), rocket.getHeight()) == 1)
			{
				rocket.setPosition(x+xDir*shotDistance, y+yDir*shotDistance, z+zDir*shotDistance+shotHeight);
				rocket.setVelocity(shotSpeed*xDir, shotSpeed*yDir, shotSpeed*zDir);
				rocket.setTarget(w.getPlayer());
				rocket.setOwner(this);
				
				w.create(rocket);
			}
			
			charge = shotDelay;
		}
	}
	
	//Handles the turret pointing at the player
	private void handleLooking(double dt)
	{
		horizontalDir += dt;
	}
	
	public void draw(GL2 gl)
	{
		gl.glPushMatrix();
		
		GLUT glut = new GLUT();
		
		double xDir = Math.cos(horizontalDir) * .5, yDir = Math.sin(horizontalDir) * .5;
		
		//Color of sphere
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0.1f,0.05f,0.05f,1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0.4f,0.4f,0.4f,1}, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 8);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,0,0,1}, 0);
		
		gl.glTranslated(x, y, z+height/2);
		glut.glutSolidSphere(.75, 10, 10);
		
		//Color of turrets
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0.15f,0.1f,0.1f,1}, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0.4f,0.4f,0.4f,1}, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 8);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, new float[] {0,0,0,1}, 0);
		
		gl.glTranslated(xDir, yDir , 0);
		gl.glRotated(horizontalDir*180/Math.PI + 180, 0, 0, 1);
		ModelTurret.draw(gl);
		gl.glRotated(-(horizontalDir*180/Math.PI + 180), 0, 0, 1);
		gl.glTranslated(-xDir, -yDir , 0);
		
		gl.glTranslated(-xDir, -yDir , 0);
		gl.glRotated(horizontalDir*180/Math.PI, 0, 0, 1);
		ModelTurret.draw(gl);
		gl.glRotated(-(horizontalDir*180/Math.PI ), 0, 0, 1);
		gl.glTranslated(xDir, yDir , 0);
		
		gl.glRotated(90, 0, 0, 1);
		
		gl.glTranslated(xDir, yDir , 0);
		gl.glRotated(horizontalDir*180/Math.PI + 180, 0, 0, 1);
		ModelTurret.draw(gl);
		gl.glRotated(-(horizontalDir*180/Math.PI + 180), 0, 0, 1);
		gl.glTranslated(-xDir, -yDir , 0);
		
		gl.glTranslated(-xDir, -yDir , 0);
		gl.glRotated(horizontalDir*180/Math.PI, 0, 0, 1);
		ModelTurret.draw(gl);
		gl.glRotated(-(horizontalDir*180/Math.PI ), 0, 0, 1);
		gl.glTranslated(xDir, yDir , 0);

		
		gl.glPopMatrix();
		
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[] {0,0,0,1}, 0);
	}

	public void applyDamage(double amount, double x, double y, double z, double knockBack, boolean absolute)
	{
		hp -= amount;
	}
	
	/**
	 * Causes several explosions where the fortress was. Called upon death
	 */
	public void deathThroes()
	{
		double xDir = Math.cos(horizontalDir) * 1, yDir = Math.sin(horizontalDir) * 1;
		
		EntityExplosion blast = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
		blast.setColor(1f, .5f, 0f);
		blast.setDuration(1);
		blast.setRadius(radius/2);
		blast.setFinalRadius(radius*2);
		blast.setPosition(x, y, z+height/2);
		w.create(blast);
		
		double startRadius = .3;
		double endRadius = .75;
		double duration = 1;
		
		EntityExplosion blast1 = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
		blast1.setColor(.5f, .25f, 0f);
		blast1.setDuration(duration);
		blast1.setRadius(startRadius);
		blast1.setFinalRadius(endRadius);
		blast1.setPosition(x + xDir, y + yDir, z+height/2);
		w.create(blast1);
		
		EntityExplosion blast2 = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
		blast2.setColor(.5f, .25f, 0f);
		blast2.setDuration(duration);
		blast2.setRadius(startRadius);
		blast2.setFinalRadius(endRadius);
		blast2.setPosition(x - xDir, y - yDir, z+height/2);
		w.create(blast2);
		
		xDir = Math.cos(horizontalDir + Math.PI/2) * .5; yDir = Math.sin(horizontalDir + Math.PI/2) * .5;
		
		
		EntityExplosion blast3 = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
		blast3.setColor(.5f, .25f, 0f);
		blast3.setDuration(duration);
		blast3.setRadius(startRadius);
		blast3.setFinalRadius(endRadius);
		blast3.setPosition(x + xDir, y + yDir, z+height/2);
		w.create(blast3);
		
		EntityExplosion blast4 = (EntityExplosion)c.createEntity(w, EI.EntityExplosion);
		blast4.setColor(.5f, .25f, 0f);
		blast4.setDuration(duration);
		blast4.setRadius(startRadius);
		blast4.setFinalRadius(endRadius);
		blast4.setPosition(x - xDir, y - yDir, z+height/2);
		w.create(blast4);
	}
}

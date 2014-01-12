import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Shows the heads up display and all other overlays
 * @author Patrick Owen
 */
public class HUD
{
	private Controller c;
	private GameMap map;
	
	private TextRenderer scoreRenderer;
	private TextRenderer[] weaponsRenderers;
	
	private String[] weaponsList;
	private double equivalent12;//Approx. Equivalent of 12 point font for the current window size
	
	private ArrayList<HitMark> hitMarks;
	private ArrayList<HitMark> deletionQueue;
	private ArrayList<HitMark> creationQueue;
	
	private double slowHP;
	
	/**
	 * Initializes the HUD.
	 * @param controller
	 */
	public HUD(Controller controller, GameMap gameMap)
	{
		c = controller;
		map = gameMap;
		
		equivalent12 = Math.min(c.getWidth(), c.getHeight())/55;
		
		scoreRenderer = new TextRenderer(new Font("Time New Roman", Font.PLAIN, (int)(2*equivalent12)), true, true);
		weaponsRenderers = new TextRenderer[map.getPlayer().getWeapons().length];
		for(int i = 0; i < weaponsRenderers.length; i++)
			weaponsRenderers[i] = new TextRenderer(new Font("Time New Roman", Font.PLAIN, (int)(2*equivalent12)), true, true);
		
		hitMarks = new ArrayList<HitMark>();
		deletionQueue = new ArrayList<HitMark>();
		creationQueue = new ArrayList<HitMark>();
		
		slowHP = 0;
		
		weaponsList = new String[weaponsRenderers.length];
		loadWeaponsList();
	}
	
	/**
	 * Removes all hit marks from the screen.
	 */
	public void clearHitMarks()
	{
		for (HitMark hitMark : hitMarks)
		{
			deletionQueue.add(hitMark);
		}
	}
	
	/**
	 * Adds a hit mark from the screen.
	 * @param angle The angle in radians at which the mark appears (counterclockwise,
	 * 0 radians is to the right).
	 */
	public void addHitMark(double angle)
	{
		creationQueue.add(new HitMark(angle));
	}
	
	/**
	 * Removes the specified hit mark.
	 * @param hitMark Reference to the hit mark to remove.
	 */
	public void removeHitMark(HitMark hitMark)
	{
		deletionQueue.add(hitMark);
	}
	
	/**
	 * Runs a step of the menu to allow checking if the menu needs updating.
	 * @param dt
	 */
	public void step(double dt)
	{
		for (HitMark hitMark : hitMarks)
		{
			hitMark.step(dt);
		}
		
		if (map.getPlayer().isDead() && !hitMarks.isEmpty())
			clearHitMarks();
		
		for (HitMark hitMark : deletionQueue)
		{
			hitMarks.remove(hitMark);
		}
		
		for (HitMark hitMark : creationQueue)
		{
			hitMarks.add(hitMark);
		}
		
		deletionQueue.clear();
		creationQueue.clear();
		
		double hp = map.getPlayer().getHP();
		if (slowHP < hp)
			slowHP = Math.min(hp, slowHP+8*dt);
		else if (slowHP > hp)
			slowHP = Math.max(hp, slowHP-2*dt);
	}
	
	/**
	 * Draws the menu. The width and height of the projection
	 * are given as parameters as a guideline to drawing the text.
	 * @param gl
	 * @param width
	 * @param height Orthographic projection width and height.
	 */
	public void draw(GL2 gl, double width, double height)
	{
		equivalent12 = Math.min(width, height)/55;
		
		for (HitMark hitMark : hitMarks)
		{
			hitMark.draw(gl, width, height);
		}
		
		drawCrosshairs(gl, width, height);
		drawHealthbar(gl, width, height);
		drawEnergyBar(gl, width, height);
		drawScore(gl, width, height);
		drawWeaponsList(gl, width, height);
	}
	
	//Displays the score in the upper right-hand corner of the screen
	private void drawScore(GL2 gl, double width, double height)
	{
		scoreRenderer.beginRendering((int)width, (int)height);
		scoreRenderer.setColor(1f, 1f, 1f, 1f);
		Rectangle2D bounds = scoreRenderer.getBounds(Integer.toString(c.getScore()));
		scoreRenderer.draw(Integer.toString(c.getScore()), (int)width - (int)bounds.getWidth() - 5,
				(int) height - (int)bounds.getHeight() - 5);
		scoreRenderer.endRendering();
	}
	
	//Displays the health in the upper left-hand corner of the screen
	private void drawHealthbar(GL2 gl, double width, double height)
	{
		double hp = map.getPlayer().getHP(), maxHP = map.getPlayer().getMaxHP();
		double barWidth = width/9;
		double barHeight = height/40;
		double barSpace = width/120;
		
		//Background
		gl.glColor3d(0, 0, 0);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex2d(barSpace, height-barHeight-barSpace);
		gl.glVertex2d(barSpace+barWidth, height-barHeight-barSpace);
		gl.glVertex2d(barSpace+barWidth, height-barSpace);
		gl.glVertex2d(barSpace, height-barSpace);
		gl.glEnd();
		
		//Health subtracting
		if (slowHP > hp)
		{
			gl.glColor3d(255, 0, 0);
			gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2d(barSpace, height-barHeight-barSpace);
			gl.glVertex2d(barSpace+barWidth*slowHP/maxHP, height-barHeight-barSpace);
			gl.glVertex2d(barSpace+barWidth*slowHP/maxHP, height-barSpace);
			gl.glVertex2d(barSpace, height-barSpace);
			gl.glEnd();
		}
		
		//Bar
		gl.glColor3d(0, 255, 0);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex2d(barSpace, height-barHeight-barSpace);
		gl.glVertex2d(barSpace+barWidth*hp/maxHP, height-barHeight-barSpace);
		gl.glVertex2d(barSpace+barWidth*hp/maxHP, height-barSpace);
		gl.glVertex2d(barSpace, height-barSpace);
		gl.glEnd();
		
		//Health adding
		if (slowHP < hp)
		{
			gl.glColor3d(255, 255, 255);
			gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2d(barSpace+barWidth*slowHP/maxHP, height-barHeight-barSpace);
			gl.glVertex2d(barSpace+barWidth*hp/maxHP, height-barHeight-barSpace);
			gl.glVertex2d(barSpace+barWidth*hp/maxHP, height-barSpace);
			gl.glVertex2d(barSpace+barWidth*slowHP/maxHP, height-barSpace);
			gl.glEnd();
		}
		
		//Border
		gl.glColor3d(0, 0, 0);
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glVertex2d(barSpace, height-barHeight-barSpace);
		gl.glVertex2d(barSpace+barWidth, height-barHeight-barSpace);
		gl.glVertex2d(barSpace+barWidth, height-barSpace);
		gl.glVertex2d(barSpace, height-barSpace);
		gl.glEnd();
	}
	
	//Draws the current weapon's energy storage
	private void drawEnergyBar(GL2 gl, double width, double height)
	{
		Weapon weapon = map.getPlayer().getCurrentWeapon();
		double energy = weapon.getEnergy();
		double max = weapon.getMaxEnergy();
		double shot = weapon.getEnergyUse();
		double shotbuffer = 5;//Extra edge on shot box
		
		double barWidth = width/4;
		double barHeight = height/16;
		double barX = width/2 - barWidth/2;
		double barY = height/16 - barHeight/2;
		
		//Bar
		gl.glColor4f(.2f, .8f, 1f, .5f);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex2d(barX, height-barHeight-barY);
		gl.glVertex2d(barX+barWidth*energy/max, height-barHeight-barY);
		gl.glVertex2d(barX+barWidth*energy/max, height-barY);
		gl.glVertex2d(barX, height-barY);
		gl.glEnd();
		
		
		//Border
		gl.glColor3d(0, 200, 255);
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glVertex2d(barX, height-barHeight-barY);
		gl.glVertex2d(barX+barWidth, height-barHeight-barY);
		gl.glVertex2d(barX+barWidth, height-barY);
		gl.glVertex2d(barX, height-barY);
		gl.glEnd();
		
		//Shot of Energy Marker
		gl.glColor4f(.2f, 1f, .8f, .25f);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex2d(barX - shotbuffer, height-barHeight-barY - shotbuffer);
		gl.glVertex2d(barX+barWidth*shot/max, height-barHeight-barY - shotbuffer);
		gl.glVertex2d(barX+barWidth*shot/max, height-barY + shotbuffer);
		gl.glVertex2d(barX - shotbuffer, height-barY + shotbuffer);
		gl.glEnd();
		
	}
	
	//Draws the crosshairs in the center of the screen
	private void drawCrosshairs(GL2 gl, double width, double height)
	{
		gl.glColor4d(1, 1, 1, 0.5);
		
		double thickness = 0.002*height;
		double size = 0.02*height;
		
		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex2d(width/2+thickness, height/2+thickness);
		gl.glVertex2d(width/2+thickness, height/2+size);
		gl.glVertex2d(width/2-thickness, height/2+size);
		gl.glVertex2d(width/2-thickness, height/2+thickness);
		gl.glVertex2d(width/2-size, height/2+thickness);
		gl.glVertex2d(width/2-size, height/2-thickness);
		gl.glVertex2d(width/2-thickness, height/2-thickness);
		gl.glVertex2d(width/2-thickness, height/2-size);
		gl.glVertex2d(width/2+thickness, height/2-size);
		gl.glVertex2d(width/2+thickness, height/2-thickness);
		gl.glVertex2d(width/2+size, height/2-thickness);
		gl.glVertex2d(width/2+size, height/2+thickness);
		gl.glEnd();
	}
	
	//Loads the list of weapons into the weaponsList String
	private void loadWeaponsList()
	{
		Weapon[] list = map.getPlayer().getWeapons();
		for(int i = 0; i < list.length; i++)
		{
			weaponsList[i] = i+1 + " "  + list[i].getName() + " ";
		}
		
	}
	
	//Draws the weapons list to the screen and highlights the current weapon
	private void drawWeaponsList(GL2 gl, double width, double height)
	{
		Weapon[] list = map.getPlayer().getWeapons();
		Weapon current = map.getPlayer().getCurrentWeapon();

		
		for(int i = 0; i < weaponsRenderers.length; i++)
		{
			weaponsRenderers[i].beginRendering((int)width, (int)height);
			if(list[i] != current)
				weaponsRenderers[i].setColor(1f, 1f, 1f, 1f);
			else
				weaponsRenderers[i].setColor(0f, 1f, 1f, 1f);
			Rectangle2D bounds = weaponsRenderers[i].getBounds(weaponsList[i]);
			int xVal = (int)(width/64);
			int yVal = (int)(width/64 + bounds.getHeight()*weaponsRenderers.length - i*2*equivalent12);
			weaponsRenderers[i].draw(weaponsList[i], xVal, yVal);
			weaponsRenderers[i].endRendering();
			
		}
	}
	
	//A hit mark class that handles timing and display of said hit mark.
	private class HitMark
	{
		private double theta;
		private double alpha;
		
		private double x;
		private double y;
		
		public HitMark(double angle)
		{
			theta = angle;
			alpha = 0.5;
			
			double angleSwept = 0.4;
			x = Math.cos(angleSwept);
			y = Math.sin(angleSwept);
		}
		
		public void step(double dt)
		{
			alpha -= dt/4;
			
			if (alpha < 0)
			{
				removeHitMark(this);
			}
		}
		
		public void draw(GL2 gl, double width, double height)
		{
			gl.glColor4d(1, 0, 0, alpha);
			
			double innerRadius = height/2*0.9;
			double outerRadius = height/2;
			
			gl.glPushMatrix();
			gl.glTranslated(width/2, height/2, 0);
			gl.glRotated(theta*180/Math.PI, 0, 0, 1);
			
			gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2d(innerRadius*x, -innerRadius*y);
			gl.glVertex2d(outerRadius*x, -outerRadius*y);
			gl.glVertex2d(outerRadius*x, outerRadius*y);
			gl.glVertex2d(innerRadius*x, innerRadius*y);
			gl.glEnd();
			
			gl.glPopMatrix();
		}
	}
}

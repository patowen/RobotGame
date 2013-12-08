import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Handles the menu that appears when the user pauses the game.
 * @author Patrick Owen
 * @author Michael Ekstrom
 */
public class PauseMenu extends Menu
{	
	private boolean paused, quit;
	
	private MenuButton buttonResume, buttonExit, buttonMainMenu;
	
	/**
	 * Initializes the pause menu.	
	 * @param controller
	 */
	public PauseMenu(Controller controller, double width, double height)
	{
		super(controller, width, height);
		
		buttonResume = new MenuButton(this, "Resume", "Impact", 0.5, 0.5, 3);
		buttonExit = new MenuButton(this, "Exit Game", "Impact", 0.5, 0.3, 3);
		buttonMainMenu = new MenuButton(this, "Return to Main Menu", "Impact", .5, .4, 3);
		
		items.add(new MenuLabel(this, "You have paused the game", "Impact", 0.5, 0.75, 5));
		items.add(buttonResume);
		items.add(buttonMainMenu);
		items.add(buttonExit);
		
		setSize(width, height);
		
		quit = false;
	}
	
	/**
	 * Updates the PauseMenu to reflect new height and width values.
	 * @param w Width
	 * @param h Height
	 */
	protected void setSize(double w, double h)
	{
		super.setSize(w, h);
	}

	
	/**
	 * Tells the pause menu that it should be running
	 */
	public void pause()
	{
		paused = true;
	}
	
	/**
	 * Tells the pause menu that it should not be running
	 */
	public void unPause()
	{
		paused = false;
	}
	
	/**
	 * Draws the menu. The width and height of the projection
	 * are given as parameters as a guideline to drawing the text.
	 * @param gl
	 */
	public void draw(GL2 gl)
	{
		gl.glColor4d(0, 0, 0, 0.5);
		gl.glBegin(GL2.GL_QUADS);
		
		gl.glVertex2d(0, 0);
		gl.glVertex2d(width, 0);
		gl.glVertex2d(width, height);
		gl.glVertex2d(0, height);
		
		gl.glEnd();
		
		super.draw(gl);
	}
	
	/**
	 * Draws a string of text given its center coordinates
	 * @param tr TextRenderer being used for this string
	 * @param s string to be drawn
	 * @param x desired x coordinate
	 * @param y desired y coordinate
	 */
	public void drawStringCenter(TextRenderer tr, String s, int x, int y)
	{
		x -= tr.getBounds(s).getWidth()/2;
		y -= tr.getBounds(s).getHeight()/2;
		tr.draw(s, x, y);
	}
	
	/**
	 * Returns whether or not the game should be paused
	 */
	public boolean isPaused()
	{
		return paused;
	}
	
	/**
	 * Returns whether or not the game should be exited
	 */
	public boolean quit()
	{
		return quit;
	}
	
	/**
	 * Performs an action called by one of the menu items.
	 * @param item Object that called the method
	 */
	public void handleAction(MenuItem item)
	{
		if (item == buttonResume)
		{
			paused = false;
			input.readMouse();
		}
		else if (item == buttonExit)
		{
			System.exit(0);
		}
		if (item == buttonMainMenu)
		{
			paused = false;
			c.setCurrentMenu(c.mainMenu, true);
			c.resetScore();
		}
	}
}

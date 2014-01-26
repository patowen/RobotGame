package patowen95.robotgame.menu;
import javax.media.opengl.GL2;

import patowen95.robotgame.Controller;
import patowen95.robotgame.InputHandler;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Handles the menu that appears when the user pauses the game.
 * @author Patrick Owen
 * @author Michael Ekstrom
 */
public class PauseMenu extends Menu
{	
	private MenuButton buttonResume, buttonMainMenu;
	
	/**
	 * Initializes the pause menu.	
	 * @param controller
	 */
	public PauseMenu(Controller controller)
	{
		super(controller);
		
		buttonResume = new MenuButton(this, "Resume", "Impact", 0.5, 0.5, 3);
		if (c.isMultiplayer())
			buttonMainMenu = new MenuButton(this, "Disconnect", "Impact", .5, .4, 3);
		else
			buttonMainMenu = new MenuButton(this, "Return to Main Menu", "Impact", .5, .4, 3);
		
		items.add(new MenuLabel(this, "You have paused the game", "Impact", 0.5, 0.75, 5));
		items.add(buttonResume);
		items.add(buttonMainMenu);
	}
	
	public void step(double dt)
	{
		super.step(dt);
		if (input.getKeyPressed(InputHandler.PAUSE))
		{
			input.cancelKey(InputHandler.PAUSE);
			c.setPaused(false);
		}
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
		gl.glVertex2d(c.getWidth(), 0);
		gl.glVertex2d(c.getWidth(), c.getHeight());
		gl.glVertex2d(0, c.getHeight());
		
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
	 * Performs an action called by one of the menu items.
	 * @param item Object that called the method
	 */
	public void handleAction(MenuItem item)
	{
		if (item == buttonResume)
		{
			c.setPaused(false);
		}
		else if (item == buttonMainMenu)
		{
			if (c.isMultiplayer())
			{
				c.disconnect();
				c.setCurrentMenu(new MainMenu(c));
			}
			else
			{
				c.setCurrentMenu(new MainMenu(c));
				c.resetScore();
			}
		}
	}
}

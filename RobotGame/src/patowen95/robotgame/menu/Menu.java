package patowen95.robotgame.menu;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;

import patowen95.robotgame.Controller;
import patowen95.robotgame.InputHandler;


/**
 * Superclass for all menus in the game.
 * @author Patrick Owen
 */
public class Menu
{
	protected Controller c;
	protected InputHandler input;
	private double width, height;
	
	protected double cursorX, cursorY;
	
	protected ArrayList<MenuItem> items;
	
	/**
	 * Creates a new generic menu
	 * @param controller
	 * @param width
	 * @param height
	 */
	public Menu(Controller controller)
	{
		c = controller;
		input = c.getInputHandler();
		
		items = new ArrayList<MenuItem>();
		
		width = c.getWidth();
		height = c.getHeight();
	}
	
	private void updateSize()
	{
		if (width != c.getWidth() || height != c.getHeight())
		{
			width = c.getWidth();
			height = c.getHeight();
			updateItems();
		}
	}
	
	/**
	 * Updates items to make sure that any newly added items are properly formatted
	 */
	protected void updateItems()
	{
		for (MenuItem item : items)
		{
			item.updateText();
		}
	}
	
	/**
	 * Runs a step of the menu to allow checking if the menu needs updating.
	 */
	public void step(double dt)
	{
		updateSize();
		
		cursorX = input.getMouseXPos();
		cursorY = c.getHeight() - input.getMouseYPos();
		
		for (MenuItem item : items)
		{
			item.step(dt);
		}
	}
	
	/**
	 * Draws the menu
	 * @param gl
	 */
	public void draw(GL2 gl)
	{
		gl.glClearColor(.1f, .1f, .1f, 1f);
		for (MenuItem item : items)
		{
			item.draw(gl);
		}
	}
	
	/**
	 * Performs an action called by one of the menu items.
	 * @param item Object that called the method
	 */
	public void handleAction(MenuItem item)
	{
		
	}
	
	/**
	 * Returns the cursor's x-position
	 */
	public double getCursorX()
	{
		return cursorX;
	}
	
	/**
	 * Returns the cursor's y-position
	 */
	public double getCursorY()
	{
		return cursorY;
	}
	
	/**
	 * Returns the parent controller object
	 */
	public Controller getController()
	{
		return c;
	}
	
	/**
	 * Returns the InputHandler object
	 */
	public InputHandler getInput()
	{
		return input;
	}
}

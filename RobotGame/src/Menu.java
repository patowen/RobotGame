import java.util.ArrayList;

import javax.media.opengl.GL2;


/**
 * Superclass for all menus in the game.
 * @author Patrick Owen
 */
public class Menu
{
	protected Controller c;
	protected InputHandler input;
	
	protected double width, height;
	
	protected double cursorX, cursorY;
	
	protected ArrayList<MenuItem> items;
	
	/**
	 * Creates a new generic menu
	 * @param controller
	 * @param width
	 * @param height
	 */
	public Menu(Controller controller, double width, double height)
	{
		c = controller;
		input = c.getInputHandler();
		
		items = new ArrayList<MenuItem>();
	}
	
	/**
	 * Sets the size of the window.
	 * @param w Width
	 * @param h Height
	 */
	protected void setSize(double w, double h)
	{
		width = w;
		height = h;
		
		for (MenuItem item : items)
		{
			item.updateText((int)w, (int)h);
		}
	}
	
	/**
	 * Updates items to make sure that any newly added items are properly formatted
	 */
	protected void updateItems()
	{
		for (MenuItem item : items)
		{
			item.updateText((int)width, (int)height);
		}
	}
	
	/**
	 * Sets the size of the window if the new size is different from the old size
	 * @param w Width
	 * @param h Height
	 */
	public void resize(double w, double h)
	{
		if (width != w || height != h)
			setSize(w, h);
	}
	
	/**
	 * Runs a step of the menu to allow checking if the menu needs updating.
	 */
	public void step(double dt)
	{
		cursorX = input.getMouseXPos();
		cursorY = height - input.getMouseYPos();
		
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
	 * Returns the width of the menu
	 */
	public double getWidth()
	{
		return width;
	}
	
	/**
	 * Returns the height of the menu
	 */
	public double getHeight()
	{
		return height;
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
	 * Returns the InputHandler object
	 */
	public InputHandler getInput()
	{
		return input;
	}
}

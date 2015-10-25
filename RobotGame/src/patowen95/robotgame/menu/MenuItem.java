package patowen95.robotgame.menu;
import com.jogamp.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;


/**
 * Superclass for all menu items. Similar to Swing's Component class
 * @author Patrick Owen
 */
public class MenuItem
{
	protected Menu menu;
	protected int align = 1;
	public static final int ALIGN_CENTER = 1, ALIGN_LEFT = 0, ALIGN_RIGHT = 2;
	
	/**
	 * Constructs a MenuItem object.
	 * @param parentMenu Menu where it is located.
	 */
	public MenuItem(Menu parentMenu)
	{
		menu = parentMenu;
	}
	
	/**
	 * Sets the alignment of the MenuLabel based on a parameter
	 * @param a see constants
	 */
	public void setAlignment(int a)
	{
		align=a;
	}
	
	/**
	 * Must be called before text is to be drawn, or there
	 * will be an exception.
	 * @param width
	 * @param height Window dimensions
	 */
	public void updateText()
	{
		
	}
	
	/**
	 * Updates the menu item on a frame-by-frame basis
	 * @param dt Time step
	 */
	public void step(double dt)
	{
		
	}
	
	/**
	 * Draws the menu item.
	 */
	public void draw(GL2 gl)
	{
		
	}
	
	/**
	 * Draws a string at its center
	 * @param tr TextRenderer object
	 * @param s String to display
	 * @param x
	 * @param y Location
	 */
	protected void drawStringCenter(TextRenderer tr, String s, int x, int y)
	{
		x -= tr.getBounds(s).getWidth()/2;
		y -= tr.getBounds(s).getHeight()/2;
		tr.draw(s, x, y);
	}
	
	/**
	 * Draws a string at the bottom left
	 * @param tr TextRenderer object
	 * @param s String to display
	 * @param x
	 * @param y Location
	 */
	protected void drawStringLeft(TextRenderer tr, String s, int x, int y)
	{
		tr.draw(s, x, y);
	}
	
	/**
	 * Draws a string at its center
	 * @param tr TextRenderer object
	 * @param s String to display
	 * @param x
	 * @param y Location
	 */
	protected void drawStringRight(TextRenderer tr, String s, int x, int y)
	{
		x -= tr.getBounds(s).getWidth();
		tr.draw(s, x, y);
	}
	
}

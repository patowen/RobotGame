import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;


/**
 * Handles anything that occurs in a specific button in a menu and represents said button.
 * @author Patrick Owen
 */
public class MenuButton extends MenuItem
{
	private String message;
	private double x, y; //0 to 1
	private double size; //Factor, 1 is equivalent to 12pt at standard resolution
	
	private TextRenderer textRenderer;
	private String fontName;
	
	/**
	 * Initializes the data of the MenuButton. updateText also needs to be
	 * called for full initialization.
	 * @param text Text to display
	 * @param font Name of the font
	 * @param xLoc
	 * @param yLoc Location of the text on the screen from 0 to 1, (0,0) is at the bottom-left.
	 * @param textSize Size of the text relative to 12pt at standard resolution
	 */
	public MenuButton(Menu menu, String text, String font, double xLoc, double yLoc, double textSize)
	{
		super(menu);
		
		message = text;
		fontName = font;
		x = xLoc;
		y = yLoc;
		size = textSize;
	}
	
	/**
	 * Must be called before text is to be drawn, or there
	 * will be an exception.
	 * @param width
	 * @param height Window dimensions
	 */
	public void updateText(int width, int height)
	{
		double smallest = Math.min(width, height);
		double equivalent12 = smallest/55;//Equivalent of 12 point font on base dimensions
		
		textRenderer = new TextRenderer(new Font(fontName, Font.PLAIN, (int)(equivalent12*size)), true, true);
	}
	
	private boolean mouseInBounds()
	{
		Rectangle2D b = textRenderer.getBounds(message);
		
		if (menu.getCursorX() > x*menu.getWidth()+b.getWidth()/2 ||
				menu.getCursorX() < x*menu.getWidth()-b.getWidth()/2) return false;
		if (menu.getCursorY() > y*menu.getHeight()+b.getHeight()/2 ||
				menu.getCursorY() < y*menu.getHeight()-b.getHeight()/2) return false;
		return true;
	}
	
	/**
	 * Tests to see if the button was pressed every frame.
	 */
	public void step(double dt)
	{
		if (mouseInBounds() && menu.getInput().getMouseButtonPressed(InputHandler.CLICK))
		{
			menu.handleAction(this);
			menu.getInput().cancelMouseButton(InputHandler.CLICK);
		}
	}
	
	/**
	 * Draws the menu button.
	 * @param gl
	 */
	public void draw(GL2 gl)
	{		
		textRenderer.beginRendering((int)menu.getWidth(), (int)menu.getHeight());
		if (mouseInBounds())
			textRenderer.setColor(1f, 1f, 0f, 1f);
		else
			textRenderer.setColor(1f, 1f, 1f, 1f);
		if (align == 0)
			drawStringLeft(textRenderer, message, (int)(menu.getWidth()*x), (int)(menu.getHeight()*y));
		if (align == 1)
			drawStringCenter(textRenderer, message, (int)(menu.getWidth()*x), (int)(menu.getHeight()*y));
		if (align == 2)
			drawStringRight(textRenderer, message, (int)(menu.getWidth()*x), (int)(menu.getHeight()*y));
		textRenderer.endRendering();
	}
}

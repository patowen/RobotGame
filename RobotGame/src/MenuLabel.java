import java.awt.Font;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;


/**
 * Represents a label in the menu (Or title text)
 * @author Patrick Owen
 * @author Michael Ekstrom
 */
public class MenuLabel extends MenuItem
{
	private String message;
	private double x, y; //0 to 1
	private double size; //Factor, 1 is equivalent to 12pt at standard resolution
	
	private TextRenderer textRenderer;
	private String fontName;
	private float[] color;
	
	
	
	/**
	 * Initializes the data of the MenuButton. updateText also needs to be
	 * called for full initialization.
	 * @param text Text to display
	 * @param font Name of the font
	 * @param xLoc
	 * @param yLoc Location of the text on the screen from 0 to 1, (0,0) is at the bottom-left.
	 * @param textSize Size of the text relative to 12pt at standard resolution
	 */
	public MenuLabel(Menu menu, String text, String font, double xLoc, double yLoc, double textSize)
	{
		super(menu);
		
		message = text;
		fontName = font;
		x = xLoc;
		y = yLoc;
		size = textSize;
		
		color = new float[]{1, 1, 1};
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
	
	/**
	 * Updates the text to be displayed
	 * @param s
	 */
	public void setText(String s)
	{
		message = s;
	}
	
	/**
	 * Sets the color of the text displayed
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void setColor(float red, float green, float blue)
	{
		color[0] = red;
		color[1] = green;
		color[2] = blue;
	}
	
	/**
	 * Draws the menu button.
	 * @param gl
	 */
	public void draw(GL2 gl)
	{		
		textRenderer.beginRendering((int)menu.getWidth(), (int)menu.getHeight());
		textRenderer.setColor(color[0], color[1], color[2], 1f);
		if (align == 0)
			drawStringLeft(textRenderer, message, (int)(menu.getWidth()*x), (int)(menu.getHeight()*y));
		if (align == 1)
			drawStringCenter(textRenderer, message, (int)(menu.getWidth()*x), (int)(menu.getHeight()*y));
		if (align == 2)
			drawStringRight(textRenderer, message, (int)(menu.getWidth()*x), (int)(menu.getHeight()*y));
		textRenderer.endRendering();
	}
	

}

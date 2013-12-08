import java.io.File;
import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * This class will deal with the options and rendering of the game's main menu
 * @author Michael Ekstrom
 */

public class MainMenu extends Menu
{
	private MenuButton startButton;
	private MenuButton scoreButton;
	private MenuButton quitButton;
	private MenuButton instructionButton;
	private MenuButton practiceButton;
	
	private Texture menuSplash;
	
	/**
	 * Creates a new MainMenu object
	 */
	public MainMenu(Controller c, double w, double h)
	{
		super(c, w, h);
		loadText();
		
		try {
			menuSplash = TextureIO.newTexture(new File("textures/MenuTexture.jpg"), true);
		} catch (GLException e) {
			System.out.println("GLException at MainMenu's constructor");
		} catch (IOException e) {
			System.out.println("IOException at MainMenu's constructor");
		}
	}
	
	/**
	 * Creates the buttons and labels to be displayed
	 */
	public void loadText()
	{
		items.add(new MenuLabel(this, "ARENA", "ImprintMT-Shadow", .25, .9, 9));
		startButton = new MenuButton(this, "Play", "Impact", .1, .4, 4);
		instructionButton = new MenuButton(this, "Instructions", "Impact", .15, .325, 4);
		practiceButton = new MenuButton(this, "Practice", "Impact", .3, .25, 4);
		scoreButton = new MenuButton(this, "High Scores", "Impact", .4, .175, 4);
		quitButton = new MenuButton(this, "Quit", "Impact", .5, .1, 4);
		
		items.add(startButton);
		items.add(instructionButton);
		items.add(practiceButton);
		items.add(scoreButton);
		items.add(quitButton);
	}
	
	/**
	 * Handles the action for each button
	 * @param item MenuButton to be referenced
	 */
	public void handleAction(MenuItem item)
	{
		if (item == startButton)
		{
			c.setCurrentLevel(c.arena);
			c.setCurrentMenu(null);
			input.readMouse();
		}
		if (item == instructionButton)
			c.setCurrentMenu(c.instructionMenu);
		if (item == practiceButton)
		{
			c.setCurrentLevel(c.practice);
			c.setCurrentMenu(null);
			input.readMouse();
		}
		if (item == scoreButton)
			c.setCurrentMenu(c.scoreMenu);
		if (item == quitButton)
			c.quit();
	}
	
	/**
	 * Overrides but calls super.draw so that the cursor is drawn on the ScoreMenu
	 * @param gl JOGL gl object
	 */
	public void draw(GL2 gl)
	{
		menuSplash.enable(gl);
		menuSplash.bind(gl);
		
		gl.glColor3d(1, 1, 1);
		gl.glBegin(GL2.GL_POLYGON);
		
		gl.glTexCoord2f(0f, 1f);
		gl.glVertex2d(0f, height);
		
		gl.glTexCoord2f(0f, 0f);
		gl.glVertex2d(0f, 0f);
		
		gl.glTexCoord2f(1f, 0f);
		gl.glVertex2d(width, 0f);
		
		gl.glTexCoord2f(1f, 1f);
		gl.glVertex2d(width, height);
		
		gl.glEnd();
		
		menuSplash.disable(gl);
		
		super.draw(gl);
	}
}
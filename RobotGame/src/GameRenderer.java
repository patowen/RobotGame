import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;


/**
 * Creates a window that displays the game.
 * @author Patrick Owen
 */
public class GameRenderer extends GLCanvas
{
	private static final long serialVersionUID = 711619000183783857L;
	
	private Controller c;
	private InputHandler input;
	
	private int fps;
	
	/**
	 * Sets up the main classes that run the game and the Open-GL
	 * initialization.
	 */
	public GameRenderer()
	{
		setPreferredSize(new Dimension(800, 600));
		c = new Controller(this);
		fps = 60;
		
		input = new InputHandler(this);
		c.setInputHandler(input);
		
		addGLEventListener(new GLEventListener()
		{
			public void display(GLAutoDrawable drawable)
			{
				c.setPerspective(drawable.getGL().getGL2(), getWidth(), getHeight());
				c.step(1.0/fps);
				c.render(drawable.getGL().getGL2());
			}
			
			public void init(GLAutoDrawable drawable)
			{
				c.init(drawable.getGL().getGL2());
			}
			
			public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
			{
				c.setPerspective(drawable.getGL().getGL2(), width, height);
			}
			
			public void dispose(GLAutoDrawable drawable)
			{
				
			}
		});
		
		setFocusable(true);
		requestFocus();
		
		FPSAnimator anim = new FPSAnimator(this, fps);
		anim.start();
	}
	
	/**
	 * Creates a window and runs the game.
	 */
	public static void main(String[] args)
	{
		/*
		 * Within the code are 5 algorithms as required.
		 * 
		 * ALGORITHM 1 is located in Player.
		 * ALGORITHM 2 is located in Collision.
		 * ALGORITHM 3 is located in ModelTurret.
		 * ALGORITHM 4 is located in AITracking.
		 * ALGORITHM 5 is located in EnemyTracking.
		 */
		
		JFrame frame = new JFrame("ARENA");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//http://stackoverflow.com/questions/1984071/how-to-hide-cursor-in-a-swing-application
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( 
		    cursorImg, new Point(0, 0), "blank cursor"); 
		frame.setCursor(blankCursor);
		
		GameRenderer game = new GameRenderer();
		frame.add(game);
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
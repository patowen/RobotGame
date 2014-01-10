import javax.media.nativewindow.WindowClosingProtocol.WindowClosingMode;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;


/**
 * Creates a window that displays the game.
 * @author Patrick Owen
 */
public class GameRenderer implements GLEventListener
{	
	private GLWindow win;
	
	private FPSAnimator anim;
	private Controller c;
	private InputHandler input;
	
	private int fps;
	
	/**
	 * Sets up the main classes that run the game and the Open-GL
	 * initialization.
	 */
	public GameRenderer()
	{
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		
		win = GLWindow.create(caps);
		win.setDefaultCloseOperation(WindowClosingMode.DO_NOTHING_ON_CLOSE);
		
		win.addWindowListener(new WindowAdapter()
		{
			public void windowDestroyNotify(WindowEvent e)
			{
				c.quit();
			}
		});
		
		win.setSize(800, 600);
		win.setTitle("ARENA");
		win.addGLEventListener(this);
		
		fps = 60;
		
		anim = new FPSAnimator(win, fps);
		anim.start();
		
		c = new Controller(win, anim);
		
		input = new InputHandler(win);
		c.setInputHandler(input);
		
		win.setVisible(true);
	}
	
	public void display(GLAutoDrawable drawable)
	{
		c.setPerspective(drawable.getGL().getGL2(), win.getWidth(), win.getHeight());
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
	
	/**
	 * Creates a window and runs the game.
	 */
	public static void main(String[] args)
	{
		GLProfile.initSingleton();
		new GameRenderer();
	}
}

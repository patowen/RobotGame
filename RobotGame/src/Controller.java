import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * Main controller class of the whole project. It manages data between levels and sets up all the views.
 * @author Patrick Owen
 * @author Michael Ekstrom
 */
public class Controller
{
	private GLWindow win;
	private FPSAnimator anim;
	private HashMap<Integer, Texture> textureInfo;
	private World currentLevel;
	private int levelType; //0=no score, 1=score
	
	private InputHandler input;
	private SoundHandler soundHandler;
	
	private boolean isMultiplayer;
	private boolean isServer;
	private GuaranteedSignalSender guaranteedSender;
	private Network network;
	private Server server;
	private Client client;
	
	private HUD hud;
	private boolean paused;
	
	/**
	 * The Menu that appears when the game is paused.
	 */
	private PauseMenu pauseMenu;
	
	/**
	 * Stores a reference to which menu is currently being displayed.
	 */
	public Menu currentMenu;
	
	private int score;
	
	//Store the window width and height
	private double width, height;
	
	/**
	 * Constructs a Controller object given the object that renders it.
	 */
	public Controller(GLWindow window, FPSAnimator animator)
	{
		win = window;
		anim = animator;
	}
	
	/**
	 * Initializes everything and determines what is shown to the user at the beginning of the game.
	 * @param gl
	 */
	public void init(GL2 gl)
	{		
		gl.glClearColor(0, 0, 0, 1);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		
		initTextures(gl);
		ModelTurret.init(gl);
		ModelPlasmaRifle.init(gl);
		
		soundHandler = new SoundHandler();
		
		width = 800; height = 600;
		
		isMultiplayer = false;
		guaranteedSender = new GuaranteedSignalSender(this);
		
		currentMenu = new MainMenu(this);
		paused = false;
		
		score = 0;
	}
	
	/**
	 * Frees up resources and quits the game.
	 */
	public void quit()
	{
		forceDisconnect();
		soundHandler.destroy();
		anim.stop();
	}
	
	/**
	 * Returns the Heads Up Display
	 */
	public HUD getHUD()
	{
		return hud;
	}
	
	/**
	 * Returns the SoundHandler object used for playing the sounds of the game.
	 */
	public SoundHandler getSoundHandler()
	{
		return soundHandler;
	}
	
	/**
	 * Determines the InputHandler object used for the controls of the game.
	 */
	public void setInputHandler(InputHandler h)
	{
		input = h;
	}
	
	/**
	 * Returns the InputHandler object used for the controls of the game.
	 */
	public InputHandler getInputHandler()
	{
		return input;
	}
	
	/**
	 * Returns whether the network is being utilized for multiplayer gaming.
	 */
	public boolean isMultiplayer()
	{
		return isMultiplayer;
	}
	
	/**
	 * Returns whether the current computer is hosting the game.
	 */
	public boolean isServer()
	{
		return isServer;
	}
	
	/**
	 * Returns the Server object if the current computer is hosting the game.
	 */
	public Server getServer()
	{
		return server;
	}
	
	public void startServer()
	{
		forceDisconnect();
		
		server = new Server(this, 5);
		isMultiplayer = true;
		isServer = true;
		network = server;
	}
	
	/**
	 * Returns the Client object if the current computer is hosting the game.
	 */
	public Client getClient()
	{
		return client;
	}
	
	public void startClient()
	{
		forceDisconnect();
		
		client = new Client(this);
		isMultiplayer = true;
		isServer = false;
		network = client;
	}
	
	public void disconnect()
	{
		if (client != null)
		{
			client.logout();
		}
		if (server != null)
		{
			server.closeServer();
		}
		
		isMultiplayer = false;
	}
	
	public void forceDisconnect()
	{
		if (network != null)
		{
			network.destroy();
			network = null;
			server = null;
			client = null;
			guaranteedSender.reset();
		}
	}
	
	/**
	 * Returns the Network object if the current computer is involved in a network
	 */
	public Network getNetwork()
	{
		return network;
	}
	
	/**
	 * Returns the GuaranteedSignalSender object
	 */
	public GuaranteedSignalSender getGuaranteedSender()
	{
		return guaranteedSender;
	}
	
	/**
	 * Sets the projection matrix to the proper type.
	 * @param gl
	 * @param w
	 * @param h Dimensions of the screen
	 */
	public void setPerspective(GL2 gl, double w, double h)
	{
		GLU glu = new GLU();
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45, w/h, 0.1, 1000);
		
		width = w;
		height = h;
	}
	
	/**
	 * Completes all rendering operations for the whole game.
	 */
	public void render(GL2 gl)
	{
		GLU glu = new GLU();
		
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		
		gl.glLoadIdentity();
		if (currentMenu == null)
		{
			currentLevel.draw(gl);
			
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();
			glu.gluOrtho2D(0, width, 0, height);
			
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glDisable(GL2.GL_LIGHTING);
			gl.glDisable(GL2.GL_DEPTH_TEST);		
			
			hud.draw(gl, width, height);
			
			if (paused)
				pauseMenu.draw(gl);
			
			gl.glEnable(GL2.GL_DEPTH_TEST);
			gl.glEnable(GL2.GL_LIGHTING);
		}
		else
		{
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadIdentity();
			glu.gluOrtho2D(0, width, 0, height);
			
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glDisable(GL2.GL_LIGHTING);
			gl.glDisable(GL2.GL_DEPTH_TEST);	
			
			currentMenu.draw(gl);
			
			gl.glEnable(GL2.GL_DEPTH_TEST);
			gl.glEnable(GL2.GL_LIGHTING);
		}
	}
	
	/**
	 * Runs a step of the game.
	 * @param dt The reciprocal of the framerate.
	 */
	public void step(double dt)
	{
		if (network != null)
			network.step(dt);
		guaranteedSender.step(dt);
		
		if (currentMenu != null)
		{
			currentMenu.step(dt);
		}
		else if (paused && !isMultiplayer)
		{
			pauseMenu.step(dt);
		}
		else
		{
			if (paused && isMultiplayer)
				pauseMenu.step(dt);
			
			if (paused)
				input.setInputEnabled(false);
			
			if (currentLevel != null)
				currentLevel.step(dt);
			
			if (paused)
				input.setInputEnabled(true);
			
			if (currentLevel != null)
			{
				hud.step(dt);
				
				if (input.getKeyPressed(InputHandler.PAUSE))
				{
					setPaused(true);
				}
			}
		}
		
		input.updatePressed();
		soundHandler.step();
	}
	
	public void setPaused(boolean paused)
	{
		this.paused = paused;
		win.setPointerVisible(paused);
		
		if (!paused)
			input.readMouse();
	}
	
	/**
	 * Handle the death of the player and go to the highscore menu. Should be called by the corresponding World
	 */
	public void handleDeath()
	{
		if (levelType == 1)
		{
			ScoreMenu scoreMenu = new ScoreMenu(this);
			setCurrentMenu(scoreMenu);
			scoreMenu.updateScore(score);
		}
		else if (levelType == 0)
			setCurrentMenu(new MainMenu(this));
	}
	
	/**
	 * Sets the current game level to the desired input
	 * @param gm The World to be set as the current level
	 */
	public void setCurrentLevel(String levelName)
	{
		if (levelName.equals("practice.txt"))
			levelType = 0;
		else
			levelType = 1;
		pauseMenu = new PauseMenu(this);
		paused = false;
		currentLevel = new World(this, new File("maps" + File.separator + levelName)); //levelName
		hud = new HUD(this, currentLevel);
		currentMenu = null;
		win.setPointerVisible(false);
		input.readMouse();
	}
	
	public World getCurrentLevel()
	{
		return currentLevel;
	}
	
	/**
	 * Sets the current menu to display to the input GameMenu
	 * @param menu The menu to display
	 */
	public void setCurrentMenu(Menu menu)
	{
		pauseMenu = null;
		hud = null;
		currentLevel = null;
		
		win.setPointerVisible(true);
		currentMenu = menu;
	}
	
	/**
	 * Returns the score.
	 */
	public int getScore()
	{
		return score;
	}
	
	/**
	 * Returns the width of the window
	 */
	public double getWidth()
	{
		return width;
	}
	
	/**
	 * Returns the height of the window
	 */
	public double getHeight()
	{
		return height;
	}
	
	/**
	 * Adds the score by a specific value
	 */
	public void addScore(double amount)
	{
		score += amount;
	}
	
	/**
	 * Resets the score to 0
	 */
	public void resetScore()
	{
		score = 0;
	}
	
	/**
	 * Initializes all textures.
	 * @param gl
	 */
	private void initTextures(GL2 gl)
	{
		textureInfo = new HashMap<Integer, Texture>();
		textureInfo.put(0, createTextureFromFile(gl, "textures/metal.png", false));
	}
	
	/**
	 * Initializes a texture based on an image file.
	 * @param gl
	 * @param fName File name
	 * @param mipmap Whether to apply mip-mapping to the texture.
	 * @return The corresponding Texture object
	 */
	private Texture createTextureFromFile(GL2 gl, String fName, boolean mipmap)
	{
		Texture tex = null;
		try
		{
			tex = TextureIO.newTexture(new File(fName), mipmap);
		}
		catch (GLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
		gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		return tex;
	}
	
	/**
	 * Returns the texture with the given id
	 */
	public Texture getTexture(int id)
	{
		return textureInfo.get(id);
	}
	
	/**
	 * Initializes an entity with the given id and returns it.<br/>
	 * @param world The world where the obstacle is being placed.
	 * @param id The index of the entity.
	 * @return An object of the appropriate kind of Entity.
	 */
	public Entity createEntity(World world, int type)
	{
		return EI.createEntity(this, world, type);
	}
	
	/**
	 * Initializes an entity with the given id and returns it.<br/>
	 * @param world The world where the obstacle is being placed.
	 * @param id The index of the entity.
	 * @return An object of the appropriate kind of Entity.
	 */
	public Entity createEntity(World world, int type, int owner, int id)
	{
		return EI.createEntity(this, world, type, owner, id);
	}
}

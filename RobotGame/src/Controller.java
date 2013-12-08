import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
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
	private HashMap<Integer, Texture> textureInfo;
	private GameMap currentLevel;
	
	/**
	 * The main map in the game where all the fun takes place.
	 */
	public GameMap arena;
	
	/**
	 * A practice map to help the user gain movement skills while not being chased.
	 */
	public GameMap practice;
	
	private InputHandler input;
	private SoundHandler soundHandler;
	
	private HUD hud;
	private boolean paused;
	
	/**
	 * The Menu that appears when the game is paused.
	 */
	public PauseMenu pauseMenu;
	
	/**
	 * The Menu that shows the high score table
	 */
	public ScoreMenu scoreMenu;
	
	/**
	 * The Menu that appears when the game starts
	 */
	public MainMenu mainMenu;
	
	/**
	 * The Menu that shows the user instructions for playing the game.
	 */
	public InstructionMenu instructionMenu;
	
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
	public Controller(GLWindow window)
	{
		win = window;
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
		
		soundHandler = new SoundHandler();
		
		arena = new GameMap(this, "maps/map001.txt");
		practice = new GameMap(this, "maps/map000.txt");
		currentLevel = arena;
		
		width = 800; height = 600;
		
		hud = new HUD(this);
		pauseMenu = new PauseMenu(this, width, height);
		scoreMenu = new ScoreMenu(this, width, height);
		mainMenu = new MainMenu(this, width, height);
		instructionMenu = new InstructionMenu(this, width, height);
		paused = false;
		
		currentMenu = mainMenu;
		
		score = 0;
	}
	
	/**
	 * Frees up resources and quits the game.
	 */
	public void quit()
	{
		soundHandler.destroy();
		System.exit(0);
	}
	
	/**
	 * Returns the Heads Up Display
	 */
	public HUD getHUD()
	{
		return hud;
	}
	
	/**
	 * Adds a key listener to the Controller object.
	 */
	public void addKeyListener(KeyListener listener)
	{
		win.addKeyListener(listener);
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
	 * Returns the player object in the map
	 */
	public Player getPlayer()
	{
		return currentLevel.getPlayer();
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
		
		pauseMenu.resize(width, height);
		scoreMenu.resize(width, height);
		mainMenu.resize(width,  height);
		instructionMenu.resize(width, height);
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
			currentLevel.drawMap(gl);
			
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
		if (!paused)
		{
			if (currentMenu == null)
			{
				currentLevel.step(dt);
				hud.step(dt);
				
				if (input.getKeyPressed(InputHandler.PAUSE))
				{
					paused = true;
					win.setPointerVisible(true);
					pauseMenu.pause();
				}
			}
			else
				currentMenu.step(dt);
		}
		else
		{
			pauseMenu.step(dt);
			
			if (input.getKeyPressed(InputHandler.PAUSE))
			{
				paused = false;
				win.setPointerVisible(false);
				pauseMenu.unPause();
				input.readMouse();
			}
			if (!pauseMenu.isPaused())
			{
				paused = false;
				if (currentMenu == null)
					win.setPointerVisible(false);
			}
			if(pauseMenu.quit())
				quit();
		}
		
		input.updatePressed();
		soundHandler.step();
	}
	
	/**
	 * Handle the death of the player and go to the highscore menu. Should be called by the corresponding GameMap
	 */
	public void handleDeath()
	{
		if (currentMenu == null)
		{
			if (currentLevel == arena)
			{
				setCurrentMenu(scoreMenu, true);
				scoreMenu.updateScore(score);
			}
			if (currentLevel == practice)
				setCurrentMenu(mainMenu, true);
		}
	}
	
	/**
	 * Sets the current game level to the desired input
	 * @param gm The GameMap to be set as the current level
	 */
	public void setCurrentLevel(GameMap gm)
	{
		currentLevel = gm;
	}
	
	/**
	 * Sets the current menu to display to the input GameMenu
	 * @param menu The menu to display
	 */
	public void setCurrentMenu(Menu menu)
	{
		setCurrentMenu(menu, false);
	}
	
	/**
	 * Sets the current menu to display to the input GameMenu
	 * @param menu The menu to display
	 * @param reset Whether the level should be reset.
	 */
	public void setCurrentMenu(Menu menu, boolean reset)
	{
		if (menu == null)
			win.setPointerVisible(false);
		else
			win.setPointerVisible(true);
		
		currentMenu = menu;
		
		if (reset)
		{
			currentLevel.resetLevel();
		}
	}
	
	/**
	 * Returns the score.
	 */
	public int getScore()
	{
		return score;
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
	 * 0: Player<br/>
	 * 1: EnemyObstacle<br/>
	 * 2: EnemyTurret<br/>
	 * 3: EnemyTracking
	 * @param gameMap The map where the obstacle is being placed.
	 * @param id The index of the entity.
	 * @return An object of the appropriate kind of Entity.
	 */
	public Entity createEntity(GameMap gameMap, int id)
	{
		switch (id)
		{
		case 0:
			return new Player(this, gameMap);
		case 1:
			return new EnemyObstacle(this, gameMap);
		case 2:
			return new EnemyTurret(this, gameMap);
		case 3:
			return new EnemyShocking(this, gameMap);
		case 4:
			return new EnemyTracking(this, gameMap);
		case 5:
			return new EnemySplitting(this, gameMap);
		case 6:
			return new EnemyFortress(this, gameMap);
		}
		
		return null;
	}
}

	
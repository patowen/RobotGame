package patowen95.robotgame.menu;
import patowen95.robotgame.Controller;


/**
 * This class will deal with the options and rendering of the game's main menu
 * @author Michael Ekstrom
 */

public class MainMenu extends Menu
{
	private MenuButton startButton;
	private MenuButton multiplayerButton;
	private MenuButton scoreButton;
	private MenuButton quitButton;
	private MenuButton instructionButton;
	private MenuButton practiceButton;
	
	/**
	 * Creates a new MainMenu object
	 */
	public MainMenu(Controller c)
	{
		super(c);
		loadText();
	}
	
	/**
	 * Creates the buttons and labels to be displayed
	 */
	public void loadText()
	{
		items.add(new MenuLabel(this, "ARENA", "ImprintMT-Shadow", .25, .9, 9));
		startButton = new MenuButton(this, "Play", "Impact", .1, .475, 4);
		multiplayerButton = new MenuButton(this, "Multiplayer", "Impact", .15, .4, 4);
		instructionButton = new MenuButton(this, "Instructions", "Impact", .2, .325, 4);
		practiceButton = new MenuButton(this, "Practice", "Impact", .3, .25, 4);
		scoreButton = new MenuButton(this, "High Scores", "Impact", .4, .175, 4);
		quitButton = new MenuButton(this, "Quit", "Impact", .5, .1, 4);
		
		items.add(startButton);
		items.add(multiplayerButton);
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
			c.setCurrentLevel("arena.txt");
			c.setCurrentLevel("testingmap.txt");
		}
		else if (item == multiplayerButton)
			c.setCurrentMenu(new MultiplayerMenu(c));
		else if (item == instructionButton)
			c.setCurrentMenu(new InstructionMenu(c));
		else if (item == practiceButton)
		{
			c.setCurrentLevel("practice.txt");
		}
		else if (item == scoreButton)
			c.setCurrentMenu(new ScoreMenu(c));
		else if (item == quitButton)
			c.quit();
	}
}

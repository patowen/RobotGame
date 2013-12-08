
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.media.opengl.GL2;

import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;

/**
 * This class handles the creation and storage of high score information, as well as rendering
 * this information to the screen.
 * @author Michael Ekstrom
 */
public class ScoreMenu extends Menu
{
	private String fileName = "data/scores.txt";
	private ArrayList<String> players = new ArrayList<String>();
	private ArrayList<Integer> scores = new ArrayList<Integer>();
	private int newIndex = -1;//index of the player list that is subject to modification
	private MenuLabel updatingPlayer;//The entry being updated on the high score menu
	private MenuLabel updatingScore;//The score of the updating player
	private MenuButton exitButton;
	private int maxScores = 10;//maximum number of scores available on the screen.
	private boolean reading;//true if the ScoreMenu should be reading user input.
	private String userInput = "";
	
	/**
	 * Constructs a ScoreMenu object.
	 * @param controller The main Controller object
	 * @param width The width of the screen
	 * @param height The height of the screen
	 */
	public ScoreMenu(Controller controller, double width, double height)
	{
		super(controller, width, height);
		readFile();
		loadLabels();
		reading = false;

		c.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e)
			{
				if (c.currentMenu == ScoreMenu.this)
				{
					if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && userInput.length() > 0)
					{
						userInput = userInput.substring(0, userInput.length()-1);
					}
					else if (e.getKeyCode() == KeyEvent.VK_ENTER)
					{
						if (reading)
						{
							finishUpdatingScore();
						}
					}
					else
					{
						if (e.isPrintableKey())
						{
							char c = e.getKeyChar();
							if (userInput.length() < 20 && c != 8 && c != 10 && c != 127)
								userInput += c;
						}
					}
				}
			}
		});
		
	}
	
	/**
	 * Saves the score to the High Score table and shows the user that the score is established.
	 */
	public void finishUpdatingScore()
	{
		reading = false;
		updatingPlayer.setColor(1f, 1f, 1f);
		updatingScore.setColor(1f, 1f, 1f);
		writeFile();
	}
	
	/**
	 * Takes the input score and updates the high score screen if necessary
	 */
	public void updateScore(int newScore)
	{
		userInput = "";
		newIndex = -1;
		
		if (newScore > 0)
		{
			for (int i = 0; i < scores.size(); i++)
			{
				if (scores.get(i) < newScore)
				{
					scores.add(i, newScore);
					players.add(i, "");
					if (scores.size() > maxScores)
					{
						scores.remove(scores.size()-1);
						players.remove(players.size()-1);
					}
					newIndex = i;
					reading = true;
					break;
				}
			}
			
			if (scores.size() < maxScores && newIndex == -1)
			{
				scores.add(newScore);
				players.add("");
				newIndex = scores.size() -1;
				reading = true;
			}
		}
			
		items.clear();
		loadLabels();
	}
	
	/**
	 * Loads the labels from the score and player lists
	 */
	public void loadLabels()
	{
		items.add(new MenuLabel(this, "High Scores", "Impact", .5, .85, 9));
		for(int i = 0; i < scores.size(); i++)
		{
			MenuLabel player = new MenuLabel(this, players.get(i), "Impact", .5, (double)(maxScores-i)/(1.5*maxScores), 4);
			MenuLabel score = new MenuLabel(this, scores.get(i) + "", "Impact", .6, (double)(maxScores-i)/(1.5*maxScores), 4);
			player.setAlignment(MenuLabel.ALIGN_RIGHT);
			score.setAlignment(MenuLabel.ALIGN_LEFT);
			items.add(player);
			items.add(score);
			
			if (i == newIndex && reading) 
			{
				updatingPlayer = player;
				updatingScore = score;
				updatingPlayer.setColor(0f, 1f, 1f);
				updatingScore.setColor(0f, 1f, 1f);
			}
		}
		exitButton = new MenuButton(this, "Done", "Impact", .925, .05, 4);
		items.add(exitButton);
		updateItems();
	}
	
	/**
	 * Overrides but calls super.draw so that the cursor is drawn on the ScoreMenu
	 * @param gl JOGL gl object
	 */
	public void draw(GL2 gl)
	{
		super.draw(gl);
	}
	
	/**
	 * Performs an action called by one of the menu items.
	 * @param item Object that called the method
	 */
	public void handleAction(MenuItem item)
	{
		if (item == exitButton)
		{
			if (reading)
			{
				finishUpdatingScore();
			}
			c.setCurrentMenu(c.mainMenu);
			c.resetScore();
		}
	}
	
	
	/**
	 * Reads the information from the designated file
	 */
	public void readFile()
	{
		//This is a field to allow reading methods to modify it.
		File file = new File(fileName);
		
		//Open and start reading the file
		Scanner data;
		try
		{
			data = new Scanner(file);
		}
		catch (FileNotFoundException e)
		{
			throw new IllegalArgumentException("Specified file cannot be found/read");
		}
		
		//Parse the data
		while (data.hasNextLine())
		{
			players.add(data.nextLine());
			scores.add(data.nextInt());
			if (data.hasNextLine())
				data.nextLine();
		}
		
		data.close();
	}
	
	/**
	 * Writes the current player and score lists to the file
	 */
	public void writeFile()
	{
		try
		{
			FileWriter fw = new FileWriter(new File(fileName));
			for (int i = 0; i < scores.size(); i++)
			{
				fw.write(players.get(i) + "\n");
				fw.write(scores.get(i) + "\n");
			}
			fw.close();
		}
		catch (IOException e)
		{
			System.out.println("Failure to write to " + fileName + " in the ScoreMenu class");
		}
	}
	
	/**
	 * Reads the user input to create a high score name
	 */
	public void updateInput()
	{
		if (updatingPlayer != null)
		{
			updatingPlayer.setText(userInput);
			players.set(newIndex, userInput);
		}
	}
	
	/**
	 * Runs the ScoreMenu for a given amount of time
	 */
	public void step(double dt)
	{
		if (reading)
			updateInput();
		super.step(dt);
	}
}
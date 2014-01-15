
/**
 * This class will deal with the rendering of a list of control instructions
 * @author Michael Ekstrom
 */

public class ConnectingMenu extends Menu
{
	private MenuButton cancelButton;
	
	private double pokesPerSecond, pokesRemaining;
	private double timeout, timeRemaining;
	
	/**
	 * Creates a new InstructionMenu object
	 */
	public ConnectingMenu(Controller c)
	{
		super(c);
		loadText();
		
		pokesPerSecond = 5;
		timeout = 10;
		
		pokesRemaining = 0;
		timeRemaining = timeout;
		
		c.startClient();
		c.getClient().login();
	}
	
	/**
	 * Creates the buttons and labels to be displayed
	 */
	public void loadText()
	{
		items.add(new MenuLabel(this, "Connecting...", "Impact", .5, .85, 8));
		cancelButton = new MenuButton(this, "Cancel", "Impact", .9, .05, 4);
		
		items.add(cancelButton);
	}
	
	public void step(double dt)
	{
		super.step(dt);
		
		pokesRemaining += pokesPerSecond*dt;
		timeRemaining -= dt;
		
		while (pokesRemaining > 1)
		{
			c.getClient().login();
			pokesRemaining -= 1;
		}
		
		if (c.getClient().isConnected())
		{
			c.setCurrentLevel("arena.txt");
			return;
		}
		
		if (timeRemaining <= 0)
		{
			c.disconnect();
			c.setCurrentMenu(new DisconnectedMenu(c));
			return;
		}
	}
	
	/**
	 * Handles the action for each button
	 * @param item MenuButton to be referenced
	 */
	public void handleAction(MenuItem item)
	{
		if (item == cancelButton)
		{
			c.disconnect();
			c.setCurrentMenu(new MultiplayerMenu(c));
		}
	}
}

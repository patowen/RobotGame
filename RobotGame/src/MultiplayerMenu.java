
/**
 * This class will deal with the rendering of a list of control instructions
 * @author Michael Ekstrom
 */

public class MultiplayerMenu extends Menu
{
	private MenuButton hostButton;
	private MenuButton joinButton;
	private MenuButton backButton;
	
	/**
	 * Creates a new InstructionMenu object
	 */
	public MultiplayerMenu(Controller c)
	{
		super(c);
		loadText();
	}
	
	/**
	 * Creates the buttons and labels to be displayed
	 */
	public void loadText()
	{
		items.add(new MenuLabel(this, "Multiplayer", "Impact", .5, .85, 8));
		hostButton = new MenuButton(this, "Host", "Impact", 0.5, 0.5, 4);
		joinButton = new MenuButton(this, "Join", "Impact", 0.5, 0.25, 4);
		backButton = new MenuButton(this, "Back", "Impact", .9, .05, 4);
		
		items.add(hostButton);
		items.add(joinButton);
		items.add(backButton);
	}
	
	/**
	 * Handles the action for each button
	 * @param item MenuButton to be referenced
	 */
	public void handleAction(MenuItem item)
	{
		if (item == hostButton)
		{
			c.startServer();
			c.setMultiplayer(true);
			c.setCurrentLevel("arena.txt");
		}
		else if (item == joinButton)
		{
			c.startClient();
			c.setMultiplayer(true);
			Client client = c.getClient();
			client.login();
			new Thread()
			{
				public void run()
				{
					for (int i=0; i<300; i++)
					{
						if (c.getClient().isConnected())
						{
							c.setCurrentLevel("arena.txt");
							return;
						}
						
						try
						{
							Thread.sleep(10);
						}
						catch (InterruptedException e)
						{
							return;
						}
					}
					c.setCurrentMenu(new MainMenu(c));
				}
			}.start();
//			c.setCurrentMenu(new MainMenu(c));
		}
		else if (item == backButton)
			c.setCurrentMenu(new MainMenu(c));
	}
}


/**
 * This class will deal with the rendering of a list of control instructions
 * @author Michael Ekstrom
 */

public class DisconnectedMenu extends Menu
{
	private MenuButton backButton;
	
	/**
	 * Creates a new InstructionMenu object
	 */
	public DisconnectedMenu(Controller c)
	{
		super(c);
		loadText();
	}
	
	/**
	 * Creates the buttons and labels to be displayed
	 */
	public void loadText()
	{
		items.add(new MenuLabel(this, "Disconnected by server", "Impact", .5, .85, 5));
		backButton = new MenuButton(this, "Back", "Impact", .9, .05, 4);
		
		items.add(backButton);
	}
	
	public void step(double dt)
	{
		super.step(dt);
		
		if (c.getClient() == null)
			return;
		
		if (c.getClient().isConnected())
		{
			c.setCurrentLevel("arena.txt");
			return;
		}
	}
	
	/**
	 * Handles the action for each button
	 * @param item MenuButton to be referenced
	 */
	public void handleAction(MenuItem item)
	{
		if (item == backButton)
		{
			c.setCurrentMenu(new MultiplayerMenu(c));
		}
	}
}


/**
 * This class will deal with the rendering of a list of control instructions
 * @author Michael Ekstrom
 */

public class ConnectingMenu extends Menu
{
	private MenuButton cancelButton;
	
	/**
	 * Creates a new InstructionMenu object
	 */
	public ConnectingMenu(Controller c)
	{
		super(c);
		loadText();
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
		if (item == cancelButton)
		{
			c.disconnect();
			c.setCurrentMenu(new MultiplayerMenu(c));
		}
	}
}

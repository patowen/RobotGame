package patowen95.robotgame.menu;
import patowen95.robotgame.Controller;


/**
 * This class will deal with the rendering of a list of control instructions
 * @author Michael Ekstrom
 */

public class InstructionMenu extends Menu
{
	private MenuButton backButton;
	
	/**
	 * Creates a new InstructionMenu object
	 */
	public InstructionMenu(Controller c)
	{
		super(c);
		loadText();
	}
	
	/**
	 * Creates the buttons and labels to be displayed
	 */
	public void loadText()
	{
		items.add(new MenuLabel(this, "Instructions", "Impact", .5, .85, 8));
		MenuLabel wasdT = new MenuLabel(this, "Player Movement", "Gulim", .45, .65, 3);
		wasdT.setAlignment(MenuItem.ALIGN_RIGHT);
		MenuLabel wasd = new MenuLabel(this, "WASD Keys", "Gulim", .55, .65, 3);
		wasd.setAlignment(MenuItem.ALIGN_LEFT);
		
		MenuLabel clickT = new MenuLabel(this, "Fire Weapon", "Gulim", .45, .6, 3);
		clickT.setAlignment(MenuItem.ALIGN_RIGHT);
		MenuLabel click = new MenuLabel(this, "Mouse Click", "Gulim", .55, .6, 3);
		click.setAlignment(MenuItem.ALIGN_LEFT);
		
		MenuLabel jumpT = new MenuLabel(this, "Jump", "Gulim", .45, .55, 3);
		jumpT.setAlignment(MenuItem.ALIGN_RIGHT);
		MenuLabel jump = new MenuLabel(this, "Space Bar", "Gulim", .55, .55, 3);
		jump.setAlignment(MenuItem.ALIGN_LEFT);
		
		MenuLabel pauseT = new MenuLabel(this, "Pause the Game", "Gulim", .45, .5, 3);
		pauseT.setAlignment(MenuItem.ALIGN_RIGHT);
		MenuLabel pause = new MenuLabel(this, "Escape Key", "Gulim", .55, .5, 3);
		pause.setAlignment(MenuItem.ALIGN_LEFT);
		
		items.add(pauseT);
		items.add(pause);
		items.add(jumpT);
		items.add(jump);
		items.add(clickT);
		items.add(click);
		items.add(wasdT);
		items.add(wasd);
		
		items.add(new MenuLabel(this, "Move the mouse to make the player face a new direction", "Gulim", .5, .35, 2.5));
		items.add(new MenuLabel(this, "You can hold down mouse click to fire as fast as you can", "Gulim", .5, .3, 2.5));
		items.add(new MenuLabel(this, "Health regenerates over time and completely after each wave", "Gulim", .5, .25, 2.5));
		items.add(new MenuLabel(this, "Shoot the enemies and survive as long as you can!", "Gulim", .5, .2, 2.5));
		
		backButton = new MenuButton(this, "Back", "Impact", .9, .05, 4);
		items.add(backButton);
	}
	
	/**
	 * Handles the action for each button
	 * @param item MenuButton to be referenced
	 */
	public void handleAction(MenuItem item)
	{
		if (item == backButton)
			c.setCurrentMenu(new MainMenu(c));
	}
}

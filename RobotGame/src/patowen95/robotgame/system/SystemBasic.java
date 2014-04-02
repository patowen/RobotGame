package patowen95.robotgame.system;

import java.io.IOException;

import patowen95.robotgame.Controller;
import patowen95.robotgame.ImportStream;
import patowen95.robotgame.World;

public abstract class SystemBasic
{
	private Controller c;
	private World w;
	
	public SystemBasic(Controller controller, World world)
	{
		c = controller;
		w = world;
	}
	
	/**
	 * Initializes data as imported in a file.
	 * @param index The index of the data that must be initialized
	 * @param data The value of the data.
	 */
	public abstract void initializeData(ImportStream input) throws IOException;
}

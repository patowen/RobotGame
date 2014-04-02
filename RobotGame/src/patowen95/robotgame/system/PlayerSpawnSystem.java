package patowen95.robotgame.system;

import java.io.IOException;
import java.util.ArrayList;

import patowen95.robotgame.Controller;
import patowen95.robotgame.ImportStream;
import patowen95.robotgame.World;
import patowen95.robotgame.entity.EntityPlayerBasic;

public class PlayerSpawnSystem extends SystemBasic
{
	private EntityPlayerBasic playerEntity;
	private ArrayList<SpawnPoint> spawnPoints;
	
	public PlayerSpawnSystem(Controller controller, World world)
	{
		super(controller, world);
	}
	
	public void initializeData(ImportStream input) throws IOException
	{
		int keepGoing = input.getByte();
		if (keepGoing != 0)
		{
			spawnPoints.add(new SpawnPoint(input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble()));
		}
	}
	
	private class SpawnPoint
	{
		public double x, y, z, horizontalDir, verticalDir;
		
		public SpawnPoint(double x, double y, double z, double hDir, double vDir)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.horizontalDir = hDir;
			this.verticalDir = vDir;
		}
	}
}

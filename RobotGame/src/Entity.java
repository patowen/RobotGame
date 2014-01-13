import javax.media.opengl.GL2;

/**
 * Represents any in-game object that can be encountered by the player, including the player. 
 * @author Patrick Owen
 */
public class Entity
{
	protected Controller c;
	
	protected boolean isLocal;
	protected final int owner; //Which computer controls the entity. 0=server, 1=client0, 2=client1, ...
	protected final int id; //An identification of the entity that remains consistent throughout its lifetime.
	protected double x, y, z;
	protected double xV, yV, zV;
	protected double xPrevious, yPrevious, zPrevious;
	protected GameMap map;
	
	/**
	 * Creates a new Entity.
	 * @param controller The active Controller object.
	 * @param gameMap The map where the Entity is placed.
	 */
	public Entity(Controller controller, GameMap gameMap)
	{		
		c = controller;
		map = gameMap;
		
		xV = 0;
		yV = 0;
		zV = 0;
		
		isLocal = true;
		if (c.isMultiplayer())
			owner = c.getNetwork().getComputerID();
		else
			owner = 0;
		id = map.generateEntityID();
	}
	
	/**
	 * Moves the entity to the specified location.
	 * @param xLoc
	 * @param yLoc
	 * @param zLoc Location of the entity.
	 */
	public void setPosition(double xLoc, double yLoc, double zLoc)
	{
		x = xLoc;
		y = yLoc;
		z = zLoc;
	}
	
	/**
	 * Sets the velocity vector of the entity. Each entity must implement velocity itself.
	 * @param xVel
	 * @param yVel
	 * @param zVel Chosen velocity
	 */
	public void setVelocity(double xVel, double yVel, double zVel)
	{
		xV = xVel;
		yV = yVel;
		zV = zVel;
	}
	
	/**
	 * Returns whether this entity should be ignored by other entities.
	 */
	public boolean isGhost()
	{
		return false;
	}
	
	/**
	 * Handles changes to the entity based on signals received by it.
	 * @param signalType The kind of signal being sent.
	 * @param data A byte array holding the data of the signal.
	 * @param offset Where the array should start being read.
	 */
	public void signalReceived(int signalType, NetworkPacket data)
	{
		switch (signalType)
		{
		case 0:
			double[] p = data.getDoubles(3);
			double[] v = data.getDoubles(3);
			setPosition(p[0], p[1], p[2]);
			setVelocity(v[0], v[1], v[2]);
			
			break;
		}
	}
	
	public void stepSendSignals()
	{
		NetworkPacket packet = new NetworkPacket(6*8);
		
		packet.addDoubles(x, y, z, xV, yV, zV);
		
		sendSignal(false, 0, packet);
	}
	
	protected void sendSignal(boolean guaranteed, int signalType, NetworkPacket data)
	{
		
	}
	
	/**
	 * Initializes data not common to all entities.
	 * @param index The index of the data that must be initialized
	 * @param data The value of the data.
	 */
	public void initializeExtraData(int index, double data)
	{
		
	}
	
	/**
	 * Returns how many pieces of data there need to be other than location to fully initialize the entity.
	 */
	public int getAmountExtraData()
	{
		return 0;
	}
	
	/**
	 * Runs a frame of the Entity's movement.
	 * @param dt Time step in seconds.
	 */
	public void step(double dt)
	{
		xPrevious = x;
		yPrevious = y;
		zPrevious = z;
	}
	
	/**
	 * Draws the specified entity.
	 * @param gl
	 */
	public void draw(GL2 gl)
	{
		
	}
	
	/**
	 * Draws transparent parts of the specified entity, called after
	 * all draw methods are called
	 * @param gl
	 */
	public void draw2(GL2 gl)
	{
		
	}
	
	/**
	 * Requests deletion of the entity.
	 */
	public void delete()
	{
		map.delete(this);
	}
	
	/**
	 * Returns the x-coordinate of the entity.
	 */
	public double getX()
	{
		return x;
	}
	
	/**
	 * Returns the y-coordinate of the entity.
	 */
	public double getY()
	{
		return y;
	}
	
	/**
	 * Returns the z-coordinate of the entity.
	 */
	public double getZ()
	{
		return z;
	}
	
	/**
	 * Returns the x-component of the velocity of the entity.
	 */
	public double getXV()
	{
		return xV;
	}
	
	/**
	 * Returns the y-component of the velocity of the entity.
	 */
	public double getYV()
	{
		return yV;
	}
	
	/**
	 * Returns the z-component of the velocity of the entity.
	 */
	public double getZV()
	{
		return zV;
	}
	
	/**
	 * Returns the previous x-coordinate of the entity.
	 */
	public double getXPrevious()
	{
		return xPrevious;
	}
	
	/**
	 * Returns the previous y-coordinate of the entity.
	 */
	public double getYPrevious()
	{
		return yPrevious;
	}
	
	/**
	 * Returns the previous z-coordinate of the entity.
	 */
	public double getZPrevious()
	{
		return zPrevious;
	}
}

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/* Original stab at the dark:
 * First byte:
 * 0: Signal not to be guaranteed
 * Next 4 bytes is an integer that increments with each sent signal to make sure it
 * is the latest data that is processed.
 * 
 * 1: Signal to be guaranteed. The way the signal is guaranteed depends on what signal it is.
 * 
 * 2: Signal is relaying a guaranteed signal to confirm success.
 * 
 * Next bytes:
 * 0, 0: Log out
 * 0, 1: Log in request (followed by username)
 * 0, 2, 0: Log in granted
 * 0, 2, 1: Log in denied
 * 
 * 1: Game setup data
 * 
 * 2: Game data
 * 2, 0: Spawn entity (entity type id, location, velocity, extra data)
 * 2, 1: Remove entity (entity type id)
 * 2, 2: Move entity (entity type id, location, velocity, extra data) (not guaranteed)
 */

/*
 * Multiplayer notes:
 * 
 * Log in/out signals:
 * [1, 0]: Log out (Relayed with [0, 1, 0])
 * [1, 1, name]: Log in request (Relayed with [0, 1, 1, id] for granted; [0, 1, 1, reason (negative)] for denied)
 * [1, 2]: Server closed (Relayed with [0, 1, 2])
 * 
 * Entity signals:
 * [3, 0, 0, entity_type, entity_id, x, y, z, xV, yV, zV, appendix]: Spawn entity (Relayed with [0, 3, 0, 0, entity_id])
 * [3, 0, 1, entity_id]: Remove entity (Relayed with [0, 3, 0, 1, entity_type])
 * [3, 0, 2, entity_id, x, y, z, xV, yV, zV, appendix]: Move entity, not relayed
 */

/**
 * Handles the sending and receiving of network data server-side and holds a list of
 * clients.
 * @author Patrick Owen
 */
public class Server extends Network
{	
	private int capacity, numPlayers;
	private String[] clientIP;
	private int[] clientPort;
	private String[] clientName;
	
	public Server(int cap)
	{
		capacity = cap;
		numPlayers = 0;
		
		clientIP = new String[capacity];
		clientPort = new int[capacity];
		clientName = new String[capacity];
		
		try
		{
			socket = new DatagramSocket(4445);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		startThread();
	}
	
	public void step(double dt)
	{
		
	}
	
	//Returns the client index for the specified data, or -1 if none exists.
	private int getClient(String ip, int port)
	{
		for (int i=0; i<numPlayers; i++)
		{
			if (clientIP[i].equals(ip) && clientPort[i] == port)
				return i;
		}
		
		return -1;
	}
	
	//Removes the specified client from the list.
	private void removeClient(int index)
	{
		for (int i=index+1; i<numPlayers; i++)
		{
			clientIP[i-1] = clientIP[i];
			clientPort[i-1] = clientPort[i];
			clientName[i-1] = clientName[i];
		}
		
		numPlayers--;
	}
	
	public void interpretSignal(NetworkPacket packet, InetAddress sender, int senderPort)
	{
		NetworkPacket ret = new NetworkPacket(256);
		
		byte signalType = packet.getByte();
		if (signalType == 1) //Standard client signal (not a relay)
		{
			byte subtype = packet.getByte();
			if (subtype == 0) //Log out
			{
				//Handle request
				removeClient(getClient(sender.getHostAddress(), senderPort));
				//Send confirmation
				ret.addBytes(0, 1, 0);
				send(ret, sender, senderPort);
			}
			else if (subtype == 1) //Log in request
			{
				//Check if already logged in
				int id = getClient(sender.getHostAddress(), senderPort);
				if (id != -1)
				{
					//Resend granted signal
					ret.addBytes(0, 1, 1, 0);
					send(ret, sender, senderPort);
				}
				else
				{
					if (numPlayers < capacity)
					{
						//Granted
						ret.addBytes(0, 1, 1, 0);
						send(ret, sender, senderPort);
						clientIP[numPlayers] = sender.getHostAddress();
						clientPort[numPlayers] = senderPort;
						clientName[numPlayers] = packet.getString();
						numPlayers++;
					}
					else
					{
						//Denied
						ret.addBytes(0, 1, 1, -1);
						send(ret, sender, senderPort);
					}
				}
			}
		}
	}
}

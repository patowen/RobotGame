import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Handles the sending and receiving of network data server-side and holds a list of
 * clients.
 * @author Patrick Owen
 */
public class Server extends Network
{	
	private int capacity, numPlayers;
	private InetAddress[] clientIP;
	private int[] clientPort;
	private String[] clientName;
	private int[] clientID;
	
	public Server(Controller controller, int cap)
	{
		super(controller);
		
		capacity = cap;
		numPlayers = 0;
		
		clientIP = new InetAddress[capacity];
		clientPort = new int[capacity];
		clientName = new String[capacity];
		clientID = new int[capacity];
		
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
	
	public void closeServer()
	{
		NetworkPacket packet = new NetworkPacket(2);
		packet.addBytes(1, 2);
		for (int i=0; i<numPlayers; i++)
		{
			sendGuaranteed(packet, clientIP[i], clientPort[i]);
		}
	}
	
	//Returns the next available Client ID to apply
	private int getNextID()
	{
		int currentID;
		boolean idFound = false, idTaken = false;
		
		for (currentID=1; !idFound; currentID++)
		{
			for (int j=0; j<numPlayers && !idTaken; j++)
			{
				if (clientID[j] == currentID)
					idTaken = true;
			}
			if (!idTaken)
				idFound = true;
		}
		
		return currentID;
	}
	
	//Returns the client index for the specified data, or -1 if none exists.
	private int getClient(InetAddress ip, int port)
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
		if (index == -1)
			return;
		
		for (int i=index+1; i<numPlayers; i++)
		{
			clientIP[i-1] = clientIP[i];
			clientPort[i-1] = clientPort[i];
			clientName[i-1] = clientName[i];
			clientID[i-1] = clientID[i];
		}
		
		numPlayers--;
	}
	
	public int getComputerID()
	{
		return 0;
	}
	
	public void sendEntityDataNormal(NetworkPacket data)
	{
		for (int i=0; i<numPlayers; i++)
		{
			sendNormal(data, clientIP[i], clientPort[i]);
		}
	}
	
	public void sendEntityDataGuaranteed(NetworkPacket data)
	{
		for (int i=0; i<numPlayers; i++)
		{
			sendGuaranteed(data, clientIP[i], clientPort[i]);
		}
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
				removeClient(getClient(sender, senderPort));
			}
			else if (subtype == 1) //Log in request
			{
				//Check if already logged in
				int id = getClient(sender, senderPort);
				if (id == -1)
				{
					if (numPlayers < capacity)
					{
						//Granted
						clientIP[numPlayers] = sender;
						clientPort[numPlayers] = senderPort;
						clientName[numPlayers] = packet.getString();
						clientID[numPlayers] = getNextID();
						ret.addBytes(0, 1, 1, clientID[numPlayers]);
						sendGuaranteed(ret, sender, senderPort);
						numPlayers++;
					}
					else
					{
						//Denied
						ret.addBytes(0, 1, 1, -1);
						sendGuaranteed(ret, sender, senderPort);
					}
				}
			}
		}
		else if (signalType == 3) //Entity information
		{
		}
	}
}

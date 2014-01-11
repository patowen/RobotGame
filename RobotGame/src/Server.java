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

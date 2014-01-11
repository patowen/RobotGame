import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Handles the sending and receiving of network data server-side and holds a list of
 * clients.
 * @author Patrick Owen
 */
public class Client extends Network
{
	private InetAddress serverIP;
	private int serverPort;
	
	private boolean connected, loggingIn, loggingOut;
	private int loginTimer, logoutTimer;
	
//	private int capacity, numPlayers;
//	private String[] clientName;
	
	public Client()
	{		
		try
		{
			serverIP = InetAddress.getByName("127.0.0.1");
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		serverPort = 4445;
		connected = false;
		
		try
		{
			socket = new DatagramSocket();
		}
		catch (SocketException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		startThread();
	}
	
	public boolean isConnected()
	{
		return connected;
	}
	
	public void logout()
	{
		loggingOut = true;
		logoutTimer = 600;
		NetworkPacket packet = new NetworkPacket(2);
		packet.addBytes(1, 0);
		send(packet);
	}
	
	public void login()
	{
		loggingIn = true;
		loginTimer = 600;
		NetworkPacket packet = new NetworkPacket(256);
		packet.addBytes(1, 1);
		packet.addString("SuperLala");
		send(packet);
	}
	
	public void step(double dt)
	{
		if (loggingIn)
		{
			loginTimer--;
			if (loginTimer == 0)
			{
				//Connection time out
				loggingIn = false;
				logout();
			}
			else if (loginTimer%30 == 0)
			{
				//Resend login signal
				NetworkPacket packet = new NetworkPacket(256);
				packet.addBytes(1, 1);
				packet.addString("SuperLala");
				send(packet);
			}
		}
		
		if (loggingOut)
		{
			logoutTimer--;
			if (logoutTimer == 0)
			{
				//Connection time out
				loggingOut = false;
			}
			else if (loginTimer%30 == 0)
			{
				//Resend login signal
				NetworkPacket packet = new NetworkPacket(2);
				packet.addBytes(1, 0);
				send(packet);
			}
		}
	}
	
	public void send(NetworkPacket packet)
	{
		send(packet, serverIP, serverPort);
	}
	
	public void interpretSignal(NetworkPacket packet, InetAddress sender, int senderPort)
	{
		NetworkPacket ret = new NetworkPacket(256);
		
		if (!sender.equals(serverIP) || senderPort != serverPort)
			return;
		
		byte signalType = packet.getByte();
		if (signalType == 0) //Signal reply from server
		{
			byte typeRelayed = packet.getByte();
			if (typeRelayed == 1)
			{
				byte subtype = packet.getByte();
				if (subtype == 1)
				{
					byte id = packet.getByte();
					loggingIn = false;
					if (id < 0)
					{
						connected = false;
					}
					else
					{
						connected = true;
					}
				}
			}
		}
		else if (signalType == 1) //Standard client signal (not a relay)
		{
			byte subtype = packet.getByte();
			if (subtype == 2)
			{
				loggingIn = false;
				loggingOut = false;
				connected = false;
				
				ret.addBytes(0, 1, 2);
				send(ret, sender, senderPort);
			}
		}
	}
}

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
public class Client
{
	private DatagramSocket socket;
	private ClientThread thread;
	
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
			thread = new ClientThread(4445);
			thread.start();
		}
		catch (SocketException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void destroy()
	{
		if (thread != null && thread.isAlive())
			thread.interrupt();
		socket.close();
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
		try
		{
			socket.send(new DatagramPacket(packet.array(), packet.length(), serverIP, serverPort));
		}
		catch (IOException e) {} //Just act like a failed signal send
	}
	
	/**
	 * Handles the loop of receiving signals from clients.
	 */
	private class ClientThread extends Thread
	{		
		public ClientThread(int port) throws SocketException
		{
			socket = new DatagramSocket();
		}
		
		public void interpretSignal(NetworkPacket packet, InetAddress sender, int senderPort) throws IOException
		{
			NetworkPacket ret = new NetworkPacket(256);
			
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
				
			}
		}
		
		public void run()
		{
			byte[] buf = new byte[256];
			
			while (true)
			{
				//Receive data
				if (socket.isClosed())
					return;
				
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try
				{
					socket.receive(packet);
					interpretSignal(new NetworkPacket(buf), packet.getAddress(), packet.getPort());
				}
				catch (IOException e) {}
			}
		}
	}
}

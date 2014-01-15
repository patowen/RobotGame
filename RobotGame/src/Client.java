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
	private int computerID;
	private long currentSignalID;
	private GuaranteedSignalReceiver guaranteedReceiver;
	
	private InetAddress serverIP;
	private int serverPort;
	
	private boolean connected;
	
	public Client(Controller controller)
	{
		super(controller);
		
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
		currentSignalID = 0;
		guaranteedReceiver = new GuaranteedSignalReceiver(c, serverIP, serverPort);
		
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
	
	public void step(double dt)
	{
		super.step(dt);
		guaranteedReceiver.step(dt);
	}
	
	public boolean isConnected()
	{
		return connected;
	}
	
	public void logout()
	{
		NetworkPacket packet = new NetworkPacket(2);
		packet.addBytes(1, 0);
		sendGuaranteed(packet);
	}
	
	public void login()
	{
		NetworkPacket packet = new NetworkPacket(256);
		packet.addBytes(1, 1);
		packet.addString("SuperLala");
		sendNormal(packet);
	}
	
	public void sendGuaranteed(NetworkPacket packet)
	{
		sendGuaranteed(packet, serverIP, serverPort);
	}
	
	public void sendNormal(NetworkPacket packet)
	{
		sendNormal(packet, serverIP, serverPort);
	}
	
	public int getComputerID()
	{
		return computerID;
	}
	
//	public void sendEntityDataNormal(NetworkPacket data)
//	{
//		sendNormal(data);
//	}
//	
//	public void sendEntityDataGuaranteed(NetworkPacket data)
//	{
//		sendGuaranteed(data);
//	}
	
	public long createSignalID(InetAddress ip, int port)
	{
		return currentSignalID++;
	}
	
	public void saveSignal(NetworkPacket packet, InetAddress sender, int senderPort, long signalID)
	{
		guaranteedReceiver.addPendingSignal(signalID, packet);
	}
	
	public void interpretSignal(NetworkPacket packet, InetAddress sender, int senderPort)
	{
//		NetworkPacket ret = new NetworkPacket(256);
		
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
					if (id < 0)
					{
						c.forceDisconnect();
						c.setCurrentMenu(new DisconnectedMenu(c));
					}
					else
					{
						connected = true;
						computerID = id;
					}
				}
			}
		}
		else if (signalType == 1)
		{
			byte subtype = packet.getByte();
			if (subtype == 2)
			{
				//Server closed
				c.forceDisconnect();
				c.setCurrentMenu(new DisconnectedMenu(c));
			}
		}
		else if (signalType == 3) //Entity information
		{
			byte subtype = packet.getByte();
			if (subtype == 0)
			{
//				World world = c.getCurrentLevel();
//				if (world != null)
//				{
//					int type = packet.getInt();
//					int owner = packet.getInt();
//					int id = packet.getInt();
//					Entity e = world.getEntity(owner, id);
//					if (e == null)
//					{
//						Entity entity = c.createEntity(world, type, owner, id);
//						world.create(entity);
//					}
//				}
//				world.create();
			}
			else if (subtype == 1)
			{
				World world = c.getCurrentLevel();
				if (world != null)
				{
					world.delete(packet.getInt(), packet.getInt());
				}
			}
			else if (subtype == 2)
			{
				World world = c.getCurrentLevel();
				if (world != null)
				{
					int type = packet.getInt();
					int owner = packet.getInt();
					int id = packet.getInt();
					Entity e = world.getEntity(owner, id);
					if (e == null)
					{
						e = c.createEntity(world, type, owner, id);
						world.create(e);
					}
					e.signalReceived(packet);
				}
			}
		}
	}
}

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/*
 * Multiplayer notes:
 * 
 * [0, rest_of_signal]: normal signal
 * [1, signal_id, rest_of_signal]: guaranteed signal
 * [2, signal_id]: relayed signal
 * 
 * Log in/out signals:
 * [1, 0]: Log out
 * [1, 1, name]: Log in request (Relayed with [0, 1, 1, id] for granted; [0, 1, 1, reason (negative)] for denied)
 * [1, 2]: Server closed
 * 
 * Entity signals:
 * [3, 0, entity_type, owner, entity_id]: Spawn entity (Relayed with [0, 3, 0, 0, entity_id])
 * [3, 1, owner, entity_id]: Remove entity
 * [3, 2, owner, entity_id, x, y, z, xV, yV, zV, appendix]: Move entity, not relayed
 */

public abstract class Network
{
	protected Controller c;
	
	protected DatagramSocket socket;
	protected NetworkThread thread;
	
	private ArrayList<Packet> packets;
	
	public Network(Controller controller)
	{
		c = controller;
		packets = new ArrayList<Packet>();
	}
	
	public void step(double dt)
	{
		ArrayList<Packet> packets = getPackets();
		for (Packet p : packets)
		{
			interpretSignal(p.packet, p.sender, p.senderPort);
		}
	}
	
	public void destroy()
	{
		if (thread != null && thread.isAlive())
		{
			thread.interrupt();
		}
		socket.close();
	}
	
	protected void startThread()
	{
		thread = new NetworkThread();
		thread.start();
	}
	
	public void sendGuaranteed(NetworkPacket data, InetAddress ip, int port)
	{
		c.getGuaranteedSender().addGuaranteedSignal(data, ip, port);
	}
	
	public void sendNormal(NetworkPacket data, InetAddress ip, int port)
	{
		NetworkPacket packet = new NetworkPacket(data.length()+1);
		packet.addByte(0);
		packet.append(data);
		if (c.isServer())
		send(packet, ip, port);
	}
	
	public void send(NetworkPacket packet, InetAddress ip, int port)
	{
		try
		{
			socket.send(new DatagramPacket(packet.array(), packet.length(), ip, port));
		}
		catch (IOException e) {} //Just act like a failed signal send
	}
	
	public abstract void interpretSignal(NetworkPacket packet, InetAddress sender, int senderPort);
	
	public abstract int getComputerID();
	
	private void interpretSignalRaw(NetworkPacket packet, InetAddress sender, int senderPort)
	{
		int flag = packet.getByte();
		if (flag == 0)
		{
			addPacket(packet, sender, senderPort);
//			interpretSignal(packet, sender, senderPort);
		}
		else if (flag == 1)
		{
			NetworkPacket relayPacket = new NetworkPacket(9);
			relayPacket.addByte(2);
			relayPacket.addLong(packet.getLong());
			send(relayPacket, sender, senderPort);
			addPacket(packet, sender, senderPort);
			
			//interpretSignal(packet, sender, senderPort); //TODO Temporary. Ordering should matter.
		}
		else if (flag == 2)
		{
			c.getGuaranteedSender().addRelayedSignal(sender, senderPort, packet);
		}
	}
	
	private synchronized void addPacket(NetworkPacket packet, InetAddress sender, int senderPort)
	{
		packets.add(new Packet(packet, sender, senderPort));
	}
	
	private synchronized ArrayList<Packet> getPackets()
	{
		ArrayList<Packet> packetsClone = new ArrayList<Packet>();
		
		for (Packet p : packets)
		{
			packetsClone.add(p);
		}
		packets.clear();
		return packetsClone;
	}
	
	private class Packet
	{
		public final NetworkPacket packet;
		public final InetAddress sender;
		public final int senderPort;
		
		public Packet(NetworkPacket packet, InetAddress sender, int senderPort)
		{
			this.packet = packet;
			this.sender = sender;
			this.senderPort = senderPort;
		}
	}
	
	private class NetworkThread extends Thread
	{
		public NetworkThread()
		{
			super("NetworkThread");
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
					interpretSignalRaw(new NetworkPacket(buf.clone()), packet.getAddress(), packet.getPort());
				}
				catch (IOException e) {}
			}
		}
	}
}

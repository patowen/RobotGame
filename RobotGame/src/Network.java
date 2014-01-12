import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
 * Multiplayer notes:
 * 
 * [0, rest_of_signal]: normal signal
 * [1, timestamp, sub_timestamp, rest_of_signal]: guaranteed signal
 * [2, timestamp, sub_timestamp]: relayed signal
 * 
 * Log in/out signals:
 * [1, 0]: Log out
 * [1, 1, name]: Log in request (Relayed with [0, 1, 1, id] for granted; [0, 1, 1, reason (negative)] for denied)
 * [1, 2]: Server closed
 * 
 * Entity signals:
 * [3, 0, 0, entity_type, entity_id, x, y, z, xV, yV, zV, appendix]: Spawn entity (Relayed with [0, 3, 0, 0, entity_id])
 * [3, 0, 1, entity_id]: Remove entity
 * [3, 0, 2, entity_id, x, y, z, xV, yV, zV, appendix]: Move entity, not relayed
 */

public abstract class Network
{
	protected DatagramSocket socket;
	protected NetworkThread thread;
	
	private GuaranteedSignalSender guaranteedSender;
	
	public Network()
	{
		guaranteedSender = new GuaranteedSignalSender(this);
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
	
	public void step(double dt)
	{
		guaranteedSender.step(dt);
	}
	
	public void sendGuaranteed(NetworkPacket data, InetAddress ip, int port)
	{
		guaranteedSender.addGuaranteedSignal(data, ip, port);
	}
	
	public void sendNormal(NetworkPacket data, InetAddress ip, int port)
	{
		NetworkPacket packet = new NetworkPacket(data.length()+1);
		packet.addByte(0);
		packet.append(data);
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
	
	protected abstract void interpretSignal(NetworkPacket packet, InetAddress sender, int senderPort);
	
	private void interpretSignalRaw(NetworkPacket packet, InetAddress sender, int senderPort)
	{
		int flag = packet.getByte();
		if (flag == 0)
		{
			interpretSignal(packet, sender, senderPort);
		}
		else if (flag == 1)
		{
			NetworkPacket relayPacket = new NetworkPacket(17);
			relayPacket.addByte(2);
			relayPacket.addLong(packet.getLong());
			relayPacket.addLong(packet.getLong());
			send(relayPacket, sender, senderPort);
			interpretSignal(packet, sender, senderPort); //Temporary. Ordering should matter.
		}
		else if (flag == 2)
		{
			guaranteedSender.addRelayedSignal(sender, senderPort, packet);
		}
	}
	
	private class NetworkThread extends Thread
	{
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
					interpretSignalRaw(new NetworkPacket(buf), packet.getAddress(), packet.getPort());
				}
				catch (IOException e) {}
			}
		}
	}
}

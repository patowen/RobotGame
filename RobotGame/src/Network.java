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
 * [1, 0]: Log out (Relayed with [0, 1, 0])
 * [1, 1, name]: Log in request (Relayed with [0, 1, 1, id] for granted; [0, 1, 1, reason (negative)] for denied)
 * [1, 2]: Server closed (Relayed with [0, 1, 2])
 * 
 * Entity signals:
 * [3, 0, 0, entity_type, entity_id, x, y, z, xV, yV, zV, appendix]: Spawn entity (Relayed with [0, 3, 0, 0, entity_id])
 * [3, 0, 1, entity_id]: Remove entity (Relayed with [0, 3, 0, 1, entity_type])
 * [3, 0, 2, entity_id, x, y, z, xV, yV, zV, appendix]: Move entity, not relayed
 */

public abstract class Network
{
	protected DatagramSocket socket;
	protected NetworkThread thread;
	
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
			
		}
		else if (flag == 2)
		{
			
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

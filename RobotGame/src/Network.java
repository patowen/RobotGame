import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
		interpretSignal(packet, sender, senderPort);
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

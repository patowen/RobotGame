import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;


public class GuaranteedSignalSender
{
	private Network network;
	
	private Set<RelayedSignal> receivedSignals;
	private Node previousSignal;
	private Node currentSignal;
	private int numSignals;
	
	private double pokesPerSecond;
	private long timeout; //in milliseconds
	
	private double pokesRemaining;
	private long currentSignalID;
	
	public GuaranteedSignalSender(Network net)
	{
		network = net;
		
		receivedSignals = new HashSet<RelayedSignal>(256);
		previousSignal = null;
		currentSignal = null;
		numSignals = 0;
		
		pokesPerSecond = 5;
		timeout = 10000;
		
		pokesRemaining = 0;
		currentSignalID = 0;
	}
	
	public void addGuaranteedSignal(NetworkPacket data, InetAddress ip, int port)
	{
		//This creates the beginning of the packet. It needs to be completed by the
		//sender of the guaranteed signal
		long timestamp = System.currentTimeMillis();
		
		NetworkPacket packet = new NetworkPacket(data.length() + 17);
		packet.addByte(1);
		packet.addLong(timestamp);
		packet.addLong(currentSignalID);
		packet.append(data);
		
		Node node = new Node();
		PendingSignal signal = new PendingSignal();
		signal.setData(new NetworkPacket(data.array()));
		signal.ip = ip;
		signal.port = port;
		signal.timestamp = timestamp;
		signal.signalID = currentSignalID;
		node.signal = signal;
		
		synchronized (this)
		{
			addPendingSignal(node);
		}
		
		currentSignalID++;
		
		network.send(packet, ip, port);
	}
	
	public void addRelayedSignal(InetAddress ip, int port, NetworkPacket data)
	{
		receivedSignals.add(new RelayedSignal(ip, port, data));
	}
	
	public void step(double dt)
	{
		long time = System.currentTimeMillis();
		
		pokesRemaining += numSignals*pokesPerSecond*dt;
		int pokesRemainingInt = (int)pokesRemaining;
		pokesRemaining -= pokesRemainingInt;
		
		for (int i=0; i<pokesRemainingInt; i++)
		{
			synchronized (this)
			{
				if (currentSignal == null)
					break;
				
				RelayedSignal rs = new RelayedSignal(currentSignal.signal);
				if (receivedSignals.remove(rs))
				{
					removePendingSignal();
				}
				else
				{
					if (time - currentSignal.signal.timestamp > timeout)
					{
						System.err.println("Don't forget to add timeout disconnecting");
						System.exit(1);
						//TODO disconnect
					}
					else
						poke();
				}
			}
			synchronized (this)
			{
				previousSignal = currentSignal;
				currentSignal = currentSignal.next;
			}
		}
	}
	
	private void poke()
	{
		PendingSignal s = currentSignal.signal;
		NetworkPacket packet = new NetworkPacket(256);
		
		packet.addByte(1);
		packet.addLong(s.timestamp);
		packet.addLong(s.signalID);
		packet.append(s.getData());
		
		network.send(packet, s.ip, s.port);
	}
	
	private void removePendingSignal()
	{
		if (previousSignal == currentSignal)
			previousSignal = currentSignal = null;
		else
		{
			previousSignal.next = currentSignal.next;
			currentSignal = currentSignal.next;
		}
		numSignals--;
	}
	
	private void addPendingSignal(Node newSignal)
	{
		if (previousSignal == null)
		{
			previousSignal = currentSignal = newSignal;
			newSignal.next = newSignal;
		}
		else
		{
			previousSignal.next = newSignal;
			newSignal.next = currentSignal;
		}
		numSignals++;
	}
	
	private class Node
	{
		public Node next;
		public PendingSignal signal;
	}
	
	private class PendingSignal
	{
		public InetAddress ip;
		public int port;
		public long timestamp;
		public long signalID;
		private NetworkPacket data;
		
		public NetworkPacket getData()
		{
			return data.duplicate();
		}
		
		public void setData(NetworkPacket data)
		{
			this.data = data;
		}
	}
	
	private final class RelayedSignal
	{
		private final InetAddress ip;
		private final int port;
		private final long timestamp;
		private final long signalID;
		
		public RelayedSignal(InetAddress ip, int port, NetworkPacket data)
		{
			this.ip = ip;
			this.port = port;
			timestamp = data.getLong();
			signalID = data.getLong();
		}
		
		public RelayedSignal(PendingSignal signal)
		{
			ip = signal.ip;
			port = signal.port;
			timestamp = signal.timestamp;
			signalID = signal.signalID;
		}
		
		public boolean equals(Object o)
		{
			RelayedSignal r = (RelayedSignal)o;
			return (r.ip == ip && r.port == port && r.timestamp == timestamp && r.signalID == signalID);
		}
		
		public int hashCode()
		{
			return (int)signalID;
		}
	}
}

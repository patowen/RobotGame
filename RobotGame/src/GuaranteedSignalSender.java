import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;


public class GuaranteedSignalSender
{
	private Controller c;
	
	private Set<RelayedSignal> receivedSignals;
	private Node previousSignal;
	private Node currentSignal;
	private int numSignals;
	
	private final double pokesPerSecond;
	private final long timeout; //in milliseconds
	
	private double pokesRemaining;
	private long currentSignalID;
	
	public GuaranteedSignalSender(Controller controller)
	{
		c = controller;
		
		receivedSignals = new HashSet<RelayedSignal>(256);
		previousSignal = null;
		currentSignal = null;
		numSignals = 0;
		
		pokesPerSecond = 5;
		timeout = 10000;
		
		pokesRemaining = 0;
		currentSignalID = 0;
	}
	
	public void reset()
	{
		receivedSignals.clear();
		previousSignal = null;
		currentSignal = null;
		numSignals = 0;
		pokesRemaining = 0;
		currentSignalID = 0;
	}
	
	private void send(NetworkPacket data, InetAddress ip, int port)
	{
		Network net = c.getNetwork();
		if (net != null)
			net.send(data, ip, port);
	}
	
	public void addGuaranteedSignal(NetworkPacket data, InetAddress ip, int port)
	{
		//This creates the beginning of the packet. It needs to be completed by the
		//sender of the guaranteed signal
		long timestamp = System.currentTimeMillis();
		
		NetworkPacket packet = new NetworkPacket(data.length() + 9);
		packet.addByte(1);
		packet.addLong(currentSignalID);
		packet.append(data);
		
		Node node = new Node();
		PendingSignal signal = new PendingSignal();
		signal.setData(data);
		signal.ip = ip;
		signal.timestamp = timestamp;
		signal.port = port;
		signal.signalID = currentSignalID;
		node.signal = signal;
		
		synchronized (this)
		{
			addPendingSignal(node);
		}
		
		currentSignalID++;
		
		send(packet, ip, port);
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
						if (!c.isServer())
						{
							reset();
							c.forceDisconnect();
							if (c.isMultiplayer())
								c.setCurrentMenu(new DisconnectedMenu(c));
						}
					}
					else
						poke();
				}
				
				if (currentSignal == null)
					break;
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
		packet.addLong(s.signalID);
		packet.append(s.getData());
		
		send(packet, s.ip, s.port);
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
		if (!c.isServer())
			System.out.println(numSignals);
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
		if (!c.isServer())
			System.out.println(numSignals);
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
		private final long signalID;
		
		public RelayedSignal(InetAddress ip, int port, NetworkPacket data)
		{
			this.ip = ip;
			this.port = port;
			signalID = data.getLong();
		}
		
		public RelayedSignal(PendingSignal signal)
		{
			ip = signal.ip;
			port = signal.port;
			signalID = signal.signalID;
		}
		
		public boolean equals(Object o)
		{
			RelayedSignal r = (RelayedSignal)o;
			return (r.ip.equals(ip) && r.port == port && r.signalID == signalID);
		}
		
		public int hashCode()
		{
			return (int)signalID;
		}
	}
}

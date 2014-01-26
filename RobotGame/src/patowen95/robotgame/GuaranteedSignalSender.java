package patowen95.robotgame;
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
	
	private double pokesRemaining;
	
	public GuaranteedSignalSender(Controller controller)
	{
		c = controller;
		
		receivedSignals = new HashSet<RelayedSignal>(256);
		previousSignal = null;
		currentSignal = null;
		numSignals = 0;
		
		pokesPerSecond = 5;
		
		pokesRemaining = 0;
	}
	
	public void reset()
	{
		receivedSignals.clear();
		previousSignal = null;
		currentSignal = null;
		numSignals = 0;
		pokesRemaining = 0;
	}
	
	private void send(NetworkPacket data, InetAddress ip, int port)
	{
		Network net = c.getNetwork();
		if (net != null)
			net.send(data, ip, port);
	}
	
	public void addGuaranteedSignal(Network network, NetworkPacket data, InetAddress ip, int port)
	{		
		Node node = new Node();
		PendingSignal signal = new PendingSignal();
		signal.setData(data);
		signal.ip = ip;
		signal.port = port;
		signal.signalID = network.createSignalID(ip, port);
		node.signal = signal;
		
		NetworkPacket packet = new NetworkPacket(data.length() + 9);
		packet.addByte(1);
		packet.addLong(signal.signalID);
		packet.append(data);
		
		addPendingSignal(node);
		
		send(packet, ip, port);
	}
	
	public void addRelayedSignal(InetAddress ip, int port, NetworkPacket data)
	{
		receivedSignals.add(new RelayedSignal(ip, port, data));
	}
	
	public synchronized void step(double dt)
	{		
		pokesRemaining += numSignals*pokesPerSecond*dt;
		int pokesRemainingInt = (int)pokesRemaining;
		pokesRemaining -= pokesRemainingInt;
		
		for (int i=0; i<pokesRemainingInt; i++)
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
				poke();
			}
			
			if (currentSignal == null)
				break;
			
			previousSignal = currentSignal;
			currentSignal = currentSignal.next;
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
	
	private synchronized void removePendingSignal()
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
	
	private synchronized void addPendingSignal(Node newSignal)
	{
		if (previousSignal == null)
		{
			previousSignal = currentSignal = newSignal;
			newSignal.next = newSignal;
		}
		else
		{
			previousSignal.next = newSignal;
			previousSignal = newSignal;
			previousSignal.next = currentSignal;
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

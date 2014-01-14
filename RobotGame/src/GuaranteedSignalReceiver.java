import java.net.InetAddress;
import java.util.PriorityQueue;


public class GuaranteedSignalReceiver
{
	private Controller c;
	
	private PriorityQueue<PendingSignal> receivedSignals;
	private InetAddress sourceIP;
	private int sourcePort;
	
	private long currentSignalID;
	
	public GuaranteedSignalReceiver(Controller controller, InetAddress ip, int port)
	{
		c = controller;
		receivedSignals = new PriorityQueue<PendingSignal>(256);
		
		sourceIP = ip;
		sourcePort = port;
		
		currentSignalID = 0;
	}
	
	public void addPendingSignal(long signalID, NetworkPacket data)
	{
		PendingSignal signal = new PendingSignal();
		signal.signalID = signalID;
		signal.setData(data);
		receivedSignals.add(signal);
	}
	
	public void step(double dt)
	{
		while (true)
		{
			PendingSignal signal = receivedSignals.peek();
			if (signal == null)
				break;
			else if (signal.signalID < currentSignalID)
				receivedSignals.remove();
			else if (signal.signalID == currentSignalID)
			{
				receivedSignals.remove();
				c.getNetwork().interpretSignal(signal.data, sourceIP, sourcePort);
				currentSignalID++;
			}
			else
				break;
		}
	}
	
	private class PendingSignal implements Comparable<PendingSignal>
	{
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
		
		public int compareTo(PendingSignal signal)
		{
			return (int)(signalID - signal.signalID);
		}
	}
}

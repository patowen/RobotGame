import java.net.InetAddress;
import java.util.PriorityQueue;


public class GuaranteedSignalReceiver
{
	private Controller c;
	
	public GuaranteedSignalReceiver(Controller controller)
	{
		c = controller;
	}
	
	public void reset()
	{
		//TODO reset
	}
	
	private class Source
	{
		private PriorityQueue<PendingSignal> receivedSignals;
		private InetAddress sourceIP;
		private int sourcePort;
		
		private long currentSignalID;
		
		public Source(InetAddress ip, int port)
		{
			receivedSignals = new PriorityQueue<PendingSignal>(256);
			
			sourceIP = ip;
			sourcePort = port;
			
			currentSignalID = 0;
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
					c.getNetwork().interpretSignal(signal.data, signal.ip, signal.port);
					currentSignalID++;
				}
				else
					break;
			}
		}
	}
	
	private class PendingSignal implements Comparable<PendingSignal>
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
		
		public int compareTo(PendingSignal signal)
		{
			return (int)(signalID - signal.signalID);
		}
	}
}

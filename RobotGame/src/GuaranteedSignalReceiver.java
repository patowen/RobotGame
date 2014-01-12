import java.net.InetAddress;
import java.util.PriorityQueue;


public class GuaranteedSignalReceiver
{
	private Network network;
	
	private PriorityQueue<PendingSignal> receivedSignals;
	private int numSignals;
	private InetAddress sourceIP;
	private int sourcePort;
	
	private long currentSignalID;
	
	public GuaranteedSignalReceiver(Network net, InetAddress ip, int port)
	{
		network = net;
		sourceIP = ip;
		sourcePort = port;
		
		receivedSignals = new PriorityQueue<PendingSignal>(256);
		numSignals = receivedSignals.size();
		
		currentSignalID = 0;
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
}

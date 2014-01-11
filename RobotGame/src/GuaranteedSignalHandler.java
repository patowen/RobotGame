
public class GuaranteedSignalHandler
{
	private Network network;
	
	private PendingSignal previousSignal;
	private PendingSignal currentSignal;
	private int numSignals;
	
	private double pokesPerSecond;
	private double pokesRemaining;
	
	public GuaranteedSignalHandler(Network net)
	{
		network = net;
		
		previousSignal = null;
		currentSignal = null;
		numSignals = 0;
		
		pokesPerSecond = 5;
		pokesRemaining = 0;
	}
	
	public void step(double dt)
	{
		pokesRemaining += numSignals*pokesPerSecond*dt;
		int pokesRemainingInt = (int)pokesRemaining;
		pokesRemaining -= pokesRemainingInt;
		
		for (int i=0; i<pokesRemainingInt; i++)
		{
		}
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
	
	private synchronized void addPendingSignal(PendingSignal newSignal)
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
	}
	
	private class PendingSignal
	{
		public PendingSignal next;
	}
}

import java.util.Iterator;
import java.util.LinkedList;


public class GuaranteedSignalHandler
{
	private LinkedList<PendingSignal> pending;
	private Iterator<PendingSignal> iter;
	
	private double pokesPerSecond;
	private double pokesRemaining;
	
	public GuaranteedSignalHandler()
	{
		pending = new LinkedList<PendingSignal>();
		iter = pending.iterator();
		
		pokesPerSecond = 5;
		pokesRemaining = 0;
	}
	
	public void step(double dt)
	{
		pokesRemaining += pending.size()*pokesPerSecond*dt;
		int pokesRemainingInt = (int)pokesRemaining;
		
		for (int i=0; i<pokesRemainingInt; i++)
		{
			
		}
	}
	
	private class PendingSignal
	{
		
	}
}

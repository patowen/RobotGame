import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Wraps ByteBuffer in a more programmer-friendly way for use in sending network signals
 * @author Patrick Owen
 */
public class NetworkPacket
{
	private ByteBuffer buf;
	
	public ByteBuffer buffer()
	{
		return buf;
	}
	
	public NetworkPacket duplicate()
	{
		NetworkPacket packet = new NetworkPacket();
		packet.buf = buf.duplicate();
		return packet;
	}
	
	public byte[] array()
	{
		return buf.array();
	}
	
	public int length()
	{
		return buf.position();
	}
	
	public void append(NetworkPacket packet)
	{
		buf.put(packet.array(), 0, packet.length());
	}
	
//	public void append(byte[] values, int length)
//	{
//		buf.put(values, 0, length);
//	}
	
//	public NetworkPacket get()
//	{
//		return new NetworkPacket(getBytes(buf.remaining()));
//	}
	
	private NetworkPacket()
	{
		
	}
	
	public NetworkPacket(int numBytes)
	{
		buf = ByteBuffer.allocate(numBytes).order(ByteOrder.LITTLE_ENDIAN);
	}
	
	public NetworkPacket(byte[] values)
	{
		buf = ByteBuffer.wrap(values).order(ByteOrder.LITTLE_ENDIAN);
	}
	
	public void addString(String value)
	{
		for (int i=0; i<value.length(); i++)
		{
			buf.putChar(value.charAt(i));
		}
		buf.putChar('\0');
	}
	
	public String getString()
	{
		StringBuilder output = new StringBuilder();
		
		char next;
		while ((next = buf.getChar()) != '\0')
		{
			output.append(next);
		}
		
		return output.toString();
	}
	
	public void addDoubles(double... values)
	{
		for (int i=0; i<values.length; i++)
		{
			buf.putDouble(values[i]);
		}
	}
	
	public void addDouble(double value)
	{
		buf.putDouble(value);
	}
	
	public double[] getDoubles(int amount)
	{
		double[] output = new double[amount];
		for (int i=0; i<amount; i++)
		{
			output[i] = buf.getDouble();
		}
		
		return output;
	}
	
	public double getDouble()
	{
		return buf.getDouble();
	}
	
	public void addLongs(long... values)
	{
		for (int i=0; i<values.length; i++)
		{
			buf.putLong(values[i]);
		}
	}
	
	public void addLong(long value)
	{
		buf.putLong(value);
	}
	
	public long[] getLongs(int amount)
	{
		long[] output = new long[amount];
		for (int i=0; i<amount; i++)
		{
			output[i] = buf.getLong();
		}
		
		return output;
	}
	
	public long getLong()
	{
		return buf.getLong();
	}
	
	public void addInts(int... values)
	{
		for (int i=0; i<values.length; i++)
		{
			buf.putInt(values[i]);
		}
	}
	
	public void addInt(int value)
	{
		buf.putInt(value);
	}
	
	public int[] getInts(int amount)
	{
		int[] output = new int[amount];
		for (int i=0; i<amount; i++)
		{
			output[i] = buf.getInt();
		}
		
		return output;
	}
	
	public int getInt()
	{
		return buf.getInt();
	}
	
	public void addShorts(int... values)
	{
		for (int i=0; i<values.length; i++)
		{
			buf.putShort((short)values[i]);
		}
	}
	
	public void addShort(int value)
	{
		buf.putShort((short)value);
	}
	
	public short[] getShorts(int amount)
	{
		short[] output = new short[amount];
		for (int i=0; i<amount; i++)
		{
			output[i] = buf.getShort();
		}
		
		return output;
	}
	
	public short getShort()
	{
		return buf.getShort();
	}
	
	public void addBytes(int... values)
	{
		for (int i=0; i<values.length; i++)
		{
			buf.put((byte)values[i]);
		}
	}
	
	public void addByte(int value)
	{
		buf.put((byte)value);
	}
	
	public byte[] getBytes(int amount)
	{
		byte[] output = new byte[amount];
		for (int i=0; i<amount; i++)
		{
			output[i] = buf.get();
		}
		
		return output;
	}
	
	public byte getByte()
	{
		return buf.get();
	}
}

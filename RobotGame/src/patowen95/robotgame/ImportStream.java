package patowen95.robotgame;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Wraps ByteBuffer in a more programmer-friendly way for use in sending network signals
 * @author Patrick Owen
 */
public class ImportStream
{
	private BufferedInputStream input;
	
	public ImportStream(BufferedInputStream input)
	{
		this.input = input;
	}
	
	public String getString() throws IOException
	{
		StringBuilder builder = new StringBuilder();
		while (true)
		{
			byte[] bytes = new byte[2];
			if (input.read(bytes) != 2) throw new IOException();
			char c = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getChar();
			if (c == '\0')
				break;
			builder.append(c);
		}
		return builder.toString();
	}
	
	public double getDouble() throws IOException
	{
		byte[] bytes = new byte[8];
		if (input.read(bytes) != 8) throw new IOException();
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getDouble();
	}
	
	public float getFloat() throws IOException
	{
		byte[] bytes = new byte[4];
		if (input.read(bytes) != 4) throw new IOException();
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
	}
	
	public long getLong() throws IOException
	{
		byte[] bytes = new byte[8];
		if (input.read(bytes) != 8) throw new IOException();
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}
	
	public int getInt() throws IOException
	{
		byte[] bytes = new byte[4];
		if (input.read(bytes) != 4) throw new IOException();
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}
	
	public int getShort() throws IOException
	{
		byte[] bytes = new byte[2];
		if (input.read(bytes) != 2) throw new IOException();
		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}
	
	public int getByte() throws IOException
	{
		return input.read();
	}
	
	public void close() throws IOException
	{
		input.close();
	}
}

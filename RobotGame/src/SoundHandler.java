import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.jogamp.openal.UnsupportedAudioFileException;
import com.jogamp.openal.sound3d.AudioSystem3D;
import com.jogamp.openal.sound3d.Buffer;
import com.jogamp.openal.sound3d.Context;
import com.jogamp.openal.sound3d.Listener;
import com.jogamp.openal.sound3d.Source;
import com.jogamp.openal.sound3d.Vec3f;

/**
 * Handles audio output in the game.
 * @author Patrick Owen
 */
public class SoundHandler
{
	private Context context;
	private Buffer[] sounds;
	private float[] gain;
	private ArrayList<Source> sources;
	private Listener l;
	
	/**
	 * Constructs a SoundHandler object.
	 */
	public SoundHandler()
	{
		sounds = new Buffer[3];
		gain = new float[3];
		sources = new ArrayList<Source>();
		
		AudioSystem3D.init();
		context = AudioSystem3D.createContext(AudioSystem3D.openDevice(null));
		AudioSystem3D.makeContextCurrent(context);
		l = AudioSystem3D.getListener();
		
		try
		{
			sounds[0] = AudioSystem3D.loadBuffer(new FileInputStream(new File("sound/laser.wav"))); gain[0] = 0.3f;
			sounds[1] = AudioSystem3D.loadBuffer(new FileInputStream(new File("sound/explosion.wav"))); gain[1] = 1f;
			sounds[2] = AudioSystem3D.loadBuffer(new FileInputStream(new File("sound/zap.wav"))); gain[2] = 0f;
		}
		catch (IOException | UnsupportedAudioFileException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		l.setPosition(-2, 0, 0);
		l.setVelocity(new Vec3f(0, 0, 0));
		l.setOrientation(new float[] {0, 0, 1, 1, 0, 0});
		l.setGain(0.3f);
		
	}
	
	public void step()
	{
		for (int i=0; i<sources.size(); i++)
		{
			if (!sources.get(i).isPlaying())
			{
				sources.get(i).delete();
				sources.remove(i);
				i--;
			}
		}
	}
	
	public void destroy()
	{
		context.getDevice().close();
		context.destroy();
	}
	
	/**
	 * Updates the location of the ears
	 * @param x
	 * @param y
	 * @param z Listener coordinates
	 */
	public void setListenerPosition(double x, double y, double z)
	{
		l.setPosition(-(float)x, (float)y, (float)z);
	}
	
	/**
	 * Updates the orientation of the ears
	 * @param x
	 * @param y
	 * @param z Listener coordinates
	 */
	public void setListenerOrientation(double xTo, double yTo, double zTo, double xUp, double yUp, double zUp)
	{
		l.setOrientation(new float[] {-(float)xUp, (float)yUp, (float)zUp, -(float)xTo, (float)yTo, (float)zTo});
	}
	
	/**
	 * Plays a sound between the listener's ears
	 * @param i index of the sound to be played
	 */
	public void playSound(int i)
	{
		Source s = AudioSystem3D.generateSource(sounds[i]);
		
		s.setPosition(0, 1, 0);
		s.setVelocity(new Vec3f(0, 0, 0));
		s.setGain(gain[i]);
		s.setSourceRelative(true);
		s.setReferenceDistance(4);
		s.setLooping(false);
		s.play();
		
		sources.add(s);
	}
	
	/**
	 * Plays a sound at a given location
	 * @param i Index of the sound to be played
	 * @param x
	 * @param y
	 * @param z Location of the sound source
	 */
	public void playSound(int i, double x, double y, double z)
	{
		Source s = AudioSystem3D.generateSource(sounds[i]);
		
		s.setPosition(-(float)x, (float)y, (float)z);
		s.setVelocity(new Vec3f(0, 0, 0));
		s.setGain(gain[i]);
		s.setSourceRelative(false);
		s.setReferenceDistance(4);
		s.setLooping(false);
		s.play();
		
		sources.add(s);
	}
}

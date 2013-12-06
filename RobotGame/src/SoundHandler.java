import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Handles audio output in the game.
 * @author Patrick Owen
 */
public class SoundHandler
{
	private double attenuationConstant = 6;//attenuation constant of sound based on distance
	private double lx, ly, lz;//Listener coordinates
	private static final String SOUND_DIR = "sound/";
	private String[] fileList = {"laser.wav", "explosion.wav"};//Designated files to be loaded, in order
	private Clip[] clipList;
	
	/**
	 * Constructs a SoundHandler object.
	 */
	public SoundHandler()
	{
		clipList = new Clip[fileList.length];
		try
		{
			for (int i = 0; i < fileList.length; i++)
			{				
				FileInputStream fs = new FileInputStream(SOUND_DIR + fileList[i]); 
				BufferedInputStream bufferStream = new BufferedInputStream(fs); 
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferStream);
				
				Clip temp = AudioSystem.getClip();
				temp.open(audioStream);
				clipList[i] = temp;
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedAudioFileException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (LineUnavailableException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates the location of the ears
	 * @param x
	 * @param y
	 * @param z Listener coordinates
	 */
	public void setListenerPosition(double x, double y, double z)
	{
		lx = x;
		ly = y;
		lz = z;
	}
	
	/**
	 * Plays a sound between the listener's ears
	 * @param i index of the sound to be played
	 */
	public void playSound(int i)
	{
		Clip sound = clipList[i];

		if (sound.isRunning())
			sound.stop();
		sound.setFramePosition(0);
		sound.start();
		FloatControl gainControl = (FloatControl) sound.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(-10f); 
		
		((FloatControl)sound.getControl(FloatControl.Type.PAN)).setValue(0);
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
		double sqrDistance = Math.pow(lx - x, 2) + Math.pow(ly - y, 2) + Math.pow(lz - z, 2);
		double attenuation = attenuationConstant * Math.log10(sqrDistance);
		
		Clip sound = clipList[i];
		if (sound.isRunning())
			sound.stop();
		sound.setFramePosition(0);
		sound.start();
		
		FloatControl gainControl = (FloatControl) sound.getControl(FloatControl.Type.MASTER_GAIN);
		if (attenuation > -gainControl.getMinimum()) attenuation = gainControl.getMinimum();
		if (attenuation < -gainControl.getMaximum()) attenuation = gainControl.getMaximum();
		gainControl.setValue(-(float)attenuation);
	}
	
}
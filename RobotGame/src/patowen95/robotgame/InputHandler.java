package patowen95.robotgame;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;

/**
 * Handles mouse grabbing and keyboard input.
 * @author Patrick Owen
 * @author Michael Ekstrom
 */
public class InputHandler implements KeyListener, MouseListener //Add MouseMotionListener, add methods at bottom
{
	private Robot robot;
	private GLWindow win;
	private KeyListener listener;
	
	private boolean inputEnabled;
	
	//Mouse controls
	private double mouseX, mouseY;
	private double mouseSensitivity;
	private double mouseXPos, mouseYPos;
	
	//Variables used outside to choose which control is being inspected
	/**
	 * Index for the specified key control, used for reference.
	 */
	public static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3, JUMP = 4, PAUSE = 5, CHARGE = 6,
			WEAPON1 = 7, WEAPON2 = 8, WEAPON3 = 9; //Keyboard
	
	/**
	 * Number of keys held by the InputHandler
	 */
	public static final int NUM_KEYS = 10;
	
	/**
	 * Index for the specified mouse control, used for reference.
	 */
	public static final int FIRE = 0, CLICK = 1; //Mouse
	
	/**
	 * Number of mouse buttons held by the InputHandler
	 */
	public static final int NUM_MOUSE_BUTTONS = 2;
	
	//Key code of each control
	private int[] keyCode;
	
	//Mouse button of each control
	private int[] mouseButton;
	
	//Whether the keys are down.
	private boolean[] keyDown;
	
	//Whether the mouse buttons are down
	private boolean[] mouseDown;
	
	//Whether the key is canceled
	private boolean[] keyCanceled;
	
	//Whether the mouse button is canceled
	private boolean[] mouseCanceled;
	
	//Whether the keys were just pressed.
	private boolean[] keyPressed;
	
	//Whether the mouse buttons were just pressed
	private boolean[] mousePressed;
	
	//Used to determine whether the keys were just pressed.
	private boolean[] keyHelper;
	
	//Used to determine whether the mouse buttons were just pressed
	private boolean[] mouseHelper;
	
	/**
	 * Initializes the inputs and sets up a Robot that controls the mouse.
	 * @param comp The component that reads keyboard events.
	 */
	public InputHandler(GLWindow window)
	{
		try
		{
			robot = new Robot();
		}
		catch (AWTException e)
		{
			throw new RuntimeException("The program will not function properly with the current permissions");
		}
		
		win = window;
		win.addKeyListener(this);
		win.addMouseListener(this);
		listener = null;
		inputEnabled = true;
		
		mouseSensitivity = 1.0/45/200;
		
		//Initialize keys
		keyCode = new int[NUM_KEYS];
		keyCode[0] = KeyEvent.VK_W;
		keyCode[1] = KeyEvent.VK_S;
		keyCode[2] = KeyEvent.VK_A;
		keyCode[3] = KeyEvent.VK_D;
		keyCode[4] = KeyEvent.VK_SPACE;
		keyCode[5] = KeyEvent.VK_ESCAPE;
		keyCode[6] = KeyEvent.VK_SHIFT;
		keyCode[7] = KeyEvent.VK_1;
		keyCode[8] = KeyEvent.VK_2;
		keyCode[9] = KeyEvent.VK_3;
		
		mouseButton = new int[NUM_MOUSE_BUTTONS];
		mouseButton[0] = MouseEvent.BUTTON1;
		mouseButton[1] = MouseEvent.BUTTON1;
		
		//Initialize key and mouse control variables.
		keyDown = new boolean[NUM_KEYS];
		keyCanceled = new boolean[NUM_KEYS];
		keyPressed = new boolean[NUM_KEYS];
		keyHelper = new boolean[NUM_KEYS];
		mouseDown = new boolean[NUM_MOUSE_BUTTONS];
		mouseCanceled = new boolean[NUM_MOUSE_BUTTONS];
		mousePressed = new boolean[NUM_MOUSE_BUTTONS];
		mouseHelper = new boolean[NUM_MOUSE_BUTTONS];
		for (int i=0; i<NUM_KEYS; i++)
		{
			keyDown[i] = false;
			keyCanceled[i] = false;
			keyPressed[i] = false;
			keyHelper[i] = false;
		}
		
		for (int i=0; i<NUM_MOUSE_BUTTONS; i++)
		{
			mouseDown[i] = false;
			mouseCanceled[i] = false;
			mousePressed[i] = false;
			mouseHelper[i] = false;
		}
	}
	
	public void setInputEnabled(boolean enabled)
	{
		inputEnabled = enabled;
	}
	
	/**
	 * Determines how far the mouse moved from the center and resets the mouse to the center.
	 * The information can be retrieved with getMouseX and getMouseY
	 * @see #getMouseX()
	 * @see #getMouseY()
	 */
	public void readMouse()
	{
		if (win.hasFocus() && inputEnabled)
		{
			Point mousePos = MouseInfo.getPointerInfo().getLocation();
			int centerX = win.getX() + win.getWidth()/2;
			int centerY = win.getY() + win.getHeight()/2;
			
			mouseX = (mousePos.getX()-centerX)*mouseSensitivity;
			mouseY = (mousePos.getY()-centerY)*mouseSensitivity;
			
			robot.mouseMove(centerX, centerY);
		}
		else
		{
			mouseX = 0;
			mouseY = 0;
		}
	}
	
	/**
	 * Adds a KeyListener to listen for the same key events that the InputHandler
	 * receives. This is useful for when the keyboard is used for something other
	 * than keyboard controls.
	 * @param keyListener The KeyListener to use. Set to null for no key listener.
	 */
	public void setKeyListener(KeyListener keyListener)
	{
		listener = keyListener;
	}
	
	/**
	 * Updates which keys are pressed and which are not.
	 */
	public void updatePressed()
	{
		for (int i=0; i<NUM_KEYS; i++)
		{
			if (keyHelper[i])
			{
				keyPressed[i] = true;
				keyHelper[i] = false;
			}
			else
			{
				keyPressed[i] = false;
			}
		}
		
		for (int i=0; i<NUM_MOUSE_BUTTONS; i++)
		{
			if (mouseHelper[i])
			{
				mousePressed[i] = true;
				mouseHelper[i] = false;
			}
			else
			{
				mousePressed[i] = false;
			}
		}
	}
	
	/**
	 * Prevents anything from receiving a given mouse event until the mouse button is released.
	 * @param button
	 */
	public void cancelMouseButton(int button)
	{
		if (mouseDown[button])
			mouseCanceled[button] = true;
		
		//Cancel all equivalent mouse events
		for (int i=0; i<NUM_MOUSE_BUTTONS; i++)
		{
			if (i == button) continue;
			
			if (mouseDown[i] && mouseButton[i] == mouseButton[button])
				mouseCanceled[i] = true;
		}
	}
	
	/**
	 * Prevents anything from receiving a given key event until the key is released.
	 * @param key
	 */
	public void cancelKey(int key)
	{
		if (keyDown[key])
			keyCanceled[key] = true;
		
		//Cancel all equivalent key events
		for (int i=0; i<NUM_KEYS; i++)
		{
			if (i == key) continue;
			
			if (keyDown[i] && keyCode[i] == keyCode[key])
				keyCanceled[i] = true;
		}
	}
	
	/**
	 * Returns the current x value of the mouse's position
	 * @return Mouse x value
	 */
	public double getMouseXPos()//HEY
	{
		return mouseXPos;
	}//end getMouseXPos
	
	/**
	 * Returns the current y value of the mouse's position
	 * @return Mouse y value
	 */
	public double getMouseYPos()//HEY
	{
		return mouseYPos;
	}//end getMouseYPos
	
	/**
	 * Returns the x-component of the displacement of the mouse determined by the readMouse method.
	 * @see #readMouse()
	 */
	public double getMouseX()
	{
		return mouseX;
	}
	
	/**
	 * Returns the y-component of the displacement of the mouse determined by the readMouse method.
	 * @see #readMouse()
	 */
	public double getMouseY()
	{
		return mouseY;
	}
	
	/**
	 * Returns whether a specific control is down.
	 * @param key Which key to check.
	 */
	public boolean getKey(int key)
	{
		if (keyCanceled[key] || !inputEnabled) return false;
		return keyDown[key];
	}
	
	/**
	 * Returns whether a specific mouse button is down.
	 * @param button Which button to check.
	 */
	public boolean getMouseButton(int button)
	{
		if (mouseCanceled[button] || !inputEnabled) return false;
		return mouseDown[button];
	}
	
	/**
	 * Returns whether a specific control was just pressed.
	 * @param key Which key to check.
	 */
	public boolean getKeyPressed(int key)
	{
		if (keyCanceled[key] || !inputEnabled) return false;
		return keyPressed[key];
	}
	
	/**
	 * Returns whether a specific mouse button was just pressed.
	 * @param button Which button to check.
	 */
	public boolean getMouseButtonPressed(int button)
	{
		if (mouseCanceled[button] || !inputEnabled) return false;
		return mousePressed[button];
	}
	
	/**
	 * Updates the keys based on the event received by the component.
	 */
	public void keyPressed(KeyEvent e)
	{
		if (listener != null)
			listener.keyPressed(e);
		
		if (e.isAutoRepeat())
			return;
		
		//Handle keys
		for (int i=0; i<NUM_KEYS; i++)
		{
			if (e.getKeyCode() == keyCode[i])
			{
				if (!keyDown[i]) keyHelper[i] = true;
				keyDown[i] = true;
			}
		}
	}
	
	/**
	 * Updates the keys based on the event received by the component.
	 */
	public void keyReleased(KeyEvent e)
	{
		if (listener != null)
			listener.keyReleased(e);
		
		if (e.isAutoRepeat())
			return;
		
		//Handle all keys
		for (int i=0; i<NUM_KEYS; i++)
		{
			if (e.getKeyCode() == keyCode[i])
			{
				keyDown[i] = false;
				keyCanceled[i] = false;
			}
		}
	}
	
	public void mouseClicked(MouseEvent e) {}
	
	/**
	 * Updates the mouse buttons based on the event received by the component.
	 */
	public void mousePressed(MouseEvent e)
	{
		//Handle other keys
		for (int i=0; i<NUM_MOUSE_BUTTONS; i++)
		{
			if (e.getButton() == mouseButton[i])
			{
				if (!mouseDown[i]) mouseHelper[i] = true;
				mouseDown[i] = true;
			}
		}
	}
	
	/**
	 * Updates the mouse buttons based on the event received by the component.
	 */
	public void mouseReleased(MouseEvent e)
	{
		//Handle all buttons
		for (int i=0; i<NUM_MOUSE_BUTTONS; i++)
		{
			if (e.getButton() == mouseButton[i])
			{
				mouseDown[i] = false;
				mouseCanceled[i] = false;
			}
		}
	}
	

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}
	
	/**
	 * Updates the mouse position when the mouse is moved
	 */
	public void mouseDragged(MouseEvent e)
	{
		mouseXPos = e.getX();
		mouseYPos = e.getY();
	}
	
	/**
	 * Updates the mouse position when the mouse is moved
	 */
	public void mouseMoved(MouseEvent e)
	{
		mouseXPos = e.getX();
		mouseYPos = e.getY();
	}
	
	public void mouseWheelMoved(MouseEvent e) {}
}

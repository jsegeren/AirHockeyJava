package airhockeyjava.input;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Interface for the input layer. Extends runnable to enforce ability to run in separate thread.
 *
 * @author Joshua Segeren
 *
 */
public interface IInputLayer extends Runnable, MouseMotionListener, MouseListener, KeyListener {

	/**
	 * Retrieve mouse position x-coordinate
	 *
	 * @return int x-pixel value
	 */
	public int getMouseX();

	/**
	 * Retrieve mouse position y-coordinate
	 *
	 * @return int y-pixel value
	 */
	public int getMouseY();

	/**
	 * Retrieve the last key pressed or VK_UNDEFINED if all keypresses have beed handled
	 *
	 * @return int key code of last key pressed
	 */
	public int handleKeyPress();
}
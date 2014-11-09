package airhockeyjava.input;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Interface for the input layer. Extends runnable to enforce ability to run in separate thread.
 * 
 * @author Joshua Segeren
 *
 */
public interface IInputLayer extends Runnable, MouseMotionListener, MouseListener {

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
}
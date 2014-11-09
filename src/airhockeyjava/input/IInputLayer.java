package airhockeyjava.input;

/**
 * Interface for the input layer. Extends runnable to enforce ability to run in separate thread.
 * 
 * @author Joshua Segeren
 *
 */
public interface IInputLayer extends Runnable {

	/**
	 * Method to start accepting input.
	 */
	public abstract void start();
}
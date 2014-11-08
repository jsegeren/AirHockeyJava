package airhockeyjava.graphics;

/**
 * Interface for the GUI layer. Extends Runnable to enforce ability to run in separate thread.
 * Intended to provide its own internal loop for rendering the visual output.
 * @author Joshua Segeren
 *
 */
public interface IGuiLayer extends Runnable {
	
	/**
	 * Method to start rendering.
	 */
	public abstract void start();
}
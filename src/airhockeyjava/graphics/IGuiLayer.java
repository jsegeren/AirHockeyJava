package airhockeyjava.graphics;

/**
 * Interface for the GUI layer. Intended to provide its own internal loop for rendering the visual output.
 * @author Joshua Segeren
 *
 */
public interface IGuiLayer {
	
	/**
	 * Method to start rendering.
	 */
	public abstract void start();
}
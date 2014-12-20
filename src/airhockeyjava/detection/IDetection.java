package airhockeyjava.detection;

/**
 * Interface for the detection layer which determines and updates states of moving items.
 * 
 * @author Joshua Segeren
 *
 */
public interface IDetection {

	/**
	 * Method to either calculate or visually detect, and then update item states.
	 */
	public abstract void detectAndUpdateItemStates(float deltaTime);

}
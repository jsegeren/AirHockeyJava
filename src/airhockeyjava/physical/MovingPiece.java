/**
 * 
 */
package airhockeyjava.physical;

/**
 * @author Joshua Segeren
 *
 */
public interface MovingPiece {
	
	/**
	 * Method to check whether two pieces will collide, given their current trajectories,
	 * and expected friction, in a given interval of time (dt).
	 * @param otherPiece, dt
	 * @return true if collision imminent
	 */
	public boolean willCollide(MovingPiece otherPiece, float dt);
	
	public void updatePosition
	
}

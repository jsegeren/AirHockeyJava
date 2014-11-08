package airhockeyjava.physical;

import airhockeyjava.util.Position;
import airhockeyjava.util.Vector2;

/** 
 * Class to represent the mallet.
 * @author Joshua Segeren
 *
 */
public class Mallet implements IMovingItem {
	
	private Position position;
	private Vector2 velocity;
	private float weight; // Used in simulated friction calculation
	
	public Mallet(Position position, Vector2 velocity) {
		this.position = position;
		this.velocity = velocity;
	}
	
	@Override
	public boolean willCollide(IMovingItem otherPiece) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void updatePosition() {
		
	}
	
	@Override
	public void updateVelocity() {
		
	}
}
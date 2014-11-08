package airhockeyjava.physical;

import airhockeyjava.util.Position;
import airhockeyjava.util.Vector2;

/** 
 * Class to represent the mallet
 * @author Josh
 *
 */
public class Mallet implements MovingPiece {
	
	private Position position;
	private Vector2 velocity;
	
	public Mallet(Position position, Vector2 velocity) {
		this.position = position;
		this.velocity = velocity;
	}
	
	@Override
	public void updatePosition(float deltaTime) {
		
	}

	@Override
	public boolean willCollide(MovingPiece otherPiece, float deltaTime) {
		// TODO Auto-generated method stub
		return false;
	}
}
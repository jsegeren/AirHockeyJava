package airhockeyjava.physical;

import airhockeyjava.util.Vector2;
import airhockeyjava.util.Color;

/** 
 * Superclass to represent moving items.
 * @author Joshua Segeren
 *
 */
public class MovingItem implements IMovingItem {
	
	private Vector2 position;
	private Vector2 velocity;
	private Color color;
	private float weight; // Used in simulated friction calculation
	
	/**
	 * Constructor which sets the starting state of the item.
	 * @param position
	 * @param velocity
	 */
	public MovingItem(Vector2 position, Vector2 velocity, Color color) {
		this.position = position;
		this.velocity = velocity;
		this.color = color;
	}
	
	@Override
	public boolean willCollide(IMovingItem otherPiece) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void updatePosition() {
		// TODO implement physics for next position calculation
	}
	
	@Override
	public void updateVelocity() {
		// TODO implement physics for next velocity calculation
	}
	
	@Override
	public Vector2 getPosition() {
		return this.position;
	}
	
	@Override
	public Vector2 getVelocity() {
		return this.velocity;
	}
}
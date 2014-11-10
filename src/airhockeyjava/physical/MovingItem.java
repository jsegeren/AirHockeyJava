package airhockeyjava.physical;

import airhockeyjava.util.Vector2;

/** 
 * Superclass to represent moving items.
 * @author Joshua Segeren
 *
 */
public abstract class MovingItem implements IMovingItem {
	
	private Vector2 position;
	private Vector2 velocity;
	private final float mass; // Used in simulated friction calculation
	private final float radius;
	
	/**
	 * Constructor which sets the starting state of the item.
	 * @param position
	 * @param velocity
	 */
	protected MovingItem(Vector2 position, Vector2 velocity, float radius, float mass) {
		this.position = position;
		this.velocity = velocity;
		this.radius = radius;
		this.mass = mass;
	}
	

	@Override
	public Vector2 getPosition() {
		return this.position;
	}
	
	@Override
	public Vector2 getVelocity() {
		return this.velocity;
	}
	
	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}
	
	@Override
	public float getRadius() {
		return this.radius;
	}
}
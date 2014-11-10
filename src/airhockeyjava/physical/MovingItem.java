package airhockeyjava.physical;

import java.util.Stack;

import airhockeyjava.game.Constants;
import airhockeyjava.util.Vector2;

/** 
 * Superclass to represent moving items.
 * @author Joshua Segeren
 *
 */
public abstract class MovingItem implements IMovingItem {

	/**
	 * Internal class used for elements of state history structure
	 *
	 */
	private class PositionAndDuration {
		private Vector2 position;
		private long nanoTime;

		PositionAndDuration(Vector2 position, long nanoTime) {
			this.position = position;
			this.nanoTime = nanoTime;
		}
	}

	/**
	 * Class for implementing fixed-size stack. Used for maintaining previous
	 * state history and allowing most recent items to be easily accessible.
	 * @author Josh
	 *
	 * @param <T>
	 */
	public class FixedStack<T> extends Stack<T> {
		private static final long serialVersionUID = 1L;
		private final int fixedMaximumSize;

		FixedStack(int fixedMaximumSize) {
			this.fixedMaximumSize = fixedMaximumSize;
		}

		@Override
		public T push(T item) {
			if (this.size() == this.fixedMaximumSize) {
				this.removeElementAt(0);
			}
			return super.push(item);
		}
	}

	private FixedStack<PositionAndDuration> previousStateStack;
	private Vector2 position;
	private Vector2 velocity;
	private final float mass; // Used in simulated friction calculation and energy transfer model
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
		previousStateStack = new FixedStack<PositionAndDuration>(
				Constants.NUMBER_PREVIOUS_STATES_TRACKED);
	}

	@Override
	public Vector2 getPosition() {
		return this.position;
	}

	@Override
	public void setPosition(Vector2 newPosition) {
		this.position = newPosition;
	}

	@Override
	public Vector2 getVelocity() {
		return this.velocity;
	}

	@Override
	public void setVelocity(Vector2 newVelocity) {
		this.velocity = newVelocity;
	}

	@Override
	public float getRadius() {
		return this.radius;
	}

	@Override
	public float getMass() {
		return this.mass;
	}

	/**
	 * Updates position, velocity based on previous state information. Could use a Kalman filter for smoothing.
	 * Right now we are using a direct average: v_1 = (p_1 - p_0) / T, where p_1 is the most recent position and
	 * p_0 is the oldest position available to the state model, and T is the total duration.
	 */
	@Override
	public void updatePositionAndCalculateVelocity(Vector2 newPosition) {
		PositionAndDuration currentState = new PositionAndDuration(newPosition, System.nanoTime());
		previousStateStack.push(currentState);
		PositionAndDuration oldestState = previousStateStack.elementAt(0);

		// Calculate and update the velocity, position if changed
		if (!newPosition.equals(oldestState.position)) {
			Vector2 newVelocity = new Vector2(newPosition);
			float deltaTimeSeconds = ((float) (currentState.nanoTime - oldestState.nanoTime)) / 1000000000f;
			newVelocity.sub(oldestState.position).scl(1f / deltaTimeSeconds);

			// Need to enforce maximum speed limit on user paddle; recalculate position if necessary
			Vector2 newLimitedVelocity = new Vector2(newVelocity)
					.limit(Constants.MAX_USER_MALLET_SPEED_METERS_PER_SECOND);

			Vector2 distanceDifference = newVelocity.sub(new Vector2(newLimitedVelocity)).scl(
					deltaTimeSeconds);
			this.position = newPosition.sub(distanceDifference);

			if (!this.velocity.equals(newLimitedVelocity)) {
				this.velocity = newLimitedVelocity;
				if (this.velocity.x - 0 > 0.00001 || this.velocity.y - 0 > 0.00001) {
					System.out.println(String.format("v_x = %f, v_y = %f", this.velocity.x,
							this.velocity.y));
				}
			} else {
				previousStateStack.pop();
			}
		}
	}
}
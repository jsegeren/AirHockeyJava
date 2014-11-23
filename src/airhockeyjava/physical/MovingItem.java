package airhockeyjava.physical;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Stack;

import airhockeyjava.game.Constants;
import airhockeyjava.util.Vector2;
import airhockeyjava.util.Intersection;

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
	private Line2D trajectoryLine;
	private Path2D predictedPath;
	private Vector2 position;
	private Vector2 velocity;
	private final float mass; // Used in simulated friction calculation and energy transfer model
	private final float radius;

	boolean predictedPathFound = true;

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
		this.trajectoryLine = new Line2D.Float(new Point2D.Float(position.x, position.y),
				new Point2D.Float(position.x, position.y));
		this.predictedPath = new Path2D.Float();
		this.previousStateStack = new FixedStack<PositionAndDuration>(2);
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

	public Line2D getTrajectoryLine() {
		return this.trajectoryLine;
	}

	public void setTrajectoryLine(Line2D newTrajectoryLine) {
		this.trajectoryLine = newTrajectoryLine;
	}

	public void updateTrajectory() {
		Point2D oldPosition = trajectoryLine.getP2();
		trajectoryLine.setLine(oldPosition.getX(), oldPosition.getY(), this.position.x,
				this.position.y);
	}

	public Path2D getPredictedPath() {
		return predictedPath;
	}

	public void setPredictedPath(Path2D predictedPath) {
		this.predictedPath = predictedPath;
	}

	/**
	 * Updates position, velocity based on previous state information. Could use a Kalman filter for smoothing.
	 * Right now we are using a direct average: v_1 = (p_1 - p_0) / T, where p_1 is the most recent position and
	 * p_0 is the oldest position available to the state model, and T is the total duration.
	 */
	@Override
	public void updatePositionAndCalculateVelocity(Vector2 newPosition, float deltaTime) {
		if (previousStateStack.isEmpty()) {
			this.position = newPosition;
			updateTrajectory();
			previousStateStack.push(new PositionAndDuration(newPosition, System.nanoTime()));
			return;
		}
		PositionAndDuration oldestState = previousStateStack.elementAt(0);

		// Calculate and update the velocity, position if changed
		PositionAndDuration currentState = new PositionAndDuration(newPosition, System.nanoTime());
		previousStateStack.push(currentState);
		Vector2 newVelocity = new Vector2(newPosition);
		float deltaTimeSeconds = ((float) (currentState.nanoTime - oldestState.nanoTime)) / 1000000000f;
		newVelocity.sub(oldestState.position).scl(1f / deltaTimeSeconds);

		if (!newPosition.equals(oldestState.position)) {
			this.position = newPosition;
			updateTrajectory();
		}

		if (!this.velocity.equals(newVelocity)) {
			this.velocity = newVelocity;
		}
	}

	public void updatePredictedPath(Rectangle2D tableCollisionFrame) {
		// Vector in forward path. Requires significant scaling otherwise won't project far 
		// enough "into the future" to actually find the intersection point. Otherwise
		// we can limit this distance based on an actual amount of time elapsed into the future.
		Vector2 futurePosition = new Vector2(this.position).add(new Vector2(this.velocity)
				.scl(100000f));
		Line2D predictedLine = new Line2D.Float(this.position.x, this.position.y, futurePosition.x,
				futurePosition.y);
		//		predictedPath = new Path2D.Float(predictedLine);

		Point2D intersectionPoint = Intersection.getIntersectionPoint(predictedLine,
				tableCollisionFrame);
		if (intersectionPoint != null) {
			predictedLine.setLine(predictedLine.getX1(), predictedLine.getY1(),
					intersectionPoint.getX(), intersectionPoint.getY());
			predictedPath = new Path2D.Float(predictedLine);
			predictedPathFound = true;

			// Check for which edge collision occurred
			Line2D collisionEdgeLine = Intersection.getCollisionEdge(intersectionPoint,
					tableCollisionFrame);

			// Get angle between collision edge and incoming trajectory line
			float incidentAngleRadians = Intersection.getAngleBetweenLines(predictedLine,
					collisionEdgeLine);
			System.out.println(String.format("Incident angle: %f degrees",
					Math.toDegrees(incidentAngleRadians)));

			AffineTransform rotationTransform = new AffineTransform();

			// Reflect according to angle of incidence
			rotationTransform.rotate(Math.PI - 2 * incidentAngleRadians,
					(float) predictedLine.getX2(), (float) predictedLine.getY2());

			Line2D secondPredictedLine = new Line2D.Float((float) predictedLine.getX2(),
					(float) predictedLine.getY2(), (float) predictedLine.getX1(),
					(float) predictedLine.getY1());
			Path2D secondPredictedPath = new Path2D.Float(secondPredictedLine);
			secondPredictedPath.transform(rotationTransform);
			predictedPath.append(secondPredictedPath, true);

		} else {
			// Only output message on first iteration
			if (predictedPathFound) {
				System.out.println("No intersection point found!");
			}
			predictedPathFound = false;
		}
	}
}
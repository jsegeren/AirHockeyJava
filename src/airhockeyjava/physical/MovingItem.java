package airhockeyjava.physical;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Stack;

import airhockeyjava.game.Constants;
import airhockeyjava.util.Conversion;
import airhockeyjava.util.Geometry;
import airhockeyjava.util.LineVectorUtils;
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

	public class PathAndFlag {
		public Path2D predictedPath;
		public boolean isCriticalFlag;

		PathAndFlag(Path2D predictedPath, boolean isCriticalPath) {
			this.predictedPath = predictedPath;
			this.isCriticalFlag = isCriticalPath;
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
	private PathAndFlag pathAndFlag;
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
		this.pathAndFlag = new PathAndFlag(new Path2D.Float(), false);
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

	public PathAndFlag getPathAndFlag() {
		return pathAndFlag;
	}

	public void setPathAndFlag(Path2D predictedPath, boolean isCriticalFlag) {
		this.pathAndFlag.predictedPath = predictedPath;
		this.pathAndFlag.isCriticalFlag = isCriticalFlag;
	}

	public void setPathAndFlag(PathAndFlag pathAndFlag) {
		this.pathAndFlag = pathAndFlag;
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

	/**
	 * Public method to updated predicted path and visual projection of the moving item. 
	 * @param tableCollisionFrame
	 * @param maximum number of reflections desired in the projection
	 */
	public void updatePredictedPath(Rectangle2D tableCollisionFrame, int numberReflections) {
		// Vector in forward path. Requires significant scaling otherwise won't project far 
		// enough "into the future" to actually find the intersection point. Otherwise
		// we can limit this distance based on an actual amount of time elapsed into the future.
		Vector2 futurePosition = new Vector2(this.position).add(new Vector2(this.velocity)
				.scl(100000f));
		Line2D predictedLine = new Line2D.Float(this.position.x, this.position.y, futurePosition.x,
				futurePosition.y);

		Point2D intersectionPoint = getIntersectionPointAndCapLine(predictedLine,
				tableCollisionFrame);
		if (intersectionPoint == null) {
			return; // Short-circuit return
		}

		// Set up path; check if critical and update flag if necessary
		pathAndFlag.predictedPath = new Path2D.Float(predictedLine);
		pathAndFlag.isCriticalFlag = isPointIntersectingGoal(intersectionPoint,
				tableCollisionFrame, Constants.GAME_GOAL_WIDTH_METERS);

		// Build and add the remaining reflections to the projected path.
		// Only build until critical path reached (i.e. a path that leads to a goal!)
		for (int i = 0; i < numberReflections && !pathAndFlag.isCriticalFlag; i++) {
			predictedLine = getReflectedPredictedLine(predictedLine, tableCollisionFrame);
			Path2D reflectedPredictedPath = new Path2D.Float(predictedLine);
			pathAndFlag.predictedPath.append(reflectedPredictedPath, true);
			pathAndFlag.isCriticalFlag = isPointIntersectingGoal(predictedLine.getP2(),
					tableCollisionFrame, Constants.GAME_GOAL_WIDTH_METERS);
		}
	}

	/**
	 * Test whether point intersecting either goal
	 * @param point
	 * @param collisionFrame
	 * @param goalWidth
	 * @return True if intersection exists
	 */
	public final boolean isPointIntersectingGoal(Point2D point, Rectangle2D collisionFrame,
			float goalWidth) {
		return isPointIntersectingGoal((float) point.getX(), (float) point.getY(), collisionFrame,
				goalWidth);
	}
	
	public final boolean isPointIntersectingGoal(float x, float y, Rectangle2D collisionFrame,
			float goalWidth) {
		// True if reaching left goal or right goal, accounting for width of item
		return isPointIntersectingLeftGoal(x, y, collisionFrame, goalWidth)
				|| isPointIntersectingRightGoal(x, y, collisionFrame, goalWidth);
	}

	public final boolean isPointIntersectingLeftGoal(float x, float y, Rectangle2D collisionFrame,
			float goalWidth) {
		float goalStartY = (float) ((collisionFrame.getHeight() - goalWidth) / 2.0f);
		return ((x <= collisionFrame.getMinX() + Constants.GAME_GOAL_ALLOWANCE)
				&& (y >= goalStartY + this.radius - Constants.GAME_GOAL_ALLOWANCE) && (y <= goalStartY
				+ goalWidth - this.radius + Constants.GAME_GOAL_ALLOWANCE));
	}

	public final boolean isPointIntersectingRightGoal(float x, float y, Rectangle2D collisionFrame,
			float goalWidth) {
		float goalStartY = (float) ((collisionFrame.getHeight() - goalWidth) / 2.0f);
		return ((x >= collisionFrame.getMaxX() - Constants.GAME_GOAL_ALLOWANCE)
				&& (y >= goalStartY + this.radius - Constants.GAME_GOAL_ALLOWANCE) && (y <= goalStartY
				+ goalWidth - this.radius + Constants.GAME_GOAL_ALLOWANCE));
	}

	/**
	 * General method to get the next path line, which is a reflection of the current predicted line against
	 * boundary with which it collides.
	 * @param predictedLine
	 * @return reflected path line, or null if there is no intersection/reflection point
	 */
	private final Line2D getReflectedPredictedLine(Line2D predictedLine, Rectangle2D collisionFrame) {
		// Get angle between collision edge and incoming trajectory line
		float incidentAngleRadians = getCollisionIncidentAngle(predictedLine, collisionFrame);
		// Reflect according to angle of incidence
		float reflectionAngleRadians = (float) (Math.PI - 2 * incidentAngleRadians);

		// Set up the reflected path
		Line2D secondPredictedLine = new Line2D.Float((float) predictedLine.getX2(),
				(float) predictedLine.getY2(), (float) predictedLine.getX1(),
				(float) predictedLine.getY1());
		//		secondPredictedLine = LineVectorUtils.rotateLineAboutStartingPoint(secondPredictedLine, reflectionAngleRadians);
		Path2D secondPredictedPath = new Path2D.Float(secondPredictedLine);

		// Set up the rotation transformation based on the reflection, and then rotate the line
		// NOTE that this is just a graphical reflection. The reflected line is not rotated geometrically.
		AffineTransform rotationTransform = new AffineTransform();
		rotationTransform.rotate(reflectionAngleRadians, (float) predictedLine.getX2(),
				(float) predictedLine.getY2());
		secondPredictedPath.transform(rotationTransform);

		// Get the rotated line, then extend it up to the next boundary (i.e. table edge)
		secondPredictedLine = Geometry.getStartpoints(secondPredictedPath);
		LineVectorUtils.scaleLine(secondPredictedLine, 100000f);
		Point2D intersectionPoint = getIntersectionPointAndCapLine(secondPredictedLine,
				collisionFrame);
		//		if (intersectionPoint == null) {
		//			return null;
		//		}
		return secondPredictedLine;
	}

	/**
	 * Internal method to retrieve intersection point of predicted line and frame. Caps the line (i.e. sets
	 * endpoint) to the intersection point.
	 * @param predictedLine
	 * @param collisionFrame
	 * @return intersection point, null if not exists. In this case, predicted line is not modified.
	 */
	private final Point2D getIntersectionPointAndCapLine(Line2D predictedLine,
			Rectangle2D collisionFrame) {
		Point2D intersectionPoint = Intersection
				.getIntersectionPoint(predictedLine, collisionFrame);
		if (intersectionPoint == null) {
			// Only output message on first iteration
			if (predictedPathFound) {
				System.out.println("No intersection point found!");
			}
			predictedPathFound = false;
		} else {
			predictedPathFound = true;
			predictedLine.setLine(predictedLine.getX1(), predictedLine.getY1(),
					intersectionPoint.getX(), intersectionPoint.getY());
		}
		return intersectionPoint;
	}

	/**
	 * Gets angle between predicted path and the edge with which it collides.
	 * Expects the final endpoint of the predicted line to be the intersection point between the lines.
	 * @param predictedLine
	 * @param collisionFrame
	 * @return
	 */
	private final static float getCollisionIncidentAngle(Line2D predictedLine,
			Rectangle2D collisionFrame) {
		return Intersection.getAngleBetweenLines(
				predictedLine,
				Intersection.getCollisionEdge((float) predictedLine.getX2(),
						(float) predictedLine.getY2(), collisionFrame));
	}
}
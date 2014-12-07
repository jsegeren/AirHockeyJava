package airhockeyjava.physical;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.ArrayList;

import airhockeyjava.game.Constants;
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

	public class PathAndFlag {
		public Path2D predictedPath;
		public boolean isCriticalFlag;

		PathAndFlag(Path2D predictedPath, boolean isCriticalPath) {
			this.predictedPath = predictedPath;
			this.isCriticalFlag = isCriticalPath;
		}
	}

	private Table table;
	private List<Point2D> predictedPathPoints;
	private Line2D trajectoryLine;
	private PathAndFlag pathAndFlag;
	private Vector2 position;
	private Vector2 velocity;
	private Vector2 acceleration;
	private final float mass; // Used in simulated friction calculation and energy transfer model
	private final float radius;

	boolean predictedPathFound = true;

	/**
	 * Constructor which sets the starting state of the item.
	 * @param position
	 * @param velocity
	 */
	protected MovingItem(Vector2 position, Vector2 velocity, float radius, float mass, Table table) {
		this.position = position;
		this.velocity = velocity;
		this.acceleration = new Vector2();
		this.radius = radius;
		this.mass = mass;
		this.predictedPathPoints = new ArrayList<Point2D>();
		this.trajectoryLine = new Line2D.Float(new Point2D.Float(position.x, position.y),
				new Point2D.Float(position.x, position.y));
		this.pathAndFlag = new PathAndFlag(new Path2D.Float(), false);
		this.table = table;
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
	public Vector2 getAcceleration() {
		return acceleration;
	}

	@Override
	public void setAcceleration(Vector2 acceleration) {
		this.acceleration = acceleration;
	}

	@Override
	public float getRadius() {
		return this.radius;
	}

	@Override
	public float getMass() {
		return this.mass;
	}

	@Override
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

	/**
	 * Gets the latest  projected point available
	 */
	public Vector2 getLastExpectedPointVector() {
		if (!predictedPathPoints.isEmpty()) {
			return new Vector2(predictedPathPoints.get(predictedPathPoints.size() - 1));
		}
		else {
			return new Vector2(this.getPosition());
		}
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
	 * Update position and velocity based on acceleration.
	 * @param deltaTime
	 */
	public void updatePositionAndVelocity(float deltaTime) {
		this.velocity.add(new Vector2(this.acceleration).scl(deltaTime));
		updatePosition(deltaTime);
	}

	/**
	 * Update position based on velocity
	 * @param deltaTime
	 */
	public void updatePosition(float deltaTime) {
		this.position.add(new Vector2(this.velocity).scl(deltaTime));
	}

	/**
	 * Get expected position of item (i.e. no external forces)
	 * @param deltaTime
	 * @return Vector2 expected future position
	 */
	public Vector2 getExpectedPosition(float deltaTime) {
		return new Vector2(this.getPosition()).add(new Vector2(this.velocity).scl(deltaTime));
	}

	/*

	/**
	 * Public method to updated predicted path and visual projection of the moving item.
	 * @param tableCollisionFrame
	 * @param maximum number of reflections desired in the projection
	 * @param whether or not to recalculate entire path or just update beginning point
	 */
	public void updatePredictedPath(int numberReflections, boolean isFullRefresh) {
		Rectangle2D tableCollisionFrame = this.table.getCollisionFrame(this);

		// If not full refresh, just update the initial point (where object currently is)
		// Otherwise, have to recalculate entire projection
		Point2D currentPoint = new Point2D.Float(this.position.x, this.position.y);
		if (!isFullRefresh) {
			if (predictedPathPoints.isEmpty()) {
				predictedPathPoints.add(currentPoint);
			} else {
				predictedPathPoints.set(0, currentPoint);
			}
		}

		else {
			pathAndFlag.isCriticalFlag = false;
			System.out.println("Updating predicted path!");
			predictedPathPoints.clear();
			predictedPathPoints.add(currentPoint);

			// Vector in forward path. Requires significant scaling otherwise won't project far
			// enough "into the future" to actually find the intersection point. Otherwise
			// we can limit this distance based on an actual amount of time elapsed into the future.
			Vector2 futurePosition = new Vector2(this.position).add(new Vector2(this.velocity)
					.scl(100000f));
			Line2D predictedLine = new Line2D.Float(this.position.x, this.position.y,
					futurePosition.x, futurePosition.y);

			Point2D intersectionPoint = getIntersectionPointAndCapLine(predictedLine,
					tableCollisionFrame);
			if (intersectionPoint == null) {
				return; // Short-circuit return
			}
			predictedPathPoints.add(intersectionPoint);
			// Build and add the remaining reflections to the projected path.
			// Only build until critical path reached (i.e. a path that leads to a goal!)
			for (int i = 0; i < numberReflections; i++) {
				predictedLine = getReflectedPredictedLine(predictedLine, tableCollisionFrame);
				// Break if predicted line (or intersection point) not found
				if (predictedLine == null) {
					break;
				}

				Point2D endPoint = predictedLine.getP2();
				predictedPathPoints.add(endPoint);
				if (this.table.isIntersectingGoal(new Vector2(endPoint), this.getRadius())) {
					pathAndFlag.isCriticalFlag = true;
					break;
				}
			}
			System.out.println(predictedPathPoints.size());
		}

		// Build path with saved points
		for (int i = 0; i < predictedPathPoints.size() - 1; i++) {
			Point2D firstPoint = predictedPathPoints.get(i);
			Point2D secondPoint = predictedPathPoints.get(i + 1);
			Path2D path = new Path2D.Float(new Line2D.Float(firstPoint, secondPoint));
			if (i == 0) {
				pathAndFlag.predictedPath = path;
			} else {
				pathAndFlag.predictedPath.append(path, true);
			}
		}
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
		if (intersectionPoint == null) {
			return null;
		}
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
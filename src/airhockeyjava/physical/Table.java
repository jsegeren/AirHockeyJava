package airhockeyjava.physical;

import airhockeyjava.game.Constants;
import airhockeyjava.util.Vector2;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/** 
 * Class to represent the air hockey table.
 * @author Joshua Segeren
 *
 */
public class Table extends RoundRectangle2D.Float {
	public static enum GoalScoredEnum {
		GOAL_SCORED_FOR_USER, GOAL_SCORED_FOR_ROBOT, NO_GOAL_SCORED
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -828896685446216562L;
	private final float goalWidth; // Width of goal

	public final Vector2 gameTableUserGoalCenterPosition;
	public final Vector2 gameTableRobotGoalCenterPosition;

	// Map to cache collision frames
	private Map<java.lang.Float, Rectangle2D> collisionFrameMap = new HashMap<java.lang.Float, Rectangle2D>();

	/**
	 * Standard constructor. 
	 * @param length Longer dimension of the table.
	 * @param width Shorter dimension of the table.
	 * @param cornerRadius Radius of rounded corners
	 */
	public Table(float height, float width, float cornerRadius, float goalWidth) {
		super(0, 0, height, width, cornerRadius, cornerRadius);
		this.goalWidth = goalWidth;
		this.gameTableUserGoalCenterPosition = new Vector2(0, height / 2);
		this.gameTableRobotGoalCenterPosition = new Vector2(width, height / 2);
	}

	public Table() {
		this(Constants.GAME_TABLE_WIDTH_METERS, Constants.GAME_TABLE_HEIGHT_METERS,
				Constants.GAME_TABLE_CORNER_RADIUS_METERS, Constants.GAME_GOAL_WIDTH_METERS);
	}

	public float getGoalWidth() {
		return this.goalWidth;
	}

	/**
	 * Returns the frame, modified to account for radius of round colliding objects. This is the effective
	 * collision bounding box of the table; we simply reduce all the dimensions by the given radius.
	 * @param collisionRadius radius of colliding object
	 * @return Rectangle2D collision frame
	 */
	public Rectangle2D getCollisionFrame(float collisionRadius) {
		Rectangle2D collisionFrame = collisionFrameMap.get(collisionRadius);
		if (collisionFrame == null) {
			collisionFrame = this.getFrame(); // Lower precision frame
			//		Rectangle2D collisionFrame = this.getBounds2D(); // Higher precision frame
			collisionFrame.setFrame(collisionFrame.getMinX() + collisionRadius,
					collisionFrame.getMinY() + collisionRadius, collisionFrame.getMaxX() - 2
							* collisionRadius, collisionFrame.getMaxY() - 2 * collisionRadius);
			collisionFrameMap.put(collisionRadius, collisionFrame);
		}
		return collisionFrame;
	}

	public Rectangle2D getRobotWorkspaceCollisionFrame(float collisionRadius) {
		return new Rectangle2D.Float((float) this.getWidth() / 2f + collisionRadius , (float) 0f
				+ collisionRadius, (float) this.getWidth() / 2f - 2 * collisionRadius,
				(float) this.getHeight() - 2 * collisionRadius);
	}

	/**
	 * Overloaded. Uses radius of moving item.
	 * @param movingItem
	 * @return Rectangle2D
	 */
	public Rectangle2D getCollisionFrame(IMovingItem movingItem) {
		return this.getCollisionFrame(movingItem.getRadius());
	}

	/**
	 * Test whether item intersecting either goal
	 * @param point
	 * @param collisionFrame
	 * @param goalWidth
	 * @return GoalScoredEnum
	 */
	public final GoalScoredEnum checkForIntersectingGoal(IMovingItem item) {
		return isIntersectingLeftGoal(item) ? GoalScoredEnum.GOAL_SCORED_FOR_ROBOT
				: (isIntersectingRightGoal(item) ? GoalScoredEnum.GOAL_SCORED_FOR_USER
						: GoalScoredEnum.NO_GOAL_SCORED);
	}

	/**
	 * Overloaded implementation.
	 * @param position
	 * @param radius
	 * @return GoalScoredEnum
	 */
	public final GoalScoredEnum checkForIntersectionGoal(Vector2 position, float radius) {
		return isIntersectingLeftGoal(position, radius) ? GoalScoredEnum.GOAL_SCORED_FOR_ROBOT
				: (isIntersectingRightGoal(position, radius) ? GoalScoredEnum.GOAL_SCORED_FOR_USER
						: GoalScoredEnum.NO_GOAL_SCORED);
	}

	public final boolean isIntersectingGoal(IMovingItem item) {
		return isIntersectingLeftGoal(item) || isIntersectingRightGoal(item);
	}

	public final boolean isIntersectingGoal(Vector2 position, float radius) {
		return isIntersectingLeftGoal(position, radius)
				|| isIntersectingRightGoal(position, radius);
	}

	public final boolean isIntersectingLeftGoal(IMovingItem item) {
		return this.isIntersectingLeftGoal(item.getPosition(), item.getRadius());
	}

	public final boolean isIntersectingRightGoal(IMovingItem item) {
		return this.isIntersectingRightGoal(item.getPosition(), item.getRadius());
	}

	public final boolean isIntersectingLeftGoal(Vector2 position, float radius) {
		return (position.x <= getCollisionFrame(radius).getMinX() + Constants.GAME_GOAL_ALLOWANCE)
				&& isGoalInY(position, radius);
	}

	public final boolean isIntersectingRightGoal(Vector2 position, float radius) {
		return (position.x >= getCollisionFrame(radius).getMaxX() - Constants.GAME_GOAL_ALLOWANCE)
				&& isGoalInY(position, radius);
	}

	private final boolean isGoalInY(Vector2 position, float radius) {
		float goalStartY = (float) ((height - goalWidth) / 2.0);
		return (position.y >= goalStartY + radius - Constants.GAME_GOAL_ALLOWANCE)
				&& (position.y <= goalStartY + goalWidth - radius + Constants.GAME_GOAL_ALLOWANCE);
	}

	/**
	 * Get the top y-bound of the goal
	 * @return
	 */
	public final float getGoalStartY() {
		return (float) ((height - goalWidth) / 2.0);
	}

	/**
	 * Get the bottom y-bound of the goal
	 */
	public final float getGoalEndY() {
		return (float) (((height - goalWidth) / 2.0) + goalWidth);
	}

	/**
	 * Applies safety constraints to position vector
	 * Modifies the input vector
	 * @param position
	 * @return the same position object, to be used for chaining
	 */
	public Vector2 enforceSafeRobotPosition(Vector2 position, float safetyMargin) {
		Rectangle2D collisionFrame = this.getRobotWorkspaceCollisionFrame(safetyMargin);
		position.x = (float) Math.min(Math.max(position.x, collisionFrame.getMinX()),
				collisionFrame.getMaxX());
		position.y = (float) Math.min(Math.max(position.y, collisionFrame.getMinY()),
				collisionFrame.getMaxY());
		return position;
	}
}

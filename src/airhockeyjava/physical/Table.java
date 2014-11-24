package airhockeyjava.physical;

import airhockeyjava.game.Constants;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/** 
 * Class to represent the air hockey table.
 * @author Joshua Segeren
 *
 */
public class Table extends RoundRectangle2D.Float {
	/**
	 * 
	 */
	private static final long serialVersionUID = -828896685446216562L;
	private final float goalWidth; // Width of goal

	/**
	 * Standard constructor. 
	 * @param length Longer dimension of the table.
	 * @param width Shorter dimension of the table.
	 * @param cornerRadius Radius of rounded corners
	 */
	public Table(float height, float width, float cornerRadius, float goalWidth) {
		super(0, 0, height, width, cornerRadius, cornerRadius);
		this.goalWidth = goalWidth;
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
		Rectangle2D collisionFrame = this.getFrame();
		collisionFrame.setFrame(collisionFrame.getMinX() + collisionRadius,
				collisionFrame.getMinY() + collisionRadius, collisionFrame.getMaxX() - 2
						* collisionRadius, collisionFrame.getMaxY() - 2 * collisionRadius);
		return collisionFrame;
	}

}

package airhockeyjava.physical;

import airhockeyjava.game.Constants;

/** 
 * Class to represent the air hockey table.
 * @author Joshua Segeren
 *
 */
public class Table {	
	private final float width; // Defined to be the longer dimension
	private final float height; // Defined to be the shorter dimension
	private final float cornerRadius; // Radius of rounded corners; fillet is assumed to be tangential to straight edges
	private final float goalWidth; // Width of goal
	
	/**
	 * Default constructor.
	 */
	public Table() {
		this.height = Constants.GAME_TABLE_HEIGHT_METERS;
		this.width = Constants.GAME_TABLE_WIDTH_METERS;
		this.cornerRadius = Constants.GAME_TABLE_CORNER_RADIUS_METERS;
		this.goalWidth = Constants.GAME_GOAL_WIDTH_METERS;
	}
	
	/**
	 * Standard constructor. 
	 * @param length Longer dimension of the table.
	 * @param width Shorter dimension of the table.
	 * @param cornerRadius Radius of rounded corners
	 */
	public Table(float height, float width, float cornerRadius, float goalWidth) {
		this.height = height;
		this.width = width;
		this.cornerRadius = cornerRadius;
		this.goalWidth = goalWidth;
	}
	
	public float getHeight() {
		return this.height;
	}
	
	public float getWidth() {
		return this.width;
	}
	
	public float getCornerRadius() {
		return this.cornerRadius;
	}
	
	public float getGoalWidth() {
		return this.goalWidth;
	}
}

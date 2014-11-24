package airhockeyjava.game;

import java.awt.Color;

/**
 *
 * Top-level class for constants. This allows us to cleanly and statically reference constants
 * throughout the project code, and make necessary changes quickly.
 *
 * @author Joshua Segeren, Evan Skeete
 *
 */
public class Constants {
	/**
	 * Physical Parameter Constants
	 */
	public static final double INTERSECTION_EPSILON_METERS = 0.025; // 2.5 millimetres
	public static final float GAME_TIME_SECONDS = 5f;
	public static final float GAME_TABLE_HEIGHT_METERS = 1.3f;
	public static final float GAME_TABLE_WIDTH_METERS = 2.5f;
	public static final float GAME_TABLE_CORNER_RADIUS_METERS = 0.25f;
	public static final float GAME_PUCK_RADIUS_METERS = 0.05f; // 2.5 centimetres
	public static final float GAME_PUCK_MASS_GRAMS = 1f;
	public static final float GAME_MALLET_RADIUS_METERS = 0.05f; // 3.5 centimetres
	public static final float GAME_MALLET_MASS_GRAMS = Float.MAX_VALUE; // Effectively infinite
	public static final float GAME_GOAL_WIDTH_METERS = 0.3f;
	// Allowed distance from edge that a goal can be counted, since the puck never actually goes into the goal
	public static final float GAME_GOAL_ALLOWANCE = 0.015f;

	// Quick visual of coordinate convention being used here, where tuples are in (x,y) form:
	//
	// (0,0) <---- x ----> (WIDTH, 0)
	//	   _____________
	//     |            |
	//  y  |            |
	//     |____________|
	// (0, HEIGHT)         (WIDTH, HEIGHT)
	//
	public static final float GAME_PUCK_INITIAL_POSITION_X = GAME_TABLE_WIDTH_METERS / 2f;
	public static final float GAME_PUCK_INITIAL_POSITION_Y = GAME_TABLE_HEIGHT_METERS / 2f;
	public static final float GAME_PUCK_INITIAL_VELOCITY_X = 0f;
	public static final float GAME_PUCK_INITIAL_VELOCITY_Y = 0f;
	public static final float USER_MALLET_INITIAL_POSITION_X = 0.1f * GAME_TABLE_WIDTH_METERS;
	public static final float USER_MALLET_INITIAL_POSITION_Y = GAME_TABLE_HEIGHT_METERS / 2f;
	public static final float USER_MALLET_INITIAL_VELOCITY_X = 0f;
	public static final float USER_MALLET_INITIAL_VELOCITY_Y = 0f;
	public static final float ROBOT_MALLET_INTIIAL_POSITION_X = 0.9f * GAME_TABLE_WIDTH_METERS;
	public static final float ROBOT_MALLET_INITIAL_POSITION_Y = GAME_TABLE_HEIGHT_METERS / 2f;
	public static final float ROBOT_MALLET_INITIAL_VELOCITY_X = 0f;
	public static final float ROBOT_MALLET_INITIAL_VELOCITY_Y = 0f;

	/**
	 * GUI Constants
	 */
	public final static int GUI_FPS = 120;
	public final static int GUI_WINDOW_WIDTH = 1280;
	public final static int GUI_WINDOW_HEIGHT = 600;
	public final static int GUI_TABLE_OFFSET_X = 64;
	public final static int GUI_TABLE_OFFSET_Y = 64;
	public final static int GUI_INFO_BAR_WIDTH = 256;

	public final static Color GUI_PUCK_COLOR = Color.GRAY;
	public final static Color GUI_MALLET_COLOR = Color.GREEN;
	public final static Color GUI_PREDICTED_PATH_COLOR = Color.CYAN;
	public final static Color GUI_PREDICTED_GOAL_COLOR = Color.RED;
	public final static Color GUI_TABLE_COLOR = Color.BLUE;
	public final static Color GUI_GOAL_COLOR = Color.RED;
	public final static Color GUI_BG_COLOR = Color.BLACK;
	public final static Color GUI_TEXT_COLOR = Color.WHITE;

	public final static float GUI_SCALING_FACTOR = (Constants.GUI_WINDOW_WIDTH
			- Constants.GUI_INFO_BAR_WIDTH - (Constants.GUI_TABLE_OFFSET_X * 2))
			/ Constants.GAME_TABLE_WIDTH_METERS;

	/**
	 * Simulation Model Constants
	 */
	public static final float WALL_PUCK_COLLISION_LOSS_COEFFICIENT = 0.1f;
	public static final float MALLET_PUCK_COLLISION_LOSS_COEFFICIENT = 0.25f;
	public static final float PUCK_SURFACE_FRICTION_LOSS_COEFFICIENT = 0.005f;
	public static final int GAME_SIMULATION_TARGET_FPS = 60;
	public static final float MAX_PUCK_SPEED_METERS_PER_SECOND = 0.06f;
	public static final float MAX_USER_MALLET_SPEED_METERS_PER_SECOND = 1f;
	public static final int NUMBER_PREDICTED_PATH_REFLECTIONS = 3;
}

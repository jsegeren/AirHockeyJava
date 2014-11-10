package airhockeyjava.game;

/**
 *
 * Top-level class for constants. This allows us to cleanly and statically reference constants
 * throughout the project code, and make necessary changes quickly.
 *
 * @author Joshua Segeren
 *
 */
public class Constants {
	public static final float FAKE_VELOCITY_BURST = 0.01f; // TODO Replace. Just using burst of energy upon collisions
	public static final double INTERSECTION_EPSILON_METERS = 0.025; // 2.5 millimetres
	public static final int GAME_SIMULATION_TARGET_FPS = 60;
	public static final float GAME_TIME_SECONDS = 5f;
	public static final float GAME_TABLE_HEIGHT_METERS = 1.3f;
	public static final float GAME_TABLE_WIDTH_METERS = 2.5f;
	public static final float GAME_PUCK_RADIUS_METERS = 0.05f; // 2.5 centimetres
	public static final float GAME_PUCK_MASS_GRAMS = 50f;
	public static final float GAME_MALLET_RADIUS_METERS = 0.07f; // 3.5 centimetres
	public static final float GAME_MALLET_MASS_GRAMS = 0f; // Doesn't matter
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


	//GUI CONSTANTS
	public final static int GUI_FPS = 60;
	public final static int GUI_WINDOW_WIDTH = 1280;
	public final static int GUI_WINDOW_HEIGHT = 600;
	public final static int GUI_TABLE_OFFSET_X = 64;
	public final static int GUI_TABLE_OFFSET_Y = 64;
	public final static int GUI_INFO_BAR_WIDTH = 256;

	public final static float GUI_SCALING_FACTOR = (Constants.GUI_WINDOW_WIDTH - Constants.GUI_INFO_BAR_WIDTH - (Constants.GUI_TABLE_OFFSET_X * 2))
			/ Constants.GAME_TABLE_WIDTH_METERS;
}

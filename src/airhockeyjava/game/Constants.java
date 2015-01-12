package airhockeyjava.game;

import java.awt.Color;
import java.awt.event.KeyEvent;

import org.opencv.core.Scalar;

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
	 * General Constants
	 */
	public static final float NANOSECONDS_IN_SECOND = 1000000000f;
	public static final String REAL_GAME_TYPE_ARG = "real";
	public static final String REAL_HEADLESS_GAME_TYPE_ARG = "realHeadless"; // No GUI output
	public static final String SIMULATED_GAME_TYPE_ARG = "sim";
	public static final int GAME_TYPE_ARG_INDEX = 0;
	public static final String DEFAULT_GAME_TYPE_ARG = SIMULATED_GAME_TYPE_ARG;

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
	public static final float GAME_GOAL_WIDTH_METERS = 0.4f;
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
	public static final float ROBOT_MALLET_INTIIAL_POSITION_X = 0.85f * GAME_TABLE_WIDTH_METERS;
	public static final float ROBOT_MALLET_INITIAL_POSITION_Y = GAME_TABLE_HEIGHT_METERS / 2f;
	public static final float ROBOT_MALLET_INITIAL_VELOCITY_X = 0f;
	public static final float ROBOT_MALLET_INITIAL_VELOCITY_Y = 0f;

	/**
	 * GUI Constants
	 */
	public final static String GUI_JFRAME_LABEL = "AirHockeySimulation";
	public final static int GUI_FRAMES_PER_SECOND = 120;
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
	 * Detection/Vision System Constants
	 */
	public static final String DETECTION_JFRAME_LABEL = "AirHockeyDetection";
	public static final Scalar DETECTION_PUCK_HSV_MIN = new Scalar(20, 65, 160);
	public static final Scalar DETECTION_PUCK_HSV_MAX = new Scalar(45, 208, 196);

	// TODO make dynamic based on capture area and physical object dimensions
	public static final int DETECTION_FRAME_HEIGHT = 1280;
	public static final int DETECTION_FRAME_WIDTH = 600;
	public static final int DETECTION_PUCK_MAX_AREA = 100 * 100;
	public static final int DETECTION_PUCK_MIN_AREA = 20 * 20;
	public static final int DETECTION_MAX_OBJECTS = 10;

	/**
	 * Simulation Model Constants
	 */
	public static final float MALLET_PUCK_COLLISION_RESTITUTION_COEFFICIENT = 0.7f;
	public static final float WALL_PUCK_COLLISION_RESTITUTION_COEFFICIENT = 0.80f;
	public static final float PUCK_SURFACE_FRICTION_LOSS_COEFFICIENT = 0.001f;
	public static final float PUCK_AIR_FRICTION_COEFFICIENT = 0.3f;
	public static final int GAME_SIMULATION_TARGET_FRAMES_PER_SECOND = 120;
	public static final float MAX_PUCK_SPEED_METERS_PER_SECOND = 15f;
	public static final int NUMBER_PREDICTED_PATH_REFLECTIONS = 3;
	public static final float DIRECTIONAL_FORCE_SCALE_FACTOR = 10000f;
	public static final float DAMPENING_FORCE_SCALE_FACTOR = 100f;

	/**
	 * Mechanical Constants
	 */
	public static final float MECHANICAL_MAX_SPEED_METERS_PER_SECOND = 1.5f;
	public static final float MECHANICAL_MAX_ACCELERATION_METERS_PER_SECOND_SQUARED = 1f;
	public static final float MECHANICAL_MAX_POSITION_RESOLUTION_METERS = 0.005f;
	public static final float MECHANICAL_STEPS_PER_METER_X = 1f; // TODO update with physical calc
	public static final float MECHANICAL_STEPS_PER_METER_Y = 1f; // TODO update with physical calc

	/**
	 * Strategy Constants
	 */
	// Fixed step distance interval for checking all possible attacks
	public static final float STRATEGY_PROJECTED_STEP_DISTANCE_METERS = 0.005f;
	public static final int STRATEGY_MAX_LOOKAHEAD_BOUNCES = 3;
	public static final float STRATEGY_TRIANGLE_DISTANCE_FROM_GOAL_METERS = 0.15f;
	public static final float STRATEGY_TRIANGLE_LOOKAHEAD_TIME_SECONDS = 0.1f;
	public static final float STRATEGY_OFFENSE_MAX_PUCK_SPEED_TO_ENGAGE = 1.5f;
	public static final float MIN_TIME_BETWEEN_STRATEGY_TRANSITION_SECONDS = 0.01f;

	public static final String STRATEGY_NAIVE_DEFENSE_STRING = "Naive defense strategy";
	public static final String STRATEGY_TRIANGLE_DEFENSE_STRING = "Triangle defense strategy";
	public static final String STRATEGY_NAIVE_OFFENSE_STRING = "Naive offense strategy";

	/**
	 * Input Constants
	 */
	public static final int INPUT_TOGGLE_AI_KEY = KeyEvent.VK_A;
	public static final String INPUT_TOGGLE_AI_NAME = "toggleAI";
	public static final int INPUT_RESET_PUCK_KEY = KeyEvent.VK_R;
	public static final String INPUT_RESET_PUCK_NAME = "resetPuck";
	public static final int INPUT_TOGGLE_RESTRICT_USER_MALLET_KEY = KeyEvent.VK_M;
	public static final String INPUT_TOGGLE_RESTRICT_USER_MALLET_NAME = "toggleRestrictUserMalletMovement";
	public static final int INPUT_TOGGLE_GOAL_DETECTION_KEY = KeyEvent.VK_G;
	public static final String INPUT_TOGGLE_GOAL_DETECTION_NAME = "toggleGoalDetection";

	/**
	 * Output/Communication Constants
	 */
	public static final String SERIAL_PORT_NAMES[] = { "/dev/tty.usbserial-A9007UX1", // Mac OS X
			"/dev/ttyACM0", // Raspberry Pi
			"/dev/ttyUSB0", // Linux
			"COM3", // Windows
	};
	public static final String SERIAL_POSITION_DELIMITER = ","; // Delimiter for position data to Arduino
	public static final int SERIAL_TIME_OUT = 2000; // Time to block while waiting for port open in milliseconds
	public static final int SERIAL_DATA_RATE = 9600; // Default bits per second for COM port
	public static final String SERIAL_POSITION_PREFIX = "_"; // Prefix for updated position data received from Arduino
}

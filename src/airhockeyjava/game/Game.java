package airhockeyjava.game;

import airhockeyjava.simulation.IDetection;
import airhockeyjava.simulation.SimulatedDetection;
import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Mallet;

import java.util.Set;
import java.util.HashSet;

/** 
 * Top-level class for the game. Used for both simulated and actual games. Simulated games
 * will simply need to swap out the detection module with the mocked version.
 * @author Joshua Segeren
 *
 */

public class Game {
	/**
	 * Enumeration type to distinguish simulated and real-world games.
	 */
	public enum GameTypeEnum {
		CAMERA_COORDINATE_SYSTEM,
		ROBOT_COORDINATE_SYSTEM
	}
	
	private static final float GAME_TIME_SECONDS = 5;
	private float gameTimeRemainingSeconds;
	
	private int userScore = 0;
	private int robotScore = 0;
	
	private Set<IMovingItem> movingItems;
	
	private IDetection detectionLayer;
	private IGuiLayer guiLayer;
	
	// Initialize local vars
	gameInit();
	
	// Start the 
	guiLayer.start();
	
	while true
}
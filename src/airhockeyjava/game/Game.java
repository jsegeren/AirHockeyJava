package airhockeyjava.game;

import airhockeyjava.simulation.IDetection;
import airhockeyjava.simulation.SimulatedDetection;
import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Mallet;
import airhockeyjava.physical.Puck;
import airhockeyjava.graphics.IGuiLayer;
import airhockeyjava.input.IInputLayer;

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
		REAL_GAME_TYPE,
		SIMULATED_GAME_TYPE
	}
	
	// Local constants
	private static final GameTypeEnum gameType = GameTypeEnum.SIMULATED_GAME_TYPE;
	private static final float GAME_TIME_SECONDS = 5;
	
	// Local variables
	private float gameTimeRemainingSeconds;
	private int userScore = 0;
	private int robotScore = 0;
	private Set<IMovingItem> movingItems;
	
	// Application layer interfaces
	private IDetection detectionLayer;
	private IGuiLayer guiLayer;
	private IInputLayer inputLayer;
	
	/**
	 * Entry-point application method. Starts and runs a game of air hockey.
	 * Currently terminates when game is finished. Void return.
	 */
	public void main() {
		// Initialize local variables
		gameInit(gameType);
		
		// Start rendering
		guiLayer.start();
	
		// Main loop for game logic
		while(true) {
			
		}
		
	}
	
	private void gameInit(GameTypeEnum gameType) {
		// Instantiate moving game items
		
		
		
		// For simulated game, instantiate the simulated detection layer
		// and the input layer which is responsible for the user position.
		if (gameType == GameTypeEnum.SIMULATED_GAME_TYPE) {
			inputLayer = new InputLayer();
			detectionLayer = new SimulatedDetection(movingItems);
		}
	}
}
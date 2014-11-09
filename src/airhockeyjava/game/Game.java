package airhockeyjava.game;

import airhockeyjava.simulation.IDetection;
import airhockeyjava.simulation.SimulatedDetection;
import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Mallet;
import airhockeyjava.physical.Puck;
import airhockeyjava.physical.Table;
import airhockeyjava.graphics.IGuiLayer;
import airhockeyjava.input.IInputLayer;
import airhockeyjava.util.Vector2;

import java.util.Set;
import java.util.HashSet;

/**
 * Top-level class for the game. Used for both simulated and actual games. Simulated games will
 * simply need to swap out the detection module with the mocked version.
 * 
 * @author Joshua Segeren
 *
 */
public class Game {
	/**
	 * Enumeration type to distinguish simulated and real-world games.
	 */
	public enum GameTypeEnum {
		REAL_GAME_TYPE, SIMULATED_GAME_TYPE
	}

	// Local constants and physical parameters
	private static final GameTypeEnum gameType = GameTypeEnum.SIMULATED_GAME_TYPE;

	// Local variables
	private float gameTimeRemainingSeconds;
	private int userScore = 0;
	private int robotScore = 0;

	private Table gameTable;
	private Puck gamePuck;
	private Mallet userMallet;
	private Mallet robotMallet;

	// Application layer interfaces
	private IDetection detectionLayer;
	private IGuiLayer guiLayer;
	private IInputLayer inputLayer;

	// Game object itself!
	private static Game game;

	/**
	 * Entry-point application method. Starts and runs a game of air hockey. Currently terminates
	 * when game is finished. Void return.
	 */
	public static void main() {
		// Initialize the game object and game layers
		game = new Game(gameType);

		// Start rendering
		game.guiLayer.start();
		// Start collecting input
		game.inputLayer.start();

		// Main loop for game logic
		while (true) {

		}

	}

	/**
	 * Top-level constructor
	 */
	private Game(GameTypeEnum gameType) {
		// Instantiate physical game items
		gameTable = new Table(Constants.GAME_TABLE_HEIGHT_METERS, Constants.GAME_TABLE_WIDTH_METERS);
		gamePuck = new Puck();
		userMallet = new Mallet(true);
		robotMallet = new Mallet(false);
		
		// Initialize items set to pass to appropriate layers
		Set<IMovingItem> movingItems = new HashSet<IMovingItem>();
		movingItems.add(gamePuck);
		movingItems.add(userMallet);
		movingItems.add(robotMallet);

		// For simulated game, instantiate the simulated detection/prediction layer thread
		// and the input layer thread which is responsible for the user position.
		if (gameType == GameTypeEnum.SIMULATED_GAME_TYPE) {
			inputLayerThread = new Thread(new InputLayer();
			detectionLayer = new SimulatedDetection(movingItems);
		}
	}
}
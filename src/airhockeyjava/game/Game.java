package airhockeyjava.game;

import airhockeyjava.simulation.IDetection;
import airhockeyjava.simulation.SimulatedDetection;
import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Mallet;
import airhockeyjava.physical.Puck;
import airhockeyjava.physical.Table;
import airhockeyjava.graphics.GuiLayer;
import airhockeyjava.input.IInputLayer;
import airhockeyjava.input.InputLayer;

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
	private static final long OPTIMAL_TIME = 1000000000 / Constants.GAME_SIMULATION_TARGET_FPS;
	private static final GameTypeEnum gameType = GameTypeEnum.SIMULATED_GAME_TYPE;

	// Game-related global variables
	public float gameTimeRemainingSeconds;
	public int userScore = 0;
	public int robotScore = 0;

	public Set<IMovingItem> movingItems;
	public Table gameTable;
	
	public Puck gamePuck;
	public Mallet userMallet;
	public Mallet robotMallet;

	// Application layer interfaces
	private IDetection detectionLayer;
	private GuiLayer guiLayer;
	private IInputLayer inputLayer;

	// Threads
	private Thread guiLayerThread;
	private Thread inputLayerThread;

	// Game object itself!
	private static Game game;

	/**
	 * Entry-point application method. Starts and runs a game of air hockey. Currently terminates
	 * when game is finished. Void return.
	 */
	public static void main() {
		long lastLoopTime = System.nanoTime();
		long lastFpsTime = 0;
		long fps = 0;

		// Initialize the game object and game layers
		game = new Game(gameType);

		// Start the threads
		game.guiLayerThread.start();
		game.inputLayerThread.start();

		// Main loop for game logic. We want to update everything as fast as possible (i.e. no discretization)
		while (true) {
			// Determine how long it's been since last update; this will be used to calculate
			// how far entities should move this loop
			long currentTime = System.nanoTime();
			long updateLengthTime = currentTime - lastLoopTime;
			lastLoopTime = currentTime;
			double deltaTime = updateLengthTime / ((double) OPTIMAL_TIME); // nanoseconds
			
			// Update frame counter
			lastFpsTime += updateLengthTime;
			fps++;
			
			// Update FPS counter and remaining game time if a second has passed since last recorded
			if (lastFpsTime >= 1000000000) {
				System.out.println(String.format("FPS: %d", fps));
				lastFpsTime = 0;
				fps = 0;
				game.gameTimeRemainingSeconds -= 1; // Decrement the game time by 1 second
			}
			
			game.updateStates((float)deltaTime);
			
			// If target FPS = 60 (for example), want each frame to take 10 ms, 
			// we sleep until the next target frame, taking into account the time
			// taken to run the loop. Note this is given in ms, but other variables are in
			// nanoseconds.
			try { 
				Thread.sleep((lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000);
			}
			catch (InterruptedException e) {
				// TODO figure out what to do with this exception, e.g. rethrow as RuntimeException?
				e.printStackTrace();
			}

		}
	}
	
	private void updateStates(float deltaTime) {
		// If simulated, we need to use input data to update user mallet state
		// Also need to use mocked detection layer to update puck position via physics
		if (gameType == GameTypeEnum.SIMULATED_GAME_TYPE) {
			game.userMallet.getPosition().x = game.inputLayer.getMouseX();
			game.userMallet.getPosition().y = game.inputLayer.getMouseY();

			game.detectionLayer.detectAndUpdateItemStates(deltaTime);
		}
	}

	/**
	 * Top-level constructor
	 */
	public Game(GameTypeEnum gameType) {
		// Initialize member variables
		gameTimeRemainingSeconds = Constants.GAME_TIME_SECONDS;

		// Instantiate physical game items with default constants
		gameTable = new Table(Constants.GAME_TABLE_HEIGHT_METERS, Constants.GAME_TABLE_WIDTH_METERS);
		gamePuck = new Puck();
		userMallet = new Mallet(true);
		robotMallet = new Mallet(false);

		// Initialize items set which is accessible to other layers
		movingItems = new HashSet<IMovingItem>();
		movingItems.add(gamePuck);
		movingItems.add(userMallet);
		movingItems.add(robotMallet);

		guiLayer = new GuiLayer(game);
		guiLayerThread = new Thread(guiLayer);

		// For simulated game, instantiate the simulated detection/prediction layer thread
		// and the input layer thread which is responsible for the user position.
		if (gameType == GameTypeEnum.SIMULATED_GAME_TYPE) {
			// TODO should the GUI thread and input thread be the same instead of two separate threads?
			inputLayer = new InputLayer(guiLayer);
			inputLayerThread = new Thread(inputLayer);
			detectionLayer = new SimulatedDetection(game);
		}
	}
}
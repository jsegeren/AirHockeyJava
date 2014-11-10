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
import airhockeyjava.util.Conversion;
import airhockeyjava.util.Vector2;

import java.util.Set;
import java.util.HashSet;

import javax.swing.JFrame;

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

	//Configureable Game Settings
	private GameSettings settings = new GameSettings();

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
	public static void main(String[] args) {
		long lastLoopTime = System.nanoTime();
		long lastFpsTime = 0;
		long fps = 0;

		// Initialize the game object and game layers
		game = new Game(gameType);

		JFrame frame = new JFrame("AirHockey");
		frame.setSize(Constants.GUI_WINDOW_WIDTH, Constants.GUI_WINDOW_HEIGHT);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(game.guiLayer);
		frame.setVisible(true);

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

			game.updateStates((float) deltaTime);

			// If target FPS = 60 (for example), want each frame to take 10 ms,
			// we sleep until the next target frame, taking into account the time
			// taken to run the loop. Note this is given in ms, but other variables are in
			// nanoseconds.
			long sleepTime = (lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000;
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO figure out what to do with this exception, e.g. rethrow as RuntimeException?
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Send game info to the GUI for display
	 */
	private void setGameInfoDisplay(){
		this.guiLayer.setExternalInfoBarData(new String[]{
				"User Score: " + this.userScore,
				"Robot Score: " + this.robotScore,
		});
	}

	/**
	 * Check if a goal has been scored by the robot or user
	 * If a goal is scored, update score and reset puck
	 */
	private void checkAndUpdateScore(){
		float goalStartY = Conversion.meterToPixel(Constants.GAME_TABLE_HEIGHT_METERS - Constants.GAME_GOAL_WIDTH_METERS) / 2.0f;

		// Check if the robot scored a goal
		if(
				//TODO this can be cleaner
				gamePuck.getPosition().x - Constants.GAME_PUCK_RADIUS_METERS <= Constants.GAME_GOAL_ALLOWANCE &&
				Conversion.meterToPixel(gamePuck.getPosition().y) > goalStartY &&
				Conversion.meterToPixel(gamePuck.getPosition().y) < goalStartY + Conversion.meterToPixel(Constants.GAME_GOAL_WIDTH_METERS)
			){
				resetPuck();
				this.robotScore ++;
		}

		// Check if the user scored a goal
		if(
				//TODO this can be cleaner
				gamePuck.getPosition().x + Constants.GAME_PUCK_RADIUS_METERS >= Constants.GAME_TABLE_WIDTH_METERS - Constants.GAME_GOAL_ALLOWANCE &&
				Conversion.meterToPixel(gamePuck.getPosition().y) > goalStartY &&
				Conversion.meterToPixel(gamePuck.getPosition().y) < goalStartY + Conversion.meterToPixel(Constants.GAME_GOAL_WIDTH_METERS)
			){
				resetPuck();
				this.userScore ++;
		}
	}

	/**
	 * Reset the puck to initial position and 0 velocity
	 */
	private void resetPuck() {
		// Reset the puck position and veliocty
		gamePuck.getPosition().x = Constants.GAME_PUCK_INITIAL_POSITION_X;
		gamePuck.getPosition().y = Constants.GAME_PUCK_INITIAL_POSITION_Y;
		gamePuck.setVelocity(new Vector2(0,0));
	}

	/**
	 * Move the user mallet based on mouse input
	 */
	private void updateUserMalletPosition(){
		//Get mouse x and y relative to the table
		int mouseX = game.inputLayer.getMouseX() - Constants.GUI_TABLE_OFFSET_X;
		int mouseY = game.inputLayer.getMouseY() - Constants.GUI_TABLE_OFFSET_Y;

		//Update mallet x position if mouse x is within the width of the players half
		if (mouseX > 0 && mouseX < Conversion.meterToPixel(Constants.GAME_TABLE_WIDTH_METERS / 2) || !this.settings.restrictUserMalletMovement) {
			game.userMallet.getPosition().x = Conversion.pixelToMeter(mouseX);
		//Else, the mouse is off the left hand side
		} else if (mouseX <= 0) {
			game.userMallet.getPosition().x = 0;
		//Else, the mouse if off the right hand side
		} else {
			game.userMallet.getPosition().x = (Constants.GAME_TABLE_WIDTH_METERS / 2);
		}

		//Update mallet y position if mouse y is within the height of the players half
		if (mouseY > 0 && mouseY < Conversion.meterToPixel(Constants.GAME_TABLE_HEIGHT_METERS) || !this.settings.restrictUserMalletMovement) {
		game.userMallet.getPosition().y = Conversion.pixelToMeter(mouseY);
		//Else, the mouse is off the top
		} else if (mouseY <= 0) {
			game.userMallet.getPosition().y = 0;
		//Else, the mouse if off the bottom
		} else {
			game.userMallet.getPosition().y = (Constants.GAME_TABLE_HEIGHT_METERS);
		}
	}

	/**
	 * Update the game state
	 */
	private void updateStates(float deltaTime) {

		if (this.settings.goalDetectionOn) checkAndUpdateScore();

		// If simulated, we need to use input data to update user mallet state
		// Also need to use mocked detection layer to update puck position via physics
		if (gameType == GameTypeEnum.SIMULATED_GAME_TYPE) {
			updateUserMalletPosition();
		}

		setGameInfoDisplay();

		game.detectionLayer.detectAndUpdateItemStates(deltaTime);
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

		guiLayer = new GuiLayer(this);
		guiLayerThread = new Thread(guiLayer);

		// For simulated game, instantiate the simulated detection/prediction layer thread
		// and the input layer thread which is responsible for the user position.
		if (gameType == GameTypeEnum.SIMULATED_GAME_TYPE) {
			// TODO should the GUI thread and input thread be the same instead of two separate threads?
			inputLayer = new InputLayer(guiLayer);
			inputLayerThread = new Thread(inputLayer);
			detectionLayer = new SimulatedDetection(this);
		}
	}
}
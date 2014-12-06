package airhockeyjava.game;

import airhockeyjava.simulation.IDetection;
import airhockeyjava.simulation.SimulatedDetection;
import airhockeyjava.strategy.IStrategy;
import airhockeyjava.strategy.NaiveDefenseStrategy;
import airhockeyjava.strategy.UserInputStrategy;
import airhockeyjava.util.Conversion;
import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Mallet;
import airhockeyjava.physical.Puck;
import airhockeyjava.physical.Table;
import airhockeyjava.control.IController;
import airhockeyjava.control.UserController;
import airhockeyjava.graphics.GuiLayer;
import airhockeyjava.input.IInputLayer;
import airhockeyjava.input.InputLayer;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

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

	/**
	 * Set up maps for key bindings. Key -> action name, then action name -> action handler.
	 * This is considered the preferred mechanism for handling key inputs versus the KeyListener. 
	 * Works equally on all platforms: Windows, Mac, Linux.
	 */
	private static final Map<Integer, String> keyToActionNameMap = new HashMap<Integer, String>() {
		private static final long serialVersionUID = -8148003520786913073L;
		{
			put(Constants.INPUT_TOGGLE_AI_KEY, Constants.INPUT_TOGGLE_AI_NAME);
			put(Constants.INPUT_RESET_PUCK_KEY, Constants.INPUT_RESET_PUCK_NAME);
			put(Constants.INPUT_TOGGLE_RESTRICT_USER_MALLET_KEY,
					Constants.INPUT_TOGGLE_RESTRICT_USER_MALLET_NAME);
			put(Constants.INPUT_TOGGLE_GOAL_DETECTION_KEY,
					Constants.INPUT_TOGGLE_GOAL_DETECTION_NAME);
		}
	};

	private final Map<String, AbstractAction> actionNameToActionMap = new HashMap<String, AbstractAction>() {
		private static final long serialVersionUID = -4097174533995138895L;
		{
			put(Constants.INPUT_TOGGLE_AI_NAME, new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					settings.enableAI = !settings.enableAI;
				}
			});
			put(Constants.INPUT_RESET_PUCK_NAME, new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					resetPuck();
				}
			});
			put(Constants.INPUT_TOGGLE_RESTRICT_USER_MALLET_NAME, new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					settings.restrictUserMalletMovement = !settings.restrictUserMalletMovement;
				}
			});
			put(Constants.INPUT_TOGGLE_GOAL_DETECTION_NAME, new AbstractAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					settings.goalDetectionOn = !settings.goalDetectionOn;
				}
			});
		}
	};

	// Local constants and physical parameters
	private static final long OPTIMAL_TIME = Conversion
			.secondsToNanoseconds(1f / Constants.GAME_SIMULATION_TARGET_FRAMES_PER_SECOND);
	private static final GameTypeEnum gameType = GameTypeEnum.SIMULATED_GAME_TYPE;

	// Configurable Game Settings
	public GameSettings settings = new GameSettings();

	// Game-related global variables
	public float gameTimeRemainingSeconds;
	public Integer userScore = 0;
	public Integer robotScore = 0;

	public Set<IMovingItem> movingItems;
	public Table gameTable;

	public Puck gamePuck;
	public Mallet userMallet;
	public Mallet robotMallet;

	// Application layer interfaces
	private IDetection detectionLayer;
	private IStrategy userStrategy;
	private IStrategy robotStrategy;
	private IController userController;
	private IController robotController;
	private GuiLayer guiLayer;
	public IInputLayer inputLayer;

	// Threads
	private Thread guiLayerThread;

	// Game object itself!
	private static Game game;

	/**
	 * Top-level constructor
	 */
	public Game(GameTypeEnum gameType) {
		// Initialize member variables
		gameTimeRemainingSeconds = Constants.GAME_TIME_SECONDS;

		// Instantiate physical game items with default constants
		gameTable = new Table();
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

		inputLayer = new InputLayer(guiLayer);
		detectionLayer = new SimulatedDetection(this, inputLayer);

		userStrategy = new UserInputStrategy(this);
		userController = new UserController(this.userMallet);

		robotStrategy = new NaiveDefenseStrategy(this);
		robotController = new UserController(this.robotMallet);

		// For simulated game, instantiate the simulated detection/prediction layer thread
		// and the input layer thread which is responsible for the user position.
		if (gameType.equals(GameTypeEnum.SIMULATED_GAME_TYPE)) {
			setKeyBindings();
		}
	}

	/**
	 * Entry-point application method. Starts and runs a game of air hockey. Currently terminates
	 * when game is finished. Void return.
	 */
	public static void main(String[] args) throws RuntimeException {
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

		// Main loop for game logic. Uses variable timestepping.
		// Reference: http://www.java-gaming.org/index.php?topic=24220.0
		while (true) {
			// Determine how long it's been since last update; this will be used to calculate
			// how far entities should move this loop
			long currentTime = System.nanoTime();
			long updateLengthTime = currentTime - lastLoopTime;
			lastLoopTime = currentTime;
			//			double deltaTime = updateLengthTime / ((double) OPTIMAL_TIME); // nanoseconds

			// Update frame counter
			lastFpsTime += updateLengthTime;
			fps++;

			// Update FPS counter and remaining game time if a second has passed since last recorded
			if (lastFpsTime >= Conversion.secondsToNanoseconds(1f)) {
				System.out.println(String.format("FPS: %d", fps));
				lastFpsTime = 0;
				fps = 0;
				game.gameTimeRemainingSeconds -= 1; // Decrement the game time by 1 second
			}

			game.updateStates(Conversion.nanosecondsToSeconds(updateLengthTime));

			// If target FPS = 60 (for example), want each frame to take 10 ms,
			// we sleep until the next target frame, taking into account the time
			// taken to run the loop. Note this is given in ms, but other variables are in
			// nanoseconds.
			long sleepTime = (lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000;
			if (sleepTime > 0) { // Only sleep if necessary; avoid negative sleep errors
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO figure out what to do with this exception, e.g. rethrow as RuntimeException?
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Send game info to the GUI for display
	 */
	private void setGameInfoDisplay() {
		this.guiLayer.setExternalInfoBarData(new String[] { "User Score: " + this.userScore,
				"Robot Score: " + this.robotScore, });
	}

	/**
	 * Destroy and reinitialize puck object entirely
	 */
	public void resetPuck() {
		movingItems.remove(gamePuck);
		gamePuck = new Puck();
		movingItems.add(gamePuck);
	}

	/**
	 * Update the game state
	 */
	private void updateStates(float deltaTime) {

		setGameInfoDisplay();

		// If simulated, we need to use input data to update user mallet state
		// Also need to use mocked detection layer to update puck position via physics
		if (gameType.equals(GameTypeEnum.SIMULATED_GAME_TYPE)) {
			game.detectionLayer.detectAndUpdateItemStates(deltaTime);
		}
		// Otherwise we will use the vision system
		else if (gameType.equals(GameTypeEnum.REAL_GAME_TYPE)) {
		}

		// Check if should control robot mallet
		if (game.settings.enableAI) {
			robotController.controlMallet(robotStrategy.getTargetPosition(deltaTime), deltaTime);
		}

		userController.controlMallet(userStrategy.getTargetPosition(deltaTime), deltaTime);
	}

	private void setKeyBindings() {
		InputMap inputMap = guiLayer.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = guiLayer.getActionMap();

		for (Map.Entry<Integer, String> keyToActionNameEntry : keyToActionNameMap.entrySet()) {
			inputMap.put(KeyStroke.getKeyStroke(keyToActionNameEntry.getKey(), 0),
					keyToActionNameEntry.getValue());
		}
		for (Map.Entry<String, AbstractAction> actionNameToActionEntry : actionNameToActionMap
				.entrySet()) {
			actionMap.put(actionNameToActionEntry.getKey(), actionNameToActionEntry.getValue());
		}
	}
}
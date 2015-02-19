package airhockeyjava.game;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.bytedeco.javacv.FrameGrabber.Exception;
import org.opencv.core.Core;
import org.opencv.highgui.VideoCapture;

import airhockeyjava.control.IController;
import airhockeyjava.control.RealRobotController;
import airhockeyjava.control.SerialConnection;
import airhockeyjava.control.SimulatedRobotController;
import airhockeyjava.control.UserController;
import airhockeyjava.detection.IDetection;
import airhockeyjava.detection.ITrackingObject;
import airhockeyjava.detection.PS3EyeFrameGrabber;
import airhockeyjava.detection.SimulatedDetection;
import airhockeyjava.detection.TableBound;
import airhockeyjava.detection.Tracking;
import airhockeyjava.graphics.GuiLayer;
import airhockeyjava.input.IInputLayer;
import airhockeyjava.input.InputLayer;
import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Mallet;
import airhockeyjava.physical.Puck;
import airhockeyjava.physical.Table;
import airhockeyjava.strategy.IStrategy;
import airhockeyjava.strategy.StrategySelector;
import airhockeyjava.strategy.UserInputStrategy;
import airhockeyjava.util.Conversion;

/**
 * Top-level class for the game. Used for both simulated and actual games.
 * Simulated games will simply need to swap out the detection module with the
 * mocked version.
 *
 * @author Joshua Segeren
 *
 */
public class Game {
	/**
	 * Enumeration type to distinguish simulated and real-world games.
	 */
	public static enum GameTypeEnum {
		REAL_GAME_TYPE(Constants.REAL_GAME_TYPE_ARG), REAL_HEADLESS_GAME_TYPE(
				Constants.REAL_HEADLESS_GAME_TYPE_ARG), SIMULATED_GAME_TYPE(
				Constants.SIMULATED_GAME_TYPE_ARG);

		private final String typeString;

		private GameTypeEnum(String typeString) {
			this.typeString = typeString;
		}

		@Override
		public final String toString() {
			return this.typeString;
		}

		public final static GameTypeEnum findByValue(String value) {
			for (GameTypeEnum gameTypeEnum : values()) {
				if (gameTypeEnum.toString().equals(value)) {
					return gameTypeEnum;
				}
			}
			return null;
		}
	}

	/**
	 * Set up maps for key bindings. Key -> action name, then action name ->
	 * action handler. This is considered the preferred mechanism for handling
	 * key inputs versus the KeyListener. Works equally on all platforms:
	 * Windows, Mac, Linux.
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
	private static GameTypeEnum gameType;

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
	private Tracking realDetectionLayer; // TODO integrate this more cleanly
	private IDetection detectionLayer;
	private IStrategy userStrategy;
	private StrategySelector robotStrategy;
	private IController userController;
	private IController robotController;
	private GuiLayer guiLayer;
	public IInputLayer inputLayer;
	public SerialConnection serialConnection;

	// Threads
	private Thread guiLayerThread;
	private Thread detectionLayerThread;

	// Game object itself!
	private static Game game;

	// GUI frame
	private JFrame jFrame;

	/**
	 * Top-level constructor
	 */
	public Game(GameTypeEnum gameType) {
		// Output game type
		System.out.println(String.format("Starting game: %s", gameType));

		// Initialize member variables
		gameTimeRemainingSeconds = Constants.GAME_TIME_SECONDS;

		// Instantiate physical game items with default constants
		gameTable = new Table();
		gamePuck = new Puck(this.gameTable);
		userMallet = new Mallet(true, this.gameTable);
		robotMallet = new Mallet(false, this.gameTable);

		// Initialize items set which is accessible to other layers
		movingItems = new HashSet<IMovingItem>();
		movingItems.add(gamePuck);
		movingItems.add(userMallet);
		movingItems.add(robotMallet);

		userStrategy = new UserInputStrategy(this);
		userController = new UserController(this.userMallet);

		// robotStrategy = new NaiveDefenseStrategy(this);
		robotStrategy = new StrategySelector(this);

		// For simulated game, instantiate the simulated detection/prediction
		// layer thread
		// and the input layer thread which is responsible for the user
		// position.
		switch (gameType) {
		// Real game with GUI
		case REAL_GAME_TYPE:
			setupGUI();
			setupRealDetection(true);
			robotController = new RealRobotController(this.robotMallet);
			robotController.initialize();
			break;
		// Real game without GUI output
		case REAL_HEADLESS_GAME_TYPE:
			setupRealDetection(false);
			robotController = new RealRobotController(this.robotMallet);
			robotController.initialize();
			break;
		// Simulated game (with GUI output, and input controls)
		case SIMULATED_GAME_TYPE:
			setupGUI();
			inputLayer = new InputLayer(guiLayer);
			detectionLayer = new SimulatedDetection(this, inputLayer);
			setKeyBindings();
			robotController = new SimulatedRobotController(this.robotMallet);
			break;
		default:
			break;
		}
	}

	/**
	 * Internal method to initialize openCV and devices, real tracking layer
	 * 
	 * @param isGuiEnabled
	 *            whether GUI output should be shown
	 */
	private void setupRealDetection(boolean isGuiEnabled) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load openCV

		//List containing the puck for tracking
		List<ITrackingObject> puckList = new ArrayList<ITrackingObject>();
		puckList.add(this.gamePuck);

		//Objects to track is a list of lists of ITrackingObjects. Each list corresponds to a type of object, e.g. puck or tableBounds
		List<List<ITrackingObject>> objectsToTrack = new ArrayList<List<ITrackingObject>>();
		objectsToTrack.add(puckList);

		// Set up video feed; get device, then open capture stream
		// Open returns false if fails
		if (settings.usePS3Camera) {
			try {
				// CLEYE_QVGA (320 x 240) - 15, 30, 60, 75, 100, 125
				// CLEYE_VGA (640 x 480) - 15, 30, 40, 50, 60, 75
				PS3EyeFrameGrabber frameGrabber = new PS3EyeFrameGrabber(0, 320, 240, 125);
				//				PS3EyeFrameGrabber frameGrabber = new PS3EyeFrameGrabber(0,
				//				640, 480, 75);
				frameGrabber.start();
				realDetectionLayer = new Tracking(objectsToTrack, frameGrabber, isGuiEnabled);
				detectionLayerThread = new Thread(realDetectionLayer);
				detectionLayerThread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			VideoCapture videoCapture = new VideoCapture(0);
			if (videoCapture.open(0)) {
				realDetectionLayer = new Tracking(objectsToTrack, videoCapture, isGuiEnabled);
				detectionLayerThread = new Thread(realDetectionLayer);
				detectionLayerThread.start();
			} else {
				// Fail out and exit
				throw new RuntimeException("Video capture device could not be opened!");
			}
		}
	}

	/**
	 * Internal method to initialize GUI thread and create JFrame
	 */
	private void setupGUI() {
		guiLayer = new GuiLayer(this);
		guiLayerThread = new Thread(guiLayer);

		jFrame = new JFrame(Constants.GUI_JFRAME_LABEL);
		jFrame.setSize(Constants.GUI_WINDOW_WIDTH, Constants.GUI_WINDOW_HEIGHT);
		jFrame.setResizable(false);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.add(guiLayer);
		jFrame.setVisible(true);

		// Start the threads
		guiLayerThread.start();
	}

	/**
	 * Entry-point application method. Starts and runs a game of air hockey.
	 * Currently terminates when game is finished. Void return.
	 */
	public static void main(String[] args) throws RuntimeException {
		long lastLoopTime = System.nanoTime();
		long lastFpsTime = 0;
		long fps = 0;

		// Set the game type based on command-line argument, or use default

		gameType = (args != null && args.length >= (Constants.GAME_TYPE_ARG_INDEX + 1) && (GameTypeEnum
				.findByValue(args[Constants.GAME_TYPE_ARG_INDEX]) != null)) ? GameTypeEnum
				.findByValue(args[Constants.GAME_TYPE_ARG_INDEX]) : GameTypeEnum
				.findByValue(Constants.DEFAULT_GAME_TYPE_ARG);

		// Initialize the game object and game layers
		game = new Game(gameType);

		// Main loop for game logic. Uses variable timestepping.
		// Reference: http://www.java-gaming.org/index.php?topic=24220.0
		while (true) {
			// Determine how long it's been since last update; this will be used
			// to calculate
			// how far entities should move this loop
			long currentTime = System.nanoTime();
			long updateLengthTime = currentTime - lastLoopTime;
			lastLoopTime = currentTime;
			// double deltaTime = updateLengthTime / ((double) OPTIMAL_TIME); //
			// nanoseconds

			// Update frame counter
			lastFpsTime += updateLengthTime;
			fps++;

			// Update FPS counter and remaining game time if a second has passed
			// since last recorded
			if (lastFpsTime >= Conversion.secondsToNanoseconds(1f)) {
				System.out.println(String.format("FPS: %d", fps));
				lastFpsTime = 0;
				fps = 0;
				game.gameTimeRemainingSeconds -= 1; // Decrement the game time
													// by 1 second
			}

			game.updateStates(Conversion.nanosecondsToSeconds(updateLengthTime));

			// If target FPS = 60 (for example), want each frame to take 10 ms,
			// we sleep until the next target frame, taking into account the
			// time
			// taken to run the loop. Note this is given in ms, but other
			// variables are in
			// nanoseconds.
			long sleepTime = (lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000;
			if (sleepTime > 0) { // Only sleep if necessary; avoid negative
									// sleep errors
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO figure out what to do with this exception, e.g.
					// rethrow as RuntimeException?
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
		gamePuck = new Puck(this.gameTable);
		movingItems.add(gamePuck);
	}

	/**
	 * Update the game state
	 */
	private void updateStates(float deltaTime) {
		// If simulated, we need to use input data to update user mallet state
		// Also need to use mocked detection layer to update puck position via
		// physics
		if (gameType.equals(GameTypeEnum.SIMULATED_GAME_TYPE)) {
			setGameInfoDisplay();
			game.detectionLayer.detectAndUpdateItemStates(deltaTime);
			userController.controlMallet(userStrategy.getTargetPosition(deltaTime), deltaTime);
		}
		// Otherwise we will use the vision system
		else if (gameType.equals(GameTypeEnum.REAL_GAME_TYPE)) {
		}

		// Check if should control robot mallet
		if (game.settings.enableAI) {
			robotController.controlMallet(game.gameTable.enforceSafeRobotPosition(robotStrategy
					.getBestStrategy().getTargetPosition(deltaTime),
					Constants.MECHANICAL_ROBOT_EDGE_SAFETY_MARGIN_METERS), deltaTime);
		}
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
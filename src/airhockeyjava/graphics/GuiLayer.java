package airhockeyjava.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import airhockeyjava.game.Constants;
import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Table;

import javax.swing.JPanel;

import airhockeyjava.game.Game;
import airhockeyjava.util.*;

/**
 * Class for simulation UI layer.
 * 
 * @author Joshua Segeren
 * @author Evan Skeete
 */

public class GuiLayer extends JPanel implements IGuiLayer {

	private static final long serialVersionUID = -7402787534218194435L;

	private Game game; // Reference to the top-level game object itself to have access to global variables

	private BufferedImage imgBuffer;
	private Graphics bufferContext;
	private InfoBar infoBar;

	private static final long GUI_FRAME_TIME = 1000000000 / Constants.GUI_FPS;
	private long currentFps = 0;

	
	public GuiLayer(Game currentGame) {
		this.game = currentGame;
	}

	/* 
	 * Run the graphics thread
	 */
	public void run() {

		long lastLoopTime = System.nanoTime();
		long lastFpsTime = 0;
		long fpsCount = 0;

		initialize();
		setVisible(true);

		// Provide loop based on desired refresh rate to render output based on item positions
		while (true) {
			
			// Determine how long it's been since last update; this will be used to calculate
			// how far entities should move this loop
			long currentTime = System.nanoTime();
			long updateLengthTime = currentTime - lastLoopTime;
			lastLoopTime = currentTime;

			// Update frame counter
			lastFpsTime += updateLengthTime;
			fpsCount++;

			//Repaint the screen (calls paint method)
			repaint();

			// Update FPS counter and remaining game time if a second has passed since last recorded
			if (lastFpsTime >= 1000000000) {
				this.currentFps = fpsCount;
				lastFpsTime = 0;
				fpsCount = 0;
			}


			// Sleep until next frame
			long sleepTime = (lastLoopTime - System.nanoTime() + GUI_FRAME_TIME) / 1000000;
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Initialize the window
	 */
	void initialize() {

		// Create a buffered image
		this.imgBuffer = new BufferedImage(Constants.GUI_WINDOW_WIDTH, Constants.GUI_WINDOW_HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		
		// Save the context of the buffered image to draw to
		this.bufferContext = imgBuffer.getGraphics();

		//Init an info bar
		this.infoBar = new InfoBar(
				Constants.GUI_WINDOW_WIDTH - Constants.GUI_INFO_BAR_WIDTH,
				Constants.GUI_TABLE_OFFSET_Y, 
				Constants.GUI_INFO_BAR_WIDTH,
				Constants.GUI_WINDOW_HEIGHT - Constants.GUI_TABLE_OFFSET_Y,
				this.bufferContext
				);
	}

	/**
	 * Draw all objects to the screen
	 */
	@Override
	public void paint(Graphics graphicsContext) {

		clearScreen();
		drawTable(this.game.gameTable);

		Iterator<IMovingItem> iter = this.game.movingItems.iterator();
		while (iter.hasNext()) {
			drawMovingItem(iter.next(), Color.WHITE);
		}

		this.infoBar.clear();
		this.infoBar.writeLine("Welcome to Airhockey");
		this.infoBar.writeLine("");
		this.infoBar.writeLine("FPS:" + this.currentFps);

		graphicsContext.drawImage(this.imgBuffer, 0, 0, this);
	}

	/**
	 * Clear the entire screen
	 * 
	 * @param context
	 */
	private void clearScreen() {
		Graphics context = this.bufferContext;
		context.setColor(Color.BLACK);
		context.fillRect(0, 0, Constants.GUI_WINDOW_WIDTH, Constants.GUI_WINDOW_HEIGHT);
	}

	/**
	 * Draw a table
	 * 
	 * @param table
	 * @param context
	 */
	private void drawTable(Table table) {
		Graphics context = this.bufferContext;
		context.setColor(Color.BLUE);
		
		//Draw the table border
		context.drawRect(
				Constants.GUI_TABLE_OFFSET_X, 
				Constants.GUI_TABLE_OFFSET_Y,
				scale(Constants.GAME_TABLE_WIDTH_METERS), 
				scale(Constants.GAME_TABLE_HEIGHT_METERS)
				);

		//Draw the center line
		context.drawLine(
				Constants.GUI_TABLE_OFFSET_X + scale(Constants.GAME_TABLE_WIDTH_METERS/2), 
				Constants.GUI_TABLE_OFFSET_Y,
				Constants.GUI_TABLE_OFFSET_X + scale(Constants.GAME_TABLE_WIDTH_METERS/2), 
				Constants.GUI_TABLE_OFFSET_Y + scale(Constants.GAME_TABLE_HEIGHT_METERS)
				);

		context.setColor(Color.RED);
		//Draw player goal
		context.fillRect(
				Constants.GUI_TABLE_OFFSET_X, 
				Constants.GUI_TABLE_OFFSET_Y + scale(Constants.GAME_TABLE_HEIGHT_METERS - Constants.GAME_GOAL_WIDTH_METERS) / 2,
				5, 
				scale(Constants.GAME_GOAL_WIDTH_METERS)
				);

		//Draw robot goal
		context.fillRect(
				Constants.GUI_TABLE_OFFSET_X + scale(Constants.GAME_TABLE_WIDTH_METERS) - 5, 
				Constants.GUI_TABLE_OFFSET_Y + scale(Constants.GAME_TABLE_HEIGHT_METERS - Constants.GAME_GOAL_WIDTH_METERS) / 2,
				5, 
				scale(Constants.GAME_GOAL_WIDTH_METERS)
				);
	}

	/**
	 * Draw a MovingItem to the table
	 * 
	 * @param item
	 * @param context
	 * @param color
	 */
	private void drawMovingItem(IMovingItem item, Color color) {
		Graphics context = this.bufferContext;
		context.setColor(color);
		Vector2 position = item.getPosition();
		float radius = item.getRadius();
		context.drawOval(scale(position.x - radius) + Constants.GUI_TABLE_OFFSET_X, scale(position.y - radius)
				+ Constants.GUI_TABLE_OFFSET_Y, scale(radius * 2), scale(radius * 2));
	}

	/**
	 * Scale a value from meters to pixels
	 * 
	 * @param value
	 */
	private int scale(float value) {
		return Conversion.meterToPixel(value);
	}

	private class InfoBar {
		private int x;
		private int y;
		private int width = 0;
		private int height = 0;
		private int cursor = 0;

		private Graphics context;

		private static final int LINE_HEIGHT = 20;
		private static final int INDENT = 0;

		InfoBar(int x, int y, int width, int height, Graphics context) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.cursor = this.y;
			this.context = context;
		}


		/**
		 * Clear the info bar
		 */
		private void clear() {
			this.context.setColor(Color.BLACK);
			this.context.drawRect(this.x, this.y, this.width, this.height);
			this.cursor = this.y;
		}

		/**
		 * Draw a line of text to the info bar
		 * 
		 * @param string
		 */
		private void writeLine(String string) {
			this.context.setColor(Color.WHITE);
			this.context.drawString(string, this.x + INDENT, this.cursor);
			this.cursor += LINE_HEIGHT;

		}
	}

}
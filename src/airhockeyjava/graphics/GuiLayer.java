package airhockeyjava.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Table;

import javax.swing.JPanel;

import airhockeyjava.game.Game;
import airhockeyjava.game.Constants;
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

	private final static int FPS = 60;

	private final static int TABLE_OFFSET_X = 80;
	private final static int TABLE_OFFSET_Y = 60;

	private final static int INFO_BAR_WIDTH = 200;

	private float scale = 100;

	private InfoBar infoBar;

	private BufferedImage backBuffer;

	private long frameTime = 1000000000 / FPS;
	private long fps = 0;

	private boolean isRunning = true;

//	public static void main(String[] args) {
//		GuiLayer guiLayer = new GuiLayer(new Game(Game.GameTypeEnum.SIMULATED_GAME_TYPE));
//
//		JFrame frame = new JFrame("AirHockey");
//		frame.setTitle("AirHockey");
//		frame.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
//		frame.setResizable(false);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.add(guiLayer);
//		frame.setVisible(true);
//
//		guiLayer.run();
//		System.exit(0);
//	}

	public GuiLayer(Game currentGame) {
		this.game = currentGame;
	}

	/* (non-Javadoc)
	 * @see airhockeyjava.graphics.IGuiLayer#start()
	 */
	public void run() {
		// Provide loop based on desired refresh rate to render output based on item positions

		initialize();
		setVisible(true);
		int fpsCounter = 0;
		long lastFpsTime = 0;
		long lastLoopTime = System.nanoTime();

		while (isRunning) {
			long now = System.nanoTime();
			lastLoopTime = now;

			repaint();

			// update our FPS counter if a second has passed since
			// we last recorded
			fpsCounter++;
			lastFpsTime += (System.nanoTime() - lastLoopTime);
			if (lastFpsTime >= 10000000) {
				this.fps = fpsCounter;
				lastFpsTime = 0;
				fpsCounter = 0;
			}

			// Sleep until next frame
			try {
				Thread.sleep((lastLoopTime - System.nanoTime() + frameTime) / 1000000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		setVisible(false);

	}

	/**
	 * Initialize the window
	 */
	void initialize() {

		// Create a buffered image
		this.backBuffer = new BufferedImage(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);

		// Set the scale of the UI, based on the table width;
		setScale();

		//Init an info bar
		this.infoBar = new InfoBar(Constants.WINDOW_WIDTH - INFO_BAR_WIDTH, TABLE_OFFSET_Y, INFO_BAR_WIDTH,
				(int) scale(this.game.gameTable.getHeight()));
	}

	/**
	 * Draw all objects to the screen
	 */
	@Override
	public void paint(Graphics graphicsContext) {
		Graphics bufferContext = backBuffer.getGraphics();

		bufferContext.setColor(Color.BLACK);
		bufferContext.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

		drawTable(this.game.gameTable, bufferContext, Color.WHITE);

		Iterator<IMovingItem> iter = this.game.movingItems.iterator();
		while (iter.hasNext()) {
			drawMovingItem(iter.next(), bufferContext, Color.WHITE);
		}

		this.infoBar.setContext(bufferContext);
		this.infoBar.clear();
		this.infoBar.writeLine("Welcome to Airhockey");
		this.infoBar.writeLine("");
		this.infoBar.writeLine("FPS:" + this.fps);

		graphicsContext.drawImage(this.backBuffer, 0, 0, this);
	}

	/**
	 * Draw a table
	 * 
	 * @param item
	 * @param buffer
	 */
	private void drawTable(Table table, Graphics context, Color color) {
		context.setColor(Color.WHITE);
		context.drawRect(TABLE_OFFSET_X, TABLE_OFFSET_Y, (int) scale(table.getWidth()),
				(int) scale(table.getHeight()));

	}

	/**
	 * Draw a MovingItem to the table
	 * 
	 * @param item
	 * @param context
	 * @param color
	 */
	private void drawMovingItem(IMovingItem item, Graphics context, Color color) {
		context.setColor(color);
		Vector2 position = item.getPosition();
		float radius = item.getRadius();
		context.drawOval((int) scale(position.x) + TABLE_OFFSET_X, (int) scale(position.y)
				+ TABLE_OFFSET_Y, (int) scale(radius*2), (int) scale(radius*2));
	}

	/**
	 * Scale a value from meters to pixels
	 * 
	 * @param value
	 */
	private float scale(float value) {
		return value * this.scale;
	}

	/*
	 * Set the scaling factor of the display based on the table length
	 */
	private void setScale() {
		this.scale = (Constants.WINDOW_WIDTH - INFO_BAR_WIDTH - (TABLE_OFFSET_X * 2))
				/ game.gameTable.getWidth();
		System.out.println(this.scale);
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

		InfoBar(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.cursor = this.y;
		}

		public void setContext(Graphics context) {
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
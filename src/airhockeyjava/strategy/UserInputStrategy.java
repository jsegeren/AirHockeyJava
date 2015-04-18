package airhockeyjava.strategy;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Conversion;
import airhockeyjava.util.Vector2;

/**
 * Strategy layer. Naive implementation which considers all feasible combinations of moves, and chooses
 * move most likely to result in a goal.
 * USER INPUT strategy. Control mallet based on mouse position.
 * 
 * @author Joshua Segeren
 *
 */
public class UserInputStrategy implements IStrategy {

	final private static String strategyLabelString = Constants.STRATEGY_USER_INPUT_STRING;
	private Game game;
	private final Rectangle2D tablePuckCollisionFrame;

	public UserInputStrategy(Game game) {
		this.game = game;
		this.tablePuckCollisionFrame = game.gameTable.getCollisionFrame(game.gamePuck.getRadius());
	}

	/**
	 * Internal method to direct user mallet based on mouse pointer.
	 * Note that we must explicitly prevent intersections as the mouse pointer can move instantaneously.
	 * @param deltaTime
	 */
	@Override
	public Vector2 getTargetPosition(float deltaTime) {
		// Get the mouse coordinates relative to the table
		int mouseX = game.inputLayer.getMouseX() - Constants.GUI_TABLE_OFFSET_X;
		int mouseY = game.inputLayer.getMouseY() - Constants.GUI_TABLE_OFFSET_Y;

		// Update the mallet position, restricting it to the bounds of the table
		// Must convert from the UI layer x-coordinate (raw pixel value) to the physical dimension
		float targetPositionX = (float) Math.max(
				((!game.settings.restrictUserMalletMovement) ? Conversion.pixelToMeter(mouseX)
						- game.userMallet.getRadius() : Math.min(Conversion.pixelToMeter(mouseX),
						game.gameTable.getWidth() / 2f - game.userMallet.getRadius())),
				game.userMallet.getRadius());

		float targetPositionY = (float) Math.max(
				Math.min(Conversion.pixelToMeter(mouseY), game.gameTable.getHeight()
						- game.userMallet.getRadius()), game.userMallet.getRadius());

		return new Vector2(targetPositionX, targetPositionY);
	}

	public String getLabelString(){
		return this.strategyLabelString;
	}
	
	@Override
	public void initStrategy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Line2D[] getStrategyLines() {
		// TODO Auto-generated method stub
		return null;
	}
}
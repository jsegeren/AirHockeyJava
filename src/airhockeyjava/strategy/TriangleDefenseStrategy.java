package airhockeyjava.strategy;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Vector2;

/**
 * Strategy layer. Naive triangle defense, which sets up a triangle in front of the goal to
 * deflect all possible incoming shots (straight, under, over).
 * 
 * @author Joshua Segeren
 *
 */
public class TriangleDefenseStrategy implements IStrategy {

	final private static String strategyLabelString = Constants.STRATEGY_TRIANGLE_DEFENSE_STRING;
	final private Game game;
	private Line2D[] triangleLines = new Line2D.Float[2];

	public TriangleDefenseStrategy(Game game) {
		//Generate lines to represent the triangle
		this.game = game;
		
		Point2D homePosition = new Point2D.Float(Constants.ROBOT_MALLET_INTIIAL_POSITION_X, Constants.ROBOT_MALLET_INITIAL_POSITION_Y);
		Point2D traingleBase1 = new Point2D.Float(Constants.GAME_TABLE_WIDTH_METERS - Constants.MECHANICAL_ROBOT_EDGE_SAFETY_MARGIN_METERS, 
												  Constants.GAME_TABLE_HEIGHT_METERS / 2 - Constants.GAME_GOAL_WIDTH_METERS / 2);
				 //Constants.MECHANICAL_ROBOT_EDGE_SAFETY_MARGIN_METERS);

				Point2D traingleBase2 = new Point2D.Float(Constants.GAME_TABLE_WIDTH_METERS - Constants.MECHANICAL_ROBOT_EDGE_SAFETY_MARGIN_METERS, 
												 Constants.GAME_TABLE_HEIGHT_METERS / 2 + Constants.GAME_GOAL_WIDTH_METERS / 2);
												//Constants.GAME_TABLE_HEIGHT_METERS - Constants.MECHANICAL_ROBOT_EDGE_SAFETY_MARGIN_METERS);

		triangleLines[0] = new Line2D.Float(homePosition, traingleBase1);
		triangleLines[1] = new Line2D.Float(homePosition, traingleBase2);
	}

	@Override
	public Vector2 getTargetPosition(float deltaTime) {
		if(game.guiLayer != null){
			game.guiLayer.strategyLines = triangleLines;
		}

		Point2D collisionPoint = game.gamePuck.getExpectedInterectionPoint();
		
		Vector2 puckPosition = game.gamePuck.getPosition();
		if(puckPosition.x < game.gameTable.getWidth() / 3){
			return new Vector2(Constants.ROBOT_MALLET_INTIIAL_POSITION_X, Constants.ROBOT_MALLET_INITIAL_POSITION_Y);
		} else {
			if(collisionPoint != null){
				return new Vector2((float)collisionPoint.getX(), (float)collisionPoint.getY());
			}else{
				return new Vector2(Constants.ROBOT_MALLET_INTIIAL_POSITION_X, Constants.ROBOT_MALLET_INITIAL_POSITION_Y);			
			}

		}
		
	
	}

	@Override
	public String toString() {
		return strategyLabelString;
	}

	@Override
	public void initStrategy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Line2D[] getStrategyLines() {
		return triangleLines;
	}
}
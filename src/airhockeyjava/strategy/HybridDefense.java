package airhockeyjava.strategy;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Vector2;

public class HybridDefense implements IStrategy {

	final private static String strategyLabelString = Constants.STRATEGY_HYBRID_DEFENSE_STRING;
	final private Game game;
	private Line2D[] defenceLines = new Line2D.Float[3];

	public HybridDefense(Game game) {
		//Generate lines to represent the triangle
		this.game = game;
		
		Point2D homePosition = new Point2D.Float(Constants.ROBOT_MALLET_INITIAL_POSITION_X, Constants.ROBOT_MALLET_INITIAL_POSITION_Y);
		

		
		Point2D traingleBase1 = new Point2D.Float(Constants.GAME_TABLE_WIDTH_METERS, 
												  Constants.GAME_TABLE_HEIGHT_METERS / 2 - 2*Constants.GAME_GOAL_WIDTH_METERS / 3);
				 //Constants.MECHANICAL_ROBOT_EDGE_SAFETY_MARGIN_METERS);

				Point2D traingleBase2 = new Point2D.Float(Constants.GAME_TABLE_WIDTH_METERS, 
												 Constants.GAME_TABLE_HEIGHT_METERS / 2 + 2*Constants.GAME_GOAL_WIDTH_METERS / 3);
												//Constants.GAME_TABLE_HEIGHT_METERS - Constants.MECHANICAL_ROBOT_EDGE_SAFETY_MARGIN_METERS);

			Line2D frontLine = new Line2D.Float(new Point2D.Float(Constants.ROBOT_MALLET_INITIAL_POSITION_X, Constants.GAME_TABLE_HEIGHT_METERS / 2 - 2*Constants.GAME_GOAL_WIDTH_METERS / 3),
												new Point2D.Float(Constants.ROBOT_MALLET_INITIAL_POSITION_X, Constants.GAME_TABLE_HEIGHT_METERS / 2 + 2*Constants.GAME_GOAL_WIDTH_METERS / 3));
		
		defenceLines[0] = frontLine;		
		defenceLines[1] = new Line2D.Float(homePosition, traingleBase1);
		defenceLines[2] = new Line2D.Float(homePosition, traingleBase2);
		
	}

	@Override
	public Vector2 getTargetPosition(float deltaTime) {
		if(game.guiLayer != null){
			game.guiLayer.strategyLines = defenceLines;
		}

		Point2D collisionPoint = game.gamePuck.getExpectedInterectionPoint();
		
		Vector2 puckPosition = game.gamePuck.getPosition();
		if(puckPosition.x < game.gameTable.getWidth() / 3){
			return new Vector2(Constants.ROBOT_MALLET_INITIAL_POSITION_X, Constants.ROBOT_MALLET_INITIAL_POSITION_Y);
		} else {

			if(collisionPoint != null){
				return new Vector2((float)collisionPoint.getX(), (float)collisionPoint.getY());
			}else{
				return new Vector2(Constants.ROBOT_MALLET_INITIAL_POSITION_X, Constants.ROBOT_MALLET_INITIAL_POSITION_Y);			
			}

		}
		
	
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
		return defenceLines;
	}
}

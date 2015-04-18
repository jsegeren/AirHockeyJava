package airhockeyjava.strategy;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Vector2;

public class RetreatingDefenseStrategy implements IStrategy {

	final private static String strategyLabelString = Constants.STRATEGY_RETREATING_DEFENSE_STRING;
	final private Game game;
	private Line2D defenceLine;

	public RetreatingDefenseStrategy(Game game) {
		this.game = game;
	}

	@Override
	public Vector2 getTargetPosition(float deltaTime) {
		float robotMalletX = game.robotMallet.getPosition().x; 
		this.defenceLine = new Line2D.Float(
				robotMalletX, 0f,
				robotMalletX,
				Constants.GAME_TABLE_HEIGHT_METERS);
		
		Point2D collisionPoint = game.gamePuck.getExpectedInterectionWithLine(this.defenceLine);

		if (collisionPoint != null){
			return new Vector2(Constants.ROBOT_MALLET_INITIAL_POSITION_X, (float) collisionPoint.getY());
		}else{
			return new Vector2(Constants.ROBOT_MALLET_INITIAL_POSITION_X, Constants.ROBOT_MALLET_INITIAL_POSITION_Y);
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
		// TODO Auto-generated method stub
		return null;
	}

}

package airhockeyjava.strategy;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Vector2;

public class HomePositionStrategy implements IStrategy {

	final private static String strategyLabelString = Constants.STRATEGY_HOMING_POSITION_STRING;

	public HomePositionStrategy(Game game) {

	}

	@Override
	public Vector2 getTargetPosition(float deltaTime) {

		return new Vector2(Constants.ROBOT_MALLET_INITIAL_POSITION_X, Constants.ROBOT_MALLET_INITIAL_POSITION_Y);			

		
	
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
		return null;
	}
}


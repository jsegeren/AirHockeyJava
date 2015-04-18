package airhockeyjava.strategy;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Vector2;

public class AroundPuckStrategy implements IStrategy{
	final private static String strategyLabelString = Constants.STRATEGY_AROUND_PUCK_MANNEUVER_STRING;
	final private Game game;
	Vector2 destination = null;
	private Vector2 homePosition = new Vector2(Constants.ROBOT_MALLET_INITIAL_POSITION_X, Constants.ROBOT_MALLET_INITIAL_POSITION_Y);

	public AroundPuckStrategy(Game game) {
		this.game = game;
	}
	
	@Override
	public Vector2 getTargetPosition(float deltaTime) {
		float yPosition = (game.gamePuck.getPosition().y > Constants.GAME_TABLE_HEIGHT_METERS/2) ? 0 : Constants.GAME_TABLE_HEIGHT_METERS;
		if(destination == null){
			destination = new Vector2(Constants.GAME_TABLE_WIDTH_METERS, yPosition);
		}
		return destination;
	}

	public String getLabelString(){
		return this.strategyLabelString;
	}
	
	@Override
	public void initStrategy() {
		destination = null;
		
	}

	@Override
	public Line2D[] getStrategyLines() {
		// TODO Auto-generated method stub
		return null;
	}

}

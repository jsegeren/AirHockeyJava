package airhockeyjava.strategy;

import java.awt.geom.Line2D;

import airhockeyjava.util.Vector2;

/**
 * Interface for the strategy layer which is responsible for robot/AI decision making.
 * 
 * @author Joshua Segeren
 *
 */
public interface IStrategy {
	
	/**
	 * Returns the desired position for this iteration, regardless of state.
	 * @param deltaTime
	 */
	public Vector2 getTargetPosition(float deltaTime);
	
	/**
	 * Initialize / reset the internal (persisted) state of the strategy component
	 */
	public void initStrategy();
	
	public String getLabelString();
	
	public Line2D[] getStrategyLines();
}


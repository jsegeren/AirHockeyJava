package airhockeyjava.detection;


import org.opencv.core.Scalar;

import airhockeyjava.game.Constants;
import airhockeyjava.util.Vector2;

//TODO Implement this properly
// This is just a placeholder for testing
public class TableBound implements ITrackingObject{

	private static Scalar HSVmin = Constants.DETECTION_PUCK_HSV_MIN;
	private static Scalar HSVmax =  Constants.DETECTION_PUCK_HSV_MIN;
	private static final int minObjectArea = Constants.DETECTION_PUCK_MIN_AREA;
	private static final int maxObjectArea = Constants.DETECTION_PUCK_MAX_AREA;
	
	@Override
	public Vector2 getPosition() {
		return new Vector2(0,0);
	}

	@Override
	public Scalar getHSVMin() {
		return HSVmin;
	}

	@Override
	public void setHSVMin(Scalar hsvMin) {
		 HSVmin = hsvMin;
	}
	
	@Override
	public void setHSVMax(Scalar hsvMax) {
		 HSVmax = hsvMax;
	}
	
	@Override
	public Scalar getHSVMax() {
		return HSVmax;
	}

	@Override
	public int getMaxObjectArea() {
		return maxObjectArea;
	}

	@Override
	public int getMinObjectArea() {
		return minObjectArea;
	}

	@Override
	public void setPosition(Vector2 newPosition) {
		// TODO Auto-generated method stub
		
	}
}

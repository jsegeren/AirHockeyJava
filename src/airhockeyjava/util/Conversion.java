package airhockeyjava.util;

import airhockeyjava.game.Constants;

public class Conversion {

	public static int meterToPixel(float val) {
		return (int) (val * Constants.SCALE);
	}
	
	public static float pixelToMeter(int val) {
		return val / Constants.SCALE;
	}

}

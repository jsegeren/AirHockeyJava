package airhockeyjava.util;

import airhockeyjava.game.Constants;

public class Conversion {

	public static int meterToPixel(float val) {
		return (int) (val * Constants.GUI_SCALING_FACTOR);
	}
	
	public static float pixelToMeter(int val) {
		return val / Constants.GUI_SCALING_FACTOR;
	}

}

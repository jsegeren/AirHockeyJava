package airhockeyjava.util;

import airhockeyjava.game.Constants;

public class Conversion {
	public static final int meterToPixel(double val) {
		return meterToPixel((float) val);
	}

	public static final int meterToPixel(float val) {
		return (int) (val * Constants.GUI_SCALING_FACTOR);
	}
	
	public static final int meterToPixel(float val, float scalingFactor) {
		return (int) (val * scalingFactor);
	}

	public static final float pixelToMeter(int val) {
		return val / Constants.GUI_SCALING_FACTOR;
	}
	
	public static final float pixelToMeter(int val, float scalingFactor) {
		return val / scalingFactor;
	}

	public static final float nanosecondsToSeconds(long nanoseconds) {
		return nanoseconds / Constants.NANOSECONDS_IN_SECOND;
	}

	public static final int secondsToNanoseconds(float seconds) {
		return (int) (seconds * Constants.NANOSECONDS_IN_SECOND);
	}

	public static final int meterToStepsX(float distance) {
		return (int) (distance * Constants.MECHANICAL_STEPS_PER_METER_X);
	}

	public static final int meterToStepsY(float distance) {
		return (int) (distance * Constants.MECHANICAL_STEPS_PER_METER_Y);
	}

	public static final float stepsToMeterX(int steps) {
		return steps / Constants.MECHANICAL_STEPS_PER_METER_X;
	}

	public static final float stepsToMeterY(int steps) {
		return steps / Constants.MECHANICAL_STEPS_PER_METER_Y;
	}
}

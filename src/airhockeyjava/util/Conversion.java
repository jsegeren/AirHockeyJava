package airhockeyjava.util;

import airhockeyjava.game.Constants;

public class Conversion {
	public static int meterToPixel(double val) {
		return meterToPixel((float) val);
	}

	public static int meterToPixel(float val) {
		return (int) (val * Constants.GUI_SCALING_FACTOR);
	}

	public static float pixelToMeter(int val) {
		return val / Constants.GUI_SCALING_FACTOR;
	}

	public static final float nanosecondsToSeconds(long nanoseconds) {
		return nanoseconds / Constants.NANOSECONDS_IN_SECOND;
	}

	public static final int secondsToNanoseconds(float seconds) {
		return (int) (seconds * Constants.NANOSECONDS_IN_SECOND);
	}

	public static int meterToStepsX(float distance) {
		return (int) (distance * Constants.MECHANICAL_STEPS_PER_METER_X);
	}

	public static int meterToStepsY(float distance) {
		return (int) (distance * Constants.MECHANICAL_STEPS_PER_METER_Y);
	}

	public static float stepsToMeterX(int steps) {
		return steps / Constants.MECHANICAL_STEPS_PER_METER_X;
	}

	public static float stepsToMeterY(int steps) {
		return steps / Constants.MECHANICAL_STEPS_PER_METER_Y;
	}
}

package airhockeyjava.physical;

import java.util.Arrays;

import airhockeyjava.game.Constants;

/**
 * Class representing dynamic friction zones of the table. Right now the friction model works
 * by overlaying a grid on the table, with each grid cell mapping to a specific friction coefficient value.
 * The values may either be explicitly set or implicitly determined from physical item collisions and subsequent
 * responses.
 * @author Joshua Segeren
 *
 */
public class FrictionGrid {
	// Use two-dimensional float array to store coefficients
	private float[][] frictionGrid;
	
	/**
	 * Normal constructor. Initializes with default surface friction coefficient.
	 * @param rows
	 * @param columns
	 */
	public FrictionGrid(int rows, int columns) {
		this(rows, columns, Constants.PUCK_SURFACE_FRICTION_LOSS_COEFFICIENT);
	}

	public FrictionGrid(int rows, int columns, float initialFrictionCoefficient) {
		frictionGrid = new float[rows][columns];
		Arrays.fill(frictionGrid,  initialFrictionCoefficient);
	}
	
	public FrictionGrid(float[][] frictionGrid) {
		this.frictionGrid = frictionGrid;
	}
}
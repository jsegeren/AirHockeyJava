package airhockeyjava.detection;

import airhockeyjava.util.Vector2;
import org.opencv.core.Scalar;

/**
 * Interface for trackable object, for use by vision/detection subsystem.
 * @author Nima
 *
 */
public interface ITrackingObject {
	public Vector2 getPosition();

	public void setPosition(Vector2 newPosition);

	public Scalar getHSVMin();

	public Scalar getHSVMax();
}

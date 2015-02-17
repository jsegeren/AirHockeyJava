package airhockeyjava.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.opencv.core.Point;

import airhockeyjava.detection.ITrackingObject;
import airhockeyjava.util.Conversion;

/**
 * Graphics panel that controls the video feed. Allows for new frames to be
 * added and for tracked object positions to be superimposed onto the image.
 * 
 * @author Nima Akhbari
 */

public class VideoDisplayPanel extends JPanel {
	private static final long serialVersionUID = 6993843089207957005L;
	private static final int DEFAULT_RADIUS_SIZE = 10;
	private long frameRate;
	private BufferedImage imageBuffer;

	private List<ITrackingObject> trackingObjects;
	private List<Point> tableBounds;
	
	public VideoDisplayPanel(List<ITrackingObject> trackingObjects) {
		super();
		this.trackingObjects = trackingObjects;
		frameRate = 0;
	}
	
	public VideoDisplayPanel(List<ITrackingObject> trackingObjects, List<Point> tableBounds) {
		this(trackingObjects);
		this.tableBounds = tableBounds;
	}

	/**
	 * Gets called by repaint(), and adds the components to the panel (this)
	 */
	public void paint(Graphics graphicsContext) {
		if (imageBuffer != null) {
			graphicsContext.clearRect(0, 0, this.getWidth(), this.getHeight());
			// Draws the image
			graphicsContext.drawImage(this.imageBuffer, 0, 0, this);
		}

		//Draw the tracking objects
		for (ITrackingObject trackingObject : this.trackingObjects) {
			// For each circle in the queue, draw the circle
			graphicsContext.setColor(GuiLayer.colorMap.get(trackingObject.getClass()));
			graphicsContext.fillOval(Conversion.meterToPixel(trackingObject.getPosition().x),
					Conversion.meterToPixel(trackingObject.getPosition().y), DEFAULT_RADIUS_SIZE,
					DEFAULT_RADIUS_SIZE);
		}

		
		graphicsContext.setColor(Color.CYAN);
		
		//Draw the table bounds
		if(this.tableBounds != null){
			for (Point point : this.tableBounds) {
				graphicsContext.fillOval((int)point.x - DEFAULT_RADIUS_SIZE/2, (int)point.y - DEFAULT_RADIUS_SIZE/2, DEFAULT_RADIUS_SIZE,  DEFAULT_RADIUS_SIZE);
			}			
		}

		
		graphicsContext.drawString("FPS: " + frameRate, 20, 20);
	}

	/**
	 * Sets the panel's buffered image
	 * 
	 * @param image
	 */
	public void setImageBuffer(BufferedImage image) {
		this.imageBuffer = image;

		// After setting a new image, repaint the panel
		repaint();
	}

	public void setFrameRate(long rate) {
		this.frameRate = rate;
	}

}
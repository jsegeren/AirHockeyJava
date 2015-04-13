package airhockeyjava.detection;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import airhockeyjava.game.Constants;
import airhockeyjava.graphics.ImageFilteringPanel;
import airhockeyjava.graphics.VideoDisplayPanel;
import airhockeyjava.util.Conversion;
import airhockeyjava.util.FileWriter;
import airhockeyjava.util.NormalDistribution;
import airhockeyjava.util.ScalarRange;
import airhockeyjava.util.Vector2;

/**
 * Accesses the computer's webcam and display the video feed in both RGB and
 * HSV. Allows the HSV images to be filtered via sliders. After a reasonably
 * sized object has been detected the centroid of it will be detected and marked
 * on both videos.
 * 
 * @author Nima Akhbari
 */
public class Tracking implements Runnable {

	// Map of tracking objects to number of occurrences/instances expected
	private List<List<ITrackingObject>> objectSetsToTrack;
	private List<Point> perspectiveBounds = new ArrayList<Point>();

	private VideoDisplayPanel normalPanel;
	private JFrame jFrame;

	private VideoDisplayPanel hsvFilteredPanel;
	private JFrame hsvFilteredFrame;

	private ImageFilteringPanel slider;
	private JFrame sliderFrame;

	private final PS3EyeFrameGrabber ps3VideoCapture;
	private final VideoCapture videoCapture;

	private MouseListener mouseListener;
	private Mat hsvImage;

	private boolean usePS3Cam;

	private boolean isGuiEnabled;
	
	private int imageCount = 0;

	// Use this to dynamically resize the JFrame based on the capture size
	// Only need to resize the JFrame after the first frame capture!
	private boolean isFirstCaptureImage = true;

	private Mat warpMat;

	public Tracking(List<List<ITrackingObject>> objectsToTrack, PS3EyeFrameGrabber ps3VideoCapture,
			boolean isGuiEnabled) {
		this(objectsToTrack, ps3VideoCapture, null, true, isGuiEnabled);
	}

	public Tracking(List<List<ITrackingObject>> objectsToTrack, VideoCapture videoCapture,
			boolean isGuiEnabled) {
		this(objectsToTrack, null, videoCapture, false, isGuiEnabled);
	}

	public Tracking(List<List<ITrackingObject>> objectsToTrack, PS3EyeFrameGrabber ps3VideoCapture,
			VideoCapture videoCapture, boolean usePS3Camera, boolean isGuiEnabled) {

		initializeMouseListener();

		this.usePS3Cam = usePS3Camera;
		this.isGuiEnabled = isGuiEnabled;

		if (usePS3Cam) {

			this.videoCapture = null;
			this.ps3VideoCapture = ps3VideoCapture;
		} else {

			this.videoCapture = videoCapture;
			this.ps3VideoCapture = null;
		}

		this.objectSetsToTrack = objectsToTrack;

		List<ITrackingObject> trackingObjects = new ArrayList<ITrackingObject>();
		for (List<ITrackingObject> objectList : objectsToTrack) {
			trackingObjects.addAll(objectList);
		}

		List<ITrackingObject> filteringObjects = new ArrayList<ITrackingObject>();
		for (List<ITrackingObject> objectList : objectsToTrack) {
			filteringObjects.add(objectList.get(0));
		}

		if (isGuiEnabled) {
			this.normalPanel = new VideoDisplayPanel(trackingObjects, perspectiveBounds);
			this.normalPanel.addMouseListener(mouseListener);

			this.jFrame = new JFrame(Constants.DETECTION_JFRAME_LABEL);
			this.jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.jFrame.setSize(Constants.DETECTION_FRAME_WIDTH, Constants.DETECTION_FRAME_HEIGHT);
			this.jFrame.add(normalPanel);
			this.jFrame.setVisible(true);

			// HSV filtered content will be displayed here
			this.hsvFilteredPanel = new VideoDisplayPanel(trackingObjects);
			this.hsvFilteredFrame = new JFrame("HSV Filtered");
			this.hsvFilteredFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.hsvFilteredFrame.setSize(Constants.DETECTION_FRAME_WIDTH,
					Constants.DETECTION_FRAME_HEIGHT);
			this.hsvFilteredFrame.add(hsvFilteredPanel);
			this.hsvFilteredFrame.setVisible(true);
			this.hsvFilteredFrame.setLocation(640, 0);			

			// Create frame to manipulate image level thresholding
			this.slider = new ImageFilteringPanel(filteringObjects, this);
			this.sliderFrame = new JFrame("HSV Thresholds");
			this.sliderFrame.setSize(500, 800);
			this.sliderFrame.setLocation(1280, 0);			
			this.sliderFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.sliderFrame.add(slider);
			this.sliderFrame.setVisible(true);

		}
	}

	/**
	 * Entry point for the tracking thread
	 */
	public void run() {
		long lastLoopTime = System.nanoTime();
		long lastFpsTime = 0;
		long fps = 0;

		// Matrix that represents the individual images
		Mat originalImage = new Mat();
		hsvImage = originalImage;
		// Iterate through frames as fast as possible! (for now)
		while (true) {

			// Determine how long it's been since last update
			long currentTime = System.nanoTime();
			long updateLengthTime = currentTime - lastLoopTime;
			lastLoopTime = currentTime;

			// Update frame counter
			lastFpsTime += updateLengthTime;
			fps++;

			// Update FPS counter and remaining game time if a second has passed
			// since last recorded
			if (lastFpsTime >= Conversion.secondsToNanoseconds(1f)) {
				//				System.out.println(String.format("FPS: %d", fps));
				normalPanel.setFrameRate(fps);
				lastFpsTime = 0;
				fps = 0;
			}

			// Read in the new video frame
			// NOTE: This is a blocking call; will sleep thread until next frame
			// available
			if (usePS3Cam) {
				originalImage = ps3VideoCapture.grabMat();
			} else {

				videoCapture.read(originalImage);
			}

			if (!originalImage.empty()) {
				//TODO FIX RESIZING OF FRAME
				// Dynamically resize the JFrames based on the capture size (device-dependent)
				if (!(imageCount == 2)) {
					slider.loadTransform();
//					Dimension actualSize = new Dimension(originalImage.width(),
//							originalImage.height());
//					if (jFrame.getWidth() != actualSize.getWidth()
//							|| jFrame.getHeight() != actualSize.getHeight()) {
//						// Update the global width & height
//						Constants.setDetectionFrameWidth(originalImage.width());
//						Constants.setDetectionFrameHeight(originalImage.height());
//						// Update the local frame sizes to display
//						jFrame.setSize(actualSize);
//						hsvFilteredFrame.setSize(actualSize);
//					}
					imageCount++;
				}

				// Perspective transformation -> 2D to 2D input warp so capture doesn't have to be flat or level
				if (warpMat != null) {
					//					System.out.println(this.normalPanel.getWidth() + " "
					//							+ this.normalPanel.getHeight());
					Imgproc.warpPerspective(originalImage, originalImage, warpMat, originalImage.size());
				}

				if (usePS3Cam) {
					// Convert matrix from RGB to HSV
					Imgproc.cvtColor(originalImage.clone(), hsvImage, Imgproc.COLOR_RGB2HSV);
					Imgproc.cvtColor(originalImage, originalImage, Imgproc.COLOR_RGB2BGR);
				} else {
					// Convert matrix from BGR to HSV
					Imgproc.cvtColor(originalImage.clone(), hsvImage, Imgproc.COLOR_BGR2HSV);
				}

				Mat hsvImageThresholded = new Mat();

				// Iterate over each type of object; there is a set of objects
				// of each type
				for (List<ITrackingObject> trackingObjectList : objectSetsToTrack) {
			
					if (trackingObjectList == null || trackingObjectList.isEmpty()) {
						continue;
					}
					// Threshold the hsv image to filter for Pucks
					Core.inRange(hsvImage, trackingObjectList.get(0).getHSVMin(),
							trackingObjectList.get(0).getHSVMax(), hsvImageThresholded);

					// reduce the noise in the image
					reduceNoise(hsvImageThresholded);

					// Find instance(s) of object to track
					findObjects(hsvImageThresholded.clone(), trackingObjectList);
				}

				// Draw if GUI available
				if (this.isGuiEnabled) {

					// Display updated image
					normalPanel.setImageBuffer(toBufferedImage(originalImage));
					hsvFilteredPanel.setImageBuffer(toBufferedImage(hsvImageThresholded));

				}

			}
			// No frame found! Either done or camera has failed
			else {
				throw new RuntimeException("No frame found!");
			}
		}
	}

	/**
	 * Convert matrix into an image
	 * 
	 * @param m
	 *            matrix to be converted
	 * @return Converted BufferedImage
	 */
	private static BufferedImage toBufferedImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}

	/**
	 * Reduces the noise in the image by shrinking the groups of pixels to
	 * eliminate outliers, and then expand the pixels to restore the shrunken
	 * pixels.
	 * 
	 * @param image
	 *            - Image to be clarified
	 */
	private static void reduceNoise(Mat image) {
		// Create structuring element that will be used to "dilate" and "erode"
		// image. the element chosen here is a 3px by 3px rectangle
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));

		// Dilate with larger element so make sure object is nicely visible
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

		// Erode will shrink the grouping of pixels
		Imgproc.erode(image, image, erodeElement);
		Imgproc.erode(image, image, erodeElement);
		Imgproc.erode(image, image, erodeElement);

		// // Dilate will expand the grouping of pixels
		Imgproc.dilate(image, image, dilateElement);
		Imgproc.dilate(image, image, dilateElement);
		Imgproc.dilate(image, image, dilateElement);
	}

	/**
	 * Find the object and return the position of the centroid
	 * 
	 * @param inputImage
	 *            - Image that will be scanned for objects
	 * @return Position of the centroid
	 * */
	private static void findObjects(Mat inputImage, List<ITrackingObject> trackingObjectList) {
		PriorityQueue<Moments> maxAreaHeap = new PriorityQueue<Moments>(trackingObjectList.size(),
				new Comparator<Moments>() {
					public int compare(Moments x, Moments y) {
						return (int) (y.get_m00() - x.get_m00());
					}
				});

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();

		// Find the contours of the image and save them into contours and
		// hierarchy Where the hierarchy holds the relationship between the
		// current contour point and the next
		Imgproc.findContours(inputImage, contours, hierarchy, Imgproc.RETR_CCOMP,
				Imgproc.CHAIN_APPROX_SIMPLE);

		// The number of items in the hierarchy is the number of contours
		if (!trackingObjectList.isEmpty() && !hierarchy.empty()
				&& hierarchy.total() < Constants.DETECTION_MAX_OBJECTS) {

			int maxArea = trackingObjectList.get(0).getMaxObjectArea();
			int minArea = trackingObjectList.get(0).getMinObjectArea();

			// Go through each contour the hierarchy is a one dimensional
			// matrix 1xN where N is the number of contours. each item is
			// array of this format [Next, Previous, First_Child, Parent],
			// so we only want the next contour
			for (int index = 0; index >= 0 && hierarchy.get(index, 0) != null; index = (int) hierarchy
					.get(index, 0)[0]) {

				Moments moment = Imgproc.moments(contours.get(index));

				// if the area is less than 20 px by 20px then it is
				// probably just noise if the area is the same as the 3/2 of
				// the image size, probably just a bad filter we only want
				// the object with the largest area so we safe a reference
				// area each iteration and compare it to the area in the
				// next iteration.

				if (moment.get_m00() > minArea && moment.get_m00() < maxArea) {
					maxAreaHeap.add(moment);
				}
			}

			// Now assign positions based on max N areas detected
			// Skip if not enough areas detected to update targets
			if (maxAreaHeap.size() >= trackingObjectList.size()) {
				for (ITrackingObject trackingObject : trackingObjectList) {
					Moments moment = maxAreaHeap.peek();
					// TODO Use real dimensions! Need to figure out ratio
					// between captured pixels
					// and table dimensions
					int detectedPositionPixelX = (int) (moment.get_m10() / moment.get_m00());
					int detectedPositionPixelY = (int) (moment.get_m01() / moment.get_m00());
					Vector2 updatedPosition = new Vector2(Conversion.pixelToMeter(
							detectedPositionPixelX, Constants.getDetectionWidthScalingFactor()),
							Conversion.pixelToMeter(detectedPositionPixelY,
									Constants.getDetectionHeightScalingFactor()));
					Vector2 filteredPosition = new Vector2(trackingObject.getPosition()).add(updatedPosition.sub(trackingObject.getPosition()).scl(Constants.VELOCITY_FILTER_ALPHA));
					trackingObject.setPosition(filteredPosition);

					// Log the position
					// System.out.println(trackingObject.getPosition().toString());
				}
			}
		}
	}

	/**
	 * Creates a range of colors to threshold based on a pixel location, using a
	 * Normal Distribution.
	 * 
	 * @param image
	 *            - HSV image to threshold
	 * @param x
	 *            - x position of desired pixel
	 * @param y
	 *            - y position of desired pixel
	 * @return Range of HSV threshold values
	 */
	private static ScalarRange GetColorRangeHSV(Mat image, int x, int y) {
		if (image.channels() < 3)
			return null;

		int radius = 10;
		NormalDistribution<Double> hueDistribution = new NormalDistribution<>();
		NormalDistribution<Double> valueDistribution = new NormalDistribution<>();
		NormalDistribution<Double> saturationDistribution = new NormalDistribution<>();
		ScalarRange range = null;

		int initX = (x - radius >= 0) ? (x - radius) : 0;
		int initY = (y - radius >= 0) ? (y - radius) : 0;
		int endX = (x + radius <= image.cols()) ? (x + radius) : image.cols();
		int endY = (y + radius <= image.rows()) ? (y + radius) : image.rows();
		double[] color;

		for (int i = initX; i < endX; i++) {
			for (int j = initY; j < endY; j++) {
				color = image.get(j, i);
				hueDistribution.addData(color[0]);
				saturationDistribution.addData(color[1]);
				valueDistribution.addData(color[2]);
			}
		}

		range = new ScalarRange(new Scalar(
				(hueDistribution.getMean() - (0.5 * hueDistribution.getDevation())),
				(saturationDistribution.getMean() - (2 * saturationDistribution.getDevation())),
				(valueDistribution.getMean() - (3 * valueDistribution.getDevation()))), new Scalar(
				(hueDistribution.getMean() + (0.5 * hueDistribution.getDevation())),
				(saturationDistribution.getMean() + (2 * saturationDistribution.getDevation())),
				(valueDistribution.getMean() + (3 * valueDistribution.getDevation()))));
		return range;
	}

	public void initializeMouseListener() {
		mouseListener = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (slider != null)
					switch (slider.getCurrentObjType()) {
					case 0:
						// Puck
						slider.setSliders(GetColorRangeHSV(hsvImage, e.getX(), e.getY()));
						break;
					case 1:
						// Table bounds
						addPerspectiveBound(new Point(e.getX(), e.getY()));
						break;
					default:
						break;
					}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
			}
		};
	}

	public void addPerspectiveBound(Point point) {
		if (this.perspectiveBounds.size() == 4) {
			this.perspectiveBounds.clear();
		}

		this.perspectiveBounds.add(point);

		if (this.perspectiveBounds.size() == 4) {
			this.computePerspectiveTransform();
		}
	}

	private void computePerspectiveTransform() {
		// Array of destination points
		List<Point> destPoints = Arrays.asList(new Point(0, 0), new Point(hsvImage.cols(), 0),
				new Point(0, hsvImage.rows()), new Point(hsvImage.cols(), hsvImage.rows()));

		this.warpMat = Imgproc.getPerspectiveTransform(new MatOfPoint2f(this.perspectiveBounds.get(0),
				this.perspectiveBounds.get(1), this.perspectiveBounds.get(2), this.perspectiveBounds.get(3)),
				new MatOfPoint2f(destPoints.get(0), destPoints.get(1), destPoints.get(2), destPoints.get(3)));
	}
	
	public void resetPrespectiveTransform(){
		this.addPerspectiveBound(new Point(0, 0));
		this.addPerspectiveBound(new Point(hsvImage.cols(), 0));
		this.addPerspectiveBound(new Point(0, hsvImage.rows()));
		this.addPerspectiveBound(new Point(hsvImage.cols(), hsvImage.rows()));
	}
	
	public List<Point> getPerspectiveBounds(){
		return this.perspectiveBounds;
	}

}
package airhockeyjava.detection;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import airhockeyjava.util.Conversion;
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

	// Constants
	private static final int VIDEO_FRAME_HEIGHT = 480;
	private static final int VIDEO_FRAME_WIDTH = 720;
	// Constrains the number of objects able to be detected (including noise)
	private static final int MAX_NUM_OBJECTS = 20;
	// Minimum valid object area in pixel x pixel
	private static final int MIN_OBJECT_AREA = 10 * 10;
	// Maximum object area is to be a percentage of the frame's area
	private static final int MAX_OBJECT_AREA = (int) ((VIDEO_FRAME_HEIGHT * VIDEO_FRAME_WIDTH) * 0.67);

	// Map of tracking objects to number of occurrences/instances expected
	private Set<List<ITrackingObject>> objectSetsToTrack;
	private VideoCapture videoCapture;

	public Tracking(Set<List<ITrackingObject>> objectsToTrack, VideoCapture videoCapture) {
		this.objectSetsToTrack = objectsToTrack;
		this.videoCapture = videoCapture;
	}

	/**
	 * Entry point for the tracking thread
	 */
	public void run() {

		// Matrix that represents the individual images
		Mat originalImage = new Mat();
		Mat hsvImage = new Mat();

		// Iterate through frames as fast as possible! (for now)
		// TODO need to check if new frame is same as previous -- avoid repeating/duplicate frames!
		while (true) {

			// Read in the new video frame
			videoCapture.read(originalImage);

			if (!originalImage.empty()) {

				// Convert matrix from RGB to HSV
				Imgproc.cvtColor(originalImage, hsvImage, Imgproc.COLOR_BGR2HSV);

				Mat hsvImageThresholded = new Mat();

				// Iterate over each type of object; there is a set of objects of each type
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
					findObjects(hsvImageThresholded, trackingObjectList);

				}

			}

			// No frame found!
			else {
				System.out.println("No frame found!");
				break;
			}
		}
	}

	/**
	 * Reduces the noise in the image by shrinking the groups of pixels to
	 * eliminate outliers, and then expand the pixels to restore the shrinked
	 * pixels.
	 * 
	 * @param image
	 *            - Image to be clarified
	 */
	private static void reduceNoise(Mat image) {

		// create structuring element that will be used to "dilate" and "erode"
		// image. the element chosen here is a 3px by 3px rectangle
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));

		// dilate with larger element so make sure object is nicely visible
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
	 * Convert matrix into an image
	 * 
	 * @param m
	 *            - matrix to be converted
	 * @return Converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Mat m) {
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
	 * Find the object and return the position of the centroid
	 * 
	 * @param inputImage
	 *            - Image that will be scanned for objects
	 * @return Position of the centroid
	 * */
	public static void findObjects(Mat inputImage, List<ITrackingObject> trackingObjectList) {
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
		if (!hierarchy.empty() && hierarchy.total() < MAX_NUM_OBJECTS) {
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

				// TODO make min and max areas based on type of object
				if (moment.get_m00() > MIN_OBJECT_AREA && moment.get_m00() < MAX_OBJECT_AREA) {
					maxAreaHeap.add(moment);
				}
			}

			// Now assign positions based on max N areas detected
			// Skip if not enough areas detected to update targets
			if (maxAreaHeap.size() >= trackingObjectList.size()) {
				for (ITrackingObject trackingObject : trackingObjectList) {
					Moments moment = maxAreaHeap.peek();
					// TODO Use real dimensions! Need to figure out ratio between captured pixels
					// and table dimensions
					trackingObject.setPosition(new Vector2((float) (Conversion
							.pixelToMeter((int) (moment.get_m10() / moment.get_m00()))),
							(float) (Conversion.meterToPixel((int) (moment.get_m01() / moment
									.get_m00())))));

					// Log the position
					System.out.println(trackingObject.getPosition().toString());
				}
			}
		}
	}
}
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

import airhockeyjava.game.Constants;
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

	// Map of tracking objects to number of occurrences/instances expected
	private Set<List<ITrackingObject>> objectSetsToTrack;
	private VideoCapture videoCapture;

	public Tracking(Set<List<ITrackingObject>> objectsToTrack,
			VideoCapture videoCapture) {
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
		while (true) {

			// Read in the new video frame
			videoCapture.read(originalImage);

			if (!originalImage.empty()) {
				// Convert matrix from RGB to HSV
				Imgproc.cvtColor(originalImage, hsvImage, Imgproc.COLOR_BGR2HSV);

				Mat hsvImageThresholded = new Mat();

				// Iterate over each type of object; there is a set of objects
				// of each type
				for (List<ITrackingObject> trackingObjectList : objectSetsToTrack) {
					
					if (trackingObjectList == null
							|| trackingObjectList.isEmpty()) {
						continue;
					}

					// Threshold the hsv image to filter for Pucks
					Core.inRange(hsvImage, trackingObjectList.get(0)
							.getHSVMin(),
							trackingObjectList.get(0).getHSVMax(),
							hsvImageThresholded);

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
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(1, 1));

		// dilate with larger element so make sure object is nicely visible
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(12, 12));

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
	public static void findObjects(Mat inputImage,
			List<ITrackingObject> trackingObjectList) {
		PriorityQueue<Moments> maxAreaHeap = new PriorityQueue<Moments>(
				trackingObjectList.size(), new Comparator<Moments>() {
					public int compare(Moments x, Moments y) {
						return (int) (y.get_m00() - x.get_m00());
					}
				});

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();

		// Find the contours of the image and save them into contours and
		// hierarchy Where the hierarchy holds the relationship between the
		// current contour point and the next
		Imgproc.findContours(inputImage, contours, hierarchy,
				Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		// The number of items in the hierarchy is the number of contours
		if (!trackingObjectList.isEmpty() && !hierarchy.empty()
				&& hierarchy.total() < Constants.MAX_NUM_DETECTABLE_OBJECTS) {

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
					trackingObject.setPosition(new Vector2((float) (Conversion
							.pixelToMeter((int) (moment.get_m10() / moment
									.get_m00()))), (float) (Conversion
							.meterToPixel((int) (moment.get_m01() / moment
									.get_m00())))));

					// Log the position
					System.out.println(trackingObject.getPosition().toString());
				}
			}
		}
	}
}
package airhockeyjava.detection;

/*
 * Copyright (C) 2011,2012 Jiri Masa, Samuel Audet
 *
 * This file is part of JavaCV.
 *
 * JavaCV is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version (subject to the "Classpath" exception
 * as provided in the LICENSE.txt file that accompanied this code).
 *
 * JavaCV is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.O
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaCV.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import sun.misc.Unsafe;
import airhockeyjava.graphics.VideoDisplayPanel;
import cl.eye.CLCamera;

/**
 * Minimal Sony PS3 Eye camera grabber implementation.
 * 
 * It allows grabbing of frames at higher speed than OpenCVFrameGrabber or
 * VideoInputFrameGrabber. Underlying implementation of last two grabbers is
 * limited to 30 FPS. PS3 allows grabbing at maximum speed of 75 FPS in VGA and
 * 187 FPS in QVGA modes.
 * 
 * This code was developed and tested with CLEyeMulticam.dll, version
 * 1.2.0.1008. The dll library is part of Code Laboratories CL-Eye Platform SDK
 * and is distributed as part of CLEyeMulticam Redistributable Dynamic Link
 * Library. For license, download and installation see
 * http://www.codelaboratories.com.
 * 
 * The grab() method returns an internal instance of IplImage image with fresh
 * camera frame. This returned image have to be considered "read only" and the
 * caller needs to create it's own copy or clone that image. Calling of
 * release() method for this image shall be avoided. Based on used resolution
 * the image is in format either 640x480 or 320x240, IPL_DEPTH_8U, 4 channel
 * (color) or 1 channel (gray). timestamp is set to actual value of
 * System.nanoTime()/1000 obtained after return from the CL driver.
 * 
 * Typical use case scenario: create new instance of PS3MiniGrabber set camera
 * parameters start() grabber wait at least 2 frames grab() in loop stop()
 * grabber release() internal resources
 * 
 * Note: This code depends on the cl.eye.CLCamera class from Code Laboratories
 * CL-Eye Platform SDK. It is suggested to download SDK and edit the sample file
 * ....\cl\eye\CLCamera.java. A few references to processing.core.PApplet class
 * shall be removed and the file recompiled. The tailored file is not included
 * here namely because of unclear licence.
 * 
 * @author jmasa, jmasa@cmail.cz
 *
 */
public class PS3EyeFrameGrabber extends FrameGrabber {
	public static String[] getDeviceDescriptions() throws Exception {
		tryLoad();
		String[] descriptions = new String[CLCamera.cameraCount()];
		for (int i = 0; i < descriptions.length; i++) {
			descriptions[i] = CLCamera.cameraUUID(i);
		}
		return descriptions;
	}

	private static Exception loadingException = null;

	public static void tryLoad() throws Exception {
		if (loadingException != null) {
			throw loadingException;
		} else {
			try {
				CLCamera.IsLibraryLoaded();
			} catch (Throwable t) {
				throw loadingException = new Exception("Failed to load "
						+ PS3EyeFrameGrabber.class, t);
			}
		}
	}

	CLCamera camera;
	int cameraIndex = 0;
	// int[] ps3_frame = null; // buffer for PS3 camera frame data

	String uuid; // assigned camera unique key

	/**
	 * Default grabber, camera idx = 0, color mode, VGA resolution, 60 FPS frame
	 * rate.
	 * 
	 */
	public PS3EyeFrameGrabber() throws Exception {
		this(0);
	}

	/**
	 * Color mode, VGA resolution, 60 FPS frame rate.
	 * 
	 * @param system
	 *            wide camera index
	 */
	public PS3EyeFrameGrabber(int cameraIndex) throws Exception {
		this(cameraIndex, 640, 480, 60);
	}

	public PS3EyeFrameGrabber(int cameraIndex, int imageWidth, int imageHeight,
			int framerate) throws Exception {
		this(cameraIndex, imageWidth, imageHeight, framerate, null);
	}

	/**
	 * Creates grabber, the caller can control basic image and grabbing
	 * parameters.
	 * 
	 * @param cameraIndex
	 *            - zero based index of used camera (OS system wide)
	 * @param imageWidth
	 *            - width of image
	 * @param imageHeight
	 *            - height of image
	 * @param framerate
	 *            - frame rate - see CLCamera for allowed frame rates based on
	 *            resolution
	 * @param applet
	 *            - PApplet object required by CLCamera
	 * @throws Exception
	 *             - if parameters don't follow CLCamera definition or camera is
	 *             not created
	 */
	public PS3EyeFrameGrabber(int cameraIndex, int imageWidth, int imageHeight,
			int framerate, Object applet) throws Exception {
		camera = null;

		if (!CLCamera.IsLibraryLoaded()) {
			throw new Exception("CLEye multicam dll not loaded");
		}

		try {
			try {
				// maybe some new version of CLCamera works without a PApplet...
				camera = CLCamera.class.newInstance();
			} catch (Throwable t) {
				if (applet == null) {
					// do some really hacky stuff to create an instance
					// without calling the constructor
					Field unsafeField = Unsafe.class
							.getDeclaredField("theUnsafe");
					unsafeField.setAccessible(true);
					Unsafe unsafe = (Unsafe) unsafeField.get(null);
					camera = (CLCamera) unsafe.allocateInstance(CLCamera.class);
				} else {
					camera = (CLCamera) CLCamera.class.getConstructors()[0]
							.newInstance(applet);
				}
			}
		} catch (Throwable t) {
			throw new Exception("Failed to construct "
					+ PS3EyeFrameGrabber.class, t);
		}
		this.cameraIndex = cameraIndex;

		this.uuid = CLCamera.cameraUUID(cameraIndex);

		if (((imageWidth == 640) && (imageHeight == 480))
				|| ((imageWidth == 320) && (imageHeight == 240))) {
			setImageWidth(imageWidth);
			setImageHeight(imageHeight);
		} else
			throw new Exception("Only 640x480 or 320x240 images supported");

		setImageMode(ImageMode.COLOR);
		setFrameRate((double) framerate);
		setTimeout(1 + 1000 / framerate);
		setBitsPerPixel(8);
		setTriggerMode(false);
		setNumBuffers(3);
	}

	/**
	 * @return system wide number of installed/detected Sony PS3 Eye cameras
	 */
	public static int getCameraCount() {
		return CLCamera.cameraCount();
	}

	/**
	 * Ask the driver for all installed PS3 cameras. Resulting array is sorted
	 * in order of camera index. Its size is defined by CLCamera.cameraCount().
	 * 
	 * @return array of camera unique uuids or null if there is no PS3 camera
	 */
	public static String[] listPS3Cameras() {
		int no = getCameraCount();
		String[] uuids;
		if (no > 0) {
			uuids = new String[no];
			for (--no; no >= 0; no--) {
				uuids[no] = CLCamera.cameraUUID(no);
			}
			return uuids;
		}
		return null;
	}

	/**
	 * Grab one frame and return it as int[] (in the internal camera format
	 * RGBA). Note: use makeImage() to create RGBA, 4-ch image
	 * 
	 * @return frame as int[] without any processing or null if frame is not
	 *         available
	 */
	public int[] grab_raw() {
		int[] frame = new int[imageWidth * imageHeight];

		if (camera.getCameraFrame(frame, timeout)) {
			return frame;
		} else
			return null;
	}

	/**
	 * Grab and convert one frame, default timeout is (1 + 1000/framerate)
	 * [milliseconds]. Every successful call returns an internal (preallocated)
	 * 640x480 or 320x240, IPL_DEPTH_8U, 4-channel image. The caller shall
	 * consider it "read only" and make a copy/clone of it before further
	 * processing.
	 * 
	 * The call might block for timeout [milliseconds].
	 * 
	 * @return the image or null if there is no new image
	 */
	public int[] grab_RGB4() {
		int[] frame = new int[imageWidth * imageHeight];
		while (!camera.getCameraFrame(frame, timeout)) {
		}

		timestamp = System.nanoTime() / 1000;
		return frame;

	}

	/**
	 * Grab one frame; the caller have to make a copy of returned image before
	 * processing.
	 * 
	 * It will throw null pointer exception if not started before grabbing.
	 * 
	 * @return "read-only" RGB, 4-channel or GRAY/1-channel image, it throws
	 *         exception if no image is available
	 */
	@Override
	public IplImage grab() throws Exception {
		return null;

	}

	/**
	 * Grab one frame; the caller have to make a copy of returned image before
	 * processing.
	 * 
	 * It will throw null pointer exception if not started before grabbing.
	 * 
	 * @return "read-only" RGB, 3-channel
	 */
	public Mat grabMat() {
		Mat matImg = new Mat(this.imageHeight, this.imageWidth, CvType.CV_8UC4);
		int[] img = grab_RGB4();
		ByteBuffer byteBuffer = ByteBuffer.allocate(img.length * 4);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		intBuffer.put(img);

		byte[] array = byteBuffer.array();
		matImg.put(0, 0, array);
		List<Mat> mv = new ArrayList<Mat>();
		Core.split(matImg, mv);
		mv.remove(0);
		Core.merge(mv, matImg);
		return matImg;

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
			// System.out.println("3 Channel BufferedImage");
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}

	/**
	 * Start camera first (before grabbing).
	 * 
	 * @return success/failure (true/false)
	 */
	public void start() throws Exception {
		boolean b;

		b = camera.createCamera(cameraIndex,
				(imageMode == ImageMode.GRAY) ? CLCamera.CLEYE_MONO_PROCESSED
						: CLCamera.CLEYE_COLOR_PROCESSED,
				(imageWidth == 320 && imageHeight == 240) ? CLCamera.CLEYE_QVGA
						: CLCamera.CLEYE_VGA, (int) frameRate);

		if (!b)
			throw new Exception("Low level createCamera() failed");

		b = camera.startCamera();
		if (!b)
			throw new Exception("Camera start() failed");
		else
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * Stop camera. It can be re-started if needed.
	 * 
	 * @return success/failure (true/false)
	 */
	public void stop() throws Exception {
		boolean b = camera.stopCamera();
		if (!b)
			throw new Exception("Camera stop() failed");
	}

	/**
	 * Release resources: - CL driver internal resources binded with camera HW -
	 * internal IplImage After calling this function, this mini-grabber object
	 * instance can not be used anymore.
	 */
	public void release() {
		if (camera != null) {
			camera.dispose();
			camera = null;
		}
	}

	/**
	 * Release internal resources, the same as calling release()
	 */
	public void dispose() {
		release();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		release();
	}

	/**
	 * Return internal CLCamera object, mainly to set camera parameters,
	 * changing camera parameters must be done on stopped camera and before
	 * start() is called. See CL SDK - setCameraParameter(int param, int val)
	 * function.
	 * 
	 * @return internal CLCamera instance
	 */
	public CLCamera getCamera() {
		return camera;
	}

	public String getUUID() {
		return uuid;
	}

	/**
	 * @return status and camera parameters of the grabber
	 */
	@Override
	public String toString() {
		return "UUID=" + uuid + "; timeout=" + timeout + "; "
				+ ((camera != null) ? camera.toString() : "<no camera>");
	}

	/**
	 * Just for testing - loads the CL CLEyeMulticam.dll file, invokes driver
	 * and lists available cameras.
	 * 
	 * @param argv
	 *            - argv is not used
	 */
	public static void main(String[] argv) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String[] uuids = listPS3Cameras();
		for (int i = 0; i < uuids.length; i++)
			System.out.println(i + ": " + uuids[i]);

		/*
		 * Typical use case scenario: create new instance of PS3MiniGrabber set
		 * camera parameters start() grabber wait at least 2 frames grab() in
		 * loop stop() grabber release() internal resources
		 */

		try {
			PS3EyeFrameGrabber frameGrabber = new PS3EyeFrameGrabber(0, 640,
					480, 180);
			frameGrabber.start();

			VideoDisplayPanel normalPanel = new VideoDisplayPanel(null);

			JFrame normalFrame = new JFrame("Normal");
			normalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			normalFrame.setSize(640, 480);
			normalFrame.add(normalPanel);
			normalFrame.setVisible(true);

			while (true) {

				Mat matImg = frameGrabber.grabMat();
				Imgproc.cvtColor(matImg, matImg, Imgproc.COLOR_RGB2BGR);
				BufferedImage buffImg = toBufferedImage(matImg);
				normalPanel.setImageBuffer(buffImg);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void trigger() throws Exception {
		// TODO Auto-generated method stub

	}

}
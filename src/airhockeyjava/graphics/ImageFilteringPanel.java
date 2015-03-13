package airhockeyjava.graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Scalar;

import airhockeyjava.detection.ITrackingObject;
import airhockeyjava.game.Constants;
import airhockeyjava.util.FileWriter;
import airhockeyjava.util.ScalarRange;

/**
 * Graphics panel that displays sliders so that an HSV image levels can be
 * mutated, based on the Hue, Saturation and Value.
 * 
 * @author Nima Akhbari
 */
public class ImageFilteringPanel extends JPanel {

	// Constants
	private static final long serialVersionUID = 1L;
	private static final int NUM_TICKS_SPACING = 50;
	
	private JButton[] objectButtons = new JButton[3];
	private String[] objectTypes = { "Puck", "Bounds", "Save"};
	
	// Sliders for the high and lows of the Hue, Saturation and Value
	private JSlider[] high = new JSlider[3];
	private JLabel [] highLabels = new JLabel[3];
	
	private JSlider[] low = new JSlider[3];
	private JLabel [] lowLabels = new JLabel[3];

	// Ranges for the sliders
	private int[] lowRange = { 0, 0, 0 };
	private int[] highRange = { 179, 255, 255 };
	private String[] levelType = { "Hue", "Saturation", "Value" };

	// initial values
	private int[] lowInit = { 0, 0, 0 };
	private int[] highInit = { 179, 255, 255 };
	
	private int currentObjType;

	List<ITrackingObject> trackingObjects;
	
	private FileWriter writer;

	
	public int getCurrentObjType() {
		return currentObjType;
	}
	
	public ImageFilteringPanel(List<ITrackingObject> trackingObjects) {
		this.trackingObjects = trackingObjects;
		this.writer = new FileWriter(Constants.DETECTION_THRESHOLD_FILE_NAME);

		initialize();
	}

	/**
	 * Initializes the panel's slider components
	 */
	private void initialize() {

		//Add the object buttons
		for (int i = 0; i < objectTypes.length; i++) {
			objectButtons[i] = new JButton(objectTypes[i]);
			this.add(objectButtons[i]);
			objectButtons[i].setActionCommand(Integer.toString(i));
			
			//Add the on click listener to change the current object type (i.e. toggle between puck and table bounds)
			objectButtons[i].addActionListener(new ActionListener() {
				
			  public void actionPerformed(ActionEvent evt) {
				  JButton src = (JButton) evt.getSource();
				  currentObjType = Integer.parseInt(src.getActionCommand());
				  System.out.println("Clicked on: " + objectTypes[currentObjType]);
				  
				  switch (objectTypes[currentObjType]){
				  case "Save":
					  saveThresholdValues();
				  }

//				  double[] hsvMax = trackingObjects.get(currentObjType).getHSVMax().val;
//				  for (int i = 0; i < high.length; i++) {
//					high[i].setValue((int)hsvMax[i]);
//				  }
//					  
//				  double[] hsvMin = trackingObjects.get(currentObjType).getHSVMin().val;
//				  for (int i = 0; i < high.length; i++) {
//					low[i].setValue((int)hsvMin[i]);
//				  }
			  }
			});
		}
		
		if (high.length == low.length) {
			for (int i = 0; i < high.length; i++) {

				// initialize the slider to it's range
				high[i] = new JSlider(lowRange[i], highRange[i]);
				highLabels[i] = new JLabel("");
				low[i] = new JSlider(lowRange[i], highRange[i]);
				lowLabels[i] = new JLabel("");

				// format the labeling and ticks
				high[i].createStandardLabels(NUM_TICKS_SPACING);
				high[i].setMajorTickSpacing(NUM_TICKS_SPACING);
				high[i].setPaintTicks(true);
				high[i].setPaintLabels(true);
				high[i].setValue(highInit[i]);
				high[i].setName(Integer.toString(i));
				highLabels[i].setText(Integer.toString(highInit[i]));
				
				//Add a change listener to set hsvMax for the current object type
				high[i].addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						// If the slider is moved, print out the values
						JSlider src = (JSlider) e.getSource();
						
						// Set the text of the labels
						highLabels[Integer.parseInt(src.getName())].setText(Integer.toString(src.getValue()));

						//Set the hsvMax values on the current object type
						trackingObjects.get(currentObjType).setHSVMax(new Scalar(high[0].getValue(), high[1].getValue(),
								high[2].getValue()));
					}
				});

				// format the labeling and ticks
				low[i].createStandardLabels(NUM_TICKS_SPACING);
				low[i].setMajorTickSpacing(NUM_TICKS_SPACING);
				low[i].setPaintLabels(true);
				low[i].setPaintTicks(true);
				low[i].setValue(lowInit[i]);
				low[i].setName(Integer.toString(i));
				lowLabels[i].setText(Integer.toString(lowInit[i]));
	
				//Add a change listener to set hsvMmin for the current object type
				low[i].addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						// If the slider is moved, print out the values
						JSlider src = (JSlider) e.getSource();
						lowLabels[Integer.parseInt(src.getName())].setText(Integer.toString(src.getValue()));
						
						trackingObjects.get(currentObjType).setHSVMin(new Scalar(low[0].getValue(), low[1].getValue(),
								low[2].getValue()));
					}
				});
				
				// Display a label and then the respective slider
				this.add(new JLabel(levelType[i] + " low"));
				this.add(low[i]);
				this.add(lowLabels[i]);
				
				this.add(new JLabel(levelType[i] + " high"));
				this.add(high[i]);
				this.add(highLabels[i]);

			}
		}
		
		loadThresholdValues();
	}

	/**
	 * Returns values of the High levels
	 * 
	 * @return Scalar of the high levels
	 */
	public Scalar getScalarHigh() {
		return new Scalar(high[0].getValue(), high[1].getValue(),
				high[2].getValue());
	}

	/**
	 * Returns values of the Low levels
	 * 
	 * @return Scalar of the low levels
	 */
	public Scalar getScalarLow() {
		return new Scalar(low[0].getValue(), low[1].getValue(),
				low[2].getValue());
	}

	public void setSliders(ScalarRange range) {
		Scalar min = range.getMin();
		Scalar max = range.getMax();
		for (int i = 0; i < 3; i++) {
			low[i].setValue((int) min.val[i]);
			high[i].setValue((int) max.val[i]);
		}
	}
	/**
	 * Prints all the level values
	 */
	private void PrintValues() {
		System.out.println("Hue Range: (" + low[0].getValue() + ","
				+ high[0].getValue() + ") Saturation Range: ("
				+ low[1].getValue() + "," + high[1].getValue()
				+ ") Value Range: (" + low[2].getValue() + ","
				+ high[2].getValue() + ")");
	}
	
	public void loadThresholdValues(){
		try {
			String text = this.writer.read();
			String[] values = text.split(",");

			int j = 0;
			for (int i = 0; i < 3; i++) {
				low[i].setValue(Integer.parseInt(values[j++]));
				high[i].setValue(Integer.parseInt(values[j++]));
			}			
		} catch (IOException e){
			
		}

	}

	public void saveThresholdValues(){
		//Generate String
		String text = low[0].getValue() + ",";
		text += high[0].getValue() + ",";
		text += low[1].getValue() + ",";
		text += high[1].getValue() + ",";
		text += low[2].getValue() + ",";
		text += high[2].getValue();
		
		//Write String
		this.writer.write(text);
	}
}


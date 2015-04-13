package airhockeyjava.control;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

import airhockeyjava.game.Constants;

public class SerialConnection implements SerialPortEventListener {

	IController controller;
	SerialPort serialPort;
	private String inputBuffer = "";

	/**
	 * Constructor
	 */
	protected SerialConnection(IController controller) {
		this.controller = controller; // Reference to instantiating controller
	}

	/**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	private BufferedReader input;
	private OutputStream output; // Output stream to the port

	public void initialize() {
		// the next line is for Raspberry Pi and 
		// gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
		//System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

		CommPortIdentifier portId = null;

		@SuppressWarnings("rawtypes")
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : Constants.SERIAL_PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			System.err.println("Could not find COM port.");
			throw new RuntimeException(); // Can't proceed without establishing connection. TODO try again instead?
		}

		try {
			// Open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					Constants.SERIAL_TIME_OUT);

			// Set port parameters
			serialPort.setSerialPortParams(Constants.SERIAL_DATA_RATE, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// Open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// Add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
//				char inputChar = (char)input.read();
////				System.out.println(inputChar);
//				if(inputChar == '\n'){
					inputBuffer = input.readLine();
//					System.out.println("FROM ARDUINO: " + inputBuffer);
//					controller.handleInterfaceMessage(inputBuffer);
//					inputBuffer = "";
//	
//				}else{
//					inputBuffer += inputChar;
//				}
				controller.handleInterfaceMessage(inputBuffer);
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	/**
	 * Write bytes over the serial port.
	 */
	public synchronized void writeBytes(byte[] byteData) {
		try {
			output.write(byteData);
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
}
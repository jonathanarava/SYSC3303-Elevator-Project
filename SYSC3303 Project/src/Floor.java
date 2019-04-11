
//no main method
//no logic beyond changing the status of requests, arrival sensor, and display
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * class Floor: Each floor thread acts as one of the 22 floors. Has direction buttons and Floor display
 * takes Arguments for how many floors you would like in Floor Subsystem(max 22)
 */
public class Floor extends Thread {

	public boolean update; // an update has been set

	// UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES
	// States
	private static final byte UP = 0x01;// elevator is going up
	private static final byte DOWN = 0x02;// elevator is going down
	private static final byte STOP = 0x03;
	private static final byte HOLD = 0x04;// elevator is in hold state
	private static final byte UPDATE_DISPLAY = 0x05;
	private static final byte ERROR = (byte) 0xE0;// an error has occured
	// Errors
	private static final byte DOOR_ERROR = (byte) 0xE1;
	private static final byte MOTOR_ERROR = (byte) 0xE2;
	// still error states between 0xE3 to 0xEE for use
	private static final byte OTHER_ERROR = (byte) 0xEF;
	private static final byte NO_ERROR = 0x00;
	// Object ID
	private static final int ELEVATOR_ID = 21;// for identifying the packet's source as elevator
	private static final int FLOOR_ID = 69;// for identifying the packet's source as floor
	private static final int SCHEDULER_ID = 54;// for identifying the packet's source as scheduler
	// Values for Running
	private static final int DOOR_OPEN = 1;// the door is open when == 1
	private static final int DOOR_CLOSE = 3; // the door is closed when == 3
	private static final int DOOR_DURATION = 4;// duration (in seconds) that doors stay open for
	private static final int REQUEST = 1;// for identifying the packet type sent to scheduler as a request
	private static final int UPDATE = 2;// for identifying the packet type sent to scheduler as a status update
	private static final int INITIALIZE = 8;// for first communication with the scheduler
	private static final byte[] ELEVATOR_INITIALIZE_PACKET_DATA = { ELEVATOR_ID, 0, INITIALIZE, 0, 0, 0, 0, 0 };
	private static final byte[] FLOOR_INITIALIZE_PACKET_DATA = { FLOOR_ID, 0, INITIALIZE, 0, 0, 0, 0, 0 };
	private static final int UNUSED = 0;// value for unused parts of data
	private static final int DOOR_CLOSE_BY = 6;// door shouldn't be open for longer than 6 seconds

	private int realTimeDirectionRequest;
	private int goingDirection;
	// Variables for displaying what is happening with the elevators

	List<String> fileRequests = new ArrayList<String>();
	/*
	 * Real-time Input Information: In the next iteration these will be provided
	 * Time from EPOCH in an int, Floor where the elevator is requested in an
	 * Int(1-4) Which direction the Button was pressed in a String(Up or Down) What
	 * floor was requested inside the elevator in an int(1-4)
	 */

	// This String List will contain ALL of the real time input information that is
	// Given to the system.
	// List index

	// THESE SHOULD ALL BE PRIVATE RIGHT?
	private boolean schedulerInstruction;
	private int sendPort_num;
	private int elevatorLocation;
	// private static int NAMING;
	private int name;
	DatagramPacket floorSendPacket, floorReceivePacket;
	DatagramSocket floorSendReceiveSocket;

	private String elevatorRequest = "";
	private int numOfFloors;
	private int[] floorsMade;
	private List<byte[]> floorTable;

	private boolean hasRequest = false;
	private static byte error = NO_ERROR;// current Error, will be sent on the next send
	
/**
 * Constructor so Floors can be initialized in a way that can be runnable in the
 * scheduler
 */
	public Floor(int getName, List<byte[]> floorTable) {// int numOfFloors) {
		// NAMING = name;// mandatory for having it actually declared as a thread object
		// use a numbering scheme for the naming
		// floorsMade = new int[numOfFloors];
		name = getName;
		this.floorTable = floorTable;
	}
/**
 * 
 * @param requestUpdateError: Weather this is an Error, or a request to be sent to the scheduler
 * @param sendErrorType: if error, what type of error
 * Sends a packet to the scheduler from the Synchronized table that the floor threads use
 */
	public synchronized void sendPacket(int requestUpdateError, byte sendErrorType) {
		synchronized (floorTable) {
			while (floorTable.size() != 0) {// there is another packet waiting to be sent, wait for an opening to send
											// the packet
				try {
					floorTable.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			floorTable.add(createResponsePacketData(requestUpdateError, sendErrorType));
			// isUpdate = false;
			floorTable.notifyAll();
		}
	}
/**
 * 
 * @param requestUpdateError: Weather this is an Error, or a request to be sent to the scheduler
 * @param errorType: if error, what type of error
 * @return returns a byte array of the responsePacket that sendPacket() method will use
 */
	public byte[] createResponsePacketData(int requestUpdateError, byte errorType) {// create the Data byte[] for
		// the response packet to be
		// sent to the scheduler

		/*
		 * ELEVATOR --> SCHEDULER (elevator or floor (elevator-21), elevator id(which
		 * elevator), FloorRequest/update, curentFloor, up or down, destFloor,
		 * instruction) (
		 */
		// creates the byte array according to the required format

		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(FLOOR_ID); // identification as an elevator, instead of floor, or scheduler
		requestElevator.write(name); // identity of this particular elevator object

		// request or update data
		if (requestUpdateError == REQUEST) {
			requestElevator.write(REQUEST); // request
			// FLOOR SHOULD NEVER BE SENDING AN UPDATE
		} else if (requestUpdateError == ERROR) {
			requestElevator.write(ERROR); // update
			requestElevator.write(UNUSED);// setSensor(sensor)); // current floor
			requestElevator.write(realTimeDirectionRequest); // up or down (not used, only for Floors)
			requestElevator.write(UNUSED); // dest floor
			requestElevator.write(UNUSED); // instruction (not used, only from the scheduler)
			requestElevator.write(errorType); // error ID
			return requestElevator.toByteArray();
		} else {// something's gone wrong with the call to this method
			requestElevator.write(ERROR);
			System.out.println(name + " Floor ERROR: called createResponsePacketData with neither REQUEST or ERROR");
			requestElevator.write(UNUSED);// setSensor(sensor)); // current floor
			requestElevator.write(UNUSED); // up or down (not used, only for Floors)
			requestElevator.write(UNUSED); // dest floor
			requestElevator.write(UNUSED); // instruction (not used, only from the scheduler)
			requestElevator.write(OTHER_ERROR); // something's gone wrong
			return requestElevator.toByteArray();
		}
		requestElevator.write(UNUSED);// setSensor(sensor)); // current floor
		requestElevator.write(realTimeDirectionRequest); // up or down (not used, only for Floors)
		requestElevator.write(UNUSED); // dest floor
		requestElevator.write(UNUSED); // instruction (not used, only from the scheduler)
		requestElevator.write(UNUSED); // no errors
		return requestElevator.toByteArray();
	}
/**
 * 
 * @param upOrDown: Sets a realTime request on the specified floor Up or Down 
 */
	public void setRealTimeRequest(int upOrDown) {
		hasRequest = true;
		realTimeDirectionRequest = upOrDown;
	}

/**
 * 
 * @param whichElevator: Which elevator is arriving
 * @param onFloor: On which floor is this message to be updated 
 * @param schedularInstruction: What instruction was given to the elevator
 */
	public void updateDisplay(int whichElevator, int onFloor, int schedularInstruction) {
		System.out.println("Floor " + name + ": Elevator " + whichElevator + " is Currently on Floor  " + onFloor);
		if (schedularInstruction == UP || schedularInstruction == DOWN) {
			goingDirection = schedularInstruction;
		} else if (schedularInstruction == STOP) {
			if (onFloor == name) {
				System.out.println("Opening Doors");
			}
			return;
		} else if (schedularInstruction == HOLD) {
			if (onFloor == name) {
				System.out.println("Closing Doors, Elevator Holding on this floor");
			}
			return;
		}

		if (goingDirection == UP) {
			System.out.println("Coming Up");
		} else if (goingDirection == DOWN) {
			System.out.println("Coming Down");
		} else {
			System.out.println("Something's gone wrong in updateDisplay() direction argument for Floor: " + name);
			System.out.println("direction given is: " + goingDirection);
			System.out.println("floor given is: " + onFloor);
		}
	}
/**
 * Thread will wait indefinitely for a real time request from the floorIntermediate
 */
	public synchronized void waitForRequest() {
		while (!hasRequest) {
			try {
				System.out.println("waiting indefinitely");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}



/**
 * 
 * @param fullFile: Takes in .txt file which contains the realTime information that the system can
 * use to feed in Requests
 */
	public void fileReader(String fullFile) {
		String text = "";
		int i = 0;
		try {
			FileReader input = new FileReader(fullFile);
			Scanner reader = new Scanner(input);
			reader.useDelimiter("[\n]");

			while (reader.hasNext()) {
				text = reader.next();
				if (i <= 1) {
					i++;
				} else if (i >= 2) {
					fileRequests.add(text);
					i++;
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
/**
 * Run method for the thread
 */
	public void run() {
		while (hasRequest) {
			// while(true) to activate all elevator threads in this system
			if (hasRequest) {// send request
				sendPacket(1, error);
				// hasRequest = !hasRequest;
				hasRequest = false;
			}
		}
	}
}

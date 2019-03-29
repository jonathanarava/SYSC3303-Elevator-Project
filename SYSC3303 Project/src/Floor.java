
//no main method
//no logic beyond changing the status of requests, arrival sensor, and display
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
 * class Floor:has direction buttons and Floor display
 */
public class Floor extends Thread {

	public boolean update; //an update has been set
	/*
	//UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES
	private static final byte HOLD = 0x00;//elevator is in hold state
	private static final byte UP = 0x02;//elevator is going up
	private static final byte DOWN = 0x01;//elevator is going down
	private static final int ELEVATOR_ID=21;//for identifying the packet's source as elevator
	private static final int FLOOR_ID=69;//for identifying the packet's source as floor
	private static final int SCHEDULER_ID=54;//for identifying the packet's source as scheduler
	private static final int DOOR_OPEN=1;//the door is open when ==1
	private static final int DOOR_DURATION=4;//duration that doors stay open for
	private static final int REQUEST=1;//for identifying the packet sent to scheduler as a request
	private static final int UPDATE=2;//for identifying the packet sent to scheduler as a status update
	private static final int FLOOR = 69;*/

	// UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES
	// States
	private static final byte UP = 0x01;// elevator is going up
	private static final byte DOWN = 0x02;// elevator is going down
	private static final byte STOP = 0x03;
	private static final byte HOLD = 0x04;// elevator is in hold state
	private static final byte UPDATE_DISPLAY = 0x05;
	private static final byte ERROR = (byte) 0xE0;// an error has occured
	// Errors
	private static final byte DOOR_ERROR = (byte)0xE1;
	private static final byte MOTOR_ERROR = (byte)0xE2;
	// still error states between 0xE3 to 0xEE for use
	private static final byte OTHER_ERROR = (byte)0xEF;
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
	private static final int INITIALIZE=8;//for first communication with the scheduler
	private static final byte[] ELEVATOR_INITIALIZE_PACKET_DATA={ELEVATOR_ID,0,INITIALIZE, 0,0,0,0,0};
	private static final byte[] FLOOR_INITIALIZE_PACKET_DATA={FLOOR_ID,0,INITIALIZE, 0,0,0,0,0};
	private static final int UNUSED = 0;// value for unused parts of data
	private static final int DOOR_CLOSE_BY = 6;// door shouldn't be open for longer than 6 seconds

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
	
	//THESE SHOULD ALL BE PRIVATE RIGHT?
	private boolean schedulerInstruction;
	private int sendPort_num;
	private int elevatorLocation;
	//private static int NAMING;
	private int name;
	DatagramPacket floorSendPacket, floorReceivePacket;
	DatagramSocket floorSendReceiveSocket;

	private String elevatorRequest = "";
	private int numOfFloors;
	private int[] floorsMade;
	private List<byte[]> floorTable;

	private boolean hasRequest = true;
	private static byte error=NO_ERROR;//current Error, will be sent on the next send
	/*
	 * Constructor so Floors can be initialized in a way that can be runnable in the
	 * scheduler
	 */
	public Floor(int getName, List<byte[]> floorTable) {//int numOfFloors) {
		//NAMING = name;// mandatory for having it actually declared as a thread object
		// use a numbering scheme for the naming
		//floorsMade = new int[numOfFloors];
		name=getName;
		this.floorTable=floorTable;
	}
	
	public synchronized void sendPacket(int requestUpdateError, byte sendErrorType) {
		synchronized (floorTable) {
			while (floorTable.size() != 0) {//there is another packet waiting to be sent, wait for an opening to send the packet
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
		//FLOOR SHOULD NEVER BE SENDING AN UPDATE
		} else if (requestUpdateError == ERROR) {
			requestElevator.write(ERROR); // update
			requestElevator.write(UNUSED);// setSensor(sensor)); // current floor
			requestElevator.write(UNUSED); // up or down (not used, only for Floors)
			requestElevator.write(UNUSED); // dest floor
			requestElevator.write(UNUSED); // instruction (not used, only from the scheduler)
			requestElevator.write(errorType); // error ID
			return requestElevator.toByteArray();
		} else {// something's gone wrong with the call to this method
			requestElevator.write(ERROR);
			System.out.println(name+ " Floor ERROR: called createResponsePacketData with neither REQUEST or ERROR");
			requestElevator.write(UNUSED);// setSensor(sensor)); // current floor
			requestElevator.write(UNUSED); // up or down (not used, only for Floors)
			requestElevator.write(UNUSED); // dest floor
			requestElevator.write(UNUSED); // instruction (not used, only from the scheduler)
			requestElevator.write(OTHER_ERROR); // something's gone wrong
			return requestElevator.toByteArray();
		}
		requestElevator.write(UNUSED);// setSensor(sensor)); // current floor
		requestElevator.write(UNUSED); // up or down (not used, only for Floors)
		requestElevator.write(UNUSED); // dest floor
		requestElevator.write(UNUSED); // instruction (not used, only from the scheduler)
		requestElevator.write(UNUSED); // no errors
		return requestElevator.toByteArray();
	}
//	public byte[] createResponsePacketData(int requestUpdateError, byte errorType) {// create the Data byte[] for
//		// the response packet to be
//		// sent to the scheduler
//
//		/*
//		 * ELEVATOR --> SCHEDULER (elevator or floor (elevator-21), elevator id(which
//		 * elevator), FloorRequest/update, curentFloor, up or down, destFloor,
//		 * instruction) (
//		 */
//		// creates the byte array according to the required format
//
//		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
//		requestElevator.write(ELEVATOR_ID); // identification as a floor
//		requestElevator.write(name); // identity of this particular elevator object
//
//		// request or update data
//		if (requestUpdateError == REQUEST) {
//			requestElevator.write(REQUEST); // request
//		} else if (requestUpdateError == UPDATE) {
//			requestElevator.write(UPDATE); // update
//		} else if (requestUpdateError == ERROR) {
//			requestElevator.write(ERROR); // update
//			requestElevator.write(UNUSED);//(byte) setSensor(sensor)); // current floor
//			requestElevator.write(UNUSED); // up or down (not used, only for Floors)
//			requestElevator.write(UNUSED); // dest floor
//			requestElevator.write(UNUSED); // instruction (not used, only from the scheduler)
//			requestElevator.write(errorType); // error ID
//			return requestElevator.toByteArray();
//		} else {// something's gone wrong with the call to this method
//			requestElevator.write(ERROR);
//			System.out.println(name
//					+ " Floor ERROR: called createResponsePacketData with neither REQUEST, UPDATE, or ERROR");
//			requestElevator.write(UNUSED);//(byte) setSensor(sensor)); // current floor
//			requestElevator.write(UNUSED); // up or down (not used, only for Floors)
//			requestElevator.write(UNUSED); // dest floor
//			requestElevator.write(UNUSED); // instruction (not used, only from the scheduler)
//			requestElevator.write(OTHER_ERROR); // something's gone wrong
//			return requestElevator.toByteArray();
//		}
//		requestElevator.write(UNUSED);//(byte) setSensor(sensor)); // current floor
//		requestElevator.write(UNUSED); // up or down (not used, only for Floors)
//		requestElevator.write(UNUSED); // dest floor
//		requestElevator.write(UNUSED); // instruction (not used, only from the scheduler)
//		requestElevator.write(UNUSED); // no errors
//		return requestElevator.toByteArray();
//	}
	public byte[] responsePacket(int NAMING, int up_or_down){
		// creates the byte array according to the required format in this case
		// 00000000-DATABYTE-00000000
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(FLOOR_ID);  // To Say That I am a floor(69) elevator has ID(21)
		requestElevator.write(NAMING); // floor ID
		if(up_or_down != 0) {
			requestElevator.write(REQUEST); // request/update. floor only makes requests
		} else {
			requestElevator.write(2);
		}
		requestElevator.write(0); // Current Floor: Which Floor is sending this packet
		requestElevator.write(up_or_down); // Up or Down is being pressed at the floor
		requestElevator.write(0); // Destination floor (null)
		requestElevator.write(0); // scheduler instruction

		return requestElevator.toByteArray();
	}
	
	//SAME AS ELEVATOR'S METHOD
	/*public void updateDisplay() {
		System.out.println("On Floor: " + sensor);
		if (isGoingUp) {
			System.out.println("Going Up");
		} else if (!isGoingUp) {
			System.out.println("Going Down");
		}
	}*/

	public void updateDisplay(int onFloor, int goingDirection) {
		System.out.println("On Floor: " + onFloor);
		if (goingDirection==UP) {
			System.out.println("Going Up");
		} else if (goingDirection==DOWN) {
			System.out.println("Going Down");
		}
		else if(goingDirection==HOLD) {
			System.out.println("Holding");
		}
		else {
			System.out.println("Something's gone wrong in updateDisplay() direction argument for Floor: "+name);
			System.out.println("direction given is: "+goingDirection);
			System.out.println("floor given is: "+onFloor);
		}
	}

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
	
	/*public void LEDOnOrOff(byte up_or_down, ) {
		while (schedulerInstruction != true) {
			if (NAMING =)
		}
	}*/

	/*
	 * Takes in a .txt file as a string. 1st and 2nd line of of txt file are
	 * discarded(due to the formatting given in project requirements) Takes the
	 * input information and creates a list of Strings that will have the real time
	 * inputs as a string. For now This section will be commented. Will be
	 * implemented for other iterations
	 */
	public void fileReader(String fullFile) { 
		String text = "";
		int i=0;
		try { 
			FileReader input = new FileReader(fullFile);
			Scanner reader = new Scanner(input);
			reader.useDelimiter("[\n]");

			while (reader.hasNext()){
				text = reader.next();
				if (i<=1) {
					i++;
				} else if(i>=2) {
					fileRequests.add(text);
					i++;
				}
			}
			reader.close();
		}catch(Exception e) { e.printStackTrace(); }
	}
	

	
	public void run() {
		while (true) {
			// while(true) to activate all elevator threads in this system
			while (hasRequest) {// send request
				sendPacket(1, error);
				// hasRequest = !hasRequest;
				hasRequest = false;
			}
			while(!hasRequest) {
			}
		}
	}
}

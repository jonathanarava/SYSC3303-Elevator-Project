
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
	private static final int FLOOR = 69;

	
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
	public boolean schedulerInstruction;
	public int sendPort_num;
	public int elevatorLocation;
	public static int NAMING;
	DatagramPacket floorSendPacket, floorReceivePacket;
	DatagramSocket floorSendReceiveSocket;

	String elevatorRequest = "";
	int numOfFloors;
	int[] floorsMade;
	/*
	 * Constructor so Floors can be initialized in a way that can be runnable in the
	 * scheduler
	 */
	public Floor(int numOfFloors) {
		//NAMING = name;// mandatory for having it actually declared as a thread object
		// use a numbering scheme for the naming
		floorsMade = new int[numOfFloors];
	}

	public byte[] responsePacket(int NAMING, int up_or_down){
		// creates the byte array according to the required format in this case
		// 00000000-DATABYTE-00000000
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(FLOOR);  // To Say That I am a floor(69) elevator has ID(21)
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
	 * implemented for other itterations
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
		}catch(Exception e) { e.printStackTrace(); }
	}
}


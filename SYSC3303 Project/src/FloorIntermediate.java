import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Intermediate/Communication member of the Floor Subsystem. Will send UDP
 * packets created by floors to the scheduler. The instructions provided by the
 * scheduler will go be provided to the floors by this Class.
 * 
 * @author Group 5
 */
public class FloorIntermediate {
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

	private static final int UNUSED = 0;// value for unused parts of data
	private static final int DOOR_CLOSE_BY = 6;// door shouldn't be open for longer than 6 seconds

	public static final int EL_RECEIVEPORTNUM = 369;
	public static final int EL_SENDPORTNUM = 159;

	private static final int SENDPORTNUM = 369;
	private static final int RECEIVEPORTNUM = 1199;

	public static final int FL_RECEIVEPORTNUM = 488;
	public static final int FL_SENDPORTNUM = 1199;

	private static DatagramPacket floorSendPacket, floorReceivePacket;
	private static DatagramSocket floorReceiveSocket, floorSendSocket;

	public static List<byte[]> floorTable = Collections.synchronizedList(new ArrayList<byte[]>());
	private static Floor floorArray[];
	private static Thread floorThreadArray[];

	// arrays to keep track of the number of elevators, eliminates naming confusion
	private boolean intialized = false;
	private static int numFloors;
	private static byte initializationData[];

	static List<String> fileRequests = new ArrayList<String>();

	/**
	 * Constructor Of FloorIntermediate
	 */
	public FloorIntermediate() {
		try {
			floorSendSocket = new DatagramSocket();// FL_RECEIVEPORTNUM);
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Sends a packet of Information through the synchronized Table for the Floor
	 * threads
	 */
	public synchronized void sendPacket() {
		synchronized (floorTable) {
			while (floorTable.isEmpty()) {
				try {
					floorTable.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (floorTable.size() != 0) {
				try {
					System.out.println("Sending to scheduler: " + Arrays.toString(floorTable.get(0)));
					floorSendPacket = new DatagramPacket(floorTable.get(0), floorTable.get(0).length,
							InetAddress.getLocalHost() /*InetAddress.getByName("134.117.59.128")*/, SENDPORTNUM);

				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.exit(1);
				}
				try {
					floorSendSocket.send(floorSendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				floorTable.clear();
				floorTable.notifyAll();
			}
		}

	}

	/**
	 * method for Receiving Datagram Packets from the scheduler
	 */
	public synchronized void receivePacket() {
		byte data[] = new byte[8];
		int elevatorOnTheMove; // which elevator the update packet regarding
		int elevatorDirection;// which direction the elevator is going or holding (data[__])
		int elevatorLocation;// where the floor is (data[__])
		int schedulerInstruction;// the instruction sent from the scheduler (data[6]
		try {
			floorReceiveSocket = new DatagramSocket(RECEIVEPORTNUM);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		floorReceivePacket = new DatagramPacket(data, data.length);

		try {
			// Block until a datagram packet is received from receiveSocket.
			System.out.println("waiting to receive");
			floorReceiveSocket.receive(floorReceivePacket);
			System.out.print("Received from scheduler: ");
			System.out.println(Arrays.toString(data) + "\n");
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		floorReceiveSocket.close();// close the socket, will be reallocated at the next method call

		elevatorOnTheMove = data[1];
		elevatorLocation = data[3];
		elevatorDirection = data[4];
		schedulerInstruction = data[6];

		if (schedulerInstruction == INITIALIZE) {
			intialized = true;
		} else {
			// floorArray[data[1]].updateDisplay(elevatorLocation, elevatorDirection);
			// send updates to all the floors
			for (int i = 0; i < numFloors; i++) {
				floorArray[i].updateDisplay(elevatorOnTheMove, elevatorLocation, schedulerInstruction);
			}
		}
	}

	/**
	 * FloorIntermediate class is initialized with the scheduler using this method
	 */
	private static void floorInitialization() {
		ByteArrayOutputStream initializationOutputStream = new ByteArrayOutputStream();
		initializationOutputStream.write(FLOOR_ID); // Identifying as the scheduler
		initializationOutputStream.write(numFloors); // (exact floor or elevator to receive)
		initializationOutputStream.write(INITIALIZE); // not needed (request or status update: for sending from
														// scheduler)
		// somewhat redundant usage since floors would only receive updates and
		// elevators would only receive requests

		initializationOutputStream.write(UNUSED); // not needed (current floor of elevator)
		initializationOutputStream.write(UNUSED); // not needed (direction of elevator)

		initializationOutputStream.write(UNUSED); // not needed (destination request)
		initializationOutputStream.write(UNUSED); // scheduler instruction
		initializationOutputStream.write(UNUSED); // error's only received by scheduler and not sent

		initializationData = initializationOutputStream.toByteArray();
		floorTable.add(initializationData);

	}

	/*
	 * Takes in a .txt file as a string. 1st and 2nd line of of txt file are
	 * discarded(due to the formatting given in project requirements) Takes the
	 * input information and creates a list of Strings that will have the real time
	 * inputs as a string. For now This section will be commented. Will be
	 * implemented for other itterations
	 */
	/*
	 * public static void fileReader(String fullFile) { String text = ""; int i=0;
	 * try { FileReader input = new FileReader(fullFile); Scanner reader = new
	 * Scanner(input); reader.useDelimiter("[\n]");
	 * 
	 * while (reader.hasNext()){ text = reader.next(); if (i<=1) { i++; } else
	 * if(i>=2) { fileRequests.add(text); i++; } } }catch(Exception e) {
	 * e.printStackTrace(); } }
	 */
	/**
	 * Main execution code of the Floor Subsystem
	 */
	public static void main(String args[]) {// throws IOException {

		// for iteration 1 there will only be 1 elevator
		// getting floor numbers from parameters set
		// int createNumFloors = Integer.parseInt(args[0]);// The number of Elevators in
		// the system is passed via argument[0]
		numFloors = Integer.parseInt(args[0]);// The number of Elevators in the system is passed via argument[0]
		FloorIntermediate floorHandler = new FloorIntermediate();
		// Floor floor = new Floor(createNumFloors);

		// fileReader("M://hello.txt");
		// read the input file and get requests for each of the floors. We will then
		// feed these real time info to the floors before we run out program.
		numFloors = Integer.parseInt(args[0]);
		floorArray = new Floor[numFloors];
		floorThreadArray = new Thread[numFloors];

		// FOR INITIATION
		floorInitialization();
		floorHandler.sendPacket();
		floorHandler.receivePacket();
		for (int i = 0; i < numFloors; i++) {
			floorArray[i] = new Floor(i, floorTable);// 0, floorTable, Integer.parseInt(args[i + 1])); // i names the

			// elevator, 0
			// initializes the
			// floor it
			// starts on
			floorThreadArray[i] = new Thread(floorArray[i]);
//			if(i == Integer.parseInt(args[1])) {
//				floorArray[Integer.parseInt(args[1])].setRealTimeRequest(UP);
//			}
			// floorThreadArray[i].start();
		}

		while (true) {
			if (!floorTable.isEmpty()) {
				floorHandler.sendPacket();
			}
			floorHandler.receivePacket();
		}
	}
}

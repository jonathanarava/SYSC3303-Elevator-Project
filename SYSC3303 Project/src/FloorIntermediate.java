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

public class FloorIntermediate {

	/*
	// UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES
	private static final byte HOLD = 0x00;// elevator is in hold state
	private static final byte UP = 0x01;// elevator is going up
	private static final byte DOWN = 0x02;// elevator is going down
	private static final int ELEVATOR_ID = 21;// for identifying the packet's source as elevator
	private static final int FLOOR_ID = 69;// for identifying the packet's source as floor
	private static final int SCHEDULER_ID = 54;// for identifying the packet's source as scheduler
	private static final int DOOR_OPEN = 1;// the door is open when ==1
	private static final int DOOR_DURATION = 4;// duration that doors stay open for
	private static final int REQUEST = 1;// for identifying the packet sent to scheduler as a request
	private static final int UPDATE = 2;// for identifying the packet sent to scheduler as a status update
	//*/
	
	//UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES
		//States
		private static final byte UP = 0x01;// elevator is going up
		private static final byte DOWN = 0x02;// elevator is going down
		private static final byte STOP = 0x03;
		private static final byte HOLD = 0x04;// elevator is in hold state
		private static final byte UPDATE_DISPLAY = 0x05;
		private static final byte ERROR=(byte)0xE0;//an error has occured
		//Errors
		private static final byte DOOR_ERROR=(byte)0xE1;
		private static final byte MOTOR_ERROR=(byte)0xE2;
		//still error states between 0xE3 to 0xEE for use
		private static final byte OTHER_ERROR=(byte)0xEF; 
		private static final byte NO_ERROR=(byte)0x00;
		//Object ID
		private static final int ELEVATOR_ID = 21;// for identifying the packet's source as elevator
		private static final int FLOOR_ID = 69;// for identifying the packet's source as floor
		private static final int SCHEDULER_ID = 54;// for identifying the packet's source as scheduler
		//Values for Running
		private static final int DOOR_OPEN = 1;// the door is open when == 1
		private static final int DOOR_CLOSE = 3; // the door is closed when == 3 
		private static final int DOOR_DURATION = 4;// duration (in seconds) that doors stay open for
		private static final int REQUEST = 1;// for identifying the packet type sent to scheduler as a request
		private static final int UPDATE = 2;// for identifying the packet type sent to scheduler as a status update
		private static final int MAKE_STOP=3;//
		private static final int PLACE_ON_HOLD=4;
		private static final int UPDATE_DISPLAYS=5;
		private static final int SHUT_DOWN=6;//for shutting down a hard fault problem elevator
		private static final int FIX_ELEVATOR=7;//
		private static final int INITIALIZE=8;//for first communication with the scheduler
		private static final int UNUSED=0;// value for unused parts of data 
		private static final int DOOR_CLOSE_BY=6;//door shouldn't be open for longer than 6 seconds

	public static final int EL_RECEIVEPORTNUM = 369;
	public static final int EL_SENDPORTNUM = 159;

	/*//FROM ELEVATORINTERMEDIATE
	private static final int SENDPORTNUM = 369;
	private static final int RECEIVEPORTNUM = 159;
	 */

	private static final int SENDPORTNUM = 369;
	private static final int RECEIVEPORTNUM = 1199;

	public static final int FL_RECEIVEPORTNUM = 488;
	public static final int FL_SENDPORTNUM = 1199;

	private static DatagramPacket floorSendPacket, floorReceivePacket;
	private static DatagramSocket floorReceiveSocket,floorSendSocket;

	public static List<byte[]> floorTable = Collections.synchronizedList(new ArrayList<byte[]>());
	private static Floor floorArray[];
	private static Thread floorThreadArray[];

	// arrays to keep track of the number of elevators, eliminates naming confusion
//	private static int name;
//	private static boolean hasRequest;
//	private static int up_or_down;
	//private boolean intialized=false;
	private static int numFloors;
	private static byte initializationData[];
	private static byte receiveData[]=new byte[8];
	/*
	 * send sockets should be allocated dynamically since the ports would be
	 * variable to the elevator or floor we have chosen
	 */
	//public static final int SENDPORTNUM = 488;

	public FloorIntermediate() {
		try {
			floorSendSocket = new DatagramSocket();//FL_RECEIVEPORTNUM);
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}


	public synchronized void sendPacket() {
		// byte[] requestElevator = new byte[7];

		/* ELEVATOR --> SCHEDULER (0, FloorRequest, cuurentFloor, 0) */

		// System.out.println("Enter floor number: ");

		// Scanner destination = new Scanner(System.in);
		// int floorRequest;
		// if (destination.nextInt() != 0) {
		// floorRequest = destination.nextInt();
		// } else {

		// }
		// destination.close();
		// requestElevator = elevatorArray[0].responsePacketRequest(1); // this goes
		// into the first index of elevatorArray list, and tells that elevator to return
		// a byte array that
		// will be the packet that is being sent to the Scheduler. This needs to be done
		// in a dynamic manner so all
		// elevators can acquire a lock to send a packet one at a time.

		// allocate sockets, packets
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
					System.out.println("\nSending to scheduler: " + Arrays.toString(floorTable.get(0)));
					floorSendPacket = new DatagramPacket(floorTable.get(0), floorTable.get(0).length, 
							InetAddress.getLocalHost() /*InetAddress.getByName("134.117.59.126")*/,
							SENDPORTNUM);
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

	public synchronized void receivePacket() {
		//byte data[] = new byte[8];
		int elevatorElement;
		int elevatorDirection;//which direction the elevator is going or holding (data[__])
		int elevatorLocation;//where the floor is (data[__])
		int schedulerInstruction;//the instruction sent from the scheduler (data[6]
		
		try {
			floorReceiveSocket = new DatagramSocket(RECEIVEPORTNUM);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		floorReceivePacket = new DatagramPacket(receiveData, receiveData.length);
//		try {
//			System.out.println("Waiting...\n"); // so we know we're waiting
//			floorReceiveSocket.receive(floorReceivePacket);
//			System.out.println("Got it");
//		}
//
//		catch (IOException e) {
//			System.out.print("IO Exception: likely:");
//			System.out.println("Receive Socket Timed Out.\n" + e);
//			e.printStackTrace();
//			System.exit(1);
//		}
		try {
			// Block until a datagram packet is received from receiveSocket.
			System.out.println("waiting to receive");
			floorReceiveSocket.receive(floorReceivePacket);
			System.out.print("Received from scheduler: ");
			System.out.println(Arrays.toString(receiveData));
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		floorReceiveSocket.close();//close the socket, will be reallocated at the next method call
		elevatorElement=receiveData[1];
		elevatorLocation=receiveData[3];
		System.out.println("FloorIntermediate's elevatorLocation: "+elevatorLocation);
		
		elevatorDirection=receiveData[4];
		System.out.println("FloorIntermediate's elevatorDirection: "+elevatorDirection);
		schedulerInstruction=receiveData[6];
		if (schedulerInstruction==INITIALIZE) {
			//intialized=true;
			System.out.println("FloorIntermediate INITIALIZED");
		}
		else if(schedulerInstruction==UPDATE_DISPLAYS) {
			for (int i = 0; i < numFloors; i++){
				floorArray[i].updateDisplay(elevatorElement, elevatorLocation, elevatorDirection);
			}
		}
		else {
			//something's gone wrong, floors should only ever be receiving updates
			System.out.println("Floor: "+ elevatorLocation+" error in receivePacket(), not an UPDATE but: "+schedulerInstruction);
		}
	}

	private static void floorInitialization() {
		ByteArrayOutputStream initializationOutputStream = new ByteArrayOutputStream();
		initializationOutputStream.write(FLOOR_ID); // Identifying as the scheduler
		initializationOutputStream.write(numFloors); // (exact floor or elevator to receive)
		initializationOutputStream.write(INITIALIZE); // not needed (request or status update: for sending from scheduler)
				initializationOutputStream.write(UNUSED); // not needed (current floor of elevator)
		initializationOutputStream.write(UNUSED); // not needed (direction of elevator)
		initializationOutputStream.write(UNUSED); // not needed (destination request)
		initializationOutputStream.write(UNUSED); // scheduler instruction
		initializationOutputStream.write(UNUSED); // error's only received by scheduler and not sent
		initializationData=initializationOutputStream.toByteArray();
		floorTable.add(initializationData);

	}

	public static void main(String args[]) {// throws IOException {

		//FOR ITERATION 5, hard coding number of elevators, floors, requests, errors
		numFloors=22;//as per specificied
		
		// getting floor numbers from parameters set
		//int createNumFloors = Integer.parseInt(args[0]);// The number of Elevators in the system is passed via argument[0]
		//numFloors = Integer.parseInt(args[0]);// The number of Elevators in the system is passed via argument[0]
		FloorIntermediate floorHandler = new FloorIntermediate();
		//Floor floor = new Floor(createNumFloors);

		//floor.fileReader("M://hello.txt");
		//createNumFloors = Integer.parseInt(args[0]);
		//numFloors = Integer.parseInt(args[0]);
		floorArray = new Floor[numFloors];
		floorThreadArray = new Thread[numFloors];
		
		//FOR INITIATION
		floorInitialization();
		floorHandler.sendPacket();
		floorHandler.receivePacket();//since self blocking, will not continue onwards until initialization has been received from scheduler
		for (int i = 0; i < numFloors; i++) {
			floorArray[i] = new Floor(i, floorTable);//0, floorTable, Integer.parseInt(args[i + 1])); // i names the
			// elevator, 0
			// initializes the
			// floor it
			
			//FOR TESTING; SETS A REQUEST FOR EACH FLOOR TO SEND
			/*if (i!=numFloors-1) {
				floorArray[i].setRequestDirection(UP);
			}
			else {
				floorArray[i].setRequestDirection(DOWN);
			}*/
			//IMPLEMENTED THROUGH ELEVATORINTERMEDIATE FOR SIMPLICITY INSTEAD
			//FOR ITERATION 5, hard coding number of elevators, floors, requests, errors
			/*if (i==1 ||1==2) {//as per specified, arrivals for levels one and two
				floorArray[i].setRequestDirection(UP);
			}*/
			
			// starts on
			floorThreadArray[i] = new Thread(floorArray[i]);
			floorThreadArray[i].start();
		}

		while(true) {
			floorHandler.sendPacket();
			floorHandler.receivePacket();
		}

	}
}
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Intermediate/Communication member of the elevator Subsystem.
 * Will send UDP packets created by elevators to the scheduler.
 * The instructions provided by the scheduler will go be provided to the elevators by this Class. 
 * @author Group 5
 */
public class ElevatorIntermediate {
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

	private static final int SENDPORTNUM = 369;// port number for sending to the scheduler
	private static final int RECEIVEPORTNUM = 159;

	private static DatagramPacket elevatorSendPacket, elevatorReceivePacket;
	private static DatagramSocket elevatorSendSocket, elevatorReceiveSocket;

	//private static boolean firstRunTime = true;
	// for iteration 1 there will only be 1 elevator
	// getting floor numbers from parameters set
	private static int createNumElevators;// The number of Elevators in the system is passed via argument[0]

	// arrays to keep track of the number of elevators, eliminates naming confusion
	private static Elevator elevatorArray[];
	private static Thread elevatorThreadArray[];

	//private byte[] requestElevator = new byte[3];
	//private boolean intialized=false;

	private static DatagramPacket schedulerSendPacket, schedulerReceivePacket;
	private static byte initializationData[];
	/*
	 * send sockets should be allocated dynamically since the ports would be
	 * variable to the elevator or floor we have chosen
	 */


	//VARIABLES
	private static byte[] receiveData= new byte[8];	
	private static int packetOrigin;// data[0]
	private static int packetElement;// data[1];
	private static int packetType;// data[2];
	private static int packetCurrentFloor;// data[3];
	private static int packetDirection;// data[4];
	private static int packetRequestFloor;// data[5];
	private static int schedulerInstruction; //data[6] Scheduler's Instruction 
	private static int packetError;// data[7];
	
	
	// synchronized table that all of the elevator threads will put their requests
	// and updates upon
	private static byte[] sendData = new byte[8];

	/**
	 * Constructor Of ElevatorIntermediate
	 */
	public ElevatorIntermediate() {
		try {
			elevatorSendSocket = new DatagramSocket();
			//elevatorSendSocket.setSoTimeout(250);// sets the maximum time for the receive function to self block
			//elevatorSendSocket.setSoTimeout(2);
			// elevatorReceiveSocket = new DatagramSocket();// can be any available port,
			// Scheduler will reply to the port
			// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			System.out.println("ElevatorIntermediate() creation went wrong");
			se.printStackTrace();
			System.exit(1);
		}

	}
	/**
	 * Sends a packet of Information through the synchronized socket for the elevators
	 */
	public void sendPacket() {//int requestUpdateError, int destinationFloor, byte sendErrorType) {
		//FOR THIS TESTING ONLY
		System.out.println("ElevatorIntermediate's SendPacket() called");
		synchronized(elevatorSendSocket) {
			//sendData=createResponsePacketData(requestUpdateError,destinationFloor,sendErrorType);
			try {
				elevatorSendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost() ,	SENDPORTNUM);
			} catch (UnknownHostException e) {
				System.out.println("Send Error for ElevatorIntermediate");
			}
			
			try {
				System.out.println("\nSending to scheduler: " + Arrays.toString(sendData));
				elevatorSendSocket.send(elevatorSendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			//System.out.println("ElevatorIntermediate's sendPacket sent");
			elevatorSendSocket.notifyAll();
		}
	}

	/**
	 * method for Receiving Datagram Packets from the scheduler
	 */
	public void receivePacket() {
		// SCHEDULER --> ELEVATOR (0, motorDirection, motorSpinTime, open OR close door,
		// 0)
		//		byte data[] = new byte[8];
		//		byte schedulerInstruction;
		//		byte elevatorElement;

		try {
			elevatorReceiveSocket = new DatagramSocket(RECEIVEPORTNUM);
			// elevatorReceiveSocket.setSoTimeout(10); Eventually we will need this to sync
			// send and receive with Scheduler.
		} catch (SocketException receiveException) {
			System.out.println("timed out of sotime");
		}
		elevatorReceivePacket = new DatagramPacket(receiveData, receiveData.length);
		// System.out.println("elevator_subsystem: Waiting for Packet.\n");

		try {
			// Block until a datagram packet is received from receiveSocket.
			System.out.println("waiting to receive");
			elevatorReceiveSocket.receive(elevatorReceivePacket);
			System.out.print("\nReceived from scheduler: ");
			System.out.println(Arrays.toString(receiveData));
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		elevatorReceiveSocket.close();

		packetOrigin = receiveData[0];
		packetElement = receiveData[1];
		packetType = receiveData[2];
		packetCurrentFloor = receiveData[3];
		packetDirection = receiveData[4];
		packetRequestFloor = receiveData[5];
		schedulerInstruction=receiveData[6];//data[6] is Scheduler's Instruction and should be left empty/ not received
		packetError=receiveData[7];
		//schedulerInstruction=data[6];
		//elevatorElement=data[1];
		if (schedulerInstruction==UP) {
			elevatorArray[packetElement].previousState = elevatorArray[packetElement].elevatorState;
			elevatorArray[packetElement].elevatorState = schedulerInstruction;
			elevatorArray[packetElement].dealWith = true;
		}
		else if (schedulerInstruction==DOWN) {
			elevatorArray[packetElement].previousState = elevatorArray[packetElement].elevatorState;
			elevatorArray[packetElement].elevatorState = schedulerInstruction;
			elevatorArray[packetElement].dealWith = true;
		}
		else if (schedulerInstruction==STOP) {
			elevatorArray[packetElement].previousState = elevatorArray[packetElement].elevatorState;
			elevatorArray[packetElement].elevatorState = schedulerInstruction;
			elevatorArray[packetElement].dealWith = true;
		}
		else if (schedulerInstruction==HOLD) {
			elevatorArray[packetElement].previousState = elevatorArray[packetElement].elevatorState;
			elevatorArray[packetElement].elevatorState = schedulerInstruction;
			elevatorArray[packetElement].dealWith = true;
		}
		else if (schedulerInstruction==INITIALIZE) {
			//intialized=true;
			System.out.println("ElevatorIntermediate INITIALIZED\n");
		}
		else if(schedulerInstruction==UPDATE_DISPLAYS) {
			System.out.println("Update Displays");
			updateDisplay();
			/*for (int i=0;i<createNumElevators;i++) {
				elevatorArray[i].updateDisplay();
			}*/
		}
		else if(schedulerInstruction==SHUT_DOWN) {
			elevatorArray[packetElement].shutDown();
		}
		else if (schedulerInstruction==FIX_ELEVATOR) {
			elevatorArray[packetElement].fixElevator();
		}
		else if(schedulerInstruction==UNUSED) {
			//do nothing
		}
		else {
			System.out.println("Elevator: "+packetElement +"; Unknown Instruction from Scheduler: "+schedulerInstruction);
		}

	}
	/**
	 * Updates the Displays for all Elevators in the subsystem
	 */
	public static void updateDisplay() {
		for (int i=0;i<createNumElevators;i++) {
			elevatorArray[i].updateDisplay();
		}
	}
	/**
	 * 
	 * @param elevatorHandler: ElevatorIntermediate class that's pointed to
	 * ElevatorIntermediate class is initialized with the Scheduler
	 */
	private static void elevatorInitialization(ElevatorIntermediate elevatorHandler) {
		ByteArrayOutputStream initializationOutputStream = new ByteArrayOutputStream();
		initializationOutputStream.write(ELEVATOR_ID); // Identifying as the scheduler
		initializationOutputStream.write(createNumElevators); // (exact floor or elevator to receive)
		initializationOutputStream.write(INITIALIZE); // not needed (request or status update: for sending from scheduler)
		// somewhat redundant usage since floors would only receive updates and
		// elevators would only receive requests

		initializationOutputStream.write(UNUSED); // not needed (current floor of elevator)
		initializationOutputStream.write(UNUSED); // not needed (direction of elevator)

		initializationOutputStream.write(UNUSED); // not needed (destination request)
		initializationOutputStream.write(UNUSED); // scheduler instruction
		initializationOutputStream.write(UNUSED); // error's only received by scheduler and not sent

		initializationData=initializationOutputStream.toByteArray();
		
		
		//FOR THIS TESTING ONLY
		//elevatorTable.add(initializationData);
		/*try {
			elevatorSendPacket = new DatagramPacket(initializationData, initializationData.length, InetAddress.getLocalHost() ,	SENDPORTNUM);
		} catch (UnknownHostException e) {
			System.out.println("Send Error for ElevatorIntermediate ");
		}*/
		sendData=initializationData;
		elevatorHandler.sendPacket();
	}
	/**
	 * Main execution code of the Elevator Subsystem
	 */
	public static void main(String args[]) throws IOException {
		// 2 arguments: args[0] is the number of Elevators in the system
		ElevatorIntermediate elevatorHandler = new ElevatorIntermediate();

		//FOR ITERATION 5, hard coding number of elevators, floors, requests, errors
		createNumElevators = 4;//as per specificied
		//createNumElevators = Integer.parseInt(args[0]);// The number of Elevators in the system is passed via
		// argument[0]

		// for keeping track of the port numbers, filled as they get declared
		// since we're not strictly replying to the immediate packet we can't get the
		// port numbers there
		// allocating port numbers to the variable number of elevators and floors would
		// also be difficult, just using the ones which are available
		//int elevatorPortNumbers[] = new int[createNumElevators];

		// Lets create a socket for the elevator Intermediate class to communicate
		// with the scheduler. All the elevator threads will use this.

		// allocate receive packet
		//byte data[] = new byte[8];
		schedulerReceivePacket = new DatagramPacket(receiveData, receiveData.length);

		// go for the argument passed into Elevator Intermediate, create an array for
		// elevators,
		elevatorInitialization(elevatorHandler);

		//INITIALIZE THE SCHEDULER
		//FOR THIS TESTING ONLY elevatorHandler.sendPacket();
		//wait for the scheduler to have access to Elevators and Floors
		elevatorHandler.receivePacket();
		//NEEDS TO BE AFTER INITIALIZATION
		// arrays to keep track of the number of elevators, eliminates naming confusion
		elevatorArray = new Elevator[createNumElevators];
		elevatorThreadArray = new Thread[createNumElevators];

		//create elevators, assign them to the array, threads, and start them
		for (int i = 0; i < createNumElevators; i++) {
			//elevatorArray[i] = new Elevator(i, 0, elevatorTable, Integer.parseInt(args[i + 1])); // i names the elevator, 0 initializes the floor it starts on
			//FOR THIS TESTING ONLYelevatorArray[i] = new Elevator(i, 0, elevatorTable);//, Integer.parseInt(args[i + 1])); // i names the
			elevatorArray[i] = new Elevator(i, 0, elevatorSendSocket);//, Integer.parseInt(args[i + 1])); // i names the
			elevatorThreadArray[i] = new Thread(elevatorArray[i]);
			elevatorThreadArray[i].start();
		}

		updateDisplay();

		//add the requesst so that the elevators stop at first and second floor
		System.out.println("Adding Arrival request at floor 1 for Elevator 0");
		elevatorArray[0].setRealTimeFloorRequest(1);//add request for elevator 0 (first elevator) to go to the first floor
		//elevatorHandler.sendPacket();
		elevatorHandler.receivePacket();

		System.out.println("Adding Arrival request at floor 2 for Elevator 1");
		elevatorArray[1].setRealTimeFloorRequest(2);//add request for elevator 1 (second elevator) to go to the second floor
		//elevatorHandler.sendPacket();
		elevatorHandler.receivePacket();

		//break third and fourth elevator (shouldn't effect the ones with arrivals
		System.out.println("Breaking Elevator 2 with DOOR_ERROR");
		elevatorArray[2].breakElevator(DOOR_ERROR);
		//elevatorHandler.sendPacket();
		elevatorHandler.receivePacket();

		System.out.println("Breaking Elevator 3 with MOTOR_ERROR");
		elevatorArray[3].breakElevator(MOTOR_ERROR);
		//elevatorHandler.sendPacket();
		elevatorHandler.receivePacket();

		while (true) {//SHOULD BE THIS INSTEAD BUT HAVING ISSUES
		//TEMPORARY FOR LOOP BECAUSE OF INFINITE LOOP SHOWING "Elevator: 2's run() while(!hasRequest)'s while (dealWith) did not receive an expected instruction: 0"
		//for (int i=0;i<20;i++) {	
			//elevatorHandler.sendPacket();
			elevatorHandler.receivePacket();
			//}
		}

	}
}

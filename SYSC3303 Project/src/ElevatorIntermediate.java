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
import java.lang.Object;

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
		private static final int INITIALIZE=8;//for first communication with the scheduler
		private static final int UNUSED=0;// value for unused parts of data 
		private static final int DOOR_CLOSE_BY=6;//door shouldn't be open for longer than 6 seconds

	/*
	private static final byte[] ELEVATOR_INITIALIZE_PACKET_DATA={ELEVATOR_ID,0,INITIALIZE, 0,0,0,0,0};
	private static final byte[] FLOOR_INITIALIZE_PACKET_DATA={FLOOR_ID,0,INITIALIZE, 0,0,0,0,0};
	 */

	/*//FROM SCHEDULE
	public static final int EL_RECEIVEPORTNUM = 369;
	public static final int EL_SENDPORTNUM = 159;

	public static final int FL_RECEIVEPORTNUM = 488;
	public static final int FL_SENDPORTNUM = 1199;
	 */

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
//	private static byte[] sendData = new byte[8];//in Elevator Class instead
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
	public static List<byte[]> elevatorTable = Collections.synchronizedList(new ArrayList<byte[]>());

	public ElevatorIntermediate() {
		try {
			elevatorSendSocket = new DatagramSocket();
			//elevatorSendSocket.setSoTimeout(250);// sets the maximum time for the receive function to self block
			elevatorSendSocket.setSoTimeout(2);
			// elevatorReceiveSocket = new DatagramSocket();// can be any available port,
			// Scheduler will reply to the port
			// that's been received
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
		synchronized (elevatorTable) {
			while (elevatorTable.isEmpty()) {
				try {
					elevatorTable.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (elevatorTable.size() != 0) {
				try {
					System.out.println("\nSending to scheduler: " + Arrays.toString(elevatorTable.get(0)));
					elevatorSendPacket = new DatagramPacket(elevatorTable.get(0), elevatorTable.get(0).length, InetAddress.getLocalHost(),
							SENDPORTNUM);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.exit(1);
				}
				try {
					elevatorSendSocket.send(elevatorSendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				elevatorTable.clear();
				elevatorTable.notifyAll();
			}
		}

	}

	public synchronized void receivePacket() {
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
			elevatorArray[packetElement].elevatorState = schedulerInstruction;
			elevatorArray[packetElement].dealWith = true;
		}
		else if (schedulerInstruction==DOWN) {
			elevatorArray[packetElement].elevatorState = schedulerInstruction;
			elevatorArray[packetElement].dealWith = true;
		}
		else if (schedulerInstruction==STOP) {
			elevatorArray[packetElement].elevatorState = schedulerInstruction;
			elevatorArray[packetElement].dealWith = true;
		}
		else if (schedulerInstruction==HOLD) {
			elevatorArray[packetElement].elevatorState = schedulerInstruction;
			elevatorArray[packetElement].dealWith = true;
		}
		else if (schedulerInstruction==INITIALIZE) {
			//intialized=true;
			System.out.println("ElevatorIntermediate INITIALIZED");
		}
		else if(schedulerInstruction==UPDATE_DISPLAYS) {
			updateDisplay();
			/*for (int i=0;i<createNumElevators;i++) {
				elevatorArray[i].updateDisplay();
			}*/
		}
		else if(schedulerInstruction==SHUT_DOWN) {
			elevatorArray[packetElement].shutDown();
		}
		else {
			System.out.println("Elevator: "+packetElement +"; Unknown Instruction from Scheduler: "+schedulerInstruction);
		}
		
	}
	public static void updateDisplay() {
		for (int i=0;i<createNumElevators;i++) {
			elevatorArray[i].updateDisplay();
		}
	}

	public static void delay(int delayValue) {
		for (int i = 0; i < delayValue;) {
			i++;
		}
	}
	private static void elevatorInitialization() {
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
		elevatorTable.add(initializationData);

	}

	public static void main(String args[]) throws IOException {
		// 2 arguments: args[0] is the number of Elevators in the system
		ElevatorIntermediate elevatorHandler = new ElevatorIntermediate();
		createNumElevators = Integer.parseInt(args[0]);// The number of Elevators in the system is passed via
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
		elevatorInitialization();

		//INITIALIZE THE SCHEDULER
		elevatorHandler.sendPacket();
		//wait for the scheduler to have access to Elevators and Floors
		elevatorHandler.receivePacket();
		//NEEDS TO BE AFTER INITIALIZATION
		// arrays to keep track of the number of elevators, eliminates naming confusion
		elevatorArray = new Elevator[createNumElevators];
		elevatorThreadArray = new Thread[createNumElevators];

		for (int i = 0; i < createNumElevators; i++) {
			elevatorArray[i] = new Elevator(i, 0, elevatorTable, Integer.parseInt(args[i + 1])); // i names the
			// elevator, 0
			// initializes the
			// floor it
			// starts on
			elevatorThreadArray[i] = new Thread(elevatorArray[i]);
			elevatorThreadArray[i].start();
		}

		updateDisplay();
		while (true) {
			elevatorHandler.sendPacket();
			//while (elevatorTable.isEmpty()) {
			elevatorHandler.receivePacket();
			//}
		}

	}
}

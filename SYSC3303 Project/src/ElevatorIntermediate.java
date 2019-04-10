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
/**
 * Intermediate/Communication member of the elevator Subsystem.
 * Will send UDP packets created by elevators to the scheduler.
 * The instructions provided by the scheduler will go be provided to the elevators by this Class.
 * Has Arguments to pass on the number of elevators to be implemented in the system and what floors their initial request will go to. 
 * @author shaviyomarasinghe
 *
 */
public class ElevatorIntermediate {

	// UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES
	private static final byte UP = 0x01;// elevator is going up
	private static final byte DOWN = 0x02;// elevator is going down
	private static final byte STOP = 0x03;
	private static final byte HOLD = 0x04;// elevator is in hold state
	private static final byte UPDATE_DISPLAY = 0x05;
	private static final byte SHUT_DOWN = 0x06;// for shutting down a hard fault problem elevator

	private static final int ELEVATOR_ID = 21;// for identifying the packet's source as elevator
	private static final int FLOOR_ID = 69;// for identifying the packet's source as floor
	private static final int SCHEDULER_ID = 54;// for identifying the packet's source as scheduler
	private static final int DOOR_OPEN = 1;// the door is open when ==1
	private static final int DOOR_DURATION = 4;// duration that doors stay open for
	private static final int REQUEST = 1;// for identifying the packet sent to scheduler as a request
	private static final int UPDATE = 2;// for identifying the packet sent to scheduler as a status update
	private static final int MAKE_STOP = 3;//
	private static final int PLACE_ON_HOLD = 4;
	private static final int UPDATE_DISPLAYS = 5;
	private static final int INITIALIZE = 8;// for first communication with the scheduler
	private static final int UNUSED = 0;// value for unused parts of data

	private static long respondStart, respondEnd;

	/*
	 * //FROM SCHEDULE public static final int EL_RECEIVEPORTNUM = 369; public
	 * static final int EL_SENDPORTNUM = 159;
	 * 
	 * public static final int FL_RECEIVEPORTNUM = 488; public static final int
	 * FL_SENDPORTNUM = 1199;
	 */

	private static final int SENDPORTNUM = 369;// port number for sending to the scheduler 
	private static final int RECEIVEPORTNUM = 159;// port number for receiving from the scheduler

	private static DatagramPacket elevatorSendPacket, elevatorReceivePacket;// DatagramPacket initialization for sending and receiving 
	private static DatagramSocket elevatorSendSocket, elevatorReceiveSocket;// DatagramSocket initialization for sending and receiving

	// getting number of elevators in the system from parameters set
	protected static int createNumElevators;// The number of Elevators in the system is passed via argument[0]

	// arrays to keep track of the number of elevators, eliminates naming confusion. Same from the elevator threads
	protected static Elevator elevatorArray[];
	private static Thread elevatorThreadArray[];

	private byte[] requestElevator = new byte[3];
	private static boolean intialized = false;
	private byte[] recentlySent;
	private static DatagramPacket schedulerSendPacket, schedulerReceivePacket;
	private static byte initializationData[];
	/*
	 * send sockets should be allocated dynamically since the ports would be
	 * variable to the elevator or floor we have chosen
	 */

	// synchronized table that all of the elevator threads will put their requests
	// and updates upon
	public static List<byte[]> elevatorTable = Collections.synchronizedList(new ArrayList<byte[]>());

	/**
	 * 
	 */
	public ElevatorIntermediate() {
		try {
			elevatorSendSocket = new DatagramSocket();
			// elevatorReceiveSocket = new DatagramSocket(RECEIVEPORTNUM);
			// elevatorReceiveSocket.setSoTimeout(8000);// sets the maximum time for the
			// receive function to self block
			// elevatorReceiveSocket = new DatagramSocket();// can be any available port,
			// Scheduler will reply to the port
			// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}

	}

	public synchronized void sendPacket() {
		respondStart = System.nanoTime();
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
					elevatorTable.wait(1);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (elevatorTable.size() != 0) {
				try {
					System.out.println("\nSending to scheduler: " + Arrays.toString(elevatorTable.get(0)));
					elevatorSendPacket = new DatagramPacket(elevatorTable.get(0), elevatorTable.get(0).length,
							InetAddress.getLocalHost()/* InetAddress.getByName("134.117.59.126") */, SENDPORTNUM);
					recentlySent = elevatorTable.get(0);
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
		byte data[] = new byte[8];
		byte schedulerInstruction;
		byte elevatorElement;
		try {
			elevatorReceiveSocket = new DatagramSocket(RECEIVEPORTNUM);
			// elevatorReceiveSocket.setSoTimeout(2500);
			// send and receive with Scheduler.
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		elevatorReceivePacket = new DatagramPacket(data, data.length);

		// System.out.println("elevator_subsystem: Waiting for Packet.\n");

		try {
			// Block until a datagram packet is received from receiveSocket.
			System.out.println("waiting to receive");
			elevatorReceiveSocket.receive(elevatorReceivePacket);
			respondEnd = System.nanoTime();
			recentlySent = null;
			System.out.print("Received from scheduler: ");
			System.out.println(Arrays.toString(data));
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			// System.exit(1);
		}
		System.out.println(
				"It took " + (respondEnd - respondStart) + " nanoseconds to get a response from the Scheduler");
		elevatorReceiveSocket.close();

		/*
		 * ELEVATOR --> SCHEDULER (elevator or floor (elevator-21), elevator id(which
		 * elevator), FloorRequest/update, curentFloor, up or down, destFloor,
		 * instruction) (
		 */

		schedulerInstruction = data[6];
		elevatorElement = data[1];
		if (schedulerInstruction == INITIALIZE) {
			intialized = true;
		} else if (schedulerInstruction == UPDATE) {
			elevatorArray[elevatorElement].updateDisplay();
			/*
			 * for (int i = 0; i < numFloors; i++) {
			 * floorArray[i].updateDisplay(elevatorLocation, elevatorDirection); }
			 */
		} else if (schedulerInstruction == SHUT_DOWN) {
			elevatorArray[elevatorElement].shutDown();
		}
		// else if(schedulerInstruction)

		// System.out.println("elevatorArray.length: "+elevatorArray.length);
		// System.out.println("elevatorArray[0]:
		// "+elevatorArray[0]);//elevatorArray.length);
		// System.out.println("elevatorArray[1]:
		// "+elevatorArray[1]);//elevatorArray.length);

		// FOR TESTING: ADDS REQUESTS
		if (elevatorArray != null) {
			switch (data[1]) {
			case 0:
				elevatorArray[0].motorDirection = data[6];
				elevatorArray[0].dealWith = true;
				break;
			case 1:
				elevatorArray[1].motorDirection = data[6];
				elevatorArray[1].dealWith = true;

				break;
			case 2:
				elevatorArray[2].motorDirection = data[6];
				elevatorArray[2].dealWith = true;
				break;
			case 3:
				elevatorArray[3].motorDirection = data[6];
				elevatorArray[3].dealWith = true;
				break;
			}
		}

		// elevatorArray[0].openCloseDoor(data[2]);

		// send packet for scheduler to know the port this elevator is allocated
		// sendPacket = new DatagramPacket(data,
		// receivePacket.getLength(),receivePacket.getAddress(),
		// receivePacket.getPort());
		// }
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
		elevatorTable.add(initializationData);
		intialized = true;
		
	}

	public static void main(String args[]) throws IOException {
		// 2 arguments: args[0] is the number of Elevators in the system
		ElevatorIntermediate elevatorHandler = new ElevatorIntermediate();
		// for iteration 1 there will only be 1 elevator
		// getting floor numbers from parameters set
		createNumElevators = Integer.parseInt(args[0]);// The number of Elevators in the system is passed via
		// argument[0]

		// for keeping track of the port numbers, filled as they get declared
		// since we're not strictly replying to the immediate packet we can't get the
		// port numbers there
		// allocating port numbers to the variable number of elevators and floors would
		// also be difficult, just using the ones which are available
		int elevatorPortNumbers[] = new int[createNumElevators];

		elevatorInitialization();

		// INITIALIZE THE SCHEDULER
		elevatorHandler.sendPacket();
		// wait for the scheduler to have access to Elevators and Floors
		elevatorHandler.receivePacket();
		// NEEDS TO BE AFTER INITIALIZATION
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

		while (true) {
			elevatorHandler.sendPacket();
			elevatorHandler.receivePacket();

			// Synchronize Intermediate send and Receive with the Scheduler's send and
			// receive.

			// delay(1000);
		}
		/* ELEVATOR --> SCHEDULER (0, FloorRequest, cuurentFloor, 0) */

		// System.out.println("Enter floor number: ");

		// Scanner destination = new Scanner(System.in);
		// int floorRequest;
		// if (destination.nextInt() != 0) {
		// floorRequest = destination.nextInt();
		// } else {

		// }
		// destination.close();

	}
}

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

public class ElevatorIntermediate {

	/*// UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES
	private static final byte HOLD = 0x00;// elevator is in hold state
	private static final byte UP = 0x02;// elevator is going up
	private static final byte DOWN = 0x01;// elevator is going down
	private static final int ELEVATOR_ID = 21;// for identifying the packet's source as elevator
	private static final int FLOOR_ID = 69;// for identifying the packet's source as floor
	private static final int SCHEDULER_ID = 54;// for identifying the packet's source as scheduler
	private static final int DOOR_OPEN = 1;// the door is open when ==1
	private static final int DOOR_DURATION = 4;// duration that doors stay open for
	private static final int REQUEST = 1;// for identifying the packet sent to scheduler as a request
	private static final int UPDATE = 2;// for identifying the packet sent to scheduler as a status update
*/
	
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
	private static final int UNUSED = 0;// value for unused parts of data
	private static final int DOOR_CLOSE_BY = 6;// door shouldn't be open for longer than 6 seconds

	
	private static final int SENDPORTNUM = 369;// port number for sending to the scheduler
	private static final int RECEIVEPORTNUM = 159;
	
	private static DatagramPacket elevatorSendPacket, elevatorReceivePacket;
	private static DatagramSocket elevatorSendSocket, elevatorReceiveSocket;

	private static boolean firstRunTime = true;
	// for iteration 1 there will only be 1 elevator
	// getting floor numbers from parameters set
	private static int createNumElevators;// The number of Elevators in the system is passed via argument[0]

	// arrays to keep track of the number of elevators, eliminates naming confusion
	private static Elevator elevatorArray[];
	private static Thread elevatorThreadArray[];

	private byte[] requestElevator = new byte[3];

	private static DatagramPacket schedulerSendPacket, schedulerReceivePacket;
	/*
	 * send sockets should be allocated dynamically since the ports would be
	 * variable to the elevator or floor we have chosen
	 */

	// synchronized table that all of the elevator threads will put their requests
	// and updates upon
	public static List<byte[]> elevatorTable = Collections.synchronizedList(new ArrayList<byte[]>());

	public ElevatorIntermediate() {
		try {
			elevatorSendSocket = new DatagramSocket();
			elevatorSendSocket.setSoTimeout(250);// sets the maximum time for the receive function to self block
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
					elevatorSendPacket = new DatagramPacket(elevatorTable.get(0), 7, InetAddress.getLocalHost(),
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
		byte data[] = new byte[7];
		try {
			elevatorReceiveSocket = new DatagramSocket(RECEIVEPORTNUM);
			// elevatorReceiveSocket.setSoTimeout(10); Eventually we will need this to sync
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
			System.out.print("Received from scheduler: ");
			System.out.println(Arrays.toString(data));
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		elevatorReceiveSocket.close();

		/*
		 * ELEVATOR --> SCHEDULER (elevator or floor (elevator-21), elevator id(which
		 * elevator), FloorRequest/update, curentFloor, up or down, destFloor,
		 * instruction) (
		 */

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

		// arrays to keep track of the number of elevators, eliminates naming confusion
		elevatorArray = new Elevator[createNumElevators];
		elevatorThreadArray = new Thread[createNumElevators];

		// Lets create a socket for the elevator Intermediate class to communicate
		// with the scheduler. All the elevator threads will use this.

		// allocate receive packet
		byte data[] = new byte[100];
		schedulerReceivePacket = new DatagramPacket(data, data.length);

		// go for the argument passed into Elevator Intermediate, create an array for
		// elevators,
		for (int i = 0; i < createNumElevators; i++) {
			elevatorArray[i] = new Elevator(i, 0, elevatorTable, Integer.parseInt(args[i + 1])); // i names the
																									// elevator, 0
																									// initializes the
																									// floor it
			// starts on
			elevatorThreadArray[i] = new Thread(elevatorArray[i]);
			elevatorThreadArray[i].start();
			
		}
		//elevatorArray[2] = new Elevator(2, 0, elevatorTable, Integer.parseInt(args[2 + 1]));
		//elevatorThreadArray[2] = new Thread(elevatorArray[2]);
		//elevatorArray[2].sendPacket(ERROR,MOTOR_ERROR);
		elevatorArray[2].errorType=MOTOR_ERROR;

		while (true) {
//			if (!elevatorTable.isEmpty()||firstRunTime) {
			elevatorHandler.sendPacket();
//				if (firstRunTime) {
//					firstRunTime=false;
//				}
//			}
			// receive blocks
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

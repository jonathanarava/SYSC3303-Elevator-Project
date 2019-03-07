import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class FloorIntermediate {

	// UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES
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

	private static DatagramPacket floorSendPacket, floorReceivePacket;
	private static DatagramSocket floorSendSocket, floorReceiveSocket;

	// for iteration 1 there will only be 1 elevator
	// getting floor numbers from parameters set
	private static int createNumFloors;// The number of Elevators in the system is passed via argument[0]

	// arrays to keep track of the number of elevators, eliminates naming confusion
	private static Floor floorArray[];
	private static Thread floorThreadArray[];

	private byte[] requestFloor;

	private static DatagramPacket schedulerSendPacket, schedulerReceivePacket;
	/*
	 * send sockets should be allocated dynamically since the ports would be
	 * variable to the elevator or floor we have chosen
	 */
	public static final int RECEIVEPORTNUM = 23;

	public FloorIntermediate() {
		try {
			floorSendSocket = new DatagramSocket();
			// elevatorReceiveSocket = new DatagramSocket();// can be any available port,
			// Scheduler will reply to the port
			// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void packetHandler() {
		requestFloor = new byte[7];

		/* ELEVATOR --> SCHEDULER (0, FloorRequest, cuurentFloor, 0) */

		// System.out.println("Enter floor number: ");

		// Scanner destination = new Scanner(System.in);
		// int floorRequest;
		// if (destination.nextInt() != 0) {
		// floorRequest = destination.nextInt();
		// } else {

		// }
		// destination.close();

		for (int i = 0; i < createNumFloors; i++) {
			requestFloor = floorArray[i].responsePacket();
			int lengthOfByteArray = floorArray[i].responsePacket().length;

			try {
				floorSendPacket = new DatagramPacket(requestFloor, lengthOfByteArray, InetAddress.getLocalHost(), 369);
				System.out.print("I've sent\n");
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

			try {
				System.out.println("Waiting...\n"); // so we know we're waiting
				floorSendSocket.receive(schedulerReceivePacket);
				System.out.println("Got it");
			}

			catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	/*
	 * if(floorArray[0].floorRequest==2) { // =
	 * floorArray[0].responsePacket(floorArray[0].floorRequest); //int
	 * lengthOfByteArray = floorArray[i].responsePacket().length;
	 * 
	 * // allocate sockets, packets try { floorSendPacket = new
	 * DatagramPacket(requestFloor, lengthOfByteArray, InetAddress.getLocalHost(),
	 * 369); System.out.print("I've sent\n"); } catch (UnknownHostException e) {
	 * e.printStackTrace(); System.exit(1); } try {
	 * floorSendSocket.send(floorSendPacket); } catch (IOException e) {
	 * e.printStackTrace(); System.exit(1); }
	 * 
	 * try { System.out.println("Waiting...\n"); // so we know we're waiting
	 * floorSendSocket.receive(schedulerReceivePacket);
	 * System.out.println("Got it"); }
	 * 
	 * catch (IOException e) { System.out.print("IO Exception: likely:");
	 * System.out.println("Receive Socket Timed Out.\n" + e); e.printStackTrace();
	 * System.exit(1); } } else {};
	 */

	public static void main(String args[]) throws IOException {
		FloorIntermediate floorHandler = new FloorIntermediate();
		// for iteration 1 there will only be 1 elevator
		// getting floor numbers from parameters set
		createNumFloors = Integer.parseInt(args[0]);// The number of Elevators in the system is passed via argument[0]

		// for keeping track of the port numbers, filled as they get declared
		// since we're not strictly replying to the immediate packet we can't get the
		// port numbers there
		// allocating port numbers to the variable number of elevators and floors would
		// also be difficult, just using the ones which are available
		int elevatorPortNumbers[] = new int[createNumFloors];

		// arrays to keep track of the number of elevators, eliminates naming confusion
		floorArray = new Floor[createNumFloors];
		floorThreadArray = new Thread[createNumFloors];

		// Lets create a socket for the elevator Intermediate class to communicate
		// with the scheduler. All the elevator threads will use this.

		// allocate receive packet
		byte data[] = new byte[6];
		schedulerReceivePacket = new DatagramPacket(data, data.length);

		for (int i = 0; i < createNumFloors; i++) {
			floorArray[i] = new Floor(i);
			floorThreadArray[i] = new Thread(floorArray[i]);
			floorThreadArray[i].start();
		}

		while (true) {
			floorHandler.packetHandler();
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

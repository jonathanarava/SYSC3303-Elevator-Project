import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ElevatorIntermediate {
	private static DatagramPacket elevatorSendPacket, elevatorReceivePacket;
	private static DatagramSocket elevatorSendSocket;
	private static DatagramSocket elevatorReceiveSocket;

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
	public static final int RECEIVEPORTNUM = 23;

	public ElevatorIntermediate() {
		try {
			elevatorSendSocket = new DatagramSocket();
			// elevatorReceiveSocket = new DatagramSocket();// can be any available port,
			// Scheduler will reply to the port
			// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void packetHandler() {
		byte[] requestElevator = new byte[7];

		/* ELEVATOR --> SCHEDULER (0, FloorRequest, cuurentFloor, 0) */

		// System.out.println("Enter floor number: ");

		// Scanner destination = new Scanner(System.in);
		// int floorRequest;
		// if (destination.nextInt() != 0) {
		// floorRequest = destination.nextInt();
		// } else {

		// }
		// destination.close();
		requestElevator = elevatorArray[0].responsePacketRequest(1);// elevatorArray[0].floorRequest);

		// allocate sockets, packets
		try {
			System.out.println("\nSending to scheduler: " + Arrays.toString(requestElevator));
			elevatorSendPacket = new DatagramPacket(requestElevator, requestElevator.length, InetAddress.getLocalHost(),
					369);
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
	}

	public synchronized void receivePacket() {
		// SCHEDULER --> ELEVATOR (0, motorDirection, motorSpinTime, open OR close door,
		// 0)

		byte data[] = new byte[7];
		elevatorReceivePacket = new DatagramPacket(data, data.length);

		// System.out.println("elevator_subsystem: Waiting for Packet.\n");

		try {
			// Block until a datagram packet is received from receiveSocket.
			elevatorSendSocket.receive(elevatorReceivePacket);
			System.out.print("Received from scheduler: ");
			System.out.println(Arrays.toString(data));
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		elevatorArray[0].runElevator(data[6]);
		//elevatorArray[0].openCloseDoor(data[2]);

		// send packet for scheduler to know the port this elevator is allocated
		// sendPacket = new DatagramPacket(data,
		// receivePacket.getLength(),receivePacket.getAddress(),
		// receivePacket.getPort());
		// }
	}

	public static void main(String args[]) throws IOException {// 2 arguments: args[0] is the number of Elevators in the
																// system and
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
			elevatorArray[i] = new Elevator(i + 1, 1);
			elevatorThreadArray[i] = new Thread(elevatorArray[i]);
			elevatorThreadArray[i].start();
		}

		while (true) {
			elevatorHandler.packetHandler();
			elevatorHandler.receivePacket();
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

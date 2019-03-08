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
import java.util.Scanner;

public class ElevatorIntermediate {

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

	private static DatagramPacket elevatorSendPacket, elevatorReceivePacket;
	private static DatagramSocket elevatorSendReceiveSocket;

	public static int currentFloor;
	public static int destFloor;
	public static int floorRequest;

	// for iteration 1 there will only be 1 elevator
	// getting floor numbers from parameters set
	private static int createNumElevators;// The number of Elevators in the system is passed via argument[0]

	// arrays to keep track of the number of elevators, eliminates naming confusion
	private static Elevator elevatorArray[];
	private static Thread elevatorThreadArray[];
	
	public static List<byte[]> elevatorTable = Collections.synchronizedList(new ArrayList<byte[]>());

	/*
	 * send sockets should be allocated dynamically since the ports would be
	 * variable to the elevator or floor we have chosen
	 */
	public static final int RECEIVEPORTNUM = 23;

	public ElevatorIntermediate() {
		try {
			elevatorSendReceiveSocket = new DatagramSocket();
			// elevatorReceiveSocket = new DatagramSocket();// can be any available port,
			// Scheduler will reply to the port
			// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}

	public synchronized void sendPacket() throws InterruptedException {

		byte[] requestElevator = new byte[7];

		/* ELEVATOR --> SCHEDULER (0, FloorRequest, cuurentFloor, 0) */

	   //System.out.println("Enter floor number: ");

		/*Scanner destination = new Scanner(System.in);
		int floorRequest;
		if (destination.nextInt() != 0) {
		floorRequest = destination.nextInt();
	    } else {

		}
		 destination.close();*/

		requestElevator = elevatorArray[0].responsePacketRequest(1);// elevatorArray[0].floorRequest);
		currentFloor = elevatorArray[0].sensor;
		destFloor = elevatorArray[0].floorRequest;

		synchronized(elevatorTable) {
			while ((currentFloor == destFloor-1)||elevatorTable.isEmpty()) {
				elevatorTable.wait();
				break;
			}
			// allocate sockets, packets
			try {
				System.out.println("\nSending to scheduler: " + Arrays.toString(requestElevator));
				elevatorSendPacket = new DatagramPacket(elevatorTable.get(0), 7, InetAddress.getLocalHost(),
						369);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}
			try {
				elevatorSendReceiveSocket.send(elevatorSendPacket);
				elevatorTable.clear();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public synchronized void receivePacket() throws InterruptedException {
		// SCHEDULER --> ELEVATOR (0, motorDirection, motorSpinTime, open OR close door,
		// 0)

		byte data[] = new byte[7];
		elevatorReceivePacket = new DatagramPacket(data, data.length);
		synchronized(this){
			// System.out.println("elevator_subsystem: Waiting for Packet.\n");

			while (currentFloor == destFloor) {
				elevatorArray[0].openCloseDoor((byte) 1);
				wait();
				break;
			}
			try {
				// Block until a datagram packet is received from receiveSocket.
				elevatorSendReceiveSocket.receive(elevatorReceivePacket);
				System.out.print("Received from scheduler: ");
				System.out.println(Arrays.toString(data));
				notifyAll();
			} catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}
		}

		elevatorArray[0].runElevator(data[6]);
		
		switch(data[2]) {
		case 0:
			elevatorArray[0].hasRequest = true;
			elevatorArray[0].motorDirection = data[6];
			break;
		case 1:
			elevatorArray[1].hasRequest = true;
			elevatorArray[1].motorDirection = data[6];
			break;
		}
		//elevatorArray[0].openCloseDoor(data[2]);

		// send packet for scheduler to know the port this elevator is allocated
		// sendPacket = new DatagramPacket(data,
		// receivePacket.getLength(),receivePacket.getAddress(),
		// receivePacket.getPort());
		// }
	}

	public static void main(String args[]) throws IOException, InterruptedException {// 2 arguments: args[0] is the number of Elevators in the
		// system and
		ElevatorIntermediate elevatorHandler = new ElevatorIntermediate();
		// for iteration 1 there will only be 1 elevator
		// getting floor numbers from parameters set
		createNumElevators = Integer.parseInt(args[0]);// The number of Elevators in the system is passed via
		// argument[0]
		System.out.println(createNumElevators);
		// for keeping track of the port numbers, filled as they get declared
		// since we're not strictly replying to the immediate packet we can't get the
		// port numbers there
		// allocating port numbers to the variable number of elevators and floors would
		// also be difficult, just using the ones which are available

		//int elevatorPortNumbers[] = new int[createNumElevators];

		// arrays to keep track of the number of elevators, eliminates naming confusion
		elevatorArray = new Elevator[createNumElevators];
		elevatorThreadArray = new Thread[createNumElevators];

		// go for the argument passed into Elevator Intermediate, create an array for
		// elevators,
		
		floorRequest = Integer.parseInt(args[1]);// The number of Elevators in the system is passed via
		elevatorArray[0] = new Elevator(0, floorRequest, elevatorTable);
		
		int floorRequest1 = Integer.parseInt(args[2]);// The number of Elevators in the system is passed via
		System.out.println(floorRequest1);
		elevatorArray[1] = new Elevator(1, floorRequest1, elevatorTable);
		
		for (int i = 0; i < createNumElevators; i++) {
/*			floorRequest = Integer.parseInt(args[i+1]);// The number of Elevators in the system is passed via
			elevatorArray[i] = new Elevator(i, floorRequest);
			floorRequest = Integer.parseInt(args[i+2]);// The number of Elevators in the system is passed via
			elevatorArray[i] = new Elevator(i, floorRequest); */// i names the elevator, 0 initializes the floor it starts on
			elevatorThreadArray[i] = new Thread(elevatorArray[i]);
			elevatorThreadArray[i].start();
		}
		//
		while (true) {
			elevatorHandler.sendPacket();
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
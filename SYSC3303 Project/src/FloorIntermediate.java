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
	//

	private static DatagramPacket floorSendPacket, floorReceivePacket;
	private static DatagramSocket floorSendReceiveSocket;

	// for iteration 1 there will only be 1 elevator
	// getting floor numbers from parameters set
	private static int createNumFloors;// The number of Elevators in the system is passed via argument[0]

	// arrays to keep track of the number of elevators, eliminates naming confusion
	private byte[] requestFloor;
	
	/*
	 * send sockets should be allocated dynamically since the ports would be
	 * variable to the elevator or floor we have chosen
	 */
	public static final int RECEIVEPORTNUM = 23;

	public FloorIntermediate() {
		try {
			floorSendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void sendPacket(byte[] requestPacket) {
		byte[] requestFloor = new byte[7];
		int lengthOfByteArray = requestPacket.length;
		
		try {
			floorSendPacket = new DatagramPacket(requestFloor, lengthOfByteArray, InetAddress.getLocalHost(), 369);
			System.out.print("I've sent\n");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		try {
			floorSendReceiveSocket.send(floorSendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void receivePacket() {
		try {
			System.out.println("Waiting...\n"); // so we know we're waiting
			floorSendReceiveSocket.receive(floorReceivePacket);
			System.out.println("Got it");
		}

		catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
	}


	public static void main(String args[]) throws IOException {
		
		// for iteration 1 there will only be 1 elevator
		// getting floor numbers from parameters set
		int createNumFloors = Integer.parseInt(args[0]);// The number of Elevators in the system is passed via argument[0]
		FloorIntermediate floorHandler = new FloorIntermediate();
		Floor x = new Floor(createNumFloors);
		boolean hasRequest = false;


		while (true) {
			floorHandler.receivePacket();
			if(hasRequest) {
				floorHandler.sendPacket(null);
			}

		}
	}
}

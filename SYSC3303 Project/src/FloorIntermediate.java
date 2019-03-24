import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class FloorIntermediate {

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
	//

	private static DatagramPacket floorSendPacket, floorReceivePacket;
	private static DatagramSocket floorSendReceiveSocket;


	// arrays to keep track of the number of elevators, eliminates naming confusion
	private static int name;
	private static boolean hasRequest;
	private static int up_or_down;
	/*
	 * send sockets should be allocated dynamically since the ports would be
	 * variable to the elevator or floor we have chosen
	 */
	public static final int SENDPORTNUM = 488;

	public FloorIntermediate() {
		try {
			floorSendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void sendPacket(byte[] requestPacket) {
		int lengthOfByteArray = requestPacket.length;
		System.out.println("Request from Floor " + requestPacket[1] + ": " + Arrays.toString(requestPacket));
		try {
			
			floorSendPacket = new DatagramPacket(requestPacket, lengthOfByteArray, InetAddress.getLocalHost(), SENDPORTNUM);
			
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
		byte data[] = new byte[7];
		floorReceivePacket = new DatagramPacket(data, data.length);
		try {
			System.out.println("Waiting...\n"); // so we know we're waiting
			floorSendReceiveSocket.receive(floorReceivePacket);
			System.out.println("Received from scheduler --> " + Arrays.toString(data));
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
		Floor floor = new Floor(createNumFloors);
		
		floor.fileReader("M://hello.txt");
		
		while (true) {
			if(floor.fileRequests.isEmpty()) {
				hasRequest = false;
			} else {
				hasRequest = true;
				String command = floor.fileRequests.remove(0);
				String segment[] = command.split(" ");
				name = Integer.parseInt(segment[1]);
				if(segment[2].equals("Up")) {
					up_or_down = UP;
				} else if(segment[2].equals("Down")) {
					up_or_down = DOWN;
				}
			}
			
			if(hasRequest == true) {
				floorHandler.sendPacket(floor.responsePacket(name, up_or_down));
			} else {
				floorHandler.sendPacket(floor.responsePacket(0, 0));
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			floorHandler.receivePacket();
		}
	}
}

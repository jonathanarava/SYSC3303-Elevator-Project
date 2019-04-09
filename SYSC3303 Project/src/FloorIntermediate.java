import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class FloorIntermediate extends Thread {

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
	private static int destination;
	/*
	 * send sockets should be allocated dynamically since the ports would be
	 * variable to the elevator or floor we have chosen
	 */
	public static final int SENDPORTNUM = 488;
	private static int nameOfElevator;
	public byte ID;
	private byte instruction;
	private boolean semaphoreOpen = false;
	private int elevatorID;
	private boolean semaphoreOpen1;

	public FloorIntermediate(int ID) {
		elevatorID = ID;
	}

	public synchronized void sendPacket(byte[] requestPacket) {
		int lengthOfByteArray = requestPacket.length;
		System.out.println("Request from Floor " + requestPacket[1] + ": " + Arrays.toString(requestPacket));
		try {
			InetAddress address = InetAddress.getByName("134.117.59.107");
			floorSendPacket = new DatagramPacket(requestPacket, lengthOfByteArray,address, SENDPORTNUM);
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
	
	public synchronized static byte[] receivePacket() {
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
		return data;
	}
	
	public void openDoor(int ID, int i) {
		String msg;
		if (instruction == DOOR_OPEN) {
			msg = "Doors are open.";
			System.out.println(currentThread() + msg + " for Elevator " + nameOfElevator);
			try {
				int j = 4;
				while (j != 0) {
					System.out.format("Seconds until Floor %d door closes for ELEVATOR %d: %d second \n", ID, i, j);
					j--;
					Thread.sleep(1000);
				}
			sendPacket(Floor.responsePacket(ID, HOLD));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			msg = "Doors are closed.";
			System.out.println(msg);
		}
	}
	
	public void run() {
		while(true) {
			if(semaphoreOpen) {
				for(int i = 0; i < Floor.floorsMade.length; i++) {
					if(ID == Floor.floorsMade[i]) {
						this.openDoor(ID, elevatorID);
						sendPacket(Floor.responsePacket(ID, 0));
						this.semaphoreOpen = false;
						break;
					}
				}
			}else if (semaphoreOpen1) {
				//System.out.println("here");
				for(int i = 0; i < Floor.floorsMade.length; i++) {
					if(ID == Floor.floorsMade[i]) {
						sendPacket(Floor.responsePacket(ID, 0));
						this.openDoor(ID, elevatorID);
						this.semaphoreOpen1 = false;
						break;
					}
				}
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String args[]) throws IOException {
		try {
			floorSendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
		// for iteration 1 there will only be 1 elevator
		// getting floor numbers from parameters set
		int createNumFloors = Integer.parseInt(args[0]);// The number of Elevators in the system is passed via argument[0]

		Floor floor = new Floor(createNumFloors);
		
		floor.fileReader("M://hello.txt");
		System.out.println("FIRST REQUEST  --> " + floor.fileRequests.get(0));
		System.out.println("Second REQUEST  --> " + floor.fileRequests.get(1));
		//byte[] responseByteArray = new byte[] {69,0,0,0,0,0,0}; // test packet

		FloorIntermediate F1 = new FloorIntermediate(0);
		FloorIntermediate F2 = new FloorIntermediate(1);
		//F1.sendPacket(responseByteArray);
		while(true) {
			if(floor.fileRequests.isEmpty()) {
				hasRequest = false;
				break;
			} else {
				hasRequest = true;
				String command = floor.fileRequests.remove(0);
				String segment[] = command.split(" ");
				name = Integer.parseInt(segment[1]);
				if(segment[2].equals("Up")) {
					up_or_down = UP;
				} else if(segment[2].equals("Down")) {
					up_or_down = DOWN;
					System.out.println("here down request");
				}
			}

			if(hasRequest == true) {
				F1.sendPacket(Floor.responsePacket(name, up_or_down));
			}
		}
		F1.sendPacket(Floor.responsePacket(0, 0));
		
		F1.start();
		F2.start();
		
		byte [] received = new byte[7];
		while (true) {
/*			if(floor.fileRequests.isEmpty()) {
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
				F1.sendPacket(Floor.responsePacket(name, up_or_down));
			} */
			
			received = receivePacket();
			int eleID = received[2];
			if(eleID == 0) {
				F1.ID = received[1];
				F1.elevatorID = received[2];
				F1.instruction = received[6];
				F1.semaphoreOpen  = true;
			} else if (eleID == 1) {
				F2.ID = received[1];
				F2.elevatorID = received[2];
				F2.instruction = received[6];
				F2.semaphoreOpen1  = true;

			}
		}
	}
}

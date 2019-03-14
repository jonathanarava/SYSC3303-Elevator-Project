import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;

public class Elevator extends Thread {
	
	/* Packet declaration to send and receive data */
	private static DatagramPacket ElevatorSendPacket, ElevatorReceivePacket;
	
	/* DatagramSocket declaration */
	private static DatagramSocket ElevatorSendRecieveReceiveSocket;

	/* UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES*/
	public static final byte HOLD = 0x00;// Elevator is in hold state
	public static final byte UP = 0x02;// Elevator is going up
	public static final byte DOWN = 0x01;// Elevator is going down
	public static final int Elevator_ID = 21;// for identifying the packet's source as Elevator
	public static final int DOOR_OPEN = 1;// the door is open when ==1
	public static final int DOOR_DURATION = 4;// duration that doors stay open for
	public static final int REQUEST = 1;// for identifying the packet sent to scheduler as a request
	public static final int UPDATE = 2;// for identifying the packet sent to scheduler as a status update
	
	/* Variables used in this class*/
	private static int sensor;
	private int nameOfElevator;
	private static byte toDoID;
	private static byte instruction;
<<<<<<< HEAD
	//private boolean isInterrupt = false;
=======
	private byte motorDirection;
>>>>>>> parent of 69f91d0... commit

	/* Table to synchronize threads */
	public LinkedList<byte[]> ElevatorTable = new LinkedList<byte[]>();

	public Elevator(int nameOfElevator, int initialFloor,  LinkedList<byte[]> ElevatorTable) {
		this.nameOfElevator = nameOfElevator;
		sensor = initialFloor;
		this.ElevatorTable = ElevatorTable;
	}
	
	public static int currentFloor(int floorSensor) { // method to initialize where the Elevator starts
		sensor = floorSensor;
		return sensor;
	}
	
	public int runElevator(byte motorDirection) {
		if (motorDirection == UP || motorDirection == DOWN) {
<<<<<<< HEAD
			//try {
				System.out.println("current floor of " + nameOfElevator + ": " + sensor); // sensor = current floor
				//Thread.sleep(3000);
=======
			try {
				System.out.println("current floor: " + sensor); // sensor = current floor
				Thread.sleep(3000);
>>>>>>> parent of 69f91d0... commit
				if (motorDirection == UP) {
					System.out.println("Elevator going up");
					sensor++; // increment the floor
					currentFloor(sensor); // updates the current floor
				} else if (motorDirection == DOWN) {
					System.out.println("Elevator going down");
					sensor--; // decrements the floor
					currentFloor(sensor); // updates the current floor
				}
/*			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		} else if (motorDirection == HOLD) {
			currentFloor(sensor); // updates current floor - in this case nothing changes
		}
		System.out.println("current floor: " + sensor); // prints out the current floor - in this case destination floor
		return currentFloor(sensor); // returns and updates the final current of the floor - in this case destination floor
	}
	
	public byte[] responsePacketRequest(int requestUpdate, int floorRequest) {

		/*
		 * Elevator --> SCHEDULER (Elevator or floor (Elevator-21), Elevator id(which
		 * Elevator), FloorRequest/update, curentFloor, up or down, destFloor,
		 * instruction) (
		 */
		// creates the byte array according to the required format

		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(Elevator_ID); // Elevator
		requestElevator.write(nameOfElevator); // Elevator id

		// request/ update
		if (requestUpdate == REQUEST) {
			requestElevator.write(REQUEST); // request/
			requestElevator.write((byte) currentFloor(sensor)); // current floor
			requestElevator.write(0); // up or down
			requestElevator.write(floorRequest); // dest floor
			requestElevator.write(0); // instruction
		} else if (requestUpdate == UPDATE) {
			requestElevator.write(UPDATE); // update
			requestElevator.write((byte) currentFloor(sensor)); // current floor
			requestElevator.write(0); // up or down
			requestElevator.write(floorRequest); // dest floor
			requestElevator.write(0); // instruction
		}
		return requestElevator.toByteArray();
	}

	public String openCloseDoor(byte door) {
		String msg;
		if (door == DOOR_OPEN) {
			msg = "Doors are open.";
			System.out.println(msg);
			try {
				int i = 4;
				while (i != 0) {
					System.out.format("Seconds until Elevator door closes: %d second \n", i);
					i--;
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			msg = "Doors are closed.";
			System.out.println(msg);
		}
		return msg;
	}
	
	public synchronized void sendPacket(byte[] toSend) throws InterruptedException {

		byte[] data = new byte[7];
		data = toSend;
		
		try {
			System.out.println("\nSending to scheduler from Elevator "+ data[1] + ":" + Arrays.toString(data));
			ElevatorSendPacket = new DatagramPacket(data, 7, InetAddress.getLocalHost(), 369);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		try {
			ElevatorSendRecieveReceiveSocket.send(ElevatorSendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public synchronized void receivePacket() throws InterruptedException {
		
		byte data[] = new byte[7];
		ElevatorReceivePacket = new DatagramPacket(data, data.length);
		
		try {
			// Block until a datagram packet is received from receiveSocket.
			ElevatorSendRecieveReceiveSocket.receive(ElevatorReceivePacket);
			System.out.print("Received from scheduler: ");
			System.out.println(Arrays.toString(data));
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		
		ElevatorTable.add(data);
	}

	
	public void run() {
		synchronized(ElevatorTable) {
			while(ElevatorTable.size() == 0) {
				try {
<<<<<<< HEAD
					ElevatorTable.wait(1);
=======
					ElevatorTable.wait();
>>>>>>> parent of 69f91d0... commit
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			byte data[] = new byte[7];

			data = ElevatorTable.get(0);
			toDoID = data[1];
			instruction = data[6];

			if(toDoID == nameOfElevator) {
				if(instruction == 2 || instruction == 1) {
<<<<<<< HEAD
					runElevator(instruction);
=======
					runElevator(motorDirection);
>>>>>>> parent of 69f91d0... commit
					try {
						sendPacket(responsePacketRequest(2,0));
					} catch (InterruptedException e) {
						e.printStackTrace();
<<<<<<< HEAD
					}
					//isInterrupt = true;				
=======
					}				
>>>>>>> parent of 69f91d0... commit
				} else if (instruction == 3 || instruction == 4 || instruction == 5) {
					openCloseDoor((byte)DOOR_OPEN);
				}
				ElevatorTable.clear();
				ElevatorTable.notifyAll();
			}			
		}
	}
	
	
	public static void main(String args[]) throws InterruptedException {
		
		int initialFloor0 = Integer.parseInt(args[0]);	// The number of Elevators in the system is passed via
		int initialFloor1 = Integer.parseInt(args[1]);	
		
		LinkedList<byte[]> ElevatorTable = new LinkedList<byte[]>();
		Elevator Elevator0 = new Elevator(0, initialFloor0, ElevatorTable);
		Elevator Elevator1 = new Elevator(1, initialFloor1, ElevatorTable);
		
		try {
			ElevatorSendRecieveReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {// if Socket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
		
		Elevator0.ElevatorTable.add(0,Elevator0.responsePacketRequest(1, 6));
		Elevator1.ElevatorTable.add(1,Elevator1.responsePacketRequest(1, 4));
		
		Elevator0.sendPacket(ElevatorTable.get(0));
		Elevator1.sendPacket(ElevatorTable.get(1));
		
		Elevator0.start();
		Elevator1.start();
		
		while(true) {
			Elevator0.receivePacket();
<<<<<<< HEAD
/*			if(Elevator0.isInterrupt == true) {
				System.out.println("here");
				Elevator0.sendPacket(Elevator0.responsePacketRequest(2,0));
				Elevator0.isInterrupt = false;
			}
			if(Elevator1.isInterrupt == true) {
				System.out.println("here");
				Elevator1.sendPacket(Elevator1.responsePacketRequest(2,0));
				Elevator1.isInterrupt = false;
			}*/
=======
>>>>>>> parent of 69f91d0... commit
		}
	}
}

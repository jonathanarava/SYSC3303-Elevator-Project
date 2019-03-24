import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.lang.*;

public class Scheduler extends Thread {

	// Packets and sockets required to connect with the Elevator and Floor class

	public static DatagramSocket schedulerSocketSendReceiveElevator, schedulerSocketSendReceiveFloor;
	public static DatagramPacket schedulerElevatorSendPacket, schedulerElevatorReceivePacket, schedulerFloorSendPacket,
			schedulerFloorReceivePacket;

	public static int PORTNUM = 69;
	// Variables

	public static byte data[] = new byte[7];
	public static byte dataFloor[] = new byte[7];
	public static int elevatorOrFloor;
	public static int elevatorOrFloorID;
	public static int requestOrUpdate;
	public static int currentFloor;
	public static int upOrDown;
	public static int destFloor;

	private static final byte HOLD = 0x00;// elevator is in hold state
	private static final byte UP = 0x01;// elevator is going up
	private static final byte DOWN = 0x02;// elevator is going down
	private static final byte STOP = 0x04;// elevator has finished all requests. 

	// number of elevators and floors. Can change here!
	public static int numElevators = 2;
	public static int numFloors = 15;

	// lists to keep track of what requests need to be handled

	//private static LinkedList<Thread> queue = new LinkedList<Thread>();
	private static LinkedList<Integer> upQueue1 = new LinkedList<Integer>();
	private static LinkedList<Integer> downQueue1 = new LinkedList<Integer>();
	private static LinkedList<Integer> upQueue2 = new LinkedList<Integer>();
	private static LinkedList<Integer> downQueue2 = new LinkedList<Integer>();
	//private static int[] allDestinationFloors = new int[queue.size()];
	public static Object obj = new Object();
	public static int limit = numFloors * numElevators;
	//private static Thread newRequest;

	public static int direction;

	public Scheduler() {
		try {
			schedulerSocketSendReceiveElevator = new DatagramSocket(369);
			schedulerSocketSendReceiveFloor = new DatagramSocket();// can be any available port, Scheduler will reply
			// to the port
			// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}

	public static void elevatorReceivePacket() {

		/* ELEVATOR RECEIVING PACKET HERE */
		schedulerElevatorReceivePacket = new DatagramPacket(data, data.length);
		// System.out.println("Server: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			System.out.println("waiting");
			schedulerSocketSendReceiveElevator.receive(schedulerElevatorReceivePacket);
			System.out.println("Request from elevator" + data[1] + ": " + Arrays.toString(data));

			// schedulerSocketReceiveElevator.close();
			// schedulerSocketSendReceiveElevator.close()

		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		/* Separating byte array received */

		elevatorOrFloor = data[0];
		elevatorOrFloorID = data[1];
		requestOrUpdate = data[2];
		currentFloor = data[3];
		upOrDown = data[4];
		destFloor = data[5];
	}

	public static void elevatorSendPacket(byte[] responseByteArray) {
		/* SENDING ELEVATOR PACKET HERE */

		/*
		 * byte[] responseByteArray = new byte[7];
		 * 
		 * responseByteArray = responseByteArray;
		 */
		System.out.println("Response to elevator " + responseByteArray[1] + ": " + Arrays.toString(responseByteArray) + "\n");
		schedulerElevatorSendPacket = new DatagramPacket(responseByteArray, responseByteArray.length,
				schedulerElevatorReceivePacket.getAddress(), schedulerElevatorReceivePacket.getPort());
		try {
			schedulerSocketSendReceiveFloor.send(schedulerElevatorSendPacket);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void floorReceivePacket() {

		/* FLOOR RECEIVING PACKET HERE */
		schedulerElevatorReceivePacket = new DatagramPacket(dataFloor, dataFloor.length);

		// Block until a datagram packet is received from receiveSocket.
		try {
			System.out.println("waiting");
			schedulerSocketSendReceiveFloor.receive(schedulerFloorReceivePacket);
			System.out.println("Request from elevator: " + Arrays.toString(data));
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		/* Separating byte array received */
		elevatorOrFloor = data[0];
		elevatorOrFloorID = data[1];
		requestOrUpdate = data[2];
		currentFloor = data[3];
		upOrDown = data[4];
		destFloor = data[5];
	}

	public static void floorSendPacket(byte[] responseByteArray) {
		/* FLOOR SENDING PACKET HERE */

		/*
		 * byte[] responseByteArray = new byte[5];
		 * 
		 * responseByteArray = responsePacket(currentFloor, destFloor);
		 */
		System.out.println("Response to Floor " + data[1] + ": " + Arrays.toString(responseByteArray) + "\n");
		try {
			schedulerFloorSendPacket = new DatagramPacket(responseByteArray, responseByteArray.length,
					InetAddress.getLocalHost(), PORTNUM);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			schedulerSocketSendReceiveFloor.send(schedulerFloorSendPacket);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static byte[] responsePacket(int ID, int currentFloor1, int floorRequest1) {

		// creates the byte array according to the required format
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(69);
		requestElevator.write(ID);
		requestElevator.write(0);
		requestElevator.write(0);
		requestElevator.write(0);
		requestElevator.write(0);

		if(floorRequest1 == -1) {
			requestElevator.write(STOP);
		} else if ((floorRequest1 - currentFloor1) < 0) {
			requestElevator.write(DOWN); // downwards
		} else if ((floorRequest1 - currentFloor1) > 0) {
			requestElevator.write(UP); // upwards
		} else if((floorRequest1 - currentFloor1) == 0) {
			requestElevator.write(HOLD); // motorDirection
		}

		// 0,2,0,1,0 (0, direction, openClose, motorSpin,0)
		return requestElevator.toByteArray();
	}

	/* Splitting the packet to determine if its for a floor or elevator request */

	public void packetDealer() {
		if (elevatorOrFloor == 21) {
			if (elevatorOrFloorID == 0) {
				if (requestOrUpdate == 1) {
					if (destFloor - currentFloor > 0) {
						System.out.println("here adding to queue 1");
						addToUpQueue(upQueue1,0);
					} else if (destFloor - currentFloor < 0) {
						addToDownQueue(downQueue1);
					}
				}
			} else if (elevatorOrFloorID == 1) {
				if (requestOrUpdate == 1) {
					if (destFloor - currentFloor > 0) {
						System.out.println("here adding to queue 2");
						addToUpQueue(upQueue2,1);
						// goingUpList(upQueue2, destFloor);
					} else if (destFloor - currentFloor < 0) {
						addToDownQueue(downQueue2);
						// goingDownList(downQueue2, destFloor);
					}
				}
			}
		} else if (elevatorOrFloor == 69) {
			floorPacketHandler();
		}
	}

	public void addToUpQueue(LinkedList<Integer> upQueue, int ID) {
		for (int i = 0; i <= upQueue.size(); i++) {
			if (ID == 0) {
				if (upQueue1.isEmpty()) {
					upQueue1.addFirst(destFloor);
					break;
				}
				if ((destFloor <= upQueue.get(i))) {
					upQueue1.add(i, destFloor);
					break;
				} else if (i == upQueue.size()) {
					upQueue1.addLast(destFloor);
					break;
				}
			} else if (ID == 1) {
				if (upQueue2.isEmpty()) {
					upQueue2.addFirst(destFloor);
					break;
				}
				if ((destFloor <= upQueue.get(i))) {
					upQueue2.add(i, destFloor);
					break;
				} else if (i == upQueue.size()) {
					upQueue2.addLast(destFloor);
					break;
				}
			}
		}
	}

	public void addToDownQueue(LinkedList<Integer> downQueue) {
		for (int i = 0; i <= downQueue.size(); i++) {
			if (downQueue.isEmpty()) {
				downQueue.addFirst(destFloor);
				break;
			}
			if ((destFloor <= downQueue.get(i))) {
				downQueue.add(i, destFloor);
				break;
			} else if (i == downQueue.size()) {
				downQueue.addLast(destFloor);
				break;
			}
		}
	}

	/*
	 * private synchronized void goingUpList(LinkedList<Integer> queueType, int
	 * destFloor2) { synchronized(queueType) { if(ID == 1) {
	 * 
	 * } else if (ID == 2) {
	 * 
	 * } } }
	 * 
	 * private synchronized void goingDownList(LinkedList<Integer> queueType, int
	 * destFloor2) { synchronized(queueType) { while(queueType.size()==0) { try {
	 * queueType.wait(); } catch (InterruptedException e) { e.printStackTrace(); } }
	 * } }
	 * 
	 * private synchronized void elevatorPacketHandler(int ID) {
	 * 
	 * }
	 */

	public final void interruptThread(Thread t) {
		t.interrupt();
	}

	private static void floorPacketHandler() {
	}

	public static int Direction(int destFloor, int currentFloor) {
		if (destFloor - currentFloor < 0 && upQueue1.isEmpty() && upQueue2.isEmpty()) {
			direction = DOWN;
		}
		if (destFloor - currentFloor > 0 && downQueue1.isEmpty() && downQueue2.isEmpty()) {
			direction = UP;
		}
		if (destFloor - currentFloor == 0) {
			direction = HOLD;
		}
		return direction;
	}
	
	public static void schedulingAlgo() {
		if (!(upQueue1.isEmpty()) && elevatorOrFloorID == 0) {
			int first = upQueue1.getFirst();
			byte[] responseByteArray = responsePacket(0, currentFloor, first);
			if (currentFloor == first) {
				upQueue1.removeFirst();
				if(upQueue1.isEmpty()) {
					responseByteArray = responsePacket(0, currentFloor, -1);
				}
			}
			Scheduler.elevatorSendPacket(responseByteArray);
		}

		if (!(upQueue2.isEmpty()) && elevatorOrFloorID == 1) {
			int first = upQueue2.getFirst();
			byte[] responseByteArray = responsePacket(1, currentFloor, first);
			if (currentFloor == first) {
				upQueue2.removeFirst();
				if(upQueue2.isEmpty()) {
					responseByteArray = responsePacket(1, currentFloor, -1);
				}
			}
			Scheduler.elevatorSendPacket(responseByteArray);
		}

		if (!(downQueue1.isEmpty()) && elevatorOrFloorID == 0) {
			int first = downQueue1.getFirst();
			byte[] responseByteArray = responsePacket(0, currentFloor, first);
			if (currentFloor == first) {
				downQueue1.removeFirst();
			}
			Scheduler.elevatorSendPacket(responseByteArray);
		}

		if (!(downQueue2.isEmpty()) && elevatorOrFloorID == 1) {
			int first = downQueue2.getFirst();
			byte[] responseByteArray = responsePacket(1, currentFloor, first);
			if (currentFloor == first) {
				downQueue2.removeFirst();
			}
			Scheduler.elevatorSendPacket(responseByteArray);
		}
	}

	public static void main(String args[]) throws InterruptedException {

		Scheduler packet = new Scheduler();

/*		ArrayList<LinkedList<Integer>> x = new ArrayList<>();
		x.add(0, upQueue1);
		x.add(1, upQueue2);
		x.add(2, downQueue1);
		x.add(3, downQueue2);
*/
		
		for (;;) {
			Scheduler.elevatorReceivePacket(); // connection to elevator class

			if (requestOrUpdate == 1) {
				packet.packetDealer();
				Direction(destFloor, currentFloor);
			}
			
			schedulingAlgo();
		}
	}
}


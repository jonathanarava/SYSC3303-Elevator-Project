import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

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
	private static final byte UP = 0x02;// elevator is going up
	private static final byte DOWN = 0x01;// elevator is going down

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
			e.printStackTrace();
		}

		try {
			schedulerSocketSendReceiveFloor.send(schedulerFloorSendPacket);
		} catch (IOException e1) {
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

		if ((floorRequest1 - currentFloor1) < 0) {
			requestElevator.write(1); // downwards
		} else if ((floorRequest1 - currentFloor1) > 0) {
			requestElevator.write(2); // upwards
		} else {
			requestElevator.write(0); // motorDirection
		}

		// 0,2,0,1,0 (0, direction, openClose, motorSpin,0)
		return requestElevator.toByteArray();
	}

	/* Splitting the packet to determine if its for a floor or elevator request */

	public void packetDealer() {
		if (elevatorOrFloor == 21) {
			if (elevatorOrFloorID == 0) {
				//if (requestOrUpdate == 1) {
					if (destFloor - currentFloor > 0) {
						addToUpQueue(upQueue1);
					} else if (destFloor - currentFloor < 0) {
						addToDownQueue(downQueue1);
					}
				//}
			} else if (elevatorOrFloorID == 1) {
				//if (requestOrUpdate == 1) {
					if (destFloor - currentFloor > 0) {
						addToUpQueue(upQueue2);
						// goingUpList(upQueue2, destFloor);
					} else if (destFloor - currentFloor < 0) {
						addToDownQueue(downQueue2);
						// goingDownList(downQueue2, destFloor);
					}
				//}
			}
		} else if (elevatorOrFloor == 69) {
			floorPacketHandler();
		}
	}

	public void addToUpQueue(LinkedList<Integer> upQueue) {
		for (int i = 0; i <= upQueue.size(); i++) {
			if (upQueue.isEmpty()) {
				upQueue.addFirst(destFloor);
				break;
			}
			if ((destFloor <= upQueue.get(i))) {
				upQueue.add(i, destFloor);
				break;
			} else if (i == upQueue.size()) {
				upQueue.addLast(destFloor);
				break;
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


	private static void floorPacketHandler() {
	}

	public int Direction(int destFloor, int currentFloor) {
		if (destFloor - currentFloor < 0) {
			direction = DOWN;
		}
		if (destFloor - currentFloor > 0) {
			direction = UP;
		}
		if (destFloor - currentFloor == 0) {
			direction = HOLD;
		}
		return direction;
	}

	public static void main(String args[]) throws InterruptedException {

		Scheduler packet = new Scheduler();

		ArrayList<LinkedList<Integer>> allQueues = new ArrayList<>();
		allQueues.add(0, upQueue1);
		allQueues.add(1, upQueue2);
		allQueues.add(2, downQueue1);

		allQueues.add(3, downQueue2);

		for (;;) {
			Scheduler.elevatorReceivePacket(); // connection to elevator class
			

			if (requestOrUpdate == 1) {
				packet.packetDealer();
				packet.Direction(destFloor, currentFloor);
			} else if (requestOrUpdate == 2) {
				packet.updateElevator();
			}
			
			//System.out.println("ELEVATOR ID --> " +elevatorOrFloorID);
			byte[] responseByteArray;
		for (int i = 0; i < 2; i++) {
				//System.out.println(i);
				switch (direction) {

				case UP:
					if (!(allQueues.get(i).isEmpty()) && elevatorOrFloorID==0) {
						LinkedList<Integer> firstUpRequest = allQueues.get(i);
						int first = firstUpRequest.getFirst();
						//System.out.println(ElevatorCurrent[i]);
						responseByteArray = responsePacket(i, ElevatorCurrent[i], first);
						if (ElevatorCurrent[i] == first && i==0 ) {
							upQueue1.removeFirst();
							System.out.println("here");
						}
						Scheduler.elevatorSendPacket(responseByteArray);
						
					} else if(!(allQueues.get(i).isEmpty()) && elevatorOrFloorID==1) {
						LinkedList<Integer> firstUpRequest = allQueues.get(i);
						int first = firstUpRequest.getFirst();
						//System.out.println(ElevatorCurrent[i]);
						responseByteArray = responsePacket(i, ElevatorCurrent[i], first);
						if (ElevatorCurrent[i] == first && i==1 ) {
							upQueue2.removeFirst();
						}
						Scheduler.elevatorSendPacket(responseByteArray);
					}

					break;
				case DOWN:
					if (!(allQueues.get(i + 2).isEmpty())) {
						LinkedList<Integer> firstDownRequest = allQueues.get(i + 2);
						int first1 = firstDownRequest.getFirst();
						//System.out.println(ElevatorCurrent[i]);
						byte[] responseByteArrayd1 = responsePacket(i, ElevatorCurrent[i], first1);
						if (ElevatorCurrent[i+2] == (first1-1) && i ==2) {
							downQueue1.removeFirst();
						}
						if (ElevatorCurrent[i+2] == first1 && i ==3) {
							downQueue2.removeFirst();
						}
						Scheduler.elevatorSendPacket(responseByteArrayd1);
					}
					break;
				default:
					break;
				}
			}
		}
	}

	public static int ElevatorCurrent[] = new int[2];
	private void updateElevator() {
		if(elevatorOrFloorID == 0) {
			ElevatorCurrent[0] = currentFloor;
		} else if(elevatorOrFloorID == 1) {
			ElevatorCurrent[1] = currentFloor;
		}
	}
}


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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

	public static int elevatorOrFloorID1;
	public static int requestOrUpdate1;
	public static int currentFloor1;
	public static int upOrDown1;
	public static int destFloor1;

	private static final byte HOLD = 0x00;// elevator is in hold state
	private static final byte UP = 0x01;// elevator is going up
	private static final byte DOWN = 0x02;// elevator is going down
	private static final byte STOP = 0x04;// elevator has finished all requests. 
	private static final byte ERROR = 0x05;// invalid packet. error

	// number of elevators and floors. Can change here!
	public static int numElevators = 2;
	public static int numFloors = 15;

	// lists to keep track of what requests need to be handled

	private static LinkedList<Integer> upQueue1 = new LinkedList<Integer>();
	private static LinkedList<Integer> downQueue1 = new LinkedList<Integer>();
	private static LinkedList<Integer> upQueue2 = new LinkedList<Integer>();
	private static LinkedList<Integer> downQueue2 = new LinkedList<Integer>();
	private static LinkedList<Integer> upWaitQueue = new LinkedList<Integer>();
	private static LinkedList<Integer> downWaitQueue = new LinkedList<Integer>();
	

	//private static int[] allDestinationFloors = new int[queue.size()];
	public static Object obj = new Object();
	public static int limit = numFloors * numElevators;
	//private static Thread newRequest;

	public static LinkedList<Integer> direction = new LinkedList<Integer>();
	private static int ele0;
	private static int ele1;
	private boolean semaphoreRemove0 = false;
	private boolean semaphoreRemove1 = false;
	
	private static boolean semaWAIT = false;
	
	public Scheduler() {
		try {
			schedulerSocketSendReceiveElevator = new DatagramSocket(369);
			schedulerSocketSendReceiveFloor = new DatagramSocket(488);// can be any available port, Scheduler will reply
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
		System.out.println("Response to elevator " + responseByteArray[1] + ": " + Arrays.toString(responseByteArray) + "\n");
		try {
			InetAddress address = InetAddress.getByName("134.117.59.127");
			schedulerElevatorSendPacket = new DatagramPacket(responseByteArray, responseByteArray.length,
					address, schedulerElevatorReceivePacket.getPort());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			schedulerSocketSendReceiveElevator.send(schedulerElevatorSendPacket);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void floorReceivePacket() {

		/* FLOOR RECEIVING PACKET HERE */
		schedulerFloorReceivePacket = new DatagramPacket(dataFloor, dataFloor.length);

		// Block until a datagram packet is received from receiveSocket.
		try {
			System.out.println("waiting");
			schedulerSocketSendReceiveFloor.receive(schedulerFloorReceivePacket);
			System.out.println("Request from Floor: " + Arrays.toString(dataFloor));
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		/* Separating byte array received */
		elevatorOrFloor = dataFloor[0];
		elevatorOrFloorID1 = dataFloor[1];
		requestOrUpdate1 = dataFloor[2];
		currentFloor1 = dataFloor[3];
		upOrDown1 = dataFloor[4];
		destFloor1 = dataFloor[5];
	}

	public static void floorSendPacket(byte[] responseByteArray) {
		System.out.println("Response to Floor " + responseByteArray[1] + ": " + Arrays.toString(responseByteArray) + "\n");
		schedulerFloorSendPacket = new DatagramPacket(responseByteArray, responseByteArray.length,
				schedulerFloorReceivePacket.getAddress(), schedulerFloorReceivePacket.getPort());
		try {
	
			schedulerSocketSendReceiveFloor.send(schedulerFloorSendPacket);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static byte[] responsePacket(int ID, int currentFloor1, int floorRequest1) {

		// creates the byte array according to the required format
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(54);
		requestElevator.write(ID);
		requestElevator.write(0);
		requestElevator.write(0);
		requestElevator.write(0);
		requestElevator.write(0);

		if(floorRequest1 == -1) {
			requestElevator.write(STOP);
		} else if ((floorRequest1 - currentFloor1) < 0 && floorRequest1>=0) {
			requestElevator.write(DOWN); // downwards
		} else if ((floorRequest1 - currentFloor1) > 0 && floorRequest1>=0) {
			requestElevator.write(UP); // upwards
		} else if((floorRequest1 - currentFloor1) == 0 && floorRequest1>=0) {
			requestElevator.write(HOLD); // hold motor direction
		} else {
			requestElevator.write(ERROR);
		}
		// 0,2,0,1,0 (0, direction, openClose, motorSpin,0)
		return requestElevator.toByteArray();
	}
	
	public static byte[] floorResponsePacket(int ID, int elevatorNumber) {

		// creates the byte array according to the required format
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(54);
		requestElevator.write(ID);
		requestElevator.write(elevatorNumber);
		requestElevator.write(0);
		requestElevator.write(0);
		requestElevator.write(0);
		requestElevator.write(1);
		
		// 0,2,0,1,0 (0, direction, openClose, motorSpin,0)
		return requestElevator.toByteArray();
	}

	/* Splitting the packet to determine if its for a floor or elevator request */

	public synchronized static void packetDealer() {
		if (elevatorOrFloor == 21) {
			if (elevatorOrFloorID == 0) {
				if (requestOrUpdate == 1) {
					if (destFloor - currentFloor > 0 && destFloor <= numFloors && destFloor >= 0) {
						addToUpQueue(upQueue1,0, destFloor);
					} else if (destFloor - currentFloor < 0 && destFloor <= numFloors && destFloor >= 0) {
						addToDownQueue(downQueue1,0, destFloor);
					} else if (destFloor < 0){
						elevatorSendPacket(responsePacket(elevatorOrFloorID,currentFloor, -2));
					}
				}
			} else if (elevatorOrFloorID == 1) {
				if (requestOrUpdate == 1) {
					if (destFloor - currentFloor > 0 && destFloor <= numFloors && destFloor >= 0) {
						addToUpQueue(upQueue2,1, destFloor);
					} else if (destFloor - currentFloor < 0 && destFloor <= numFloors && destFloor >= 0) {
						addToDownQueue(downQueue2,1, destFloor);
					}
					else if (destFloor < 0){
						elevatorSendPacket(responsePacket(elevatorOrFloorID,currentFloor, -2));
					}
				}
			}
		}
	}

	private synchronized void floorPacketHandler() {
		if (elevatorOrFloor == 69) {
			interrupt();
			if (requestOrUpdate1 == 1) {
				if(upOrDown1 == UP) {
					if(upQueue1.contains(elevatorOrFloorID1) || upQueue2.contains(elevatorOrFloorID1)) {
						return;
					}else if (ele0 == elevatorOrFloorID1) {
						addToUpQueue(upQueue1, 0, elevatorOrFloorID1);
					}else if (ele1 == elevatorOrFloorID1) {
						addToUpQueue(upQueue2, 1, elevatorOrFloorID1);
					} else if (ele0 < elevatorOrFloorID1 && (elevatorOrFloorID1-ele0 < elevatorOrFloorID1 - ele1) ) {
						addToUpQueue(upQueue1, 0, elevatorOrFloorID1);
					} else if (ele1 < elevatorOrFloorID1 && (elevatorOrFloorID1-ele1 < elevatorOrFloorID1 - ele0)) {
						addToUpQueue(upQueue2, 1, elevatorOrFloorID1);
					} else if(ele0 > elevatorOrFloorID1 && ele1 > elevatorOrFloorID1 && !upWaitQueue.contains(elevatorOrFloorID1)) {
						upWaitQueue.add(elevatorOrFloorID1);
					}
				} else if (upOrDown == DOWN) {
					if(downQueue1.contains(elevatorOrFloorID1) || downQueue2.contains(elevatorOrFloorID1)) {
						return;
					}else if (ele0 == elevatorOrFloorID1) {
						addToDownQueue(upQueue1, 0, elevatorOrFloorID1);
					}else if (ele1 == elevatorOrFloorID1) {
						addToDownQueue(upQueue2, 1, elevatorOrFloorID1);
					}  else if (ele0 > elevatorOrFloorID1) {
						addToDownQueue(downQueue1, 0, elevatorOrFloorID1);
					} else if (ele1 < elevatorOrFloorID1) {
						addToDownQueue(downQueue2, 1, elevatorOrFloorID1);
					} else if(ele0 > elevatorOrFloorID1 && ele1 > elevatorOrFloorID1 && !downWaitQueue.contains(elevatorOrFloorID1)) {
						downWaitQueue.add(elevatorOrFloorID1);
					}
				}
			}
		}
	}

	public synchronized static void addToUpQueue(LinkedList<Integer> upQueue, int ID, int destFloor) {
		for (int i = 0; i <= upQueue.size(); i++) {
			if (ID == 0) {
				if (upQueue1.isEmpty()) {
					upQueue1.addFirst(destFloor);
					break;
				}
				if (destFloor < upQueue.get(i)) {
					upQueue1.add(i, destFloor);
					break;
				} else if (destFloor == upQueue.get(i)){
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
				if (destFloor < upQueue.get(i)) {
					upQueue2.add(i, destFloor);
					break;
				} else if (destFloor == upQueue.get(i)){
					break;
				} else if (i == upQueue.size()) {
					upQueue2.addLast(destFloor);
					break;
				}
			}
		}
	}

	public synchronized static void addToDownQueue(LinkedList<Integer> downQueue, int ID, int destFloor) {
		for (int i = 0; i <= downQueue.size(); i++) {
			if (ID == 0) {
				if (downQueue1.isEmpty()) {
					downQueue1.addFirst(destFloor);
					break;
				}
				if (destFloor > downQueue.get(i)) {
					downQueue1.add(i, destFloor);
					break;
				} else if (destFloor == downQueue.get(i)){
					break;
				} else if (i == downQueue.size()) {
					downQueue1.addLast(destFloor);
					break;
				}
			} else if (ID == 1) {
				if (downQueue2.isEmpty()) {
					downQueue2.addFirst(destFloor);
					break;
				}
				if (destFloor > downQueue.get(i)) {
					downQueue2.add(i, destFloor);
					break;
				} else if (destFloor == downQueue.get(i)){
					break;
				}  else if (i == downQueue.size()) {
					downQueue2.addLast(destFloor);
					break;
				}
			}
		}
	}


	public static LinkedList<Integer> Direction(int ID, int destFloor, int currentFloor) {

		synchronized(direction) {

		}
		if(ID == 0) {
			if (destFloor - currentFloor < 0 && upQueue1.isEmpty()) {
				direction.add(0, (int) DOWN);
			}
			if (destFloor - currentFloor > 0 && downQueue1.isEmpty()) {
				direction.add(0, (int) UP);
			}
			if (destFloor - currentFloor == 0) {
				direction.add(0, (int) HOLD);
			}
		} else if (ID == 1) {
			if (destFloor - currentFloor < 0 && upQueue2.isEmpty()) {
				direction.add(1, (int) DOWN);
			}
			if (destFloor - currentFloor > 0 && downQueue2.isEmpty()) {
				direction.add(1, (int) UP);
			}
			if (destFloor - currentFloor == 0) {
				direction.add(1, (int) HOLD);
			}
		}
		return direction;
	}

	public synchronized void schedulingAlgo() {
		/* ELevator 1 logic */
		if (!(upQueue1.isEmpty()) && elevatorOrFloorID == 0 && (direction.get(0) == UP || direction.get(0) == HOLD)) {
			int first = upQueue1.getFirst();
			byte[] responseByteArray = responsePacket(0, currentFloor, first);
			if (currentFloor == first) {
				upQueue1.removeFirst();
				semaphoreRemove0 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if (!(downQueue1.isEmpty()) && elevatorOrFloorID == 0 && (direction.get(0) == DOWN || direction.get(0) == HOLD)) {
			int first = downQueue1.getFirst();
			byte[] responseByteArray = responsePacket(0, currentFloor, first);
			if (currentFloor == first) {
				downQueue1.removeFirst();
				semaphoreRemove0 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if(upQueue1.isEmpty() && downQueue1.isEmpty() && elevatorOrFloorID == 0) {
			byte[] responseByteArray = responsePacket(0, currentFloor, -1);
			elevatorSendPacket(responseByteArray);
			return;
		}


		/* ELevator 2 logic */

		if (!(upQueue2.isEmpty()) && elevatorOrFloorID == 1 && (direction.get(0) == UP || direction.get(0) == HOLD)) {
			int first = upQueue2.getFirst();
			byte[] responseByteArray = responsePacket(1, currentFloor, first);
			if (currentFloor == first) {
				upQueue2.removeFirst();
				semaphoreRemove1 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if (!(downQueue2.isEmpty()) && elevatorOrFloorID == 1 && (direction.get(0) == DOWN || direction.get(0) == HOLD)) {
			int first = downQueue2.getFirst();
			byte[] responseByteArray = responsePacket(1, currentFloor, first);
			if (currentFloor == first) {
				downQueue2.removeFirst();
				semaphoreRemove1 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if(upQueue2.isEmpty() && downQueue2.isEmpty() && elevatorOrFloorID == 1) {
			byte[] responseByteArray = responsePacket(1, currentFloor, -1);
			elevatorSendPacket(responseByteArray);
			return;
		}
	}
	
	public boolean getSemaphore0() {
		return semaphoreRemove0; 
	}
	
	public boolean getSemaphore1() {
		return semaphoreRemove1; 
	}

	public static void main(String args[]) throws InterruptedException {

		Scheduler packet = new Scheduler();
		//byte[] responseByteArray = new byte[] {69,0,0,0,0,0,1};
		Thread floor = new Thread() {
			public void run() {
				while (true) {
					packet.floorReceivePacket();
					if (requestOrUpdate1 == 1) {
						if(semaWAIT == true) {
							semaWAIT = false;
						}
						packet.floorPacketHandler();
					}
					while(true) {
						if (packet.getSemaphore0()) {
							byte[] floorResponseByteArray = floorResponsePacket(ele0, 0);
							packet.floorSendPacket(floorResponseByteArray);
							packet.semaphoreRemove0 = false;
							break;
						}
						if (packet.getSemaphore1()) {
							byte[] floorResponseByteArray = floorResponsePacket(ele1, 1);
							packet.floorSendPacket(floorResponseByteArray);
							packet.semaphoreRemove1 = false;
							break;
						}
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		};
		
		direction.add(0, (int) HOLD);
		direction.add(1, (int) HOLD);

		floor.start();
		//floorSend.start();
		for (;;) {
			Scheduler.elevatorReceivePacket(); // connection to elevator class

			if(requestOrUpdate == 3) {
				semaWAIT = true;
				while(semaWAIT==true) {
					Thread.sleep(1);
				}
			}else if (requestOrUpdate == 1) {
				packet.packetDealer();
				Direction(elevatorOrFloorID, destFloor, currentFloor);
			} else if(requestOrUpdate == 2) {
				currentFloorTracker();
			}

			packet.schedulingAlgo();
		}
	}

	private static void currentFloorTracker() {
		if (elevatorOrFloorID == 0) {
			ele0 = currentFloor;
		} if(elevatorOrFloorID == 1) {
			ele1 = currentFloor;
		}
	}
}


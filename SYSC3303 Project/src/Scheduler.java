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

	public static int elevatorOrFloorID1;
	public static int requestOrUpdate1;
	public static int currentFloor1;
	public static int upOrDown1;
	public static int destFloor1;

	private static final byte HOLD = 0x00;// elevator is in hold state
	private static final byte UP = 0x01;// elevator is going up
	private static final byte DOWN = 0x02;// elevator is going down
	private static final byte STOP = 0x04;// elevator has finished all requests. 
	private static final byte ERROR_DOOR = (byte) 0xE1;// invalid packet. error
	private static final byte ERROR_MOTOR = (byte) 0xE2;// invalid packet. error

	// number of elevators and floors. Can change here!
	public static int numElevators = 4;
	public static int numFloors = 22;

	// lists to keep track of what requests need to be handled
	
	private static LinkedList<Integer> upQueue1 = new LinkedList<Integer>();
	private static LinkedList<Integer> downQueue1 = new LinkedList<Integer>();
	private static LinkedList<Integer> upQueue2 = new LinkedList<Integer>();
	private static LinkedList<Integer> downQueue2 = new LinkedList<Integer>();
	private static LinkedList<Integer> upQueue3 = new LinkedList<Integer>();
	private static LinkedList<Integer> downQueue3 = new LinkedList<Integer>();
	private static LinkedList<Integer> upQueue4 = new LinkedList<Integer>();
	private static LinkedList<Integer> downQueue4 = new LinkedList<Integer>();
	private static LinkedList<Integer> upWaitQueue = new LinkedList<Integer>();
	private static LinkedList<Integer> downWaitQueue = new LinkedList<Integer>();
	

	//private static boolean runEle;
	

	//private static int[] allDestinationFloors = new int[queue.size()];
	public static Object obj = new Object();
	public static int limit = numFloors * numElevators;
	//private static Thread newRequest;

	public static LinkedList<Integer> direction = new LinkedList<Integer>();
	protected static int ele0;
	protected static int ele1;
	protected static int ele2;
	protected static int ele3;
	
	private boolean semaphoreRemove0 = false;
	private boolean semaphoreRemove1 = false;
	private boolean semaphoreRemove2 = false;
	private boolean semaphoreRemove3 = false;
	private static int wakeUpEle;
	
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
			System.out.println("waiting...");
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

	public static void elevatorSendPacket(byte[] responseByteArray) throws UnknownHostException {
		System.out.println("Response to elevator " + responseByteArray[1] + ": " + Arrays.toString(responseByteArray) + "\n");
		//InetAddress address = InetAddress.getByName("134.117.59.108");
		schedulerElevatorSendPacket = new DatagramPacket(responseByteArray, responseByteArray.length,
				InetAddress.getLocalHost(), schedulerElevatorReceivePacket.getPort());

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
			System.out.println("waiting...");
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
			requestElevator.write(ERROR_DOOR);
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

	public synchronized static void packetDealer() throws UnknownHostException {
		if (elevatorOrFloor == 21) {
			if (elevatorOrFloorID == 0) {
				if (requestOrUpdate == 1) {
					if (destFloor < 0){
						elevatorSendPacket(responsePacket(elevatorOrFloorID,currentFloor, -2));
					} else if (destFloor - currentFloor > 0 && destFloor <= numFloors && destFloor >= 0) {
						addToUpQueue(upQueue1,0, destFloor);
					} else if (destFloor - currentFloor < 0 && destFloor <= numFloors && destFloor >= 0) {
						addToDownQueue(downQueue1, 0, destFloor);
					} 
				}
			} else if (elevatorOrFloorID == 1) {
				if (requestOrUpdate == 1) {
					if (destFloor < 0){
					elevatorSendPacket(responsePacket(elevatorOrFloorID,currentFloor, -2));
					} else if (destFloor - currentFloor > 0 && destFloor <= numFloors && destFloor >= 0) {
						addToUpQueue(upQueue2,1, destFloor);
					} else if (destFloor - currentFloor < 0 && destFloor <= numFloors && destFloor >= 0) {
						addToDownQueue(downQueue2, 1, destFloor);
					}
				}
			} else if (elevatorOrFloorID == 2) {
				if (requestOrUpdate == 1) {
					if (destFloor - currentFloor > 0 && destFloor <= numFloors && destFloor >= 0) {
						addToUpQueue(upQueue3,2, destFloor);
					} else if (destFloor - currentFloor < 0 && destFloor <= numFloors && destFloor >= 0) {
						addToDownQueue(downQueue3, 2, destFloor);
					}
					else if (destFloor < 0){
						elevatorSendPacket(responsePacket(elevatorOrFloorID,currentFloor, -2));
					}
				}
			} else if (elevatorOrFloorID == 3) {
				if (requestOrUpdate == 1) {
					if (destFloor - currentFloor > 0 && destFloor <= numFloors && destFloor >= 0) {
						addToUpQueue(upQueue4,3, destFloor);
					} else if (destFloor - currentFloor < 0 && destFloor <= numFloors && destFloor >= 0) {
						addToDownQueue(downQueue4, 3, destFloor);
					}
					else if (destFloor < 0){
						elevatorSendPacket(responsePacket(elevatorOrFloorID,currentFloor, -2));
					}
				}
			}
		}
	}
	
	public boolean checkClosestUpEle(int elevatorOrFloorID1, int elevatorInQuestion, int competingEle1, int competingEle2, int competingEle3) {
		int compEle1, compEle2, compEle3;
		if(elevatorOrFloorID1 - competingEle1 < 0 ) {
			compEle1 = 21;
		} else { 
			compEle1 = elevatorOrFloorID1 - competingEle1;
		}
		
		if(elevatorOrFloorID1 - competingEle2 < 0 ) {
			compEle2 = 21;
		} else { 
			compEle2 = elevatorOrFloorID1 - competingEle2;
		}
		
		if(elevatorOrFloorID1 - competingEle3 < 0 ) {
			compEle3 = 21;
		} else { 
			compEle3 = elevatorOrFloorID1 - competingEle3;
		}

		if ((elevatorOrFloorID1-elevatorInQuestion < compEle1) && (elevatorOrFloorID1-elevatorInQuestion < compEle2) &&
				(elevatorOrFloorID1-elevatorInQuestion < compEle3)){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean checkClosestDownEle(int elevatorOrFloorID1, int elevatorInQuestion, int competingEle1, int competingEle2, int competingEle3) {

		int compEle1, compEle2, compEle3;
		if(elevatorOrFloorID1 - competingEle1 < 0 ) {
			compEle1 = 21;
		} else { 
			compEle1 = elevatorOrFloorID1 - competingEle1;
		}
		
		if(elevatorOrFloorID1 - competingEle2 < 0 ) {
			compEle2 = 21;
		} else { 
			compEle2 = elevatorOrFloorID1 - competingEle2;
		}
		
		if(elevatorOrFloorID1 - competingEle3 < 0 ) {
			compEle3 = 21;
		} else { 
			compEle3 = elevatorOrFloorID1 - competingEle3;
		}
		
		if((elevatorInQuestion - elevatorOrFloorID1 > competingEle1 - elevatorOrFloorID1) && (elevatorInQuestion - elevatorOrFloorID1 > competingEle2 - elevatorOrFloorID1) &&
				(elevatorInQuestion - elevatorOrFloorID1 > competingEle3 - elevatorOrFloorID1)) {
			return true;
		}else {
			return false;
		}
	}

	private synchronized void floorPacketHandler() {
		if (elevatorOrFloor == 69) {
			interrupt();
			if (requestOrUpdate1 == 1) {
				if(upOrDown1 == UP) {
					if(upQueue1.contains(elevatorOrFloorID1) || upQueue2.contains(elevatorOrFloorID1) ||  upQueue3.contains(elevatorOrFloorID1) || upQueue4.contains(elevatorOrFloorID1)) {
						return;		
					}else if (ele0 == elevatorOrFloorID1) {
						addToUpQueue( upQueue1, 0, elevatorOrFloorID1);
						if(direction.get(0) == HOLD) {
							direction.add(0, (int) UP);
							wakeUpEle = 0;
						}
					}else if (ele1 == elevatorOrFloorID1) {
						addToUpQueue(upQueue2, 1, elevatorOrFloorID1);
						if(direction.get(1) == HOLD) {
							direction.add(1, (int) UP);
							wakeUpEle = 1;
						}
					}else if (ele2 == elevatorOrFloorID1) {
						addToUpQueue( upQueue3, 2, elevatorOrFloorID1);
						if(direction.get(2) == HOLD) {
							direction.add(2, (int) UP);
							wakeUpEle = 2;
						}
					}else if (ele3 == elevatorOrFloorID1) {
						addToUpQueue( upQueue4, 3, elevatorOrFloorID1);
						if(direction.get(3) == HOLD) {
							direction.add(3, (int) UP);
							wakeUpEle = 3;
						}
					} else if (ele0 < elevatorOrFloorID1 && checkClosestUpEle(elevatorOrFloorID1,ele0,ele1,ele2,ele3)) {
						addToUpQueue(upQueue1, 0, elevatorOrFloorID1);
						System.out.println("ADDED TO UPQUEUE 1 ----> " + upQueue1.size());
						if(direction.get(0) == HOLD) {
							direction.add(0, (int) UP);
							wakeUpEle = 0;
						}
					} else if (ele1 < elevatorOrFloorID1 && checkClosestUpEle(elevatorOrFloorID1,ele1,ele0,ele2,ele3)) {
						addToUpQueue(upQueue2, 1, elevatorOrFloorID1);
						if(direction.get(1) == HOLD) {
							direction.add(1, (int) UP);
							wakeUpEle = 1;
						}
					} else if (ele2 < elevatorOrFloorID1 && checkClosestUpEle(elevatorOrFloorID1,ele2,ele1,ele0,ele3)) {
						addToUpQueue(upQueue3, 2, elevatorOrFloorID1);
						if(direction.get(2) == HOLD) {
							direction.add(2, (int) UP);
							wakeUpEle = 2;
						}
					} else if (ele3 < elevatorOrFloorID1 && checkClosestUpEle(elevatorOrFloorID1,ele3,ele1,ele2,ele0)) {
						addToUpQueue(upQueue4, 3, elevatorOrFloorID1);
						System.out.println("ADDED TO UPQUEUE 4 ----> " + upQueue4.size());
						if(direction.get(3) == HOLD) {
							direction.add(3, (int) UP);
							wakeUpEle = 3;
						}
					} else if(ele0 > elevatorOrFloorID1 && ele1 > elevatorOrFloorID1 && ele2 > elevatorOrFloorID1  && ele3 > elevatorOrFloorID1  && !upWaitQueue.contains(elevatorOrFloorID1)) {
						upWaitQueue.add(elevatorOrFloorID1);	
					}
				} else if (upOrDown1 == DOWN) {
					if(downQueue1.contains(elevatorOrFloorID1) || downQueue2.contains(elevatorOrFloorID1) || downQueue3.contains(elevatorOrFloorID1) || downQueue4.contains(elevatorOrFloorID1)) {
						return;
					}else if (ele0 == elevatorOrFloorID1) {
						addToDownQueue(downQueue1, 0, elevatorOrFloorID1);
						if(direction.get(0) == HOLD) {
							direction.add(0, (int) DOWN);
							wakeUpEle = 0;
						}
					}else if (ele1 == elevatorOrFloorID1) {
						addToDownQueue(downQueue2, 1, elevatorOrFloorID1);

						if(direction.get(1) == HOLD) {
							direction.add(1, (int) DOWN);
							wakeUpEle = 1;
						}
					}else if (ele2 == elevatorOrFloorID1) {
						addToDownQueue(downQueue3, 2, elevatorOrFloorID1);
						System.out.println("ADDED TO DOWNQUEUE 3 ----> " + downQueue3.size() );
						if(direction.get(2) == HOLD) {
							direction.add(2, (int) DOWN);
							wakeUpEle = 2;
						}
					}else if (ele3 == elevatorOrFloorID1) {
						addToDownQueue(downQueue4, 3, elevatorOrFloorID1);
						if(direction.get(3) == HOLD) {
							direction.add(3, (int) DOWN);
							wakeUpEle = 3;
						}
					} else if (ele0 > elevatorOrFloorID1 && checkClosestDownEle(elevatorOrFloorID1,ele0,ele1,ele2,ele3)) {
						addToDownQueue(downQueue1, 0, elevatorOrFloorID1);
						if(direction.get(0) == HOLD) {
							direction.add(0, (int) DOWN);
							wakeUpEle = 0;
						}
					} else if (ele1 > elevatorOrFloorID1 && checkClosestDownEle(elevatorOrFloorID1,ele1,ele0,ele2,ele3)) {
						addToDownQueue(downQueue2, 1, elevatorOrFloorID1);
						if(direction.get(1) == HOLD) {
							direction.add(1, (int) DOWN);
							wakeUpEle = 1;
						}
					} else if (ele2 > elevatorOrFloorID1 && checkClosestDownEle(elevatorOrFloorID1,ele2,ele1,ele0,ele3)) {
						addToDownQueue(downQueue3, 2, elevatorOrFloorID1);
						System.out.println("ADDED TO DOWNQUEUE 2 ----> " + downQueue2.size());
						if(direction.get(2) == HOLD) {
							direction.add(2, (int) DOWN);
							wakeUpEle = 2;
						}
					} else if (ele3 > elevatorOrFloorID1 && checkClosestDownEle(elevatorOrFloorID1,ele3,ele1,ele2,ele0)) {
						addToDownQueue(downQueue4, 3, elevatorOrFloorID1);
						if(direction.get(3) == HOLD) {
							direction.add(3, (int) DOWN);
							wakeUpEle = 3;
						}
					} else if(ele0 > elevatorOrFloorID1 && ele1 > elevatorOrFloorID1 && ele2 > elevatorOrFloorID1 && ele3 > elevatorOrFloorID1 && !downWaitQueue.contains(elevatorOrFloorID1)) {
						downWaitQueue.add(elevatorOrFloorID1);
					}
				}
			}
		}
	}

	public synchronized static void addToUpQueue(LinkedList<Integer> upQueue, int ID, int destFloor) {
		if (upQueue.isEmpty()) {
			upQueue.addLast(destFloor);
			System.out.println("ELEVATOR 0 UPQUEUE LIST SIZE --> " + upQueue1.size());
		} else {
			for (int i = 0; i < upQueue.size(); i++) {

				if (destFloor < upQueue.get(i)) {
					upQueue.add(i, destFloor);
					break;
				} else if (destFloor == upQueue.get(i)){
					break;
				} else if (i == upQueue.size()) {
					upQueue.addLast(destFloor);
					break;
				}
			}
		}
	}

	public synchronized static void addToDownQueue(LinkedList<Integer> downQueue, int ID, int destFloor) {
		if (downQueue.isEmpty()) {
			downQueue.addFirst(destFloor);
			System.out.println("ELEVATOR 1 DOWNQUEUE LIST SIZE --> " + downQueue2.size());
		} else {
			for (int i = 0; i < downQueue.size(); i++) {

				if (destFloor > downQueue.get(i)) {
					downQueue.add(i, destFloor);
					System.out.println("ELEVATOR 1 DOWNQUEUE LIST SIZE --> " + downQueue2.size());
					break;
				} else if (destFloor == downQueue.get(i)){
					break;
				} else if (i == downQueue.size()) {
					downQueue.addLast(destFloor);
					break;
				}
			}
		}
	}

	public static LinkedList<Integer> Direction(int ID, int destFloor, int currentFloor) {


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

	public synchronized void schedulingAlgo() throws UnknownHostException {
		/* ELevator 1 logic */
		if (!(upQueue1.isEmpty()) && elevatorOrFloorID == 0 && (direction.get(0) == UP || direction.get(0) == HOLD)) {
			System.out.println("DIRECTION DOWN -----> " + direction.get(0));
			System.out.println(currentFloor);
			int first = upQueue1.getFirst();
			System.out.println(first);
			byte[] responseByteArray = responsePacket(0, ele0, first);
			if (ele0 == first) {
				upQueue1.removeFirst();
				semaphoreRemove0 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if (!(downQueue1.isEmpty()) && elevatorOrFloorID == 0 && (direction.get(0) == DOWN || direction.get(0) == HOLD)) {
			System.out.println("DIRECTION DOWN -----> " + direction.get(0));
			int first = downQueue1.getFirst();
			byte[] responseByteArray = responsePacket(0, ele0, first);
			if (ele0 == first) {
				downQueue1.removeFirst();
				semaphoreRemove0 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if(upQueue1.isEmpty() && elevatorOrFloorID == 0 && downQueue1.isEmpty() && elevatorOrFloorID == 0 && !semaphoreRemove0) {
			byte[] responseByteArray = responsePacket(0, ele0, -1);
			elevatorSendPacket(responseByteArray);
			return;
		}

		/* ELevator 2 logic */

		if (!(upQueue2.isEmpty()) && elevatorOrFloorID == 1 && (direction.get(1) == UP || direction.get(1) == HOLD)) {
			int first = upQueue2.getFirst();
			byte[] responseByteArray = responsePacket(1, ele1, first);
			if (ele1 == first) {
				upQueue2.removeFirst();
				semaphoreRemove1 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if (!(downQueue2.isEmpty()) && elevatorOrFloorID == 1 && (direction.get(1) == DOWN || direction.get(1) == HOLD)) {
			int first = downQueue2.getFirst();
			byte[] responseByteArray = responsePacket(1, ele1, first);
			if (ele1 == first) {
				downQueue2.removeFirst();
				semaphoreRemove1 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if(upQueue2.isEmpty() && downQueue2.isEmpty() && elevatorOrFloorID == 1 && elevatorOrFloorID == 1 && !semaphoreRemove1) {
			byte[] responseByteArray = responsePacket(1, ele1, -1);
			elevatorSendPacket(responseByteArray);
			return;
		}
		
		/* ELevator 3 logic */
		if (!(upQueue3.isEmpty()) && elevatorOrFloorID == 2 && (direction.get(2) == UP || direction.get(2) == HOLD)) {
			int first = upQueue3.getFirst();
			byte[] responseByteArray = responsePacket(2, ele2, first);
			if (ele2 == first) {
				upQueue3.removeFirst();
				semaphoreRemove2 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if (!(downQueue3.isEmpty()) && elevatorOrFloorID == 2 && (direction.get(2) == DOWN || direction.get(2) == HOLD)) {
			int first = downQueue3.getFirst();
			byte[] responseByteArray = responsePacket(2, ele2, first);
			if (ele2 == first) {
				downQueue3.removeFirst();
				semaphoreRemove2 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if(upQueue3.isEmpty() && downQueue3.isEmpty() && elevatorOrFloorID == 2) {
			byte[] responseByteArray = responsePacket(2, ele2, -1);
			elevatorSendPacket(responseByteArray);
			return;
		}


		/* ELevator 4 logic */

		if (!(upQueue4.isEmpty()) && elevatorOrFloorID == 3 && (direction.get(3) == UP || direction.get(3) == HOLD)) {
			int first = upQueue4.getFirst();
			byte[] responseByteArray = responsePacket(3, ele3, first);
			if (ele3 == first) {
				upQueue4.removeFirst();
				semaphoreRemove3 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if (!(downQueue4.isEmpty()) && elevatorOrFloorID == 3 && (direction.get(3) == DOWN || direction.get(3) == HOLD)) {
			int first = downQueue4.getFirst();
			byte[] responseByteArray = responsePacket(3, ele3, first);
			if (ele3 == first) {
				downQueue4.removeFirst();
				semaphoreRemove3 = true;
			}
			elevatorSendPacket(responseByteArray);
			return;
		}

		if(upQueue4.isEmpty() && downQueue4.isEmpty() && elevatorOrFloorID == 3) {
			byte[] responseByteArray = responsePacket(3, ele3, -1);
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
	
	public boolean getSemaphore2() {
		return semaphoreRemove2; 
	}
	
	public boolean getSemaphore3() {
		return semaphoreRemove3; 
	}

	public static void main(String args[]) throws InterruptedException, UnknownHostException {

		Scheduler packet = new Scheduler();

		Thread floor = new Thread() {
			public void run() {
				while (true) {
					packet.floorReceivePacket();
					while (requestOrUpdate1 == 1) {
						switch(requestOrUpdate1) {
						case 1:
							packet.floorPacketHandler();
							elevatorOrFloorID = wakeUpEle;
							System.out.println("                      " + elevatorOrFloorID + upQueue4.size() + direction.get(3) );
							try {
								packet.schedulingAlgo();
								System.out.println("SENDING FROM FLOOR THREAD HERE ---- ");
							} catch (UnknownHostException e) {
								e.printStackTrace();
							}
							packet.floorReceivePacket();
						case 2:
							if(semaWAIT == true) {
								semaWAIT = false;
							}
							//runEle = true;
							break;
						}
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
							e.printStackTrace();
						}
					}
				}
			}


		};
		
		direction.add(0, (int) HOLD);
		direction.add(1, (int) HOLD);
		direction.add(2, (int) HOLD);
		direction.add(3, (int) HOLD);
		



		floor.start();


		for (;;) {
			Scheduler.elevatorReceivePacket(); // connection to elevator class
			if(requestOrUpdate == 3) {
				semaWAIT = true;
				while(semaWAIT==true) {
					Thread.sleep(100);
				}
				
			}else if (requestOrUpdate == 1) {
				packet.packetDealer();
				Direction(elevatorOrFloorID, destFloor, currentFloor);
				currentFloorTracker();
			} else if(requestOrUpdate == 2) {
				currentFloorTracker();
			}
			
			packet.schedulingAlgo();
		}
	}

	public static void currentFloorTracker() {
		if (elevatorOrFloorID == 0) {
			ele0 = currentFloor;
			System.out.println("CURRENT FLOOR OF ELEVATOR 0 --->" + ele0);
		} if(elevatorOrFloorID == 1) {
			ele1 = currentFloor;
			System.out.println("CURRENT FLOOR OF ELEVATOR 1 --->" + ele1);
		}if(elevatorOrFloorID == 2) {
			ele2 = currentFloor;
			System.out.println("CURRENT FLOOR OF ELEVATOR 2 --->" + ele2);
		}if(elevatorOrFloorID == 3) {
			ele3 = currentFloor;
			System.out.println("CURRENT FLOOR OF ELEVATOR 3 --->" + ele3);
		}
	}
}


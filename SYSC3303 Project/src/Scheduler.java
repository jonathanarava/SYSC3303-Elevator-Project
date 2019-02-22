import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Scheduler extends Thread{

	// Packets and sockets required to connect with the Elevator and Floor class

	public static DatagramSocket schedulerSocketSendReceiveElevator, schedulerSocketSendReceiveFloor;
	public static DatagramPacket schedulerElevatorSendPacket, schedulerElevatorReceivePacket, schedulerFloorSendPacket, schedulerFloorReceivePacket;


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
	
	// number of elevators and floors. Can change here!
	public static int numElevators = 3;
	public static int numFloors = 15;
	
	
	// lists to keep track of what requests need to be handled
	
	private static List<Thread> queue  = new LinkedList<Thread>();
	public static Object obj = new Object();
	

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

	public static void elevatorPacket() throws InterruptedException {

														/* ELEVATOR RECEIVING PACKET HERE*/
		schedulerElevatorReceivePacket = new DatagramPacket(data, data.length);
		// System.out.println("Server: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			System.out.println("waiting");
			schedulerSocketSendReceiveElevator.receive(schedulerElevatorReceivePacket);
			System.out.println("Request from elevator: " + Arrays.toString(data));

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

														/* ELEVATOR SENDING PACKET HERE*/

		byte[] responseByteArray = new byte[7];

		responseByteArray = responsePacket(currentFloor, destFloor);
		System.out.println(
				"Response to elevator " + data[1] + ": " + Arrays.toString(responseByteArray) + "\n");
		schedulerElevatorSendPacket = new DatagramPacket(responseByteArray, responseByteArray.length,
				schedulerElevatorReceivePacket.getAddress(), schedulerElevatorReceivePacket.getPort());
		try {
			schedulerSocketSendReceiveFloor.send(schedulerElevatorSendPacket);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void floorPacket() throws InterruptedException {

													/* FLOOR RECEIVING PACKET HERE*/
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


													/*FLOOR SENDING PACKET HERE*/

		byte[] responseByteArray = new byte[5];

		responseByteArray = responsePacket(currentFloor, destFloor);
		System.out.println(
				"Response to Floor " + data[1] + ": " + Arrays.toString(responseByteArray) + "\n");
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

	public static byte[] responsePacket(int currentFloor1, int floorRequest1) {

		// creates the byte array according to the required format
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(69);
		requestElevator.write(0);
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
	
	public void packetDealer() {
		if (elevatorOrFloor == 21) {
			elevatorPacketHandler();
		} else if (elevatorOrFloor == 69 ) {
			floorPacketHandler();
		}
		
	}
	
	private synchronized void elevatorPacketHandler() {
		synchronized(queue) {
			if (!newRequest()) {
				for(int i = 0; i < numElevators; i++)
					
				while(!isInterrupted()) {

				}
			}else {	

			}
		}
	}
	
	
	public static boolean newRequest() {
		if (requestOrUpdate == 2) {
			return true;
		} else {
			return false;
		}
	}
	
	private static void floorPacketHandler() {
		// TODO Auto-generated method stub
		
	}

	public static void main(String args[]) throws InterruptedException {

		//int createNumElevators = Integer.parseInt(args[0]);
		//int createNumFloors = Integer.parseInt(args[1]);
		Scheduler packet = new Scheduler();
		for (;;) {

			packet.elevatorPacket();   // connection to elevator class


			//packet.floorPacket();		// connection to floor class
		}
	}
}
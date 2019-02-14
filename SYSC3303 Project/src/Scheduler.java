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

public class Scheduler {

	public static DatagramSocket schedulerSocetSendElevator, schedulerSocketReceiveElevator;
	public static DatagramPacket schedulerSendPacket, schedulerReceivePacket;

	// public boolean isListen;

	public int elevatorID;
	public int floorRequest;
	public int currentFloor;
	public int elevatorOrFloor;
	public int destFloor;
	
	public static LinkedList<Integer> ElevatorList = new LinkedList<Integer>();
	public static final int limit = 20;
	

	public Scheduler() {
		try {
			schedulerSocetSendElevator = new DatagramSocket();
			schedulerSocketReceiveElevator = new DatagramSocket(369);// can be any available port, Scheduler will reply
																		// to the port
			// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void breakDown(byte[] x) {

	}

	public void receivedPacket() throws InterruptedException {

		byte data[] = new byte[7];

		schedulerReceivePacket = new DatagramPacket(data, data.length);
		// System.out.println("Server: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			System.out.println("waiting");
			schedulerSocketReceiveElevator.receive(schedulerReceivePacket);
			System.out.println("Request from elevator: " + Arrays.toString(data));

			// schedulerSocketReceiveElevator.close();
			// schedulerSocetSendElevator.close()

		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}

		
		elevatorOrFloor = data[0];
		elevatorID = data[1];
		floorRequest = data[2];
		currentFloor = data[3];
		destFloor = data[5];
		

		byte[] responseByteArray = new byte[5];
		

		if (elevatorOrFloor == 21) {
			if (elevatorID == 1) {
				if (floorRequest == 2) { // if its a new request
					addToListQueue(destFloor);
				}
				
				if (currentFloor != destFloor) {
					responseByteArray = responsePacket(currentFloor, destFloor);
					System.out.println(
							"Response to elevator " + data[1] + ": " + Arrays.toString(responseByteArray) + "\n");
					schedulerSendPacket = new DatagramPacket(responseByteArray, responseByteArray.length,
							schedulerReceivePacket.getAddress(), schedulerReceivePacket.getPort());
					
					try {
						schedulerSocetSendElevator.send(schedulerSendPacket);
						// System.out.println("Sent");
					} catch (IOException e) {
						System.out.print("hi");
						e.printStackTrace();
						System.exit(1);
					}
				} else if (currentFloor == destFloor) {
					System.out.println("waiting");
					removeFromListQueue();
				}
			}
		}
	}
	
	public synchronized void addToListQueue(int destFloor2) throws InterruptedException {
		synchronized(this) {
			while (ElevatorList.size()==limit) {
				wait();
				System.out.println("Request already made. wait for elevator to complete request");
			}
			ElevatorList.add(destFloor2);
			notifyAll();
		}
	}
	
	public synchronized void removeFromListQueue() throws InterruptedException {
		synchronized(this) {
			while (ElevatorList.size()==0) {
				wait();
				System.out.println("No recent requests made by any elevator");
			}
			ElevatorList.remove();
			notifyAll();
		}
	}

	public byte[] responsePacket(int currentFloor1, int floorRequest1) {

		/*
		 * ELEVATOR --> SCHEDULER (elevator or floor (elevator-21), elevator id(which
		 * elevator), FloorRequest/update, curentFloor, up or down, destFloor,
		 * instruction) (
		 */
		
		// creates the byte array according to the required format
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(0);

		if ((floorRequest1 - currentFloor1) < 0) {
			requestElevator.write(1); // downwards
			requestElevator.write(0);
		} else if ((floorRequest1 - currentFloor1) > 0) {
			requestElevator.write(2); // upwards
			requestElevator.write(0);
		} else {
			requestElevator.write(0); // motorDirection
			requestElevator.write(1); // open or Close
		}

		if ((floorRequest1 - currentFloor1) != 0) {
			requestElevator.write(1);
		} else {
			requestElevator.write(0); // MotorSpin Time
		}
		requestElevator.write(0);
		// 0,2,0,1,0 (0, direction, openClose, motorSpin,0)

		return requestElevator.toByteArray();

	}

	/*
	 * void stopListening() { isListen=false;
	 * schedulerSocketReceiveElevator.close(); }
	 */

	public static void main(String args[]) throws InterruptedException {

		Scheduler packet = new Scheduler();
		for (;;) {

			packet.receivedPacket();
			// Thread.sleep(1000);
			// packet.stopListening();

		}

	}
}
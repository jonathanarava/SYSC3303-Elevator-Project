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

public class Scheduler {

	public static DatagramSocket schedulerSocetSendElevator, schedulerSocketReceiveElevator;
	public static DatagramPacket schedulerSendPacket, schedulerReceivePacket;

	// public boolean isListen;

	public static byte data[] = new byte[7];
	public static int elevatorID;
	public static int floorRequest;
	public static int currentFloor;
	public static int elevatorOrFloor;
	public static int destFloor;

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

	public static void receivedPacket() throws InterruptedException {

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
	}

	public static void sendPacket() throws InterruptedException {
		byte[] responseByteArray = new byte[5];


		// Send the datagram packet to the client via the send socket.
		try {
			if (elevatorOrFloor == 21) {
				if (elevatorID == 0) {
					if (currentFloor != destFloor) {
						responseByteArray = responsePacket(currentFloor, destFloor);
						System.out.println(
								"Response to elevator " + data[1] + ": " + Arrays.toString(responseByteArray) + "\n");
						schedulerSendPacket = new DatagramPacket(responseByteArray, responseByteArray.length,
								schedulerReceivePacket.getAddress(), schedulerReceivePacket.getPort());
						schedulerSocetSendElevator.send(schedulerSendPacket);
					} else if (currentFloor == destFloor) {
						System.out.println("waiting");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
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

	/*
	 * void stopListening() { isListen=false;
	 * schedulerSocketReceiveElevator.close(); }
	 */

	public static void main(String args[]) throws InterruptedException {

		//int createNumElevators = Integer.parseInt(args[0]);
		//int createNumFloors = Integer.parseInt(args[1]);
		Scheduler packet = new Scheduler();
		for (;;) {

			packet.receivedPacket();
			packet.sendPacket();
			// Thread.sleep(1000);
			// packet.stopListening();

		}

	}
}
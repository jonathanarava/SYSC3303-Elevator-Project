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

	public int elevatorID;
	public int floorRequest;
	public int currentFloor;
	public int elevatorOrFloor;
	public int destFloor;

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
			Thread.sleep(5000);
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
				if (currentFloor != destFloor) {
					responseByteArray = responsePacket(currentFloor, destFloor);
					System.out.println(
							"Response to elevator " + data[1] + ": " + Arrays.toString(responseByteArray) + "\n");
					schedulerSendPacket = new DatagramPacket(responseByteArray, responseByteArray.length,
							schedulerReceivePacket.getAddress(), schedulerReceivePacket.getPort());
					// }
				}
			}
		}

		// or (as we should be sending back the same thing)
		// System.out.println(received);

		// Send the datagram packet to the client via the send socket.
		try {
			schedulerSocetSendElevator.send(schedulerSendPacket);
			// System.out.println("Sent");
		} catch (IOException e) {
			System.out.print("hi");
			e.printStackTrace();
			System.exit(1);
		}

		// System.out.println();
	}

	public byte[] responsePacket(int currentFloor1, int floorRequest1) {

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
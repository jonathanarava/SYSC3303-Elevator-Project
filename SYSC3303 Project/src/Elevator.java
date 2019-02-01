
//no main method
//Output: floor request, 
//Input: Motor control (up, down, stop), door (open, close), Floor number (for display), direction (display)
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Elevator implements Runnable {
	public static String NAMING;
	public static int floorRequest;

	DatagramPacket elevatorSendPacket, elevatorReceivePacket;
	DatagramSocket elevatorSendSocket, elevatorReceiveSocket;

	public Elevator(String name) {
		NAMING = name;// mandatory for having it actually declared as a thread object

		try {
			elevatorSendSocket = new DatagramSocket(23);
			elevatorReceiveSocket = new DatagramSocket();// can be any available port, Scheduler will reply to the port
															// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}

		// arbitrary usage of 23 for port number of Scheduler's receive
		// use a numbering scheme for the naming

		// allocate sockets, packets
		/*
		 * try { //ClientRWSocket = new DatagramSocket(23);//initialize ClientRWSocket
		 * for reading and writing to the Intermediate server //port 23 is the
		 * well-known port number of Intermediate } catch (SocketException se) {//if
		 * DatagramSocket creation fails an exception is thrown se.printStackTrace();
		 * System.exit(1); } //run checking loop indefinitely //status of elevator floor
		 * number, input of floor requests, direction of elevator, motor input, door
		 * input //only waits for packet reception? check data of packet and change
		 * accordingly
		 */
	}

	public void sensor() {
		try {
			int i = 4;
			while (i != 0) {
				System.out.format("Seconds until elevator door closes: %d second \n", i);
				i--;
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public byte[] responsePacket(int floorRequest) {

		// creates the byte array according to the required format
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(0);
		requestElevator.write((byte) floorRequest);
		requestElevator.write(0);
		return requestElevator.toByteArray();

	}

	public void run() {
		byte[] requestElevator = new byte[3];
		while (true) {

			System.out.println("Enter floor number: ");

			Scanner destination = new Scanner(System.in);
			int floorRequest = destination.nextInt();
			destination.close();

			requestElevator = responsePacket(floorRequest);
			int lengthOfByteArray = responsePacket(floorRequest).length;

			// allocate sockets, packets
			try {
				elevatorSendPacket = new DatagramPacket(requestElevator, lengthOfByteArray, InetAddress.getLocalHost(),
						23);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}

			// send packet for scheduler to know the port this elevator is allocated
			// sendPacket = new DatagramPacket(data,
			// receivePacket.getLength(),receivePacket.getAddress(),
			// receivePacket.getPort());
		}
	}
}

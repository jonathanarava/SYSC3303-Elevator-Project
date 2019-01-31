
//no main method
//no logic beyond changing the status of requests, arrival sensor, and display
import java.io.*;
import java.net.*;

//class Floor:has direction buttons and Floor display


public class Floor implements Runnable {
	public static String NAMING;

	public Floor(String name) {
		NAMING = name;// mandatory for having it actually declared as a thread object
		// use a numbering scheme for the naming

	}

	public void run() {
		// Declare Variables for THREADS
		DatagramPacket floorSendPacket, floorReceivePacket;
		DatagramSocket floorSendSocket, floorReceiveSocket;

		// allocate sockets, packets
		try {
			// ClientRWSocket = new DatagramSocket(23);//initialize ClientRWSocket for
			// reading and writing to the Intermediate server
			// port 23 is the well-known port number of Intermediate
			floorSendSocket = new DatagramSocket(23);// arbitrary usage of 23 for port number of Scheduler's receive
														// port
			floorReceiveSocket = new DatagramSocket();// can be any available port, Scheduler will reply to the port
														// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}

		// send packet for scheduler to know the port this elevator is allocated
	}
	//

}

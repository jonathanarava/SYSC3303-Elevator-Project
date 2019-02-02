
//no main method
//no logic beyond changing the status of requests, arrival sensor, and display
import java.io.*;
import java.net.*;

/*
 * class Floor:has direction buttons and Floor display
 */
public class Floor implements Runnable {
	
	/* Real-time Input Information: In the next iteration these will be provided 
	 * Time from EPOCH in an int, Floor where the elevator is requested in an Int(1-4)
	 * Which direction the Button was pressed in a String(Up or Down)
	 * What floor was requested inside the elevator in an int(1-4)
	 */
	public int real_time = 65456;
	public int whoamI = 2;
	public String up_or_down = "up";
	public int wheredoIwanttogo = 4;
	
	public int elevatorLocation;
	public static int NAMING;
	DatagramPacket floorSendPacket, floorReceivePacket;
	DatagramSocket floorSendSocket, floorReceiveSocket;
	
	public Floor() {}
	public Floor(int name) {
		NAMING = name;// mandatory for having it actually declared as a thread object
		// use a numbering scheme for the naming
		
		try {
			floorSendSocket = new DatagramSocket(name+22);
			floorReceiveSocket = new DatagramSocket();// can be any available port, Scheduler will reply to the port that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	
	/* Gets an elevator request as an int(up or down)
	 * @returns a byte[] array that can be then used to send to the Schedular
	 */
	public byte[] responsePacket() {
		// creates the byte array according to the required format in this case 00000000-DATABYTE-00000000
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		if(NAMING == whoamI) {
			requestElevator.write(0);
			requestElevator.write(real_time);
			requestElevator.write(0);
			requestElevator.write(whoamI);
			requestElevator.write(0);
			try {
				requestElevator.write(up_or_down.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			requestElevator.write(0);
			return requestElevator.toByteArray();
		}
		else return requestElevator.toByteArray();
	}
	

	public void run() {

		// send packet for scheduler to know the port this elevator is allocated
	}
	//

}

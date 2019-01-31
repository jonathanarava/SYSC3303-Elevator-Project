//no main method
//Output: floor request, 
//Input: Motor control (up, down, stop), door (open, close), Floor number (for display), direction (display)
import java.io.*;
import java.net.*;
public class Elevator implements Runnable {
	public static String NAMING;
	
	public Elevator(String name){
		NAMING = name;//mandatory for having it actually declared as a thread object
		//use a numbering scheme for the naming


		//allocate sockets, packets
		/*try {
			//ClientRWSocket = new DatagramSocket(23);//initialize ClientRWSocket for reading and writing to the Intermediate server
			//port 23 is the well-known port number of Intermediate
		} catch (SocketException se) {//if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
		//run checking loop indefinitely 
			//status of elevator floor number, input of floor requests, direction of elevator, motor input, door input
			//only waits for packet reception? check data of packet and change accordingly
		 */
	}
	public void run() {
		//Declare Variables for THREADS
		DatagramPacket elevatorSendPacket, elevatorReceivePacket; 
		DatagramSocket elevatorSendSocket, elevatorReceiveSocket; 

		//allocate sockets, packets
		try {
			elevatorSendSocket=new DatagramSocket(23);//arbitrary usage of 23 for port number of Scheduler's receive port
			elevatorReceiveSocket=new DatagramSocket();//can be any available port, Scheduler will reply to the port that's been received
		} catch (SocketException se) {//if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}

		//send packet for scheduler to know the port this elevator is allocated
		//sendPacket = new DatagramPacket(data, receivePacket.getLength(),receivePacket.getAddress(), receivePacket.getPort());
	}
}


//no main method
//no logic beyond changing the status of requests, arrival sensor, and display
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
	
	//This String List will contain ALL of the real time input information that is Given to the system. 
	//List index 
	public List<String> realTime_Input;
	
	public int sendPort_num;
	public int elevatorLocation;
	public static int NAMING;
	DatagramPacket floorSendPacket, floorReceivePacket;
	DatagramSocket floorSendSocket, floorReceiveSocket;
	
	String elevatorRequest = "";
	
	/*
	 * Blank constructor so Elevator Class can access Input information
	 * This will be changed for final iteration 
	 */
	public Floor() {}
	
	
	/*
	 * Constructor so Floors can be initialized in a way that can be runnable in the scheduler
	 */
	public Floor(int name) {
		NAMING = name;// mandatory for having it actually declared as a thread object
		// use a numbering scheme for the naming
		sendPort_num = name + 22;
		try {
			floorSendSocket = new DatagramSocket(sendPort_num);
			floorReceiveSocket = new DatagramSocket();// can be any available port, Scheduler will reply to the port that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	
	/*
	 * Opens and closes the doors in the floor by printing a message of what is happening
	 */
	public String openCloseDoor(byte door) {
		String msg;
		if (door == 1) {
			msg = "Doors are open.";
			System.out.println(msg);
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
		} else {
			msg = "Doors are closed.";
			System.out.println(msg);
		}
		return msg;
	}
	
	
	/* Gets an elevator request as an int(up or down)
	 * @returns a byte[] array that can be then used to send to the Scheduler
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
			if(up_or_down.equalsIgnoreCase("up")) {
				requestElevator.write(1);
			}
			else if(up_or_down.equalsIgnoreCase("down")) {
				requestElevator.write(0);
			}
			requestElevator.write(0);
			return requestElevator.toByteArray();
		}
		else return requestElevator.toByteArray();
	}
	
	
	/*
	 * Takes in a .txt file as a string. 1st and 2nd line of of txt file are discarded(due to the formatting given in project requirements)
	 * Takes the input information and creates a list of Strings that will have the real time inputs as a string. 
	 * 
	 */
	public void fileReader(String fullFile) {
		String text = "";
		int i=0;
		List<String> strings = new ArrayList<String>();
		try {
			FileReader input = new FileReader(fullFile);
			Scanner reader = new Scanner(input);
			reader.useDelimiter("[\n]");
			
			while (reader.hasNext()){
				text = reader.next();
				if (i<=1) {
					i++;
				} else if (i>=2) {
					strings.add(text);
				}
				 
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String elevatorRequestFromFile(String request) {
		String[] req = request.split(" ");
		elevatorRequest = req[3];
		
		return elevatorRequest;
		
	}
	
	
	/*
	 * (Runnable method for Floor Class)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		//fileReader(String fullFile);
		//elevatorRequestFromFile(String request);
		Floor floor1 = new Floor(1);
		Floor floor2 = new Floor(2);
		Floor floor3 = new Floor(3);
		Floor floor4 = new Floor(4);
		
		/* {inside while
		 * if statement
		 * if(whoamI == (number of floor)){
		 * 	returns a byte[]: floor(number of floor).create a packet to schedular(); 
		 * }
		 * 
		 * floor(number of floor).send the byte[] from above if statement to schedular;
		 * 
		 * 
		 * 
		 * 
		 */
		
		while (true) {

		/* FLOOR --> SCHEDULER (0, real_time, 0, whoamI, 0, up_or_down, 0) */
			//requestElevator = responsePacket(floorRequest);
			byte[] requestElevator = new byte[7]; 
			requestElevator = responsePacket();
			int lengthOfByteArray = requestElevator.length;

			// allocate packets
			if(requestElevator != null) {
				try {
					floorSendPacket = new DatagramPacket(requestElevator, lengthOfByteArray, InetAddress.getLocalHost(),
							sendPort_num);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
							/* SCHEDULER --> FLOOR (0, open OR close door, 0)	 */
			byte data[] = new byte[3];
			floorReceivePacket = new DatagramPacket(data, data.length);

			System.out.println("floor_subsystem: Waiting for Packet.\n");
			
			try {
				// Block until a datagram packet is received from receiveSocket.
				floorReceiveSocket.receive(floorReceivePacket);
			} catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}

			openCloseDoor(data[1]);
	}
	
	}

}

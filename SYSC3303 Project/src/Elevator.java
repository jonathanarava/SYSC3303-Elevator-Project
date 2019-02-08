
//no main method
//Output: floor request, 
//Input: Motor control (up, down, stop), door (open, close), Floor number (for display), direction (display)
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Elevator implements Runnable {
	public static String NAMING;
	public int floorRequest;
	private static byte hold = 0x00;
	private static byte up = 0x01;
	private static byte down = 0x02;
	protected int sensor;            // this variable keeps track of the current floor of the elevator
	
	
	DatagramPacket elevatorSendPacket, elevatorReceivePacket;
	DatagramSocket elevatorSendSocket, elevatorReceiveSocket;
	public Elevator() {}
	
	public Elevator(String name) {
		NAMING = name;// mandatory for having it actually declared as a thread object

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

	public  byte[] responsePacket(int floorRequest) {

		// creates the byte array according to the required format
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(0);
		requestElevator.write(1);
		requestElevator.write((byte) floorRequest);
		requestElevator.write((byte) currentFloor(sensor));
		requestElevator.write(0);
		return requestElevator.toByteArray();

	}

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
	
	public int currentFloor(int floorSensor) {  // method to initialize where the elevator starts
		sensor = floorSensor;
		return sensor;
	}
	
	public int runElevator(byte motorDirection, byte motorSpinTime/*, int currentFloor*/) {
		//sensor = currentFloor;				 //sensor is at current floor
        	int time = (int) motorSpinTime;
		if (motorDirection == up || motorDirection == down) {
			while (time > 0){
				try {
					System.out.println(sensor); // sensor = current floor
					Thread.sleep(1000);
					time--;
					if (motorDirection == up) {
						System.out.println("Elevator going up");
						sensor++;               //increment the floor
						currentFloor(sensor);   //updates the current floor
					} else if (motorDirection == down) {
						System.out.println("Elevator going down");
						sensor--;               //decrements the floor
						currentFloor(sensor);   //updates the current floor
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if (motorDirection == hold) {
			currentFloor(sensor);       //updates current floor - in this case nothing changes
		}
		System.out.println(sensor);     //prints out the current floor - in this case destination floor
		return currentFloor(sensor);    //returns and updates the final current of the floor - in this case destination floor
	}
	
	
	//sets Current location of elevator through this setter
	public void setSensor(int currentSensor) {
			sensor = currentSensor;
		}
	
	
	public void run() {
		//System.out.println("Enter floor number: ");
		//floorRequest = 2;
				//Scanner destination = new Scanner(System.in);
				
				//if (destination.nextInt() != 0) {
				//floorRequest = destination.nextInt();
				//} else {
				
				//}
				//destination.close();	
		
		byte[] requestElevator = new byte[3];
		
		/* ELEVATOR --> SCHEDULER (0, FloorRequest, cuurentFloor, 0) */


		 
		Scanner destination = new Scanner(System.in);
		int floorRequest=2;
		int value = destination.nextInt();
		
		if ( value != 0) {
			floorRequest = value;
		} else {
			floorRequest=0;
		}
		destination.close();

		requestElevator = responsePacket(floorRequest);
		System.out.println(requestElevator.toString());
		int lengthOfByteArray = responsePacket(floorRequest).length;

		// allocate sockets, packets
		try {
			elevatorSendPacket = new DatagramPacket(requestElevator, lengthOfByteArray, InetAddress.getLocalHost(),
					23);
			System.out.println("sent");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			elevatorSendSocket.send(elevatorSendPacket);
	      } 
		catch (IOException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }
		
		
		//destination.close();
			//SCHEDULER --> ELEVATOR (0,motorDirection, motorSpinTime, open OR close door, 0)	 



			byte data[] = new byte[5];
			elevatorReceivePacket = new DatagramPacket(data, data.length);

			System.out.println("elevator_subsystem: Waiting for Packet.\n");
			
			try {
				// Block until a datagram packet is received from receiveSocket.
				elevatorReceiveSocket.receive(elevatorReceivePacket);
			} catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}

//			runElevator(data[1], data[2]);
			openCloseDoor(data[3]);

			// send packet for scheduler to know the port this elevator is allocated
			// sendPacket = new DatagramPacket(data,
			// receivePacket.getLength(),receivePacket.getAddress(),
			// receivePacket.getPort());
		//}
		 
	}
}


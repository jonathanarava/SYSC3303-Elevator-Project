
//no main method
//Output: floor request, 
//Input: Motor control (up, down, stop), door (open, close), Floor number (for display), direction (display)
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class Elevator extends Thread {
	public int name;
	public int floorRequest = 8; 
	private static byte hold = 0x00;
	private static byte up = 0x02;
	private static byte down = 0x01;
	protected int sensor;            // this variable keeps track of the current floor of the elevator

	DatagramPacket elevatorSendPacket, elevatorReceivePacket;
	DatagramSocket elevatorSendSocket, elevatorReceiveSocket;
	public Elevator() {}

	public Elevator(int name, int initiateFloor) {
		this.name = name;// mandatory for having it actually declared as a thread object
		sensor = initiateFloor;

		try {
			elevatorSendSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public  byte[] responsePacketRequest(int requestUpdate) {

		/* ELEVATOR --> SCHEDULER (elevator or floor (elevator-21), elevator id(which elevator), FloorRequest/update, curentFloor, up or down, destFloor, instruction)  ( */
		// creates the byte array according to the required format
		
		
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(21);					// elevator
		requestElevator.write(name);	// elevator id
		
		//request/ update
		if(requestUpdate == 1) {
			requestElevator.write(1);	//request/ 
			requestElevator.write((byte) currentFloor(sensor)); // current floor
			requestElevator.write(0);		//up or down
			requestElevator.write(floorRequest);		//dest floor
			requestElevator.write(0);		// instruction
		} else if(requestUpdate == 2) {
			requestElevator.write(2);	//update
			requestElevator.write((byte) currentFloor(sensor)); // current floor
			requestElevator.write(0);		//up or down
			requestElevator.write(0);		//dest floor
			requestElevator.write(0);		// instruction
		}
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

	public int runElevator(byte motorDirection) {
		//sensor = currentFloor;				 //sensor is at current floor
		int time = 1;
		if (motorDirection == up || motorDirection == down) {
			while (time > 0){
				try {
					System.out.println("current floor: " + sensor); // sensor = current floor
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
		System.out.println("current floor: " + sensor); //prints out the current floor - in this case destination floor
		return currentFloor(sensor);    //returns and updates the final current of the floor - in this case destination floor
	}


	//sets Current location of elevator through this setter
	public void setSensor(int currentSensor) {
		sensor = currentSensor;
	}

/*	public synchronized void sendPacket() throws InterruptedException {
		byte[] requestElevator = new byte[8];

		 ELEVATOR --> SCHEDULER (elevator or floor (elevator-21), elevator id(which elevator), FloorRequest/update, curentFloor, up or down, destFloor, instruction)  ( 

		System.out.print("Enter floor number: ");
		Scanner destination = new Scanner(System.in);
		int floorRequest=1;
		int value = destination.nextInt();
		if ( value != 0) {
			floorRequest = value;
		} else {
			destination.close();
		}
		

		
		requestElevator = responsePacketRequest(1);
		//updateElevator = responsePacketRequest(update);
		//System.out.println(requestElevator.toString());

		try {

			elevatorSendPacket = new DatagramPacket(requestElevator, requestElevator.length, InetAddress.getLocalHost(),
					23);

		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			elevatorSendSocket.send(elevatorSendPacket);
			System.out.println("sent");
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		//}
	}

	public synchronized void receivePacket() {
		//SCHEDULER --> ELEVATOR (0, motorDirection, motorSpinTime, open OR close door, 0)	 

		byte data[] = new byte[5];
		elevatorReceivePacket = new DatagramPacket(data, data.length);

		System.out.println("elevator_subsystem: Waiting for Packet.\n");

		try {
			// Block until a datagram packet is received from receiveSocket.
			elevatorSendSocket.receive(elevatorReceivePacket);
			System.out.print("Received from scheduler: ");
			System.out.println(Arrays.toString(data));
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		runElevator(data[1]);
		openCloseDoor(data[2]);

		// send packet for scheduler to know the port this elevator is allocated
		// sendPacket = new DatagramPacket(data,
		// receivePacket.getLength(),receivePacket.getAddress(),
		// receivePacket.getPort());
		//}
	}
*/

/*	public void run() {
		//System.out.println("Enter floor number: ");
		//floorRequest = 2;
		//Scanner destination = new Scanner(System.in);

		//if (destination.nextInt() != 0) {
		//floorRequest = destination.nextInt();
		//} else {

		//}
		//destination.close();	

		try {
			sendPacket();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		receivePacket();

		//destination.close();
*/
	}



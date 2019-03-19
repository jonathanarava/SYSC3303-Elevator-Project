
//no main method
//Output: floor request, 
//Input: Motor control (up, down, stop), door (open, close), Floor number (for display), direction (display)
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Elevator extends Thread {

	// UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES
	private static final byte HOLD = 0x04;// elevator is in hold state
	private static final byte UPDATEDISPLAY = 0x05;
	private static final byte UP = 0x01;// elevator is going up
	private static final byte DOWN = 0x02;// elevator is going down
	private static final byte STOP = 0x03;
	private static final int ELEVATOR_ID = 21;// for identifying the packet's source as elevator
	private static final int FLOOR_ID = 69;// for identifying the packet's source as floor
	private static final int SCHEDULER_ID = 54;// for identifying the packet's source as scheduler
	private static final int DOOR_OPEN = 1;// the door is open when == 1
	private static final int DOOR_DURATION = 4;// duration that doors stay open for
	private static final int REQUEST = 1;// for identifying the packet sent to scheduler as a request
	private static final int UPDATE = 2;// for identifying the packet sent to scheduler as a status update

	public byte motorDirection; // make getters and setters:
	public boolean hasRequest = true; // make getters and setters: This Boolean will be set to true when the Elevator
										// Intermediate wants a specific elevator thread to do something.
										// if hasRequest is true, then the Elevator thread will not send another
										// request. Ie, he needs to take care of the job he is told to do by the
										// intermediate
										// before he takes more real time requests by the people. Incidentally,
										// hasRequest == true means that the elevator should move up or down a floor.
	public boolean hasRTRequest = false; // Real time variable for *****TESTING LINE 1.0******
	
	public boolean isUpdate = false;	// This boolean is set to true in the ElevatorIntermediate, if the elevator intermediate is expecting an update from the elevator
	public boolean isGoingUp;
	
	public int elevatorNumber;
	public int RealTimefloorRequest;

	protected int sensor; // this variable keeps track of the current floor of the elevator

	DatagramPacket elevatorSendPacket, elevatorReceivePacket;
	DatagramSocket elevatorSendSocket, elevatorReceiveSocket;

	private List<byte[]> elevatorTable;

	public Elevator(int name, int initiateFloor, List<byte[]> elevatorTable, int RealTimefloorRequest) {
		this.elevatorNumber = name; // mandatory for having it actually declared as a thread object
		this.elevatorTable = elevatorTable;
		sensor = initiateFloor;
		this.RealTimefloorRequest = RealTimefloorRequest;
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
	
	/**
	 * 
	 * @param requestUpdate
	 * @return byte[] to be put on the synchronized table
	 */
	public byte[] responsePacketRequest(int requestUpdate) {

		/*
		 * ELEVATOR --> SCHEDULER (elevator or floor (elevator-21), elevator id(which
		 * elevator), FloorRequest/update, curentFloor, up or down, destFloor,
		 * instruction) (
		 */
		// creates the byte array according to the required format

		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(ELEVATOR_ID); // elevator
		requestElevator.write(elevatorNumber); // elevator id

		// request/ update
		if (requestUpdate == REQUEST) {
			requestElevator.write(REQUEST); // request/
			requestElevator.write((byte) setSensor(sensor)); // current floor
			requestElevator.write(0); // up or down
			requestElevator.write(RealTimefloorRequest); // dest floor
			requestElevator.write(0); // instruction
		} else if (requestUpdate == UPDATE) {
			requestElevator.write(UPDATE); // update
			requestElevator.write((byte) setSensor(sensor)); // current floor
			requestElevator.write(0); // up or down
			requestElevator.write(RealTimefloorRequest); // dest floor
			requestElevator.write(0); // instruction
		}
		return requestElevator.toByteArray();
	}

	/*
	 * COMENTING OUT FOR TESTING REASONS, DO NOT DELETE public String
	 * openCloseDoor(byte door) { String msg; if (door == DOOR_OPEN) { msg =
	 * "Doors are open."; System.out.println(msg); try { int i = 4 ; while (i != 0)
	 * { System.out.format("Seconds until elevator door closes: %d second \n", i);
	 * i--; Thread.sleep(1000); } } catch (InterruptedException e) {
	 * e.printStackTrace(); } } else { msg = "Doors are closed.";
	 * System.out.println(msg); } return msg; }
	 */
	private int setSensor(int floorSensor) { // method to initialize where the elevator starts
		sensor = floorSensor;
		return sensor;
	}
	
	public void updateDisplay() {
		System.out.println("On Floor: " + sensor);
		if(isGoingUp) {
			System.out.println("Going Up");
		}
		else if(!isGoingUp) {
			System.out.println("Going Down");
		}
	}

	public int runElevator() {
		// sensor = setSensor; //sensor is at current floor
		if (motorDirection == UP || motorDirection == DOWN) {
			//try {

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (motorDirection == UP) {
					System.out.println("Elevator is going up...");
					isGoingUp = true;
					sensor++; // increment the floor
					setSensor(sensor); // updates the current floor
				} else if (motorDirection == DOWN) {
					System.out.println("Elevator is going down...");
					isGoingUp = false;
					sensor--; // decrements the floor
					setSensor(sensor); // updates the current floor
				} else if (motorDirection == HOLD || motorDirection == STOP || motorDirection == UPDATEDISPLAY ) {
					
				}
			//} catch (InterruptedException e) {
				//e.printStackTrace();
			//}
		} else if (motorDirection == HOLD) {
			setSensor(sensor); // brings the elevator back to holding state. 
		} else if(motorDirection == UPDATEDISPLAY) {
			setSensor(sensor);	// update the display so the current floor is shown
		}
		return setSensor(sensor); // returns and updates the final current of the floor - in this case destination
		// floor
	}

	// sets Current location of elevator through this setter

	public synchronized void sendPacket(int requestOrUpdate) {
		synchronized (elevatorTable) {
			while (elevatorTable.size() != 0) {
				try {
					elevatorTable.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			elevatorTable.add(responsePacketRequest(requestOrUpdate));
			//isUpdate = false;
			elevatorTable.notifyAll();
		}
	}

	public void run() {
		while (true) {
			// while(true) to activate all elevator threads in this system
			while(hasRequest) {
				sendPacket(1);
				hasRequest = !hasRequest;
			}
			 
			while(!hasRequest) {
				System.out.println("Reached here");
				if (motorDirection == UP || motorDirection == DOWN) {
					runElevator();
					sendPacket(2);
				} else if (motorDirection == UPDATEDISPLAY) {
					updateDisplay();
					sendPacket(2);
					//set the lights sensors and stuff to proper value
					isUpdate = false;
				} else if (motorDirection == STOP) {
					sendPacket(2);
				} else if (motorDirection == HOLD) {
					while(!hasRequest) {
						try {
							wait(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			} 
		}
	}

}

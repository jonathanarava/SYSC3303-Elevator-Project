
//no main method
//Output: floor request, 
//Input: Motor control (up, down, stop), door (open, close), Floor number (for display), direction (display)
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;

/**
 * Each elevator thread acts as one of the 4 elevators. Takes requests from the
 * user to go the floors. There is display that shows movement of the elevators
 * and opening and closing of doors. Takes Arguments at Initialization; How many
 * elevators on the system(max 4), and where those what floors those elevators
 * will have requests to. (if request is 0, that elevator will imediately go
 * into holding state)
 * 
 * @author Group 5
 *
 */
public class Elevator extends Thread {

	// UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES
	// States
	private static final byte UP = 0x01;// elevator is going up
	private static final byte DOWN = 0x02;// elevator is going down
	private static final byte STOP = 0x03;
	private static final byte HOLD = 0x04;// elevator is in hold state
	private static final byte UPDATE_DISPLAY = 0x05;
	private static final byte SHUT_DOWN = 0x06;// for shutting down a hard fault problem elevator
	private static final byte ERROR = (byte) 0xE0;// an error has occured
	// Errors
	private static final byte DOOR_ERROR = (byte) 0xE1;
	private static final byte MOTOR_ERROR = (byte) 0xE2;
	// still error states between 0xE3 to 0xEE for use
	private static final byte OTHER_ERROR = (byte) 0xEF;
	private static final byte NO_ERROR = 0x00;
	// Object ID
	private static final int ELEVATOR_ID = 21;// for identifying the packet's source as elevator
	private static final int FLOOR_ID = 69;// for identifying the packet's source as floor
	private static final int SCHEDULER_ID = 54;// for identifying the packet's source as scheduler
	// Values for Running
	private static final int DOOR_OPEN = 1;// the door is open when == 1
	private static final int DOOR_CLOSE = 3; // the door is closed when == 3
	private static final int DOOR_DURATION = 4;// duration (in seconds) that doors stay open for

	private static final int REQUEST = 1;// for identifying the packet type sent to scheduler as a request
	private static final int UPDATE = 2;// for identifying the packet type sent to scheduler as a status update
	private static final int MAKE_STOP = 3;//
	private static final int PLACE_ON_HOLD = 4;
	private static final int UPDATE_DISPLAYS = 5;
	// private static final int SHUT_DOWN=6;//for shutting down a hard fault problem
	// elevator
	private static final int UNUSED = 0;// value for unused parts of data
	private static final int INITIALIZE = 8;// for first communication with the scheduler
	private static final byte[] ELEVATOR_INITIALIZE_PACKET_DATA = { ELEVATOR_ID, 0, INITIALIZE, 0, 0, 0, 0, 0 };
	private static final byte[] FLOOR_INITIALIZE_PACKET_DATA = { FLOOR_ID, 0, INITIALIZE, 0, 0, 0, 0, 0 };
	private static final int DOOR_CLOSE_BY = 6;// door shouldn't be open for longer than 6 seconds

	public byte motorDirection; // make getters and setters:
	public boolean hasRequest = true; // make getters and setters: This Boolean will be set to true when the Elevator
	// Intermediate wants a specific elevator thread to do something.
	// if hasRequest is true, then the Elevator thread will not send another
	// request. Ie, he needs to take care of the job he is told to do by the
	// intermediate
	// before he takes more real time requests by the people. Incidentally,
	// hasRequest == true means that the elevator should move up or down a floor.
	public boolean hasRTRequest = false; // Real time variable for *****TESTING LINE 1.0******

	public boolean dealWith = false; // dealWith boolean is set by the Intermediate class for a specific elevator if
										// THAT SPECIFIC elevator has a job set by scheduler it needs to dealWith

	private int motionOfMotor; // Directory of the motor. This will be set by elevatorDirection, which is the
								// Instruction that is sent by the scheduler

	public boolean isUpdate = false; // This boolean is set to true in the ElevatorIntermediate, if the elevator
										// intermediate is expecting an update from the elevator
	public boolean isGoingUp;
	private boolean elevatorBroken = false; // whether the elevator is broken or not

	private int elevatorNumber;
	private int RealTimefloorRequest;

	protected int sensor; // this variable keeps track of the current floor of the elevator

	DatagramPacket elevatorSendPacket, elevatorReceivePacket;// Datagram packet for sending and receiving
	DatagramSocket elevatorSendSocket, elevatorReceiveSocket;// Datagram socket for sending and receiving

	private List<byte[]> elevatorTable;
	private boolean doorStatusOpen = false; // whether the doors are open(true) or closed (false)
	private long doorOpenTime, doorCloseTime;// for error checking that doors are closed within time

	public Elevator(int name, int initiateFloor, List<byte[]> elevatorTable, int RealTimefloorRequest) {
		this.elevatorNumber = name; // mandatory for having it actually declared as a thread object
		this.elevatorTable = elevatorTable;
		sensor = initiateFloor;
		this.RealTimefloorRequest = RealTimefloorRequest;
	}

	/**
	 * 
	 * @param requestUpdateError
	 * @return byte[] to be put on the synchronized table
	 */
	public byte[] createResponsePacketData(int requestUpdateError, byte errorType) {// create the Data byte[] for
																					// the response packet to be
																					// sent to the scheduler

		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(ELEVATOR_ID); // identification as an elevator, instead of floor, or scheduler
		requestElevator.write(elevatorNumber); // identity of this particular elevator object

		// request or update data
		switch (requestUpdateError) {
		case REQUEST:
			break;
		case UPDATE:
			break;
		case ERROR:
			requestElevator.write((byte) setSensor(sensor)); // current floor
			requestElevator.write(UNUSED); // up or down (not used, only for Floors)
			requestElevator.write(RealTimefloorRequest); // dest floor
			requestElevator.write(requestUpdateError); // instruction (not used, only from the scheduler)
			requestElevator.write(errorType); // error ID
			return requestElevator.toByteArray();
		}
		requestElevator.write(requestUpdateError);
		requestElevator.write((byte) setSensor(sensor)); // current floor
		requestElevator.write(UNUSED); // up or down (not used, only for Floors)
		requestElevator.write(RealTimefloorRequest); // dest floor
		requestElevator.write(UNUSED); // instruction (not used, only from the scheduler)
		requestElevator.write(UNUSED); // no errors
		return requestElevator.toByteArray();
	}

	/**
	 * 
	 * @param doorOpenCloseError: Instruction to Open or Close the door of the
	 *        elevator
	 */
	public void openCloseDoor(int doorOpenCloseError) {
		// String msg;

		if (doorOpenCloseError == DOOR_OPEN) { // instruction is to open the doors for DOOR_DURATION seconds

			// msg = "Opening Doors";
			// System.out.println(msg);
			System.out.println("Opening Doors");
			doorStatusOpen = true;// open the doors
			doorOpenTime = System.nanoTime();// time that the doors opened
			/*
			 * try { int i = DOOR_DURATION ; while (i != 0){
			 * System.out.format("Seconds until elevator door closes: %d second \n", i);
			 * i--; Thread.sleep(1000); //travel time per floor } } catch
			 * (InterruptedException e) { e.printStackTrace(); } else { msg =
			 * "Doors are closed."; }
			 */
			for (int i = DOOR_DURATION; i > 0; i--) {
				System.out.format("Seconds until elevator door closes: %d second \n", i);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} // 1 sec
				System.out.format("Should be calling to Close the Doors");
			}
			// System.out.println()
			// System.out.println(msg);
		} else if (doorOpenCloseError == DOOR_CLOSE) {
			System.out.println("Closing Doors");
			doorStatusOpen = false;
			doorCloseTime = System.nanoTime();// time when the doors closed
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Doors Closed");
		} else {// error
			sendPacket(ERROR, DOOR_ERROR);
		}
		// check that the doors were closed and done so on time
		if ((doorCloseTime - doorOpenTime) > DOOR_CLOSE_BY * 1000000000) {
			sendPacket(ERROR, DOOR_ERROR);
		}
		// return msg;
	}

	/**
	 * Puts the elevator into out of order mode during a hard fault. "Emergency
	 * services" are automatically contacted
	 */
	public void shutDown() {
		motionOfMotor = STOP;
		System.out.println("Elevator Out of Order, Maintenance and Emergency Fire Services have been Contacted");
		if (elevatorBroken == true) {
			try {// wait for 1000
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * fixes the broekn elevator, lets know in the console that the elevator has
	 * been marked "fixed"
	 */
	public void fixElevator() {
		elevatorBroken = false;
		System.out.println("Elevator has been fixed");
	}

	/**
	 * 
	 * @param int floorSensor will be set as the current location of the elevator
	 * @return returns sensor(current location of the elevator)
	 */
	private int setSensor(int floorSensor) { // method to initialize where the elevator starts
		sensor = floorSensor;
		return sensor;
	}

	/**
	 * Updates the display(Console) of the elevator depending on the current state
	 * of the elevator
	 */
	public void updateDisplay() {
		System.out.println("On Floor: " + sensor);
		if (isGoingUp) {
			System.out.println("Going Up");
		} else if (!isGoingUp) {
			System.out.println("Going Down");
		}
	}

	/**
	 * Turns the elevator Motor up or down floors when instructed by the scheduler.
	 */
	public void runElevator() {
		// for testing
		try {// wait for 1000
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// for
		switch (motionOfMotor) {
		case UP:
			System.out.println("Elevator is going up...");
			isGoingUp = true;
			sensor++; // increment the floor
			setSensor(sensor); // updates the current floor
			break;
		case DOWN:
			System.out.println("Elevator is going down...");
			isGoingUp = false;
			sensor--; // decrements the floor
			setSensor(sensor); // updates the current floor
			break;
		}
	}

	// sets Current location of elevator through this setter

	public synchronized void sendPacket(int requestUpdateError, byte sendErrorType) {
		synchronized (elevatorTable) {
			while (elevatorTable.size() != 0) {// wait for an opening to send the packet
				try {
					elevatorTable.wait(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			elevatorTable.add(createResponsePacketData(requestUpdateError, sendErrorType));
			elevatorTable.notifyAll();
		}
	}

	/**
	 * Run method for the Elevator thread that will take care of the elevator
	 * states, instructions, and real time requests when the system is in use
	 */
	public void run() {
		while (true) {
			// while(true) to activate all elevator threads in this system
			while (hasRequest) {// send request
				sendPacket(1, NO_ERROR);
				// hasRequest = !hasRequest;
				hasRequest = false;
			}

			while (!hasRequest) {// send updates
				while (dealWith) {
					switch (motorDirection) {
					case UP:
						motionOfMotor = motorDirection;
						runElevator();
						dealWith = !dealWith;
						sendPacket(2, NO_ERROR);
						break;

					case DOWN:
						motionOfMotor = motorDirection;
						runElevator();
						dealWith = !dealWith;
						sendPacket(2, NO_ERROR);
						break;

					case UPDATE_DISPLAY:
						switch (motionOfMotor) {
						case UP:
							runElevator();
							break;

						case DOWN:
							runElevator();
							break;
						}
						updateDisplay();
						dealWith = !dealWith;
						sendPacket(2, NO_ERROR);
						break;
					case STOP:
						motionOfMotor = STOP;
						dealWith = !dealWith;
						openCloseDoor(DOOR_OPEN);
						sendPacket(2, NO_ERROR);
						break;
					case HOLD:
						motionOfMotor = HOLD;
						System.out.println("Reached Hold state in elevator");
						dealWith = !dealWith;
						break;
					case SHUT_DOWN:
						shutDown();
						break;

					}
				}
				break;

			}
		}
	}

}

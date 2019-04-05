import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.lang.*;
import java.util.concurrent.TimeUnit;//for measuring time to respond

public class SchedulerSeperateAlgorithm {
//elevator
	public static final int EL_RECEIVEPORTNUM = 369;
	public static final int EL_SENDPORTNUM = 159;
//floor
	public static final int FL_RECEIVEPORTNUM = 488;
	public static final int FL_SENDPORTNUM = 1199;

	private static final int RECEIVEPORTNUM = 369;
	
	//UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES
		//States
		private static final byte UP = 0x01;// elevator is going up
		private static final byte DOWN = 0x02;// elevator is going down
		private static final byte STOP = 0x03;
		private static final byte HOLD = 0x04;// elevator is in hold state
		private static final byte UPDATE_DISPLAY = 0x05;
		private static final byte ERROR=(byte)0xE0;//an error has occured
		//Errors
		private static final byte DOOR_ERROR=(byte)0xE1;
		private static final byte MOTOR_ERROR=(byte)0xE2;
		//still error states between 0xE3 to 0xEE for use
		private static final byte OTHER_ERROR=(byte)0xEF; 
		private static final byte NO_ERROR=(byte)0x00;
		//Object ID
		private static final int ELEVATOR_ID = 21;// for identifying the packet's source as elevator
		private static final int FLOOR_ID = 69;// for identifying the packet's source as floor
		private static final int SCHEDULER_ID = 54;// for identifying the packet's source as scheduler
		//Values for Running
		private static final int DOOR_OPEN = 1;// the door is open when == 1
		private static final int DOOR_CLOSE = 3; // the door is closed when == 3 
		private static final int DOOR_DURATION = 4;// duration (in seconds) that doors stay open for
		private static final int TRAVEL_TIME_PER_FLOOR=1;
		//Instructions
		private static final int REQUEST = 1;// for identifying the packet type sent to scheduler as a request
		private static final int UPDATE = 2;// for identifying the packet type sent to scheduler as a status update
		private static final int MAKE_STOP=3;//
		private static final int PLACE_ON_HOLD=4;
		private static final int UPDATE_DISPLAYS=5;
		private static final int SHUT_DOWN=6;//for shutting down a hard fault problem elevator
		private static final int FIX_ELEVATOR=7;//
		private static final int INITIALIZE=8;//for first communication with the scheduler
		private static final int UNUSED=0;// value for unused parts of data 
		private static final int DOOR_CLOSE_BY=6;//door shouldn't be open for longer than 6 seconds

	// Packets and sockets required to connect with the Elevator and Floor class

	public static DatagramSocket schedulerReceiveSocket, schedulerElevatorSendSocket,schedulerFloorSendSocket;//schedulerSocketSendReceiveElevator, schedulerSocketSendReceiveFloor;
	public static DatagramPacket schedulerReceivePacket,schedulerSendPacket;//schedulerElevatorSendPacket, schedulerElevatorReceivePacket, schedulerFloorSendPacket,schedulerFloorReceivePacket;
	
	//public static int PORTNUM = 69;
	//VARIABLES
	//private static byte data[] = new byte[8];
	private static int packetOrigin;// data[0]
	private static int packetElement;// data[1];
	private static int packetType;// data[2];
	private static int packetCurrentFloor;// data[3];
	private static int packetDirection;// data[4];
	private static int packetRequestFloor;// data[5];
	private static int schedulerInstruction; //for placing data[6]
	//data[6] is Scheduler's Instruction and should be left empty/ not received
	private static int packetError;// data[7];

	// SETTING THE NUMBER OF ELEVATORS AND FLOORS PRESENT IN THE SYSTEM
	private static int numElevators;
	private static int numFloors;

	// scheduling alogrithm variable declaration
	private static int elevatorCurrentFloor[];// = new int[numElevators];
	private static int elevatorStatus[];// = new int[numElevators];
	private static int elevatorNextStop[];// = new int[numElevators];
	private static int elevatorNumStops[];// = new int[numElevators];
	private static int elevatorHighestRequestFloor[];// = new int[numElevators];
	private static int elevatorLowestRequestFloor[];// = new int[numElevators];

	// temporary sorting algorithm variables
	//private static int floorRequestDirection;
	private static LinkedList<Integer>[] elevatorRequestsUp;// = new LinkedList[numElevators];
	private static LinkedList<Integer>[] elevatorStopsUp;// = new LinkedList[numElevators];
	private static LinkedList<Integer>[] elevatorRequestsDown;// = new LinkedList[numElevators];
	private static LinkedList<Integer>[] elevatorStopsDown;// = new LinkedList[numElevators];

	private static byte[] sendData = new byte[8];
	private static byte[] receiveData= new byte[8];	
	private static InetAddress elevatorAddress,floorAddress;
	
	//calculation of response time
	private static int[] responseTime;// response time of individual elevators to got to a floor request
	private static int indexOfFastestElevator = 0;// index of array for which elevator is fastest
	//private static int temp;// temporary for finding the fastest response time
	//timing measurement
	private static long respondStart, respondEnd;// variables for the measurements to respond
	//initialization
	private static boolean elevatorInitialized=false;
	private static boolean floorInitialized=false;
	//for use at initialization, both elevatorIntermediate and floorIntermediate need to send their initialization packets
	//-otherwise the update packets sent will not have a valid socket address to do so with
	//-when both have been received, both of the below variables will be TRUE, thus then the scheduler will send a response packet
	//		notifying each that they may continue
//CONSTRUCTORS
	public SchedulerSeperateAlgorithm(boolean a) {
		// Constructor for Junit Testing
	}
	public SchedulerSeperateAlgorithm() {
		try {
			schedulerReceiveSocket=new DatagramSocket(RECEIVEPORTNUM);
			schedulerElevatorSendSocket=new DatagramSocket();
			schedulerFloorSendSocket=new DatagramSocket();
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}
	//INITIALIZATION METHODS
	private static void linkedListInitialization() {
		elevatorRequestsUp = new LinkedList[numElevators];
		elevatorStopsUp= new LinkedList[numElevators];
		elevatorRequestsDown = new LinkedList[numElevators];
		elevatorStopsDown = new LinkedList[numElevators];
		for (int i = 0; i < numElevators; i++) {
			//System.out.println("linkedListInitialization for loop variable i: "+i);
			elevatorRequestsUp[i] = new LinkedList<Integer>();
			elevatorStopsUp[i] = new LinkedList<Integer>();
			elevatorRequestsDown[i] = new LinkedList<Integer>();
			elevatorStopsDown[i] = new LinkedList<Integer>();
			elevatorStatus[i]=HOLD;
		}
	}
	private static void schedulerInitilization() {
		elevatorCurrentFloor = new int[numElevators];
		elevatorStatus = new int[numElevators];
		elevatorNextStop = new int[numElevators];
		elevatorNumStops = new int[numElevators];
		elevatorHighestRequestFloor = new int[numElevators];
		elevatorLowestRequestFloor = new int[numElevators];
		linkedListInitialization();
		createSendingData(0,0,0, INITIALIZE);
		elevatorFloorSendPacket(ELEVATOR_ID);
		createSendingData(0,0,0, INITIALIZE);
		elevatorFloorSendPacket(FLOOR_ID);
		System.out.println("Scheduler is INITIALIZED and may proceed with operations\n");
	}
	private static void scheduleElevatorInitialize() {
		if (packetType==INITIALIZE) {
			elevatorInitialized=true;
			numElevators=packetElement;
			elevatorAddress=schedulerReceivePacket.getAddress();
			System.out.println("Received Elevator Initialization \n");
			if (floorInitialized==true) {
				schedulerInitilization();
			}
		}
	}
	private static void scheduleFloorInitialize() {
		floorInitialized=true;
		System.out.println("Received Floor Initialization\n");
		numFloors=packetElement;
		floorAddress=schedulerReceivePacket.getAddress();
		
		if (elevatorInitialized==true) {
			schedulerInitilization();
		}
	}
	//SCHEDULING ALGORITHMS FOR ELEVATOR
	private static void scheduleElevatorRequest() {
		// CHECK IF THE REQUEST IS A DUPLICATE, if so then ignore
		if (elevatorStatus[packetElement] != HOLD) {// elevator is not in hold mode, currently moving
			// check direction
			if (elevatorStatus[packetElement] == UP) {// elevator is going up
				if (packetCurrentFloor < packetRequestFloor) {// we haven't reached that floor yet and can still stop
					// in time
					if (elevatorStopsUp[packetElement].contains(packetRequestFloor)) {// check if the request
						// is already in the linked list (duplicate) if so then do nothing, else add it
						// do nothing, don't want duplicates
					} else {
						elevatorStopsUp[packetElement].add(packetRequestFloor);// add to the stopsUp
						// linkedlist for the current elevator
					}
				} else {// the stop has already been missed
					elevatorStopsDown[packetElement].add(packetRequestFloor);
					// add it to the stopDown linked list
				}
			} else {// elevator is going down
				if (packetCurrentFloor > packetRequestFloor) {// we haven't reached that floor yet and can still stop
					// in time
					if (elevatorStopsDown[packetElement].contains(packetRequestFloor)) {// check if the
						// request is already in the linked list (duplicate) if so then do nothing,
						// else add it do nothing, don't want duplicates
					} else {
						// add to the stopsDown linkedlist for the current elevator
						elevatorStopsDown[packetElement].add(packetRequestFloor);
					}
				} else {// the stop has already been missed
					elevatorStopsUp[packetElement].add(packetRequestFloor);
					// add it to the stopDown linked list
				}
			}
		} else {// currently in hold mode, we can fulfill that request immediately
			if (packetCurrentFloor < packetRequestFloor) {// we are below the destination floor, we need to go up
				elevatorStopsUp[packetElement].add(packetRequestFloor);
				elevatorStatus[packetElement] = UP;
				createSendingData(packetElement, UNUSED, UNUSED, UP);// 
			} else if (packetCurrentFloor > packetRequestFloor) {// we are above the destination floor, we need to go
				// down
				elevatorStopsDown[packetElement].add(packetRequestFloor);
				elevatorStatus[packetElement] = DOWN;
				createSendingData(packetElement, UNUSED, UNUSED, DOWN);// 
			}
			// currently in hold mode and remain in hold mode
			else if (packetCurrentFloor ==packetRequestFloor) {
				elevatorStatus[packetElement] = HOLD;// place back into hold state
				createSendingData(packetElement, UNUSED, UNUSED, HOLD);
			}
			elevatorFloorSendPacket(ELEVATOR_ID);// originally the only send in the method
		}
	}
	private static void scheduleElevatorUpdate() {
		if (elevatorStatus[packetElement] == UP) {// direction that the elevator is going is up
			if (elevatorStopsUp[packetElement].contains(packetCurrentFloor)) {// we have reached a
				// destination stop and
				// need to stop the
				// elevator
				// open the doors (closes automatically after preallocated duration)
				// create sendpacket to stop the elevator (and open, close the door)
				// send the sendPacket
				// remove the stop from goingup linked list
				// check if there are more stops
				createSendingData(packetElement, UNUSED, UNUSED, MAKE_STOP);//
				elevatorFloorSendPacket(ELEVATOR_ID);

				if (elevatorStopsUp[packetElement].size() == 1) {// no more stops Up
					// check if there are more requests
					if (elevatorRequestsUp[packetElement].isEmpty()) {// no missed floors for going Up
						// do nothing
					} else {// there are outstanding requests to go Up
						elevatorStopsUp[packetElement] = elevatorRequestsUp[packetElement];
						// the requests to go Up can now be met once we've finished going down first
						elevatorRequestsUp[packetElement].clear();
					}
					// check if there are more stops down
					if (elevatorStopsDown[packetElement].isEmpty()) {// no more stops
						try {
							Thread.currentThread().sleep(2);
						} catch (InterruptedException e) { // THIS SLEEP IS HERE TO GIVE THE ELEVATOR ENOUGH
							// TIME RECEIVE THE PACKET FOR 'HOLD' DO NOT REMOVE
							// TODO Auto-generated catch block // UNLESS YOU KNOW WHAT YOU'RE DOING
							e.printStackTrace();
						}
						createSendingData(packetElement, UNUSED, UNUSED, HOLD);// 4: place on hold state
						elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet immediately, otherwise will be
						// overwritten before being sent at the very end
						elevatorStopsUp[packetElement].clear();

					} else {// we have stops to go up, start fulfilling those
						createSendingData(packetElement, UNUSED, UNUSED, UP);//
						elevatorFloorSendPacket(ELEVATOR_ID);
					}
				} else {// finished stopping for destination floor, continue going Up to fulfill other
					// stops create and send SendPacket to restart the motor/ have the motor in the up
					// direction
					createSendingData(packetElement, UNUSED, UNUSED, UP);
					elevatorFloorSendPacket(ELEVATOR_ID);
				}

			} else {// not a floor that we need to stop at
				// Look at this.
				System.out.println(
						"reached else of Eleavtor Update while going UP; not a floor that is contained in the stop list");
				createSendingData(packetElement, packetCurrentFloor,packetDirection, UPDATE_DISPLAYS);
				elevatorFloorSendPacket(ELEVATOR_ID);
				elevatorFloorSendPacket(FLOOR_ID);//STATUS UPDATES SHOULD BE SENT TO ALL FLOORS
			}

		} else {// elevator is going down
			if (elevatorStopsDown[packetElement].contains(packetCurrentFloor)) {// we have reached a
				// destination stop and
				// need to stop the
				// elevator
				// open the doors (closes automatically after preallocated duration)
				// create sendpacket to stop the elevator (and open, close the door)
				// send the sendPacket
				// remove the stop from goingup linked list
				// check if there are more stops
				createSendingData(packetElement, UNUSED, UNUSED, MAKE_STOP);// 3: make a stop
				elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet immediately, otherwise will be
				// overwritten before being sent at the very end
				if (elevatorStopsDown[packetElement].isEmpty()) {
					// check if there are more requests
					if (elevatorRequestsDown[packetElement].isEmpty()) {// no missed floors for going
						// down
						// do nothing
					} else {// there are outstanding requests to go down
						elevatorStopsDown[packetElement] = elevatorRequestsDown[packetElement];
						// the requests to go down can be met once we have finished going up first
						elevatorRequestsDown[packetElement].clear();
					}
					// check if there are more stops up
					if (elevatorStopsUp[packetElement].isEmpty()) {// no more stops
						// create and send sendPacket to hold the motor
						createSendingData(packetElement, UNUSED, UNUSED, HOLD);// 4: hold
						elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet immediately, otherwise will be
						// overwritten before being sent at the very end
						elevatorStopsDown[packetElement].clear();
					} else {// we have stops to go up, start fulfilling those
						// create and send SendPacket for the motor to go Down
						createSendingData(packetElement, UNUSED, UNUSED, DOWN);// 2: down
						elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet immediately, otherwise will be
						// overwritten before being sent at the very end
					}
				} else {// finished stopping for a destination floor, continue fulfilling other stops
					// create and send SendPacket to restart the motor/ have the motor in the down
					// direction
					createSendingData(packetElement, UNUSED, UNUSED, DOWN);// 2: down
					// IMPORTANT:
					// this packet sending may not be necessary and could be what's causing the
					// double sending at the very beginning
					elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet immediately, otherwise will be
					// overwritten before being sent at the very end
				}
			} else {// not a floor that we need to stop at
				// do nothing
				System.out.println(
						"reached else of Eleavtor Update while going Down; not a floor that is contained in the stop list");
			}

		}

		// }
		// update floor number and direction displays for elevator and all floors
		//createSendingData(0, 0, elevatorDirection, 5);// 5: status update
		createSendingData(packetElement, packetCurrentFloor, packetDirection, UPDATE_DISPLAYS);
		elevatorFloorSendPacket(FLOOR_ID);//STATUS UPDATES SHOULD BE SENT TO ALL FLOORS
		elevatorFloorSendPacket(ELEVATOR_ID);
	}
	
	//SCHEDULING ALGORITHMS FOR FLOOR
	private static void scheduleFloorRequest() {
		//responseTime = calculateResponseTimes(packetElement, packetDirection);//response times of the elevators to reach the floor in the requested direction
		calculateResponseTimes(packetElement, packetDirection);//response times of the elevators to reach the floor in the requested direction
		fastestElevator();
		// RECALL THE FLOOR CALLING SHOULD ONLY LET PASSENGERS IN WHEN IN THE CHOSEN
		// DIRECTION (UP/DOWN)
		if (elevatorStatus[indexOfFastestElevator] != HOLD) {// not in hold
			if (elevatorStatus[indexOfFastestElevator] == UP) {// elevator is going up
				if (packetDirection == elevatorStatus[indexOfFastestElevator]) {// floor is requesting to
					// go up also
					if (packetElement > packetCurrentFloor) {// still time
						if (elevatorStopsUp[indexOfFastestElevator].contains(packetElement)) {
							// already have that stop requested, don't want to duplicate
						} else {
							elevatorStopsUp[indexOfFastestElevator].add(packetElement);// add to stops list
						}
					} else {// missed
						if (elevatorRequestsUp[indexOfFastestElevator].contains(packetElement)) {
							// already have that stop requested, don't want to duplicate
						} else {
							elevatorRequestsUp[indexOfFastestElevator].add(packetElement);// add the floor
							// to requests
						}
					}
				} else {// elevator is currently fulfilling down stops
					if (elevatorRequestsUp[indexOfFastestElevator].contains(packetElement)) {
						// already have that stop requested, don't want to duplicate
					} else {
						elevatorRequestsUp[indexOfFastestElevator].add(packetElement);// add the floor to
						// requests
					}
				}
			} else {// elevator is going down
				if (packetDirection == elevatorStatus[indexOfFastestElevator]) {// floor is requesting to
					// go Down also
					if (packetElement < packetCurrentFloor) {// still time
						if (elevatorStopsDown[indexOfFastestElevator].contains(packetElement)) {
							// already have that stop requested, don't want to duplicate
						} else {
							elevatorStopsDown[indexOfFastestElevator].add(packetElement);// add to stops
							// list
						}
					} else {// missed
						if (elevatorRequestsDown[indexOfFastestElevator].contains(packetElement)) {
							// already have that stop requested, don't want to duplicate
						} else {
							elevatorRequestsDown[indexOfFastestElevator].add(packetElement);// add the
							// floor to
							// requests
						}
					}
				} else {// elevator is currently fulfilling Up stops
					if (elevatorRequestsDown[indexOfFastestElevator].contains(packetElement)) {
						// already have that stop requested, don't want to duplicate
					} else {
						elevatorRequestsDown[indexOfFastestElevator].add(packetElement);// add the floor to
						// requests
					}
				}
			}
		} else {// holding, can fulfill immediately
			// can assume no stops or requests exist, don't need to check for duplicates if
			// above
			if (packetElement > packetCurrentFloor) {// the floor requesting is above the elevator's current
				// location
				elevatorRequestsDown[indexOfFastestElevator].add(packetElement);
				// create and send sendPacket to start motor in Down direction
				createSendingData(packetElement, UNUSED, UNUSED, DOWN);// 2: down
			} else {// (packetElementIndex<elevatorLocation) {//the floor requesting is below the
				// elevator's current location
				elevatorRequestsUp[indexOfFastestElevator].add(packetElement);
				// create and send sendPacket to start motor in Up direction
				createSendingData(packetElement, UNUSED, UNUSED, UP);// 1: up
			}
			//elevatorFloorSendPacket(FLOOR_ID);// send the created packet with the sendData values prescribed above
			elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet with the sendData values prescribed above
			//System.out.println("From Floor-Hold, Should have sent to elevator");
		}
	}
	
	//RESPONSE TIME METHODS (FOR SERVING FLOOR REQUESTS)
	public static void calculateResponseTimes(int destination, int requestDirection) {
		System.out.println("calculateResponseTimes() method called");
		//int[] responseTime = new int[numElevators];
		responseTime = new int[numElevators];
		int distance = 0;// number of floors traveled before arriving at the destination
		int stops = 0;// number of stops that need to be made before the destination (floor that's
		// making the request)
		int highest;// highest requested
		int lowest;// lowest requested
		int current;// current floor
		int status;// elevator's status
		int next;// next stop

		for (int i = 0; i < numElevators; i++) {
			// check and set status, highest, current, and lowest floors,
			highest = elevatorHighestRequestFloor[i];
			lowest = elevatorLowestRequestFloor[i];
			current = elevatorCurrentFloor[i];
			status = elevatorStatus[i];
			next = elevatorNextStop[i];
			if (status == HOLD) {// elevator in hold
				// distance=|destination-current|
				System.out.println("calculateResponseTimes() Elevator: "+i+" in HOLD");
				distance = destination - elevatorCurrentFloor[i];
				stops = 0;// stops=0 since by definition hold means there were no prior requests or stops

			} else if (status == UP) {// elevator going up
				if (requestDirection == UP) {// if requesting to go up
					// if along the way
					if (destination >= next) {
						distance = destination - current;
					}
					// distance=destination-current
					// stops=stops between destination and current
					else {
						distance = (highest - current) + (highest - lowest) + (destination - lowest);
						stops = elevatorStopsUp[i].size() + elevatorStopsDown[i].size()
								+ stopsBetween(elevatorRequestsUp[i], lowest, destination, UP);
					}
					// else if missed
					// distance=(top-current)+(top-bottom)+(destination-bottom)
					// stops=upStops+downStops+upRequests before destination
				} else if (requestDirection == DOWN) {// if requesting to go down
					distance = (highest - current) + (highest - destination);
					stops = elevatorStopsUp[i].size() + stopsBetween(elevatorStopsDown[i], highest, destination, DOWN);
					// distance=(Top-current)+(top-destination)
					// stops=upStops+downStops between destination and top
				}
			} else if (status == DOWN) {// elevator going down
				if (requestDirection == UP) {// if requesting to go up
					distance = (current - lowest) + (destination - lowest);
					stops = elevatorStopsDown[i].size() + stopsBetween(elevatorStopsUp[i], lowest, destination, UP);
					// distance=(current-Bottom)+(destination-Bottom)
					// stops=downStops+upStops between destination and botom
				} else if (requestDirection == DOWN) {// if requesting to go down
					if (destination <= elevatorNextStop[i]) {
						distance = current - destination;
						stops = stopsBetween(elevatorStopsDown[i], current, destination, DOWN);
					} else {
						distance = (current - lowest) + (highest - lowest) + (highest - destination);
						stops = elevatorStopsUp[i].size() + elevatorStopsDown[i].size()
								+ stopsBetween(elevatorRequestsDown[i], highest, destination, DOWN);
					}
					// if along the way
					// distance=current-destination
					// stops=stops between destinatino and current
					// else if missed
					// distance=(current-Bottom)+(top-bottom)+(top-destination)
					// stops=upStops+downStops+downRequests before destination
				}
			} else {
				// catastrophic error
				System.out.println("calculateResponseTimes() mismatch between motor status variables, status is " + elevatorStatus[i]
						+ " should only be HOLD: " + HOLD + " , UP: " + UP + " , and DOWN: " + DOWN);
			}
			responseTime[i] = distance * TRAVEL_TIME_PER_FLOOR + stops * DOOR_DURATION;
		}
	}

	public static int stopsBetween(LinkedList<Integer> floors, int current, int destination, int direction) {
		// calculates how many stops are between the destination and current floor for
		// use in responseTime calculation
		System.out.println("stopsBetween() method called");
		int stops = 0;
		if (direction == UP) {
			for (int i = current; i < destination; i++) {
				if (floors.contains(current)) {
					stops++;
				}
			}
		} else if (direction == DOWN) {
			for (int i = current; i > destination; i--) {
				if (floors.contains(current)) {
					stops++;
				}
			}
		}
		return stops;
	}
	public static void fastestElevator() {
		System.out.println("fastestElevator() method called");
		int temp;
		temp = responseTime[0];//set to the first elevator at first
		for (int i = 1; i < responseTime.length; i++) {//find the quickest (smallest) response time
			if (responseTime[i] < temp) {
				temp = responseTime[i];
				indexOfFastestElevator = i;
			}
		}
		System.out.println("The Fastest Elevator to respond is: "+indexOfFastestElevator);
	}
//SEND & RECEIVE METHODS
	public static void elevatorFloorSendPacket(int sendTo) {//SENDING ELEVATOR PACKET HERE
		System.out.println("elevatorFloorSendPacket() called, sending to: "+sendTo);
		if (sendTo==ELEVATOR_ID) {
			System.out.println("Response to Elevator " + packetElement + ": " + Arrays.toString(sendData) + "\n");

			DatagramPacket sendingPacket=new DatagramPacket(sendData, sendData.length,elevatorAddress, EL_SENDPORTNUM);
			try {
				schedulerElevatorSendSocket.send(sendingPacket);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else if(sendTo==FLOOR_ID) {
			System.out.println("Response to Floor " + packetElement + ": " + Arrays.toString(sendData) + "\n");

			DatagramPacket sendingPacket=new DatagramPacket(sendData, sendData.length,floorAddress, FL_SENDPORTNUM);
			try {
				schedulerFloorSendSocket.send(sendingPacket);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else {
			//catastrophic error
			//sendingPortNum=elevatorPortNum;//SHOULDN'T BE HERE, but otherwise throws warning if variable not set
			System.out.println("elevatorFloorSendPacket() Error; given argument is neither ELEVATOR_ID nor FLOOR_ID but: "+sendTo);
		}
	}
	//public static byte[] createSendingData(int target, int currentFloor, int direction, int instruction) {
	public static void createSendingData(int target, int currentFloor, int direction, int instruction) {

		ByteArrayOutputStream sendingOutputStream = new ByteArrayOutputStream();
		sendingOutputStream.write(SCHEDULER_ID); // Identifying as the scheduler
		sendingOutputStream.write(target); // (exact floor or elevator to receive)
		sendingOutputStream.write(UNUSED); // not needed (request or status update: for sending from scheduler)
		
		if (instruction == UPDATE_DISPLAYS) {// update displays of the floors
			sendingOutputStream.write(currentFloor); // (current floor of elevator)
			sendingOutputStream.write(direction); // (direction of elevator)
		} else {
			sendingOutputStream.write(UNUSED); // not needed (current floor of elevator)
			sendingOutputStream.write(UNUSED); // not needed (direction of elevator)
		}
		sendingOutputStream.write(UNUSED); // not needed (destination request)
		sendingOutputStream.write(instruction); // scheduler instruction
		sendingOutputStream.write(UNUSED); // error's only received by scheduler and not sent
		sendData = sendingOutputStream.toByteArray();
	}
	public static void elevatorFloorReceivePacket() {
		//public static byte[] elevatorFloorReceivePacket() {
	
		// ELEVATOR AND FLOOR RECEIVING PACKET HERE 
		//DatagramPacket temporaryReceivePacket=new DatagramPacket(receiveData, receiveData.length);
		// System.out.println("Server: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.tm
		schedulerReceivePacket=new DatagramPacket(receiveData,receiveData.length);
		try {
			System.out.println("waiting");
			schedulerReceiveSocket.receive(schedulerReceivePacket);//shedulerSocketSendReceiveElevator.receive(temporaryReceivePacket);
			//System.out.println("Request from Elevator: " + Arrays.toString(data));
			
			// schedulerSocketReceiveElevator.close();
			// schedulerSocketSendReceiveElevator.close()
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		// Separating byte array received 
		//receiveData = temporaryReceivePacket.getData();
		packetOrigin = receiveData[0];
		packetElement = receiveData[1];
		packetType = receiveData[2];
		packetCurrentFloor = receiveData[3];
		packetDirection = receiveData[4];
		packetRequestFloor = receiveData[5];
		//data[6] is Scheduler's Instruction and should be left empty/ not received
		packetError=receiveData[7];
				
		if (packetOrigin==ELEVATOR_ID) {
			if (packetType==INITIALIZE) {
				System.out.println("INITIALIZE from Elevator: " + Arrays.toString(receiveData));
				scheduleElevatorInitialize();
			}
			else if (packetType==REQUEST) {
				System.out.println("REQUEST from Elevator: " + Arrays.toString(receiveData));
				scheduleElevatorRequest();
			}
			else if (packetType==UPDATE){
				System.out.println("UPDATE from Elevator: " + Arrays.toString(receiveData));
				scheduleElevatorUpdate();
			}
			else if (packetType==ERROR){
				System.out.println("ERROR from Elevator: " + Arrays.toString(receiveData));
				errorResponse();
			}
			else {
				//error
				System.out.println("Elevator sent packet that isn't INITIALIZE, REQUEST, UPDATE, or ERROR: "+Arrays.toString(receiveData));
			}
			//System.out.println("from elevator: " + packetElement+ " Direction: "+ upOrDown+ " Current Floor: "+currentFloor+ " Destination: "+ destFloor+"\n");
			
			//schedulerElevatorReceivePacket=temporaryReceivePacket;
		}
		else if(packetOrigin==FLOOR_ID) {
			if (packetType==INITIALIZE) {
				System.out.println("INITIALIZE from Floor: " + Arrays.toString(receiveData));
				scheduleFloorInitialize();
			}
			else if (packetType==REQUEST) {
				System.out.println("REQUEST from Floor: " + Arrays.toString(receiveData));
				scheduleFloorRequest();
			//schedulerFloorReceivePacket=temporaryReceivePacket;
			}
			else if (packetType==ERROR){
				System.out.println("ERROR from Floor: " + Arrays.toString(receiveData));
				errorResponse();
			}
			else {
				//error
				System.out.println("Floor sent packet that isn't INITIALIZE, REQUEST, UPDATE, or ERROR: "+Arrays.toString(receiveData));
			}
			//System.out.println("from Floor: " + Arrays.toString(receiveData));
		}
		else {
			//Error packet?
			//actual issue otherwise
			System.out.print("packet received in Scheduler from neither Elevator nor Floor"+Arrays.toString(receiveData));
		}
	}
	//DEALING WITH ERRORS
		private static void errorResponse(){
			if (packetError==DOOR_ERROR) {
				//Transient Fault: handle gracefully
				//re-call the stop for that elevator: reopens and closes the problem door, will resume prior function automatically after
				createSendingData(packetElement, UNUSED, UNUSED, MAKE_STOP);//resending the stop signal to the elevator should RE- open and close the door causing the issue-> "Fixing" it
				elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet with the sendData values prescribed above
				//after the stop, the elevator should then RE- start and continue with it's previous operation automatically
			}
			else if (packetError==MOTOR_ERROR) {
				//Hard Fault
				//"shut down the corresponding elevator": stop the elevator (instruction 3), then place it on hold (instruction 4)
				//call for help?
				//re-call the stop for that elevator: reopens and closes the problem door, will resume prior function automatically after
				createSendingData(packetElement, UNUSED, UNUSED, SHUT_DOWN);// shuts down the problem elevator and notifies maintenance and emergency fire services for help
				elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet with the sendData values prescribed above
			}
			else if (packetError==OTHER_ERROR) {
				//tell the problem elevator to make a stop, possibly "fixing" anythiing that's wrong
				createSendingData(packetElement, UNUSED, UNUSED, MAKE_STOP);
				elevatorFloorSendPacket(ELEVATOR_ID);
			}
			else if (packetError==NO_ERROR) {
				System.out.println("Scheduler was notified of an error but NO_ERROR was shown for elevator: "+ packetType);
				//still need to reply in order for everything to continue on
				createSendingData(packetElement, UNUSED, UNUSED, UNUSED);
				elevatorFloorSendPacket(ELEVATOR_ID);
				//continue as normal
			}
			else{
				System.out.println("Scheduler received an unknown error");
			}
		}
	/*---------------------------MAIN----------------------------------*/
	public static void main(String args[]) throws InterruptedException {
		SchedulerSeperateAlgorithm schedulerHandler = new SchedulerSeperateAlgorithm();
		
		//for (;;) {
		while (true) {
			elevatorFloorReceivePacket();
			//respondStart = System.nanoTime();// start the timer
			//schedulerHandler.SchedulingAlgorithm();//receiveData);
			//respondEnd = System.nanoTime();// end for the timer
			//System.out.println("Scheduler took: " + (respondEnd - respondStart) + " nanoseconds to respond");
		}
	}
}

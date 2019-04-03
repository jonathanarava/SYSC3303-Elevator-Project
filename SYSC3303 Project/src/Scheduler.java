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

public class Scheduler {

	// Packets and sockets required to connect with the Elevator and Floor class

	public static DatagramSocket schedulerSocketSendReceiveElevator, schedulerSocketSendReceiveFloor;
	public static DatagramPacket schedulerElevatorSendPacket, schedulerElevatorReceivePacket, schedulerFloorSendPacket,
	schedulerFloorReceivePacket;

	// Variables
	public static byte data[] = new byte[8];
	public static byte dataFloor[] = new byte[8];
	public static int elevatorOrFloor;
	public static int elevatorOrFloorID;
	public static int requestOrUpdate;
	public static int currentFloor;
	public static int upOrDown;
	public static int destFloor;
	public static int instruction;
	public static byte errorType;

	// SETTING THE NUMBER OF ELEVATORS AND FLOORS PRESENT IN THE SYSTEM
	public static int numElevators;// = 4;
	public static int numFloors;// = 15;

	// lists to keep track of what requests need to be handled
	public static Object obj = new Object();
	public static int limit = numFloors * numElevators;

	// scheduling alogrithm variable declaration
	public static int elevatorCurrentFloor[];// = new int[numElevators];
	public static int elevatorStatus[];// = new int[numElevators];
	public static int elevatorNextStop[];// = new int[numElevators];

	public static int elevatorNumStops[];// = new int[numElevators];
	public static int elevatorHighestRequestFloor[];// = new int[numElevators];
	public static int elevatorLowestRequestFloor[];// = new int[numElevators];

	// temporary sorting algorithm variables
	public static int floorRequestDirection;
	public static LinkedList<Integer>[] elevatorRequestsUp;// = new LinkedList[numElevators];
	public static LinkedList<Integer>[] elevatorStopsUp;// = new LinkedList[numElevators];
	public static LinkedList<Integer>[] elevatorRequestsDown;// = new LinkedList[numElevators];
	public static LinkedList<Integer>[] elevatorStopsDown;// = new LinkedList[numElevators];
	public static InetAddress packetAddress;
	public static int packetPort;


	public static byte[] sendData = new byte[8];
	public static byte[] receiveData;
	
	public static long respondStart, respondEnd;// variables for the measurements to respond

	// Declare timing constants
	public static final int TIME_PER_FLOOR = 1;// time for the elevator to travel per floor
	public static final int DOOR_DURATION = 4;// time that taken for the door to open and close when given the open
	// command (door closes automatically after alloted time)

	// Declare Motor States:
	/*
	 * public static final ___ HOLD=__; public static final ___ UP=__; public static
	 * final ___ DOWN=__; public static final ___ ELEVATOR_ID=___; public static
	 * final ___ FLOOR_ID=___; public static final ___ SCHEDULER_ID=___; public
	 * static final ___ STATUS=___; public static final ___ REQUEST=___;
	 */
	public static final int EL_RECEIVEPORTNUM = 369;
	public static final int EL_SENDPORTNUM = 159;

	public static final int FL_RECEIVEPORTNUM = 488;
	public static final int FL_SENDPORTNUM = 1199;

	private static final byte HOLD = 0x00;// elevator is in hold state
	private static final byte STOP = 0x03;// elevator is
	private static final byte UP = 0x02;// elevator is going up
	private static final byte DOWN = 0x01;// elevator is going down
	private static final int ELEVATOR_ID = 21;// for identifying the packet's source as elevator
	private static final int FLOOR_ID = 69;// for identifying the packet's source as floor
	private static final int SCHEDULER_ID = 54;// for identifying the packet's source as scheduler
	private static final int DOOR_OPEN = 1;// the door is open when ==1
	private static final byte ERROR=(byte)0xE0;//an error has occured
	//Errors
	private static final byte DOOR_ERROR=(byte)0xE1;
	private static final byte MOTOR_ERROR=(byte)0xE2;
	//still error states between 0xE3 to 0xEE for use
	private static final byte OTHER_ERROR=(byte)0xEF; 
	private static final byte NO_ERROR=(byte)0x00;
	// private static final int DOOR_DURATION=4;//duration that doors stay open for
	private static final int REQUEST = 1;// for identifying the packet sent to scheduler as a request
	private static final int UPDATE = 2;// for identifying the packet sent to scheduler as a status update
	private static final int INITIALIZE=8;//for first communication with the scheduler
	//private static final byte[] ELEVATOR_INITIALIZE_PACKET_DATA={ELEVATOR_ID,0,INITIALIZE, 0,0,0,0,0};
	//private static final byte[] FLOOR_INITIALIZE_PACKET_DATA={FLOOR_ID,0,INITIALIZE, 0,0,0,0,0};
	private static final int UNUSED=0;// value for unused parts of data 

	//for use at initialization, both elevatorIntermediate and floorIntermediate need to send their initialization packets
	//-otherwise the update packets sent will not have a valid socket address to do so with
	//-when both have been received, both of the below variables will be TRUE, thus then the scheduler will send a response packet
	//		notifying each that they may continue
	private static boolean elevatorInitialized=false;
	private static boolean floorInitialized=false;


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
		}
	}

	
	public Scheduler(boolean a) {
		// Constructor for Junit Testing
	}

	public Scheduler() {
		try {
			schedulerSocketSendReceiveElevator = new DatagramSocket(EL_RECEIVEPORTNUM);
			schedulerSocketSendReceiveFloor = new DatagramSocket(FL_RECEIVEPORTNUM);// can be any available port,
			// Scheduler will reply
			// to the port
			// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}

	public static void elevatorFloorReceivePacket() {
		//public static byte[] elevatorFloorReceivePacket() {
	
		// ELEVATOR AND FLOOR RECEIVING PACKET HERE 
		DatagramPacket temporaryReceivePacket=new DatagramPacket(data, data.length);
		// System.out.println("Server: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.tm
		try {
			System.out.println("waiting");
			schedulerSocketSendReceiveElevator.receive(temporaryReceivePacket);
			//System.out.println("Request from elevator: " + Arrays.toString(data));

			// schedulerSocketReceiveElevator.close();
			// schedulerSocketSendReceiveElevator.close()

		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		// Separating byte array received 
		elevatorOrFloor = data[0];
		receiveData = temporaryReceivePacket.getData();
		elevatorOrFloorID = data[1];
		requestOrUpdate = data[2];
		currentFloor = data[3];
		upOrDown = data[4];
		destFloor = data[5];
		errorType=data[7];
		
		if (elevatorOrFloor==ELEVATOR_ID) {
			if (requestOrUpdate==REQUEST) {
				System.out.print("Request ");
			}
			else {
				System.out.print("Update ");
			}
			//System.out.println("from elevator: " + elevatorOrFloorID+ " Direction: "+ upOrDown+ " Current Floor: "+currentFloor+ " Destination: "+ destFloor+"\n");
			System.out.println("from elevator: " + Arrays.toString(data));
			schedulerElevatorReceivePacket=temporaryReceivePacket;
		}
		else if(elevatorOrFloor==FLOOR_ID) {
			//System.out.print("Request from floor: "+elevatorOrFloorID+ " Direction: "+ upOrDown+ "\n");// Current Floor: "+currentFloor+ " Destination: "+ destFloor+"\n");
			System.out.println("Request from floor: " + Arrays.toString(data));
			schedulerFloorReceivePacket=temporaryReceivePacket;
		}
		else if (elevatorOrFloor==ERROR) {
			errorResponse();
			
		}
		else {
			//Error packet?
			//actual issue otherwise
			System.out.print("packet received in Scheduler from neither Elevator nor Floor");
		}
		
		
	}
	private static void errorResponse() {
		if (errorType==DOOR_ERROR) {
			//stop the elevator
			//recall the door open-close
		}
		else if (errorType==MOTOR_ERROR) {
			//stop the elevator
			//call for help?
		}
		else if (errorType==OTHER_ERROR) {
			//stop the elevator
		}
		else if (errorType==NO_ERROR) {
			System.out.println("Scheduler was notified of an error but NO_ERROR was shown");
		}
		else{
			System.out.println("Scheduler received an unknown error");
		}
	}

	private static void schedulerInitilization() {
		elevatorCurrentFloor = new int[numElevators];
		elevatorStatus = new int[numElevators];
		elevatorNextStop = new int[numElevators];

		elevatorNumStops = new int[numElevators];
		elevatorHighestRequestFloor = new int[numElevators];
		elevatorLowestRequestFloor = new int[numElevators];
		
		/*for (int i = 0; i < numElevators; i++) {
			elevatorRequestsUp[i] = new LinkedList<Integer>();
			elevatorStopsUp[i] = new LinkedList<Integer>();
			elevatorRequestsDown[i] = new LinkedList<Integer>();
			elevatorStopsDown[i] = new LinkedList<Integer>();
		}*/

		createSendingData(0,0,0, INITIALIZE);
		elevatorFloorSendPacket(ELEVATOR_ID);
		createSendingData(0,0,0, INITIALIZE);
		elevatorFloorSendPacket(FLOOR_ID);
		System.out.println("Scheduler is INITIALIZED and may proceed with operations\n\n\n\n");
	}
	//public byte[] SchedulingAlgorithm(byte[] packetData) {
	private static void SchedulingAlgorithm(byte[] packetData) {//should be private and shouldn't needa return a global variable

		// byte[] packetData = schedulerElevatorReceivePacket.getData();
		int packetSentFrom = packetData[0];// elevator, floor, or other(testing/ error)
		// 21=elevator, 69=floor
		int packetElementIndex = packetData[1];// index to find/ retrieve specific element from our array of
		// elevators and floors
		// should have been the name given to threads' constructor at creation
		//
		int packetIsStatus = packetData[2];// whether it is a status update from elevator or a request (elevator or
		// floor but handled differently)
		// 1=request, 2=status update
		int elevatorLocation = packetData[3];// where the elevator is currently located (sensor information sent
		// from elevator as status update)
		int elevatorDirection=packetData[4];
		int stopRequest;// =packetData[5]; //a request to give to an elevator for stopping at a given
		// floor (from elevator or floor)
		// public static int floorRequesting;

		int[] responseTime;// response time of individual elevators to got to a floor request
		int indexOfFastestElevator = 0;// index of array for which elevator is fastest
		int temp;// temporary for finding the fastest response time

		// check whether the packet was from an elevator (requests and status) or a
		// floor(request)
		// floor: allocate to an appropriate elevator (same direction, fastest response
		// time, least load)
		// if no currently allocatable elevators then add to requests linked list
		// elevator:

		// update unpack/ coordinate/ allocate action variables
		// packetData = schedulerElevatorReceivePacket.getData();
		// packetAddress = schedulerElevatorReceivePacket.getAddress();
		// packetPort = schedulerElevatorReceivePacket.getPort();

		// packetElementIndex = packetData[1];// index to find/ retrieve specific
		// element from our array of elevators
		// and floors
		// should have been the name given to threads' constructor at creation
		//
		// packetSentFrom = packetData[0];// elevator, floor, or other(testing/ error)
		// 0=? 1=? 2=?
		// packetIsStatus = packetData[2];// whether it is a status update from elevator
		// or a request (elevator or
		// floor but handled differently)
		//
		elevatorLocation = packetData[3];// where the elevator is currently located (sensor information sent from
		// elevator as status update)
		floorRequestDirection = packetData[4];// which direction the requesting floor wants to go
		stopRequest = packetData[5];// a request to stop at a given floor (-1 if no request)
		if (packetSentFrom == ELEVATOR_ID) {// if it is an elevator
			
			if (packetIsStatus==INITIALIZE) {
				elevatorInitialized=true;
				numElevators=packetElementIndex;
				linkedListInitialization();
				System.out.println("Received Elevator Initialization \n");
				if (floorInitialized==true) {
					schedulerInitilization();
				}
			}

			// elevatorNum=__;//which elevator it is in
			// status or request
			else {
				if (packetIsStatus == UPDATE) {// status update from Elevator
					// elevatorLocation=packetData[___];//status/ floor number from sensor in
					// Elevator
					// compare floor number with next stop of the elevator (==nextStop variable)
					// if (floorStatus==nextStop[packetElementIndex])
					elevatorCurrentFloor[packetElementIndex] = elevatorLocation;
					if (elevatorStatus[packetElementIndex] == UP) {// direction that the elevator is going is up
						if (elevatorStopsUp[packetElementIndex].contains(elevatorLocation)) {// we have reached a
							// destination stop and
							// need to stop the
							// elevator
							// open the doors (closes automatically after preallocated duration)
							// create sendpacket to stop the elevator (and open, close the door)
							// send the sendPacket
							// remove the stop from goingup linked list
							// check if there are more stops
							createSendingData(packetElementIndex, 0, 0, 3);// 3: make a stop
							elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet immediately, otherwise will be
							// overwritten before being sent at the very end
							// elevatorStopsUp[packetElementIndex].remove(elevatorLocation);
							// elevatorStopsUp[packetElementIndex].remove(elevatorLocation);

							if (elevatorStopsUp[packetElementIndex].size() == 1) {// no more stops Up
								// if (elevatorStopsUp[packetElementIndex].isEmpty()) {// no more stops Up
								// check if there are more requests
								if (elevatorRequestsUp[packetElementIndex].isEmpty()) {// no missed floors for going Up
									// do nothing
								} else {// there are outstanding requests to go Up
									elevatorStopsUp[packetElementIndex] = elevatorRequestsUp[packetElementIndex];
									// the requests to go Up can now be met once we've finished going down first
									elevatorRequestsUp[packetElementIndex].clear();
								}
								// check if there are more stops down
								if (elevatorStopsDown[packetElementIndex].isEmpty()) {// no more stops
									// create and send sendPacket to hold the motor

									try {
										Thread.currentThread().sleep(2);
									} catch (InterruptedException e) { // THIS SLEEP IS HERE TO GIVE THE ELEVATOR ENOUGH
										// TIME RECEIVE THE PACKET FOR 'HOLD' DO NOT REMOVE
										// TODO Auto-generated catch block // UNLESS YOU KNOW WHAT YOU'RE DOING
										e.printStackTrace();
									}

									createSendingData(packetElementIndex, 0, 0, 4);// 4: place on hold state
									elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet immediately, otherwise will be
									// overwritten before being sent at the very end
									elevatorStopsUp[packetElementIndex].clear();
									// Sleep method

								} else {// we have stops to go up, start fulfilling those
									// create and send SendPacket for the motor to go Up
									createSendingData(packetElementIndex, 0, 0, 1);// 1: up
									elevatorFloorSendPacket(ELEVATOR_ID);
								}
							} else {// finished stopping for destination floor, continue going Up to fulfill other
								// stops
								// create and send SendPacket to restart the motor/ have the motor in the up
								// direction
								createSendingData(packetElementIndex, 0, 0, 1);// 1: up
								// IMPORTANT:
								// this packet sending may not be necessary and could be what's causing the
								// double sending at the very beginning
								elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet immediately, otherwise will be
								// overwritten before being sent at the very end
							}

						} else {// not a floor that we need to stop at
							// Look at this.
							System.out.println(
									"reached else of Eleavtor Update while going UP; not a floor that is contained in the stop list");
							createSendingData(packetElementIndex, elevatorLocation, elevatorDirection, 5);
							elevatorFloorSendPacket(ELEVATOR_ID);
							elevatorFloorSendPacket(FLOOR_ID);//STATUS UPDATES SHOULD BE SENT TO ALL FLOORS
						}

					} else {// elevator is going down
						if (elevatorStopsDown[packetElementIndex].contains(elevatorLocation)) {// we have reached a
							// destination stop and
							// need to stop the
							// elevator
							// open the doors (closes automatically after preallocated duration)
							// create sendpacket to stop the elevator (and open, close the door)
							// send the sendPacket
							// remove the stop from goingup linked list
							// check if there are more stops
							createSendingData(packetElementIndex, 0, 0, 3);// 3: make a stop
							elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet immediately, otherwise will be
							// overwritten before being sent at the very end
							if (elevatorStopsDown[packetElementIndex].isEmpty()) {
								// check if there are more requests
								if (elevatorRequestsDown[packetElementIndex].isEmpty()) {// no missed floors for going
									// down
									// do nothing
								} else {// there are outstanding requests to go down
									elevatorStopsDown[packetElementIndex] = elevatorRequestsDown[packetElementIndex];
									// the requests to go down can be met once we have finished going up first
									elevatorRequestsDown[packetElementIndex].clear();
								}
								// check if there are more stops up
								if (elevatorStopsUp[packetElementIndex].isEmpty()) {// no more stops
									// create and send sendPacket to hold the motor
									createSendingData(packetElementIndex, 0, 0, 4);// 4: hold
									elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet immediately, otherwise will be
									// overwritten before being sent at the very end
									elevatorStopsDown[packetElementIndex].clear();
								} else {// we have stops to go up, start fulfilling those
									// create and send SendPacket for the motor to go Down
									createSendingData(packetElementIndex, 0, 0, 2);// 2: down
									elevatorFloorSendPacket(ELEVATOR_ID);// send the created packet immediately, otherwise will be
									// overwritten before being sent at the very end
								}
							} else {// finished stopping for a destination floor, continue fulfilling other stops
								// create and send SendPacket to restart the motor/ have the motor in the down
								// direction
								createSendingData(packetElementIndex, 0, 0, 2);// 2: down
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
					createSendingData(packetElementIndex, elevatorLocation, elevatorDirection, 5);
					elevatorFloorSendPacket(FLOOR_ID);//STATUS UPDATES SHOULD BE SENT TO ALL FLOORS 
					elevatorFloorSendPacket(ELEVATOR_ID);
				} else {// elevator sent a request

					// floorRequesting=packetElementIndex;//the floor# of the requesting floor
					// proximity

					// check availability and either allocate to a moving elevator, initiate the
					// movement of another, or add to request linked list if none available (or
					// wrong direction)

					// CHECK IF THE REQUEST IS A DUPLICATE, if so then ignore
					elevatorCurrentFloor[packetElementIndex] = elevatorLocation;
					if (elevatorStatus[packetElementIndex] != HOLD) {// elevator is not in hold mode, currently moving
						// check direction
						if (elevatorStatus[packetElementIndex] == UP) {// elevator is going up
							if (elevatorLocation < stopRequest) {// we haven't reached that floor yet and can still stop
								// in time
								if (elevatorStopsUp[packetElementIndex].contains(stopRequest)) {// check if the request
									// is already in the
									// linked list
									// (duplicate) if so
									// then do nothing, else
									// add it
									// do nothing, don't want duplicates
								} else {
									elevatorStopsUp[packetElementIndex].add(stopRequest);// add to the stopsUp
									// linkedlist for the
									// current elevator
								}
							} else {// the stop has already been missed
								elevatorStopsDown[packetElementIndex].add(stopRequest);
								// add it to the stopDown linked list
							}
						} else {// elevator is going down
							if (elevatorLocation > stopRequest) {// we haven't reached that floor yet and can still stop
								// in time
								if (elevatorStopsDown[packetElementIndex].contains(stopRequest)) {// check if the
									// request is
									// already in the
									// linked list
									// (duplicate) if so
									// then do nothing,
									// else add it
									// do nothing, don't want duplicates
								} else {
									// add to the stopsDown linkedlist for the current elevator
									elevatorStopsDown[packetElementIndex].add(stopRequest);
								}

							} else {// the stop has already been missed
								elevatorStopsUp[packetElementIndex].add(stopRequest);
								// add it to the stopDown linked list
							}
						}
					} else {// currently in hold mode, we can fulfill that request immediately
						// can assume no stops or requests exist, don't need to check for duplicates
						if (elevatorLocation < stopRequest) {// we are below the destination floor, we need to go up
							elevatorStopsUp[packetElementIndex].add(stopRequest);
							elevatorStatus[packetElementIndex] = UP;
							// create and send sendPacket to start the motor
							createSendingData(packetElementIndex, 0, 0, 1);// 1: up
						} else if (elevatorLocation > stopRequest) {// we are above the destination floor, we need to go
							// down
							elevatorStopsDown[packetElementIndex].add(stopRequest);
							elevatorStatus[packetElementIndex] = DOWN;
							// create and send sendPacket to start the motor
							createSendingData(packetElementIndex, 0, 0, 2);// 2: down
						}
						// currently in hold mode and remain in hold mode
						else if (elevatorLocation == stopRequest) {
							// Since the elevator is in hold mode and a request for the current floor (where
							// it already is) the elevator just needs to let the person out
							// and then resume the hold state since there aren't any other requests made

							// ___________________ if in Hold mode, the elevator should not be moving
							// anywhere cause that means the elevator

							// elevatorStatus[packetElementIndex] = STOP;//stop the elevator (temporary)
							// sendData = createSendingData(packetElementIndex, 0, 0, 3);// 3: stop the
							// elevator turns off the motor but also utilizes the door open/ close letting
							// the person out
							// elevatorSendPacket(sendData);//send the created packet immediately, otherwise
							// will be overwritten before being sent at the very end
							elevatorStatus[packetElementIndex] = HOLD;// place back into hold state
							createSendingData(packetElementIndex, 0, 0, 4);// doesn't need an immediate send
							// since there is nothing below to
							// overwrite the sendData before it
							// is sent
						}
						elevatorFloorSendPacket(ELEVATOR_ID);// originally the only send in the method
						elevatorFloorSendPacket(FLOOR_ID);
					}
				}
			}
		} else {// FROM FLOOR
			if (packetIsStatus==INITIALIZE) {//for initializing contact with elevatorIntermediate or floorIntermediate
				floorInitialized=true;
				System.out.println("Floor initialization received\n");
				numFloors=packetElementIndex;
				if (elevatorInitialized==true) {
					schedulerInitilization();
				}
			}
			else {// Its an up or down request coming from one of the floors
				responseTime = calculateResponseTimes(packetElementIndex, floorRequestDirection);//response times of the elevators to reach the floor in the requested direction
				temp = responseTime[0];//set to the first elevator at first
				for (int i = 1; i < responseTime.length; i++) {//find the quickest (smallest) response time
					if (responseTime[i] < temp) {
						temp = responseTime[i];
						indexOfFastestElevator = i;
					}
				}

				// RECALL THE FLOOR CALLING SHOULD ONLY LET PASSENGERS IN WHEN IN THE CHOSEN
				// DIRECTION (UP/DOWN)
				if (elevatorStatus[indexOfFastestElevator] != HOLD) {// not in hold
					if (elevatorStatus[indexOfFastestElevator] == UP) {// elevator is going up
						if (floorRequestDirection == elevatorStatus[indexOfFastestElevator]) {// floor is requesting to
							// go up also
							if (packetElementIndex > elevatorLocation) {// still time
								if (elevatorStopsUp[indexOfFastestElevator].contains(packetElementIndex)) {
									// already have that stop requested, don't want to duplicate
								} else {
									elevatorStopsUp[indexOfFastestElevator].add(packetElementIndex);// add to stops list
								}
							} else {// missed
								if (elevatorRequestsUp[indexOfFastestElevator].contains(packetElementIndex)) {
									// already have that stop requested, don't want to duplicate
								} else {
									elevatorRequestsUp[indexOfFastestElevator].add(packetElementIndex);// add the floor
									// to requests
								}
							}
						} else {// elevator is currently fulfilling down stops
							if (elevatorRequestsUp[indexOfFastestElevator].contains(packetElementIndex)) {
								// already have that stop requested, don't want to duplicate
							} else {
								elevatorRequestsUp[indexOfFastestElevator].add(packetElementIndex);// add the floor to
								// requests
							}
						}
					} else {// elevator is going down
						if (floorRequestDirection == elevatorStatus[indexOfFastestElevator]) {// floor is requesting to
							// go Down also
							if (packetElementIndex < elevatorLocation) {// still time
								if (elevatorStopsDown[indexOfFastestElevator].contains(packetElementIndex)) {
									// already have that stop requested, don't want to duplicate
								} else {
									elevatorStopsDown[indexOfFastestElevator].add(packetElementIndex);// add to stops
									// list
								}
							} else {// missed
								if (elevatorRequestsDown[indexOfFastestElevator].contains(packetElementIndex)) {
									// already have that stop requested, don't want to duplicate
								} else {
									elevatorRequestsDown[indexOfFastestElevator].add(packetElementIndex);// add the
									// floor to
									// requests
								}
							}
						} else {// elevator is currently fulfilling Up stops
							if (elevatorRequestsDown[indexOfFastestElevator].contains(packetElementIndex)) {
								// already have that stop requested, don't want to duplicate
							} else {
								elevatorRequestsDown[indexOfFastestElevator].add(packetElementIndex);// add the floor to
								// requests
							}
						}
					}
				} else {// holding, can fulfill immediately
					// can assume no stops or requests exist, don't need to check for duplicates if
					// above
					if (packetElementIndex > elevatorLocation) {// the floor requesting is above the elevator's current
						// location
						elevatorRequestsDown[indexOfFastestElevator].add(packetElementIndex);
						// create and send sendPacket to start motor in Down direction
						createSendingData(packetElementIndex, 0, 1, 5);// 2: down
					} else {// (packetElementIndex<elevatorLocation) {//the floor requesting is below the
						// elevator's current location
						elevatorRequestsUp[indexOfFastestElevator].add(packetElementIndex);
						// create and send sendPacket to start motor in Up direction
						createSendingData(packetElementIndex, 0, 2, 5);// 1: up
					}
					elevatorFloorSendPacket(FLOOR_ID);// send the created packet with the sendData values prescribed above
				}
			}
		}
		//return sendData;
	}

	//public static byte[] createSendingData(int target, int currentFloor, int direction, int instruction) {
	public static void createSendingData(int target, int currentFloor, int direction, int instruction) {

		ByteArrayOutputStream sendingOutputStream = new ByteArrayOutputStream();
		sendingOutputStream.write(SCHEDULER_ID); // Identifying as the scheduler
		sendingOutputStream.write(target); // (exact floor or elevator to receive)
		sendingOutputStream.write(UNUSED); // not needed (request or status update: for sending from scheduler)
		// somewhat redundant usage since floors would only receive updates and
		// elevators would only receive requests
		if (instruction == 5) {// update displays of the floors
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
		//return sendData;
	}

	public static int[] calculateResponseTimes(int destination, int requestDirection) {
		// destination is the floor that is making the request
		// elevatorLowestRequestFloor
		// elevatorHighestRequestFloor
		// TIME_PER_FLOOR
		// DOOR_DURATION
		// UP
		// DOWN
		// HOLD
		int[] responseTime = new int[numElevators];
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
			} else if (elevatorStatus[i] == DOWN) {// elevator going down
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
				System.out.println("mismatch between motor status variables, status is " + elevatorStatus[i]
						+ " should only be HOLD: " + HOLD + " , UP: " + UP + " , and DOWN: " + DOWN);
			}
			responseTime[i] = distance * TIME_PER_FLOOR + stops * DOOR_DURATION;
		}
		return responseTime;
	}

	public static int stopsBetween(LinkedList<Integer> floors, int current, int destination, int direction) {
		// calculates how many stops are between the destination and current floor for
		// use in responseTime calculation
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

	public static void elevatorFloorSendPacket(int sendTo) {////SENDING ELEVATOR PACKET HERE 

		//byte[] responseByteArray = new byte[7];
		//responseByteArray = sendData;

		if (sendTo==ELEVATOR_ID) {
			System.out.println("Response to Elevator " + data[1] + ": " + Arrays.toString(sendData) + "\n");
			schedulerElevatorSendPacket = new DatagramPacket(sendData, sendData.length,
					schedulerElevatorReceivePacket.getAddress(), EL_SENDPORTNUM);
			try {
				schedulerSocketSendReceiveElevator.send(schedulerElevatorSendPacket);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else if(sendTo==FLOOR_ID) {
			System.out.println("Response to Floor " + data[1] + ": " + Arrays.toString(sendData) + "\n");
			schedulerFloorSendPacket = new DatagramPacket(sendData, sendData.length,
					schedulerFloorReceivePacket.getAddress(), FL_SENDPORTNUM);// EL_SENDPORTNUM);
			try {
				schedulerSocketSendReceiveFloor.send(schedulerFloorSendPacket);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else {
			//catastrophic error
		}

	}
	/*public static void elevatorSendPacket(byte[] sendData) {//SENDING ELEVATOR PACKET HERE 

		byte[] responseByteArray = new byte[7];
		responseByteArray = sendData;

		// responseByteArray = createSendingData(elevatorOrFloor, currentFloor,
		// upOrDown, instruction);
		System.out.println("Response to elevator " + data[1] + ": " + Arrays.toString(responseByteArray) + "\n");
		schedulerElevatorSendPacket = new DatagramPacket(responseByteArray, responseByteArray.length,
				schedulerElevatorReceivePacket.getAddress(), EL_SENDPORTNUM);
		try {
			schedulerSocketSendReceiveElevator.send(schedulerElevatorSendPacket);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void floorSendPacket() {//FLOOR SENDING PACKET HERE 

		byte[] responseByteArray = new byte[5];
		responseByteArray = createSendingData(elevatorOrFloor, currentFloor, upOrDown, instruction);
		System.out.println("Response to Floor " + data[1] + ": " + Arrays.toString(responseByteArray) + "\n");
		try {
			schedulerFloorSendPacket = new DatagramPacket(responseByteArray, responseByteArray.length,
					InetAddress.getLocalHost(), PORTNUM);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			schedulerSocketSendReceiveFloor.send(schedulerFloorSendPacket);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}*/

	/*---------------------------MAIN----------------------------------*/
	public static void main(String args[]) throws InterruptedException {
		Scheduler schedulerHandler = new Scheduler();
		//Scheduler.linkedListInitialization();

		/*
		 * //elevatorStopsUp[0].add(3); //elevatorStopsDown[1].add(2);
		 * 
		 * 
		 * //create temporary testing packets to simulate "Requests" sent to the
		 * schedulingAlrogirthm because it needs a parameter
		 * 
		 * byte tempTestData[] = new byte[7]; tempTestData[0]= ELEVATOR_ID;//pretending
		 * to be an elevator tempTestData[1]= 0;//pretending to be elevator #1
		 * tempTestData[2]= REQUEST;//simulating a request tempTestData[3]= 0;//ground
		 * floor tempTestData[4]= 0;//up or down tempTestData[5]= 2;//destination floor,
		 * request for floor 2 tempTestData[6]= 0;//scheduler instruction- not used now
		 * 
		 * DatagramPacket testingPacket=new DatagramPacket
		 * (tempTestData,tempTestData.length); Scheduler.SchedulingAlgorithm(
		 * schedulerElevatorReceivePacket);//call method with simulated packet for
		 * elevator #1 tempTestData[1]=1;//for elevator number 2
		 * tempTestData[5]=3;//destination floor, request for floor 3 DatagramPacket
		 * testingPacket2=new DatagramPacket (tempTestData,tempTestData.length);
		 * Scheduler.SchedulingAlgorithm(schedulerElevatorReceivePacket);//call method
		 * with simulated packet for elevator #2
		 */
		for (;;) {

			// Receives the Packet
			//byte[] packetRecieved = //SHOULD BE DATARECEIVED
			elevatorFloorReceivePacket();
			respondStart = System.nanoTime();// start the timer
			// Sorts the received Packet and returns the byte array to be sent
			//sendData = Scheduler.SchedulingAlgorithm(packetRecieved);//sendData is a global variable, completely redundant to set itself being passed to itself
			//schedulerHandler.SchedulingAlgorithm(packetRecieved);
			schedulerHandler.SchedulingAlgorithm(receiveData);
			// Sends the Packet to Elevator
			// elevatorSendPacket(sendData);
			respondEnd = System.nanoTime();// end for the timer
			System.out.println("Scheduler took: " + (respondEnd - respondStart) + " nanoseconds to respond");
		}
	}
}

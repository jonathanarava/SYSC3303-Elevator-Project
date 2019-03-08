
//only one with main method; allocates the number of Elevator and Floor objects
//most logic for changing of states
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.LinkedList;

public class Scheduler {

	public static DatagramPacket schedulerSendPacket, schedulerReceivePacket;
	public static DatagramSocket schedulerSendSocket, schedulerReceiveSocket;
	/*
	 * send sockets should be allocated dynamically since the ports would be
	 * variable to the elevator or floor we have chosen
	 */
	public static final int RECEIVEPORTNUM = 369;
	public static byte[] sendData  = new byte[7];
	
	// request list
	// Define Data Types for passing to and from Elevator(s) and Floor(s)

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
	private static final byte HOLD = 0x00;// elevator is in hold state
	private static final byte UP = 0x02;// elevator is going up
	private static final byte DOWN = 0x01;// elevator is going down
	private static final int ELEVATOR_ID = 21;// for identifying the packet's source as elevator
	private static final int FLOOR_ID = 69;// for identifying the packet's source as floor
	private static final int SCHEDULER_ID = 54;// for identifying the packet's source as scheduler
	private static final int DOOR_OPEN = 1;// the door is open when ==1
	// private static final int DOOR_DURATION=4;//duration that doors stay open for
	private static final int REQUEST = 1;// for identifying the packet sent to scheduler as a request
	private static final int UPDATE = 2;// for identifying the packet sent to scheduler as a status update

	// scheduling alogrithm variable declaration
	public static int elevatorCurrentFloor[];// =new int[createNumElevators];
	public static int elevatorStatus[];// =new int[createNumElevators];//each elevator is either holding(0), going
	// up(1), or going down(2)
	public static int elevatorNextStop[];// =new int [createNumElevators];//the floor number of the next stop for that
	// elevator
	// public static int elevatorLowestRequest[];//=new int
	// [createNumElevators];//the floor that the elevator will go down to once the
	// requests going down have been met to then go up again
	// public static int elevatorHighestRequest[];//=new int
	// [createNumElevators];//the floor that the elevator will go up to once down
	// requests have been fulfilled
	// public static int elevatorProximity[];//=new int [createNumElevators];//the
	// distance between the next request and the current floor the elevator is on
	public static int elevatorNumStops[];// =new int [createNumElevators];//number of stops that each elevator has,
	// int elevatorCurrentFloor[]=new int[createNumElevators];
	// int elevatorStatus[]=new int[createNumElevators];//each elevator is either
	// holding(0), going up(1), or going down(2)
	// int elevatorNextStopUp[]=new int [createNumElevators];//the floor number of
	// the next stop for that elevator
	public static int elevatorHighestRequestFloor[];// =new int [createNumElevators];//the floor that the elevator will
	// go down to once the requests going down have been met to then go
	// up again
	public static int elevatorLowestRequestFloor[];// =new int [createNumElevators];//the floor that the elevator will
	// go up to once down requests have been fulfilled
	// int elevatorProximity[]=new int [createNumElevators];//the distance between
	// the next request and the current floor the elevator is on
	// int elevatorNumStops[]=new int [createNumElevators];//number of stops that
	// each elevator has,

	// temporary sorting algorithm variables
	public static int floorRequestDirection;// the floor is requesting to go up or down
	// up= ,down=
	public static LinkedList<Integer>[] elevatorRequestsUp;// = new LinkedList();//[createNumElevators];//requests to go
	// up from floors which aren't currently allocated to an
	// elevator (in use past the floor)
	public static LinkedList<Integer>[] elevatorStopsUp;// =new LinkedList();//[createNumElevators];//linked list for
	// stops needed in the up direction
	public static LinkedList<Integer>[] elevatorRequestsDown;// = new LinkedList();//[createNumElevators];//requests to
	// go down from floors which aren't currently allocated
	// to an elevator (in use past the floor)
	public static LinkedList<Integer>[] elevatorStopsDown;// =new LinkedList();//[createNumElevators];//linked list for
	// stops needed in the down direction
	// int nextStop[]=new int[createNumElevators];//the next stop for each elevator;
	// if unallocated (in hold) then set as -1
	// variable declarations for replying/ creating send packet
	public static InetAddress packetAddress;// =schedulerReceivePacket.getAddress();
	public static int packetPort;// =schedulerReceivePacket.getPort();
	public static int numElevators;
	public static int numFloors;
	public static byte[] sendingData;//data in the format specified to be sent to either FloorIntermediate or ElevatorIntermediate

	public static void main(String args[]) {// 2 arguments: args[0] is the number of Elevators in the system and

		// SINCE THE INTERMEDIATE CLASSES NOW START THE THREADS AN INITIAL CONNECTION
		// PACKET FROM EACH IS NEEDED
		// WILL PASS THE NUMBER OF ELEVATORS AND FLOORS IN THE SYSTEM IN THE INITIAL
		// CONNECTION
		numElevators = 4;
		numFloors = 6;

		// setting up array of linked lists for keeping track of stops and requests
		// array of size numElevators stores a linked list for each elevator for each
		// use
		elevatorRequestsUp = new LinkedList[numElevators];// ();//[createNumElevators];//requests to go up from floors
		// which aren't currently allocated to an elevator (in use
		// past the floor)
		elevatorStopsUp = new LinkedList[numElevators];// ();//[createNumElevators];//linked list for stops needed in
		// the up direction
		elevatorRequestsDown = new LinkedList[numElevators];// ();//[createNumElevators];//requests to go down from
		// floors which aren't currently allocated to an elevator
		// (in use past the floor)
		elevatorStopsDown = new LinkedList[numElevators];// ();//[createNumElevators];//linked list for stops needed in
		// the down direction
		// initialize new Integer type linked lists in the array
		for (int i = 0; i < numElevators; i++) {
			elevatorRequestsUp[i] = new LinkedList<Integer>();// [createNumElevators];//requests to go up from floors
			// which aren't currently allocated to an elevator (in
			// use past the floor)
			elevatorStopsUp[i] = new LinkedList<Integer>();// [createNumElevators];//linked list for stops needed in the
			// up direction
			elevatorRequestsDown[i] = new LinkedList<Integer>();// [createNumElevators];//requests to go down from
			// floors which aren't currently allocated to an
			// elevator (in use past the floor)
			elevatorStopsDown[i] = new LinkedList<Integer>();// [createNumElevators];//linked list for stops needed in
			// the down direction
		}

		/*
		 * UNUSED BECAUSE OF INCLUSION OF INTERMEDIATE CLASSES... //getting floor
		 * numbers from parameters set int createNumElevators =
		 * Integer.parseInt(args[0]);//The number of Elevators in the system is passed
		 * via argument[0] int createNumFloors = Integer.parseInt(args[1]);//The number
		 * of Floors in the system is passed via argument[0]
		 * 
		 * //for keeping track of the port numbers, filled as they get declared //since
		 * we're not strictly replying to the immediate packet we can't get the port
		 * numbers there //allocating port numbers to the variable number of elevators
		 * and floors would also be difficult, just using the ones which are available
		 * int elevatorPortNumbers[]=new int[createNumElevators]; int
		 * floorPortNumbers[]=new int[createNumFloors];
		 * 
		 * //addresses of the created threads int elevatorAddresses[]=new
		 * int[createNumElevators]; int floorAddresses[]=new int[createNumFloors];
		 * 
		 * //arrays to keep track of the number of elevators, eliminates naming
		 * confusion Elevator elevatorArray[]=new Elevator[createNumElevators]; //Thread
		 * Elevator elevatorArray[]=new Elevator[createNumElevators]; Floor
		 * floorArray[]=new Floor[createNumFloors];
		 */

		// numElevators and numFloors now replace createNumElevators because of the
		// thread creation being done by intermediate classes instead of scheduler now
		elevatorCurrentFloor = new int[numElevators];
		elevatorStatus = new int[numElevators];// each elevator is either holding(0), going up(1), or going down(2)
		elevatorNextStop = new int[numElevators];// the floor number of the next stop for that elevator
		elevatorNumStops = new int[numElevators];// number of stops that each elevator has,
		elevatorHighestRequestFloor = new int[numElevators];// the floor that the elevator will go down to once the
		// requests going down have been met to then go up again
		elevatorLowestRequestFloor = new int[numElevators];// the floor that the elevator will go up to once down
		// requests have been fulfilled

		// allocation of Datagram Sockets
		// allocate receive socket
		// send socket allocated dynamically for specific port of current elevator or
		// floor
		try {
			schedulerReceiveSocket = new DatagramSocket(RECEIVEPORTNUM);
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}

		// allocate receive packet
		
		// System.out.println("Server: Waiting for Packet.\n");

		/*
		 * UNUSED BECAUSE OF INCLUSION OF INTERMEDIATE CLASSES... //creation of Elevator
		 * and Floor thread objects and start them for (int i=0;i<createNumElevators;
		 * i++) { elevatorArray[i]=new Elevator(Integer.toString(i));
		 * elevatorArray[i].start(); // Block until a datagram packet is received from
		 * receiveSocket. try { //System.out.println("Waiting..."); // so we know we're
		 * waiting schedulerReceiveSocket.receive(schedulerReceivePacket) } catch
		 * (IOException e) { System.out.print("IO Exception: likely:");
		 * System.out.println("Receive Socket Timed Out.\n" + e); e.printStackTrace();
		 * System.exit(1); } elevatorPortNumbers[i]=schedulerReceivePacket.getPort();
		 * elevatorAddresses[i]=scheduleReceivePacket.getAddress();
		 * elevatorCurrentFloor[i]=0;//the elevators are created and initialized at the
		 * ground floor(0) elevatorStatus[i]=0;//elevators created and initialized to
		 * the hold state
		 * 
		 * } for (int j=0;j<createNumFloors; j++) { floorArray[j]=new
		 * Floor(Integer.toString(j)); floorArray[j].start(); //define the port number
		 * of the started floor thread into the array try {// Block until a datagram
		 * packet is received from receiveSocket. //System.out.println("Waiting..."); //
		 * so we know we're waiting
		 * schedulerReceiveSocket.receive(schedulerReceivePacket) } catch (IOException
		 * e) { System.out.print("IO Exception: likely:");
		 * System.out.println("Receive Socket Timed Out.\n" + e); e.printStackTrace();
		 * System.exit(1); } floorPortNumbers[j]=schedulerReceivePacket.getPort(); }
		 */

		/*
		 * //variable definitions used to unpack/ coordinate/ allocate actions
		 * packetData=schedulerReceivePacket.getData();
		 * packetElementIndex=packetData[1];//index to find/ retrieve specific element
		 * from our array of elevators and floors //should have been the name given to
		 * threads' constructor at creation // packetSentFrom=packetData[0];//elevator,
		 * floor, or other(testing/ error) //21=elevator, 69=floor
		 * packetIsStatus=packetData[2];//whether it is a status update from elevator or
		 * a request (elevator or floor but handled differently) //1=request, 2=status
		 * update elevatorLocation=packetData[3];//where the elevator is currently
		 * located (sensor information sent from elevator as status update)
		 * stopRequest=packetData[5]; //a request to give to an elevator for stopping at
		 * a given floor (from elevator or floor) //int floorRequesting;
		 */
		// int [] responseTime;//response time of individual elevators to got to a floor
		// request
		// int indexOfFastestElevator;//index of array for which elevator is fastest
		// int temp;//temporary for finding the fastest response time

		while (true) {
			byte data[] = new byte[7];
			schedulerReceivePacket = new DatagramPacket(data, data.length);
			//packetAddress = schedulerReceivePacket.getAddress();
			//packetPort = schedulerReceivePacket.getPort();
			
			try {// Block until a datagram packet is received from receiveSocket.
				// System.out.println("Waiting..."); // so we know we're waiting
				schedulerReceiveSocket.receive(schedulerReceivePacket);
				System.out.println("Recieving from Elevator: ");
				System.out.println(Arrays.toString(data));
			} catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}

			// variable definitions used to unpack/ coordinate/ allocate actions
			byte[] packetData = schedulerReceivePacket.getData();
			int packetElementIndex = packetData[1];// index to find/ retrieve specific element from our array of
			// elevators and floors
			// should have been the name given to threads' constructor at creation
			//
			int packetSentFrom = packetData[0];// elevator, floor, or other(testing/ error)
			// 21=elevator, 69=floor
			int packetIsStatus = packetData[2];// whether it is a status update from elevator or a request (elevator or
			// floor but handled differently)
			// 1=request, 2=status update
			int elevatorLocation = packetData[3];// where the elevator is currently located (sensor information sent
			// from elevator as status update)
			int stopRequest;// =packetData[]; //a request to give to an elevator for stopping at a given
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
			packetData = schedulerReceivePacket.getData();
			packetAddress = schedulerReceivePacket.getAddress();
			packetPort = 23; //schedulerReceivePacket.getPort();

			packetElementIndex = packetData[1];// index to find/ retrieve specific element from our array of elevators
			// and floors
			// should have been the name given to threads' constructor at creation
			//
			packetSentFrom = packetData[0];// elevator, floor, or other(testing/ error)
			// 0=? 1=? 2=?
			packetIsStatus = packetData[2];// whether it is a status update from elevator or a request (elevator or
			// floor but handled differently)
			//
			elevatorLocation = packetData[3];// where the elevator is currently located (sensor information sent from
			// elevator as status update)
			stopRequest = packetData[5];// a request to stop at a given floor (-1 if no request)

			if (packetSentFrom == ELEVATOR_ID) {// if it is an elevator
				// elevatorNum=__;//which elevator it is in
				// status or request
				if (packetIsStatus == UPDATE) {// status update from Elevator
					// elevatorLocation=packetData[___];//status/ floor number from sensor in
					// Elevator
					// compare floor number with next stop of the elevator (==nextStop variable)
					// if (floorStatus==nextStop[packetElementIndex])
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
							createSendingData(packetElementIndex,0,0,3);//3: make a stop
							if (elevatorStopsUp[packetElementIndex].isEmpty()) {// no more stops Up
								// check if there are more requests
								if (elevatorRequestsUp[packetElementIndex].isEmpty()) {// no missed floors for going Up
									// do nothing
								} else {// there are outstanding requests to go Up
									elevatorStopsUp[packetElementIndex] = elevatorRequestsUp[packetElementIndex];// the
									// requests
									// to
									// go
									// Up
									// can
									// now
									// be
									// met
									// once
									// we've
									// finished
									// going
									// down
									// first
									elevatorRequestsUp[packetElementIndex].clear();
								}
								// check if there are more stops down
								if (elevatorStopsDown[packetElementIndex].isEmpty()) {// no more stops
									// create and send sendPacket to hold the motor
									createSendingData(packetElementIndex,0,0,4);//4: place on hold state
									
								} else {// we have stops to go up, start fulfilling those
									// create and send SendPacket for the motor to go Up
									createSendingData(packetElementIndex,0,0,1);//1: up
								}
							} 
							else {// finished stopping for destination floor, continue going Up to fufill other
								// stops
								// create and send SendPacket to restart the motor/ have the motor in the up
								// direction
								createSendingData(packetElementIndex,0,0,1);//1: up
							}
						} else {// not a floor that we need to stop at
							// do nothing
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
							createSendingData(packetElementIndex,0,0,3);//3: make a stop
							if (elevatorStopsDown[packetElementIndex].isEmpty()) {
								// check if there are more requests
								if (elevatorRequestsDown[packetElementIndex].isEmpty()) {// no missed floors for going
									// down
									// do nothing
								} else {// there are outstanding requests to go down
									elevatorStopsDown[packetElementIndex] = elevatorRequestsDown[packetElementIndex];// the
									// requests
									// to
									// go
									// down
									// can
									// now
									// be
									// met
									// once
									// we've
									// finished
									// going
									// up
									// first
									elevatorRequestsDown[packetElementIndex].clear();
								}
								// check if there are more stops up
								if (elevatorStopsUp[packetElementIndex].isEmpty()) {// no more stops
									// create and send sendPacket to hold the motor
									createSendingData(packetElementIndex,0,0,4);//4: hold
								} else {// we have stops to go up, start fulfilling those
									// create and send SendPacket for the motor to go Down
									createSendingData(packetElementIndex,0,0,2);//2: down
								}
							} else {// finished stopping for a destination floor, continue fulfilling other stops
								// create and send SendPacket to restart the motor/ have the motor in the down
								// direction
								createSendingData(packetElementIndex,0,0,2);//2: down
							}
						} else {// not a floor that we need to stop at
							// do nothing
						}

					}

					// }
					// update floor number and direction displays for elevator and all floors
					createSendingData(0,0,0,5);//5: status update
				} else {// elevator sent a request

					// floorRequesting=packetElementIndex;//the floor# of the requesting floor
					// proximity

					// check availability and either allocate to a moving elevator, initiate the
					// movement of another, or add to request linked list if none available (or
					// wrong direction)

					// CHECK IF THE REQUEST IS A DUPLICATE, if so then ignore
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
									elevatorStopsDown[packetElementIndex].add(stopRequest);// add to the stopsDown
									// linkedlist for the
									// current elevator
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
							// create and send sendPacket to start the motor
							createSendingData(packetElementIndex,0,0,1);//1: up
						} else {// we are above the destination floor, we need to go down
							elevatorStopsDown[packetElementIndex].add(stopRequest);
							// create and send sendPacket to start the motor
							createSendingData(packetElementIndex,0,0,2);//2: down
						}

					}
				}
			} else {// request is from floor 
				responseTime = calculateResponseTimes(packetElementIndex, floorRequestDirection);
				temp = responseTime[0];
				for (int i = 1; i < responseTime.length; i++) {
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
						} else {// eleveator is currently fulfilling Up stops
							if (elevatorRequestsDown[indexOfFastestElevator].contains(packetElementIndex)) {
								// already have that stop requested, don't want to duplicate
							} else {
								elevatorRequestsDown[indexOfFastestElevator].add(packetElementIndex);// add the floor to
								// requests
							}
						}
					}
				} else {// holding, can fulfill immediately
					// can assume no stops or requests exist, don't need to check for duplicates
					// if above
					if (packetElementIndex > elevatorLocation) {// the floor requesting is above the elevator's current
						// location
						elevatorRequestsDown[indexOfFastestElevator].add(packetElementIndex);
						// create and send sendPacket to start motor in Down direction
						createSendingData(packetElementIndex,0,0,2);//2: down
					}

					else {// (packetElementIndex<elevatorLocation) {//the floor requesting is below the
						// elevator's current location
						elevatorRequestsUp[indexOfFastestElevator].add(packetElementIndex);
						// create and send sendPacket to start motor in Up direction
						createSendingData(packetElementIndex,0,0,1);//1: up
					}
				}
			}
			
			//sendData[0] = 41;			//********INITIALIZED TO 41 BECAUSE sendData DID NOT HAVE ANY CONTENT IN IT TO BE SENT********************
										//		  Ideally this sendData byte array should be filled from the scheduler algorithm
			System.out.println(Arrays.toString(sendData));
			sendThePacket(sendData,packetAddress,packetPort);
		}
	}

	/*
	 * schedulerSendPacket = new DatagramPacket(responseByteArray,
	 * responseByteArray.length, schedulerReceivePacket.getAddress(),
	 * schedulerReceivePacket.getPort()); //}
	 * 
	 * 
	 * 
	 * // or (as we should be sending back the same thing) //
	 * System.out.println(received);
	 * 
	 * // Send the datagram packet to the client via the send socket. try {
	 * schedulerSendSocket.send(schedulerSendPacket); System.out.println("Sent"); }
	 * catch (IOException e) {; e.printStackTrace(); System.exit(1); } } }
	 */

	public static int[] calculateResponseTimes(int destination, int requestDirection) {// destination is the floor that
		// is making the request

		// elevatorLowestRequestFloor
		// elevatorHighestRequestFloor
		// TIME_PER_FLOOR
		// DOOR_DURATION
		// UP
		// DOWN
		// HOLD
		int[] responseTime = new int[numElevators];
		int distance = 0;// number of floors traveled before arrivating at the destination
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
					// stops=stops between destinatino and current
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

	public static int stopsBetween(LinkedList<Integer> floors, int current, int destination, int direction) {// calculates
		// how
		// many
		// stops
		// are
		// between
		// the
		// destination
		// and
		// current
		// floor
		// for
		// use
		// in
		// responseTime
		// calculation
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
	public static void sendThePacket(byte[] data, InetAddress address, int port) {

		try {
			schedulerSendSocket = new DatagramSocket(port);
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
		schedulerSendPacket=new DatagramPacket(data, data.length, address, port);
	}
	public static void createSendingData(int target, int currentFloor, int direction, int instruction) {
		/*parameters:
		-target: exact elevator/ floor to send to
		-currentFloor: for updating the Floor of where the elevator is at for the display
		-direction: for updating the Floor of which direction the elevator is going for the display
		-instruction: tells the floors/ elevators what to do
		 */
		// creates the byte array according to the required format in this case

		/*Scheduler Instructions/ Commands:
		-sending to elevator
		1:start motor up
		2:start motor down
		3:stop motor/ put on hold
		4:open & close doors
		-sending to floor
		5:update floor's displays (current floor of the elvator, and direction of the elevator)
		 */
		ByteArrayOutputStream sendingOutputStream = new ByteArrayOutputStream();
		sendingOutputStream.write(59); //Identifying as the scheduler
		sendingOutputStream.write(target); // (exact floor or elevator to receive)
		sendingOutputStream.write(0); // not needed (request or status update: for sending to scheduler)
		// somewhat redundant usage since floors would only receive updates and elevators would only receive requests
		if (instruction==5) {//update displays of the floors
			sendingOutputStream.write(currentFloor); // (current floor of elevator)
			sendingOutputStream.write(direction); // (direction of elevator)
		}
		else {
			sendingOutputStream.write(0); // not needed (current floor of elevator)
			sendingOutputStream.write(0); // not needed (direction of elevator)
		}
		sendingOutputStream.write(0); // not needed (destination request)
		sendingOutputStream.write(instruction); // scheduler instruction
		sendData= sendingOutputStream.toByteArray();
	}
}


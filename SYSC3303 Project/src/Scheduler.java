
//only one with main method; allocates the number of Elevator and Floor objects
//most logic for changing of states
import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Scheduler {

	public static DatagramPacket schedulerSendPacket, schedulerReceivePacket;
	public static DatagramSocket schedulerSendSocket,schedulerReceiveSocket;
	/*send sockets should be allocated dynamically since the ports would be
	variable to the elevator or floor we have chosen
	 */
	public static final int RECEIVEPORTNUM = 23;

	// request list
	// Define Data Types for passing to and from Elevator(s) and Floor(s)

	// Declare timing constants
	public static final float TIME_PER_FLOOR=1;//time for the elevator to travel per floor
	public static final float DOOR_OPEN=4;//time that taken for the door to open and close when given the open command (door closes automatically after alloted time)

	public static void main(String args[]){//2 arguments: args[0] is the number of Elevators in the system and 
		//for iteration 1 there will only be 1 elevator

		//getting floor numbers from parameters set
		int createNumElevators = Integer.parseInt(args[0]);//The number of Elevators in the system is passed via argument[0]
		int createNumFloors = Integer.parseInt(args[1]);//The number of Floors in the system is passed via argument[0]


		//for keeping track of the port numbers, filled as they get declared
		//since we're not strictly replying to the immediate packet we can't get the port numbers there
		//allocating port numbers to the variable number of elevators and floors would also be difficult, just using the ones which are available
		int elevatorPortNumbers[]=new int[createNumElevators];
		int floorPortNumbers[]=new int[createNumFloors];

		//addresses of the created threads
		InetAddress elevatorAddresses[]=new InetAddress[createNumElevators];
		int floorAddresses[]=new int[createNumFloors];

		//arrays to keep track of the number of elevators, eliminates naming confusion
		Elevator elevatorArray[]=new Elevator[createNumElevators];
		//Thread Elevator elevatorArray[]=new Elevator[createNumElevators];
		Floor floorArray[]=new Floor[createNumFloors];
		
		//
		Thread threadArray[]= new Thread[createNumFloors + createNumElevators];


		//scheduling algorithm variable declaration
		int elevatorCurrentFloor[]=new int[createNumElevators];
		int elevatorStatus[]=new int[createNumElevators];//each elevator is either holding(0), going up(1), or going down(2)
		int elevatorNextStopUp[]=new int [createNumElevators];//the floor number of the next stop for that elevator
		int elevatorLowUpRequest[]=new int [createNumElevators];//the floor that the elevator will go down to once the requests going down have been met to then go up again
		int elevatorHighDownRequest[]=new int [createNumElevators];//the floor that the elevator will go up to once down requests have been fulfilled
		int elevatorProximity[]=new int [createNumElevators];//the distance between the next request and the current floor the elevator is on
		int elevatorNumStops[]=new int [createNumElevators];//number of stops that each elevator has, 
		
		//temporary sorting algorithm variables
		
		//linked list for requests, up direction; individiaul list for each elevator as well as total
	    LinkedList elevatorRequestsUp[] = new LinkedList[createNumElevators];//requests to go up from floors which aren't currently allocated to an elevator (in use past the floor or in the wrong direction)
		LinkedList elevatorStopsUp[]    = new LinkedList[createNumElevators];//linked list for stops needed 


		//allocation of Datagram Sockets
		//allocate receive socket
		//send socket allocated dynamically for specific port of current elevator or floor 
		try {
			schedulerReceiveSocket=new DatagramSocket(23);//arbitrary usage of 23 for port number of Scheduler's receive port
		} catch (SocketException se) {//if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}

		//allocate receive packet
		byte data[] = new byte[100];
		schedulerReceivePacket = new DatagramPacket(data, data.length);
		//System.out.println("Server: Waiting for Packet.\n");

		//creation of Elevator and Floor thread objects and start them
		for (int i=0;i<createNumElevators; i++) {
			elevatorArray[i]=new Elevator(Integer.toString(i));
			threadArray[i] = new Thread(elevatorArray[i]);
			threadArray[i].start();	//We need to create a Thread Array with the runnable classes inside of it and start that. So the runnables will be inside threadArray[]
			// Block until a datagram packet is received from receiveSocket.
			try {        
				//System.out.println("Waiting..."); // so we know we're waiting
				schedulerReceiveSocket.receive(schedulerReceivePacket);
			} 
			catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}
			elevatorPortNumbers[i]=schedulerReceivePacket.getPort();
			elevatorAddresses[i]=schedulerReceivePacket.getAddress();
			elevatorCurrentFloor[i]=0;//the elevators are created and initialized at the ground floor(0)
			elevatorStatus[i]=0;//elevators created and initialized to the hold state

		}
		for (int j=0;j<createNumFloors; j++) {
			floorArray[j]=new Floor(Integer.toString(j));
			floorArray[j].start();
			//define the port number of the started floor thread into the array
			try {// Block until a datagram packet is received from receiveSocket.        
				//System.out.println("Waiting..."); // so we know we're waiting
				schedulerReceiveSocket.receive(schedulerReceivePacket)
			} 
			catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}
			floorPortNumbers[j]=schedulerReceivePacket.getPort();
		}

		//scheduling algorithm variable declaration
		int elevatorCurrentFloor[]=new int[createNumElevators];
		int elevatorStatus[]=new int[createNumElevators];//each elevator is either holding(0), going up(1), or going down(2)
		int elevatorNextStopUp[]=new int [createNumElevators];//the floor number of the next stop for that elevator
		int elevatorUpRequestFloor[]=new int [createNumElevators];//the floor that the elevator will go down to once the requests going down have been met to then go up again
		int elevatorDownRequestFloor[]=new int [createNumElevators];//the floor that the elevator will go up to once down requests have been fulfilled
		//int elevatorProximity[]=new int [createNumElevators];//the distance between the next request and the current floor the elevator is on
		//int elevatorNumStops[]=new int [createNumElevators];//number of stops that each elevator has, 
		//int elevator

		//temporary sorting algorithm variables
		int floorRequestDirection;//the floor is requesting to go up or down
		//linked list for requests, up direction; individiaul list for each elevator as well as total
		linkedlist elevatorRequestsUp[]= new linkedList[createNumElevators];//requests to go up from floors which aren't currently allocated to an elevator (in use past the floor)
		linkedlist elevatorStopsUp[]=new LinkedList[createNumElevators];//linked list for stops needed in the up direction
		linkedlist elevatorRequests[]= new linkedList[createNumElevators];//requests to go down from floors which aren't currently allocated to an elevator (in use past the floor)
		linkedlist elevatorStopsDown[]=new LinkedList[createNumElevators];//linked list for stops needed in the down direction 
		//int nextStop[]=new int[createNumElevators];//the next stop for each elevator; if unallocated (in hold) then set as -1
		//variable declarations for replying/ creating send packet
		byte[] packetAddress=schedulerReceivePacket.getAddress();
		byte [] packetPort=schedulerReceivePacket.getPort();

		//variable definitions used to unpack/ coordinate/ allocate actions
		byte [] packetData=schedulerReceivePacket.getData();
		//SHOULD BE BYTE INSTEAD OF INT?			
		int packetElementIndex=packetData[___];//index to find/ retrieve specific element from our array of elevators and floors
		//should have been the name given to threads' constructor at creation
		//
		int packetSentFrom=packetData[___];//elevator, floor, or other(testing/ error)
		//0=? 1=? 2=?
		int packetIsStatus=packetData[___];//whether it is a status update from elevator or a request (elevator or floor but handled differently)
		//
		int elevatorLocation=packetData[__];//where the elevator is currently located (sensor information sent from elevator as status update)
		int stopRequest=packetData[___]; //a request to stop at a given floor (from elevator or floor)

		while(true) {
			try {// Block until a datagram packet is received from receiveSocket.        
				//System.out.println("Waiting..."); // so we know we're waiting
				schedulerReceiveSocket.receive(schedulerReceivePacket)
			} 
			catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}

			//check whether the packet was from an elevator (requests and status) or a floor(request)
			//floor: allocate to an appropriate elevator (same direction, fastest response time, least load)
			//if no currently allocatable elevators then add to requests linked list
			//elevator: 

			//update unpack/ coordinate/ allocate action variables 
			packetData=schedulerReceivePacket.getData();
			packetAddress=schedulerReceivePacket.getAddress();
			packetPort=schedulerReceivePacket.getPort();			
			packetElementIndex=packetData[___];//index to find/ retrieve specific element from our array of elevators and floors
			//should have been the name given to threads' constructor at creation
			//
			packetSentFrom=packetData[___];//elevator, floor, or other(testing/ error)
			//0=? 1=? 2=?
			packetIsStatus=packetData[___];//whether it is a status update from elevator or a request (elevator or floor but handled differently)
			//
			elevatorLocation=packetData[__];//where the elevator is currently located (sensor information sent from elevator as status update)
			stopRequest=packetData[___];//a request to stop at a given floor (-1 if no request)

			if (packetSentFrom==__)) {//if it is an elevator
				//elevatorNum=__;//which elevator it is in 
				//status or request
				if (packetIsStatus==___) {//status update from Elevator
					//elevatorLocation=packetData[___];//status/ floor number from sensor in Elevator
					//compare floor number with next stop of the elevator (==nextStop variable)
					//if (floorStatus==nextStop[packetElementIndex])
					if (elevatorStatus==___) {//direction that the elevator is going is up

						if (elevatorStopsUp[packetElementIndex].contains(elevatorLocation)) {//we have reached a destination stop and need to stop the elevator
							//open the doors (closes automatically after preallocated duration)
							//create sendpacket to stop the elevator (and open, close the door)
							//send the sendPacket
							//remove the stop from goingup linked list
							//check if there are more stops
							if (elevatorStopsUp[packetElementIndex].isEmpty()) {//no more stops Up
								//check if there are more requests
								if (elevatorRequestsUp.isEmpty()) {//no missed floors for going Up
									//do nothing
								}
								else {//there are outstanding requests to go Up
									elevatorStopsUp[packetElementIndex]=ElevatorRequestUp[packetElementIndex];//the requests to go Up can now be met once we've finished going down first
									elevatorRequestUp[packetElementIndex].clear();
								}
								//check if there are more stops down 
								if (elevatorStopsDown[].isEmpty()) {//no more stops
									//create and send sendPacket to hold the motor
								}
								else {//we have stops to go up, start fulfilling those
									//create and send SendPacket for the motor to go Up
								}
							}
							else {//finished stopping for destination floor, continue going Up to fufill other stops
								//create and send SendPacket to restart the motor/ have the motor in the up direction
							}
						}
						else {//not a floor that we need to stop at
							//do nothing
						}

					}
					else {//elevator is going down
						if (elevatorStopsDown[packetElementIndex].contains(elevatorLocation)) {//we have reached a destination stop and need to stop the elevator
							//open the doors (closes automatically after preallocated duration)
							//create sendpacket to stop the elevator (and open, close the door)
							//send the sendPacket
							//remove the stop from goingup linked list
							//check if there are more stops
							if (elevatorStopsDown[packetElementIndex].isEmpty()) {
								//check if there are more requests
								if (elevatorRequestsDown.isEmpty()) {//no missed floors for going down
									//do nothing
								}
								else {//there are outstanding requests to go down
									elevatorStopsDown[packetElementIndex]=ElevatorRequestDown[packetElementIndex];//the requests to go down can now be met once we've finished going up first
									elevatorRequestDown[packetElementIndex].clear();
								}
								//check if there are more stops up 
								if (elevatorStopsUp[].isEmpty()) {//no more stops
									//create and send sendPacket to hold the motor
								}
								else {//we have stops to go up, start fulfilling those
									//create and send SendPacket for the motor to go Down
								}
							}
							else {//finished stopping for a destination floor, continue fulfilling other stops
								//create and send SendPacket to restart the motor/ have the motor in the up direction
							}
						}
						else {//not a floor that we need to stop at
							//do nothing
						}

					}

				}
				//update floor number and direction displays for elevator and all floors
			}
			else {//elevator sent a request//ONLY FOR A SINGLE ELEVATOR RIGHT NOW
				//check availability and either allocate to a moving elevator, initiate the movement of another, or add to request linked list if none available (or wrong direction)

				//CHECK IF THE REQUEST IS A DUPLICATE, if so then ignore
				if (elevatorStatus!=__) {//elevator is not in hold mode, currently moving 
					//check direction
					if(elevatorStatus==__) {//elevator is going up
						if (elevatorLocation<stopRequest) {//we haven't reached that floor yet and can still stop in time
							if(elevatorStopUp[packetElementIndex].contains(stopRequest)) {//check if the request is already in the linked list (duplicate) if so then do nothing, else add it
								//do nothing, don't want duplicates 
							}
							else {
								elevatorStopUp[packetElementIndex].add(stopRequest);//add to the stopsUp linkedlist for the current elevator
							}
						}
						else {//the stop has already been missed 
							elevatorStopDown[packetElementIndex].add(stopRequest);
							//add it to the stopDown linked list
						}
					}
					else {//elevator is going down
						if (elevatorLocation>stopRequest) {//we haven't reached that floor yet and can still stop in time
							if(elevatorStopDown[packetElementIndex].contains(stopRequest)) {//check if the request is already in the linked list (duplicate) if so then do nothing, else add it
								//do nothing, don't want duplicates 
							}
							else {
								elevatorStopDown[packetElementIndex].add(stopRequest);//add to the stopsDown linkedlist for the current elevator
							}

						}
						else {//the stop has already been missed 
							elevatorStopUp[packetElementIndex].add(stopRequest);
							//add it to the stopDown linked list
						}
					}
				}
				else {//currently in hold mode, we can fulfill that request immediately
					//can assume no stops or requests exist, don't need to check for duplicates
					if (elevatorLocation<stopRequest) {//we are below the destination floor, we need to go up
						elevatorStopUp[packetElementIndex].add(stopRequest)
						//create and send sendPacket to start the motor
					}
					else{//we are above the destination floor, we need to go down
						elevatorStopDown[packetElementIndex].add(stopRequest)
						//create and send sendPacket to start the motor
					} 

				}
			}
		}
		else {//request is from floor (FOR SINGLE ELEVATOR ONLY)
			//RECALL THE FLOOR CALLING SHOULD ONLY LET PASSENGERS IN WHEN IN THE CHOSEN DIRECTION (UP/DOWN)
			if (elevatorStatus!=__) {//not in hold
				if(elevatorStatus==__) {//elevator is going up
					if (floorRequestDirection==elevatorStatus) {//floor is requesting to go up also
						if(packetElementIndex>elevatorLocation) {//still time
							if (elevatorStopsUp.contains(packetElementIndex)) {
								//already have that stop requested, don't want to duplicate
							}
							else{
								elevatorStopsUp.add(packetElementIndex)//add to stops list
							}
						}
						else {//missed
							if (elevatorRequestsUp.contains(packetElementIndex)) {
								//already have that stop requested, don't want to duplicate
							}
							else {
								elevetorRequestsUp.add(packetElementIndex);//add the floor to requests
							}
						}
					}
					else {//eleveator is currently fulfilling down stops
						if (elevatorRequestsUp.contains(packetElementIndex)) {
							//already have that stop requested, don't want to duplicate
						}
						else {
							elevetorRequestsUp.add(packetElementIndex);//add the floor to requests
						}
					}
				}
				else {//elevator is going down
					if (floorRequestDirection==elevatorStatus) {//floor is requesting to go Down also
						if(packetElementIndex<elevatorLocation) {//still time
							if (elevatorStopsDown.contains(packetElementIndex)) {
								//already have that stop requested, don't want to duplicate
							}
							else{
								elevatorStopsDown.add(packetElementIndex)//add to stops list
							}
						}
						else {//missed
							if (elevatorRequestsDown.contains(packetElementIndex)) {
								//already have that stop requested, don't want to duplicate
							}
							else {
								elevetorRequestsDown.add(packetElementIndex);//add the floor to requests
							}
						}
					}
					else {//eleveator is currently fulfilling Up stops
						if (elevatorRequestsDown.contains(packetElementIndex)) {
							//already have that stop requested, don't want to duplicate
						}
						else {
							elevetorRequestsDown.add(packetElementIndex);//add the floor to requests
						}
					}
				}
			}
			else {//holding, can fulfill immediately
				//can assume no stops or requests exist, don't need to check for duplicates
				//if above
				if (packetElementIndex>elevatorLocation) {//the floor requesting is above the elevator's current location
					elevatorRequestDown.add(packetElementIndex);
					//create and send sendPacket to start motor in Down direction
				}

				else {// (packetElementIndex<elevatorLocation) {//the floor requesting is below the elevator's current location
					elevatorRequestUp.add(packetElementIndex);
					//create and send sendPacket to start motor in Up direction
				}
			}
		}
	}
}


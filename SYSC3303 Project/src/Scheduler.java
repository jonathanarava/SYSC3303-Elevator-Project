
//only one with main method; allocates the number of Elevator and Floor objects
//most logic for changing of states
import java.io.*;
import java.net.*;

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
	public static final TIME_PER_FLOOR=1;//time for the elevator to travel per floor
	public static final DOOR_OPEN=4;//time that taken for the door to open and close when given the open command (door closes automatically after alloted time)

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
		int elevatorAddresses[]=new int[createNumElevators];
		int floorAddresses[]=new int[createNumFloors];

		//arrays to keep track of the number of elevators, eliminates naming confusion
		Elevator elevatorArray[]=new Elevator[createNumElevators];
		//Thread Elevator elevatorArray[]=new Elevator[createNumElevators];
		Floor floorArray[]=new Floor[createNumFloors];

		int elevatorCurrentFloor[]=new int[createNumElevators];
		int elevatorStatus[]=new int[createNumElevators];//each elevator is either holding(0), going up(1), or going down(2)
		int elevatorNextStopUp[]=new int [createNumElevators];//the floor number of the next stop for that elevator
		int elevatorLowUpRequest[]=new int [createNumElevators];//the floor that the elevator will go down to once the requests going down have been met to then go up again
		int elevatorHighDownRequest[]=new int [createNumElevators];//the floor that the elevator will go up to once down requests have been fulfilled
		int elevatorProximity[]=new int [createNumElevators];//the distance between the next request and the current floor the elevator is on
		int elevatorNumStops[]=new int [createNumElevators];//number of stops that each elevator has, 
		//int elevator

		//linked list for requests, up direction
		//individiaul list for each elevator as well as total
		linkedlist elevatorStops[]



				//receive, elevator(status and requests) or floor(requests only)
				//Floor:
				//check if lists are empty then any can be allocated to fulfill the request, lowest number elevator fulfills 
				//check proximity


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
			elevatorArray[i].start();
			// Block until a datagram packet is received from receiveSocket.
			try {        
				//System.out.println("Waiting..."); // so we know we're waiting
				schedulerReceiveSocket.receive(schedulerReceivePacket)
			} 
			catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}
			elevatorPortNumbers[i]=schedulerReceivePacket.getPort();
			elevatorAddresses[i]=scheduleReceivePacket.getAddress();
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
		//receive packets
		//update list
		//change elevator(s) states
		//update elevator(s) and floor(s)
		//use threads for object creation? can allow for argument based input for number of elevators and floors

	}
}

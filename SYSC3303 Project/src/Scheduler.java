//only one with main method; allocates the number of Elevator and Floor objects
//most logic for changing of states
import java.io.*;
import java.net.*;
public class Scheduler {


	public static DatagramPacket schedulerSendPacket, schedulerReceivePacket;
	public static DatagramSocket schedulerSendSocket, schedulerReceiveSocket;/*send sockets should be allocated dynamically 
	since the ports would be variable to the elevator or floor we have chosen*/
	public static final int RECEIVEPORTNUM=23;

	//request list
	//Define Data Types for passing to and from Elevator(s) and Floor(s)

	//Declare timing constants
	//public static final TIME_PER_FLOOR=
	//public static final DOOR_OPEN=
	//public static final DOOR_CLOSE=
	public int currentFloor() {
		int floor;
		if (floor - destFloor() < 0) {
			floor++;
		} else if (floor - destFloor() > 0){
			floor--;
		}
		return floor;
	}
	
	public static void main(String args[]){
		//getting floor numbers from parameters set
		int createNumElevators=args[];
		int createNumFloors=args[];
		/*or from defined constants
	    int createNumElevators=____VARNAME;
	    int createNumFloors=____VARNAME;
		 */

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

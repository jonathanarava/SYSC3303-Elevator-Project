import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ElevatorIntermediate {
	static DatagramPacket elevatorSendPacket, elevatorReceivePacket;
	static DatagramSocket elevatorSendSocket;
	static DatagramSocket elevatorReceiveSocket;
	
	public static void main(String args[]){
		
		try {
			elevatorSendSocket = new DatagramSocket();
			elevatorReceiveSocket = new DatagramSocket();// can be any available port, Scheduler will reply to the port
															// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
		
		
		Elevator elevatorArray[]=new Elevator[1];
		Thread elevatorThreadArray[]= new Thread[1];
		
		elevatorArray[0]=new Elevator(Integer.toString(1));
		elevatorThreadArray[0] = new Thread(elevatorArray[0]);
		
		elevatorThreadArray[0].start();
		
		
		byte[] requestElevator = new byte[3];
		
		/* ELEVATOR --> SCHEDULER (0, FloorRequest, cuurentFloor, 0) */

		//System.out.println("Enter floor number: ");

		//Scanner destination = new Scanner(System.in);
		//int floorRequest;
		//if (destination.nextInt() != 0) {
		//floorRequest = destination.nextInt();
		//} else {
		
		//}
		//destination.close();
		while(true) {
			if(elevatorArray[0].floorRequest==2) {
				requestElevator = elevatorArray[0].responsePacket(elevatorArray[0].floorRequest);
				int lengthOfByteArray = elevatorArray[0].responsePacket(elevatorArray[0].floorRequest).length;
				
				// allocate sockets, packets
				try {
					elevatorSendPacket = new DatagramPacket(requestElevator, lengthOfByteArray, InetAddress.getLocalHost(),
							369);
					System.out.print("I've sent\n");
				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.exit(1);
					}
			}
			else {};
		}
		
		}
}

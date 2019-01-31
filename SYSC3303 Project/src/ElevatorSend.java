import java.io.ByteArrayOutputStream;
import java.util.Scanner;


public class ElevatorSend {
	//request: yes or no (boolean)
	//floor number request (integer)
	//sensor data: boolean (yes, no)
	public static boolean isRequest;
	public static boolean arriveSensor;
	public static int requestFloorNumber;
	public static int floor;
	
	public ElevatorSend(boolean request, int floorRequest, boolean sensor){
		isRequest=request;
		requestFloorNumber=floorRequest;
		arriveSensor=sensor;
	}
	//add get methods
	



	public int destFloor() {
		System.out.println("Enter floor number: ");
		Scanner destination = new Scanner(System.in);
		int n = destination.nextInt();
		//destination.close();
		return n;	
	}


	public byte[] responsePacket() {
	
		// creates the byte array according to the required format
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(0);
		requestElevator.write((byte)destFloor());
		requestElevator.write(0);
		return requestElevator.toByteArray();

	}
	
	
	
}

import java.io.ByteArrayOutputStream;
import java.util.Scanner;

public class ElevatorSend {
	// request: yes or no (boolean)
	// floor number request (integer)
	// sensor data: boolean (yes, no)
	public static boolean isRequest;
	public static boolean arriveSensor;
	public static int requestFloorNumber;
	public static int floor;

	public ElevatorSend() {
		System.out.println("Enter floor number: ");
		
		@SuppressWarnings("resource")
		Scanner destination = new Scanner(System.in);
		int floorRequest = destination.nextInt();
		// destination.close();
		responsePacket(floorRequest);
	}
	// add get methods


	public byte[] responsePacket(int floorRequest) {

		// creates the byte array according to the required format
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(0);
		requestElevator.write((byte) floorRequest);
		requestElevator.write(0);
		return requestElevator.toByteArray();

	}
	
	/*public static void main(String args[]){
		ElevatorSend e = new ElevatorSend();
		System.out.println(e);
	}*/ // test

}

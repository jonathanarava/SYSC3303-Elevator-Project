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
	

	public int runElevator(byte x) {
		int time = (int)x;
		int button;

		button = floor; 
		while (time != 0){
			try {
				Thread.sleep(1000);
				time--;
				button++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return button;
	}
	
	public int currentFloor() {
		if (floor - destFloor() < 0) {
			floor++;
		} else if (floor - destFloor() > 0){
			floor--;
		}
		return floor;
	}
	
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
		requestElevator.write((byte)currentFloor());
		requestElevator.write((byte)destFloor());
		requestElevator.write(0);
		return requestElevator.toByteArray();

	}
	
	
	
}

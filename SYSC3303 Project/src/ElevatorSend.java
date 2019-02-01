import java.io.ByteArrayOutputStream;
import java.util.Scanner;

public class ElevatorSend {
	// request: yes or no (boolean)
	// floor number request (integer)
	// sensor data: boolean (yes, no)
	
	//public static boolean isRequest;
	//public static boolean arriveSensor;
	//public static int requestFloorNumber;
	public static int floorRequest;

	public ElevatorSend() {
		sensor();
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
	
	public int lengthOfByteArray() {
		int len = responsePacket(floorRequest).length;
		return len;
		
	}
	
	public void sensor() {
		try {
			int i=4;
			while(i != 0){
				System.out.format("Seconds till door closes %d second \n", i);
				i--;
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/*public static void main(String args[]){
		ElevatorSend e = new ElevatorSend();
		System.out.println(e);
	}*/ // test

}


public class ElevatorSend {
	//request: yes or no (boolean)
	//floor number request (integer)
	//sensor data: boolean (yes, no)
	public static boolean isRequest;
	public static boolean arriveSensor;
	public static int requestFloorNumber;
	public ElevatorSend(boolean request, int floorRequest, boolean sensor){
		isRequest=request;
		requestFloorNumber=floorRequest;
		arriveSensor=sensor;
	}
	//add get methods
}


public class ElevatorReceive {
	//Floor number display(int)
	//direction display: up, down, hold (int)
	//Motor: up, down, hold (int)
	//door: open, close (boolean)
	public static boolean isRequest;
	public static int requestFloorNumber;
	
	ElevatorReceive(byte[] receivedSchedule){

		runElevator(receivedSchedule[1], receivedSchedule[2]);
		//isRequest=request;
		//requestFloorNumber=floorRequest;
	}

	public void runElevator(byte motorSpinTime, byte currentFloor ) {
		int time = (int)motorSpinTime;
		int button = (int)currentFloor;

		while (time != 0){
			try {
				System.out.println(button);
				Thread.sleep(1000);
				time--;
				button++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	

}

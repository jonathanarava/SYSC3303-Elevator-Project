
public class ElevatorReceive {
	//Floor number display(int)
	//direction display: up, down, hold (int)
	//Motor: up, down, hold (int)
	//door: open, close (boolean)
	public static boolean isRequest;
	public static int requestFloorNumber;
	
	private static byte hold = 0x00;
	private static byte up = 0x01;
	private static byte down = 0x02;

	
	
	ElevatorReceive(byte[] receivedSchedule){
		runElevator(receivedSchedule[1], receivedSchedule[2], receivedSchedule[3]);
		openCloseDoor(receivedSchedule[4]);
		//isRequest=request;
		//requestFloorNumber=floorRequest;
	}

	public void runElevator(byte motorDirection, byte motorSpinTime, byte currentFloor ) {
		int time = (int)motorSpinTime;
		int floorDisplay = (int)currentFloor;

		if(motorDirection == up || motorDirection == down) {
			while (time != 0){
				try {
					System.out.println(floorDisplay);
					Thread.sleep(1000);
					time--;
					if(motorDirection == up) {
						floorDisplay++;
					} else {
						floorDisplay--;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} 
		}else if (motorDirection == hold) {
			
		}
	}
	
	public void openCloseDoor(byte door) {
		if (door == 1) {
			System.out.println("Doors are open.");
		} else if (door == 0) {
			System.out.println("Doors are closing...");
		}
	}
}


import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;



class ElevatorTest {
	
	public static String NAMING;
	public static int floorRequest;
	/*private static byte hold = 0x00;
	private static byte up = 0x01;
	private static byte down = 0x02;
	private static int sensor = 1;

	DatagramPacket elevatorSendPacket, elevatorReceivePacket;
	DatagramSocket elevatorSendSocket, elevatorReceiveSocket;

	  public static final int TENTH_FLOOR = 10;
	  public static final int SECOND_FLOOR = 2;
	  public static final int BASEMENT_TWO = -2;*/

	  private Elevator elevator;

	  @Before
	  public void initializeElevator(){
	    elevator = new Elevator("one");
	  }

	  @Test
	  public void TestCurrentFloor(){
		//elevator = new Elevator("one");
		  
		int value = elevator.currentFloor(3);
	    assertEquals(value, 3);
	  }
	  
	  @Test
	  public void testResponsePacket(){
		  floorRequest = 2;
		  elevator.responsePacket(floorRequest);
	  
	  }
	  
	  @Test
		void test() {
			fail("Not yet implemented.");
	  }

	  @Test
	  public void testRun() throws Exception {
		throw new RuntimeException("not yet implemented");
	  }

		

	  /*@Test
	  public void checkCurrentFloor(){
	    // Move to floor 2
	    elevator.moveUp();
	    elevator.moveUp();
	    assertEquals(SECOND_FLOOR, elevator.currentFloor());
	  }

	  @Test
	  public void checkMoveDown(){
	    elevator.moveDown();
	    elevator.moveDown();
	    assertEquals(BASEMENT_TWO, elevator.currentFloor());
	  }

	  @Test
	  public void checkDirectionUp(){
	    elevator.addNewDestinatoin(SECOND_FLOOR);
	    assertEquals(ElevatorDirection.ELEVATOR_UP, elevator.direction());
	  }

	  @Test
	  public void checkDirectionDown(){
	    elevator.addNewDestinatoin(BASEMENT_TWO);
	    assertEquals(ElevatorDirection.ELEVATOR_DOWN, elevator.direction());
	  }

	  @Test
	  public void checkDirectionHold(){
	    assertEquals(ElevatorDirection.ELEVATOR_HOLD, elevator.direction());
	  }

	  @Test
	  public void checkStatusEmpty(){
	    assertEquals(ElevatorStatus.ELEVATOR_EMPTY, elevator.status());
	  }

	  @Test
	  public void checkStatusOccupied(){
	    elevator.addNewDestinatoin(TENTH_FLOOR);
	    assertEquals(ElevatorStatus.ELEVATOR_OCCUPIED, elevator.status());
	  }*/
	 
}

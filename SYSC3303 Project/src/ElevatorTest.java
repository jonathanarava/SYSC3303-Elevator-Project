import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

//import static org.assertj.core.api.Assertions.*;

class ElevatorTest {
	
	public static String NAMING;
	public static int floorRequest;
	private static byte hold = 0x00;
	private static byte up = 0x01;
	private static byte down = 0x02;
	private static int sensor = 1;

	DatagramPacket elevatorSendPacket, elevatorReceivePacket;
	DatagramSocket elevatorSendSocket, elevatorReceiveSocket;

	  public static final int TENTH_FLOOR = 10;
	  public static final int SECOND_FLOOR = 2;
	  public static final int BASEMENT_TWO = -2;

	  private Elevator elevator;

	  @Before
	  public void initializeElevator(){
	    elevator = new Elevator("0");
	  }

	  @Test
	  public void testAddingDestination(){
		  
	    elevator.addNewDestinatoin(TENTH_FLOOR);
	    assertEquals(TENTH_FLOOR, elevator.nextDestionation());
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

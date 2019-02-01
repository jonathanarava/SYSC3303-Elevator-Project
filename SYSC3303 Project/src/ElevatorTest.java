import static org.junit.jupiter.api.Assertions.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ElevatorTest extends Elevator {

	public ElevatorTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	//Mock Scheduler
	public void mockScheduler() {
		// data being sent from the scheduler to the Elevator
		byte motorDirection = 0;
		byte motorSpinTime = 1;
		byte currentFloor = 3;
		
	}
	
	 @Before
	    public void setUp() throws Exception {
		 Elevator elevator1 = new Elevator("1");
	 }
	 
	@Test
	void TestElevatorSendPacket() {
		//Elevator elevator1 = new Elevator("1");
		
		//
	}
	
	@Test
	void TestDoorOpen() {
		Elevator elevator1 = null;
		elevator1.openCloseDoor((byte)1);
		
	}
	
	@Test 
	void TestRunELevator() {
		
	}
	
	
	void test() {
		
		
		fail("Not yet implemented");
	}

}

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchedulerTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testCurrentFloorTracker() {
		//Scheduler s = new Scheduler();
		Scheduler.currentFloor = 5;
		Scheduler.elevatorOrFloorID = 1;
		Scheduler.currentFloorTracker();
		System.out.println(Scheduler.ele1);
		assertTrue(Scheduler.ele0 == 5);
	}
	@Test
	void test() {
		fail("Not yet implemented");
	}

}

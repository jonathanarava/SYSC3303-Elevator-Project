import static org.junit.Assert.assertEquals;
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

	void testCurrentFloorTracker() {
		Scheduler s = new Scheduler();
		s.currentFloor = 5;
		s.elevatorOrFloorID = 1;
		s.currentFloorTracker();
		System.out.println(s.ele0);
		assertEquals(s.ele0,5);
	}
	@Test
	void test() {
		fail("Not yet implemented");
	}

}

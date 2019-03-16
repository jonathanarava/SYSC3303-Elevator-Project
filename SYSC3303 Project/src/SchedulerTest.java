import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchedulerTest {
	//Scheduler Scheduler = new Scheduler();
	
	
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
	void SchedulerAlgotest() {
		Scheduler Scheduler = new Scheduler();
		Scheduler.linkedListInitialization();
		
		// Input from Elevator to Scheduler
		byte[] inputArray = new byte[] {(byte)21,(byte) 0,(byte) 1,(byte) 0,(byte) 0,(byte) 3,(byte) 0 };
		
		// Expected Output
		byte[] expected = new byte[] {(byte)54,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 1};
		// Actual Output
		byte[] actual = Scheduler.SchedulingAlgorithm(inputArray);
		
		
		Assert.assertArrayEquals(expected, actual);
		
	}

}

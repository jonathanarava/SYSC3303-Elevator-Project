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
	Scheduler Scheduler = new Scheduler(true);

	@Test 
	void SchedulerAlgotestUP() {
		Scheduler.linkedListInitialization();
				
		// Input from Elevator to Scheduler
		byte[] inputArray = new byte[] {(byte)21,(byte) 0,(byte) 1,(byte) 0,(byte) 0,(byte) 3,(byte) 0 };
		System.out.println(Arrays.toString(inputArray));

		// Expected Output
		byte[] expected = new byte[] {(byte)54,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 1};
		// Actual Output
		byte[] actual = Scheduler.SchedulingAlgorithm(inputArray);
		System.out.println(Arrays.toString(actual));

		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test 
	void SchedulerAlgotestDOWN() {
		Scheduler.linkedListInitialization();		
		// Input from Elevator to Scheduler
		byte[] inputArray = new byte[] {(byte)21,(byte) 0,(byte) 1,(byte) 4,(byte) 0,(byte) 1,(byte) 0 };
		System.out.println(Arrays.toString(inputArray));
		
		// Expected Output
		byte[] expected = new byte[] {(byte)54,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 2};
		// Actual Output
		byte[] actual = Scheduler.SchedulingAlgorithm(inputArray);
		System.out.println(Arrays.toString(actual));
		
		Assert.assertArrayEquals(expected, actual);
	}
	
	@Test 
	void SchedulerAlgotestHOLD() {
		Scheduler.linkedListInitialization();		
		// Input from Elevator to Scheduler
		byte[] inputArray = new byte[] {(byte)21,(byte) 0,(byte) 0,(byte) 1,(byte) 0,(byte) 1,(byte) 0 };
		System.out.println(Arrays.toString(inputArray));

		// Expected Output
		byte[] expected = new byte[] {(byte)54,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 4};
		// Actual Output
		byte[] actual = Scheduler.SchedulingAlgorithm(inputArray);
		System.out.println(Arrays.toString(actual));
		
		Assert.assertArrayEquals(expected, actual);
	}

}

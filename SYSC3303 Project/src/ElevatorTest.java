
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ElevatorTest {

	public static String NAMING;
	public static int floorRequest;
	private static int sensor = 1;
	/*
	 * private static byte hold = 0x00; private static byte up = 0x01; private
	 * static byte down = 0x02;
	 * 
	 * 
	 * DatagramPacket elevatorSendPacket, elevatorReceivePacket; DatagramSocket
	 * elevatorSendSocket, elevatorReceiveSocket;
	 * 
	 * public static final int TENTH_FLOOR = 10; public static final int
	 * SECOND_FLOOR = 2; public static final int BASEMENT_TWO = -2;
	 */

	private Elevator elevator;

	@Test
	public void testinitializeElevator() {
		elevator = new Elevator("one");
		assertNotEqual(elevator,null);
		
		
	}


	@Test
	public void testResponsePacket() {
		Elevator elevator=new Elevator();
		floorRequest = 2;
		byte[] testarray = new byte[4];
		testarray[0] = 0;
		testarray[1] = 2;
		testarray[2] = 0;
		testarray[3] = 0;

		assertArrayEquals(testarray, elevator.responsePacket(floorRequest));

	}

	@Test
	void test() {
		fail("Not yet implemented.");
	}

	@Test
	public void testRun() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	@Test
	public void testElevator() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	@Test
	public void testOpenCloseDoor() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	@Test
	public void testCurrentFloor() {
		Elevator elevator=new Elevator();
		// elevator = new Elevator("one");
		assertEquals(3, elevator.currentFloor(3));
	}

	@Test
	public void testRunElevator() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/*
	 * @Test public void checkCurrentFloor(){ // Move to floor 2 elevator.moveUp();
	 * elevator.moveUp(); assertEquals(SECOND_FLOOR, elevator.currentFloor()); }
	 * 
	 * @Test public void checkMoveDown(){ elevator.moveDown(); elevator.moveDown();
	 * assertEquals(BASEMENT_TWO, elevator.currentFloor()); }
	 * 
	 * @Test public void checkDirectionUp(){
	 * elevator.addNewDestinatoin(SECOND_FLOOR);
	 * assertEquals(ElevatorDirection.ELEVATOR_UP, elevator.direction()); }
	 * 
	 * @Test public void checkDirectionDown(){
	 * elevator.addNewDestinatoin(BASEMENT_TWO);
	 * assertEquals(ElevatorDirection.ELEVATOR_DOWN, elevator.direction()); }
	 * 
	 * @Test public void checkDirectionHold(){
	 * assertEquals(ElevatorDirection.ELEVATOR_HOLD, elevator.direction()); }
	 * 
	 * @Test public void checkStatusEmpty(){
	 * assertEquals(ElevatorStatus.ELEVATOR_EMPTY, elevator.status()); }
	 * 
	 * @Test public void checkStatusOccupied(){
	 * elevator.addNewDestinatoin(TENTH_FLOOR);
	 * assertEquals(ElevatorStatus.ELEVATOR_OCCUPIED, elevator.status()); }
	 */

}

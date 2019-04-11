
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

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
		int name = 0;
		int initiateFloor = 0;
		List<byte[]> elevatorTable = null;
		int RealTimefloorRequest = 10;
		elevator = new Elevator(name, initiateFloor, elevatorTable, RealTimefloorRequest);
		assertNotNull(elevator);
	}

	@Test
	public void testCreateResponsePacketData() {
		Elevator elevator = new Elevator();
		int update = 2;
		int request = 1;
		elevator.elevatorNumber = 0;
		elevator.setSensor(4);
		elevator.RealTimefloorRequest = 10;

		byte[] testarrayRequest = new byte[] { 21, 0, 1, 4, 0, 10, 0, 0 };
		byte[] testarrayUpdate = new byte[] { 21, 0, 2, 4, 0, 10, 0, 0 };

		assertArrayEquals(testarrayRequest, elevator.createResponsePacketData(request, (byte) 0));
		assertArrayEquals(testarrayUpdate, elevator.createResponsePacketData(update, (byte) 0));
	}

	/*
	 * @Test public void testOpenCloseDoor() throws Exception { Elevator elevator1 =
	 * new Elevator(); // Test for Doors closed String expected =
	 * "Doors are closed."; assertEquals(expected, elevator1.openCloseDoor((byte)
	 * 0)); // Test for Doors open String expected1 = "Doors are open.";
	 * assertEquals(expected1, elevator1.openCloseDoor((byte) 1)); System.out.
	 * println("------------------End of testOpenCloseDoor()---------------------\n "
	 * ); }
	 */

	@Test
	public void testSetFloor() {
		Elevator elevator = new Elevator();

		// elevator = new Elevator("one");
		assertEquals(10, elevator.setSensor(10));

	}

	/*
	 * @Test public void testRunElevator() throws Exception { Elevator elevator =
	 * new Elevator(); elevator.currentFloor(2); // Elevator going up //
	 * System.out.println(elevator.runElevator((byte) 1,(byte) 4)); assertEquals(6,
	 * elevator.runElevator((byte) 1, (byte) 4)); // Elevator going down //
	 * assertEquals(3, elevator.runElevator((byte) 2,(byte) 4));
	 * System.out.println("current floor: " + elevator.sensor + "\n\n");
	 * 
	 * assertEquals(10, elevator.runElevator((byte) 1, (byte) 4));
	 * System.out.println("current floor: " + elevator.sensor);
	 * 
	 * System.out.
	 * println("------------------End of testRunElevator()-------------------\n ");
	 * }
	 */

}

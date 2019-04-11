import static org.junit.Assert.assertArrayEquals;
import org.junit.jupiter.api.Test;

class FloorTest {

	@Test
	public void testCreateResponsePacket() throws Exception {
		Floor floor = new Floor();
		floor.name = 0;
		floor.realTimeDirectionRequest = 1;
		byte[] testarrayRequest = new byte[] { 69, 0, 1, 0, 1, 0, 0, 0 };

		assertArrayEquals(testarrayRequest, floor.createResponsePacketData(1, (byte) 0));

	}

}

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

//import static org.assertj.core.api.Assertions.*;

class ElevatorTest {

	
	 @Test
    public void exists() throws Exception {
        assertThat(new Elevator()).isNotNull();
	 }
	 
}

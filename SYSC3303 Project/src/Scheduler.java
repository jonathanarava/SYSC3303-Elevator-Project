import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Scheduler {
	
	public static DatagramSocket schedulerSocetSendElevator, schedulerSocketReceiveElevator;
	public static DatagramPacket schedulerSendPacket, schedulerReceivePacket;
	
	//public boolean isListen;
	
	public int elevatorID;
	public int floorRequest;
	public int currentFloor;

	public Scheduler() {}

	public void breakDown(byte[] x) {
		
	}

	public void receivedPacket() throws InterruptedException  {

		byte data[] = new byte[4];

		schedulerReceivePacket = new DatagramPacket(data, data.length);
		//System.out.println("Server: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {   
				schedulerSocketReceiveElevator.receive(schedulerReceivePacket);
				System.out.println("Received it");
				System.out.println(data.toString());

				//schedulerSocketReceiveElevator.close();
				//schedulerSocetSendElevator.close()
		
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}	
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e ) {
			e.printStackTrace();
			System.exit(1);
		}
		
		byte[] parseThroughData = new byte[4];
		
		decodingRequest(parseThroughData);

		byte [] responseByteArray = responsePacket();
		if(elevatorID == 1) {
			while(floorRequest != 0) {
				floorRequest--;
				schedulerSendPacket = new DatagramPacket(responseByteArray, schedulerReceivePacket.getLength(),
						schedulerReceivePacket.getAddress(), schedulerReceivePacket.getPort());
			}
		}
		
		// or (as we should be sending back the same thing)
		// System.out.println(received); 

		// Send the datagram packet to the client via the send socket. 
		try {
			schedulerSocetSendElevator.send(schedulerSendPacket);
		} catch (IOException e) {
			System.out.print("hi");
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println(data);
	}
	
	public void decodingRequest(byte[] data) {
		
		elevatorID = data[1];
		floorRequest = data[2];
		currentFloor = data[3];
		
	}
	
	public  byte[] responsePacket() {

		// creates the byte array according to the required format
		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(0);
		
		if((currentFloor - floorRequest) <0) {
			requestElevator.write(1);
			requestElevator.write(0);
		} else if((currentFloor - floorRequest) >0) {
			requestElevator.write(2);
			requestElevator.write(0);
		} else {
			requestElevator.write(0);    //motorDirection
			requestElevator.write(0); 	//open or Close
		}
		
		if((currentFloor-floorRequest) != 0) {
			requestElevator.write(1);
		} else {
			requestElevator.write(0);    //MotorSpin Time
		}
		
		
		return requestElevator.toByteArray();

	}

	/*void stopListening() {
		isListen=false;
		schedulerSocketReceiveElevator.close();
	}*/
	
	public static void main(String args[]) throws InterruptedException {
		
		Scheduler packet = new Scheduler();
		for(;;) {
		try {
			schedulerSocetSendElevator = new DatagramSocket(23);
			//schedulerSocketReceiveElevator = new DatagramSocket();// can be any available port, Scheduler will reply to the port
															// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
		
		packet.receivedPacket();
		//Thread.sleep(1000);
		//packet.stopListening();

		}


	}
}
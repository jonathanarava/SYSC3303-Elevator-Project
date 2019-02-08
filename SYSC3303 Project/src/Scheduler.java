import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Scheduler {
	
	public static DatagramSocket schedulerSocetSendElevator, schedulerSocketReceiveElevator;
	public static DatagramPacket schedulerSendPacket, schedulerReceivePacket;
	
	public static Object object = new Object();
	//public boolean isListen;
	
	public int elevatorID;
	public int floorRequest;
	public int currentFloor;

	public Scheduler() {
		try {
			schedulerSocetSendElevator = new DatagramSocket();
			schedulerSocketReceiveElevator = new DatagramSocket(23);// can be any available port, Scheduler will reply to the port
															// that's been received
		} catch (SocketException se) {// if DatagramSocket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void breakDown(byte[] x) {
		
	}

	public synchronized void receivedPacket() throws InterruptedException  {

		byte data[] = new byte[4];

		schedulerReceivePacket = new DatagramPacket(data, data.length);
		//System.out.println("Server: Waiting for Packet.\n");
		
		//synchronized(object) {
		// Block until a datagram packet is received from receiveSocket.
		try {   
					//System.out.println("waiting");	
					schedulerSocketReceiveElevator.receive(schedulerReceivePacket);
					System.out.println("Received it");
					//object.wait();		
			
			System.out.println(Arrays.toString(data));
			//schedulerSocketReceiveElevator.close();
			//schedulerSocetSendElevator.close()
		
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}	
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e ) {
			e.printStackTrace();
			System.exit(1);
		}
		
		byte[] parseThroughData = new byte[4];
		
		decodingRequest(parseThroughData);

		byte [] responseByteArray = new byte[4];
		responseByteArray[0]=0;
		responseByteArray[1]=1;
		responseByteArray[2]=1;
		responseByteArray[3]=0;
		/*if(elevatorID == 1) {
			while(floorRequest != 0) {*/
				floorRequest--;
				//byte [] responseByteArray = responsePacket();
				schedulerSendPacket = new DatagramPacket(responseByteArray, schedulerReceivePacket.getLength(),
						schedulerReceivePacket.getAddress(), schedulerReceivePacket.getPort());
			//}
		//}
		
		// or (as we should be sending back the same thing)
		// System.out.println(received); 

		// Send the datagram packet to the client via the send socket. 
		try {
			schedulerSocetSendElevator.send(schedulerSendPacket);
			System.out.println("Sent");
		} catch (IOException e) {;
			e.printStackTrace();
			System.exit(1);
		}
	}
		//System.out.println();
	
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
		Elevator e = new Elevator("1", object);
		for(;;) {
		Thread t1 = new Thread(new Runnable() {				// thread to run the agent method to produce the ingredients 
			public void run() {
				try {
					e.sendPacket();
					e.receivePacket();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

			}
			});
		
		Thread t2 = new Thread(new Runnable() {				// thread to run the agent method to produce the ingredients 
			public void run() {
				try {
					packet.receivedPacket();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			});
		t1.start();
		t2.start();
		
		t1.join();
		t2.join();
		//Thread.sleep(1000);
		//packet.stopListening();

		}


	}
}
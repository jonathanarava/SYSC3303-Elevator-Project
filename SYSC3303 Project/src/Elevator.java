import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Elevator extends Thread {
	
	/* Packet declaration to send and receive data */
	private static DatagramPacket ElevatorSendPacket, ElevatorReceivePacket;
	
	/* DatagramSocket declaration */
	private static DatagramSocket ElevatorSendRecieveReceiveSocket;

	/* UNIFIED CONSTANTS DECLARATION FOR ALL CLASSES*/
	public static final byte HOLD = 0x00;// Elevator is in hold state
	public static final byte UP = 0x01;// Elevator is going up
	public static final byte DOWN = 0x02;// Elevator is going down
	public static final int Elevator_ID = 21;// for identifying the packet's source as Elevator
	public static final int DOOR_OPEN = 1;// the door is open when ==1
	public static final int DOOR_DURATION = 4;// duration that doors stay open for
	public static final int REQUEST = 1;// for identifying the packet sent to scheduler as a request
	public static final int UPDATE = 2;// for identifying the packet sent to scheduler as a status update
	
	/* Variables used in this class*/
	private int sensor;
	private int nameOfElevator;
	private int toDoID;
	private byte instruction;
	private int initialFloor;
	private static List<String> fileRequests = new ArrayList<String>();

	private boolean hasRequest = false;
	private static int floorRequest;
	//private byte motorDirection;

	private static int floorButton;

	/* Table to synchronize threads */
	public LinkedList<byte[]> ElevatorTable =new LinkedList<byte[]>();
	//public static List<byte[]> ElevatorTable = Collections.synchronizedList(new ArrayList<byte[]>());
	public boolean runningStatus= false;

	private boolean holdReceived;

	public static int sensorArray[] = new int[4];
	
	public Elevator() {};
	
	public Elevator(int nameOfElevator, int initialFloor,  LinkedList<byte[]> ElevatorTable) {
		this.nameOfElevator = nameOfElevator;
		this.setInitialFloor(initialFloor);
		this.ElevatorTable = ElevatorTable;
	}

	public int getInitialFloor() {
		return this.sensor;
	}

	public void setInitialFloor(int initialFloor) {
		sensor = initialFloor;
		if(nameOfElevator == 0) {
			sensorArray[0] = sensor;
		} else if(nameOfElevator == 1) {
			sensorArray[1] = sensor;
		}else if(nameOfElevator == 2) {
			sensorArray[2] = sensor;
		} else if(nameOfElevator == 3) {
			sensorArray[3] = sensor;
		}
	}
	
	public int runElevator(byte motorDirection) {
		if (motorDirection == UP || motorDirection == DOWN) {
			try {
				System.out.println("current floor: " + sensor + " --> of Elevator "+nameOfElevator); // sensor = current floor
				Thread.sleep(2000);
				if (motorDirection == UP) {
					System.out.println("Elevator going up");
					sensor++; // increment the floor
					setInitialFloor(sensor); // updates the current floor
				} else if (motorDirection == DOWN) {
					System.out.println("Elevator going down");
					sensor--; // decrements the floor
					setInitialFloor(sensor); // updates the current floor
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else if (motorDirection == HOLD) {
			setInitialFloor(sensor); // updates current floor - in this case nothing changes
		}
		System.out.println("current floor: " + sensor + " --> of Elevator "+nameOfElevator); // prints out the current floor - in this case destination floor
		return getInitialFloor(); // returns and updates the final current of the floor - in this case destination floor
	}
	
	public byte[] responsePacketRequest(int requestUpdate, int floorRequest) {

		/*
		 * Elevator --> SCHEDULER (Elevator or floor (Elevator-21), Elevator id(which
		 * Elevator), FloorRequest/update, curentFloor, up or down, destFloor,
		 * instruction) (
		 */
		// creates the byte array according to the required format

		ByteArrayOutputStream requestElevator = new ByteArrayOutputStream();
		requestElevator.write(Elevator_ID); // Elevator
		requestElevator.write(nameOfElevator); // Elevator id

		// request/ update
		if (requestUpdate == REQUEST) {
			requestElevator.write(REQUEST); // request/
			requestElevator.write((byte) getInitialFloor()); // current floor
			requestElevator.write(0); // up or down
			requestElevator.write(floorRequest); // dest floor
			requestElevator.write(0); // instruction
		} else if (requestUpdate == UPDATE) {
			requestElevator.write(UPDATE); // update
			requestElevator.write((byte) getInitialFloor()); // current floor
			requestElevator.write(0); // up or down
			requestElevator.write(floorRequest); // dest floor
			requestElevator.write(0); // instruction
		} else {
			requestElevator.write(requestUpdate); // update
			requestElevator.write((byte) getInitialFloor()); // current floor
			requestElevator.write(0); // up or down
			requestElevator.write(floorRequest); // dest floor
			requestElevator.write(0);
		}
		return requestElevator.toByteArray();
	}

	public String openCloseDoor(byte door) {
		String msg;
		if (door == DOOR_OPEN) {
			msg = "Doors are open.";
			System.out.println("\n" + msg);
			try {
				int i = 4;
				while (i != 0) {
					System.out.format("Seconds until Elevator %d door closes: %d second \n", nameOfElevator,i);
					i--;
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			msg = "Doors are closed.";
			System.out.println(msg);
		}
		return msg;
	}
	
	public synchronized static void sendPacket(byte[] toSend) throws InterruptedException {

		byte[] data = new byte[7];
		data = toSend;
		
		System.out.print("Sending to scheduler: ");
		System.out.println(Arrays.toString(data));
		try {
			InetAddress address = InetAddress.getByName("134.117.59.107");
			//System.out.println("\nSending to scheduler from Elevator "+ data[1] + ":" + Arrays.toString(data));
			ElevatorSendPacket = new DatagramPacket(data, 7,address, 369);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		try {
			ElevatorSendRecieveReceiveSocket.send(ElevatorSendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public synchronized byte[] receivePacket() throws InterruptedException {
		
		byte data[] = new byte[7];
		ElevatorReceivePacket = new DatagramPacket(data, data.length);
		
		try {
			// Block until a datagram packet is received from receiveSocket.
			ElevatorSendRecieveReceiveSocket.receive(ElevatorReceivePacket);
			//System.out.print("Received from scheduler: ");
			//System.out.println(Arrays.toString(data));
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		
		//ElevatorTable.add(data);
		System.out.print("Received from scheduler: ");
		System.out.println(Arrays.toString(data));
		return data;

	}
	
	public static int get1() {
		return sensorArray[1];
	}


	public void fileReader(String fullFile) { 
		String text = "";
		int i=0;
		try { 
			FileReader input = new FileReader(fullFile);
			Scanner reader = new Scanner(input);
			reader.useDelimiter("[\n]");

			while (reader.hasNext()){
				text = reader.next();
				if (i<=1) {
					i++;
				} else if(i>=2) {
					fileRequests.add(text);
					i++;
				}
			}
		}catch(Exception e) { e.printStackTrace(); }
	}
	
	public void run() {
		while(!isInterrupted()) {
				while(runningStatus == true) {
					if(toDoID == nameOfElevator) {
						
						System.out.println("Elevator number" +toDoID + " is on floor " + sensorArray[1]);
						if(instruction == 2 || instruction == 1) {
							//System.out.println(instruction);
							this.runElevator(instruction);
							try {
								Thread.sleep(10);
								sendPacket(responsePacketRequest(UPDATE,0));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}				
						} else if (instruction == 0) {
							System.out.println();
							System.out.printf("------------------------------ OPENING DOOR FOR ELEVATOR %d -----------------", nameOfElevator);
							openCloseDoor((byte)DOOR_OPEN);
							this.holdReceived = true;
						} else if (instruction == 4) {
							//System.out.println(instruction + "  ---> ELEVATOR " + nameOfElevator);
							System.out.printf("No requests. Elevator %d has stopped\n", nameOfElevator);
/*							try {
								Thread.sleep(1);
								sendPacket(responsePacketRequest(UPDATE,0));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}*/		
						} else if(instruction == 5) {
							
						}
					}
					this.runningStatus = false;
				}
			}
		System.out.println("Elevator " + nameOfElevator + " Thread ended");
	}

	public static void main(String args[]) throws InterruptedException {
		
		int initialFloor0 = Integer.parseInt(args[0]);	// The number of Elevators in the system is passed via
		int initialFloor1 = Integer.parseInt(args[1]);	
		int initialFloor2 = Integer.parseInt(args[2]);	// The number of Elevators in the system is passed via
		int initialFloor3 = Integer.parseInt(args[3]);	
		
		LinkedList<byte[]> ElevatorTable1 =new LinkedList<byte[]>();
		Elevator Ele0 = new Elevator(0, initialFloor0, ElevatorTable1);
		Elevator Ele1 = new Elevator(1, initialFloor1, ElevatorTable1);
		Elevator Ele2 = new Elevator(2, initialFloor2, ElevatorTable1);
		Elevator Ele3 = new Elevator(3, initialFloor3, ElevatorTable1);
		System.out.println("Elevator number" + get1());
		
		try {
			ElevatorSendRecieveReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {// if Socket creation fails an exception is thrown
			se.printStackTrace();
			System.exit(1);
		}
		
		Ele0.fileReader("M://hello.txt");
		//System.out.println(fileRequests.get(0));
		
		sendPacket(Ele1.responsePacketRequest(UPDATE,0));
		sendPacket(Ele0.responsePacketRequest(UPDATE,0));
		sendPacket(Ele2.responsePacketRequest(UPDATE,0));
		sendPacket(Ele3.responsePacketRequest(UPDATE,0));
		
		sendPacket(Ele1.responsePacketRequest(3,0));
		//sendPacket(Ele1.responsePacketRequest(UPDATE,0));
/*		Ele0.ElevatorTable.add(0,Ele0.responsePacketRequest(1, 6));
		Ele1.ElevatorTable.add(1,Ele1.responsePacketRequest(1, 4));
		
		sendPacket(ElevatorTable1.get(0));
		sendPacket(ElevatorTable1.get(1));
		ElevatorTable1.clear();*/

		Thread fileStuff = new Thread() {
			public void run() {
				while(true) {
					while(Ele0.holdReceived || Ele1.holdReceived || Ele2.holdReceived || Ele3.holdReceived) {
						if(fileRequests.isEmpty() && Ele0.holdReceived) {
							try {
								sendPacket(Ele0.responsePacketRequest(UPDATE,0));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							Ele0.holdReceived = false;
							break;
						} else if(fileRequests.isEmpty() && Ele1.holdReceived) {
							try {
								sendPacket(Ele1.responsePacketRequest(UPDATE,0));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							Ele1.holdReceived = false;
							break;
						} else if(fileRequests.isEmpty() && Ele2.holdReceived) {
							try {
								sendPacket(Ele2.responsePacketRequest(UPDATE,0));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							Ele2.holdReceived = false;
							break;
						} else if(fileRequests.isEmpty() && Ele3.holdReceived) {
							try {
								sendPacket(Ele3.responsePacketRequest(UPDATE,0));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							Ele3.holdReceived = false;
							break;
							
						} else {
							for(int i = 0; i <fileRequests.size(); i++) {
								String command = fileRequests.get(i);
								String segment[] = command.split(" ");
								floorButton = Integer.parseInt(segment[1]);
								floorRequest = Integer.parseInt(segment[3]);
								if(floorButton == Ele0.getInitialFloor()) {								
									try {
										sendPacket(Ele0.responsePacketRequest(REQUEST, floorRequest));
										fileRequests.remove(i);
										break;
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} else if(floorButton == Ele1.getInitialFloor()) {
									try {
										sendPacket(Ele1.responsePacketRequest(REQUEST, floorRequest));
										fileRequests.remove(i);
										break;
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}else if(floorButton == Ele2.getInitialFloor()) {
									try {
										sendPacket(Ele2.responsePacketRequest(REQUEST, floorRequest));
										fileRequests.remove(i);
										break;
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}else if(floorButton == Ele3.getInitialFloor()) {
									try {
										sendPacket(Ele3.responsePacketRequest(REQUEST, floorRequest));
										fileRequests.remove(i);
										break;
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
							Ele0.holdReceived = false;
							Ele1.holdReceived = false;
							Ele2.holdReceived = false;
							Ele3.holdReceived = false;
							break;
						}
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		

		GUI gui = new GUI();
		Thread guiThread = new Thread(gui);
		guiThread.start();

		Ele0.start();
		Ele1.start();
		Ele2.start();
		Ele3.start();
		fileStuff.start();

		try {
			while(true) {
				byte[] x = new byte[7];
				byte[] data = new byte[7];
				long startTime = System.nanoTime();
				x = Ele0.receivePacket();
				ElevatorTable1.add(x);
				if(x[1] == 0) {
					data = x;
					Ele0.toDoID = data[1];
					Ele0.instruction = data[6];
					Ele0.runningStatus = true;
				}
				if(x[1] == 1) {
					data = x;
					Ele1.toDoID = data[1];
					Ele1.instruction = data[6];
					Ele1.runningStatus = true;
				
				}
				if (x[1] == 2) {
					data = x;
					Ele2.toDoID = data[1];
					Ele2.instruction = data[6];
					Ele2.runningStatus = true;
				}
				if(x[1] == 3) {
					data = x;
					Ele3.toDoID = data[1];
					Ele3.instruction = data[6];
					System.out.println("        ELEVATOR 4 INSTRUCTION               " +  data[6]+ " ELEVATOR ID " + data[1]);
					Ele3.runningStatus = true;	
				} 
				long endTime = System.nanoTime();
				long timeElapsed = endTime - startTime;
				System.out.println("\n\nExecution time in milliseconds : " + 
						timeElapsed / 1000000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}

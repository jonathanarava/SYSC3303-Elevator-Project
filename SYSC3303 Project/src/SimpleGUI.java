import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

public class SimpleGUI extends Scheduler{
	// Project specification: Dunton Tower ELevator System Simulator ( 4 elevaltors, 22 floors)
	int numElevators = 4;
	int numFloors = 22;
	public JButton DirectionButtons[] = new JButton[8]; // UP and DOWN buttons for the elevators

	//String Initialization
	String elevator1msg = "Elevator 1";
	String elevator1 = "Elevator 1";

	// Elevator Status declaration used for elevator direction
	// Consistent with the Scheduler class
	private static final byte UP = 0x02;// elevator is going up
	private static final byte DOWN = 0x01;// elevator is going down
	private static final byte STOP = 0x03;
	private static final byte HOLD = 0x04;// elevator is in hold state
	
	// JTextarea to display current floor of each elevator and to display error msg
	public JTextArea textArea1, textArea2, textArea3, textArea4, ErrorMsgTextArea;

	SimpleGUI() {
		JFrame frame = new JFrame("SimpleElevator GUI");
		
		JPanel Elevator1 = new JPanel();
		JPanel ButtonPannel1 = new JPanel();
		JPanel Elevator2 = new JPanel();
		JPanel ButtonPannel2 = new JPanel();
		JPanel Elevator3 = new JPanel();
		JPanel ButtonPannel3 = new JPanel();
		JPanel Elevator4 = new JPanel();
		JPanel ButtonPannel4 = new JPanel();

		JPanel FullPanel = new JPanel();
		JPanel ElevatorStatus = new JPanel();
		JPanel ErrorMsg = new JPanel();

		// UP and DOWN button initialization
		DirectionButtons = new JButton[8];

		//Elevator Status Panel that holds 4 panels: Elevator1 - Elevator4
		ElevatorStatus.setLayout(new GridLayout(2, 2));
		ElevatorStatus.setBorder(new TitledBorder("Elevator Status"));

		ErrorMsg.setLayout(new FlowLayout());
		ErrorMsg.setBorder(new TitledBorder("Error Message"));

		// Elevator 1
		Elevator1.setLayout(new FlowLayout());
		Elevator1.setBorder(new TitledBorder("Elevator 1"));
		// textArea1 is initialized to set current floor of elevator 1 to 0
		// which is later updated by the run method
		textArea1 = new JTextArea("0"/* Integer.toString(elevatorCurrentFloor[0]) */);
		textArea1.setLineWrap(true);
		textArea1.setWrapStyleWord(true);
		textArea1.setEditable(false);
		DirectionButtons[0] = new JButton(" Down ");
		DirectionButtons[0].setEnabled(false);
		DirectionButtons[1] = new JButton(" UP ");
		DirectionButtons[1].setEnabled(false);

		ButtonPannel1.setLayout(new BorderLayout());
		ButtonPannel1.add(DirectionButtons[0], BorderLayout.SOUTH);
		ButtonPannel1.add(DirectionButtons[1], BorderLayout.NORTH);
		Elevator1.add(textArea1, BorderLayout.WEST);
		Elevator1.add(ButtonPannel1, BorderLayout.EAST);
		// Elevator 1 end

		// Elevator 2
		Elevator2.setLayout(new FlowLayout());
		Elevator2.setBorder(new TitledBorder("Elevator 2"));
		// textArea2 is initialized to set current floor of elevator 1 to 0
		// which is later updated by the run method
		textArea2 = new JTextArea("0"/* Integer.toString(elevatorCurrentFloor[1]) */);
		textArea2.setLineWrap(true);
		textArea2.setWrapStyleWord(true);
		textArea2.setEditable(false);
		DirectionButtons[2] = new JButton(" Down ");
		DirectionButtons[2].setEnabled(false);
		DirectionButtons[3] = new JButton(" UP ");
		DirectionButtons[3].setEnabled(false);

		ButtonPannel2.setLayout(new BorderLayout());
		ButtonPannel2.add(DirectionButtons[2], BorderLayout.SOUTH);
		ButtonPannel2.add(DirectionButtons[3], BorderLayout.NORTH);
		Elevator2.add(textArea2, BorderLayout.WEST);
		Elevator2.add(ButtonPannel2, BorderLayout.EAST);
		// Elevator 2 end

		// Elevator 3
		Elevator3.setLayout(new FlowLayout());
		Elevator3.setBorder(new TitledBorder("Elevator 3"));
		// textArea3 is initialized to set current floor of elevator 1 to 0
		// which is later updated by the run method
		textArea3 = new JTextArea("0"/* Integer.toString(elevatorCurrentFloor[2]) */);
		textArea3.setLineWrap(true);
		textArea3.setWrapStyleWord(true);
		textArea3.setEditable(false);
		DirectionButtons[4] = new JButton(" Down ");
		DirectionButtons[4].setEnabled(false);
		DirectionButtons[5] = new JButton(" UP ");
		DirectionButtons[5].setEnabled(false);

		ButtonPannel3.setLayout(new BorderLayout());
		ButtonPannel3.add(DirectionButtons[4], BorderLayout.SOUTH);
		ButtonPannel3.add(DirectionButtons[5], BorderLayout.NORTH);
		Elevator3.add(textArea3, BorderLayout.WEST);
		Elevator3.add(ButtonPannel3, BorderLayout.EAST);
		// Elevator 3 end

		// Elevator 4
		Elevator4.setLayout(new FlowLayout());
		Elevator4.setBorder(new TitledBorder("Elevator 4"));
		// textArea4 is initialized to set current floor of elevator 1 to 0
		// which is later updated by the run method
		textArea4 = new JTextArea("0"/* Integer.toString(elevatorCurrentFloor[3]) */);
		textArea4.setLineWrap(true);
		textArea4.setWrapStyleWord(true);
		textArea4.setEditable(false);
		DirectionButtons[6] = new JButton(" Down ");
		DirectionButtons[6].setEnabled(false);
		DirectionButtons[7] = new JButton(" UP ");
		DirectionButtons[7].setEnabled(false);

		ButtonPannel4.setLayout(new BorderLayout());
		ButtonPannel4.add(DirectionButtons[6], BorderLayout.SOUTH);
		ButtonPannel4.add(DirectionButtons[7], BorderLayout.NORTH);
		Elevator4.add(textArea4, BorderLayout.WEST);
		Elevator4.add(ButtonPannel4, BorderLayout.EAST);
		// Elevator 4 end

		// ErrorMsg
		JTextArea ErrorMsgTextArea = new JTextArea("Elevator 2 Working");
		ErrorMsgTextArea.setLineWrap(true);
		ErrorMsgTextArea.setWrapStyleWord(true);
		ErrorMsgTextArea.setEditable(false);
		JScrollPane ErrorMsgScrollPlane = new JScrollPane(ErrorMsgTextArea);
		ErrorMsgScrollPlane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		ErrorMsgScrollPlane.setPreferredSize(new Dimension(390, 100));
		ErrorMsg.add(ErrorMsgScrollPlane);
		// ErrorMsg End

		// Add Elevator1, Elevator2, Elevator3, Elevator4 panels to ElevatorStatus Panel 
		ElevatorStatus.add(Elevator1);
		ElevatorStatus.add(Elevator2);
		ElevatorStatus.add(Elevator3);
		ElevatorStatus.add(Elevator4);

		// Add ElevatorStatus an ErrorMsg Panel to FullPanel
		FullPanel.add(ElevatorStatus, BorderLayout.NORTH);
		FullPanel.add(ErrorMsg, BorderLayout.SOUTH);
		
		// Add FullPanel to the frame
		frame.add(FullPanel);

		// Set frame dimesnions and set the frame visible
		frame.setSize(425, 380);
		frame.setVisible(true);
	}

	//UpdateGUICurrentFloor method updates the current floor of each elevator
	public void UpdateGUICurrentFloor(int elevator, int currentFloor) {
		switch (elevator) {
			case 0:	// Elevator 1
				//sets the current floor to diplay on the GUI
				textArea1.setText(Integer.toString(currentFloor));
				break;
			case 1: // Elevator 2
				textArea2.setText(Integer.toString(currentFloor));
				break;
			case 2: // Elevator 3
				textArea3.setText(Integer.toString(currentFloor));
				break;
			case 3: // Elevator 4
				textArea4.setText(Integer.toString(currentFloor));
				break;
		}
	}

	// UpdateGUIErrorMsg takes in an errorMsg sent from the Scheduler to display on
	// the errorMsg scrollplane
	public void UpdateGUIErrorMsg(String errorMsg) {
		ErrorMsgTextArea.append(Integer.toString(currentFloor));
		 //textArea = new JTextArea(Integer.toString(currentFloor));
	}
	
	// setFloor method updates the colour of the DirectionButton
	// of a specific elevator ( passed as a parameter) to cyan
	public void setFloor(int elevator, int direction) {
		if(elevator == 3) {
			DirectionButtons[6].setBackground(Color.cyan);
			DirectionButtons[7].setBackground(Color.cyan);
		}
		
	}

	// UpdateElevatorGUIDirection sets the direction button of the elevators, 
	// by taking the status of each elevator from the scheduler
	public void UpdateElevatorGUIDirection(int elevator, int direction) {
		// Polls through each elevator to update the direction buttons
		switch(elevator) {
			case 0:
				break;
			case 1:
				elevator = 2;
				break;
			case 2:
				elevator = 4;
				break;
			case 3:
				elevator = 6;
				break;
		}
		// Direction of elevator can be: UP, DOWN, STOP or HOLD
		switch (direction) {
			case STOP:
				// Only udates the GUI if the status of the elevator has changed from STOP
				if(DirectionButtons[elevator].getBackground() != Color.red) {
					DirectionButtons[elevator].setBackground(Color.red);
					DirectionButtons[elevator + 1].setBackground(Color.red);
				}
				break;
			case UP:
				// Only udates the GUI if the status of the elevator has changed from UP
				if(DirectionButtons[elevator].getBackground() != Color.green) {
					DirectionButtons[elevator].setBackground(null);
					DirectionButtons[elevator + 1].setBackground(Color.green);
				}
				break;
			case DOWN:
				// Only udates the GUI if the status of the elevator has changed from DOWN
				if(DirectionButtons[elevator].getBackground() != Color.green) {
					DirectionButtons[elevator + 1].setBackground(null);
					DirectionButtons[elevator].setBackground(Color.green);
				}
				break;
			case HOLD:
				// Only udates the GUI if the status of the elevator has changed from HOLD
				if(DirectionButtons[elevator].getBackground() != Color.yellow) {
					DirectionButtons[elevator].setBackground(Color.yellow);
					DirectionButtons[elevator + 1].setBackground(Color.yellow);
				}
				break;
			}
	}

//	public void run() {
//		while (true) {
//			for (int i = 0; i < numElevators; i++) {
//				switch (i) {
//				case 0:
//					// System.out.println(" Elevator 1 " + elevatorCurrentFloor[i]);
//					UpdateElevatorGUIDirection(i);
//					UpdateGUICurrentFloor(textArea1, elevatorCurrentFloor[i]);
//					// UpdateGUIErrorMsg("ELevator 1");
//					break;
//				case 1:
//					UpdateElevatorGUIDirection(i);
//					// System.out.println(" Elevator 2 " + elevatorCurrentFloor[i]);
//					UpdateGUICurrentFloor(textArea2, elevatorCurrentFloor[i]);
//					// UpdateGUIErrorMsg("ELevator 2");
//					break;
//				case 2:
//					UpdateElevatorGUIDirection(i);
//					// System.out.println(" Elevator 3 " + elevatorCurrentFloor[i]);
//					UpdateGUICurrentFloor(textArea3, elevatorCurrentFloor[i]);
//					// UpdateGUIErrorMsg("ELevator 3");
//					break;
//				case 3:
//					UpdateElevatorGUIDirection(i);
//					// System.out.println(" Elevator 4 " + elevatorCurrentFloor[i]);
//					UpdateGUICurrentFloor(textArea4, elevatorCurrentFloor[i]);
//					// UpdateGUIErrorMsg("ELevator 4");
//					break;
//				}
//			}
//			try {
//				Thread.sleep(1);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	/*
	 * public static void main(String args[]) { SimpleGUI gui = new SimpleGUI(); }
	 */
}

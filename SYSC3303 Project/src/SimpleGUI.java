import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;



public class SimpleGUI  extends Scheduler implements Runnable{
	int numElevators = 4;	
	int numFloors = 22;
	public JButton DirectionButtons[] = new JButton[8] ;
	
	String elevator1msg = "Elevator 1"; 
	String elevator1 = "Elevator 1"; 
	
	private static final byte UP = 0x02;// elevator is going up
	private static final byte DOWN = 0x01;// elevator is going down
	private static final byte STOP = 0x03;
	private static final byte HOLD = 0x04;// elevator is in hold state
	public JTextArea textArea1, textArea2, textArea3, textArea4, ErrorMsgTextArea;
	
	SimpleGUI(){
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
		
		
		/*// Testing Values; 
		int currentFloor1 = 10, currentFloor2 = 0, currentFloor3 = 4, currentFloor4 = 6;
		int direction1 = 1;*/
		
		DirectionButtons = new JButton[8] ;
		
		//
		ElevatorStatus.setLayout(new GridLayout(2,2));
		ElevatorStatus.setBorder(new TitledBorder("Elevator Status"));
		
		ErrorMsg.setLayout(new FlowLayout());
		ErrorMsg.setBorder(new TitledBorder("Error Message"));
		
		
		//Elevator 1
		Elevator1.setLayout(new FlowLayout());
		Elevator1.setBorder(new TitledBorder("Elevator 1"));
		
		textArea1 = new JTextArea("0"/*Integer.toString(elevatorCurrentFloor[0])*/);
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
		Elevator1.add(textArea1,BorderLayout.WEST);
		Elevator1.add(ButtonPannel1,BorderLayout.EAST);
		//Elevator 1 end
		
		//Elevator 2
		Elevator2.setLayout(new FlowLayout());
		Elevator2.setBorder(new TitledBorder("Elevator 2"));
		
		textArea2 = new JTextArea("0"/*Integer.toString(elevatorCurrentFloor[1])*/);
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
		Elevator2.add(textArea2,BorderLayout.WEST);
		Elevator2.add(ButtonPannel2,BorderLayout.EAST);
		//Elevator 2 end
		
		//Elevator 3
		Elevator3.setLayout(new FlowLayout());
		Elevator3.setBorder(new TitledBorder("Elevator 3"));
		
		textArea3 = new JTextArea("0"/*Integer.toString(elevatorCurrentFloor[2])*/);
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
		Elevator3.add(textArea3,BorderLayout.WEST);
		Elevator3.add(ButtonPannel3,BorderLayout.EAST);
		//Elevator 3 end
				
		//Elevator 4
		Elevator4.setLayout(new FlowLayout());
		Elevator4.setBorder(new TitledBorder("Elevator 3"));
		
		textArea4 = new JTextArea("0"/*Integer.toString(elevatorCurrentFloor[3])*/);
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
		Elevator4.add(textArea4,BorderLayout.WEST);
		Elevator4.add(ButtonPannel4,BorderLayout.EAST);
		//Elevator 4 end
				
		//ErrorMsg
		JTextArea ErrorMsgTextArea = new JTextArea("Elevator 2 Working");
		ErrorMsgTextArea.setLineWrap(true);
		ErrorMsgTextArea.setWrapStyleWord(true);
		ErrorMsgTextArea.setEditable(false);

		JScrollPane ErrorMsgScrollPlane = new JScrollPane(ErrorMsgTextArea);
		ErrorMsgScrollPlane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		ErrorMsgScrollPlane.setPreferredSize(new Dimension(390, 100));
		ErrorMsg.add(ErrorMsgScrollPlane);
		//ErrorMsg End
		
		//Add Panel to frame
		ElevatorStatus.add(Elevator1);
		ElevatorStatus.add(Elevator2);
		ElevatorStatus.add(Elevator3);
		ElevatorStatus.add(Elevator4);
		
		//full.add(Elevator2);
		FullPanel.add(ElevatorStatus,BorderLayout.NORTH);
		FullPanel.add(ErrorMsg,BorderLayout.SOUTH);
	
		frame.add(FullPanel);
		
		//Frame dimensions
		frame.setSize(425,380);  
		frame.setVisible(true); 
	}
	
	public void UpdateGUICurrentFloor(JTextArea textArea, int currentFloor) {
		textArea.setText(Integer.toString(currentFloor));
	}
	
	public void UpdateGUIErrorMsg(String errorMsg) {
		ErrorMsgTextArea.append(Integer.toString(currentFloor));
		//textArea = new JTextArea(Integer.toString(currentFloor));
	}
	
	public void UpdateElevatorGUIDirection(int elevator) {
		switch(elevatorStatus[elevator]) {
			case STOP:
				DirectionButtons[elevator].setBackground(Color.red);
				DirectionButtons[elevator+1].setBackground(Color.red);
				break;
			case UP:
				DirectionButtons[elevator].setBackground(null);
				DirectionButtons[elevator+1].setBackground(Color.green);
				break;
			case DOWN:
				DirectionButtons[elevator+1].setBackground(null);
				DirectionButtons[elevator].setBackground(Color.green);
				break;
			case HOLD:
				DirectionButtons[elevator].setBackground(Color.yellow);
				DirectionButtons[elevator+1].setBackground(Color.yellow);
				break;
		}
	}
	
	
	public void run() {
		while(true) {
			for(int i = 0; i < numElevators; i++) {
				switch(i) {
					case 0:
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("Elevator 1" + elevatorCurrentFloor[i]);
						UpdateElevatorGUIDirection(i);
						UpdateGUICurrentFloor(textArea1, elevatorCurrentFloor[i]);
						//UpdateGUIErrorMsg("ELevator 1");
					case 1:
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						UpdateElevatorGUIDirection(i);	
						System.out.println("Elevator 2" + elevatorCurrentFloor[i]);
						UpdateGUICurrentFloor(textArea2, elevatorCurrentFloor[i]);
						//UpdateGUIErrorMsg("ELevator 2");
					case 2:
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						UpdateElevatorGUIDirection(i);
						UpdateGUICurrentFloor(textArea3, elevatorCurrentFloor[i]);
						//UpdateGUIErrorMsg("ELevator 3");
					case 3:
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						UpdateElevatorGUIDirection(i);
						UpdateGUICurrentFloor(textArea4, elevatorCurrentFloor[i]);
						//UpdateGUIErrorMsg("ELevator 4");
					}
				}
			}
		}
	
		
	public static void main(String args[]) {
		SimpleGUI gui = new SimpleGUI();
	}
}


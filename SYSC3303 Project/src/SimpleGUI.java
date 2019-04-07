import java.awt.BorderLayout;
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

public class SimpleGUI {
	int numElevators = 4;	
	int numFloors = 22;
	public JButton DirectionButtons[];
	
	
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
		JTextArea textArea1, textArea2, textArea3, textArea4;
		
		// Testing Values
		String elevator1 = "Elevator 1"; 
		int currentFloor1 = 10, currentFloor2 = 0, currentFloor3 = 4, currentFloor4 = 6;
		int direction1 = 1;
		String elevator1msg = "Elevator 1"; 
		DirectionButtons = new JButton[8] ;
		
		//
		ElevatorStatus.setLayout(new GridLayout(2,2));
		ElevatorStatus.setBorder(new TitledBorder("Elevator Status"));
		
		ErrorMsg.setLayout(new FlowLayout());
		ErrorMsg.setBorder(new TitledBorder("Error Message"));
		
		
		//Elevator 1
		Elevator1.setLayout(new FlowLayout());
		Elevator1.setBorder(new TitledBorder("Elevator 1"));
		
		textArea1 = new JTextArea( Integer.toString(currentFloor1));
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
		
		textArea2 = new JTextArea( Integer.toString(currentFloor2));
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
		
		textArea3 = new JTextArea( Integer.toString(currentFloor3));
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
		
		textArea4 = new JTextArea( Integer.toString(currentFloor4));
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
	
	JPanel ElevatorPanelSetup(JPanel panel, String elevator, int currentFloor, int Direction, JTextArea textArea, String errorMsg, JButton button[]) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(new TitledBorder(elevator));
		textArea = new JTextArea( Integer.toString(currentFloor));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		
		return panel;
	}

		
		
	public static void main(String args[]) throws IOException {
		SimpleGUI gui = new SimpleGUI();
	}
}


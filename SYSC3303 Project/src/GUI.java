import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import javafx.scene.text.Font;

public class GUI extends ElevatorIntermediate {

	// Value of numOfElevators must be taken from elevator intermediate as it initializes the number of elevators
	int numElevators = createNumElevators;	
	
GUI() {
		//	INITIALIZATIONS
		//Frame initialization
		JFrame frame = new JFrame("Elevator GUI");
		//Button(s) & TesxtArea initialization
		JButton button[] = new JButton[numElevators] ;
		JTextArea textArea[] = new JTextArea[numElevators];
		Panel pnl1 = new Panel(new GridLayout());
		Panel pnl2 = new Panel(new FlowLayout());
		
		//Layout Initialization
		FlowLayout flowLayout = new FlowLayout();
		frame.setLayout(flowLayout);
		
		
		// Dynamically creates the number of buttons/ textAreas depending on numOfElevators 
		// initialized in elevator intermediate class
		
		//Buttons
		for(int i = 0; i< numElevators; i++) {
			button[i] = new JButton("Elevator " + i);	
			pnl1.add(button[i]);
		}
		
		//CurrentFloor
		for(int i = 0; i< numElevators; i++) {
			textArea[i] = new JTextArea("Current FLoor " /*+ elevatorCurrentFloor[i]*/);	
			pnl2.add(textArea[i]);
		}
		
		/*String[] labels = {"Name: ", "Fax: ", "Email: ", "Address: "};
		int numPairs = labels.length;

		//Create and populate the panel.
		JPanel p = new JPanel(new SpringLayout());
		for (int i = 0; i < numPairs; i++) {
		    JLabel l = new JLabel(labels[i], JLabel.TRAILING);
		    p.add(l);
		    JTextField textField = new JTextField(10);
		    l.setLabelFor(textField);
		    p.add(textField);
		}

		//Lay out the panel.
		SwingUtilities.makeCompactGrid(p,
		                                numPairs, 2, //rows, cols
		                                6, 6,        //initX, initY
		                                6, 6);       //xPad, yPad
	
		frame.add(p);
*/			
		JTextArea TextErrorArea = new JTextArea(
			    "Elevator Error Message: \n" +
			    "A text area is a \"plain\" text component, " +
			    "which means that although it can display text " +
			    "in any font, all of the text is in the same font."
			);
		TextErrorArea.setLineWrap(true);
		TextErrorArea.setWrapStyleWord(true);
		TextErrorArea.setEditable(false);
			
		JScrollPane areaScrollPane = new JScrollPane(TextErrorArea);
		areaScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(250, 100));
		
		frame.add(pnl1);
		frame.add(pnl2);
		frame.add(areaScrollPane);
		
		//Set Layout to frame
		frame.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		
		//Frame dimensions
		frame.setSize(450,200); 
		 
		frame.setVisible(true);  
	}
}
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import javafx.scene.text.Font;

public class GUI {

	// Value of numOfElevators must be taken from elevator intermediate as it initializes the number of elevators
	int numOfElevators = 4;
		
	GUI(){
		//	INITIALIZATIONS
		//Frame initialization
		JFrame frame = new JFrame("Elevator GUI");
		//Button(s) initialization
		JButton button;
		//Layout Initialization
		FlowLayout experimentLayout = new FlowLayout();
		frame.setLayout(experimentLayout);	// Set frame layout to FlowLayout
		
		// Dynamically creates the number of buttons depending on numOfElevators initialized in elevator intermediate class
		for(int i = 0; i< numOfElevators; i++) {
			button = new JButton("Elevator " + i);	
			frame.add(button);
		}
		
	
		JTextArea textArea = new JTextArea(
			    "This is an editable JTextArea. " +
			    "A text area is a \"plain\" text component, " +
			    "which means that although it can display text " +
			    "in any font, all of the text is in the same font."
			);
			//textArea.setFont(new Font("Serif", Font.ITALIC, 16));
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);
			
		JScrollPane areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setVerticalScrollBarPolicy(
		                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(250, 100));
		
		frame.add(areaScrollPane);
		
		//Set Layout to frame
		frame.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		
		//Frame dimensions
		frame.setSize(500,300); 
		
		frame.setVisible(true);  
	}
	
	public  static void main(String[] args){
		new GUI();
	}
}
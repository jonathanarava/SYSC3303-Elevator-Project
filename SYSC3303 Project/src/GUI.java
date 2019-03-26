
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
public class GUI {
	
int numOfElevators = 5000;
	
GUI(){
	JFrame frame = new JFrame("Elevator GUI");
	JButton button,button1, button2, button3,button4;
	FlowLayout experimentLayout = new FlowLayout();
	frame.setLayout(experimentLayout);
	// 
	for(int i = 0; i< numOfElevators; i++) {
		button = new JButton("Elevator " + i);	
		frame.add(button);
	}
	button1 = new JButton("right");
	button2 = new JButton("top");
	button3 = new JButton("bottom");
	button4 = new JButton("center");
	//frame.add(button,BorderLayout.WEST);
	frame.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	//frame.add(button1, BorderLayout.EAST);
	//frame.add(button2, BorderLayout.NORTH);
	//frame.add(button3, BorderLayout.SOUTH);
	//frame.add(button4, BorderLayout.CENTER);
	
	frame.setSize(500,300);  
	frame.setVisible(true);  
	}
	public  static void main(String[] args){
		new GUI();
		}
}
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import javafx.scene.text.Font;

public class GUI extends Scheduler implements Runnable {

	private static final byte UP = 0x01;// elevator is going up
	private static final byte DOWN = 0x02;// elevator is going down
	private static final byte STOP = 0x03;
	private static final byte HOLD = 0x04;// elevator is in hold state
	private static final byte UPDATE_DISPLAY = 0x05;
	private static final byte SHUT_DOWN = 0x06;// for shutting down a hard fault problem elevator

	private static final int ELEVATOR_ID = 21;// for identifying the packet's source as elevator
	private static final int FLOOR_ID = 69;// for identifying the packet's source as floor
	private static final int SCHEDULER_ID = 54;// for identifying the packet's source as scheduler
	private static final int DOOR_OPEN = 1;// the door is open when ==1
	private static final int DOOR_DURATION = 4;// duration that doors stay open for
	private static final int REQUEST = 1;// for identifying the packet sent to scheduler as a request
	private static final int UPDATE = 2;// for identifying the packet sent to scheduler as a status update
	private static final int MAKE_STOP = 3;//
	private static final int PLACE_ON_HOLD = 4;
	private static final int UPDATE_DISPLAYS = 5;
	private static final int INITIALIZE = 8;// for first communication with the scheduler
	private static final int UNUSED = 0;// value for unused parts of data

	// Value of numOfElevators must be taken from elevator intermediate as it
	// initializes the number of elevators
	int numElevators = 4;
	int numFloors = 22;
	public JButton button1[];
	public JButton button2[];
	public JButton button3[];
	public JButton button4[];

	public int elev1;
	public int elev2;
	public int elev3;
	public int elev4;

	GUI() {
		// INITIALIZATIONS
		// Frame initialization
		JFrame frame = new JFrame("Elevator GUI");
		// Button(s) & TesxtArea initialization
		button1 = new JButton[numFloors];
		button2 = new JButton[numFloors];
		button3 = new JButton[numFloors];
		button4 = new JButton[numFloors];

		// JTextArea textArea[] = new JTextArea[numElevators];

		JPanel Full = new JPanel();
		JPanel North = new JPanel();
		JPanel South = new JPanel();

		JPanel Elevator1 = new JPanel();
		Elevator1.setLayout(new BoxLayout(Elevator1, BoxLayout.Y_AXIS));
		Elevator1.setBorder(new TitledBorder("Elevator 1"));

		JPanel Elevator2 = new JPanel();
		Elevator2.setLayout(new BoxLayout(Elevator2, BoxLayout.Y_AXIS));
		Elevator2.setBorder(new TitledBorder("Elevator 2"));

		JPanel Elevator3 = new JPanel();
		Elevator3.setLayout(new BoxLayout(Elevator3, BoxLayout.Y_AXIS));
		Elevator3.setBorder(new TitledBorder("Elevator 3"));

		JPanel Elevator4 = new JPanel();
		Elevator4.setLayout(new BoxLayout(Elevator4, BoxLayout.Y_AXIS));
		Elevator4.setBorder(new TitledBorder("Elevator 4"));

		// Buttons
		for (int i = numFloors - 1; i >= 0; i--) {
			button1[i] = new JButton("   " + i + "   ");
			button1[i].setEnabled(false);
			Elevator1.add(button1[i]);

			button2[i] = new JButton("   " + i + "   ");
			button2[i].setEnabled(false);
			Elevator2.add(button2[i]);

			button3[i] = new JButton("   " + i + "   ");
			button3[i].setEnabled(false);
			Elevator3.add(button3[i]);

			button4[i] = new JButton("   " + i + "   ");
			button4[i].setEnabled(false);
			Elevator4.add(button4[i]);
		}

		North.add(Elevator1);
		North.add(Elevator2);
		North.add(Elevator3);
		North.add(Elevator4);
		////
		JTextArea TextErrorArea1 = new JTextArea("Elevator Error");
		TextErrorArea1.setLineWrap(true);
		TextErrorArea1.setWrapStyleWord(true);
		TextErrorArea1.setEditable(false);

		JScrollPane Elevator1Dialog = new JScrollPane(TextErrorArea1);
		Elevator1Dialog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		Elevator1Dialog.setPreferredSize(new Dimension(100, 50));
		/////
		JTextArea TextErrorArea2 = new JTextArea("Elevator 2 Working");
		TextErrorArea2.setLineWrap(true);
		TextErrorArea2.setWrapStyleWord(true);
		TextErrorArea2.setEditable(false);

		JScrollPane Elevator2Dialog = new JScrollPane(TextErrorArea2);
		Elevator2Dialog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		Elevator2Dialog.setPreferredSize(new Dimension(100, 50));
		///
		JTextArea TextErrorArea3 = new JTextArea("Elevator 3 Working");
		TextErrorArea3.setLineWrap(true);
		TextErrorArea3.setWrapStyleWord(true);
		TextErrorArea3.setEditable(false);

		JScrollPane Elevator3Dialog = new JScrollPane(TextErrorArea3);
		Elevator3Dialog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		Elevator3Dialog.setPreferredSize(new Dimension(100, 50));
		///
		JTextArea TextErrorArea4 = new JTextArea("Elevator 4 Working");
		TextErrorArea4.setLineWrap(true);
		TextErrorArea4.setWrapStyleWord(true);
		TextErrorArea4.setEditable(false);

		JScrollPane Elevator4Dialog = new JScrollPane(TextErrorArea4);
		Elevator4Dialog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		Elevator4Dialog.setPreferredSize(new Dimension(100, 50));

		///
		South.add(Elevator1Dialog);
		South.add(Elevator2Dialog);
		South.add(Elevator3Dialog);
		South.add(Elevator4Dialog);

		Full.add(North, BorderLayout.NORTH);
		Full.add(South, BorderLayout.SOUTH);

		// Layout Initialization
		BorderLayout borderLayout = new BorderLayout();
		frame.setLayout(borderLayout);

		frame.add(Full);

		// Frame dimensions
		frame.setSize(450, 800);
		frame.setVisible(true);
	}

	public void run() {
		while (true) {
			for (int i = 0; i < numElevators; i++) {
				switch (i) {
				case 0:
					/*
					 * for(int j = 0; j < numFloors; j++) { button1[j].setBackground(null); }
					 */
					switch (elevatorStatus[i]) {
					case STOP:
						button1[elevatorCurrentFloor[i]].setBackground(Color.red);
						break;
					case UP:
						button1[elevatorCurrentFloor[i]].setBackground(Color.green);
						break;
					case DOWN:
						button1[elevatorCurrentFloor[i]].setBackground(Color.green);
						break;
					case HOLD:
						button1[elevatorCurrentFloor[i]].setBackground(Color.yellow);
					}
				case 1:

					/*
					 * button2[i+1].setBackground(null); if(i <= 1) {
					 * button2[i-1].setBackground(null); }
					 */
					switch (elevatorStatus[i]) {
					case STOP:
						button2[elevatorCurrentFloor[i]].setBackground(Color.red);
						break;
					case UP:
						button2[elevatorCurrentFloor[i]].setBackground(Color.green);
						break;
					case DOWN:
						button2[elevatorCurrentFloor[i]].setBackground(Color.green);
						break;
					case HOLD:
						button2[elevatorCurrentFloor[i]].setBackground(Color.yellow);
					}

				case 2:
					/*
					 * for(int j = 0; j < numFloors; j++) { button3[j].setBackground(null); }
					 */
					switch (elevatorStatus[i]) {
					case STOP:
						button3[elevatorCurrentFloor[i]].setBackground(Color.red);
						break;
					case UP:
						button3[elevatorCurrentFloor[i]].setBackground(Color.green);
						break;
					case DOWN:
						button3[elevatorCurrentFloor[i]].setBackground(Color.green);
						break;
					case HOLD:
						button3[elevatorCurrentFloor[i]].setBackground(Color.yellow);
					}

				case 3:
					/*
					 * for(int j = 0; j < numFloors; j++) { button4[j].setBackground(null); }
					 */
					switch (elevatorStatus[i]) {
					case STOP:
						button4[elevatorCurrentFloor[i]].setBackground(Color.red);
						break;
					case UP:
						button4[elevatorCurrentFloor[i]].setBackground(Color.green);
						break;
					case DOWN:
						button4[elevatorCurrentFloor[i]].setBackground(Color.green);
						break;
					case HOLD:
						button4[elevatorCurrentFloor[i]].setBackground(Color.yellow);
					}
				}
			}
		}
	}
}
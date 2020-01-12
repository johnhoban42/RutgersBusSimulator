package apps;

import java.awt.EventQueue;
import javax.swing.JFrame;
import java.util.*;
import java.util.regex.Pattern;

import buses.*;
import java.io.*;
import java.awt.Color;
import java.awt.SystemColor;
import java.awt.Toolkit;

import javax.swing.JTextPane;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Image;
import java.awt.Component;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.Canvas;
import javax.swing.JScrollBar;
import java.awt.Panel;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.Graphics;
import javax.swing.Timer;

import java.lang.Math;
import javax.swing.JLabel;

public class RutgersBusSimulator {
	
	int time = 25200; // 7:00:00 AM
	
	// FORM-RELATED VARIABLES
	// Declared here so that they're visible within the timer loop
	int arrivalPage = 1; // "Page" of arrivals being shown in the arrivals pane
	int totalArrivals = 0; // Total number of upcoming arrivals for a given stop
	int departurePage = 1; // "Page" of arrivals being shown in the departures pane
	int totalDepartures = 0; // Total number of upcoming departures for a given stop

	private JFrame frmRutgersBusSimulator;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RutgersBusSimulator window = new RutgersBusSimulator();
					window.frmRutgersBusSimulator.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public RutgersBusSimulator() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		// INITIALIZE LOGICAL COMPONENTS
		
		// Create an "observer" for all the stops
		Hashtable<String,ArrayList<Bus>> stopsObserver = new Hashtable<String,ArrayList<Bus>>(35, 1);
		File f = new File("allstops.txt");
		Scanner sc;
		try {	
			sc = new Scanner(f);
		}catch(IOException e) {
			System.out.println("Bad input, halting execution.");
			return;
		}
		while(sc.hasNextLine()) {
			String stopName = sc.nextLine();
			stopsObserver.put(stopName, new ArrayList<Bus>());
		}
		sc.close();
		
		// Load all buses into hash tables for later use
		// Buses are retrievable by their 4-digit ID
		Hashtable<String,Bus> buses = new Hashtable<String,Bus>();
		Hashtable<String,Bus> aBuses = new Hashtable<String,Bus>();
		Hashtable<String,Bus> bBuses = new Hashtable<String,Bus>();
		Hashtable<String,Bus> cBuses = new Hashtable<String,Bus>();
		Hashtable<String,Bus> eeBuses = new Hashtable<String,Bus>();
		Hashtable<String,Bus> fBuses = new Hashtable<String,Bus>();
		Hashtable<String,Bus> hBuses = new Hashtable<String,Bus>();
		Hashtable<String,Bus> lxBuses = new Hashtable<String,Bus>();
		Hashtable<String,Bus> rexbBuses = new Hashtable<String,Bus>();
		Hashtable<String,Bus> rexlBuses = new Hashtable<String,Bus>();
		
		Random random = new Random();
		String ID; Bus b;
		for(int i = 0; i < 6; i++) {
			ID = String.format("%04d", random.nextInt(9999));
			b = new Bus(ID, "A", i);
			buses.put(ID, b);
			aBuses.put(ID, b);
		}
		for(int i = 0; i < 8; i++) {
			ID = String.format("%04d", random.nextInt(9999));
			b = new Bus(ID, "B", i);
			buses.put(ID, b);
			bBuses.put(ID, b);
		}
		// There is only ever one C bus
		ID = String.format("%04d", random.nextInt(9999));
		b = new Bus(ID, "C", 0);
		buses.put(ID, b);
		cBuses.put(ID, b);
		for(int i = 0; i < 5; i++) {
			ID = String.format("%04d", random.nextInt(9999));
			b = new Bus(ID, "EE", 3*i);
			buses.put(ID, b);
			eeBuses.put(ID, b);
		}
		for(int i = 0; i < 5; i++) {
			ID = String.format("%04d", random.nextInt(9999));
			b = new Bus(ID, "F", i);
			buses.put(ID, b);
			fBuses.put(ID, b);
		}
		for(int i = 0; i < 5; i++) {
			ID = String.format("%04d", random.nextInt(9999));
			b = new Bus(ID, "H", i);
			buses.put(ID, b);
			hBuses.put(ID, b);
		}
		for(int i = 0; i < 7; i++) {
			ID = String.format("%04d", random.nextInt(9999));
			b = new Bus(ID, "LX", i);
			buses.put(ID, b);
			lxBuses.put(ID, b);
		}
		for(int i = 0; i < 3; i++) {
			ID = String.format("%04d", random.nextInt(9999));
			b = new Bus(ID, "REXB", 2*i);
			buses.put(ID, b);
			rexbBuses.put(ID, b);
		}
		for(int i = 0; i < 3; i++) {
			ID = String.format("%04d", random.nextInt(9999));
			b = new Bus(ID, "REXL", 2*i);
			buses.put(ID, b);
			rexlBuses.put(ID, b);
		}
		
		// Parse dot positions text file
		Hashtable<String, int[]> dotLocations = parseDotLocations();
		
		
		// Create rider object
		Rider R = new Rider("Busch Student Center");
		
		
		// INITIALIZE VISUAL COMPONENTS
		
		frmRutgersBusSimulator = new JFrame();
		frmRutgersBusSimulator.setTitle("Rutgers Bus Simulator");
		frmRutgersBusSimulator.getContentPane().setBackground(new Color(0, 0, 0));
		frmRutgersBusSimulator.getContentPane().setLayout(null);
		
		JTextPane textPane = new JTextPane();
		textPane.setBounds(0, 0, 6, 960);
		frmRutgersBusSimulator.getContentPane().add(textPane);
		
		JTextPane lblCurrent = new JTextPane();
		lblCurrent.setText("Current stop:");
		lblCurrent.setFont(new Font("Arial", Font.ITALIC, 24));
		lblCurrent.setForeground(Color.GREEN);
		lblCurrent.setBackground(new Color(0, 0, 0));
		lblCurrent.setBounds(38, 77, 172, 32);
		frmRutgersBusSimulator.getContentPane().add(lblCurrent);
		
		JSeparator separator = new JSeparator();
		separator.setBackground(Color.GREEN);
		separator.setBounds(0, 67, 1200, 2);
		frmRutgersBusSimulator.getContentPane().add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBackground(Color.GREEN);
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setBounds(716, 0, 6, 69);
		frmRutgersBusSimulator.getContentPane().add(separator_1);
		
		JTextPane lblTitle = new JTextPane();
		lblTitle.setFont(new Font("Arial", Font.BOLD, 30));
		lblTitle.setText("Welcome to the RUTGERS BUS SIMULATOR");
		lblTitle.setForeground(Color.GREEN);
		lblTitle.setBackground(Color.BLACK);
		lblTitle.setBounds(38, 16, 650, 44);
		frmRutgersBusSimulator.getContentPane().add(lblTitle);
		
		JTextPane lblTime = new JTextPane();
		lblTime.setText("Current time: 07:00:00 AM");
		lblTime.setForeground(Color.GREEN);
		lblTime.setFont(new Font("Arial", Font.BOLD, 30));
		lblTime.setBackground(Color.BLACK);
		lblTime.setBounds(773, 16, 386, 44);
		frmRutgersBusSimulator.getContentPane().add(lblTime);
		
		JTextPane lblStop = new JTextPane();
		lblStop.setText(R.getStop());
		lblStop.setForeground(Color.GREEN);
		lblStop.setFont(new Font("Arial", Font.BOLD, 30));
		lblStop.setBackground(Color.BLACK);
		lblStop.setBounds(38, 109, 500, 76);
		frmRutgersBusSimulator.getContentPane().add(lblStop);
		
		JTextPane lblArrivingIn = new JTextPane();
		lblArrivingIn.setText("Arriving in");
		lblArrivingIn.setForeground(Color.GREEN);
		lblArrivingIn.setFont(new Font("Arial", Font.ITALIC, 24));
		lblArrivingIn.setBackground(Color.BLACK);
		lblArrivingIn.setBounds(38, 180, 386, 43);
		frmRutgersBusSimulator.getContentPane().add(lblArrivingIn);
		
		JTextPane lblCurrentBus = new JTextPane();
		lblCurrentBus.setText("RIDING BUS 0000 (ROUTE REXL)");
		lblCurrentBus.setForeground(Color.GREEN);
		lblCurrentBus.setFont(new Font("Arial", Font.BOLD, 24));
		lblCurrentBus.setBackground(Color.BLACK);
		lblCurrentBus.setBounds(38, 213, 386, 43);
		frmRutgersBusSimulator.getContentPane().add(lblCurrentBus);
		
		JButton cmdDeboardCurrentBus = new JButton("DEBOARD");
		cmdDeboardCurrentBus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					R.deboard();
				}catch(DeboardingException de) {
					System.out.println("Cannot deboard, bus is in motion.");
				}
			}
		});
		cmdDeboardCurrentBus.setBounds(38, 261, 115, 29);
		frmRutgersBusSimulator.getContentPane().add(cmdDeboardCurrentBus);
		
		ImagePanel imgMap = new ImagePanel("map.jpg");
		imgMap.setFont(new Font("Tahoma", Font.PLAIN, 7));
		imgMap.setBounds(38, 295, 410, 450);
		frmRutgersBusSimulator.getContentPane().add(imgMap);
		imgMap.setLayout(null);
		
		JLabel lblDot = new JLabel("X");
		lblDot.setBounds(199, 5, 12, 25);
		lblDot.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblDot.setForeground(Color.WHITE);
		imgMap.add(lblDot);
		
		
		// **ARRIVALS**
		
		Panel panel = new Panel();
		panel.setBackground(new Color(0, 0, 0));
		panel.setForeground(new Color(0, 255, 0));
		panel.setBounds(600, 93, 584, 294);
		frmRutgersBusSimulator.getContentPane().add(panel);
		panel.setLayout(null);
		
		JTextPane txtpnUpcomingArrivals = new JTextPane();
		txtpnUpcomingArrivals.setBounds(0, 0, 205, 35);
		txtpnUpcomingArrivals.setText("Upcoming arrivals:");
		txtpnUpcomingArrivals.setForeground(Color.GREEN);
		txtpnUpcomingArrivals.setFont(new Font("Arial", Font.ITALIC, 24));
		txtpnUpcomingArrivals.setBackground(Color.BLACK);
		panel.add(txtpnUpcomingArrivals);
		
		JPanel[] pnlArrival = new JPanel[3];
		JTextPane[] lblArrivalBus = new JTextPane[3];
		JTextPane[] lblArrivalRoute = new JTextPane[3];
		JTextPane[] lblArrivalTime = new JTextPane[3];
		JButton[] cmdFlag = new JButton[3];
		
		// ARRIVAL PANES
		for(int i = 0; i < 3; i++) {
			pnlArrival[i] = new JPanel();
			pnlArrival[i].setBackground(new Color(0, 0, 0));
			pnlArrival[i].setBounds(0, 46+70*i, 584, 69);
			panel.add(pnlArrival[i]);
			pnlArrival[i].setLayout(null);
			
			lblArrivalBus[i] = new JTextPane();
			lblArrivalBus[i].setForeground(Color.GREEN);
			lblArrivalBus[i].setFont(new Font("Arial", Font.PLAIN, 20));
			lblArrivalBus[i].setBackground(Color.BLACK);
			lblArrivalBus[i].setBounds(26, 16, 50, 35);
			pnlArrival[i].add(lblArrivalBus[i]);
			
			lblArrivalRoute[i] = new JTextPane();
			lblArrivalRoute[i].setForeground(Color.GREEN);
			lblArrivalRoute[i].setFont(new Font("Arial", Font.PLAIN, 20));
			lblArrivalRoute[i].setBackground(Color.BLACK);
			lblArrivalRoute[i].setBounds(91, 16, 63, 35);
			pnlArrival[i].add(lblArrivalRoute[i]);
			
			lblArrivalTime[i] = new JTextPane();
			lblArrivalTime[i].setForeground(Color.GREEN);
			lblArrivalTime[i].setFont(new Font("Arial", Font.PLAIN, 20));
			lblArrivalTime[i].setBackground(Color.BLACK);
			lblArrivalTime[i].setBounds(169, 16, 224, 35);
			pnlArrival[i].add(lblArrivalTime[i]);
			
			/*cmdFlag[i] = new JButton("FLAG");
			cmdFlag[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					System.out.println(String.format("Boarded bus %s.", lblArrivalBus[0].getText()));
				}
			});
			cmdFlag[i].setBounds(419, 16, 118, 29);
			pnlArrival[i].add(cmdFlag[i]);*/
			
		}
		
		// OTHER ARRIVAL ELEMENTS
		JButton cmdArrivalLeft = new JButton("<");
		cmdArrivalLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				arrivalPage--;
			}
		});
		cmdArrivalLeft.setEnabled(false);
		cmdArrivalLeft.setBounds(186, 265, 55, 29);
		panel.add(cmdArrivalLeft);
		cmdArrivalLeft.setFont(new Font("Tahoma", Font.BOLD, 10));
		
		JButton cmdArrivalRight = new JButton(">");
		cmdArrivalRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				arrivalPage++;
			}
		});
		cmdArrivalRight.setEnabled(false);
		cmdArrivalRight.setFont(new Font("Tahoma", Font.BOLD, 10));
		cmdArrivalRight.setBounds(356, 265, 55, 29);
		panel.add(cmdArrivalRight);
		
		JTextPane lblArrivalPage = new JTextPane();
		lblArrivalPage.setText("1 of 1");
		lblArrivalPage.setForeground(Color.GREEN);
		lblArrivalPage.setFont(new Font("Arial", Font.PLAIN, 20));
		lblArrivalPage.setBackground(Color.BLACK);
		lblArrivalPage.setBounds(270, 265, 71, 35);
		panel.add(lblArrivalPage);
		
		// **DEPARTURES**
		
		Panel panel_1 = new Panel();
		panel_1.setLayout(null);
		panel_1.setForeground(Color.GREEN);
		panel_1.setBackground(Color.BLACK);
		panel_1.setBounds(600, 430, 584, 294);
		frmRutgersBusSimulator.getContentPane().add(panel_1);
		
		JTextPane txtpnUpcomingDepartures = new JTextPane();
		txtpnUpcomingDepartures.setText("Upcoming departures:");
		txtpnUpcomingDepartures.setForeground(Color.GREEN);
		txtpnUpcomingDepartures.setFont(new Font("Arial", Font.ITALIC, 24));
		txtpnUpcomingDepartures.setBackground(Color.BLACK);
		txtpnUpcomingDepartures.setBounds(0, 0, 240, 35);
		panel_1.add(txtpnUpcomingDepartures);
		
		JPanel[] pnlDeparture = new JPanel[3];
		JTextPane[] lblDepartureBus = new JTextPane[3];
		JTextPane[] lblDepartureRoute = new JTextPane[3];
		JTextPane[] lblDepartureTime = new JTextPane[3];
		JButton[] cmdBoard = new JButton[3];
		
		// DEPARTURE PANES
		for(int i = 0; i < 3; i++) {
			pnlDeparture[i] = new JPanel();
			pnlDeparture[i].setLayout(null);
			pnlDeparture[i].setBackground(Color.BLACK);
			pnlDeparture[i].setBounds(0, 46+70*i, 584, 69);
			panel_1.add(pnlDeparture[i]);
			
			lblDepartureBus[i] = new JTextPane();
			lblDepartureBus[i].setForeground(Color.GREEN);
			lblDepartureBus[i].setFont(new Font("Arial", Font.PLAIN, 20));
			lblDepartureBus[i].setBackground(Color.BLACK);
			lblDepartureBus[i].setBounds(26, 16, 55, 35);
			pnlDeparture[i].add(lblDepartureBus[i]);
			
			lblDepartureRoute[i] = new JTextPane();
			lblDepartureRoute[i].setForeground(Color.GREEN);
			lblDepartureRoute[i].setFont(new Font("Arial", Font.PLAIN, 20));
			lblDepartureRoute[i].setBackground(Color.BLACK);
			lblDepartureRoute[i].setBounds(91, 16, 63, 35);
			pnlDeparture[i].add(lblDepartureRoute[i]);
			
			lblDepartureTime[i] = new JTextPane();
			lblDepartureTime[i].setForeground(Color.GREEN);
			lblDepartureTime[i].setFont(new Font("Arial", Font.PLAIN, 20));
			lblDepartureTime[i].setBackground(Color.BLACK);
			lblDepartureTime[i].setBounds(169, 16, 224, 35);
			pnlDeparture[i].add(lblDepartureTime[i]);
			
			cmdBoard[i] = new JButton("BOARD");
			cmdBoard[i].setBounds(419, 16, 118, 29);
			pnlDeparture[i].add(cmdBoard[i]);
		}
		// When the user chooses a bus, retrieve it from the buses hashtable given its ID and board it
		cmdBoard[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Bus b = buses.get(lblDepartureBus[0].getText());
				R.board(b);
			}
		});
		cmdBoard[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Bus b = buses.get(lblDepartureBus[1].getText());
				R.board(b);
			}
		});
		cmdBoard[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Bus b = buses.get(lblDepartureBus[2].getText());
				R.board(b);
			}
		});
		
		// OTHER DEPARTURE ELEMENTS
		JButton cmdDepartureLeft = new JButton("<");
		cmdDepartureLeft.setEnabled(false);
		cmdDepartureLeft.setFont(new Font("Tahoma", Font.BOLD, 10));
		cmdDepartureLeft.setBounds(186, 265, 55, 29);
		panel_1.add(cmdDepartureLeft);
		cmdDepartureLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				departurePage--;
			}
		});
		
		JButton cmdDepartureRight = new JButton(">");
		cmdDepartureRight.setEnabled(false);
		cmdDepartureRight.setFont(new Font("Tahoma", Font.BOLD, 10));
		cmdDepartureRight.setBounds(356, 265, 55, 29);
		panel_1.add(cmdDepartureRight);
		cmdDepartureRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				departurePage++;
			}
		});
		
		JTextPane lblDeparturePage = new JTextPane();
		lblDeparturePage.setText("1 of 1");
		lblDeparturePage.setForeground(Color.GREEN);
		lblDeparturePage.setFont(new Font("Arial", Font.PLAIN, 20));
		lblDeparturePage.setBackground(Color.BLACK);
		lblDeparturePage.setBounds(270, 265, 71, 35);
		panel_1.add(lblDeparturePage);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBackground(Color.GREEN);
		separator_2.setOrientation(SwingConstants.VERTICAL);
		separator_2.setBounds(549, 97, 6, 648);
		frmRutgersBusSimulator.getContentPane().add(separator_2);
		
		frmRutgersBusSimulator.setBackground(SystemColor.control);
		frmRutgersBusSimulator.setResizable(false);
		frmRutgersBusSimulator.setBounds(100, 100, 1200, 800);
		frmRutgersBusSimulator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// MAIN PROGRAM LOOP: LOGIC & UPDATING COMPONENTS
		ActionListener updateFrame = new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
		    	
		    	// Advance all buses
		    	for(String k: buses.keySet()) {
		    		Bus b = buses.get(k);
		    		try {
		    			b.advance();
		    		// When a bus arrives at a stop, add it to the queue of buses waiting to leave that stop
		    		// TODO: Implement as a priority queue
		    		}catch(ArrivalException ae) {
		    			ArrayList<Bus> departures = stopsObserver.get(ae.location);
		    			departures.add(b);
		    			
		    			// If the rider is on the bus that has arrived, he is no longer moving and is now at that stop
		    			if(b == R.getBus()){
		    				R.isMoving = false;
		    				R.setStop(b.getPrevStop());
		    				lblCurrent.setText("Current stop:");
		    			}
		    			
		    		// Remove the bus from its departure queue. If the user has boarded the bus, the user
		    		// is now in transit
		    		}catch(DepartureException de) {
		    			stopsObserver.get(de.location).remove(b);
		    			if(b == R.getBus()){
		    				R.isMoving = true;
		    				lblCurrent.setText("Next stop:");
		    			}
		    		}
		    	}

		    	// Departures and arrivals display: only show if the rider is NOT moving
		    	if(!R.isMoving) {
		    		
		    		panel.setVisible(true);
		    		panel_1.setVisible(true);
		    		
		    		// Get the closest arrival for each route
		    		ArrayList<Bus> arrivals = new ArrayList<Bus>(5);
		    		arrivals = addToArrivals(getClosestBus(R.getStop(), aBuses), arrivals);
		    		arrivals = addToArrivals(getClosestBus(R.getStop(), bBuses), arrivals);
		    		arrivals = addToArrivals(getClosestBus(R.getStop(), cBuses), arrivals);
		    		arrivals = addToArrivals(getClosestBus(R.getStop(), eeBuses), arrivals);
		    		arrivals = addToArrivals(getClosestBus(R.getStop(), fBuses), arrivals);
		    		arrivals = addToArrivals(getClosestBus(R.getStop(), hBuses), arrivals);
		    		arrivals = addToArrivals(getClosestBus(R.getStop(), lxBuses), arrivals);
		    		arrivals = addToArrivals(getClosestBus(R.getStop(), rexbBuses), arrivals);
		    		arrivals = addToArrivals(getClosestBus(R.getStop(), rexlBuses), arrivals);
		    		
		    		totalArrivals = arrivals.size();
		    		
		    		// Adjust page range for arrivals in case current page is out of range
		    		int maxPage = (int)Math.ceil((double)arrivals.size() / 3);
		    		maxPage = (maxPage == 0) ? 1 : maxPage;
		    		
		    		if(3*arrivalPage > arrivals.size() && totalArrivals > 0) {
		    			arrivalPage = maxPage;
		    		}
		    		lblArrivalPage.setText(String.format("%d of %d", arrivalPage, maxPage));
		    		
		    		// Enable/disable arrival page buttons
		    		boolean leftEnabled = arrivalPage > 1 ? true : false;
		    		boolean rightEnabled = arrivalPage < maxPage ? true: false;
		    		cmdArrivalLeft.setEnabled(leftEnabled);
		    		cmdArrivalRight.setEnabled(rightEnabled);
		    		
		    		// Display 3 arrivals at a time, while making sure not to iterate outside the queue
		    		for(int i = 0; i < 3; i++) {
		    			int busIndex = totalArrivals - 3*(arrivalPage-1) - (i+1);
		    			if(busIndex >= 0) {
		    				Bus b = arrivals.get(busIndex);
		    				showArrival(R, b, pnlArrival[i], lblArrivalBus[i],
		    						lblArrivalRoute[i], lblArrivalTime[i]);
		    			}else {
		    				// Unused arrival panels are hidden
		    				while(i < 3) {
		    					pnlArrival[i].setVisible(false);
		    					i++;
		    				}
		    				break;
		    			}
		    		}
		    		
		    		// Get departures queue   		
		    		ArrayList<Bus> departures = stopsObserver.get(R.getStop());
		    		totalDepartures = departures.size();
		    		
		    		// Adjust page range for departures in case current page is out of range
		    		maxPage = (int)Math.ceil((double)departures.size() / 3);
		    		maxPage = (maxPage == 0) ? 1 : maxPage;
		    		
		    		if(3*departurePage > departures.size() && totalDepartures > 0) {
		    			departurePage = maxPage;
		    		}
		    		lblDeparturePage.setText(String.format("%d of %d", departurePage, maxPage));
		    		
		    		// Enable/disable departure page buttons
		    		leftEnabled = departurePage > 1 ? true : false;
		    		rightEnabled = departurePage < maxPage ? true: false;
		    		cmdDepartureLeft.setEnabled(leftEnabled);
		    		cmdDepartureRight.setEnabled(rightEnabled);
		    		
		    		// Display 3 departures at a time, while making sure not to iterate outside the queue
		    		for(int i = 0; i < 3; i++) {
		    			int busIndex = totalDepartures - 3*(departurePage-1) - (i+1);
		    			if(busIndex >= 0) {
		    				Bus b = departures.get(busIndex);
		    				showDeparture(R, b, pnlDeparture[i], lblDepartureBus[i],
		    						lblDepartureRoute[i], lblDepartureTime[i], cmdBoard[i]);
		    			}else {
		    				// Unused departure panels are hidden
		    				while(i < 3) {
		    					pnlDeparture[i].setVisible(false);
		    					i++;
		    				}
		    				break;
		    			}
		    		}
		    		
		    		// No arrival countdown
		    		lblArrivingIn.setVisible(false);
		    	
		    	}else {
		    		// If the rider is moving, show neither the arrival or departure panes
		    		panel.setVisible(false);
		    		panel_1.setVisible(false);
		    		
		    		// Show next stop, distance from next stop, etc.
		    		lblStop.setText(R.getBus().getNextStop());
		    		String arrivalTime = timerString(R.getBus().getTimeFromNextStop());
		    		lblArrivingIn.setText(String.format("Arriving in %s", arrivalTime));
		    		lblArrivingIn.setVisible(true);
		    	}
		    	
		    	// Current bus
		    	if(R.getBus() == null) {
		    		cmdDeboardCurrentBus.setEnabled(false);
		    		lblCurrentBus.setText("NO BUS SELECTED");
		    		lblCurrentBus.setForeground(Color.GRAY);
		    	}else {
		    		lblCurrentBus.setText(String.format("RIDING BUS %s (ROUTE %s)",
		    				R.getBus().ID, R.getBus().getRouteName()));
		    		lblCurrentBus.setForeground(Color.GREEN);
		    		cmdDeboardCurrentBus.setEnabled(R.isMoving ? false : true);
		    	}
		    	
		    	// Show the X on the map at the appropriate location
		    	showDot(R, lblDot, dotLocations);
		    	
		    	// Advance time display
		    	time++;
		    	int hours = time/3600;
		    	int minutes = time/60 - 60*hours;
		    	int seconds = time - (3600*hours + 60*minutes);
		    	String meridiem = (12 <= hours && hours < 24) ? "PM" : "AM";
		    	hours = hours % 24;
		    	lblTime.setText(String.format("Current time: %02d:%02d:%02d %s", hours, minutes, seconds, meridiem));
		    	
		    }
		};
		Timer t = new Timer(100, updateFrame);
		t.start();
	}
	
	// Parse the hashtable of dot locations on the map
	private Hashtable<String, int[]> parseDotLocations(){
		Hashtable<String, int[]> h = new Hashtable<String, int[]>();
		File f = new File("dotPositions.txt");
		Scanner sc;
		try {
			sc = new Scanner(f);
		}catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			return null;
		}
		
		sc.useDelimiter(Pattern.compile(",|\\r\\n"));
		while(sc.hasNextLine()) {
			String stop = sc.next();
			int x = sc.nextInt();
			int y = sc.nextInt();
			h.put(stop, new int[] {x, y});
		}
		sc.close();
		
		return h;
	}
	
	// Show the dot "X" on the map and change its position if necessary
	private void showDot(Rider r, JLabel lblDot, Hashtable<String, int[]> dotLocations) {
		String stop = r.isMoving ? r.getBus().getNextStop() : r.getStop();
		int[] coordinates = dotLocations.get(stop);
		lblDot.setBounds(coordinates[0], coordinates[1], 17, 17);
		
		// Rider is moving: the X will blink every half-second at the rider's bus's next stop
		// Otherwise, the X does not blink
		if(r.isMoving) {
			lblDot.setVisible(time % 6 < 3 ? true : false);
		}else {
			lblDot.setVisible(true);
		}
	}

	// General method for showing a departure
	private void showDeparture(Rider r, Bus b, JPanel pnlDeparture, JTextPane lblID, JTextPane lblRoute,
			JTextPane lblWaitTime, JButton cmdBoard) {
		lblID.setText(b.ID);
		lblRoute.setText(b.getRouteName());
		int wt = b.getWaitTime();
		String strwt = timerString(wt);
		lblWaitTime.setText(strwt);
		cmdBoard.setEnabled(r.getBus() == null ? true : false);
		pnlDeparture.setVisible(true);
	}
	
	// Returns the bus closest to arriving at the given stop for a given route
	// Returns null if the given stop is not on the route
	// Buses that are at the stop (time = 0) are not included
	private Bus getClosestBus(String stop, Hashtable<String,Bus> route) {
		Bus closest = null;
		for(Bus b : route.values()) {
			int time = b.getTimeFromStop(stop);
			if(time == -1) {
				return null;
			}
			if(time > 0 && (closest == null || time < closest.getTimeFromStop(stop))) {
				closest = b;
			}
		}
		return closest;
	}
	
	// Wrapper for the ArrayList add() method that doesn't allow null entries
	private ArrayList<Bus> addToArrivals(Bus b, ArrayList<Bus> arr) {
		if(b != null) {arr.add(b);}
		return arr;
	}
	
	// General method for showing an arrival
	private void showArrival(Rider r, Bus b, JPanel pnlArrival, JTextPane lblID, JTextPane lblRoute,
			JTextPane lblWaitTime) {
		lblID.setText(b.ID);
		lblRoute.setText(b.getRouteName());
		int wt = b.getTimeFromStop(r.getStop());
		String strwt = timerString(wt);
		lblWaitTime.setText(strwt);
		//cmdFlag.setEnabled(r.getBus() == null ? true : false);
		pnlArrival.setVisible(true);
	}
	
	// Converts an integer wait timer to a String equivalent with minutes and seconds
	private String timerString(int timeRemaining) {
		if(timeRemaining < 60) {
			return String.format("%d seconds", timeRemaining);
		}
		return String.format("%d minutes, %d seconds", timeRemaining/60, timeRemaining%60);
	}
}

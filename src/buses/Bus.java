package buses;

import java.util.*;
import java.util.regex.*;
import java.io.*;

public class Bus {
	
	public String ID;
	private String routeName;
	private ArrayList<Stop> route;
	public int[] stopDistances;
	private int nextStop;
	private int waitTime;
	private boolean moving;
	

	public Bus(String ID, String routeName, int start) {
		
		this.ID = ID;
		this.routeName = routeName;
		
		// Read the route from file
		String f_name = routeName + ".txt";
		File f = new File(f_name);
		Scanner sc;
		try {	
			sc = new Scanner(f);
		}catch(IOException e) {
			System.out.println("Bad input, stopping constructor.");
			return;
		}
		sc.useDelimiter(Pattern.compile(",|\\n"));
		
		this.route = new ArrayList<Stop>();
		while(sc.hasNextLine()) {
			Stop s = new Stop();
			s.name = sc.next();
			s.arrivalTime = sc.nextInt();
			s.stopTime = sc.nextInt();
			if(sc.next() == "b") {
				// s.breaks = true;
			}else {
				// s.breaks = false;
			}
			route.add(s);
		}
		sc.close();
		
		// Set the bus's starting position
		this.nextStop = start % this.route.size();
		this.waitTime = 0;
		this.moving = true;
		
		// Initialize distances from each stop
		this.stopDistances = new int[route.size()];
		int time = route.get(this.nextStop).arrivalTime;
		stopDistances[this.nextStop] = time;
		
		// Modular increment to iterate over the whole route from any starting index
		// For each stop, the distance increases by the previous stop's wait time plus the distance between
		// the previous and next stops
		for(int i = (start + 1) % route.size(); i != this.nextStop; i = (i + 1) % route.size()) {
			int prev = (i - 1 == -1) ? route.size()-1 : i - 1;
			time += route.get(prev).stopTime + route.get(i).arrivalTime;
			stopDistances[i] = time;
		}
		
	}
	
	// Gets the index of the previous stop on the bus route via modular decrement
	private int prevStop() {
		return (nextStop - 1 == -1) ? route.size()-1 : nextStop - 1;
	}
	
	// Advance the bus by 1 second
	public void advance() throws ArrivalException, DepartureException{
		// Decrement distance from every stop
		for(int i = 0; i < stopDistances.length; i++) {
			if(moving || i != nextStop-1) {stopDistances[i]--;}
		}
		
		// If the bus is moving, move it 1 second closer to the next stop
		if(moving) {
			
			if(stopDistances[nextStop] == 0) {
				
				moving = false;
				waitTime = route.get(nextStop).stopTime;
				
				// TODO: Account for a 75% chance of taking a break at appropriate stops
				/*Random r = new Random();
				if(route.get(nextStop).breaks) {
					if(r.nextInt(4) > 0) {
						int delay = 240 + r.nextInt(120);
						waitTime += delay;
						// A break increases the distance from each stop, so adjust these times accordingly
						// The distance to the bus's current stop remains 0
						for(int i = 0; i < stopDistances.length; i++) {
							if(i != nextStop-1) {stopDistances[i] += delay;}
						}
					}
				}
				*/
				nextStop = (nextStop + 1) % route.size();
				
				// Raise exception for arrival
				throw new ArrivalException(getPrevStop());
			}
		}
		
		// If it's waiting at a stop, reduce its wait time by 1 second
        // Send the bus to the next stop when its wait time is 0
		else {
			waitTime--;
			// If the bus is done waiting, throw a departure exception and update the bus's distance from the
			// stop it has just left. This is equal to the total route time minus the wait time at that stop
			if(waitTime == 0) {
				moving = true;
				stopDistances[prevStop()] = getTotalRouteTime() - route.get(prevStop()).stopTime;
				throw new DepartureException(getPrevStop());
			}
		}
		
	}
	
	// Methods for accessing private fields
	public boolean isMoving() {
		return moving;
	}
	
	public String getPrevStop() {
		return route.get(prevStop()).name;
	}
	
	public String getNextStop() {
		return route.get(nextStop).name;
	}
	
	public int getTimeFromNextStop() {
		return stopDistances[nextStop];
	}
	
	// Returns the bus's time from any given stop in its route via a linear search for that stop
	// Returns -1 if the stop isn't on the bus's route
	public int getTimeFromStop(String targetStop) {
		for(int i = 0; i < route.size(); i++) {
			if(targetStop.equals(route.get(i).name)) {
				return stopDistances[i];
			}
		}
		return -1;
	}
	
	// Returns -1 if the bus is moving
	public int getWaitTime() {
		if(moving) {
			return -1;
		}
		return waitTime;
	}
	
	public String getRouteName() {
		return routeName;
	}
	
	// Approximate, does not account for breaks
	public int getTotalRouteTime() {
		int t = 0;
		for(Stop s: route) {
			t += (s.arrivalTime + s.stopTime);
		}
		return t;
	}
	
	public String toString() {
		return "Bus " + ID;
	}
	
}

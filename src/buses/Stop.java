package buses;

/*
 * Consolidates all variables for a stop in one data type. Accessed within only the Bus class.
 */
public class Stop {

	public String name;
	public int arrivalTime;
	public int stopTime;
	public boolean breaks;
	
	public Stop(String name, int arrivalTime, int stopTime, boolean breaks) {
		this.name = name;
		this.arrivalTime = arrivalTime;
		this.stopTime = stopTime;
		this.breaks = breaks;
	}
	
	// Alternate constructor, used when waiting for input to fill fields
	public Stop() {}
	
}

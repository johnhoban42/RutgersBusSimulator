package buses;

public class Rider {

	private Bus bus;
	private String stop;
	public boolean isMoving;
	
	// Create a new Rider, specifying the starting location
	public Rider(String startingLocation) {
		this.stop = startingLocation;
		this.bus = null;
	}
	
	// Get on a bus
	public void board(Bus bus) {
		this.bus = bus;
		//System.out.println("Boarded bus " + bus.ID);
	}
	
	// Get off a bus. This should only be used if the bus is stationary
	public void deboard() throws DeboardingException{
		if(bus.isMoving()) {
			throw new DeboardingException();
		}
		stop = bus.getPrevStop();
		bus = null;
	}
	
	public Bus getBus() {
		return bus;
	}
	
	public String getStop() {
		return stop;
	}
	
	public void setStop(String stop) {
		this.stop = stop;
	}
	
}

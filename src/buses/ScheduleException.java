package buses;

public class ScheduleException extends Exception{

	public String location;
	
	public ScheduleException(String location) {
		this.location = location;
	}
	
}

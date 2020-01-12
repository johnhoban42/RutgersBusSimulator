package buses;

public class DeboardingException extends Exception{

	public String toString() {
		return "Rider attempted to deboard a moving bus.";
	}
	
}

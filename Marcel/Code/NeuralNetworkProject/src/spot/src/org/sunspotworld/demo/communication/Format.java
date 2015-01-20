package spot.src.org.sunspotworld.demo.communication;

public class Format {
	
	/**
	 * Convertes a double number into a String of the length 6.
	 * 
	 * @param number
	 * @return String s
	 */
	public static String format(double number){
		
		String s = "" + number;
		
		s = s.substring(0, 6);
		
		return s;
		
	}//Ende format()

}

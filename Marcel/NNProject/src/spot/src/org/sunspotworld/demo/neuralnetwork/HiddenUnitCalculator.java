package spot.src.org.sunspotworld.demo.neuralnetwork;


public class HiddenUnitCalculator {

	public static int getHiddenUnits(int sensor_count){
		
		int hidden_units = sensor_count - 1;
		
		hidden_units = (int)( (2*(sensor_count+1)/3) + (Math.sqrt(sensor_count)) ) / 2;
		
		if (hidden_units<2)
			hidden_units=2;
		
		return hidden_units;
		
	}
	
	
}

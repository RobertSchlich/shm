package spot;

// author Jahr&Schlich

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.util.Utils;
import java.lang.Math;
import com.sun.spot.resources.transducers.ITriColorLED;



public class AccelerationSampler 
{
	IAccelerometer3D accelerometer = 
			(IAccelerometer3D) Resources.lookup(IAccelerometer3D.class);
	
	
    ITriColorLED led = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED7");
    ITriColorLED led2 = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED6");
	
	public double[] getaccelerationArray (int samplePeriodIfListening, 
										  int samplePeriodIfMeasuring, 
										  int arrayLength, 
										  double threshold) 
	{
		
		double[] accelerationArray = new double[arrayLength];
		
        try {
        	
        	double accely = accelerometer.getAccelY();
        	
            // Flash an LED to indicate a sampling event
            led.setRGB(255, 255, 255);
            led.setOn();
        	
        	// measure acceleration with low samplerate, until acceleration 
            // exceeds threshold
        	while (Math.abs(accely) < threshold) {
        		Utils.sleep(samplePeriodIfListening);
        		accely = accelerometer.getAccelY();
			}
            led.setOff();
            
            
            // light an LED to indicate a sampling event
            led2.setRGB(0, 255, 255);
            led2.setOn();
        	
        	// measure acceleration with high samplerate, store values in array
			for (int i = 0; i < arrayLength; i++) {
				long start = System.currentTimeMillis();
				accelerationArray[i] = accelerometer.getAccelY();
				System.out.println("accY=  " + accelerationArray[i]);
				Utils.sleep(samplePeriodIfMeasuring - 
										(System.currentTimeMillis() - start));
				
			}
			
			// turn off LED
            led2.setOff();
			
        } catch (Exception e) {
            System.err.println("Caught " + e + " during sensor sampling.");
        }
       
        return accelerationArray;
	}
	
}

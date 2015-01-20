package spot.src.org.sunspotworld.demo.measurement;

import spot.src.org.sunspotworld.demo.communication.LEDSignals;

import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;

public class Calibration {
	
	private LEDSignals signals = new LEDSignals();
	
	public Calibration(){
		
	}
	
	/**
	 * Calibration of the accelerometer with the defined sample period for 10 seconds.
	 * Returns a double value for the difference to the measured values to the passive state calculated through the mean value of the calibration time.
	 * The sensor nodes should be in a passive state over the time of the calibration.
	 * 
	 * 
	 * @param sampleperiod integer value for the sample period used for the calibration
	 * @return calibration value c; used: acceleration = measured_acceleration - c;
	 */
	public double calibrate(int sampleperiod){
		
		//LED-Signal start Kalibrierung
		signals.on(0, LEDColor.RED);
		
		System.out.println("\tstart calibration with " + 1000/sampleperiod + " Hz ...");
		
		double sum = 0.0;
		double calib = 0.0;	//value of the calibration
		int length = 0;
		Acceleration acceleration = new Acceleration();
		long start = System.currentTimeMillis();	//start-time
        long end = System.currentTimeMillis();	//time after each measure
        
        //reading acceleration for a time
        int index = 0;
        while (end-start<10000){
        	end = System.currentTimeMillis();
        	acceleration.addAcceleration(index);
        	index++;
        	long sleep = sampleperiod - (System.currentTimeMillis() - end);
        	if(sleep<0)
        		continue;
        	Utils.sleep(sampleperiod - (System.currentTimeMillis() - end));
        }
		
        //writes the measured data from acceleration into the calilist
        double[] caliList = acceleration.getAccelerationArray();
        
        //calculate the length and the sum of the data
		for(int i=0; i<caliList.length; i++){
			double d=caliList[i];
			if(d!=0){
				sum+=d;
				length++;
			}
		}
		
		calib = sum/length;
		
		System.out.println("\tcalibration result: " + calib +" g");
		
		signals.offLastIndex();
		
		return calib;
		
	}//Ende calibrate

}

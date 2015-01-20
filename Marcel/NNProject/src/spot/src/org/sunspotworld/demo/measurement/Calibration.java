package spot.src.org.sunspotworld.demo.measurement;

import spot.src.org.sunspotworld.demo.communication.LEDSignals;

import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;

public class Calibration {
	
	private LEDSignals signals = new LEDSignals();
	
	public Calibration(){
		
	}
	
	public double calibrate( int sampleperiod){
		
		//LED-Signal start Kalibrierung
		signals.on(0, LEDColor.RED);
		
		System.out.println("***Start calibration with " + 1000/sampleperiod + " Hz***");
		
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
		
		System.out.println("Calibration result: " + calib +" g");
		
		signals.offLastIndex();
		
		return calib;
		
	}//Ende calibrate

}

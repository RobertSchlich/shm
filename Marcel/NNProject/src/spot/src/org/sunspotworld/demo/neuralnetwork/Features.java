package spot.src.org.sunspotworld.demo.neuralnetwork;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.sensorboard.peripheral.TriColorLEDArray;
import com.sun.spot.util.Utils;
import com.sun.squawk.util.MathUtils;

public class Features {
	
	
	public static void blinkall(TriColorLEDArray colorLEDArray, int sleep){								// all LED ON
   	 colorLEDArray.setOn();
        Utils.sleep(sleep);
        colorLEDArray.setOff();
   }

	
	 public static void showdiv(double div, double divLED, TriColorLEDArray colorLEDArray){						// show divergence with LEDArray
	    	int ledCount = (int) Math.abs(((div-div%divLED)/divLED));
			for(int i=0;i<8;i++)
			{
				if (i<= ledCount)
					colorLEDArray.getLED(i).setOn();
			}	
			Utils.sleep(2000);		
			colorLEDArray.setOff();
	    }
	 
	 public static double CalibrationMeasurements() throws Exception{										// calibrate to reduce failure
	    	double res=0;
	    	IAccelerometer3D iacc = (IAccelerometer3D)Resources.lookup(IAccelerometer3D.class);
	    	for (int i=0;i<500;i++){
	    		double ax=iacc.getAccelX(), az=iacc.getAccelZ();
	        	 res=res+Math.sqrt(MathUtils.pow(ax, 2)+MathUtils.pow(az, 2));
	    	}
	    	return((res/500));
	    }
	 
	 public static double mess() throws Exception{												// measurement - get resulting acceleration
	    	IAccelerometer3D acc = (IAccelerometer3D)Resources.lookup(IAccelerometer3D.class);
	    	
	    	double ax=acc.getAccelX(), az=acc.getAccelZ();	
	    	double res=MathUtils.pow(ax, 2)+MathUtils.pow(az, 2);
	    	res=Math.sqrt(res);

	    	double sign=1.0;
	    	
	    	if(Math.abs(ax)>Math.abs(az)){
	    		if(ax!=0)
	    		sign=ax/Math.abs(ax);
	    	}
	    	else if (az!=0) sign=az/Math.abs(az);
	    	
	    	res=res*sign;
	    	return res;
	    	
	    }
	 
	 public static double Calibration (TriColorLEDArray colorLEDArray, double divLED){
		 double divergence = 0;
		 for (int i=0;i<10;i++){
	        	blinkall(colorLEDArray, 1000);
	        	Utils.sleep(1000);
	        	}
	        
	        try {
	        	colorLEDArray.setColor(LEDColor.GREEN);
	        	colorLEDArray.setOn();
				divergence=CalibrationMeasurements();
				colorLEDArray.setOff();
				System.out.println("divergence:  "+divergence+" g");	//	shows the divergence on LED (one LED = 0.015g)		
				showdiv(divergence, divLED, colorLEDArray);	

					
			} catch (Exception e) {
				System.err.println("Caught " + e + " in Calibration.");
				//notifyDestroyed();
			}
	        colorLEDArray.setOff();
	        
			return divergence;
	        
	 }
	 
	 
}

package spot.src.org.sunspotworld.demo.measurement;

import java.io.IOException;
import java.util.Vector;

import spot.src.org.sunspotworld.demo.communication.Format;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.util.Utils;

public class Acceleration{
	
	private double[] accList = new double[32];
	private Vector time = new Vector();
	private Vector timeForMeasurement = new Vector();
	private double acc = 0.0;
	private double correction = 0.0;
	private int factor = 1;
	private IAccelerometer3D accelerometer3d = (IAccelerometer3D)Resources.lookup(IAccelerometer3D.class);

	/**
	 * Creates an array for the measured acceleration values and sets the factor for the floating average.
	 * 
	 * @param amount_of_data amount of data that will be measured
	 * @param factor count of iterations for the floating average
	 */
	public Acceleration(int amount_of_data, int factor){		
		accList = new double[amount_of_data];
		this.factor = factor;
	}//Ende Acceleration()
	
	/**
	 * Creates an array with the length 32 for the measured acceleration values.
	 * The factor for the floating average is set 1.
	 * 
	 */
	public Acceleration(){
		accList = new double[32];		
	}//Ende Acceleration()
	
	
	/**
	 * Gets the acceleration value in z-direction from IAccelerometer3D and subtracts the correction value from the calibration.  
	 * <p>{@code acc = accZ - correction;}
	 * @return acceleration value in z-direction
	 */
	private double getAcceleration3D(){
		
		acc = 0.0;
		
		try{
			double accZ = accelerometer3d.getAccelZ();
//			double accY = accelerometer3d.getAccelY();
//			double accX = accelerometer3d.getAccelX();
			acc = accZ-correction;			
		}catch(IOException io){
			io.printStackTrace();
		}
		
		return acc;
			
	}//Ende getAcceleration3D()
	
	/**
	 * Modifies the acceleration array with the floating average method.
	 * Every value is replaced by the average of its two neighbour values and itself:
	 * <p>{@code floatingAverageAccelerationList[i] = (accList[i-1]+accList[i]+accList[i+1])/3;}
	 * <p>The value of {@code factor} controls the number of iterations of this process.
	 * Afterwards the characteristic of the acceleration diagram will be smoother than with the raw values.
	 * The original acceleration array will be overwritten.
	 *  
	 * @return Array with acceleration values for a smoother characteristic
	 */
	public double[] setFloatingAverage(){
		
		double[] floatingAverageAccelerationList = new double[accList.length];
					
		if(factor<=0){
			floatingAverageAccelerationList=accList;
		}
		
		for (int j = 0; j<factor; j++){
		
			for(int i = 0; i<accList.length; i++){
				
				if(i!=0 && i!=accList.length-1){
					floatingAverageAccelerationList[i] = (accList[i-1]+accList[i]+accList[i+1])/3;
				}else{
					if(i==0){
						floatingAverageAccelerationList[i] = (accList[i]*2+accList[i+1])/3;
					}
					if(i==accList.length-1){
						floatingAverageAccelerationList[i]=(accList[i-1]+accList[i]*2)/3;
					}
					
				}
				
			}//Ende for(i)
			
			accList=floatingAverageAccelerationList;
			
		}//Ende for(j)
		
		System.out.println("\town values...");
		for(int i=0; i<accList.length; i++){
				System.out.println("\t" + Format.format(accList[i]));
		}
		
		return floatingAverageAccelerationList;
		
	}//Ende setFloatingAverage()
	
	/**
	 * @return Array of the times the measurements were executed.
	 */
	public long[] getTimeArray(){
		
		long[] _time = new long[time.size()];
		
		for(int i=0; i<_time.length; i++){
			_time[i] = ((Long)time.elementAt(i)).longValue();
		}
		
		return _time;
		
	}//Ende getTimeArray()
	
	
	/**
	 * @return Array of the times every measurement needed.
	 */
	public long[] getTimeForMeasurementArray(){

		long[] _tfm = new long[timeForMeasurement.size()];
		
		for(int i=0; i<_tfm.length; i++){
			_tfm[i] = ((Long)timeForMeasurement.elementAt(i)).longValue();
		}
		
		return _tfm;
		
	}//Ende getTimeForMeasurementArray()
	
	
	/**
	 * Adds the measured acceleration value to the array.
	 * 
	 * @param index index of the value in the array
	 */
	public void addAcceleration(int index){
		
		if(index>=accList.length){
						
			int N = accList.length*2;
			double [] _accList = new double[N];
			for(int i=0; i<N/2; i++){
				_accList[i] = accList[i];
			}
			accList = new double[N];
			accList=_accList;
			
		}
		
		accList[index] = getAcceleration3D();
		
	}//Ende addAcceleration
	
	
	/**
	 * @param correction value from calibration.
	 */
	public void setCorrection(double correction){
		this.correction = correction;
	}//Ende setCorrection()
	
	
	private void addTimeData(long t, long tfm){
		time.addElement(Long.valueOf(t));
		timeForMeasurement.addElement(Long.valueOf(tfm));
	}//Ende addTimeData()
	
	
	/**
	 * Starts the measurements of a defined amount of data and a defined sample period.
	 * In every step the acceleration value, the current time and the time needed for the measurement is saved. 
	 * 
	 * @param sample_period sample period for the measurements
	 */
	public void startMeasurement(int sample_period){
		
		//schleife zum messen der Daten
        for(int i=0; i<accList.length; i++) {
        	
            try {
                // Get the current time and sensor reading
                long now = System.currentTimeMillis();
                // add the acceleration
                addAcceleration(i);
                // get the time for 1 measurement                
	            long time = System.currentTimeMillis()-now;
	            // add the time data
	            addTimeData(now, time);
                                
                // Go to sleep to conserve battery
                Utils.sleep(sample_period - (System.currentTimeMillis() - now));
            } catch (Exception e) {
                System.err.println("Caught " + e + " while collecting/sending sensor sample.");
            }
            
        }
        
	}//Ende startMeasurement()
	
	/**
	 * Convertes the acceleration array in a format used by the class {@code NetworkMethods}.
	 * 
	 * @return double[][] desiredOutput
	 */
	public double[][] getDesiredOutput(){
		
		int length = accList.length;
		
		double[][] desiredOutput = new double[length][1];
		
		for(int i = 0; i<length;i++){
    		desiredOutput[i][0]=accList[i];
    	}
		
		return desiredOutput;
		
	}//Ende getDesiredOutput()
	
	

	/**
	 * @return Array with acceleration values.
	 */
	public double[] getAccelerationArray() {
		return accList;
	}


		
}

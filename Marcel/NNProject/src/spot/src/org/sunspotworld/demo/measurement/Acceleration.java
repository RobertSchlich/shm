package spot.src.org.sunspotworld.demo.measurement;

import java.io.IOException;
import java.util.Vector;

import spot.src.org.sunspotworld.demo.communication.LEDSignals;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;
import com.sun.squawk.util.MathUtils;

public class Acceleration{
	
	private double[] accList = new double[32];
	
	
	private Vector time = new Vector();
	private Vector timeForMeasurement = new Vector();
	private double acc = 0.0;
	private double correction = 0.0;
	private LEDSignals signals = new LEDSignals();
	private int Faktor = 1;
	
	

	public Acceleration(int amount_of_data, int Faktor){
		
		accList = new double[amount_of_data];
		
	}
	
	public Acceleration(){
		accList = new double[32];
		
		
	}
	
	private double getAcceleration3D(){
		
		
		
		IAccelerometer3D accelerometer3d = (IAccelerometer3D)Resources.lookup(IAccelerometer3D.class);
		
		acc = 0.0;
		
		try{
			//double signum = accelerometer3d.getAccel()/Math.abs(accelerometer3d.getAccel());
			double accX = accelerometer3d.getAccelZ();
//			double accY = accelerometer3d.getAccelY()+1;
//			double accZ = accelerometer3d.getAccelZ();
//			acc = signum * Math.sqrt(accX*accX + accY*accY + accZ*accZ) - correction;*/
			//acc = accelerometer3d.getAccel() - correction;
			acc = accX-correction;
			
		}catch(IOException io){
			io.printStackTrace();
		}
		
		return acc;
			
	}//Ende getAcceleration3D()
	
	
	public double[] getAccelerationArray(){
		
		double[] HaccList = new double[accList.length];
		double[] OaccList = new double[accList.length];
		
		OaccList=accList;
//		double Tacc;
//		for(int i=0;i<(int)accList.length/16-1;i++){
//			Tacc=0;
//			for(int j=0; j<16;j++)
//				Tacc+=accList[i*16+j];
//			HaccList[i]=Tacc/16*Faktor;
			
		if(Faktor<=0) HaccList=accList;
		
		for (int j = 0; j<Faktor; j++){
		
			for(int i = 0; i<accList.length; i++){
				if(i!=0 && i!=accList.length-1){
					HaccList[i]=
							(accList[i-1]+accList[i]+accList[i+1])/3;
				}
				else{
					if(i==0)
						HaccList[i]=
							(accList[i]*2+accList[i+1])/3;
					if(i==accList.length-1)
						HaccList[i]=
							(accList[i-1]+accList[i]*2)/3;
					
				}
			}
			accList=HaccList;
		}
		for(int i = 0; i<accList.length; i++){
				HaccList[i]=HaccList[i]*10000;
				HaccList[i]=MathUtils.round(HaccList[i]);
				HaccList[i]=HaccList[i]/10000;
			}
		
		for(int i=0;i<OaccList.length;i++)
			System.out.println("Original:\t"+OaccList[i]);
			
		return HaccList;
//		return accList;
		
	}//Ende getAccelerationArray()
	
	
	public long[] getTimeArray(){
		
		long[] _time = new long[time.size()];
		
		for(int i=0; i<_time.length; i++){
			_time[i] = ((Long)time.elementAt(i)).longValue();
		}
		
		return _time;
		
	}//Ende timeArray()
	
	
	
	public long[] getTimeForMeasurementArray(){

		long[] _tfm = new long[timeForMeasurement.size()];
		
		for(int i=0; i<_tfm.length; i++){
			_tfm[i] = ((Long)timeForMeasurement.elementAt(i)).longValue();
		}
		
		return _tfm;
		
	}//Ende getTimeForMeasurementArray()
	
	
	//Beschleunigungswert messen
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
	
	
	public void setCorrection(double _correction){
		correction = _correction;
	}//Ende setCorrection()
	
	
	private void addTimeData(long t, long tfm){
		time.addElement(Long.valueOf(t));
		timeForMeasurement.addElement(Long.valueOf(tfm));
	}//Ende addTimeData()
	
	
	//Messung der Beschleunigungswerte
	public void startMeasurement(int sample_period){
		
		//signals.on(0, LEDColor.GREEN);
		
		//schleife zum messen der Daten
        for(int i=0; i<accList.length; i++) {
        	
            try {
                // Get the current time and sensor reading
                long now = System.currentTimeMillis();
                // get the acceleration
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
        
       // signals.offLastIndex();
		
	}//Ende startmeasurement()
		
}

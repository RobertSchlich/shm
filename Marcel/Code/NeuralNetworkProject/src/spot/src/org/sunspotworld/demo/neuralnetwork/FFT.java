package spot.src.org.sunspotworld.demo.neuralnetwork;

import java.util.Vector;

import javax.microedition.io.Datagram;

import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.sensorboard.peripheral.TriColorLEDArray;
import com.sun.spot.util.Utils;
import com.sun.squawk.util.MathUtils;

public class FFT {
	
	public static Features feature = new Features();

	public double[] toFFT(Vector vnow, Vector vacc,int notZERO, long Intervall, int MAXFREQ, double lowPeak, double bandwithsize, Datagram dg, RadiogramConnection rCon) throws Exception{
    	
    	TriColorLEDArray colorLEDArray = (TriColorLEDArray)Resources.lookup(TriColorLEDArray.class);	// to get access to the LEDs

    	
//	deletes the first elements which are zero (becouse its possible to measure 49 times zero in starting mode)
    	
    	int firstZERO=0;
    	while (((Double) vacc.elementAt(firstZERO)).doubleValue()==0.0){
    		vacc.removeElementAt(firstZERO);
    		firstZERO++;
    	}
    	
//	vector to array (static field needed for FFT)

    	Double[] aAcc=new Double[notZERO-firstZERO];
    	Long[] aTime=new Long[notZERO-firstZERO];
    
    	for(int i=0;i<notZERO-firstZERO;i++){
    		aAcc[i]=(Double) vacc.elementAt(i);
    		aTime[i]=(Long) vnow.elementAt(i+firstZERO);
    	}

    	notZERO=notZERO-firstZERO;								// removes amount of first elements which were zero (just the last elements after the measurement are removed until now)
    	
//	FFT requires a count of elements to the power of 2 (2,4,8,16,32,64...)
    	
    	double pow=MathUtils.log(notZERO)/MathUtils.log(2);		// gets real power
    	pow=pow-pow%1;											// gets always value below the original value (4,9=4.0)
    	int rest=(notZERO)-(int)MathUtils.pow(2, pow);			
    	int usedElements=notZERO-rest;

    	double[] PureAcceleration= new double[usedElements];
    	for(int i=0;i<PureAcceleration.length;i++){
    		PureAcceleration[i]= aAcc[i].doubleValue();
    		}
    	

      
    
//*******************		FFT		*******************     	// Line   204..211 used from source shown in paper
	
    		 	
    		  Complex[] f = new Complex[usedElements];
    		  double[]	fabs= new double[usedElements];
    		  for(int r=0; r<usedElements; r++)
    		    f[r] = new Complex(PureAcceleration[r], 0);
    		  
    		  FFT(f, -1);                  						 // Hintransformation
    		  
    		  for(int r=0; r<usedElements; r++){
    			  	fabs[r]=f[r].abs();							// absolute value required without imaginary part
    			  	
//    		  System.out.println("^fd[" + r + "] = " + fd[r]);
    		  }
//    		  
//    		  FFT(f, 1);                    					// Rueckransformation
//    		  for(int r=0; r<N; r++)
//    		    System.out.println("f[" + r + "] = " + f[r]
//    		         + "\tOriginaldaten: " + adx[r]);
       
    		  ReturnForGetPeaks result;
    		  
    		  result=getPeaks(Intervall, usedElements, MAXFREQ, fabs, lowPeak, bandwithsize, colorLEDArray);
    		  double step = result.step;
    		  boolean b=sendPeaks(result, aTime, dg, rCon);
    		  //b=sendAllData(fabs, step, MAXFREQ, aTime, dg, rCon);
    		
    		
			return PureAcceleration;
    		
    		
    		
    }
   
    public static void FFT(Complex[] f, int sign) {												// method FFT and Class Complex used from sources shown in paper
    	  int N=f.length;       // Java-Arrays kennen ihre Laenge
    	  int mask;
    	  // *** Teste, ob N 2er-Potenz ist
    	  for(mask=1; mask<N; mask <<= 1)       ;
    	  if(mask != N)
    	    throw new RuntimeException("N = " + " ist keine 2er-Potenz !");
    	  // *** Teile Daten durch sqrt(N)
    	  double isqrtN = 1/Math.sqrt(N);
    	  for(int r=0; r<N; r++)
    	    f[r] = f[r].times(isqrtN);
    	  // *** Bit-Umkehr
    	  for(int t=0, r=0; r<N; r++) {
    	     if(t > r) {        // Vertausche f[r] und f[t]
    	        Complex temp = f[r];
    	        f[r] = f[t];
    	        f[t] = temp;
    	      }
    	     mask = N;          // Bit-umgekehrtes Inkrement von t
    	     do {
    	        mask >>= 1;
    	        t ^= mask;
    	      } while(((t & mask) == 0) && (mask != 0));
    	   }
    	  // *** Danielson-Lanczos Teil
    	  int n, no2 = 1;
    	  for(int m=1; (n=(no2 << 1)) <= N; m++) {
    	     Complex W = new Complex(Math.cos(2*Math.PI/n),
    	                 sign*Math.sin(2*Math.PI/n));           // W_n
    	     Complex Wk = new Complex(1, 0);
    	     for(int k=0; k<no2; k++) {
    	        for(int l=k; l<N; l+=n) {
    	           Complex temp = Wk.times(f[l+no2]);
    	           f[l+no2] = f[l].minus(temp);
    	           f[l]     = f[l].plus(temp);
    	         }
    	        Wk = Wk.times(W);       // Wk = W^k
    	      }
    	     no2 = n;
    	   }
    	 }
    

    public ReturnForGetPeaks getPeaks(long Intervall, int usedElements, int 	MAXFREQ, double[] fabs, double lowPeak, double bandwithsize, TriColorLEDArray colorLEDArray){
	   
	   ReturnForGetPeaks result= new ReturnForGetPeaks();
	   
//		get frequency stepsize    		

		int count=0;											// counter for peak detection
	
		double rate=1000/Intervall;								// in this case 40Hz
		double step= rate/ ((double) usedElements);				// stepsize of frequenzy on X-axis


//peak detection 
		
		Vector maxMag =new Vector ();
		Vector maxFreq =new Vector ();
		Vector maxPoint =new Vector ();
		
 		double cMax=0, bandinElements=0;
 		int Point=0;
 		for (int i=0;i<fabs.length;i++){
 			if (cMax<fabs[i] && i*step<MAXFREQ){						// while below maximum and 21Hz
				 cMax=fabs[i];
 			 Point=i;
 			 }
			} 
 		
 		colorLEDArray.setColor(LEDColor.TURQUOISE);						// set color
		colorLEDArray.setOff();											// set LEDs off

 		
			bandinElements=((cMax/Math.sqrt(2))/step)*2;					// amount of elements in bandwith (to count later)
			int block= (int)MathUtils.round(bandinElements),  				// requires integer value   
			anzpeak=0;
			if(block<1)block=1;
 		System.out.println("max: "+cMax+" range: "+block+" point: "+Point+"/"+fabs.length);
 		
 		int i=0, o=0;													// different counters for different loops												
 		int 	MaximumPoint=0;											// initialize
 		double 	MaximumMagnitude=0.0, 
 				MaximumFrequenz=0.0;
 		
 	
 		while(o<fabs.length && step*o<=MAXFREQ-0.5){
// 		System.out.println("0");
 			MaximumMagnitude=0.0;
 			for(o=1+count;(o<=block*(i+1) && o<fabs.length-1);o++)	{
//				System.out.println("1");
				if (MaximumMagnitude<fabs[o]&& step*o<=MAXFREQ-1){
//					System.out.println("2");
   				 if (fabs[o]>lowPeak*cMax) {						// reduce noise
   					double 	dy1=fabs[o]-fabs[o-1];					// calculate rising
   					double 	dy2=fabs[o+1]-fabs[o];
   					double	dx1=step;
   					double 	dx2=step;
   					double  m1=dy1/dx1, m2=dy2/dx2;
   					 
   					 if (m1!=0) m1=m1/Math.abs(m1);
   					 if (m2!=0) m2=m2/Math.abs(m2);
   					 
   					 if (m1>m2){									// its only a peak if it is king of the hill
   						 MaximumMagnitude=fabs[o];
   						 MaximumFrequenz=o*step;
   						 MaximumPoint=o;
   					 
   					 }	 
   				 }
   				
   			}		
			}
			
			if(MaximumMagnitude!=0){
			 feature.blinkall(colorLEDArray,250);	
			 maxMag.addElement(new Double (MaximumMagnitude));
			 maxPoint.addElement(new Integer (MaximumPoint));
			 maxFreq.addElement(new Double (MaximumFrequenz));
			}
			i++;
			count=count+block;											// to step into the next block
		}
 		
 		for(int u=0;u<maxMag.size();u++) 
 			if(((Double)maxMag.elementAt(u)).doubleValue()!=0.0)  {
// 				String mag=maxMag.elementAt(u).toString(); 
//     			String si=maxPoint.elementAt(u).toString(); 
//     			System.out.println("Index: "+u+" Mag: "+mag+" i: "+si+" anzpeak: "+anzpeak);
 				anzpeak++;
 			}
//one of two peaks that are to close to each other is not a peak
//save the higher one, delete the other
 		
 		for(int u=0;u<maxMag.size()-1;u++) {
 			
 			Double mag1=(Double)maxMag.elementAt(u), 
 					mag2=(Double)maxMag.elementAt(u+1);
 			
 			Integer si1=(Integer)maxPoint.elementAt(u), 
 					si2=(Integer)maxPoint.elementAt(u+1); 
 			
 			if(si2.intValue()-si1.intValue()<(block*bandwithsize)) {
 				if(mag1.doubleValue()>mag2.doubleValue()){ 
 					maxMag.removeElementAt(u+1);
 					maxPoint.removeElementAt(u+1);
 					maxFreq.removeElementAt(u+1); 
 					anzpeak--; 
 					}
 					else{
 						maxMag.removeElementAt(u); 
 						maxPoint.removeElementAt(u); 
 						maxFreq.removeElementAt(u); 
 						anzpeak--; 
 						} 
 				} 
 			} 
 		for(int u=0;u<maxMag.size();u++) {
 			
 			String mag=maxMag.elementAt(u).toString(); 
 			String si=maxPoint.elementAt(u).toString(); 
 			System.out.println("Index: "+u+" Mag: "+mag+" i: "+si+" anzpeak: "+anzpeak);
 			}
 		
 		result.anzpeak=anzpeak;
 		result.maxFreq=maxFreq;
 		result.maxMag=maxMag;
 		result.maxPoint=maxPoint;
 		result.step=step;
 		
 		maxMag.removeAllElements();															// clear all vectors
		maxPoint.removeAllElements();
		maxFreq.removeAllElements();
	   
	return result;
	   
   }
	
    public static boolean sendPeaks(ReturnForGetPeaks result, Long[] aTime, Datagram dg, RadiogramConnection rCon){
	   Vector maxMag=result.maxMag;
		 Vector maxFreq=result.maxFreq;
		 Vector maxPoint=result.maxPoint;
		 int anzpeak=result.anzpeak;
		  
//send peaks
		
		for(int z=0;z<maxMag.size();z++){
			
				if(((Double)maxMag.elementAt(z)).doubleValue()!=0.0){
			dg.reset();
			try{
				dg.writeUTF("peak");															// identifier
				Long time=(Long) aTime[((Integer)maxPoint.elementAt(z)).intValue()] ;			// time
//				System.out.println("ZEIT: "+time);
				dg.writeLong(time.longValue());
				dg.writeDouble(((Double)maxMag.elementAt(z)).doubleValue());					// magnitude
				dg.writeDouble(((Double)maxFreq.elementAt(z)).doubleValue());					// frequency
				dg.writeInt(anzpeak);															// element count
	   			Utils.sleep(100);	
				rCon.send(dg);																	// ENTER :)
				}catch(Exception e)
				{
				System.out.println("Fehler beim Senden der Daten: "+e);	
				}
			}
		}
	   return true;
   }
   
    public static boolean  sendAllData(double[] fabs,double step, int 	MAXFREQ, Long[] aTime, Datagram dg, RadiogramConnection rCon){


		
		for(int z=0;z<fabs.length;z++){
			
				
			dg.reset();
			try{

				if (z*step<=MAXFREQ){
					
					dg.reset();
					
					dg.writeUTF("all");									// identifier
					Long time=(Long) aTime[z] ;							// time
					dg.writeLong(time.longValue());
					dg.writeDouble(fabs[z]);							// magnitude
					dg.writeDouble(z*step);								// frequency
					dg.writeInt(fabs.length);							// element count
					Utils.sleep(100);
					rCon.send(dg);										// ENTER :)
				}												// ENTER :)
				}catch(Exception e)
				{
				System.out.println("Fehler beim Senden der Daten: "+e);	
				}
			
			
		}
	   Utils.sleep(2500);
	   return true;
   }
   
	
}

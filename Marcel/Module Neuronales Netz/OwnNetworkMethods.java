package spot.src.org.sunspotworld.demo;

import java.util.Vector;

public class OwnNetworkMethods {

    
    public static   void autoNetworktrain(NeuralNetwork net, double[][] newinput, double[][] desout,int SensorCount, int HiddenUnits){

    	
    	
    	double fehler=0;
    	
    	TrainingSampleLesson lesson = new TrainingSampleLesson(newinput,desout); //TrainingSampleLesson.getEncoderSampleLesson(2, 1, -1);
    	
    	
//    	DecimalFormat df = new DecimalFormat("#.#");
    	for (int i = 0; i < lesson.countSamples(); i++) {
    		double[] output = (lesson.getInputs()[i]);
    		for (int j = 0; j < output.length; j++) {
    			System.out.print(i + "\t"+ (output[j]) + "\t");
    		}
    		System.out.println("");
    	}
    	
    	/*
    	 * Train that sucker with backprop in three phases with different
    	 * learning rates. In between, display progress, and measure overall
    	 * time.
    	 */
    	long startTime = System.currentTimeMillis();
    	System.out.println("Root Mean Square Error before training:\t"
    			+ ErrorMeasurement.getErrorRootMeanSquareSum(net, lesson));
    	do{
    	net.trainBackpropagationOfError(lesson, 5000, 0.2);
    	fehler =ErrorMeasurement.getErrorRootMeanSquareSum(net, lesson);
    	System.out.println("Root Mean Square Error after phase 1:\t"
    			+ fehler);
    	}
    	while(fehler>0.01);
   
    	long endTime = System.currentTimeMillis();
    	long time = endTime - startTime;

    	
    	
    	System.out.println("\nTime taken: " + time + "ms");
    		

    }
    
    public static void  autoNetworktest(NeuralNetwork net, double[][] newinput){

    	
    		
    	System.out.println("\nNetwork output:");
    	for (int i = 0; i < 100; i++) {
    		double[] output = net.propagate(newinput[i]);
    		for (int j = 0; j < output.length; j++) {
    			System.out.println((output[j]) + "\t");
   		}
    	}
   		
    	
    	System.out.println("");
    
    }
    
public static ArrayOfArray GetAllWeights(NeuralNetwork net, int SensorCount, int HiddenUnits){
  	


double[][] WeightIntoHid=null;//Weight.feld1; 
double[][] WeightHidtoOut=null;//=Weight.feld2;
ArrayOfArray Weights=null;

	
	for(int x=1;x<=SensorCount;x++){
		for(int y=SensorCount+1;y<=SensorCount+HiddenUnits;y++){
			//
			net.setSynapse(x, y, WeightIntoHid[x-1][y-SensorCount-1]);
//			System.out.println("Weight "+x+" - "+y+ ":"+net.getWeight(x, y));
		}	
	}
	for(int z=SensorCount+1;z<=SensorCount+HiddenUnits;z++){
		net.setSynapse(z, SensorCount+HiddenUnits+1, WeightHidtoOut[z-SensorCount-1][0]);
//			System.out.println("Weight "+z+" - "+"O"+ ":"+net.getWeight(z, SensorCount+hidden+1));
			
	}
	Weights.feld1=WeightIntoHid;
	Weights.feld2=WeightHidtoOut;
	
	
	return Weights;	
	
}
   
	
}

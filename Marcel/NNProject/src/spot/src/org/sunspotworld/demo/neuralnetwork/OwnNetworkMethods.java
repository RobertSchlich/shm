package spot.src.org.sunspotworld.demo.neuralnetwork;

import java.util.Vector;

public class OwnNetworkMethods {

    //newinput[anzahl messwerte][anzahl sensoren - 1], desout[anzahlMesswerte][anzahl ausgänge]
    public static   void autoNetworktrain(NeuralNetwork net, double[][] newinput, double[][] desout,int SensorCount, int HiddenUnits, double eta, double LEARN_LIM, int LEARN_COUNT){

    	double fehler=0, limit=0, hilf=0;
    	
    	TrainingSampleLesson lesson = new TrainingSampleLesson(newinput,desout); //TrainingSampleLesson.getEncoderSampleLesson(2, 1, -1);
    	
    	for (int i = 0; i < lesson.countSamples(); i++) {
    		double[] output = (lesson.getInputs()[i]);
    		for (int j = 0; j < output.length; j++) {
//    			System.out.print(i + "\t"+ (output[j]) + "\t");
    		}
//    		System.out.println("");
    	}
    	
    	/*
    	 * Train that sucker with backprop in three phases with different
    	 * learning rates. In between, display progress, and measure overall
    	 * time.
    	 */
    	long startTime = System.currentTimeMillis();
    	System.out.println("Root Mean Square Error before training:\t"
    			+ ErrorMeasurement.getErrorRootMeanSquareSum(net, lesson));
    	int loop=0;
    	do{
    	loop++;
    	net.trainBackpropagationOfError(lesson, LEARN_COUNT, eta);
    	fehler =ErrorMeasurement.getErrorRootMeanSquareSum(net, lesson);
    	System.out.println("Root Mean Square Error after phase "+loop+":\t"
    			+ fehler);
    	
    	limit=limit+fehler;
    	hilf=100000/ LEARN_COUNT *2.5;
    	if(loop>hilf)	LEARN_LIM=limit/loop;
    	System.out.println("LEARN_LIM:\t"+LEARN_LIM);
    	}while(fehler>LEARN_LIM*0.95);
   
    	long endTime = System.currentTimeMillis();
    	long time = endTime - startTime;

    	
    	
    	System.out.println("\nTime taken: " + time + "ms");
    		

    }
    
    public static double[]  autoNetworktest(NeuralNetwork net, double[][] newinput){

    	
    	double[] backout=new double[newinput.length];	
//    	System.out.println("\nNetwork output:");
    	for (int i = 0; i < newinput.length; i++) {
    		double[] output = net.propagate(newinput[i]);
    		for (int j = 0; j < output.length; j++) {
//    			System.out.println((output[j]) + "\t");
    			backout[i]=output[j];
    			
   		}
    	}
   		
    	
    	System.out.println("");
		return backout;
    
    }
    
public static ArrayOfArray GetAllWeightsForNetworkWithOneHiddenLayer(NeuralNetwork net, int SensorCount, int HiddenUnits){
  	


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

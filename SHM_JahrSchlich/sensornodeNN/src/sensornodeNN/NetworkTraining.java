/**
 * @author Jahr&Schlich
 * 
 */

package sensornodeNN;

public class NetworkTraining {
	  
	public void Train(NeuralNetwork net, TrainingSampleLesson lesson) {

		int runs = 100000; // number of random patterns to train
		double eta = 0.5; //learning rate
		double threshold = 1e-5;
	 
		// Train neural network until error falls below limit
		double error;
		double newerror;
		int numOfPhases = 0;
		do{
			numOfPhases++;
			error = ErrorMeasurement.getErrorRootMeanSquareSum(net, lesson);
			System.out.println("Error 1: "+ error);
			// lower training rate with every cycle
			eta = eta/2;
			//train the neural network
			net.trainBackpropagationOfError(lesson, runs, eta);
			//calculate new error
			newerror = ErrorMeasurement.getErrorRootMeanSquareSum(net, lesson);
			System.out.println("Error after training phase "+ numOfPhases + " = " + newerror );
			// try to lower error 10 times
			if (numOfPhases > 10) break;
			
		}while(newerror > threshold);
		
		System.out.println(numOfPhases + " trainings. ");
		}
	}

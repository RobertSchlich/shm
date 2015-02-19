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
			
			eta = eta/2;
			
			net.trainBackpropagationOfError(lesson, runs, eta);
			
			//net.trainResilientBackpropagation(lesson, runs, false);
			
			
			
			newerror = ErrorMeasurement.getErrorRootMeanSquareSum(net, lesson);
			
			System.out.println("Error after training phase "+ numOfPhases + " = " + newerror );
			//System.out.println("Abbruchbedingung "+ Math.abs(newerror-error)*100 / newerror);
			
			if (numOfPhases > 10) break;
			
		}while(newerror > threshold);
		
		System.out.println(numOfPhases + " training phases. ");

		}

	}

package sensornodeNN;

public class NetworkTraining {
	  

	public static void Train(NeuralNetwork net, TrainingSampleLesson lesson) {

		 
		int runs = 20000; // number of random patterns to train
		double eta = 0.2; //learning rate
		double threshold = 0.99;
	 
		
		// Train neural network until error falls below limit
		double error;
		double newerror;
		int numOfPhases = 0;
	
		do{
			numOfPhases++;
			error = ErrorMeasurement.getErrorRootMeanSquareSum(net, lesson);
			net.trainBackpropagationOfError(lesson, runs, eta);
			newerror = ErrorMeasurement.getErrorRootMeanSquareSum(net, lesson);
			System.out.println("Error after training phase "+ numOfPhases + " = " + newerror );
		}while(newerror / error > threshold);
		
		System.out.println(numOfPhases + "training phases. ");

		}

	}

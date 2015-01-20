package spot.src.org.sunspotworld.demo.neuralnetwork;




/**
 * 
 * This class contains several methods of error measurement for use in a static
 * way.
 * 
 * @author David Kriesel / dkriesel.com
 * 
 */
public class ErrorMeasurement {

	/**
	 * Calculates the sum of absolute errors of the given network over all
	 * samples in the training lesson.
	 * 
	 * @param net
	 * @param lesson
	 * 
	 * @return calculated error
	 */
	public static double getErrorAbsoluteSum(NeuralNetwork net,
			TrainingSampleLesson lesson) {

		double[][] inputs = lesson.getInputs();
		double[][] desiredOutputs = lesson.getDesiredOutputs();

		double error = 0.0;

		for (int i = 0; i < inputs.length; i++) {

			double[] outputs = net.propagate(inputs[i]);

			for (int j = 0; j < outputs.length; j++) {
				double temp = 0.0;
				temp = desiredOutputs[i][j] - outputs[j];
				temp = Math.abs(temp);
				error += temp;
			}
		}
		return error;

	}

	/**
	 * Calculates the sum of euclidean errors of the given network over all
	 * samples in the training lesson.
	 * 
	 * @param net
	 * @param lesson
	 * 
	 * @return calculated error
	 */
	public static double getErrorEuclideanSum(NeuralNetwork net,
			TrainingSampleLesson lesson) {

		double[][] inputs = lesson.getInputs();
		double[][] desiredOutputs = lesson.getDesiredOutputs();

		double error = 0.0;

		for (int i = 0; i < inputs.length; i++) {

			double[] outputs = net.propagate(inputs[i]);

			for (int j = 0; j < outputs.length; j++) {
				double temp = 0.0;
				temp = desiredOutputs[i][j] - outputs[j];
				temp = temp * temp;
				temp = Math.sqrt(temp);
				error += temp;
			}
		}
		return error;
	}

	/**
	 * Calculates the Squared percentage error of the given network over all
	 * samples in the training lesson. This method is proposed by Prechelt
	 * (Prechelt 1994, Proben1 - A set of neural Network Benchmarking Problems
	 * and benchmarking rules) and seeks for an error measurement that is
	 * independent of output dimensionality and number of samples.
	 * 
	 * Note that networks can (and in early training phases often will) produce
	 * more than 100% squared error percentage if they use output neurons, whose
	 * output range is not restricted to the minimal and maximal output values
	 * in the training samples' desired outputs.
	 * 
	 * @param net
	 * @param lesson
	 * @return calculated squared error percentage
	 */
	public static double getErrorSquaredPercentagePrechelt(NeuralNetwork net,
			TrainingSampleLesson lesson) {
		double[][] inputs = lesson.getInputs();
		double[][] desiredOutputs = lesson.getDesiredOutputs();

		double error = 0.0;

		double minValue = Double.MAX_VALUE;
		double maxValue = Double.MIN_VALUE;

		for (int i = 0; i < inputs.length; i++) {

			double[] outputs = net.propagate(inputs[i]);

			for (int j = 0; j < outputs.length; j++) {

				if (desiredOutputs[i][j] < minValue) {
					minValue = desiredOutputs[i][j];
				}
				if (desiredOutputs[i][j] > maxValue) {
					maxValue = desiredOutputs[i][j];
				}

				double temp = 0.0;
				temp = desiredOutputs[i][j] - outputs[j];
				temp = temp * temp;
				temp = Math.sqrt(temp);
				error += temp;
			}
		}

		error *= 100.0 * ((maxValue - minValue) / (lesson
				.getDimensionalityDesiredOutputs() * lesson.countSamples()));
		return error;
	}

	/**
	 * Calculates the sum of square errors of the given network over all samples
	 * in the training lesson.
	 * 
	 * @param net
	 * @param lesson
	 * 
	 * @return calculated error
	 */
	public static double getErrorSquareSum(NeuralNetwork net,
			TrainingSampleLesson lesson) {

		double[][] inputs = lesson.getInputs();
		double[][] desiredOutputs = lesson.getDesiredOutputs();

		double error = 0.0;

		for (int i = 0; i < inputs.length; i++) {

			double[] outputs = net.propagate(inputs[i]);

			for (int j = 0; j < outputs.length; j++) {
				double temp = 0.0;
				temp = desiredOutputs[i][j] - outputs[j];
				temp = temp * temp;
				error += temp;
			}
		}

		return error;

	}

	/**
	 * Calculates root mean square errors of the given network over all samples
	 * in the training lesson.
	 * 
	 * @param net
	 * @param lesson
	 * 
	 * @return calculated error
	 */
	public static double getErrorRootMeanSquareSum(NeuralNetwork net,
			TrainingSampleLesson lesson) {

		double[][] inputs = lesson.getInputs();
		double[][] desiredOutputs = lesson.getDesiredOutputs();

		double error = 0.0;

		for (int i = 0; i < inputs.length; i++) {

			double[] outputs = net.propagate(inputs[i]);

			for (int j = 0; j < outputs.length; j++) {
				double temp = 0.0;
				temp = desiredOutputs[i][j] - outputs[j];
				temp = temp * temp;
				error += temp;
			}
		}
		//if(lesson.getDimensionalityDesiredOutputs()!=0 )
		error /= lesson.getDimensionalityDesiredOutputs();// else error = error;
		error = Math.sqrt(Math.abs(error));  // hier eigentlich ohne Math.abs 
		return error;

	}

}

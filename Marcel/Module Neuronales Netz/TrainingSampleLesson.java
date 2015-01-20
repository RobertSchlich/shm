package spot.src.org.sunspotworld.demo;


//import java.io.Serializable;
import java.util.Random;



/**
 * This class represents a set of training samples in order to train a neural
 * network. Once created, an instance of this class checks whether input or
 * output are null, of inequal internal length, not given, etc. Inputs and
 * desired output samples are given as 2D-double-arrays
 * <ul>
 * <li>inputs[n][i] and
 * <li>desiredOutputs[n][o],
 * </ul>
 * where n is the number of samples, i is the input dimensionality and o is the
 * output dimensionality.
 * 
 * <p>
 * It is recommended to both optimize inputs and desired outputs using the
 * methods starting with "optimize" (read their documentation).
 * 
 * <p>
 * If teaching classification problems you should consider placing the binary
 * values at -x and x, where x is the absolute location of the absolute maxima
 * of the 2nd derivative of the activation function (LeCun - Efficient
 * Backprop). Use the scaleDesiredOutputsForClassificationProblem method in
 * order to do this.
 * 
 * @author David Kriesel / dkriesel.com
 */
public class TrainingSampleLesson{// implements Serializable {

	private static final long serialVersionUID = 1L;
	private double[][] inputs;
	private double[][] desiredOutputs;

	/**
	 * Creates a TrainingSampleLesson Instance.
	 * 
	 * @param inputs
	 *            The first array index represents the index of a training
	 *            sample, the second represents the number of an input neuron.
	 *            Attention: This array is cloned to avoid conflicts with
	 *            changes in the original one.
	 * @param desiredOutputs
	 *            The first array index represents the index of a training
	 *            sample, the second represents the number of an output neuron.
	 *            Attention: This array is cloned to avoid conflicts with
	 *            changes in the original one.
	 */
	public TrainingSampleLesson(double[][] inputs, double[][] desiredOutputs) {

		if (inputs == null) {
			throw new IllegalArgumentException("Inputs null.");
		}
		if (desiredOutputs == null) {
			throw new IllegalArgumentException("Desired Outputs null.");
		}
		if (inputs == desiredOutputs) {
			throw new IllegalArgumentException(
					"Inputs and Desired Outputs are the same array.");
		}
		if (inputs.length < 1) {
			throw new IllegalArgumentException("0 Inputs given.");
		}

		if (desiredOutputs.length < 1) {
			throw new IllegalArgumentException("0 Desired Outputs given.");
		}

		if (desiredOutputs.length != inputs.length) {
			throw new IllegalArgumentException(
					"Desiredoutputs.length != inputs.length.");
		}

		if (inputs[0] == null) {
			throw new IllegalArgumentException("Input " + 0 + " null.");
		}
		for (int i = 0; i < inputs.length - 1; i++) {
			if (inputs[i + 1] == null) {
				throw new IllegalArgumentException("Input " + (i + 1)
						+ " null.");
			}
			if (inputs[i].length != inputs[i + 1].length) {
				throw new IllegalArgumentException(
						"Inputs not of equal lenght.");
			}
		}

		if (desiredOutputs[0] == null) {
			throw new IllegalArgumentException("Desired Output " + 0 + " null.");
		}
		for (int i = 0; i < desiredOutputs.length - 1; i++) {
			if (desiredOutputs[i + 1] == null) {
				throw new IllegalArgumentException("Desired Output " + (i + 1)
						+ " null.");
			}
			if (desiredOutputs[i].length != desiredOutputs[i + 1].length) {
				throw new IllegalArgumentException(
						"Desired Outputs not of equal lenght.");
			}
		}

		double[][] inputClone = new double[inputs.length][inputs[0].length];
		double[][] desiredOutputClone = new double[desiredOutputs.length][desiredOutputs[0].length];

		for (int i = 0; i < desiredOutputClone.length; i++) {
			inputClone[i] =(double[]) inputs[i];//.clone();
			desiredOutputClone[i] = (double[])desiredOutputs[i];//.clone();
		}

		this.inputs = inputClone;
		this.desiredOutputs = desiredOutputClone;

	}

	/**
	 * Creates a training lesson for training an encoder/decoder problem, like
	 * for example the 8-3-8 problem. To train an 8-3-8 network, first create a
	 * network with 8 inputs, 3 hidden neurons and 8 outputs, then train an
	 * encoder sample lesson with dimensionality 8.
	 * 
	 * @param dim
	 * @param positiveValue
	 * @param negativeValue
	 * @return the desired encoder sample lesson
	 */
	public static TrainingSampleLesson getEncoderSampleLesson(int dim,
			double positiveValue, double negativeValue) {
		double[][] inputs = new double[dim][dim];
		double[][] desiredOutputs = new double[dim][dim];
		for (int i = 0; i < desiredOutputs.length; i++) {
			for (int j = 0; j < desiredOutputs[i].length; j++) {
				inputs[i][j] = i == j ? positiveValue : negativeValue;
				desiredOutputs[i][j] = i == j ? positiveValue : negativeValue;
			}
		}
		return new TrainingSampleLesson(inputs, desiredOutputs);
	}

	/**
	 * @return the input dimensionality
	 */
	public int getDimensionalityInputs() {
		return inputs[0].length;
	}

	/**
	 * @return the output dimensionality
	 */
	public int getDimensionalityDesiredOutputs() {
		return desiredOutputs[0].length;
	}

	/**
	 * @return the number of samples
	 */
	public int countSamples() {
		return inputs.length;
	}

	/**
	 * @return the inputs. attention: This is no clone! If you change anything,
	 *         be sure to know what you are doing.
	 */
	public double[][] getInputs() {
		return inputs;
	}

	/**
	 * @return the desiredOutputs. attention: This is no clone. If you change
	 *         anything, be sure to know what you are doing.
	 */
	public double[][] getDesiredOutputs() {
		return desiredOutputs;
	}

	/**
	 * Splits a TrainingSampleLesson in two according to a given ratio in the
	 * range ]0;1[ in
	 * O(NUMBEROFSAMPLES*MAX(INPUTDIMENSIONALITY;DESIREDOUTPUTDIMENSIONALITY)).
	 * This ratio represents the probability that any of the training samples
	 * will be randomly chosen to be assigned to the first result lesson, thus
	 * 1-ratio represents the probability for any training sample to become part
	 * of the second result lesson. Training Samples will be cloned by this
	 * operation, no shallow copy will be performed.
	 * 
	 * Please note that each training sample will be assigned to each of the two
	 * result lessons by a pseudo random number generator. Therefore, for very
	 * small numbers of samples and rations chosen very close to 1 or 0, it may
	 * obviously happen that no sample is assigned to one of the result lessons.
	 * 
	 * To overcome this caveat that also might cause exceptions, two measures
	 * are implemented. First, invocation of splitLesson on
	 * TrainingSampleLessons with less than 2 samples throws an exception.
	 * Second, it is checked if one of the result lessons is assigned zero
	 * samples. In such a case, it will be assigned one training sample lesson
	 * of the other lesson that was before assigned all samples.
	 * 
	 * @param ratio
	 *            must be in interval ]0;1[
	 * @return an array of 2 TrainingSampleLessons. Index 0 is the one of the
	 *         first lesson, index 1 the one of the second.
	 */
	public TrainingSampleLesson[] splitLesson(double ratio) {
		if (countSamples() < 2) {
			throw new IllegalArgumentException(
					"Can't split TrainingSampleLessons with less than 2 samples.");
		}
		if (ratio >= 1.0 || ratio <= 0.0) {
			throw new IllegalArgumentException(
					"Ratio for splitting must be in the range ]0;1[.");
		}

		Random random = new Random(System.currentTimeMillis());

		// form an boolean array that decides whether a sample will be assigned
		// to the first lesson (true, probability ratio) or to the second
		// (false, probability 1-ratio)
		int firstLessonCounter = 0;
		int secondLessonCounter = 0;
		boolean[] assignment = new boolean[countSamples()];
		for (int i = 0; i < assignment.length; i++) {
			if (random.nextDouble() < ratio) {
				assignment[i] = true;
				firstLessonCounter++;
			} else {
				assignment[i] = false;
				secondLessonCounter++;
			}
		}

		if (firstLessonCounter == 0) {
			// means all assignment booleans are false
			assignment[random.nextInt(assignment.length)] = true;
			firstLessonCounter++;
		}
		if (secondLessonCounter == 0) {
			// means all assignment booleans are true
			assignment[random.nextInt(assignment.length)] = false;
			secondLessonCounter++;
		}

		double[][] firstLessonInputs = new double[firstLessonCounter][getDimensionalityInputs()];
		double[][] firstLessonDesiredOutputs = new double[firstLessonCounter][getDimensionalityDesiredOutputs()];
		double[][] secondLessonInputs = new double[secondLessonCounter][getDimensionalityInputs()];
		double[][] secondLessonDesiredOutputs = new double[secondLessonCounter][getDimensionalityDesiredOutputs()];

		int firstRunningIndex = 0;
		int secondRunningIndex = 0;
		for (int i = 0; i < assignment.length; i++) {
			if (assignment[i]) {
				// assign sample to first lesson
				firstLessonInputs[firstRunningIndex] =(double[]) inputs[i];//.clone();
				firstLessonDesiredOutputs[firstRunningIndex] = (double[])desiredOutputs[i];//.clone();
				firstRunningIndex++;
			} else {
				// assign sample to second lesson
				secondLessonInputs[secondRunningIndex] =(double[]) inputs[i];//.clone();
				secondLessonDesiredOutputs[secondRunningIndex] = (double[]) desiredOutputs[i];//.clone();
				secondRunningIndex++;
			}
		}

		TrainingSampleLesson[] result = new TrainingSampleLesson[2];
		result[0] = new TrainingSampleLesson(firstLessonInputs,
				firstLessonDesiredOutputs);
		result[1] = new TrainingSampleLesson(secondLessonInputs,
				secondLessonDesiredOutputs);

		return result;
	}

	/**
	 * Permute all training samples of this lesson in O(NUMBEROFSAMPLES).
	 */
	public void shuffleSamples() {
		int[] newOrder = new int[countSamples()];

		Random random = new Random(System.currentTimeMillis());

		// create initial ordered index array
		for (int i = 0; i < newOrder.length; i++) {
			newOrder[i] = i;
		}

		int permutationBorder = newOrder.length - 1;

		while (permutationBorder > 0) {

			int nextIndexToSwap = random.nextInt(permutationBorder + 1);

			// swap them
			int tempIndex = newOrder[permutationBorder];
			newOrder[permutationBorder] = newOrder[nextIndexToSwap];
			newOrder[nextIndexToSwap] = tempIndex;

			permutationBorder--;
		}

		double[][] newInputs = new double[countSamples()][getDimensionalityInputs()];
		double[][] newDesiredOutputs = new double[countSamples()][getDimensionalityDesiredOutputs()];

		for (int i = 0; i < newOrder.length; i++) {
			newInputs[i] = inputs[newOrder[i]];
			newDesiredOutputs[i] = desiredOutputs[newOrder[i]];
		}

		inputs = newInputs;
		desiredOutputs = newDesiredOutputs;
	}

	/**
	 * Scales the input components so that all of them have the same covariance
	 * in O(NUMBEROFSAMPLES*INPUTDIMENSIONALITY) It is strongly recommended to
	 * use optimizeInputsCancelMeans() before because this function expects the
	 * averages of each input component to be zero. The covariance should be
	 * matched with that of the sigmoid used in the neurons.
	 * 
	 * @param desiredCovariance
	 *            the covariance you want the input components to have
	 * @return the factors the input components are scaled with.
	 */
	public double[] optimizeInputsAlignCovariances(double desiredCovariance) {

		// get covariances
		double[] covariances = new double[getDimensionalityInputs()];

		for (int i = 0; i < inputs.length; i++) {
			for (int j = 0; j < covariances.length; j++) {
				covariances[j] += (inputs[i][j] * inputs[i][j]);
			}
		}
		for (int i = 0; i < covariances.length; i++) {
			covariances[i] /= inputs.length;
		}

		// calculate scaling factors
		double[] scalingFactors = new double[getDimensionalityInputs()];
		for (int i = 0; i < scalingFactors.length; i++) {
			scalingFactors[i] = Math.sqrt(desiredCovariance / covariances[i]);
		}

		// scale inputs
		for (int i = 0; i < inputs.length; i++) {
			for (int j = 0; j < scalingFactors.length; j++) {
				inputs[i][j] *= scalingFactors[j];
			}
		}

		return scalingFactors;
	}

	/**
	 * First calculates the average of each input component, then subtracts the
	 * resulting average vector from all input samples in
	 * O(NUMBEROFSAMPLES*INPUTDIMENSIONALITY).
	 * 
	 * @return the average vector of the inputs before subtraction.
	 */
	public double[] optimizeInputsCancelMeans() {

		double[] means = new double[getDimensionalityInputs()];

		// calculate means
		for (int i = 0; i < inputs.length; i++) {
			for (int j = 0; j < means.length; j++) {
				means[j] += inputs[i][j];
			}
		}
		for (int i = 0; i < means.length; i++) {
			means[i] /= inputs.length;
		}

		// substract means
		for (int i = 0; i < inputs.length; i++) {
			for (int j = 0; j < means.length; j++) {
				inputs[i][j] -= means[j];
			}
		}

		return means;
	}

	/**
	 * Scales all inputs by the given factor in
	 * O(NUMBEROFSAMPLES*INPUTDIMENSIONALITY).
	 * 
	 * @param factor
	 */
	public void scaleInputs(double factor) {
		for (int i = 0; i < inputs.length; i++) {
			for (int j = 0; j < inputs[i].length; j++) {
				inputs[i][j] *= factor;
			}
		}
	}

	/**
	 * Scales all desired Outputs by the given factor in
	 * O(NUMBEROFSAMPLES*DESIREDOUTPUTDIMENSIONALITY).
	 * 
	 * @param factor
	 */
	public void scaleDesiredOutputs(double factor) {
		for (int i = 0; i < desiredOutputs.length; i++) {
			for (int j = 0; j < desiredOutputs[i].length; j++) {
				desiredOutputs[i][j] *= factor;
			}
		}
	}

	/**
	 * 
	 * If teaching classification problems you should consider placing the
	 * binary values at -x and x, where x is the absolute location of the
	 * absolute maxima of the 2nd derivative of the activation function (LeCun -
	 * Efficient Backprop). Use the scaleDesiredOutputsForClassificationProblem
	 * method in order to do this.
	 * 
	 * The function first detects the largest and smallest output values, which
	 * are considered the two target values for binary classification. Then, all
	 * desired outputs are shifted in order to place both values symmetrically
	 * around the origin. In a last step, the outputs are scaled in order to
	 * place them directly on the absolute location of the absolute maxima of
	 * the given activity function's second derivative.
	 * 
	 * Effort: O(NUMBEROFSAMPLES*DESIREDOUTPUTDIMENSIONALITY).
	 * 
	 * @param function
	 *            to take the absolute location of the absolute maxima of the
	 *            git's second derivative.
	 */
	public void optimizeDesiredOutputsForClassificationProblem(
			NeuronBehavior function) {

		optimizeDesiredOutputsForClassificationProblem(function
				.getAbsoluteMaximumLocationOfSecondDerivative());

	}

	/**
	 * 
	 * If teaching classification problems you should consider placing the
	 * binary values at -x and x, where x is the absolute location of the
	 * absolute maxima of the 2nd derivative of the activation function (LeCun -
	 * Efficient Backprop). Use the scaleDesiredOutputsForClassificationProblem
	 * method in order to do this.
	 * 
	 * The function first detects the largest and smallest output values, which
	 * are considered the two target values for binary classification. Then, all
	 * desired outputs are shifted in order to place both values symmetrically
	 * around the origin. In a last step, the outputs are scaled in order to
	 * place them directly on the absolute location of the absolute maxima of
	 * the given network's output activity function's second derivative.
	 * 
	 * Effort: O(NUMBEROFSAMPLES*DESIREDOUTPUTDIMENSIONALITY).
	 * 
	 * @param net
	 *            to take the output activity function from.
	 */
	public void optimizeDesiredOutputsForClassificationProblem(NeuralNetwork net) {

		optimizeDesiredOutputsForClassificationProblem(net.getDescriptor()
				.getNeuronBehaviorOutputNeurons()
				.getAbsoluteMaximumLocationOfSecondDerivative());

	}

	/**
	 * 
	 * If teaching classification problems you should consider placing the
	 * binary values at -x and x, where x is the absolute location of the
	 * absolute maxima of the 2nd derivative of the activation function (LeCun -
	 * Efficient Backprop). Use the scaleDesiredOutputsForClassificationProblem
	 * method in order to do this.
	 * 
	 * The function first detects the largest and smallest output values, which
	 * are considered the two target values for binary classification. Then, all
	 * desired outputs are shifted in order to place both values symmetrically
	 * around the origin. In a last step, the outputs are scaled in order to
	 * place them directly on the absolute locations represented by the given
	 * target, which may be the location of the absolute maxima of an activity
	 * function's second derivative.
	 * 
	 * Effort: O(NUMBEROFSAMPLES*DESIREDOUTPUTDIMENSIONALITY).
	 * 
	 * @param target
	 *            the target value of classification outputs.
	 */
	public void optimizeDesiredOutputsForClassificationProblem(double target) {

		double minValue = Double.MAX_VALUE;
		double maxValue = Double.MIN_VALUE;

		for (int i = 0; i < desiredOutputs.length; i++) {
			for (int j = 0; j < desiredOutputs[i].length; j++) {
				if (desiredOutputs[i][j] < minValue) {
					minValue = desiredOutputs[i][j];
				}
				if (desiredOutputs[i][j] > maxValue) {
					maxValue = desiredOutputs[i][j];
				}
			}
		}

		double offset = (minValue + maxValue) / 2.0;
		for (int i = 0; i < desiredOutputs.length; i++) {
			for (int j = 0; j < desiredOutputs[i].length; j++) {
				desiredOutputs[i][j] -= offset;
			}
		}

		minValue -= offset;
		maxValue -= offset;

		double factor = target / maxValue;

		for (int i = 0; i < desiredOutputs.length; i++) {
			for (int j = 0; j < desiredOutputs[i].length; j++) {
				desiredOutputs[i][j] *= factor;
			}
		}

	}

}

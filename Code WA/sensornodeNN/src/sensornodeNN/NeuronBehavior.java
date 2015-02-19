package sensornodeNN;



/**
 * 
 * Defines the interface for a neuron behavior, which denotes the generalization
 * of activity functions.
 * 
 * <p>There are two kinds of assigning behaviors to neurons: Neurons can just be
 * assigned a reference to a behavior, which saves memory and is okay for standard
 * state-less activation functions. However, there may be other desired neuron
 * behaviors that require neuron-dedicated internal data. In this case, every
 * neuron needs a separate, dedicated instance of the neuron behavior class.
 * Please implement the needsDedicatedInstancePerNeuron method in order to
 * define your kind of neuron behavior when you inherit from this interface.
 * 
 * @author David Kriesel / dkriesel.com
 * 
 */
public interface NeuronBehavior{
	/**
	 * @param x
	 *            usually the net input
	 * @return the activation value of the neuron behavior given x.
	 */
	public double computeActivation(double x);

	/**
	 * @param x
	 *            usually the net input
	 * @return the first derivative of the activation function at x or
	 *         Double.NaN if not applicable. Note that if you return Double.NaN,
	 *         gradient training will be faulty (return NaNs, too, possibly
	 *         destroying network functionality).
	 */
	public double computeDerivative(double x);

	
	/**
	 * @return a separate instance of this class, initialized with the same
	 *         parameters as this one.
	 */
	public NeuronBehavior getDedicatedInstance();

	/**
	 * Returns the absolute location of the absolute maxima of the second
	 * derivative. If for instance 1 is returned here, the maxima of the second
	 * derivative are located at +1 and -1. Only positive values should be
	 * returned. It is important to know the approximate location of those
	 * maxima because they define the range of desired outputs a network learns
	 * best with (see "Efficient Backprop", LeCun). Return Double.NaN if not
	 * applicable. This function is also used as parameter for randomization of
	 * weight initialization, however, if you return Double.NaN, it is supposed
	 * to be 1.0.
	 * 
	 * @return the absolute location of the maxima of the second derivative or
	 *         Double.NaN if not applicable.
	 */
	public double getAbsoluteMaximumLocationOfSecondDerivative();

	/**
	 * This method tells the neural net initialization mechanisms whether or not
	 * every neuron assigned this behavior should get a dedicated instance of
	 * it. This enables you to create neuron-dependent behavior parametrization,
	 * which is needed for example when designing dynamic neuron behaviors like
	 * leaky integrators or other functions that behave different at each
	 * individual neuron (for example location-based radial basis functions).
	 * 
	 * @return whether or not every neuron assigned this behavior should get a
	 *         dedicated instance of it
	 */
	public boolean needsDedicatedInstancePerNeuron();
}

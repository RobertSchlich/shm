package sensornodeNN;

//import java.io.Serializable;



/**
 * <b>Read and instantiate first</b> - A NeuralNetworkDescriptor object defines
 * a set of high-level general parameters that are used to create a possibly
 * large group of neural network instances; so first, you have to create a
 * NeuralNetworkDescriptor, then use it to create as many neural network
 * instances as you wish; moreover, some fundamental information about SNIPE
 * usage is given in the documentation of this class.
 * 
 * <h1>Fundamentals and Usage</h1>
 * 
 * To create a neural network, you first define a descriptor, and then create a
 * neural network using the descriptor. This layout is useful for the creation
 * of even large populations of networks, for instance when using population
 * based metaheuristics.
 * 
 * I suggest you to
 * <ol>
 * <li>define what layers your network(s) shall contain creating an int array as
 * described in the constructor documentation (or use one of the quick
 * constructor wrappers),
 * <li>then create a descriptor instance using the layer array,
 * <li>then use one of the quick functions starting with "setSettings" if you
 * want to use common network topologies,
 * <li>tweak some further descriptor settings (for example, neuron behaviors
 * e.g. activity functions) and
 * <li>create as many networks as you wish out of the descriptor (if you wish,
 * use one of the "create" methods of the descriptor itself).
 * </ol>
 * Once networks are created, settings in the descriptor can still be changed
 * via the setters (useful if you for example want to allow or deny synapse
 * types later on for all networks created using one descriptor). However, the
 * neurons per layer are directly defined using the constructor and do not have
 * a setter. They can't be changed.
 * 
 * <h1>Descriptors Outline Networks, they do not define them</h1>
 * 
 * Descriptors <i>outline</i> networks, but don't define the exact topology or
 * even the number of inner neurons or last, the synaptic weight values. The
 * neuron per layer array is a good example for this. With this array, you can
 * define neuron numbers per layer to be static or to be randomized every time a
 * network is generated from a descriptor. See the constructor documentation to
 * get used to this.
 * 
 * <p>
 * In addition to this, you are of course allowed to change anything according
 * to network instances which is allowed by the descriptor (for instance, add
 * and remove neurons and synapses, etc).
 * 
 * <h1>Some remarks and default descriptor values</h1>
 * 
 * First: Read the documentation of network Frequency in the setFrequency
 * method: It allows to define the propagation behavior of the network which may
 * save lots and lots of calculating time for example in large feed forward
 * networks (look for "fastprop") as well as other advantages. After reading
 * this, you will also understand what the remarks in the documentation of the
 * "setSettings" functions referring to the frequency mean.
 * 
 * <p>
 * Default neuron behaviors are plain activity functions: Identity at the input
 * layer and Tangens Hyperbolicus in hidden and output layers, use the
 * corresponding setters to change. By default, all synapse classes are allowed.
 * 
 * <p>
 * Max and min layer neuron numbers are initialized with 0, all synapse classes
 * will be allowed at the beginning.
 * 
 * <p>
 * You can also use the setInitializeAllowedSynapses setter to control wether
 * all allowed synapses are created and initialize randomly in newly created
 * networks dependent from this descriptor.
 * 
 * <p>
 * The synapseInitialRange value is 0.4 by default, which means that new
 * synaptic weight values are chosen uniformly random out of [-0.4;0.4].
 * 
 * <h1>Changing settings in a descriptor after it has already been used to
 * create networks</h1>
 * 
 * If you change the allowed synapse types after networks have already been
 * created using a descriptor, you might want to invoke the
 * removeSynapsesNotAllowed() method of those networks. It removes synapses,
 * that exist but are not allowed by the descriptor.
 * 
 * <p>
 * If you change the neuron behaviors after networks have already been created
 * using a descriptor, you might want to invoke the setNeuronBehaviors() method
 * of those networks. It retrieves the neuron behaviors from the descriptor and
 * assigns them to neurons.
 * 
 * @version 0.8
 * 
 * @author David Kriesel / dkriesel.com
 */

public class NeuralNetworkDescriptor{// implements Serializable{


	private static final long serialVersionUID = 1L;
	
	// neural net standard parameters
	private int[] neuronsPerLayer;
	private int minLayerNeuronNumber = 0;
	private int maxLayerNeuronNumber = 0;
	private boolean initializeAllowedSynapses = true;
	private boolean allowForwardShortcutSynapses = true;
	private boolean allowForwardSynapses = true;
	private boolean allowBackwardSynapses = true;
	private boolean allowBackwardShortcutSynapses = true;
	private boolean allowSelfSynapses = true;
	private boolean allowLateralSynapses = true;
	private double synapseInitialRange = 0.4;

	/**
	 * The maximum absolute a synapse will be initialized with. Default is 0.4,
	 * which means that a synapse will be initialized at random with values in
	 * the range [-0.4;0.4].
	 * 
	 * @return the synapseInitialRange
	 */
	public double getSynapseInitialRange() {
		return synapseInitialRange;
	}

	/**
	 * The maximum absolute a synapse will be initialized with. Default is 0.4,
	 * which means that a synapse will be initialized at random with values in
	 * the range [-0.4;0.4].
	 * 
	 * @param synapseInitialRange
	 *            the synapseInitialRange to set
	 */
	public void setSynapseInitialRange(double synapseInitialRange) {
		this.synapseInitialRange = synapseInitialRange;
	}

	private NeuronBehavior inputNeuronsNeuronBehavior = new Identity();
	private NeuronBehavior hiddenNeuronsNeuronBehavior = new  Identity();
	private NeuronBehavior outputNeuronsNeuronBehavior = new Identity();
	private int frequency = 1;

	/**
	 * Creates a new neuralNetworkDescriptor according to a neuronsPerLayer
	 * integer array. Such an array contains one integer per layer to create in
	 * a neural net.
	 * 
	 * <p>
	 * Index 0 of the array represents the input layer, the last index
	 * represents the output layer. If any layer is given the neuron number -1,
	 * it denotes that the number will to be chosen randomly between min- and
	 * maxLayerNeuronNumber once a neural net is created using this descriptor.
	 * 
	 * <p>
	 * This choice will be made during the constructor of any neural net
	 * generated from this neuronsPerLayer array. This layout is chosen because
	 * every neural net has a separate random number generator, in order to work
	 * more thread safe, so I avoided placing additional random number
	 * generators in the descriptors.
	 * 
	 * <p>
	 * A small example: An Array containing {3;7;-1;8} would create networks
	 * with 3 input neurons, 8 output neurons and two hidden layers. The hidden
	 * layer closer to the input layer would contain exactly 7 neurons, the
	 * neuron number of the one closer to the output layer would be chosen by
	 * random every time a new network is created.
	 * 
	 * 
	 * @param neuronsPerLayer
	 * 
	 */
	public NeuralNetworkDescriptor(int[] neuronsPerLayer) {
		setNeuronsPerLayer(neuronsPerLayer);
	}

	/**
	 * Just wraps the main Constructor NeuralNetworkDescriptor(int[]
	 * neuronsPerLayer) for your convenience, so you don't have to use an array
	 * when creating a neural network with 2 layers.
	 * 
	 * <p>
	 * Please read the documentation of the main constructor for further
	 * information.
	 * 
	 * @param inputLayer
	 * @param outputLayer
	 */
	public NeuralNetworkDescriptor(int inputLayer, int outputLayer) {
		int[] array = new int[2];
		array[0] = inputLayer;
		array[1] = outputLayer;
		setNeuronsPerLayer(array);
	}

	/**
	 * Just wraps the main Constructor NeuralNetworkDescriptor(int[]
	 * neuronsPerLayer) for your convenience, so you don't have to use an array
	 * when creating a neural network with 3 layers.
	 * 
	 * <p>
	 * Please read the documentation of the main constructor for further
	 * information.
	 * 
	 * @param inputLayer
	 * @param hiddenLayer
	 * @param outputLayer
	 */
	public NeuralNetworkDescriptor(int inputLayer, int hiddenLayer,
			int outputLayer) {
		int[] array = new int[3];
		array[0] = inputLayer;
		array[1] = hiddenLayer;
		array[2] = outputLayer;
		setNeuronsPerLayer(array);
	}

	/**
	 * Just wraps the main Constructor NeuralNetworkDescriptor(int[]
	 * neuronsPerLayer) for your convenience, so you don't have to use an array
	 * when creating a neural network with 4 layers.
	 * 
	 * <p>
	 * Please read the documentation of the main constructor for further
	 * information.
	 * 
	 * @param inputLayer
	 * @param hiddenLayer1
	 * @param hiddenLayer2
	 * @param outputLayer
	 */
	public NeuralNetworkDescriptor(int inputLayer, int hiddenLayer1,
			int hiddenLayer2, int outputLayer) {
		int[] array = new int[4];
		array[0] = inputLayer;
		array[1] = hiddenLayer1;
		array[2] = hiddenLayer2;
		array[3] = outputLayer;
		setNeuronsPerLayer(array);
	}

	/**
	 * Just wraps the main Constructor NeuralNetworkDescriptor(int[]
	 * neuronsPerLayer) for your convenience, so you don't have to use an array
	 * when creating a neural network with 5 layers.
	 * 
	 * <p>
	 * Please read the documentation of the main constructor for further
	 * information.
	 * 
	 * @param inputLayer
	 * @param hiddenLayer1
	 * @param hiddenLayer2
	 * @param hiddenLayer3
	 * @param outputLayer
	 */
	public NeuralNetworkDescriptor(int inputLayer, int hiddenLayer1,
			int hiddenLayer2, int hiddenLayer3, int outputLayer) {
		int[] array = new int[5];
		array[0] = inputLayer;
		array[1] = hiddenLayer1;
		array[2] = hiddenLayer2;
		array[3] = hiddenLayer3;
		array[4] = outputLayer;
		setNeuronsPerLayer(array);
	}

	/**
	 * Creates a new Neural Network created using this descriptor. Same as
	 * invoking new NeuralNetwork(descriptor).
	 * 
	 * @return a new Neuralnetwork
	 */
	public NeuralNetwork createNeuralNetwork() {
		return new NeuralNetwork(this);
	}

	/**
	 * Like createNeuralNetwork, but creates n Networks and returns them in an
	 * array.
	 * 
	 * @param n
	 * @return an array of n neural Networks.
	 */
	public NeuralNetwork[] createNeuralNetworks(int n) {
		NeuralNetwork[] result = new NeuralNetwork[n];
		for (int i = 0; i < result.length; i++) {
			result[i] = new NeuralNetwork(this);
		}
		return result;
	}

	/**
	 * @return the allowForwardShortcutSynapses
	 */
	public boolean isAllowForwardShortcutSynapses() {
		return allowForwardShortcutSynapses;
	}

	/**
	 * @param allowForwardShortcutSynapses
	 *            the allowForwardShortcutSynapses to set
	 */
	public void setAllowForwardShortcutSynapses(
			boolean allowForwardShortcutSynapses) {
		this.allowForwardShortcutSynapses = allowForwardShortcutSynapses;
	}

	/**
	 * @return the allowForwardSynapses
	 */
	public boolean isAllowForwardSynapses() {
		return allowForwardSynapses;
	}

	/**
	 * @param allowForwardSynapses
	 *            the allowForwardSynapses to set
	 */
	public void setAllowForwardSynapses(boolean allowForwardSynapses) {
		this.allowForwardSynapses = allowForwardSynapses;
	}

	/**
	 * @return the allowBackwardSynapses
	 */
	public boolean isAllowBackwardSynapses() {
		return allowBackwardSynapses;
	}

	/**
	 * @param allowBackwardSynapses
	 *            the allowBackwardSynapses to set
	 */
	public void setAllowBackwardSynapses(boolean allowBackwardSynapses) {
		this.allowBackwardSynapses = allowBackwardSynapses;
	}

	/**
	 * @return the allowBackwardShortcutSynapses
	 */
	public boolean isAllowBackwardShortcutSynapses() {
		return allowBackwardShortcutSynapses;
	}

	/**
	 * @param allowBackwardShortcutSynapses
	 *            the allowBackwardShortcutSynapses to set
	 */
	public void setAllowBackwardShortcutSynapses(
			boolean allowBackwardShortcutSynapses) {
		this.allowBackwardShortcutSynapses = allowBackwardShortcutSynapses;
	}

	/**
	 * Allows only forward synapses. Fastprop is activated.
	 */
	public void setSettingsTopologyFeedForward() {
		allowForwardShortcutSynapses = false;
		allowForwardSynapses = true;
		allowBackwardSynapses = false;
		allowBackwardShortcutSynapses = false;
		allowSelfSynapses = false;
		allowLateralSynapses = false;
		setFrequency(0);
	}

	/**
	 * Allows only forward synapses and forward shortcut synapses. Fastprop is
	 * activated.
	 */
	public void setSettingsTopologyFeedForwardWithShortcuts() {
		allowForwardShortcutSynapses = true;
		allowForwardSynapses = true;
		allowBackwardSynapses = false;
		allowBackwardShortcutSynapses = false;
		allowSelfSynapses = false;
		allowLateralSynapses = false;
		setFrequency(0);
	}

	/**
	 * Allows every possible connection (those towards bias neuron and input
	 * neurons, however, remain impossible).
	 */
	public void setSettingsTopologyCompleteConnection() {
		allowForwardShortcutSynapses = true;
		allowForwardSynapses = true;
		allowBackwardSynapses = true;
		allowBackwardShortcutSynapses = true;
		allowSelfSynapses = true;
		allowLateralSynapses = true;
	}

	/**
	 * Allows every possible connection but no self connections (those towards
	 * bias neuron and input neurons, however, remain impossible).
	 */
	public void setSettingsTopologyHopfield() {
		allowForwardShortcutSynapses = true;
		allowForwardSynapses = true;
		allowBackwardSynapses = true;
		allowBackwardShortcutSynapses = true;
		allowSelfSynapses = false;
		allowLateralSynapses = true;
	}

	/**
	 * @return the allowSelfSynapses
	 */
	public boolean isAllowSelfSynapses() {
		return allowSelfSynapses;
	}

	/**
	 * @param allowSelfSynapses
	 *            the allowSelfSynapses to set
	 */
	public void setAllowSelfSynapses(boolean allowSelfSynapses) {
		this.allowSelfSynapses = allowSelfSynapses;
	}

	/**
	 * @return the allowLateralSynapses
	 */
	public boolean isAllowLateralSynapses() {
		return allowLateralSynapses;
	}

	/**
	 * @param allowLateralSynapses
	 *            the allowLateralSynapses to set
	 */
	public void setAllowLateralSynapses(boolean allowLateralSynapses) {
		this.allowLateralSynapses = allowLateralSynapses;
	}

	/**
	 * @return the frequency. Frequency 0 denotes the fast propagation mode for
	 *         feed forward networks.
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * 
	 * There are two methods of propagation: the normal mode and the fast mode.
	 * Which mode is taken, is defined by the frequency.
	 * 
	 * <p>
	 * In normal mode (which is denoted by any frequency-number greater or equal
	 * 1), all neurons first calculate their net input collecting data from
	 * their incoming connections. Afterwards, they calculate their activation
	 * values by propagating their net input through their activation function.
	 * So virtually, all neurons change states in parallel, network time steps
	 * do not overlap, which is called "synchronous update". They do so the a
	 * number of times equal to the frequency. So, a frequency of 10 makes the
	 * network propagate the data 10 times. A frequency of 1 is the default
	 * value. This is the natural way for generalized recurrent networks.
	 * However, for example, a feed forward network with three weight layers
	 * would need a frequency of 3 in order propagate data through the entire
	 * network. In such cases, fastprop saves a lot of time.
	 * 
	 * <p>
	 * In the fastprop mode (which is denoted by frequency 0) activations are
	 * directly updated, along with the net inputs which causes the activation
	 * order of the neurons to be relevant. Any neuron whose activation is
	 * calculated will immediately use the new activation values of neurons with
	 * indices smaller (asynchronous update). For feedforward networks, the
	 * propagation effort is divided by the number of synapse layers while
	 * getting the same results. For networks with recurrent connections
	 * allowed, this propagation mode will throw exceptions.
	 * 
	 * 
	 * @param frequency
	 *            the frequency to set.
	 */
	public void setFrequency(int frequency) {
		if (frequency < 0) {
			throw new IllegalArgumentException(
					"Freqency must be 0 for fastprop, or greater zero.");
		}
		this.frequency = frequency;
	}

	/**
	 * @return the numberOfInputNeurons
	 */
	public int countInputNeurons() {
		return neuronsPerLayer[0];
	}

	/**
	 * @return the numberOfOutputNeurons
	 */
	public int countOutputNeurons() {
		return neuronsPerLayer[neuronsPerLayer.length - 1];
	}

	/**
	 * @return the inputNeuronsNeuronBehavior
	 */
	public NeuronBehavior getNeuronBehaviorInputNeurons() {
		return inputNeuronsNeuronBehavior;
	}

	/**
	 * @param inputNeuronsNeuronBehavior
	 *            the inputNeuronsNeuronBehavior to set
	 */
	public void setNeuronBehaviorInputNeurons(
			NeuronBehavior inputNeuronsNeuronBehavior) {
		this.inputNeuronsNeuronBehavior = inputNeuronsNeuronBehavior;
	}

	/**
	 * @return the hiddenNeuronsNeuronBehavior
	 */
	public NeuronBehavior getNeuronBehaviorHiddenNeurons() {
		return hiddenNeuronsNeuronBehavior;
	}

	/**
	 * @param hiddenNeuronsNeuronBehavior
	 *            the hiddenNeuronsNeuronBehavior to set
	 */
	public void setNeuronBehaviorHiddenNeurons(
			NeuronBehavior hiddenNeuronsNeuronBehavior) {
		this.hiddenNeuronsNeuronBehavior = hiddenNeuronsNeuronBehavior;
	}

	/**
	 * @return the outputNeuronsNeuronBehavior
	 */
	public NeuronBehavior getNeuronBehaviorOutputNeurons() {
		return outputNeuronsNeuronBehavior;
	}

	/**
	 * @param outputNeuronsNeuronBehavior
	 *            the outputNeuronsNeuronBehavior to set
	 */
	public void setNeuronBehaviorOutputNeurons(
			NeuronBehavior outputNeuronsNeuronBehavior) {
		this.outputNeuronsNeuronBehavior = outputNeuronsNeuronBehavior;
	}

	/**
	 * @return the number of layers including input and output layers.
	 */
	public int countLayers() {
		return neuronsPerLayer.length;
	}

	/**
	 * @return the neuronsPerLayer (cloned). Layer neuron numbers declared to be
	 *         randomized by -1 will be given out as -1.
	 */
	public int[] getNeuronsPerLayer() {
		return (int[])neuronsPerLayer;//.clone();
	}

	private void setNeuronsPerLayer(int[] neuronsPerLayer) {
		if (neuronsPerLayer.length < 2) {
			throw new IllegalArgumentException(
					"There must be at least 2 layers.");
		}
		if (neuronsPerLayer[0] < 1) {
			throw new IllegalArgumentException(
					"There must be at least 1 input neuron.");
		}
		if (neuronsPerLayer[neuronsPerLayer.length - 1] < 1) {
			throw new IllegalArgumentException(
					"There must be at least 1 output neuron.");
		}

		for (int i = 0; i < neuronsPerLayer.length; i++) {
			if (neuronsPerLayer[i] < -1) {
				throw new IllegalArgumentException(
						"Illegal neuron number definition in layer " + i + ".");
			}
		}
		this.neuronsPerLayer = (int[])neuronsPerLayer;//.clone();
	}

	/**
	 * @return the initializeAllowedConnections
	 */
	public boolean isInitializeAllowedSynapses() {
		return initializeAllowedSynapses;
	}

	/**
	 * @return the minLayerNeuronNumber
	 */
	public int getMinLayerNeuronNumber() {
		return minLayerNeuronNumber;
	}

	/**
	 * @param minLayerNeuronNumber
	 *            the minLayerNeuronNumber to set
	 */
	public void setMinLayerNeuronNumber(int minLayerNeuronNumber) {
		if (minLayerNeuronNumber < 0) {
			throw new IllegalArgumentException(
					"minLayerNeuronNumber must be 0 or greater.");
		}
		this.minLayerNeuronNumber = minLayerNeuronNumber;
	}

	/**
	 * @return the maxLayerNeuronNumber
	 */
	public int getMaxLayerNeuronNumber() {
		return maxLayerNeuronNumber;
	}

	/**
	 * @param maxLayerNeuronNumber
	 *            the maxLayerNeuronNumber to set
	 */
	public void setMaxLayerNeuronNumber(int maxLayerNeuronNumber) {
		if (maxLayerNeuronNumber < 0) {
			throw new IllegalArgumentException(
					"maxLayerNeuronNumber must be 0 or greater.");
		}
		this.maxLayerNeuronNumber = maxLayerNeuronNumber;
	}

	/**
	 * @param initializeAllowedSynapses
	 *            the initializeAllowedSynapses to set
	 */
	public void setInitializeAllowedSynapses(boolean initializeAllowedSynapses) {
		this.initializeAllowedSynapses = initializeAllowedSynapses;
	}

	// /**
	// * In order to use the maximum power of GNARL (which you may restrict
	// later
	// * on), the topology is set to complete connection and the init of all
	// * allowed synapses is set to false.
	// */
	// public void setSettingsGNARL() {
	// initializeAllowedSynapses = false;
	// setSettingsTopologyCompleteConnection();
	// }
	//
	// /**
	// * In order to use the maximum power of NEAT (which you may restrict later
	// * on), the topology is set to complete connection and the init of all
	// * allowed synapses is set to false.
	// */
	// public void setSettingsNEAT() {
	// initializeAllowedSynapses = false;
	// setSettingsTopologyCompleteConnection();
	// }
	//
	// /**
	// * In order to use the maximum power of EPNet (which you may restrict
	// later
	// * on), the topology is set to feed forward with shortcuts, and the init
	// of
	// * all allowed synapses is set to false. Fastprop is activated.
	// */
	// public void setSettingsEPNet() {
	// initializeAllowedSynapses = false;
	// setSettingsTopologyFeedForwardWithShortcuts();
	// setFrequency(0);
	// }

}

package sensornodeNN;

//import java.io.Serializable;
import java.util.Hashtable;
import java.util.Random;

import com.sun.squawk.util.Arrays;
import com.sun.squawk.util.MathUtils;

/**
 * <b>Instantiated second using a NeuralNetworkDescriptor instance,</b> this
 * class represents the core functionality of SNIPE: A neural network of
 * arbitrary topology with a lightweight and calculation efficient data
 * structure, the possibility to change the entire network topology as well as
 * the synaptic weights and many other features; In the documentation of this
 * class, detail information about the efficient data structure of the neural
 * network is given, as well as information about its features. Goals of the
 * design include:
 * 
 * <ol>
 * <li><b>Generalized data structure for arbitrary network topologies</b>, so
 * that virtually all network structures can be realized or even easily
 * hand-crafted.
 * <li><b>Built-In, fast and easy-to-use learning operators</b> for gradient
 * descent or evolutionary learning, as well as mechanisms for efficent control
 * of even large network populations.
 * <li><b>Mechanisms for design and control of even large populations of neural
 * networks</b>
 * <li><b>Optimal speed neural network data propagation</b> in contrast to naive
 * data structures, even in special cases like multi layer perceptrons and
 * sparse networks, as well as low computational topology editing effort.
 * <li><b>Low memory consumption</b> - grows only with the number of existing
 * synapses, not quadratically with the number of neurons
 * <li><b>In-situ processing</b> - no extra memory or preprocessing of the data
 * structure is necessary in order to use the network after editing
 * <li><b>Usage of only low level data structures (arrays)</b> for easy
 * portability. It is not the goal to quench the last tiniest bit of
 * asymptotical complexity out of the structure, but to make it usable, light
 * weight and fast in praxis.
 * <li><b>No object-oriented overload</b>, like objects for every neuron or even
 * synapses, etc.
 * </ol>
 * 
 * 
 * 
 * <h1>Fundamentals</h1>
 * 
 * 
 * 
 * <p>
 * The Neuron enumeration structure is defined as follows: Neuron 0 is the bias
 * neuron, at least neuron 1 is an input neuron which may be followed by other
 * input neurons, depending on the input dimensionality. The most important
 * information considering the neuron index set is, that it is partitioned in
 * layers. Neuron 0 is the bias neuron (no layer). From neuron index 1 on, all
 * input neurons (layer 0) are enumerated, after that layer 1,2,... and so on.
 * The last layer is the output layer. Neurons can be inserted and removed from
 * layers. The number of layers per neural net, however, is static once the net
 * is initialized.
 * 
 * <p>
 * The Neural Network can be parametrized in many ways (neurons, synapses,
 * Activation functions, propagation mode, and more) via NeuralNetworkDescriptor
 * instances. To create a neural network, you first have to define a descriptor,
 * and then create a neural network using the descriptor. Each Networks
 * fundamental features are defined by one single descriptor instance, however,
 * a descriptor can define many networks' fundamental features. This layout is
 * useful for controlling even large populations of networks (which this
 * framework allows for). Just read through the descriptor and neuralnetwork
 * documentation to get used to the possibilities and operators. For example,
 * using the respective methods in the descriptor, you can define if <i>synapse
 * classes</i> (for example: forward shortcut synapses) of synapses are allowed
 * for usage in your network. Some synapses are _never_ allowed, for example
 * those towards the input layer or bias neuron. Have a look at the method
 * setSynapse if you want to create particular synapses in your neural network.
 * Only synapses that exist will be used for calculating outputs, errors, and
 * such.
 * 
 * <p>
 * The Neural Networks efficient calculation and lightweight storage complexity
 * will be analyzed thoroughly later in the text.
 * 
 * <p>
 * In addition, the neural network has an output method for GraphViz code
 * (http://www.graphviz.org) in order to visualize.
 * 
 * <p>
 * Method names are organized with prefixes and additional succeeding
 * information as follows. Methods with prefix <i>clear</i> are used to clear
 * some caches that may used by training algorithms or evolutionary operators.
 * Methods with prefix <i>count</i> return numbers of different items in the
 * network, like countNeurons or countSynapses. Methods with prefix
 * <i>create</i> add some structure to the Network or parse the network
 * structure from a string (except for synapse, you can create them using
 * setSynapse, which either changes the synaptic weight of an existing synapse
 * or creates a new one with the given weight). Getter and Setter methods work
 * as usual and set and return several different data, like synaptic weight
 * values and neuron indices. <i>is</i>-Methods check for the boolean values of
 * different statements, for example whether or not a synapse is allowed.
 * Methods with prefix <i>mutate</i> perform different kinds of mutation on the
 * neural network topology or synaptic weight values that cannot easily be done
 * with an invocation of existing methods. For example, adding random non
 * existent synapse can easily be done by invoking first
 * getSynapseNonExistentAllowedRandom and then setSynapse, therefore there is no
 * delete items from the network topology. The prefix <i>train</i> marks methods
 * that implement some common training algorithms. The method propagate
 * propagates data through the net.
 * 
 * <p>
 * Synapses created newly using different operators will be assigned a random
 * value dependent on the synapseInitialRange defined in the descriptor.
 * 
 * <p>
 * If you read through this javadoc and all regarding methods once, you know
 * what can be done with the network.
 * 
 * 
 * 
 * 
 * <h1>Implementation of Lightweight Data Structure</h1>
 * 
 * <p>
 * I present a space- and time efficient, light-weight data structure for
 * generalized neural networks enabling fast data propagation and arbitrary
 * topological changes. The data structure internally only makes use of arrays.
 * It doesn't use any high level dynamic data structures. I am aware of the fact
 * that some (few) asymptotically complexity classes could be outperformed by
 * using structures like hashmaps, but they would in my opinion add a
 * significant multiplicative constant to the performance classes and be
 * obsolete in reality.
 * 
 * 
 * <h2>Data Structure Assembly</h2>
 * 
 * 
 * Let SYNAPSES be the number of synapses and NEURONS the number of neurons in
 * the following paragraphs. Nice to know: In the following speed calculations,
 * non-existent synapses do not cause the tiniest bit of computational or
 * storage effort. All data is kept in native arrays, for wrapping native java
 * types like int and double for storage in e.g. array lists would be pretty
 * slow.
 * 
 * <p>
 * The entire data structure is made of four 2D arrays (one sub-array in every
 * array for each neuron): "predecessors", "successors", "predecessorWeights"
 * and "successorWeightIndexInPendantPredecessorArray" (yeah, long name, huh? I
 * like speaking names). The 2D integer arrays "predecessors" and "successors"
 * store the neuron indices of all predecessors and successors <i>in ascending
 * order</i>. The 2D double Array "predecessorWeights" stores the synaptic
 * weights from each neuron's incoming synapse to its source neuron ordered by
 * ascending predecessor index as well. This drives data propagation through the
 * net to the maximum possible speed: Only incoming synapses are interesting in
 * this case and you don't have to search for synapse weights like for example
 * in a naive synapse list implementation.
 * 
 * <p>
 * For any Neuron N and any Successor Neuron S, N's
 * "successorWeightIndexInPendantPredecessorArray" sub-array, also ordered by
 * ascending successor neuron indices, saves the index of the S's
 * predecessorWeights sub-array, where the synaptic weight value is stored. This
 * seems a bit complicated, but gives us the advantage of storing weights only
 * once so we also have to update them only once. This saves search time in many
 * cases respectively makes searching obsolete at all.
 * 
 * <p>
 * To get you used to the Data structure, I now describe how basic operations
 * are performed. In the following description, Capitals (like N) represent
 * Neurons. Neuron-related Expressions represent sets of Synapses: N_in
 * represents the number of all incoming synapses of Neuron N, N_out analogously
 * the number of all outgoing ones. Both N_in and N_out can be as large as
 * NEURONS in worst case.
 * 
 * <p>
 * Note again, that high-level data structures are intentionally not used. Of
 * course, we could squeeze the last bit of asymptotic speed out of the network
 * by using hashmaps etc - but we would decrease propagation speed: Even though
 * the propagation complexity (for instance) would stay the same, its
 * multiplicative constant would be higher because of the data structure
 * overhead.
 * 
 * <p>
 * The "old data structure" I will sometimes refer to consists of a weight
 * matrix and a synapse existence matrix (storage in O(NEURONS^2)). You need a
 * matrix like this for the naive storage of neural networks in a such
 * generalized way as we do here. Double values are used for synapses (8 byte
 * per double) and boolean values for synapse existances in this data structure
 * (1 byte per boolean).
 * 
 * 
 * <h2>Storage Size</h2>
 * 
 * 
 * Old data stucture: 9*NEURONS^2 bytes. Lightweight structure: 20*SYNAPSES.
 * This means that if 45% of the possible synapses (including the illegal ones
 * for example towards the input layer or bias) are existent, the lightweight
 * data structure has the same size in memory like the old one. This is a pretty
 * rare case.
 * 
 * 
 * <h2>Common Operations are Performed at Maximum Speed Possible</h2>
 * 
 * <p>
 * <b>Reading incoming data per neuron N with N_in incoming synapses:</b>
 * O(N_in). The Indices of Neurons sending to N are stored in predecessors[N]
 * and can therefore be read straight forward with computational effort of
 * O(N_in). This is the most important action, as it is performed pretty often
 * during the usage of the Neural Net. It can't be performed faster, since every
 * incoming synaptic weight must be taken into account. The old matrix data
 * structure caused O(NEURONS) of computational effort.
 * 
 * <p>
 * <b>Backpropagating per neuron N with N_out outgoing synapses:</b> O(N_out).
 * Basically the same principle, but with a little higher multiplicative
 * constant for resolving the real weight out of the index (you have to look in
 * two arrays instead of one). It can't be performed faster, since every
 * outgoing synaptic weight must be taken into account. The old matrix data
 * structure caused O(NEURONS) of computational effort.
 * 
 * <p>
 * <b>Batch reading and writing all weights (for example for training):</b>
 * O(SYNAPSES). You can go through all weights in a linear way if you do it
 * either in ascending or descending order within the neuron-based adjacency
 * arrays. It can't be performed faster, since every synaptic weight must be
 * taken into account. The old matrix data structure caused O(NEURONS^2) of
 * computational effort, however had the advantage to allow random access. We
 * will get to this aspect later.
 * 
 * 
 * 
 * <h2>Other Operations and Structural Changes are Performed Still Fast</h2>
 * 
 * <p>
 * <b>Changing a single weight of a synapse from I to J (random access):</b>
 * O(log(MIN(I_out,J_in)))). Done by choosing the smaller array of I_out and
 * J_in, performing binary search on it, then resolving and changing the weight
 * value. In both cases the weight can be changed with constant computational
 * effort. The old matrix data structure caused O(1) of computational effort,
 * because just one double in the synapse weight matrix had to be resolved and
 * altered, so no search is needed.
 * 
 * <p>
 * <b>Checking synapse existence / retrieving weight of Synapse from I to J
 * (random access): </b> O(log(MIN(I_out,J_in)))). In both cases the weight can
 * be retrieved with computational effort analogous to weight change. The old
 * matrix data structure caused O(1) of computational effort, because one bit in
 * the synapse existence matrix just had to be read or a weight cell had to be
 * read.
 * 
 * <p>
 * <b>Add particular synapse from I to J:</b> Check if exists. If it does, see
 * random access weight change, and you're done. The interesting case is, if it
 * doesn't. Them, we have to rewrite successorarrays for I and predecessorarrays
 * for J O(I_out+J_in). The altering of predecessorarrays is easy: Just add the
 * cell hat represents the synapse at the rightfull (=sorted) place into the
 * arrays while rewriting. Altering the successorarrays is similar: Just add
 * cells for the synapse to the arrays. Once this is done, you're done with the
 * altering regarding the data structures of neurons I and J. But now, here's a
 * hazzle: We altered the predecessorarray of neuron j which other
 * successorarrays might point to in order to read synaptic weights. Those
 * pointers have to be updated, too. In particular implementation, they should
 * to be updated before the predecessorarray is, which avoids some neat
 * programming errors you can encounter. To do this, just go through the
 * predecessor-array-entries <i>behind</i> the place where the new synapse will
 * be filled in. Those entries are all shifted by one cell to the ascending
 * direction, meaning that their indices will all be increased by one. So you
 * need to increase all integers in all
 * successorWeightIndexInPendantPredecessorArrays that point to an weight index
 * </>behind</i> the insertion point (read twice to get this, but once you're
 * done you understood the data structure). So, for every predecessor of J with
 * neuron index equal or larger than I's, the successor weight index of the
 * synapse pointing to J has to be incremented. In some (rare!) cases, There
 * could be O(NEURONS) predecessors of J with a equal or larger index than I,
 * for which the search has to be applied. So the worst case of computational
 * effort of the entire operation would be O(NEURONS*log(NEURONS)). Obviously,
 * J_in will usually be a lot smaller than NEURONS and most likely, we won't
 * insert the new synapse in the very first cell of the predecessorArrays, so
 * not every single pointer to the following cells needs to be updated. The old
 * matrix data structure caused O(1) of computational effort, because just one
 * boolean in the synapse existence matrix had to be flipped.
 * 
 * <p>
 * <b>Remove synapse from I to J:</b> First: Existence check as above. If
 * synapse doesn't exist, you're done. If it does, rewrite successorarray for I
 * and predecessorarray for J: O(I_out+J_in). After this, update the successor
 * arrays of all neurons with index >= I that have J as an successor (all
 * predecessors from J), because J's predecessor array, that stores the
 * regarding weights, is decremented from position I on, like above, but vice
 * versa. Computational effort of this: O(J_in times searching in the regarding
 * predecessor index arrays), worst case each O(NEURONS*log(NEURONS)). As you
 * can see, the effort is the same as adding a synapse. The old matrix data
 * structure caused O(1) of computational effort, because one bit in the synapse
 * existence matrix just had to be flipped.
 * 
 * <p>
 * <b>Add a single neuron at index N:</b> Increase Netinputs/Activations arrays
 * that are used for processing: O(NEURONS). Enlarge Data Structure (one
 * dimension of 2D-arrays): O(NEURONS). Since just references of sub-arrays are
 * copied, this computational effort is virtually constant. Increment neuron
 * indices in data structure, worst case O(SYNAPSES) if all existing synapses
 * are incident to neurons whose index is larger than N, usually much lower. So:
 * O(NEURONS+SYNAPSES) worst case. The old matrix data structure would cause an
 * computational effort of O(NEURONS^2) (a line and a column in the large 2D
 * matrices would have to be added).
 * 
 * <p>
 * <b>Remove Neuron N (random access):</b> Remove all Synapses incident to
 * neuron (computational effort see above), shrink Data Structure O(NEURONS).
 * Since just references of sub-arrays are copied, this computational effort is
 * virtually constant. Decrement neuron indices in data structure, worst case
 * O(SYNAPSES) if all existing synapses are incident to neurons with index
 * larger than N, usually much lower. So, worst case O(SYNAPSES+NEURONS) again.
 * The old matrix data structure, like above, caused O(NEURONS^2) of
 * computational effort, because the entire matrices had to be rewritten
 * removing a line and a column.
 * 
 * 
 * 
 * 
 * <h1>Built-In Gradient-Descent Strategies for Synaptic Weight Modification.</h1>
 * 
 * Those Strategies work using a training sample lesson (see the respective
 * class documentation, it's pretty easy). For parametrization of the training
 * methods, see the respective method documentation.
 * 
 * <p>
 * <b> Backpropagation of Error [MR86]:</b> Implemented in the
 * trainBackpropagationOfError() Method. It trains online with a random sample
 * order.
 * 
 * <p>
 * <b>Resilient Backpropagation [RB94]:</b> Implemented in the
 * trainResilientBackpropagation() method. Trains, of course, offline for it
 * needs stable gradients. Aditionally, you may decide whether to use the
 * improvements of ResilientPropagation published in [Igel2003], which will
 * increase the iteration time but may (not: must) yield better results. Thanks
 * go to Martin Westhoven for bugfixing Rprop!
 * 
 * 
 * 
 * <h1>Built-In Evolution Strategy Operators on Synaptic Weights and Topology</h1>
 * 
 * This class features several evolutionary operators, both altering its
 * synaptic weights and topology. Some of those operators need additional memory
 * in O(SYNAPSES) for storing for example informations that define how to
 * perturbate every weight when the next mutation step is invoked. Those
 * additional data structures with data points defined on every weight are built
 * similar to the ones storing the weights and topology and are automatically
 * size-maintained if topological changes are performed, for example if neurons
 * or synapses are added. They are initialized at the point of time the
 * respective operator is invoked the first time and independent from each
 * other, so you can mutate the net with several mutation operators without
 * losing any operator data storage. They are also cloned if the neuralnet is
 * cloned, for they may hold data important for next evolution generations. You
 * can discard this information for memory reasons invoking the clearCache
 * methods. All gaussian random number processes are done by an unsynchronized
 * and tuned mersenne twister random number generator provided by the great
 * evolutionary framework ECJ (http://cs.gmu.edu/~eclab/projects/ecj/).
 * 
 * <p>
 * <b> Operator implementation policy:</b> Only operators, that cannot easily be
 * done with an invocation of existing methods from outside (or would be <i>a
 * lot</i> slower) are implemented. For example, adding a random non existent
 * synapse can easily be done by invoking first
 * getSynapseNonExistentAllowedRandom and then setSynapse, therefore there is no
 * mutateTopologyCreateRandomSynapse method.
 * 
 * 
 * <h2>Synaptic Weight Mutation Operators</h2>
 * 
 * The weight change mutators are ordered in ascending complexity. All of their
 * method names have the prefix "mutateWeights".
 * 
 * <p>
 * <b>Genetic Algorithm style synaptic weight mutation.</b> Uniform-Randomly
 * chooses a single synaptic weight and Uniform-Randomly adds or subtract
 * exp(x), x Uniform-Randomly chosen out of [-4,1]. Not recommended for usage
 * for it converges pretty slow. It's just implemented for testing purposes.
 * 
 * <p>
 * <b>Gaussian Mutation</b> [Fogel94]. Adds a gaussian random variable with mean
 * 0 and a standard-deviation defined by the number of weights to each weight.
 * Does not need additional space in memory. Better then the above -Style method
 * for some problems, because it does not only move on the axices of the problem
 * coordinate system, but still slow.
 * 
 * <p>
 * <b>Temperature-Driven Gaussian Mutation</b> [Angeline94]. This operator is
 * used by the GNARL Evolution system for recurrent neural networks. Adds a
 * gaussian random variable with mean 0 to each weight. Different to the
 * standard gaussian Mutation above, the standard deviation is not only defined
 * by the number of weights, but also determined by how optimal the neural net
 * already solves the given problem. If the network solves the problem quite
 * good, only small changes to the weights will be applied, and vice versa .
 * Does not need additional space in memory.
 * 
 * <p>
 * <b>Perturbation-Vector Driven Adaptive Gaussian Mutation</b> [Fogel94].
 * Mutates the weight in the gaussian way as the standard gaussian mutation, but
 * in addition, the perturbation strength is evolved in-line per each single
 * weight, so the evolution strategy can self-adapt which weights are to train
 * stronger. This mutation needs O(SYNAPSES) additional space in memory - one
 * double value per synapse. </i> This strategy seems to be the standard for
 * weight mutation throughout lots of evolutionary neural network publications I
 * read.</i>
 * 
 * 
 * <h2>Structural Mutation Operators</h2>
 * 
 * <b>Split Neuron</b> [Odri93]: A given inner neuron x is split in two neurons
 * y and z, in a way that keeps a strong behavioral link between x and (y and
 * z). The new neurons y and z get the same incident synapses as x. Incoming
 * synapses' weights are left untouched, and outgoing synapses' weights are
 * multiplied by (1+alpha) for neuron y and by (-alpha) for neuron z.
 * 
 * 
 * <p>
 * <b>Split synapse and add Neuron</b> [Stanley02]: Splits a synapse from neuron
 * i to neuron k by removing it and inserting another neuron j and two synapses
 * from i to j and from j to k.
 * 
 * <h2>Other Features</h2>
 * 
 * <b>Nonconvergent connection significance test </b>[Finnoff93]: Implemented in
 * methods with "nonconvergent" in the name. Tests existing and non-existing
 * connections as well as existing neurons for significance according to a given
 * Data Set so you can determine which connections to create or to delete.
 * 
 * 
 * 
 * <h1>References</h1>
 * 
 * <ol>
 * <li>[Schwefel81] Schwefel: Numerical Optimization of Computer Models, 1981.
 * 
 * <li>[Fogel94] Fogel: An Introduction to Simulated Evolutionary Optimization,
 * IEEE Transactions on Neural Networks Vol 5 No 1, 1994
 * 
 * <li>[Angeline94] Angeline, Saunders, Pollack: An Evolutionary Algorithm that
 * constructs recurrent Neural Networks, IEEE Transactions on Neural Networks
 * Vol 5 No 1, 1994
 * 
 * <li>[MR86] J. L. McClelland and D. E. Rumelhart. Parallel Distributed
 * Processing: Explorations in the Microstructure of Cognition, volume 2. MIT
 * Press, Cambridge, 1986.
 * 
 * <li>[RB94] Martin Riedmiller and Heinrich Braun. RPROP Description and
 * Imple- mentation Details. Technical report, University of Karlsruhe, January
 * 1994.
 * 
 * <li>[Finnoff93] Finnoff 1993. Improving model selection by nonconvergent
 * methods. Neural Networks, Vol 6.
 * 
 * <li>[Stanley02] Kenneth O Stanley and Risto Miikkulainen 2002 - Evolving
 * Neural Networks through augmenting topologies.
 * 
 * <li>[Yao97] Yao and Lio, A new Evolutionary System for evolving artificial
 * neural networks. IEEETrans on NN.
 * 
 * <li>[Odri93] Odri et al, Evolutional Development of a multilevel neural
 * network, Neural Networks, Vol 6 no 4 pp 583-595, 1993.
 * 
 * <li>[Igel2003] Igel and Hsken, Empirical evaluation of the improved Rprop
 * learning algorithms, Neurocomputing Vol 50, pp 105--123, Elsevier, 2003.
 * 
 * </ol>
 * 
 * @version 0.9
 * 
 * @author David Kriesel / dkriesel.com
 * 
 */
public class NeuralNetwork {//implements Serializable 

	
	
	private static final long serialVersionUID = 1L;

	private NeuralNetworkDescriptor descriptor;

	// main data storage
	private int[][] predecessors;
	private double[][] predecessorWeights;
	private int[][] successors;
	private int[][] successorWeightIndexInPendantPredecessorArray;

	// additional data storage for learning rules (initialized once used, can be
	// deleted with clearAllCaches()
	
	//private Vector shadow, shadclon;
	
	private Hashtable shadows;// = new Map();
	private Hashtable shadclon;
	
	
	//private TreeMap<String, double[][]> shadows = new TreeMap<String, double[][]>();
	private static final String SHADOWKEY_resilientBackpropagationGradients = "resilientBackpropagationGradients";
	private static final String SHADOWKEY_resilientBackpropagationLearningRates = "resilientBackpropagationLearningRates";
	private static final String SHADOWKEY_resilientBackpropagationLastWeightUpdates = "resilientBackpropagationLastWeightUpdates";
	private static final String SHADOWKEY_gaussianMutationAdaptivePertubationVector = "gaussianMutationAdaptivePertubationVector";
	private static final String USERSHADOWPREFIX = "USER_";

	// layer organisation
	private int[] layerStartingNeurons;
	private int[] neuronsPerLayer;

	// neural processing caches
	private double[] activations;
	private double[] netInputs;
	private NeuronBehavior[] neuronBehaviors;

	// learning parameters for Resilient Backpropagation
	private double resilientBackpropagationDeltaZero = 0.1;
	private double resilientBackpropagationDeltaMax = 50;
	private double resilientBackpropagationDeltaMin = 0.000001;
	private double resilientBackpropagationEtaMinus = 0.5;
	private double resilientBackpropagationEtaPlus = 1.2;

	// gaussian mutation parameters
	private final double gaussianMutationAdaptivePertubationInit = 1;

	// string delimiters
	private final static String dataDelimiter = "###NEURALNETDATADELIMITER###";

	// other
	private MersenneTwisterFast random = new MersenneTwisterFast();
	private static final double EPSILON = 0.000001;

	private Object NeuronsPerLayerClone1;
	
	/**
	 * Creates a neural network defined by the input and output layer neuron
	 * numbers in the given descriptor. Uses the same initialization values as
	 * the array-based constructor. Does the same as calling
	 * createNeuralNetwork() from the descriptor.
	 * 
	 * @param descriptor
	 *            the descriptor defining some constraints on the NeuralNetwork.
	 */
	public NeuralNetwork(NeuralNetworkDescriptor descriptor) {
		NeuronsPerLayerClone1=descriptor.getNeuronsPerLayer();
		int[] layers = (int[]) NeuronsPerLayerClone1;//descriptor.getNeuronsPerLayer().clone();

		for (int i = 0; i < layers.length; i++) {
			if (layers[i] == -1) {
				layers[i] = getRandomIntegerBetweenIncluding(
						descriptor.getMinLayerNeuronNumber(),
						descriptor.getMaxLayerNeuronNumber());
			}
		}
		initialize(descriptor, layers);
		if (descriptor.isInitializeAllowedSynapses()) {
			createSynapsesAllowed();
		}
	}

	/**
	 * GA-Style mutation of a single random weight. Uniform-Randomly chooses a
	 * single synaptic weight (if at least one synapse exists) and
	 * Uniform-Randomly adds or subtracts exp(x), x Uniform-Randomly chosen out
	 * of [-4,1]. Not recommended for usage for it converges pretty slow.
	 */
	public void mutateWeightsGAStyle() {
		int[] weight = getSynapseRandomExistent();
		if (weight != null) {
			double originalWeight = getWeight(weight[0], weight[1]);
			Random r = new Random();
			double rand=r.nextDouble();//.nextInt(bottomX-topX)+topX;
			if (rand > 0.5) {
				setSynapse(
						weight[0],
						weight[1],
						originalWeight
								+ MathUtils.exp(getRandomDoubleBetweenIncluding(-4,
										1)));
			} else {
				setSynapse(
						weight[0],
						weight[1],
						originalWeight
								- MathUtils.exp(getRandomDoubleBetweenIncluding(-4,
										1)));
			}
		}
	}

	/**
	 * Gaussian Mutation of all weights [Fogel94]. Adds a gaussian random
	 * variable with mean 0 and a standard-deviation 1.0 / (Math.sqrt(2 * Math
	 * .sqrt(numberOfSynapses))) to each weight. Does not need additional space
	 * in memory. Better than the GA-Style method for some problems, because it
	 * does not only move on the axices of the problem coordinate system, but
	 * still slow.
	 */
	public void mutateWeightsGaussian() {
		double numberOfSynapses = countSynapses();
		double globalStandardDeviation = 1.0 / (Math.sqrt(2 * numberOfSynapses));
		for (int i = 0; i < predecessorWeights.length; i++) {
			for (int j = 0; j < predecessorWeights[i].length; j++) {
				predecessorWeights[i][j] += globalStandardDeviation
						* random.nextGaussian();
			}
		}
	}

	/**
	 * Temperature-Driven Gaussian Mutation [Angeline94]. This operator is used
	 * by the GNARL Evolution system for recurrent neural networks. Adds a
	 * gaussian random variable with mean 0 to each weight. Different to the
	 * standard gaussian Mutation above, the standard deviation is not only
	 * defined by the number of weights, but also determined by how optimal the
	 * neural net already solves the given problem. If the network solves the
	 * problem quite good, only small changes to the weights can be applied, and
	 * vice versa. Does not need additional space in memory. It is recommended
	 * to calculate the temperature directly by the network error on a given
	 * training set. The temperature is directly multiplied with the gaussian to
	 * add to each weight. The result is then multiplied with another random
	 * variable uniformly chosen of [0,1).
	 * 
	 * @param temperature
	 *            Has to be near 1 if large mutations should be performed, and
	 *            near 0, if only small mutations are desired.
	 */
	public void mutateWeightsGaussianTemperatureDriven(double temperature) {
		double numberOfSynapses = countSynapses();
		double globalStandardDeviation = 1.0 / (Math.sqrt(2 * numberOfSynapses));
		for (int i = 0; i < predecessorWeights.length; i++) {
			for (int j = 0; j < predecessorWeights[i].length; j++) {
				predecessorWeights[i][j] += random.nextDouble() * temperature
						* globalStandardDeviation * random.nextGaussian();
			}
		}

	}

	/**
	 * Perturbation-Vector driven adaptive gaussian mutation (standard)
	 * [Fogel94]. Mutates the weight in the gaussian way as the standard
	 * gaussian mutation, but in addition, the perturbation strength is evolved
	 * in-line per each single weight, so the evolution strategy can self-adapt
	 * which weights are to train stronger. This mutation needs O(SYNAPSES)
	 * additional space in memory - one double value per synapse. <i> Seems to
	 * be standard method over lots of evolutionary neural networks papers.</i>
	 */
	public void mutateWeightsGaussianAdaptivePertubationVectorDriven() {
		double numberOfSynapses = countSynapses();
		double globalStandardDeviation = 1.0 / (Math.sqrt(2 * numberOfSynapses)); // \tau'
		double localStandardDeviation = 1.0 / (Math.sqrt(2 * Math
				.sqrt(numberOfSynapses)));
		double globalGaussian = random.nextGaussian();

		// initialize perturbation vector if neccessary
		if (shadows.get(SHADOWKEY_gaussianMutationAdaptivePertubationVector) == null) {
			double[][] perturbationVectorToInit = createShadow(SHADOWKEY_gaussianMutationAdaptivePertubationVector);
			for (int i = 0; i < perturbationVectorToInit.length; i++) {
				for (int j = 0; j < perturbationVectorToInit[i].length; j++) {
					perturbationVectorToInit[i][j] = gaussianMutationAdaptivePertubationInit;
				}
			}
		}

		// from this point on it is made sure that the shadow exists and has
		// been initialized. it can therefore be retrieved from the treemap and
		// used.
		double[][] perturbationVector = ((double[][]) shadows
				.get(SHADOWKEY_gaussianMutationAdaptivePertubationVector));

		// mutate perturbation vector
		for (int i = 0; i < predecessorWeights.length; i++) {
			for (int j = 0; j < predecessorWeights[i].length; j++) {
				perturbationVector[i][j] *= MathUtils.exp(globalStandardDeviation
						* globalGaussian + localStandardDeviation
						* random.nextGaussian());
			}
		}

		// mutate synaptic weights
		for (int i = 0; i < predecessorWeights.length; i++) {
			for (int j = 0; j < predecessorWeights[i].length; j++) {
				predecessorWeights[i][j] += perturbationVector[i][j]
						* random.nextGaussian();
			}
		}
	}

	private Object layersClone;
	
	private void initialize(NeuralNetworkDescriptor descriptor, int[] layers) {
		this.descriptor = descriptor;
		layersClone=layers;
		int[] layersCopy = (int[])layersClone;

		// count neurons

		layerStartingNeurons = new int[layersCopy.length];

		// define neuron numbers of first neuron for each layer
		// (neuron 0 is bias)
		layerStartingNeurons[0] = 1;
		int currentNeuron = 1;
		for (int i = 0; i < layersCopy.length - 1; i++) {
			currentNeuron += layersCopy[i];
			layerStartingNeurons[i + 1] = currentNeuron;
		}

		int numberOfNeuronsTemp = 0;
		for (int i = 0; i < layersCopy.length; i++) {
			numberOfNeuronsTemp += layersCopy[i];
		}

		neuronsPerLayer = layersCopy;

		// initialize synaptic data structure including the bias neuron 0
		predecessors = new int[numberOfNeuronsTemp + 1][];
		// From this line, getnumberofneurons can be used.
		predecessorWeights = new double[numberOfNeuronsTemp + 1][];
		successors = new int[numberOfNeuronsTemp + 1][];
		successorWeightIndexInPendantPredecessorArray = new int[numberOfNeuronsTemp + 1][];

		for (int i = 0; i < (countNeurons() + 1); i++) {
			predecessors[i] = new int[0];
			predecessorWeights[i] = new double[0];
			successors[i] = new int[0];
			successorWeightIndexInPendantPredecessorArray[i] = new int[0];
		}

		// initializing activations, netinputs and activationfunctions
		activations = new double[countNeurons() + 1];
		netInputs = new double[countNeurons() + 1];
		neuronBehaviors = new NeuronBehavior[countNeurons() + 1];
		activations[0] = 1;
		resetInternalValues();
		setNeuronBehaviors();
	}

	/**
	 * @param x
	 * @param y
	 * @return a double chosen uniformly at random between x and y.
	 */
	protected double getRandomDoubleBetweenIncluding(double x, double y) {
		double doubleRandom = random.nextDouble();
		// strecken um y-x, damit maximal y erreicht wird
		doubleRandom *= (y - x);
		// +x, damit minimal x erreicht wird
		doubleRandom += x;
		return doubleRandom;
	}

	/**
	 * Adds all allowed synapses to the network topology in an more efficient
	 * way than adding lots of single ones with the setSynapse() method, with
	 * expected computational effort O(ALLOWEDSYNAPSES). The synaptic weights
	 * are chosen uniformly by random within the range defined in the
	 * descriptor. Exact computational effort:
	 * O(epsilon*NEURONS^2+ALLOWEDSYNAPSES), with a <i>very</i> small epsilon.
	 * 
	 * <p>
	 * The synapses will be assigned a random value dependent on the
	 * synapseInitialRange defined in the descriptor. TODO hier uu den Fan in
	 * nehmen oder spezielle funktion dafür schreiben
	 */
	public void createSynapsesAllowed() {
		// count synapses to create
		int[] predecessorCounts = new int[countNeurons() + 1];
		int[] successorCounts = new int[countNeurons() + 1];
		for (int i = 0; i < countNeurons() + 1; i++) {
			for (int j = 0; j < countNeurons() + 1; j++) {
				if (isSynapseAllowed(i, j)) {
					predecessorCounts[j]++;
					successorCounts[i]++;
				}
			}
		}

		clearCacheAll();

		// prepare arrays
		predecessors = new int[countNeurons() + 1][];
		predecessorWeights = new double[countNeurons() + 1][];
		successors = new int[countNeurons() + 1][];
		successorWeightIndexInPendantPredecessorArray = new int[countNeurons() + 1][];

		for (int i = 0; i < (countNeurons() + 1); i++) {
			predecessors[i] = new int[predecessorCounts[i]];
			predecessorWeights[i] = new double[predecessorCounts[i]];
			successors[i] = new int[successorCounts[i]];
			successorWeightIndexInPendantPredecessorArray[i] = new int[successorCounts[i]];
		}

		int[] predecessorsIdx = new int[countNeurons() + 1];
		int[] successorsIdx = new int[countNeurons() + 1];

		// go through neurons in a way that all arrays are sorted by ascending
		// neuron index.

		// init predecessors and predecessor weights
		for (int i = 0; i < countNeurons() + 1; i++) {
			for (int j = 0; j < countNeurons() + 1; j++) {
				if (isSynapseAllowed(i, j)) {
					predecessors[j][predecessorsIdx[j]] = i;
					predecessorWeights[j][predecessorsIdx[j]] = getRandomDoubleBetweenIncluding(
							-descriptor.getSynapseInitialRange(),
							descriptor.getSynapseInitialRange());
					successors[i][successorsIdx[i]] = j;
					successorWeightIndexInPendantPredecessorArray[i][successorsIdx[i]] = predecessorsIdx[j];
					predecessorsIdx[j]++;
					successorsIdx[i]++;
				}
			}
		}

	}

	/**
	 * Clears any cache and shadows.
	 */
	public void clearCacheAll() {
		if( shadows!=null)
		shadows.clear();
	}

	/**
	 * Clears the learning rate, gradient and last weight update caches of
	 * resilient propagation.
	 * 
	 */
	public void clearCacheResilientBackpropagation() {
		removeShadow(SHADOWKEY_resilientBackpropagationGradients);
		removeShadow(SHADOWKEY_resilientBackpropagationLearningRates);
		removeShadow(SHADOWKEY_resilientBackpropagationLastWeightUpdates);
	}

	/**
	 * Clears the adaptive perturbation cache used in the
	 * mutateWeightsGaussianAdaptivePertubationVectorDriven method.
	 */
	public void clearCacheGaussianMutationAdaptivePertubation() {
		removeShadow(SHADOWKEY_gaussianMutationAdaptivePertubationVector);
	}

	/**
	 * Deletes all synapses and inner neurons from the net with expected
	 * computational effort in O(1). Exact computational effort is in
	 * O(EPSILON*NEURONS), where EPSILON is very small. Unlike removing every
	 * single synapse and neuron via the removeSynapse or -Neurons method, this
	 * method runs very fast by just re-initializing the data structure.
	 */
	public void removeSynapsesAndNeuronsAll() {
		for (int i = 1; i < neuronsPerLayer.length - 1; i++) {
			neuronsPerLayer[i] = 0;
		}
		initialize(descriptor, neuronsPerLayer);
	}

	private void resetInternalValues() {
		for (int i = 1; i < (countNeurons() + 1); i++) {
			activations[i] = 0;
			netInputs[i] = 0;
		}
	}

	/**
	 * Retrieves the neuron behaviors from the descriptor and assigns them to
	 * neurons.
	 */
	public void setNeuronBehaviors() {

		for (int i = 1; i < (countNeurons() + 1); i++) {
			if (isNeuronInput(i)) {
				NeuronBehavior behavior = descriptor
						.getNeuronBehaviorInputNeurons();
				if (behavior.needsDedicatedInstancePerNeuron()) {
					neuronBehaviors[i] = behavior.getDedicatedInstance();
				} else {
					neuronBehaviors[i] = behavior;
				}
			} else {
				if (isNeuronOutput(i)) {
					NeuronBehavior behavior = descriptor
							.getNeuronBehaviorOutputNeurons();
					if (behavior.needsDedicatedInstancePerNeuron()) {
						neuronBehaviors[i] = behavior.getDedicatedInstance();
					} else {
						neuronBehaviors[i] = behavior;
					}
				} else {
					NeuronBehavior behavior = descriptor
							.getNeuronBehaviorHiddenNeurons();
					if (behavior.needsDedicatedInstancePerNeuron()) {
						neuronBehaviors[i] = behavior.getDedicatedInstance();
					} else {
						neuronBehaviors[i] = behavior;
					}
				}
			}

		}
	}

	/**
	 * Returns the number of the first neuron in the given layer (the one with
	 * the smallest index) with computational effort in O(1).
	 * 
	 * @param layer
	 * @return the number of the first neuron in the layer.
	 */
	public int getNeuronFirstInLayer(int layer) {
		if (layer < 0) {
			throw new IllegalArgumentException("Illegal layer chosen.");
		}
		if (layer >= countLayers()) {
			throw new IllegalArgumentException("Illegal layer chosen.");
		}
		return layerStartingNeurons[layer];
	}

	/**
	 * Returns the number of the last neuron in the given layer (the one with
	 * the largest index) with computational effort in O(1).
	 * 
	 * @param layer
	 * @return the number of the last neuron in the layer.
	 */
	public int getNeuronLastInLayer(int layer) {
		if (layer < 0) {
			throw new IllegalArgumentException("Illegal layer chosen.");
		}
		if (layer >= countLayers()) {
			throw new IllegalArgumentException("Illegal layer chosen.");
		}
		if (layer == countLayers() - 1) {
			// last layer chosen, so the very last neuron is the one to return
			return countNeurons();
		}
		return layerStartingNeurons[layer + 1] - 1;
	}

	/**
	 * calculates the output value of the neural net given an input by
	 * propagating the given input through the network. There are two methods of
	 * propagation: the normal mode and the fast mode. Which mode is taken, is
	 * defined by the frequency.
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
	 * <p>
	 * Computational Effort: O(SYNAPSES) per single propagation.
	 * 
	 * TODO was passiert, wenn ein Netz z.b. backwardsynapsen hat, diese aber
	 * später im descriptor als unerlaubt markiert werden? Hier müsste es ein
	 * pruning geben
	 * 
	 * @param input
	 * @return the output @ if the lenght the input is wrong.
	 */
	public double[] propagate(double[] input) {
		if (input.length != descriptor.countInputNeurons()) {
			throw new IllegalArgumentException("Wrong number of inputs.");
		}

		double[] output = new double[descriptor.countOutputNeurons()];

		// Propagate bias neuron -------------------------------
		netInputs[0] = 0;
		activations[0] = 1;

		// Propagate input layer -------------------------------
		for (int i = 1; i < getNeuronFirstInLayer(1); i++) {
			if (Double.isNaN(input[i - 1])) {
				System.err.println("Neural Net Input " + (i - 1)
						+ " was NaN and was set zero before propagation.");
				input[i - 1] = 0;
			}
			netInputs[i] = input[i - 1];
			activations[i] = neuronBehaviors[i].computeActivation(netInputs[i]);
		}

		// Fast propagation mode of remaining neurons ----------
		if (descriptor.getFrequency() == 0) {
			if (descriptor.isAllowBackwardSynapses()
					|| descriptor.isAllowBackwardShortcutSynapses()
					|| descriptor.isAllowLateralSynapses()
					|| descriptor.isAllowSelfSynapses()) {
				throw new IllegalArgumentException(
						"Can't fastprop. Only forward and forward shortcut Synapses are allowed.");
			}

			// calculate net input and activations directly together
			for (int i = getNeuronFirstInLayer(1); i < (countNeurons() + 1); i++) {
				netInputs[i] = 0;
				for (int j = 0; j < predecessors[i].length; j++) {
					netInputs[i] += activations[predecessors[i][j]]
							* predecessorWeights[i][j];
				}
				activations[i] = neuronBehaviors[i]
						.computeActivation(netInputs[i]);
			}
		}

		// Normal propagation mode of remaining Neurons --------
		if (descriptor.getFrequency() > 0) {
			for (int frequency = 0; frequency < descriptor.getFrequency(); frequency++) {
				// calculate net inputs
				for (int i = getNeuronFirstInLayer(1); i < (countNeurons() + 1); i++) {
					netInputs[i] = 0;
					for (int j = 0; j < predecessors[i].length; j++) {
						netInputs[i] += activations[predecessors[i][j]]
								* predecessorWeights[i][j];
					}
				}

				// calculate activations
				for (int i = getNeuronFirstInLayer(1); i < (countNeurons() + 1); i++) {
					activations[i] = neuronBehaviors[i]
							.computeActivation(netInputs[i]);
				}
			}

		}

		// write output
		for (int i = countNeurons(); i >= getNeuronFirstInLayer(countLayers() - 1); i--) {
			output[mapOutputNeuronToOutputNumber(i)] = activations[i];
		}

		return output;
	}

	/**
	 * Wraps setSynapse for your convenience - to get more information, read the
	 * setSynapse documentation.
	 * 
	 * @param i
	 * @param j
	 * @param newWeight
	 */
	public void createSynapse(int i, int j, double newWeight) {
		setSynapse(i, j, newWeight);
	}

	/**
	 * Updates or randomly chooses the synaptic weight value from the neuron i
	 * to neuron j (computational effort: O(log(MIN(I_out,J_in))))) - If the
	 * synapse doesn't exist, it is added, if it is allowed (worst case
	 * computational effort: O(NEURONS*log(NEURONS))).
	 * <p>
	 * If newWeight is assigned the value Double.NaN, the synapse will be
	 * assigned a random value dependent on the synapseInitialRange defined in
	 * the descriptor.
	 * 
	 * @param i
	 *            the start neuron of the synapse
	 * @param j
	 *            the end neuron of the synapse
	 * @param newWeight
	 *            If assigned the value Double.NaN, the synapse will be assigned
	 *            a random value dependent on the synapseInitialRange defined in
	 *            the descriptor.
	 * 
	 */
	public void setSynapse(int i, int j, double newWeight) {
		if (i < 0 || i > countNeurons()) {
			throw new IllegalArgumentException("Illegal Synapse.");
		}
		if (j < 0 || j > countNeurons()) {
			throw new IllegalArgumentException("Illegal Synapse.");
		}

		// compute synapse value if necessary
		double weightToAssign = newWeight;
		if (Double.isNaN(newWeight)) {
			weightToAssign = getRandomDoubleBetweenIncluding(
					-descriptor.getSynapseInitialRange(),
					descriptor.getSynapseInitialRange());
		}

		// search for synaptic weight. if the synapse doesn't exist, it is
		// added. Otherwise, it's weight value is altered.
		int predIdx = Arrays.binarySearch(predecessors[j], i);
		if (predIdx >= 0) {
			// synapse exists and just has to be altered.
			predecessorWeights[j][predIdx] = weightToAssign;
		} else {
			if (isSynapseAllowed(i, j)) {

				// synapse does not exist, so it has to be added. the succIdx
				// and precIdx contain the addition position that is used in
				// order to keep the arrays sorted by adjacent neuron indices.
				int succIdx = Arrays.binarySearch(successors[i], j);
				predIdx = -(predIdx + 1);
				succIdx = -(succIdx + 1);

				// Before adding, update the successor arrays of all neurons
				// with index >= I that have J as an successor (all predecessors
				// from J), because J's predecessor array, that stores the
				// regarding weights, is shifted left from position I on.
				// of this: O(J_in) times searching in the regarding predecessor
				// index arrays.
				for (int predOfJIdx = predIdx; predOfJIdx < predecessors[j].length; predOfJIdx++) {
					int predOfJ = predecessors[j][predOfJIdx];
					int idxToIncrease = Arrays.binarySearch(
							successors[predOfJ], j);
					successorWeightIndexInPendantPredecessorArray[predOfJ][idxToIncrease]++;
				}

				// adding successor entry for i
				int[] newSuccessors = new int[successors[i].length + 1];
				int[] newSuccessorWeightIndexInPendantPredecessorArray = new int[successorWeightIndexInPendantPredecessorArray[i].length + 1];
				for (int idx = 0; idx < newSuccessorWeightIndexInPendantPredecessorArray.length; idx++) {
					if (idx < succIdx) {
						// just copy old content
						newSuccessors[idx] = successors[i][idx];
						newSuccessorWeightIndexInPendantPredecessorArray[idx] = successorWeightIndexInPendantPredecessorArray[i][idx];
					}
					if (idx == succIdx) {
						// add synapse here
						newSuccessors[idx] = j;
						newSuccessorWeightIndexInPendantPredecessorArray[idx] = predIdx;
					}
					if (idx > succIdx) {
						// shift content to the ascending direction by one cell
						newSuccessors[idx] = successors[i][idx - 1];
						newSuccessorWeightIndexInPendantPredecessorArray[idx] = successorWeightIndexInPendantPredecessorArray[i][idx - 1];
					}
				}

				// adding predecessor entry for j

				// create new arrays that will be replacing old ones
				int[] newPredecessors = new int[predecessors[j].length + 1];
				double[] newPredecessorWeights = new double[predecessorWeights[j].length + 1];

				// go through each of the arrays
				for (int idx = 0; idx < newPredecessorWeights.length; idx++) {
					if (idx < predIdx) {
						// just copy old content
						newPredecessors[idx] = predecessors[j][idx];
						newPredecessorWeights[idx] = predecessorWeights[j][idx];
					}
					if (idx == predIdx) {
						// add synapse here
						newPredecessors[idx] = i;
						newPredecessorWeights[idx] = weightToAssign;
					}
					if (idx > predIdx) {
						// shift content to the ascending direction by one cell
						newPredecessors[idx] = predecessors[j][idx - 1];
						newPredecessorWeights[idx] = predecessorWeights[j][idx - 1];
					}
				}

				// update data structure
				successors[i] = newSuccessors;
				successorWeightIndexInPendantPredecessorArray[i] = newSuccessorWeightIndexInPendantPredecessorArray;
				predecessors[j] = newPredecessors;
				predecessorWeights[j] = newPredecessorWeights;

				// update shadows analogue
			
				shadclon= shadows;
				
				int keycount=0, keyit=-1;
				
				if(shadows.containsKey((Object)SHADOWKEY_gaussianMutationAdaptivePertubationVector))
						keycount++;
				if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationGradients))
					keycount++;
				if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLastWeightUpdates))
					keycount++;
				if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLearningRates))
					keycount++;
				Object[] keyIterator = new Object[keycount];
				
				if(shadows.containsKey((Object)SHADOWKEY_gaussianMutationAdaptivePertubationVector)){
					keyit++;
					keyIterator[keyit]=SHADOWKEY_gaussianMutationAdaptivePertubationVector;
				}
			if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationGradients)){
				keyit++;
			keyIterator[keyit]=SHADOWKEY_resilientBackpropagationGradients;}
			if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLastWeightUpdates)){
				keyit++;
			keyIterator[keyit]=SHADOWKEY_resilientBackpropagationLastWeightUpdates;}
			if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLearningRates)){
				keyit++;
			keyIterator[keyit]=SHADOWKEY_resilientBackpropagationLearningRates;}
				//keyIterator=shadows.keySet().toArray();
			for (int k=0;k<keycount;k++){	
				
				
				//Iterator keyIterator =  keyIterator.hasNext();) {
				String key = keyIterator[k].toString();	
//				for (Iterator keyIterator = shadows.keySet().iterator(); keyIterator.hasNext();) {
//					String key = keyIterator.next().toString();

					// get shadow
					double[][] shadow = (double[][]) shadows.get(key);
					// create new shadow part
					double[] newShadowPart = new double[shadow[j].length + 1];

					// fill shadow part
					for (int idx = 0; idx < newPredecessorWeights.length; idx++) {
						if (idx < predIdx) {
							// just copy old content
							newShadowPart[idx] = shadow[j][idx];
						}
						if (idx == predIdx) {
							// add synapse here
							newShadowPart[idx] = 0;
							// TODO mal gucken ob da noch irgendwie ein
							// standardwert vorgegeben werden soll
						}
						if (idx > predIdx) {
							// shift content to the ascending direction by
							// one cell
							newShadowPart[idx] = shadow[j][idx - 1];
						}
						// assign to shadow (no need to rebuild shadows
						// treemap for references of shadows do not change)

					}
					shadow[j] = newShadowPart;
				}

			} else {
				throw new IllegalArgumentException(
						"Synapse not allowed to add.");
			}
		}
	}

	/**
	 * Removes synapses, that exist in the network but are not allowed (this may
	 * happen if a network is created and the allowed synapse types are changed
	 * in the descriptor afterwards) with expected computational effort in
	 * O(SYNAPSESTOREMOVE * SYNAPSEREMOVALEFFORT).
	 * 
	 * <p>
	 * Exact computational effort: O(epsilon * SYNAPSES + SYNAPSESTOREMOVE *
	 * SYNAPSEREMOVALEFFORT), where epsilon is very small and SYNAPSESTOREMOVE
	 * denotes the number of synapses that are actually removed. To learn about
	 * SYNAPSEREMOVALEFFORT, please check the documentation of the removeSynapse
	 * method.
	 */
	public void removeSynapsesNotAllowed() {

		// count synapses that exist but are not allowed
		int counter = 0;
		for (int j = 0; j < predecessors.length; j++) {
			for (int i = 0; i < predecessors[j].length; i++) {
				// synapse predecessors[j][i] --> j
				if (!isSynapseAllowed(predecessors[j][i], j)) {
					counter++;
				}
			}
		}

		// memorize synapses that exist but are not allowed
		int[] sources = new int[counter];
		int[] targets = new int[counter];
		counter = 0;
		for (int j = 0; j < predecessors.length; j++) {
			for (int i = 0; i < predecessors[j].length; i++) {
				// synapse predecessors[j][i] --> j
				if (!isSynapseAllowed(predecessors[j][i], j)) {
					sources[counter] = predecessors[j][i];
					targets[counter] = j;
					counter++;
				}
			}
		}

		// remove them
		for (int i = 0; i < targets.length; i++) {
			removeSynapse(sources[i], targets[i]);
		}
	}

	/**
	 * 
	 * Removes a synapse from neuron I to neuron J, if existent with
	 * computational effort in O(NEURONS*log(NEURONS)) (worst case). If synapse
	 * doesn't exist, the computational effort is reduced to the synapse
	 * existence check.
	 * 
	 * @param i
	 * @param j
	 */
	public void removeSynapse(int i, int j) {
		// search for synaptic weight. if the synapse doesn't exist, there is no
		// need for removing. If it exists, it has to be removed.
		int succIdx = Arrays.binarySearch(successors[i], j);
		if (succIdx >= 0) {
			// synapse exists, has to be removed.
			int predIdx = Arrays.binarySearch(predecessors[j], i);

			// removing successor entry for i
			int[] newSuccessors = new int[successors[i].length - 1];
			int[] newSuccessorWeightIndexInPendantPredecessorArray = new int[successorWeightIndexInPendantPredecessorArray[i].length - 1];
			for (int idx = 0; idx < newSuccessorWeightIndexInPendantPredecessorArray.length; idx++) {
				if (idx < succIdx) {
					// just copy old content
					newSuccessors[idx] = successors[i][idx];
					newSuccessorWeightIndexInPendantPredecessorArray[idx] = successorWeightIndexInPendantPredecessorArray[i][idx];
				}
				// if idx == succIdx, synapse is removed
				if (idx > succIdx) {
					// shift content to the descending direction by one cell
					newSuccessors[idx] = successors[i][idx + 1];
					newSuccessorWeightIndexInPendantPredecessorArray[idx] = successorWeightIndexInPendantPredecessorArray[i][idx + 1];
				}
			}

			// removing predecessor entry for j
			int[] newPredecessors = new int[predecessors[j].length - 1];
			double[] newPredecessorWeights = new double[predecessorWeights[j].length - 1];

			for (int idx = 0; idx < newPredecessorWeights.length; idx++) {
				if (idx < predIdx) {
					// just copy old content
					newPredecessors[idx] = predecessors[j][idx];
					newPredecessorWeights[idx] = predecessorWeights[j][idx];
				}
				// if idx == precIdx, synapse is removed
				if (idx > predIdx) {
					// shift content to the descending direction by one cell
					newPredecessors[idx] = predecessors[j][idx + 1];
					newPredecessorWeights[idx] = predecessorWeights[j][idx + 1];
				}
			}

			// update data structure
			successors[i] = newSuccessors;
			successorWeightIndexInPendantPredecessorArray[i] = newSuccessorWeightIndexInPendantPredecessorArray;
			predecessors[j] = newPredecessors;
			predecessorWeights[j] = newPredecessorWeights;

			// Now, update the successor arrays of all neurons with index >=
			// I that have J as an successor (all predecessors from J), because
			// J's predecessor array, that stores the regarding weights, is
			// decremented from position I on. Effort of this: O(J_in times
			// searching in the regarding predecessor index arrays).
			// first predecessor from J with index >= i has already be found:
			// predIdx, the place where the removed synapse once was.
			for (int predOfJIdx = predIdx; predOfJIdx < predecessors[j].length; predOfJIdx++) {
				int predOfJ = predecessors[j][predOfJIdx];
				int idxToDecrease = Arrays.binarySearch(successors[predOfJ], j);
				successorWeightIndexInPendantPredecessorArray[predOfJ][idxToDecrease]--;
			}

			// for all shadows, do the same update
//			Object[] keyIterator = new Object[shadows.keySet().size()];
//			keyIterator=shadows.keySet().toArray();
			int keycount=0, keyit=-1;
			
			if(shadows.containsKey((Object)SHADOWKEY_gaussianMutationAdaptivePertubationVector))
					keycount++;
			if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationGradients))
				keycount++;
			if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLastWeightUpdates))
				keycount++;
			if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLearningRates))
				keycount++;
			Object[] keyIterator = new Object[keycount];
			
			if(shadows.containsKey((Object)SHADOWKEY_gaussianMutationAdaptivePertubationVector)){
				keyit++;
				keyIterator[keyit]=SHADOWKEY_gaussianMutationAdaptivePertubationVector;
			}
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationGradients)){
			keyit++;
		keyIterator[keyit]=SHADOWKEY_resilientBackpropagationGradients;}
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLastWeightUpdates)){
			keyit++;
		keyIterator[keyit]=SHADOWKEY_resilientBackpropagationLastWeightUpdates;}
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLearningRates)){
			keyit++;
		keyIterator[keyit]=SHADOWKEY_resilientBackpropagationLearningRates;}
			
			
			
		for (int z=0;z<keycount;z++){					//Iterator keyIterator =  keyIterator.hasNext();) {
			String key = keyIterator[z].toString();	
//			for (Iterator keyIterator = shadows.keySet().iterator(); keyIterator.hasNext();) {
//				String key = keyIterator.next().toString();

				// get shadow
				double[][] shadow = (double[][])shadows.get(key);
				// create new shadow part
				double[] newShadowPart = new double[shadow[j].length - 1];

				// fill shadow part
				for (int idx = 0; idx < newPredecessorWeights.length; idx++) {
					if (idx < predIdx) {
						// just copy old content
						newShadowPart[idx] = shadow[j][idx];
					}
					// if idx == precIdx, synapse is removed
					if (idx > predIdx) {
						// shift content to the descending direction by one cell
						newShadowPart[idx] = shadow[j][idx + 1];
					}
					// assign to shadow (no need to rebuild shadows
					// treemap for references of shadows do not change)
				}
				shadow[j] = newShadowPart;
			}

		}
	}

	/**
	 * Returns whether a synapse exists with computational effort in
	 * O(log(MIN(I_out,J_in)))).
	 * 
	 * @param i
	 *            the start neuron of the synapse
	 * @param j
	 *            the end neuron of the synapse
	 * @return if the synapse exists depending on if it is switched on and if it
	 *         is allowed @ if i and j are illegal chosen in some way
	 */
	public boolean isSynapseExistent(int i, int j) {
		if (i < 0 || i > countNeurons()) {
			throw new IllegalArgumentException("Illegal Synapse.");
		}
		if (j < 0 || j > countNeurons()) {
			throw new IllegalArgumentException("Illegal Synapse.");
		}

		// look for the smaller array to search in and search. a negative return
		// of the search function means that the element was not found so the
		// synapse doesn't exist.
		if (predecessors[j].length <= successors[i].length) {
			int result = Arrays.binarySearch(predecessors[j], i);
			return result >= 0;
		} else {
			int result = Arrays.binarySearch(successors[i], j);
			return result >= 0;
		}
	}

	/**
	 * Gets the synaptic weight value from the neuron i to neuron j with
	 * computational effort in O(log(MIN(I_out,J_in)))).
	 * 
	 * @param i
	 *            the start neuron of the synapse
	 * @param j
	 *            the end neuron of the synapse
	 * @return the weight value.
	 */
	public double getWeight(int i, int j) {
		if (i < 0 || i > countNeurons()) {
			throw new IllegalArgumentException("Illegal Synapse: " + i + ","
					+ j + " NumberOfNeurons: " + countNeurons());
		}
		if (j < 0 || j > countNeurons()) {
			throw new IllegalArgumentException("Illegal Synapse." + i + "," + j
					+ " NumberOfNeurons: " + countNeurons());
		}

		// look for the smaller array to search in and search. a negative return
		// of the search function means that the element was not found so the
		// synapse doesn't exist.
		if (predecessors[j].length <= successors[i].length) {
			int idx = Arrays.binarySearch(predecessors[j], i);
			if (idx >= 0) {
				return predecessorWeights[j][idx];
			}
		} else {
			int idx = Arrays.binarySearch(successors[i], j);
			if (idx >= 0) {
				return predecessorWeights[j][successorWeightIndexInPendantPredecessorArray[i][idx]];
			}
		}

		// when this place of code is reached, nothing was returned.
		throw new IllegalArgumentException("Synapse " + i + "," + j
				+ " is legal, but does not exist.");

	}

	/**
	 * Gets the value in user shadow that is shadowing the synapse from neuron i
	 * to neuron j in shadow named key with computational effort in
	 * O(log(NUMBEROFSHADOWS)+log(MIN(I_out,J_in)))).
	 * 
	 * @param i
	 *            the start neuron of the synapse that is shadowed
	 * @param j
	 *            the end neuron of the synapse that is shadowed
	 * @param key
	 *            the user shadow key
	 * @return the shadow value.
	 */
	protected double getUserShadowValue(int i, int j, String key) {
		if (i < 0 || i > countNeurons()) {
			throw new IllegalArgumentException("Illegal Synapse: " + i + ","
					+ j + " NumberOfNeurons: " + countNeurons());
		}
		if (j < 0 || j > countNeurons()) {
			throw new IllegalArgumentException("Illegal Synapse." + i + "," + j
					+ " NumberOfNeurons: " + countNeurons());
		}
		double[][] shadow = (double[][])shadows.get(USERSHADOWPREFIX + key);
		if (shadow == null) {
			throw new IllegalArgumentException("User Shadow \"" + key
					+ "\" does not exist.");
		}

		// look for the smaller array to search in and search. a negative return
		// of the search function means that the element was not found so the
		// synapse doesn't exist.
		if (predecessors[j].length <= successors[i].length) {
			int idx = Arrays.binarySearch(predecessors[j], i);
			if (idx >= 0) {
				return shadow[j][idx];
			}
		} else {
			int idx = Arrays.binarySearch(successors[i], j);
			if (idx >= 0) {
				return shadow[j][successorWeightIndexInPendantPredecessorArray[i][idx]];
			}
		}

		// when this place of code is reached, nothing was returned.
		throw new IllegalArgumentException("Synapse " + i + "," + j
				+ " is legal, but does not exist.");

	}

	/**
	 * Sets the value in user shadow that is shadowing the synapse from neuron i
	 * to neuron j in shadow named key with computational effort in
	 * O(log(NUMBEROFSHADOWS)+log(MIN(I_out,J_in)))).
	 * 
	 * @param i
	 *            the start neuron of the synapse that is shadowed
	 * @param j
	 *            the end neuron of the synapse that is shadowed
	 * @param key
	 *            the user shadow key
	 * @param value
	 */
	protected void setUserShadowValue(int i, int j, String key, double value) {
		if (i < 0 || i > countNeurons()) {
			throw new IllegalArgumentException("Illegal Synapse: " + i + ","
					+ j + " NumberOfNeurons: " + countNeurons());
		}
		if (j < 0 || j > countNeurons()) {
			throw new IllegalArgumentException("Illegal Synapse." + i + "," + j
					+ " NumberOfNeurons: " + countNeurons());
		}
		double[][] shadow = (double[][])shadows.get(USERSHADOWPREFIX + key);
		if (shadow == null) {
			throw new IllegalArgumentException("User Shadow \"" + key
					+ "\" does not exist.");
		}

		// look for the smaller array to search in and search. a negative return
		// of the search function means that the element was not found so the
		// synapse doesn't exist.
		if (predecessors[j].length <= successors[i].length) {
			int idx = Arrays.binarySearch(predecessors[j], i);
			if (idx >= 0) {
				shadow[j][idx] = value;
			}
		} else {
			int idx = Arrays.binarySearch(successors[i], j);
			if (idx >= 0) {
				shadow[j][successorWeightIndexInPendantPredecessorArray[i][idx]] = value;
			}
		}

		// when this place of code is reached, nothing was returned.
		throw new IllegalArgumentException("Synapse " + i + "," + j
				+ " is legal, but does not exist.");

	}

	/**
	 * @param outputNumber
	 *            output number to map to neuron number
	 * @return neuron number
	 */
	protected int mapOutputNumberToOutputNeuron(int outputNumber) {
		if ((outputNumber >= descriptor.countInputNeurons())
				|| (outputNumber < 0)) {
			throw new IllegalArgumentException("Wrong Output Index.");
		}
		int neuron = outputNumber + 1
				+ (countNeurons() - descriptor.countOutputNeurons());
		return neuron;
	}

	/**
	 * @param neuron
	 *            neuron number to map to output number
	 * @return output number
	 */
	protected int mapOutputNeuronToOutputNumber(int neuron) {
		if ((neuron > countNeurons()) || (neuron < 1)) {
			throw new IllegalArgumentException("Wrong Neuron Index: " + neuron);
		}
		int output = neuron - 1
				- (countNeurons() - descriptor.countOutputNeurons());
		if (output < 0) {
			throw new IllegalArgumentException("Neuron " + neuron
					+ " is not an output Neuron.");
		} else {
			return output;
		}
	}

	/**
	 * Trains the Neural Network with the Training Method "Backpropagation of
	 * Error" [MR86]. If other connections than forward and forward shortcuts
	 * are allowed, an exception will be thrown. It trains online with a random
	 * sample order.
	 * 
	 * @param lesson
	 *            the training lesson to learn
	 * 
	 * @param runs
	 *            The number of random patterns to train.
	 * @param eta
	 *            The learning rate.
	 * 
	 */
	public void trainBackpropagationOfError(TrainingSampleLesson lesson,
			int runs, double eta) {

		double[][] inputs = lesson.getInputs();
		double[][] desiredOutputs = lesson.getDesiredOutputs();

		int numberOfSamples = inputs.length;

		if (descriptor.isAllowBackwardSynapses()
				|| descriptor.isAllowBackwardShortcutSynapses()
				|| descriptor.isAllowLateralSynapses()
				|| descriptor.isAllowSelfSynapses()) {
			throw new IllegalArgumentException(
					"Can't backprop. Only forward and forward shortcut Synapses are allowed.");
		}

		for (int run = 0; run < runs; run++) {
			// Choose Sample
			int chosenSample = getRandomIntegerBetweenIncluding(0,
					numberOfSamples - 1);

			double[] delta = new double[countNeurons() + 1];
			for (int i = countNeurons(); i >= getNeuronFirstInLayer(1); i--) {
				delta[i] = 0;
				// first part of delta
				delta[i] = neuronBehaviors[i].computeDerivative(netInputs[i]);
				// second part of delta depending on kind of neuron
				if (isNeuronOutput(i)) {
					// Propagate Sample
					double[] outputs = propagate(inputs[chosenSample]);
					delta[i] *= (desiredOutputs[chosenSample][mapOutputNeuronToOutputNumber(i)] - outputs[mapOutputNeuronToOutputNumber(i)]);
					delta[i] *= eta;// multiplication with eta is done here and
					// can therefore be ommited when calculating
					// the weight changes
				} else {
					double temp = 0;
					// collect delta from connected neuron
					for (int j = 0; j < successors[i].length; j++) {
						temp += (predecessorWeights[successors[i][j]][successorWeightIndexInPendantPredecessorArray[i][j]] * delta[successors[i][j]]);
					}
					delta[i] *= temp;
				}
			}

			// alter weights
			for (int i = 0; i < countNeurons() + 1; i++) {
				for (int j = 0; j < predecessors[i].length; j++) {
					// behandelt wird synapse predecessors[i][j]-->i
					predecessorWeights[i][j] += activations[predecessors[i][j]]
							* delta[i];
					// multiplication with eta is ommited here because it is
					// already done while calculating errors
				}
			}
		}

	}

	/**
	 * @param i
	 * @param j
	 * @return a random integer in the interval [i,j].
	 */
	protected int getRandomIntegerBetweenIncluding(int i, int j) {
		int rnd = random.nextInt(j - i + 1);
		rnd += i;
		return rnd;
	}

	private double[][] createShadow(String key) {
		if (shadows.containsKey(key)) {
			// impossible to accidently overwrite shadows
			throw new IllegalArgumentException("Synapse shadow key \"" + key
					+ "\" already exists.");
		}

		// create new unmanaged shadow
		double[][] newShadow = createShadowUnmanaged();
		// manage it by adding it to the list of shadows

		shadows.put(key, newShadow);
		return newShadow;
	}

	/**
	 * Creates a user shadow that is automatically adapted if network topology
	 * changes in time O(log(NUMBEROFSHADOWS)+log(SYNAPSES)). If the key entered
	 * is already occupied, an IllegalArgumentException is thrown. <b>Be aware
	 * of the fact that if synapses are added to the network, the corresponding
	 * new shadow fields are initialized with 0!</b> If this behavior has
	 * negative influence on how you use the shadows, you need to keep track of
	 * the topological changes in the network yourself and handle them
	 * accordingly.
	 * 
	 * @param key
	 *            the key to identify the shadow later on.
	 * @return the new user shadow.
	 */
	protected double[][] createUserShadow(String key) {
		if (shadows.containsKey(USERSHADOWPREFIX + key)) {
			// impossible to accidently overwrite shadows
			throw new IllegalArgumentException("User Synapse shadow key \""
					+ key + "\" already exists.");
		}

		// create new unmanaged shadow
		double[][] newShadow = createShadowUnmanaged();
		// manage it by adding it to the list of shadows

		shadows.put(USERSHADOWPREFIX + key, newShadow);
		return newShadow;
	}

	/**
	 * @return a shadow for local use that is <i>not</i> automatically adapted
	 *         if network topology changes in time O(log(SYNAPSES)).
	 */
	protected double[][] createShadowUnmanaged() {
		// create new shadow
		double[][] newShadow = new double[predecessorWeights.length][];
		for (int i = 0; i < newShadow.length; i++) {
			newShadow[i] = new double[predecessorWeights[i].length];
		}

		return newShadow;
	}

	private void removeShadow(String key) {
		shadows.remove(key);
	}

	/**
	 * Removes a managed user shadow with the given key, if existent, in time
	 * O(log(NUMBEROFSHADOWS)). This shadow is no longer maintained if topology
	 * changes and will be subject to deletion by garbage collection if not
	 * referenced elsewhere.
	 * 
	 * @param key
	 */
	protected void removeUserShadow(String key) {
		shadows.remove(USERSHADOWPREFIX + key);
	}

	/**
	 * Returns a managed user shadow with the given key, if existent, or null,
	 * of not. Takes time O(log(NUMBEROFSHADOWS)). No clone but the original
	 * reference stored in the shadow data structure is returned, so if
	 * something within the shadow is changed, the changes will be accessible
	 * via getUserShadow later on.
	 * 
	 * @param key
	 * @return the user shadow
	 */
	protected double[][] getUserShadow(String key) {
		return (double[][])shadows.get(USERSHADOWPREFIX + key);
	}

	/**
	 * Checks whether or not a managed user shadow with the given key exists in
	 * time O(log(NUMBEROFSHADOWS)).
	 * 
	 * @param key
	 * @return whether or not a managed user shadow with the given key exists.
	 */
	protected boolean existsUserShadow(String key) {
		return shadows.containsKey(USERSHADOWPREFIX + key);
	}

	/**
	 * Trains the Neural Network with the Training Method "Resilient
	 * Backpropagation of Error" [RB94]. If other connections than forward or
	 * forward shortcuts are allowed, an exception will be thrown. It trains
	 * offline for it needs stable gradients in order to work. The initial
	 * weight updates are set to 0.1, the maximum weight updates to 50 as
	 * proposed in [RB94]. The method permanently caches synaptic weight
	 * learning rates, last weight updates and gradients with a storage effort
	 * of O(SYNAPSES). Some additional caching in O(SYNAPSES) is done during the
	 * execution of the method. This cache is maintained even after invocation
	 * of this method in case training will continue later. If you like, use the
	 * respective clear method to clear those caches. A boolean parameter lets
	 * you decide to use the improvements of ResilientPropagation published in
	 * [Igel2003], which will increase the iteration time but may (not: must)
	 * yield better results. In the context of this method, special thanks go to
	 * Martin Westhoven for bugfixing and testing!
	 * 
	 * @param lesson
	 *            the training lesson to learn.
	 * @param runs
	 *            The number of iterations.
	 * @param improvedRprop
	 *            If improved Rprop after [Igel2003] shall be used
	 * 
	 */
	public void trainResilientBackpropagation(TrainingSampleLesson lesson,
			int runs, boolean improvedRprop) {

		double[][] inputs = lesson.getInputs();
		double[][] desiredOutputs = lesson.getDesiredOutputs();

		// initialize caches if necessary
		if (shadows.get(SHADOWKEY_resilientBackpropagationLearningRates) == null) {
			double[][] learningRatesToInit = createShadow(SHADOWKEY_resilientBackpropagationLearningRates);
			for (int i = 0; i < learningRatesToInit.length; i++) {
				for (int j = 0; j < learningRatesToInit[i].length; j++) {
					learningRatesToInit[i][j] = resilientBackpropagationDeltaZero;
				}
			}
		}

		boolean initialRun = false;
		// initialize gradients if neccessary
		if (shadows.get(SHADOWKEY_resilientBackpropagationGradients) == null) {
			createShadow(SHADOWKEY_resilientBackpropagationGradients);
			initialRun = true;
		}

		if (shadows.get(SHADOWKEY_resilientBackpropagationLastWeightUpdates) == null) {
			createShadow(SHADOWKEY_resilientBackpropagationLastWeightUpdates);
		}

		// from this point on it is made sure that the shadows exist and have
		// been initialized. it can therefore be retrieved from the treemap and
		// used.
		double[][] learningRates = (double[][])shadows
				.get(SHADOWKEY_resilientBackpropagationLearningRates);
		double[][] gradients =(double[][]) shadows
				.get(SHADOWKEY_resilientBackpropagationGradients);
		double[][] lastUpdates =(double[][]) shadows
				.get(SHADOWKEY_resilientBackpropagationLastWeightUpdates);

		if (descriptor.isAllowBackwardSynapses()
				|| descriptor.isAllowBackwardShortcutSynapses()
				|| descriptor.isAllowLateralSynapses()
				|| descriptor.isAllowSelfSynapses()) {
			throw new IllegalArgumentException(
					"Can't rprop. Only forward and forward shortcut Synapses are allowed.");
		}

		// Error Measurement for Improved Rprop, comment out or delete for clean
		// rprop
		double lastErr = 0;
		double err = 0;
		if (improvedRprop) {
			lastErr = ErrorMeasurement.getErrorAbsoluteSum(this, lesson);
		}

		for (int run = 0; run < runs; run++) {
			double[][] newGradients = createShadowUnmanaged();
			if (run > 0 && initialRun) // Check if we still need an
										// initialisation of the
										// ResilientBackpropagationGradients
				initialRun = false;
			// calculate Deltas
			double[] delta = new double[countNeurons() + 1];
			for (int chosenSample = 0; chosenSample < lesson.countSamples(); chosenSample++) {
				// Propagate Sample
				double[] outputs = propagate(inputs[chosenSample]);
				for (int i = countNeurons(); i >= getNeuronFirstInLayer(1); i--) {
					// second part of delta depending on kind of neuron
					if (isNeuronOutput(i)) {
						delta[i] = (desiredOutputs[chosenSample][mapOutputNeuronToOutputNumber(i)] - outputs[mapOutputNeuronToOutputNumber(i)]);
					} else {
						delta[i] = 0;
						// collect delta from connected neuron
						for (int j = 0; j < successors[i].length; j++) {
							delta[i] += (predecessorWeights[successors[i][j]][successorWeightIndexInPendantPredecessorArray[i][j]] * delta[successors[i][j]]);
						}
					}
					// first part of delta
					delta[i] *= neuronBehaviors[i]
							.computeDerivative(netInputs[i]);
					// all deltas collected, compute gradient
					for (int j = 0; j < predecessors[i].length; j++)
						// Fehler nach gewicht(predecessors[i][j],i) abgeleitet
						// ist
						// gleich
						// -delta(i) * o(predecessors[i][j])
						newGradients[i][j] += -delta[i]
								* activations[predecessors[i][j]];
				}
			}
			if (initialRun) {
				gradients = newGradients;
			}
			if (improvedRprop) {
				// get an up to date error
				err = ErrorMeasurement.getErrorAbsoluteSum(this, lesson);
			}
			// alter weights
			for (int i = 0; i < countNeurons() + 1; i++) {
				for (int j = 0; j < predecessors[i].length; j++) {
					// behandelt wird synapse predecessors[i][j]-->i
					double decisionValue = newGradients[i][j] * gradients[i][j];
					boolean done = false;
					if (Math.abs(decisionValue) < EPSILON) {
						// gradient "zero"
						done = true;
						gradients[i][j] = newGradients[i][j];
						lastUpdates[i][j] = -(Math.abs(gradients[i][j])/gradients[i][j])
								* learningRates[i][j];
						predecessorWeights[i][j] += lastUpdates[i][j];
					}
					if (decisionValue > 0.0 && !done) {
						// gradient did not change signum
						gradients[i][j] = newGradients[i][j];
						learningRates[i][j] = learningRates[i][j]
								* resilientBackpropagationEtaPlus;
						if (learningRates[i][j] > resilientBackpropagationDeltaMax) {
							learningRates[i][j] = resilientBackpropagationDeltaMax;
						}
						lastUpdates[i][j] = -(Math.abs(gradients[i][j])/gradients[i][j])
								* learningRates[i][j];
						predecessorWeights[i][j] += lastUpdates[i][j];
					}
					if (decisionValue < 0.0 && !done) {
						// gradient changed signum
						learningRates[i][j] = learningRates[i][j]
								* resilientBackpropagationEtaMinus;
						if (learningRates[i][j] > resilientBackpropagationDeltaMin) {
							learningRates[i][j] = resilientBackpropagationDeltaMin;
						}
						// if normal rprop, step is always executed. If
						// improved, it is only executed with err > lastErr.
						if ((improvedRprop && err > lastErr) || !improvedRprop) {
							// reverse last update step
							predecessorWeights[i][j] -= lastUpdates[i][j];
						}
						// for normal rprop
						gradients[i][j] = 0.0;
					}
				}
			}
			lastErr = err;// copy err to lastErr for reference in the next step
		}

		// double[][] inputs = lesson.getInputs();
		// double[][] desiredOutputs = lesson.getDesiredOutputs();
		//
		// // initialize learning rates if necessary
		// if (shadows.get(SHADOWKEY_resilientBackpropagationLearningRates) ==
		// null) {
		// double[][] learningRatesToInit =
		// createShadow(SHADOWKEY_resilientBackpropagationLearningRates);
		// for (int i = 0; i < learningRatesToInit.length; i++) {
		// for (int j = 0; j < learningRatesToInit[i].length; j++) {
		// learningRatesToInit[i][j] = resilientBackpropagationDeltaZero;
		// }
		// }
		// }
		// // initialize gradients if neccessary
		// if (shadows.get(SHADOWKEY_resilientBackpropagationGradients) == null)
		// {
		// createShadow(SHADOWKEY_resilientBackpropagationGradients);
		// }
		//
		// // from this point on it is made sure that the shadow exists and has
		// // been initialized. it can therefore be retrieved from the treemap
		// and
		// // used.
		// double[][] learningRates = shadows
		// .get(SHADOWKEY_resilientBackpropagationLearningRates);
		// double[][] gradients = shadows
		// .get(SHADOWKEY_resilientBackpropagationGradients);
		//
		// if (descriptor.isAllowBackwardSynapses()
		// || descriptor.isAllowBackwardShortcutSynapses()
		// || descriptor.isAllowLateralSynapses()
		// || descriptor.isAllowSelfSynapses()) {
		// throw new IllegalArgumentException(
		// "Can't rprop. Only forward and forward shortcut Synapses are allowed.");
		// }
		//
		// for (int run = 0; run < runs; run++) {
		// // calculate Deltas
		// double[] delta = new double[countNeurons() + 1];
		// for (int i = countNeurons(); i >= getNeuronFirstInLayer(1); i--) {
		// delta[i] = 0;
		// // second part of delta depending on kind of neuron
		// if (isNeuronOutput(i)) {
		// // Propagate Sample
		// for (int chosenSample = 0; chosenSample < lesson
		// .countSamples(); chosenSample++) {
		// double[] outputs = propagate(inputs[chosenSample]);
		// delta[i] +=
		// (desiredOutputs[chosenSample][mapOutputNeuronToOutputNumber(i)] -
		// outputs[mapOutputNeuronToOutputNumber(i)]);
		// }
		// // first part of delta
		// delta[i] *= neuronBehaviors[i]
		// .computeDerivative(netInputs[i]);
		// } else {
		// double temp = 0;
		// // collect delta from connected neuron
		// for (int j = 0; j < successors[i].length; j++) {
		// temp +=
		// (predecessorWeights[successors[i][j]][successorWeightIndexInPendantPredecessorArray[i][j]]
		// * delta[successors[i][j]]);
		// }
		// delta[i] *= temp;
		// }
		// }
		//
		// // alter weights
		// for (int i = 0; i < countNeurons() + 1; i++) {
		// for (int j = 0; j < predecessors[i].length; j++) {
		// // behandelt wird synapse predecessors[i][j]-->i
		//
		// // behandelt wird synapse predecessors[i][j]-->i
		// // Fehler nach gewicht(predecessors[i][j],i) abgeleitet ist
		// // gleich
		// // -delta(i) * o(predecessors[i][j])
		// double newGradient = -delta[i]
		// * activations[predecessors[i][j]];
		// double decisionValue = newGradient * gradients[i][j];
		// boolean done = false;
		// if (Math.abs(decisionValue) < EPSILON) {
		// // gradient "zero"
		// done = true;
		// predecessorWeights[i][j] += -(Math
		// .signum(gradients[i][j])) * learningRates[i][j];
		// gradients[i][j] = newGradient;
		// }
		// if (decisionValue > 0.0 && !done) {
		// // gradient did not change signum
		// learningRates[i][j] = learningRates[i][j]
		// * resilientBackpropagationEtaPlus;
		// if (learningRates[i][j] > resilientBackpropagationDeltaMax) {
		// learningRates[i][j] = resilientBackpropagationDeltaMax;
		// }
		// predecessorWeights[i][j] += -(Math
		// .signum(gradients[i][j])) * learningRates[i][j];
		// gradients[i][j] = newGradient;
		// }
		// if (decisionValue < 0.0 && !done) {
		// // gradient changed signum
		// learningRates[i][j] = learningRates[i][j]
		// * resilientBackpropagationEtaMinus;
		// if (learningRates[i][j] > resilientBackpropagationDeltaMin) {
		// learningRates[i][j] = resilientBackpropagationDeltaMin;
		// }
		// gradients[i][j] = 0.0;
		// }
		// }
		// }
		// }
	}

	/**
	 * Checks whether the given neuron is an output neuron with computational
	 * effort in O(1).
	 * 
	 * @param neuron
	 * @return if the given neuron is an output neuron.
	 */
	public boolean isNeuronOutput(int neuron) {
		return (getLayerOfNeuron(neuron) == (countLayers() - 1));
	}

	/**
	 * Checks whether the given neuron is a hidden neuron with computational
	 * effort in O(1).
	 * 
	 * @param neuron
	 * @return if the given neuron is a hidden neuron.
	 */
	public boolean isNeuronHidden(int neuron) {
		return (!isNeuronInput(neuron) && !isNeuronOutput(neuron) && (neuron != 0));
	}

	/**
	 * Returns the neuron indices of a existing synapse selected uniformly
	 * random with worst-case computational effort in O(NEURONS). If no such
	 * synapse exists, null is returned.
	 * 
	 * @return the neuron indices of a existing synapse selected uniformly
	 *         random. result[0] is the predecessor, result[1] the successor.
	 *         Returns null if no such synapse exists.
	 */
	public int[] getSynapseRandomExistent() {
		if (countSynapses() == 0) {
			return null;
		}

		// choose a synapse
		int chosenSynapse = getRandomIntegerBetweenIncluding(1, countSynapses());

		// prepare result array
		int[] result = new int[2];

		// ziehe so lange unterarraylängen von der chosensynapse ab, bis
		// chosensynapse <= aktuellem unterarray
		// dann dekrementiere chosenSynapse um eins (arraynummerierung beginnt
		// bei null)
		for (int i = 0; i < predecessors.length; i++) {
			// System.out.println(i + " -- chosenSynapse: " + chosenSynapse +
			// "\t PredILenght: " + predecessors[i].length + "\t Neurons: " +
			// countNeurons());
			if (chosenSynapse <= predecessors[i].length) {
				// chosen synapse is in the current subarray
				chosenSynapse--;
				result[0] = predecessors[i][chosenSynapse];
				result[1] = i;
				return result;
			} else {
				chosenSynapse -= predecessors[i].length;
			}
		}

		// this can't happen
		return null;

	}

	/**
	 * Returns the neuron indices of a non existing, even though allowed synapse
	 * selected uniformly random with expected worst case computational effort
	 * in O(ALLOWEDSYNAPSES*LOG(SYNAPSES)). Exact computational effort is
	 * O(ALLOWEDSYNAPSES*LOG(SYNAPSES)+EPSILON*NOTALLOWEDSYNAPSES) in worst
	 * case, where EPSILON is very small. If no such synapse exists, null is
	 * returned.
	 * 
	 * @return the neuron indices of a existing synapse selected uniformly
	 *         random. result[0] is the predecessor, result[1] the successor.
	 *         Returns null of no such synapse exists.
	 */
	public int[] getSynapseNonExistentAllowedRandom() {
		int possibleSynapses = countNonExistentAllowedSynapses();
		if (possibleSynapses == 0) {
			return null;
		}
		int chosenSynapse = getRandomIntegerBetweenIncluding(1,
				possibleSynapses);
		int[] result = new int[2];
		for (int i = 0; i < (countNeurons() + 1); i++) {
			for (int j = 0; j < (countNeurons() + 1); j++) {
				if (isSynapseAllowed(i, j) && isSynapseExistent(i, j)) {
					chosenSynapse--;
					if (chosenSynapse == 0) {
						result[0] = i;
						result[1] = j;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Searches the least important of all existing feed forward synapses, using
	 * the nonconvergent method of Finnoff 1993.
	 * 
	 * @param lesson
	 *            the lesson to measure
	 * @return the least important existing feed forward connection according to
	 *         finnoff's nonconvergent method or null if no synapse exists.
	 */
	public int[] getSynapseFeedForwardExistingLeastImportantNonConvergentMethod(
			TrainingSampleLesson lesson) {

		double[][] inputs = lesson.getInputs();
		double[][] desiredOutputs = lesson.getDesiredOutputs();

		if (descriptor.isAllowBackwardSynapses()
				|| descriptor.isAllowBackwardShortcutSynapses()
				|| descriptor.isAllowLateralSynapses()
				|| descriptor.isAllowSelfSynapses()) {
			throw new IllegalArgumentException(
					"Can't use nonconvergent method. Only forward and forward shortcut Synapses are allowed.");
		}

		if (countSynapses() == 0) {
			return null;
		}

		// calculate Deltas
		double[] delta = new double[countNeurons() + 1];
		for (int i = countNeurons(); i >= getNeuronFirstInLayer(1); i--) {
			delta[i] = 0;
			// second part of delta depending on kind of neuron
			if (isNeuronOutput(i)) {
				// Propagate Sample
				for (int chosenSample = 0; chosenSample < lesson.countSamples(); chosenSample++) {
					double[] outputs = propagate(inputs[chosenSample]);
					delta[i] += (desiredOutputs[chosenSample][mapOutputNeuronToOutputNumber(i)] - outputs[mapOutputNeuronToOutputNumber(i)]);
				}
				// first part of delta
				delta[i] *= neuronBehaviors[i].computeDerivative(netInputs[i]);
			} else {
				double temp = 0;
				// collect delta from connected neuron
				for (int j = 0; j < successors[i].length; j++) {
					temp += (predecessorWeights[successors[i][j]][successorWeightIndexInPendantPredecessorArray[i][j]] * delta[successors[i][j]]);
				}
				delta[i] *= temp;
			}
		}

		// generate testVariables as SynapseShadow
		double[][] testVariables = createShadowUnmanaged();
		double testVariableAbsoluteSum = 0;
		double testVariableSum = 0;
		for (int i = 0; i < countNeurons() + 1; i++) {
			for (int j = 0; j < predecessors[i].length; j++) {

				// behandelt wird synapse predecessors[i][j]-->i
				// Fehler nach gewicht(predecessors[i][j],i) abgeleitet ist
				// gleich -delta(i) * o(predecessors[i][j])
				testVariables[i][j] = predecessorWeights[i][j] - delta[i]
						* activations[predecessors[i][j]];
				testVariableAbsoluteSum += Math.abs(testVariables[i][j]);
				testVariableSum += testVariables[i][j];
			}
		}
		double testVariableMean = testVariableSum / countSynapses();
		double smallestValue = Double.MAX_VALUE;
		int[] result = new int[2];

		// for all predecessor synapses of every neuron calculate test value and
		// search for smallest one
		for (int i = 0; i < countNeurons() + 1; i++) {
			for (int j = 0; j < predecessors[i].length; j++) {
				// behandelt wird synapse predecessors[i][j]-->i
				double finalTestValue = testVariables[i][j] - testVariableMean;
				finalTestValue *= finalTestValue;
				finalTestValue = testVariableAbsoluteSum
						/ Math.sqrt(finalTestValue);
				if (finalTestValue < smallestValue) {
					smallestValue = finalTestValue;
					result[0] = predecessors[i][j];
					result[1] = i;
				}
			}
		}
		return result;
	}

	/**
	 * Creates a synapse from every neuron in the source layer to every neuron
	 * in the target layer, which may be the same, with computational effort
	 * O(SYNAPSESTOCREATE * SYNAPSECREATIONCOST) (look in the setSynapse
	 * documentation to learn about the latter). Synapses that are not allowed
	 * will not be created. Existing synapses will be overwritten. Synapses will
	 * be assigned a random value dependent on the synapseInitialRange defined
	 * in the descriptor.
	 * 
	 * @param sourceLayer
	 * @param targetLayer
	 */
	public void createSynapsesFromLayerToLayer(int sourceLayer, int targetLayer) {
		if (sourceLayer > countLayers() - 1 || sourceLayer < 0) {
			throw new IllegalArgumentException("Source layer doesn't exist.");
		}
		if (targetLayer > countLayers() - 1 || targetLayer < 0) {
			throw new IllegalArgumentException("Target layer doesn't exist.");
		}
		if (targetLayer == 0) {
			throw new IllegalArgumentException("Input layer can't be a target.");
		}

		for (int source = getNeuronFirstInLayer(sourceLayer); source <= getNeuronLastInLayer(sourceLayer); source++) {
			for (int target = getNeuronFirstInLayer(targetLayer); target <= getNeuronLastInLayer(targetLayer); target++) {
				if (isSynapseAllowed(source, target)) {
					setSynapse(source, target, Double.NaN);
				}
			}
		}
	}

	/**
	 * Creates a synapse from every neuron in the source layer to every neuron
	 * in the target layer (which may be the same) -- but only with a given
	 * probability and with computational effort O(SYNAPSESTOCREATE *
	 * SYNAPSECREATIONCOST) (look in the setSynapse documentation to learn about
	 * the latter). Synapses that are not allowed will not be created. Existing
	 * synapses from sourceLayer to targetLayer are not deleted prior to this.
	 * If you want to do so, use the removeSynapsesFromLayerToLayer method.
	 * However, existing synapses may be overwritten by new ones. New synapses
	 * will be assigned a random value dependent on the synapseInitialRange
	 * defined in the descriptor.
	 * 
	 * <p>
	 * An example where to use this method is if you want to create sparse
	 * dynamic reservoirs to create echo state networks.
	 * 
	 * 
	 * @param sourceLayer
	 * @param targetLayer
	 * @param probability
	 *            must be between 0 and 1, inclusive
	 */
	public void createSynapsesFromLayerToLayerWithProbability(int sourceLayer,
			int targetLayer, double probability) {
		if (sourceLayer > countLayers() - 1 || sourceLayer < 0) {
			throw new IllegalArgumentException("Source layer doesn't exist.");
		}
		if (targetLayer > countLayers() - 1 || targetLayer < 0) {
			throw new IllegalArgumentException("Target layer doesn't exist.");
		}
		if (targetLayer == 0) {
			throw new IllegalArgumentException("Input layer can't be a target.");
		}
		if (probability > 1.0 || probability < 0.0) {
			throw new IllegalArgumentException("Probability out of range.");
		}

		for (int source = getNeuronFirstInLayer(sourceLayer); source <= getNeuronLastInLayer(sourceLayer); source++) {
			for (int target = getNeuronFirstInLayer(targetLayer); target <= getNeuronLastInLayer(targetLayer); target++) {
				if (isSynapseAllowed(source, target)) {
					if (random.nextBoolean(probability)) {
						setSynapse(source, target, Double.NaN);
					}
				}
			}
		}
	}

	/**
	 * Removes synapse that point from every neuron in the source layer to every
	 * neuron in the target layer, which may be the same, with computational
	 * effort O(SYNAPSESTOREMOVE * SYNAPSECREATIONCOST) (look in the setSynapse
	 * documentation to learn about the latter).
	 * 
	 * @param sourceLayer
	 * @param targetLayer
	 */
	public void removeSynapsesFromLayerToLayer(int sourceLayer, int targetLayer) {
		if (sourceLayer > countLayers() - 1 || sourceLayer < 0) {
			throw new IllegalArgumentException("Source layer doesn't exist.");
		}
		if (targetLayer > countLayers() - 1 || targetLayer < 0) {
			throw new IllegalArgumentException("Target layer doesn't exist.");
		}
		if (targetLayer == 0) {
			throw new IllegalArgumentException("Input layer can't be a target.");
		}

		for (int source = getNeuronFirstInLayer(sourceLayer); source <= getNeuronLastInLayer(sourceLayer); source++) {
			for (int target = getNeuronFirstInLayer(targetLayer); target <= getNeuronLastInLayer(targetLayer); target++) {
				removeSynapse(source, target);
			}
		}
	}

	/**
	 * Searches the most important of all not existing feed forward synapses,
	 * using the nonconvergent method of Finnoff 1993.
	 * 
	 * 
	 * @param set
	 *            the trainingset to measure
	 * @return the most important not existing feed forward connection according
	 *         to finnoff's nonconvergent method
	 */
	public int[] getSynapseFeedForwardNonExistingMostImportantNonConvergentMethod(
			TrainingSampleLesson set) {

		double[][] inputs = set.getInputs();
		double[][] desiredOutputs = set.getDesiredOutputs();

		if (descriptor.isAllowBackwardSynapses()
				|| descriptor.isAllowBackwardShortcutSynapses()
				|| descriptor.isAllowLateralSynapses()
				|| descriptor.isAllowSelfSynapses()) {
			throw new IllegalArgumentException(
					"Can't use nonconvergent method. Only forward and forward shortcut Synapses are allowed.");
		}

		if (countNonExistentAllowedSynapses() == 0) {
			return null;
		}

		// calculate Deltas
		double[] delta = new double[countNeurons() + 1];
		for (int i = countNeurons(); i >= getNeuronFirstInLayer(1); i--) {
			delta[i] = 0;
			// second part of delta depending on kind of neuron
			if (isNeuronOutput(i)) {
				// Propagate Sample
				for (int chosenSample = 0; chosenSample < set.countSamples(); chosenSample++) {
					double[] outputs = propagate(inputs[chosenSample]);
					delta[i] += (desiredOutputs[chosenSample][mapOutputNeuronToOutputNumber(i)] - outputs[mapOutputNeuronToOutputNumber(i)]);
				}
				// first part of delta
				delta[i] *= neuronBehaviors[i].computeDerivative(netInputs[i]);
			} else {
				double temp = 0;
				// collect delta from connected neuron
				for (int j = 0; j < successors[i].length; j++) {
					temp += (predecessorWeights[successors[i][j]][successorWeightIndexInPendantPredecessorArray[i][j]] * delta[successors[i][j]]);
				}
				delta[i] *= temp;
			}
		}

		double[][] testVariables = createShadowUnmanaged();
		double testVariableAbsoluteSum = 0;
		double testVariableSum = 0;
		for (int i = 0; i < countNeurons() + 1; i++) {
			for (int j = 0; j < predecessors[i].length; j++) {

				// behandelt wird synapse predecessors[i][j]-->i
				// Fehler nach gewicht(predecessors[i][j],i) abgeleitet ist
				// gleich -delta(i) * o(predecessors[i][j])
				testVariables[i][j] = predecessorWeights[i][j] - delta[i]
						* activations[predecessors[i][j]];
				testVariableAbsoluteSum += Math.abs(testVariables[i][j]);
				testVariableSum += testVariables[i][j];
			}
		}

		// +1 for the empty one
		int virtualSynapseNumber = countSynapses() + 1;
		double testVariableMean = testVariableSum / virtualSynapseNumber;
		double greatestValue = Double.MIN_VALUE;
		int[] result = new int[2];

		for (int i = 0; i < (countNeurons() + 1); i++) {
			for (int j = 0; j < (countNeurons() + 1); j++) {
				if (isSynapseAllowed(i, j) && !isSynapseExistent(i, j)) {
					double virtualTestVariable = -delta[j] * activations[i];
					testVariableMean += (virtualTestVariable / virtualSynapseNumber);
					testVariableAbsoluteSum += Math.abs(virtualTestVariable);
					testVariableSum += virtualTestVariable;
					double finalTestValue = virtualTestVariable
							- testVariableMean;
					finalTestValue *= finalTestValue;
					finalTestValue = testVariableAbsoluteSum
							/ Math.sqrt(finalTestValue);
					if (finalTestValue > greatestValue) {
						greatestValue = finalTestValue;
						result[0] = i;
						result[1] = j;
					}

					testVariableMean -= (virtualTestVariable / virtualSynapseNumber);
					testVariableAbsoluteSum -= Math.abs(virtualTestVariable);
					testVariableSum -= virtualTestVariable;
				}
			}
		}

		return result;

	}

	/**
	 * Returns the number of non existent, even though allowed synapses with
	 * computational effort in
	 * O(ALLOWEDSYNAPSES*LOG(SYNAPSES)+NOTALLOWEDSYNAPSES) worst case. Could
	 * probably be optimized once I have time to do this.
	 * 
	 * @return the number of non existent, even though allowed synapses.
	 */
	public int countNonExistentAllowedSynapses() {
		int result = 0;
		for (int i = 0; i < (countNeurons() + 1); i++) {
			for (int j = 0; j < (countNeurons() + 1); j++) {
				if (isSynapseAllowed(i, j) && !isSynapseExistent(i, j)) {
					result++;
				}
			}
		}
		return result;
	}

	/**
	 * Assigns a Neuron Behavior to a neuron. Note that the behavior instance is
	 * not cloned. The neuron is assigned the very same instance given as an
	 * argument at time of method invocation.
	 * 
	 * @param neuron
	 * @param behavior
	 */
	public void setNeuronBehavior(int neuron, NeuronBehavior behavior) {
		if (neuron < 1 || neuron > countNeurons()) {
			throw new IllegalArgumentException(
					"Illegal Neuron to set behavior.");
		}
		neuronBehaviors[neuron] = behavior;
	}

	/**
	 * Gets a Neuron Behavior from a neuron. Note that the behavior instance is
	 * not cloned. This method returns the very same behavior instance assigned
	 * to the given neuron. This method can for example be used to edit
	 * particular neuron behaviors, like changing neuron locations in Radial
	 * Basis Function Networks.
	 * 
	 * @param neuron
	 * @return the behavior of the given neuron
	 */
	public NeuronBehavior getNeuronBehavior(int neuron) {
		if (neuron < 1 || neuron > countNeurons()) {
			throw new IllegalArgumentException(
					"Illegal Neuron to get behavior.");
		}
		return neuronBehaviors[neuron];
	}

	/**
	 * Checks whether the given neuron is an input neuron with computational
	 * effort in O(1).
	 * 
	 * @param neuron
	 * @return if the given neuron is an input neuron.
	 */
	public boolean isNeuronInput(int neuron) {
		return (getLayerOfNeuron(neuron) == 0);
	}

	/**
	 * Checks whether a synapse is allowed with computational effort in O(1).
	 * 
	 * @param fromNeuron
	 * @param toNeuron
	 * @return if the synapse from the neuron to the other neuron is allowed.
	 */
	public boolean isSynapseAllowed(int fromNeuron, int toNeuron) {

		// synapse to input neuron or bias
		if (toNeuron < getNeuronFirstInLayer(1)) {
			// target neuron is either bias neuron or input neuron
			return false;
		}
		// synapses from bias
		if ((fromNeuron == 0) && (toNeuron >= getNeuronFirstInLayer(1))) {
			// target neuron is either bias neuron or input neuron
			return true;
		}

		// backward Synapses
		if ((getLayerOfNeuron(fromNeuron) - 1) == getLayerOfNeuron(toNeuron)) {
			return descriptor.isAllowBackwardSynapses();
		}

		// backward ShortcutSynapses
		if ((getLayerOfNeuron(fromNeuron) - 1) > getLayerOfNeuron(toNeuron)) {
			return descriptor.isAllowBackwardShortcutSynapses();
		}

		// forward Synapses
		if ((getLayerOfNeuron(fromNeuron) + 1) == getLayerOfNeuron(toNeuron)) {
			return descriptor.isAllowForwardSynapses();
		}

		// forward ShortcutSynapses
		if ((getLayerOfNeuron(fromNeuron) + 1) < getLayerOfNeuron(toNeuron)) {
			return descriptor.isAllowForwardShortcutSynapses();
		}

		// lateral Synapses
		if (!(fromNeuron == toNeuron)
				&& getLayerOfNeuron(fromNeuron) == getLayerOfNeuron(toNeuron)) {
			return descriptor.isAllowLateralSynapses();
		}

		// self Synapses
		if (fromNeuron == toNeuron) {
			return descriptor.isAllowSelfSynapses();
		}

		return false;
	}

	/**
	 * Returns the layer number from a neuron with computational effort in
	 * O(LAYERS). Remember the Neuron Indices: The first neuron is the bias
	 * neuron, after that all input neurons (layer 0) are enumerated, after that
	 * layer 1,2,... and so on. The last layer is the output layer.
	 * 
	 * @param neuronNumber
	 * @return the layer number of the given Neuron.
	 */
	public int getLayerOfNeuron(int neuronNumber) {
		if (neuronNumber == 0) {
			// bias neuron
			return -1;
		}
		int layer = 0;
		while (layer < countLayers()
				&& neuronNumber >= getNeuronFirstInLayer(layer)) {
			layer++;
		}
		layer--;
		return layer;
	}

	/**
	 * Returns the number of layers of this network with computational effort in
	 * O(1). Layer 0 is the input layer, the return of this function decreased
	 * by 1 is the output layer.
	 * 
	 * @return the number of layers
	 */
	public int countLayers() {
		return neuronsPerLayer.length;
	}

	/**
	 * Returns the number of all neurons in this neural network with
	 * computational effort in O(1). Doesn't count the bias.
	 * 
	 * @return the number of all neurons
	 */
	public int countNeurons() {
		return predecessors.length - 1; // -1 because of bias is not counted
	}

	/**
	 * Adds a neuron to the end of a random inner layer uniformly chosen with
	 * computational effort in O(NEURONS+SYNAPSES) (worst case). No synapses are
	 * added, thus the neuron remains unconnected.
	 * 
	 * @return the number of the new neuron.
	 */
	public int createNeuronInRandomLayer() {
		// choose an inner layer
		int choice = random.nextInt(countLayers() - 2) + 1;
		return createNeuronInLayer(choice);
	}

	/**
	 * Splits a synapse from neuron i to neuron k by removing it and inserting
	 * another neuron j and two synapses from i to j and from j to k. This
	 * structural mutation operator is proposed in [Stanley02].
	 * 
	 * <p>
	 * If the Neural Network consists of only 2 layers (input and output), an
	 * exception is thrown for neurons cannot be added to any of those layers.
	 * 
	 * <p>
	 * The new neuron will be added to the layer of k. If k is an output neuron,
	 * it will be added to the layer before the layer of k.
	 * 
	 * <p>
	 * Let x be the synaptic weight of the former synapse from i to k. Then the
	 * synapse from i to j is given the weight 1, and that from j to k is given
	 * the weight x.
	 * 
	 * <p>
	 * This mutation operator induces a further nonlinearity, but keeps the
	 * behavioral gap reasonably small.
	 * 
	 * @param i
	 *            neuron i index
	 * @param k
	 *            neuron k index
	 * @return the index of the neuron j that was added
	 */
	public int mutateTopologySplitSynapseAndAddNeuron(int i, int k) {
		if (countLayers() <= 2) {
			throw new IllegalArgumentException(
					"Too few layers, need at least one hidden layer to split synapse and add neuron.");
		}

		int targetLayer = getLayerOfNeuron(k);

		if (isNeuronOutput(k)) {
			targetLayer--;
		}

		int j = createNeuronInLayer(targetLayer);

		double weight = getWeight(i, k);
		removeSynapse(i, k);
		setSynapse(i, j, 1);
		setSynapse(j, k, weight);

		return j;

	}

	/**
	 * Splits an inner Neuron according to the cell division process in
	 * [Odri93].
	 * 
	 * <p>
	 * In order to do this, the given inner neuron x is split in two neurons y
	 * and z, in a way that keeps a strong behavioral link between x and (y and
	 * z).
	 * 
	 * <p>
	 * The new neurons y and z get the same incident synapses as x. Incoming
	 * synapses' weights are left untouched, and outgoing synapses' weights are
	 * multiplied by (1+alpha) for neuron y and by (-alpha) for neuron z.
	 * 
	 * <p>
	 * The parameter alpha is chosen uniformly random between 0 and 1 for each
	 * invocation of this method.
	 * 
	 * <p>
	 * Special case: Self Connections of the neuron to split will be reproduced
	 * as self connections at the new neuron also.
	 * 
	 * @param neuron
	 *            the neuron to split.
	 * @return the index of the neuron that was created.
	 */
	public int mutateTopologySplitNeuron(int neuron) {
		if (!isNeuronHidden(neuron)) {
			throw new IllegalArgumentException(
					"No input, output or bias neuron can be split.");
		}
		double alpha = random.nextDouble();

		// add neuron
		int newNeuron = createNeuronInLayer(getLayerOfNeuron(neuron));

		// add incoming synapses
		for (int i = 0; i < predecessors[neuron].length; i++) {
			if (predecessors[neuron][i] != neuron) {
				// normal synapse
				setSynapse(predecessors[neuron][i], newNeuron,
						predecessorWeights[neuron][i]);
			} else {
				// self synapse
				setSynapse(newNeuron, newNeuron, predecessorWeights[neuron][i]);
			}
		}

		// add outgoing synapses
		for (int i = 0; i < successors[neuron].length; i++) {
			if (successors[neuron][i] != neuron) {
				// normal synapse
				setSynapse(
						newNeuron,
						successors[neuron][i],
						predecessorWeights[successors[neuron][i]][successorWeightIndexInPendantPredecessorArray[neuron][i]]);
			}
			// self synapses were already added during the addition of incoming
			// synapses.
		}

		// multiply outgoing synapses of old neuron by -alpha
		for (int i = 0; i < successors[neuron].length; i++) {
			predecessorWeights[successors[neuron][i]][successorWeightIndexInPendantPredecessorArray[neuron][i]] *= -alpha;
		}

		// multiply outgoing synapses of new neuron by 1+alpha
		for (int i = 0; i < successors[newNeuron].length; i++) {
			predecessorWeights[successors[newNeuron][i]][successorWeightIndexInPendantPredecessorArray[newNeuron][i]] *= (1 + alpha);
		}

		return newNeuron;
	}

	/**
	 * Adds a neuron to the end of a given layer with computational effort in
	 * O(NEURONS+SYNAPSES) (worst case). No Synapses are added, thus the neuron
	 * remains unconnected.
	 * 
	 * @param layer
	 * @return the number of the new neuron.
	 */
	public int createNeuronInLayer(int layer) {
		// check if position in input or output layer or not existent
		if (layer < 1 || layer > countLayers() - 2) {
			throw new IllegalArgumentException(
					"Can only add neurons to inner layers. You chose layer "
							+ layer);
		}

		// move back all layer starting neurons the layer to extend by one
		// neuron
		for (int i = 0; i < countLayers(); i++) {
			if (i > layer) {
				layerStartingNeurons[i] += 1;
			}
		}

		// neuron will be added to end of layer
		int positionOfNewNeuron = getNeuronFirstInLayer(layer + 1) - 1;

		// increase netinput array
		double[] newNetInputs = new double[netInputs.length + 1];
		for (int i = 0; i < newNetInputs.length; i++) {
			if (i < positionOfNewNeuron) {
				newNetInputs[i] = netInputs[i];
			}
			if (i == positionOfNewNeuron) {
				newNetInputs[i] = 0.0;
			}
			if (i > positionOfNewNeuron) {
				newNetInputs[i] = netInputs[i - 1];
			}
		}
		netInputs = newNetInputs;

		// increase activation array
		double[] newActivations = new double[activations.length + 1];
		for (int i = 0; i < newActivations.length; i++) {
			if (i < positionOfNewNeuron) {
				newActivations[i] = activations[i];
			}
			if (i == positionOfNewNeuron) {
				newActivations[i] = 0.0;
			}
			if (i > positionOfNewNeuron) {
				newActivations[i] = activations[i - 1];
			}
		}
		activations = newActivations;

		// increase neuron behavior array
		NeuronBehavior[] newNeuronBehaviors = new NeuronBehavior[neuronBehaviors.length + 1];
		for (int i = 0; i < newNeuronBehaviors.length; i++) {
			if (i < positionOfNewNeuron) {
				newNeuronBehaviors[i] = neuronBehaviors[i];
			}
			if (i == positionOfNewNeuron) {
				if (descriptor.getNeuronBehaviorHiddenNeurons()
						.needsDedicatedInstancePerNeuron()) {
					newNeuronBehaviors[i] = descriptor
							.getNeuronBehaviorHiddenNeurons()
							.getDedicatedInstance();
				} else {
					newNeuronBehaviors[i] = descriptor
							.getNeuronBehaviorHiddenNeurons();
				}
			}
			if (i > positionOfNewNeuron) {
				newNeuronBehaviors[i] = neuronBehaviors[i - 1];
			}
		}
		neuronBehaviors = newNeuronBehaviors;

		// increase Neurons per layer
		neuronsPerLayer[layer] += 1;

		// update indices: All neuron indices >= positionOfNewNeuron will
		// be increased.
		for (int i = 0; i < countNeurons() + 1; i++) {
			for (int j = 0; j < predecessors[i].length; j++) {
				if (predecessors[i][j] >= positionOfNewNeuron) {
					predecessors[i][j]++;
				}
			}
			for (int j = 0; j < successors[i].length; j++) {
				if (successors[i][j] >= positionOfNewNeuron) {
					successors[i][j]++;
				}
			}
		}

		// Update Synapse Data Structure
		int[][] newPredecessors = new int[countNeurons() + 2][];
		double[][] newPredecessorWeights = new double[countNeurons() + 2][];
		int[][] newSuccessors = new int[countNeurons() + 2][];
		int[][] newSuccessorWeightIndexInPendantPredecessorArray = new int[countNeurons() + 2][];

		for (int i = 0; i < countNeurons() + 2; i++) {
			// smaller indices than positionOfNewNeuron are just copied.
			if (i < positionOfNewNeuron) {
				newPredecessors[i] = predecessors[i];
				newSuccessors[i] = successors[i];
				newPredecessorWeights[i] = predecessorWeights[i];
				newSuccessorWeightIndexInPendantPredecessorArray[i] = successorWeightIndexInPendantPredecessorArray[i];

			}

			// greater ones smaller indices than positionOfNewNeuron are just
			// copied.
			if (i > positionOfNewNeuron) {
				newPredecessors[i] = predecessors[i - 1];
				newSuccessors[i] = successors[i - 1];
				newPredecessorWeights[i] = predecessorWeights[i - 1];
				newSuccessorWeightIndexInPendantPredecessorArray[i] = successorWeightIndexInPendantPredecessorArray[i - 1];

			}
		}

		// create storage for new neuron in data structure
		newPredecessors[positionOfNewNeuron] = new int[0];
		newSuccessors[positionOfNewNeuron] = new int[0];
		newPredecessorWeights[positionOfNewNeuron] = new double[0];
		newSuccessorWeightIndexInPendantPredecessorArray[positionOfNewNeuron] = new int[0];

		// overwrite old data structure
		predecessors = newPredecessors;
		successors = newSuccessors;
		predecessorWeights = newPredecessorWeights;
		successorWeightIndexInPendantPredecessorArray = newSuccessorWeightIndexInPendantPredecessorArray;

		// update shadows the same way
//		Object[] keyIterator = new Object[shadows.keySet().size()];
//		keyIterator=shadows.keySet().toArray();
		int keycount=0, keyit=-1;
		
		if(shadows.containsKey((Object)SHADOWKEY_gaussianMutationAdaptivePertubationVector))
				keycount++;
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationGradients))
			keycount++;
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLastWeightUpdates))
			keycount++;
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLearningRates))
			keycount++;
		Object[] keyIterator = new Object[keycount];
		
		if(shadows.containsKey((Object)SHADOWKEY_gaussianMutationAdaptivePertubationVector)){
			keyit++;
			keyIterator[keyit]=SHADOWKEY_gaussianMutationAdaptivePertubationVector;
		}
	if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationGradients)){
		keyit++;
	keyIterator[keyit]=SHADOWKEY_resilientBackpropagationGradients;}
	if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLastWeightUpdates)){
		keyit++;
	keyIterator[keyit]=SHADOWKEY_resilientBackpropagationLastWeightUpdates;}
	if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLearningRates)){
		keyit++;
	keyIterator[keyit]=SHADOWKEY_resilientBackpropagationLearningRates;}
	for (int j=0;j<keycount;j++){					//Iterator keyIterator =  keyIterator.hasNext();) {
		String key = keyIterator[j].toString();	
//		for (Iterator keyIterator = shadows.keySet().iterator(); keyIterator.hasNext();) {
//			String key = keyIterator.next().toString();

			// get shadow
			double[][] shadow = (double[][])shadows.get(key);

			// create new shadow
			double[][] newShadow = new double[countNeurons() + 2][];

			// fill new shadow
			for (int i = 0; i < countNeurons() + 2; i++) {
				// smaller indices than positionOfNewNeuron are just copied.
				if (i < positionOfNewNeuron) {
					newShadow[i] = shadow[i];
				}

				// greater ones smaller indices than positionOfNewNeuron are
				// just
				// copied.
				if (i > positionOfNewNeuron) {
					newShadow[i] = shadow[i - 1];
				}
			}
			// create storage for new neuron in shadow
			newShadow[positionOfNewNeuron] = new double[0];

			// overwrite old shadow with key
			shadows.put(key, newShadow);

		}

		return positionOfNewNeuron;
	}

	/**
	 * Removes a neuron at the given position as well as all incident synapses
	 * with computational effort in O(NEURONS+SYNAPSES) (worst case) if the
	 * neuron is unconnected, otherwise with additional synapse removal effort.
	 * To learn about synapse removal effort, read the doc of removeSynapse.
	 * Updates the internal data structures. However, you can't remove neurons
	 * in input and output layers.
	 * 
	 * @param neuronToRemove
	 *            the neuron to remove
	 */
	public void removeNeuron(int neuronToRemove) {
		// check if position in input or output layer or not existent
		if (neuronToRemove < getNeuronFirstInLayer(1)
				|| neuronToRemove >= getNeuronFirstInLayer(countLayers() - 1)) {
			throw new IllegalArgumentException(
					"Can only remove neurons from inner layers.");
		}

		// switch off incident synapses
		while (successors[neuronToRemove].length > 0) {
			removeSynapse(neuronToRemove, successors[neuronToRemove][0]);
		}
		while (predecessors[neuronToRemove].length > 0) {
			removeSynapse(predecessors[neuronToRemove][0], neuronToRemove);
		}

		// decrease Neurons per layer
		neuronsPerLayer[getLayerOfNeuron(neuronToRemove)] -= 1;

		// move backward layer starting neurons beyond neuron position
		for (int i = 0; i < countLayers(); i++) {
			if (getNeuronFirstInLayer(i) > neuronToRemove) {
				layerStartingNeurons[i] -= 1;
			}
		}

		// decrease netinput array
		double[] newNetInputs = new double[netInputs.length - 1];
		for (int i = 0; i < newNetInputs.length + 1; i++) {
			if (i < neuronToRemove) {
				newNetInputs[i] = netInputs[i];
			}

			if (i > neuronToRemove) {
				newNetInputs[i - 1] = netInputs[i];
			}
		}
		netInputs = newNetInputs;

		// decrease activation array
		double[] newActivations = new double[activations.length - 1];
		for (int i = 0; i < newActivations.length + 1; i++) {
			if (i < neuronToRemove) {
				newActivations[i] = activations[i];
			}

			if (i > neuronToRemove) {
				newActivations[i - 1] = activations[i];
			}
		}
		activations = newActivations;

		// decrease neuron behavior array
		NeuronBehavior[] newNeuronBehaviors = new NeuronBehavior[neuronBehaviors.length - 1];
		for (int i = 0; i < newNeuronBehaviors.length + 1; i++) {
			if (i < neuronToRemove) {
				newNeuronBehaviors[i] = neuronBehaviors[i];
			}
			if (i > neuronToRemove) {
				newNeuronBehaviors[i - 1] = neuronBehaviors[i];
			}
		}
		neuronBehaviors = newNeuronBehaviors;

		// Update Synapse Data Structure

		int[][] newPredecessors = new int[countNeurons()][];
		double[][] newPredecessorWeights = new double[countNeurons()][];
		int[][] newSuccessors = new int[countNeurons()][];
		int[][] newSuccessorWeightIndexInPendantPredecessorArray = new int[countNeurons()][];

		for (int i = 0; i < countNeurons() + 1; i++) {
			// smaller indices than positionOfNewNeuron are just copied.
			if (i < neuronToRemove) {
				newPredecessors[i] = predecessors[i];
				newSuccessors[i] = successors[i];
				newPredecessorWeights[i] = predecessorWeights[i];
				newSuccessorWeightIndexInPendantPredecessorArray[i] = successorWeightIndexInPendantPredecessorArray[i];
			}

			// greater ones smaller indices than positionOfNewNeuron are just
			// copied and shifted
			if (i > neuronToRemove) {
				newPredecessors[i - 1] = predecessors[i];
				newSuccessors[i - 1] = successors[i];
				newPredecessorWeights[i - 1] = predecessorWeights[i];
				newSuccessorWeightIndexInPendantPredecessorArray[i - 1] = successorWeightIndexInPendantPredecessorArray[i];
			}
		}

		// update indices: All neuron indices >= positionOfNewNeuron will
		// be decreased.
		for (int i = 0; i < newPredecessors.length; i++) {
			for (int j = 0; j < newPredecessors[i].length; j++) {
				if (newPredecessors[i][j] >= neuronToRemove) {
					newPredecessors[i][j]--;
				}
			}
			for (int j = 0; j < newSuccessors[i].length; j++) {
				if (newSuccessors[i][j] >= neuronToRemove) {
					newSuccessors[i][j]--;
				}
			}
		}

		// overwrite old data structure
		predecessors = newPredecessors;
		successors = newSuccessors;
		predecessorWeights = newPredecessorWeights;
		successorWeightIndexInPendantPredecessorArray = newSuccessorWeightIndexInPendantPredecessorArray;

		// update shadows the same way
//		Object[] keyIterator = new Object[shadows.keySet().size()];
//		keyIterator=shadows.keySet().toArray();
		int keycount=0, keyit=-1;
		
		if(shadows.containsKey((Object)SHADOWKEY_gaussianMutationAdaptivePertubationVector))
				keycount++;
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationGradients))
			keycount++;
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLastWeightUpdates))
			keycount++;
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLearningRates))
			keycount++;
		Object[] keyIterator = new Object[keycount];
		
		if(shadows.containsKey((Object)SHADOWKEY_gaussianMutationAdaptivePertubationVector)){
			keyit++;
			keyIterator[keyit]=SHADOWKEY_gaussianMutationAdaptivePertubationVector;
		}
	if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationGradients)){
		keyit++;
	keyIterator[keyit]=SHADOWKEY_resilientBackpropagationGradients;}
	if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLastWeightUpdates)){
		keyit++;
	keyIterator[keyit]=SHADOWKEY_resilientBackpropagationLastWeightUpdates;}
	if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLearningRates)){
		keyit++;
	keyIterator[keyit]=SHADOWKEY_resilientBackpropagationLearningRates;}
	for (int j=0;j<keycount;j++){					//Iterator keyIterator =  keyIterator.hasNext();) {
		String key = keyIterator[j].toString();	
//		for (Iterator keyIterator = shadows.keySet().iterator(); keyIterator.hasNext();) {
//			String key = keyIterator.next().toString();

			// get shadow
			double[][] shadow =(double[][]) shadows.get(key);

			// create new shadow
			double[][] newShadow = new double[countNeurons()][];

			// fill new shadow
			for (int i = 0; i < countNeurons() + 1; i++) {
				// smaller indices than positionOfNewNeuron are just copied.
				if (i < neuronToRemove) {
					newShadow[i] = shadow[i];
				}

				// greater ones smaller indices than positionOfNewNeuron are
				// just
				// copied and shifted
				if (i > neuronToRemove) {
					newShadow[i - 1] = shadow[i];
				}
			}

			// overwrite old shadow with key
			shadows.put(key, newShadow);

		}

	}

	/**
	 * Returns the number of inner neurons with computational effort in O(1).
	 * 
	 * @return the numberOfInnerNeurons
	 */
	public int countNeuronsInner() {
		return countNeurons() - descriptor.countInputNeurons()
				- descriptor.countOutputNeurons();
	}

	/**
	 * Just calls the descriptor's getNumberOfOutputNeurons() and returns the
	 * result with computational effort in O(1). This method exists only for
	 * convenience.
	 * 
	 * @return the number of output neurons
	 */
	public int countNeuronsOutput() {
		return descriptor.countOutputNeurons();
	}

	/**
	 * Just calls the descriptor's getNumberOfInputNeurons() and returns the
	 * result with computational effort in O(1). This method exists only for
	 * convenience.
	 * 
	 * @return the number of input neurons
	 */
	public int countNeuronsInput() {
		return descriptor.countInputNeurons();
	}

	/**
	 * Returns the last activation value of a given neuron.
	 * 
	 * @param neuron
	 * @return the activation for the given neuron.
	 */
	public double getActivation(int neuron) {
		if (neuron > countNeurons() || neuron < 0) {
			throw new IllegalArgumentException("Illegal Neuron.");
		}
		return activations[neuron];
	}

	/**
	 * Sets the value that will be used as the last activation value of a given
	 * neuron. Nice feature for some network topologies, e.g. hopfield networks.
	 * If you do this, be absolutely sure what you are doing.
	 * 
	 * @param neuron
	 * @param activation
	 *            the activation for the given neuron.
	 */
	public void setActivation(int neuron, double activation) {
		if (neuron > countNeurons() || neuron < 0) {
			throw new IllegalArgumentException("Illegal Neuron.");
		}
		activations[neuron] = activation;
	}

	/**
	 * Returns the last net input value of a given neuron.
	 * 
	 * @param neuron
	 * @return the net input for the given neuron.
	 */
	public double getNetInput(int neuron) {
		if (neuron > countNeurons() || neuron < 0) {
			throw new IllegalArgumentException("Illegal Neuron.");
		}
		return netInputs[neuron];
	}

	/**
	 * 
	 * <p>
	 * Parses a Neural Net String built by the exportFromString method and
	 * builds up the neural net contained. The old neural network data will be
	 * discarded.
	 * 
	 * <p>
	 * NOTE: This function includes several SetSynapse calls and thus is not
	 * optimal in calculating complexity, even though it is not too slow either.
	 * 
	 * <p>
	 * IMPORTANT: Synapses that are not allowed from the neural network
	 * descriptor will not be created and no exception will be thrown. If
	 * furthermore the parsed string contains a different number of layers,
	 * inputs, or outputs than defined in the layer, an exception will be thrown
	 * and nothing will be discarded.
	 * 
	 * 
	 * @param description
	 * @throws Exception
	 *             if number of layers, inputs, or outputs is different to those
	 *             in the descriptor or another parsing error occurs
	 */
//	public void importFromString(String description) throws Exception {
//
//		String[] input = description.split(dataDelimiter);
//		if (input.length != 2) {
//			throw new Exception("Invalid data string given.");
//		}
//
//		String layerString = input[0];
//		String weightString = input[1];
//
//		// parse layer data
//		String[] parsedLayerData = layerString.split(";");
//		int[] intLayerData = new int[parsedLayerData.length];
//		for (int i = 0; i < intLayerData.length; i++) {
//			intLayerData[i] = Integer.parseInt(parsedLayerData[i]);
//		}
//
//		// check layer data consistency with descriptor
//		if (intLayerData.length != descriptor.countLayers()) {
//			throw new Exception(
//					"Number of layers not consistent with descriptor.");
//		}
//		if (intLayerData[0] != descriptor.countInputNeurons()) {
//			throw new Exception(
//					"Input dimensionality not consistent with descriptor.");
//		}
//		if (intLayerData[intLayerData.length - 1] != descriptor
//				.countOutputNeurons()) {
//			throw new Exception(
//					"Output dimensionality not consistent with descriptor.");
//		}
//
//		// apply layer data
//		initialize(descriptor, intLayerData);
//
//		// neural net is empty now
//
//		// parse weights of existing synapse and apply them
//		String[] parsedWeights = weightString.split(";");
//
//		for (int singleParsedWeight = 0; singleParsedWeight < parsedWeights.length; singleParsedWeight++) {
//			String[] furtherParsed = parsedWeights[singleParsedWeight]
//					.split(":");
//			int i = Integer.parseInt(furtherParsed[0]);
//			int j = Integer.parseInt(furtherParsed[1]);
//			double weightValue = Double.parseDouble(furtherParsed[2]);
//			if (isSynapseAllowed(i, j)) {
//				setSynapse(i, j, weightValue);
//			}
//		}
//
//	}

	/**
	 * Searches through all inner neurons, which is the least important
	 * according to the sum of incident synapse importance using the
	 * nonconvergent method of Finnoff 1993. Returns the inner neuron index
	 * found with smallest sum of synapse importance or -1 if there is no
	 * synapse respective no inner neuron.
	 * 
	 * @param set
	 *            the training set
	 * @return the neuron index
	 */
	//@SuppressWarnings("unused")
	public int getNeuronInnerLeastImportantNonConvergentMethod(
			TrainingSampleLesson set) {

		double[][] inputs = set.getInputs();
		double[][] desiredOutputs = set.getDesiredOutputs();

		if (descriptor.isAllowBackwardSynapses()
				|| descriptor.isAllowBackwardShortcutSynapses()
				|| descriptor.isAllowLateralSynapses()
				|| descriptor.isAllowSelfSynapses()) {
			throw new IllegalArgumentException(
					"Can't use nonconvergent method. Only forward and forward shortcut Synapses are allowed.");
		}

		if (countSynapses() == 0 || countNeuronsInner() == 0) {
			return -1;
		}

		// calculate Deltas
		double[] delta = new double[countNeurons() + 1];
		for (int i = countNeurons(); i >= getNeuronFirstInLayer(1); i--) {
			delta[i] = 0;
			// second part of delta depending on kind of neuron
			if (isNeuronOutput(i)) {
				// Propagate Sample
				for (int chosenSample = 0; chosenSample < set.countSamples(); chosenSample++) {
					double[] outputs = propagate(inputs[chosenSample]);
					delta[i] += (desiredOutputs[chosenSample][mapOutputNeuronToOutputNumber(i)] - outputs[mapOutputNeuronToOutputNumber(i)]);
				}
				// first part of delta
				delta[i] *= neuronBehaviors[i].computeDerivative(netInputs[i]);
			} else {
				double temp = 0;
				// collect delta from connected neuron
				for (int j = 0; j < successors[i].length; j++) {
					temp += (predecessorWeights[successors[i][j]][successorWeightIndexInPendantPredecessorArray[i][j]] * delta[successors[i][j]]);
				}
				delta[i] *= temp;
			}
		}

		// generate testVariables as SynapseShadow
		double[][] testVariables = createShadowUnmanaged();
		double testVariableAbsoluteSum = 0;
		double testVariableSum = 0;
		for (int i = 0; i < countNeurons() + 1; i++) {
			for (int j = 0; j < predecessors[i].length; j++) {

				// behandelt wird synapse predecessors[i][j]-->i
				// Fehler nach gewicht(predecessors[i][j],i) abgeleitet ist
				// gleich -delta(i) * o(predecessors[i][j])
				testVariables[i][j] = predecessorWeights[i][j] - delta[i]
						* activations[predecessors[i][j]];
				testVariableAbsoluteSum += Math.abs(testVariables[i][j]);
				testVariableSum += testVariables[i][j];
			}
		}
		double testVariableMean = testVariableSum / countSynapses();
		int[] synapseLocation = new int[2];

		// go through all synapses and add absolute test values per neuron.
		// report least important neuron.
		double[] neuronTestValues = new double[countNeurons()];

		// for all neurons calculate test value sum and
		// search for smallest one
		for (int i = 0; i < countNeurons() + 1; i++) {
			for (int j = 0; j < predecessors[i].length; j++) {
				// behandelt wird synapse predecessors[i][j]-->i
				double finalTestValue = testVariables[i][j] - testVariableMean;
				finalTestValue *= finalTestValue;
				finalTestValue = testVariableAbsoluteSum
						/ Math.sqrt(finalTestValue);

				neuronTestValues[predecessors[i][j]] += Math
						.abs(finalTestValue);
				neuronTestValues[i] += Math.abs(finalTestValue);
			}
		}

		int leastImportantNeuron = getNeuronFirstInLayer(1);

		for (int i = getNeuronFirstInLayer(1); i < getNeuronFirstInLayer(1)
				+ countNeuronsInner(); i++) {
			if (neuronTestValues[i] < neuronTestValues[leastImportantNeuron]) {
				leastImportantNeuron = i;
			}
		}
		return leastImportantNeuron;
	}

	/**
	 * Returns the inner neuron with the smallest absolute sum of incident
	 * synapse weights with computational effort in O(SYNAPSES).
	 * 
	 * @return the inner neuron with the smallest absolute sum of incident
	 *         synapse weights.
	 */
	//@SuppressWarnings("unused")
	public int getNeuronInnerLeastConnected() {

		double[] weightSums = new double[countNeurons()];

		int result = getNeuronFirstInLayer(1);

		for (int i = getNeuronFirstInLayer(1); i < getNeuronFirstInLayer(1)
				+ countNeuronsInner(); i++) {
			for (int j = 0; j < predecessors[i].length; j++) {
				weightSums[i] += Math.abs(predecessorWeights[i][j]);
				weightSums[predecessors[i][j]] += Math
						.abs(predecessorWeights[i][j]);
			}
		}

		int targetNeuronIndex = getNeuronFirstInLayer(1);
		double targetNeuronValue = Double.MAX_VALUE;
		for (int i = getNeuronFirstInLayer(1); i < getNeuronFirstInLayer(1)
				+ countNeuronsInner(); i++) {
			if (weightSums[i] <= targetNeuronValue) {
				targetNeuronValue = weightSums[i];
				targetNeuronIndex = i;
			}
		}

		return targetNeuronIndex;

	}

	/**
	 * Returns the inner neuron with the smallest absolute sum of outgoing
	 * synapse weights with computational effort in O(SYNAPSES).
	 * 
	 * @return the inner neuron with the smallest absolute sum of outgoing
	 *         synapse weights.
	 */
	//@SuppressWarnings("unused")
	public int getNeuronInnerLeastSending() {
		double[] weightSums = new double[countNeurons()];

		int result = getNeuronFirstInLayer(1);

		for (int i = getNeuronFirstInLayer(1); i < getNeuronFirstInLayer(1)
				+ countNeuronsInner(); i++) {
			for (int j = 0; j < predecessors[i].length; j++) {
				weightSums[predecessors[i][j]] += Math
						.abs(predecessorWeights[i][j]);
			}
		}

		int targetNeuronIndex = getNeuronFirstInLayer(1);
		double targetNeuronValue = Double.MAX_VALUE;
		for (int i = getNeuronFirstInLayer(1); i < getNeuronFirstInLayer(1)
				+ countNeuronsInner(); i++) {
			if (weightSums[i] <= targetNeuronValue) {
				targetNeuronValue = weightSums[i];
				targetNeuronIndex = i;
			}
		}

		return targetNeuronIndex;

	}

	/**
	 * Returns the inner neuron with the smallest absolute sum of incoming
	 * synapse weights with computational effort in O(SYNAPSES).
	 * 
	 * @return the inner neuron with the smallest absolute sum of incoming
	 *         synapse weights.
	 */
	//@SuppressWarnings("unused")
	public int getNeuronInnerLeastReceiving() {
		double[] weightSums = new double[countNeurons()];

		int result = getNeuronFirstInLayer(1);

		for (int i = getNeuronFirstInLayer(1); i < getNeuronFirstInLayer(1)
				+ countNeuronsInner(); i++) {
			for (int j = 0; j < predecessors[i].length; j++) {
				weightSums[i] += Math.abs(predecessorWeights[i][j]);
			}
		}

		int targetNeuronIndex = getNeuronFirstInLayer(1);
		double targetNeuronValue = Double.MAX_VALUE;
		for (int i = getNeuronFirstInLayer(1); i < getNeuronFirstInLayer(1)
				+ countNeuronsInner(); i++) {
			if (weightSums[i] <= targetNeuronValue) {
				targetNeuronValue = weightSums[i];
				targetNeuronIndex = i;
			}
		}

		return targetNeuronIndex;

	}

	/**
	 * This method returns a string description of this neural network that in
	 * turn can be parsed by the importFromString method. It saves the topology,
	 * namely number of neurons in every layer, synapses and synaptic weights.
	 * No activation and neuron behavior information is stored.
	 * 
	 * @return a string describing the neural network topology.
	 */
	public String exportToString() {
		//StringBuilder layerData = new StringBuilder();
		String layerdata="";
		//StringBuilder weightString = new StringBuilder();
		String weightString="";
		// generate string for neurons per layer
		for (int i = 0; i < neuronsPerLayer.length; i++) {
			layerdata=layerdata+(neuronsPerLayer[i]) + ";";
			//layerData.append(Integer.toString(neuronsPerLayer[i]) + ";");
		}

		// save synapse string
		for (int i = 0; i < predecessors.length; i++) {
			for (int j = 0; j < predecessors[i].length; j++) {
				weightString=weightString+predecessors[i][j] + ":" + i + ":"
						+ predecessorWeights[i][j] + ";";
//				weightString.append(predecessors[i][j] + ":" + i + ":"
//						+ predecessorWeights[i][j] + ";");
			}
		}

		// compose result
		//StringBuilder result = new StringBuilder();
		String result="";
		result=result+layerdata + dataDelimiter;
	//	result.append(layerdata.toString() + dataDelimiter);
	//	result.append(weightString.toString() + dataDelimiter);
		result=result+weightString + dataDelimiter;
		return result;//.toString();

	}

	/**
	 * Returns the number of neurons in a given layer with computational effort
	 * in O(1).
	 * 
	 * @param layer
	 * @return the number of neurons in a given layer.
	 */
	public int countNeuronsInLayer(int layer) {
		return neuronsPerLayer[layer];
	}

	/**
	 * Counts all existing synapses with expected computational effort in O(1).
	 * Exact computational effort is in O(EPSILON*NEURONS), where EPSILON is
	 * very small.
	 * 
	 * @return the numberOfExistingSynapses
	 */
	public int countSynapses() {
		int result = 0;
		for (int i = 0; i < predecessors.length; i++) {
			result += predecessors[i].length;
		}
		return result;
	}

	/**
	 * Uniformly selects a random inner neuron with computational effort in
	 * O(1).
	 * 
	 * @return a uniformly selected inner neuron index or -1 if none exists.
	 */
	public int getNeuronInnerRandom() {
		if (countNeuronsInner() == 0) {
			return -1;
		}
		int choice = random.nextInt(countNeuronsInner());
		choice += descriptor.countInputNeurons() + 1;
		return choice;
	}

	/**
	 * Returns a very simple string representation just printing out the neurons
	 * per layer structure of the neural net and the number of synapses.
	 * 
	 * @return the string representation
	 */
	public String toString() {
		String layerstring = "";
		for (int i = 0; i < neuronsPerLayer.length; i++) {
			layerstring = layerstring + neuronsPerLayer[i];
			if (i != neuronsPerLayer.length - 1) {
				layerstring = layerstring + "-";
			}
		}
		String string = "N: " + layerstring + ", S: " + countSynapses();
		return string;
	}

	/**
	 * Hashes the neural network and returns the hash.
	 * 
	 * @return hash value
	 */
	public int hashCode() {
		// stolen from GPIndividual. It's a decent algorithm.
		int hash = this.getClass().hashCode();

		hash = (hash << 1 | hash >>> 31) ^ predecessorWeights.hashCode()
				^ predecessors.hashCode();

		return hash;
	}

	/**
	 * Returns the NeuralNetworkDescriptor instance that was used to create this
	 * NeuralNetwork.
	 * 
	 * @return the descriptor
	 */
	public NeuralNetworkDescriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * Performs a complete deep copy of this Neural Network and returns it.
	 * Every data structures, caches and shadows are cloned.
	 * 
	 * @return the clone of this object
	 */
	
	private Object 	predecessorsClone1,predecessorsClone2,
					predecessorWeightsClone1,predecessorWeightsClone2,
					successorsClone1,successorsClone2,
					successorWeightIndexInPendantPredecessorArrayClone1, successorWeightIndexInPendantPredecessorArrayClone2;
	
	
	public NeuralNetwork clone() {
		NeuralNetwork clonedNeuralNet = new NeuralNetwork(descriptor);

		// 2dim arrays have to be cloned manually
		predecessorsClone1=predecessors;
		int[][] predecessorsClone = (int[][])predecessorsClone1;//predecessors.clone();
		for (int i = 0; i < predecessorsClone.length; i++) {
			predecessorsClone2=(int[])predecessorsClone[i];
			predecessorsClone[i] = (int[]) predecessorsClone2;//(int[])predecessorsClone[i].clone();
		}
		predecessorWeightsClone1=predecessorWeights;
		double[][] predecessorWeightsClone = (double[][]) predecessorWeightsClone1;//predecessorWeights.clone();
		for (int i = 0; i < predecessorWeightsClone.length; i++) {
			predecessorWeightsClone2=(double[])predecessorWeightsClone[i];
			predecessorWeightsClone[i] =(double[])predecessorWeightsClone2;// predecessorWeightsClone[i].clone();
		}
		
		successorsClone1=successors;
		int[][] successorsClone =(int[][]) successorsClone1;// successors.clone();
		for (int i = 0; i < successorsClone.length; i++) {
			successorsClone2= (int[]) successorsClone[i];
			successorsClone[i] = (int[])successorsClone2;//successorsClone[i].clone();
		}

		successorWeightIndexInPendantPredecessorArrayClone1=successorWeightIndexInPendantPredecessorArray;
		int[][] successorWeightIndexInPendantPredecessorArrayClone = (int[][])successorWeightIndexInPendantPredecessorArrayClone1;//successorWeightIndexInPendantPredecessorArray
				//.clone();
		for (int i = 0; i < successorWeightIndexInPendantPredecessorArrayClone.length; i++) {
			successorWeightIndexInPendantPredecessorArrayClone2=(int[])  successorWeightIndexInPendantPredecessorArrayClone[i];
			successorWeightIndexInPendantPredecessorArrayClone[i] =(int[])  successorWeightIndexInPendantPredecessorArrayClone2;// successorWeightIndexInPendantPredecessorArrayClone[i]
					//.clone();
		}

		// clone shadows, store them in new shadowTreeMap, use new local fields
		// of the new neural network as keys.
		Hashtable synapseShadowsClone=null;// = new Map();

//		for (Iterator keyIterator = shadows.keySet().iterator(); keyIterator.hasNext();) {
//		Object[] keyIterator = new Object[shadows.keySet().size()];
//			keyIterator=shadows.keySet().toArray();
		int keycount=0, keyit=-1;
		
		if(shadows.containsKey((Object)SHADOWKEY_gaussianMutationAdaptivePertubationVector))
				keycount++;
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationGradients))
			keycount++;
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLastWeightUpdates))
			keycount++;
		if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLearningRates))
			keycount++;
		Object[] keyIterator = new Object[keycount];
		
		if(shadows.containsKey((Object)SHADOWKEY_gaussianMutationAdaptivePertubationVector)){
			keyit++;
			keyIterator[keyit]=SHADOWKEY_gaussianMutationAdaptivePertubationVector;
		}
	if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationGradients)){
		keyit++;
	keyIterator[keyit]=SHADOWKEY_resilientBackpropagationGradients;}
	if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLastWeightUpdates)){
		keyit++;
	keyIterator[keyit]=SHADOWKEY_resilientBackpropagationLastWeightUpdates;}
	if(shadows.containsKey((Object)SHADOWKEY_resilientBackpropagationLearningRates)){
		keyit++;
	keyIterator[keyit]=SHADOWKEY_resilientBackpropagationLearningRates;}
			
	
	
			
		for (int j=0;j<keycount;j++){					//Iterator keyIterator =  keyIterator.hasNext();) {
			String key = keyIterator[j].toString();				//String key = keyIterator.next().toString();
			double[][] shadow = (double[][])shadows.get(key);
			double[][] clone = (double[][])shadow;//.clone();
			for (int i = 0; i < clone.length; i++) {
				clone[i] = (double[])shadow[i];//.clone();
			}
			synapseShadowsClone.put(new String(key), clone);

		}

		// 1dim arrays are cloned automatically
		int[] layerStartingNeuronsClone = (int[])layerStartingNeurons;//.clone();
		int[] neuronsPerLayerClone = (int[])neuronsPerLayer;//.clone();
		double[] activationsClone = (double[])activations;//.clone();
		double[] netInputsClone = (double[])netInputs;//.clone();

		NeuronBehavior[] neuronBehaviorsClone = new NeuronBehavior[neuronBehaviors.length];
		for (int i = 0; i < neuronBehaviorsClone.length; i++) {
			if (neuronBehaviors[i] != null) {
				if (neuronBehaviors[i].needsDedicatedInstancePerNeuron()) {
					neuronBehaviorsClone[i] = neuronBehaviors[i].getDedicatedInstance();
				} else {
					neuronBehaviorsClone[i] = neuronBehaviors[i];
				}
			}
		}

		clonedNeuralNet.resilientBackpropagationDeltaZero = resilientBackpropagationDeltaZero;
		clonedNeuralNet.resilientBackpropagationDeltaMax = resilientBackpropagationDeltaMax;
		clonedNeuralNet.resilientBackpropagationDeltaMin = resilientBackpropagationDeltaMin;
		clonedNeuralNet.resilientBackpropagationEtaMinus = resilientBackpropagationEtaMinus;
		clonedNeuralNet.resilientBackpropagationEtaPlus = resilientBackpropagationEtaPlus;
		clonedNeuralNet.predecessors = predecessorsClone;
		clonedNeuralNet.predecessorWeights = predecessorWeightsClone;
		clonedNeuralNet.successors = successorsClone;
		clonedNeuralNet.successorWeightIndexInPendantPredecessorArray = successorWeightIndexInPendantPredecessorArrayClone;
		clonedNeuralNet.shadows = synapseShadowsClone;
		clonedNeuralNet.layerStartingNeurons = layerStartingNeuronsClone;
		clonedNeuralNet.neuronsPerLayer = neuronsPerLayerClone;
		clonedNeuralNet.activations = activationsClone;
		clonedNeuralNet.netInputs = netInputsClone;
		clonedNeuralNet.neuronBehaviors = neuronBehaviorsClone;

		return clonedNeuralNet;

	}

	/**
	 * Checks if this object equals another. If the other is a neural network
	 * too, it is checked if the four primary data storage arrays equal each
	 * other. If so, true is returned, otherwise false.
	 * 
	 * @param obj
	 *            the object to check
	 */
	public boolean equals(Object obj) {
		NeuralNetwork net = null;
		if (obj instanceof NeuralNetwork) {
			net = (NeuralNetwork) obj;
		} else {
			return false;
		}

		if (net.countNeurons() != countNeurons()) {
			return false;
		}

		for (int i = 0; i < countNeurons() + 1; i++) {
			if (!Arrays.equals(predecessors[i], net.predecessors[i])
					|| !Arrays.equals(predecessorWeights[i],
							net.predecessorWeights[i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @return the resilientBackpropagationDeltaZero
	 */
	public double getResilientBackpropagationDeltaZero() {
		return resilientBackpropagationDeltaZero;
	}

	/**
	 * Sets the resilientBackpropagationDeltaZero. Default Value is 0.1.
	 * 
	 * @param resilientBackpropagationDeltaZero
	 *            the resilientBackpropagationDeltaZero to set
	 */
	public void setResilientBackpropagationDeltaZero(
			double resilientBackpropagationDeltaZero) {
		this.resilientBackpropagationDeltaZero = resilientBackpropagationDeltaZero;
	}

	/**
	 * *
	 * 
	 * @return the resilientBackpropagationDeltaMax
	 */
	public double getResilientBackpropagationDeltaMax() {
		return resilientBackpropagationDeltaMax;
	}

	/**
	 * Sets the resilientBackpropagationDeltaMax. Default value is 50.
	 * 
	 * @param resilientBackpropagationDeltaMax
	 *            the resilientBackpropagationDeltaMax to set
	 */
	public void setResilientBackpropagationDeltaMax(
			double resilientBackpropagationDeltaMax) {
		this.resilientBackpropagationDeltaMax = resilientBackpropagationDeltaMax;
	}

	/**
	 * @return the resilientBackpropagationDeltaMin
	 */
	public double getResilientBackpropagationDeltaMin() {
		return resilientBackpropagationDeltaMin;
	}

	/**
	 * Sets the resilientBackpropagationDeltaMin. Default value is 0.000001.
	 * 
	 * @param resilientBackpropagationDeltaMin
	 *            the resilientBackpropagationDeltaMin to set
	 */
	public void setResilientBackpropagationDeltaMin(
			double resilientBackpropagationDeltaMin) {
		this.resilientBackpropagationDeltaMin = resilientBackpropagationDeltaMin;
	}

	/**
	 * @return the resilientBackpropagationEtaMinus
	 */
	public double getResilientBackpropagationEtaMinus() {
		return resilientBackpropagationEtaMinus;
	}

	/**
	 * Sets the resilientBackpropagationEtaMinus. Default value is 0.5.
	 * 
	 * @param resilientBackpropagationEtaMinus
	 *            the resilientBackpropagationEtaMinus to set
	 */
	public void setResilientBackpropagationEtaMinus(
			double resilientBackpropagationEtaMinus) {
		this.resilientBackpropagationEtaMinus = resilientBackpropagationEtaMinus;
	}

	/**
	 * @return the resilientBackpropagationEtaPlus
	 */
	public double getResilientBackpropagationEtaPlus() {
		return resilientBackpropagationEtaPlus;
	}

	/**
	 * Sets the resilientBackpropagationEtaPlus. Default Value is 1.2.
	 * 
	 * @param resilientBackpropagationEtaPlus
	 *            the resilientBackpropagationEtaPlus to set
	 */
	public void setResilientBackpropagationEtaPlus(
			double resilientBackpropagationEtaPlus) {
		this.resilientBackpropagationEtaPlus = resilientBackpropagationEtaPlus;
	}

}

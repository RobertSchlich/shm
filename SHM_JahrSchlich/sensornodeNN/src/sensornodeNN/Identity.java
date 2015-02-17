package sensornodeNN;


/**
 * Implements the Identity Function and its derivative. Note: The
 * getAbsoluteMaximumLocationOfSecondDerivative() function just returns 1 in
 * order to not cause malfunctions for example in synapse initialization.
 * 
 * @author David Kriesel / dkriesel.com
 * 
 */
public class Identity implements NeuronBehavior{

//	@Override
	public double computeDerivative(double x) {
		return 1;
	}

//	@Override
	public double computeActivation(double x) {
		return x;
	}

//	@Override
	public NeuronBehavior getDedicatedInstance() {
		return new Identity();
	}

//	@Override
	public boolean needsDedicatedInstancePerNeuron() {
		return false;
	}


//	@Override
	public double getAbsoluteMaximumLocationOfSecondDerivative() {
		return 1;
	}

}

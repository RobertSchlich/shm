package spot.src.org.sunspotworld.demo.neuralnetwork;

import com.sun.squawk.util.MathUtils;



/**
 * Implements the Tangens Hyperbolicus and its derivative.
 * 
 * @author David Kriesel / dkriesel.com
 *
 */
public class TangensHyperbolicus implements NeuronBehavior{

//	@Override
	public double computeDerivative(double x) {
		double t = 1-2/(MathUtils.exp(2*x)+1);
		return 1 - (t*t);
	}

//	@Override
	public double computeActivation(double x) {
		double tanh=1-2/(MathUtils.exp(2*x)+1);
		return tanh;
		//return Math.tanh(x);
	}

//	@Override
	public NeuronBehavior getDedicatedInstance() {
		return new TangensHyperbolicus();
	}

//	@Override
	public boolean needsDedicatedInstancePerNeuron() {
		return false;
	}


//	@Override
	public double getAbsoluteMaximumLocationOfSecondDerivative() {
		return 0.66;
	}
	
}

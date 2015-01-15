package fft;

import java.util.Vector;

public class SimData {
	public static void main(){
	// generate x-vector
	//Vector x = new Vector();
	//x = arange(0, 20, 0.001);
			
		// define a sinus function
		
	Vector<Double> y = new Vector<Double>();
		
	y.addElement(1.);
		
		
	/*
	//y = sin(pi * x);
	//another sinus function
	z = 0.1* sin(pi * 30 * x)+1;
	// generate noise by adding values from gaussian distribution
	n = random.normal(-0.01,0.01,len(y));
	// and now evanescent
	y = exp(-0.1 * x) * sin(pi * x);
	z = exp(-0.1 * x) * 0.1* sin(pi * 30 * x)+1;
	// add signals
	y = y + z + n;
	print(y);
	*/
	}
}

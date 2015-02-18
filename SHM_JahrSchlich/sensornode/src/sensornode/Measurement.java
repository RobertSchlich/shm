package sensornode;
public class Measurement{
	
	public String address;
	public float frequency;
	public double magnitude;
	public int error;
	public double prediction = 0;
	
	// initiates a measurement
	Measurement(String a, double m, float f, int e){
		
		address = a;
		magnitude = m;
		frequency = f;
		error = e;
	}
	
	// initiates a measurement
	Measurement(){
		
		address = "a";
		magnitude = 0;
		frequency = 0;
		error = 0;
	}		
}
package sensornodeNN;

public class Measurement{
	
	
		public String address;
		public float frequency;
		public double magnitude;
		
		// initiates a measurement
		Measurement(String a, double m, float f){
			
			address = a;
			magnitude = m;
			frequency = f;
		}
		
		// initiates a measurement
		Measurement(){
			
			address = "a";
			magnitude = 0;
			frequency = 0;
		}		
		
		 
        
    }
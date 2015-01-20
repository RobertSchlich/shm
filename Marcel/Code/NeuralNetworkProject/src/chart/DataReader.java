package chart;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class DataReader {
	
	private int nodeIndex;
	private int experimentIndex;
	private ArrayList<Double> acceleration = new ArrayList<Double>();
	private ArrayList<Integer> time = new ArrayList<Integer>();
	
	/**
	 * Sets the important indizes to read the data from a special file.
	 * 
	 * @param nodeIndex
	 * @param experimentIndex
	 */
	public DataReader(int nodeIndex, int experimentIndex){
		this.nodeIndex = nodeIndex;
		this.experimentIndex = experimentIndex;
	}//Ende DataReader()
	
	public DataReader() {
		
	}

	/**
	 * Reads all acceleration data from a special file.
	 * Adds all values to the {@code acceleration} list and the {@code time} list.
	 * 
	 * @param phase of the experiment [measurement/calculation]
	 * @throws Exception
	 */
	public void readData(String phase) throws Exception{
		
		String directory = System.getProperty("user.home") + "/Documents/SensorValues/" + experimentIndex + "_" + phase + "_sensorValues_" + (nodeIndex+1) + ".txt";
		
		Scanner sc = new Scanner(new File(directory)); 
		
		int sample_period = sc.nextInt();
		
		acceleration.clear();
		time.clear();
		
		int i=0;
		while(sc.hasNext()){
			String d = sc.next();
			acceleration.add(Double.parseDouble(d));
			time.add(i*sample_period);
			i++;
		}
		
		sc.close();
		
	}//Ende readData()
	
	/** 
	 * @param phase of the experiment [measurement/calculation]
	 * @return the number of different experiment files in the folder
	 */
	public int countExperiments(String phase){
		
		int count = 1;
		while(true){
			String directory = System.getProperty("user.home") + "/Documents/SensorValues/" + count + "_" + phase + "_sensorValues_1.txt";
			if(!(new File(directory).exists())){
				break;
			}
			count++;
		}
		
		return count;
		
	}//Ende countExperiments()
	
	/** 
	 * @param phase of the experiment [measurement/calculation]
	 * @return the number of different node files in the folder
	 */
	public int countNodes(String phase){
		
		int count = 1;
		while(true){
			String directory = System.getProperty("user.home") + "/Documents/SensorValues/1_" + phase + "_sensorValues_" + count + ".txt";
			if(!(new File(directory).exists())){
				break;
			}
			count++;
		}
		
		return count-1;
		
	}//Ende countNodes()
	
	/** 
	 * @param phase of the experiment [measurement/calculation]
	 * @return the sample period used for the measurements
	 */
	public int getSamplePeriod(String phase){
		
		int sample_period = 0;
		String directory = System.getProperty("user.home") + "/Documents/SensorValues/1_" + phase + "_sensorValues_1.txt";
		
		try{
			Scanner sc = new Scanner(new File(directory));
			sample_period = Integer.parseInt(sc.next());
			sc.close();
		}catch(Exception e){
			
		}
				
		return sample_period;
		
	}//Ende getSamplePeriod()

	/**
	 * @return the acceleration
	 */
	public ArrayList<Double> getAcceleration() {
		return acceleration;
	}

	/**
	 * @return the time
	 */
	public ArrayList<Integer> getTime() {
		return time;
	}

}

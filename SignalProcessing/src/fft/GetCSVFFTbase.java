package fft;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import fft.Complex;

public class GetCSVFFTbase
{
    public static double[] Load(String file) throws FileNotFoundException
    {
        //Get scanner instance
        Scanner scanner = new Scanner(new File(file));
        //Set the delimiter used in file
        scanner.useDelimiter(",");
        
        //Count elements in CSV file
        // TO DO: there must be a better way
        int i = 0;
        while (scanner.hasNextLine())
        {
            scanner.nextLine();
            i+=1;
        }
        // print number of values
        //System.out.println(i);
    
        //close scanner
        scanner.close();
        
        
        //Get second scanner instance
        // TO DO: Set scanner 1 to first position instead
        Scanner scanner2 = new Scanner(new File(file));
        //Set the delimiter used in file
        scanner.useDelimiter(",");
                
        // initialize array of complex numbers
        double[] y = new double[i];
        
        // parse text to double, create complex number, add it to array
        int j = 0;    
        while (scanner2.hasNextLine())
        {
        	double a = Double.valueOf(scanner2.nextLine());
        	y[j]=new Double(a);
            //System.out.print(y[j]);
        	j+=1;
        }
        
        //close scanner2 
        scanner2.close();

        // return array of complex numbers
        return y;
    }
}
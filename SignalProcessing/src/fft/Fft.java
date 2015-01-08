package fft;


// Autor: H.W. Lang, FH Flensburg
// lang@fh-flensburg.de 
// Fast Fourier Transform (FFT)
public class Fft
{    
    // Version 1: Buch Algorithmen in Java
    public static Complex[] transform(Complex[] a)
    {
        int n=a.length;
        fft(a, n, 0, Complex.root(n));
        return a;
    }

    // Version 1: Buch Algorithmen in Java
    public static Complex[] invtrans(Complex[] a)
    {
        int n=a.length;
        for (int i=0; i<n; i++)
            a[i]=a[i].div(n);
        fft(a, n, 0, Complex.root(n).conj());
        return a;
    }
    
    // alternative Version 2:
    // Der Faktor 1/n, der bei der R�cktransformation anzuwenden
    // ist, wird symmetrisch als 1/sqrt(n) auf Hin- und R�ck-
    // Transformation aufgeteilt. Ferner wird die Hin-
    // Transformation mit der konjugierten n-ten Einheitswurzel
    // ausgef�hrt (entspricht der R�ck-Transformation von Version 1)
    public static Complex[] transform2(Complex[] a)
    {
        int n=a.length;
        double r=Math.sqrt(n);
        for (int i=0; i<n; i++)
            a[i]=a[i].div(r);
        fft(a, n, 0, Complex.root(n).conj());
        return a;
    }

    // R�cktransformation Version 2
    public static Complex[] invtrans2(Complex[] a)
    {
        int n=a.length;
        double r=Math.sqrt(n);
        for (int i=0; i<n; i++)
            a[i]=a[i].div(r);
        fft(a, n, 0, Complex.root(n));
        return a;
    }

    public static void fft(Complex[] a, int n, int lo, Complex w)
    {
        Complex z, v, h;
        if (n>1)
        {
            int m=n/2;
            z=Complex.ONE;
            for (int i=lo; i<lo+m; i++)
            {
                h=a[i].sub(a[i+m]);
                a[i]=a[i].add(a[i+m]);
                a[i+m]=h.mul(z);
                z=z.mul(w);
            }
            v=w.sq();
            fft(a, m, lo, v);
            fft(a, m, lo+m, v);
            shuffle (a, n, lo);
        }
    }
    
    private static void shuffle(Complex[] a, int n, int lo)
    {
        int i, m=n/2;
        Complex[] b=new Complex[m];

        for (i=0; i<m; i++)
            b[i]=a[lo+i];
        for (i=0; i<m; i++)
            a[lo+i+i+1]=a[lo+i+m];
        for (i=0; i<m; i++)
            a[lo+i+i]=b[i];
    }
    
    public static double[] magnitude(Complex[] transformed)
    {
    	// calculate magnitudes on fft array
    	int i, n = transformed.length;
    	double magnitude[] = new double[n];
    	
        for (i=0; i<n; i++)
        {
        	magnitude[i] = transformed[i].norm();
        }
        
        return magnitude;
    }
    
    public static double[] freq(Complex[] transformed, Double samplerate)   
    {
        // calculate frequencies of peaks
    	int i, n = transformed.length;
    	double frequency[] = new double[n];
          
        for (i=0; i<n; i++)
        {
        	frequency[i] = i * samplerate / n;
        }
        
        return frequency;
    	
    }
    


}   // end class Fft

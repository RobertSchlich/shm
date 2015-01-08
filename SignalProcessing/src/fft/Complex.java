package fft;



// Komplexe Zahl
public class Complex
{
    public static final Complex ZERO=new Complex();
    public static final Complex ONE=new Complex(1);
    private double re, im;
    
    // Konstruktoren
    public Complex(double re, double im)
    {
        this.re=re;
        this.im=im;
    }
    
    public Complex(double re)
    {
        this(re, 0);
    }

    public Complex()
    {
        this(0);
    }
    
    public Complex(Complex x)
    {
        this(x.re, x.im);
    }
    
    // Methoden
    public Complex add(Complex b)
    {
        return new Complex(re+b.re, im+b.im);
    }

    public Complex sub(Complex b)
    {
        return add(b.neg());
    }

    public Complex mul(Complex b)
    {
        return new Complex(re*b.re-im*b.im, im*b.re+re*b.im);
    }
    
    public Complex mul(double b)
    {
        return new Complex(re*b, im*b);
    }
    
    public Complex div(Complex b)
    {
        return mul(b.rec());
    }
    
    public Complex div(double b)
    {
        return new Complex(re/b, im/b);
    }
    
    // negative Zahl
    public Complex neg()
    {
        return new Complex(-re, -im);
    }

    // konjugiert komplexe Zahl
    public Complex conj()
    {
        return new Complex(re, -im);
    }
    
    // Kehrwert
    public Complex rec()
    {
        return conj().div(norm2());
    }

    // Quadrat
    public Complex sq()
    {
        return mul(this);
    }

    // Quadrat des Betrags
    public double norm2()
    {
        return re*re+im*im;
    }
    
    // Betrag
    public double norm()
    {
        return Math.sqrt(norm2());
    }

    // primitive n-te Einheitswurzel hoch k
    public static Complex root(int n, int k)
    {
        double w=k*2*Math.PI/n;
        return new Complex(Math.cos(w), Math.sin(w));
    }
    
    // primitive n-te Einheitswurzel
    public static Complex root(int n)
    {
        return root(n, 1);
    }

    public String toString()
    {
        return round(re,3)+" + "+round(im,3)+" i";
    }

    /** rundet eine double-Zahl auf k Stellen nach dem Komma
     */
    private static double round(double r, int k)
    {   
        double p=Math.pow(10,k);
        return Math.rint(r*p)/p;
    }
    

}   // end class Complex

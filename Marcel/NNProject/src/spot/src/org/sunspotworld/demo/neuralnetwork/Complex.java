package spot.src.org.sunspotworld.demo.neuralnetwork;

public class Complex
{

    private final double re;	// Realteil und
    private final double im;	// Imaginaerteil - koennen nicht veraendert werden

    // Konstruktor: Erzeuge neues Objekt mit gegebenem Real- und Imaginaerteil
    public Complex(double a, double b)
    {
	re = a;
	im = b;
    }

    // Konstruktor: Erzeuge neues Objekt mit gegebenem Realteil, Imaginaerteil=0
    public Complex(double r )
    {
	re = r;
	im = 0.;
    }

    // Konvertiere komplexe Zahl in String
    public String toString() {
        if (im == 0) return Double.toString(re);
        if (re == 0) return im + " I";
        if (im <  0) return re + " - " + (-im) + " I";
        return re + " + " + im + " I";
    }

    // Zugriff von ausserhalb auf Realteil
    public double Real()
    {
	return re;
    }

    // Zugriff von ausserhalb auf Imaginaerteil
    public double Imag()
    {
	return im;
    }

    // Absolutbetrag der komplexen Zyahl
    public double abs()
    {
	return Math.sqrt(re*re+im*im);
    }

    // Erzeuge ein neues Objekt mit Wert (this + r)
    public Complex plus(Complex r)
    {
	return new Complex(re+r.re, im+r.im); 
    }

    // Erzeuge ein neues Objekt mit Wert (this - r)
    public Complex minus(Complex r)
    {
	return new Complex(re-r.re, im-r.im);
    }

    // Erzeuge ein neues Objekt mit Wert (this * r)
    public Complex times(Complex r)
    {
	return new Complex(re*r.re-im*r.im, re*r.im+im*r.re); 
    }

    // Erzeuge ein neues Objekt mit Wert (this * d), wobei d reell ist
    public Complex times(double d)
    {
	return new Complex(re*d, im*d);
    }

    // Erzeuge ein neues Objekt mit Wert (this / r)
    public Complex divide(Complex r)
    {
	// Verwende this/r = r^* * this /|r|^2
	return this.times(r.conjugate()).divide(r.re*r.re+r.im*r.im);
    }

    // Erzeuge ein neues Objekt mit Wert (this / d), wobei d reell ist
    public Complex divide(double d)
    {
        if(d == 0)
	   System.err.println("### Complex: Division durch 0");
	return new Complex(re/d, im/d);
    }

    // Erzeuge ein neues Objekt mit Wert this^*
    public Complex conjugate()
    {
	return new Complex(re, -im);
    }
	
    public static void main(String[] args)
    {
        Complex a = new Complex(5.0, 6.0);
        Complex b = new Complex(-3.0, 4.0);

	System.out.println("a            = " + a);
        System.out.println("b            = " + b);
        System.out.println("Real(a)      = " + a.Real());
        System.out.println("Imag(a)      = " + a.Imag());
        System.out.println("a + b        = " + a.plus(b));
        System.out.println("a - b        = " + a.minus(b));
        System.out.println("a * b        = " + a.times(b));
        System.out.println("a * E        = " + a.times(Math.E));
        System.out.println("b * a        = " + b.times(a));
        System.out.println("b / a        = " + b.divide(a));
        System.out.println("b / Pi       = " + b.divide(Math.PI));
        System.out.println("(b / a) * a  = " + b.divide(a).times(a));
        System.out.println("b^*          = " + b.conjugate());
        System.out.println("|b|          = " + b.abs());
 
// Loesung von Blatt 3, Aufgabe 3(b)
	Complex c = new Complex(0.23, -.999);
	Complex d = new Complex(0.1, -1.8);
	Complex e = new Complex(3.5);
	System.out.println("*** Loesung von Blatt 3, Aufgabe 3(b):");
	System.out.println("a = " + a);
	System.out.println("b = " + b);
	System.out.println("c = " + c);
	System.out.println("d = " + d);
	System.out.println("e = " + e);
	Complex res = a.plus(b.times(c)).conjugate().divide(d).minus(e);
	System.out.println("Ergebnis = " + res);

    }

}

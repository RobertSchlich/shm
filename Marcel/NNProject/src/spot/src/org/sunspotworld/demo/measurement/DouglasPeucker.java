package spot.src.org.sunspotworld.demo.measurement;

public class DouglasPeucker {

	public double[] getDP(double[] list, double step, double epsilon) throws IndexOutOfBoundsException{
		
	    // Finde den Punkt mit dem größten Abstand
	    double dmax = 0;
	    int index = 0;
	    for (int i=2; i< list.length; i++){
	        double d = getDistance(step*i, list[i], 0, list[0], step*(list.length-1), list[list.length-1]);
	        if(d > dmax){
	            index = i;
	            dmax = d;
	        }
	    }
	    
	    double[] resultList = new double[list.length];
	    
	    // Wenn die maximale Entfernung größer als Epsilon ist, dann rekursiv vereinfachen
	    if(dmax >= epsilon){
	        // Recursive call
	        //recResults1[] = DouglasPeucker(PointList[1...index], epsilon)
	       // recResults2[] = DouglasPeucker(PointList[index...end], epsilon)
	 
	        // Ergebnisliste aufbauen
	        //ResultList[] = {recResults1[1...end-1] recResults2[1...end]}
	    }else{
	        //ResultList[] = {PointList[1], PointList[end]}
	    }
	    
	    // Ergebnis zurückgeben
	    return resultList;
	    
	}
	
	public static double getDistance(double  ax, double ay, double r1x, double r1y, double r2x, double r2y)
    {
        return Math.sqrt(getSquaredDistance(ax, ay, r1x, r1y, r2x, r2y));
    }
   
    public static double getSquaredDistance(double  ax, double  ay, double r1x, double r1y, double r2x, double r2y)
    {
        // Normalisierte Richtung r1 --> r2 ausrechnen
        double dx = r2x - r1x;
        double dy = r2y - r1y;
        double len = Math.sqrt(dx*dx+dy*dy);
        dx /= len;
        dy /= len;
 
        // Richtung r1 --> a ausrechnen
        double dax = ax - r1x;
        double day = ay - r1y;
       
        // Punkt a auf Gerade r1 --> r2 projizieren
        double dot = dax * dx + day * dy;
        double px = r1x + dx * dot;
        double py = r1y + dy * dot;
       
        // Abstand zwischen a und projiziertem Punkt ausrechnen
        double ddx = ax-px;
        double ddy = ay-py;
        double squaredDistance = ddx * ddx + ddy * ddy;
        return squaredDistance;
    }
}

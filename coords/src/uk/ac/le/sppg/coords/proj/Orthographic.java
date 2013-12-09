/*
 * Orthographic.java
 *
 * Equations for the projection were obtained from Mathworld at
 * Wolfram Research (http://mathworld.wolfram.com/topics/MapProjections.html)
 *
 * Created on 18 June 2004, 22:53
 */

package uk.ac.le.sppg.coords.proj;

import uk.ac.le.sppg.coords.CoordConstants;
import uk.ac.le.sppg.coords.Geographic;
import uk.ac.le.sppg.coords.CartesianVector;
import java.awt.geom.Point2D;
import java.lang.Math;


/**
 *
 * @author  nigel
 */
public class Orthographic extends Projection implements CoordConstants {
    

    CartesianVector centreVector;
    
    /** Creates a new instance of Orthographic */
    /**
     * creates an Orthographic {@link Projection}
     * 
     * @param scale
     * determines the scale factor when performing the projection.
     * The value required will depend on the size of the component into which the drawing
     * is to be done. Trial and error is the only way to determine a suitable value.
     * @param centre
     * the location at the centre of the projection.
     */
    public Orthographic(double scale, Geographic centre) {
    	setCentre( centre );
    	this.scale = scale;
        type = ProjectionType.Orthographic;
    }

    public void setScale( double scale ) { 
        this.scale = scale; 
    }
    
    public Point2D geoToPoint( Geographic geo ) {
    	CartesianVector v = geo.toGeocentric();
        
        double angle = centreVector.angle( v );
        
        // if the point is hidden on the far side, return infinity.
        //System.out.println( geo + " "+ angle);
        if ( angle > 90.0 ) {
            return new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        
        double p = geo.latRadians;
        double p1 = centre.latRadians;
        double l = geo.lonRadians;
        double l0 = centre.lonRadians;

        double x = scale * ( Math.cos(p)*Math.sin(l-l0));
        double y = scale * ( Math.cos(p1)*Math.sin(p) - Math.sin(p1)*Math.cos(p)*Math.cos(l-l0));
    	
    	return new Point2D.Double( x, y );
    }
    
    public Geographic pointToGeo( Point2D point ) {
        double p1 = centre.latRadians;
        double l0 = centre.lonRadians;
        double x = point.getX() / scale;
        double y = point.getY() / scale;

        double r = Math.sqrt( x*x + y*y );
        double c = Math.asin(r);
        
        double lat = Math.asin(Math.cos(c)*Math.sin(p1) + y*Math.sin(c)*Math.cos(p1)/r);
        double lon = l0 + Math.atan2( x*Math.sin(c), r*Math.cos(p1)*Math.cos(c) - y*Math.sin(p1)*Math.sin(c));
        
        return new Geographic( Math.toDegrees(lat), Math.toDegrees(lon), 0.0 );
    }

    public void setCentre( Geographic centre ) {
        this.centre = centre;
        centreVector = centre.toGeocentric();
    }

    public Projection copy() {
        Orthographic result = new Orthographic( this.scale, this.centre );
        return result;
    }
  
}

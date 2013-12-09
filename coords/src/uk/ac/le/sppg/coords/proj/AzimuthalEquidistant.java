/*
 * AzimuthalEquidistant.java
 *
 * Equations for the projection were obtained from Mathworld at
 * Wolfram Research (http://mathworld.wolfram.com/topics/MapProjections.html)
 *
 * Created on 17 June 2004, 22:53
 */

package uk.ac.le.sppg.coords.proj;

import uk.ac.le.sppg.coords.CoordConstants;
import uk.ac.le.sppg.coords.Geographic;
import java.awt.geom.Point2D;
import java.lang.Math;


/**
 *
 * @author  Nigel Wade
 */

public class AzimuthalEquidistant extends Projection implements CoordConstants {
    
    
    /** Creates a new instance of AzimuthalEquidistant */
    /**
     * creates an AzimuthalEquidistance {@link Projection}
     * 
     * @param scale
     * determines the scale factor when performing the projection.
     * The value required will depend on the size of the component into which the drawing
     * is to be done. Trial and error is the only way to determine a suitable value.
     * @param centre
     * the location at the centre of the projection.
     */
    public AzimuthalEquidistant(double scale, Geographic centre ) {
    	setCentre( centre );
    	this.scale = scale;
        type = ProjectionType.AzimuthalEquidistant;
    }

    public void setScale( double scale ) { 
        this.scale = scale; 
    }
    
    public Point2D geoToPoint( Geographic geo ) {
    	
        double p = geo.latRadians;
        double p1 = centre.latRadians;
        double l = geo.lonRadians;
        double l0 = centre.lonRadians;
        
        double c = Math.acos(Math.sin(p1)*Math.sin(p)+Math.cos(p1)*Math.cos(p)*Math.cos(l-l0));
        double k = scale * c / Math.sin(c);

        double x, y;
        
        if ( Math.toDegrees(Math.abs(l-l0)) < 0.1 && Math.toDegrees(Math.abs(p-p1)) < 0.1 ) {
            x = 0.0;
            y = 0.0;
        }
        else {
            x = k * ( Math.cos(p)*Math.sin(l-l0));
            y = k * ( Math.cos(p1)*Math.sin(p) - Math.sin(p1)*Math.cos(p)*Math.cos(l-l0));
        }
     
    	
    	return new Point2D.Double( x, y );
    }
    
    public Geographic pointToGeo( Point2D point ) {
        double p1 = centre.latRadians;
        double l0 = centre.lonRadians;
        double x = point.getX() / scale;
        double y = point.getY() / scale;

        double c = Math.sqrt( x*x + y*y );
        
        double lat = Math.asin( Math.cos(c)*Math.sin(p1) + y*Math.sin(c)*Math.cos(p1) / c );
        double lon;
        if ( Math.toDegrees(p1) > 89.999 ) {
            lon = l0 + Math.atan( -x / y );
        }
        else if ( Math.toDegrees(p1) < -89.999 ) {
            lon = l0 + Math.atan( x/y );
        }
        else {
            lon = l0 + Math.atan2( x*Math.sin(c), ( c*Math.cos(p1)*Math.cos(c) - y*Math.sin(p1)*Math.sin(c) ) );
        }
        
        return new Geographic( Math.toDegrees(lat), Math.toDegrees(lon), 0.0 );
    }

    public void setCentre( Geographic centre ) {
        this.centre = centre;
    }

    public Projection copy() {
        AzimuthalEquidistant result = new AzimuthalEquidistant( this.scale, this.centre );
        return result;
    }
  
}

package uk.ac.le.sppg.coords.proj;


import java.awt.geom.Point2D;

import uk.ac.le.sppg.coords.CoordConstants;
import uk.ac.le.sppg.coords.Geographic;

/**
 * Provides a stereographic projection.
 * 
 * @author Nigel Wade
 */
public class Stereo extends Projection implements CoordConstants {

    
    /**
     * Creates a <code>Projection</code> which will project 
     * <code>Geographic</code> locations onto a plane surface.
     * <p>
     * The <code>Geographic</code> location of the centre of 
     * the projection is set by <code>centre</code> and a scale
     * factor for zooming the projection is set by <code>scale</code>.
     * @param scale
     * the scale factor for the projection.
     * 1.0 is no scaling. Larger values zoom in, smaller values zoom out.
     * @param centre
     * the <code>Geographic</code> location of the centre
     */
    public Stereo( double scale, Geographic centre ) {
    	setCentre( centre );
    	this.scale = scale;
        type = ProjectionType.Stereographic;
    }

    public void setScale( double scale ) { 
        this.scale = scale; 
    }
    
    public Point2D geoToPoint( Geographic geo ) {
    	
    	double z = Math.sin(centre.latRadians)* 
    			Math.sin(geo.latRadians) +
    		       Math.cos(centre.latRadians)*
    			Math.cos(geo.latRadians)*
    			Math.cos(geo.lonRadians - centre.lonRadians);
       	double k = 2 * scale / (1 + z);

    	double x = (1.0 + k * Math.cos( geo.latRadians ) *
    		     Math.sin( geo.lonRadians - centre.lonRadians )) / 2.0;
    		       
    	double y = (1.0 - k * ( Math.cos( centre.latRadians ) * Math.sin( geo.latRadians ) -
    			Math.sin( centre.latRadians ) * Math.cos( geo.latRadians ) *
    			 Math.cos( geo.lonRadians - centre.lonRadians ) )) / 2.0;
    	
    	//x = (x - lowerLeftGeo.longitude) / lonRange;
    	//y = 1.0 - (y - lowerLeftGeo.latitude) / latRange;
    	
    	return new Point2D.Double( x, y );
    }
    
    public Geographic pointToGeo( Point2D point ) {
        double x = point.getX();
        double y = point.getY();
        
        double rho = Math.sqrt( x*x + y*y);
        double c = 2.0 * Math.atan(rho / (2 * scale));
        
        double lat = Math.asin( Math.cos(c)*Math.sin(centre.latRadians)+y*Math.sin(c)*Math.cos(centre.latRadians/rho));
        double lon = centre.lonRadians + Math.atan(x*Math.sin(c) / (rho*Math.cos(centre.latRadians*Math.cos(c)-y*Math.sin(centre.latRadians*Math.sin(c)))));
        
        return new Geographic( Math.toDegrees(lat), Math.toDegrees(lon), 0.0 );
    }

    public void setCentre( Geographic centre ) {
        this.centre = centre;
    }

    public Projection copy() {
        Stereo result = new Stereo( this.scale, this.centre );
        return result;
    }

}

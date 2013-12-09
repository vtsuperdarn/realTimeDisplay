package uk.ac.le.sppg.coords.proj;


// this is an implementation of the Projection interface which does nothing
// other than to scale the latitude to the range of the frame area.
//
// the lowerLeft geographic point is mapped to the lower left corner of the
// drawing area and upperRight to the upper right corner

import java.awt.geom.Point2D;

import uk.ac.le.sppg.coords.Geographic;

/**
 * this is an implementation of the <code>Projection</code>
 *  interface which does nothing other than to scale the 
 * latitude to the range of the frame area.
 * The <code>lowerLeft</code> <code>Geographic</code> point 
 * is mapped to the lower left corner of the drawing area and 
 *  <code>upperRight|</code> to the upper right corner.
 * <p>
 * If <code>scale</code> is set to anything other than 1.0 the 
 * viewing are will be scaled by that amount resulting in a zoom in/
 * zoom out effect. The mapping of <code>lowerLeft</code> and
 * <code>upperRight</code> will be lost until <code>scale</code> is
 * set back to 1.0.
 * 
 * @author Nigel Wade
 */
public class None extends Projection {

    
    /**
     * creates a projection such that <code>lowerLeft</code> will be
     * mapped to the lower left corner of the drawing area and
     * <code>upperRight</code> will be mapped to the upper right corner.
     * @param lowerLeft
     * the <code>Geographic</code> location of the lower left corner.
     * @param upperRight
     * the <code>Geographic</code> location of the upper right corner.
     */
    public None( Geographic lowerLeft, Geographic upperRight ) {
        scale = 1.0;
        setGeo( lowerLeft, upperRight );
        type = ProjectionType.None;
    }
    
    public Point2D geoToPoint( Geographic geo ) {
    	
    	
    	double x = (geo.longitude - lowerLeftGeo.longitude) / lonRange;
    	double y = (geo.latitude - lowerLeftGeo.latitude) / latRange;
    	
        x = 2*(x - 0.5);
        y = 2*(y - 0.5);

        Point2D p = new Point2D.Double( x, y );
    	
    	//System.out.println( "project "+geo.latitude+","+geo.longitude+
    	//    		"  to "+p.getX()+","+p.getY());
    	
    	return p;
    }
    
    public Geographic pointToGeo( Point2D p ) {
        double x = p.getX();
        double y = p
        .getY();
        x = x / 2 + 0.5;
        y = y / 2 + 0.5;
        double lon = x * lonRange + lowerLeftGeo.longitude;
        double lat = y * latRange + lowerLeftGeo.latitude;
        
        return new Geographic( lat, lon, 0.0 );
    }

    /**
     * Sets the <code>Geographic</code> limits of the projected area and
     * calculates the centre.
     * @param lowerLeft
     * <code>Geographic</code> location of the lower left corner.
     * @param upperRight
     * <code>Geographic</code> location of the upper right corner.
     */
    public void setGeo( Geographic lowerLeft, Geographic upperRight ) {
        this.lowerLeftGeo = lowerLeft;
        this.upperRightGeo = upperRight;
        
        latRange = upperRightGeo.latitude - lowerLeftGeo.latitude;
        lonRange = upperRightGeo.longitude - lowerLeftGeo.longitude;
 
        this.lowerLeftPoint = geoToPoint( lowerLeft );
        this.upperRightPoint = geoToPoint( upperRight );
        
    
        xRange = upperRightPoint.getX() - lowerLeftPoint.getX();
        yRange = upperRightPoint.getY() - lowerLeftPoint.getY();
        
        centre = new Geographic( lowerLeftGeo.latitude+latRange/2,
                                 lowerLeftGeo.longitude+latRange/2, 0.0 );
        
    }

    /**
     * Sets the scale to zoom in and out.
     * The initial scale is 1.0. By changing the scale the lowerLeft and 
     * upperRight locations are changed.
     * @param scale
     * the value of the scale factor.
     */
    public void setScale( double scale ) { 

        latRange = this.scale * (upperRightGeo.latitude - lowerLeftGeo.latitude) / scale;
        lonRange = this.scale * (upperRightGeo.longitude - lowerLeftGeo.longitude) / scale;
        
        this.scale = scale;

        double lowerLat = centre.latitude - latRange / 2.0;
        double upperLat = centre.latitude + latRange / 2.0;
        double lowerLon = centre.longitude - lonRange / 2.0;
        double upperLon = centre.longitude + lonRange / 2.0;
        
        this.lowerLeftGeo = new Geographic(lowerLat, lowerLon, 0.0); 
        this.upperRightGeo = new Geographic(upperLat, upperLon, 0.0);
        
        this.lowerLeftPoint = geoToPoint( this.lowerLeftGeo );
        this.upperRightPoint = geoToPoint( this.upperRightGeo );
   
        xRange = upperRightPoint.getX() - lowerLeftPoint.getX();
        yRange = upperRightPoint.getY() - lowerLeftPoint.getY();
        

    }
    
    public void setCentre( Geographic centre ) {
        this.centre = centre;

        double lowerLat = centre.latitude - latRange / 2.0;
        double upperLat = centre.latitude + latRange / 2.0;
        double lowerLon = centre.longitude - lonRange / 2.0;
        double upperLon = centre.longitude + lonRange / 2.0;
        
        this.lowerLeftGeo = new Geographic(lowerLat, lowerLon, 0.0); 
        this.upperRightGeo = new Geographic(upperLat, upperLon, 0.0);
        
        this.lowerLeftPoint = geoToPoint( this.lowerLeftGeo );
        this.upperRightPoint = geoToPoint( this.upperRightGeo );
   
        xRange = upperRightPoint.getX() - lowerLeftPoint.getX();
        yRange = upperRightPoint.getY() - lowerLeftPoint.getY();

    }
    
    public Projection copy() {
        None result =  new None(this.lowerLeftGeo, this.upperRightGeo);
        return result;
    }

}

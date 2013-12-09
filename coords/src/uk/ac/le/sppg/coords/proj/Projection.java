package uk.ac.le.sppg.coords.proj;


import uk.ac.le.sppg.coords.GeographicOutline;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import uk.ac.le.sppg.coords.Geographic;
import java.util.ArrayList;

/**
 * An abstract class representing a generic projection.
 * 
 * @author Nigel Wade
 */
public abstract class Projection {
   
    Geographic centre;
    Point2D lowerLeftPoint;
    Point2D upperRightPoint;
    Geographic lowerLeftGeo;
    Geographic upperRightGeo;
    double latRange;
    double lonRange;
    double xRange;
    double yRange;
    
    int height, width;
    
    double scale = 1.0;
    
    ProjectionType type;
    
    boolean debug = false;
    
    /**
     * Projects the <code>Geographic</code> location <code>loc</code>
     * according to the projection in force. The returned 
     * <code>Point2D</code> is in the projected reference frame.
     * @param loc
     * the <code>Geographic</code> location to be projected
     * @return
     * a <code>Point2D</code> representation of <code>loc</code>
     * in the projection frame.
     */
    public abstract Point2D geoToPoint( Geographic loc );
    
    public abstract Geographic pointToGeo( Point2D point );
    
    /**
     * sets the centre of the viewing area.
     * @param centre
     *  The Geographic location of the centre of the projection
     */
    public abstract void setCentre( Geographic centre );

    /**
     * @return
     * the <code>Geographic</code> centre of the projected area.
     */
    public Geographic getCentre() {	return centre; }
    
    /**
     * @return
     * the current scaling factor
     */
    public double getScale() { return scale; }
    
    /**
     * sets the current scaling factor
     * @param scale
     * the new value for the scaling factor
     */
    public abstract void setScale( double scale );
    
    
    /**
     * Get the lower left Geographic location of the projection.
     * Some projections may not be able to calculate this, and will return null.
     * @return
     * the <code>Geographic</code> lower left point, or null if not available.
     */
    public Geographic getLowerLeft() { return lowerLeftGeo; }
    /**
     * Get the upper right Geographic location of the projection.
     * Some projections may not be able to calculate this, and will return null.
     * @return
     * the <code>Geographic</code> upper right point, or null if not available.
     */
    public Geographic getUpperRight() { return upperRightGeo; }
    	
    /**
     * @return
     * returns a new instance of Projection which is a copy of the current Projection.
     */
    public abstract Projection copy();
    
    /**
     * 
     * @return
     * The type of Projection which the sub-class has implemented.
     */
    public ProjectionType getType() { return type; }

    
    /**
     * This class projects a GeographicOutline onto the projection plane.
     * It returns a GeneralPath of the projected points.
     * 
     * @param outline
     *  The GeographicOutline to be projected.
     * @param closed
     *  A boolean indicating whether the path should be closed i.e. wether GeneralPath.lineTo() 
     *  should be invoked to join the last point to the first point.
     * @return
     *  The projected points contained in a GeneralPath
     */
    public GeneralPath projectGeographicOutline( GeographicOutline outline, boolean closed ) {
        
        GeneralPath result = null;
        ArrayList<Geographic> geoPath;
                
        Point2D p;
        float px;
        float py;
        boolean move;
        boolean visible;
        
        
        geoPath = outline.getPath();

        //System.out.println("new path: length "+inner.size() );

            
        result = new GeneralPath();

        if ( debug )
            System.out.println( "start path");

        move = true;
        visible = false;

        boolean first = true;
        boolean skipClose = ! closed;

        for ( Geographic geo : geoPath ) {

            p = geoToPoint( geo );

            if ( p.getX() > 9999.0 ) {
                move = true;
                if ( first ) {
                    skipClose = true;
                }
                continue;
            }

            px = (float) p.getX();
            py = (float) p.getY();

            if ( !visible )
                visible = (px >= -1.0 && px <= 1.0 && py >= -1.0 && py <= 1.0);


            if ( move ) {
                if ( first )
                    skipClose = true;
                result.moveTo( px, py  );
                if ( debug )
                    System.out.println("move to "+px+" "+py);
            } else {
                result.lineTo( px, py );
                if ( debug )
                    System.out.println("lin to "+px+" "+py);
            }

            move = false;
            first = false;
        }

        if ( debug )
            System.out.println("close path" );


        // don't do anything with the path if its empty( getCurent return null)
        if ( result.getCurrentPoint() != null && ! skipClose  )
            result.closePath();
        
        return result;
        
    }
}

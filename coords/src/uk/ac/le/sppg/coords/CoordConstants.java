package uk.ac.le.sppg.coords;


/**
 * Constants used by the coordinate classes.
 * @author Nigel Wade
 */
public interface CoordConstants {
	
    /**
     * Semi-major axis (equatorial radius) in metres. (WGS 84)
     */
    public static final double A = 6378137.0;      // radius in metres.
    public static final double ASQUARED = A*A;
    
    /**
     * Flattening (WGS 84)
     */
    public static final double flattening = 1.0 / 298.257223563;
    
    /**
     * Eccentricity
     */
    public static final double eccentricity = Math.sqrt( 2*flattening - flattening*flattening );
    
    /**
     * Semi-minor axis (polar radius).
     * (oblateness values for the Earth).
     */
    public static final double B = A*(1.0-flattening);
    public static final double BSQUARED = B*B;
}

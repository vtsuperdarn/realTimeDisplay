package uk.ac.le.sppg.coords;



import java.io.*;

/**
 * This class holds a vector in polar coordinates.
 * <p>
 * The fields are immutable.
 * 
 * @author Nigel Wade
 */
public class PolarVector implements Serializable {

    private static final long serialVersionUID = 0x5253505047000005L;
    
    /**
     * the radius of the vector
     */
    public final double r;	
    /**
     * the angle from the vertical (z) axis in radians
     */
    public final double theta;
    /**
     * the "azimuth" angle in radians
     */
    public final double phi;
    
    /**
     * Creates a <code>PolarVector</code> from the given values.
     * @param r
     * the radius of the vector
     * @param theta
     * the angle from vertical in radians
     * @param phi
     * the "azimuth" angle in radians.
     */
    PolarVector( double r, double theta, double phi ) {
    	this.r = r;
    	this.theta = theta;
    	this.phi = phi;
    }
    
    /**
     * Converts this vector into cartesian coordinates
     * @return
     * a <code>CartesianVector</code> equavlent of this vector.
     */
    public CartesianVector toCartesian() {
    	
    	double x = r * Math.sin( theta ) * Math.cos( phi );
    	double y = r * Math.sin( theta ) * Math.sin( phi );
    	double z = r * Math.cos( theta );
    	
    	return new CartesianVector( x, y, z );
    }
  
}

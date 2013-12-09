package uk.ac.le.sppg.coords;


import java.io.*;

/**
 * Geocentric coordinates are distances from the centre of the Earth
 * in metres represented as a <code>CartesianVector</code>.
 * <p>
 * X is the equatorial distance along the 0 meridian.
 * Y is the equatorial distance along the 90E meridian.
 * Z is the north polar distance.
 *
 * @author Nigel Wade
 */
public class Geocentric extends CartesianVector implements CoordConstants, Serializable {
    
    private static final long serialVersionUID = 0x5253505047000003L;
    /**
     * creates a new <code>Geocentric</code> vector based on the
     * X, Y, Z coordinte values.
     * @param x
     * the X coordinate (metres)
     * @param y
     * the Y coordinate (metres)
     * @param z
     * the Z coordinate (metres)
     */
    public Geocentric( double x, double y, double z ) {
        super( x, y, z );
    }
    
    /**
     * Creates a <code>Geocentric</code> vector from an existing
     * <code>CartesianVector</code>
     * @param v
     * <code>CartesianVector</code> to be copied.
     */
    public Geocentric( CartesianVector v ) {
        
        super( v.x, v.y, v.z );
    }
    
    
    /**
     * Converts this location to <code>Geographic</code> coordinates.
     * After K.M. Borkowski,  Torun Radio Astronomy Observatory,
     *  "ACCURATE ALGORITHMS TO TRANSFORM GEOCENTRIC TO GEODETIC COORDINATES"
     * 	http://www.astro.uni.torun.pl/~kb/Papers/geod/Geod-BG.htm
     * @return
     * the <code>Geographic</code> equivalent of this location.
     */
    public Geographic toGeographic() {
        
        double lat;
        double lon;
        double alt;
        
        // if the position is along the pole then know all the values.
        if ( Math.abs( x ) < 1.0 && Math.abs( y ) < 1.0 ) {
            lon = 0.0;
            if ( z > 0.0 )
                lat = 90.0;
            else
                lat = -90.0;
            
            alt = z - B;
            
        } else {
            
            // longitude is easy
            lon = Math.toDegrees( Math.atan2( y, x ) );
            
            double rSquared = x*x + y*y;
            double r = Math.sqrt( rSquared );  // equatorial component.
            
            // useful constants
            double W = Math.atan2( B*z, A*r );
            double c = (ASQUARED - BSQUARED) / Math.sqrt(ASQUARED*rSquared + BSQUARED*z*z);
            
            // starting value of iterated solution
            double newy = Math.atan2( A*z, B*r );
            
            double w;
            double oldy;
            
            do {
                oldy = newy;
                
                w = 2 * ( Math.cos(oldy-W) - c*Math.cos(2*oldy) );
                newy = oldy - ( 2*Math.sin(oldy-W) - c*Math.sin(2*oldy) ) / w;
                
            } while( Math.abs(newy-oldy)/Math.abs(newy) > 0.01 );
            
            lat = Math.atan( (A/B) * Math.tan(newy) );
            alt = (r - A*Math.cos(newy)) * Math.cos(lat) + (z - B*Math.sin(newy)) * Math.sin(lat);
            lat = Math.toDegrees( lat );
        }
        
        return new Geographic( lat, lon, alt );
        
    }
    
//    public Geographic toGeographic() {
//
//    	double lat;
//    	double lon;
//    	double alt;
//
//    	// if the position is along the pole then know all the values.
//    	if ( Math.abs( x ) < 1.0 && Math.abs( y ) < 1.0 ) {
//    	    lon = 0.0;
//    	    if ( z > 0.0 )
//    	        lat = 90.0;
//    	    else
//    	        lat = -90.0;
//
//    	    alt = z - B;
//
//    	}
//    	else {
//
//    	    // longitude is easy
//    	    lon = Math.toDegrees( Math.atan2( y, x ) );
//
//    	    /*
//    	     * have to calculate the latitude and altitude by iteration.
//    	     * estimate tan(latitude) from initial values of
//    	     * tan(lat) and latitude. stop when latitude and altitude do
//    	     * not vary much.
//    	     * should converge quickly for reasonable altitudes.
//    	     */
//
//    	    double p = Math.sqrt( x*x + y*y );
//
//    	    // initial values for the iteration
//    	    int i = 0;
//
//    	    // assumes initial altitude of zero.
//    	    double tanLat = z * A*A / ( p * B*B );
//
//    	    double temp = Math.sqrt( A*A + B*B*tanLat*tanLat );
//    	    lat = Math.toDegrees( Math.atan( tanLat ) );
//    	    alt = 0.0;
//
//    	    double latold;
//    	    double altold;
//    	    double temp2;
//
//    	    do {
//    	    	latold = lat;
//    	    	altold = alt;
//
//    	    	// iterate the next value of tan(lat) from old one
//    	    	tanLat = z / ( p - (A*A - B*B)/temp );
//
//    	    	// calculate new estimates of latitude and altitude
//                // based on new value of tan(lat).
//    	    	temp = Math.sqrt( A*A + B*B*tanLat*tanLat );
//    	    	temp2 = Math.sqrt( 1.0 + tanLat*tanLat );
//
//    	    	lat = Math.toDegrees( Math.atan( tanLat ) );
//    	    	alt = temp2 * ( p - A*A / temp );
//
//    	    	i++;
//
//    	    } while ( (Math.abs( lat - latold ) > 0.01 ||
//    	    	       Math.abs( alt - altold ) > 0.01) &&
//    	    	      i < 20 );
//
//    	}
//
//    	return new Geographic( lat, lon, alt );
//    }
    
    
    
    /**
     * Calculates the <code>Topocentric</code> location of this vector
     * as viewed from the reference site, <code>siteName</code>.
     * <p>
     * <code>siteName</code> must already exist in the static list of
     * sites in <code>SiteList</code>. If <code>siteName</code> does
     * not exist this method returns <code>null</code>.
     *
     * @param siteName
     * the name of a site in the <code>SiteList</code>.
     * @return
     * a <code>Topocentric</code> vector from <code>siteName</code> to
     * this vector, or <code>null</code> if <code>siteName</code> does
     * not exist.
     * @see coords.SiteList
     */
    public Topocentric toTopocentric( String siteName ) {
        
        Site site = GeneralSiteList.getList().get(siteName );
        return toTopocentric(site);
    }
    
    public Topocentric toTopocentric( Site site ) {
        
        if ( site == null )
            return null;
        
        /* get vector from site to target in geocentric coords */
        CartesianVector siteToTarget = this.difference( site.getGeocentric() );
        
        /* convert it to local cartesian coords */
        siteToTarget = siteToTarget.rotate( site.getLocalToGeocentric() );
        
        PolarVector p = siteToTarget.toPolar();
        
        Topocentric result = new Topocentric( 180.0-Math.toDegrees( p.phi ),
                90.0-Math.toDegrees( p.theta ),
                p.r,
                site.getName() );
        return result;
    }
    
    
    /**
     * Calculates the <code>Topocentric</code> location of this vector
     * as viewed from the geographic location <code>origin</code>.
     * @param origin
     * A <code>Geographic</code> viewpoint.
     * @return
     * a <code>Topocentric</code> vector from <code>origin</code> to
     * this vector.
     */
    public Topocentric toTopocentric( Geographic origin ) {
        
        Site site = new GeneralSite( "", "", "", -1, origin.latitude, origin.longitude, origin.altitude );
        
        
        /* get vector from site to target in geocentric coords */
        CartesianVector siteToTarget = this.difference( site.getGeocentric() );
        
        /* convert it to local cartesian coords */
        siteToTarget = siteToTarget.rotate( site.getGeocentricToLocal() );
        
        PolarVector p = siteToTarget.toPolar();
        
        Topocentric result = new Topocentric( 180.0-Math.toDegrees( p.phi ),
                90.0-Math.toDegrees( p.theta ),
                p.r,
                site );
        return result;
    }
    
    @Override
    public String toString() {
        String result = new String( "Geocentric: X: "+x+" Y: "+y+" Z: "+z );
        return result;
    }
}

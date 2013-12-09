package uk.ac.le.sppg.coords;


import java.io.Serializable;
import java.text.NumberFormat;

/**
 * This class holds a geographic location.
 * <p>
 * This class is immutable.
 * 
 * @author Nigel Wade
 */
public class Geographic implements CoordConstants, Serializable {

    private static final long serialVersionUID = 0x5253505047000004L;
    
    private NumberFormat nf = NumberFormat.getNumberInstance();
    
    /**
     * geodetic latitude in degrees
     */
    public final double latitude;	// degrees
    /**
     * geodetic longitude in degrees
     */
    public final double longitude;	// degrees
    /**
     * geodetic altitude above the surface in metres.
     */
    public final double altitude;	// metres
    /**
     * the latitude in radians
     */
    public final double latRadians;
    /**
     * the longitude in radians.
     */
    public final double lonRadians;
    
    /**
     * Create a <code>Geographic</code> object from geodetic values.
     * @param lat
     * the geodetic latitude in degrees
     * @param lon
     * the geodetic longitude in degrees
     * @param alt
     * the altitude in metres
     */
    public Geographic( double lat, double lon, double alt ) {
    	this.latitude = lat;
    	this.longitude = lon;
    	this.altitude = alt;
    	
   	    this.latRadians = Math.toRadians( latitude );
    	this.lonRadians = Math.toRadians( longitude );
        
        nf.setMaximumFractionDigits(2);
    }
    
    /**
     * Converts this location to <code>Geocentric</code> coordinates.
     * @return
     * the <code>Geocentric</code> equivalent of this location.
     */
    public Geocentric toGeocentric ( ) {
 
    	double sinLat = Math.sin( latRadians );
    	double cosLat = Math.cos( latRadians );
    	double sinLon = Math.sin( lonRadians );
    	double cosLon = Math.cos( lonRadians );
    
    	/* equations validated against WGS 84 */
    	
    	double primeRadius = ASQUARED / Math.sqrt( ASQUARED*cosLat*cosLat + BSQUARED*sinLat*sinLat );
    
    	double x = (primeRadius + altitude) * cosLat * cosLon;
    	double y = (primeRadius + altitude) * cosLat * sinLon;
    	double z = (primeRadius*BSQUARED/(ASQUARED) + altitude) * sinLat;
    
    	return new Geocentric( x, y, z );
    
    }

    /**
     * converts this object to a string reprentation
     * @return
     * a string representation of the geographic coordinates.
     */
    public String toString() {
        String result = new String( "Geographic: lat: "+nf.format(latitude)+'\u00b0'+" lon: "+nf.format(longitude)+'\u00b0'+" alt: "+nf.format(altitude/1000)+"km" );
        return result;
    }    
}

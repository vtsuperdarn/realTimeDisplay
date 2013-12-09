package uk.ac.le.sppg.coords;


import java.io.*;

/**
 * Representation of a location in topocentric coordinates from a 
 * reference site.
 * <p>
 * The reference site may be specified by name (in which case it must 
 * exist in the <code>SiteList</code>, by name and <code>Geographic</code>
 * location (the site will be added to <code>SiteList</code>, 
 * or as a <code>SiteList.Site</code>.
 * <p>
 * This class is immutable.
 * 
 * @author Nigel Wade
 */
public class Topocentric implements Serializable {

    private static final long serialVersionUID = 0x525350504700000AL;
    
    /**
     * the azimuth at which this location appears from the reference
     * site in degrees.
     */
    public final double azimuth;
    /**
     * the elevation at which this location appears from the reference
     * site in degrees.
     */
    public final double elevation;
    /**
     * the distance between this location and the reference site in metres.
     */
    public final double range;
    
    /**
     * the reference site.
     */
    public final Site site;
    
    
    /**
     * Create a <code>Topocenctric</code> vector relative to <code>site</code>.
     * 
     * @param az
     * azimuth from site to this location (degrees).
     * @param el
     * elevation from site to this location (degrees).
     * @param r
     * distance from site to this location (metres).
     * @param site
     * name of a reference site in <code>SiteList</code>.
     */
    public Topocentric( double az, double el, double r, String site ) {
    	this.azimuth = az;
    	this.elevation = el;
    	this.range = r;
    	
    	this.site = GeneralSiteList.getList().get(site );
    }
    
    /**
     * Create a <code>Topocenctric</code> vector relative to <code>site</code>.
     * 
     * @param az
     * azimuth from site to this location (degrees).
     * @param el
     * elevation from site to this location (degrees).
     * @param r
     * distance from site to this location (metres).
     * @param site
     * name with which site will be known.
     * @param origin
     * <code>Geographic</code> location of reference site.
     */
    public Topocentric( double az, double el, double r, String site, Geographic origin ) {
    	this.azimuth = az;
    	this.elevation = el;
    	this.range = r;
    	
    	this.site = new GeneralSite( site, "x", "", -1, origin.latitude, origin.longitude, origin.altitude );
    	
    }
     
    /**
     * Create a <code>Topocenctric</code> vector relative to <code>site</code>.
     * 
     * @param az
     * azimuth from site to this location (degrees).
     * @param el
     * elevation from site to this location (degrees).
     * @param r
     * distance from site to this location (metres).
     * @param site
     * the reference site from <code>SiteList</code>
     */
    public Topocentric( double az, double el, double r, Site site ) {
    	this.azimuth = az;
    	this.elevation = el;
    	this.range = r;
    	
    	this.site = site;
    }


    /**
     * Creates a <code>Topocentric</code> representation of this location.
     * @return
     * this location as a <code>Topocentric</code> vector.
     */
    public Geocentric toGeocentric() {

    	double sinAz = Math.sin( Math.toRadians( this.azimuth ) );
    	double sinEl = Math.sin( Math.toRadians( this.elevation ) ); 
    	double cosAz = Math.cos( Math.toRadians( this.azimuth ) );
    	double cosEl = Math.cos( Math.toRadians( this.elevation ) );
    
    	CartesianVector siteToTarget = new CartesianVector(
    	    -range*cosAz*cosEl, range*sinAz*cosEl, range*sinEl );
    
    	siteToTarget = siteToTarget.rotate( site.getLocalToGeocentric() );
    	
    	Geocentric result = new Geocentric( siteToTarget.sum( site.getGeocentric() ) );
    	return result;
    }
    
    /**
     * Creates a <code>String</code> representation of this location.
     * @return
     * this location as a <code>String</code>.
     */
    public String toString() {
        String result = new String( "Topocentric: az: "+azimuth+'\u00b0'+" el: "+elevation+'\u00b0'+" range: "+range/1000+"km" );
        return result;
    }
}

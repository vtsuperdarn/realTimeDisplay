package uk.ac.le.sppg.coords.superdarn;

import uk.ac.le.sppg.coords.GeneralSite;
import uk.ac.le.sppg.coords.Geocentric;
import uk.ac.le.sppg.coords.Geographic;
import java.util.Date;


/**
 * The SiteList class holds a set of pre-defined sites which can be
 * accessed either by name or index number.
 * <p>
 * A <code>Site</code> contains its location in <code>Geographic</code>
 * and <code>Geocentric</code> coordinates, its name, abbreviated name,
 * and a station id. SuperDARN sites are sub-classed from <code>Site</code>
 * as <code>SuperDarnSite</code> and additionally have values for
 * boresite, beam separation and receiver rise time.
 * <p>
 * <code>Site.stationId</code> for EISCAT sites is the
 * NCAR number, and for SuperDARN sites is the station id number.
 * <p>
 * The class statically pre-defines the EISCAT and SuperDARN sites plus
 * a few other sites of interest.
 * Other sites can be added to the list explicitely by the
 * <code>addSite</code> and <code>addSuperDarnSite</code> methods,
 * and implicitely by the
 * <code>Topocenctric(double, double, double, String, Geographic)</code>
 * constructor.
 *
 *
 * @author Nigel Wade
 */

import java.util.TreeMap;

/**
 * This class extends <code>Site</code> to add values which are
 * only applicable to SuperDARN sites.
 *
 * @author Nigel Wade
 */
public class SuperDarnSite extends GeneralSite {
    private static final long serialVersionUID = 0x5253505047000108L;
    
    private final TreeMap<Date,SuperDarnSiteDetails> details = new TreeMap<Date,SuperDarnSiteDetails>();
    
    SuperDarnSite( String n, String n2, String i, int id,
            double lat, double lon, double alt,
            double bore, double sep, double rise, int maxBeams) {
        
        super(n, n2, i, id, lat, lon, alt);
        
        SuperDarnSiteDetails detail = new SuperDarnSiteDetails(new Geographic(lat, lon, alt), bore, sep, rise, maxBeams);
        
        // default validity is for ever
        Date date = new Date(Long.MAX_VALUE);
        details.put(date, detail);
        
    }
    
    SuperDarnSite( String n, String n2, String i, int id,
            Date date,
            double lat, double lon, double alt,
            double bore, double sep, double rise, int maxBeams) {
        
        super(n, n2, i, id, lat, lon, alt);

        SuperDarnSiteDetails detail = new SuperDarnSiteDetails(new Geographic(lat, lon, alt), bore, sep, rise, maxBeams);
        
        details.put(date, detail);
        
    }
    
    public void addDetails(Date date, double lat, double lon, double alt, double bore, double sep, double rise, int maxBeams) {
        SuperDarnSiteDetails detail = new SuperDarnSiteDetails(new Geographic(lat, lon, alt), bore, sep, rise, maxBeams);
        details.put(date, detail);
    }
    
//    public Geocentric getGeocentric() {
//        
//        // default to get details for today
//        return getDetails(new Date()).getGeocentric();
//    }
//
//    public Geocentric getGeocentric(Date date) {
//        return getDetails(date).getGeocentric();
//    }
//
//    public Geographic getGeographic() {
//        // default to get details for today
//        return getDetails(new Date()).getGeographic();
//    }
//    
//    public Geographic getGeographic(Date date) {
//        return getDetails(date).getGeographic();
//    }
//
//    public String getIdentifier() {
//        return identifier;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public String getShortName() {
//        return shortName;
//    }

    public int getStationId() {
        return stationId;
    }


    /**
     * tests whether this <code>Site</code> is a SuperDARN site.
     * @return
     * <code>true</code> if the site is a SuperDARN site.
     */
    public boolean isSuperDarnSite() {
        return true;
    }
    
    private SuperDarnSiteDetails getDetails(Date date) {
        for(Date d:details.keySet()) {
            if ( d.after(date)) 
                return details.get(d);
        }
        
        // this shouldn't happen, but return the final value anyway
        return details.get(details.lastKey());
    }
 
    public double getBeamSeparation() {
        return getDetails(new Date()).getBeamSeparation();
    }
    public double getBeamSeparation(Date date) {
        return getDetails(date).getBeamSeparation();
    }
 
    public double getRxRiseTime() {
        return getDetails(new Date()).getRxRiseTime();
    }
    public double getRxRiseTime(Date date) {
        return getDetails(date).getRxRiseTime();
    }

    public double getBoreSite() {
        return getDetails(new Date()).getBoreSite();
    }
    public double getBoreSite(Date date) {
        return getDetails(date).getBoreSite();
    }

    public int getMaxBeams() {
        return getDetails(new Date()).getMaxBeams();
    }
    public int getMaxBeams(Date date) {
        return getDetails(date).getMaxBeams();
    }

    /**
     *
     */
    public Geographic siteCentre( int firstRange, int separation, int nRanges) {
        
        Geocentric beam0 = GeoMap.geo(false, this, firstRange, separation, 0, 0, nRanges).toGeocentric();
        Geocentric beam15 = GeoMap.geo(false, this, firstRange, separation, 0, 15, nRanges).toGeocentric();
        Geocentric siteLocation = details.get(details.lastKey()).getGeocentric();
        
        // calculate centre as the controid of the triangle formed
        // by the radar location, beam0 and beam15.
        Geocentric centroid = new Geocentric(
                (siteLocation.x + beam0.x + beam15.x) / 3.0,
                (siteLocation.y + beam0.y + beam15.y) / 3.0,
                (siteLocation.z + beam0.z + beam15.z) / 3.0);
        
        return centroid.toGeographic();
    }    
    
}

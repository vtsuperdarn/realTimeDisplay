/*
 * Site.java
 *
 * Created on 20 September 2007, 12:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.coords;

import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

/**
 * This internal class defines a general site by name, id, numeric id,
 * Geographic and Topocenctric location.
 *
 * @author Nigel Wade
 */
public class GeneralSite implements Site, Serializable {
    private static final long serialVersionUID = 0x5253505047000007L;
    
    /**
     * the name of the site.
     * A site can be retrieved from the SiteList by its name.
     */
    protected final String name;
    /**
     * the compact name of the site, name with whitespace removed
     * A site can be retrieved from the SiteList by its name.
     */
    protected final String compactName;
    /**
     * the short (3 char) name of the site.
     * A site can be retrieved from the SiteList by its name.
     */
    protected final String shortName;
    /**
     * an additional identifier.
     */
    protected final String identifier;
    /**
     * an integer station id.
     * For general sites this can be anything. The NCAR number is
     * used for EISCAT sites.
     */
    protected final int stationId;

    private final TreeMap<Date,SiteDetails> details = new TreeMap<Date,SiteDetails>();
    
    public GeneralSite(String name, String shortName, String identifier, int id, double lat, double lon, double alt) {
        
        this.name = new String(name);
        this.compactName = this.name.replaceAll("\\s", "");
        this.shortName = new String(shortName);
        this.identifier = new String(identifier);
        stationId = id;
        
        SiteDetails detail = new SiteDetails(new Geographic(lat, lon, alt));
        
        // default validity is for ever
        Date date = new Date(Long.MAX_VALUE);
        details.put(date, detail);
    }
    
    public GeneralSite(String name, String shortName, String identifier, Date date, 
            int id, double lat, double lon, double alt) {
        
        this.name = new String(name);
        this.compactName = this.name.replaceAll("\\s", "");
        this.shortName = new String(shortName);
        this.identifier = new String(identifier);
        stationId = id;
        
        SiteDetails detail = new SiteDetails(new Geographic(lat, lon, alt));
        
        details.put(date, detail);
    }

    public void addDetails(Date date, int id, double lat, double lon, double alt) {
        SiteDetails detail = new SiteDetails(new Geographic(lat, lon, alt));        
        details.put(date, detail);
    }
    
    public Geocentric getGeocentric() {
        
        // default to get details for today
        return getDetails(new Date()).getGeocentric();
    }

    public Geocentric getGeocentric(Date date) {
        return getDetails(date).getGeocentric();
    }

    public Geographic getGeographic() {
        // default to get details for today
        return getDetails(new Date()).getGeographic();
    }
    
    public Geographic getGeographic(Date date) {
        return getDetails(date).getGeographic();
    }
    
    public TransformMatrix getGeocentricToLocal() {
        return getDetails(new Date()).getGeocentricToLocal();
    }
    public TransformMatrix getGeocentricToLocal(Date date) {
        return getDetails(date).getGeocentricToLocal();
    }
    
    public TransformMatrix getLocalToGeocentric() {
        return getDetails(new Date()).getLocalToGeocentric();
    }
    public TransformMatrix getLocalToGeocentric(Date date) {
        return getDetails(date).getLocalToGeocentric();
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getCompactName() {
        return compactName;
    }

    public int getStationId() {
        return stationId;
    }


    /**
     * tests whether this <code>Site</code> is a SuperDARN site.
     * @return
     * <code>true</code> if the site is a SuperDARN site.
     */
    public boolean isSuperDarnSite() {
        return false;
    }
    
    private SiteDetails getDetails(Date date) {
        for(Date d:details.keySet()) {
            if ( d.after(date)) 
                return details.get(d);
        }
        
        // this shouldn't happen, but return the final value anyway
        return details.get(details.lastKey());
    }
    
}

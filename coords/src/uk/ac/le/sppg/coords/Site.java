/*
 * Site.java
 *
 * Created on 20 September 2007, 12:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.coords;

import java.util.Date;

/**
 * This internal class defines a general site by name, id, numeric id,
 * Geographic and Topocenctric location.
 *
 * @author Nigel Wade
 */
public interface Site  {
       
    /**
     * the name of the site.
     * A site can be retrieved from the SiteList by its name.
     */
    public abstract String getName();
    /**
     * the compact name of the site (the name with whitespace removed).
     * A site can be retrieved from the SiteList by its compact name.
     */
    public abstract String getCompactName();
    /**
     * the short (3 char) name of the site.
     * A site can be retrieved from the SiteList by its name.
     */
    public abstract String getShortName();
    /**
     * an additional single character identifier.
     */
    public abstract String getIdentifier();
    /**
     * an integer station id.
     * For general sites this can be anything. The NCAR number is
     * used for EISCAT sites.
     */
    public abstract int getStationId();
    /**
     * the <code>Geographic</code> location of the site.
     */
    public abstract Geographic getGeographic();
    public abstract Geographic getGeographic(Date date);
    /**
     * the <code>Geocentric</code> location of the site.
     */
    public abstract Geocentric getGeocentric();
    public abstract Geocentric getGeocentric(Date date);
    
    /**
     * tests whether this <code>Site</code> is a SuperDARN site.
     * @return
     * <code>true</code> if the site is a SuperDARN site.
     */
    public abstract boolean isSuperDarnSite();
    
    public abstract TransformMatrix getGeocentricToLocal();
    public abstract TransformMatrix getLocalToGeocentric();
    public abstract TransformMatrix getGeocentricToLocal(Date date);
    public abstract TransformMatrix getLocalToGeocentric(Date date);
    

}

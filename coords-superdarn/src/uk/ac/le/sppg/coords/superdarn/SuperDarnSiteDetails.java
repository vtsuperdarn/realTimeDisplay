/*
 * SiteDetails.java
 *
 * Created on 19 November 2007, 14:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.coords.superdarn;

import uk.ac.le.sppg.coords.Geocentric;
import uk.ac.le.sppg.coords.Geographic;
import uk.ac.le.sppg.coords.SiteDetails;
import uk.ac.le.sppg.coords.TransformMatrix;

/**
 *
 * @author nigel
 */
public class SuperDarnSiteDetails extends SiteDetails {
        
    /**
     * the separation between beams in degrees.
     */
    protected final double boreSite;
    /**
     * the azimuth of the boresite of the array in degrees.
     */
    protected final double beamSeparation;
    /**
     * the rise time of the receiver in uS.
     */
    protected final double rxRiseTime;
    /**
     * the maximum number of beams.
     */
    protected final int maxBeams;
    
    SuperDarnSiteDetails(Geographic geographic, double boreSite, double beamSeparation, double rxRiseTime, int maxBeams) {
       super(geographic);
        this.boreSite = boreSite;
        this.beamSeparation = beamSeparation;
        this.rxRiseTime = rxRiseTime;
        this.maxBeams = maxBeams;
        double d = geographic.altitude;
    }

    SuperDarnSiteDetails(Geocentric geocentric, double boreSite, double beamSeparation, double rxRiseTime, int maxBeams) {
        super(geocentric);
        this.boreSite = boreSite;
        this.beamSeparation = beamSeparation;
        this.rxRiseTime = rxRiseTime;
        this.maxBeams = maxBeams;
    }
    
    public double getBeamSeparation() {
        return beamSeparation;
    }
    
    public double getRxRiseTime() {
        return rxRiseTime;
    }

    public double getBoreSite() {
        return boreSite;
    }

    public int getMaxBeams() {
        return maxBeams;
    }


    
}

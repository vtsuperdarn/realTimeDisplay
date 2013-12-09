/*
 * SuperDarnBandSite.java
 *
 * Created on 20 September 2007, 12:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.coords.superdarn;

import java.util.Date;

/**
 *
 * @author nigel
 */

// this class defines a SuperDARN site which has restricted frequency bands defined.

public class SuperDarnBandSite extends SuperDarnSite {
    
    private static final long serialVersionUID = 0x5253505047000109L;
    
    FrequencyBand[] bands = new FrequencyBand[0];
    
    SuperDarnBandSite(String n, String n2, String i,
            int id,
            double lat, double lon, double alt,
            double bore, double sep, double rise, int maxBeams,
            FrequencyBand[] bands) {
        
        super(n, n2, i, id, lat, lon, alt, bore, sep, rise, maxBeams);
        
        this.bands = bands;
    }
    
    SuperDarnBandSite(String n, String n2, String i,
            int id, Date date,
            double lat, double lon, double alt,
            double bore, double sep, double rise, int maxBeams,
            FrequencyBand[] bands) {
        
        super(n, n2, i, id, date, lat, lon, alt, bore, sep, rise, maxBeams);
        
        this.bands = bands;
    }
 
    public FrequencyBand[] getBands() {
        return bands;
    }
    
}


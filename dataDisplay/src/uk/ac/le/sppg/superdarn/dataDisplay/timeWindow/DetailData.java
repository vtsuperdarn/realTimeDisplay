package uk.ac.le.sppg.superdarn.dataDisplay.timeWindow;

import java.util.Date;

/**
 * @author Nigel Wade
 */

class DetailData {

    public Date date;
    
    public double lat,
                  lon,
                  range,
                  data;
    
    public int    beam,
                  gate,
                  freq,
                  noise;
                  
    public boolean gs,
                   flag;
                  
}

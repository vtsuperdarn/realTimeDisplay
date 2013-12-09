package uk.ac.le.sppg.superdarn.dataDisplay.fanWindow;

import java.util.*;


/**
 * @author Nigel Wade
 */
class DetailData {

    public Date date;
    
    public double lat,
                  lon,
                  range, // km
                  data;
    
    public int    beam,
                  gate,
                  freq,
                  noise;
                  
    public boolean gs,
                   flag;
                  
}

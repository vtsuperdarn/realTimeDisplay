package uk.ac.le.sppg.superdarn.dataDisplay.timeWindow;



// this class contains the defintion of a single range gate in time/space
// it holds the location of the 4 corners + the data for that gate.

// There are 2 constructors.
//   The primary constructor calculates the 4 corners based on local
//   local coordinates from a radar location
//   The other constructor uses a previous range gate for 2 of the
//   coordinate points, so only 2 have to be calculated.



import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Date;


/**
 * @author Nigel Wade
 */
// class to hold a range gate for the TimeWindow.
// The drawing area for the TimeWindow is mapped in user coordinates
// of kilometres in y and seconds in x.

class RangeGate {

    private Rectangle2D outline;   // the outline of the gate in user coords.
            
   
    public RangeGate( double startRange, double endRange, Date startDate, Date endDate ) {

        long startTime = startDate.getTime();  // milliseconds since epoch
        long endTime = endDate.getTime();
        double width = endTime - startTime;
        
    	outline = new Rectangle2D.Double( startTime, startRange, width, endRange-startRange );	
		
    }
    
    
    public Shape getOutline() { 
    	return outline;
    }
    

}

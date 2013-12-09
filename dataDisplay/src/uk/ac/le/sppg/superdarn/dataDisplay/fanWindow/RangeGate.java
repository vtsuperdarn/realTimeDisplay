package uk.ac.le.sppg.superdarn.dataDisplay.fanWindow;



// this class contains the defintion of a single range gate in space
// it holds the location of the 4 corners + the data for that gate.

// There are 2 constructors.
//   The primary constructor calculates the 4 corners based on local
//   local coordinates from a radar location
//   The other constructor uses a previous range gate for 2 of the
//   coordinate points, so only 2 have to be calculated.

import uk.ac.le.sppg.coords.Site;
import uk.ac.le.sppg.coords.superdarn.GeoMap;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSite;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import uk.ac.le.sppg.coords.Geographic;
import uk.ac.le.sppg.coords.GeneralSiteList;
import uk.ac.le.sppg.coords.Topocentric;
import uk.ac.le.sppg.coords.proj.Projection;

/**
 * @author Nigel Wade
 */
class RangeGate {

  
    public Geographic[] corners = new Geographic[4];
    private Point2D[] projectionCorners = new Point2D[4];
    private GeneralPath path;   // the path to be plotted
    
    //public final int[] x = new int[4];
    //public final int[] y = new int[4];
    
    // this constructor calculates the corners of the range gate 
    // based on local coordinates relative to a radar station.
    // the corners are stored as geographic coords.
    
   
    public RangeGate( Site site, 
		      double az, double el, double beamWidth,
		      double startRange, double endRange ) {
    	double minAz = az - beamWidth / 2.0;
    	double maxAz = az + beamWidth / 2.0;
    	
        double startMetres = startRange * 1000.0;
        double endMetres = endRange * 1000.0;
        
    	Topocentric t = new Topocentric( minAz, el, startMetres, site );
    	corners[0] = t.toGeocentric().toGeographic();
    
    	t = new Topocentric( maxAz, el, startMetres, site );
    	corners[1] = t.toGeocentric().toGeographic();
    	
    	t = new Topocentric( maxAz, el, endMetres, site );
    	corners[2] = t.toGeocentric().toGeographic();
    	
    	t = new Topocentric( minAz, el, endMetres, site );
    	corners[3] = t.toGeocentric().toGeographic();
	
//System.out.println("RangeGate:"+site.getName()+":"+corners[0]+":"+corners[1]+":"+corners[2]+":"+corners[3]);
    }
    
    public RangeGate( SuperDarnSite sdSite, int gateNumber, int beamNumber, 
    			int firstRangeDistance, int rangeSeparation, int rxRiseTime ) {
        
        corners[0] = GeoMap.geo( false, sdSite, firstRangeDistance, rangeSeparation, 
        				rxRiseTime, beamNumber, gateNumber );
        corners[1] = GeoMap.geo( false, sdSite, firstRangeDistance, rangeSeparation, 
        				rxRiseTime, beamNumber+1, gateNumber );
        
        corners[2] = GeoMap.geo( false, sdSite, firstRangeDistance, rangeSeparation, 
        				rxRiseTime, beamNumber+1, gateNumber+1 );
        corners[3] = GeoMap.geo( false, sdSite, firstRangeDistance, rangeSeparation, 
        				rxRiseTime, beamNumber, gateNumber+1 );
        
//System.out.println("RangeGate:"+sdSite.getName()+":"+corners[0]+":"+corners[1]+":"+corners[2]+":"+corners[3]);
    }
    
    
    // this constructor takes a previous range gate and uses its furthest
    // two corners as the nearest 2 corners for this range gate.
    // The other two corners are calculated as for the main constructor.
    public RangeGate( RangeGate previous, Site site, 
		      double az, double el, double beamWidth, double endRange ) {
	
    	double minAz = az - beamWidth / 2.0;
    	double maxAz = az + beamWidth / 2.0;
    	double endMetres = endRange * 1000.0;
        
    	corners[0] = previous.corners[3];
    	corners[1] = previous.corners[2];
    	
    	Topocentric t = new Topocentric( maxAz, el, endMetres, site );
    	corners[2] = t.toGeocentric().toGeographic();
    	
    	t = new Topocentric( minAz, el, endMetres, site );
    	corners[3] = t.toGeocentric().toGeographic();

//System.out.println("RangeGate:"+site.getName()+":"+corners[0]+":"+corners[1]+":"+corners[2]+":"+corners[3]);
    }
    
    public RangeGate( RangeGate previous, SuperDarnSite sdSite, int gateNumber, 
    			int beamNumber, int firstRangeDistance, int rangeSeparation, int rxRiseTime ) {
        
    	corners[0] = previous.corners[3];
    	corners[1] = previous.corners[2];
            
        corners[2] = GeoMap.geo( false, sdSite, firstRangeDistance, rangeSeparation, 
        				rxRiseTime, beamNumber+1, gateNumber+1 );
        corners[3] = GeoMap.geo( false, sdSite, firstRangeDistance, rangeSeparation, 
        				rxRiseTime, beamNumber, gateNumber+1 );
//System.out.println("RangeGate:"+sdSite.getName()+":"+corners[0]+":"+corners[1]+":"+corners[2]+":"+corners[3]);
        
   }

    public void strokePath( ) {

    	// creates the path of the points which outline the range gate
    	
    	path = new GeneralPath( GeneralPath.WIND_EVEN_ODD, projectionCorners.length );
    	path.moveTo( (float)projectionCorners[0].getX(), (float)projectionCorners[0].getY() );
    	//System.out.println( "New gate path" );
    	//System.out.println( "Start at "+projectionCorners[0].getX()+" "+projectionCorners[0].getY() );
	
    	for ( int i=1; i<projectionCorners.length; i++ ) {
    	    path.lineTo( (float)projectionCorners[i].getX(), (float)projectionCorners[i].getY() );
        	//System.out.println( "Line to "+projectionCorners[i].getX()+" "+projectionCorners[i].getY() );
    	}
    	path.closePath();
    }
    
    // strokes the outline of the RangeGate for the corner indices
    // and adds them to the GeneralPath path.
    public void strokeEdges( GeneralPath path, boolean move, int[] indices ) {
        
        if ( indices == null || indices.length == 0 )
            return;
            
        
        if ( move )
            path.moveTo( (float)projectionCorners[indices[0]].getX(), (float)projectionCorners[indices[0]].getY() );
        
        for ( int i=1; i<indices.length; i++ ) { 
            path.lineTo( (float)projectionCorners[indices[i]].getX(), (float)projectionCorners[indices[i]].getY() );
        }
        
    }
    
    public void projectCorners( Projection proj ) {

    	// projects each corner from Geographic coords to user space coords
        //System.out.println( "project gate corners" );
    	for ( int i=0; i<projectionCorners.length; i++ ) {
    	    projectionCorners[i] = proj.geoToPoint( corners[i] );
            //Point p = proj.geoToPoint( corners[i] );
            //x[i] = p.x;
            //y[i] = p.y;
       	}
    	
    	strokePath();
    }
    
    public Point2D getProjectionCorner( int index ) {
        return projectionCorners[index];
    }

    public Geographic getCorner( int index ) {
        return corners[index];
    }
    
    public GeneralPath getPath() { 
    	return path;
    }
    

}

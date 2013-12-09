package uk.ac.le.sppg.superdarn.dataDisplay.fanWindow;



import uk.ac.le.sppg.coords.Site;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Date;

import uk.ac.le.sppg.coords.proj.Projection;
import uk.ac.le.sppg.superdarn.colour.ColourScale;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotSubParameter;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;


/**
 * @author Nigel Wade
 */
// This class holds an array of RangeGate's and the data for them

class Beam {

    // the array of RangeGates in this beam
    RangeGate[] rangeGates;
    
    // first range in km
    final double startRange;
    // range increment in km
    final double rangeIncrement;
    // beam width in degrees
    final double beamWidth;
    // name of radar
    final Site site;

    // the beam number
    final int beam;
    final Integer beamNumber;
    
    Date date;
    short frequency;
    int noise;
    
    // arrays of data
    float[] velocity = null;
    float[] lambdaPower = null;
    float[] lambdaSpectralWidth = null;
    boolean[] groundScatter = null;
    byte[] ranges;
    int nRanges;
    int nBeams;
    int nGates;
    
    private float[] data = null;
    
    
    private byte[] dataMap = null;
    private PlotSubParameter dataType = null;
    
    GeneralPath beamEdge = null;
    
    GeneralPath gateOutlines = null;
    int strokeStep = -1;
    
    boolean dataSet = false;
    
    double minMap, maxMap;
    
    // constructor for general site
    //
    // parameters:
    //  site - site from which beam originates
    //  beam - beam number
    //  az   - azimuth in degrees
    //  el   - elevation in degrees
    //  beamWidth - beam width in degrees
    //  startRange - range to first gate in km
    //  rangeIncrement - range separation in km
    //  nGates -  number of gates
    //  what   - string containing initial data type to be plotted
    //  parent - a FanPlot
    
    public Beam( Site site, int beam, double az, double el, double beamWidth, 
    		 double startRange, double rangeIncrement, int nGates, int maxBeams, PlotSubParameter what ) {
 
//System.out.println("Beam:"+site.getName()+": no:"+beam+" start:"+startRange+" incr:"+rangeIncrement);
    	this.dataType = what;

    	//System.out.println( "New beam: "+beam );
     	this.beam = beam;
        beamNumber = new Integer( beam );
 	
    	rangeGates = new RangeGate[nGates];
    	this.startRange = startRange;
    	this.rangeIncrement = rangeIncrement;
    	this.beamWidth = beamWidth;
    	this.site = site;
    	
        this.nGates = nGates;
        this.nBeams = maxBeams;

    	double range = startRange;
    	
    	// calculate the geographic corners of the first range gate.
    	rangeGates[0] = new RangeGate( site, az, el, beamWidth, 
    				startRange, startRange+rangeIncrement );

        
     	// increment the range twice, so it's now the range to
     	// the end of the second gate.
    	range += 2 * rangeIncrement;

        
    	// calculate the geographic location of the 2nd and subsequent gates
    	// each is calculated using the previous gate as the start.
    	
    	for( int i=1; i<nGates; i++ ) {

    	    rangeGates[i] = new RangeGate( rangeGates[i-1], site, 
    	    				az, el, beamWidth, range );
    	    range += rangeIncrement;
            
    	}

    }
    
    public Beam( SuperDarnSite sdSite, int beam, int firstRangeDistance, 
    		  int rangeSeparation, int rxRiseTime, int nGates, PlotSubParameter what ) {

//System.out.println("Beam:"+sdSite.getName()+": no:"+beam+" start:"+firstRangeDistance+" incr:"+rangeSeparation);
    	this.dataType = what;
    	
    	this.beam = beam;
        beamNumber = new Integer( beam );
    	//System.out.println( "New beam: "+beam );
 	
    	rangeGates = new RangeGate[nGates];
    	
    	if ( rxRiseTime == 0 )
    	    rxRiseTime = (int)sdSite.getRxRiseTime();
    	
    	this.startRange = firstRangeDistance;
    	this.rangeIncrement = rangeSeparation;
    	
    	this.beamWidth = sdSite.getBeamSeparation();
    	this.site = sdSite;
        
        this.nGates = nGates;
        this.nBeams = sdSite.getMaxBeams();

    	
    	// calculate the geographic corners of the first range gate.
    	rangeGates[0] = new RangeGate( sdSite, 0, beam, firstRangeDistance, 
    		  			rangeSeparation, rxRiseTime );
    	
    	// calculate the geographic location of the 2nd and subsequent gates
    	// each is calculated using the previous gate as the start.
	
    	for( int i=1; i<nGates; i++ ) {

    	    rangeGates[i] = new RangeGate( rangeGates[i-1], sdSite, i, beam, firstRangeDistance, 
    		  			rangeSeparation, rxRiseTime);

    	}

    }
    
    private void strokeEdges() {

        int[] indices = new int[2];

        beamEdge = new GeneralPath(); 
        
        // stroke the outline of the gates.
        if ( beam == 0 ) {
            indices[0] = 1;
            indices[1] = 0;
            
            rangeGates[0].strokeEdges( beamEdge, true, indices );

            indices[0] = 0;
            indices[1] = 3;
            for( int i=1; i<nGates; i++ ) {
                rangeGates[i].strokeEdges( beamEdge, false, indices );
            }
            
            indices[0] = 3;
            indices[1] = 2;
            rangeGates[nGates-1].strokeEdges( beamEdge, false, indices );

        }
        else if ( beam == nBeams-1 ) {
            indices[0] = 0;
            indices[1] = 1;
            
            rangeGates[0].strokeEdges( beamEdge, true, indices );

            indices[0] = 1;
            indices[1] = 2;
            for( int i=1; i<nGates; i++ ) {
                rangeGates[i].strokeEdges( beamEdge, false, indices );
            }
            
            indices[0] = 2;
            indices[1] = 3;
            rangeGates[nGates-1].strokeEdges( beamEdge, false, indices );

        }
        else {
            indices[0] = 0;
            indices[1] = 1;            
            rangeGates[0].strokeEdges( beamEdge, true, indices );

            indices[0] = 2;
            indices[1] = 3;            
            rangeGates[nGates-1].strokeEdges( beamEdge, true, indices );
        }

    }
    
    
    public void setData( FitacfData data, double min, double max ) {

    	this.velocity = data.velocity;
    	this.lambdaPower = data.lambdaPower;
    	this.lambdaSpectralWidth = data.lambdaSpectralWidth;
    	this.groundScatter = data.groundScatter;
        this.ranges = data.ranges;
        this.nRanges = data.ranges.length;
        
        this.frequency = data.radarParms.txFrequency;
        this.noise = data.radarParms.noiseLevel;
        
    	
        this.date = data.radarParms.date;
        
    	dataMap = null;
	
    	mapData( min, max );
    	
    	dataSet = true;
    }
    
    
    public void setWhat( PlotSubParameter what, double min, double max ) {

    	if ( what == null || what == dataType && minMap == min && maxMap == max )
    	    return;
    	    
    	this.dataType = what;
    	
    	mapData( min, max );
    }
    
    
    public void mapData( double min, double max ) {
    
    	// if the type of data is already mapped just return.
    	//if ( dataMap != null && what.equals( mappedData ) ) return;
    
        
        switch(dataType.type) {
            case velocity:
                if ( data == this.velocity && minMap == min && maxMap == max )
                    return;
                data = this.velocity;
                break;
            case power:
                if ( data == this.lambdaPower && minMap == min && maxMap == max )
                    return;
                data = this.lambdaPower;
                break;
            case width:
                if ( data == this.lambdaSpectralWidth && minMap == min && maxMap == max )
                    return;
                data = this.lambdaSpectralWidth;
                break;
            default:
        	return;
        }
    	
        minMap = min;
        maxMap = max;
                
    	dataMap = new byte[data.length];
    	for( int i = 0; i < data.length; i++ ) 
    	    if ( data[i] > max )
    	    	dataMap[i] = Byte.MAX_VALUE;
    	    else if ( data[i] < min )
    	    	dataMap[i] = Byte.MIN_VALUE;
    	    else
    	        dataMap[i] = (byte) (Byte.MIN_VALUE + ((data[i]-min) * (Byte.MAX_VALUE-Byte.MIN_VALUE) / (max-min)));

    }
    
    
    public void setProjection( Projection p ) {
    	
    	// project the corners for each of the RangeGates
        //System.out.println( "change projection for beam: "+this.beamNumber );
    	for ( int i=0; i<rangeGates.length; i++ )
    	    rangeGates[i].projectCorners( p );

        // only stroke edges for SuperDarn sites.        
        if ( this.site.isSuperDarnSite() ) {
            beamEdge = new GeneralPath();
            strokeEdges();
        }
        
        if ( strokeStep > 0 ) {
            strokeGates( strokeStep );
        }

    }
    
    	
    public void fillGates( Graphics2D g2, ColourScale scale, boolean gs ) {

    	// fill each RangeGate
    	
    	
    	if ( dataMap == null )
    	    return;
    	    
    	Composite cOld = g2.getComposite();

        int type = AlphaComposite.SRC_OVER;
        float alpha;
        AlphaComposite rule;

    	Color colour;
    	
       
        int rangeIndex = 0;
   	
       	for( int i=0; i<this.nGates; i++ ) {
            
            if ( rangeIndex < this.nRanges && ranges[rangeIndex] == i ) {
                // there is data for this range                
            
                // if it's ground scatter and the flag is set to display ground scatter
                // set the colour to grey.
                if ( gs && groundScatter[rangeIndex] ) {
                    colour = Color.GRAY;
                    alpha = 0.25f;
                } else {
                    colour = scale.colour( dataMap[rangeIndex] );
                    alpha = 0.65f;
                }
                
                rangeIndex++;
                
                rule = AlphaComposite.getInstance(type, alpha);
                g2.setComposite(rule);

                g2.setColor( colour );
                g2.fill( rangeGates[i].getPath() );
                //System.out.println( rangeGates[i].x[0]+" "+rangeGates[i].y[0] );
                //((Graphics)g2).setColor( colour );
                //((Graphics)g2).fillPolygon( rangeGates[i].x, rangeGates[i].y, 4 );
   	        
            }
       	}

        g2.setComposite(cOld);

    }    	
   
    public void drawEdge( Graphics2D g2, Color colour ) {
        
        if ( this.beamEdge != null ) {
            g2.setColor( colour );
            g2.draw( beamEdge );
        }
    }


    // method to stroke a path to draw the outlines of the
    // individual range gates at the interval of step.
    public void strokeGates( int step ) {
        
        this.strokeStep = step;
        
        if ( strokeStep < 0 ) {
            gateOutlines = null;
            return;
        }
        
        Point2D point;
        
        gateOutlines = new GeneralPath();
        
        point = rangeGates[0].getProjectionCorner( 0 );
        gateOutlines.moveTo( (float)point.getX(), (float)point.getY() );
        
        point = rangeGates[rangeGates.length-1].getProjectionCorner( 3 );
        gateOutlines.lineTo( (float)point.getX(), (float)point.getY() );
        
        point = rangeGates[0].getProjectionCorner( 1 );
        gateOutlines.moveTo( (float)point.getX(), (float)point.getY() );
        
        point = rangeGates[rangeGates.length-1].getProjectionCorner( 2 );
        gateOutlines.lineTo( (float)point.getX(), (float)point.getY() );
        
        for( int i=step; i<rangeGates.length; i+= step ) {
            point = rangeGates[i].getProjectionCorner( 0 );
            gateOutlines.moveTo( (float)point.getX(), (float)point.getY() );

            point = rangeGates[i].getProjectionCorner( 1 );
            gateOutlines.lineTo( (float)point.getX(), (float)point.getY() );

        }
        
    }


    // draw the beam outline if it's been created
    public void drawGates( Graphics2D g2, Color colour ) {

    	// fill each RangeGate
    		
    	//System.out.println( "Transform: "+g.getTransform() );
    	
       	if ( gateOutlines != null ) {
            g2.setColor( colour );
            g2.draw( gateOutlines );
       	}

    }
    
    public DetailData findDetails( double x, double y ) {
        
        for( int i=0; i < rangeGates.length; i++ ) {
             
            GeneralPath path = rangeGates[i].getPath();
            
            if ( path.contains( x, y ) ) {
                DetailData d = new DetailData();
                
                d.beam = this.beam;
                d.date = this.date;
                d.freq = this.frequency;
                d.gate = i;
                
                // set approx centre of gate
                d.lat = ( rangeGates[i].corners[0].latitude + 
                            rangeGates[i].corners[2].latitude ) / 2;
                d.lon = ( rangeGates[i].corners[0].longitude + 
                            rangeGates[i].corners[2].longitude ) / 2;
                d.range = startRange + (i+0.5) * rangeIncrement;
                
                d.noise = noise;

                // find the gate in the list of ranges.
                d.flag = false;
                
                for ( int j=0; j<ranges.length; j++ ) {
                    if ( ranges[j] == i ) {
                        d.gs = this.groundScatter[j];
                
                        d.flag = true;
                        d.data = data[j];
                        
                        break;
                    }
                }
                
                return d;
            }
        }
        
        return null;
    }
    
    public void resetDataFlag() {
    	dataSet = false;
    }
    
    public boolean getDataSet() {
    	return dataSet;
    }
}

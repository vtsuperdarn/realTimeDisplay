package uk.ac.le.sppg.superdarn.dataDisplay.timeWindow;



import uk.ac.le.sppg.coords.Site;
import uk.ac.le.sppg.coords.superdarn.GeoMap;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.Date;

import uk.ac.le.sppg.coords.Geographic;
import uk.ac.le.sppg.coords.Topocentric;
import uk.ac.le.sppg.superdarn.colour.ColourScale;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotSubParameter;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;


/**
 * @author Nigel Wade
 */
// This class holds an array of RangeGate's and the data for them

class Beam {
    
    // first range in km
    final double startRange;
    // range increment in km
    final double rangeIncrement;
    final int nGates;
    
    // name of radar
    final Site site;
    
    // the beam number
    final int beam;
    final int rxRiseTime;       // uS.
    final double beamWidth;
    final double minAz, maxAz, el;
    
    
    short frequency;
    int noise;
    
    Date date;
    int integrationPeriod;
    long endTime;
    
    float[] velocity;
    float[] lambdaSpectralWidth;
    float[] lambdaPower;
    boolean[] groundScatter;
    byte[] ranges;
    int nRanges;
    
    private float[] data = null;
    
    //private TimePlot parent;
    
    
    private byte[] dataMap = null;
    private PlotSubParameter dataType = null;
    
    double minMap, maxMap;
    
    // constructor for non-SuperDarn sites
    // parameters
    //
    //  site - the site from which the beam originates
    //  beam - the integer beam number
    //  az   - the beam azimuth (degrees)
    //  el   - the beam elevation (degrees)
    //  beamWidth - the beam width (degrees)
    //  startRange - distance to first range gate (km)
    //  rangeIncrement - distance between gates (km)
    //  nGates - number of range gates
    //  what - string containing what data type is to be plotted
    //  timeWidth - integer, width in seconds of the beam on the plot
    //  parent - a TimePlot in which the beam will be displayed
    
    public Beam( Site site, int beam, double az, double el, double beamWidth,
            double startRange, double rangeIncrement, int nGates ) {
        
        
        //System.out.println( "New beam: "+beam );
        this.beam = beam;
        
        this.startRange = startRange;
        this.rangeIncrement = rangeIncrement;
        this.nGates = nGates;
        this.site = site;
        
        rxRiseTime = -1;
        minAz = az - beamWidth / 2.0;
        maxAz = az + beamWidth / 2.0;
        this.el = el;
        this.beamWidth = beamWidth;
        
        
        
    }
    
    public Beam( SuperDarnSite sdSite, int beam, int firstRangeDistance,
            int rangeSeparation, int rxRiseTime, int nGates ) {
        
        
        
        //System.out.println( "New SuperDARN beam: "+beam );
        this.beam = beam;
        this.rxRiseTime = rxRiseTime;
        
        // this constructor does not use these values.
        this.minAz = this.maxAz = this.el = this.beamWidth = -1;
        
        this.startRange = firstRangeDistance;
        this.rangeIncrement = rangeSeparation;
        this.nGates = nGates;
        this.site = sdSite;
        
        
    }
    
    
    public void setData( FitacfData data  ) {
        
        this.velocity = data.velocity;
        this.lambdaPower = data.lambdaPower;
        this.lambdaSpectralWidth = data.lambdaSpectralWidth;
        this.groundScatter = data.groundScatter;
        this.ranges = data.ranges;
        
        this.nRanges = data.ranges.length;
        
        this.frequency = data.radarParms.txFrequency;
        this.noise = data.radarParms.noiseLevel;
        
        this.date = new Date( data.radarParms.date.getTime() );
        this.integrationPeriod = data.radarParms.integrationPeriod;
        
        //System.out.println( "set date: "+this.date );
        
        dataMap = null;
        
    }
    
    
    public void setWhat( PlotSubParameter what, double min, double max ) {
        
        if ( what == null || what == dataType && min == minMap && max == maxMap )
            return;
        
        this.dataType = what;
        
        mapData( min, max );
    }
    
    
    public void mapData( double min, double max ) {
        
        // if the type of data is already mapped just return.
        //if ( dataMap != null && what.equals( mappedData ) ) return;
        
        if ( dataType == null )
            return;
        
        switch(dataType.type) {
            case velocity:
                if ( data == this.velocity && minMap == min && maxMap == max )
                    return;
                data = this.velocity;
                break;
            case power:
                if ( data == this.lambdaPower  && minMap == min && maxMap == max )
                    return;
                data = this.lambdaPower;
                break;
            case width:
                if ( data == this.lambdaSpectralWidth  && minMap == min && maxMap == max )
                    return;
                data = this.lambdaSpectralWidth;
                break;
            default:
                return;
        }    
        if ( data == null )
            return;
        
        minMap = min;
        maxMap = max;
        
        dataMap = new byte[data.length];
        for( int i = 0; i < data.length; i++ ) {
            if ( data[i] > max )
                dataMap[i] = Byte.MAX_VALUE;
            else if ( data[i] < min )
                dataMap[i] = Byte.MIN_VALUE;
            else
                dataMap[i] = (byte) (Byte.MIN_VALUE + ((data[i]-min) * (Byte.MAX_VALUE-Byte.MIN_VALUE) / (max-min)));
            
            //System.out.println("gate:"+ranges[i]+" range: "+(startRange+i*rangeIncrement)+" data:"+data[i]+" byte:"+dataMap[i] );
        }
    }
    
    
    
    public void fillGates( Graphics2D g2, ColourScale scale, long timeAxisStart, long timeWidth, boolean forward ) {
        
        // fill each RangeGate
        
//System.out.println("fillGates: "+timeAxisStart+ " "+timeWidth+" date:"+this.date);
        if ( dataMap == null || data == null )
            return;
        
        
//System.out.println( "Transform: "+g2.getTransform() );
//Point2D p = new Point2D.Double( this.date.getTime(), startRange );
//Point2D p2 = g2.getTransform().transform( p, null );
//System.out.println( p2 );
//p = new Point2D.Double( this.date.getTime()+timeWidth, startRange+rangeIncrement );
//p2 = g2.getTransform().transform( p, null );
//System.out.println( p2 );
//
        Color colour;
        
        
        GeneralPath outline = new GeneralPath();
        
        
        long startTime, startOffset;
        long endOffset;
        
        // if the integration period of the data is wider than the requested
        // plot width, draw with the integration period of the data.
        if ( integrationPeriod > timeWidth/1000 ) {
            timeWidth = integrationPeriod * 1000;
        }
        
        //AffineTransform t = g2.getTransform();
        
        if ( forward ) {
            startTime = this.date.getTime();
            endTime = startTime + timeWidth;
        } else {
            endTime = this.date.getTime() + integrationPeriod*1000 + timeWidth;
            startTime = endTime - timeWidth;
        }
        
        startOffset = startTime - timeAxisStart;
        endOffset = endTime - timeAxisStart;
        
//        if ( gs && dataType.equals("velocity")) {
//            System.out.println( "data: time: "+date );
//            AffineTransform t = g2.getTransform();
//            Point2D d = new Point2D.Double( startTime, 0.0 );
//            System.out.println( t.transform( d, null ) );
//            d.setLocation( startTime+timeLength, 0.0 );
//            System.out.println( t.transform( d, null ) );
//            System.out.println( this.nRanges );
//            System.out.println( this.nGates );
//        }
        
        int rangeIndex = 0;
        double range = startRange;
        double gateRange;
        
        // this.nGates is the number of gates in the experiment.
        for( int i=0; i<this.nGates; i++ ) {
            
            // first find the colour
            // ranges store the range numbers which have data, indexed from range 1.
            // this.nRanges is the number of ranges which have data.
            if ( rangeIndex < this.nRanges && ranges[rangeIndex] == i ) {
                // there is data for this range
                
                // if it's ground scatter and the flag is set to display ground scatter
                // set the colour to grey.
                if ( dataType.gs && groundScatter[rangeIndex] )
                    colour = Color.GRAY;
                else
                    colour = scale.colour( dataMap[rangeIndex] );
                
                rangeIndex++;
                
                // blank out missing ranges
                outline.reset();
                
                g2.setColor( Color.WHITE );
                
                // range to start of gate
                gateRange = startRange+i*rangeIncrement;
                
                // range contains the end of the previously plotted gate
                // fill with white from there to the start of this gate
                outline.moveTo( startOffset, (float)range );
                outline.lineTo( endOffset, (float)range );
                outline.lineTo( endOffset, (float)gateRange );
                outline.lineTo( startOffset, (float)gateRange );
                outline.closePath();
                
//                if ( !forward && gs && dataType.equals("velocity"))
//                System.out.println( "fill: white: "+range+" to "+gateRange );
                
                range = gateRange + rangeIncrement;
                g2.fill( outline );
                
                // plot the range gate
                g2.setColor( colour );
                
                outline.reset();
                outline.moveTo( startOffset, (float)gateRange );
                outline.lineTo( endOffset, (float)gateRange );
                outline.lineTo( endOffset, (float)range );
                outline.lineTo( startOffset, (float)range );
                outline.closePath();
                
//                if ( !forward && gs && dataType.equals("velocity"))
//                System.out.println( "fill: "+colour+": "+gateRange+" to "+range );
                
                //System.out.println( outline );
                
//Point2D p1 = new Point2D.Double( startTime, gateRange );
//System.out.println( g2.getTransform().transform( p1, null )+" "+startTime );
//p1.setLocation( endTime, range );
//System.out.println( g2.getTransform().transform( p1, null ) );
                
                //System.out.println( colour );
                //System.out.println( startTime+" "+gateRange );
                g2.fill( outline );
            }
        }
        
        // blank out to end of ranges
        double endRange = startRange+this.nGates*rangeIncrement;
        if ( range < endRange ) {
            g2.setColor( Color.WHITE );
            
            outline.reset();
            outline.moveTo( startOffset, (float)range );
            outline.lineTo( endOffset, (float)range );
            outline.lineTo( endOffset, (float)endRange );
            outline.lineTo( startOffset, (float)endRange );
            outline.closePath();
            
//            if ( !forward && gs && dataType.equals("velocity"))
//            System.out.println( "fill: white: "+range+" to "+endRange );
            
            g2.fill( outline );
        }
    }
    
    
    public DetailData findDetails( long dateTime, double range ) {
        
        if ( dateTime < this.date.getTime() || dateTime > this.endTime )
            return null;
        
        double gateRange = startRange;
        
        for( int i=0; i < this.nGates; i++ ) {
            
            
            if ( range >= gateRange && range <= gateRange+rangeIncrement ) {
                DetailData d = new DetailData();
                
                d.beam = this.beam;
                d.date = this.date;
                d.freq = this.frequency;
                d.gate = i;
                
                System.out.println("Detail "+this.date+" "+new Date(this.endTime)+" "+i);
                Geographic corner1;
                Geographic corner2;
                
//System.out.println( site.getClass() );
                
                if ( site.getClass().equals( SuperDarnSite.class ) ) {
                    corner1 =
                            GeoMap.geo( false, (SuperDarnSite)site,
                            (int)startRange, (int)rangeIncrement,
                            rxRiseTime, beam, i );
                    
                    corner2 =
                            GeoMap.geo( false, (SuperDarnSite)site,
                            (int)startRange, (int)rangeIncrement,
                            rxRiseTime, beam+1, i+1 );
                } else {
                    Topocentric t = new Topocentric( minAz, el, startRange*1000.0, site );
                    corner1 = t.toGeocentric().toGeographic();
                    t = new Topocentric( maxAz, el, startRange*1000.0, site );
                    corner2 = t.toGeocentric().toGeographic();
                }
                
                // set approx centre of gate
                d.lat = ( corner1.latitude + corner2.latitude ) / 2;
                d.lon = ( corner1.longitude + corner2.longitude ) / 2;
                d.range = startRange + (i+0.5) * rangeIncrement;
                
                d.noise = noise;
                
                d.flag = false;
                for ( int j=0; j<ranges.length; j++ ) {
                    // ranges counts range gates from 1 up.
                    if ( ranges[j] == i ) {
                        d.flag = true;
                        d.data = data[j];
                        
                        d.gs = groundScatter[j];
                        break;
                    } else if ( ranges[j] > i ) {
                        break;
                    }
                }
                
                return d;
            }
            
            gateRange += rangeIncrement;
            
        }
        
        return null;
    }
    
    public Date getDate() { return date; }
    
    public short getFrequency() { return frequency; }
    public int getNoise() { return noise; }
    
    
}

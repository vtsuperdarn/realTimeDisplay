package uk.ac.le.sppg.superdarn.colour;

/**
 * @author Nigel Wade
 *
 * Creates a colour scale suitable for plotting velocity.
 * The scale runs from white-cyan-blue for +ve and red-yellow-green for -ve.
 */

import java.awt.Color;

public class VelocityScaleTwo extends ColourScale {
    
    
    public VelocityScaleTwo( int nColours ) {  // nSteps indicates how many distinct colours to create
        
        float hue;
        
        this.nColours = nColours;
        
        if ( nColours % 2 != 0 )
            throw new IllegalArgumentException( "Number of colours must be even" );
        
        if ( nColours == 2 ) {
            for ( int i=0; i<NMAPVALUES/2; i++ )
                colourMap[i] = Color.RED;
            for ( int i=NMAPVALUES/2; i<NMAPVALUES; i++ )
                colourMap[i] = Color.BLUE;
            
            return;
        }
        
        // first section is yellow->red
        // this will map values from -127 to zero
        
        // yellow has a hue of 60 in the 0-360 colour space
        hue = 60.0f / 360.0f;
        
        for ( int i=0; i<NMAPVALUES/2; i++ )
            colourMap[i] = new Color( Color.HSBtoRGB( hue-hue/((nColours+1)/2-1)*((nColours*i)/NMAPVALUES), 1.0f, 1.0f ) );
        
        // the next section is for the positive colours and runs from
        // blue->green.
                
        hue = 240.0f/360.0f;
        float endhue = 110.0f/360.0f;
        
        for ( int i=NMAPVALUES/2; i<NMAPVALUES; i++ )
            colourMap[i] = new Color( Color.HSBtoRGB( hue-(hue-endhue)/((nColours+2)/2-1)*((nColours*(i-NMAPVALUES/2))/NMAPVALUES), 1.0f, 1.0f ) );
            }
    
}

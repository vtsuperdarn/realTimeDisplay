package uk.ac.le.sppg.superdarn.colour;

/**
 * @author Nigel Wade
 *
 * Creates a colour scale suitable for plotting velocity.
 * The scale runs from white-cyan-blue for +ve and red-yellow-green for -ve.
 */

import java.awt.Color;

public class VelocityScale extends ColourScale {
    
    
    public VelocityScale( int nColours ) {  // nSteps indicates how many distinct colours to create
        
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
        
        // first section is green->yellow->red
        // this will map values from -127 to zero
        
        // green has a hue of 1/3 of the colour space
        hue = 1.0f / 3.0f;
        
        for ( int i=0; i<NMAPVALUES/2; i++ )
            colourMap[i] = new Color( Color.HSBtoRGB( hue-hue/((nColours+1)/2-1)*((nColours*i)/NMAPVALUES), 1.0f, 1.0f ) );
        
        // the next section is for the positive colours and runs from
        // blue->cyan->white.
        
        // The first bit is from blue (hue 2/3) to cyan (1/2).
        hue = 2.0f/3.0f;
        float endhue = 0.5f;
        
        for ( int i=NMAPVALUES/2; i<NMAPVALUES*3/4; i++ )
            colourMap[i] = new Color( Color.HSBtoRGB( hue-(hue-endhue)/((nColours+2)/4-1)*((nColours*(i-NMAPVALUES/2))/NMAPVALUES), 1.0f, 1.0f ) );
        
        
        // now the last bit which goes from cyan->white
        // saturation runs from 1.0 to 0.0
        hue = 0.5f;
        float saturation = 1.0f;
        for ( int i=NMAPVALUES*3/4; i<NMAPVALUES; i++ ) {
            colourMap[i] = new Color( Color.HSBtoRGB( hue, saturation-saturation/((nColours+2)/4-1)*((nColours*(i-3*NMAPVALUES/4))/NMAPVALUES), 1.0f ) );
        }
    }
    
}

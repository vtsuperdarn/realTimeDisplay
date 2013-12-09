package uk.ac.le.sppg.superdarn.colour;

/**
 * @author Nigel Wade
 *
 * Creates a colour scale suitable for plotting linearly based +ve contours.
 * The scale runs from blue->red.
 */

import java.awt.Color;

public class LinearScale extends ColourScale {

    // set up the colour scale

    
    public LinearScale( int nColours ) {
    	
    	this.nColours = nColours;
    	
    	// the range of hue from blue to red is 0.6667->0.
    	// this needs to be spread over the range of the colours such
    	// that blue and red are always the limits, so scale over nColours-1.
    	float hue = 2.0f / 3.0f;

    	for ( int i=0; i<NMAPVALUES; i++ ) 
    	    colourMap[i] = new Color( Color.HSBtoRGB( hue-hue/(nColours-1)*((nColours*i)/NMAPVALUES), 1.0f, 1.0f ) );


    }
    

}

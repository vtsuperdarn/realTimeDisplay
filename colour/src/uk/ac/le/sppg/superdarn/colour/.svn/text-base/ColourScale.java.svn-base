package uk.ac.le.sppg.superdarn.colour;


import java.awt.Color;


/**
 * A superclass for the colour scale classes.
 * 
 * It provides definitions for the values of the number of colours 
 * in the scale NMAPVALUES, and the colour scale itself (colourMap).
 * It also includes a method for accessing the colour from the colour 
 * table.
 * 
 * It is the job of the sub-class to decide the actual colours
 * in the colourMap.
 * 
 * Data which is to be colour mapped using this scale should normally
 * be mapped onto the range of byte (-128 - 127). Then the method
 * <code>colour(byte)</code> can be called to get the colour 
 * corresponding to that
 * data value.
 * 
 * @author Nigel Wade
 */
public abstract class ColourScale {

    /**
     * the data is mapped into byte, so one colour for each possible
     * value of the data.
     */
    static final int NMAPVALUES = Byte.MAX_VALUE - Byte.MIN_VALUE + 1;

    /**
     * the colours to be used in the colour scale.
     * These values must be set by the concrete sub-class.
     */
    Color[] colourMap = new Color[NMAPVALUES];
    
    /**
     * The number of colours which span the data range.
     * The colour scale always holds NMAPVALUES colours, but 
     * the sub-class will only define nColours distinct colours.
     */
    int nColours;
    
 
    /**
     * returns the colour from the colourMap which corresponds
     * to the byte value parameter. Data should be mapped into 
     * the range of byte, and then this byte value passed to this
     * routine to get the corresponding colour scale value.
     *  
     * @param mapValue
     * the data value mapped to the range of a byte
     * @return
     * the colour from the colour scale which corresponds to a byte
     * value of mapValue.
     */
    public Color colour( byte mapValue ) {
    	
    	// scale the mapValue (range -128->127) onto the 
    	// colour scale range 0->nColours-1;
    	//int index = (mapValue-Byte.MIN_VALUE) / (Byte.MAX_VALUE-Byte.MIN_VALUE) * (nColours-1);
    	return colourMap[128+mapValue];
    	
    }
    
    /**
     * gets the number of distinct colours defined in the colour scale.
     * @return
     * the number of colours defined in this colour scale.
     */
    public int getNColours() {
    	return nColours;
    }

}

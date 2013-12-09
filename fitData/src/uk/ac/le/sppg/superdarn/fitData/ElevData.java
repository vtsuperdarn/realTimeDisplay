package uk.ac.le.sppg.superdarn.fitData;


import java.io.*;

/**
 * <code>ElevData</code> is a part of a {@link fitData.FitData FitData} object. 
 *  
 * @author Nigel Wade
 */

public class ElevData implements Serializable {

    private static final long serialVersionUID = 0x5253505047000018L;

    /**
     * elevation angle
     */
    public double normal;  // elevation angle
    /**
     * lowest estimate of elevation angle
     */
    public double low;     // lowest estimate of elevation angle
    /**
     * highest estimate of elevation angle
     */
    public double high;    // highest estimate of elevation angle
	/**
	 * @return
	 */
	public double getHigh() {
		return high;
	}

	/**
	 * @return
	 */
	public double getLow() {
		return low;
	}

	/**
	 * @return
	 */
	public double getNormal() {
		return normal;
	}

}

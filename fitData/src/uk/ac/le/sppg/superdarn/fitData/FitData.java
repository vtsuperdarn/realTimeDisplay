package uk.ac.le.sppg.superdarn.fitData;

import java.util.Date;



/**
 * The fit data object.
 * <p>
 * This object contains a single fit data record.
 * 
 * @author Nigel Wade
 */
public class FitData implements Comparable {

    /**
     * Pulse pattern length
     */
    public static final int PULSE_PAT_LEN_V110 = 7;
    public static final int PULSE_PAT_LEN_V130 = 16;

    /**
     * Lag table length
     */
    public static final int LAG_TAB_LEN_V110 = 17;
    public static final int LAG_TAB_LEN_V130 = 48;

    /**
     * Comment buffer size.
     * The amount of storage for the comment buffer.
     */
    public static final int COMBF_SIZE = 80;
    /**
     * The maximum number of ranges.
     */
//    public static final int MAX_RANGE = 75;	// max number of range gates
    /**
     * The number of ranges which are sent in a partial data block
     */
    public static final int PART_RANGE = 25; // number of gates in data block

    /**
     * New data message identifier.
     */
    public static final int MESSAGE_ID = 0x49081e00;

    /**
     * the radar parameter block
     */
    public RadarParms radarParms;
    
    /**
     * the pulse pattern
     */
    public short[] pulse;
    /** 
     * the lags table
     */
    public short[][] lag;
     
    /**
     * the comment buffer
     */
    public char[] combf = new char[COMBF_SIZE];
    
    /**
     * noise data
     */
    public NoiseData noise = new NoiseData();
    
    /**
     * fitted auto-correlation data
     */
    public RangeData[] rangeData;
    /**
     * fitted cross-correlation data
     */
    public RangeData[] xRangeData;
    /**
     * elevation angle data
     */
    public ElevData[] elevData;
    
    
    /**
     * Create an empty <code>FitData</code> record.
     */

    public FitData() {

    }
    
    public FitData(int nRanges) {
        rangeData = new RangeData[nRanges];
        for( int i=0; i<nRanges; i++ )
            rangeData[i] = new RangeData();
    }
	/**
	 * @return
	 */
	public char[] getCombf() {
		return combf;
	}

	/**
	 * @return
	 */
	public ElevData[] getElevData() {
		return elevData;
	}

	/**
	 * @return
	 */
	public short[][] getLag() {
		return lag;
	}

	/**
	 * @return
	 */
	public NoiseData getNoise() {
		return noise;
	}

	/**
	 * @return
	 */
	public short[] getPulse() {
		return pulse;
	}

	/**
	 * @return
	 */
	public RadarParms getRadarParms() {
		return radarParms;
	}

	/**
	 * @return
	 */
	public RangeData[] getRangeData() {
		return rangeData;
	}

	/**
	 * @return
	 */
	public RangeData[] getXRangeData() {
		return xRangeData;
	}

    public int compareTo( Object o ) {
        Date date = ((FitData)o).radarParms.date;
        return this.radarParms.date.compareTo(date);
    }
}

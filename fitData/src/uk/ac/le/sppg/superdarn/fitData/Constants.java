package uk.ac.le.sppg.superdarn.fitData;

/**
 * Constants used in the <code>fitData</code> package.
 * @author Nigel Wade
 */

interface Constants {
    /**
     * Version specific constants
     */

    /**
     * Pulse pattern length
     */
    static final int PULSE_PAT_LEN_V110 = 7;
    static final int PULSE_PAT_LEN_V130 = 16; 
    
    /**
     * Lag table length
     */
    static final int LAG_TAB_LEN_V110 = 17;
    static final int LAG_TAB_LEN_V130 = 48;
    
    /**
     * Comment buffer size.
     * The amount of storage for the comment buffer.
     */
    static final int COMBF_SIZE = 80;
    /**
     * The maximum number of ranges.
     */
    static final int MAX_RANGE = 75;	// max number of range gates
    /**
     * The number of ranges which are sent in a partial data block
     */
    static final int PART_RANGE = 25; // number of gates in data block

    /**
     * New data message identifier.
     */
    static final int MESSAGE_ID = 0x49081e00;
    
}

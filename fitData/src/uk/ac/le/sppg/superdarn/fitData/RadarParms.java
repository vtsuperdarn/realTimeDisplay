package uk.ac.le.sppg.superdarn.fitData;


import java.util.*;
import java.io.*;
import java.nio.*;


/**
 * The radar parameter block.
 * 
 * @author Nigel Wade
 */
public class RadarParms implements Cloneable, Serializable {

//    private static final long serialVersionUID = 0x525350504700001BL;
    private static final long serialVersionUID = 0x50C69CC56FD6FC7DL;

    /**
     * the fit data format revision
     */
    public FitRev revision = new FitRev();
    
    /**
     * the station id value.
     * @see coords.SiteList
     */
    public short stationId;

    /**
     * the date of the data
     */
    public Date date = new Date();
    /**
     * the transmitted power 
     */
    public short txPower;
    /**
     * number of times the pulse sequence was tranmitted
     */
    public short numberAveraged;
    /**
     * attenuation level
     */
    public short attnLevel;
    /**
     * lag to first range in uS.
     */
    public short lagToFirstRange;       // uS.
    /**
     * sample separation in 
     */
    public short sampleSeparation;
    /**
     * 
     */
    public short errorCode;
    /**
     *
     */
    public short AGCstatusWord;
    /**
     *
     */
    public short lowPowerStatusWord;
    /**
     *
     */
    public short nBaud;
    /**
     *
     */
    public int noiseLevel;
    /**
     *
     */
    public int noiseMean;
    /**
     *
     */
    public short channel;
    /**
     * receiver rise time in uS.
     */
    public short rxRiseTime;        // uS.
    /**
     * integration period in seconds.
     */
    public short integrationPeriod;
    /**
     * pulse length in uS.
     */
    public short txPulseLength;
    /**
     * lag separation in uS of acf/ccf.
     */
    public short MPlagSeparation;
    /**
     *  number of pulses in multi-pulse
     */
    public short MPnPulses;
    /**
     * number of lags in acf/ccf.
     */
    public short MPnLags;
    /**
     * number of ranges in the data.
     */
    public short numberOfRanges;
    /**
     * distance to first range gate in km.
     */
    public short firstRangeDistance;    // km.
    /**
     * separation between range gates in km.
     */
    public short rangeSeparation;       // km
    /**
     *
     */
    public short beamNumber;
    /**
     * flag indicating data is cross-correlation not auto-correlation.
     */
    public boolean crossCorrelation;
    /**
     * transmitted frequency in kHz.
     */
    public short txFrequency;
    /**
     * indicates direction of scan, -1 for clockwise, 1 for anti-.
     */
    public short scanFlag;

    /**
     * maximum allowed power level
     */
    public int maxPower;
    /**
     * maximum allowed noise level.
     */
    public int maxNoise;
    /**
     *
     */
    public int usrResL1;
    /**
     *
     */
    public int usrResL2;

    /**
     *
     */
    public short controlProgramId; 
    /**
     *
     */
    public short usrResS1;
    /**
     *
     */
    public short usrResS2;
    /**
     *
     */
    public short usrResS3;

    protected transient static TimeZone tz = TimeZone.getTimeZone( "GMT0" );
    protected transient Calendar calendar = new GregorianCalendar( tz);

    
    public Object clone() 
    throws CloneNotSupportedException {  
        RadarParms result = (RadarParms) super.clone(); 
        result.date = (Date) this.date.clone();
            
        return result; 
        
    }

    // method to read the radar parameters from
    // a ByteBuffer.

    public void read( ByteBuffer bb ) 
        throws IOException {
    
        try {
    
            // read the revision, first 2 bytes
            this.revision.major = bb.get();
            this.revision.minor = bb.get();
    
            // read the number of parameters
            bb.getShort();
            // read the station id.
            this.stationId = bb.getShort();
    
            // read the date/time and convert to a Calendar then a Date
            short year = bb.getShort();
            short month = bb.getShort();
            short day = bb.getShort();
            short hour = bb.getShort();
            short minute = bb.getShort();
            short second = bb.getShort();
    
            this.calendar.set( year, month-1, day, hour, minute, second );
            this.calendar.set( Calendar.MILLISECOND, 0 );
            this.date = this.calendar.getTime();
    
            // read the Tx power
            this.txPower = bb.getShort();
    
            this.numberAveraged = bb.getShort();
            this.attnLevel = bb.getShort();
            this.lagToFirstRange = bb.getShort();
            this.sampleSeparation = bb.getShort();
            this.errorCode = bb.getShort();
            this.AGCstatusWord = bb.getShort();
            this.lowPowerStatusWord = bb.getShort();
            this.nBaud = bb.getShort();
    
            this.noiseLevel = bb.getInt();
            this.noiseMean = bb.getInt();
    
            this.channel = bb.getShort();
            this.rxRiseTime = bb.getShort();
            this.integrationPeriod = bb.getShort();
            this.txPulseLength = bb.getShort();
            this.MPlagSeparation = bb.getShort();
            this.MPnPulses = bb.getShort();
            this.MPnLags = bb.getShort();
            this.numberOfRanges = bb.getShort();
            this.firstRangeDistance = bb.getShort();
            this.rangeSeparation = bb.getShort();
            this.beamNumber = bb.getShort();
            this.crossCorrelation = ( bb.getShort() != 0 );
            this.txFrequency = bb.getShort();
            this.scanFlag = bb.getShort();
    
            this.maxPower = bb.getInt();
            this.maxNoise = bb.getInt();
            this.usrResL1 = bb.getInt();
            this.usrResL2 = bb.getInt();
    
            this.controlProgramId = bb.getShort();
            this.usrResS1 = bb.getShort();
            this.usrResS2 = bb.getShort();
            this.usrResS3 = bb.getShort();
         }
         catch ( BufferUnderflowException bue ) {
            throw new IOException( "insufficient data for radar parms" );
         }

    }



   /**
	 * @return
	 */
	public short getAGCstatusWord() {
		return AGCstatusWord;
	}

	/**
	 * @return
	 */
	public short getAttnLevel() {
		return attnLevel;
	}

	/**
	 * @return
	 */
	public short getBeamNumber() {
		return beamNumber;
	}

	/**
	 * @return
	 */
	public short getChannel() {
		return channel;
	}

	/**
	 * @return
	 */
	public short getControlProgramId() {
		return controlProgramId;
	}

	/**
	 * @return
	 */
	public boolean isCrossCorrelation() {
		return crossCorrelation;
	}

	/**
	 * @return
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return
	 */
	public short getErrorCode() {
		return errorCode;
	}

	/**
	 * @return
	 */
	public short getFirstRangeDistance() {
		return firstRangeDistance;
	}

	/**
	 * @return
	 */
	public short getIntegrationPeriod() {
		return integrationPeriod;
	}

	/**
	 * @return
	 */
	public short getLagToFirstRange() {
		return lagToFirstRange;
	}

	/**
	 * @return
	 */
	public short getLowPowerStatusWord() {
		return lowPowerStatusWord;
	}

	/**
	 * @return
	 */
	public int getMaxNoise() {
		return maxNoise;
	}

	/**
	 * @return
	 */
	public int getMaxPower() {
		return maxPower;
	}

	/**
	 * @return
	 */
	public short getMPlagSeparation() {
		return MPlagSeparation;
	}

	/**
	 * @return
	 */
	public short getMPnLags() {
		return MPnLags;
	}

	/**
	 * @return
	 */
	public short getMPnPulses() {
		return MPnPulses;
	}

	/**
	 * @return
	 */
	public short getNBaud() {
		return nBaud;
	}

	/**
	 * @return
	 */
	public int getNoiseLevel() {
		return noiseLevel;
	}

	/**
	 * @return
	 */
	public int getNoiseMean() {
		return noiseMean;
	}

	/**
	 * @return
	 */
	public short getNumberAveraged() {
		return numberAveraged;
	}

	/**
	 * @return
	 */
	public short getNumberOfRanges() {
		return numberOfRanges;
	}

	/**
	 * @return
	 */
	public short getRangeSeparation() {
		return rangeSeparation;
	}

	/**
	 * @return
	 */
	public FitRev getRevision() {
		return revision;
	}

	/**
	 * @return
	 */
	public short getRxRiseTime() {
		return rxRiseTime;
	}

	/**
	 * @return
	 */
	public short getSampleSeparation() {
		return sampleSeparation;
	}

	/**
	 * @return
	 */
	public short getScanFlag() {
		return scanFlag;
	}

	/**
	 * @return
	 */
	public short getStationId() {
		return stationId;
	}

	/**
	 * @return
	 */
	public short getTxFrequency() {
		return txFrequency;
	}

	/**
	 * @return
	 */
	public short getTxPower() {
		return txPower;
	}

	/**
	 * @return
	 */
	public short getTxPulseLength() {
		return txPulseLength;
	}

	/**
	 * @return
	 */
	public int getUsrResL1() {
		return usrResL1;
	}

	/**
	 * @return
	 */
	public int getUsrResL2() {
		return usrResL2;
	}

	/**
	 * @return
	 */
	public short getUsrResS1() {
		return usrResS1;
	}

	/**
	 * @return
	 */
	public short getUsrResS2() {
		return usrResS2;
	}

	/**
	 * @return
	 */
	public short getUsrResS3() {
		return usrResS3;
	}

}

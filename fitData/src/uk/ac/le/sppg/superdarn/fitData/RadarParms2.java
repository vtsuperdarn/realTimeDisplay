package uk.ac.le.sppg.superdarn.fitData;

import java.util.*;
import java.io.*;
import java.nio.*;

/**
 * The radar parameter block.
 * 
 * @author Nigel Wade
 */
public class RadarParms2 extends RadarParms
        implements Cloneable, Serializable {

//    private static final long serialVersionUID = 0x525350504700001BL;
  private static final long serialVersionUID = 0x1L;
  public FitRev fitacfRev = new FitRev();
  public NoiseData noise = new NoiseData();
  public Origin origin = new Origin();
  public float beamAzimuth;
  public IntegrationPeriod integration = new IntegrationPeriod();
  // offset between channels for a stereo radar (zero otherwise).
  public short offset;
  public String comment;

  public RadarParms2() {
    super();
  }

  public RadarParms2(RadarParms parms) {
    super();
    revision = parms.revision;
    stationId = parms.stationId;
    date = parms.date;
    txPower = parms.txPower;
    numberAveraged = parms.numberAveraged;
    attnLevel = parms.attnLevel;
    lagToFirstRange = parms.lagToFirstRange;       // uS.
    sampleSeparation = parms.sampleSeparation;
    errorCode = parms.errorCode;
    AGCstatusWord = parms.AGCstatusWord;
    lowPowerStatusWord = parms.lowPowerStatusWord;
    nBaud = parms.nBaud;
    noiseLevel = parms.noiseLevel;
    noiseMean = parms.noiseMean;
    channel = parms.channel;
    rxRiseTime = parms.rxRiseTime;        // uS.
    integrationPeriod = parms.integrationPeriod;
    txPulseLength = parms.txPulseLength;
    MPlagSeparation = parms.MPlagSeparation;
    MPnPulses = parms.MPnPulses;
    MPnLags = parms.MPnLags;
    numberOfRanges = parms.numberOfRanges;
    firstRangeDistance = parms.firstRangeDistance;    // km.
    rangeSeparation = parms.rangeSeparation;       // km
    beamNumber = parms.beamNumber;
    crossCorrelation = parms.crossCorrelation;
    txFrequency = parms.txFrequency;
    scanFlag = parms.scanFlag;
    maxPower = parms.maxPower;
    maxNoise = parms.maxNoise;
    usrResL1 = parms.usrResL1;
    usrResL2 = parms.usrResL2;
    controlProgramId = parms.controlProgramId;
    usrResS1 = parms.usrResS1;
    usrResS2 = parms.usrResS2;
    usrResS3 = parms.usrResS3;

  }

  public Object clone()
          throws CloneNotSupportedException {
    RadarParms2 result = (RadarParms2) super.clone();
    result.date = (Date) this.date.clone();
    result.noise = this.noise.clone();
    result.origin = this.origin.clone();

    return result;
  }
}

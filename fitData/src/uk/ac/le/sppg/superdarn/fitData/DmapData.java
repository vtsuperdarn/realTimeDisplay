/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.fitData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
 * @author nigel
 */
public class DmapData {
  protected transient static TimeZone tz = TimeZone.getTimeZone( "GMT0" );
  protected transient Calendar calendar = new GregorianCalendar( tz);

  private final ByteBuffer bb;

  DmapData(byte[] data) {
    bb = ByteBuffer.wrap(data);
  }

  DmapData(ByteBuffer bb) {
    this.bb = bb;
  }
  
  public String getString() {
    int offset = bb.position();
    int length = 0;

    // count the length of C null-terminated string.
    while(bb.get(offset+length) != 0 ) {
      length++;
    }

    // extract the bytes in the string
    byte[] buf = new byte[length];
    bb.get(buf);
    bb.get(); // skip the null (zero) terminator
    return new String(buf);
  }

  public DmapType getType() {
    int i = bb.get();
    return DmapType.get(i);
  }

  public final ByteBuffer order(ByteOrder bo) {
    return bb.order(bo);
  }

  public void decode( FitacfData data ) {

    short year = 0;
    short month = 0;
    short day = 0;
    short hour = 0;
    short minute = 0;
    short second = 0;
    int   milliSec = 0;

    int code;
    int size;
    int nScalar;
    int nArray;


    code = bb.getInt();
    size = bb.getInt();
    nScalar = bb.getInt();
    nArray = bb.getInt();

    // read the scalars
    byte dataByte=0;
    short dataShort=0;
    int dataInt=0;
    float dataFloat=0;
    double dataDouble=0;
    String dataString="";

    for(int i=0; i<nScalar; i++) {
      String name = getString();
      DmapType dataType = getType();
      switch(dataType) {
        case CHAR:
          dataByte = bb.get(); break;
        case SHORT:
          dataShort = bb.getShort(); break;
        case INT:
          dataInt = bb.getInt(); break;
        case FLOAT:
          dataFloat = bb.getFloat(); break;
        case DOUBLE:
          dataDouble = bb.getDouble(); break;
        case STRING:
          dataString = getString(); break;
      }

      if ( name.equals("radar.revision.major") &&
              dataType == DmapType.CHAR ) {
        data.radarParms.revision.major = dataByte;
      }
      if ( name.equals("radar.revision.minor") &&
              dataType == DmapType.CHAR ) {
        data.radarParms.revision.minor = dataByte;
      }
      if ( name.equals("origin.code") &&
              dataType == DmapType.CHAR ) {
        data.radarParms.origin.code = (char) dataByte;
      }
      if ( name.equals("origin.time") &&
              dataType == DmapType.STRING ) {
        data.radarParms.origin.time = dataString;
      }
        if ( name.equals("origin.command") &&
                dataType == DmapType.STRING ) {
        data.radarParms.origin.command = dataString;
      }

      if ( name.equals("cp") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.controlProgramId = dataShort;
      }
      if ( name.equals("stid") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.stationId = dataShort;
      }
      if ( name.equals("time.yr") &&
              dataType == DmapType.SHORT ) {
        year = dataShort;
      }
      if ( name.equals("time.mo") &&
              dataType == DmapType.SHORT ) {
        month = dataShort;
      }
      if ( name.equals("time.dy") &&
              dataType == DmapType.SHORT ) {
        day = dataShort;
      }
      if ( name.equals("time.hr") &&
              dataType == DmapType.SHORT ) {
        hour = dataShort;
      }
      if ( name.equals("time.mt") &&
              dataType == DmapType.SHORT ) {
        minute = dataShort;
      }
      if ( name.equals("time.sc") &&
              dataType == DmapType.SHORT ) {
        second = dataShort;
      }
      if ( name.equals("time.us") &&
              dataType == DmapType.INT ) {
        milliSec = dataInt / 1000;
      }

      if ( name.equals("txpow") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.txPower = dataShort;
      }
      if ( name.equals("nave") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.numberAveraged = dataShort;
      }
      if ( name.equals("atten") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.attnLevel = dataShort;
      }
      if ( name.equals("lagfr") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.lagToFirstRange = dataShort;
      }
      if ( name.equals("smsep") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.sampleSeparation = dataShort;
      }
      if ( name.equals("ercod") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.errorCode = dataShort;
      }
      if ( name.equals("ercod") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.errorCode = dataShort;
      }
      if ( name.equals("stat.agc") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.AGCstatusWord = dataShort;
      }
      if ( name.equals("stat.lopwr") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.lowPowerStatusWord = dataShort;
      }
      if ( name.equals("noise.search") &&
              dataType == DmapType.FLOAT ) {
        data.radarParms.noiseLevel = (int) dataFloat;
        data.radarParms.noise.search = data.radarParms.noiseLevel;
      }
      if ( name.equals("noise.mean") &&
              dataType == DmapType.FLOAT ) {
        data.radarParms.noiseMean = (int) dataFloat;
        data.radarParms.noise.mean = data.radarParms.noiseMean;
      }
      if ( name.equals("channel") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.channel = dataShort;
      }
      if ( name.equals("bmnum") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.beamNumber = dataShort;
      }
      if ( name.equals("bmazm") &&
              dataType == DmapType.FLOAT ) {
        data.radarParms.beamAzimuth = dataFloat;
      }
      if ( name.equals("scan") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.scanFlag = dataShort;
      }
      if ( name.equals("offset") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.offset = dataShort;
      }
      if ( name.equals("rxrise") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.rxRiseTime = dataShort;
      }
      if ( name.equals("intt.sc") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.integrationPeriod = dataShort;
        data.radarParms.integration.seconds = dataShort;
      }
      if ( name.equals("intt.us") &&
              dataType == DmapType.INT ) {
        data.radarParms.integration.microSeconds = dataInt;
      }
      if ( name.equals("txpl") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.txPulseLength = dataShort;
      }
      if ( name.equals("mpinc") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.MPlagSeparation = dataShort;
      }
      if ( name.equals("mppul") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.MPnPulses = dataShort;
      }
      if ( name.equals("mplgs") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.MPnLags = dataShort;
      }
      if ( name.equals("nrang") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.numberOfRanges = dataShort;
      }
      if ( name.equals("frang") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.firstRangeDistance = dataShort;
      }
      if ( name.equals("rsep") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.rangeSeparation = dataShort;
      }
      if ( name.equals("xcf") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.crossCorrelation = (dataShort != 0);
      }
      if ( name.equals("tfreq") &&
              dataType == DmapType.SHORT ) {
        data.radarParms.txFrequency = dataShort;
      }
      if ( name.equals("mxpwr") &&
              dataType == DmapType.INT ) {
        data.radarParms.maxPower = dataInt;
      }
      if ( name.equals("lvmax") &&
              dataType == DmapType.INT ) {
        data.radarParms.maxNoise = dataInt;
      }
      if ( name.equals("fitacf.revision.major") &&
              dataType == DmapType.INT ) {
        data.radarParms.fitacfRev.major = dataInt;
      }
      if ( name.equals("fitacf.revision.minor") &&
              dataType == DmapType.INT ) {
        data.radarParms.fitacfRev.minor = dataInt;
      }
      if ( name.equals("combf") &&
                dataType == DmapType.STRING ) {
        data.radarParms.comment = dataString;
      }

      if ( name.equals("noise.sky") &&
              dataType == DmapType.FLOAT ) {
        data.radarParms.noise.sky = (int) dataFloat;
      }
      if ( name.equals("noise.lag0") &&
              dataType == DmapType.FLOAT ) {
        data.radarParms.noise.lag0 = (int) dataFloat;
      }
      if ( name.equals("noise.vel") &&
              dataType == DmapType.FLOAT ) {
        data.radarParms.noise.vel = (int) dataFloat;
      }

    }

//System.out.println(""+year+" "+month+" "+day+" "+hour+" "+minute+" "+second+" "+milliSec);
    this.calendar.set( year, month-1, day, hour, minute, second );
    this.calendar.set( Calendar.MILLISECOND, milliSec );
    data.radarParms.date = this.calendar.getTime();
//System.out.println(this.calendar)    ;
//System.out.println(this.calendar.getTime())    ;
//System.out.println(this.calendar.getTime().getTime())    ;
//System.out.println(data.radarParms.date)    ;
//System.out.println(data.radarParms.date.getTime())    ;

    boolean foundQflag = false;

    for(int i=0; i<nArray; i++) {
      // get the name of the array
      String name = getString();
      // get the data type of the array
      DmapType dataType = getType();

      // get the dimensions of the array
      int nDim = getInt();
      int dims[] = new int[nDim];
      int nElements = 1;
      for(int j=0; j<nDim; j++) {
        dims[j] = getInt();
        nElements *= dims[j];
      }

      boolean arrayRead = false;
//System.out.println(name+" :"+nElements);
      if ( name.equals("slist") ) {
        if ( dataType == DmapType.SHORT ) {
          data.ranges = new byte[nElements];
          for(int j=0; j<nElements; j++) {
            data.ranges[j] = (byte) bb.getShort();
          }
          arrayRead = true;
        }
      }
      else if ( name.equals("gflg") ) {
        if ( dataType == DmapType.CHAR ) {
          data.groundScatter = new boolean[nElements];
          for(int j=0; j<nElements; j++) {
            data.groundScatter[j] = (bb.get() != 0);
          }
          arrayRead = true;
        }
      }
      else if ( name.equals("qflg") ) {
        if ( dataType == DmapType.CHAR ) {
          data.qualityFlag = new boolean[nElements];
          for(int j=0; j<nElements; j++) {
            data.qualityFlag[j] = (bb.get() != 0);
          }
          arrayRead = true;
        }
        foundQflag = true;
      }
      else if ( name.equals("p_l")) {
        if ( dataType == DmapType.FLOAT ) {
          data.lambdaPower = new float[nElements];
          for(int j=0; j<nElements; j++) {
            data.lambdaPower[j] = bb.getFloat();
          }
          arrayRead = true;
        }
      }
      else if ( name.equals("p_l_e")) {
        if ( dataType == DmapType.FLOAT ) {
          data.lambdaPowerError = new float[nElements];
          for(int j=0; j<nElements; j++) {
            data.lambdaPowerError[j] = bb.getFloat();
          }
          arrayRead = true;
        }
      }
      else if ( name.equals("p_s")) {
        if ( dataType == DmapType.FLOAT ) {
          data.sigmaPower = new float[nElements];
          for(int j=0; j<nElements; j++) {
            data.sigmaPower[j] = bb.getFloat();
          }
          arrayRead = true;
        }
      }
      else if ( name.equals("p_s_e")) {
        if ( dataType == DmapType.FLOAT ) {
          data.sigmaPowerError = new float[nElements];
          for(int j=0; j<nElements; j++) {
            data.sigmaPowerError[j] = bb.getFloat();
          }
          arrayRead = true;
        }
      }
      else if ( name.equals("w_l")) {
        if ( dataType == DmapType.FLOAT ) {
          data.lambdaSpectralWidth = new float[nElements];
          for(int j=0; j<nElements; j++) {
            data.lambdaSpectralWidth[j] = bb.getFloat();
          }
          arrayRead = true;
        }
      }
      else if ( name.equals("w_l_e")) {
        if ( dataType == DmapType.FLOAT ) {
          data.lambdaSpectralWidthError = new float[nElements];
          for(int j=0; j<nElements; j++) {
            data.lambdaSpectralWidthError[j] = bb.getFloat();
          }
          arrayRead = true;
        }
      }
      else if ( name.equals("w_s")) {
        if ( dataType == DmapType.FLOAT ) {
          data.sigmaSpectralWidth = new float[nElements];
          for(int j=0; j<nElements; j++) {
            data.sigmaSpectralWidth[j] = bb.getFloat();
          }
          arrayRead = true;
        }
      }
      else if ( name.equals("w_s_e")) {
        if ( dataType == DmapType.FLOAT ) {
          data.sigmaSpectralWidthError = new float[nElements];
          for(int j=0; j<nElements; j++) {
            data.sigmaSpectralWidthError[j] = bb.getFloat();
          }
          arrayRead = true;
        }
      }
      else if ( name.equals("v")) {
        if ( dataType == DmapType.FLOAT ) {
          data.velocity = new float[nElements];
          for(int j=0; j<nElements; j++) {
            data.velocity[j] = bb.getFloat();
          }
          arrayRead = true;
        }
      }
      else if ( name.equals("v_e")) {
        if ( dataType == DmapType.FLOAT ) {
          data.velocityError = new float[nElements];
          for(int j=0; j<nElements; j++) {
            data.velocityError[j] = bb.getFloat();
          }
          arrayRead = true;
        }
      }

      // if the array was not read skip it
      if ( ! arrayRead ) {
        switch(dataType) {
          case CHAR:
            for(int j=0; j<nElements; j++) {
              bb.get();
            }
            break;
          case SHORT:
            for(int j=0; j<nElements; j++) {
              bb.getShort();
            }
            break;
          case INT:
            for(int j=0; j<nElements; j++) {
              bb.getInt();
            }
            break;
          case FLOAT:
            for(int j=0; j<nElements; j++) {
              bb.getFloat();
            }
            break;
          case DOUBLE:
            for(int j=0; j<nElements; j++) {
              bb.getDouble();
            }
            break;
          case STRING:
            for(int j=0; j<nElements; j++) {
              getString();
            }
            break;
        }
      }
    }

    // if the quality flag was not set in the data, make it up from the
    // range data
    if ( ! foundQflag || data.qualityFlag.length == 0 ) {
      data.qualityFlag = new boolean[data.ranges.length];
      for(int j=0; j<data.qualityFlag.length; j++) {
        data.qualityFlag[j] = true;
      }
    }
  }

  public byte get() { return bb.get();}
  public byte get(int index) { return bb.get(index); }
  public char getChar() { return bb.getChar(); }
  public char getChar(int index) { return bb.getChar(index); }
  public short getShort() { return bb.getShort(); }
  public short getShort(int index) { return bb.getShort(index); }
  public int getInt() { return bb.getInt(); }
  public int getInt(int index) { return bb.getInt(index); }
  public long getLong() { return bb.getLong(); }
  public long getLong(int index) { return bb.getLong(index); }
  public float getFloat() { return bb.getFloat(); }
  public float getFloat(int index) { return bb.getFloat(index); }
  public double getDouble() { return bb.getDouble(); }
  public double getDouble(int index) { return bb.getDouble(index); }
}

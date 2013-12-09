package uk.ac.le.sppg.superdarn.fitData;

import java.io.*;
import java.util.Date;
import uk.ac.le.sppg.superdarn.fitData.NetFitData;

/**
 * definition of the fit data sent out by the web_server 
 * from SuperDARN radars.
 * <p>
 * The contents of this data are *not* the same as the full fit data, 
 * it is severely compressed to reduce network traffic.
 * Data is only included in the arrays where it actually fitted.
 * The field <code>ranges</code> contains a list of the range gate numbers where 
 * data actually exists.
 * <p>
 * The data is served little-endian so it always has to be converted
 * to big endian.
 * 
 * @author Nigel Wade
 */
public class FitacfData implements Constants, Cloneable, Serializable,
        Comparable<Date> {

//    private static final long serialVersionUID = 0x5253505047000019L;
  private static final long serialVersionUID = 0x1L;
  /**
   * the radar parameter block
   */
  public RadarParms2 radarParms = new RadarParms2();

  /**
   * the array of range gate numbers (numbered from 1) for
   * which data exists. There is one value in the other arrayd
   * fields for each gate listed here.
   */
  public byte[] ranges = new byte[0];
  /**
   * flag to indicate data is groundScatter
   */
  public boolean[] groundScatter = new boolean[0];
  /**
   * lambda power values
   */
  public float[] lambdaPower = new float[0];
  public float[] lambdaPowerError = new float[0];
  /**
   * sigma power values
   */
  public float[] sigmaPower = new float[0];
  public float[] sigmaPowerError = new float[0];
  /**
   * velocity values
   */
  public float[] velocity = new float[0];
  public float[] velocityError = new float[0];
  /**
   * spectral width values
   */
  public float[] lambdaSpectralWidth = new float[0];
  public float[] lambdaSpectralWidthError = new float[0];
  public float[] sigmaSpectralWidth = new float[0];
  public float[] sigmaSpectralWidthError = new float[0];

  /**
   * quality of fit flag
   */
  public boolean[] qualityFlag = new boolean[0];

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  public FitacfData clone()
          throws CloneNotSupportedException {
    FitacfData result = (FitacfData) super.clone();
    result.radarParms = (RadarParms2) this.radarParms.clone();

      result.ranges = new byte[this.ranges.length];
      result.groundScatter = new boolean[this.ranges.length];
      result.lambdaPower = new float[this.ranges.length];
      result.lambdaPowerError = new float[this.ranges.length];
      result.sigmaPower = new float[this.ranges.length];
      result.sigmaPowerError = new float[this.ranges.length];
      result.velocity = new float[this.ranges.length];
      result.velocityError = new float[this.ranges.length];
      result.lambdaSpectralWidth = new float[this.ranges.length];
      result.lambdaSpectralWidthError = new float[this.ranges.length];
      result.sigmaSpectralWidth = new float[this.ranges.length];
      result.sigmaSpectralWidthError = new float[this.ranges.length];
      result.qualityFlag = new boolean[this.ranges.length];

    for (int i = 0; i < this.ranges.length; i++) {
        result.groundScatter[i] = this.groundScatter[i];
        result.ranges[i] = this.ranges[i];
        result.velocity[i] = this.velocity[i];
        result.velocityError[i] = this.velocityError[i];
        result.lambdaPower[i] = this.lambdaPower[i];
        result.lambdaPowerError[i] = this.lambdaPowerError[i];
        result.sigmaPower[i] = this.sigmaPower[i];
        result.sigmaPowerError[i] = this.sigmaPowerError[i];
        result.lambdaSpectralWidth[i] = this.lambdaSpectralWidth[i];
        result.lambdaSpectralWidthError[i] = this.lambdaSpectralWidthError[i];
        result.sigmaSpectralWidth[i] = this.sigmaSpectralWidth[i];
        result.sigmaSpectralWidthError[i] = this.sigmaSpectralWidthError[i];
        result.qualityFlag[i] = this.qualityFlag[i];
    }

    return result;
  }

  /**
   * @return
   */
  public boolean[] getGroundScatter() {
    return groundScatter;
  }

  /**
   * @return
   */
  public float[] getLambdaPower() {
    return lambdaPower;
  }

  /**
   * @return
   */
  public float[] getLambdaSpectralWidth() {
    return lambdaSpectralWidth;
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
  public byte[] getRanges() {
    return ranges;
  }

  /**
   * @return
   */
  public float[] getVelocity() {
    return velocity;
  }

  public int compareTo(Date date) {
//        Date date = (Date)o;
    return this.radarParms.date.compareTo(date);
  }

  public NetFitData toNetFit() {
    NetFitData result = new NetFitData();

    result.radarParms = this.radarParms;
    result.groundScatter = this.groundScatter;
    result.lambdaPower = this.lambdaPower;
    result.lambdaSpectralWidth = this.lambdaSpectralWidth;
    result.ranges = this.ranges;
    result.velocity = this.velocity;

    return result;
  }
}

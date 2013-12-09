package uk.ac.le.sppg.superdarn.fitData;

import java.io.*;
import java.util.Date;

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
public class NetFitData implements Constants, Cloneable, Serializable,
        Comparable<Date> {

//    private static final long serialVersionUID = 0x5253505047000019L;
  private static final long serialVersionUID = 0xA8432A9DB6A23C4BL;
  /**
   * the radar parameter block
   */
  public RadarParms radarParms = new RadarParms();
  /**
   * the array of range gate numbers (numbered from 1) for
   * which data exists. There is one value in the other arrayd
   * fields for each gate listed here.
   */
  public byte[] ranges = null;
  /**
   * flag to indicate data is groundScatter
   */
  public boolean[] groundScatter = null;
  /**
   * lambda power values
   */
  public float[] lambdaPower = null;
  /**
   * velocity values
   */
  public float[] velocity = null;
  /**
   * spectral width values
   */
  public float[] lambdaSpectralWidth = null;

  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  public NetFitData clone()
          throws CloneNotSupportedException {
    NetFitData result = (NetFitData) super.clone();
    result.radarParms = (RadarParms) this.radarParms.clone();

    ranges = new byte[this.ranges.length];
    groundScatter = new boolean[this.ranges.length];
    lambdaPower = new float[this.ranges.length];
    velocity = new float[this.ranges.length];
    lambdaSpectralWidth = new float[this.ranges.length];

    for (int i = 0; i < this.ranges.length; i++) {
      result.groundScatter[i] = this.groundScatter[i];
      result.ranges[i] = this.ranges[i];
      result.velocity[i] = this.velocity[i];
      result.lambdaPower[i] = this.lambdaPower[i];
      result.lambdaSpectralWidth[i] = this.lambdaSpectralWidth[i];
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
}

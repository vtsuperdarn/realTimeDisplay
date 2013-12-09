package uk.ac.le.sppg.superdarn.fitData;


import java.io.*;

/**
 * <code>NoiseData</code> is a part of a {@link fitData.FitData FitData} object.
 * @author Nigel Wade
 */
public class NoiseData implements Serializable, Cloneable {
    private static final long serialVersionUID = 0x2L;

    /**
     * noise from fitted noise acf and ccf.
     */
    public double vel;       // noise from fitted noise acf and ccf.
    /**
     * background skynoise from lowest lag0 powers.
     */
    public double sky;  // background skynoise from lowest lag0 powers
    /**
     * lag0 power from noise acf.
     */
    public double lag0; // lag0 power from noise acf.

    /**
     * noise level from clear frequency search
     */
    public double search;

    /**
     * average noise across the frequency band
     */
    public double mean;
	/**
	 * @return
	 */
	public double geLag0() {
		return lag0;
	}

	/**
	 * @return
	 */
	public double getSky() {
		return sky;
	}

	/**
	 * @return
	 */
	public double getVel() {
		return vel;
	}

  public NoiseData clone() {
    NoiseData result = new NoiseData();
    result.mean = mean;
    result.lag0 = lag0;
    result.search = search;
    result.sky = sky;
    result.vel = vel;
    return result;
  }

}

/*
 * Created on 14-Jun-2004
 */
package uk.ac.le.sppg.superdarn.fitData;

/**
 * @author Nigel Wade
 */
public class FitData130 extends FitData  {
    public FitData130() {
		pulse = new short[PULSE_PAT_LEN_V130];
		lag = new short[2][LAG_TAB_LEN_V130];
    }
}

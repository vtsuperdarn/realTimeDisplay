package uk.ac.le.sppg.superdarn.fitData;


import java.io.*;

/**
 * A class to hold the fit file format revision information.
 * 
 * @author Nigel Wade
 */
public class FitRev implements Serializable {

//    private static final long serialVersionUID = 0x5253505047000048L;
    private static final long serialVersionUID = 0x2L;

    /**
     * the minor revision number.
     */
    public int minor;
    /**
     * the major revision number.
     */
    public int major;

    public String toString() {
        String result = String.valueOf(major) + "." +String.valueOf(minor);
        return result;
    }
}

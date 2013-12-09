/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.realTimeControl;

/**
 *
 * @author nigel
 */
public enum PlotType {
    TIME, FAN;
    
    public static PlotType value(String str) 
    throws IllegalArgumentException {
        return valueOf(str.toUpperCase());
    }
}

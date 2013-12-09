/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.dataDisplay;

/**
 *
 * @author nigel
 */
public enum PlotParameter {
    velocity, width, power;
    
    public static PlotParameter value(String str) 
    throws IllegalArgumentException {
        return valueOf(str.toLowerCase());
    }
}

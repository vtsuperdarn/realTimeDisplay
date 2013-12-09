/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.realTimeControl;

/**
 *
 * @author nigel
 */
public enum DisplayParameter {
    START, DURATION, BEAM, CHANNEL, TYPE, SITE, PAR, PARAMETER, LIMIT, OLDDATA, ZOOM, SCALE, X, Y, W, H, 
    GS, GROUND_SCATTER, PANEL_X, PANEL_Y, PANEL_ICONIFIED, PANX, PANY, PANI, SERVER, INVALID;
    
    public static DisplayParameter value(String str) 
    throws IllegalArgumentException {
        return valueOf(str.toUpperCase());
    }
}

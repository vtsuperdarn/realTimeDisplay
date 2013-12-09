/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.dataDisplay;

import uk.ac.le.sppg.superdarn.colour.Legend;

/**
 *
 * @author nigel
 */
public interface PlotWindow {
    public Legend getLegend();
    public Legend getLegend(PlotParameter what) ;
    public void groundscatterOff();
    public void groundscatterOn();
    public void setWhat( PlotSubParameter what ) throws NoSuchFieldException ;
}

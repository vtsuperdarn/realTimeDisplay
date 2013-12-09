/*
 * PlotSubParameter.java
 *
 * Created on 06 December 2007, 11:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.dataDisplay;

/**
 *
 * @author nigel
 */
public class PlotSubParameter {
    
    public final PlotParameter type;
    public final boolean gs;
    
    public PlotSubParameter(PlotParameter type, boolean gs) {
        this.type = type;
        this.gs = gs;
    }
    
    public boolean equals(Object o) {
        return this == o;
    }
    
    public boolean equals(PlotParameter o) {
        return this.type == o;
    }
    
    public boolean equals(PlotSubParameter o) {
        return this.type == o.type && this.gs == o.gs;
    }
    
}

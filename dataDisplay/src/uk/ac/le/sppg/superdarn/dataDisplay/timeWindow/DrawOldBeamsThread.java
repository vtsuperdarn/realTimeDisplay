/*
 * Created on 10-Jun-2004
 */
package uk.ac.le.sppg.superdarn.dataDisplay.timeWindow;

import uk.ac.le.sppg.superdarn.dataDisplay.PlotSubParameter;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.SwingUtilities;

/**
 * @author Nigel Wade
 */
class DrawOldBeamsThread extends Thread {

    volatile boolean runnable = true;
    PlotSubParameter what = null;
    final Object oldBeamThreadSync;
    final ArrayList<Beam> beams;
    final TimePlot parent;
    final Date frameStartDate;
    
    public DrawOldBeamsThread(Object sync, ArrayList<Beam> beams, TimePlot parent, Date frameStartDate) {
        oldBeamThreadSync = sync;
        this.beams = beams;
        this.parent = parent;
        this.frameStartDate = frameStartDate;
    }
    public DrawOldBeamsThread(PlotSubParameter what, Object sync, ArrayList<Beam> beams, TimePlot parent, Date frameStartDate) {
        this.what = what;
        oldBeamThreadSync = sync;
        this.beams = beams;
        this.parent = parent;
        this.frameStartDate = frameStartDate;
    }

    public void abort() {
        runnable = false;
    }

    Runnable paintParent = new Runnable() {
        public void run() { parent.repaint(); }
    };

    public void run() {

        int beamIndex = 0;

        synchronized (oldBeamThreadSync) {
            
            int count = 0;
            Beam beam = null;
            
            while (runnable && beamIndex < beams.size() ) {

                synchronized (beams) {
                    beam = beams.get(beamIndex);
                    beamIndex++;
                }

                if (beam.date.before(frameStartDate)) {
                    continue;
                }
                // fill the gate with the colour for the data 
                if (what != null) {
                    parent.drawOldBeam(beam, what);
                } else {
                    parent.drawOldBeam(beam, null);
                }

                count++;
                if ( count > 50 ) {
                    SwingUtilities.invokeLater(paintParent);
                    count = 0;
                }

                // invokeLater() {
                //repaint();
            }

            SwingUtilities.invokeLater(paintParent);
            
            // if the data doesn't extend back to the beginning of the plot
            // start an oldDataThread in the grand-parent (TimeWindow).
            beam = beams.get(0);
            if (runnable && beam != null && beam.date.after(frameStartDate) && what == null) {
                parent.parent.drawOldData(parent.parent.oldReader, frameStartDate.getTime(), beam.date.getTime());
            }
        }
    }


}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.dataDisplay.timeWindow;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotParameter;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotSubParameter;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;

/**
 *
 * @author nigel
 */
public class TimeWindowConfig {
    protected Date startDate = null;
    protected Date endDate = null;
    protected ArrayList<PlotSubParameter> what = null;
    protected ChannelId channel = null;
    protected boolean showNewData = false;
    protected int beamNumber = 0;
    protected Dimension size = null;
    protected Point location = null;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public ArrayList<PlotSubParameter> getWhat() {
        return what;
    }

    public void setWhat(ArrayList<PlotSubParameter> what) {
        this.what = what;
    }

    public ChannelId getChannel() {
        return channel;
    }

    public void setChannel(ChannelId channel) {
        this.channel = channel;
    }

    public boolean isShowNewData() {
        return showNewData;
    }

    public void setShowNewData(boolean showNewData) {
        this.showNewData = showNewData;
    }

    public int getBeamNumber() {
        return beamNumber;
    }

    public void setBeamNumber(int beamNumber) {
        this.beamNumber = beamNumber;
    }

    public Dimension getSize() {
        return size;
    }

    public void setSize(Dimension size) {
        this.size = size;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }


}
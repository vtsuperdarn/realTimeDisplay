/*
 * ControlPanelBean.java
 *
 * Created on 22 November 2007, 10:14
 */

package uk.ac.le.sppg.superdarn.dataDisplay.controlPanel;

import uk.ac.le.sppg.coords.Geographic;
import uk.ac.le.sppg.coords.proj.Projection;
import uk.ac.le.sppg.coords.proj.Stereographic;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSite;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import uk.ac.le.sppg.general.worldMap.World;
import uk.ac.le.sppg.superdarn.fitDataViewers.RmiServerBrowser;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotParameter;
import uk.ac.le.sppg.superdarn.dataDisplay.fanWindow.FanWindow;
import uk.ac.le.sppg.superdarn.dataDisplay.timeWindow.TimeWindow;
import uk.ac.le.sppg.superdarn.dataServer.FitRemote2;
import uk.ac.le.sppg.superdarn.dataServer.Radars;
import uk.ac.le.sppg.superdarn.dataServer.RemoteData;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;
import uk.ac.le.sppg.superdarn.fitData.NewData;

/**
 *
 * @author  nigel
 */
public class ControlPanelBean
        extends javax.swing.JPanel
        implements WindowListener {


    private class SpinnerNumberModelWrap extends SpinnerNumberModel {

        private static final long serialVersionUID = 0x5253505047000037L;

        SpinnerNumberModelWrap() {
            super();
        }

        SpinnerNumberModelWrap(double a, double b, double c, double d) {
            super(a, b, c, d);
        }

        SpinnerNumberModelWrap(int a, int b, int c, int d) {
            super(a, b, c, d);
        }

        SpinnerNumberModelWrap(Number a, Comparable b, Comparable c, Number d) {
            super(a, b, c, d);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object getNextValue() {

            Object result;

            // if the current value is >= the maximum set it to the minimum
            if (getMaximum().compareTo(getValue()) <= 0) {
                result = super.getMinimum();
                setValue(result);
            } else {
                result = super.getNextValue();
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object getPreviousValue() {

            Object result;

            // if the current value is <= the minimum set it to the maximum
            if (getMinimum().compareTo(getValue()) >= 0) {
                result = super.getMaximum();
                setValue(result);
            } else {
                result = super.getPreviousValue();
            }
            return result;
        }

    }

    private class OldDataReader implements TimeWindow.OldData {

        FitRemote2 remote;

        String siteName;

        public OldDataReader(URL url, String siteName)
        throws MalformedURLException, IOException {
//            URL url = new URL("http://" + server
//                    + ":"+port+"/dataServlet/dataServer");
            remote = new RemoteData(url);
            this.siteName = siteName;
        }

        public FitacfData getNextData(ChannelId channel, long oldest,
                int beamNumber) throws IOException {
            FitacfData fit = remote.next(siteName, channel, oldest, beamNumber,	10);
            return fit;
        }

        public String site() {
            return siteName;
        }
    }


    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

//    String server = "";
//    String port = "8080";

    URL baseURL;

    TreeMap<String, DataSource> dataSources = new TreeMap<String, DataSource>();

    FitacfData firstFit;
    Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT0"));

    private Dimension timePlotDimension = null;
    private Point timePlotLocation = null;
    private Dimension fanPlotDimension = null;
    private Point fanPlotLocation = null;

    private World world = null;

    /** Creates new form ControlPanelBean */
    public ControlPanelBean() {
        initComponents();
    }

    public void setServer(URL url) {
        baseURL = url;
        try {
            URL radarURL = new URL(baseURL, "radarList");
           ArrayList radarList = Radars.getRadarList(radarURL);

            // add the list to the radars shown in the combo-box.
            radarCombo.removeAllItems();
            radarCombo.addItem("Choose radar");
            for (int i = 0; i < radarList.size(); i++) {
                radarCombo.addItem(radarList.get(i));
            }
            radarCombo.setSelectedIndex(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), e.getMessage(),
                    "IO Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean setFanCentre(Geographic geo) {
        if ( geo != null ) {
            latSpinner.setValue(new Double(geo.latitude));
            lonSpinner.setValue(new Double(geo.longitude));
            return true;
        }
        return false;
    }

    public boolean setSiteChannel(String site, ChannelId channel) {
        if (site != null) {
            for (int i = 0; i < radarCombo.getItemCount(); i++) {
                if (radarCombo.getItemAt(i).equals(site)) {
                    radarCombo.setSelectedIndex(i);
                    if (channel != null && channelCombo.getItemCount() > 1) {
                        for (int j = 0; j < channelCombo.getItemCount(); j++) {
                            if (channelCombo.getItemAt(j).equals(channel)) {
                                channelCombo.setSelectedIndex(j);
                                break;
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void setTimePlotDefaults(boolean loadOldData,
            boolean plotVel, boolean plotPow, boolean plotWidth,
            Integer beamNumber, int zoom) {
        this.loadOldData.setSelected(loadOldData);
        this.timePlotPow.setSelected(plotPow);
        this.timePlotVel.setSelected(plotVel);
        this.timePlotWidth.setSelected(plotWidth);
        if ( beamNumber != null )
            this.beamSpinner.setValue(beamNumber);
        zoomCombo.setSelectedIndex(zoom);
    }

    public void setTimePlotStart(int hour, int min, int sec, float duration ) {
        hourSpinnerStart.setValue(new Integer(hour));
        minuteSpinnerStart.setValue(new Integer(min));
        secondSpinnerStart.setValue(new Integer(sec));
        hourSpinnerLength.setValue(new Integer((int)duration));
        int mins = (int) ((duration-(int)duration)*60);
        minuteSpinnerLength.setValue(new Integer(mins));
    }

    public void setTimePlotDimensions(Point loc, Dimension dim) {
        timePlotLocation = loc;
        timePlotDimension = dim;
    }

    public void setFanPlotDimensions(Point loc, Dimension dim) {
        fanPlotLocation = loc;
        fanPlotDimension = dim;
    }

    public void setFanPlotDefaults(boolean plotVel, boolean plotPow, boolean plotWidth) {
        this.fanPlotPow.setSelected(plotPow);
        this.fanPlotVel.setSelected(plotVel);
        this.fanPlotWidth.setSelected(plotWidth);
    }

    public TimeWindow doTimePlot(String siteName, Date startDate, float duration,
            ChannelId channel, int beam, PlotParameter what, double limit, boolean gs,
            boolean oldData, int zoom,
            Dimension size, Point location)
        throws NoSuchFieldException, MalformedURLException, IOException {

        Date endDate = new Date(startDate.getTime() + (long)(duration*60.0*60.0*1000));

        DataSource source = dataSources.get(siteName);

        if ( source == null ) {
            throw new IOException("No data source for radar: "+siteName);
        }

        ArrayList<PlotParameter> plotWhat = new ArrayList<PlotParameter>();
        plotWhat.add(what);

        TimeWindow timeWin;

        Date now = new Date();

System.out.println("ControlPanelBean: endDate: "+endDate+" now: "+now);
        if ( size == null ) {
            timeWin = new TimeWindow(startDate, endDate, plotWhat,
                channel, gs, true, (endDate.getTime() >= now.getTime()), beam);
        }
        else {
            timeWin = new TimeWindow(startDate, endDate, plotWhat,
                channel, gs, true,
                (endDate.getTime() >= now.getTime()),
                beam, size);
        }
        // set the width of the drawn values to just over 2mins.
        timeWin.setDrawWidth(125);

        timeWin.addWindowListener(this);
        source.addListener(timeWin, channel);

        timeWin.setVisible(true);

        if ( location != null )
            timeWin.setLocation(location);

        if ( zoom != 0 )
            timeWin.setZoom(zoom);

        if ( limit > 0.0 ) {
           timeWin.getLegend().setLimits(-limit, +limit);
        }

        if ( oldData ) {
            OldDataReader oldDataReader = new OldDataReader(new URL(baseURL, "dataServer"), siteName);
            timeWin.drawOldData(oldDataReader, startDate.getTime(), endDate.getTime());
        }

        return timeWin;
    }

    public void doTimePlot() {
        timePlotButton.doClick();
    }

    public FanWindow doFanPlot(String siteName,
            ChannelId channel, PlotParameter what, double limit, boolean gs,
            double scale, Dimension size, Point location, Projection proj)
        throws NoSuchFieldException, MalformedURLException, IOException, InterruptedException {

        if ( world == null ) {
            world = new World();
        }
        DataSource source = dataSources.get(siteName);

        if ( source == null ) {
            throw new IOException("No data source for radar: "+siteName);
        }

        FanWindow fanWin;

        world.waitUntilLoaded();
        if ( size == null ) {
            fanWin = new FanWindow( proj, world, what, gs, new Dimension(300,300), true );
        }
        else {
            fanWin = new FanWindow( proj, world, what, gs, size, true );
        }

        fanWin.addWindowListener(this);
        source.addListener(fanWin, channel);

        fanWin.setVisible(true);

        if ( location != null )
            fanWin.setLocation(location);

        if ( scale > 0.0 )
            fanWin.setScale(scale);

        if ( limit > 0.0 ) {
           fanWin.getLegend().setLimits(-limit, +limit);
        }

        return fanWin;
    }

    public void doFanPlot() {
        fanPlotButton.doClick();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel5 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        connectPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        radarCombo = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        channelCombo = new javax.swing.JComboBox();
        timePanel = new javax.swing.JPanel();
        timeSubPanel1 = new javax.swing.JPanel();
        timePlotVel = new javax.swing.JCheckBox();
        timePlotPow = new javax.swing.JCheckBox();
        timePlotWidth = new javax.swing.JCheckBox();
        timeSubPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        hourSpinnerStart = new javax.swing.JSpinner();
        minuteSpinnerStart = new javax.swing.JSpinner();
        secondSpinnerStart = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        hourSpinnerLength = new javax.swing.JSpinner();
        minuteSpinnerLength = new javax.swing.JSpinner();
        jPanel9 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        beamSpinner = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        zoomCombo = new javax.swing.JComboBox();
        loadOldData = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        timePlotButton = new javax.swing.JButton();
        fanPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        fanPlotVel = new javax.swing.JCheckBox();
        fanPlotPow = new javax.swing.JCheckBox();
        fanPlotWidth = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        latSpinner = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        lonSpinner = new javax.swing.JSpinner();
        jPanel10 = new javax.swing.JPanel();
        fanPlotButton = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jButton2.setBackground(new java.awt.Color(234, 85, 92));
        jButton2.setFont(new java.awt.Font("DejaVu Sans", 1, 15)); // NOI18N
        jButton2.setText("Plot all radars on one map");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fanPlotAllAction(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanel5.add(jButton2, gridBagConstraints);

        jButton1.setFont(new java.awt.Font("DejaVu Sans", 1, 15)); // NOI18N
        jButton1.setText("Open Parameter/Data Browser");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDataBrowser(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanel5.add(jButton1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(jPanel5, gridBagConstraints);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Individual Radar Controls", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        jPanel7.setLayout(new java.awt.GridBagLayout());

        connectPanel.setBorder(null);
        connectPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 10));

        jLabel1.setText("Radar: ");
        connectPanel.add(jLabel1);

        radarCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Disconnected" }));
        radarCombo.setToolTipText("List of radars served");
        radarCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radarComboAction(evt);
            }
        });
        connectPanel.add(radarCombo);

        jLabel2.setText("Channel: ");
        connectPanel.add(jLabel2);

        connectPanel.add(channelCombo);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 10, 0);
        jPanel7.add(connectPanel, gridBagConstraints);

        timePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Time Series Plot (RTI)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        timePanel.setLayout(new java.awt.GridLayout(4, 0));

        timeSubPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        timePlotVel.setText("Vel");
        timePlotVel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        timeSubPanel1.add(timePlotVel);

        timePlotPow.setText("Pow");
        timePlotPow.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        timeSubPanel1.add(timePlotPow);

        timePlotWidth.setText("Width");
        timePlotWidth.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        timeSubPanel1.add(timePlotWidth);

        timePanel.add(timeSubPanel1);

        timeSubPanel2.setAlignmentX(0.0F);
        timeSubPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel3.setText("Start (UT):");
        timeSubPanel2.add(jLabel3);

        hourSpinnerStart.setModel(new SpinnerNumberModelWrap(0, 0, 23, 1));
        timeSubPanel2.add(hourSpinnerStart);

        minuteSpinnerStart.setModel(new SpinnerNumberModelWrap(0, 0, 59, 1));
        timeSubPanel2.add(minuteSpinnerStart);

        secondSpinnerStart.setModel(new SpinnerNumberModelWrap(0, 0, 59, 1));
        timeSubPanel2.add(secondSpinnerStart);

        jLabel4.setText("Duration:");
        timeSubPanel2.add(jLabel4);

        hourSpinnerLength.setModel(new SpinnerNumberModel(0, 0, 99, 1));
        timeSubPanel2.add(hourSpinnerLength);

        minuteSpinnerLength.setModel(new SpinnerNumberModelWrap(0, 0, 59, 1));
        timeSubPanel2.add(minuteSpinnerLength);

        timePanel.add(timeSubPanel2);

        jPanel9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel5.setText("Beam:");
        jPanel9.add(jLabel5);

        beamSpinner.setModel(new SpinnerNumberModel(1, 0, 15, 1));
        jPanel9.add(beamSpinner);

        jLabel9.setText("Zoom: ");
        jPanel9.add(jLabel9);

        zoomCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1x", "2x", "4x" }));
        jPanel9.add(zoomCombo);

        loadOldData.setText("Load old data");
        loadOldData.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jPanel9.add(loadOldData);

        timePanel.add(jPanel9);

        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        timePlotButton.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
        timePlotButton.setText("Time plot");
        timePlotButton.setToolTipText("Starts a time plot with requested start time and beam number");
        timePlotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timePlotAction(evt);
            }
        });
        jPanel8.add(timePlotButton);

        timePanel.add(jPanel8);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 3, 10);
        jPanel7.add(timePanel, gridBagConstraints);

        fanPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Fan Plot", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        fanPanel.setLayout(new java.awt.GridLayout(3, 1));

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        fanPlotVel.setText("Vel");
        fanPlotVel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jPanel3.add(fanPlotVel);

        fanPlotPow.setText("Pow");
        fanPlotPow.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jPanel3.add(fanPlotPow);

        fanPlotWidth.setText("Width");
        fanPlotWidth.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jPanel3.add(fanPlotWidth);

        fanPanel.add(jPanel3);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel6.setText("Centre location: ");
        jPanel4.add(jLabel6);

        jLabel7.setText("    Lat:");
        jPanel4.add(jLabel7);

        latSpinner.setModel(new SpinnerNumberModel(0.0, -90.0, 90.0, 0.5));
        jPanel4.add(latSpinner);

        jLabel8.setText("Lon:");
        jPanel4.add(jLabel8);

        lonSpinner.setModel(new SpinnerNumberModelWrap(0.0, -180.0, 180.0, 0.5));
        jPanel4.add(lonSpinner);

        fanPanel.add(jPanel4);

        jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        fanPlotButton.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
        fanPlotButton.setText("Fan plot");
        fanPlotButton.setToolTipText("Starts a fan plot centred on the specified lat and lon.");
        fanPlotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fanPlotAction(evt);
            }
        });
        jPanel10.add(fanPlotButton);

        fanPanel.add(jPanel10);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 10, 10);
        jPanel7.add(fanPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(jPanel7, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridLayout(2, 1));

        jLabel10.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jLabel10.setText("(c) 2005-2011, Nigel Wade, RSPPG, Leicester, UK");
        jLabel10.setMaximumSize(new java.awt.Dimension(312, 20));
        jLabel10.setMinimumSize(new java.awt.Dimension(312, 20));
        jPanel6.add(jLabel10);

        jLabel11.setFont(new java.awt.Font("DejaVu Sans", 0, 10));
        jLabel11.setText("(c) 2010-2011, Lasse Clausen, Virginia Tech, USA");
        jPanel6.add(jLabel11);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        add(jPanel6, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void fanPlotAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fanPlotAction

        
        // get the DataSource currently selected
        String radar = (String) radarCombo.getSelectedItem();
        ChannelId channel = (ChannelId) channelCombo.getSelectedItem();

        DataSource source;

        if (dataSources.containsKey(radar)) {
            source = dataSources.get(radar);
        } else {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "No data reader for "
                    + radar, "Internal Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean plotStarted = false;

        if (fanPlotVel.isSelected()) {
            fanPlot(source, channel, PlotParameter.velocity);
            plotStarted = true;
        }
        if (fanPlotPow.isSelected()) {
            fanPlot(source, channel, PlotParameter.power);
            plotStarted = true;
        }
        if (fanPlotWidth.isSelected()) {
            fanPlot(source, channel, PlotParameter.width);
            plotStarted = true;
        }

        if ( ! plotStarted ) {
            JOptionPane.showMessageDialog(this, "No data type selected.", "No data type selected", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_fanPlotAction

    private void timePlotAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timePlotAction
        // get the DataSource currently selected


        String radar = (String) radarCombo.getSelectedItem();
        ChannelId channel = (ChannelId) channelCombo.getSelectedItem();

        DataSource source;
        if (dataSources.containsKey(radar)) {
            source = dataSources.get(radar);
        } else {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "No data reader for "
                    + radar, "Internal Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean plotStarted = false;

        if (timePlotVel.isSelected()) {
            timePlot(source, radar, channel, PlotParameter.velocity);
            plotStarted = true;
        }
        if (timePlotPow.isSelected()) {
            timePlot(source, radar, channel, PlotParameter.power);
            plotStarted = true;
        }
        if (timePlotWidth.isSelected()) {
            timePlot(source, radar, channel, PlotParameter.width);
            plotStarted = true;
        }

        if ( ! plotStarted ) {
            JOptionPane.showMessageDialog(this, "No data type selected.", "No data type selected", JOptionPane.WARNING_MESSAGE);
        }

    }//GEN-LAST:event_timePlotAction

    private void radarComboAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radarComboAction
        // Add your handling code here:
        String radar = (String) radarCombo.getSelectedItem();

        // when radarCombo.removeAllItems method is invoked it in turn invokes
        // the actionCallback
        // resulting in this method being invoked. At this time radar will be
        // null.
        if (radar == null)
            return;

        if (radarCombo.getSelectedIndex() == 0) {

            setComboNoServer();

            return;
        }

        try {
            DataSource source = setRadar(radar);

            if (source == null) {
                setComboNoServer();
            } else {

                // get the list of channels
                ChannelId[] channels = source.remote.channels(radar);
                // get the latest data from the first channel
                firstFit = source.remote.latest(radar, channels[0]);

                channelCombo.removeAllItems();
                for (int i = 0; i < channels.length; i++) {
                    channelCombo.addItem(channels[i]);
                }
                channelCombo.setSelectedIndex(0);

                // set the fan plot centre location.
                SuperDarnSite site = SuperDarnSiteList.getList().get(radar);

                Geographic centre;
                if ( firstFit != null ) {
                    centre = site.siteCentre(
                        firstFit.radarParms.firstRangeDistance,
                        firstFit.radarParms.rangeSeparation,
                        firstFit.radarParms.numberOfRanges);
                }
                else {
                  centre = site.siteCentre(1, 45, 70);
                }
                // set lat and lon spinners
                double d = Math.floor(centre.latitude * 10) / 10.0;
                latSpinner.setValue(new Double(d));
                d = Math.floor(centre.longitude * 10) / 10.0;
                lonSpinner.setValue(new Double(d));

                // set the start time to 23 hours ago
                cal.setTime(new Date());
                cal.add(Calendar.HOUR_OF_DAY, -23);
                this.hourSpinnerStart.setValue(new Integer(cal.get(Calendar.HOUR_OF_DAY)));

                this.hourSpinnerLength.setValue(new Integer(24));

                this.channelCombo.setEnabled(true);
                this.timePlotButton.setEnabled(true);
                this.hourSpinnerLength.setEnabled(true);
                this.minuteSpinnerLength.setEnabled(true);
                this.hourSpinnerStart.setEnabled(true);
                this.minuteSpinnerStart.setEnabled(true);
                this.secondSpinnerStart.setEnabled(true);
                this.beamSpinner.setEnabled(true);
                this.loadOldData.setEnabled(true);
                this.timePlotPow.setEnabled(true);
                this.timePlotVel.setEnabled(true);
                this.timePlotWidth.setEnabled(true);
                this.zoomCombo.setEnabled(true);
                this.fanPlotPow.setEnabled(true);
                this.fanPlotVel.setEnabled(true);
                this.fanPlotWidth.setEnabled(true);

                this.fanPlotButton.setEnabled(true);
                this.latSpinner.setEnabled(true);
                this.lonSpinner.setEnabled(true);

            }
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), e.getMessage(),
                    "Exception", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), e.getMessage(),
                    "Exception", JOptionPane.ERROR_MESSAGE);
        }

    }

    public DataSource setRadar(String radar)
    throws MalformedURLException, IOException {
        // if there is no DataSource for this server+radar create
        // a new DataSource
            DataSource source;

            if (!dataSources.containsKey(radar)) {
                source = new DataSource(new URL(baseURL, "dataServer"), radar);
                dataSources.put(radar, source);
            } else {
                source = dataSources.get(radar);
            }

        return source;
}//GEN-LAST:event_radarComboAction

    private void fanPlotAllAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fanPlotAllAction
        PlotParameter what = PlotParameter.velocity;
        boolean plotParameterSelected = false;
        
        if (fanPlotVel.isSelected()) {
            what = PlotParameter.velocity;
            plotParameterSelected = true;
        }
        if (fanPlotPow.isSelected()) {
            what = PlotParameter.power;
            plotParameterSelected = true;
        }
        if (fanPlotWidth.isSelected()) {
            what = PlotParameter.width;
            plotParameterSelected = true;
        }
        
        if ( ! plotParameterSelected ) {
            JOptionPane.showMessageDialog(this, "No data type selected.", "No data type selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if ( world == null ) {
            world = new World();
        }
        
        //double lat = ((Double) (latSpinner.getValue())).doubleValue();
        //double lon = ((Double) (lonSpinner.getValue())).doubleValue();
        double lat =  60.0;
        double lon = -95.0;
        Geographic centre = new Geographic(lat, lon, 0.0);
        
        Projection proj = new Stereographic(1.8, centre);
        
        FanWindow fanWindow;
        
        Dimension windowSize;
        
        if ( fanPlotDimension != null )
            windowSize = fanPlotDimension;
        else
            windowSize = new Dimension(screenSize.height/2, screenSize.height/2);
        
        
        try {
            world.waitUntilLoaded();
            fanWindow = new FanWindow(proj, world, what, true, windowSize, false);
            fanWindow.setFanViewCentre(centre);
            
            fanWindow.setVisible(true);
            fanWindow.addWindowListener(this);
            
            //if ( fanPlotLocation != null )
            //    fanWindow.setLocation(fanPlotLocation);
            
        } catch (NoSuchFieldException e) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(),
                    "Invalid data parameter for fan window", "Internal Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } catch( InterruptedException e) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(),
                    "World map loading was interrupted.", "Internal Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        DataSource source = null;
        
        int radarCount = radarCombo.getItemCount();
        
        try {
            // get all datasources
            for (int r=1; r < radarCount; r++) {
                String radar = (String) radarCombo.getItemAt(r);
                source = setRadar(radar);
                if (source == null) {
                    JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "No data reader for "
                            + radar, "Internal Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                // get all channels
                ChannelId[] channels = source.remote.channels(radar);
                //for (int c = 0; c < channels.length; c++)
                //{
                source.addListener(fanWindow, channels[0]);
                //}
            }
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), e.getMessage(),
                    "Exception", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), e.getMessage(),
                    "Exception", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_fanPlotAllAction

    private void openDataBrowser(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDataBrowser
        // TODO add your handling code here:
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RmiServerBrowser("superdarn.ece.vt.edu:8080").setVisible(true);
            }
        });
    }//GEN-LAST:event_openDataBrowser


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner beamSpinner;
    private javax.swing.JComboBox channelCombo;
    private javax.swing.JPanel connectPanel;
    private javax.swing.JPanel fanPanel;
    private javax.swing.JButton fanPlotButton;
    private javax.swing.JCheckBox fanPlotPow;
    private javax.swing.JCheckBox fanPlotVel;
    private javax.swing.JCheckBox fanPlotWidth;
    private javax.swing.JSpinner hourSpinnerLength;
    private javax.swing.JSpinner hourSpinnerStart;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSpinner latSpinner;
    private javax.swing.JCheckBox loadOldData;
    private javax.swing.JSpinner lonSpinner;
    private javax.swing.JSpinner minuteSpinnerLength;
    private javax.swing.JSpinner minuteSpinnerStart;
    private javax.swing.JComboBox radarCombo;
    private javax.swing.JSpinner secondSpinnerStart;
    private javax.swing.JPanel timePanel;
    private javax.swing.JButton timePlotButton;
    private javax.swing.JCheckBox timePlotPow;
    private javax.swing.JCheckBox timePlotVel;
    private javax.swing.JCheckBox timePlotWidth;
    private javax.swing.JPanel timeSubPanel1;
    private javax.swing.JPanel timeSubPanel2;
    private javax.swing.JComboBox zoomCombo;
    // End of variables declaration//GEN-END:variables

    private void setComboNoServer() {

        this.channelCombo.setEnabled(false);
        this.timePlotButton.setEnabled(false);
        this.hourSpinnerLength.setEnabled(false);
        this.minuteSpinnerLength.setEnabled(false);
        this.hourSpinnerStart.setEnabled(false);
        this.minuteSpinnerStart.setEnabled(false);
        this.secondSpinnerStart.setEnabled(false);
        this.beamSpinner.setEnabled(false);
        this.loadOldData.setEnabled(false);
        this.timePlotPow.setEnabled(false);
        this.timePlotVel.setEnabled(false);
        this.timePlotWidth.setEnabled(false);
        this.zoomCombo.setEnabled(false);
        this.fanPlotPow.setEnabled(false);
        this.fanPlotVel.setEnabled(false);
        this.fanPlotWidth.setEnabled(false);

        this.fanPlotButton.setEnabled(false);
        this.latSpinner.setEnabled(false);
        this.lonSpinner.setEnabled(false);

    }

    private void fanPlot(DataSource source, ChannelId channel, PlotParameter what) {
        if ( world == null ) {
            world = new World();
        }

        //double lat = ((Double) (latSpinner.getValue())).doubleValue();
        //double lon = ((Double) (lonSpinner.getValue())).doubleValue();
        double lat =  60.0;
        double lon = -95.0;
        Geographic centre = new Geographic(lat, lon, 0.0);

        Projection proj = new Stereographic(1.8, centre);

        FanWindow fanWindow;

        Dimension windowSize;

        if ( fanPlotDimension != null )
            windowSize = fanPlotDimension;
        else
            windowSize = new Dimension(screenSize.height/2, screenSize.height/2);


        try {
            world.waitUntilLoaded();
            fanWindow = new FanWindow(proj, world, what, true, windowSize, true);
            fanWindow.setFanViewCentre(centre);

            source.addListener(fanWindow, channel);

            fanWindow.setVisible(true);
            fanWindow.addWindowListener(this);

            if ( fanPlotLocation != null )
                fanWindow.setLocation(fanPlotLocation);

        } catch (NoSuchFieldException e) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(),
                    "Invalid data parameter for fan window", "Internal Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } catch( InterruptedException e) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(),
                    "World map loading was interrupted.", "Internal Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }


    }

    private void timePlot(DataSource source, String radar, ChannelId channel, PlotParameter what) {

        // set the calendar to the time specified in the start spinners.
        Date now = new Date();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, ((Integer) this.hourSpinnerStart
                .getValue()).intValue());
        cal.set(Calendar.MINUTE, ((Integer) this.minuteSpinnerStart.getValue()).intValue());
        cal.set(Calendar.SECOND, 0);

        // re-compute the values in the Calendar.
        cal.get(Calendar.HOUR);

        // if the time is after the current data time, set it back 24 hours
        // (comparing Calendar with Date doesn't work, it doesn't generate any
        // error though...

        if (cal.getTime().after(now)) {
            cal.add(Calendar.HOUR_OF_DAY, -24);
            // "make it so..."
            if ( cal.get(Calendar.MINUTE) > 0 ) {
                cal.set(Calendar.MINUTE, 0);
                cal.add(Calendar.HOUR_OF_DAY, 1);
            }
            cal.get(Calendar.HOUR);
        }

        Date startDate = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, ((Integer) this.hourSpinnerLength.getValue()).intValue());
        cal.add(Calendar.MINUTE,
                ((Integer) this.minuteSpinnerLength.getValue()).intValue());

       float duration = ((Integer) this.hourSpinnerLength.getValue()).floatValue() +
                ((Integer) this.minuteSpinnerLength.getValue()).floatValue() / 60.0f;


        Dimension windowSize;

        if ( timePlotDimension != null )
            windowSize = timePlotDimension;
        else
            windowSize = new Dimension(screenSize.width * 3 / 4, screenSize.height / 3);

        TimeWindow timeWin;

        try {

            timeWin = doTimePlot(radar, startDate, duration, channel,
                    ((Integer) beamSpinner.getValue()).intValue(),
                    what, -1.0, true, loadOldData.isSelected(), zoomCombo.getSelectedIndex(),
                    windowSize, timePlotLocation);
        } catch (NoSuchFieldException e) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(),
                    "Invalid data parameter for time window", "Internal Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), e.getMessage(),
                    "Exception", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), e.getMessage(),
                    "Exception", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void windowClosing(WindowEvent e) {
        Window w = e.getWindow();
//System.out.println("ControlPanelBean window closing: "+w);

        for(DataSource d: dataSources.values() ) {
            if ( w instanceof TimeWindow || w instanceof FanWindow ) {

                d.removeListener((NewData)w);
            }
            else {
                d.shutdown();
            }
        }
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
}

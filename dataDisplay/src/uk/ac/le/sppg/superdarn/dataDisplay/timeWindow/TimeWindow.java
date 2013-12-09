package uk.ac.le.sppg.superdarn.dataDisplay.timeWindow;

import java.awt.Toolkit;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotSubParameter;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import uk.ac.le.sppg.general.display.IntegerField;
import uk.ac.le.sppg.superdarn.colour.Legend;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotParameter;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotWindow;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;
import uk.ac.le.sppg.superdarn.fitData.NewData;

/**
 * This class provides a time sequence view of SuperDARN and SPEAR data
 * from a single beam.
 * It can display the velocity, lambda spectral width and lambda power
 * values either with or without ground scatter identification.
 * The values are colour coded according to a {@link Legend} which sets
 * the number of colours and the upper and lower limits.
 * <p>
 * The plot consitsts of a panel containing the time plot itself,
 * a {@link Legend} and an outer area showing various information
 * from the data.
 * These fields show the time of the last data plotted, the beam
 * number, frequency, noise level and attenuator setting.
 * <p>
 * The initial type of data to display is set in the constructor
 * by the string
 * <code>what</code> which should be one of "velocity", "power"
 * or "width". Any other string will result in a <code>NoSuchFieldException</code>
 * being thrown.
 * The value can be changed programatically via the {@link #setWhat(String)}
 * method, or interactively from a popup menu.
 * <p>
 * The intitial start and end times of the plot window can be specified in
 * the constructor.
 * If they are not set the plot will be the 24 hour period which contains
 * the time at which the plot is created.
 * The range extent of the plot is not set until the first data record is
 * passed to it.
 * <P>
 * New data is added to the plot by calling the {@link #newData(FitacfData)}
 * method.
 * This draws a colour coded vertical strip on the plot at the time of
 * the data.
 * The time extent of this strip is determined from the time extent of the
 * plot such that for a 24hr plot it is 90s.
 * The reason for not using the actual integration time of the beam is that
 * for scanning experiments this would result in very narrow (or possibly
 * invisible) strips with large gaps between.
 * <P>
 * When the end time of the frame is reached the plot window will scroll
 * along by 1/12th the frame time extent and new start/end times
 * will be calculated.
 * <p>
 * Old data can be plotted by calling either of the the methods
 * {@link #oldData(FitacfData)} and {@link #drawOldData(OldData,long)}.
 * The {@link #oldData(FitacfData)} method requires the calling program to
 * read the old data and then pass it to the method for display.
 * The {@link #drawOldData(OldData,long)} method creates a thread which will
 * read the old data and then plot it, stepping back in time to the beginning
 * time of the frame.
 * This method requires an object which implements the {@link #OldData}
 * interface to read the old data.
 * <p>
 * The plot provides a popup menu which allows some aspects of the plot to be
 * controlled interactively. Various aspects of the world map can be altered,
 * the data type can be selected and ground scatter can be turned on and off.
 * Also, the plot can be zoomed and magnified.
 * The differenct between zooming and magnification relates to the data
 * resolution.
 * <P>
 * Zooming keeps the data resolution the same, but provides an increased
 * size viewing area in a scrolled panel.
 * Use this if just want to have a closer look at the plot as it is.
 * <p>
 * Magnification keeps the viewing are the same but reduces the time extent
 * of the frame by 2 and also the width of the strip drawn for each data record.
 * This effectively increases the resolution of the plot.
 * All the data has to be redrawn so it is not instantaneous.
 * Use this for high time resolution data which doesn't show correctly with
 * the default strip width.
 * <p>
 * The beam number which is displayed can be changed by entering a new
 * value into the beam <code>JTextArea</code> field and pressing Enter.
 * This will result in all currently plotted data being erased.
 * If {@link drawOldData(OldData,long)} has been called previously to
 * establish an <code>OldData</code> supporting interface, this interface
 * will be used to again read old data for the new beam number.
 *
 * @author Nigel Wade
 */
public class TimeWindow
        extends JFrame
        implements PlotWindow, Legend.Limits, NewData, ActionListener, WindowListener {
    
    private static final long serialVersionUID = 0x5253505047000041L;
    
    final static int ONE_SECOND = 1000;
    
    class SetTitle extends Thread {
        TimeWindow win;
        public SetTitle(TimeWindow parent){
            this.win = parent;
        }
        public void run() {
            if ( metrics != null ) {
                synchronized( siteString ) {
                    win.setTitle(siteString + " " + what.type.name() + " vs. time " + channelString );
                    
                    int width = metrics.stringWidth(dateString);
                    Insets i = dateField.getInsets();
                    width = width + i.left + i.right;
                    
                    Dimension d = dateField.getSize();
                    
                    if ( d.width < width ) {
                        d.width = width;
                        dateField.setMinimumSize(d);
                        dateField.setPreferredSize(d);
                        dateField.invalidate();
                        win.validate();
                    }
                    
                    dateField.setText(dateString);
                    CPIDField.setText(CPIDString);
                    channelField.setText(currentChannel.toString());
                    
                    if ( !beamField.isFocusOwner())
                        beamField.setValue(beamNumber);
                }
            }
            
        }
    }
    
    class SetNewDataLabel extends Thread {
        boolean ok;
        public SetNewDataLabel(boolean ok){
            this.ok = ok;
        }
        public void run() {
            if ( ok ) {
                noNewData.setForeground(Color.DARK_GRAY);
                noNewData.setText("Data ok");
            } else {
                noNewData.setForeground(Color.RED);
                noNewData.setText("No new data received");
            }
        }
        
    }
    
    public interface OldData {
        public FitacfData getNextData(ChannelId channel, long afterTime, int beam)
        throws IOException;
        
        public String site();
    }
    
    OldData oldReader = null;
    
    class OldDataThread extends Thread {
        long lastTime;
        long start;
        long end;
        int beamRequired;
        ChannelId channel;
        
        Timer paintPlotTimer = new Timer(ONE_SECOND, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                plot.repaint();
            }
        });
        
        private volatile boolean runnable = true;
        
        // create a thread to read the old data.
        public OldDataThread(OldData reader,ChannelId channel,int beam,long start, long end) {
            
            oldReader = reader;
            this.beamRequired = beam;
            this.start = start;
            this.end = end;
            this.channel = channel;
            
            setName("OldDataThread:"+oldReader.site()+":"+channel.name()+":"+beam);
        }
        
        
        public void abort() {
            runnable = false;
        }
        public OldData getReader() {
            return oldReader;
        }
        
        public void run() {
            
            paintPlotTimer.start();
            
            lastTime = start;
//            System.out.println(">>>>>> old data thread "+this+" "+new Date(start)+" "+new Date(end));
            
            int sleepMillis = 2000;
            while (runnable && oldReader != null) {
                
                // read the next data record
                try {
                    
//System.out.println(this+" requst data after time "+new Date(lastTime));
                    FitacfData fit = oldReader.getNextData(channel,lastTime,beamRequired);
                    sleepMillis = 2000;
                    //oldReader.getPreviousData(
                    //	channel,
                    //	lastTime,
                    //	beamRequired);
                    
                    // returned null indicates no new data.
                    if (fit == null) {
                        System.out.println(this+" null fit record");
                        runnable = false;
                        System.out.println("Old data thread read null: site "+oldReader.site()+": channel "+channel+": beam "+beamRequired);
                        break;
                    }
                    
//System.out.println(this+" read data for time "+fit.radarParms.date+" "+fit.radarParms.beamNumber+" "+fit.radarParms.stationId);
                    lastTime = fit.radarParms.date.getTime();
                    if ( lastTime > end || oldData(fit)) {
                        runnable = false;
                        System.out.println("Old data thread reached end time: site "+oldReader.site()+": channel "+channel+": beam "+beamRequired);
                        System.out.println(this+" "+(lastTime-end));
                    }
                    
                } catch (IOException e) {
                    System.out.println("Error reading old data: site "+oldReader.site()+": channel "+channel+": beam "+beamRequired);
                    System.out.println(e.getMessage());
                    try {
                        sleep(sleepMillis);
                        if ( sleepMillis < 500000 ) {
                            sleepMillis *= 2;
                        }
                    } catch(Exception eee ){}
                }
                
                if ( ! runnable ) {
                    System.out.println("Old data thread no longer runnable: site "+oldReader.site()+": channel "+channel+": beam "+beamRequired);
                }
            }
            System.out.println("Old data thread ending: site "+oldReader.site()+": channel "+channel+": beam "+beamRequired);
            paintPlotTimer.stop();
            
            plot.paintPlot();
            
        }
        
    }
    
    OldDataThread thread = null;
    
    JScrollPane scrollPane;
    
    JPanel timePanel;
    JPanel legendPanel;
    JCheckBox allowNewData;
    JLabel noNewData;
    
    Timer newDataTimer;
    
    TimePlot plot; // the TimePlot which displays the data
    
    JTextField dateField; // displays the date/time of the data in
    // the NORTH area of the frame.
    DateFormat dateFormat =
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
    // date/time format for the dateField
    // currently LONG format for locale GMT
    FontMetrics metrics;
    
    private JTextField CPIDField;
    IntegerField beamField;
    private JTextField channelField;
    
    // strings used in title.
    private String siteString = "";
    private PlotSubParameter what;
    private String channelString = "";
    private String dateString = "";
    private String CPIDString = "";
    private ChannelId currentChannel = ChannelId.UNKNOWN;
    
    public interface Constants {
        public static final double MINVEL = -1000.0;
        public static final double MAXVEL = 1000.0;
        public static final double MINPOWER = 0.0;
        public static final double MAXPOWER = 30.0;
        public static final double MINWIDTH = 0.0;
        public static final double MAXWIDTH = 500.0;
    }
    
    public boolean disposed = false;
    
    // Legend for the data
    public Legend legend;
    Legend velocityLegend;
    Legend powerLegend;
    Legend widthLegend;
    
    int beamNumber;
    final ChannelId channel;
    
    static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    static final Dimension defaultSize = new Dimension(screenSize.width * 3 / 4, screenSize.height / 3);
    
    static final int MAX_NEW_CACHE = 500;
    ArrayList<FitacfData> newDataCache = new ArrayList<FitacfData>();
    boolean cacheNewData = false;
    
    Date mostRecent = null;
    
    public TimeWindow(TimeWindowConfig config) 
    throws NoSuchFieldException
    {
        if ( config.size == null )
            this.setSize(defaultSize);
        else
            this.setSize(config.size);
        
        if ( config.location != null )
            this.setLocation(config.location);
        
        this.beamNumber = config.beamNumber;
        this.channel = config.channel;
        
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        
        ArrayList<PlotSubParameter> whatSub = config.what;
        plot = new TimePlot(config.startDate, config.endDate, beamNumber, whatSub, this);
        
        scrollPane = new JScrollPane(plot);
        //plot.setPreferredSize( new Dimension( PLOT_WIDTH, PLOT_HEIGHT ) );
        
        createPanel(whatSub.get(0), config.showNewData );
        
    }
    
    /**
     * Creates a <code>TimeWindow</code> with the specified start and end
     * times.
     * @param startDate
     * the required start of the plot window
     * @param endDate
     * the required end of the plot window
     * @param what
     * ArrayList of Strings containing which parameters to plot.
     * Currently allowed values are "velocity", "power" and "width".
     * @param channel
     * which radar channel to plot data from.
     * For non-stereo radars this should be "" and for stereo it should be
     * normally be either A or B.
     * @param gs
     * whether to indicate ground scatter in grey
     * @param beamNumber
     * the beam number of interest.
     * @throws NoSuchFieldException
     * if <code>what</code> is not one of the allowed values.
     */
    public TimeWindow(
            Date startDate,
            Date endDate,
            ArrayList<PlotParameter> what,
            ChannelId channel,
            boolean gs,
            boolean single,
            boolean showNewData,
            int beamNumber)
            throws NoSuchFieldException {
        
        this.setSize(defaultSize);
        this.beamNumber = beamNumber;
        this.channel = channel;
        
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        
        ArrayList<PlotSubParameter> whatSub = plotTypeToSub(what, gs, single);
        plot = new TimePlot(startDate, endDate, beamNumber, whatSub, this);
        
        scrollPane = new JScrollPane(plot);
        //plot.setPreferredSize( new Dimension( PLOT_WIDTH, PLOT_HEIGHT ) );
        
        createPanel(whatSub.get(0), showNewData );
    }
    
    /**
     * Creates a <code>TimeWindow</code> which will show the current day.
     * The start time is 0UT and the end time is 24UT.
     * @param what
     * ArrayList of Strings containing which parameters to plot.
     * Currently allowed values are "velocity", "power" and "width".
     * @param channel
     * which radar channel to plot data from.
     * For non-stereo radars this should be "" and for stereo it should be
     * normally be either A or B.
     * @param gs
     * whether to indicate ground scatter in grey
     * @param beamNumber
     * the beam number of interest.
     * @throws NoSuchFieldException
     * if <code>what</code> is not one of the allowed values.
     */
    public TimeWindow(ArrayList<PlotParameter> what, ChannelId channel, boolean gs, boolean single, boolean showNewData, int beamNumber)
    throws NoSuchFieldException {
        
        this.setSize(defaultSize);
        
        this.beamNumber = beamNumber;
        this.channel = channel;
        
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        ArrayList<PlotSubParameter> whatSub = plotTypeToSub(what, gs, single);
        plot = new TimePlot(beamNumber, whatSub, this);
        
        scrollPane = new JScrollPane(plot);
        
        createPanel(whatSub.get(0), showNewData);
    }
    
    
    /**
     * Creates a <code>TimeWindow</code> with the specified start and end
     * times.
     * @param startDate
     * the required start of the plot window
     * @param endDate
     * the required end of the plot window
     * @param what
     * ArrayList of Strings containing which parameters to plot.
     * Currently allowed values are "velocity", "power" and "width".
     * @param channel
     * which radar channel to plot data from.
     * For non-stereo radars this should be "" and for stereo it should be
     * normally be either A or B.
     * @param gs
     * whether to indicate ground scatter in grey
     * @param beamNumber
     * the beam number of interest.
     * @param size
     * the size of the plot area in pixels.
     * @throws NoSuchFieldException
     * if <code>what</code> is not one of the allowed values.
     */
    public TimeWindow(
            Date startDate,
            Date endDate,
            ArrayList<PlotParameter> what,
            ChannelId channel,
            boolean gs,
            boolean single,
            boolean showNewData,
            int beamNumber,
            Dimension size)
            throws NoSuchFieldException {
        
        this.beamNumber = beamNumber;
        this.channel = channel;
        
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        ArrayList<PlotSubParameter> whatSub = plotTypeToSub(what, gs, single);
        plot = new TimePlot(startDate, endDate, beamNumber, whatSub, this);
        
        scrollPane = new JScrollPane(plot);
        //scrollPane.setPreferredSize( new Dimension( width, height ) );
        setSize(size);
        
        createPanel(whatSub.get(0), showNewData);
    }
    /**
     * Creates a <code>TimeWindow</code> which will show the current day.
     * The start time is 0UT and the end time is 24UT.
     * @param what
     * ArrayList of Strings containing which parameters to plot.
     * Currently allowed values are "velocity", "power" and "width".
     * @param channel
     * which radar channel to plot data from.
     * For non-stereo radars this should be "" and for stereo it should be
     * normally be either A or B.
     * @param gs
     * whether to indicate ground scatter in grey
     * @param beamNumber
     * the beam number of interest.
     * @param szie
     * the size of the plot area in pixels.
     * @throws NoSuchFieldException
     * if <code>what</code> is not one of the allowed values.
     */
    public TimeWindow(
            ArrayList<PlotParameter> what,
            ChannelId channel,
            boolean gs,
            boolean single,
            boolean showNewData,
            int beamNumber,
            Dimension size)
            throws NoSuchFieldException {
        
        this.setSize(size);
        
        this.beamNumber = beamNumber;
        this.channel = channel;
        
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        ArrayList<PlotSubParameter> whatSub = plotTypeToSub(what, gs, single);
        plot = new TimePlot(beamNumber, whatSub, this);
        
        scrollPane = new JScrollPane(plot);
        //scrollPane.setPreferredSize( new Dimension( width, height ) );
        
        createPanel(whatSub.get(0), showNewData);
    }
    
    private ArrayList<PlotSubParameter> plotTypeToSub(ArrayList<PlotParameter> what, boolean gs, boolean single) {
        ArrayList<PlotSubParameter> whatSub = new ArrayList<PlotSubParameter>();
        
        for (PlotParameter s:what) {

            whatSub.add(new PlotSubParameter(s,gs));
            
            if ( !single ) {
                whatSub.add(new PlotSubParameter(s, !gs));
            }
        }
        
        return whatSub;
        
    }
    
    private void createPanel(PlotSubParameter what, boolean showNewData) throws NoSuchFieldException {
        
        Border loweredBevelBorder = BorderFactory.createLoweredBevelBorder();
        
        Container pane = this.getContentPane();
        timePanel = new JPanel();
        timePanel.setLayout(new BorderLayout());
        pane.add(timePanel);
        
        JPanel upperPanel = new JPanel();
        //upperPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        // a field to show the date/time at the top of the frame
        dateField = new JTextField();
        dateField.setEditable(false);
        dateField.setBorder(loweredBevelBorder);
        dateField.setBackground(Color.WHITE);
        //dateField.setColumns( 20 );
        
        upperPanel.add(dateField);
        
        if (channel != null && !channel.equals("")) {
            upperPanel.add(new JLabel("Channel:"));
            channelField = new JTextField();
            channelField.setBorder(loweredBevelBorder);
            channelField.setColumns(3);
            channelField.setHorizontalAlignment(JTextField.RIGHT);
            channelField.setText(channel.toString());
            channelField.setEditable(false);
            upperPanel.add(channelField);
        }
        
        upperPanel.add(new JLabel("Beam:"));
        beamField = new IntegerField(beamNumber);
        beamField.setBorder(loweredBevelBorder);
        beamField.addActionListener(this);
        beamField.setColumns(3);
        beamField.setHorizontalAlignment(JTextField.RIGHT);
        upperPanel.add(beamField);
        
        //upperPanel.add( new JLabel( "Time: " ) );
        
        JPanel CPIDPanel = new JPanel();
        CPIDPanel.add(new JLabel("CPID: "));
        
        CPIDField = new JTextField();
        CPIDField.setColumns(4);
        CPIDField.setEditable(false);
        
        CPIDPanel.add(CPIDField);
        
        upperPanel.add(CPIDPanel);
        
        // check box for toggling updating of new data
        allowNewData = new JCheckBox("New data update");
        allowNewData.setSelected(showNewData);
        cacheNewData = ! allowNewData.isSelected();
        allowNewData.addActionListener(this);
        upperPanel.add(allowNewData);
        
        JPanel newDataPanel = new JPanel();
        
        // warning for no new data in specified time
        noNewData = new JLabel("No new data received");
        //noNewData.setEnabled(false);
        noNewData.setForeground(Color.RED);
        newDataPanel.add(noNewData);
        
        newDataTimer = new Timer(300*ONE_SECOND, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setNewDataLabel(false);
            }
        });
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        
        topPanel.add(upperPanel, BorderLayout.WEST);
        topPanel.add(newDataPanel, BorderLayout.EAST);
        
        timePanel.add(topPanel, BorderLayout.NORTH);
        
        // create legends
        velocityLegend =
                new Legend(false, Constants.MINVEL, Constants.MAXVEL, 256, this);
        powerLegend =
                new Legend(true, Constants.MINPOWER, Constants.MAXPOWER, 256, this);
        widthLegend =
                new Legend(true, Constants.MINWIDTH, Constants.MAXWIDTH, 256, this);
        //legend = velocityLegend;
        
        legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.X_AXIS));
        //legendPanel.add( legend );
        
        setWhat(what);
        
        timePanel.add(legendPanel, BorderLayout.EAST);
        
        // the data plot and legend in the centre of the frame
        
        plot.setScrollPane(scrollPane);
        
        //scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        timePanel.add(scrollPane, BorderLayout.CENTER);
        
        // pack the components
        this.setVisible(true);
        
        // calculate the width of the legend necessary to be able to
        // display the text.
        //double legendWidth = legend.requiredWidth();
        
        //		  Dimension pd = plot.getSize();
        //		  Dimension ld = legend.getSize();
        //		  System.out.println( "legend size: "+legend.getSize() );
        //		  Insets li = legend.getInsets();
        //		  System.out.println( li );
        //		  System.out.println( legendWidth );
        //
        //		  ld.setSize( (int)legendWidth+li.left+li.right, pd.height );
        //		  legend.setPreferredSize( ld );
        //		  System.out.println( "legend preferred size: "+legend.getPreferredSize() );
        //
        //		  legend.invalidate();
        
        // must do this after the GUI is pack'ed and therefore realized
        // otherwise there is no Graphics.
        metrics = this.getGraphics().getFontMetrics();
        int width =
                metrics.stringWidth(
                dateFormat.format(new Date(System.currentTimeMillis())));
        
        Dimension d = dateField.getMinimumSize();
        Insets i = dateField.getInsets();
        d.width = width + i.left + i.right;
        dateField.setMinimumSize(d);
        dateField.setPreferredSize(d);
        dateField.invalidate();
        
        this.validate();
        
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(this);
        
        plot.paintPlot();
        
    }
    
    /**
     * Draws old data into the plot window back to the beginning of the
     * plot window.
     * This method creates a thread which will run in the background
     * requesting old data from <code>reader</code> until either there
     * is no older data or the frame start time is reached.
     * The method starts by requesting the most recent data <em>before</em>
     * <code>newest</code>, and steps back in time so the plot progresses
     * in the reverse time direction.
     * <p>
     * The parameter <code>reader</code> must implement the
     * <code>OldData</code> interface.
     * @param reader
     * the source of the old data
     * @param newest
     * the time before which data will be requested.
     * {@see java.util.Date} <code>getTime()</code> method.
     * <p>
     *
     */
    public void drawOldData(OldData reader, long oldest, long newest) {
        
        if (thread != null && thread.isAlive()) {
            thread.abort();
            thread.interrupt();
        }
        
        if (reader == null)
            return;
        
        // create a thread to read old data
        thread = new OldDataThread(reader, channel, beamNumber, oldest, newest);
        
        // this method clears all currently stored beams and graphics images.
        //plot.setBeamNumber( beamNumber );
        
        beamField.setValue(beamNumber);
        
        // run the old data thread.
        thread.start();
    }
    
    public void limitsChanged(double min, double max) {
        
        plot.setLimits(min, max);
    }
    
    /**
     * Sets what type of data is to be drawn.
     * @param what
     * an accepted data type, currently "velocity", "power" and "width".
     * @throws NoSuchFieldException
     * if <code>what</code> is not one of the accepted data types.
     */
    public void setWhat(PlotSubParameter what) throws NoSuchFieldException {
        setLegend(what);
    }
    
    /**
     * Sets what type of data is to be drawn.
     * @param what
     * an accepted data type, currently "velocity", "power" and "width".
     * @throws NoSuchFieldException
     * if <code>what</code> is not one of the accepted data types.
     */
    public void setLegend(PlotSubParameter what) throws NoSuchFieldException {
        
        switch ( what.type ) {
            case velocity:
                legend = velocityLegend;
                break;
            case width:
                legend = widthLegend;
                break;
            case power:
                legend = powerLegend;
                break;
            default:
                throw new NoSuchFieldException("Cannot plot data of type " + what.type);
        }
        
        legendPanel.removeAll();
        legendPanel.add(legend);
        
        this.what = what;
        
        SwingUtilities.invokeLater(new SetTitle(this));
        
        plot.setWhat(what);
        
        this.validate();
    }
    
    /**
     * Gets the current <code>Legend</code> being used in the plot.
     * @return
     * the <code>Legend</code> in use.
     */
    public Legend getLegend() {
        return legend;
    }
    
    public ChannelId getChannel() {
        return channel;
    }
    
    /**
     * Gets the <code>Legend</code> for a specific data types.
     * @param what
     * the data type for which the <code>Legend</code> is required.
     * @return
     * the <code>Legend</code> which is used for the required data type,
     * or <code>null</code> if <code>what</code> is not one of the
     * accepted types.
     */
    public Legend getLegend(PlotParameter what) {
        
        switch ( what ) {
            case velocity:
                return velocityLegend;
            case width:
                return widthLegend;
            case power:
                return powerLegend;
            default:
                return null;
        }
        
    }
    
    /**
     * Sets the time width for each strip of the plot.
     * @param seconds
     * the width in seconds.
     */
    public void setDrawWidth(int seconds) {
        plot.setDrawWidth(seconds);
    }
    
    /**
     * Called by the DataSource if it aborts
     */
    public void dataSourceAbort() {
        windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    
    /**
     * Add new data to the plot.<P>
     * The new data will be drawn at the relevent time and with an
     * effective width of either that set by {@link #setWidth} or
     * the default of 2/2880ths, or 90s for a 24hr plot.
     * Specifically, the strip is drawn from the time of the data
     * up to this time + strip width.
     * @param data
     * the <code>FitacfData</code> which is to be plotted.
     */
    public void newData(FitacfData data) {
        
        
        // set the newDataLabel to indicate data is being received and
        // reset the timer which sets it to no new data received
        SwingUtilities.invokeLater(new SetNewDataLabel(true));
        newDataTimer.restart();
        
//System.out.println("new data "+data.radarParms.beamNumber+" "+data.radarParms.channel);
        if (data.radarParms.beamNumber != beamNumber)
            return;
        
        
        // if the plotting of new data has been temporarily disabled then cache it
        // internally. The max which will be store is MAX_NEW_CACHE and
        // new data pushes out old data.
        if ( cacheNewData ) {
            if ( newDataCache.size() >= MAX_NEW_CACHE ) {
                newDataCache.remove(0);
            }
            newDataCache.add(data);
            //System.out.println("cache new data");
            return;
        }
        
        
        synchronized( siteString ) {
            siteString = SuperDarnSiteList.getList().getById(data.radarParms.stationId).getName();
            
            if (data.radarParms.channel == 1) {
                channelString = "  channel: A";
                if ( channel != ChannelId.A ) {
                    return;
                }
                currentChannel = ChannelId.A;
            } else if (data.radarParms.channel == 2) {
                channelString = "  channel: B";
                if ( channel != ChannelId.B ) {
                    return;
                }
                currentChannel = ChannelId.B;
            } else {
                channelString = "  channel: unknown";
                currentChannel = ChannelId.UNKNOWN;
            }
            
            dateString = dateFormat.format(data.radarParms.date);
            
            
            CPIDString = String.valueOf(data.radarParms.controlProgramId);
            
            mostRecent = data.radarParms.date;
        }
        SwingUtilities.invokeLater(new SetTitle(this));
        
        plot.newData(data);
    }
    
    /**
     * Add old data to the plot.
     * The old data will be drawn at the data integration end time
     * and with an effective width of either that set by {@link #setWidth}
     * or the default of 2/2880ths, or 90s for a 24hr plot.
     * Specifically, the strip is drawn from the end time of the integration
     * period back to this time - strip width.
     * @param data
     * the <code>FitacfData</code> which is to be plotted.
     */
    public boolean oldData(FitacfData data) {
        
        synchronized( siteString ) {
            if ( siteString.equals("") || mostRecent == null || mostRecent.before(data.radarParms.date) ) {
                siteString = SuperDarnSiteList.getList().getById(data.radarParms.stationId).getName();
                
                if (data.radarParms.channel == 1) {
                    channelString = "  channel: A";
                    if ( channel != ChannelId.A ) {
                        return false;
                    }
                    currentChannel = ChannelId.A;
                } else if (data.radarParms.channel == 2) {
                    channelString = "  channel: B";
                    if ( channel != ChannelId.B ) {
                        return false;
                    }
                    currentChannel = ChannelId.B;
                } else {
                    channelString = "  channel: unknown";
                    currentChannel = ChannelId.UNKNOWN;
                }

                
                CPIDString = String.valueOf(data.radarParms.controlProgramId);
                
                if ( mostRecent == null || data.radarParms.date.getTime()-mostRecent.getTime() > 600000 ) {
                    dateString = dateFormat.format(data.radarParms.date);
                    SwingUtilities.invokeLater(new SetTitle(this));
                    mostRecent = data.radarParms.date;
                }
            }
        }
        
        boolean result = plot.oldData(data);
        return result;
    }
    
    
    /**
     * Turns off indication of ground scatter
     */
    public void groundscatterOff() {
        plot.groundscatterOff();
    }
    /**
     * Turns on indication of ground scatter.
     */
    public void groundscatterOn() {
        plot.groundscatterOn();
    }
    
    public void paintPlot() {
        plot.paintPlot();
    }
    
    public void magnify() {
        plot.magnify();
    }
    
    public void reduce() {
        plot.reduce();
    }
    
    public void setMagnification(int mag) {
        plot.setMagnification(mag);
    }
    
    public void zoomIn() {
        plot.zoomIn();
    }
    
    public void zoomOut() {
        plot.zoomOut();
    }
    
    public void setZoom(int zoom) {
        plot.setZoom(zoom);
    }
    
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if (o == beamField) {
            beamNumber = (int) beamField.getValue();
            
            // this clears all the stored beam data in the plot
            // and deletes all the images and associated graphics.
            plot.setBeamNumber(beamNumber);
            
            drawOldData(oldReader, plot.frameStart, plot.frameEnd);
        } else if  (o == allowNewData) {
            // this toggle button controls plotting/caching of new data.
            // if enabled then new data will be plotted and passed to the plot window.
            // if it is disabled then new data will be cached internally.
            // When the toggle changed from enabled to disabled a new cache is created.
            // When the toggle changes from disabled to enabled the cached data is plotted.
            //System.out.println("toggled new data");
            
            cacheNewData = ! allowNewData.isSelected();
            //System.out.println(cacheNewData+" "+allowNewData.isSelected());
            if (cacheNewData) {
                newDataCache.clear();
            } else {
                if ( newDataCache.size() > 0) {
                    for( FitacfData data : newDataCache ) {
//                    for(Iterator i=newDataCache.iterator(); i.hasNext();) {
//                        FitacfData data = (FitacfData)i.next();
                        newData(data);
                    }
                }
            }
        }
        
    }
    
    private void setNewDataLabel(boolean  ok ) {
        if ( ok ) {
            noNewData.setForeground(Color.DARK_GRAY);
            noNewData.setText("Data ok");
        } else {
            noNewData.setForeground(Color.RED);
            noNewData.setText("No new data received");
        }
    }
    
    public void windowClosing(WindowEvent e) {
        // if the old data thread is running wait for it to finish.
//System.out.println("TimeWindow closing");
        if (thread != null) {
            thread.abort();
            
            try {
                thread.join(10000);
            } catch (InterruptedException e2) {
            }
            
            thread = null;
        }
        
        if ( plot != null ) {
            plot.clearAll();
            plot = null;

            beamField.removeActionListener(this);
            this.removeWindowListener(this);

            velocityLegend.setCallback(null);
            powerLegend.setCallback(null);
            widthLegend.setCallback(null);

            removeAll();
        } 
        
//System.out.println("TimeWindow closing returns");
    }
    public void windowClosed(WindowEvent e) {
        
//System.out.println("TimeWindow closed");
        disposed = true;
        
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

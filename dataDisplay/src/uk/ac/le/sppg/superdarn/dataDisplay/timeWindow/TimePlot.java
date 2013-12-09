package uk.ac.le.sppg.superdarn.dataDisplay.timeWindow;


import uk.ac.le.sppg.superdarn.dataDisplay.PlotParameter;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotSubParameter;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSite;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import uk.ac.le.sppg.general.display.RulerBean;
import uk.ac.le.sppg.superdarn.colour.LinearScale;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;

/**
 * @author Nigel Wade
 */
// the time plot sub-divides the panel into a set of sub-images.
// when the time of the data exceeds the end of a sub-image a new image
// is created. If the time exceeds the end of the plot window the oldest
// image is deleted and the time limits are moved up.
// This way, the time window scrolls allong by the size of a sub-image.
// There are 6 images created for each sub-image, one each for each
// parameter with and without ground scatter displayed.
class TimePlot extends JPanel implements ActionListener, Scrollable {

    private static final long serialVersionUID = 0x5253505047000040L;
    
    final static int ONE_SECOND = 1000;

    boolean debug = false;

    Runnable paintIt = new Runnable() {
        public void run() {
            repaint();
        }
    };

    // this timer task runs when the frame end time is reached.
    // it changes the frame end time
    //    class EndFrameTask extends TimerTask {
    //        public void run() {
    //            synchronized(allSubPlots) {
    //                System.out.println("timer fired to set end time");
    //                // need to check if the data update toggle is set, if not don't do
    // anything.
    //                setFrameEndTime(frameEnd + frameTimeExtent/SUB_IMAGES);
    //            }
    //        }
    //    }

    Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT0"));

    SimpleDateFormat timeAxisFormat = new SimpleDateFormat("HH:mm");

    ArrayList<Beam> beams = new ArrayList<Beam>(1500);

    int oldBeamIndex = 0;

    FreqSelector freqList = new FreqSelector(this);

    boolean plotFrequencyRestricted = false;

    boolean freqListInitialized = false;

    int beamNumber;

    double startRange = -1.0;

    double endRange = -2.0;

    String startRangeStr;

    String endRangeStr;

    long frameStart;
    long frameEarliestNewData = 0;

    long frameEnd;

    long frameTimeExtent;

    Date frameEndDate;

    Point2D origin;

    Point2D upperRight;

    long axisStep;

    long axisSubStep;

    long axisStart;

    //long imageStart;
    long imageEnd;

    long currentTime = -1;


    JCheckBoxMenuItem groundScatterCheck;

    // Background popup menu.
    // Allows control of which bits of the World are plotted

    JScrollPane scrollPane = null;

    int zoomLevel = 0;
    int magnification = 0;

    boolean trackWidth = true;

    Dimension preferredSize = null;

    // the actual popup menu
    JPopupMenu popup;

    ArrayList<JMenuItem> menuItems = new ArrayList<JMenuItem>();

    MouseListener popupListener;

    JRadioButtonMenuItem velocityRadio;

    JRadioButtonMenuItem powerRadio;

    JRadioButtonMenuItem widthRadio;

    JSlider scaleSlider; // slider to set the scale for Sterographic projection

    DetailBox detailBox;

    final int DEFAULT_SUB_IMAGES = 12;

    final int DEFAULT_IMAGE_WIDTH = 2880 / DEFAULT_SUB_IMAGES;

    int DRAW_WIDTH; // width in milli-seconds of each profile

    boolean manualDrawWidth = false;

    final int IMAGE_WIDTH; // image width

    final int IMAGE_HEIGHT = 675; // 9 pixel per range

    final int SUB_IMAGES; // the number of sub-images

    final int FREQ_NOISE_IMAGE_HEIGHT = 20;

    final int FREQ_MAX = 19000;

    final int FREQ_MIN = 9000;

    final double LOGE_10 = Math.log(10.0);

    static final Color backgroundColour = Color.WHITE;

    static final Color axesColour = Color.BLACK;

    // the number of images in a sub-plot.
    // this depends on the value of imageLevel.
    // if imageLevel == 0, then only 1 image is drawn
    // if imageLevel == 1 then 2 images are drawn, the parameter with and
    // without ground scatter.
    // any other imageLevel draws all data types with and without ground scatter
    //final int SUB_PLOT_IMAGES;

    // the current sub images being drawn into
    SubPlot currentImage = null;

    SubPlot previousImage = null;

    // lists to store the SubPlot's for the entire display
    ArrayList<SubPlot> allSubPlots = new ArrayList<SubPlot>();

    LinearScale noiseFreqColours = new LinearScale(10);

    AffineTransform axisTransform = null;

    Dimension size = null;

    Insets insets;

    FontMetrics metrics;

    // labels for freq and noise display
    String freqLabel = new String("Freq.");

    String noiseLabel = new String("Noise");

    int freqLabelLength;

    int noiseLabelLength;

    Object oldBeamThreadSync = new Object();

    // synchronization object for oldBeamThread.

    // Remember what is to be plotted
    PlotSubParameter showWhat;

    ArrayList<PlotSubParameter> plotWhat = new ArrayList<PlotSubParameter>();

    TimeWindow parent;

    Timer frameEndTimer;
    
    RulerBean ruler;

    private void init(int beamNumber, ArrayList<PlotSubParameter> what) 
    throws NoSuchFieldException {

        setLayout(null);
        
//        JDesktopPane desktop = new javax.swing.JDesktopPane();
        ruler = new RulerBean();

//        setLayout(new java.awt.BorderLayout());

//        desktop.setOpaque(false);
//        ruler.setVisible(true);
        ruler.setBounds(0, 0, 200, 27);
        ruler.setOpacity(0.6f);
        
//        desktop.add(ruler, javax.swing.JLayeredPane.DEFAULT_LAYER);

        this.add(ruler);



        this.beamNumber = beamNumber;

        plotWhat.addAll(what);
        
        // default plot parameter is first in list;
        showWhat = what.get(0);

        addMenu();

        detailBox = new DetailBox("Details", this);

        currentTime = frameStart;

        timeAxisFormat.setCalendar(calendar);

        //frameEndTimer = new Timer(true);
        //frameEndTimer.schedule(new EndFrameTask(), new Date(frameEnd));
        int delay = (int) (frameEnd - (new Date()).getTime());
        frameEndTimer = new Timer(delay, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //                System.out.println("end frame timer fired at "+new Date());
                //              // need to check if the data update toggle is set, if not
                // don't do anything.
                if (parent.allowNewData.isSelected()) {
                    setFrameEndTime(frameEnd + frameTimeExtent / SUB_IMAGES);
                }
            }
        });
        frameEndTimer.setRepeats(false);
        if (delay < 0) {
            frameEndTimer.stop();
        } else {
            frameEndTimer.start();
        }
        //        System.out.println("Initial time is "+new Date());
        //        System.out.println("end frame timer set to fire in "+delay);


        freqList.setPlotWidth(DRAW_WIDTH / 1000);
        
    }

    public TimePlot(Date startDate, Date endDate, int beamNumber, ArrayList<PlotSubParameter> what, TimeWindow parent) 
    throws NoSuchFieldException {

        this.parent = parent;

        frameStart = startDate.getTime();
        frameEnd = endDate.getTime();
        frameEndDate = new Date(frameEnd);

        frameTimeExtent = frameEnd - frameStart;

        IMAGE_WIDTH = DEFAULT_IMAGE_WIDTH;
        SUB_IMAGES = DEFAULT_SUB_IMAGES;

        // set the plot width so it's 3 pixels
        DRAW_WIDTH = (int) ((3f * frameTimeExtent) / IMAGE_WIDTH / SUB_IMAGES);
        imageEnd = frameStart;

        init(beamNumber, what);
        //SUB_PLOT_IMAGES = plotTypes.length * 2;

    }

    public TimePlot(int beamNumber, ArrayList<PlotSubParameter> what, TimeWindow parent)
            throws NoSuchFieldException {

        //SUB_PLOT_IMAGES = plotTypes.length * 2;

        this.parent = parent;

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT0"));
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        frameStart = cal.getTimeInMillis();

        cal.add(Calendar.HOUR, 24);
        frameEnd = cal.getTimeInMillis();
        frameEndDate = new Date(frameEnd);

        frameTimeExtent = frameStart - frameEnd;

        IMAGE_WIDTH = DEFAULT_IMAGE_WIDTH;
        SUB_IMAGES = DEFAULT_SUB_IMAGES;

        // set the plot width so it's 3 pixels
        DRAW_WIDTH = (int) ((3f * frameTimeExtent) / IMAGE_WIDTH / SUB_IMAGES);
        init(beamNumber, what);
    }

    public TimePlot(Date startDate, Date endDate, int beamNumber, ArrayList<PlotSubParameter> what, 
            int width, int images, TimeWindow parent) throws NoSuchFieldException {

        this.parent = parent;

        //SUB_PLOT_IMAGES = plotTypes.length * 2;

        frameStart = startDate.getTime();
        frameEnd = endDate.getTime();
        frameEndDate = new Date(frameEnd);
        frameTimeExtent = frameEnd - frameStart;

        IMAGE_WIDTH = width;
        SUB_IMAGES = images;

        // set the plot width so it's 3 pixels
        DRAW_WIDTH = (int) ((3f * frameTimeExtent) / IMAGE_WIDTH / SUB_IMAGES);
        init(beamNumber, what);
    }

    public TimePlot(int beamNumber, ArrayList<PlotSubParameter> what, int width, int images, TimeWindow parent)
            throws NoSuchFieldException {

        this.parent = parent;

        //SUB_PLOT_IMAGES = plotTypes.length * 2;

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT0"));
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        frameStart = cal.getTimeInMillis();

        cal.add(Calendar.HOUR, 24);
        frameEnd = cal.getTimeInMillis();
        frameEndDate = new Date(frameEnd);

        frameTimeExtent = frameEnd - frameStart;

        IMAGE_WIDTH = width;
        SUB_IMAGES = images;

        // set the plot width so it's 3 pixels
        DRAW_WIDTH = (int) ((3f * frameTimeExtent) / IMAGE_WIDTH / SUB_IMAGES);
        init(beamNumber, what);

    }

    // the background popup menu allows varioius parts of the plot
    // to be toggled on and off.
    private void addMenu() {

        popup = new JPopupMenu();

        JMenu submenu1 = new JMenu("Select data");
        JMenu submenu2 = new JMenu("Zoom");

        ButtonGroup b = new ButtonGroup();

        velocityRadio = new JRadioButtonMenuItem("velocity");
        velocityRadio.addActionListener(this);
        if (showWhat.type == PlotParameter.velocity) {
            velocityRadio.setSelected(true);
        }
        b.add(velocityRadio);
        submenu1.add(velocityRadio);

        widthRadio = new JRadioButtonMenuItem("spectral width");
        widthRadio.addActionListener(this);
        widthRadio.setActionCommand("width");
        if (showWhat.type == PlotParameter.width) {
            widthRadio.setSelected(true);
        }
        b.add(widthRadio);
        submenu1.add(widthRadio);

        powerRadio = new JRadioButtonMenuItem("lamba power");
        powerRadio.setActionCommand("power");
        powerRadio.addActionListener(this);
        if (showWhat.type == PlotParameter.power) {
            powerRadio.setSelected(true);
        }
        b.add(powerRadio);
        submenu1.add(powerRadio);

        submenu1.add(new JSeparator());

        // toggle for ground scatter display
        groundScatterCheck = new JCheckBoxMenuItem("ground scatter");
        groundScatterCheck.setState(showWhat.gs);
        groundScatterCheck.addActionListener(this);
        submenu1.add(groundScatterCheck);

        popup.add(submenu1);

        JMenuItem menuItem = new JMenuItem("Zoom in");
        menuItem.setActionCommand("zoom in");
        menuItem.addActionListener(this);
        submenu2.add(menuItem);
        menuItems.add(menuItem);

        menuItem = new JMenuItem("Zoom out");
        menuItem.setActionCommand("zoom out");
        menuItem.addActionListener(this);
        submenu2.add(menuItem);
        menuItems.add(menuItem);

        submenu2.add(new JSeparator());

        menuItem = new JMenuItem("Magnify");
        menuItem.setActionCommand("magnify");
        menuItem.addActionListener(this);
        submenu2.add(menuItem);
        menuItems.add(menuItem);

        menuItem = new JMenuItem("Reduce");
        menuItem.setActionCommand("reduce");
        menuItem.addActionListener(this);
        submenu2.add(menuItem);
        menuItems.add(menuItem);

        popup.add(submenu2);
        
        popup.add(new JSeparator());
        menuItem = new JMenuItem("Restrict frequencies...");
        menuItem.setActionCommand("restrict frequencies");
        menuItem.addActionListener(this);

        popup.add(menuItem);

        popup.add(new JSeparator());
        menuItem = new JMenuItem("Ruler...");
        menuItem.setActionCommand("ruler");
        menuItem.addActionListener(this);
        
        popup.add(menuItem);

        //Add listener to components that can bring up popup menus.
        popupListener = new popupListener();
        this.addMouseListener(popupListener);
    }

    public void setScrollPane(JScrollPane pane) {
        scrollPane = pane;
    }

    // zoom in and zoom out simply change the viewport on-screen.
    // The images backing the display are the same, they are just
    // expaned/reduced along the time
    // axis and scrollbars displayed as necessary.
    public void zoomIn() {
        zoomLevel++;
//        System.out.println("Size: "+getSize());
//        System.out.println("Visible: "+this.getVisibleRect());
        
        // the scrollbar maximum is the viewport size minus the extent (size of scroll "button" on bar).
        // the value is the left edge of the scroll "button".
        // When the viewport is doubled in size the "button" should maintain its relative location.
        
        // What is needed is to calculate the buttons new position. The left edge of the button is
        // posititioned such that the value+extent+remainder = size.
        // When the viewport is resized the extent halves, the remainder doubles, so we should be able
        // to calculate the new value.
        
        
        JScrollBar bar = scrollPane.getHorizontalScrollBar();
//        System.out.println("Before: bar min, max, value, visible: "+bar.getMinimum()+" "+bar.getMaximum()+" "+bar.getValue()+" "+bar.getVisibleAmount());
//        
        int visibleAmount = bar.getVisibleAmount();
        int rightOffset = bar.getMaximum() - bar.getValue() - visibleAmount;
//
        Rectangle visibleRect = getVisibleRect();

        Dimension size = getSize();
        Dimension newSize = new Dimension(size);
        
        newSize.width = newSize.width * 2;
        trackWidth = false;
        this.setPreferredSize(newSize);
        

        revalidate();
        
        // double the right offset, halve the visibleAmount
        int newValue = newSize.width - visibleAmount/2 - rightOffset*2;
        
//        System.out.println("width, visibleAmount, offset, newValue: "+
//                newSize.width+" "+visibleAmount+" "+rightOffset+" "+newValue);
        
        Rectangle rect = null;
        if ( visibleRect.x+visibleRect.width == size.width) {
            rect = new Rectangle(new Point((int)newSize.getWidth(),0));
        }
        else {
            rect = new Rectangle(new Point(newValue,0));
        }
        this.scrollRectToVisible(rect);
//        bar = scrollPane.getHorizontalScrollBar();
//        System.out.println("After: bar min, max, visible, value: "+bar.getMinimum()+" "+bar.getMaximum()+" "+bar.getVisibleAmount()+" "+bar.getValue());
        
        bar.setValue(newValue);

    }

    public void zoomOut() {
        if (zoomLevel > 0) {
            zoomLevel--;

//            Dimension newSize = new Dimension(this.size);
            Dimension newSize = getSize();
            newSize.width = newSize.width / 2;
            this.setPreferredSize(newSize);
        } else {
            trackWidth = true;
            this.setPreferredSize(scrollPane.getViewport().getViewSize());
        }

        revalidate();
    }
    
    public void setZoom(int newZoomLevel) {
        while(newZoomLevel != zoomLevel ) {
            if ( newZoomLevel > zoomLevel ) 
                zoomIn();
            else 
                zoomOut();
        }
    }

    public void magnify() {
        changeMagnification(true);
    }
    
    public void reduce() {
        changeMagnification(false);
    }
    
    public void setMagnification(int newMagnification) {
        while(newMagnification != magnification ) {
            if ( newMagnification > magnification ) 
                magnify();
            else 
                reduce();
        }
    }
    
    // changing the magnification reduces/increases the extent of the time axis
    // and also
    // increases/decreases the resolution of the plot so the data needs to be
    // redrawn.
    private void changeMagnification(boolean increase) {

        if (increase) {
            frameTimeExtent /= 2;
            magnification++;
        } else {
            frameTimeExtent *= 2;
            magnification--;
        }

        frameStart = frameEnd - frameTimeExtent;
        frameEarliestNewData = 0;

        redrawAllOldBeams();
    }

    public synchronized void setRange(double startRange, double endRange) {
        this.startRange = startRange;
        this.endRange = endRange;

        startRangeStr = String.valueOf((int) startRange);
        endRangeStr = String.valueOf((int) (endRange + 0.5));

        // create a new SubPlot with the correct mapping for the new
        // ranges, and set the time limits to what is left of the
        // current SubPlot.

        setAxisTransform();

    }

    // this method clears all current beams and all current graphics images.

    public synchronized void setBeamNumber(int beamNumber) {
        this.beamNumber = beamNumber;

        deleteAllBeams();

        deleteAllPlots();
        revalidate();
    }

    public void setDrawWidth(int seconds) {
        DRAW_WIDTH = seconds * 1000;
        manualDrawWidth = true;
    }

    // called by the legend if the limits of the legend are changed
    public synchronized void setLimits(double min, double max) {

        for (Beam b : beams ) {

            b.mapData(min, max);
        }

        redrawAllOldBeams();
    }

    public void groundscatterOff() {
        if ( showWhat.gs ) {
            setWhat(new PlotSubParameter(showWhat.type, false));
        }
    }

    public void groundscatterOn() {
        if ( ! showWhat.gs ) {
            setWhat(new PlotSubParameter(showWhat.type, true));
        }
    }

    // method to pass new data to a beam
    public synchronized void newData(FitacfData data) {

        //        System.out.println("new data time is now "+new Date());

        // if it's not the required beam number return
        if (data.radarParms.beamNumber != beamNumber)
            return;

        Beam beam;

        currentTime = data.radarParms.date.getTime();
        
        if ( frameEarliestNewData == 0 ) {
            frameEarliestNewData = currentTime;
        }

        // get the site from the SiteList list
        SuperDarnSite site = SuperDarnSiteList.getList().getById(data.radarParms.stationId);

        // can't do anything if we don't know about the site!
        if (site == null)
            return;

        // if it's not a SuperDarn site why has it provided fit data?
        if (!site.isSuperDarnSite())
            return;

        // disable the frameEndTimer so it doesn't fire an event while we are
        // processing new data
        frameEndTimer.stop();

        // create a new beam
        // we need to get the az, el and beam width
        // for Spear, this is in the Site object,

        // for SuperDARN sites we need to do it differently.

        // calculate startRange in km.
        // firstRangeDistance is in km
        double newStartRange = data.radarParms.firstRangeDistance;
        double newEndRange = newStartRange + data.radarParms.numberOfRanges * data.radarParms.rangeSeparation;

        // rangeSeparation is in km, convert to m.
        double rangeIncrement = data.radarParms.rangeSeparation;

        // set plot startRange

        if (this.startRange < 0.0 || this.endRange < 0.0 || newStartRange < this.startRange
                || newEndRange > this.endRange) {

            setRange(newStartRange, newEndRange);

            if (currentImage != null) {
                //                    System.out.println("new image 1");
                long currentEnd = currentImage.endTime;
                previousImage = currentImage;
                currentImage = new SubPlot(plotWhat, parent, IMAGE_WIDTH, IMAGE_HEIGHT, FREQ_NOISE_IMAGE_HEIGHT,
                        newStartRange, newEndRange, currentTime, currentEnd);
                addSubPlot(currentImage);
            }

        }

        if (site.getName().equals("Spear")) {
            double beamWidth = site.getBeamSeparation();

            // I'm assuming here the az and el are packed into the
            // upper and lower bytes of the beamNumber.
            double az = data.radarParms.beamNumber & 0xf;
            double el = (data.radarParms.beamNumber >>> 8) & 0xf;

            beam = new Beam(site, data.radarParms.beamNumber, az, el, beamWidth, startRange, rangeIncrement,
                    data.radarParms.numberOfRanges);

        } else {
            SuperDarnSite sdSite = (SuperDarnSite) site;

            beam = new Beam(sdSite, data.radarParms.beamNumber, data.radarParms.firstRangeDistance,
                    data.radarParms.rangeSeparation, data.radarParms.rxRiseTime, data.radarParms.numberOfRanges);
        }

        // if there is any part of the data for this beam in the current
        // SubImage
        // then plot it.

        beams.add(beam);

        // pass the new data to the relevent beam and plot it.
        beam.setData(data);

        freqList.increment(data.radarParms.txFrequency);

        long dataTime = data.radarParms.date.getTime();

        // if any of the data fits in the previous image
        if (previousImage != null && dataTime < previousImage.endTime) {
            drawBeam(beam, previousImage, null, true);
        }

        // draw what fits in the current image
        if (dataTime < imageEnd && currentImage != null) {
            drawBeam(beam, currentImage, null, true);
        }

        // if any of the data fits in the next image, create
        // a new image and draw the data again

        // if the end of the current sub-image has been reached
        // move the time along and create a new set of images.
        if (dataTime + this.DRAW_WIDTH > imageEnd) {

            // keep stepping along, adding the sub-image time and
            // creating images until the data time is within the sub-image
            // time range.
            long imageStart = 0;
            while (dataTime + this.DRAW_WIDTH > imageEnd) {
                imageStart = imageEnd;
                imageEnd = imageStart + frameTimeExtent / SUB_IMAGES;
            }

            //                System.out.println("new image 2");
            previousImage = currentImage;
            currentImage = new SubPlot(plotWhat, parent, IMAGE_WIDTH, IMAGE_HEIGHT, FREQ_NOISE_IMAGE_HEIGHT,
                    startRange, endRange, imageStart, imageEnd);

            addSubPlot(currentImage);

            if (imageEnd > frameEnd) {
                setFrameEndTime(imageEnd);
            }

            // draw the beam in the new SubImage
            drawBeam(beam, currentImage, null, true);

        }

        frameEndTimer.start();

        paintPlot();
        //        System.gc();

    }

    void addSubPlot(SubPlot subPlot) {
        synchronized (allSubPlots) {
            allSubPlots.add(subPlot);
        }
    }

    private void drawBeamWhat(Beam beam, SubPlot subPlot, PlotSubParameter what, boolean forward) {

        //        System.out.println("drawBeamWhat:
        // "+Thread.currentThread().getName());

        if (plotFrequencyRestricted && !freqList.isEnabled(beam.frequency)) {
            //			System.out.println("Dont plot freq "+beam.frequency);
            return;
        }

        if (!freqListInitialized) {
            freqList.initBands(beam.site);
            freqListInitialized = true;
        }

        SubPlot.PlotImage plotImage = (SubPlot.PlotImage) subPlot.plotImages.get(what);

        //System.out.println("got subplot "+plotImage);
        if (plotImage == null) {
            // this parameter does not currently have an image, so create one.
            plotImage = subPlot.addParameter(what);

            if (plotImage == null) {
                return;
            }
        }

        beam.setWhat(what, plotImage.legend.getMin(), plotImage.legend.getMax());

        // fill the gate with the colour for the data
        // first with groundScatter...
        beam.fillGates(plotImage.graphics, plotImage.legend.getColourScale(), subPlot.startTime, DRAW_WIDTH, forward);

    }

    private void plotNoise(Graphics2D g2, long timeAxisStart, int noise, long time) {
        double nlog = (Math.log((double) (noise / 200.0)) / LOGE_10) / 3.0;

        if (nlog < 0.0)
            nlog = 0;
        else if (nlog > 1.0)
            nlog = 1.0;

        // scale to range of a byte.
        byte index = (byte) ((Byte.MAX_VALUE - Byte.MIN_VALUE) * nlog + Byte.MIN_VALUE);

        g2.setColor(noiseFreqColours.colour(index));

        // the area to be filled is time-time+DRAW_WIDTH in time
        // and from startRange to endRange
        Rectangle2D r = new Rectangle2D.Double();

        r.setRect(time - timeAxisStart, startRange, DRAW_WIDTH, endRange - startRange);
        g2.fill(r);

    }

    private void plotFreq(Graphics2D g2, long timeAxisStart, int freq, long time) {

        byte index = (byte) ((Byte.MAX_VALUE - Byte.MIN_VALUE) * (freq - FREQ_MIN) / (FREQ_MAX - FREQ_MIN) + Byte.MIN_VALUE);

        g2.setColor(noiseFreqColours.colour(index));

        // the area to be filled is time-time+DRAW_WIDTH in time
        // and from startRange to endRange
        Rectangle2D r = new Rectangle2D.Double();
        r.setRect(time - timeAxisStart, startRange, DRAW_WIDTH, endRange - startRange);
        g2.fill(r);

    }

    private synchronized void plotNoiseFreq(Beam beam, SubPlot subPlot) {
        long dataTime = beam.getDate().getTime();

        plotNoise(subPlot.noiseImage.graphics, subPlot.startTime, beam.getNoise(), dataTime);

        plotFreq(subPlot.freqImage.graphics, subPlot.startTime, beam.getFrequency(), dataTime);

    }

    private synchronized void drawBeam(Beam beam, SubPlot subPlot, PlotSubParameter what, boolean forward) {

        //        System.out.println("drawBeam: "+beam.date);

        if (what == null) {
            for (PlotSubParameter par: plotWhat) {
                drawBeamWhat(beam, subPlot, par, forward);
            }
        } else {
            drawBeamWhat(beam, subPlot, what, forward);
        }

        plotNoiseFreq(beam, subPlot);
        //        System.out.println("drawBeam: return");

        //repaint();

    }

    // method to pass old data to a beam
    public synchronized boolean oldData(FitacfData data) {

        //        System.out.println( "old beam data" );

        // if it's not the required beam number return
        if (data.radarParms.beamNumber != beamNumber) {
            //        System.out.println("oldData return");
            return false;
        }

        // if the time is after the frame start, tell the caller.
        if (data.radarParms.date.after(frameEndDate)) {
            //        System.out.println("oldData return");
            return true;
        }
        
        // if the data is after new data which has arrived return true
        if (frameEarliestNewData > 0 && data.radarParms.date.getTime() > frameEarliestNewData) {
             return true;
        }

        Beam beam;

        //currentTime = data.radarParms.date.getTime();

        // get the site from the SiteList list
        SuperDarnSite site = SuperDarnSiteList.getList().getById(data.radarParms.stationId);

        // can't do anything if we don't know about the site!
        if (site == null) {
            return true;
        }
        // if it's not a SuperDarn site why has it provided fit data?
        if (!site.isSuperDarnSite()) {
            //        System.out.println("oldData return");
            return true;
        }
        //
        //System.out.println("correct beam,site");
        // create a new beam
        // we need to get the az, el and beam width
        // for Spear, this is in the Site object,

        // calculate startRange in km.
        // firstRangeDistance is in km, rxRiseTime in uS.
        double newStartRange = data.radarParms.firstRangeDistance;
        double newEndRange = newStartRange + data.radarParms.numberOfRanges * data.radarParms.rangeSeparation;

        // rangeSeparation is in km, convert to m.
        double rangeIncrement = data.radarParms.rangeSeparation;

        // set plot startRange
        if (this.startRange < 0.0 || this.endRange < 0.0 || newStartRange < this.startRange
                || newEndRange > this.endRange) {

            setRange(newStartRange, newEndRange);

        }

        // for SuperDARN sites we need to do it differently.

        if (site.getName().equals("Spear")) {
            double beamWidth = site.getBeamSeparation();

            // I'm assuming here the az and el are packed into the
            // upper and lower bytes of the beamNumber.
            double az = data.radarParms.beamNumber & 0xf;
            double el = (data.radarParms.beamNumber >>> 8) & 0xf;

            beam = new Beam(site, data.radarParms.beamNumber, az, el, beamWidth, startRange, rangeIncrement,
                    data.radarParms.numberOfRanges);

        } else {
            SuperDarnSite sdSite = (SuperDarnSite) site;

            beam = new Beam(sdSite, data.radarParms.beamNumber, data.radarParms.firstRangeDistance,
                    data.radarParms.rangeSeparation, data.radarParms.rxRiseTime, data.radarParms.numberOfRanges);
        }

        beams.add(oldBeamIndex, beam);
        oldBeamIndex++;

        // pass the new data to the relevent beam and plot it.
        beam.setData(data);
        //System.out.println("created new beam");
        freqList.increment(data.radarParms.txFrequency);

        drawOldBeam(beam, null);
        //        System.out.println("oldData return");
        return false;
    }

    synchronized void drawOldBeam(Beam beam, PlotSubParameter what) {

        // find the SubPlot in which this data is to be drawn

        //        System.out.println("drawOldBeam");
        int subIndex = 0;
        SubPlot sub = null;

        boolean plottedAll = false;

        long dataTime = beam.getDate().getTime();

        //System.out.println("old data "+beam.getDate()+" "+new
        // Date(dataTime+DRAW_WIDTH));
        int subPlotSize;
        synchronized (allSubPlots) {
            subPlotSize = allSubPlots.size();
        }

        if (subPlotSize == 0) {
            // there are no sub-plots on the frame.
            // create one which will contain the start time of the beam.
            long newImageEnd = frameStart;
            while (dataTime > newImageEnd) {
                newImageEnd += frameTimeExtent / SUB_IMAGES;
            }

            long newImageStart = newImageEnd - frameTimeExtent / SUB_IMAGES;
            if (newImageStart < frameStart) {
                newImageStart = frameStart;
            }

            //                System.out.println("create new image "+new Date(newImageStart)+"
            // "+new Date(newImageEnd));
            sub = new SubPlot(plotWhat, parent, IMAGE_WIDTH, IMAGE_HEIGHT, FREQ_NOISE_IMAGE_HEIGHT, startRange,
                    endRange, newImageStart, newImageEnd);

            synchronized (allSubPlots) {
                allSubPlots.add(sub);
                subPlotSize = allSubPlots.size();
            }

            if (currentImage == null || currentImage.startTime < sub.startTime) {
                imageEnd = newImageEnd;
                previousImage = currentImage;
                currentImage = sub;
            }
        }

        while (subIndex < subPlotSize) {
            synchronized (allSubPlots) {
                sub = allSubPlots.get(subIndex);
            }

            if (dataTime >= sub.startTime && dataTime <= sub.endTime) {
                // some of the beam is in this image, draw it
                drawBeam(beam, sub, what, false);
                //System.out.println("plot old data in "+new
                // Date(sub.startTime));
                // if all the data is within this image we've done
                plottedAll = (dataTime + DRAW_WIDTH <= sub.endTime);

            }

            if (!plottedAll && dataTime + DRAW_WIDTH >= sub.startTime && dataTime + DRAW_WIDTH <= sub.endTime) {
                // some of the beam is in this image, draw it
                drawBeam(beam, sub, what, false);
                //System.out.println("plot old data in "+new
                // Date(sub.startTime));
                plottedAll = true;
            }

            if (sub.endTime >= dataTime + DRAW_WIDTH) {
                // data time ends in this image.
                break;
            }

            subIndex++;
            synchronized (allSubPlots) {
                subPlotSize = allSubPlots.size();
            }
        }

        if (!plottedAll) {

            // a new image is required to plot part of the beam
            long newImageEnd;
            long newImageStart;

            // start the new image where the previous one finishes
            if (subIndex == 0) {
                newImageStart = frameStart;
            } else {
                newImageStart = allSubPlots.get(subIndex - 1).endTime;
            }
            while (newImageStart + frameTimeExtent / SUB_IMAGES < dataTime) {
                newImageStart += frameTimeExtent / SUB_IMAGES;
            }

            if (subIndex < allSubPlots.size() && newImageStart + frameTimeExtent / SUB_IMAGES > sub.startTime) {
                // there's an image which has a start time which is less than
                // frameTimeExtent/SUB_IMAGES
                // beyond this image start, so use that as the end time for this
                // image
                newImageEnd = sub.startTime;
            } else {
                newImageEnd = newImageStart + frameTimeExtent / SUB_IMAGES;
            }

            //System.out.println("new sub plot for old data "+new
            // Date(newImageStart)+ " "+new Date(newImageEnd));
            sub = new SubPlot(plotWhat, parent, IMAGE_WIDTH, IMAGE_HEIGHT, FREQ_NOISE_IMAGE_HEIGHT, startRange,
                    endRange, newImageStart, newImageEnd);

            synchronized (allSubPlots) {
                allSubPlots.add(subIndex, sub);
            }
            //System.out.println("new image "+new Date(newImageStart)+" "+new
            // Date(newImageEnd));

            if (currentImage == null || currentImage.startTime < sub.startTime) {
                imageEnd = newImageEnd;
                previousImage = currentImage;
                currentImage = sub;
            }

            drawBeam(beam, sub, what, true);
            //System.out.println("plot data in "+new Date(sub.startTime));

        }

        //        System.out.println("drawOldBeam return");

    }

    //    // changes the scale for the plot
    //    public void scale( int width, int height ) {
    //    	AffineTransform at = new AffineTransform();
    //
    //     	//at.scale( width, height );
    //    	
    //    	//((Graphics2D)getGraphics()).transform( at );
    //    	
    //    	
    //    	this.height = height;
    //    	this.width = width;
    //    	
    //    	System.out.println( "Set scale to "+height+" "+width );
    //    }

    public synchronized void setWhat(PlotSubParameter parameter) {

        // do nothing if the new data type is the same as the current one
        if (parameter.equals(showWhat))
            return;

        showWhat = parameter;


        switch( parameter.type ) {
           case velocity:
                this.velocityRadio.setSelected(true);
                break;
            case power:
                this.groundscatterOff();
                this.powerRadio.setSelected(true);
                break;
            case width:
                this.groundscatterOff();
                this.widthRadio.setSelected(true);
                break;
        }
         
        groundScatterCheck.setSelected(showWhat.gs);
        
        // if not all images are currently drawn then draw the images of the
        // existing beams for this data type
        if ( ! plotWhat.contains(parameter) ) {
            plotWhat.add(parameter);
            redrawOldBeams(parameter);
        }
    }

    public void paintPlot() {
        SwingUtilities.invokeLater(paintIt);
    }

    public void paintComponent(Graphics g) {
        //        synchronized (allSubPlots) {

        Graphics2D g2 = (Graphics2D) g;
 
        super.paintComponent(g); //clears the background

        // set initial "default" range
        if (startRangeStr == null || endRangeStr == null)
            setRange(0.0, 1.0);

        // not yet realized?
        if (this.size == null || this.insets == null) {
            return;
        }


        Dimension size = this.getSize();

        if (size.height != this.size.height || size.width != this.size.width) {
            setAxisTransform();
        }

        //Insets insets = this.getInsets();

        g.setColor(TimePlot.backgroundColour);
        g.fillRect(0, 0, size.width, size.height);

        //System.out.println( size );


        int ysize = size.height - insets.top - insets.bottom;
        int y0;

        // get each of the sub images and plot them

        Image img;
        Point2D p1 = new Point2D.Double();
        Point2D p2 = new Point2D.Double();

        Point2D p3, p4;

        // set a clipping region on the plot.
        Shape clip = g2.getClip();

        Rectangle r = new Rectangle(insets.left, 0, size.width - insets.left - insets.right, size.height
                - insets.bottom);
        g2.clip(r);

        synchronized (allSubPlots) {
            for (SubPlot sub : allSubPlots) {

                img = sub.getImage(showWhat);

                if (img == null)
                    continue;

                // calculate the x and y location in which to draw the image.
                p1.setLocation(sub.startTime, sub.startRange);
                p2.setLocation(sub.endTime, sub.endRange);

                p3 = axisTransform.transform(p1, null);
                p4 = axisTransform.transform(p2, null);
                //System.out.println( p3+" "+p4 );

                //System.out.println( p3 );
                //System.out.println( p4 );

                // plot the data image
                g2.drawImage(img, (int) p3.getX(), (int) p4.getY(), (int) (p4.getX() - p3.getX()) + 1,
                        (int) (p3.getY() - p4.getY()), null);

                // plot the freq. image
                img = sub.freqImage.image;
                y0 = 2;
                ysize = metrics.getHeight() + metrics.getLeading() - 5;
                g2.drawImage(img, (int) p3.getX(), y0, (int) (p4.getX() - p3.getX()) + 1, ysize, null);

                // finally the noise image
                img = sub.noiseImage.image;
                y0 = metrics.getHeight() + metrics.getLeading() + 2;
                g2.drawImage(img, (int) p3.getX(), y0, (int) (p4.getX() - p3.getX()) + 1, ysize, null);

            }
        }

        g2.setClip(clip);

        g.setColor(Color.BLACK);
        g.drawString(freqLabel, insets.left - freqLabelLength - 5, metrics.getHeight() + metrics.getLeading() - 3);
        g.drawString(noiseLabel, insets.left - freqLabelLength - 5,
                2 * (metrics.getHeight() + metrics.getLeading()) - 3);

        drawAxes(g);
        //        System.out.println("paint plot exit");
        //        }

    }

    public long getFrameStart() {
        return frameStart;
    }

    public long getFrameEnd() {
        return frameEnd;
    }

    public ArrayList<Beam> getBeams() {
        return beams;
    }

    // listener for right click to post the popup menu.
    class popupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {

            // a double click on the window will bring up the
            // detail box.
            // when the detail box is displayed a single click
            // will search for the data at the location clicked on.
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (e.getClickCount() == 2)
                    detailBox.setVisible(true);

                if (detailBox.isVisible()) {
                    Point p = new Point(e.getX(), e.getY());
                    detailBox.toFront();

                    try {
                        AffineTransform t = getAxisTransform();
                        if (t == null)
                            return;

                        Point2D p2 = t.inverseTransform(p, null);
                        synchronized (beams) {
                            detailBox.showDetails((long) p2.getX(), p2.getY());
                        }
                    } catch (NoninvertibleTransformException exc) {
                        detailBox.showDetails(0, 0.0);
                    }
                }
            } else
                maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                // create the frequency list sub-menu

                popup.show(e.getComponent(), mouseX, mouseY);
            }
        }
    }

    // listener for checkbox activity in the popup menu
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        String ac = e.getActionCommand();

        if (ac.equals("zoom in")) {
            zoomIn();
        } else if (ac.equals("zoom out")) {
            zoomOut();
        } else if (ac.equals("magnify")) {
            changeMagnification(true);
        } else if (ac.equals("reduce")) {
            changeMagnification(false);
        } else if (ac.equals("restrict frequencies")) {
            freqList.setPlotWidth(DRAW_WIDTH / 1000);
            freqList.setVisible(true);
        } else if (ac.equals("ruler")) {
            ruler.show();
        } else if (ac.equals("redraw")) {
            plotFrequencyRestricted = freqList.isRestricted();
            int redrawPlotWidth = freqList.getPlotWidth();
            if (redrawPlotWidth * 1000 != DRAW_WIDTH) {
                setDrawWidth(redrawPlotWidth);
            }
            redrawAllOldBeams();
        } else {
            PlotSubParameter newType;
            if (o == groundScatterCheck) {
                newType = new PlotSubParameter(showWhat.type, groundScatterCheck.getState());
            } else {
                newType = new PlotSubParameter(Enum.valueOf(PlotParameter.class, ac), groundScatterCheck.getState());
            }
            try {
                parent.setWhat(newType);
            } catch (NoSuchFieldException ex) {
                System.out.println("class implimentation error in menu." + ex.getMessage());
            }
        }

        repaint();
    }

    //    private synchronized Image getImage( int n ) {
    //        Image img = null;
    //        
    //        if ( allSubPlots == null || n > allSubPlots.size()-1 )
    //            return null;
    //            
    //        Image[] images = (Image[]) allSubPlots.get( n );
    //        
    //        for ( int i=0; i<images.length; i++ ) {
    //            if ( plotWhat.equals( plotTypes[i] ) ) {
    //                if ( showGroundScatter )
    //                    img = images[i];
    //                else
    //                    img = images[i+plotTypes.length];
    //                break;
    //            }
    //        }
    //    
    //        return img;
    //    }
    //    
    //    private synchronized Image getNoiseImage( int n ) {
    //        if ( noiseImageList == null || n > noiseImageList.size()-1 )
    //            return null;
    //            
    //        return (Image) noiseImageList.get( n );
    //    }
    //    
    //    private synchronized Image getFreqImage( int n ) {
    //        if ( noiseImageList == null || n > freqImageList.size()-1 )
    //            return null;
    //            
    //        return (Image) freqImageList.get( n );
    //    }

    //    private AffineTransform getPlotTransform() {
    //        AffineTransform t = null;
    //        
    //        if ( plotGraphics == null )
    //            return null;
    //        
    //        for ( int i=0; i<plotTypes.length; i++ ) {
    //            if ( plotWhat.equals( plotTypes[i] ) ) {
    //                if ( this.showGroundScatter )
    //                    t = plotGraphics[i].getTransform();
    //                else
    //                    t = plotGraphics[i+plotTypes.length].getTransform();
    //                break;
    //            }
    //        }
    //    
    //        return t;
    //    }

    protected synchronized void setFrameEndTime(long newFrameEnd) {
        frameEndTimer.stop();

        frameEnd = newFrameEnd;
        frameStart = frameEnd - frameTimeExtent;
        frameEndDate = new Date(frameEnd);

        // remove any images which are before the new frame start time
        for (Iterator<SubPlot> i = allSubPlots.iterator(); i.hasNext();) {
            SubPlot sp = i.next();
            if (sp.endTime <= frameStart) {
                sp.dispose();
                i.remove();
            }
        }

        setAxisTransform();

        // remove any old beams which are before the start time.
        for (Iterator<Beam> i = beams.iterator(); i.hasNext();) {
            Beam b = i.next();

            if (b.getDate().getTime() < frameStart) {
                i.remove();
                freqList.decrement(b.getFrequency());
            } else {
                break;
            }
        }

        // update the timer
        int delay = (int) (frameEnd - (new Date()).getTime());
        //delay=2000;
        //        System.out.println("Time now is "+new Date());
        //        System.out.println("End of frame time is "+new Date(frameEnd));

        if (delay > 0) {
            frameEndTimer.setInitialDelay(delay);
            frameEndTimer.restart();
        }
        //        System.out.println("end frame timer set to fire in "+delay);

        paintPlot();

    }

    private AffineTransform getAxisTransform() {
        return axisTransform;
    }

    private void setAxisTransform() {

        Dimension size = this.getSize();

        if (insets == null) {
            // calculate insets necessary for text and noise/freq. display
            Graphics g = this.getGraphics();
            metrics = g.getFontMetrics();

            freqLabelLength = metrics.stringWidth(freqLabel);
            noiseLabelLength = metrics.stringWidth(noiseLabel);

            // space for x axis is text height + leading + 5 for tick
            int xAxisSpace = metrics.getHeight() + metrics.getLeading() + 5;

            // y axis has space for 5 characters +
            int yAxisSpace = metrics.stringWidth("00000.0") + 5;

            int noiseAndFreq = (metrics.getHeight() + metrics.getLeading()) * 2;

            insets = new Insets(noiseAndFreq, yAxisSpace, xAxisSpace, 0);

            g.dispose();

        }

        //System.out.println( size );
        //System.out.println( insets );

        this.size = size;

        double xlen = size.width - insets.left - insets.right;
        double ylen = size.height - insets.top - insets.bottom;

        // new AffineTransform to flip vertical axis
        axisTransform = new AffineTransform(1., 0., 0., -1., 0., (double) size.height);

        axisTransform.translate(insets.left, insets.bottom);

        // create a transform to flip the vertical axis
        //AffineTransform transform = new AffineTransform(
        // 1.,0.,0.,-1.,0.,(double) ylen );

        // scale to the plot size.
        //transform.scale( xlen, ylen );

        // scale to the axis ranges.
        axisTransform.scale((double) xlen / (frameEnd - frameStart), (double) ylen / (endRange - startRange));
        // translate the origin
        axisTransform.translate(-frameStart, -startRange);

        // calculate origin and upperRight coords in pixel coords
        Point2D p = new Point2D.Double(frameStart, startRange);
        origin = axisTransform.transform(p, null);

        p = new Point2D.Double(frameEnd, endRange);
        upperRight = axisTransform.transform(p, null);

        // calculate the time values to plot
        calendar.setTimeInMillis(frameStart);
        //System.out.println( "initial calendar: "+calendar );

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        long frameSecs = frameTimeExtent / 1000;
        long frameMins = frameSecs / 60;
        long frameHours = frameMins / 60;

        if (frameHours > 12) {
            axisStep = 4 * 60 * 60 * 1000; // step is 4 hours
            axisSubStep = axisStep / 4; // sub step is 1 hour;
            calendar.set(Calendar.HOUR_OF_DAY, (hour / 4) * 4);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } else if (frameHours > 6) {
            axisStep = 2 * 60 * 60 * 1000; // step is 2 hour
            axisSubStep = axisStep / 4; // sub step is 1/2 hour;
            calendar.set(Calendar.HOUR_OF_DAY, (hour / 2) * 2);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } else if (frameHours > 3) {
            axisStep = 1 * 60 * 60 * 1000; // 1 hour
            axisSubStep = axisStep / 4; // sub step is 15 mins;
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } else if (frameHours > 1) {
            axisStep = 30 * 60 * 1000; // 30 mins.
            axisSubStep = axisStep / 6; // sub step is 5 mins;
            calendar.set(Calendar.MINUTE, (min / 30) * 30);
            calendar.set(Calendar.SECOND, 0);
        } else if (frameMins > 30) {
            axisStep = 20 * 60 * 1000; // 20 mins.
            axisSubStep = axisStep / 4; // sub step is 5 mins;
            calendar.set(Calendar.MINUTE, (min / 20) * 20);
            calendar.set(Calendar.SECOND, 0);
        } else {
            axisStep = 10 * 60 * 1000; // 10 minutes
            axisSubStep = axisStep / 5; // sub step is 2 mins;
            calendar.set(Calendar.MINUTE, (min / 10) * 10);
            calendar.set(Calendar.SECOND, 0);
        }

        axisStart = calendar.getTimeInMillis();
        if (axisStart < frameStart)
            axisStart += axisStep;

    }

    private void drawAxes(Graphics g) {

        //AffineTransform t = g2.getTransform();
        if (startRangeStr == null || endRangeStr == null)
            return;

        g.setColor(axesColour);
        g.drawRect((int) origin.getX(), (int) upperRight.getY(), (int) (upperRight.getX() - origin.getX()) - 1,
                (int) (origin.getY() - upperRight.getY()));

        g.drawRect((int) origin.getX(), 2, (int) (upperRight.getX() - origin.getX()) - 1, metrics.getHeight()
                + metrics.getLeading() - 5);
        g.drawRect((int) origin.getX(), metrics.getHeight() + metrics.getLeading() + 2,
                (int) (upperRight.getX() - origin.getX()) - 1, metrics.getHeight() + metrics.getLeading() - 5);

        // add the range limits
        int strlen = metrics.stringWidth(startRangeStr);
        g.drawString(startRangeStr, (int) origin.getX() - strlen - 5, (int) origin.getY());

        strlen = metrics.stringWidth(endRangeStr);
        g.drawString(endRangeStr, (int) origin.getX() - strlen - 5, (int) upperRight.getY() + metrics.getHeight());

        // draw the time axis
        long axisTime = axisStart;
        String str;

        Point2D p = new Point2D.Double();
        Point2D p1, p2;

        //System.out.println( "Axis start: "+new Date( axisStart )+"
        // "+axisStart );

        long axisSubTime;

        while (axisTime < frameEnd) {
            str = this.timeAxisFormat.format(new Date(axisTime));
            strlen = metrics.stringWidth(str);

            p.setLocation(axisTime, startRange);
            p1 = axisTransform.transform(p, null);

            p.setLocation(axisTime, endRange);
            p2 = axisTransform.transform(p, null);

            g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());

            g.drawString(str, (int) p1.getX() - strlen / 2, (int) p1.getY() + metrics.getHeight());

            axisSubTime = axisTime + axisSubStep;
            axisTime += axisStep;

            while (axisSubTime < axisTime) {
                p.setLocation(axisSubTime, startRange);
                p1 = axisTransform.transform(p, null);

                p.setLocation(axisTime, endRange);
                p2 = axisTransform.transform(p, null);

                g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p1.getX(), (int) p1.getY() - 5);
                g.drawLine((int) p1.getX(), (int) p2.getY(), (int) p1.getX(), (int) p2.getY() + 5);

                axisSubTime += axisSubStep;
            }
            //System.out.println( p1.getX()+" "+origin.getX()+" "+str );

        }
    }

    private void redrawOldBeams(PlotSubParameter what) {
        if (beams.size() == 0)
            return;
        
        // reset the time of the earliest new data
        // this will allow the old data drawing thread to plot all the beams
        // up until any new data arrives.
        frameEarliestNewData = 0;

        DrawOldBeamsThread t = new DrawOldBeamsThread(what, oldBeamThreadSync, beams, this, new Date(frameStart));

        t.start();
    }

    private void redrawAllOldBeams() {

        if (beams.size() == 0)
            return;

        // determine the sub-plot start and end times for the most
        // recent beam.
        //long lastTime = ((Beam) beams.get(beams.size() - 1)).date.getTime();


        deleteAllPlots();

        // determine the new frame start and end time

        // determine the plot values;
        //if (!manualDrawWidth) {
        //    DRAW_WIDTH = (int) ((3f * frameTimeExtent) / IMAGE_WIDTH /
        // SUB_IMAGES);
        //}

        //        imageEnd = lastTime;
        //        while (imageEnd > frameStart ) {
        //            imageEnd -= frameTimeExtent / SUB_IMAGES;
        //        }
        //        if ( imageEnd <= frameStart ) {
        //            imageEnd += frameTimeExtent / SUB_IMAGES;
        //        }
        //        long imageStart = imageEnd - frameTimeExtent / SUB_IMAGES;
        //
        //        //frameEnd = imageEnd;
        //        //frameStart = frameEnd - frameTimeExtent;
        //
        //System.out.println("create initial image "+new Date(imageStart)+"
        // "+new Date(imageEnd));
        //        previousImage = currentImage;
        //        currentImage =
        //            new SubPlot(
        //                plotWhat,
        //                parent,
        //                IMAGE_WIDTH,
        //                IMAGE_HEIGHT,
        //                FREQ_NOISE_IMAGE_HEIGHT,
        //                startRange,
        //                endRange,
        //                imageStart,
        //                imageEnd);
        //        allSubPlots.add(currentImage);

        imageEnd = frameStart;

        setAxisTransform();

        // draw the old beams in a thread.
        // draw all the old beams
        
        // reset the time of the earliest new data
        // this will allow the old data drawing thread to plot all the beams
        // up until any new data arrives.
        frameEarliestNewData = 0;
        
        DrawOldBeamsThread t = new DrawOldBeamsThread(oldBeamThreadSync, beams, this, new Date(frameStart));

        t.start();

    }

    public synchronized void deleteAllBeams() {

        beams.clear();
        oldBeamIndex = 0;

        freqListInitialized = false;

    }

    public synchronized void deleteAllPlots() {
        for (Iterator<SubPlot>  i = allSubPlots.iterator(); i.hasNext();) {
            SubPlot sp = i.next();
            sp.dispose();

            i.remove();
        }
        currentImage = null;
    }

    // get rid of dangling references which will prevent an instance from being
    // GC'd.
    public void clearAll() {
        deleteAllBeams();
        deleteAllPlots();

        parent = null;

        velocityRadio.removeActionListener(this);
        widthRadio.removeActionListener(this);
        powerRadio.removeActionListener(this);
        groundScatterCheck.removeActionListener(this);
        this.removeMouseListener(popupListener);
        popupListener = null;

//        Iterator i = menuItems.iterator();
//        JMenuItem item;
//        while (i.hasNext()) {
//            item = (JMenuItem) i.next();
        for(JMenuItem item:menuItems) {
            item.removeActionListener(this);
        }

        if (detailBox != null) {
            detailBox.plot = null;
            detailBox.removeAll();
            detailBox = null;
        }

        removeAll();

    }

    //    private void drawBeams() {
    //
    //        // if the drawing area limits are not initialized return
    //        if ( endRange < startRange )
    //            return;
    //
    //        System.out.println( "draw beams "+startDate+ " "+endDate );
    //        
    //        Rectangle2D outline = new Rectangle2D.Double();
    //        outline.setRect( startDate.getTime(), startRange,
    //                         endDate.getTime()-startDate.getTime(), endRange-startRange );
    //        System.out.println( outline );
    //        
    //        String what;
    //        Graphics2D g1, g2;
    //        for ( int index=0; index < plotTypes.length; index++ ) {
    //            what = plotTypes[index];
    //            
    //            g1 = plotGraphics[index];
    //            g2 = plotGraphics[index+plotTypes.length];
    //            
    //            // clear the images
    //            g1.setColor( backgroundColour );
    //            g1.fill( outline );
    //            g2.setColor( backgroundColour );
    //            g2.fill( outline );
    //        
    //            repaint();
    //            
    //             // draw the beams
    //            for ( Iterator i = beams.iterator(); i.hasNext(); ) {
    //                Beam beam = (Beam) i.next();
    //                
    //                if ( beam.getDate().before( startDate ) ) {
    //                    i.remove();
    //                    System.out.println( "beam removed: "+beam.getDate()+"
    // "+beam.fitData.radarParms.beamNumber );
    //                    continue;
    //                }
    //                
    //                //System.out.println( "plotting a beam "+beam.getDate() );
    //                beam.setWhat( what );
    //                    
    //                // fill the gate with the colour for the data
    //                beam.fillGates( g1, true );
    //                beam.fillGates( g2, false );
    //                
    //                repaint();
    //                
    //            }
    //        }
    //        
    //    }

    public void setPreferredSize(Dimension size) {
        preferredSize = new Dimension(size);
    }

    public Dimension getPreferredSize() {

        if (preferredSize == null || trackWidth) {
            return super.getPreferredSize();
        } else
            return preferredSize;
    }

    // Scrollable interface

    public Dimension getPreferredScrollableViewportSize() {
        if (trackWidth) {
            return scrollPane.getViewport().getViewSize();
        } else {
            return getPreferredSize();
        }
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 1;
    }

    public boolean getScrollableTracksViewportHeight() {
        return true;
    }

    public boolean getScrollableTracksViewportWidth() {
        return trackWidth;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 1;
    }
}
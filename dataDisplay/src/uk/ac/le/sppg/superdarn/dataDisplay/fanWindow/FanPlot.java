package uk.ac.le.sppg.superdarn.dataDisplay.fanWindow;

import uk.ac.le.sppg.coords.Geographic;
import uk.ac.le.sppg.coords.Site;
import uk.ac.le.sppg.coords.proj.AzimuthalEquidistant;
import uk.ac.le.sppg.coords.proj.LambertAzimuthal;
import uk.ac.le.sppg.coords.proj.None;
import uk.ac.le.sppg.coords.proj.Orthographic;
import uk.ac.le.sppg.coords.proj.Projection;
import uk.ac.le.sppg.coords.proj.ProjectionType;
import uk.ac.le.sppg.coords.proj.Stereographic;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSite;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import uk.ac.le.sppg.general.worldMap.World;
import uk.ac.le.sppg.general.worldMap.WorldMapLoadingException;
import uk.ac.le.sppg.general.worldMap.WorldProjection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.Timer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import uk.ac.le.sppg.superdarn.colour.Legend;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotParameter;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotSubParameter;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;

/**
 * @author Nigel Wade
 */
class FanPlot extends JPanel implements ActionListener {

    final static int ONE_SECOND = 1000;
    
    private static final long serialVersionUID = 0x5253505047000021L;
    
    private TreeMap<Integer,Beam> beams = new TreeMap<Integer,Beam>();
    
    private Projection proj = null;
    
    private Projection initialProjection = null;
    
    // Remember what is to be plotted
    PlotSubParameter what = null;
    
    // Remember the size so we can tell if we have been resized
    int width = -1;
    
    int height = -1;
    
    // The world map to be drawn.
    final World world;
    private WorldProjection projectedWorld = null;
    
    // Colours for drawing the World
    private Color landColour = new Color(242, 255, 204);
    
    private Color coastlineColour = new Color(150, 150, 150);
    
    private Color seaColour = new Color(204, 214, 255);
    
    private Color gridlineColour = new Color(175, 175, 175);
    
    private Color rangecellColour = new Color(120, 120, 120);
    
    private Color lakeColour = seaColour;
    
    // Control which bits of the World are drawn
    private boolean plotContinents = true;
    
    private boolean plotCoastlines = true;
    
    private boolean plotGridlines = true;
    
    private boolean plotRangecells = false;
        
    private JCheckBoxMenuItem groundScatterCheck;
    
    // Background popup menu.
    // Allows control of which bits of the World are plotted
    private JCheckBoxMenuItem continentCheck;
    
    private JCheckBoxMenuItem coastlineCheck;
    
    private JCheckBoxMenuItem gridCheck;
    
    int rangecellIncrement = -1;
    
    private FanWindow parent;
    
    private BufferedImage worldImage = null;
    
    private BufferedImage coastlineImage = null;
    
    private BufferedImage gridlineImage = null;
    
    private AffineTransform imageTransform = null;
    
    // the actual popup menu
    JPopupMenu popup;
    
    JRadioButtonMenuItem velocityRadio;
    
    JRadioButtonMenuItem powerRadio;
    
    JRadioButtonMenuItem widthRadio;
    
    StereoViewpoint viewpoint = null;
        
    
    // Frame to hold the panel to allow the viewpoint corners to be changed
    JFrame cornersFrame = null;
    
    JSpinner lowerLatSpinner;
    
    JSpinner lowerLonSpinner;
    
    JSpinner upperLatSpinner;
    
    JSpinner upperLonSpinner;
    
    JSlider noneScaleSlider;
    
    private DetailBox detailBox;
    
    Legend legend;
    
    Short CPID = null;
    
    DecimalFormat twoDecimal = new DecimalFormat("#.##");
    Timer newDataTimer;
    
    
    
    private void init(Projection proj, PlotSubParameter what)
    throws NoSuchFieldException, InterruptedException {
        
        this.what = what;
                
        this.proj = proj;
        
        this.initialProjection = proj.copy();
        
        projectedWorld = world.getProjection(this.proj, 4);
//System.out.println("world: "+world);
//System.out.println("world projection:"+projectedWorld);
//System.out.println("fan plot centre: "+proj.getCentre());
        
        addMenu();
        
        detailBox = new DetailBox("Details", this);

        //newDataTimer = new Timer(30*ONE_SECOND, new ActionListener() {
        //    public void actionPerformed(ActionEvent evt) {
        //        purgeOldBeams();
        //    }
        //});

    }
    
    public FanPlot(Geographic lowerLeft, Geographic upperRight, World world,
            PlotSubParameter what, FanWindow parent)
            throws NoSuchFieldException, InterruptedException {
        
        this.parent = parent;
        this.world = world;
        init(new None(lowerLeft, upperRight), what);
        
    }
    
    public FanPlot(Projection proj, World world, PlotSubParameter what,
            FanWindow parent) 
            throws NoSuchFieldException, InterruptedException {
        
        this.parent = parent;
        this.world = world;
        
        init(proj, what);
        
    }
    
    /*private void purgeOldBeams() {
        Date curDate = new Date();

        synchronized (beams) {
            Collection bc = beams.values();
            for (Iterator i = bc.iterator(); i.hasNext();) {
                Beam aBeam = (Beam) i.next();
                if (aBeam.date.getTime() < curDate.getTime() - 120*ONE_SECOND) {
                    i.remove();
                }
            }
        }
        repaint();
        newDataTimer.restart();
    }*/

    // the background popup menu allows varioius parts of the plot
    // to be toggled on and off.
    private void addMenu() {
        
        popup = new JPopupMenu();
        
        JMenu submenu1 = new JMenu("Select data");
        JMenu submenu2 = new JMenu("Plot details");
        JMenu submenu3 = new JMenu("Projection");
        
        ButtonGroup b = new ButtonGroup();
        
        velocityRadio = new JRadioButtonMenuItem("velocity");
        velocityRadio.addActionListener(this);
        if (what.type == PlotParameter.velocity) {
            velocityRadio.setSelected(true);
        }
        b.add(velocityRadio);
        submenu1.add(velocityRadio);
        
        widthRadio = new JRadioButtonMenuItem("spectral width");
        widthRadio.addActionListener(this);
        widthRadio.setActionCommand("width");
        if (what.type == PlotParameter.width) {
            widthRadio.setSelected(true);
        }
        b.add(widthRadio);
        submenu1.add(widthRadio);
        
        powerRadio = new JRadioButtonMenuItem("lamba power");
        powerRadio.setActionCommand("power");
        powerRadio.addActionListener(this);
        if (what.type == PlotParameter.power) {
            powerRadio.setSelected(true);
        }
        b.add(powerRadio);
        submenu1.add(powerRadio);
        
        submenu1.add(new JSeparator());
        
        // toggle for ground scatter display
        groundScatterCheck = new JCheckBoxMenuItem("ground scatter");
        groundScatterCheck.setState(what.gs);
        groundScatterCheck.addActionListener(this);
        submenu1.add(groundScatterCheck);
        
        popup.add(submenu1);
        
        // toggle for the continents i.e. is land filled
        continentCheck = new JCheckBoxMenuItem("continents");
        continentCheck.setState(plotContinents);
        continentCheck.addActionListener(this);
        submenu2.add(continentCheck);
        
        // toggle for the coastlines
        coastlineCheck = new JCheckBoxMenuItem("coastlines");
        coastlineCheck.setState(plotCoastlines);
        coastlineCheck.addActionListener(this);
        submenu2.add(coastlineCheck);
        
        // toggle for the meridians/parallels
        gridCheck = new JCheckBoxMenuItem("gridlines");
        gridCheck.setState(plotGridlines);
        gridCheck.addActionListener(this);
        submenu2.add(gridCheck);
        
        // menu for the range cell boundaries
        JMenu cellMenu = new JMenu("rangecells");
        ButtonGroup bg = new ButtonGroup();
        JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem("Off");
        rbmi.addActionListener(this);
        rbmi.setActionCommand("cells off");
        rbmi.setSelected(true);
        bg.add(rbmi);
        cellMenu.add(rbmi);
        
        rbmi = new JRadioButtonMenuItem("5");
        rbmi.addActionListener(this);
        rbmi.setActionCommand("5 cells");
        bg.add(rbmi);
        cellMenu.add(rbmi);
        
        rbmi = new JRadioButtonMenuItem("10");
        rbmi.addActionListener(this);
        rbmi.setActionCommand("10 cells");
        bg.add(rbmi);
        cellMenu.add(rbmi);
        
        rbmi = new JRadioButtonMenuItem("15");
        rbmi.addActionListener(this);
        rbmi.setActionCommand("15 cells");
        bg.add(rbmi);
        cellMenu.add(rbmi);
        
        submenu2.add(new JSeparator());
        submenu2.add(cellMenu);
        
        popup.add(submenu2);
        
        ButtonGroup b2 = new ButtonGroup();
        for(ProjectionType p : ProjectionType.values()) {
            JRadioButtonMenuItem radio = new JRadioButtonMenuItem(p.name());
            radio.addActionListener(this);
            if (proj.getType() == p ) {
                radio.setSelected(true);
            }
            b2.add(radio);
            submenu3.add(radio);
        }
        
        popup.add(submenu3);
        
        popup.add(new JSeparator());
        
        JMenuItem viewpointMenu = new JMenuItem("Viewpoint...");
        viewpointMenu.addActionListener(this);
        popup.add(viewpointMenu);
        
        //Add listener to components that can bring up popup menus.
        this.addMouseListener(new PopupListener());
    }
    
    private void createCornersPanel() {
        
        cornersFrame = new JFrame("Viewpoint");
        
        JPanel cornersPanel = new JPanel();
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        cornersPanel.setLayout(gbLayout);
        
        // lat and lon are controlled by spinners which allow
        // the latitude to be set in 0.5 degree increments and longitude in 1
        // deg increments.
        Geographic lowerLeft = proj.getLowerLeft();
        Geographic upperRight = proj.getUpperRight();
        
        SpinnerNumberModel lowerLatSpinModel = new SpinnerNumberModel(
                lowerLeft.latitude, -90.0, 90.0, 0.5);
        SpinnerNumberModel lowerLonSpinModel = new SpinnerNumberModel(
                (int) lowerLeft.longitude, -180, 180, 1);
        SpinnerNumberModel upperLatSpinModel = new SpinnerNumberModel(
                upperRight.latitude, -90.0, 90.0, 0.5);
        SpinnerNumberModel upperLonSpinModel = new SpinnerNumberModel(
                (int) upperRight.longitude, -180, 180, 1);
        
        JLabel l1 = new JLabel("Lower left:");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        c.insets = new Insets(20, 0, 0, 0);
        gbLayout.setConstraints(l1, c);
        cornersPanel.add(l1);
        
        JLabel l2 = new JLabel("Lat: ");
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(0, 0, 0, 0);
        gbLayout.setConstraints(l2, c);
        cornersPanel.add(l2);
        
        // the change listener will be called if the spinner is activated
        // the listener will remap the World
        SpinnerChanged spinListener = new SpinnerChanged();
        
        lowerLatSpinner = new JSpinner(lowerLatSpinModel);
        lowerLatSpinner.addChangeListener(spinListener);
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        gbLayout.setConstraints(lowerLatSpinner, c);
        cornersPanel.add(lowerLatSpinner);
        
        JLabel l3 = new JLabel("Lon: ");
        c.gridx = 3;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        gbLayout.setConstraints(l3, c);
        cornersPanel.add(l3);
        
        lowerLonSpinner = new JSpinner(lowerLonSpinModel);
        lowerLonSpinner.addChangeListener(spinListener);
        c.gridx = 4;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        gbLayout.setConstraints(lowerLonSpinner, c);
        cornersPanel.add(lowerLonSpinner);
        
        JLabel l4 = new JLabel("Upper right:");
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 4;
        c.insets = new Insets(20, 0, 0, 0);
        gbLayout.setConstraints(l4, c);
        cornersPanel.add(l4);
        
        JLabel l5 = new JLabel("Lat: ");
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 3;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(0, 0, 0, 0);
        gbLayout.setConstraints(l5, c);
        cornersPanel.add(l5);
        
        upperLatSpinner = new JSpinner(upperLatSpinModel);
        upperLatSpinner.addChangeListener(spinListener);
        c.gridx = 1;
        c.gridy = 3;
        c.anchor = GridBagConstraints.WEST;
        gbLayout.setConstraints(upperLatSpinner, c);
        cornersPanel.add(upperLatSpinner);
        
        JLabel l6 = new JLabel("Lon: ");
        c.gridx = 3;
        c.gridy = 3;
        c.anchor = GridBagConstraints.EAST;
        gbLayout.setConstraints(l6, c);
        cornersPanel.add(l6);
        
        upperLonSpinner = new JSpinner(upperLonSpinModel);
        upperLonSpinner.addChangeListener(spinListener);
        c.gridx = 4;
        c.gridy = 3;
        c.anchor = GridBagConstraints.WEST;
        gbLayout.setConstraints(upperLonSpinner, c);
        cornersPanel.add(upperLonSpinner);
        
        // the scale (which zooms in and out) is controlled by a slider.
        // the range is pretty arbitrary and is set by trial and error to
        // appropriate values.
        JLabel l7 = new JLabel("Scale: ");
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(20, 0, 0, 0);
        gbLayout.setConstraints(l7, c);
        cornersPanel.add(l7);
        
        noneScaleSlider = new JSlider(1, 1000, (int) (proj.getScale() * 100));
        noneScaleSlider.addChangeListener(new SliderChanged());
        noneScaleSlider.setOrientation(JSlider.HORIZONTAL);
        noneScaleSlider.setToolTipText(String.valueOf(proj.getScale()));
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        //c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        gbLayout.setConstraints(noneScaleSlider, c);
        
        Dimension sd = noneScaleSlider.getPreferredSize();
        
        sd.setSize(1, sd.height);
        noneScaleSlider.setPreferredSize(sd);
        
        cornersPanel.add(noneScaleSlider);
        
        cornersFrame.setContentPane(cornersPanel);
        cornersFrame.pack();
        
    }
    
    public Legend getLegend() {
        return parent.getLegend();
    }
    
    // called by the legend if the limits of the legend are changed
    public synchronized void setLimits(double min, double max) {
                
        for(Beam beam:beams.values()) {
            beam.mapData(min,max);
        }
        
        repaint();
    }
    
    // method to pass new data to a beam
    public synchronized void newData(FitacfData data) {

        synchronized (beams) {
            Collection bc = beams.values();
            for (Iterator i = bc.iterator(); i.hasNext();) {
                Beam aBeam = (Beam) i.next();
                if (data.radarParms.scanFlag != 0 &&
                        aBeam.site.getStationId() == data.radarParms.stationId)
                {

                    // the flag in the Beam is set when it receives new data.
                    // if it's not set then this beam received no new data this
                    // scan,
                    // so delete it.
                    if (!aBeam.getDataSet()) {
                        i.remove();
                    } else {
                        // reset the flag on remaining beams so we pick up on
                        // this scan.
                        aBeam.resetDataFlag();
                    }
                    repaint();
                }
            }
        }
        Beam beam = null;
        synchronized (beams) {
            Collection bc = beams.values();
            for (Iterator i = bc.iterator(); i.hasNext();) {
                Beam aBeam = (Beam) i.next();
                if (aBeam.site.getStationId() == data.radarParms.stationId &&
                        aBeam.beamNumber == data.radarParms.beamNumber)
                {
                    beam = aBeam;
                }
            }
        }
        if (beam == null)
        {
            // get the site from the SiteList list
            SuperDarnSite site = SuperDarnSiteList.getList().getById(data.radarParms.stationId);

            // can't do anything if we don't know about the site!
            if (site == null)
                return;

            // if it's not a SuperDarn site why has it provided fit data?
            if (!site.isSuperDarnSite())
                return;

            // create a new beam
            // we need to get the az, el and beam width
            // for Spear, this is in the Site object,

            // for SuperDARN sites we need to do it differently.

            if (site.getName().equals("Spear")) {
                double beamWidth = site.getBeamSeparation();

                // calculate startRange in km.
                // firstRangeDistance is in km
                double startRange = data.radarParms.firstRangeDistance;

                // rangeSeparation is in km, convert to m.
                double rangeIncrement = data.radarParms.rangeSeparation;

                // I'm assuming here the az and el are packed into the
                // upper and lower bytes of the beamNumber.
                double az = data.radarParms.beamNumber & 0xf;
                double el = (data.radarParms.beamNumber >>> 8) & 0xf;

                beam = new Beam(site, data.radarParms.beamNumber, az, el,
                        beamWidth, startRange, rangeIncrement, 16, 
                        data.radarParms.numberOfRanges, what);
            } else {
                SuperDarnSite sdSite = (SuperDarnSite) site;
                beam = new Beam(sdSite, data.radarParms.beamNumber,
                        data.radarParms.firstRangeDistance,
                        data.radarParms.rangeSeparation,
                        data.radarParms.rxRiseTime,
                        data.radarParms.numberOfRanges, what);
            }

            beam.setProjection(this.proj);

            beam.strokeGates(this.rangecellIncrement);

            beams.put(new Integer(data.radarParms.stationId*100+beam.beamNumber), beam);
        }

        if (legend == null) {
            legend = parent.getLegend();
        }
        // pass the new data to the relevent beam and plot it.
        beam.setData(data, legend.getMin(), legend.getMax());

        repaint();

        /*


        // if the CPID changes then clear the beams.
        if (CPID == null
                || data.radarParms.controlProgramId != CPID.shortValue()) {
            clearBeams();
            CPID = new Short(data.radarParms.controlProgramId);
        }

        // if it's the end of the scan, check which beams received new data.
        if (data.radarParms.scanFlag != 0) {
            synchronized (beams) {
                Collection bc = beams.values();
                for (Iterator i = bc.iterator(); i.hasNext();) {
                    Beam beam = (Beam) i.next();

                    if (beam.site.getStationId() != data.radarParms.stationId)
                        continue;

                    // the flag in the Beam is set when it receives new data.
                    // if it's not set then this beam received no new data this
                    // scan,
                    // so delete it.
                    if (!beam.getDataSet()) {
                        i.remove();
                    } else {
                        // reset the flag on remaining beams so we pick up on
                        // this scan.
                        beam.resetDataFlag();
                    }
                    repaint();
                }
            }
        }
        
        // need a Number object as thats what the beams Map contains
        Integer beamNumber = new Integer(data.radarParms.beamNumber);



        
        // if this beam number isn't in the map, create a new Beam object
        if (!beams.containsKey(beamNumber)) {
            
            // get the site from the SiteList list
            SuperDarnSite site = SuperDarnSiteList.getList().getById(data.radarParms.stationId);
            
            // can't do anything if we don't know about the site!
            if (site == null)
                return;
            
            // if it's not a SuperDarn site why has it provided fit data?
            if (!site.isSuperDarnSite())
                return;
            
            // create a new beam
            // we need to get the az, el and beam width
            // for Spear, this is in the Site object,
            
            // for SuperDARN sites we need to do it differently.
            
            if (site.getName().equals("Spear")) {
                double beamWidth = site.getBeamSeparation();
                
                // calculate startRange in km.
                // firstRangeDistance is in km
                double startRange = data.radarParms.firstRangeDistance;
                
                // rangeSeparation is in km, convert to m.
                double rangeIncrement = data.radarParms.rangeSeparation;
                
                // I'm assuming here the az and el are packed into the
                // upper and lower bytes of the beamNumber.
                double az = data.radarParms.beamNumber & 0xf;
                double el = (data.radarParms.beamNumber >>> 8) & 0xf;
                
                beam = new Beam(site, data.radarParms.beamNumber, az, el,
                        beamWidth, startRange, rangeIncrement,
                        data.radarParms.numberOfRanges, what);
            } else {
                SuperDarnSite sdSite = (SuperDarnSite) site;
                beam = new Beam(sdSite, data.radarParms.beamNumber,
                        data.radarParms.firstRangeDistance,
                        data.radarParms.rangeSeparation,
                        data.radarParms.rxRiseTime,
                        data.radarParms.numberOfRanges, what);
            }
            
            beam.setProjection(this.proj);
            
            beam.strokeGates(this.rangecellIncrement);
            
            beams.put(beamNumber, beam);
        } else {
            beam = (Beam) beams.get(beamNumber);
        }
        
        if (legend == null) {
            legend = parent.getLegend();
        }
        // pass the new data to the relevent beam and plot it.
        beam.setData(data, legend.getMin(), legend.getMax());
        
        repaint();
         *
         */
    }
    
    public void clearBeams() {
        synchronized (beams) {
            beams.clear();
        }
        
        repaint();
    }
    
    public synchronized void setProjection(Projection proj)
    throws WorldMapLoadingException {
//System.out.println("fan plot new centre: "+proj.getCentre());
//Thread.dumpStack();
        this.proj = proj;
        
        Collection bc = beams.values();
        for (Iterator i = bc.iterator(); i.hasNext();) {
            Beam beam = (Beam) i.next();
            beam.setProjection(proj);
        }
        
        clearImage(worldImage);
        clearImage(coastlineImage);
        clearImage(gridlineImage);
        
        worldImage = null;
        coastlineImage = null;
        gridlineImage = null;
        
        try {
            projectedWorld = world.getProjection(proj,4);
        }
        catch(InterruptedException e) {
            throw new WorldMapLoadingException("Loading of world map was interrupted");
        }
    }
    
    public synchronized void setWhat(PlotSubParameter what) {
        
        this.what = what;
                
        legend = parent.getLegend();
        
        switch( what.type ) {
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
        
        Collection bc = beams.values();
        for (Iterator i = bc.iterator(); i.hasNext();) {
            Beam beam = (Beam) i.next();
            beam.setWhat(what, legend.getMin(), legend.getMax());
        }
    }
    
    public void gridOn() {
        plotGridlines = true;
        gridCheck.setState(plotGridlines);
    }
    
    public void gridOff() {
        plotGridlines = false;
        gridCheck.setState(plotGridlines);
    }
    
    public void coastlineOn() {
        plotCoastlines = true;
        coastlineCheck.setState(plotCoastlines);
    }
    
    public void coastlineOff() {
        plotCoastlines = false;
        coastlineCheck.setState(plotCoastlines);
    }
    
    public void continentsOn() {
        plotContinents = true;
        continentCheck.setState(plotContinents);
    }
    
    public void continentsOff() {
        plotContinents = false;
        continentCheck.setState(plotContinents);
    }
    
    public void groundscatterOff() {
        what = new PlotSubParameter(what.type, false);
        groundScatterCheck.setState(what.gs);
    }
    
    public void groundscatterOn() {
        what = new PlotSubParameter(what.type, true);
        groundScatterCheck.setState(what.gs);
    }
    
    public void setCoastColour(Color c) {
        this.coastlineColour = c;
    }
    
    public void setSeaColour(Color c) {
        this.seaColour = c;
    }
    
    public void setLandColour(Color c) {
        this.landColour = c;
    }
    
    public void setLakeColour(Color c) {
        this.lakeColour = c;
    }
    
    public void setGridColour(Color c) {
        this.gridlineColour = c;
    }
    
    public void setCellColour(Color c) {
        this.rangecellColour = c;
    }
    
    public void setScale(double newScale) {
        proj.setScale(newScale);
        projectAndDraw();
    }
    
    public double getScale() {
        return proj.getScale();
    }
    
    public void setCentre(Geographic newCentre) {
        proj.setCentre(newCentre);
        projectAndDraw();
    }
    
    public void setCenteLatitude(double latitude) {
        Geographic newCentre = new Geographic(latitude,
                proj.getCentre().longitude, 0.0);
        setCentre(newCentre);
    }
    
    public void setCenteLongitude(double longitude) {
        Geographic newCentre = new Geographic(proj.getCentre().latitude,
                longitude, 0.0);
        setCentre(newCentre);
    }
    
    @Override
    public synchronized void paintComponent(Graphics g) {


        String[] selectedRadars = parent.getSelectedRadars();

        Graphics2D g2 = (Graphics2D) g;
        
        super.setBackground(seaColour);
        super.paintComponent(g); //clears the background
        
        if (legend == null) {
            legend = parent.getLegend();
        }
        
        // calculate the size of the drawing area.
        Insets insets = getInsets();
        int currentWidth = getWidth() - insets.left - insets.right;
        int currentHeight = getHeight() - insets.top - insets.bottom;
        
        // if the drawing area has changed size rescale the plot
        boolean replotGates = false;
        
        if (currentWidth != width || currentHeight != height
                || imageTransform == null) {
            this.width = currentWidth;
            this.height = currentHeight;
            
            // need to create a new images of the World components
            clearImage(worldImage);
            clearImage(coastlineImage);
            clearImage(gridlineImage);
            worldImage = null;
            coastlineImage = null;
            gridlineImage = null;
            
            //System.out.println( "plot size: "+this.getSize() );
            
            //proj.setScale( proj.getScale(), currentHeight, currentWidth );
            
            // need to replot all the gates
            //replotGates = true;
            
            // new AffineTransform to flip vertical axis
            imageTransform = new AffineTransform(1., 0., 0., -1., 0.,
                    (double) currentHeight);
            
            //imageTransform.translate(insets.left, insets.bottom);
            
            // create a transform to flip the vertical axis
            //AffineTransform transform = new AffineTransform(
            // 1.,0.,0.,-1.,0.,(double) ylen );
            
            // scale to the plot size.
            //transform.scale( xlen, ylen );
            
            // scale to the axis ranges.
            imageTransform.scale(currentWidth / 2.0, currentHeight / 2.0);
            // translate the origin
            imageTransform.translate(1.0, 1.0);
            
//			System.out.println("created transform "+imageTransform);
            
        }
        
        // create the image of the filled continents.
        if (plotContinents && worldImage == null) {
            
            // this creates a BufferedImage with alpha, basically it's
            // transparent.
            worldImage = new BufferedImage(currentWidth, currentHeight,
                    BufferedImage.TYPE_4BYTE_ABGR);
            
            Graphics2D worldGraphics = worldImage.createGraphics();
            
            worldGraphics.setTransform(imageTransform);
            
            if (!projectedWorld.fillAll(worldGraphics, landColour, lakeColour)) {
                clearImage(worldImage);
                worldImage = null;
            }
            
        }
        
        // create the image of the coastlines and lakes.
        if (plotCoastlines && coastlineImage == null) {
            coastlineImage = new BufferedImage(currentWidth, currentHeight,
                    BufferedImage.TYPE_4BYTE_ABGR);
            
            Graphics2D coastlineGraphics = coastlineImage.createGraphics();
            
            coastlineGraphics.setTransform(imageTransform);
            coastlineGraphics.setStroke(new BasicStroke(4.0f / currentWidth));
                if (!projectedWorld.drawCoastlines(coastlineGraphics, coastlineColour)
                    || !projectedWorld.drawLakes(coastlineGraphics, coastlineColour)) {
                        clearImage(coastlineImage);
                        coastlineImage = null;
                }
            
        }
        
        // create the image of the grid lines.
        if (plotGridlines && gridlineImage == null) {
            gridlineImage = new BufferedImage(currentWidth, currentHeight,
                    BufferedImage.TYPE_4BYTE_ABGR);
            
            Graphics2D gridlineGraphics = gridlineImage.createGraphics();
            
            gridlineGraphics.setTransform(imageTransform);
                gridlineGraphics.setStroke(new BasicStroke(2.0f / currentWidth));
                if (!projectedWorld.drawGridlines(gridlineGraphics, gridlineColour)) {
                    clearImage(gridlineImage);
                    gridlineImage = null;
                }
            
        }
        
        // plot the continents if they are required and there is a suitable
        // image
        // ( the image may not be drawn if this class is realized and painted
        // before
        //   the World is loaded and projected).
        if (plotContinents && worldImage != null)
            g2.drawImage(worldImage, 0, 0, this);
        //world.fillContinents( g2, landColour, lakeColour );

        // remove the previous scaling to draw the images unscaled.
        // draw the coastlines and the gridlines
        if (plotCoastlines && coastlineImage != null)
            g2.drawImage(coastlineImage, 0, 0, this);
        //world.drawCoastline( g2, coastlineColour );
        if (plotGridlines && gridlineImage != null)
            //world.drawGridlines( g2, gridlineColour );
            g2.drawImage(gridlineImage, 0, 0, this);
        
        AffineTransform saveTransform = g2.getTransform();
        
        if ( saveTransform.isIdentity() ) {
            g2.setTransform(imageTransform);
        } else {
            AffineTransform newTransform = new AffineTransform(saveTransform);
            newTransform.concatenate(imageTransform);
            g2.setTransform(newTransform);
        }
        
        g2.setStroke(new BasicStroke(1.0f / currentWidth));
        
        // draw the filled gates.
        synchronized (beams) {
            Collection bc = beams.values();
            
            for (Iterator i = bc.iterator(); i.hasNext();) {
                Beam beam = (Beam) i.next();

                boolean drawBeam = parent.getSingleRadar();
                for (int j=0; j<selectedRadars.length; j++) {
                    //if (beam.site.getCompactName().equals(selectedRadars[j])) {
                    if (beam.site.getShortName().toUpperCase().equals(selectedRadars[j])) {
                        drawBeam = true;
                        break;
                    }
                }
                if (drawBeam) {
                    if (replotGates)
                        beam.setProjection(proj);

                    // fill the gate with the colour for the data
                    beam.fillGates(g2, legend.getColourScale(), what.gs);

                    beam.drawEdge(g2, rangecellColour);

                    // maybe draw the range cell outline
                    if (plotRangecells)
                        beam.drawGates(g2, rangecellColour);
                }
            }
        }
        
        // remove the previous scaling to draw the images unscaled.
        
        g2.setTransform(saveTransform);
        
        // draw the coastlines and the gridlines
        //if (plotCoastlines && coastlineImage != null)
        //    g2.drawImage(coastlineImage, 0, 0, this);
        //world.drawCoastline( g2, coastlineColour );
        //if (plotGridlines && gridlineImage != null)
            //world.drawGridlines( g2, gridlineColour );
        //    g2.drawImage(gridlineImage, 0, 0, this);
        
    }
    
    public TreeMap getBeams() {
        return beams;
    }
    
    // listener for right click to post the popup menu.
    class PopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            
            // a double click on the window will bring up the
            // detail box.
            // when the detail box is displayed a single click
            // will search for the data at the location clicked on.
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (e.getClickCount() == 2) {
                    detailBox.setVisible(true);
                }
                
                if (detailBox.isVisible()) {
                    detailBox.toFront();
                    detailBox.showDetails(e.getX(), e.getY());
                }
                
            } else {
                maybeShowPopup(e);
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int mouseX = e.getX();
                int mouseY = e.getY();
                popup.show(e.getComponent(), mouseX, mouseY);
            }
        }
    }
    
    // listener for checkbox activity in the popup menu
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        try {
            if (o == continentCheck)
                plotContinents = ((JCheckBoxMenuItem) o).getState();
            else if (o == coastlineCheck)
                plotCoastlines = ((JCheckBoxMenuItem) o).getState();
            else if (o == gridCheck)
                plotGridlines = ((JCheckBoxMenuItem) o).getState();
            else if (o == groundScatterCheck)
                what = new PlotSubParameter(what.type, ((JCheckBoxMenuItem) o).getState());
            else if (e.getActionCommand().equals("Viewpoint...")) {
                String className = proj.getClass().getName();
                if (className.endsWith("proj.None")) {
                    if (cornersFrame == null) {
                        createCornersPanel();
                    }
                    cornersFrame.setVisible(true);
                } else {
                    if (viewpoint == null) {
                        viewpoint = new StereoViewpoint(parent, false, this);
                    }
                    Geographic centre = proj.getCentre();
                    viewpoint.setLatValue(new Double(centre.latitude));
                    viewpoint.setLonValue(new Double(centre.longitude));
                    viewpoint.setScaleValue(new Double(proj.getScale()));
                    viewpoint.setVisible(true);
                }
            }
            
            else {
                String command = e.getActionCommand();
                String currentProj = proj.getClass().getName();
                
                rangecellIncrement = -1;
                
                if (command.equals("cells off")) {
                    plotRangecells = false;
                } else if (command.equals("5 cells")) {
                    plotRangecells = true;
                    rangecellIncrement = 5;
                } else if (command.equals("10 cells")) {
                    plotRangecells = true;
                    rangecellIncrement = 10;
                } else if (command.equals("15 cells")) {
                    plotRangecells = true;
                    rangecellIncrement = 15;
                }
                
                else if (command.equals("None")) {
                    if (!currentProj.endsWith("None")) {
                        Geographic lowerLeft = proj
                                .pointToGeo(new Point2D.Double(-1.0, -1.0));
                        Geographic upperRight = proj
                                .pointToGeo(new Point2D.Double(1.0, 1.0));
                        Projection p = new None(lowerLeft, upperRight);
                        setProjection(p);
                    }
                } else if (command.equals("Stereographic")) {
                    if (!currentProj.endsWith("Stereographic")) {
                        Geographic centre = proj.getCentre();
                        double scale = proj.getScale();
                        Projection p = new Stereographic(scale, centre);
                        setProjection(p);
                    }
                } else if (command.equals("Orthographic")) {
                    if (!currentProj.endsWith("Orthographic")) {
                        Geographic centre = proj.getCentre();
                        double scale = proj.getScale();
                        Projection p = new Orthographic(scale, centre);
                        setProjection(p);
                    }
                } else if (command.equals("Azimuthal Equidistant")) {
                    if (!currentProj.endsWith("AzimuthalEquidistant")) {
                        Geographic centre = proj.getCentre();
                        double scale = proj.getScale();
                        Projection p = new AzimuthalEquidistant(scale, centre);
                        setProjection(p);
                    }
                } else if (command.equals("Lambert Azimuthal")) {
                    if (!currentProj.endsWith("LambertAzimuthal")) {
                        Geographic centre = proj.getCentre();
                        double scale = proj.getScale();
                        Projection p = new LambertAzimuthal(scale, centre);
                        setProjection(p);
                    }
                }
                
                else {
                    try {
                        parent.setWhat(new PlotSubParameter(Enum.valueOf(PlotParameter.class, command), what.gs));
                    } catch (NoSuchFieldException ex) {
                        System.out.println("class implimentation error in menu."
                                + ex.getMessage());
                    }
                }
                
                synchronized (beams) {
                    Collection bc = beams.values();
                    
                    for (Iterator i = bc.iterator(); i.hasNext();) {
                        Beam beam = (Beam) i.next();
                        
                        beam.strokeGates(rangecellIncrement);
                    }
                }
            }
            
        } catch (WorldMapLoadingException err) {
            System.err
                    .println("FanPlot: attempt to project points whilst World map loading");
            err.printStackTrace();
        }
        repaint();
    }
    
    private class SpinnerChanged implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSpinner source = (JSpinner) e.getSource();
            double newLat;
            double newLon;
            
            if (source == lowerLatSpinner) {
                newLat = ((Double) source.getValue()).doubleValue();
                Geographic upper = proj.getUpperRight();
                Geographic lower = proj.getLowerLeft();
                ((None) proj).setGeo(new Geographic(newLat, lower.longitude,
                        0.0), upper);
            } else if (source == lowerLonSpinner) {
                newLon = ((Double) source.getValue()).doubleValue();
                Geographic upper = proj.getUpperRight();
                Geographic lower = proj.getLowerLeft();
                ((None) proj).setGeo(
                        new Geographic(lower.latitude, newLon, 0.0), upper);
            } else if (source == upperLatSpinner) {
                newLat = ((Double) source.getValue()).doubleValue();
                Geographic upper = proj.getUpperRight();
                Geographic lower = proj.getLowerLeft();
                ((None) proj).setGeo(lower, new Geographic(newLat,
                        upper.longitude, 0.0));
            } else if (source == upperLonSpinner) {
                newLon = ((Double) source.getValue()).doubleValue();
                Geographic upper = proj.getUpperRight();
                Geographic lower = proj.getLowerLeft();
                ((None) proj).setGeo(lower, new Geographic(upper.latitude,
                        newLon, 0.0));
            }
            
            try {
                setProjection(proj);
            } catch (WorldMapLoadingException err) {
                System.err
                        .println("FanPlot: attempt to project points whilst World map loading");
                err.printStackTrace();
            }
            //world.projectPoints( proj );
            repaint();
        }
    }
    
    private class SliderChanged implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            
            
            if (source == noneScaleSlider) {
                if (source.getValueIsAdjusting())
                    return;
                double newScale = source.getValue() / 100.0;
                //System.out.println( "scale set to " + newScale );
                proj.setScale(newScale);
                
                source.setToolTipText(String.valueOf(newScale));
            }
            
            try {
                setProjection(proj);
            } catch (WorldMapLoadingException err) {
                System.err
                        .println("FanPlot: attempt to project points whilst World map loading");
                err.printStackTrace();
            }
            //world.projectPoints( proj );
            repaint();
        }
    }
    
    private void clearImage(Image image) {
        if (image != null)
            image.getGraphics().dispose();
    }
    
    void moveCentre(double latIncrement, double lonIncrement) {
        Geographic centre = proj.getCentre();
        double newLat;
        double newLon;
        
        newLat = centre.latitude + latIncrement;
        newLon = centre.longitude + lonIncrement;
        
        if (newLat > 90.0) {
            newLat = 90 - newLat;
            newLon = +180.0;
        }
        
        newLon = newLon % 360.0;
        
        proj.setCentre(new Geographic(newLat, newLon, 0.0));
        projectAndDraw();
        
    }
    
    void moveNorthWest() {
        moveCentre(0.5, -0.5);
    }
    
    void moveNorth() {
        moveCentre(0.5, 0.0);
    }
    
    void moveNorthEast() {
        moveCentre(0.5, 0.5);
    }
    
    void moveWest() {
        moveCentre(0.0, -0.5);
    }
    
    void moveEast() {
        moveCentre(0.0, 0.5);
    }
    
    void moveSouthWest() {
        moveCentre(-0.5, -0.5);
    }
    
    void moveSouth() {
        moveCentre(-0.5, 0.0);
    }
    
    void moveSouthEast() {
        moveCentre(-0.5, 0.5);
    }
    
    void centreView() {
        
        Geographic centre = initialProjection.getCentre();
        
        synchronized (beams) {
            Beam beam = (Beam) beams.get(beams.lastKey());
            
            Site site = beam.site;
            if (site.isSuperDarnSite()) {
                centre = ((SuperDarnSite) site).siteCentre(
                        (int) beam.startRange, (int) beam.rangeIncrement,
                        beam.nGates);
            }
            
        }
        
        proj.setCentre(centre);
        //proj.setScale( initialProjection.getScale() );
        
        projectAndDraw();
    }
    
    void zoomIn() {
        double scale = proj.getScale() * 1.25;
        proj.setScale(scale);
        
        projectAndDraw();
        
    }
    
    void zoomOut() {
        double scale = proj.getScale() / 1.25;
        proj.setScale(scale);
        
        projectAndDraw();
    }
    
    void resetZoom() {
        proj.setCentre(initialProjection.getCentre());
        proj.setScale(initialProjection.getScale());
        
        projectAndDraw();
    }
    
    private void projectAndDraw() {
                    if (viewpoint == null) {
                        viewpoint = new StereoViewpoint(parent, false, this);
                    }
        viewpoint.setScaleValue(new Double(proj.getScale()));
        viewpoint.setLatValue(new Double(proj.getCentre().latitude));
        viewpoint.setLonValue(new Double(proj.getCentre().longitude));
        
        try {
            setProjection(proj);
        } catch (WorldMapLoadingException err) {
            System.err.println("FanPlot: attempt to project points whilst World map loading");
            err.printStackTrace();
        }
        //world.projectPoints( proj );
        repaint();
    }
    
}
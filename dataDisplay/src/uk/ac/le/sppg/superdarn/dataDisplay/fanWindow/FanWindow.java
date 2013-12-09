package uk.ac.le.sppg.superdarn.dataDisplay.fanWindow;

import uk.ac.le.sppg.coords.proj.Stereographic;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSite;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.TreeMap;
import java.net.URL;
import java.io.IOException;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

import uk.ac.le.sppg.coords.Geographic;
import uk.ac.le.sppg.coords.proj.Projection;
import uk.ac.le.sppg.general.worldMap.World;
import uk.ac.le.sppg.general.worldMap.WorldMapLoadingException;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import uk.ac.le.sppg.superdarn.colour.Legend;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotParameter;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotSubParameter;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotWindow;
import uk.ac.le.sppg.superdarn.dataDisplay.controlPanel.DataSource;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;
import uk.ac.le.sppg.superdarn.fitData.NewData;
import uk.ac.le.sppg.superdarn.dataServer.Radars;


/**
 * A class to create a plot window in a frame which will display SuperDARN
 * and SPEAR data as a "fan" plot. The data is drawn as beams superimposed
 * over a world map. The map and beams can be projected onto the plane
 * according to a specific {@link coords#Projection}.
 * It can display the velocity, lambda spectral width and lambda power
 * values either with or without ground scatter identification.
 * The values are colour coded according to a {@link Legend} which sets
 * the number of colours and the upper and lower limits.
 * <p>
 * The plot consitsts of a panel containing the fan plot itself,
 * a {@link Legend} and an outer area showing various information
 * from the data.
 * These fields show the time of the last beam plotted, the beam
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
 * A popup menu allows the plot to be controlled interactively.
 * Various aspects of the world map can be altered, the data type can be
 * selected and ground scatter can be turned on and off.
 * Also, the viewpoint can be selected by changing the scale of the projection
 * and the centre/corners.
 *
 * @author Nigel Wade
 */
public class FanWindow extends JFrame
    implements PlotWindow, Legend.Limits, NewData, WindowListener {
    
    private static final long serialVersionUID = 0x5253505047000016L;
    private final static String newline = "\n";
    private final static int ONE_SECOND = 1000;
    
    private JPanel fanPanel;
    
    private FanPlot plot; // the FanPlot which displays the data and the World
    private World world; // the World to be drawn in the FanPlot
    private static final Projection defaultProj = new Stereographic(3.5, new Geographic(0.0,0.0,0.0));
    private Projection proj; // the projection to be used on the FanPlot
    private Geographic lowerLeft; // used when Projection None is in operation
    private Geographic upperRight; //  this projection has lower left and upper right range
    private Geographic fanViewCentre = null;
            
    private JTextField dateField; // displays the date/time of the data in
    // the NORTH area of the frame.
    private FontMetrics metrics;
    // date/time format for the dateField
    // currently LONG format for locale GMT
    private SimpleDateFormat sDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'UT'");

    private JTextField treemapInfoField;
    private JTextArea beamField; // field to display the beam number
    private JTextArea freqField; // field to display the frequency
    private JTextArea noiseField; // field to display the noise level.
    private JTextArea CPIDField;
    private JTextArea siteNameField;
    private JTextArea naveField;
    private JLabel noNewData;
    
    private JTextField counterInfoField;
    private JTextField memoryInfoField;

    //private JButton addRadarButton;
    //private JComboBox radarCombo;
    //private JComboBox channelCombo;
    private JPanel radarChooserPanel;
    private JCheckBox[] radarChooserBoxes;
    private JPanel centerPanel;

    TreeMap<String, DataSource> dataSources = new TreeMap<String, DataSource>();

    Timer newDataTimer;
    
    // get the runtime to be able to call the
    // garbage collector
    private Runtime rt;
    // i am stupid, so instead of timing it, 
    // i'll call the gc every time 100 new beams
    // have been received
    private int beamCounter = 0;
    
    
    public interface Constants {
        public static final double MINVEL = -1000.0;
        public static final double MAXVEL = 1000.0;
        public static final double MINPOWER = 0.0;
        public static final double MAXPOWER = 30.0;
        public static final double MINWIDTH = 0.0;
        public static final double MAXWIDTH = 500.0;
    }
    
    static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    static final Dimension defaultSize = new Dimension(screenSize.width / 3, screenSize.width / 3);
    
    // Legend for the data
    // Legend for the data
    public Legend legend;
    Legend velocityLegend;
    Legend powerLegend;
    Legend widthLegend;
    
    JPanel legendPanel;
    
    // this is true when only one radar is displayed, false otherwise
    private boolean singleRadar;
    
    // strings used in title.
    private String siteString="";
//    private String whatString="";
    private PlotSubParameter what;
    
    private String channelString="";

    class SetNewDataLabel extends Thread {
        boolean ok;
        public SetNewDataLabel(boolean ok){
            this.ok = ok;
        }
        @Override
        public void run() {
            if ( ok ) {
                noNewData.setForeground(Color.DARK_GRAY);
                noNewData.setText("Data ok");
            }
            else {
                noNewData.setForeground(Color.RED);
                noNewData.setText("No new data received");
            }
        }
        
    }

    public FanWindow( Projection proj, World world, PlotParameter what, boolean gs, Dimension size, boolean singleRadar)
    throws NoSuchFieldException, InterruptedException {

        if ( proj == null )
            proj = defaultProj;
        else
            this.proj = proj;
        this.world = world;
        this.singleRadar = singleRadar;

        sDateFormat.setTimeZone( TimeZone.getTimeZone("UTC") );

        setSize( size );

        PlotSubParameter whatSub = new PlotSubParameter(what, gs);
        plot = new FanPlot( proj, world, whatSub, this );


        //plot.setPreferredSize( new Dimension( width, height ));
        createPanel( whatSub, singleRadar );
        //plot.setSize( new Dimension( width, height ));
        //plot.setMinimumSize( new Dimension( width, height ));
        //plot.invalidate();
        
        
        rt = Runtime.getRuntime();

    }
    
    // called by the legend if the limits of the legend are changed
    public void limitsChanged( double min, double max ) {
        
        plot.setLimits( min, max );
    }
    
    
    /**
     * Sets the parameter to be plotted by changing the
     * <code>Legend</code> to be used on the plot.
     * There are 3 <code>Legends</code> defined, one for each of the
     * 3 parameter types.
     * @param what
     * The parameter to be plotted.
     * Must be one of "velocity", "power" or "width".
     * @throws NoSuchFieldException
     * if <code>what</code> is not one of the allowed values.
     */
    public void setLegend( PlotSubParameter what )
    throws NoSuchFieldException {
        
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
        
        plot.setWhat( what );
        
        legendPanel.removeAll();
        legendPanel.add( legend );
        
        this.what = what;
        setTitle();
        
        plot.setWhat( what );
        
        this.validate();
    }
    
    /**
     * Gets the current <code>Legend</code> being used in the plot.
     * @return
     * the <code>Legend</code> currently in use.
     */
    public Legend getLegend() {
        return legend;
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
     * Removes all cached <code>Beams</code> in the plot.
     * <p>
     * This method can be used if the control program changes to
     * ensure there are no cached <code>Beams</code> left over from
     * the old control program.
     * <p>
     * The <code>FanPlot</code> should do this for itself.
     */
    public void clearBeams() {
        plot.clearBeams();
    }
    
    private void createPanel( PlotSubParameter what, boolean singleRadar )
    throws NoSuchFieldException {

        ArrayList radarList;
        URL baseURL;
        URL radarURL;
        Border loweredBevelBorder = BorderFactory.createLoweredBevelBorder();
        
        JPanel pane = (JPanel) this.getContentPane();
        fanPanel = new JPanel();
        fanPanel.setLayout( new BorderLayout() );
        
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );
        
        
        // a field to show the date/time at the top of the frame
        JPanel datePanel = new JPanel();
        datePanel.add( new JLabel( "Time: " ) );
        dateField = new JTextField();
        dateField.setEditable( false );
        dateField.setBorder( loweredBevelBorder );
        dateField.setBackground( Color.WHITE );
        dateField.setFocusable(false);
        datePanel.add( dateField );

        // a field to show the number of beams in memory at the top of the frame
        JPanel treemapInfoPanel = new JPanel();
        treemapInfoPanel.add( new JLabel( "  #Beams: " ) );
        treemapInfoField = new JTextField();
        treemapInfoField.setEditable( false );
        treemapInfoField.setColumns( 5 );
        treemapInfoField.setBorder( loweredBevelBorder );
        treemapInfoField.setBackground( Color.WHITE );
        treemapInfoField.setFocusable(false);
        treemapInfoPanel.add( treemapInfoField );
        
        // a field to show the amount of memory used at the top of the frame
        JPanel memoryInfoPanel = new JPanel();
        memoryInfoPanel.add( new JLabel( "  Counter: " ) );
        counterInfoField = new JTextField();
        counterInfoField.setEditable( false );
        counterInfoField.setColumns( 3 );
        counterInfoField.setBorder( loweredBevelBorder );
        counterInfoField.setBackground( Color.WHITE );
        counterInfoField.setFocusable(false);
        memoryInfoPanel.add( counterInfoField );
        memoryInfoPanel.add( new JLabel( "  Free Mem: " ) );
        memoryInfoField = new JTextField();
        memoryInfoField.setEditable( false );
        memoryInfoField.setColumns( 5 );
        memoryInfoField.setBorder( loweredBevelBorder );
        memoryInfoField.setBackground( Color.WHITE );
        memoryInfoField.setFocusable(false);
        memoryInfoPanel.add( memoryInfoField );
        
        upperPanel.add( dateField );
        upperPanel.add( treemapInfoPanel );
        upperPanel.add( memoryInfoPanel );

        // warning for no new data in specified time
        JPanel newDataPanel = new JPanel();
        noNewData = new JLabel("No new data received");
        //noNewData.setEnabled(false);
        noNewData.setForeground(Color.RED);
        newDataPanel.add(noNewData);
        
        newDataTimer = new Timer(30*ONE_SECOND, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setNewDataLabel(false);
            }    
        });
       
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
       
        topPanel.add(upperPanel, BorderLayout.WEST);
        topPanel.add(newDataPanel, BorderLayout.EAST);
        
        // fields at the bottom to display beam, frequency, noise level and attenuation
        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );

        siteNameField = new JTextArea();
        siteNameField.setEditable( false );
        siteNameField.setColumns( 20 );
        siteNameField.setRows( 5 );
        siteNameField.setBorder( loweredBevelBorder );
        siteNameField.setBackground( Color.WHITE );
        siteNameField.setFocusable(false);
        
        beamField = new JTextArea();
        beamField.setEditable( false );
        beamField.setColumns( 3 );
        beamField.setRows( 5 );
        beamField.setBorder( loweredBevelBorder );
        beamField.setBackground( Color.WHITE );
        beamField.setFocusable(false);
        
        freqField = new JTextArea();
        freqField.setEditable( false );
        freqField.setColumns( 6 );
        freqField.setRows( 5 );
        freqField.setBorder( loweredBevelBorder );
        freqField.setBackground( Color.WHITE );
        freqField.setFocusable(false);
        
        noiseField = new JTextArea();
        noiseField.setEditable( false );
        noiseField.setColumns( 4 );
        noiseField.setRows( 5 );
        noiseField.setBorder( loweredBevelBorder );
        noiseField.setBackground( Color.WHITE );
        noiseField.setFocusable(false);

        CPIDField = new JTextArea();
        CPIDField.setEditable( false );
        CPIDField.setColumns( 4 );
        CPIDField.setRows( 5 );
        CPIDField.setBorder( loweredBevelBorder );
        CPIDField.setBackground( Color.WHITE );
        CPIDField.setFocusable(false);

        naveField = new JTextArea();
        naveField.setEditable( false );
        naveField.setColumns( 4 );
        naveField.setRows( 5 );
        naveField.setBorder( loweredBevelBorder );
        naveField.setBackground( Color.WHITE );
        naveField.setFocusable(false);

        lowerPanel.add( new JLabel( "Site: " ) );
        lowerPanel.add( siteNameField );

        lowerPanel.add( new JLabel( "Beam: " ) );
        lowerPanel.add( beamField );
        
        lowerPanel.add( new JLabel( "Freq: " ) );
        lowerPanel.add( freqField );
        
        lowerPanel.add( new JLabel( "Noise: " ) );
        lowerPanel.add( noiseField );

        lowerPanel.add( new JLabel( "CPID: " ) );
        lowerPanel.add( CPIDField );

        lowerPanel.add( new JLabel( "Nave: " ) );
        lowerPanel.add( naveField );
        
        
        // a toolbox at the right of the frame
        // to allow the centre of the plot and the scale to be changed.
        
        // the data plot and legend in the centre of the frame


        // the center portion of fanPanel consists of
        // another panel, holding the components needed to add
        // other radars on the top and the actual map display

        // panel at the top with checkboxes to turn radars on an off
        // make checkboxes for all radars
        // but only turn those checkboxes on for which we have data
        radarChooserPanel = new JPanel();
        String[] incRadars = {"INV", "RKN", "KSR", "KOD", "PGR", "SAS", "KAP", "GBR", "STO",
            "ADW", "ADE", "CVW", "CVE", "FHW", "FHE", "BKS", "WAL", "AZW", "AZE"};
        radarChooserBoxes = new JCheckBox[incRadars.length];
        for (int i=0; i<incRadars.length; i++) {
            radarChooserBoxes[i] = new JCheckBox(incRadars[i], false);
            radarChooserBoxes[i].setEnabled(false);
        }
        if (!singleRadar) {
            radarChooserPanel.setLayout(new GridLayout(2, 5, 5, 5));
            // add checkboxes to panel
            // KOD
            JPanel panel00 = new JPanel();
            panel00.add(radarChooserBoxes[0]);
            panel00.add(radarChooserBoxes[1]);
            radarChooserPanel.add(panel00);
            // PGR
            JPanel panel01 = new JPanel();
            panel01.add(radarChooserBoxes[2]);
            panel01.add(radarChooserBoxes[3]);
            radarChooserPanel.add(panel01);
            // INV & SAS
            JPanel panel02 = new JPanel();
            panel02.add(radarChooserBoxes[4]);
            panel02.add(radarChooserBoxes[5]);
            radarChooserPanel.add(panel02);
            // KAP & GBR
            JPanel panel03 = new JPanel();
            panel03.add(radarChooserBoxes[6]);
            panel03.add(radarChooserBoxes[7]);
            radarChooserPanel.add(panel03);
            // STO
            JPanel panel04 = new JPanel();
            panel04.add(radarChooserBoxes[8]);
            radarChooserPanel.add(panel04);
            // ADW & ADE
            JPanel panel10 = new JPanel();
            panel10.add(radarChooserBoxes[9]);
            panel10.add(radarChooserBoxes[10]);
            radarChooserPanel.add(panel10);
            // CVW & CVE
            JPanel panel11 = new JPanel();
            panel11.add(radarChooserBoxes[11]);
            panel11.add(radarChooserBoxes[12]);
            radarChooserPanel.add(panel11);
            // FHW & FHE
            JPanel panel12 = new JPanel();
            panel12.add(radarChooserBoxes[13]);
            panel12.add(radarChooserBoxes[14]);
            radarChooserPanel.add(panel12);
            // BKS & WAL
            JPanel panel13 = new JPanel();
            panel13.add(radarChooserBoxes[15]);
            panel13.add(radarChooserBoxes[16]);
            radarChooserPanel.add(panel13);
            // AZW & AZE
            JPanel panel14 = new JPanel();
            panel14.add(radarChooserBoxes[17]);
            panel14.add(radarChooserBoxes[18]);
            radarChooserPanel.add(panel14);
            try {
                // Lookup the javax.jnlp.BasicService object
                BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
                baseURL = bs.getCodeBase();
                radarURL = new URL(baseURL, "radarList");
                radarList = Radars.getRadarList(radarURL);
                if (!singleRadar) {
                    for (int i = 0; i < radarList.size(); i++) {
                        SuperDarnSite sdSite = SuperDarnSiteList.getList().get(radarList.get(i).toString());
                        for (int j=0; j < incRadars.length; j++) {
                            if (radarChooserBoxes[j].getText().toUpperCase().compareTo(sdSite.getShortName().toUpperCase()) == 0) {
                                radarChooserBoxes[j].setEnabled(true);
                                radarChooserBoxes[j].setSelected(true);
                                radarChooserBoxes[j].addActionListener( new java.awt.event.ActionListener() {
                                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                                        plot.repaint();
                                    }
                                });
                            }
                        }
                    }
                }
            }
            catch(UnavailableServiceException ue) {
                System.err.println("This is a JNLP initiated Web service");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                        "IO Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
        // panel that holds everything
        centerPanel = new JPanel();
        centerPanel.setLayout( new BorderLayout() );


        // create panel for legend and zoomPanPanel
        // create legends
        velocityLegend = new Legend( false, Constants.MINVEL, Constants.MAXVEL, 256, this );
        powerLegend = new Legend( true, Constants.MINPOWER, Constants.MAXPOWER, 256, this );
        widthLegend = new Legend( true, Constants.MINWIDTH, Constants.MAXWIDTH, 256, this );
        legend = velocityLegend;

        legendPanel = new JPanel();
        legendPanel.setLayout( new BoxLayout( legendPanel, BoxLayout.X_AXIS ) );
        legendPanel.add( legend );

        centerPanel.add( topPanel, BorderLayout.NORTH);
        centerPanel.add( plot, BorderLayout.CENTER );
        centerPanel.add( legendPanel, BorderLayout.EAST );
        centerPanel.add( lowerPanel, BorderLayout.SOUTH );

        fanPanel.add( radarChooserPanel, BorderLayout.NORTH );
        fanPanel.add( centerPanel, BorderLayout.CENTER );

        pane.add( fanPanel );

        // pack the components
        this.setVisible(true);
        
        setLegend( what );
        
        // must do this after the GUI is pack'ed and therefore realized
        // otherwise there is no Graphics.
        metrics = this.getGraphics().getFontMetrics();
        int width = metrics.stringWidth( sDateFormat.format( new Date(System.currentTimeMillis())) );
        
        Dimension d = dateField.getMinimumSize();
        Insets i = dateField.getInsets();
        d.width = width + i.left + i.right;
        dateField.setMinimumSize( d );
        dateField.setPreferredSize( d );
        dateField.invalidate();
        
        validate();
        
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(this);
        
        InputMap inputMap = pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = pane.getActionMap();
        
        Action northAction = new ViewpointNavigationAction(plot, 
                ViewpointNavigationAction.Direction.NORTH,
                ViewpointNavigationAction.Direction.NORTH.name(), 
                new ImageIcon(getClass().getResource("/images/N.gif")),
                "Move the viewpoint North");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0), "moveNorth");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8,0), "moveNorth");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP,0), "moveNorth");
        actionMap.put("moveNorth", northAction);

        Action southAction = new ViewpointNavigationAction(plot, 
                ViewpointNavigationAction.Direction.SOUTH,
                ViewpointNavigationAction.Direction.SOUTH.name(), 
                new ImageIcon(getClass().getResource("/images/S.gif")),
                "Move the viewpoint South");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0), "moveSouth");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2,0), "moveSouth");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN,0), "moveSouth");
        actionMap.put("moveSouth", southAction);
    
        Action eastAction = new ViewpointNavigationAction(plot, 
                ViewpointNavigationAction.Direction.EAST,
                ViewpointNavigationAction.Direction.EAST.name(), 
                new ImageIcon(getClass().getResource("/images/E.gif")),
                "Move the viewpoint East");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0), "moveEast");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6,0), "moveEast");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT,0), "moveEast");
        actionMap.put("moveEast", eastAction);
        
        Action westAction = new ViewpointNavigationAction(plot, 
                ViewpointNavigationAction.Direction.WEST,
                ViewpointNavigationAction.Direction.WEST.name(), 
                new ImageIcon(getClass().getResource("/images/W.gif")),
                "Move the viewpoint West");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0), "moveWest");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4,0), "moveWest");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT,0), "moveWest");
        actionMap.put("moveWest", westAction);
        
        Action southEastAction = new ViewpointNavigationAction(plot, 
                ViewpointNavigationAction.Direction.SOUTH_EAST,
                ViewpointNavigationAction.Direction.SOUTH_EAST.name(), 
                new ImageIcon(getClass().getResource("/images/SE.png")),
                "Move the viewpoint South East");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3,0), "moveSouthEast");
        actionMap.put("moveSouthEast", southEastAction);

        Action southWestAction = new ViewpointNavigationAction(plot, 
                ViewpointNavigationAction.Direction.SOUTH_WEST,
                ViewpointNavigationAction.Direction.SOUTH_WEST.name(), 
                new ImageIcon(getClass().getResource("/images/SW.png")),
                "Move the viewpoint South West");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1,0), "moveSouthWest");
        actionMap.put("moveSouthWest", southWestAction);
        
        Action northEastAction = new ViewpointNavigationAction(plot, 
                ViewpointNavigationAction.Direction.NORTH_EAST,
                ViewpointNavigationAction.Direction.NORTH_EAST.name(), 
                new ImageIcon(getClass().getResource("/images/NE.png")),
                "Move the viewpoint North East");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9,0), "moveNorthEast");
        actionMap.put("moveNorthEast", northEastAction);

        Action northWestAction = new ViewpointNavigationAction(plot, 
                ViewpointNavigationAction.Direction.NORTH_WEST,
                ViewpointNavigationAction.Direction.NORTH_WEST.name(), 
                new ImageIcon(getClass().getResource("/images/NW.png")),
                "Move the viewpoint North West");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7,0), "moveNorthWest");
        actionMap.put("moveNorthWest", northWestAction);

        Action zoomInAction = new ViewpointNavigationAction(plot, 
                ViewpointNavigationAction.Direction.ZOOM_IN,
                ViewpointNavigationAction.Direction.ZOOM_IN.name(), 
                new ImageIcon(getClass().getResource("/images/zoomIn.gif")),
                "Zoom in the viewpoint");
        inputMap.put(KeyStroke.getKeyStroke('+'), "zoomIn");
        inputMap.put(KeyStroke.getKeyStroke('P'), "zoomIn");
//        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_P,0), "zoomIn");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,0), "zoomIn"); // keypad plus
        actionMap.put("zoomIn", zoomInAction);

        Action zoomOutAction = new ViewpointNavigationAction(plot, 
                ViewpointNavigationAction.Direction.ZOOM_OUT,
                ViewpointNavigationAction.Direction.ZOOM_OUT.name(), 
                new ImageIcon(getClass().getResource("/images/zoomOut.gif")),
                "Zoom out the viewpoint");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,0), "zoomOut");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,0), "zoomOut");
        actionMap.put("zoomOut", zoomOutAction);

        Action centreAction = new ViewpointNavigationAction(plot, 
                ViewpointNavigationAction.Direction.CENTRE,
                ViewpointNavigationAction.Direction.CENTRE.name(), 
                new ImageIcon(getClass().getResource("/images/Centre.gif")),
                "Centre the viewpoint over the radar");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5,0), "centreViewpoint");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,0), "centreViewpoint");
        actionMap.put("centreViewpoint", centreAction);
    }

    public String[] getSelectedRadars() {
        int rclength = radarChooserBoxes.length;
        if ( rclength < 1 ) {
            return null;
        }
        String[] ret = new String[rclength];
        for (int i = 0; i < rclength; i++) {
            if (radarChooserBoxes[i].isSelected()) {
                ret[i] = new String(radarChooserBoxes[i].getText());
            }
        }
        return ret;
    }
       
    /**
     * method called when the DataSource is aborting.
     * In this class this is a null method, nothing needs to be done.
     */
    public void dataSourceAbort() {
        windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    public void setFanViewCentre(Geographic fanViewCentre) {
        this.fanViewCentre = fanViewCentre;
    }

    /**
     * Adds new data to the plot. If the control program has changed
     * since the previous data record all the previous data will
     * be erased.
     * @param data
     * a <code>FitacfData</code> record.
     */
    public void newData( FitacfData data ) {

        String[] selectedRadars = getSelectedRadars();
        
        // set the newDataLabel to indicate data is being received and 
        // reset the timer which sets it to no new data received 
        SwingUtilities.invokeLater(new SetNewDataLabel(true));
        newDataTimer.restart();
        
        // if the default projection is in use, create on based on the site in the data
        SuperDarnSite site = SuperDarnSiteList.getList().getById( data.radarParms.stationId );
        
        if ( proj == defaultProj || fanViewCentre == null ) {
            fanViewCentre = site.siteCentre(data.radarParms.firstRangeDistance,
                    data.radarParms.rangeSeparation, data.radarParms.numberOfRanges);
            Projection newProj = new Stereographic(plot.getScale(), fanViewCentre);
            try {
                plot.setProjection(newProj);
                proj = newProj;
            }
            // can safely ignore this exception.
            // If the world map hasnt loaded yet proj won't get set.
            // Thus setProjection will keep getting called until the map is loaded.
            catch(WorldMapLoadingException e) {}
        }
        
        siteString = SuperDarnSiteList.getList().getById( data.radarParms.stationId ).getName();
        
        if ( data.radarParms.channel == 1 )
            channelString = "  channel: A";
        else if ( data.radarParms.channel == 2 )
            channelString = "  channel: B";
        else
            channelString = "  channel: unknown";
        
        setTitle( );
        
        String str = sDateFormat.format( data.radarParms.date );
        int width = metrics.stringWidth( str );
        Insets i = dateField.getInsets();
        
        Dimension d = dateField.getSize();
        d.width = width + i.left + i.right;
        
        if ( d.width < width ) {
            d.width = width;
            dateField.setMinimumSize( d );
            dateField.setPreferredSize( d );
            dateField.invalidate();
            this.validate();
        }

        TreeMap lbeams = plot.getBeams();
        String treemapInfoText = String.valueOf(lbeams.size());
        treemapInfoField.setText( treemapInfoText );
        counterInfoField.setText( ""+beamCounter );
        memoryInfoField.setText( ""+rt.freeMemory()/1024 );

        boolean updateInfo = singleRadar;
        for (int j=0; j<selectedRadars.length; j++) {
            if (SuperDarnSiteList.getList().getById( data.radarParms.stationId ).getShortName().toUpperCase().equals(selectedRadars[j])) {
                    updateInfo = true;
                    break;
            }
        }
        
        if (updateInfo) {
            dateField.setText( str );

            StringBuffer tmp;

            String ttmp[] = siteNameField.getText().split(newline);
            tmp = new StringBuffer(siteString + newline);
            for (int l=0; l<(Math.min(ttmp.length,4)); l++) {
                tmp.append(ttmp[l] + (l == (Math.min(ttmp.length,4))-1 ? "" : newline));
            }
            siteNameField.setText( tmp.toString() );

            ttmp = beamField.getText().split(newline);
            tmp = new StringBuffer(String.valueOf( data.radarParms.beamNumber ) + newline);
            for (int l=0; l<(Math.min(ttmp.length,4)); l++) {
                tmp.append(ttmp[l] + (l == (Math.min(ttmp.length,4))-1 ? "" : newline));
            }
            beamField.setText( tmp.toString() );
            
            ttmp = freqField.getText().split(newline);
            tmp = new StringBuffer(String.valueOf( data.radarParms.txFrequency ) + newline);
            for (int l=0; l<(Math.min(ttmp.length,4)); l++) {
                tmp.append(ttmp[l] + (l == (Math.min(ttmp.length,4))-1 ? "" : newline));
            }
            freqField.setText( tmp.toString() );

            ttmp = noiseField.getText().split(newline);
            tmp = new StringBuffer(String.valueOf( data.radarParms.noiseLevel ) + newline);
            for (int l=0; l<(Math.min(ttmp.length,4)); l++) {
                tmp.append(ttmp[l] + (l == (Math.min(ttmp.length,4))-1 ? "" : newline));
            }
            noiseField.setText( tmp.toString() );

            ttmp = CPIDField.getText().split(newline);
            tmp = new StringBuffer(String.valueOf( data.radarParms.controlProgramId ) + newline);
            for (int l=0; l<(Math.min(ttmp.length,4)); l++) {
                tmp.append(ttmp[l] + (l == (Math.min(ttmp.length,4))-1 ? "" : newline));
            }
            CPIDField.setText( tmp.toString() );

            ttmp = naveField.getText().split(newline);
            tmp = new StringBuffer(String.valueOf( data.radarParms.numberAveraged ) + newline);
            for (int l=0; l<(Math.min(ttmp.length,4)); l++) {
                tmp.append(ttmp[l] + (l == (Math.min(ttmp.length,4))-1 ? "" : newline));
            }
            naveField.setText( tmp.toString() );
        }
        plot.newData( data );
        
        // run the garbage collector to 
        // free up memory
        // if we have received 100 new beams
        if (beamCounter > 100) {
            rt.gc();
            beamCounter = 0;
        } else
            beamCounter += 1;
    }
    
    private void setTitle() {
        if (singleRadar) {
            super.setTitle( siteString+": "+what.type.name()+" vs. beam, Ch"+channelString );
        } else {
            super.setTitle( "Multiple Radar Display: "+what.type.name() );
        }
        
    }

    private void setNewDataLabel(boolean  ok ) {
        if ( ok ) {
            noNewData.setForeground(Color.DARK_GRAY);
            noNewData.setText("Data ok");
        }
        else {
            noNewData.setForeground(Color.RED);
            noNewData.setText("No new data received");
            plot.clearBeams();
        }
    }
    
    /**
     * Change which parameter is plotted.
     * @param what
     * the parameter to be plotted. Must be one of "velocity", "power"
     * or "width".
     * @throws NoSuchFieldException
     * if <code>what</code> is not one of the allowed types.
     */
    public void setWhat( PlotSubParameter what )
    throws NoSuchFieldException {
        setLegend( what );
    }
    
    /**
     * Turns on displaying of the meridional and zonal gridlines.
     */
    public void gridOn() { plot.gridOn();  }
    /**
     * Turns off displaying of the meridional and zonal gridlines.
     */
    public void gridOff() { plot.gridOff(); }
    /**
     * Turns on highlighting of the coasts and lakes.
     */
    public void coastlineOn() { plot.coastlineOn(); }
    /**
     * Turns off highlighting of the coasts and lakes.
     */
    public void coastlineOff() { plot.coastlineOff(); }
    /**
     * Turns on drawing of filled continents
     */
    public void continentsOn() { plot.continentsOn(); }
    /**
     * Turns off drawing of filled continents
     */
    public void continentsOff() { plot.continentsOff(); }
    /**
     * Turns on marking ground scatter in grey
     */
    public void groundscatterOff() { plot.groundscatterOff(); }
    /**
     * Turns off marking ground scatter in grey
     */
    public void groundscatterOn() { plot.groundscatterOn(); }
    
    /**
     * Sets the colour in which to draw the coastlines.
     * @param c
     * the colour for coastlines
     */
    public void setCoastColour( Color c ) { plot.setCoastColour( c ); }
    /**
     * Sets the colour in which to fill the sea.
     * @param c
     * the colour for sea
     */
    public void setSeaColour( Color c ) { plot.setSeaColour(c); }
    /**
     * Sets the colour in which to fill the land.
     * @param c
     * the colour for land
     */
    public void setLandColour( Color c ) { plot.setLandColour(c); }
    /**
     * Sets the colour in which to fill the lakes.
     * @param c
     * the colour for lakes
     */
    public void setLakeColour( Color c ) { plot.setLakeColour(c); }
    /**
     * Sets the colour in which to draw the gridlines.
     * @param c
     * the colour for gridlines
     */
    public void setGridColour( Color c ) { plot.setGridColour(c); }

    public void zoomIn() {
        plot.zoomIn();
    }
    
    public void zoomOut() {
        plot.zoomOut();
    }
    
    public void setScale(double scale) {
        plot.setScale(scale);
    }
    
    public void windowClosing(WindowEvent e) {
//System.out.println("FanWindow closing");
      
            this.removeWindowListener(this);

            velocityLegend.setCallback(null);
            powerLegend.setCallback(null);
            widthLegend.setCallback(null);
        
//System.out.println("FanWindow closing returns");
    }
    public void windowClosed(WindowEvent e) {
        
//System.out.println("FanWindow closed");
        
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

    public Runtime getRuntime() {
        return rt;
    }

    public boolean getSingleRadar() {
        return singleRadar;
    }
    
    public int getBeamCounter() {
        return beamCounter;
    }
}

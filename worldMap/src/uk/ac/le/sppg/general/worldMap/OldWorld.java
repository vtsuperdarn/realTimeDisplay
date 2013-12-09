package uk.ac.le.sppg.general.worldMap;



import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import uk.ac.le.sppg.coords.Geographic;
import uk.ac.le.sppg.coords.proj.Projection;

/**
 * A class for holding a world map.
 * <p>
 * The coordinate data can be read from a file or from a URL. If the data is
 * to be read from a URL it is done in a thread to allow for slow network
 * links. The method {@link #isComplete()} should be called to test whether
 * the data has completely loaded.
 * <p>
 * The coordinate data is static, therefore there is only one copy of this
 * data for every instance of <code>OldWorld</code>. The projected points
 * are instance variables, so each instance of <code>OldWorld</code> needs
 * to project the <code>Geographic</code> coordinate to a 2D plane by calling
 * {@link #projectPoints(Projection)}.
 * <p>
 * The coastline data are stored in an {@link java.util.ArrayList}.
 * Each element of the <code>ArrayList</code> is an individual {@link OldWorld.OutlineGeo}
 * coastline. <code>OutlineGeo</code> contains a <code>ArrayList</code>
 * of {@link coords.Geographic} points and a flag to indicate whether
 * it is a sea coastline or a lake. The geographic coastlines can be
 * accessed via the {@link #getGeographicCoasts()} method.
 * <p>
 * The projected coastlines are stored in an <code>ArrayList</code>.
 * Each element of the <code>ArrayList</code> is a {@link OldWorld.OutlinePath}
 * coastline.
 * <code>OutlinePath</code> contains a {@link java.awt.geom.GeneralPath}
 * of points and a flag to indicate whether it is a sea coastline or a lake.
 * The projected coastlines can be accessed via the
 * {@link #getProjectedCoasts()} method.
 * The projected points are scaled so that they range from 0.0 to 1.0 so the
 * graphics context of any drawing area should be suitably scaled using the
 * {@link Graphics2D} methods <code>scale()</code> or <code>setTransform()</code>.
 * A suitable scale to start with would be to scale to the size of the
 * drawing area.
 * Also, the stroke width needs to be scaled or the coast and lake lines will
 * be drawn the thickness of the drawing area.
 * <code>
 *    int currentWidth = getWidth() - insets.left - insets.right;
 *    int currentHeight = getHeight() - insets.top - insets.bottom;
 *
 *    g2.scale( currentWidth, currentHeight );
 *    g2.setStroke( new BasicStroke( 1.0f/currentWidth ) );
 *
 *
 *
 * @author Nigel Wade
 */

public class OldWorld {
    
    static class LoadingMonitor {
        private boolean isLoading;
        private boolean loaded;
        
        public LoadingMonitor(boolean initial) {
            isLoading = initial;
            loaded = false;
        }
        public void setLoading(boolean state) {
            isLoading = state;
            loaded = false;
        }
        public void setLoaded(boolean state) {
            loaded = state;
            isLoading = false;
            this.notifyAll();
        }
        public boolean isLoading() {
            return isLoading;
        }
        public boolean isLoaded() {
            return loaded;
        }
    }
    
    
    private static ArrayList<OutlineGeo> coastsGeo = new ArrayList<OutlineGeo>();
    
    private static LoadingMonitor loading = new LoadingMonitor(false);
    private static String sourceName;
    private static LoadFromURL urlLoader = null;
    
    private static ArrayList<OutlineGeo> meridiansGeo = new ArrayList<OutlineGeo>();
    private static ArrayList<OutlineGeo> parallelsGeo = new ArrayList<OutlineGeo>();
    
    private boolean projecting = false;
    
    private Projection projection = null;
    private boolean projected = false;
    
    private ArrayList<OutlinePath> coastsProj = null;
    private ArrayList<OutlinePath> meridiansProj = null;
    private ArrayList<OutlinePath> parallelsProj = null;
    
    boolean debug = false;
    
    int minLength; // the minimum number of points in a coastline/lake to
    
    
    /**
     * Creates an instance of the <code>OldWorld</code> class.
     * <p>
     * The class must first be initialized to load the outlines by
     * calling the class method {@link #load(String)} or {@link #load(URL)}.
     * An instance of <code>OldWorld</class> can be created whilst the
     * outlines are being loaded, but no other operation should be
     * performed on it until {@link #isComplete()} returns <code>true</code>
     * to indicate that the outlines have been loaded.
     * <p>
     * After loading the outlines the <code>Geographic</code> coordinates need
     * to be projected onto a 2D plane according to some {@link coords.Projection} before
     * they can be drawn.
     *
     * @param minLength
     * the minimum number of coordinates in an outline which should be drawn.
     */
    public OldWorld( int minLength ) {
        
        this.minLength = minLength;
        
        createGridLines( 15.0, 15.0 );
        //createGridLines( 45.0, 45.0 );
        
    }
    
    /**
     * Class which holds the outline of a coast or lake after projection.
     * The outline is stored as a <code>GeneralPath</code>.
     * @author Nigel Wade
     */
    public static class OutlinePath {
        GeneralPath path;
        boolean coast;
        
        /**
         * Indicates if the outline is a coastline or a lake.
         * @return
         * <code>true</code> if the outline is a coast.
         */
        public boolean isCoast() { return coast; }
        /**
         * Gets the outline which has been projected by {@link #projectPoints}
         * @return
         * The outline as a <code>GeneralPath</code>.
         */
        public GeneralPath getPath() { return path; }
    }
    
    /**
     * Class which defines a continent or lake outline.
     * <P>
     * The points are stored as {@link coords.Geographic} locations in a
     * <code>ArrayList</code>. The method <code>isCoast()</code>
     * indicates whether the outline is a coastline or a lake.
     *
     * @author Nigel Wade
     */
    public static class OutlineGeo {
        ArrayList<Geographic> path;
        boolean coast;
        /**
         * Indicates if the outline is a coastline or a lake.
         * @return
         * <code>true</code> if the outline is a coast.
         */
        public boolean isCoast() { return coast; }
        /**
         * Returns the list of <code>Geographic</code> points in the outline.
         * @return
         * An <code>ArrayList</code> of coast/lake outline.
         */
        public ArrayList<Geographic> getPath() { return path; }
    }
    
    private static class LoadFromURL extends Thread {
        // the run method of the thread, this actually loads the OldWorld
        // from a URL
        
        URL source;
        
        public LoadFromURL( URL url ) {
            source = url;
            sourceName = source.toString();
        }
        
        public void run() {
            
            synchronized (loading) {
                loading.setLoading(true);
            }
            coastsGeo.clear();
            
            DataInputStream input = null;
            
            try {
                input = new DataInputStream( source.openStream() );
                //StreamTokenizer textInput = new StreamTokenizer( new FileReader( "antarctica.txt" ) );
                
                float latitude;
                float longitude;
                
                int nPoints;
                
                // the first item read is an integer which is the number of
                // points in this coastline.
                while ( (nPoints = input.readInt()) > 0 ) {
                    
                    int s = input.readByte();
                    //System.out.println( "coast with "+nPoints+" points. s:"+s );
                    
                    OutlineGeo outline = new OutlineGeo();
                    
                    outline.path = new ArrayList<Geographic>( nPoints );
                    outline.coast = s != 0;
                    
                    for ( int i = 0; i < nPoints; i++ ) {
                        latitude = input.readFloat();
                        longitude = input.readFloat();
                        
                        outline.path.add( new Geographic( latitude, longitude, 0.0 ) );
                        
                    }
                    coastsGeo.add( outline );
                }
                
                synchronized(loading) {
                    loading.setLoaded(true);
                }
                
                
            } catch( EOFException eof ) {
                synchronized(loading) {
                    loading.setLoaded(true);
                }
            } catch( IOException e ) {
                e.printStackTrace();
            } catch( NumberFormatException nf ) {}
//            finally {
            try { input.close(); } catch( Exception any ) {}
//            }
            
        }
        
    }
    // method to create the lines for the meridians and the parallels.
    // these are created here in Geographic coordinates.
    // They will later be projected.
    
    private static void createGridLines( double meridianStep, double parallelStep ) {
        
        // the parallels and meridians are stored in parallelsGeo and meridiansGeo
        // which are ArrayLists each element of which is a OutlineGeo.
        
        for ( double lat = -90.0+parallelStep; lat < 90.0; lat += parallelStep ) {
            OutlineGeo parallel = new OutlineGeo();
            parallel.path = new ArrayList<Geographic>();
            parallel.coast = false;
            
            for ( double lon = -180.0; lon < 180.0; lon += 5.0 )
                parallel.path.add( new Geographic( lat, lon, 0.0 ) );
            
            parallelsGeo.add( parallel );
        }
        
        for ( double lon = 0.0; lon < 180.0; lon += meridianStep ) {
            OutlineGeo meridian = new OutlineGeo();
            meridian.path = new ArrayList<Geographic>();
            meridian.coast = false;
            
            for ( double lat = -88.0; lat < 90.0; lat += 2.0 )
                meridian.path.add( new Geographic( lat, lon, 0.0 ) );
            for ( double lat = 90.0; lat > -90.0; lat -= 2.0 )
                meridian.path.add( new Geographic( lat, lon-180.0, 0.0 ) );
            
            meridiansGeo.add( meridian );
        }
        
    }
    
    // whether a projection is being performed.
    private boolean isProjecting() {
        return projecting;
    }
    
    private synchronized void setProjecting( boolean s ) {
        projecting = s;
    }
    
    /**
     * Saves the <code>Geographic</code> outlines into a file.
     * If the outlines have not yet been read it will fail and
     * return <code>false</code>.
     * @param file
     * the name of the file to write the outlines to.
     * @return
     * returns <code>true</code> to indicate success.
     * @throws IOException
     */
    public synchronized static boolean saveGeo( String file )
    throws IOException {
        
        if ( ! loading.isLoaded() )
            return false;
        
        FileOutputStream out = new FileOutputStream( file );
        ObjectOutputStream s = new ObjectOutputStream( out );
        
        s.writeObject( coastsGeo );
        s.flush();
        
        s.close();
        out.close();
        
        return true;
        
    }
    
    /**
     * Loads the coastal and lake outlines from a file.
     * @param file
     * The name of the file to load from.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public synchronized static void load( String file )
    throws IOException {
        
        // if it's already loading don't start another
        
        synchronized(loading) {
            if ( loading.isLoading() )
                return;
            loading.setLoading(true);
            sourceName = file;
        }
        
        FileInputStream in = new FileInputStream( file );
        ObjectInputStream s = new ObjectInputStream( in );
        
        try {
            coastsGeo = (ArrayList<OutlineGeo>) s.readObject();
        } catch ( ClassNotFoundException e ) {
            throw new IOException( "Failed to load coastline, ClassNotFoundException: "+e );
        } finally {
            s.close();
            in.close();
        }
        
        synchronized(loading) {
            loading.setLoaded(true);
        }
    }
    
    /**
     * Loads the coastal and lake outlines from a URL.
     *
     * The loading is done in a separate thread so this method will return
     * alsmost immediately. The caller can get on with other things whilst
     * the coastlines are loaded, but must either call {@link #isComplete()} to
     * test whether the operation is complete, or {@link #waitUntilLoaded()}
     * before attempting to do any other operation on the class.
     *
     * If the outlines are still being loaded when another operation is attempted
     * then a WorldMapLoadingException will be thrown.
     *
     * @param whence
     * the <code>URL</code> to load from.
     * @return
     * <code>false</code> if another load operation is in progress.
     */
    public synchronized static boolean load( URL whence ) {
        
        // if it's already loading don't start another
        synchronized(loading) {
            if ( loading.isLoading() )
                return false;
        }
        
        urlLoader = new LoadFromURL( whence );
        
        urlLoader.start();
        
        return true;
    }
    
    /**
     * Loads the coastal and lake outlines from a the default location.
     *
     * The loading is done in a separate thread so this method will return
     * alsmost immediately. The caller can get on with other things whilst
     * the coastlines are loaded, but must either call {@link #isComplete()} to
     * test whether the operation is complete, or {@link #waitUntilLoaded()}
     * before attempting to do any other operation on the class.
     *
     * If the outlines are still being loaded when another operation is attempted
     * then a WorldMapLoadingException will be thrown.
     *
     * @param whence
     * the <code>URL</code> to load from.
     * @return
     * <code>false</code> if another load operation is in progress.
     */
    public synchronized static boolean load() {
        
        // if it's already loading don't start another
        synchronized(loading) {
            if ( loading.isLoading() )
                return false;
        }
        
        urlLoader = new LoadFromURL(OldWorld.class.getResource("/map_data.i"));
        
        urlLoader.start();
        
        return true;
    }
    
    
    
    
    /**
     * The OldWorld class contains a static list of the coastal and lake outlines which
     * can be read from a file or URL.
     * If they are read from a URL it is done in a separate thread because it
     * could be slow and the application might as well get on with something if
     * it can.
     * <p>
     * This method should be called to test if the loading process is complete.
     * @return
     * <code>true</code> if the coast and lake outlines have been loaded.
     */
    public synchronized static boolean isComplete() {
        
        return loading.isLoaded();
    }
    
    /**
     * Waits until the thread which is loading the outlines is complete.
     *
     */
    public static void waitUntilLoaded()
    throws InterruptedException {
        synchronized(loading) {
            if ( ! loading.isLoaded() ) {
                loading.wait();
            }
        }
    }
    
    /**
     * Projects the outlines of continents and lakes according to the currently set projection
     * (@see #setProjection(Projection) or @see #projectPoints(Projection)).
     * @throws WorldMapLoadingException
     * If the outlines are currently being read a WorldMapLoadingException will be thrown.
     */
    public synchronized void projectPoints()
    throws WorldMapLoadingException {
        if ( ! loading.isLoaded() )
            return;
        
        if ( projection != null ) {
            projectPoints( projection );
        }
    }
    
    /**
     * Projects the geographics outlines according the the <code>Projection proj</code>.
     * @param proj
     * the projection to use to convert from geographic coordinates to a 2D plane.
     * @throws WorldMapLoadingException
     * If the outlines are currently being read a WorldMapLoadingException will be thrown.
     */
    public synchronized void projectPoints( Projection proj )
    throws WorldMapLoadingException {
        
        synchronized(loading) {
            if ( loading.isLoading() ) {
                throw new WorldMapLoadingException("World map is currently loading from "+sourceName);
            }
        }
        
        if ( !loading.isLoaded() )
            return;
        
        this.projection = proj;
        projected = false;
        
        // set the lock to prevent the data being used
        setProjecting( true );
        
        // project the coastlines without clipping for filling
        if ( coastsProj == null )
            coastsProj = new ArrayList<OutlinePath>();
        else
            coastsProj.clear();
        
        project( coastsGeo, coastsProj, proj, minLength );
        
        
        // project the meridians and parallels
        if ( meridiansProj == null )
            meridiansProj = new ArrayList<OutlinePath>();
        else
            meridiansProj.clear();
        
        project( meridiansGeo, meridiansProj, proj, 0 );
        
        if ( parallelsProj == null )
            parallelsProj = new ArrayList<OutlinePath>();
        else
            parallelsProj.clear();
        
        //debug = true;
        project( parallelsGeo, parallelsProj, proj, 0 );
        debug = false;
        
        setProjecting( false );
        
        projected = true;
    }
    
    private synchronized void project( ArrayList<OutlineGeo> geoList, ArrayList<OutlinePath> projected, Projection proj, int minLength ) {
        
        //Projection proj2 = new Stereo2( proj.getScale(), proj.getCentre() );
        
        ArrayList<Geographic> geoPath;
        
        OutlinePath outline;
        
        Point2D p;
        float px;
        float py;
        boolean move;
        boolean visible;
        
        
        for ( OutlineGeo geoLine : geoList ) {
            geoPath = geoLine.path;
            
            //System.out.println("new path: length "+inner.size() );
            
            if ( geoPath.size() < minLength )
                continue;
            
            outline = new OutlinePath();
            outline.path = new GeneralPath();
            outline.coast = geoLine.coast;
            
            if ( debug )
                System.out.println( "start path");
            
            move = true;
            visible = false;
            
            boolean first = true;
            boolean skipClose = false;
            
            for ( Geographic geo : geoPath ) {
//            for ( Iterator c = geoPath.iterator(); c.hasNext(); ) {
//                
//                geo = (Geographic) c.next();
                
                p = proj.geoToPoint( geo );
                
                if ( p.getX() > 9999.0 ) {
                    move = true;
                    if ( first ) {
                        skipClose = true;
                    }
                    continue;
                }
                //geo2 = proj.pointToGeo(p);
                //Point2D p2 = proj2.geoToPoint( geo );
                //if ( Math.abs(geo.latitude-geo2.latitude) > 0.01 || Math.abs(geo.longitude-geo2.longitude) > 0.01 ) {
                //    System.out.println( geo + " " + geo2);
                //}
                px = (float) p.getX();
                py = (float) p.getY();
                
                if ( !visible )
                    visible = (px >= -1.0 && px <= 1.0 && py >= -1.0 && py <= 1.0);
                
                
                if ( move ) {
                    if ( first )
                        skipClose = true;
                    outline.path.moveTo( px, py  );
                    if ( debug )
                        System.out.println("move to "+px+" "+py);
                } else {
                    outline.path.lineTo( px, py );
                    if ( debug )
                        System.out.println("lin to "+px+" "+py);
                }
                
                move = false;
                first = false;
            }
            
            if ( debug )
                System.out.println("close path" );
            
            // if the path intersects the viewing area add it to the
            // ArrayList of coastlines/lakes
            
            // don't do anything with the path if its empty( getCurent return null)
            if ( outline.path.getCurrentPoint() != null ) {
                if ( ! skipClose  )
                    outline.path.closePath();
                if ( visible )
                    projected.add( outline );
            }
            
        }
        
    }
    
    
    /**
     * Fills the land and lakes with the requested colours.
     * @param g2
     * the graphics context into which the coastlines will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled.
     * @param landColour
     * colour with which to fill the land
     * @param lakeColour
     * colour with which to fill the lakes
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public synchronized boolean fillAll( Graphics2D g2, Color landColour, Color lakeColour ) {
        
        // if a projection is in progress return.
        // Don't throw an exception since this code might be run in the EDT
        // and it's not good to make it either synchronize or have exceptions thrown.
        if ( !loading.isLoaded() || coastsProj == null || isProjecting() )
            return false;
        
        OutlinePath outline;
        GeneralPath path;
        
        Color c = g2.getColor();
        
        
        for ( Iterator i = coastsProj.iterator(); i.hasNext(); ) {
            outline = (OutlinePath) i.next();
            
            path = outline.path;
            
            if ( outline.coast )
                g2.setColor( landColour );
            else
                g2.setColor( lakeColour );
            
            g2.fill( path );
        }
        
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Fills the land the requested colour.
     * @param g2
     * the graphics context into which the coastlines will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled.
     * @param landColour
     * colour with which to fill the land
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public synchronized boolean fillContinents( Graphics2D g2, Color landColour ) {
        
        // if a projection is in progress return.
        // Don't throw an exception since this code might be run in the EDT
        // and it's not good to make it either synchronize or have exceptions thrown.
        if ( !loading.isLoaded() || coastsProj == null || isProjecting() )
            return false;
        
        OutlinePath outline;
        GeneralPath path;
        
        Color c = g2.getColor();
        
        g2.setColor( landColour );
        
        for ( Iterator i = coastsProj.iterator(); i.hasNext(); ) {
            outline = (OutlinePath) i.next();
            
            path = outline.path;
            
            if ( outline.coast )
                g2.fill( path );
        }
        
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Fills the lakes with the requested colour.
     * @param g2
     * the graphics context into which the coastlines will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled.
     * @param lakeColour
     * colour with which to fill the lakes
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public synchronized boolean fillLakes( Graphics2D g2, Color lakeColour ) {
        
        // if a projection is in progress return.
        // Don't throw an exception since this code might be run in the EDT
        // and it's not good to make it either synchronize or have exceptions thrown.
        if ( !loading.isLoaded() || coastsProj == null || isProjecting() )
            return false;
        
        OutlinePath outline;
        GeneralPath path;
        
        Color c = g2.getColor();
        
        g2.setColor( lakeColour );
        
        for ( Iterator i = coastsProj.iterator(); i.hasNext(); ) {
            outline = (OutlinePath) i.next();
            
            path = outline.path;
            
            if ( ! outline.coast )
                g2.fill( path );
        }
        
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Draws the projected coastlines in the specified colour into the
     * <code>Graphics2D</code> context.
     * @param g2
     * the graphics context into which the coastlines will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled..
     * @param colour
     * the colour in which the coastlines should be drawn.
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public synchronized boolean drawCoastlines( Graphics2D g2, Color colour ) {
        
        // if a projection is in progress return.
        // Don't throw an exception since this code might be run in the EDT
        // and it's not good to make it either synchronize or have exceptions thrown.
        if ( !loading.isLoaded() || coastsProj == null || isProjecting() )
            return false;
        
        OutlinePath path;
        Color c = g2.getColor();
        //System.out.println( g2.getTransform() );
        g2.setColor( colour );
        
        for ( Iterator i = coastsProj.iterator(); i.hasNext(); ) {
            path = (OutlinePath) i.next();
            
            if ( path.coast )
                g2.draw( path.path );
        }
        
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Draws the projected lakes in the specified colour into the
     * <code>Graphics2D</code> context.
     * @param g2
     * the graphics context into which the lakes will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled.
     * @param colour
     * the colour in which the lakes should be drawn.
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public synchronized boolean drawLakes( Graphics2D g2, Color colour ) {
        
        // if a projection is in progress return.
        // Don't throw an exception since this code might be run in the EDT
        // and it's not good to make it either synchronize or have exceptions thrown.
        if ( !loading.isLoaded() || coastsProj == null || isProjecting() )
            return false;
        
        OutlinePath path;
        Color c = g2.getColor();
        
        g2.setColor( colour );
        
        for ( Iterator i = coastsProj.iterator(); i.hasNext(); ) {
            path = (OutlinePath) i.next();
            
            if ( ! path.coast )
                g2.draw( path.path );
        }
        
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Draws the meridional and zonal grid lines.
     * These are created at 15 degree intervals.
     * @param g2
     * the graphics context into which the gridlines will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled.
     * @param colour
     * the colour in which the lakes should be drawn.
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public synchronized boolean drawGridlines( Graphics2D g2, Color colour ) {
        
        // if a projection is in progress return.
        // Don't throw an exception since this code might be run in the EDT
        // and it's not good to make it either synchronize or have exceptions thrown.
        if ( !loading.isLoaded() || meridiansProj == null || parallelsProj == null || isProjecting() )
            return false;
        
        GeneralPath path;
        Color c = g2.getColor();
        
        
        g2.setColor( colour );
        
        for ( Iterator i = meridiansProj.iterator(); i.hasNext(); ) {
            path = ((OutlinePath) i.next()).path;
            
            g2.draw( path );
        }
        
        for ( Iterator i = parallelsProj.iterator(); i.hasNext(); ) {
            path = ((OutlinePath) i.next()).path;
            
            g2.draw( path );
        }
        
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Gets the geographic outlines of coasts and lakes.
     * Each coast and lake is stored as a separate entry in an <code>ArrayList</code>.
     * Each outline is a {@link #OutlineGeo}.
     * @return
     * The <code>ArrayList</code> containing the geographic outlines.
     * @throws WorldMapLoadingException
     * if the outlines are currently being loaded.
     */
    public ArrayList getGeographicOutlines()
    throws WorldMapLoadingException {
        
        synchronized(loading) {
            if ( loading.isLoading() ) {
                throw new WorldMapLoadingException("World map is currently loading from "+sourceName);
            }
        }
        
        return coastsGeo;
    }
    
    /**
     * Gets the projected outlines of coasts and lakes.
     * Each projected coast and lake is stored as a separate entry in an <code>ArrayList</code>.
     * Each outline is a {@link #OutlinePath}.
     * This method is synchronzed so will block if a projection is currently in progress.
     * @return
     * The <code>ArrayList</code> containing the projected outlines.
     * @throws WorldMapLoadingException
     * if the outlines are currently being loaded.
     */
    public synchronized ArrayList getProjectedOutlines()
    throws WorldMapLoadingException {
        
        synchronized(loading) {
            if ( loading.isLoading() ) {
                throw new WorldMapLoadingException("World map is currently loading from "+sourceName);
            }
        }
        
        return coastsProj;
    }
    
    /**
     * Sets the current projection, and if the outlines have been loaded
     * actually performs the projection.
     * This method is synchronized so will block if another projection is curently
     * in progress.
     * @param proj
     * The projection to be used. (@see Projection)
     * @throws WorldMapLoadingException
     * if the outlines are currently being loaded.
     */
    public synchronized void setProjection( Projection proj )
    throws WorldMapLoadingException {
        this.projection = proj;
        projected = false;
        
        if ( loading.isLoaded() ) {
            projectPoints( projection );
        }
    }
    
    /**
     * Returns the current state of the projection.
     * @return
     * if the outlines have been projected the return value is true. If no projection
     * has been performed, or a projection is in progress it returns false.
     */
    public boolean isProjected() {
        return projected;
    }
}


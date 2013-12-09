package uk.ac.le.sppg.general.worldMap;



import uk.ac.le.sppg.coords.GeographicOutline;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;

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
 * data for every instance of <code>World</code>. 
 * <p>
 * The coastline data are stored in an {@link java.util.ArrayList}.
 * Each element of the <code>ArrayList</code> is an individual {@link World.OutlineGeo}
 * coastline. <code>OutlineGeo</code> contains a <code>ArrayList</code>
 * of {@link coords.Geographic} points and a flag to indicate whether
 * it is a sea coastline or a lake. The geographic coastlines can be
 * accessed via the {@link #getGeographicCoasts()} method.
 * <p>
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

public class World {
    
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
    
    
    private  ArrayList<WorldOutline> coasts = new ArrayList<WorldOutline>();
    private  ArrayList<GeographicOutline> meridians = new ArrayList<GeographicOutline>();
    private  ArrayList<GeographicOutline> parallels = new ArrayList<GeographicOutline>();
    
    
    private  LoadingMonitor loading = new LoadingMonitor(false);
    private  String sourceName;
    private  LoadFromURL urlLoader = null;
    
    static boolean debug = true;
    
    
    /**
     * Creates an instance of the <code>World</code> class.
     * <p>
     * The class must first be initialized to load the outlines by
     * calling the class method {@link #load(String)} or {@link #load(URL)}.
     * An instance of <code>World</class> can be created whilst the
     * outlines are being loaded, but no other operation should be
     * performed on it until {@link #isComplete()} returns <code>true</code>
     * to indicate that the outlines have been loaded.
     * <p>
     * After loading the outlines the <code>Geographic</code> coordinates need
     * to be projected onto a 2D plane according to some {@link coords.Projection} before
     * they can be drawn.
     *
     */
    public World() {
        
        createGridLines( 15.0, 15.0 );
        
        synchronized(loading) {
            if ( ! loading.isLoaded() && ! loading.isLoading() ) {
                load();
            }
        }
        //createGridLines( 45.0, 45.0 );
        
    }    

    private class LoadFromURL extends Thread {
        // the run method of the thread, this actually loads the World
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
            coasts.clear();
            
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
                    boolean coastLine = (s != 0);
                    WorldOutline outline = new WorldOutline(nPoints, coastLine);
                                        
                    for ( int i = 0; i < nPoints; i++ ) {
                        latitude = input.readFloat();
                        longitude = input.readFloat();
                        
                        outline.add( new Geographic( latitude, longitude, 0.0 ) );
                        
                    }
                    coasts.add( outline );
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
    
    private void createGridLines( double meridianStep, double parallelStep ) {
        
        // the parallels and meridians are stored in parallelsGeo and meridiansGeo
        // which are ArrayLists each element of which is a OutlineGeo.
        
        for ( double lat = -90.0+parallelStep; lat < 90.0; lat += parallelStep ) {
            GeographicOutline parallel = new GeographicOutline(1);
            
            for ( double lon = -180.0; lon < 180.0; lon += 5.0 )
                parallel.add( new Geographic( lat, lon, 0.0 ) );
            
            parallels.add( parallel );
        }
        
        for ( double lon = 0.0; lon < 180.0; lon += meridianStep ) {
            GeographicOutline meridian = new GeographicOutline(1);
            
            for ( double lat = -88.0; lat < 90.0; lat += 2.0 )
                meridian.add( new Geographic( lat, lon, 0.0 ) );
            for ( double lat = 90.0; lat > -90.0; lat -= 2.0 )
                meridian.add( new Geographic( lat, lon-180.0, 0.0 ) );
            
            meridians.add( meridian );
        }
        
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
    public synchronized boolean saveGeo( String file )
    throws IOException {
        
        if ( ! loading.isLoaded() )
            return false;
        
        FileOutputStream out = new FileOutputStream( file );
        ObjectOutputStream s = new ObjectOutputStream( out );
        
        s.writeObject( coasts );
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
    public synchronized void load( String file )
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
            coasts = (ArrayList<WorldOutline>) s.readObject();
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
    public synchronized boolean load( URL whence ) {
        
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
    public synchronized boolean load() {
        
        // if it's already loading don't start another
        synchronized(loading) {
            if ( loading.isLoading() )
                return false;
        }
        
        urlLoader = new LoadFromURL(World.class.getResource("/map_data.i"));
        
        urlLoader.start();
        
        return true;
    }
    
    
    
    
    /**
     * The World class contains a static list of the coastal and lake outlines which
     * can be read from a file or URL.
     * If they are read from a URL it is done in a separate thread because it
     * could be slow and the application might as well get on with something if
     * it can.
     * <p>
     * This method should be called to test if the loading process is complete.
     * @return
     * <code>true</code> if the coast and lake outlines have been loaded.
     */
    public synchronized boolean isComplete() {
        
        return loading.isLoaded();
    }
    
    /**
     * Waits until the thread which is loading the outlines is complete.
     *
     */
    public void waitUntilLoaded()
    throws InterruptedException {
        synchronized(loading) {
            if ( ! loading.isLoaded() ) {
                loading.wait();
            }
        }
    }
    
    public ArrayList<WorldPath> projectCoastlines( Projection proj, int minLength ) {
        
        ArrayList<WorldPath> result = new ArrayList<WorldPath>();
        
        for(WorldOutline outline: coasts) {
            if ( outline.getPath().size() >= minLength ) {
                GeneralPath projectedOutline = proj.projectGeographicOutline(outline, true);
                if ( projectedOutline.getCurrentPoint() != null )
                    result.add(new WorldPath(projectedOutline,outline.isCoastline()));
            }
        }
        
        return result;
        
    }
    
    public ArrayList<GeneralPath> projectMeridians( Projection proj, int minLength ) {
        ArrayList<GeneralPath> result = new ArrayList<GeneralPath>();
        
        for(GeographicOutline outline: meridians) {
            if ( outline.getPath().size() >= minLength ) {
                GeneralPath projectedOutline = proj.projectGeographicOutline(outline, true);
                if ( projectedOutline.getCurrentPoint() != null )
                    result.add(projectedOutline);
            }
        }
        
        return result;
     }
    
    public ArrayList<GeneralPath> projectParallels( Projection proj, int minLength ) {
        ArrayList<GeneralPath> result = new ArrayList<GeneralPath>();
        
        for(GeographicOutline outline: parallels) {
            if ( outline.getPath().size() >= minLength ) {
                GeneralPath projectedOutline = proj.projectGeographicOutline(outline, true);
                if ( projectedOutline.getCurrentPoint() != null )
                    result.add(projectedOutline);
            }
        }
        
        return result;
    }
    

    public ArrayList<WorldOutline> getCoastlines()
    throws WorldMapLoadingException {
        
        synchronized(loading) {
            if ( loading.isLoading() ) {
                throw new WorldMapLoadingException("World map is currently loading from "+sourceName);
            }
        }
        
        return coasts;
    }
    
    public WorldProjection getProjection(Projection proj, int minLength) 
    throws InterruptedException {
        waitUntilLoaded();
        
        return new WorldProjection(this, minLength, proj);
        
    }

}


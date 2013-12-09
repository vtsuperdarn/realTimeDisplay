/*
 * Created on 24-Jun-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package uk.ac.le.sppg.superdarn.rmiServer.server;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import uk.ac.le.sppg.coords.Site;
import org.apache.log4j.Logger;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;
import uk.ac.le.sppg.superdarn.fitData.FitacfNetReader;

/**
 * Thread to read new data for the dataServlet
 *
 * @author Nigel Wade
 */
public class NewDataThread extends Thread {
    
    private class FileMatch implements FilenameFilter {
        public boolean accept( File f, String n ) {
            return n.endsWith( site.getShortName()+channel );
        }
    }
    
    
    String host;
    int port;
    Date lastDate;
    ChannelId channel;
    Site site;
    
    Thread readerThread = null;
    
    volatile boolean runnable = true;
    FitacfNetReader reader = null;
    Object mutex = new Object();
    
    DataStore store;
    
    final Logger logger;
    
    
    int dataHour = -1;
    
    Date cacheEnd = null;
    String cacheFile;
    
    File cacheDirFile;
    
    ObjectOutputStream writer = null;
    
    Calendar calendar = new GregorianCalendar( TimeZone.getTimeZone( "GMT0" ));
    SimpleDateFormat timeFormat = new SimpleDateFormat( "yyyyMMddHHmm" );
    
    FileMatch fileMatcher = new FileMatch();
    
    static final long CACHE_FILE_LENGTH = 1000*60*60;  // length of time of cache file (mS).
    
    public NewDataThread( String host, int port, DataStore store, Site site, ChannelId channel) {
        
        this.host = host;
        this.port = port;
        this.store = store;
        this.channel = channel;
        this.site = site;

        logger = Logger.getLogger("rmiServer."+site.getShortName()+".newDataThread."+channel);
        
        timeFormat.setCalendar( calendar );
        
        setName("superdarn.rmiServer.server.newDataThread."+site);
        
    }
    
    public void abort() {
        runnable = false;
        if ( readerThread != null )
            readerThread.interrupt();
        
        try {
            synchronized( mutex ) {
                if ( reader != null ) {
                    reader.close();
                    reader = null;
                }
            }
        } catch ( IOException e ) {
        }
    }
    
    @Override
    public void run() {
        
        logger.info( "starting data reading thread ");
        
        readerThread = currentThread();
        
        while ( runnable ) {
            
            // if the reader isn't open, try to open it.
            synchronized( mutex ) {
                
                if ( reader == null && runnable ) {
                    try {
                        reader = connectServer( host, port, (byte)1 );
                    } catch( java.net.UnknownHostException uhe ) {
                        logger.error( "Failed to connect to "+host, uhe);
                        return;
                    } catch ( IOException ioe ) {
                        logger.error( "Failed to connect to site: "+host+"."+channel, ioe );
                        
                        try {
                            Thread.sleep( 10000 );
                        } catch ( InterruptedException ie ) {
                            logger.info( site+":"+channel+": new data thread interrupted ");
                            break;
                        }
                        
                        continue;
                        
                    }
                }
            }
            
            // read the next data record
            try {
                FitacfData fit = reader.next();
                
                if ( fit == null ) {
                    logger.warn("Null data read");
                    this.sleep(5000);
                    continue;
                }
                else
                    logger.debug( "Read data for "+ fit.radarParms.date );
                
                if ( !runnable )
                    break;
                
                if ( lastDate == null )
                    lastDate = fit.radarParms.date;
                
                if ( fit.radarParms.date.after( lastDate ) ) {
                    // store the data record in the list
                    
                    store.addData(channel, fit);
                    store.purgeData(channel);
                    
                    lastDate = fit.radarParms.date;
                    
                    // cache the data
                    cacheData( fit );
                    
                    // reset the alarm timer
                }
                
            } catch ( SocketTimeoutException e) {
                logger.info( "new data thread: timeoout reading data " );
                synchronized( mutex ) {
                    if ( reader != null ) {
                        try {
                            reader.close();
                        } catch ( IOException e2 ) {}
                        
                        reader = null;
                    }
                }
            } catch ( IOException e ) {
                logger.error( "new data thread IOException ", e );
                synchronized( mutex ) {
                    if ( reader != null ) {
                        try {
                            reader.close();
                        } catch ( IOException e2 ) {}
                        
                        reader = null;
                    }
                }
            }
            
            catch ( Exception e ) {
                logger.error( "new data thread Exception ", e );
                e.printStackTrace();
                runnable = false;
            }
            
            catch( Error e ) {
                logger.error("Error in new data thread:", e);
                e.printStackTrace();
                runnable = false;
                
                //System.exit(-1);
            }
            
        }
        
        logger.info( "new data thread no longer runnable ");
        
        // the thread is complete, close the reader.
        try {
            synchronized( mutex ) {
                if ( reader != null )
                    reader.close();
                reader = null;
            }
        } catch ( IOException e ) {}
        
        logger.info( "New data thread ended");
        
    }
    
    
    private synchronized FitacfNetReader connectServer( String server, int port, byte stream )
    throws java.net.UnknownHostException, IOException {
        FitacfNetReader fitReader = new FitacfNetReader( server, port, 300 );
        
        logger.info( "New data thread, attempting connection to "+server+":"+port );
        fitReader.open(stream);
        
        logger.info( "New data thread, opened connection to "+server+":"+port );
        
        return fitReader;
    }
    
    
    private void cacheData( FitacfData fit ) {
        
        // cache the data
        Date date = fit.radarParms.date;
        
        
        if ( cacheEnd == null ) {
            cacheEnd = new Date( date.getTime() + CACHE_FILE_LENGTH );
        }
        
        // check if data is beyond end of cache file (1 hour in length)
        cacheDirFile = new File( store.getCacheDir() );

        if ( date.after( cacheEnd ) ) {
            
            // close current file
          logger.debug("closing cache file: "+date+" :"+cacheEnd);
            try {
                writer.close();
            } catch( IOException e ) {
                logger.error( "Error closing cache file: "+cacheFile, e);
            } finally {
                writer = null;
            }
            
            cacheEnd = new Date( date.getTime() + CACHE_FILE_LENGTH );
            // remove old cache files.
            
            String[] fileList = cacheDirFile.list( fileMatcher );
            for ( int i=0; i<fileList.length; i++ ){
                File f = new File( store.getCacheDir(), fileList[i] );
                
                
                if ( ! f.exists() )
                    continue;
                
                long modified = f.lastModified();
                
                // if time is more than 24 hours ago delete the file
                if ( date.getTime() > modified + 1000*24*60*60 ) {
                    if ( f.delete() ) {
                        logger.debug( "Deleted old cache file "+fileList[i] );
                    } else {
                        logger.error( "Failed to delete cache file "+fileList[i] );
                    }
                }
                
            }
            
            
        }
        
        // if needed, open a cache file.
        if ( writer == null ) {
            
            String df = timeFormat.format( date );
                        
            try {
                // if the cache directory does not exist, create it.
                if ( ! cacheDirFile.exists() ) {
                    if ( ! cacheDirFile.mkdirs() ) {
                        throw new FileNotFoundException("Unable to make directory: "+cacheDirFile);
                    }
                }
                
                cacheFile = cacheDirFile + System.getProperty( "file.separator" ) +
                        df + site.getShortName() + channel;
                
                FileOutputStream fos = new FileOutputStream( cacheFile, true );
                
                writer = new ObjectOutputStream( fos );
                
            } catch( FileNotFoundException e ) {
                logger.error( "Unable to open cache file", e );
            } catch ( IOException e ) {
                logger.error( "Unable to open ObjectOutputStream for "+cacheFile, e );
            }
          logger.debug("opened new cache file: "+cacheFile+" valid until:"+cacheEnd);
            
        }
        
        try {
            // synchronize the write with the same mutex as used in abort().
            // thay way another thread which wants to abort the reader won't succeed
            // whilst a write to the cache is in progress.
            synchronized( mutex ) {
                if ( writer != null ) {
                    writer.writeObject( fit );
                    writer.flush();
                }
            }
        } catch ( IOException e ) {
            logger.error( "Error caching data: ", e );
            try {
                writer.reset();
                writer.close();
            } catch ( IOException e2 ) {} finally {
                writer = null;
            }
            
        }
        
    }
    
    
    
}

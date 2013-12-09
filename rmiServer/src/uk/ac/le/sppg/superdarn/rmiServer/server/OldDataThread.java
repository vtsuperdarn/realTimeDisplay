/*
 * Created on 24-Jun-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package uk.ac.le.sppg.superdarn.rmiServer.server;


import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;

import uk.ac.le.sppg.superdarn.fitData.FitacfData;

/**
 * Thread to read cached data for the specified site.
 *
 * @author Nigel Wade
 */
public class OldDataThread extends Thread {
    
    DataStore store;
    final Logger logger;
    String site;
    ChannelId channel;
    
    volatile boolean runnable = true;
    
    ObjectInputStream reader = null;
    Object mutex = new Object();
    
    
    File cacheDirFile;
    
    public OldDataThread( String cacheSite, ChannelId channel, DataStore store ) {
        
        this.store = store;
        this.site = cacheSite;
        this.channel = channel;
        
        cacheDirFile = new File( store.getCacheDir() );
        
        logger = Logger.getLogger("rmiServer."+cacheSite+".oldDataThread."+channel);
        
        setName("superdarn.rmiServer.server.oldDataThread."+site);
    }
    
    
    public void abort() {
        runnable = false;
        synchronized( mutex ) {
            if ( reader != null ) {
                try {
                    this.reader.close();
                    reader = null;
                } catch ( IOException e ) {
                }
            }
        }
    }
    
    @Override
    public void run() {
        
        FitacfData fit;
        Object o;
        String inputFile = null;
        
        ArrayList<FitacfData> oldDataList = new ArrayList<FitacfData>();
        
        // get a list of cache files and sort
        
        logger.info( "OldDataThread: locating cache files for "+site+channel);
        String[] fileArray = cacheDirFile.list( new FilenameFilter() {
            public boolean accept( File d, String name ) { return name.endsWith( site+channel ); } } );
            
            if ( fileArray == null || fileArray.length < 1 ) {
                logger.info("OldDataThread: no old data in "+store.getCacheDir());
                return;
            }
            // TreeSet is sorted Collection.
            TreeSet<String> fileList = new TreeSet<String>();
            for ( int i=0; i<fileArray.length; i++ ) {
                fileList.add( fileArray[i] );
            }
            
            long currentTime = System.currentTimeMillis();
            final long oneDayAgo = currentTime - 24*60*60*1000;
            final Date oldestRequired = new Date( oneDayAgo );
            
            //System.out.println( fileList );
            //System.out.flush();
            
            Iterator<String> iter = fileList.iterator();
            
            while ( runnable ) {
                
                try {
                    
                    synchronized( mutex ) {
                        if ( reader == null && runnable ) {
                            
                            if ( !iter.hasNext()  ) {
                                // there are no more cache files to read.
                                break;
                            }
                            String fileName = iter.next();
                            inputFile = store.getCacheDir() + System.getProperty( "file.separator") + fileName;
                            reader = new ObjectInputStream( new FileInputStream( inputFile ) );
                            logger.info( "Reading cached data from "+inputFile );
                        }
                    }
                    
                    o = reader.readObject();
                    
                    fit = (FitacfData) o;
                    //logger.displayString( fit.radarParms.date.toString() );
                    
                    // if the data is less than 24hours old add it to the list
                    
                    if ( fit != null && fit.radarParms.date.after( oldestRequired ) ) {
                        //logger.displayString( "Add cached data "+fit.radarParms.date );
                        oldDataList.add( fit );
                        //logger.displayString( this.site+" Store data "+fit.radarParms.date+" "+fit.radarParms.channel+" "+fit.radarParms.txFrequency );
                    }
                    
                }
                
                catch ( EOFException e ) {
                    logger.info( "End of cache file: "+inputFile );
                    synchronized( mutex ) {
                        if ( reader != null ) {
                            try {
                                reader.close();
                            } catch ( IOException e2 ) {}
                            reader = null;
                        }
                    }
                }
                
                catch ( IOException e ) {
                    logger.error( "Error reading cache file: "+inputFile+": ", e );
                    synchronized( mutex ) {
                        if ( reader != null ) {
                            try {
                                reader.close();
                            } catch ( IOException e2 ) {}
                            reader = null;
                        }
                    }
                }
                
                catch ( ClassNotFoundException e ) {
                    logger.error( "Error reading cache file: "+inputFile+": ", e );
                    runnable = false;
                    
                }
                
            }
            
            // add the old data to the list if the thread is still runnable
            logger.info( "old data thread complete");
            
            if ( runnable ) {
                store.addAllData( channel, 0, oldDataList );
            }
    }
    
    
}

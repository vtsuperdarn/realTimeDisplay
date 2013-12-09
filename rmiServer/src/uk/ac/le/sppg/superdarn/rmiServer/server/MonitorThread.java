package uk.ac.le.sppg.superdarn.rmiServer.server;


import java.util.Date;

import uk.ac.le.sppg.coords.Site;
import org.apache.log4j.Logger;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;

/**
 * Thread to monitor the NewDataThread and start a new one if it stops.
 *
 * @author Nigel Wade
 */
public class MonitorThread extends Thread {
    
    String host;
    int port;
    Date lastDate;
    Site site;
    ChannelId channel;
    
    Object mutex = new Object();
    boolean stopping = false;
    
    DataStore store;
    
    final Logger logger;
    
    NewDataThread readerThread;
    
    public MonitorThread( String host, int port, DataStore store, Site site, ChannelId channel) {
        
        this.host = host;
        this.port = port;
        this.store = store;
        this.site = site;
        this.channel = channel;
                
        this.setName("superdarn.rmiServer.server.monitor."+site);
        
        logger = Logger.getLogger("rmiServer."+site.getShortName()+".monitorThread."+channel);
    }
    
    public void abort() {
        
        logger.info( "Abort: Shutting down new data thread");
        
        synchronized(mutex) {
            stopping = true;
        }
        readerThread.abort();
        
    }
    
    @Override
    public void run() {        
        
        readerThread = new NewDataThread( host, port, store, site, channel );
        readerThread.start();
        
        try {
            readerThread.join();
        } catch( InterruptedException e) {
            logger.info("thread interrupted");
        }
        
        logger.info( "thread exiting");
        
        // when the new data thread exits shutdown the data store;
        synchronized(mutex) {
            if ( !stopping ) {
                store.stop();
            }
        }
    }
    
    
}

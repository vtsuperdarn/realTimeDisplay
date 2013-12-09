/*
 * Created on 17-May-2004
 */
package uk.ac.le.sppg.superdarn.rmiServer.server;

import uk.ac.le.sppg.superdarn.rmiServer.server.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import uk.ac.le.sppg.coords.Site;
import java.util.Date;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;

/**
 * @author Nigel Wade
 */
public class RmiServer
        extends UnicastRemoteObject
        implements RmiServerInterface {
    
    private static final long serialVersionUID = 0x525350504700000DL;
    
    private final String myBindName;
    Site site;
    public DataStore data;
//    final Logger logger = Logger.getRootLogger();
    final Logger logger;
    String cacheDir;
    private Date startTime = new Date();
        
    private boolean shuttingDown = false;
    
    public RmiServer(Site site, String cacheDir)
    throws RemoteException {
        super();
        this.site = site;
        data = new DataStore(this, site, cacheDir);

        myBindName = "//localhost/" + site.getCompactName() + "." + RmiServerInterface.bindName;
        
        logger = Logger.getLogger("rmiServer."+site.getShortName());
                
    }
    
    public void bind() 
    throws RemoteException, MalformedURLException {        
        // bind the server with the RMI server
        Naming.rebind( myBindName, this );
        
    }
    
    
    public synchronized void stop() {
        if ( shuttingDown )
            return;
        
        shuttingDown = true;
        
        try {
            logger.info("Shutdown in progress");
            data.shutdown();
        } catch(Error e) {
            logger.error("Error in shutdown", e);
        }
        
        try {
            Naming.unbind(myBindName);
            UnicastRemoteObject.unexportObject(this, true);
        } catch(Exception e) {
            logger.error("Error unbinding", e);
            e.printStackTrace();
        } catch(Error e) {
            logger.error("Error unbinding", e);
            e.printStackTrace();
        }
        
    }
    
    void addChannel( String serverHost, ChannelId channel, int port )
    throws NumberFormatException, Exception {
        try {
            data.addChannel(serverHost, channel, port);
        } catch( Error e ) {
            logger.error("Error adding channel "+channel,e);
            stop();
        }
    }
    
    
    public FitacfData get(ChannelId channel, long time)
    throws RemoteException {
        // return the data for a particular time, or null
        
        FitacfData result = null;
        try {
            result = data.get(channel, time);
        } catch( Error e ) {
            logger.error("Error getting data: ", e);
            stop();
        }
        
        return result;
    }
    
    
    public FitacfData next(ChannelId channel, long afterTime, int beam, int timeoutSecs)
    throws RemoteException {
        
        FitacfData result = null;
        try {
            result = data.next(channel, afterTime, beam, timeoutSecs );
        } catch( Error e ) {
            logger.error("Error getting data: ", e);
            stop();
        }
        
        return result;
        
    }
    
    public FitacfData previous(ChannelId channel, long beforeTime, int beam)
    throws RemoteException {
        FitacfData result = null;
        try {
            result = data.previous(channel, beforeTime, beam);
        } catch( Error e ) {
            logger.error("Error getting data: ", e);
           stop();
        }
        
        return result;
    }
    
    public FitacfData latest(ChannelId channel, int beam)
    throws RemoteException {
        FitacfData result = null;
        try {
            result = data.latest(channel, beam);
        } catch( Error e ) {
            logger.error("Error getting data: ", e);
            stop();
        }
        
        return result;
    }
    
    public FitacfData oldest(ChannelId channel, int beam)
    throws RemoteException {
        FitacfData result = null;
        try {
            result = data.oldest(channel, beam);
        } catch( Error e ) {
            logger.error("Error getting data: ", e);
            stop();
        }
        
        return result;
    }
    
    public FitacfData next(ChannelId channel, long afterTime, int timeoutSecs )
    throws RemoteException {
        FitacfData result = null;
        try {
            result = data.next(channel, afterTime, timeoutSecs);
        } catch( Error e ) {
            logger.error("Error getting data: ", e);
            stop();
        }
        
        return result;
    }
    
    public FitacfData previous(ChannelId channel, long beforeTime)
    throws RemoteException {
        FitacfData result = null;
        try {
            result = data.previous(channel, beforeTime);
        } catch( Error e ) {
            logger.error("Error getting data: ", e);
            stop();
        }
        
        return result;
    }
    
    public FitacfData latest(ChannelId channel)
    throws RemoteException {
        FitacfData result = null;
        try {
            result = data.latest(channel);
        } catch( Error e ) {
            logger.error("Error getting data: ", e);
            stop();
        }
        
        return result;
    }
    
    public FitacfData oldest(ChannelId channel)
    throws RemoteException {
        FitacfData result = null;
        try {
            result = data.oldest(channel);
        } catch( Error e ) {
            logger.error("Error getting data: ", e);
            stop();
        }
        
        return result;
    }
    
    public boolean checkNet()
    throws RemoteException {
        return true;
    }
    
    public ChannelId[] channels()
    throws RemoteException {
        ChannelId[] result = null;
        try {
            result = data.channels();
        } catch( Error e ) {
            logger.error("Error getting channels: ", e);
            stop();
        }
        
        return result;
    }
    
    public void shutdown()
    throws RemoteException {
        // abort the reader thread.
        try {
            logger.info("Shutdown in progress");
            data.shutdown();
        } catch(Error e) {
            logger.error("Error in shutdown", e);
        }
        
        try {
            Naming.unbind(myBindName);
        } catch(Exception e) {
            logger.error("Error unbinding", e);
            e.printStackTrace();
        } catch(Error e) {
            logger.error("Error unbinding", e);
            e.printStackTrace();
        }
        
        UnicastRemoteObject.unexportObject(this, true);
    }
    
    public java.util.Date startTime() throws RemoteException {
        return startTime;
    }
    
    public void setLogLevel(Level level) throws RemoteException {
        Logger topLogger = Logger.getRootLogger();
        topLogger.warn("log level changed to: "+level);
        topLogger.setLevel(level);
    }
    
    public Level getLogLevel() throws RemoteException {
        if ( logger.getLevel() == null ) 
            return logger.getEffectiveLevel();
        else
            return logger.getLevel();
    }
}

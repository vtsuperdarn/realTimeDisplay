/*
 * Created on 17-May-2004
 */
package uk.ac.le.sppg.superdarn.rmiServer.monitor;

import uk.ac.le.sppg.superdarn.rmiServer.monitor.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.le.sppg.superdarn.rmiServer.server.RmiServerInterface;

/**
 * this is the RMI server which is responsible for starting and stopping
 * the data server processes.
 * It listens for RMI requests and acts on those requests. The RMI actions which
 * it provides are specified in {@link MonitorInterface}.
 */
public class RmiMonitor
        extends UnicastRemoteObject
        implements MonitorInterface {
    
    private static final long serialVersionUID = 0x525350504700000BL;
    
//    final Logger logger = Logger.getLogger(RmiMonitor.class);
    static Logger logger = Logger.getRootLogger();
    Properties properties;
    
    final String bindName = "//localhost/" + MonitorInterface.bindName;
    
    HashMap<String, Server> servers = new HashMap<String, Server>();
    
    public RmiMonitor(Properties properties)
    throws RemoteException, MalformedURLException {
        super();
        this.properties = properties;
        
        // bind the server with the RMI server
        Naming.rebind( bindName, this );
    }
    
    // shutdown the entire data server system.
        /* (non-Javadoc)
         * @see rmiServer.monitor.MonitorInterface#shutdown()
         * @return
         * the status string return will begin with "OK:" if the action succeeded
         * and "ERROR:" if any error occured.
         */
    public String shutdown() 
    throws RemoteException {
        String result;
        
        try {
            
            // first shutdown each data server.
            String[] sites = listServers();
            for(int i=0; i<sites.length; i++) {
                logger.info("stopping "+sites[i]);
                shutdown(sites[i]);
            }
            
            // now unbind the Monitor RMI object from the rmiregistry.
            Naming.unbind(bindName);
            UnicastRemoteObject.unexportObject(this, true);
            
            result = "OK: shutdown";
            logger.info(result);
        } catch( RemoteException e ) {
            result = "ERROR: Remote exception - is rmiregistry running?";
            logger.error(result,e);
            throw new RemoteException(result,e);
        } catch( MalformedURLException e ) {
            result = "ERROR: Malformed URL";
            logger.error(result,e);
            throw new RemoteException(result,e);
        } catch(Exception e) {
            result = e.toString();
            logger.error(result,e);
            throw new RemoteException(result,e);
        } catch(Error e) {
            result = e.toString();
            logger.error(result,e);
            throw new RemoteException(result,e);
        }
        
        return result;
    }
    
    // shutdown the data server for a  specified site.
        /* (non-Javadoc)
         * @param site
         * the name of the site to be shutdown.
         * The name must be one of the currently running sites.
         * @return
         * the status string return will begin with "OK:" if the action succeeded
         * and "ERROR:" if any error occured.
         * @see rmiServer.monitor.MonitorInterface#shutdown(java.lang.String)
         */
    public synchronized String shutdown(String site)
    throws RemoteException {
        
        String result;
        
        try {
            logger.info("Stopping server for site "+site);
            Server server = servers.get(site);
            if ( server != null ) {
                server.stop();
                servers.remove(site);
                logger.info("Server for site "+site+" shutdown");
                result = "OK: Server for "+site+" shutdown";
            }
            else {
                result = "ERROR: Not monitoring server for site "+site+", can't terminate.";
                logger.error("Not monitoring server for site "+site+", can't terminate.");
                throw new RemoteException(result);
            }
        } catch( NotBoundException e) {
            result = "ERROR: No server bound for site "+site;
            logger.error(result,e);
            throw new RemoteException(result,e);
        } catch( RemoteException e ) {
            result = "ERROR: Error invoking shutdown for site "+site;
            logger.error(result,e);
            throw new RemoteException(result,e);
        }
        
        return result;
    }
    
        /* (non-Javadoc)
         * @param site
         * the name of the site to be restarted.
         * The name must be one of the currently running sites.
         * @return
         * the status string return will begin with "OK:" if the action succeeded
         * and "ERROR:" if any error occured.
         * @see rmiServer.monitor.MonitorInterface#restart(java.lang.String)
         */
    public String restart(String site)
    throws RemoteException {
        
        String result;
        
        result = shutdown(site);
        result = start(site);
        
        return result;
    }
    
        /* (non-Javadoc)
         * @param site
         * the name of the site to be started.
         * The site must be defined in the properties file.
         * @return
         * the status string return will begin with "OK:" if the action succeeded
         * and "ERROR:" if any error occured.
         * @see rmiServer.monitor.MonitorInterface#start(java.lang.String)
         */
    public synchronized String start(String site)
    throws RemoteException {
        String result;
        try {
            System.out.println("//localhost/"+site+"."+RmiServerInterface.bindName);
            RmiServerInterface i = (RmiServerInterface) 
                Naming.lookup("//localhost/"+site+"."+RmiServerInterface.bindName);
            i.getLogLevel();
            result = "ERROR: Server for "+site+" is already running";
            System.out.println(result);
            logger.error(result);
            throw new RemoteException(result);
        } catch( MalformedURLException e ) {
            result = "ERROR: Malformed URL for registry lookup";
            System.out.println(result);
            logger.error(result,e);
            throw new RemoteException(result,e);
        } catch( NotBoundException e) {
            Server server = new Server(site, properties);
            if ( server.getProcess(5000) != null ) {
                servers.put(site, server);
                result = "OK: Server for "+site+" started";
                System.out.println(result);
                logger.info(result);
            } else {
                result = "ERROR: Failed to exec server process for "+site;
                System.out.println(result);
                logger.error(result);
                throw new RemoteException(result);
            }
        } catch( RemoteException e ) {
            result = "ERROR: Error invoking lookup for site "+site;
            System.out.println(result);
            logger.error(result,e);
            throw new RemoteException(result,e);
        }
        
        return result;
    }
    
    public Properties getProperties()
    throws RemoteException {
        return MonitorMain.getProperties();
    }
    
        /* (non-Javadoc)
         * Replaces the properties and then restarts each server to ensure they are using
         * the new properties.
         * @return
         * true if the new properties was set.
         * @see rmiServer.monitor.MonitorInterface#setProperties(java.util.Properties)
         */
    public boolean setProperties(Properties p)
    throws RemoteException {
        
        boolean result = MonitorMain.setProperties(p);
        
        // restart each of the servers so they get the new properties.
        String[] sites = listServers();
        for(int i=0; i<sites.length; i++) {
//        for(String site:servers.keySet()) {
//            String site = (String) i.next();
            restart(sites[i]);
        }
        
        return result;
    }
    
        /* (non-Javadoc)
         * @see rmiServer.monitor.MonitorInterface#listServers()
         */
    public synchronized String[] listServers() {
        return servers.keySet().toArray(new String[]{});
    }
    
        /* (non-Javadoc)
         * @return
         * always returns 1, to indicate that the RMI interface is up and ready
         * @see rmiServer.monitor.MonitorInterface#status()
         */
    public int status() {
        return 1;
    }
    
    public Level getLevel() {
        if ( logger.getLevel() == null ) 
            return logger.getEffectiveLevel();
        else
            return logger.getLevel();
    }
    
    public void setLevel(Level level) {
        logger.setLevel(level);
    }
}

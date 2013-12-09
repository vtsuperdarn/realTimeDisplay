/*
 * RmiLogger.java
 *
 * Created on 18 October 2007, 14:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.rmiServer.rmilogger;

import uk.ac.le.sppg.superdarn.rmiServer.rmilogger.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 *
 * @author nigel
 */
public class RmiLogger 
        extends UnicastRemoteObject
        implements RmiLoggerInterface {
    
    Logger logger = Logger.getLogger(RmiLogger.class);
    
    /** Creates a new instance of RmiLogger */
    public RmiLogger() 
    throws RemoteException, MalformedURLException {
        // bind the server with the RMI server
        Naming.rebind( bindName, this );
    }

    public boolean isTraceEnabled() throws RemoteException {
        return logger.isTraceEnabled();
    }

    public void trace(Object message) throws RemoteException {
        logger.trace(message);
    }

    public void trace(Object message, Throwable t) throws RemoteException {
        logger.trace(message, t);
    }

    public void debug(Object message) throws RemoteException {
        logger.debug(message);
    }

    public void debug(Object message, Throwable t) throws RemoteException {
        logger.debug(message, t);
    }

    public void error(Object message) throws RemoteException {
        logger.error(message);
    }

    public void error(Object message, Throwable t) throws RemoteException {
        logger.error(message, t);
    }

    public void fatal(Object message) throws RemoteException {
        logger.fatal(message);
    }

    public void fatal(Object message, Throwable t) throws RemoteException {
        logger.fatal(message, t);
    }

    public void info(Object message) throws RemoteException {
        logger.info(message);
    }

    public void info(Object message, Throwable t) throws RemoteException {
        logger.info(message, t);
    }

    public void warn(Object message) throws RemoteException {
        logger.warn(message);
    }

    public void warn(Object message, Throwable t) throws RemoteException {
        logger.warn(message, t);
    }

    public void log(Priority l, Object message) {
        logger.log(l, message);
    }

    public void log(Priority l, Object message, Throwable t) {
        logger.log(l, message, t);
    }

    public Level getLevel() {
        if ( logger.getLevel() == null ) 
            return logger.getEffectiveLevel();
        else
            return logger.getLevel();
    }

    public void setLevel(Level level) {
        logger.info("log level set to "+level);
        logger.setLevel(level);
    }

    public void shutdown() throws RemoteException {
        logger.info("shutdown requested.");
        LogManager.shutdown();
        try {
            Naming.unbind(bindName);
        } catch (Exception ex) {
            throw new RemoteException("Failed to unbind logger", ex);
        }
        UnicastRemoteObject.unexportObject(this, true);
    }

    public int status() throws RemoteException {
        return 1;
    }
    
}

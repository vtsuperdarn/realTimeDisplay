/*
 * RmiLoggerInterface.java
 *
 * Created on 17 October 2007, 16:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.rmiServer.rmilogger;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;

/**
 *
 * @author nmw
 */
public interface RmiLoggerInterface extends Remote {
    
    public static final int majorVersion = 1;
    public static final int minorVersion = 0;
    public static final String versionString = String.valueOf(majorVersion) + "." + String.valueOf(minorVersion);
    public static final String bindName = "Logger_" + versionString;

    final static Level[] levels = new Level[] { 
        Level.ALL, Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL, Level.OFF
    };

    public boolean isTraceEnabled() throws RemoteException;
    
    public void trace(Object message) throws RemoteException;
    public void trace(Object message, Throwable t) throws RemoteException;
    
    public void debug(Object message) throws RemoteException;
    public void debug(Object message, Throwable t) throws RemoteException;
    
    public void error(Object message) throws RemoteException;
    public void error(Object message, Throwable t) throws RemoteException;
    
    public void fatal(Object message) throws RemoteException;
    public void fatal(Object message, Throwable t) throws RemoteException;
    
    public void info(Object message) throws RemoteException;
    public void info(Object message, Throwable t) throws RemoteException;
    
    public void warn(Object message) throws RemoteException;
    public void warn(Object message, Throwable t) throws RemoteException;
    
    public void log(Priority l, Object message) throws RemoteException;
    public void log(Priority l, Object message, Throwable t) throws RemoteException;
    
    public Level getLevel() throws RemoteException;
    public void setLevel(Level level) throws RemoteException;
    
    public void shutdown() throws RemoteException;
    public int status() throws RemoteException;
    
}

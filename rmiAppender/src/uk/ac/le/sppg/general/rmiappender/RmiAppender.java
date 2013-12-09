/*
 * RmiAppender.java
 *
 * Created on 18 October 2007, 16:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.general.rmiappender;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ThrowableInformation;
import uk.ac.le.sppg.superdarn.rmiServer.rmilogger.RmiLoggerInterface;

/**
 *
 * @author nigel
 *
 * This class sends log4j messages to a RMI server.
 * The server should impliment the general.rmilogger.RmiLoggerInterface, and be
 * registered with the name RmiLoggerInterface.bindName.
 *
 */
public class RmiAppender 
extends AppenderSkeleton {
    
    private int port = 1099;
    private String host = "localhost";
    
    private RmiLoggerInterface logger;
    
    /** Creates a new instance of RmiAppender */
    public RmiAppender() {
    }

    public RmiAppender(int port, String host) {
        this.port = port;
        this.host = host;
        
        connectRmiLogger();
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return this.port;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    public String getHost() {
        return host;
    }
    
    public boolean requiresLayout() { return true;  }

    // after the options have been set we can connect to the RMI server
    // and obtain the RmiLogger.
    public void activateOptions() {
        connectRmiLogger();
    }
    

    protected void append(LoggingEvent loggingEvent) {
        Level l = loggingEvent.getLevel();
        String message = this.getLayout().format(loggingEvent);
        
        // strip \n from end of message
        message = message.trim();

        ThrowableInformation ti = loggingEvent.getThrowableInformation();
        Throwable t=null;
        if ( ti != null ) {
            t = ti.getThrowable();
        }
        
        try {
            if ( l == Level.WARN ) {
                if ( t == null ) {
                    logger.warn(message);
                }
                else {
                    logger.warn(message, t);
                }
            }
            else if ( l == Level.INFO ) {
                if ( t == null ) {
                    logger.info(message);
                }
                else {
                    logger.info(message, t);
                }
            }
            else if ( l == Level.TRACE ) {
                if ( t == null ) {
                    logger.trace(message);
                }
                else {
                    logger.trace(message, t);
                }
            }
            else if ( l == Level.DEBUG ) {
                if ( t == null ) {
                    logger.debug(message);
                }
                else {
                    logger.debug(message, t);
                }
            }
            else if ( l == Level.TRACE ) {
                if ( t == null ) {
                    logger.trace(message);
                }
                else {
                    logger.trace(message, t);
                }
            }
            else if ( l == Level.ERROR ) {
                if ( t == null ) {
                    logger.error(message);
                }
                else {
                    logger.error(message, t);
                }
            }
            else if ( l == Level.FATAL ) {
                if ( t == null ) {
                    logger.fatal(message);
                }
                else {
                    logger.fatal(message, t);
                }
            }
        }
        catch(RemoteException e) {
            errorHandler.error("Failed to log message to RmiLogger: ", e, 3, loggingEvent);
        }
            
    }

    public void close() {
    }
   
    private void connectRmiLogger() {
        String lookup = "//" + host + ":" + String.valueOf(port) + "/" + RmiLoggerInterface.bindName;
        try {
            logger = (RmiLoggerInterface)Naming.lookup(lookup);
        }
        catch(NotBoundException e) {
            errorHandler.error("RmiLogger not bound to registry: ", e, 0);
        }
        catch(RemoteException e) {
            errorHandler.error("Failed to get RmiLogger interface from registry: ", e, 1);
        }
        catch(MalformedURLException e) {
            errorHandler.error("MalformedURLException: ", e, 2);
        }
    }
        
}

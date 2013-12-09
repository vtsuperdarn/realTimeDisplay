/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.serverManager.manager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;
import uk.ac.le.sppg.processRunner.ProcessRunner;
import uk.ac.le.sppg.superdarn.rmiServer.rmilogger.RmiLoggerInterface;

/**
 *
 * @author nigel
 */
public class Logger {

    RmiLoggerInterface theLogger = null;
    Properties properties;
    static final String separator = System.getProperty("file.separator");
    StringBuffer response = null;
    String newline = System.getProperty("line.separator"); //This will retrieve line separator dependent on OS.
    
    public Logger(Properties props) {
        this.properties = props;
    }
    
    public StringBuffer start() {

//System.out.println("Logger: start");
        RmiLoggerInterface logger = check();
        String javaHome = System.getProperties().getProperty("java.home");
        String jarLocation = properties.getProperty("installDir");
        response = new StringBuffer();
        
        try {
            logger = check();

//System.out.println("Logger: first check");
            String[] process = new String[] { javaHome+"/bin/java", 
                    "-server", "-jar", jarLocation+separator+"rmiLogger.jar" };
            
            // Print the command
            for (int i=0; i<process.length; i++) {
                System.out.print(process[i]+" ");
            }
            System.out.println("");

            ProcessRunner proc = new ProcessRunner(process);                   

//System.out.println("Logger: started rmiLogger process");
            Thread.sleep(2000);

            // if the process has exited then it failed
            // if it hasn't exited then we get a IllegalThreadStateException
            // which acutally means success.
            try {
                int exitStatus = proc.exitValue();
                proc.streamHandler();
                response.append(proc.getOutput()).append(newline);
                response.append(proc.getError().toString()).append(newline);
                String errorMess = proc.getError().toString();
System.err.println("Error: Logger failed to start");
                throw new IOException("Logger process exited");
            }
            catch(IllegalThreadStateException e) {
System.out.println("Logger: illegal thread state (that's okay)");
                // ignore it, we want this exception to occur.
            }

            logger = check();
System.out.println("Logger: second check");

            if ( logger == null ) {
                response.append("Failed to start Logger");
                response.append(newline);
            }
            else {
System.out.println("Logger: started");
                return null;
            }

        }
        catch(IOException e) {
            response.append("IOException starting Logger");
            response.append(newline);
            response.append(e.getMessage());
            response.append(newline);
        }
        catch(InterruptedException e) {
            response.append("Interrupted exception caught while starting Logger");
            response.append(newline);
        }

//System.out.println("Logger: response: "+response);
        return response;
    }
    
    public StringBuffer stop() {
        response = new StringBuffer();

        RmiLoggerInterface logger = check();
        if ( logger == null ) {
            response.append( "Unable to get a RMI handle to the Logger" );
            response.append(newline);
        }
        else {
            try {
                logger.shutdown();
                int check = 0;
                do {
                    Thread.sleep(500);
                    logger = check();
                    check++;
                } while( logger != null && check < 10 );

                if ( logger != null ) {
                    response.append("Failed to shutdown Logger");
                    response.append(newline);
                }
                else {
                    return null;
                }
            }
            catch(RemoteException e) {
                response.append("Remote exception trying to shutdown Logger");
                response.append(newline);
            }
            catch(InterruptedException e) {
                response.append("Interrupted");
                response.append(newline);
            }
        }

        return response;
    }
    
    public boolean isRunning() {
        try {
            if ( theLogger == null ) 
                theLogger = (RmiLoggerInterface)Naming.lookup("//localhost/"+RmiLoggerInterface.bindName);
            
            return ( theLogger != null && theLogger.status() == 1 );

        }

        catch(Exception e) {
            return false;
        }    
    }

    public RmiLoggerInterface check() {
        try {
            if ( theLogger == null ) 
                theLogger = (RmiLoggerInterface)Naming.lookup("//localhost/"+RmiLoggerInterface.bindName);
            
            if ( theLogger != null && theLogger.status() != 1 ) 
                theLogger = null;
//System.out.println("Logger: check: "+theLogger);
        }

        catch(RemoteException e) {
            theLogger = null;
//System.out.println("Logger: check: remoteException");
        }
        catch(NotBoundException e) {
            theLogger = null;
//System.out.println("Logger: check: notBoundException");
        }
        catch(MalformedURLException e) {
            theLogger = null;
//System.out.println("Logger: check: MalformedURLException");
        }

        return theLogger;
    }
    
    public StringBuffer getResponse() {
        return response;
    }
}

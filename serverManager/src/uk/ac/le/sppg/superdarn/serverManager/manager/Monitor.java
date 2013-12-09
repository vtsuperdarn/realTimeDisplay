/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.serverManager.manager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;
import uk.ac.le.sppg.processRunner.ProcessRunner;
import uk.ac.le.sppg.superdarn.rmiServer.monitor.MonitorInterface;

/**
 *
 * @author nigel
 */
public class Monitor {

    MonitorInterface theMonitor = null;
    Properties properties;
    static final String separator = System.getProperty("file.separator");
    
    StringBuffer response = null;

    public Monitor(Properties props) {
        this.properties = props;
    }

    public StringBuffer start() {
        response = new StringBuffer();
        
        MonitorInterface monitor = check();
        String javaHome = System.getProperties().getProperty("java.home");
        String jarLocation = properties.getProperty("installDir");

        try {
            monitor = check();

            String[] process = new String[] { javaHome+"/bin/java", 
                    "-server", "-jar", jarLocation+separator+"rmiMonitor.jar" };

            ProcessRunner proc = new ProcessRunner(process);                   

            Thread.sleep(2000);

            // if the process has exited then it failed
            // if it hasn't exited then we get a IllegalThreadStateException
            // which acutally means success.
            try {
                int exitStatus = proc.exitValue();
                proc.streamHandler();
                response.append(proc.getOutput());
                response.append(proc.getError());
                throw new IOException("Monitor process exited");
            }
            catch(IllegalThreadStateException e) {
                // ignore it, we want this exception to occur.
            }

            monitor = check();

            if ( monitor == null ) {
                response.append("Failed to start Monitor");
            }
            else {
                return null;
            }

        }
        catch(IOException e) {
            response.append("IOException starting Monitor");
            response.append(e.getMessage());
        }
        catch(InterruptedException e) {
            response.append("InterruptedException starting Monitor");
            response.append(e.getMessage());
        }

        return response;
    }
    
    public StringBuffer stop() {
        response = new StringBuffer();

        MonitorInterface monitor = check();
        if ( monitor == null ) {
            response.append( "Unable to get a RMI handle to the Monitor" );
        }
        else {
            try {
                monitor.shutdown();
                int check = 0;
                do {
                    Thread.sleep(500);
                    monitor = check();
                    check++;
                } while( monitor != null && check < 10 );

                if ( monitor != null ) {
                    response.append("Failed to shutdown Monitor");
                }
                else {
                    return null;
                }
            }
            catch(RemoteException e) {
                response.append("Remote exception trying to shutdown Monitor");
            }
            catch(InterruptedException e) {
                response.append("Interrupted");
            }
        }

        return response;
    }
    
    public boolean isRunning() {
        try {
            if ( theMonitor == null ) 
                theMonitor = (MonitorInterface)Naming.lookup("//localhost/"+MonitorInterface.bindName);
            
            return ( theMonitor != null && theMonitor.status() == 1 );

        }

        catch(Exception e) {
            return false;
        }    
    }

    public MonitorInterface check() {
        try {
            if ( theMonitor == null ) 
                theMonitor = (MonitorInterface)Naming.lookup("//localhost/"+MonitorInterface.bindName);
            
            if ( theMonitor.status() != 1 ) 
                theMonitor = null;

        }

        catch(RemoteException e) {
            theMonitor = null;
        }
        catch(NotBoundException e) {
            theMonitor = null;
        }
        catch(MalformedURLException e) {
            theMonitor = null;
        }

        return theMonitor;
    }

    public StringBuffer getResponse() {
        return response;
    }
}


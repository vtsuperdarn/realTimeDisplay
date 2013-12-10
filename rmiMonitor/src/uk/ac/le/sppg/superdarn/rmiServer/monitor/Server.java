/*
 * Created on 10-Nov-2004
 *
 */
package uk.ac.le.sppg.superdarn.rmiServer.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

import java.util.Properties;
import org.apache.log4j.Logger;
import uk.ac.le.sppg.superdarn.rmiServer.server.RmiServerInterface;

/**
 * This class creates a thread which sits in a loop. The loop starts
 * a server process for the site, then waits for it to finish.
 * This loop continues until the Server is shutdown.
 *
 */
public class Server implements Runnable {
    
    private Thread thread = null;
//    final Logger logger = Logger.getLogger(Server.class);
    static Logger logger = Logger.getRootLogger();
    String site;
    Process server;
    Properties properties;
    
    final Object monitor = new Object();
    boolean processStarted;
    
    boolean runnable;
    
    /**
     * Creates a new Server thread and server process.
     * The server process is a separate java invokation running dataServer.server.
     * The process reads data from the relevent site and caches it both internally,
     * and to disk.
     * @param site
     * the site name for which the server should get data.
     * This site must be defined in the server properties.
     * @param logger
     * a DisplayLogger for logging messages
     * @param properties
     * the server properties
     */
    public Server(String site, Properties properties) {
        // start a new server process for the site.
        // setup a thread which will monitor that process and keep restarting it
        // if it should stop for any reason.
        
        processStarted = false;
        
        this.site = site;
        this.properties = properties;
        System.out.println(runnable);
        // create a new thread which will execute the run() method.
        runnable = true;
        thread = new Thread(this, "superdarn.rmiServer.monitor."+site);
        thread.start();
    }
    
    public void run() {
        try {
            
            // loop, starting the server then wait for it to finish.
            //     if the server finishes, start another...
            while (runnable) {
                synchronized(monitor) {
                    // attempt to start the server process, and notify all threads
                    // waiting on the monitor synchronization object
                    server = runServer(site);
                    processStarted = true;
                    monitor.notifyAll();
                }
                if ( server == null )
                    return;
                
                logger.info("wait for server at "+site);
                
                // wait for the server process to finish...
                server.waitFor();
                
                logger.info("run method detected server for "+site+" has terminted");
                
                // ... and notify all threads waiting on the monitor...
                synchronized(monitor) {
                    server = null;
                    processStarted = false;
                    monitor.notifyAll();
                }
                
                logger.info("server terminated for "+site);
                
            }
        } catch (InterruptedException e){
            logger.error("Monitor interrupted for site "+site, e);
        }
    }
    
    // stop this monitor thread and the associated server process.
    public void stop()
    throws NotBoundException, RemoteException {
        
        // interrupt the monitoring thread so it won't restart the server.
        runnable = false;
        thread.interrupt();
        
        TimedWait timer;
        
        // stop the data server process
        try {
            // request an orderly shutdown of the server process...
            RmiServerInterface local = (RmiServerInterface)Naming.lookup("//localhost/"+site+"."+RmiServerInterface.bindName);
            local.shutdown();
            
            // start the timer...
            timer = new TimedWait(Thread.currentThread(), 5000L);
            
            // wait for the server process to end.
            server.waitFor();
            
            // cancel the timer.
            timer.cancel();
        } catch(InterruptedException e) {
            // the timer fired before the process shutdown...
            // the process didn't terminate when it should have.
            // Perform a disorderly shutdown.
            server.destroy();
            try {
                Naming.unbind("//localhost/"+site+"."+RmiServerInterface.bindName);
            } catch( MalformedURLException e2 ) {
            }
        } catch( MalformedURLException e ) {
        }
    }
    
    
    /**
     * Get the Process which is running the data server.
     * @param timeout
     * the maximum amount of time to wait in milliseconds for the
     * monitor thread to start the server process.
     * @return
     * the data server Process.
     */
    public Process getProcess(long timeout) {
        
        synchronized(monitor) {
            if ( ! processStarted ) {
                try {
                    monitor.wait(timeout);
                } catch(InterruptedException e) {}
            }
        }
        
        return server;
    }
    
    // procedure to start the data server process.
    private Process runServer(String site) {
        Process result = null;
        String javaHome = System.getProperty("java.home");
        String separator = System.getProperty("file.separator");
        String installDir = properties.getProperty("installDir");
        
        
        try {
            result = Runtime.getRuntime().exec(new String[] {
                javaHome+"/bin/java", "-server", "-jar", installDir+separator+"rmiServer.jar", site});
            
            Thread.sleep(2000);
            
            BufferedReader procStderr = new BufferedReader(new InputStreamReader(result.getErrorStream()));
            while ( procStderr.ready() ) {
                logger.info(procStderr.readLine());
            }
            BufferedReader procStdout = new BufferedReader(new InputStreamReader(result.getInputStream()));
            while ( procStdout.ready() ) {
                logger.info(procStdout.readLine());
            }
            
            // here we want to throw an IOException if the following code doesn't throw
            // and IllegalThreadState.
            // Unfortunately there is no easy way to test if the process has terminated,
            // the only method is to read the process exit value.
            // If the process is still running we get the Exception thrown and if it's
            // terminated it succeeds.
            try {
                // use this to test if the process has died.
                result.exitValue();
                result = null;
                throw new IOException();
            } catch(IllegalThreadStateException e) {}
            
            logger.info("started server for "+site);
        } catch(IOException e) {
            logger.error("Failed to start server for site "+site, e);
        } catch(InterruptedException e){}
        
        return result;
    }
    
    // class to implement a timer.
    class TimedWait {
        Timer timer;
        public TimedWait(Thread p, long t) {
            timer = new Timer();
            timer.schedule(new InterruptWait(p), t);
        }
        public void cancel() {
            timer.cancel();
        }
        
        class InterruptWait extends TimerTask {
            Thread t;
            public InterruptWait(Thread o) {
                t = o;
            }
            public void run() {
                t.interrupt();
                timer.cancel();
            }
        }
    }
    
    
}

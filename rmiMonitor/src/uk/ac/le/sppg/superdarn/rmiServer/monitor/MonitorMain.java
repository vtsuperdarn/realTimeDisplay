/*
 * Created on 10-Nov-2004
 *
 */
package uk.ac.le.sppg.superdarn.rmiServer.monitor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import uk.ac.le.sppg.superdarn.rmiServer.server.RmiServerInterface;

/**
 * the Monitor process starts each data server and then monitors the process, restarting
 * it if it fails for any reason.
 * The Monitor process is an RMI server which accepts commands from the Manager, or
 * other applications. The RMI server is used to start,stop and monitor data servers
 * for each radar/site which it supports.
 */
public class MonitorMain {
    
    // properties for the Monitor are read from a properties file.
    // This info contains the radar sites and bands which should be monitored.
    static Properties properties = null;
    
    
    // a list of RMI servers, one for ourselves (Monitor) and one for each sites dataServer.
    static ArrayList<RmiMonitor> rmiServers = new ArrayList<RmiMonitor>();
    
    static final String separator = System.getProperty("file.separator");
        
    
    public static void main(String[] args) {
        
        // load the properties
        File classPath = new File(System.getProperty("java.class.path"));
        String installDir;
        
        // find the classpath, this will normally be the directory in which
        // the jar file is installed, so the properties should be in the
        // same directory.
        if ( classPath.isDirectory() ) {
            installDir = classPath.getAbsolutePath();
        } else {
            installDir = classPath.getAbsoluteFile().getParent();
        }
                
        InputStream in = null;
        
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty("installDir",installDir);
        
       
        
        try {
            
            // load default properties from the FitLocalServer jar file.
            
            in = RmiServerInterface.class.getResourceAsStream("/default.properties");
            defaultProperties.load(in);
            in.close();
            
            // create the application properties using the default properties as base.
            properties = new Properties(defaultProperties);

            // now try to load a properties file which may override defaults.
            try {
                in = new FileInputStream(installDir+separator+"properties");
                properties.load(in);
                in.close();
            } catch( IOException e) {
                // no properties file, not a problem
            }
        } catch( IOException e) {
            properties = new Properties(defaultProperties);
            System.out.println("No default properties found");
        }

        // if the default log4j configuration has not been set in the 
        // properties then set it:
        if ( properties.getProperty("monitor.log4j.configuration") != null ) {
            // set log4j configuration to the specified property.  
            System.setProperty("log4j.configuration", System.getProperty("monitor.log4j.configuration"));
        }
        else if ( System.getProperty("log4j.configuration") == null ) {
            // first look for an overriding file called log4j.configuration
            File log4jConfig = new File(installDir+separator+"log4j.configuration");
            if ( log4jConfig.isFile() ) {
                System.setProperty("log4j.configuration", "file://"+log4jConfig.getAbsolutePath());
            }
            else {
                // no configuration file so use the one in the jar if there is one.
                System.setProperty("log4j.configuration", "log4j.configuration");
            }
        }
         
        // the Logger is used to output logging information.
        final  Logger logger = Logger.getLogger(MonitorMain.class);
        
        
        try {
            String[] rmiServerNames = new String[0];
            
            // check the rmi registry is running
            final int maxTries = 5;
            for(int i=1; i<=maxTries; i++ ) {
                try {
                    rmiServerNames = Naming.list("//localhost");
                    break;
                } catch( ConnectException e) {
                    if ( i==maxTries ) {
                        logger.error("rmiregistry not running");
                        System.err.println("rmiregistry not running");
                        System.exit(1);
                    }
                    Thread.sleep(1000);
                }
            }
            
            // create an instance of the Monitor RMI server.
            RmiMonitor rmiServer = new RmiMonitor(properties);
            rmiServers.add(rmiServer);
            logger.info("monitor running");
            
            // step through each name which is registered.
            // For any data servers see if we can attach them to the list
            
            Pattern p = Pattern.compile(".*/([^/]+)\\."+RmiServerInterface.bindName);
            
            for(String rmiName:rmiServerNames) {
                if ( rmiName.endsWith(RmiServerInterface.bindName)) {
                    Matcher m = p.matcher(rmiName);
//                    System.out.println(m.matches());
//                    System.out.println("group 0: "+m.group(0));
                    if ( m.matches() && m.groupCount() > 0 ) {
                        String site = m.group(1);
                        logger.info("Server for site "+site+" is already registered.");
                        
                        RmiServerInterface server = (RmiServerInterface) Naming.lookup("//localhost/"+site+"."+RmiServerInterface.bindName);
                        try {
                            server.shutdown();
                            rmiServer.start(site);                            
                        }
                        catch(RemoteException e) {
                            logger.error("Failed to restarte site "+site, e);
                        }
                    }
//                    System.out.println("group 1: "+site);
                    
                }
            }
            
            
        } catch( RemoteException re ) {
            System.err.println( "Remote exception - is rmiregistry running?");
            re.printStackTrace();
        } catch( Exception e ) {
            //System.err.println( e.getMessage());
            e.printStackTrace();
        }
    }
    
    static void usage() {
        System.out.println("Usage:");
        System.out.println("java -jar dataMonitor.jar");
    }
    
    public static Properties getProperties() {
        return properties;
    }
    
    /**
     * replaces the properties for the data server.
     * @param newProperties
     * new properties, which will also be saved.
     * @return
     * true if the Properties were set and saved
     */
    public static boolean setProperties(Properties newProperties) {
        
        properties = newProperties;
        
        try {
            FileOutputStream out = new FileOutputStream("dataMonitor.properties");
            properties.store(out, "Properties for the monitor");
            out.close();
            return true;
        } catch( IOException e) {
            return false;
        }
    }
    
}

/*
 * Main.java
 *
 * Created on 17 October 2007, 16:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.rmiServer.rmilogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author nigel
 */
public class RmiLoggerMain {
    
    static final String separator = System.getProperty("file.separator");
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File classPath = new File(System.getProperty("java.class.path"));
        String installDir;
        
        if ( classPath.isDirectory() ) {
            installDir = classPath.getAbsolutePath();
        } else {
            installDir = classPath.getAbsoluteFile().getParent();
        }

        Properties properties;
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty("installDir",installDir);
        
        InputStream in = null;
        try {
            
            // load default properties from the jar file.
            
            in = RmiLoggerMain.class.getResourceAsStream("/default.properties");
            if ( in != null ) {
                defaultProperties.load(in);
                in.close();
            }
            
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
        
        try {
            
            // check the rmi registry is running
            final int maxTries = 5;
            for(int i=1; i<=maxTries; i++ ) {
                try {
                    String[] rmiServerNames = Naming.list("//localhost");
                    break;
                } catch( ConnectException e) {
                    if ( i==maxTries ) {
                        System.err.println("rmiregistry not running");
                        System.exit(1);
                    }
                    Thread.sleep(1000);
                }
            }
         
            // if the default log4j configuration has not been set in the 
            // properties then set it:
            if ( System.getProperty("log4j.configuration") == null ) {
                // first look for an overriding file called log4j.configuration
                File log4jConfig = new File(installDir+separator+"log4j.configuration");
                if ( log4jConfig.isFile() ) {
                    System.setProperty("log4j.configuration", "file://"+log4jConfig.getAbsolutePath());
                }
                else {
                    // no configuration file so use the one in the jar.
                    System.setProperty("log4j.configuration", "log4j.configuration");
                }
            }
            
                        
            // create an instance of the Logger RMI server.
            RmiLogger rmiLogger = new RmiLogger();
            if ( rmiLogger.getLevel() == null )
                rmiLogger.setLevel(Level.INFO);
            
            rmiLogger.info("logger is enabled");
                        
        } catch( RemoteException re ) {
            System.err.println( "Remote exception - is rmiregistry running?");
            re.printStackTrace();
        } catch( Exception e ) {
            //System.err.println( e.getMessage());
            e.printStackTrace();
        }        
//    System.out.println("Logger main thread ending");
    }
}

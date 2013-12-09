/*
 * Created on 17-May-2004
 */
package uk.ac.le.sppg.superdarn.rmiServer.server;

import uk.ac.le.sppg.coords.Site;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.log4j.Logger;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;

/**
 * @author Nigel Wade
 */
public class RmiServerMain  {
    
    
    static final String separator = System.getProperty("file.separator");
    

    public static void main(String[] args) {
        
        
        if ( args.length == 0 ) {
            usage();
            System.exit(1);
        }
        
        File classPath = new File(System.getProperty("java.class.path"));
        String installDir;
        
        if ( classPath.isDirectory() ) {
            installDir = classPath.getAbsolutePath();
        } else {
            installDir = classPath.getAbsoluteFile().getParent();
        }
        
        
        // first try to load from a properties file
        InputStream in = null;
        
        Properties properties;
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty("installDir",installDir);
        
        try {
            
            // load default properties from the FitLocalServer jar file.
            
            in = RmiServerMain.class.getResourceAsStream("/default.properties");
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
        if ( properties.getProperty("server.log4j.configuration") != null ) {
            // set log4j configuration to the specified property.  
            System.setProperty("log4j.configuration", System.getProperty("server.log4j.configuration"));
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
        
        final Logger logger = Logger.getLogger("rmiServer");

        String serverHost = properties.getProperty("fitServer");
        if ( serverHost == null ) {
            System.err.println("No fitServer host specified in properties for reading fit data");
        }
        
        try {
            
            // check the rmi registry is running
            final int maxTries = 5;
            for(int i=1; i<=maxTries; i++ ) {
                try {
                    Naming.list("//localhost");
                    break;
                } catch( ConnectException e) {
                    if ( i==maxTries ) {
                        System.err.println("rmiregistry not running");
                        System.exit(1);
                    }
                    Thread.sleep(1000);
                }
            }
            
            for( int i=0; i<args.length; i++ ) {
                
                String siteName = args[i];
                
                // check the radar name is known...
                Site site = SuperDarnSiteList.getList().get(siteName);
                if (site == null) {
                    throw new Exception(
                            "Site " + siteName + " is not recognized");
                }
                
                // check the propeties for the site.
                if ( properties.getProperty(siteName+".ports") == null ||
                       properties.getProperty(siteName+".channels") == null ) {
                    throw new Exception(
                            "Site " + siteName + " has no ports/channels in the properties");
                }
                
                String[] portStrings = properties.getProperty(siteName+".ports").split(" ");
                String[] channelStrings = properties.getProperty(siteName+".channels", "").split(" ");
                
                if ( portStrings.length != channelStrings.length ) {
                    throw new Exception(
                            "Site " + siteName + " has unmatched ports/channels");
                }
                
                
                // create a new RmiServer to handle this site.
                // Each site has an instance of RmiServer to handle its data, each registered under the site name.
                RmiServer rmiServer = new RmiServer(site, properties.getProperty("cacheDir","/"));
                
                logger.info("bind as : "+site.getCompactName());
                rmiServer.bind();
                
                
                for(int j=0; j<portStrings.length; j++) {
                    int port = Integer.parseInt(portStrings[j]);
                    rmiServer.addChannel( serverHost, Enum.valueOf(ChannelId.class,channelStrings[j]), port);
                    logger.info("RMI data server started for : "+site.getName()+" :"+channelStrings[j]);
                }
            }
        } catch(  NumberFormatException nfe ) {
            logger.error( "NumberFormatException parsing arguments", nfe);
        } catch( RemoteException re ) {
            System.err.println( "Remote exception - is rmiregistry running?");
            re.printStackTrace();
            logger.error("Remote exception - is rmiregistry running?", re);
        } catch( Exception e ) {
            //System.err.println( e.getMessage());
            e.printStackTrace();
            logger.error("Exception at startup", e);
        }
    }
    
    static void usage() {
        System.out.println("Usage:");
        System.out.println("java -jar rmiServer.jar site...");
    }
}

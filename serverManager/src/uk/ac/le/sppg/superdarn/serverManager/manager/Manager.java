/*
 * Created on 09-Nov-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package uk.ac.le.sppg.superdarn.serverManager.manager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

import java.io.File;
import uk.ac.le.sppg.superdarn.rmiServer.monitor.MonitorInterface;
import uk.ac.le.sppg.superdarn.rmiServer.server.RmiServerMain;

/**
 * First pass at a manager process for the real-time data server.
 * Future versions will include GUI, and remote interfaces for control
 * by servlet,web etc.
 */
public class Manager {
    
    static Properties properties = null;
    static final String separator = System.getProperty("file.separator");
    
    public static void main(String[] args) {        
        
        if ( args.length == 0 ) {
            usage();
            System.exit(1);
        }
        
        // the only supported operations so far are start, stop and restart
        // for the entire system or individual sites.
        if ( ! args[0].equals("stop") && ! args[0].equals("start") &&
                ! args[0].equals("restart") && ! args[0].equals("list") &&
                ! args[0].equals("start_registry") && ! args[0].equals("stop_registry")) {
            usage();
            System.exit(1);
        }
        
        // load the properties
        // the properties file contians configuration information.
        File classPath = new File(System.getProperty("java.class.path"));
        String installDir;
        
        if ( classPath.isDirectory() ) {
            installDir = classPath.getAbsolutePath();
        } else {
            installDir = classPath.getAbsoluteFile().getParent();
        }
        
        properties = new Properties();
        properties.setProperty("installDir",installDir);
        
        // first try to load from a properties file
        InputStream in = null;
        
        try {
            try {
                in = new FileInputStream(installDir+separator+"properties");
            } catch( IOException e) {
                // no properties file, so try to load from the default properties in the jar
                in = RmiServerMain.class.getResourceAsStream("/default.properties");
                if ( in == null )
                    throw new IOException("default.properties not found");
            }
            properties.load(in);
            in.close();
        } catch( IOException e) {
            System.out.println("No properties found");
            System.exit(1);
        }
        
        // try to get the RMI registry
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry();
            String[] remoteList = registry.list();
        }
        catch(RemoteException e) {
            // the registry is not running.
            registry = null;
        }

        if ( args[0].equals("start_registry") ||
                (args[0].equals("start") && registry == null)) {
            if ( registry == null ) {
                StringBuffer result = RegistryControl.startRegistry(installDir);
                if ( result.length() != 0 ) {
                    System.err.println(result);
                    System.exit(1);
                }
                
                if ( args[0].equals("start_registry")) {
                    System.exit(0);
                }
                else {
                    try {
                        registry = LocateRegistry.getRegistry();
                    }
                    catch(RemoteException e) {
                        // the registry is not running.
                        System.err.println("The registry failed to start.");
                        System.exit(1);

                    }
                }
            }
            else {
                System.err.println("The registry is already running.");
                System.exit(1);
            }
        }
        
        if ( args[0].equals("stop_registry")) {
            if ( registry != null ) {
                StringBuffer result = RegistryControl.stopRegistry(null);
                if ( result.length() != 0 ) {
                    System.err.println(result);
                    System.exit(1);
                }
                System.exit(0);
            }
            else {
                System.err.println("The registry is not running.");
                System.exit(1);
            }
        }
        
//        try {
//            
//            // check the rmi registry is running
//            final int maxTries = 5;
//            for(int i=1; i<=maxTries; i++ ) {
//                try {
//                    String[] rmiServerNames = Naming.list("//localhost");
//                    System.out.println("found "+rmiServerNames.length+" services running");
//                    break;
//                } catch( ConnectException e) {
//                    if ( i==maxTries ) {
//                        System.err.println("rmiregistry not running");
//                        System.exit(2);
//                    }
//                    System.out.println("Waiting for rmiregistry to start...");
//                    Thread.sleep(1000);
//                }
//            }
//            
//        } catch( RemoteException re ) {
//            System.err.println( "Remote exception - is rmiregistry running?");
//            re.printStackTrace();
//            System.exit(1);
//        } catch( Exception e ) {
//            //System.err.println( e.getMessage());
//            e.printStackTrace();
//            System.exit(1);
//        }
        
        // the registry should now be running.
        
        if ( registry == null ) {
            System.err.println("Registry is not running.");
            System.exit(1);
        }
        
        try {
            
            if ( args[0].equals("list") ) {
                
                // print RMI server names (logger, monotor, sites)
                String[] rmiServerNames = Naming.list("//localhost");
                for(int i=0; i<rmiServerNames.length; i++) {
                    System.out.println(rmiServerNames[i]);
                }
            }
            
            Logger logger = new Logger(properties);
            Monitor monitor = new Monitor(properties);

            if ( args[0].equals("stop") || args[0].equals("restart")) {
                
                // check the Monitor is running
                
                
                if ( args.length == 1 ) {
                    StringBuffer errorMessage;
                    
                    // this stops all site servers and the Monitor
                    if ( monitor.check() != null ) {
                        monitor.stop();
                    }
                    if ( logger.check() != null ) {
                        if ( (errorMessage = logger.stop()) != null ) {
                            System.err.println("Manager: failed to stop the Logger: "+errorMessage);
                        }
                    }
                    String[] rmiServerNames = Naming.list("//localhost");
                    for(int i=0; i<rmiServerNames.length; i++) {
                        Naming.unbind(rmiServerNames[i]);
                    }
                    
                    // stop the registry if shutting down
                    if ( args[0].equals("stop") )
                        RegistryControl.stopRegistry(null);
                    
                } else {
                    // stop the sites requested
                    MonitorInterface monitorInterface = monitor.check();
                    
                    for(int i=1; i<args.length; i++) {
                        stopServer(monitorInterface, args[i]);
                        String[] rmiServerNames = Naming.list("//localhost");
                        for(int j=0; j<rmiServerNames.length; j++) {
                            if ( rmiServerNames[i].equals("//localhost/"+args[i]+".rmiServer"))
                                Naming.unbind(rmiServerNames[i]);
                        }
                    }
                }
                
                
            }
            
            if ( args[0].equals("start") || args[0].equals("restart") ) {
                
                // start the Logger first.
                StringBuffer errorMessage;
                
                if ( (errorMessage = logger.start()) != null ) {
                    System.err.println("Manager failed to start the Logger: ");
                    System.err.print(errorMessage);
                    System.exit(2);
                }
                
                // then the Monitor.

                if ( (errorMessage = monitor.start()) != null ) {
                    System.err.println("Manager failed to start the Monitor: "+errorMessage);
                    System.exit(2);
                }
                MonitorInterface monitorInterface = monitor.check();
                
                // if there are at least 2 args the remainder are the site names to be started
                if ( args.length == 1 ) {
                    // otherwise start all the servers listed in the properties file.
                    String[] serverList = properties.getProperty("dataServers","").split(" ");
                    for(int i=0; i<serverList.length; i++) {
                        System.out.println("Starting requested server for site "+serverList[i]);
                        int ret = startServer(monitorInterface, serverList[i]);
                        System.out.println("ret values is "+ret);
                    }
                } else {
                    // start the sites requested
                    for(int i=1; i<args.length; i++) {
                        System.out.println("Starting requested server for site "+args[i]);
                        startServer(monitorInterface, args[i]);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        System.exit(0);
    }
    
    static void usage() {
        System.out.println("Usage:");
        System.out.println("java -jar serverManager.jar <action>");
        System.out.println("");
        System.out.println("    Where <action> is one of the following:");
        System.out.println("        start [site]");
        System.out.println("          starts the server manager.");
        System.out.println("          site is a list of space separated radar site names to be started (defaults to all).");
        System.out.println("");
        System.out.println("        stop [site]");
        System.out.println("          stops the server manager.");
        System.out.println("          site is a list of space separated radar site names to be started (defaults to all).");
        System.out.println("");
        System.out.println("        restart [site]");
        System.out.println("          restarts the server manager.");
        System.out.println("          site is a list of space separated radar site names to be started (defaults to all).");
        System.out.println("");
        System.out.println("        list");
        System.out.println("          prints RMI servers (monitor, logger, sites).");
        System.out.println("");
        System.out.println("        start_registry");
        System.out.println("          starts the RMI registry.");
        System.out.println("");
        System.out.println("        stop_registry");
        System.out.println("          stops the RMI registry (first step).");
        System.out.println("");
    }
    
//    private static RmiLoggerInterface checkLogger()
//    throws RemoteException, MalformedURLException {
//        try {
//            RmiLoggerInterface logger = (RmiLoggerInterface)Naming.lookup("//localhost/"+RmiLoggerInterface.bindName);
//            return logger;
//        } catch(NotBoundException e) {
//            return null;
//        } catch(ConnectException e) {
//            return null;
//        }
//    }
//    
//    private static RmiLoggerInterface startLogger()
//    throws RemoteException, MalformedURLException, IOException, InterruptedException {
//        // make sure the Manager is running
//        String javaHome = System.getProperties().getProperty("java.home");
//        final int maxTries = 5;
//        RmiLoggerInterface logger = null;
//        
//        logger = checkLogger();
//        
////		String output;
//        
//        String jarLocation = properties.getProperty("installDir");
//        for(int i=0; logger==null && i<maxTries; i++ ) {
//            
//            System.out.println("attempting to start Logger");
//            Process proc = Runtime.getRuntime().exec(new String[] {
//                javaHome+"/bin/java", "-server", "-jar", jarLocation+separator+"rmiMonitor.jar" } );
//            
//            Thread.sleep(2000);
//            
//            BufferedReader procStderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
//            while ( procStderr.ready() ) {
//                System.err.println(procStderr.readLine());
//            }
//            BufferedReader procStdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//            while ( procStdout.ready() ) {
//                System.out.println(procStdout.readLine());
//            }
//            
//            monitor = checkMonitor();
//            
//            if ( monitor == null ) {
//                System.err.println("failed to start dataMonitor");
//                procStderr.close();
//                procStdout.close();
//                proc.destroy();
//            }
//        }
//        
//        return monitor;
//    }
//
//    private static MonitorInterface checkMonitor()
//    throws RemoteException, MalformedURLException {
//        try {
//            MonitorInterface monitor = (MonitorInterface)Naming.lookup("//localhost/"+MonitorInterface.bindName);
//            monitor.status();
//            return monitor;
//        } catch(NotBoundException e) {
//            return null;
//        } catch(ConnectException e) {
//            return null;
//        }
//    }
//    
//    private static MonitorInterface startMonitor()
//    throws RemoteException, MalformedURLException, IOException, InterruptedException {
//        // make sure the Manager is running
//        String javaHome = System.getProperties().getProperty("java.home");
//        final int maxTries = 5;
//        MonitorInterface monitor = null;
//        
//        monitor = checkMonitor();
//        
////		String output;
//        
//        String jarLocation = properties.getProperty("installDir");
//        for(int i=0; monitor==null && i<maxTries; i++ ) {
//            
//            System.out.println("attempting to start Monitor");
//            Process proc = Runtime.getRuntime().exec(new String[] {
//                javaHome+"/bin/java", "-server", "-jar", jarLocation+separator+"rmiMonitor.jar" } );
//            
//            Thread.sleep(2000);
//            
//            BufferedReader procStderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
//            while ( procStderr.ready() ) {
//                System.err.println(procStderr.readLine());
//            }
//            BufferedReader procStdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//            while ( procStdout.ready() ) {
//                System.out.println(procStdout.readLine());
//            }
//            
//            monitor = checkMonitor();
//            
//            if ( monitor == null ) {
//                System.err.println("failed to start dataMonitor");
//                procStderr.close();
//                procStdout.close();
//                proc.destroy();
//            }
//        }
//        
//        return monitor;
//    }
    
    private static int stop(MonitorInterface monitor) {
        try {
            System.out.println(monitor.shutdown());
            return 0;
        } catch( RemoteException e) {
            return 1;
        }
    }
    
    private static int stopServer(MonitorInterface monitor, String server) {
        try {
            System.out.println(monitor.shutdown(server));
            return 0;
        } catch( RemoteException e) {
            return 1;
        }
    }
    
    private static int startServer(MonitorInterface monitor, String server) {
        try {
            System.out.println(monitor.start(server));
            return 0;
        } catch( RemoteException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return 1;
        }
    }
    
    
}
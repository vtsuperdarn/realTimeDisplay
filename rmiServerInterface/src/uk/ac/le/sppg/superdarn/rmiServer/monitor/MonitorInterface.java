package uk.ac.le.sppg.superdarn.rmiServer.monitor;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Properties;
import org.apache.log4j.Level;


/**
 * The MonitorInterface.
 *
 * This interface is used to communicate with the Monitor using RMI.
 * <p>
 */
public interface MonitorInterface extends Remote {
    
    public static final int majorVersion = 1;
    public static final int minorVersion = 0;
    public static final String versionString = String.valueOf(majorVersion) + "." + String.valueOf(minorVersion);
    public static final String bindName = "Monitor_" + versionString;

    /**
     * restarts the data server for the specified site.
     * If the server for that site is running it is shut down first.
     * @param site
     * the name of the site.
     * @return
     * any messages generated during the restart process.
     * The messages are defined in the implementation, see the documentation
     * for the actual implementation for strings returned.
     * @throws RemoteException
     */
    public String restart(String site) throws RemoteException;
    /**
     * @param site
     * creates a server for the specified site.
     * @return
     * any messages generated during the start process.
     * The messages are defined in the implementation, see the documentation
     * for the actual implementation for strings returned.
     * @throws RemoteException
     */
    public String start(String site) throws RemoteException;
    /**
     * @return
     * any messages generated during the start process.
     * The messages are defined in the implementation, see the documentation
     * for the actual implementation for strings returned.
     * @throws RemoteException
     */
   public String shutdown(String site) throws RemoteException;
    /**
     * @return
     * any messages generated during the shutdown process.
     * The messages are defined in the implementation, see the documentation
     * for the actual implementation for strings returned.
     * @throws RemoteException
     */
    public String shutdown() throws RemoteException;
    /**
     * @return
     * a list of the servers registered with the RMI server.
     * @throws RemoteException
     */
    public String[] listServers() throws RemoteException;
    /**
     * @return
     * an integer status
     * see the documentation for the implementation to see the meaining of the
     * values returned.
     * @throws RemoteException
     */
    public int status() throws RemoteException;
    /**
     * @return
     * the properties for the Monitor
     * @throws RemoteException
     */
    public Properties getProperties() throws RemoteException;
    /**
     * @param p
     * a set of properties to replace the current properties for the server.
     * @return
     * true if the property was successfully set.
     * @throws RemoteException
     */
    public boolean setProperties(Properties p) throws RemoteException;
    
    public Level getLevel() throws RemoteException;
    public void setLevel(Level level) throws RemoteException;
    
}

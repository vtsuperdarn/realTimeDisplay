package uk.ac.le.sppg.superdarn.rmiServer.server;

import java.rmi.RemoteException;
import java.io.Serializable;
import java.rmi.Remote;

import java.util.Date;
import org.apache.log4j.Level;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;

/**
 * The FitLocal interface.
 * <p>
 * These are the RMI calls which the rmiServer provides.
 * @author Nigel Wade
 */
public interface RmiServerInterface extends Remote {
    
    //	an enumerated class for the remote Method code
    
    public static final int majorVersion = 1;
    public static final int minorVersion = 0;
    public static final String versionString = String.valueOf(majorVersion) + "." + String.valueOf(minorVersion);
    public static final String bindName = "rmiServer_" + versionString;
            
    public class Method implements Serializable {
        private static final long serialVersionUID = 0x525350504700000CL;
        
        private static final int unknown = -1;
        private static final int checkNet = 1;
        private static final int getTime = 2;
        private static final int latest = 3;
        private static final int latestBeam = 4;
        private static final int nextTimeTimeout = 5;
        private static final int nextTimeBeamTimeout = 6;
        private static final int oldest = 7;
        private static final int oldestBeam = 8;
        private static final int previousTime = 9;
        private static final int previousTimeBeam = 10;
        private static final int getChannels = 11;
        
        private final int index;
        private Method(int i) {
            index = i;
        };
        
        public static final Method UNKNOWN = new Method(unknown);
        public static final Method CHECKNET = new Method(checkNet);
        public static final Method GETTIME = new Method(getTime);
        public static final Method LATEST = new Method(latest);
        public static final Method LATESTBEAM = new Method(latestBeam);
        public static final Method NEXTTIMETIMEOUT =
                new Method(nextTimeTimeout);
        public static final Method NEXTTIMEBEAMTIMEOUT =
                new Method(nextTimeBeamTimeout);
        public static final Method OLDEST = new Method(oldest);
        public static final Method OLDESTBEAM = new Method(oldestBeam);
        public static final Method PREVIOUSTIME = new Method(previousTime);
        public static final Method PREVIOUSTIMEBEAM =
                new Method(previousTimeBeam);
        public static final Method GETCHANNELS = new Method(getChannels);
        
        public boolean equals(int i) {
            return (i == index);
        }
        public boolean equals(Method c) {
            return (c.index == index);
        }
        
        private Object readResolve() throws java.io.ObjectStreamException {
            switch (index) {
                case checkNet :
                    return CHECKNET;
                case getTime :
                    return GETTIME;
                case latest :
                    return LATEST;
                case latestBeam :
                    return LATESTBEAM;
                case nextTimeTimeout :
                    return NEXTTIMETIMEOUT;
                case nextTimeBeamTimeout :
                    return NEXTTIMEBEAMTIMEOUT;
                case oldest :
                    return OLDEST;
                case oldestBeam :
                    return OLDESTBEAM;
                case previousTime :
                    return PREVIOUSTIME;
                case previousTimeBeam :
                    return PREVIOUSTIMEBEAM;
                case getChannels :
                    return GETCHANNELS;
                default :
                    return UNKNOWN;
            }
            
        }
        
    }
    
    /**
     * returns the fit data for a specific channel and time.
     * If that data is not available it returns <code>null</code>.
     * @param channel
     * the id of the data channel.
     * @param time
     * the time for which the data is required.
     * @see java.util.Date#getTime
     * @return
     * a <code>FitacfData</code> object or <code>null</code>.
     * @throws RemoteException
     */
    public FitacfData get(ChannelId channel, long time) throws RemoteException;
    
    /**
     * returns the next fit data from beam <code>beamNumber</code>
     * after the time <code>afterTime</code>. It will wait a maximum time
     * of <code>timeoutSecs</code> after which it will return <code>null</code>.
     *
     * @param channel
     * the id of the data channel.
     * @param afterTime
     * the time after which data should be returned.
     * @param beamNumber
     * the beam number of interest.
     * @param timeoutSecs
     * the maximum time to wait for new data in seconds.
     * @return
     * a <code>FitacfData</code> object or <code>null</code>.
     * @throws RemoteException
     * @see java.util.Date#getTime
     */
    public FitacfData next(ChannelId channel, long afterTime, int beamNumber, int timeoutSecs)
    throws RemoteException;
    /**
     * returns a fit data record from beam <code>beamNumber</code>
     * with time prior to <code>beforeTime</code>.
     * If no such record exists it will return <code>null</code>.
     *
     * @param channel
     * the id of the data channel.
     * @param beforeTime
     * the time prior to which data should be returned.
     * @param beamNumber
     * the beam number of interest.
     * @return
     * a <code>FitacfData</code> object or <code>null</code>.
     * @throws RemoteException
     * @see java.util.Date#getTime
     */
    public FitacfData previous(ChannelId channel, long beforeTime, int beamNumber)
    throws RemoteException;
    /**
     * returns the most recent fit record from beam <code>beamNumber</code>
     * or <code>null</code> if none exist.
     *
     * @param channel
     * the id of the data channel.
     * @param beamNumber
     * the beam number of interest.
     * @return
     * a <code>FitacfData</code> object or <code>null</code>.
     * @throws RemoteException
     */
    public FitacfData latest(ChannelId channel, int beamNumber) throws RemoteException;
    /**
     * returns the oldest fit record from beam <code>beamNumber</code>
     * or <code>null</code> if none exist.
     *
     * @param channel
     * the id of the data channel.
     * @param beamNumber
     * the beam number of interest.
     * @return
     * a <code>FitacfData</code> object or <code>null</code>.
     * @throws RemoteException
     */
    public FitacfData oldest(ChannelId channel, int beamNumber) throws RemoteException;
    
    /**
     * returns the next fit record with time later than <code>afterTime</code>.
     * It will wait a maximum of <code>timeout</code> seconds for the
     * data to become available after which it will return <code>null</code>.
     *
     * @param channel
     * the id of the data channel.
     * @param afterTime
     * time after which data is required
     * @param timeout
     * the maxmimum time to wait in seconds
     * @return
     * a <code>FitacfData</code> object or <code>null</code>.
     * @throws RemoteException
     * @see java.util.Date#getTime
     */
    public FitacfData next(ChannelId channel, long afterTime, int timeout) throws RemoteException;
    /**
     * returns the fit data immediately prior to time <code>beforeTime</code>
     * or <code>null</code> if no such record exists.
     *
     * @param channel
     * the id of the data channel.
     * @param beforeTime
     * time before which data is required
     * @return
     * a <code>FitacfData</code> object or <code>null</code>.
     * @throws RemoteException
     * @see java.util.Date#getTime
     */
    public FitacfData previous(ChannelId channel, long beforeTime) throws RemoteException;
    /**
     * returns the most recent fit data, or null if none exist.
     * @param channel
     * the id of the data channel.
     * @return
     * a <code>FitacfData</code> object or <code>null</code>.
     *
     * @throws RemoteException
     */
    public FitacfData latest(ChannelId channel) throws RemoteException;
    /**
     * returns the oldest fit data, or <code>null</code> if none
     * exist.
     * @param channel
     * the id of the data channel.
     * @return
     * a <code>FitacfData</code> object or <code>null</code>.
     * @throws RemoteException
     */
    public FitacfData oldest(ChannelId channel) throws RemoteException;
    
    /**
     * tests the network connection between the data server and
     * the radar. It returns <code>true</code> if the connection
     * is ok.
     * @param channel
     * the name of the data channel ( A or B for stereo, otherwise "").
     * @return
     * <code>true</code> if the network is ok.
     * @throws RemoteException
     */
    public boolean checkNet() throws RemoteException;
    
    public ChannelId[] channels() throws RemoteException;
    
    public void shutdown() throws RemoteException;
    
    public Date startTime() throws RemoteException;
        
    public void setLogLevel(Level level) throws RemoteException;
    public Level getLogLevel() throws RemoteException;
}

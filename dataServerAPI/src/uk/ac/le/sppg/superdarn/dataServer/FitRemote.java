package uk.ac.le.sppg.superdarn.dataServer;

import java.io.IOException;
import java.io.Serializable;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;

import uk.ac.le.sppg.superdarn.fitData.NetFitData;

/**
 * The FitRemote interface.
 * <p>
 * These are the data services which the dataServlet provides.
 * @author Nigel Wade
 */
public interface FitRemote {
    
    //	an enumerated class for the remote Method code
    
    public class Method implements Serializable {
//        private static final long serialVersionUID = 0x5253505047000023L;
        private static final long serialVersionUID = 0xBEBFFD4C2C15A4CL;
        
        
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
        private static final int initStreamPrevious = 11;
        private static final int initStreamPreviousBeam = 12;
        private static final int getStream = 13;
        private static final int getChannels = 14;
        
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
        public static final Method INITSTREAMPREVIOUS =
                new Method(initStreamPrevious);
        public static final Method INITSTREAMPREVIOUSBEAM =
                new Method(initStreamPreviousBeam);
        public static final Method GETSTREAM = new Method(getStream);
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
     * returns the fit data for a specific time.
     * If that data is not available it returns <code>null</code>.
     * @param site
     * the name of the site.
     * @param channel
     * the id of the data channel (A or B for stereo, otherwise "?").
     * @param time
     * the time for which the data is required.
     * @see java.util.Date#getTime
     * @return
     * a <code>NetFitData</code> object or <code>null</code>.
     * @throws IOException
     */
    public NetFitData get(String site, ChannelId channel, long time) throws IOException;
    
    /**
     * returns the next fit data from beam <code>beamNumber</code>
     * after the time <code>afterTime</code>. It will wait a maximum time
     * of <code>timeoutSecs</code> after which it will return <code>null</code>.
     *
     * @param site
     * the name of the site.
     * @param channel
     * the id of the data channel (A or B for stereo, otherwise "?").
     * @param afterTime
     * the time after which data should be returned.
     * @param beamNumber
     * the beam number of interest.
     * @param timeoutSecs
     * the maximum time to wait for new data in seconds.
     * @return
     * a <code>NetFitData</code> object or <code>null</code>.
     * @throws IOException
     * @see java.util.Date#getTime
     */
    public NetFitData next(String site, ChannelId channel, long afterTime, int beamNumber, int timeoutSecs)
    throws IOException;
    /**
     * returns a fit data record from beam <code>beamNumber</code>
     * with time prior to <code>beforeTime</code>.
     * If no such record exists it will return <code>null</code>.
     *
     * @param site
     * the name of the site.
     * @param channel
     * the id of the data channel (A or B for stereo, otherwise "?").
     * @param beforeTime
     * the time prior to which data should be returned.
     * @param beamNumber
     * the beam number of interest.
     * @return
     * a <code>NetFitData</code> object or <code>null</code>.
     * @throws IOException
     * @see java.util.Date#getTime
     */
    public NetFitData previous(String site, ChannelId channel, long beforeTime, int beamNumber)
    throws IOException;
    /**
     * returns the most recent fit record from beam <code>beamNumber</code>
     * or <code>null</code> if none exist.
     *
     * @param site
     * the name of the site.
     * @param channel
     * the id of the data channel (A or B for stereo, otherwise "?").
     * @param beamNumber
     * the beam number of interest.
     * @return
     * a <code>NetFitData</code> object or <code>null</code>.
     * @throws IOException
     */
    public NetFitData latest(String site, ChannelId channel, int beamNumber) throws IOException;
    /**
     * returns the oldest fit record from beam <code>beamNumber</code>
     * or <code>null</code> if none exist.
     *
     * @param site
     * the name of the site.
     * @param channel
     * the id of the data channel (A or B for stereo, otherwise "?").
     * @param beamNumber
     * the beam number of interest.
     * @return
     * a <code>NetFitData</code> object or <code>null</code>.
     * @throws IOException
     */
    public NetFitData oldest(String site, ChannelId channel, int beamNumber) throws IOException;
    
    /**
     * returns the next fit record with time later than <code>afterTime</code>.
     * It will wait a maximum of <code>timeout</code> seconds for the
     * data to become available after which it will return <code>null</code>.
     *
     * @param site
     * the name of the site.
     * @param channel
     * the id of the data channel (A or B for stereo, otherwise "?").
     * @param afterTime
     * time after which data is required
     * @param timeout
     * the maxmimum time to wait in seconds
     * @return
     * a <code>NetFitData</code> object or <code>null</code>.
     * @throws IOException
     * @see java.util.Date#getTime
     */
    public NetFitData next(String site, ChannelId channel, long afterTime, int timeout) throws IOException;
    /**
     * returns the fit data immediately prior to time <code>beforeTime</code>
     * or <code>null</code> if no such record exists.
     *
     * @param site
     * the name of the site.
     * @param channel
     * the id of the data channel (A or B for stereo, otherwise "?").
     * @param beforeTime
     * time before which data is required
     * @return
     * a <code>NetFitData</code> object or <code>null</code>.
     * @throws IOException
     * @see java.util.Date#getTime
     */
    public NetFitData previous(String site, ChannelId channel, long beforeTime) throws IOException;
    /**
     * returns the most recent fit data, or null if none exist.
     * @param site
     * the name of the site.
     * @param channel
     * the id of the data channel (A or B for stereo, otherwise "?").
     * @return
     * a <code>NetFitData</code> object or <code>null</code>.
     *
     * @throws IOException
     */
    public NetFitData latest(String site, ChannelId channel) throws IOException;
    /**
     * returns the oldest fit data, or <code>null</code> if none
     * exist.
     * @param site
     * the name of the site.
     * @param channel
     * the id of the data channel (A or B for stereo, otherwise "?").
     * @return
     * a <code>NetFitData</code> object or <code>null</code>.
     * @throws IOException
     */
    public NetFitData oldest(String site, ChannelId channel) throws IOException;
    
    /**
     * tests the network connection between the data server and
     * the radar. It returns <code>true</code> if the connection
     * is ok.
     * @return
     * <code>true</code> if the network is ok.
     * @throws IOException
     */
    public boolean checkNet() throws IOException;
    
    public ChannelId[] channels(String site) throws IOException;
    
}

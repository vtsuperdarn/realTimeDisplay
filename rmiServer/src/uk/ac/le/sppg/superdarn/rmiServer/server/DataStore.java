/*
 * Created on 17-May-2004
 */
package uk.ac.le.sppg.superdarn.rmiServer.server;

import uk.ac.le.sppg.superdarn.fitData.FitacfData;
import uk.ac.le.sppg.superdarn.rmiServer.server.*;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import uk.ac.le.sppg.coords.Site;
import java.util.TreeMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;

/**
 * The DataStore class handles the real time data for a data server.
 * When a channel is added it creates a MonitorThread, which in turn
 * creates a NewDataThread to read new data from the site.
 * An OldDataThread is also created to read previously cached data from
 * disk.
 *
 * @author Nigel Wade
 */
public class DataStore
        implements RmiServerInterface {
    
    // the time for which data is cached internally in milliseconds.
    static final int CACHE_TIME = 24*60*60*1000;
    
    // the MonitorThreads, one per channel, which monitor the NewDataThreads.
    ArrayList<MonitorThread> monitorThreads = new ArrayList<MonitorThread>();
    // the OldDataThreads which read old data from disk for each channel.
    ArrayList<OldDataThread> oldDataThreads = new ArrayList<OldDataThread>();
    
    final boolean trace = false;
    
    private Date startTime = new Date();
    
    Site site;
    private RmiServer server;
    private String cacheDir;
    
    
    // allData contains the data for each channel indexed by channel
    TreeMap<ChannelId,ArrayList<FitacfData>> allData = new TreeMap<ChannelId,ArrayList<FitacfData>>();
    Logger logger;
    
    /**
     * Creates a new instance of a DataStore.
     * One DataStore is used for each server.
     * The DataStore is capable of handling ultiple channels for each site.
     * @param server
     * the RmiServer which listens for data requests.
     * @param site
     * the Site from which data is to be read and stored.
     * @param cacheDir
     * the directory where data is to be cached to disk
     * @param logger
     * the logger which is used to display messages.
     */
    public DataStore(RmiServer server, Site site, String cacheDir) {
        this.site = site;
        this.server = server;
        this.cacheDir = cacheDir;
        
        logger = Logger.getLogger("rmiServer."+site.getShortName());
        
        logger.info("Created store.");
    }
    
    /**
     * Method to add a channel to the DataStore.
     * For each channel added a MonitorThread is started which in turn starts
     * a NewDataThread which gets data for the channel for the site.
     * Also, and OldDataThread is started to read old data which has been cached
     * to disk.
     * @param serverHost
     * The hostname from which new data can be read.
     * @param channel
     * The channel identifier.
     * @param port
     * the port number on serverHost on which the data server can be reached.
     * @throws NumberFormatException
     * @throws Exception
     */
    void addChannel( String serverHost, ChannelId channel, int port )
    throws NumberFormatException, Exception {
        // check that there isn't already an instance of this site/channel
        if (allData.containsKey(channel)) {
            throw new Exception("Duplicate channel: " + channel);
        }
        
        ArrayList<FitacfData> channelData = new ArrayList<FitacfData>();
        
        // create new data reading thread
        MonitorThread monitorThread =  new MonitorThread(serverHost, port, this,
                site, channel);
        monitorThread.start();
        
        // store the monitorThread
        monitorThreads.add( monitorThread );
        
        // create old data reading thread
        OldDataThread oldDataThread = new OldDataThread(site.getShortName(), channel, this);
        oldDataThread.start();
        
        // store the oldDataThread
        oldDataThreads.add( oldDataThread );
        
        // store the instance of this site/channel
        allData.put(channel, channelData);
        
        logger.info("server running for channel "+channel+", getting data from "+serverHost+":"+port);
        
    }
    
    
    /**
     * Appends one FitacfData record to the data store.
     * @param channel
     * the channel to which the data should be added.
     * @param fit
     * the data record to be stored.
     */
    public void addData(ChannelId channel, FitacfData fit) {
        ArrayList<FitacfData> data = allData.get(channel);
        
        logger.debug("store data in channel "+channel+": "+fit.radarParms.date);
        
        if ( data != null ) {
            synchronized (data) {

                if ( fit.radarParms.stationId != site.getStationId() ) {
                    logger.error("data is from "+SuperDarnSiteList.getList().getById(fit.radarParms.stationId).getName());
                    return;
                }
                // add the new data
    //			logger.displayString("Channel "+channel+" add data for "+fit.radarParms.date);
                data.add( fit );

                data.notifyAll();
            }
        }
    }
    
    /**
     * Inserts one data record into the data store at the specified index.
     * @param channel
     * the channel to which the data should be added.
     * @param index
     * the index at which to store the data record.
     * @see ArrayList#add(int, Object)
     * @param fit
     * the data record to be stored.
     */
    public void addData(ChannelId channel, int index, FitacfData fit) {
        ArrayList<FitacfData> data = allData.get(channel);

        logger.debug("store data at index "+index+" in channel "+channel+": "+fit.radarParms.date);

        if ( data != null ) {
            synchronized (data) {

                if ( fit.radarParms.stationId != site.getStationId() ) {
                    logger.error(" data is from "+SuperDarnSiteList.getList().getById(fit.radarParms.stationId).getName());
                    return;
                }
                // add the new data
    //			logger.displayString("Channel "+channel+" add data for "+fit.radarParms.date);
                data.add( index, fit );

                data.notifyAll();
            }
        }
    }
    
    /**
     * Appends several FitacfData records to the data store.
     * @param channel
     * the channel to which the data should be added.
     * @param dataList
     * the data records to be stored.
     */
    public void addAllData(ChannelId channel, ArrayList<FitacfData> dataList) {
        ArrayList<FitacfData> data = allData.get(channel);

        logger.debug("store "+dataList.size()+" data records in channel "+channel);

        if ( data != null ) {
            synchronized (data) {

                // add the  data
    //			logger.displayString( "adding "+dataList.size()+" items ");
                data.addAll( dataList );
    //			logger.displayString( "list contains "+data.size()+" items ");

                data.notifyAll();
            }
        }
    }
    
    /**
     * Inserts several FitacfData records to the data store.
     * @param channel
     * the channel to which the data should be added.
     * @param index
     * the index at which to store the data record.
     * @see ArrayList#add(int, Object)
     * @param dataList
     * the data records to be stored.
     */
    public void addAllData(ChannelId channel, int index, ArrayList<FitacfData> dataList) {
        ArrayList<FitacfData> data = allData.get(channel);

        logger.debug("store "+dataList.size()+" data records at index "+index+" in channel "+channel);
        if ( data != null ) {
            synchronized (data) {

                // add the  data
    //			logger.displayString( "adding "+dataList.size()+" items ");
                data.addAll( index, dataList );
    //			logger.displayString( "list contains "+data.size()+" items ");

                data.notifyAll();
            }
        }
    }
    
    /**
     * Removes old data from the internal and disk cache.
     * @param channel
     */
    public void purgeData(ChannelId channel) {
        ArrayList<FitacfData> data = allData.get(channel);
        
        logger.debug("purging channel "+channel);

        if ( data != null ) {
            synchronized (data) {

                if (data.size() > 0) {
                    FitacfData lastFit = data.get(data.size() - 1);

                    // remove old data from more than 24hours ago
                    Date firstRequired = new Date(lastFit.radarParms.date.getTime() - CACHE_TIME);

                    Date date;
    //				logger.displayString("remove data before "+firstRequired);
                    for ( Iterator i=data.iterator(); i.hasNext(); ) {
                        date = ((FitacfData) i.next()).radarParms.date;
                        if ( date.before( firstRequired ) ) {
    //						logger.displayString("Remove data for "+date);
                            i.remove();
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private FitacfData getInternal(ChannelId channel, long time) {
        // return the data for a particular time, or null
        
        ArrayList<FitacfData> data = allData.get(channel);

        if ( data != null ) {
            synchronized (data) {
                int index = Collections.binarySearch(data, new Date(time));

                if (index >= 0 && index < data.size())
                    return data.get(index);
            }
        }
        
        return null;
    }
    
    
    private FitacfData nextInternal(ChannelId channel, long afterTime, int beam, int timeoutSecs) {
        Date date = new Date(afterTime);
        
        ArrayList<FitacfData> data = allData.get(channel);
        if (data == null) {
            return null;
        }
        
        synchronized (data) {
            
            if (data.size() > 0) {
                
                int index = Collections.binarySearch(data, date);
                if ( index < 0 )
                    index = -index -2;
                
                // if there's at least one more data item after the one found...
                index++;
                
                // iterate the remaining data items for the required beam.
                while (index < data.size()) {
                    FitacfData fit = data.get(index);
                    
                    // if it matches the requirements return it.
                    
                    if (fit.radarParms.beamNumber == beam)
                        return fit;
                    
                    index++;
                }
            }
        }
        
        // didn't find data after the requested time with the correct
        // beam number so wait for new data of the correct beam number
        
        FitacfData fit;
        
        long timeStart;
        long timeoutTime = timeoutSecs * 1000;
        long timeNow = System.currentTimeMillis();
        
        while (true) {
            synchronized (data) {
                
                timeStart = timeNow;
                
                try {
                    data.wait(timeoutTime);
                    
                    // new data available
                    fit = data.get(data.size() - 1);
                    
                    // if it's ok return it.
                    if (fit.radarParms.date.getTime() > afterTime
                            && fit.radarParms.beamNumber == beam)
                        return fit;
                    
                    // calculate how much timeout time remains.
                    timeNow = System.currentTimeMillis();
                    timeoutTime -= (timeNow - timeStart);
                    
                    if (timeoutTime > 0) {
                        continue;
                    }
                    
                } catch (InterruptedException e) {
                    
                }
                
                // timed out, no new data, so return
                return null;
            }
        }
        
    }
    
    private FitacfData previousInternal(ChannelId channel, long beforeTime, int beam) {
        Date date = new Date(beforeTime);
        
        ArrayList<FitacfData> data = allData.get(channel);
        if (data == null) {
            return null;
        }
        
        synchronized (data) {
            int index = Collections.binarySearch(data, date);
            
            if (index > 0) {
                FitacfData fit;
                
                // want the previous data.
                index--;
                
                while (index >= 0) {
                    fit = data.get(index);
                    if (fit.radarParms.beamNumber == beam)
                        return fit;
                    index--;
                }
            } else {
                index = -index -2;
                
                if ( index >= 0 ) {
                    return data.get(index);
                } else
                    return null;
            }
        }
        
        return null;
        
    }
    
    private FitacfData latestInternal(ChannelId channel, int beam) {
        // return the most recent data record with given beam number
        ArrayList<FitacfData> data = allData.get(channel);
        if (data == null) {
            return null;
        }
        
        synchronized (data) {
            if (data.size() > 0) {
                FitacfData fit;
                ListIterator<FitacfData> i = data.listIterator(data.size());
                
                while (i.hasPrevious()) {
                    fit = i.previous();
                    if (fit.radarParms.beamNumber == beam)
                        return fit;
                }
            }
            
            return null;
            
        }
    }
    
    private FitacfData oldestInternal(ChannelId channel, int beam) {
        // return the oldest data record
        ArrayList<FitacfData> data = allData.get(channel);
        if (data == null) {
            return null;
        }
        synchronized (data) {
            if (data.size() > 0) {
                return data.get(0);
            }
            return null;
            
        }
    }
    
    private FitacfData nextInternal(ChannelId channel, long afterTime, int timeoutSecs ) {
        Date date = new Date(afterTime);
        
        ArrayList<FitacfData> data = allData.get(channel);
        if (data == null) {
            return null;
        }
        synchronized (data) {
            
            if (data.size() > 0) {
                
                int index = Collections.binarySearch(data, date);
                
                if ( index < 0 )
                    index = -index -2;
                
                // if there's at least one more data item after the one found
                // return it.
                if (++index < data.size()) {
                    
                    return data.get(index);
                }
            }
        }
        
        //InterruptTimer timer = new InterruptTimer( Thread.currentThread(), timeoutSecs );
        
        FitacfData fit;
        
        long timeStart;
        long timeoutTime = timeoutSecs * 1000;
        long timeNow = System.currentTimeMillis();
        
        while (true) {
            synchronized (data) {
                
                timeStart = timeNow;
                
                try {
                    data.wait(timeoutTime);
                    
                    // new data available
                    fit = data.get(data.size() - 1);
                    
                    // if it's ok return it.
                    if (fit.radarParms.date.getTime() > afterTime)
                        return fit;
                    
                    // calculate how much timeout time remains.
                    timeNow = System.currentTimeMillis();
                    timeoutTime -= (timeNow - timeStart);
                    
                    if (timeoutTime > 0) {
                        continue;
                    }
                    
                } catch (InterruptedException e) {
                    
                }
                
                // timed out, no new data, so return
                return null;
            }
        }
        
    }
    
    private FitacfData previousInternal(ChannelId channel, long beforeTime) {
        Date date = new Date(beforeTime);
        
        ArrayList<FitacfData> data = allData.get(channel);
        if (data == null) {
            return null;
        }
        synchronized (data) {
            int index = Collections.binarySearch(data, date);
            
            if (index > 0) {
                return data.get(index - 1);
            } else {
                index = -index -2;
                
                if ( index >= 0 ) {
                    return data.get(index);
                } else
                    return null;
            }
        }
        
    }
    
    private FitacfData latestInternal(ChannelId channel) {
        // return the most recent data record
        ArrayList<FitacfData> data = allData.get(channel);
        if (data == null) {
            return null;
        }
        synchronized (data) {
            if (data.size() > 0) {
                return data.get(data.size() - 1);
            }
            return null;
        }
    }
    
    private FitacfData oldestInternal(ChannelId channel) {
        
        // return the oldest data record
        ArrayList<FitacfData> data = allData.get(channel);
        if (data == null) {
            return null;
        }
        synchronized (data) {
            if (data.size() > 0) {
                return data.get(0);
            }
            return null;
            
        }
    }
    
    
    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///    public interface
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public FitacfData get(ChannelId channel, long time) {
        // return the data for a particular time, or null
        
        FitacfData result = getInternal(channel, time);
//    if ( result != null )
//        logger.displayString(result.radarParms.date+" "+result.radarParms.txFrequency);
        return result;
    }
    
    
    public FitacfData next(ChannelId channel, long afterTime, int beam, int timeoutSecs) {
        
        FitacfData result = nextInternal(channel, afterTime, beam, timeoutSecs);
        if (trace &&  result != null )
            logger.debug(result.radarParms.date+" "+result.radarParms.txFrequency);
        return result;
        
    }
    
    public FitacfData previous(ChannelId channel, long beforeTime, int beam) {
        FitacfData result = previousInternal(channel, beforeTime, beam);
        if ( trace &&  result != null )
            logger.debug(result.radarParms.date+" "+result.radarParms.txFrequency);
        return result;
        
    }
    
    public FitacfData latest(ChannelId channel, int beam){
        FitacfData result = latestInternal(channel, beam);
        if ( trace && result != null )
            logger.debug(result.radarParms.date+" "+result.radarParms.txFrequency);
        return result;
    }
    
    public FitacfData oldest(ChannelId channel, int beam) {
        FitacfData result = oldestInternal(channel, beam);
        if ( trace && result != null )
            logger.debug(result.radarParms.date+" "+result.radarParms.txFrequency);
        return result;
    }
    
    public FitacfData next(ChannelId channel, long afterTime, int timeoutSecs )  {
        FitacfData result = nextInternal(channel, afterTime, timeoutSecs);
        if ( trace && result != null )
            logger.debug(result.radarParms.date+" "+result.radarParms.txFrequency);
        return result;
    }
    
    public FitacfData previous(ChannelId channel, long beforeTime) {
        FitacfData result = previousInternal(channel, beforeTime);
        if ( trace && result != null )
            logger.debug(result.radarParms.date+" "+result.radarParms.txFrequency);
        return result;
        
    }
    
    public FitacfData latest(ChannelId channel) {
        FitacfData result = latestInternal(channel);
        if ( trace && result != null )
            logger.debug(result.radarParms.date+" "+result.radarParms.txFrequency);
        return result;
    }
    
    public FitacfData oldest(ChannelId channel) {
        FitacfData result = oldestInternal(channel);
        if ( trace && result != null )
            logger.debug(result.radarParms.date+" "+result.radarParms.txFrequency);
        return result;
    }
    
    
    public boolean checkNet() throws RemoteException {
        return true;
    }
    
    public ChannelId[] channels() {
//        Object[] keys = allData.keySet().toArray();
//        String[] result = new String[keys.length];
//        for (int i = 0; i < keys.length; i++) {
//            result[i] = (String) keys[i];
//        }
        return allData.keySet().toArray(new ChannelId[0]);
    }
    
    public void shutdown() {
//        for(Iterator i=oldDataThreads.iterator(); i.hasNext();) {
//            OldDataThread dataThread = (OldDataThread)i.next();
        for(OldDataThread dataThread : oldDataThreads) {
            if ( dataThread.isAlive() ) {
                dataThread.abort();
            }
        }
//        for(Iterator i=monitorThreads.iterator(); i.hasNext();) {
//            MonitorThread monitor = (MonitorThread)i.next();
        for(MonitorThread monitor : monitorThreads) {
            if ( monitor.isAlive() ) {
                monitor.abort();
            }
        }
    }
    
    protected void stop() {
        server.stop();
    }
    
    public String getCacheDir() {
        synchronized(cacheDir) {
            return cacheDir;
        }
    }
    
    public Date startTime() throws RemoteException {
        return startTime;
    }
 
    public void setLogLevel(Level level) throws RemoteException {
        Logger topLogger = Logger.getRootLogger();
        topLogger.setLevel(level);
    }
    
    public Level getLogLevel() throws RemoteException {
        return logger.getLevel();
    }    
}

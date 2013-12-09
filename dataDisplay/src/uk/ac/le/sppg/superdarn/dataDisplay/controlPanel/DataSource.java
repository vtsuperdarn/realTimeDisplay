/*
 * Created on Nov 28, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package uk.ac.le.sppg.superdarn.dataDisplay.controlPanel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.TreeMap;
import uk.ac.le.sppg.superdarn.dataServer.FitRemote2;
import uk.ac.le.sppg.superdarn.dataServer.RemoteData;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;
import uk.ac.le.sppg.superdarn.fitData.NewData;

/**
 * @author nigel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DataSource {

    private class NewDataThread extends Thread {
        long lastTime = -1;
        String siteName;
        ChannelId channel;
        FitRemote2 remote;
        protected ArrayList<NewData> dataListeners = new ArrayList<NewData>();
        Object listenerMutex = new Object();
        Object waitMutex = new Object();

        volatile boolean runnable = true;

        // create a thread to read the old data.
        NewDataThread(FitRemote2 remote, String siteName, ChannelId channel) {
            this.remote = remote;
            this.siteName = siteName;
            this.channel = channel;
            
            this.setName("DataSource:"+siteName+":"+channel.name());

            this.start();
        }

        public void abort() {
            runnable = false;
            this.interrupt();
        }

        public void addListener(NewData l) {
            synchronized (listenerMutex) {
                dataListeners.add(l);
            }
            synchronized (waitMutex) {
                waitMutex.notifyAll();
            }
        }

        public boolean removeListener(NewData l) {
            synchronized (listenerMutex) {
                return dataListeners.remove(l);
            }

        }

        public ArrayList<NewData> getListeners() {
            synchronized (listenerMutex) {
                return dataListeners;
            }
        }
        
        public void run() {

            FitacfData fit;
//            boolean noListeners = true;
            
            
            // sleep time if connection fails.
            int sleepMillis = 2000;

            while (runnable && remote != null) {                
                
                // read the next data record
                try {
                    if (lastTime < 0) {
                        fit = remote.latest(siteName, channel);
                    } else {
                        fit = remote.next(siteName, channel, lastTime, 5);
                    }

                    if (fit == null) {
                        sleep(1000);
                        continue;
                    }
//                    else {
//                        System.out.println("Read data for "+siteName+" "+channel);
//                    }

                    sleepMillis = 2000;
                    
                    lastTime = fit.radarParms.date.getTime();

                    // if there are no listeners go to sleep until
                    // woken up by a notify() on waitMutex.
                    try {
                        synchronized (listenerMutex) {
                            if (dataListeners.size() == 0) {
                                runnable = false;
                            } else {
//                                noListeners = false;
                                for(NewData l : dataListeners ) {
                                    l.newData(fit);
                                }
                            }
                        }
//                        if (noListeners) {
//                            synchronized (waitMutex) {
//System.out.println("NewDataThread for "+siteName+" "+channel+" waiting");
//                                waitMutex.wait();
//System.out.println("NewDataThread for "+siteName+" "+channel+" woken up");
//                                lastTime = -1;
//                            }
//                        }
//                    } catch (InterruptedException e) {
//                        lastTime = -1;
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            sleep(2000);
                        }
                        catch(Exception eee ){}
                    }
                } catch (InterruptedException e) {
                    lastTime = -1;

                } catch (Exception e) {
                    System.out.println("error reading data");
//					System.err.println(e);
					System.out.println(e);
                    try {
                        sleep(sleepMillis);
                        if ( sleepMillis < 500000 ) {
                            sleepMillis *= 2;
                        }
                    }
                    catch(Exception eee ){}
                    
                }

            }

            // send null data to the listeners
            try {
                synchronized (listenerMutex) {
                    for(NewData l : dataListeners ) {
                        l.dataSourceAbort();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sleep(2000);
                }
                catch(Exception eee ){}
            }
        }

    }

    private TreeMap<ChannelId,NewDataThread> dataReaders = new TreeMap<ChannelId,NewDataThread>();

    public FitRemote2 remote;
    String radar;
    URL url;

    public DataSource(URL sourceUrl, String radar) throws MalformedURLException, IOException {
        
        url = sourceUrl;
        remote = new RemoteData(url);
        this.radar = radar;

        if (remote == null) {
            throw new IOException("failed to connect to " + url);
        } else {

            // get the list of channels
//            ChannelId[] channels = remote.channels(radar);
//
//            // create a new Thread for each channel.
//            if (channels == null) {
//                channels = new ChannelId[] { ChannelId.UNKOWN };
//            }
//
//            for(ChannelId channel : channels) {
//
//                if (remote == null) {
//                    throw new IOException("failed to connect to " + url);
//                } else {
//                    dataReaders.put(channel, new NewDataThread(remote, radar, channel));
//                }
//            }
        }
    }

    public void addListener(NewData listener, ChannelId channel) {


        NewDataThread reader = dataReaders.get(channel);
        
        if ( reader == null ) {
            reader = new NewDataThread(remote, radar, channel);
            dataReaders.put(channel, reader);
        }

        reader.addListener(listener);
        
    }

    public void removeListener(NewData listener) {

        for(Iterator<ChannelId> i=dataReaders.keySet().iterator(); i.hasNext();) {
            ChannelId channel = i.next();
            NewDataThread reader = dataReaders.get(channel);
            if ( reader.removeListener(listener) && reader.dataListeners.isEmpty() ) {
                i.remove();
            }
        }
        
    }
    
    public void shutdown() {
        for (NewDataThread reader : dataReaders.values()) {
            for(NewData listener: reader.getListeners()) {
                System.out.println("DataSource listener: "+listener);
                System.out.println(listener.getClass());
                
                listener.dataSourceAbort();
            }
            reader.abort();
        }
      
        dataReaders.clear();
    }
    
    public void finalize() {
        for (NewDataThread reader : dataReaders.values()) {
            reader.abort();
        }
        
    }

}

/*
 * Created on Nov 13, 2003
 *
 */
package uk.ac.le.sppg.superdarn.dataServer;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;

import uk.ac.le.sppg.superdarn.fitData.FitacfData;

/**
 * @author Nigel Wade
 *
 * Class to implement the client side code for implimenting the FitRemote2 interface.
 */
public class RemoteData implements FitRemote2 {
    
    URL servlet;
    URLConnection connection = null;
    ObjectInputStream inputStream = null;
    
    public RemoteData(URL url) {
        
        servlet = url;
    }
    
    
    private Object doRemote(Object[] parameters) throws IOException {
        
        try {
            URLConnection con = servlet.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            
            /*
             * set the property on the connection, this contains the method information
             */
            con.setRequestProperty("Content-Type", "application/octet-stream");
            
            // to send text
            //PrintWriter pw = new PrintWriter(con.getOutputStream());
            
            ObjectOutputStream oos =
                    new ObjectOutputStream(con.getOutputStream());
            
            for (int i = 0; i < parameters.length; i++) {
                oos.writeObject(parameters[i]);
            }
            oos.flush();
            
            ObjectInputStream ois = new ObjectInputStream(con.getInputStream());
            
            ArrayList<Object> results = new ArrayList<Object>();
            
            try {
                Object result;
                while ( true ) {
                    result = ois.readObject();
                    
                    results.add( result );
//					if ( result != null )
//						System.out.println( "read object class: "+result.getClass() );
//					else
//						System.out.println( "read null ");
                }
                
            } catch( EOFException e ) {
                // don't want to do anything
                //System.out.println("EOF exception in doRemote");
            }
            
            oos.close();
            ois.close();
            
            if ( results.size() > 0 ) {
                if (results.get(results.size()-1) instanceof IOException) {
                    throw (IOException) results.get(results.size()-1);
                }
                
                // return the final object sent
                return results.get(results.size()-1);
            } else {
                return null;
            }
            
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found exception unmarshalling result");
        }
        
    }
    
    public FitacfData get(String site, ChannelId channel, long time) throws IOException {
        
        Object[] objects = { Method.GETTIME, site, channel, new Long(time)};
        
        return (FitacfData) doRemote(objects);
    }
    
    public FitacfData next(String site, ChannelId channel, long afterTime, int timeoutSecs)
    throws IOException {
        Object[] objects =
        {
            Method.NEXTTIMETIMEOUT,
            site, channel,
            new Long(afterTime),
            new Integer(timeoutSecs)};
        
        return (FitacfData) doRemote(objects);
    }
    
    public FitacfData next(String site, ChannelId channel, long afterTime, int beamNumber, int timeoutSecs)
    throws IOException {
        Object[] objects =
        {
            Method.NEXTTIMEBEAMTIMEOUT,
            site, channel,
            new Long(afterTime),
            new Integer(beamNumber),
            new Integer(timeoutSecs)};
        
        return (FitacfData) doRemote(objects);
    }
    
    public FitacfData latest(String site, ChannelId channel) throws IOException {
        Object[] objects = { Method.LATEST, site, channel };
        
        return (FitacfData) doRemote(objects);
    }
    
    public FitacfData latest(String site, ChannelId channel, int beamNumber) throws IOException {
        Object[] objects = { Method.LATESTBEAM, site, channel, new Integer(beamNumber)};
        
        return (FitacfData) doRemote(objects);
    }
    
    public FitacfData previous(String site, ChannelId channel, long beforeTime) throws IOException {
        Object[] objects = { Method.PREVIOUSTIME, site, channel, new Long(beforeTime)};
        
        return (FitacfData) doRemote(objects);
    }
    
    public FitacfData previous(String site, ChannelId channel, long beforeTime, int beamNumber)
    throws IOException {
        Object[] objects =
        {
            Method.PREVIOUSTIMEBEAM,
            site, channel,
            new Long(beforeTime),
            new Integer(beamNumber)};
        
        return (FitacfData) doRemote(objects);
    }
    
    public FitacfData oldest(String site, ChannelId channel) throws IOException {
        Object[] objects = { Method.OLDEST, site, channel };
        
        return (FitacfData) doRemote(objects);
    }
    
    public FitacfData oldest(String site, ChannelId channel, int beamNumber) throws IOException {
        Object[] objects = { Method.OLDESTBEAM, site, channel, new Integer(beamNumber)};
        
        return (FitacfData) doRemote(objects);
    }
    
    public boolean checkNet() throws IOException {
        Object[] objects = { Method.CHECKNET };
        
        return ((Boolean) doRemote(objects)).booleanValue();
    }
    
    public ChannelId[] channels(String site ) throws IOException {
        Object[] objects = { Method.GETCHANNELS, site };
        
        ChannelId[] result =  (ChannelId[])doRemote(objects);
        return result;
        
    }
    
    
}

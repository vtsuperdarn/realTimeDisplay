/*
 * Created on 06-Aug-2003
 */
package uk.ac.le.sppg.superdarn.dataServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * @author Nigel Wade
 *
 * class to access the RadarList servlet on behalf of clients.
 */
public class Radars {
    
    
    @SuppressWarnings("unchecked")
    public static ArrayList<String> getRadarList( URL servletURL )
    throws IOException {
        
        try {
            URLConnection con = servletURL.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            
                        /*
                         * set the property on the connection, this contains the method information
                         */
            con.setRequestProperty("Content-Type", "application/octet-stream");
            
            ObjectOutputStream oos =
                    new ObjectOutputStream(con.getOutputStream());
            oos.flush();
            
            ObjectInputStream ois = new ObjectInputStream(con.getInputStream());
            
            Object result = ois.readObject();
            
            if (result instanceof IOException) {
                throw (IOException) result;
            }
            
            oos.close();
            ois.close();
            
            return (ArrayList<String>) result;
            
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found exception unmarshalling result");
        }
        
        
    }
    
}

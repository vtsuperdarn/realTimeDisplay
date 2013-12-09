package uk.ac.le.sppg.superdarn.dataServer;


import java.io.*;


/**
 * An exception which is thrown if the data server is not running and
 * a request for new data times out.
 * 
 * @author Nigel Wade
 */
public class DataServerDownException extends FitRemoteException implements Serializable {
    private static final long serialVersionUID = 0x5253505047000043L;
    
    
}

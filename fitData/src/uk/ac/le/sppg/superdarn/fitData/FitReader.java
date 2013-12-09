package uk.ac.le.sppg.superdarn.fitData;


import java.nio.*;
import java.io.*;

/**
 * An abstract class which any class which reads fit data should implement.
 * 
 * @author Nigel Wade
 */
public abstract class FitReader implements Constants {

    
    /**
     * close any open data input streams
     * 
     * @throws IOException
     */
    public abstract void close() throws IOException;

    ByteBuffer dataBuffer;

    protected boolean testBit( byte[] bytes, int bitNumber ) {
    	
    	byte b = bytes[bitNumber/8];

    	return ( b & (1 << (bitNumber % 8)) ) != 0;
    	
    }
}

package uk.ac.le.sppg.superdarn.fitData;


import java.io.*;
import java.nio.*;
import java.net.*;


/**
 * The FitNetReader class reads SuperDARN real-time compressed fit data from 
 * the radar web_server process.
 * <p>
 * The location from which to read the data is specified in terms of the hostname
 * and the port.
 * 
 * @author Nigel Wade
 */
public class FitNetReader extends FitReader implements Constants {

    int port = 0;
    String host = null;
    final int timeoutSeconds;

    Socket socket = null;
    InputStream in = null;
    DataInputStream dataIn = null;
    
    boolean[] gs = new boolean[MAX_RANGE];
    
    
    /**
     * @param host
     * the DNS name of the host which is running the web_server process.
     * 
     * @param port
     * the port number on which the web_server process is listening.
     */
    public FitNetReader( String host, int port ) {
    	this.port = port;
    	this.host = host;
    	this.timeoutSeconds = 0;
    }

    /**
     * @param host
     * the DNS name of the host which is running the web_server process.
     * 
     * @param port
     * the port number on which the web_server process is listening.
     */
    public FitNetReader( String host, int port, int timeoutSeconds ) {
    	this.port = port;
    	this.host = host;
    	this.timeoutSeconds = timeoutSeconds;
    }


    
    /**
     *  opens a network connection to {@link #host host}
     *  and {@link #port port}.
     * 
     * @throws UnknownHostException
     * @throws IOException
     */
    public synchronized void open() 
	throws UnknownHostException, IOException  {

    	// open a socket to the web_server process
    	socket = new Socket( this.host, this.port );
    	
    	// set the socket timeout to 5 minutes;
    	if ( timeoutSeconds > 0 ) {
    	    socket.setSoTimeout(timeoutSeconds*1000); 
    	}
    	
    	// get an InputStream on the socket
    	in = socket.getInputStream();
    	
    	// get a DataInputStream which can be used to read words.
    	dataIn = new DataInputStream( in );
	
    }
    


    
    /**
     * reads a compressed real-time fit data record from the radar.
     * @return
     * the next data record as a NetFitData object.
     * @throws IOException
     */
    // reads the next fit data record
    
    public NetFitData next( ) 
        throws IOException, SocketException {
    	
    	byte[] dataBytes = null;
    	byte message;
    	
    	if ( this.dataIn == null )
    	    throw new IOException( "FitNetReader: no data source open" );
    	
    	// loop, skipping log messages.
    	do {
    		
        	// read the message header
        	// the header is 4 bytes, but here we read it as an int
        	// and compare it to a byte swapped little endian
        	int id = dataIn.readInt();
        	
        	if ( id != MESSAGE_ID )
        	    throw new IOException( "FitNetReader: bad message id or I/O out of sequence" );
        	
        	// read the data length (this is little endian)
        	byte[] lengthBytes = new byte[4];
        	dataIn.readFully( lengthBytes );
        	
        	// wrap the byte array in a ByteBuffer, this allows
        	// reading of little endian data
        	ByteBuffer bb = ByteBuffer.wrap( lengthBytes);
        	bb.order( ByteOrder.LITTLE_ENDIAN );
        	int length = bb.getInt();
        	
        	// the first byte of the data is the mesage type.
        	message = dataIn.readByte();
        
        	// read the rest of the data into a new buffer	
        	dataBytes = new byte[length-1];
        	dataIn.readFully( dataBytes );
        	
    	} while ( message == 'l' ); // skip log messages
    	
    	// if it's data decode it
    	if ( message == 'd' ) {
    	    
    	    NetFitData fitData = new NetFitData();
    	    
    	    // wrap the data in a ByteBuffer so it can 
    	    // be read little endian.
    	    dataBuffer = ByteBuffer.wrap( dataBytes );
    	    dataBuffer.order( ByteOrder.LITTLE_ENDIAN );
    	
    	    // read the fit data from the ByteBuffer
    	    readFitData( fitData, dataBuffer );
    	    
    	    return fitData;
    	}
    	else if ( message == 'f' ) 
    	    throw new IOException( "no data stream available" );
    	else
    	    throw new IOException( "unknown message type" );
	
    }


    protected void readFitData( NetFitData fit, ByteBuffer bb ) 
        throws IOException, SocketException {
    
        try {
            // decode the radar parms from the buffer
                    
            fit.radarParms.read( bb );
                
            // the first 10 bytes are the packed ground scatter flags
            // the get(byteArray, index, N ) method 
            // extracts the next N bytes into byteArray starting at
            // the given index in the byteArray.
            byte[] bytes = new byte[10];
            bb.get( bytes, 0, 10 );
            for ( int i=0; i<MAX_RANGE; i++ )
                gs[i] = testBit( bytes, i );
                
            // the next 10 bytes are the data flags
            bb.get( bytes, 0, 10 );
            
            int nRanges = 0;
            for ( int i=0; i<MAX_RANGE; i++ )
                if ( testBit( bytes, i ) )
                    nRanges++;
    
            fit.ranges = new byte[nRanges];
            fit.groundScatter = new boolean[nRanges];
            fit.lambdaPower = new float[nRanges];
            fit.lambdaSpectralWidth = new float[nRanges];
            fit.velocity = new float[nRanges];
            
            int index = 0;
            for ( int i=0; i<MAX_RANGE; i++ ) {
                if ( testBit( bytes, i ) ) {
                    fit.ranges[index] = (byte)i;
                    fit.groundScatter[index] = gs[i];
                    index++;
                }
            }
             

            // now read the lambda power values
            // there's one value for each range set in dataFlag
            for ( int i=0; i<fit.ranges.length; i++ )
                 fit.lambdaPower[i] = (float)bb.getDouble();
    
            // the same for the velocity data
            for ( int i=0; i<fit.ranges.length; i++ )
                fit.velocity[i] = (float)bb.getDouble();       
    
            // finally the spectral width data
            for ( int i=0; i<fit.ranges.length; i++ )
                fit.lambdaSpectralWidth[i] = (float)bb.getDouble();

        } 
        catch ( BufferUnderflowException bue ) {
            throw new IOException( "insufficient data for fit record" );
        }
        

    }

    /**
     * @see fitData.FitReader#close()
     */
    public void close() 
    throws IOException {        
                
        dataIn.close();
        in.close();
        socket.close();
        
        socket = null;
        dataIn = null;
        in = null;
        
    }
}

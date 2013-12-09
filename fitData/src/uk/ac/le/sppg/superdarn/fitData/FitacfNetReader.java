package uk.ac.le.sppg.superdarn.fitData;

import java.io.*;
import java.nio.*;
import java.net.*;

/**
 * The FitacfNetReader class reads SuperDARN real-time compressed fit data from
 * the radar web_server process.
 * <p>
 * The location from which to read the data is specified in terms of the hostname
 * and the port.
 * 
 * @author Nigel Wade
 */
public class FitacfNetReader implements Constants {

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
  public FitacfNetReader(String host, int port) {
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
   *
   * @param timeoutSeconds
   *   timeout for connection in seconds
   */
  public FitacfNetReader(String host, int port, int timeoutSeconds) {
    this.port = port;
    this.host = host;
    this.timeoutSeconds = timeoutSeconds;
  }

  /**
   *  opens a network connection to {@link #host host}
   *  and {@link #port port}.
   *
   * @param stream
   *   The stream number to read from the source data feed
   *
   * @throws UnknownHostException
   * @throws IOException
   */
  public synchronized void open(byte stream)
          throws UnknownHostException, IOException {

    // open a socket to the web_server process
    socket = new Socket(this.host, this.port);

    // set the socket timeout to 5 minutes;
    if (timeoutSeconds > 0) {
      socket.setSoTimeout(timeoutSeconds * 1000);
    }

    // get an InputStream on the socket
    in = socket.getInputStream();

    OutputStream out = socket.getOutputStream();
    byte[] streamBytes = new byte[]{stream};
    out.write(streamBytes);

    // get a DataInputStream which can be used to read words.
    dataIn = new DataInputStream(in);

  }

  /**
   * reads a compressed real-time fit data record from the radar.
   * @return
   * the next data record as a NetFitData object.
   * @throws IOException
   * @throws SocketException
   */
  // reads the next fit data record
  public FitacfData next()
          throws IOException, SocketException {

    byte[] dataBytes = null;

    if (this.dataIn == null) {
      throw new IOException("FitacfNetReader: no data source open");
    }

    // read the message header
    // the header is 4 bytes, but here we read it as an int
    // and compare it to a byte swapped little endian
    int id = dataIn.readInt();

    if (id != MESSAGE_ID) {
      // attempt a fix...

      // create byte array to hold message data,
      // drop first byte, we don't need it.
      byte[] idBytes = new byte[] { (byte)(MESSAGE_ID>>24 & 0xff), (byte)(MESSAGE_ID>>16 & 0xff),
                                  (byte)(MESSAGE_ID>>8 & 0xff), (byte)(MESSAGE_ID & 0xff)};
      byte[] bytes = new byte[] { (byte)(id>>16 & 0xff), (byte)(id>>8 & 0xff),
                                  (byte)(id & 0xff), (byte)(id & 0xff)};

      // skip a max. of 8kb, if not found in this time them abort
      int max = 8096;
      boolean found = false;
      while ( max > 0  && ! found ) {
        bytes[3] = dataIn.readByte();
        if ( bytes[3] == idBytes[3] && bytes[2] == idBytes[2] && bytes[1] == idBytes[1] && bytes[0] == idBytes[0] ) {
          found = true;
        } else {
          for(int i=1; i<bytes.length; i++ ) {
            bytes[i-1] = bytes[i];
          }
        }
        max--;
      }

      if ( ! found )
        throw new IOException("FitacfNetReader: bad message id or I/O out of sequence");
    }

    // read the data length (this is little endian)
    byte[] lengthBytes = new byte[4];
    dataIn.readFully(lengthBytes);

    // wrap the byte array in a ByteBuffer, this allows
    // reading of little endian data
    ByteBuffer bb = ByteBuffer.wrap(lengthBytes);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    int length = bb.getInt();

    // read the data into a new buffer
    dataBytes = new byte[length];
    dataIn.readFully(dataBytes);


    FitacfData fitData = new FitacfData();

    // wrap the data in a ByteBuffer so it can
    // be read little endian.
    DmapData data = new DmapData(dataBytes);
    data.order(ByteOrder.LITTLE_ENDIAN);

    // read the fit data from the ByteBuffer
    readFitacfData(fitData, data);

    return fitData;

  }

  protected void readFitacfData(FitacfData fit, DmapData data)
          throws IOException, SocketException {

    try {
      // decode the radar parms from the buffer

      data.decode(fit);

    } catch (BufferUnderflowException bue) {
      throw new IOException("insufficient data for fit record");
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

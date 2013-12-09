/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.fitDataViewers;

import java.io.IOException;
import java.net.SocketTimeoutException;
import uk.ac.le.sppg.superdarn.fitData.FitNetReader;
import uk.ac.le.sppg.superdarn.fitData.NetFitData;

/**
 *
 * @author nigel
 */
public class NetFitReader {

    /**
     * @param args the command line arguments
     */
    static String server;
    static int port;

    NetFitData fitCache = null;


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        if (args.length != 2) {
            System.err.print("NetFitViewer requires 2 arguments,");
            System.err.println(" the server and port number on which the data server is listening");
            System.exit(1);
        }

        server = args[0];
        port = Integer.parseInt(args[1]);

        String status = "";

        FitNetReader reader = null;
        while ( true ) {

            // if the reader isn't open, try to open it.

            if ( reader == null ) {
                try {
                    reader = connectServer( server, port );
                } catch( java.net.UnknownHostException uhe ) {
                    status = "Failed to connect to "+server;
                    System.out.println( status+": "+uhe.getMessage() );
                    break;
                } catch ( IOException ioe ) {
                    System.out.println( "Failed to connect to server: "+server+": "+ioe.getMessage() );

                    try {
                        Thread.sleep( 10000 );
                    } catch ( InterruptedException ie ) {
                        status = "read interrupted ";
                        System.out.println(status);
                        break;
                    }

                    continue;

                }
            }


            // read the next data record
            try {
                NetFitData fit = reader.next();

                if ( fit == null ) {
                    System.out.println("Null data read");
                    Thread.sleep(5000);
                    continue;
                }
                else {
                    System.out.println( "Read data for "+ fit.radarParms.date );
                }

//                publish(fit);


            } catch ( SocketTimeoutException e) {
                System.out.println( "new data thread: timeoout reading data " );
                    if ( reader != null ) {
                        try {
                            reader.close();
                        } catch ( IOException e2 ) {}

                        reader = null;
                    }
            } catch ( IOException e ) {
                System.out.println( "new data thread IOException " );
                e.printStackTrace();;
                    if ( reader != null ) {
                        try {
                            reader.close();
                        } catch ( IOException e2 ) {}

                        reader = null;
                }
            }

            catch ( Exception e ) {
                System.out.println( "new data thread Exception " );
                e.printStackTrace();
            }

            catch( Error e ) {
                System.out.println("Error in new data thread:");
                e.printStackTrace();

                //System.exit(-1);
            }

        }

    }

  // Variables declaration - do not modify
  // End of variables declaration

    private static FitNetReader connectServer( String server, int port )
    throws java.net.UnknownHostException, IOException {
        FitNetReader fitReader = new FitNetReader( server, port, 300 );

        System.out.println( "New data thread, attempting connection to "+server+":"+port );
        fitReader.open();

        System.out.println( "New data thread, opened connection to "+server+":"+port );

        return fitReader;
    }

}

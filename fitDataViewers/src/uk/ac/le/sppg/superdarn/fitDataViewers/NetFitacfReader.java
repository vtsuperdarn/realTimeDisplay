/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.fitDataViewers;

import java.io.IOException;
import java.net.SocketTimeoutException;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import uk.ac.le.sppg.superdarn.fitData.FitacfNetReader;
import uk.ac.le.sppg.superdarn.fitData.NetFitData;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;

/**
 *
 * @author nigel
 */
public class NetFitacfReader {

    /**
     * @param args the command line arguments
     */
    static String server;
    static int port;
    static byte stream = -1;

    FitacfData fitCache = null;


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
      System.out.println(args.length);
        if (args.length != 2 && args.length != 3 ) {
            System.err.print("NetFitacfReader requires 2 or 3 arguments,");
            System.err.println(" the server and port number on which the data server is listening, and the optional stream number");
            System.exit(1);
        }

        server = args[0];
        port = Integer.parseInt(args[1]);
        if ( args.length == 3 ) {
          stream = Byte.parseByte(args[2]);
        }

        String status = "";

        FitacfNetReader reader = null;
        while ( true ) {

            // if the reader isn't open, try to open it.

            if ( reader == null ) {
                try {
                    reader = connectServer( server, port, stream );
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
                FitacfData fit = reader.next();

                if ( fit == null ) {
                    System.out.println("Null data read");
                    Thread.sleep(5000);
                    continue;
                }
                else {
                    System.out.println( "Read data for "+ fit.radarParms.date );
                }

                publish(fit);


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

    private static FitacfNetReader connectServer( String server, int port, byte stream )
    throws java.net.UnknownHostException, IOException {
        FitacfNetReader fitReader = new FitacfNetReader( server, port, 300 );

        System.out.println( "attempting connection to "+server+":"+port );
        fitReader.open(stream);

        System.out.println( "opened connection to "+server+":"+port );

        return fitReader;
    }

    private static void publish(FitacfData data) {
      System.out.println(data.radarParms.stationId+" "+SuperDarnSiteList.getList().getById(data.radarParms.stationId).getName()+" "+data.radarParms.date);
      System.out.println("bmnum = "+data.radarParms.beamNumber+"\tbmazm = "+data.radarParms.beamAzimuth+
              "\tchannel = "+data.radarParms.channel+"\tintt = "+data.radarParms.integrationPeriod);
      System.out.println("nrang = "+data.radarParms.numberOfRanges+"\t frang = "+data.radarParms.firstRangeDistance+
              "\t tfreq = "+data.radarParms.txFrequency);
      System.out.println("rsep = "+data.radarParms.rangeSeparation+"\tnoise.search = "+data.radarParms.noise.search);
      System.out.println("noise.mean = "+data.radarParms.noise.mean+"\tscan = "+data.radarParms.scanFlag);
      System.out.println("cpid = "+data.radarParms.controlProgramId);
      StringBuffer qualityDisplay = new StringBuffer();
      int range=0;
      for(int i=0; i<data.radarParms.numberOfRanges; i++) {
        if ( range < data.ranges.length && data.ranges[range] == i) {
          if ( data.qualityFlag[range])
            qualityDisplay.append("d");
          else
            qualityDisplay.append("-");
          range++;
        }
        else
          qualityDisplay.append("-");
      }
      System.out.println(qualityDisplay);
      
      StringBuffer groundDisplay = new StringBuffer();
      range=0;
      for(int i=0; i<data.radarParms.numberOfRanges; i++) {
        if ( range < data.ranges.length && data.ranges[range] == i) {
          if ( data.groundScatter[range])
            groundDisplay.append("g");
          else
            groundDisplay.append("-");
          range++;
        }
        else
          groundDisplay.append("-");
      }
      System.out.println(groundDisplay);


      for(int i=0; i<data.ranges.length;i++) {
        System.out.println(data.ranges[i]+":"+data.velocity[i]+"\t"+data.velocityError[i]+"\t"+
                data.lambdaPower[i]+"\t"+data.lambdaSpectralWidth[i]);
      }
    }

}

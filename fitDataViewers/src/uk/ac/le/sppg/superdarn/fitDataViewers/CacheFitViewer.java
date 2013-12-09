/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.fitDataViewers;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import uk.ac.le.sppg.superdarn.fitData.NetFitData;

/**
 *
 * @author rtdisp
 */
public class CacheFitViewer {
    
    private String fileName;
    private ObjectInputStream reader = null;

    public CacheFitViewer(String fileName) {
        this.fileName = fileName;
        NetFitData fit;
        openFitFile();
        while ( (fit = getNextFitData()) != null) {
            for (int r=0; r<fit.ranges.length; r++) {
                if (fit.ranges[r] > 74) {
                    System.out.println(fit.radarParms.date);
                    System.out.println(fit.radarParms.numberOfRanges);
                    System.out.println(fit.ranges[r]);
                }
            }
        }
        closeFitFile();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        final String[] strings = args;
        //if (strings.length != 1) {
        //    System.out.println("Must give full path to file.");
        //    return;
        //}
        new CacheFitViewer("/home/sebastien/Desktop/rtdisplay/rtcached/201010111701walA");
    }

    private void openFitFile() {
        try {
             reader = new ObjectInputStream( new FileInputStream( fileName ) );
        }
        catch ( IOException e ) {
            System.out.println( "Error reading cache file: "+fileName+": "+ e.toString() );
            if ( reader != null ) {
                try {
                    reader.close();
                } catch ( IOException e2 ) {}
                reader = null;
            }
        }
    }

    private void closeFitFile() {
        if ( reader != null ) {
            try {
                reader.close();
            } catch ( IOException e2 ) {}
            reader = null;
        }
    }

    private NetFitData getNextFitData() {
        Object o;
        NetFitData ret;
        try {
            o = reader.readObject();
            ret = (NetFitData) o;
        }
        catch ( EOFException e ) {
            System.out.println( "End of cache file: "+fileName );
            if ( reader != null ) {
                try {
                    reader.close();
                } catch ( IOException e2 ) {}
                reader = null;
            }
            return null;
        }
        catch ( IOException e ) {
            System.out.println( "Error reading cache file: "+fileName+": "+ e.toString() );
            if ( reader != null ) {
                try {
                    reader.close();
                } catch ( IOException e2 ) {}
                reader = null;
            }
            return null;
        }
        catch ( ClassNotFoundException e ) {
            System.out.println( "Error reading cache file: "+fileName+": "+ e.toString() );
            return null;
        }
        return ret;
    }
}

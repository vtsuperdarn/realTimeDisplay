/*
 * Created on 02-Jun-2004
 */
package uk.ac.le.sppg.superdarn.fitData;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * @author Nigel Wade
 */
public class FitFilesReader {

    final File[] files;
    
    int fileIndex = -1;
    
    FitFileReader reader = null;
    
    
    public FitFilesReader( String[] files ) {
        this.files = new File[files.length];
        for(int i=0; i<files.length; i++) {
            this.files[i] = new File(files[i]);
        }
    }
    
    public FitFilesReader( File[] files ) {
        this.files = files;
    }

    public void openNext() 
    throws IOException {

        reader = null;

        while( ++fileIndex < files.length ) {
            try {
                reader = new FitFileReader(files[fileIndex]);
                return;
            }
            catch( FileNotFoundException e ) {
                System.err.println( "FitFilesReader.next: file not found: "+files[fileIndex].getAbsolutePath());
            }
        }

        fileIndex = files.length;
        
        // if the file list is exhausted, throw an IOException.
        throw new EOFException("End of file list");
        
    }

    public void openPrevious() 
    throws IOException {

        reader = null;

        while( --fileIndex >= 0 ) {
            try {
                reader = new FitFileReader(files[fileIndex]);
                return;
            }
            catch( FileNotFoundException e ) {
                System.err.println( "FitFilesReader.previous: file not found: "+files[fileIndex].getAbsolutePath());
            }
        }

        fileIndex = -1;
        // the file list is exhausted, throw an IOException.
        throw new IOException("Beginning of file list");
        
    }
    
    public FitData next( ) 
    throws IOException, DataFormatException {    
        
        if ( reader == null ) {
            openNext();
        }
        
        try {
            return reader.next();
        }
        catch( EOFException e ) {
            reader = null;
            return next();
        }
    } 

   
    public NetFitData nextAsNet() 
    throws IOException, DataFormatException {

        if ( reader == null ) {
            openNext();
        }
        
        try {
            return reader.nextAsNet();
        }
        catch( EOFException e ) {
            reader = null;
            return nextAsNet();
        }
    } 
    
    public void rewind()
    throws IOException {
        if ( reader != null ) 
            reader.rewind();
    }
    
    public void close() 
    throws IOException {
        if ( reader != null )
            reader.close();
    }
    
}

/*
 * ProcessStreamReader.java
 *
 * Created on 05 January 2006, 09:34
 */

package uk.ac.le.sppg.processRunner;

import uk.ac.le.sppg.general.display.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.swing.JTextArea;

/**
 *
 * @author nigel
 */

/*
 * This class is used to read the standard output/standard error stream from
 * a Process. 
 
 * A typical usage would be:


    Process proc = Runtime.getRuntime().exec(command);

    ProcessStreamReader inputReader = new ProcessStreamReader(proc.getInputStream());
    ProcessStreamReader errorReader = new ProcessStreamReader(proc.getErrorStream());

    inputReader.start();
    errorReader.start();

    int status = proc.waitFor();

    inputReader.join();
    errorReader.join();

    StringBuffer outputBuffer = inputReader.getResult();
    StringBuffer errorBuffer = errorReader.getResult();

 */


public class ProcessStreamReader 
        extends StreamReader {
    
    /** Creates a new instance of ProcessStreamReader */
    InputStream is;
    StringBuffer records = new StringBuffer();
    StringBuffer result = null;
    String newline = System.getProperty("line.separator");
    
    Logger logger;
    
    ProcessStreamReader(InputStream is) {
        this.is = is;
    }
    
    ProcessStreamReader(InputStream is, Logger logger) {
        this.is = is;
        this.logger = logger;
    }
    
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null) {
                records.append(line).append(newline);
                
                if ( logger != null ) {
                    logger.displayStringLn(line);
                }
            }
//            logger.displayStringln("stream reader loop over");
                
        } catch (IOException ioe) {
            ioe.printStackTrace();  
        }
        synchronized(records) {
            result = records;
        }
//        logger.displayStringln("stream reader thread complete");
    }
    
    public StringBuffer getResult() {
        synchronized(records) {
            return result;
        }
    }
}
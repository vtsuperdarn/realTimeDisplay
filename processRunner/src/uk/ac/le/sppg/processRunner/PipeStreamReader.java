/*
 * PipeStreamReader.java
 *
 * Created on 05 January 2006, 09:34
 */

package uk.ac.le.sppg.processRunner;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.util.ArrayList;
import javax.swing.JTextArea;

/**
 *
 * @author nigel
 */

/*
 * This class is used to read the standard output stream from
 * a Process and feed it into a PipedInputStream
 
 * A typical usage would be:


    Process proc = Runtime.getRuntime().exec(command);


 */


public class PipeStreamReader 
        extends StreamReader {
    
    /** Creates a new instance of ProcessStreamReader */
    InputStream is;
    OutputStream os;
    
    PipeStreamReader(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }
        
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(is);
            DataOutputStream dos = new DataOutputStream(os);
            
            byte[] buffer = new byte[1024];
            
            int nRead;
            while ( (nRead = dis.read(buffer)) > 0  ) {
                dos.write(buffer, 0, nRead);
            }
            os.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();  
        }
    }
}
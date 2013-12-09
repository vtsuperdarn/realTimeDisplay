/*
 * ProcessRunner.java
 *
 * Created on 29 November 2006, 12:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.processRunner;

import uk.ac.le.sppg.general.display.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author nigel
 */
public class ProcessRunner {
    
    Process proc;
    String[] commandArgs;
    String[] envp;
    Thread outputReader = null;
    StreamReader outputStreamReader;
    Thread errorReader = null;
    ProcessStreamReader errorStreamReader;
    
    ProcessRunner pipeDestination = null;
    Logger logger = null;
    
    public ProcessRunner(String command) 
    throws IOException, InterruptedException {
        this(command, null, null, null);
    }

    public ProcessRunner(String command, String[] envp) 
    throws IOException, InterruptedException {
        this(command, envp, null, null);
    }
    
    public ProcessRunner(String[] commands) 
    throws IOException, InterruptedException {
        this(commands, null, null, null);
    }
    public ProcessRunner(String[] commands, String[] envp) 
    throws IOException, InterruptedException {
        this(commands, envp, null, null);
    }
    
    public ProcessRunner(String[] commands, ProcessRunner pipeDestination) 
    throws IOException, InterruptedException {
        this(commands, null, pipeDestination, null);
    }
    public ProcessRunner(String[] commands, String[] envp, ProcessRunner pipeDestination) 
    throws IOException, InterruptedException {
        this(commands, envp, pipeDestination, null);
    }

                
    /** Creates a new instance of ProcessRunner */
    public ProcessRunner(String command, String[] envp, ProcessRunner pipeDestination, Logger logger) 
    throws IOException, InterruptedException {
        this.commandArgs = command.split("\\s");
        this.envp = envp;
        this.pipeDestination = pipeDestination;
        this.logger = logger;
        startProcess();
    }

    public ProcessRunner(String[] commands, String[] envp, ProcessRunner pipeDestination, Logger logger) 
    throws IOException, InterruptedException {
        this.commandArgs = commands;
        this.envp = envp;
        this.pipeDestination = pipeDestination;
        this.logger = logger;
        startProcess();
    }
    
    private void startProcess() 
    throws IOException, InterruptedException {
        proc = Runtime.getRuntime().exec(commandArgs, envp);
        
        // Process.getInputStream returns the standard output.
        if ( pipeDestination == null ) {
            outputStreamReader = new ProcessStreamReader(proc.getInputStream(), logger);
        }
        else {
            outputStreamReader = new PipeStreamReader(proc.getInputStream(), pipeDestination.getOutputStream());
        }
        
        outputReader = new Thread(outputStreamReader);
        outputReader.start();
        errorStreamReader = new ProcessStreamReader(proc.getErrorStream(), logger);
        errorReader = new Thread(errorStreamReader);

        errorReader.start();
        
        if ( pipeDestination != null )
            pipeDestination.streamHandler();
    }

    public int streamHandler() 
    throws IOException, InterruptedException {
        
        
        // Process.getInputStream returns the standard output.
//        if ( pipeDestination == null ) {
//            outputStreamReader = new ProcessStreamReader(proc.getInputStream(), logger);
//        }
//        else {
//            outputStreamReader = new PipeStreamReader(proc.getInputStream(), pipeDestination.getOutputStream());
//        }
//        
//        outputReader = new Thread(outputStreamReader);
//        outputReader.start();
//        errorStreamReader = new ProcessStreamReader(proc.getErrorStream(), logger);
//        errorReader = new Thread(errorStreamReader);
//
//        errorReader.start();
//        
//        if ( pipeDestination != null )
//            pipeDestination.streamHandler();
        
        int status = proc.waitFor();

        outputReader.join();
        errorReader.join();
        
        return status;
    }
    
    public StringBuffer getOutput() {
        if ( outputStreamReader instanceof ProcessStreamReader )
            return ((ProcessStreamReader)outputStreamReader).getResult();
        else
            return null;
    }
    
    public StringBuffer getError() {
        return errorStreamReader.getResult();
    }
    
    public OutputStream getOutputStream() {
        return proc.getOutputStream();
    }
    
    public InputStream getInputStream() {
        return proc.getInputStream();
    }
    
    public InputStream getErrorStream() {
        return proc.getErrorStream();
    }
    
    public int exitValue() {
        return proc.exitValue();
    }
    
    public int waitFor() 
    throws InterruptedException {
        return proc.waitFor();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.serverManager.manager;

import java.io.IOException;
import uk.ac.le.sppg.processRunner.ProcessRunner;

/**
 *
 * @author nigel
 */
public class RegistryControl {

    static final String separator = System.getProperty("file.separator");

    public static StringBuffer startRegistry(String installDir) {
        StringBuffer errors;
        
        try {
            ProcessRunner proc = startRegistryProcess(installDir);

            Thread.sleep(2000);

            try {
                int exitStatus = proc.exitValue();
                proc.streamHandler();
                errors = proc.getOutput();
                errors.append(proc.getError());
//System.out.println("Error: Logger failed to start");
                throw new IOException("Logger process exited");
            }
            catch(IllegalThreadStateException e) {
//System.out.println("Logger: illegal thread state");
                // ignore it, we want this exception to occur.
                errors = new StringBuffer();
                proc.getErrorStream().close();
                proc.getOutputStream().close();
                proc.getInputStream().close();
            }
        }
        catch(Exception e) {
            errors = new StringBuffer(e.toString());
            e.printStackTrace();
            System.err.println("startRegistry: Exception errors: "+errors);
        }

        return errors;
        
    }

    private static ProcessRunner startRegistryProcess(String installDir) 
    throws IOException, InterruptedException {
        
                            
        String javaHome = System.getProperties().getProperty("java.home");
//        String installDir = properties.getProperty("installDir");
        String[] command = {javaHome+"/bin/rmiregistry"};
        String[] environment = {"CLASSPATH="+installDir+separator+"lib:"+installDir+separator+"rmiServer.jar:"+installDir+separator+"rmiLogger.jar"};

        ProcessRunner proc = new ProcessRunner(command, environment);
        return proc;
        
    }
    
    public static StringBuffer stopRegistry(Monitor monitor) {

        StringBuffer errors;
        
        try {
            ProcessRunner proc = stopRegistryProcess(monitor);
            errors = proc.getError();
            StringBuffer output = proc.getOutput();

            int status = proc.waitFor();
        }
        catch(Exception e) {
            errors = new StringBuffer(e.toString());
        }

        return errors;        
    }
    
    private static ProcessRunner stopRegistryProcess(Monitor monitor) 
    throws Exception {
        StringBuffer errorMessage = stopMonitor(monitor);
        
        if ( errorMessage != null )
            throw new Exception(errorMessage.toString());
        
        ProcessRunner proc = new ProcessRunner(new String[] { "killall", "-9", "rmiregistry"} );
        
        return proc;
        
    }

    public static StringBuffer stopMonitor(Monitor monitor) {

        if ( monitor != null ) {
            synchronized( monitor ) {
                if ( monitor.isRunning())
                    return monitor.stop();
            }
        }
        return null;
    }
     
     

}

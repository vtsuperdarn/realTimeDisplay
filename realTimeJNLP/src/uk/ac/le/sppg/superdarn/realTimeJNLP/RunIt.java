/*
 * Created on 06-Dec-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package uk.ac.le.sppg.superdarn.realTimeJNLP;

import java.net.URL;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import uk.ac.le.sppg.superdarn.realTimeControl.ProcessArgs;
import uk.ac.le.sppg.superdarn.realTimeControl.ControlPanel;
import java.lang.reflect.InvocationTargetException;

/**
 * @author nigel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RunIt {

    ControlPanel panel;
    public RunIt() 
    throws InterruptedException, InvocationTargetException {
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                panel = new ControlPanel();
                panel.setVisible(true);
            }
        });
        
    }
    
    public static void main(String[] args) {
        try {
           // Lookup the javax.jnlp.BasicService object
           BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
           URL url = bs.getCodeBase();           

            RunIt r = new RunIt();
            ProcessArgs app = new ProcessArgs(url, args, r.panel);
        }
        catch(UnavailableServiceException ue) {
            System.err.println("This is a JNLP initiated Web service");
        }
        catch(Exception e ) {}
    }
}

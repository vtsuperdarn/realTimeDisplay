/*
 * GuiManager.java
 *
 * Created on 17 November 2004, 17:16
 */

package uk.ac.le.sppg.superdarn.serverManager.guiManager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jdesktop.swingworker.SwingWorker;
import java.lang.Exception;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultListModel;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.log4j.Level;
import uk.ac.le.sppg.processRunner.ProcessRunner;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;
import uk.ac.le.sppg.superdarn.rmiServer.monitor.MonitorInterface;
import uk.ac.le.sppg.superdarn.rmiServer.rmilogger.RmiLoggerInterface;
import uk.ac.le.sppg.superdarn.rmiServer.server.RmiServerInterface;
import uk.ac.le.sppg.superdarn.rmiServer.server.RmiServerMain;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;

/**
 *
 * @author  nigel
 */
public class GuiManager extends JFrame
implements Runnable {
    
    private static final long serialVersionUID = 0x5253505047000039L;
    
    /** Creates new form GuiManager */
    
    static ImageIcon runningIcon = new ImageIcon(GuiManager.class.getResource("/images/stock_calc-accept.png"));
    static ImageIcon stoppedIcon = new ImageIcon(GuiManager.class.getResource("/images/stock_calc-cancel.png"));
    static ImageIcon warningIcon = new ImageIcon(GuiManager.class.getResource("/images/stock_dialog-warning.png"));
    
    static final String separator = System.getProperty("file.separator");

    private class RmiServerStore {
        RmiServerInterface rmi;
        ServerBean bean;
        public RmiServerStore(RmiServerInterface rmi, ServerBean bean) {
            this.rmi = rmi;
            this.bean = bean;
        }
    }
    
    uk.ac.le.sppg.superdarn.serverManager.manager.Logger rmiLogger = null;
//    RmiLoggerInterface theLogger = null;
    uk.ac.le.sppg.superdarn.serverManager.manager.Monitor rmiMonitor = null;
    
    Object lockMonitor = new Object();

    private Thread updateThread = null;
    
    public void run() {
        Thread myThread = Thread.currentThread();
        while (updateThread == myThread) {
            updateDisplay();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e){
                // the VM doesn't want us to sleep anymore,
                // so get back to work
            }
        }
    }
    
    HashMap<String,RmiServerStore> rmiServers = new HashMap<String,RmiServerStore>();
    
    HashMap<String,Method> nonEdtMethods = new HashMap<String,Method>();
    
    Properties properties;
    
//    ServerBean[] dataServerBeans;
    
    DefaultListModel registeredRmiServers = new DefaultListModel();
    
    
    public GuiManager()
    throws NoSuchMethodException {

        // load the properties
        // the properties file contians configuration information.
        File classPath = new File(System.getProperty("java.class.path"));
        String installDir;
        
        if ( classPath.isDirectory() ) {
            installDir = classPath.getAbsolutePath();
        }
        else {
            installDir = classPath.getAbsoluteFile().getParent();
        }

        properties = new Properties();
        properties.setProperty("installDir",installDir);
        
        // first try to load from a properties file
        InputStream in = null;
        
        try {
            try {
                in = new FileInputStream(installDir+separator+"properties");
            } catch( IOException e) {
                // no properties file, so try to load from the default properties in the jar
                in = RmiServerMain.class.getResourceAsStream("/default.properties");
                if ( in == null )
                    throw new IOException("default.properties not found");
            }
            properties.load(in);
            in.close();
        } catch( IOException e) {
            System.out.println("No properties found");
        }

        rmiLogger = new uk.ac.le.sppg.superdarn.serverManager.manager.Logger(properties);
        rmiMonitor = new uk.ac.le.sppg.superdarn.serverManager.manager.Monitor(properties);
        
        initComponents();
        
        // create server beans for each data server listed in the properties file.
        String servers[] = properties.getProperty("dataServers","").split(" ");
        for(int i=0; i<servers.length; i++) {
            ServerBean dataServerBean = new ServerBean(servers[i], null);
            dataServerBean.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    serverControlActionPerformed(evt);
                }
            });
            serversPanel.addTab(servers[i], stoppedIcon, dataServerBean);
            rmiServers.put(servers[i], new RmiServerStore((RmiServerInterface)null, dataServerBean));
        }
        pack();
        
        nonEdtMethods.put("setRegistryState", GuiManager.class.getDeclaredMethod("setRegistryState", new Class[] {Boolean.class}));
        nonEdtMethods.put("setStateUnknown", GuiManager.class.getDeclaredMethod("setStateUnknown", new Class[] {}));
        nonEdtMethods.put("setServerStateUnknown", GuiManager.class.getDeclaredMethod("setServerStateUnknown", new Class[] {}));
        nonEdtMethods.put("setMonitorState", GuiManager.class.getDeclaredMethod("setMonitorState", new Class[] {Boolean.class}));
        nonEdtMethods.put("setServerBeanStatus", GuiManager.class.getDeclaredMethod("setServerBeanStatus", new Class[] {ServerBean.class, Boolean.class}));
        nonEdtMethods.put("setLoggerState", GuiManager.class.getDeclaredMethod("setLoggerState", new Class[] {RmiLoggerInterface.class}));
        
        // create a thread which will monitor the state of the various processes.
        updateThread = new Thread(this, "Update");
        updateThread.start();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        controlPanel = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        restartButton = new javax.swing.JButton();
        shutdownButton = new javax.swing.JButton();
        registryPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        registryStatus = new javax.swing.JLabel();
        registryControl = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        registeredServers = new javax.swing.JList();
        loggerPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        loggerStatus = new javax.swing.JLabel();
        loggerControl = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        loggerLevel = new JComboBox(RmiLoggerInterface.levels);
        monitorPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        monitorStatus = new javax.swing.JLabel();
        monitorControl = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        monitorLogLevel = new JComboBox(RmiLoggerInterface.levels);
        serversPanel = new javax.swing.JTabbedPane();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        controlPanel.setLayout(new java.awt.GridBagLayout());

        controlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("System control"));
        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        controlPanel.add(startButton, gridBagConstraints);

        restartButton.setText("Restart");
        restartButton.setEnabled(false);
        restartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restartButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.1;
        controlPanel.add(restartButton, gridBagConstraints);

        shutdownButton.setText("Shutdown");
        shutdownButton.setEnabled(false);
        shutdownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shutdownButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        controlPanel.add(shutdownButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(controlPanel, gridBagConstraints);

        registryPanel.setLayout(new java.awt.GridBagLayout());

        registryPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("rmiregistry"), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED)));
        jLabel1.setText("Status:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        registryPanel.add(jLabel1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        registryStatus.setFont(new java.awt.Font("Dialog", 0, 12));
        registryStatus.setText("Not running");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(registryStatus, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        registryPanel.add(jPanel1, gridBagConstraints);

        registryControl.setText("Start");
        registryControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registryControlActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        registryPanel.add(registryControl, gridBagConstraints);

        registeredServers.setModel(registeredRmiServers);
        registeredServers.setVisibleRowCount(4);
        jScrollPane.setViewportView(registeredServers);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        registryPanel.add(jScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        getContentPane().add(registryPanel, gridBagConstraints);

        loggerPanel.setLayout(new java.awt.GridBagLayout());

        loggerPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Logger"), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED)));
        jLabel3.setText("Status:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        loggerPanel.add(jLabel3, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        loggerStatus.setFont(new java.awt.Font("Dialog", 0, 12));
        loggerStatus.setText("Not running");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel3.add(loggerStatus, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        loggerPanel.add(jPanel3, gridBagConstraints);

        loggerControl.setText("Start");
        loggerControl.setEnabled(false);
        loggerControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggerControlActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        loggerPanel.add(loggerControl, gridBagConstraints);

        jLabel4.setText("Log level: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        loggerPanel.add(jLabel4, gridBagConstraints);

        loggerLevel.setEnabled(false);
        loggerLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggerLevelActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        loggerPanel.add(loggerLevel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        getContentPane().add(loggerPanel, gridBagConstraints);

        monitorPanel.setLayout(new java.awt.GridBagLayout());

        monitorPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Monitor"), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED)));
        jLabel2.setText("Status:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        monitorPanel.add(jLabel2, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        monitorStatus.setFont(new java.awt.Font("Dialog", 0, 12));
        monitorStatus.setText("Not running");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(monitorStatus, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        monitorPanel.add(jPanel2, gridBagConstraints);

        monitorControl.setText("Start");
        monitorControl.setEnabled(false);
        monitorControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monitorControlActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        monitorPanel.add(monitorControl, gridBagConstraints);

        jLabel5.setText("Log level: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        monitorPanel.add(jLabel5, gridBagConstraints);

        monitorLogLevel.setEnabled(false);
        monitorLogLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monitorLogLevelActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        monitorPanel.add(monitorLogLevel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        getContentPane().add(monitorPanel, gridBagConstraints);

        serversPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Servers"), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED)));
        serversPanel.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(serversPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void monitorLogLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monitorLogLevelActionPerformed
        JComboBox b = (JComboBox) evt.getSource();
        Level l = (Level) b.getSelectedItem();
        
        setMonitorLogLevel(l);
    }//GEN-LAST:event_monitorLogLevelActionPerformed

    private void loggerLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggerLevelActionPerformed
        JComboBox b = (JComboBox) evt.getSource();
        Level l = (Level) b.getSelectedItem();
        
        setLoggerLevel(l);
    }//GEN-LAST:event_loggerLevelActionPerformed

    private void loggerControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggerControlActionPerformed
        String loggerAction = evt.getActionCommand();
        
        if ( loggerAction.equals("Stop")) {
            int result =
                JOptionPane.showConfirmDialog(this, new String[] {"Shutting down the Logger will disable all logging.",
                "Do you really want to shut down the Logger?"},
                "Confirm Logger shutdown", JOptionPane.YES_NO_OPTION);
        

            if ( result == JOptionPane.YES_OPTION ) 
                stopLoggerInBackground(this);
        }
        else if ( loggerAction.equals("Start")) {
            startLoggerInBackground(this);
        }
    }//GEN-LAST:event_loggerControlActionPerformed
    
    private void restartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restartButtonActionPerformed
        restartSystemInBackground(this);
    }//GEN-LAST:event_restartButtonActionPerformed
    
    private void shutdownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shutdownButtonActionPerformed
        shutdownSystemInBackground(this);
    }//GEN-LAST:event_shutdownButtonActionPerformed
    
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        // start up the entire system.
        startSystemInBackground(this);
    }//GEN-LAST:event_startButtonActionPerformed
    
    
    private void monitorControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monitorControlActionPerformed
        String monitorAction = evt.getActionCommand();
        
        if ( monitorAction.equals("Stop")) {
            int result =
            JOptionPane.showConfirmDialog(this, new String[] {"Shutting down the Monitor will shutdown all Servers.",
            "Do you really want to shut down the Monitor?"},
            "Confirm Monitor shutdown", JOptionPane.YES_NO_OPTION);
        

            if ( result == JOptionPane.YES_OPTION ) {
                stopMonitorInBackground(this);
            }
        }
        else if ( monitorAction.equals("Start")) {
            startMonitorInBackground(this);
        }
        
    }//GEN-LAST:event_monitorControlActionPerformed
    
    private void registryControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registryControlActionPerformed
        String registryAction = evt.getActionCommand();
        
        if ( registryAction.equals("Stop")) {
            int result = JOptionPane.YES_OPTION;
            if ( rmiMonitor != null && rmiMonitor.check() != null ) {
                result = JOptionPane.showConfirmDialog(this, new String[] {"Shutting down the rmiregistry when the Monitor",
                "is running will shutdown the Monitor and all servers.", "Do you really want to shut down the rmiregistry?"},
                "Confirm rmiregistry shutdown", JOptionPane.YES_NO_OPTION);
            }

            if ( result == JOptionPane.YES_OPTION ) 
                stopRegistryInBackground(this);
        }
        else if ( registryAction.equals("Start")) {
            startRegistryInBackground(this);
        }
        
    }//GEN-LAST:event_registryControlActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
                
        try {
            new GuiManager().setVisible(true);
        }
        catch(NoSuchMethodException e) {
            System.err.println("Error in implementation of GuiManager: method in class not found");
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JButton loggerControl;
    private javax.swing.JComboBox loggerLevel;
    private javax.swing.JPanel loggerPanel;
    private javax.swing.JLabel loggerStatus;
    private javax.swing.JButton monitorControl;
    private javax.swing.JComboBox monitorLogLevel;
    private javax.swing.JPanel monitorPanel;
    private javax.swing.JLabel monitorStatus;
    private javax.swing.JList registeredServers;
    private javax.swing.JButton registryControl;
    private javax.swing.JPanel registryPanel;
    private javax.swing.JLabel registryStatus;
    private javax.swing.JButton restartButton;
    private javax.swing.JTabbedPane serversPanel;
    private javax.swing.JButton shutdownButton;
    private javax.swing.JButton startButton;
    // End of variables declaration//GEN-END:variables
    
    private void restartSystemInBackground(Component comp) {
        final Component c = comp;
        SwingWorker<StringBuffer, Void> worker = new SwingWorker<StringBuffer, Void>() {
            public StringBuffer doInBackground() {
                StringBuffer result;
                if ( (result = shutdownSystem()) == null ) {
                    result = startSystem();
                }
                return result;
            }
            public void done() {
                updateThread.interrupt();
                StringBuffer status=new StringBuffer();
                try {
                    status = get();
                }catch(InterruptedException ignore) {}
                catch(ExecutionException e) {
                    status = new StringBuffer(e.toString());
                }
                if ( status.length() != 0 ) {
                    JOptionPane.showMessageDialog(c, status.toString(), "Error restarting system", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();

    }
    
    private SwingWorker startSystemInBackground(Component comp) {
        final Component c = comp;
        // First the rmiregistry
        SwingWorker<StringBuffer, Void> worker = new SwingWorker<StringBuffer, Void>() {
            public StringBuffer doInBackground() {
                return startSystem();
            }
            public void done() {
                updateThread.interrupt();
                StringBuffer status=new StringBuffer();
                try {
                    status = get();
                }catch(InterruptedException ignore) {}
                catch(ExecutionException e) {
                    status = new StringBuffer(e.toString());
                }
                if ( status.length() != 0 ) {
                    JOptionPane.showMessageDialog(c, status.toString(), "Error starting system", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        
        return worker;

    }
    
    private StringBuffer startSystem() {
        
        StringBuffer errorMessage = startRegistry();
        
        
        if ( errorMessage.length() == 0 )
            errorMessage.append(startLogger());

        if ( errorMessage.length() == 0 )
            errorMessage.append(startMonitor());
        
        if ( errorMessage.length() == 0 ) {
            // Finally the data servers.

            String servers[] = properties.getProperty("dataServers","").split(" ");

            for(int i=0; i<servers.length; i++) {
                errorMessage.append(startServer(servers[i]));
            }

        }
        
        return errorMessage;
        
    }
    
    private SwingWorker<StringBuffer, Void> shutdownSystemInBackground(Component comp) {
        final Component c = comp;
        
        // First the rmiregistry
        SwingWorker<StringBuffer, Void> worker = new SwingWorker<StringBuffer, Void>() {
            public StringBuffer doInBackground() {
                return shutdownSystem();
            }
            public void done() {
                updateThread.interrupt();
                StringBuffer status=new StringBuffer();
                try {
                    status = get();
                }catch(InterruptedException ignore) {}
                catch(ExecutionException e) {
                    status = new StringBuffer(e.toString());
                }
                if ( status != null && status.length() != 0 ) {
                    JOptionPane.showMessageDialog(c, status.toString(), "Error shutting down system", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        
        return worker;
    }
    
    private StringBuffer shutdownSystem() {
        StringBuffer errorMessage = stopMonitor();

        if ( errorMessage.length() == 0 ) {
            errorMessage = stopLogger();
        }

        if ( errorMessage.length() == 0 ) {
            errorMessage = stopRegistry();
        }

        return errorMessage;
        
    }

    private SwingWorker<StringBuffer, Void> startRegistryInBackground(Component comp) {
        final Component c = comp;
        SwingWorker<StringBuffer, Void> worker = new SwingWorker<StringBuffer, Void>() {
            public StringBuffer doInBackground() {
                return startRegistry();
            }
            public void done() {
                updateThread.interrupt();
                StringBuffer status=new StringBuffer();
                try {
                    status = get();
                }catch(InterruptedException ignore) {}
                catch(ExecutionException e) {
System.out.println("SwingWorker ExecutionException: "+e);
                    status = new StringBuffer(e.toString());
                }
                if ( status != null && status.length() != 0 ) {
                    JOptionPane.showMessageDialog(c, status.toString(), "Error starting registry", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        
        return worker;
        
    }
    
    private StringBuffer startRegistry() {
        StringBuffer errors;
        
        try {
            ProcessRunner proc = startRegistryProcess();

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
            System.out.println("startRegistry: Exception errors: "+errors);
        }

        return errors;
        
    }

    private ProcessRunner startRegistryProcess() 
    throws IOException, InterruptedException {
        
                            
        String javaHome = System.getProperties().getProperty("java.home");
        String installDir = properties.getProperty("installDir");
        String[] command = {javaHome+"/bin/rmiregistry"};
        String[] environment = {"CLASSPATH="+installDir+separator+"lib:"+installDir+separator+"rmiServer.jar:"+installDir+separator+"rmiLogger.jar"};

        ProcessRunner proc = new ProcessRunner(command, environment);
        return proc;
        
    }
    
    private SwingWorker<StringBuffer, Void> stopRegistryInBackground(Component comp) {
        final Component c = comp;

        SwingWorker<StringBuffer, Void> worker = new SwingWorker<StringBuffer, Void>() {
            public StringBuffer doInBackground() {
                return stopRegistry();
            }
            public void done() {
                updateThread.interrupt();
                StringBuffer status=new StringBuffer();
                try {
                    status = get();
                }catch(InterruptedException ignore) {}
                catch(ExecutionException e) {
                    status = new StringBuffer(e.toString());
                }
                if ( status != null && status.length() != 0 ) {
                    JOptionPane.showMessageDialog(c, status.toString(), "Error stopping Registry", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();

        return worker;
    }
    
    private StringBuffer stopRegistry() {

        StringBuffer errors;
        
        try {
            ProcessRunner proc = stopRegistryProcess();
            errors = proc.getError();
            StringBuffer output = proc.getOutput();

            int status = proc.waitFor();
        }
        catch(Exception e) {
            errors = new StringBuffer(e.toString());
        }

        return errors;        
    }
    
    private ProcessRunner stopRegistryProcess() 
    throws Exception {
        StringBuffer errorMessage = stopMonitor();
        
        if ( errorMessage.length() != 0 )
            throw new Exception(errorMessage.toString());
        
        ProcessRunner proc = new ProcessRunner(new String[] { "killall", "-9", "rmiregistry"} );
        
        return proc;
        
    }
     
    private SwingWorker<StringBuffer, Void> startMonitorInBackground(Component comp) {
        final Component c = comp;
        SwingWorker<StringBuffer, Void> worker = new SwingWorker<StringBuffer, Void>() {
            public StringBuffer doInBackground() {
                return startMonitor();
            }
            public void done() {
                updateThread.interrupt();
                StringBuffer status=new StringBuffer();
                try {
                    status = get();
                }catch(InterruptedException ignore) {}
                catch(ExecutionException e) {
                    status = new StringBuffer(e.toString());
                }
                if ( status != null && status.length() != 0 ) {
                    JOptionPane.showMessageDialog(c, status.toString(), "Error starting Monitor", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        
        return worker;
        
    }
     
    private StringBuffer startMonitor() {
        synchronized(rmiMonitor) {
            return rmiMonitor.start();
        }        
    }
    
    private SwingWorker<StringBuffer, Void> stopMonitorInBackground(Component comp) {
        final Component c = comp;
        SwingWorker<StringBuffer, Void> worker = new SwingWorker<StringBuffer, Void>() {
            public StringBuffer doInBackground() {
                return stopMonitor();
            }
            public void done() {
                updateThread.interrupt();
                StringBuffer status=new StringBuffer();
                try {
                    status = get();
                }catch(InterruptedException ignore) {}
                catch(ExecutionException e) {
                    status = new StringBuffer(e.toString());
                }
                if ( status != null && status.length() != 0 ) {
                    JOptionPane.showMessageDialog(c, status.toString(), "Error stopping Monitor", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        
        return worker;
        
    }

    private StringBuffer stopMonitor() {

        synchronized( rmiMonitor ) {
            if ( rmiMonitor.isRunning())
                return rmiMonitor.stop();
            else
                return new StringBuffer();
        }
    }
     
     
    private SwingWorker<StringBuffer, Void> startServerInBackground(Component comp, String sitename) {
        final Component c = comp;
        final String s = sitename;
        
        SwingWorker<StringBuffer, Void> worker = new SwingWorker<StringBuffer, Void>() {
            public StringBuffer doInBackground() {
                synchronized(rmiMonitor) {
                    return startServer(s);
                }
            }
            public void done() {
                updateThread.interrupt();
                StringBuffer status=new StringBuffer();
                try {
                    status = get();
                }catch(InterruptedException ignore) {}
                catch(ExecutionException e) {
                    status = new StringBuffer(e.toString());
                }
                if ( status != null && status.length() != 0 ) {
                    JOptionPane.showMessageDialog(c, status.toString(), "Error starting Server for "+s, 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        
        return worker;
        
    }
    
    private StringBuffer startServer(String sitename) {
        synchronized(rmiMonitor) {
            MonitorInterface monitor = rmiMonitor.check();

            if ( monitor == null ) {
                return new StringBuffer("Monitor not running");
            }
            else {
                StringBuffer errors = new StringBuffer();
                
                try {
                    String message = monitor.start(sitename);
                    if ( message.startsWith("ERROR:") ) {
                        errors.append(message);
                    }
                }
                catch(Exception e) {
                    errors.append(e.toString());
                }
                return errors;
            }
        }
        
    }

    private SwingWorker<StringBuffer, Void> stopServerInBackground(Component comp, String sitename) {
        final Component c = comp;
        final String s = sitename;
        
        SwingWorker<StringBuffer, Void> worker = new SwingWorker<StringBuffer, Void>() {
            public StringBuffer doInBackground() {
                synchronized(rmiMonitor) {
                    return stopServer(s);
                }
            }
            public void done() {
                updateThread.interrupt();
                StringBuffer status=new StringBuffer();
                try {
                    status = get();
                }catch(InterruptedException ignore) {}
                catch(ExecutionException e) {
                    status = new StringBuffer(e.toString());
                }
                if ( status != null && status.length() != 0 ) {
                    JOptionPane.showMessageDialog(c, status.toString(), "Error stopping Server for "+s, 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        
        return worker;
        
    }
    
    private StringBuffer stopServer(String sitename) {

        synchronized( rmiMonitor ) {
            MonitorInterface monitor = rmiMonitor.check();

            if ( monitor == null ) {
                return new StringBuffer("Monitor not running");
            }
            else {
                StringBuffer errors = new StringBuffer();
                
                try {
                    String message = monitor.shutdown(sitename);
                    if ( message.startsWith("ERROR:") ) {
                        errors.append(message);
                    }
                }
                catch(Exception e) {
                    errors.append(e.toString());
                }
                return errors;
            }

        }
    }
    
    private SwingWorker<StringBuffer, Void> startLoggerInBackground(Component comp) {
        final Component c = comp;
        SwingWorker<StringBuffer, Void> worker = new SwingWorker<StringBuffer, Void>() {
            public StringBuffer doInBackground() {
                return startLogger();
            }
            public void done() {
//System.out.println("start logger in background done");
                updateThread.interrupt();
                StringBuffer status=new StringBuffer();
                try {
                    status = get();
                }catch(InterruptedException ignore) {}
                catch(ExecutionException e) {
                    status = new StringBuffer(e.toString());
                }
                if ( status != null && status.length() != 0 ) {
                    JOptionPane.showMessageDialog(c, status.toString(), "Error starting Logger", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        
        return worker;
        
    }
     
    
    private StringBuffer startLogger() {
        synchronized(rmiLogger) {
//System.out.println("starting logger");
            StringBuffer errorMessage = rmiLogger.start();
//System.out.println("started logger, response: "+errorMessage);
            return errorMessage;
        }
    }
     
    private SwingWorker<StringBuffer, Void> stopLoggerInBackground(Component comp) {
        final Component c = comp;

        SwingWorker<StringBuffer, Void> worker = new SwingWorker<StringBuffer, Void>() {
            public StringBuffer doInBackground() {
                return stopLogger();
            }
            public void done() {
                updateThread.interrupt();
                StringBuffer status=new StringBuffer();
                try {
                    status = get();
                }catch(InterruptedException ignore) {}
                catch(ExecutionException e) {
                    status = new StringBuffer(e.toString());
                }
                if ( status != null && status.length() != 0 ) {
                    JOptionPane.showMessageDialog(c, status.toString(), "Error stopping Logger", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();

        return worker;
    }
    

    private StringBuffer stopLogger() {

        synchronized( rmiLogger ) {
            if ( rmiLogger.isRunning())
                return rmiLogger.stop();
            else
                return new StringBuffer();
        }
    }
     

    private void serverControlActionPerformed(ActionEvent evt) {
        JButton button = (JButton) evt.getSource();
        String site = button.getName();
        String serverAction = evt.getActionCommand();
        
        if ( serverAction.equals("Start")) {
            startServer(site);
        }
        else {
            stopServer(site);
        }
        
    }
    
    private void updateDisplay() {
        try {
            // check the rmi registry is running
            final int maxTries = 5;
            for(int i=1; i<=maxTries; i++ ) {
                try {
                    Naming.list("//localhost");
                    runInEdt("setRegistryState", new Object[] {new Boolean(true)} );
                    break;
                }
                catch( ConnectException e) {
                    if ( i==maxTries ) {
                        runInEdt("setRegistryState", new Object[] {new Boolean(false)});
                        return;
                    }
                    try {
                        Thread.sleep(500);
                    }
                    catch(InterruptedException ee) {}
                }
            }
            
            // see if the Logger is running
            RmiLoggerInterface logger = rmiLogger.check();
            
            runInEdt("setLoggerState", new Object[] {logger});
            
            
            // see if the Monitor is running
            MonitorInterface monitor = rmiMonitor.check();
            
            runInEdt("setMonitorState", new Object[] {new Boolean(monitor != null)});
            
            for(RmiServerStore entry : rmiServers.values()) {
//                setServerBeanStatus(entry.bean, (monitor != null));
                runInEdt("setServerBeanStatus", new Object[] {entry.bean, new Boolean(monitor != null)});
            }
            
            
        }
        catch( RemoteException re ) {}
        catch(MalformedURLException e) {}
        
    }
    
    private  uk.ac.le.sppg.superdarn.serverManager.manager.Monitor checkMonitor()
    throws RemoteException {
        synchronized(lockMonitor) {
//            try {
                if ( rmiMonitor == null || rmiMonitor.check() == null ) {
                    rmiMonitor = new uk.ac.le.sppg.superdarn.serverManager.manager.Monitor(properties);
                }
                if ( rmiMonitor == null )
                  JOptionPane.showMessageDialog(this, "Failed to get connection to RMI Monitor");
                else
                  rmiMonitor.check();
//            }
//            catch(NotBoundException e) {
//                rmiMonitor = null;
//            }
//            catch(ConnectException e) {
//                rmiMonitor = null;
//            }
//            catch(MalformedURLException e) {
//                System.err.println("Malformed URL in checkMonitor");
//                JOptionPane.showMessageDialog(this, "Malformed URL Exception in checkMonitor");
//                rmiMonitor = null;
//            }
            
            return rmiMonitor;
        }
    }
    
    private void setLoggerLevel(Level level) {
        try {
            rmiLogger.check().setLevel(level);
        }
        catch(Exception e) {
//            System.err.println("Error setting logger level");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error setting logger level", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setMonitorLogLevel(Level level) {
        try {
            rmiMonitor.check().setLevel(level);
        }
        catch(Exception e) {
//            System.err.println("Error setting Monitor log level");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error setting Monitor log level", JOptionPane.ERROR_MESSAGE);
        }
    }
    

   
    private RmiServerInterface checkServer(String site) {
        synchronized(lockMonitor) {
            RmiServerStore dataStore = rmiServers.get(site);
            if ( dataStore == null )
                return null;
            try {
                if ( dataStore.rmi == null ) {
                    dataStore.rmi = (RmiServerInterface) Naming.lookup("//localhost/"+site+"."+RmiServerInterface.bindName);
                    dataStore.bean.setServer(dataStore.rmi);
                }
                dataStore.rmi.getLogLevel();
            }
            catch(NotBoundException e) {
                dataStore.rmi = null;
            }
            catch(MalformedURLException e) {
//                System.err.println("Malformed URL in checkServer");
                JOptionPane.showMessageDialog(this, "Malformed URL Exception in checkServer");
                dataStore.rmi = null;
            }
            catch(RemoteException e) {
                dataStore.rmi = null;
            }
            
            return dataStore.rmi;
        }
    }
    
    protected void setServerBeanStatus(ServerBean serverBean, Boolean monitorRunning) {
        String site = serverBean.getSite();
        RmiServerInterface server = checkServer(site);
        
        boolean enableControl = monitorRunning.booleanValue() || server != null;
        
        serverBean.setStatusEnabled(enableControl);
        serverBean.setLabelEnabled(enableControl);
        serverBean.setLogLevelEnabled(enableControl);
        
        int tabIndex = serversPanel.indexOfComponent(serverBean);
        
        serverBean.setControlEnabled(enableControl);
        
        if ( server != null ) {
            serverBean.setStatusText("Running");
            serverBean.setControlText("Stop");
            serverBean.enableStartTime(true);
            serversPanel.setIconAt(tabIndex, runningIcon);
            
            try {
                Date start = server.startTime();
                serverBean.setStartTime(start);
                serverBean.showServerLogLevel(server.getLogLevel());
            }
            catch(RemoteException e) {
                serverBean.enableStartTime(false);
            }
            
            try {
                ChannelId[] channels = server.channels();
                for(int i=0; i<channels.length; i++) {
                    try {
                        FitacfData fit = server.latest(channels[i]);
                        serverBean.setLatestData(channels[i], fit);
                        serverBean.setLatestEnabled(channels[i], true);
                    }
                    catch(RemoteException e){
                        serverBean.setLatestData(channels[i], null);
                        serverBean.setLatestEnabled(channels[i], false);
                    }
                    
                    try {
                        FitacfData fit = server.oldest(channels[i]);
                        serverBean.setEarliestData(channels[i], fit);
                        serverBean.setEarliestEnabled(channels[i], true);
                    }
                    catch(RemoteException e){
                        serverBean.setEarliestData(channels[i], null);
                        serverBean.setEarliestEnabled(channels[i], false);
                    }
                }
            }
            catch(RemoteException e) {
                serverBean.setEarliestEnabled(false);
                serverBean.setLatestEnabled(false);
                
                serversPanel.setIconAt(tabIndex, warningIcon);
                serverBean.setStatusText("Failed");
            }
        }
        else {
            serverBean.setStatusText("Stopped");
            serverBean.setControlText("Start");
            serversPanel.setIconAt(tabIndex, stoppedIcon);
            serverBean.enableStartTime(false);
            serverBean.setLatestEnabled(false);
            serverBean.setEarliestEnabled(false);
        }
        
    }
    
    private void runInEdt(String methodName, final Object args[]) {
        final Method method = (Method) nonEdtMethods.get(methodName);
        final Object o = this;
        Runnable doIt = new Runnable() {
            public void run() {
                try {
                    method.invoke(o, args);
                }
                catch(IllegalAccessException e) {
                    e.printStackTrace();
                }
                catch(InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        };
        SwingUtilities.invokeLater(doIt);
    }
    
    protected void setRegistryState(Boolean running) {
        
        boolean run = running.booleanValue();
        
        registeredRmiServers.clear();

        if ( run ) {
            registryControl.setText("Stop");
            registryStatus.setText("Running");
            loggerControl.setEnabled(true);
            loggerLevel.setEnabled(true);
            monitorControl.setEnabled(true);
            
            try {
                String[] rmiServerNames = Naming.list("//localhost");
                for(String s:rmiServerNames) {
                    registeredRmiServers.addElement(s);
                }
            }
            catch(RemoteException e ) {
                JOptionPane.showMessageDialog(this, e.getMessage(), 
                        "Error obtaining list of services registered with RMI", JOptionPane.ERROR_MESSAGE);
            }
            catch(MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else {
            registryControl.setText("Start");
            registryStatus.setText("Not running");
            setStateUnknown();
        }
        
        startButton.setEnabled(!run);
        restartButton.setEnabled(run);
        shutdownButton.setEnabled(run);
        
    }
    
    protected void setStateUnknown() {
        loggerControl.setEnabled(false);
        loggerLevel.setEnabled(false);
        monitorControl.setEnabled(false);
        monitorStatus.setText("Unknown");
        
        setServerStateUnknown();
    }
    
    protected void setServerStateUnknown() {
        for(RmiServerStore server : rmiServers.values()) {
//        for(Iterator i=dataServers.keySet().iterator(); i.hasNext();) {
//            DataServerStore server = (RmiServerStore) i.next();
            server.bean.setControlEnabled(false);
            server.bean.setLabelEnabled(false);
            server.bean.setStatusText("Unknown");
            server.bean.setStatusEnabled(false);
            server.bean.setLatestEnabled(false);
            server.bean.setEarliestEnabled(false);
        }
    }
    
    protected void setMonitorState(Boolean running) {
        
        // if the monitor is running set the label and then button label to Stop.
        boolean run = running.booleanValue();
        
        monitorLogLevel.setEnabled(running);

        if ( run ) {
            monitorControl.setText("Stop");
            monitorStatus.setText("Running");
            try {
                Level level = rmiMonitor.check().getLevel();
                if ( monitorLogLevel.getSelectedItem() == null || 
                        ! ((Level)monitorLogLevel.getSelectedItem()).equals(level) ) 
                    monitorLogLevel.setSelectedItem(level);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        else {
            monitorControl.setText("Start");
            monitorStatus.setText("Not running");
            
            // if the Monitor is not running disable the server control functions.
            setServerStateUnknown();
        }
        
        
    }
    
    protected void setLoggerState(RmiLoggerInterface logger) {
        
        // if the logger is running set the label and then button label to Stop.
        boolean run = logger != null;
        
        loggerLevel.setEnabled(run);

        if ( run ) {
            loggerControl.setText("Stop");
            loggerStatus.setText("Running");
            try {
                Level level = logger.getLevel();
                if ( loggerLevel.getSelectedItem() == null || 
                        ! ((Level)loggerLevel.getSelectedItem()).equals(level) ) 
                    loggerLevel.setSelectedItem(level);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        else {
            loggerControl.setText("Start");
            loggerStatus.setText("Not running");
        }
        
        
    }
    
//    protected void setServerState(String site, Boolean running) {
//        RmiServerStore server = (RmiServerStore) dataServers.get(site);
//System.out.println("set server state for: "+site);
//        setServerBeanStatus(server.bean, running.booleanValue());
//        
//    }
    
    
}

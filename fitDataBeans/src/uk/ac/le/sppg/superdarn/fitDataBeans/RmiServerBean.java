/*
 * RmiServerBean.java
 *
 * Created on 24 July 2003, 15:13
 */

package uk.ac.le.sppg.superdarn.fitDataBeans;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import uk.ac.le.sppg.superdarn.fitData.ChannelId;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;
import uk.ac.le.sppg.superdarn.rmiServer.server.RmiServerInterface;
import uk.ac.le.sppg.superdarn.dataServer.Radars;



/**
 *
 * @author  nigel
 */
public class RmiServerBean extends JPanel {
    
    private static final long serialVersionUID = 0x5253505047000035L;
    
    String server = null;
    RmiServerInterface rmiServer = null;
    String[] noServerStrings = new String[] { "Connect" };
    ChannelId[] channels;
    int channelIndex;
    
    /** Creates new form BeanForm */
    public RmiServerBean() {
        initComponents();
        //connectServer(server);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        optionPane = new javax.swing.JOptionPane();
        connectPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        serverCombo = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        channelCombo = new javax.swing.JComboBox();
        navPanel = new javax.swing.JPanel();
        oldestButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        latestButton = new javax.swing.JButton();
        beamCheck = new javax.swing.JCheckBox();
        beamField = new javax.swing.JTextField();
        waitCheck = new javax.swing.JCheckBox();
        waitField = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        connectPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 5));

        jLabel2.setText("Server:");
        connectPanel.add(jLabel2);

        serverCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Disconnected" }));
        serverCombo.setToolTipText("List of radars served");
        serverCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectServer(evt);
            }
        });
        connectPanel.add(serverCombo);

        jLabel1.setText("Channel: ");
        connectPanel.add(jLabel1);

        channelCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboSetChannel(evt);
            }
        });
        connectPanel.add(channelCombo);

        add(connectPanel, java.awt.BorderLayout.NORTH);

        navPanel.setEnabled(false);

        oldestButton.setText("<<");
        oldestButton.setToolTipText("Get oldest data");
        oldestButton.setEnabled(false);
        oldestButton.setIconTextGap(0);
        oldestButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        oldestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oldest(evt);
            }
        });
        navPanel.add(oldestButton);

        previousButton.setText("<");
        previousButton.setToolTipText("Get previous data record");
        previousButton.setEnabled(false);
        previousButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previous(evt);
            }
        });
        navPanel.add(previousButton);

        nextButton.setText(">");
        nextButton.setToolTipText("Get next data record");
        nextButton.setEnabled(false);
        nextButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                next(evt);
            }
        });
        navPanel.add(nextButton);

        latestButton.setText(">>");
        latestButton.setToolTipText("Get latest data record");
        latestButton.setEnabled(false);
        latestButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        latestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                latest(evt);
            }
        });
        navPanel.add(latestButton);

        beamCheck.setText("beam");
        beamCheck.setToolTipText("Only view a specific beam");
        beamCheck.setEnabled(false);
        beamCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                beamCheckItemStateChanged(evt);
            }
        });
        navPanel.add(beamCheck);

        beamField.setColumns(2);
        beamField.setToolTipText("Beam number to view");
        beamField.setEnabled(false);
        navPanel.add(beamField);

        waitCheck.setText("wait");
        waitCheck.setToolTipText("Wait for new data");
        waitCheck.setEnabled(false);
        waitCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                waitCheckItemStateChanged(evt);
            }
        });
        navPanel.add(waitCheck);

        waitField.setColumns(2);
        waitField.setToolTipText("Time to wait in seconds");
        waitField.setEnabled(false);
        navPanel.add(waitField);

        add(navPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    private void comboSetChannel(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboSetChannel
        // Add your handling code here:
        channelIndex = channelCombo.getSelectedIndex();
        next();
//        System.out.println( "channel set: "+channelIndex+" "+channels[channelIndex] );
    }//GEN-LAST:event_comboSetChannel
    
    private void connectServer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectServer
        // Add your handling code here:

        if ( serverCombo.getSelectedIndex() < 0 )
            return;
        
        String radar = (String)serverCombo.getSelectedItem();
        
        if ( serverCombo.getSelectedIndex() == 0 ) {
            
            enableNavigation( false );
            
            if ( radar.equals("Connect")) {
                setServer(server);
            }
            
            return;
        }
        
//        rmiServer = null;
//        server = "superdarn.ece.vt.edu";
        java.net.URL serverURL = null;
        try {
            serverURL = new java.net.URL("http://"+server);
        } catch (MalformedURLException ex) {
            Logger.getLogger(RmiServerBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            System.out.println("haha: trying to connect to rmiServer: "+serverURL.getHost());
//            rmiServer = new RemoteData( new URL("http://"+server+":8080/dataServlet/rmiServer") );
//            System.out.println("connected to rmiServer");
            String[] rmiServerNames = Naming.list("//" + serverURL.getHost());
            System.out.println("Looking for "+radar);
            for(String serverName: rmiServerNames) {
              System.out.println(serverName);
//              Object obj = Naming.lookup("//superdarn.ece.vt.edu/Blackstone.rmiServer_1.0");
              Object obj = Naming.lookup(serverName);
              System.out.println("Class name = " + obj.getClass().getName());
                if ( serverName.contains("rmiServer") && serverName.contains(radar)) {
//                    rmiServer = (RmiServerInterface) Naming.lookup("//superdarn.ece.vt.edu:1099/Blackstone.rmiServer_1.0");
                    rmiServer = (RmiServerInterface) Naming.lookup(serverName);
                    break;
                }
            }
            System.out.println("connected to rmiServer");
            System.out.println(rmiServer);

            if ( rmiServer == null ) {
                setComboNoServer();
            }
            else {
                enableNavigation( true );
                
                // get the list of channels
                System.out.println("Getting channels from rmiServer " + server);
                channels = rmiServer.channels();
                System.out.println("Got channels from rmiServer " + server);
                
                channelCombo.removeAllItems();
                
                if ( channels != null ) {
                    for( int i=0; i<channels.length; i++ ) {
                        channelCombo.addItem( channels[i] );
                    }
                    channelCombo.setSelectedIndex(0);
                }
                channelIndex = 0;
                
                next();
            }
        } catch( IOException e ) {
            JOptionPane.showMessageDialog( this, e.getMessage(), "IO Exception", JOptionPane.ERROR_MESSAGE );
        }
        catch( NotBoundException e ) {
            JOptionPane.showMessageDialog( this, e.getMessage(), "NotBoundException", JOptionPane.ERROR_MESSAGE );
        }
       
        return;
    }//GEN-LAST:event_connectServer
    
    private void waitCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_waitCheckItemStateChanged
        waitField.setEnabled(waitCheck.isSelected());
    }//GEN-LAST:event_waitCheckItemStateChanged
    
    private void beamCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_beamCheckItemStateChanged
        beamField.setEnabled(beamCheck.isSelected());
    }//GEN-LAST:event_beamCheckItemStateChanged
    
    private void latest(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_latest
        // Add your handling code here:
        latest();
    }//GEN-LAST:event_latest
    
    private void next(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_next
        // Add your handling code here:
        next();
    }//GEN-LAST:event_next
    
    private void previous(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previous
        // Add your handling code here:
        previous();
    }//GEN-LAST:event_previous
    
    private void oldest(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oldest
        // Add your handling code here:
        oldest();
    }//GEN-LAST:event_oldest
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox beamCheck;
    private javax.swing.JTextField beamField;
    private javax.swing.JComboBox channelCombo;
    private javax.swing.JPanel connectPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton latestButton;
    private javax.swing.JPanel navPanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton oldestButton;
    private javax.swing.JOptionPane optionPane;
    private javax.swing.JButton previousButton;
    private javax.swing.JComboBox serverCombo;
    private javax.swing.JCheckBox waitCheck;
    private javax.swing.JTextField waitField;
    // End of variables declaration//GEN-END:variables
    
    long dataTime = 0;
    
    /** Holds value of property netFit. */
    private FitDataBean dataBean;
    
    public void enableNavigation( boolean ok ) {
        oldestButton.setEnabled( ok );
        previousButton.setEnabled( ok );
        nextButton.setEnabled( ok );
        latestButton.setEnabled( ok );
        beamCheck.setEnabled(ok );
        beamField.setEnabled(ok);
        waitCheck.setEnabled(ok);
        waitField.setEnabled(ok);
    }
    
    private void oldest() {
        if ( rmiServer != null ) {
            
            FitacfData data;
            String radar = (String)serverCombo.getSelectedItem();
            
            try {
                if ( beamCheck.isSelected() ) {
                    int beam = Integer.parseInt(beamField.getText());
                    data = rmiServer.oldest( channels[channelIndex], beam );
                } else {
                    data = rmiServer.oldest(channels[channelIndex]);
                }
                
                if ( data != null ) {
                    dataTime = data.radarParms.date.getTime();
                    if ( dataBean != null )
                        dataBean.setData( data );
                } 
            } catch ( IOException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "IO Exception", JOptionPane.ERROR_MESSAGE );
                rmiServer = null;
                setComboNoServer();
            } catch ( NumberFormatException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "Illegal beam number", JOptionPane.ERROR_MESSAGE );
            }
            
        }
    }
    
    private void previous() {
        if ( rmiServer != null ) {
            
            FitacfData data;
            
            String radar = (String)serverCombo.getSelectedItem();
            
            try {
                Date date = new Date( dataTime);
                
                if ( beamCheck.isSelected() ) {
                    int beam = Integer.parseInt(beamField.getText());
                    data = rmiServer.previous( channels[channelIndex], dataTime, beam );
                } else {
                    data = rmiServer.previous( channels[channelIndex], dataTime );
                }
                
                if ( data != null ) {
                    dataTime = data.radarParms.date.getTime();
                    if ( dataBean != null )
                        dataBean.setData( data );
                } 
            } catch ( IOException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "IO Exception", JOptionPane.ERROR_MESSAGE );
                rmiServer = null;
                setComboNoServer();
            } catch ( NumberFormatException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "Illegal beam number", JOptionPane.ERROR_MESSAGE );
            }
        }
    }
    
    private void latest() {
        if ( rmiServer != null ) {
            
            FitacfData data;
            
            String radar = (String)serverCombo.getSelectedItem();
            
            try {
                if ( beamCheck.isSelected() ) {
                    int beam = Integer.parseInt(beamField.getText());
                    data = rmiServer.latest( channels[channelIndex], beam );
                } else {
                    data = rmiServer.latest( channels[channelIndex] );
                }
                
                if ( data != null ) {
                    dataTime = data.radarParms.date.getTime();
                    if ( dataBean != null )
                        dataBean.setData( data );
                } 
            } catch ( IOException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "IO Exception", JOptionPane.ERROR_MESSAGE );
                rmiServer = null;
                setComboNoServer();
            } catch ( NumberFormatException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "Illegal beam number", JOptionPane.ERROR_MESSAGE );
            }
        }
    }
    
    private void next() {
        if ( rmiServer != null ) {
            
            FitacfData data;
            
            String radar = (String)serverCombo.getSelectedItem();
            
            try {
                int beam = -1;
                int timeout = 1;
                
                if ( beamCheck.isSelected() ) {
                    beam = Integer.parseInt(beamField.getText());
                }
                
                if ( waitCheck.isSelected() ) {
                    timeout = Integer.parseInt(waitField.getText());
                }
                
                
                if ( beam >= 0 ) {
                    data = rmiServer.next( channels[channelIndex], dataTime, beam, timeout );
                } else {
                    data = rmiServer.next( channels[channelIndex], dataTime, timeout );
                }
                
                if ( data != null ) {
                    dataTime = data.radarParms.date.getTime();
                    if ( dataBean != null )
                        dataBean.setData( data );
                } 
            } catch ( IOException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "IO Exception", JOptionPane.ERROR_MESSAGE );
                rmiServer = null;
                setComboNoServer();
            } catch ( NumberFormatException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "Illegal beam number", JOptionPane.ERROR_MESSAGE );
            }
        }
    }
    
    
    /** Getter for property netFit.
     * @return Value of property netFit.
     *
     */
    public FitDataBean getFitBean() {
        return this.dataBean;
    }
    
    /** Setter for property netFit.
     * @param netFit New value of property netFit.
     *
     */
    public void setFitBean(FitDataBean dataBean) {
        this.dataBean = dataBean;
    }
    
    /** Setter for server
     * @param String - FQDN of server where the dataServlet is running
     * @author Nigel Wade
     *
     */
    
    public void setServer( String server ) {
        this.server = new String( server );
        
        // get the list of RMI names of radars being served
        enableNavigation( false );
        
        if ( server == null || server.equals("")  ) {
            return;
        }
        
        try {
            System.out.println( "try to connect to "+server);
            //ArrayList<String> serverList = getRadarList();
            java.net.URL radarURL = new java.net.URL(new java.net.URL("http://"+server), "dataServer/radarList");
            System.out.println( radarURL.toString() );
           ArrayList serverList = Radars.getRadarList(radarURL);
//            System.out.println("got radar list "+serverList);
            // add the list to the radars shown in the combo-box.
            serverCombo.removeAllItems();
//            System.out.println("remove all items");
            serverCombo.addItem( "Choose radar" );
//            System.out.println("add base item");
            
            if ( serverList != null ) {
                for( int i=0; i<serverList.size(); i++ ) {
                    serverCombo.addItem( serverList.get(i));
                }
            }
            serverCombo.setSelectedIndex(0);
        } catch( RemoteException e ) {
            JOptionPane.showMessageDialog( this, e.getMessage(), "RemoteException", JOptionPane.ERROR_MESSAGE );
        
        } catch( MalformedURLException e ) {
            JOptionPane.showMessageDialog( this, e.getMessage(), "MalformedURLException", JOptionPane.ERROR_MESSAGE );
        } catch( IOException e ) {
            JOptionPane.showMessageDialog( this, e.getMessage(), "IOException", JOptionPane.ERROR_MESSAGE );
        }
        
    }
    
    private void setComboNoServer() {
        setServer( server );
    }
    
    Pattern p = Pattern.compile(".*/(\\w[\\w ]*\\w)\\.rmiServer.*$");
    private ArrayList<String> getRadarList() 
    throws RemoteException, MalformedURLException {
        String[] rmiServerNames = Naming.list("//" + server);
        ArrayList<String> radarNames = new ArrayList<String>();
        
        for(String serverName: rmiServerNames) {
            Matcher m = p.matcher(serverName);
            if ( m.matches() ) {
                radarNames.add(m.group(1));
            }
        }
        
        return radarNames;
    }
    
}

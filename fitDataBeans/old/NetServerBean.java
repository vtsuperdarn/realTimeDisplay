/*
 * NetServerBean.java
 *
 * Created on 24 July 2003, 15:13
 */

package superdarn.fitDataBeans;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import superdarn.dataServlet.FitRemote;
import superdarn.dataServlet.Radars;
import superdarn.dataServlet.RemoteData;
import superdarn.fitData.ChannelId;
import superdarn.fitData.NetFitData;



/**
 *
 * @author  nigel
 */
public class NetServerBean extends JPanel {
    
    private static final long serialVersionUID = 0x5253505047000035L;
    
    String server=null;
    String[] noServerStrings = new String[] { "Connect" };
    ChannelId[] channels;
    int channelIndex;
    
    /** Creates new form BeanForm */
    public NetServerBean() {
        initComponents();
        //connectServer(server);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
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

        jLabel2.setText("Radar:");
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
        oldestButton.setIconTextGap(0);
        oldestButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        oldestButton.setEnabled(false);
        oldestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oldest(evt);
            }
        });

        navPanel.add(oldestButton);

        previousButton.setText("<");
        previousButton.setToolTipText("Get previous data record");
        previousButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        previousButton.setEnabled(false);
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previous(evt);
            }
        });

        navPanel.add(previousButton);

        nextButton.setText(">");
        nextButton.setToolTipText("Get next data record");
        nextButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        nextButton.setEnabled(false);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                next(evt);
            }
        });

        navPanel.add(nextButton);

        latestButton.setText(">>");
        latestButton.setToolTipText("Get latest data record");
        latestButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
        latestButton.setEnabled(false);
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

    }//GEN-END:initComponents
    
    private void comboSetChannel(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboSetChannel
        // Add your handling code here:
        channelIndex = channelCombo.getSelectedIndex();
        System.out.println( "channel set: "+channelIndex+" "+channels[channelIndex] );
    }//GEN-LAST:event_comboSetChannel
    
    private void connectServer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectServer
        // Add your handling code here:
        
        String radar = (String)serverCombo.getSelectedItem();
        
        if ( serverCombo.getSelectedIndex() == 0 ) {
            
            enableNavigation( false );
            
            if ( radar.equals("Connect")) {
                setServer(server);
            }
            
            return;
        }
        
        remote = null;
        
        try {
            System.out.println("trying to connect to dataServer");
            remote = new RemoteData( new URL("http://"+server+":8080/dataServer/dataServer") );
            System.out.println("connected to dataServer");
            
            if ( remote == null ) {
                setComboNoServer();
            } else {
                enableNavigation( true );
                
                // get the list of channels
                System.out.println("trying to get channel list");
                channels = remote.channels(radar);
                System.out.println("got channel list "+channels);
                
                channelCombo.removeAllItems();
                
                if ( channels != null ) {
                    for( int i=0; i<channels.length; i++ ) {
                        channelCombo.addItem( channels[i] );
                    }
                    channelCombo.setSelectedIndex(0);
                }
                channelIndex = 0;
                
                latest( );
            }
        } catch( MalformedURLException e ) {
            JOptionPane.showMessageDialog( this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE );
        } catch( IOException e ) {
            JOptionPane.showMessageDialog( this, e.getMessage(), "IO Exception", JOptionPane.ERROR_MESSAGE );
            System.out.println(e.getMessage());
            e.printStackTrace();
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
    private javax.swing.JButton previousButton;
    private javax.swing.JComboBox serverCombo;
    private javax.swing.JCheckBox waitCheck;
    private javax.swing.JTextField waitField;
    // End of variables declaration//GEN-END:variables
    
    FitRemote remote = null;
    long dataTime = 0;
    
    /** Holds value of property netFit. */
    private FitDataBean netFit;
    
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
        if ( remote != null ) {
            
            NetFitData data;
            String radar = (String)serverCombo.getSelectedItem();
            
            try {
                if ( beamCheck.isSelected() ) {
                    int beam = Integer.parseInt(beamField.getText());
                    System.out.println( "get data for beam "+beam);
                    data = remote.oldest( radar, channels[channelIndex], beam );
                } else {
                    data = remote.oldest(radar, channels[channelIndex]);
                }
                
                if ( data != null ) {
                    System.out.println( "Got data for time: "+data.radarParms.date );
                    dataTime = data.radarParms.date.getTime();
                    if ( netFit != null )
                        netFit.setData( data );
                } else
                    System.out.println( "Got no data" );
            } catch ( IOException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "IO Exception", JOptionPane.ERROR_MESSAGE );
                remote = null;
                setComboNoServer();
            } catch ( NumberFormatException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "Illegal beam number", JOptionPane.ERROR_MESSAGE );
            }
            
        }
    }
    
    private void previous() {
        if ( remote != null ) {
            
            NetFitData data;
            
            String radar = (String)serverCombo.getSelectedItem();
            
            try {
                Date date = new Date( dataTime);
                System.out.println( "Requesting data before time "+date );
                
                if ( beamCheck.isSelected() ) {
                    int beam = Integer.parseInt(beamField.getText());
                    System.out.println( "get data for beam "+beam);
                    data = remote.previous( radar, channels[channelIndex], dataTime, beam );
                } else {
                    data = remote.previous( radar, channels[channelIndex], dataTime );
                }
                
                if ( data != null ) {
                    System.out.println( "Got data for time: "+data.radarParms.date );
                    dataTime = data.radarParms.date.getTime();
                    if ( netFit != null )
                        netFit.setData( data );
                } else
                    System.out.println( "Got no data" );
            } catch ( IOException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "IO Exception", JOptionPane.ERROR_MESSAGE );
                remote = null;
                setComboNoServer();
            } catch ( NumberFormatException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "Illegal beam number", JOptionPane.ERROR_MESSAGE );
            }
        }
    }
    
    private void latest() {
        if ( remote != null ) {
            
            NetFitData data;
            
            String radar = (String)serverCombo.getSelectedItem();
            
            try {
                if ( beamCheck.isSelected() ) {
                    int beam = Integer.parseInt(beamField.getText());
                    System.out.println( "get data for beam "+beam);
                    data = remote.latest( radar, channels[channelIndex], beam );
                } else {
                    data = remote.latest( radar, channels[channelIndex] );
                }
                
                if ( data != null ) {
                    dataTime = data.radarParms.date.getTime();
                    System.out.println( "Got data for time: "+data.radarParms.date+"  "+dataTime );
                    if ( netFit != null )
                        netFit.setData( data );
                } else
                    System.out.println( "Got no data" );
            } catch ( IOException e ) {
                e.printStackTrace();
                JOptionPane.showMessageDialog( this, e.getMessage(), "IO Exception", JOptionPane.ERROR_MESSAGE );
                remote = null;
                setComboNoServer();
            } catch ( NumberFormatException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "Illegal beam number", JOptionPane.ERROR_MESSAGE );
            }
        }
    }
    
    private void next() {
        if ( remote != null ) {
            
            NetFitData data;
            
            String radar = (String)serverCombo.getSelectedItem();
            
            try {
                int beam = -1;
                int timeout = 1;
                
                if ( beamCheck.isSelected() ) {
                    beam = Integer.parseInt(beamField.getText());
                    System.out.println( "get data for beam "+beam);
                }
                
                if ( waitCheck.isSelected() ) {
                    timeout = Integer.parseInt(waitField.getText());
                    System.out.println( "wait for "+timeout);
                }
                
                
                if ( beam >= 0 ) {
                    data = remote.next( radar, channels[channelIndex], dataTime, beam, timeout );
                } else {
                    data = remote.next( radar, channels[channelIndex], dataTime, timeout );
                }
                
                if ( data != null ) {
                    System.out.println( "Got data for time: "+data.radarParms.date );
                    dataTime = data.radarParms.date.getTime();
                    if ( netFit != null )
                        netFit.setData( data );
                } else
                    System.out.println( "Got no data" );
            } catch ( IOException e ) {
                JOptionPane.showMessageDialog( this, e.getMessage(), "IO Exception", JOptionPane.ERROR_MESSAGE );
                remote = null;
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
    public FitDataBean getNetFit() {
        return this.netFit;
    }
    
    /** Setter for property netFit.
     * @param netFit New value of property netFit.
     *
     */
    public void setNetFit(FitDataBean netFit) {
        this.netFit = netFit;
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
            ArrayList serverList = Radars.getRadarList( new URL( "http://"+server+":8080/dataServer/radarList") );
            System.out.println("got radar list "+serverList);
            // add the list to the radars shown in the combo-box.
            //serverCombo.removeAllItems();
            System.out.println("remove all items");
            //serverCombo.addItem( "Choose radar" );
            System.out.println("add base item");
            
            if ( serverList != null ) {
                for( int i=0; i<serverList.size(); i++ ) {
                    serverCombo.addItem( serverList.get(i));
                    System.out.println(serverList.get(i));
                }
            }
            serverCombo.setSelectedIndex(0);
        } catch( IOException e ) {
            JOptionPane.showMessageDialog( this, e.getMessage(), "IO Exception", JOptionPane.ERROR_MESSAGE );
            e.printStackTrace();
        }
        
    }
    
    private void setComboNoServer() {
        
        setServer( server );
        
    }
    
}
/*
 * Created on 15-Feb-2005
 *
 */
package uk.ac.le.sppg.superdarn.dataDisplay.fanWindow;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JFormattedTextField.AbstractFormatter;

import uk.ac.le.sppg.coords.Geographic;
/**
 * @author nigel
 *
 */
public class StereoViewpoint extends JDialog {

    private static final long serialVersionUID = 0x5253505047000017L;

	private javax.swing.JPanel jContentPane = null;

	private JLabel jLabel = null;  //  @jve:decl-index=0:visual-constraint="386,123"
	private JPanel navPanel = null;
	private JPanel zoomPanel = null;
	private JPanel navButtonPanel = null;
	private JPanel latLonPanel = null;
	private JButton northWestButton = null;
	private JButton northEastButton = null;
	private JButton northButton = null;
	private JButton southButton = null;
	private JButton southWestButton = null;
	private JButton centreButton = null;
	private JButton westButton = null;
	private JButton eastButton = null;
	private JButton southEastButton = null;
	private JLabel jLabel1 = null;
	private JFormattedTextField latField = null;
	private JLabel jLabel2 = null;
	private JFormattedTextField lonField = null;
	private JToolBar jToolBar = null;
	private JButton showWorld = null;
	private JButton zoomIn = null;
	private JButton fitViewToWindow = null;
	private JButton zoomOut = null;
	private JFormattedTextField scaleField = null;

    private FanPlot plot;
    private NumberFormat numberFormat;
    
    private final double defaultScale;
	
    class ScaleVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            JFormattedTextField f = (JFormattedTextField) input;
            AbstractFormatter format = f.getFormatter();
            System.out.println("scale verifier: "+f.getText());
            try {
                Double d = (Double) format.stringToValue(f.getText());
                
                if ( d.doubleValue() < 0.0 ) {
                    return false;
                }
                return true;
            }
            catch( ParseException e ) {
                return false;
            }
        }
    }

    class LatitudeVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            JFormattedTextField f = (JFormattedTextField) input;
            AbstractFormatter format = f.getFormatter();
            try {
                Double d = (Double) format.stringToValue(f.getText());
                if ( d.doubleValue() <= -90.0 || d.doubleValue() >= 90.0 ) {
                    return false;
                }
                return true;
            }
            catch( ParseException e ) {
                return false;
            }
        }
    }

    class LongitudeVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            JFormattedTextField f = (JFormattedTextField) input;
            AbstractFormatter format = f.getFormatter();
            try {
                Double d = (Double) format.stringToValue(f.getText());
                if ( d.doubleValue() <= -180.0 || d.doubleValue() > 360.0 ) {
                    return false;
                }
                return true;
            }
            catch( ParseException e ) {
                return false;
            }
        }
    }

    
    /** Creates new form Viewpoint */
    public StereoViewpoint(Frame parent, boolean modal, FanPlot plot) {
        super(parent, modal);
        this.plot = plot;
        
        defaultScale = plot.getScale();
        
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        
        initialize();
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setSize(442, 209);
		this.setContentPane(getJContentPane());
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jLabel = new JLabel();
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jLabel.setText("Set viewpoint");
			jLabel.setHorizontalTextPosition(javax.swing.SwingConstants.TRAILING);
			jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jContentPane.add(jLabel, java.awt.BorderLayout.NORTH);
			jContentPane.add(getNavPanel(), java.awt.BorderLayout.CENTER);
			jContentPane.add(getZoomPanel(), java.awt.BorderLayout.SOUTH);
		}
		return jContentPane;
	}
	/**
	 * This method initializes navPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getNavPanel() {
		if (navPanel == null) {
			navPanel = new JPanel();
			navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
			navPanel.add(getNavButtonPanel(), null);
			navPanel.add(getLatLonPanel(), null);
		}
		return navPanel;
	}
	/**
	 * This method initializes zoomPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getZoomPanel() {
		if (zoomPanel == null) {
			zoomPanel = new JPanel();
			zoomPanel.add(getJToolBar(), null);
			zoomPanel.add(getScaleField(), null);
		}
		return zoomPanel;
	}
	/**
	 * This method initializes navButtonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getNavButtonPanel() {
		if (navButtonPanel == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			navButtonPanel = new JPanel();
			navButtonPanel.setLayout(new GridBagLayout());
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints4.gridx = 2;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridy = 1;
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 2;
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.gridy = 2;
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridy = 1;
			gridBagConstraints9.gridx = 2;
			gridBagConstraints9.gridy = 1;
			gridBagConstraints10.gridx = 2;
			gridBagConstraints10.gridy = 2;
			navButtonPanel.add(getNorthWestButton(), gridBagConstraints2);
			navButtonPanel.add(getNorthButton(), gridBagConstraints3);
			navButtonPanel.add(getNorthEastButton(), gridBagConstraints4);
			navButtonPanel.add(getWestButton(), gridBagConstraints8);
			navButtonPanel.add(getCentreButton(), gridBagConstraints5);
			navButtonPanel.add(getEastButton(), gridBagConstraints9);
			navButtonPanel.add(getSouthWestButton(), gridBagConstraints6);
			navButtonPanel.add(getSouthButton(), gridBagConstraints7);
			navButtonPanel.add(getSouthEastButton(), gridBagConstraints10);
		}
		return navButtonPanel;
	}
	/**
	 * This method initializes latLonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getLatLonPanel() {
		if (latLonPanel == null) {
			jLabel2 = new JLabel();
			jLabel1 = new JLabel();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			latLonPanel = new JPanel();
			latLonPanel.setLayout(new GridBagLayout());
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			jLabel1.setText("Lat:");
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.gridy = 0;
			gridBagConstraints21.weightx = 0.0D;
			gridBagConstraints21.fill = java.awt.GridBagConstraints.NONE;
			gridBagConstraints21.insets = new java.awt.Insets(0,5,0,0);
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.gridy = 1;
			jLabel2.setText("Lon:");
			gridBagConstraints41.gridx = 1;
			gridBagConstraints41.gridy = 1;
			gridBagConstraints41.weightx = 0.0D;
			gridBagConstraints41.fill = java.awt.GridBagConstraints.NONE;
			gridBagConstraints41.gridwidth = 1;
			gridBagConstraints41.insets = new java.awt.Insets(0,5,0,0);
			latLonPanel.add(jLabel1, gridBagConstraints1);
			latLonPanel.add(getLatField(), gridBagConstraints21);
			latLonPanel.add(getLonField(), gridBagConstraints41);
			latLonPanel.add(jLabel2, gridBagConstraints31);
		}
		return latLonPanel;
	}
	/**
	 * This method initializes northWestButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getNorthWestButton() {
		if (northWestButton == null) {
			northWestButton = new JButton();
			northWestButton.setIcon(new ImageIcon(getClass().getResource("/images/NW.png")));
			northWestButton.setIconTextGap(0);
			northWestButton.setToolTipText("Move viewing area North West");
			northWestButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			northWestButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
			northWestButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
			        plot.moveNorthWest();
				}
			});
		}
		return northWestButton;
	}
	/**
	 * This method initializes northEastButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getNorthEastButton() {
		if (northEastButton == null) {
			northEastButton = new JButton();
			northEastButton.setIcon(new ImageIcon(getClass().getResource("/images/NE.png")));
			northEastButton.setIconTextGap(0);
			northEastButton.setToolTipText("Move viewing area North East");
			northEastButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
			northEastButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			northEastButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					plot.moveNorthEast();
				}
			});
		}
		return northEastButton;
	}
	/**
	 * This method initializes northButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getNorthButton() {
		if (northButton == null) {
			northButton = new JButton();
			northButton.setIcon(new ImageIcon(getClass().getResource("/images/N.gif")));
			northButton.setIconTextGap(0);
			northButton.setToolTipText("Move viewing area North");
			northButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
			northButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			northButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					plot.moveNorth();
				}
			});
		}
		return northButton;
	}
	/**
	 * This method initializes southButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getSouthButton() {
		if (southButton == null) {
			southButton = new JButton();
			southButton.setIcon(new ImageIcon(getClass().getResource("/images/S.gif")));
			southButton.setIconTextGap(0);
			southButton.setToolTipText("Move viewing area South");
			southButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
			southButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			southButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					plot.moveSouth();
				}
			});
		}
		return southButton;
	}
	/**
	 * This method initializes southWestButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getSouthWestButton() {
		if (southWestButton == null) {
			southWestButton = new JButton();
			southWestButton.setIcon(new ImageIcon(getClass().getResource("/images/SW.png")));
			southWestButton.setIconTextGap(0);
			southWestButton.setToolTipText("Move viewing area South West");
			southWestButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
			southWestButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			southWestButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					plot.moveSouthWest();
				}
			});
		}
		return southWestButton;
	}
	/**
	 * This method initializes centreButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getCentreButton() {
		if (centreButton == null) {
			centreButton = new JButton();
			centreButton.setIcon(new ImageIcon(getClass().getResource("/images/Centre.gif")));
			centreButton.setIconTextGap(0);
			centreButton.setToolTipText("Centre viewing area on radar");
			centreButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
			centreButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			centreButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					plot.centreView();
				}
			});
		}
		return centreButton;
	}
	/**
	 * This method initializes westButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getWestButton() {
		if (westButton == null) {
			westButton = new JButton();
			westButton.setIcon(new ImageIcon(getClass().getResource("/images/W.gif")));
			westButton.setIconTextGap(0);
			westButton.setToolTipText("Move viewing area West");
			westButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
			westButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			westButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					plot.moveWest();
				}
			});
		}
		return westButton;
	}
	/**
	 * This method initializes eastButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getEastButton() {
		if (eastButton == null) {
			eastButton = new JButton();
			eastButton.setIcon(new ImageIcon(getClass().getResource("/images/E.gif")));
			eastButton.setIconTextGap(0);
			eastButton.setToolTipText("Move viewing area East");
			eastButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
			eastButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			eastButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					plot.moveEast();
				}
			});
		}
		return eastButton;
	}
	/**
	 * This method initializes southEastButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getSouthEastButton() {
		if (southEastButton == null) {
			southEastButton = new JButton();
			southEastButton.setIcon(new ImageIcon(getClass().getResource("/images/SE.png")));
			southButton.setIconTextGap(0);
			southEastButton.setToolTipText("Move viewing area South East");
			southEastButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
			southEastButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			southEastButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					plot.moveSouthEast();
				}
			});
		}
		return southEastButton;
	}
	/**
	 * This method initializes latField	
	 * 	
	 * @return javax.swing.JFormattedTextField	
	 */    
	private JFormattedTextField getLatField() {
		if (latField == null) {
			latField = new JFormattedTextField();
			latField.setColumns(6);
			latField.setToolTipText("Enter latitude for centre of viewing area");
			latField.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					latFieldActionPerformed(e);
				}
			});
		}
		return latField;
	}
	/**
	 * This method initializes lonField	
	 * 	
	 * @return javax.swing.JFormattedTextField	
	 */    
	private JFormattedTextField getLonField() {
		if (lonField == null) {
			lonField = new JFormattedTextField();
			lonField.setColumns(6);
			lonField.setToolTipText("Enter longitude of centre of viewing area");
			lonField.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					lonFieldActionPerformed(e);
				}
			});
		}
		return lonField;
	}
	/**
	 * This method initializes jToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */    
	private JToolBar getJToolBar() {
		if (jToolBar == null) {
			jToolBar = new JToolBar();
			jToolBar.add(getZoomIn());
			jToolBar.add(getZoomOut());
			jToolBar.add(getFitViewToWindow());
			jToolBar.add(getShowWorld());
		}
		return jToolBar;
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getShowWorld() {
		if (showWorld == null) {
			showWorld = new JButton();
			showWorld.setIcon(new ImageIcon(getClass().getResource("/images/world.gif")));
			showWorld.setToolTipText("Show entire World");
			showWorld.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					showWorldActionPerformed(e);
				}
			});
		}
		return showWorld;
	}
	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getZoomIn() {
		if (zoomIn == null) {
			zoomIn = new JButton();
			zoomIn.setIcon(new ImageIcon(getClass().getResource("/images/zoomIn.gif")));
			zoomIn.setToolTipText("Zoom in");
			zoomIn.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					plot.zoomIn();
				}
			});
		}
		return zoomIn;
	}
	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getFitViewToWindow() {
		if (fitViewToWindow == null) {
			fitViewToWindow = new JButton();
			fitViewToWindow.setIcon(new ImageIcon(getClass().getResource("/images/normal.gif")));
			fitViewToWindow.setToolTipText("Reset zoom to default");
			fitViewToWindow.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					plot.resetZoom();
				}
			});
		}
		return fitViewToWindow;
	}
	/**
	 * This method initializes jButton3	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getZoomOut() {
		if (zoomOut == null) {
			zoomOut = new JButton();
			zoomOut.setIcon(new ImageIcon(getClass().getResource("/images/zoomOut.gif")));
			zoomOut.setToolTipText("Zoom out");
			zoomOut.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					plot.zoomOut();
				}
			});
		}
		return zoomOut;
	}
	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JFormattedTextField getScaleField() {
		if (scaleField == null) {
			scaleField = new JFormattedTextField();
			scaleField.setColumns(4);
			scaleField.setToolTipText("Enter zoom scale factor");
			scaleField.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					scaleFieldActionPerformed(e);
				}
			});
		}
		return scaleField;
	}
	

    private void showWorldActionPerformed(java.awt.event.ActionEvent evt) {
        plot.setCentre( new Geographic(0.0, 0.0, 0.0));
        plot.setScale( 0.15 );
    }

    private void lonFieldActionPerformed(java.awt.event.ActionEvent evt) {
        JFormattedTextField tf = (JFormattedTextField) evt.getSource();
        if ( tf.getText() == null ) {
            return;
        }
        
        try {
            double value = verifyDecimalFieldInput(tf, -180.0, 180.0 );
            plot.setCenteLongitude(value);
        }
        catch( NumberFormatException e ) {
            // not much we can do with bad input in a GUI
            // unless we popup a dialog box for information
        }

    }

    private void latFieldActionPerformed(java.awt.event.ActionEvent evt) {
        JFormattedTextField tf = (JFormattedTextField) evt.getSource();
        if ( tf.getText() == null ) {
            return;
        }
        
        try {
            double value = verifyDecimalFieldInput(tf, -89.999, 89.999);
            plot.setCenteLatitude(value);
        }
        catch( NumberFormatException e ) {
            // not much we can do with bad input in a GUI
            // unless we popup a dialog box for information
        }

    }

    private void scaleFieldActionPerformed(java.awt.event.ActionEvent evt) {
        JFormattedTextField tf = (JFormattedTextField) evt.getSource();
        if ( tf.getText() == null ) {
            return;
        }
        
        try {
            double value = verifyDecimalFieldInput(tf, 0.0, Double.MAX_VALUE);
            plot.setScale(value*defaultScale);
        }
        catch( NumberFormatException e ) {
            // not much we can do with bad input in a GUI
            // unless we popup a dialog box for information
        }

    }
    private double verifyDecimalFieldInput( JFormattedTextField tf, double min, double max ) 
    throws NumberFormatException {

        // attempt to interpret the value as a double
        double d = Double.parseDouble(tf.getText());

        return d;
    }
    
    public void setScaleValue( Object value ) {
        scaleField.setValue(value);
    }
    public void setLatValue( Object value ) {
        latField.setValue(value);
    }
    public void setLonValue( Object value ) {
        lonField.setValue( value );
    }
	
}  //  @jve:decl-index=0:visual-constraint="25,41"

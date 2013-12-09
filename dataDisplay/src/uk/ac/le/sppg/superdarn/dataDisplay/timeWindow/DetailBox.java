package uk.ac.le.sppg.superdarn.dataDisplay.timeWindow;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * @author Nigel Wade
 */
class DetailBox extends JFrame {

    private static final long serialVersionUID = 0x525350504700003EL;
    
    TimePlot plot;
    
    JTextField dateField,
               dataField,
               latField,
               lonField,
               gateField,
               rangeField,
               beamField,
               freqField,
               noiseField; 
               
    JLabel groundScatter;
    
    private DateFormat dateFormat = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG );
    private NumberFormat geoFormat = NumberFormat.getNumberInstance();
    private NumberFormat decimalFormat = NumberFormat.getNumberInstance();
    private NumberFormat exponentFormat = new DecimalFormat( "#.###E0" );
    private NumberFormat integerFormat = NumberFormat.getIntegerInstance();
    
    static final char degreeSymbol = '\u00b0';
    

    public DetailBox( String title, TimePlot plot ) {
        
        super( title );
        
        dateFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
         
        geoFormat.setMaximumFractionDigits( 1 );
        decimalFormat.setMaximumFractionDigits( 3 ); 

        this.plot = plot;
        createDetailBox();      
    }
    
    // the detailBox shows details of the data at a location in the panel
    // if the location clicked is within a range cell it shows info about
    // the cell and the data from that cell.
    private void createDetailBox() {
        
        JPanel contentPane = new JPanel();
        
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        contentPane.setLayout( gbLayout );
        
        // The date field at the top spans all columns
        dateField = new JTextField();
        dateField.setEditable( false );
        dateField.setBorder( BorderFactory.createLoweredBevelBorder() );
        dateField.setHorizontalAlignment( JTextField.CENTER );
        
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy = 0;
        
        gbLayout.setConstraints( dateField, c );
        contentPane.add( dateField );
        
        
        // The first column of labels
        
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.fill = 0;
        c.insets = new Insets( 5, 5, 5, 5 );
        c.anchor = GridBagConstraints.EAST;
        
        JLabel label = new JLabel( "Lat:" );
        gbLayout.setConstraints( label, c );
        contentPane.add( label );
        
        c.gridy = GridBagConstraints.RELATIVE;
        
        label = new JLabel( "Beam:" );
        gbLayout.setConstraints( label, c );
        contentPane.add( label );
        
        label = new JLabel( "Range:" );
        gbLayout.setConstraints( label, c );
        contentPane.add( label );

        label = new JLabel( "Value:" );
        gbLayout.setConstraints( label, c );
        contentPane.add( label );
        
        
        // column 2, the first set of fields
        c.gridx = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets( 5, 0, 5, 5 );


        latField = new JTextField( 6 );
        latField.setEditable( false );
        latField.setBorder( BorderFactory.createLoweredBevelBorder() );
        latField.setHorizontalAlignment( JTextField.RIGHT );
        c.weightx = 0.5;
        gbLayout.setConstraints( latField, c );
        contentPane.add( latField );
        
        c.gridy = GridBagConstraints.RELATIVE;

        beamField = new JTextField( 6 );
        beamField.setEditable( false );
        beamField.setBorder( BorderFactory.createLoweredBevelBorder() );
        beamField.setHorizontalAlignment( JTextField.RIGHT );
        gbLayout.setConstraints( beamField, c );
        contentPane.add( beamField );
        
        rangeField = new JTextField( 6 );
        rangeField.setEditable( false );
        rangeField.setBorder( BorderFactory.createLoweredBevelBorder() );
        rangeField.setHorizontalAlignment( JTextField.RIGHT );
        gbLayout.setConstraints( rangeField, c );
        contentPane.add( rangeField );

        dataField = new JTextField( 6 );
        dataField.setEditable( false );
        dataField.setBorder( BorderFactory.createLoweredBevelBorder() );
        dataField.setHorizontalAlignment( JTextField.RIGHT );
        gbLayout.setConstraints( dataField, c );
        contentPane.add( dataField );
        
        // column 3, another set of labels.
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets( 5, 5, 5, 5 );
        
        label = new JLabel( "Lon:" );
        //label.setBorder( BorderFactory.createLineBorder( Color.BLACK ));
        gbLayout.setConstraints( label, c );
        contentPane.add( label );
        
        c.gridy = GridBagConstraints.RELATIVE;
        
        label = new JLabel( "Gate:" );
        gbLayout.setConstraints( label, c );
        contentPane.add( label );
        
        label = new JLabel( "Freq:" );
        gbLayout.setConstraints( label, c );
        contentPane.add( label );

        label = new JLabel( "Noise:" );
        gbLayout.setConstraints( label, c );
        contentPane.add( label );        

        // column 4, the second set of fields
        c.gridx = 3;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets( 5, 0, 5, 5 );

        lonField = new JTextField( 6 );
        lonField.setEditable( false );
        lonField.setBorder( BorderFactory.createLoweredBevelBorder() );
        lonField.setHorizontalAlignment( JTextField.RIGHT );
        c.weightx = 0.5;
        gbLayout.setConstraints( lonField, c );
        contentPane.add( lonField );
        
        c.gridy = GridBagConstraints.RELATIVE;
        
        gateField = new JTextField( 6 );
        gateField.setEditable( false );
        gateField.setBorder( BorderFactory.createLoweredBevelBorder() );
        gateField.setHorizontalAlignment( JTextField.RIGHT );
        gbLayout.setConstraints( gateField, c );
        contentPane.add( gateField );
        
        freqField = new JTextField( 6 );
        freqField.setEditable( false );
        freqField.setBorder( BorderFactory.createLoweredBevelBorder() );
        freqField.setHorizontalAlignment( JTextField.RIGHT );
        gbLayout.setConstraints( freqField, c );
        contentPane.add( freqField );

        noiseField = new JTextField( 6 );
        noiseField.setEditable( false );
        noiseField.setBorder( BorderFactory.createLoweredBevelBorder() );
        noiseField.setHorizontalAlignment( JTextField.RIGHT );
        gbLayout.setConstraints( noiseField, c );
        contentPane.add( noiseField );
        
        // the ground scatter label and checkbox
        // column 3, another set of labels.
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        
        label = new JLabel( "Ground Scatter:" );
        //label.setBorder( BorderFactory.createLineBorder( Color.BLACK ));
        gbLayout.setConstraints( label, c );
        contentPane.add( label );
        
        c.gridx = 2;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        groundScatter = new JLabel( "No" );
        groundScatter.setBorder( BorderFactory.createLineBorder( Color.BLACK ));
        gbLayout.setConstraints( groundScatter, c );
        contentPane.add( groundScatter );
        
        
        this.setContentPane( contentPane );
        
        this.pack();

    }
    
    // determine the details at location Date, range.
    // search the beams for the rangeGate containing this point
    // and display the data in the frame
    public void showDetails( long dateTime, double range ) {
        

        
        ArrayList beams = plot.getBeams();
        
        // draw the filled gates.
        DetailData dd;
        
        
        for ( Iterator i = beams.iterator(); i.hasNext(); ) {
            Beam beam = (Beam) i.next();
            
            if ( (dd = beam.findDetails( dateTime, range )) != null ) {
                dateField.setText( dateFormat.format( dd.date ) );
                
                latField.setText( geoFormat.format( dd.lat )+degreeSymbol );
                lonField.setText( geoFormat.format( dd.lon )+degreeSymbol );
                rangeField.setText( geoFormat.format( dd.range ) );
                
                beamField.setText( String.valueOf( dd.beam ) );
                gateField.setText( String.valueOf( dd.gate ) );
                
                noiseField.setText( integerFormat.format( dd.noise ) );
                freqField.setText( integerFormat.format( dd.freq ) );
                
                if ( dd.flag ) 
                    if ( Math.abs(dd.data) > 0.01 && Math.abs(dd.data) < 1000 ) 
                        dataField.setText( decimalFormat.format( dd.data ) );
                    else
                        dataField.setText( exponentFormat.format( dd.data ) );
                else
                    dataField.setText( "No data" );
                    
                if ( dd.gs ) 
                    groundScatter.setText( "Yes" );
                else
                    groundScatter.setText( "No" );
                
            }
        
        }
    }
    
}

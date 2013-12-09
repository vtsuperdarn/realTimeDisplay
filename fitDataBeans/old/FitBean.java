/*
 * FitBean.java
 *
 * Created on 07 August 2003, 12:09
 */


package superdarn.fitDataBeans;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import superdarn.fitData.FitData;



/**
 *
 * @author  nigel
 */
public class FitDataBean extends JPanel {
    
    private static final long serialVersionUID = 0x5253505047000044L;
    
    class FitDataValue extends Number {
        
        private static final long serialVersionUID = 0x5253505047000045L;
        
        FitDataValue(double init) {
            value = init;
        }
        public double value;
        
        public String toString() {
            return String.valueOf( value );
        }
        
        public double doubleValue() {
            return value;
        }
        
        public float floatValue() {
            return (float)value;
        }
        
        public int intValue() {
            return (int) value;
        }
        
        public long longValue() {
            return (long) value;
        }
        
    }
    
    class FitDataModel extends AbstractTableModel {
        
        private static final long serialVersionUID = 0x5253505047000025L;
        
        class FitDataData {
            Integer gateNumber;
            Double range;
            Boolean groundScatter;
            FitDataValue power;
            FitDataValue velocity;
            FitDataValue width;
            
            FitDataData( int gate, double range, boolean gs, double pow, double vel, double w ) {
                this.gateNumber = new Integer(gate);
                this.range = new Double(range);
                this.groundScatter = new Boolean(gs);
                this.power = new FitDataValue(pow);
                this.velocity = new FitDataValue(vel);
                this.width = new FitDataValue(w);
            }
        }
        
        ArrayList<FitDataData> data = new ArrayList<FitDataData>();
        
        final String[] columnTitles = {
            "Range gate",
            "Range (km)",
            "Ground scatter",
            "Power",
            "Velocity",
            "Width"
        };
        final int COLUMNS = columnTitles.length;
        
        
        public int getColumnCount() {
            return COLUMNS;
        }
        
        public int getRowCount() {
            return data.size();
        }
        
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object result = "";
            
            if ( rowIndex >= 0 && rowIndex < data.size() ) {
                FitDataData datum = (FitDataData) data.get(rowIndex);
                switch( columnIndex ) {
                    case 0: result = datum.gateNumber; break;
                    case 1: result = datum.range; break;
                    case 2: result = datum.groundScatter; break;
                    case 3: result = datum.power; break;
                    case 4: result = datum.velocity; break;
                    case 5: result = datum.width; break;
                }
            }
            
            return result;
        }
        
        public void addRow( int gate, double range, boolean gs, double pow, double vel, double w ) {
            data.add( new FitDataData(gate, range, gs, pow, vel, w));
            fireTableRowsInserted( data.size()-1, data.size()-1 );
        }
        
        public void clear() {
            data.clear();
            fireTableDataChanged();
        }
        
        //        public boolean isCellEditable( int row, int col ) { return false; }
        public String getColumnName(int col) {
            String result;
            
            try {
                result = columnTitles[col];
            } catch ( IndexOutOfBoundsException e ) {
                result = super.getColumnName(col);
            }
            
            return result;
        }
        
        public int findColumn( String name ) {
            for( int i=0; i<columnTitles.length; i++ ) {
                if ( columnTitles[i].equals(name) )
                    return i;
            }
            
            return -1;
        }
        
        public Class getColumnClass( int col ) {
            return getValueAt( 0, col ).getClass();
        }
        
    }
    
    private FontMetrics metrics;
    private DateFormat dateFormat = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG );
    // date/time format for the dateField
    // currently LONG format for locale GMT
    
    /** Creates new form BeanForm */
    public FitDataBean() {
        initComponents();
        dateFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
        
        TableColumn column = parmsTable.getColumnModel().getColumn(0);
        column.setPreferredWidth( 20 );
        column = parmsTable.getColumnModel().getColumn(2);
        column.setPreferredWidth( 20 );
        
        dataTable.setDefaultRenderer(FitDataValue.class, new ValueRenderer());
        
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        topPanel = new javax.swing.JPanel();
        dateField = new javax.swing.JTextField();
        cpidField = new javax.swing.JTextField();
        beamField = new javax.swing.JTextField();
        dataPane = new javax.swing.JTabbedPane();
        parmsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        parmsTable = new javax.swing.JTable();
        dataPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dataTable = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        dateField.setEditable(false);
        dateField.setColumns(20);
        dateField.setToolTipText("Date of data");
        dateField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        topPanel.add(dateField);

        cpidField.setColumns(5);
        cpidField.setEditable(false);
        cpidField.setToolTipText("CPID");
        cpidField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        topPanel.add(cpidField);

        beamField.setColumns(2);
        beamField.setEditable(false);
        beamField.setToolTipText("Beam number");
        beamField.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        topPanel.add(beamField);

        add(topPanel, java.awt.BorderLayout.NORTH);

        parmsPanel.setLayout(new java.awt.BorderLayout());

        parmsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Revision", null, null},
                {"Station id", null, null},
                {"Date", null, null},
                {"Tx power", null, "kW"},
                {"No. Ave.", null, ""},
                {"Attn.", null, "dB"},
                {"Lag to 1st ", null, "uS"},
                {"Sample sep", null, "uS"},
                {"Error code", null, null},
                {"AGC status", null, null},
                {"Low pwr status", null, null},
                {"No. Baud", null, null},
                {"Noise level", null, null},
                {"Noise mean", null, null},
                {"Channel", null, null},
                {"Rx rise time", null, "uS"},
                {"Integration", null, "S"},
                {"Pulse length", null, "uS"},
                {"Lag sep.", null, "uS"},
                {"No. pulses", null, null},
                {"No. lags", null, null},
                {"No. ranges", null, null},
                {"First range", null, "km"},
                {"Range sep.", null, "km"},
                {"Beam no.", null, null},
                {"X corr.", null, null},
                {"Tx freq.", null, "kHz"},
                {"Scan flag", null, null},
                {"Max. power", null, null},
                {"Max. noise", null, null},
                {"User L1", null, null},
                {"User L2", null, null},
                {"CPID", null, null},
                {"User S1", null, null},
                {"User S2", null, null},
                {"User S3", null, null}
            },
            new String [] {
                "Name", "Value", "Notes"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(parmsTable);

        parmsPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        dataPane.addTab("Radar parms", null, parmsPanel, "Radar parameters");

        dataPanel.setLayout(new java.awt.BorderLayout());

        dataTable.setModel(dataModel);
        jScrollPane2.setViewportView(dataTable);

        dataPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        dataPane.addTab("Data", null, dataPanel, "Data");

        add(dataPane, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField beamField;
    private javax.swing.JTextField cpidField;
    private javax.swing.JTabbedPane dataPane;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JTable dataTable;
    private javax.swing.JTextField dateField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel parmsPanel;
    private javax.swing.JTable parmsTable;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
    
    FitDataModel dataModel = new FitDataModel();
    
    public void setData( FitData data ) {
        
        if ( metrics == null ) {
            Graphics graphics = this.getGraphics();
            if ( graphics != null ) {
                metrics = graphics.getFontMetrics();
                
                TableModel model = parmsTable.getModel();
                
                for ( int col=0; col<3; col+=2 ) {
                    String str = model.getColumnName(col);
                    int width = metrics.stringWidth( str );
                    int maxWidth = width;
                    
                    for ( int i=0; i<model.getRowCount();  i++ ) {
                        str = (String) model.getValueAt(i, col);
                        if ( str != null ) {
                            width = metrics.stringWidth( str );
                            maxWidth = Math.max( width, maxWidth );
                        }
                    }
                    
                    TableColumn column = parmsTable.getColumnModel().getColumn(col);
                    column.setPreferredWidth( maxWidth );
                    
                }
                
                parmsTable.invalidate();
                this.validate();
                
            }
        }
        
        if ( data == null ) {
            clear();
            return;
        }

        // set the values of the textFields
        String str = dateFormat.format( data.radarParms.date );
        dateField.setText( str );
        
        cpidField.setText( String.valueOf( data.radarParms.controlProgramId ) );
        beamField.setText( String.valueOf( data.radarParms.beamNumber ) );
        
        // set the values in the radarParms table
        
        // find the index of the Value column
        int columnIndex = -1;
        for ( int index=0; index<parmsTable.getColumnCount(); index++ ) {
            if ( parmsTable.getColumnName(index).equals("Value") ) {
                columnIndex = index;
                break;
            }
        }
        if ( columnIndex != -1 ) {
            parmsTable.setValueAt( new Float( data.radarParms.revision.major+data.radarParms.revision.minor*0.1 ), 0, columnIndex );
            parmsTable.setValueAt( new Short( data.radarParms.stationId ), 1, columnIndex );
            parmsTable.setValueAt( str, 2, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.txPower), 3, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.numberAveraged), 4, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.attnLevel), 5, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.lagToFirstRange), 6, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.sampleSeparation), 7, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.errorCode), 8, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.AGCstatusWord), 9, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.lowPowerStatusWord), 10, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.nBaud), 11, columnIndex );
            parmsTable.setValueAt( new Integer(data.radarParms.noiseLevel), 12, columnIndex );
            parmsTable.setValueAt( new Integer(data.radarParms.noiseMean), 13, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.channel), 14, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.rxRiseTime), 15, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.integrationPeriod), 16, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.txPulseLength),17, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.MPlagSeparation), 18, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.MPnPulses), 19, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.MPnLags), 20, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.numberOfRanges), 21, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.firstRangeDistance), 22, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.rangeSeparation), 23, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.beamNumber), 24, columnIndex );
            
            parmsTable.setValueAt( new Boolean(data.radarParms.crossCorrelation), 25, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.txFrequency), 26, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.scanFlag), 27, columnIndex );
            parmsTable.setValueAt( new Integer(data.radarParms.maxPower), 28, columnIndex );
            parmsTable.setValueAt( new Integer(data.radarParms.maxNoise), 29, columnIndex );
            parmsTable.setValueAt( new Integer(data.radarParms.usrResL1), 30, columnIndex );
            parmsTable.setValueAt( new Integer(data.radarParms.usrResL2), 31, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.controlProgramId), 32, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.usrResS1), 33, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.usrResS2), 34, columnIndex );
            parmsTable.setValueAt( new Short(data.radarParms.usrResS3), 35, columnIndex );
            
            ((AbstractTableModel)parmsTable.getModel()).fireTableDataChanged();
            
        }
        
        dataModel.clear();
        for ( int index=0; index<data.radarParms.numberOfRanges; index++ ) {
            dataModel.addRow(index,
                    data.radarParms.firstRangeDistance+index*data.radarParms.rangeSeparation,
                    data.rangeData[index].groundScatterFlag,
                    data.rangeData[index].lambdaPower,
                    data.rangeData[index].velocity,
                    data.rangeData[index].lambdaSpectralWidth );
        }
    }
    
    public void clear() {
        // set the values of the textFields
        dateField.setText( "" );
        
        cpidField.setText( "" );
        beamField.setText( "" );
        
        
        // find the index of the Value column
        int columnIndex = -1;
        for ( int index=0; index<parmsTable.getColumnCount(); index++ ) {
            if ( parmsTable.getColumnName(index).equals("Value") ) {
                columnIndex = index;
                break;
            }
        }
        
        if ( columnIndex != -1 ) {
            int rows = parmsTable.getRowCount();
            for(int row=0; row<rows; row++) {
                parmsTable.setValueAt( "", row, columnIndex );
            }
        }
        
        dataModel.clear();
        
    }
    
    
    // renderer for characters to get them right-aligned.
    // also renders the '\0' character as <null>
    
    private class ValueRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 0x5253505047000027L;
        
        ValueRenderer() {
            super();
            setHorizontalAlignment(RIGHT);
        }
        
        DecimalFormat formatter = new DecimalFormat( "#0.0" );
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            Object gs = table.getValueAt(row, 2);
            String str = formatter.format( value );
            setValue(str);
            
            boolean b = ((Boolean)gs).booleanValue();
            
            c.setEnabled( !b );
//				  if ( b ) {
//					  c.setBackground( table.getForeground() );
//					  c.setForeground( table.getBackground() );
//				  }
//				  else {
//					  c.setBackground( table.getBackground() );
//					  c.setForeground( table.getForeground() );
//				  }
            
            return c;
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.fitDataBeans;

import uk.ac.le.sppg.coords.superdarn.SuperDarnSite;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import java.text.DateFormat;
import java.util.TimeZone;
import javax.swing.table.DefaultTableModel;
import uk.ac.le.sppg.superdarn.fitData.RadarParms2;

/**
 *
 * @author nigel
 */
public class ParmsDataModel 
extends DefaultTableModel {
    
    private static final SuperDarnSiteList siteList = SuperDarnSiteList.getList();
    
    private DateFormat dateFormat = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG );
    private static class ParmsData {
        protected static final Object[][] initialData = 
             new Object[][] {
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
                {"Command", null, null},
                {"Orig date", null, null},
                {"Combf", null, null},
                {"User L1", null, null},
                {"User L2", null, null},
                {"CPID", null, null},
                {"User S1", null, null},
                {"User S2", null, null},
                {"User S3", null, null}
        };
        
    }
    
    static final String[] columnNames = {"Name", "Value", "Notes"};
    
    public ParmsDataModel() {
        super(ParmsData.initialData, columnNames);
        dateFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
    }
    
    public void setData(RadarParms2 newParameters) {

        if ( newParameters == null )
            return;
        
//        Object[][] newData = ParmsData.getInitialData();
        
        int columnIndex = -1;
//        for ( int index=0; index<getColumnCount(); index++ ) {
//            if ( getColumnName(index).equals("Value") ) {
//                columnIndex = index;
//                break;
//            }
//        }
        
//        setValueAt(dateFormat.format( newParameters.date ), 2, 1);
        
        columnIndex = 1;
        if ( columnIndex != -1 ) {
            SuperDarnSite site = siteList.getById(newParameters.stationId);
            setValueAt(new Float( newParameters.revision.major+newParameters.revision.minor*0.1 ), 0, columnIndex);
            String station = String.valueOf(newParameters.stationId);
            if ( site != null )
                station = station + "  (" + site.getName() +")";
            setValueAt(station, 1, columnIndex);
            setValueAt(dateFormat.format( newParameters.date ), 2, columnIndex);
            setValueAt(new Short(newParameters.txPower), 3, columnIndex);
            setValueAt(new Short(newParameters.numberAveraged), 4, columnIndex);
            setValueAt(new Short(newParameters.attnLevel), 5, columnIndex);
            setValueAt(new Short(newParameters.lagToFirstRange), 6, columnIndex);
            setValueAt(new Short(newParameters.sampleSeparation), 7, columnIndex);
            setValueAt(new Short(newParameters.errorCode), 8, columnIndex);
            setValueAt(new Short(newParameters.AGCstatusWord), 9, columnIndex);
            setValueAt(new Short(newParameters.lowPowerStatusWord), 10, columnIndex);
            setValueAt(new Short(newParameters.nBaud), 11, columnIndex);
            setValueAt(new Integer(newParameters.noiseLevel), 12, columnIndex);
            setValueAt(new Integer(newParameters.noiseMean), 13, columnIndex);
            setValueAt(new Short(newParameters.channel), 14, columnIndex);
            setValueAt(new Short(newParameters.rxRiseTime), 15, columnIndex);
            setValueAt(new Short(newParameters.integrationPeriod), 16, columnIndex);
            setValueAt(new Short(newParameters.txPulseLength), 17, columnIndex);
            setValueAt(new Short(newParameters.MPlagSeparation), 18, columnIndex);
            setValueAt(new Short(newParameters.MPnPulses), 19, columnIndex);
            setValueAt(new Short(newParameters.MPnLags), 20, columnIndex);
            setValueAt(new Short(newParameters.numberOfRanges), 21, columnIndex);
            setValueAt(new Short(newParameters.firstRangeDistance), 22, columnIndex);
            setValueAt(new Short(newParameters.rangeSeparation), 23, columnIndex);
            setValueAt(new Short(newParameters.beamNumber), 24, columnIndex);
            setValueAt(new Boolean(newParameters.crossCorrelation), 25, columnIndex);
            setValueAt(new Short(newParameters.txFrequency), 26, columnIndex);
            setValueAt(new Short(newParameters.scanFlag), 27, columnIndex);
            setValueAt(new Integer(newParameters.maxPower), 28, columnIndex);
            setValueAt(new Integer(newParameters.maxNoise), 29, columnIndex);
            
            if ( newParameters.origin != null ) {
              setValueAt(newParameters.origin.command, 30, columnIndex);
              setValueAt(newParameters.origin.time, 31, columnIndex);
            }
            setValueAt(newParameters.comment, 32, columnIndex);

            setValueAt(new Integer(newParameters.usrResL1), 33, columnIndex);
            setValueAt(new Integer(newParameters.usrResL2), 34, columnIndex);
            setValueAt(new Short(newParameters.controlProgramId), 35, columnIndex);
            setValueAt(new Short(newParameters.usrResS1), 36, columnIndex);
            setValueAt(new Short(newParameters.usrResS2), 37, columnIndex);
            setValueAt(new Short(newParameters.usrResS3), 38, columnIndex);
        }    
        
    }
    
    public void clear() {
                
        setDataVector(ParmsData.initialData, columnNames);
        
    }
    
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}

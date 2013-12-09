/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.fitDataBeans;

import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import uk.ac.le.sppg.superdarn.fitData.FitData;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;
import uk.ac.le.sppg.superdarn.fitData.NetFitData;

/**
 *
 * @author nigel
 */
public class RangeDataModel 
extends DefaultTableModel {
    
    private static final long serialVersionUID = 0x5253505047000025L;

    class FitDataData {

        Integer gateNumber;
        Double range;
        Boolean groundScatter;
        FitDataValue power;
        FitDataValue velocity;
        FitDataValue width;

        FitDataData(int gate, double range, boolean gs, double pow, double vel, double w) {
            this.gateNumber = new Integer(gate);
            this.range = new Double(range);
            this.groundScatter = new Boolean(gs);
            this.power = new FitDataValue(pow);
            this.velocity = new FitDataValue(vel);
            this.width = new FitDataValue(w);
        }
    }
    
    ArrayList<FitDataData> data = new ArrayList<FitDataData>();
    
    static final String[] columnNames = {
        "Range gate", "Range (km)", "Ground scatter", "Power", "Velocity", "Width"
    };
    final int COLUMNS = columnNames.length;

    public RangeDataModel() {
        setColumnIdentifiers(columnNames);
    }
    

    public int getColumnCount() {
        return COLUMNS;
    }

    public int getRowCount() {
        if ( data == null )
            return 0;
        return data.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = "";

        if (rowIndex >= 0 && rowIndex < data.size()) {
            FitDataData datum = (FitDataData) data.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    result = datum.gateNumber;
                    break;
                case 1:
                    result = datum.range;
                    break;
                case 2:
                    result = datum.groundScatter;
                    break;
                case 3:
                    result = datum.power;
                    break;
                case 4:
                    result = datum.velocity;
                    break;
                case 5:
                    result = datum.width;
                    break;
                }
        }

        return result;
    }

    public void addRow(int gate, double range, boolean gs, double pow, double vel, double w) {
        data.add(new FitDataData(gate, range, gs, pow, vel, w));
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void clear() {
        data.clear();
        fireTableDataChanged();
    }

    //        public boolean isCellEditable( int row, int col ) { return false; }
    public String getColumnName(int col) {
        String result;

        try {
            result = columnNames[col];
        } catch (IndexOutOfBoundsException e) {
            result = super.getColumnName(col);
        }

        return result;
    }

    public int findColumn(String name) {
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public Class getColumnClass(int col) {
        return getValueAt(0, col).getClass();
    }
    
  public void setData(NetFitData newData) {
    data.clear();
    if (newData == null || newData.ranges.length == 0 ) {
      fireTableDataChanged();
      return;
    }

    for (int index = 0; index < newData.ranges.length; index++) {
      addRow(newData.ranges[index],
              newData.radarParms.firstRangeDistance + newData.ranges[index] * newData.radarParms.rangeSeparation,
              newData.groundScatter[index],
              newData.velocity[index],
              newData.lambdaPower[index],
              newData.lambdaSpectralWidth[index]);
    }
  }
    
  public void setData(FitacfData newData) {
    data.clear();
    if (newData == null || newData.ranges.length == 0 ) {
      fireTableDataChanged();
      return;
    }

    for (int index = 0; index < newData.ranges.length; index++) {
      addRow(newData.ranges[index],
              newData.radarParms.firstRangeDistance + newData.ranges[index] * newData.radarParms.rangeSeparation,
              newData.groundScatter[index],
              newData.velocity[index],
              newData.lambdaPower[index],
              newData.lambdaSpectralWidth[index]);
    }
  }

  public void setData(FitData newData) {
    data.clear();
    if (newData == null || newData.radarParms.numberOfRanges == 0 ) {
      fireTableDataChanged();
      return;
    }
    for (int index = 0; index < newData.radarParms.numberOfRanges; index++) {
      addRow(index,
              newData.radarParms.firstRangeDistance + index * newData.radarParms.rangeSeparation,
              newData.rangeData[index].groundScatterFlag,
              newData.rangeData[index].lambdaPower,
              newData.rangeData[index].velocity,
              newData.rangeData[index].lambdaSpectralWidth);
    }
  }
 
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package superdarn.fitDataBeans;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import superdarn.fitData.FitData;

/**
 *
 * @author nigel
 */
public class FitDataModel
        extends AbstractTableModel {

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
            result = columnTitles[col];
        } catch (IndexOutOfBoundsException e) {
            result = super.getColumnName(col);
        }

        return result;
    }

    public int findColumn(String name) {
        for (int i = 0; i < columnTitles.length; i++) {
            if (columnTitles[i].equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public Class getColumnClass(int col) {
        return getValueAt(0, col).getClass();
    }
    
    public void setData(FitData newData) {
        
    }
}

package com.xhive.adminclient.tablemodels;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public abstract class XhiveAbstractTableModel extends AbstractTableModel {

    private ArrayList data = new ArrayList();

    public String getColumnName(int column) {
        return getColumnNames()[column];
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return getColumnNames().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Object[] elements = (Object[]) data.get(rowIndex);
        return elements[columnIndex];
    }

    protected void addRow(Object[] row) {
        data.add(row);
    }

    protected void removeRow(int index) {
        data.remove(index);
    }

    protected void clear() {
        data.clear();
    }

    protected abstract String[] getColumnNames();
}

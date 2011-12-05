package com.xhive.adminclient.tablemodels;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Xhive table model
 *
 *
 */
public abstract class XhiveTableModel extends AbstractTableModel {

    public String[] columns = null;
    Vector data = new Vector();

    public int getColumnCount() {
        return columns.length;
    }

    public abstract Vector getVector();

    public int getRowCount() {
        return data.size();
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public Object elementAt(int row) {
        return data.elementAt(row);
    }

    public Object getValueAt(int row, int column) {
        Object element = data.elementAt(row);
        return getValueAt(element, column);
    }

    public Object getValueAt(Object element, int column) {
        return ((Object[]) element)[column];
    }

    public void addRow(Object[] row) {}


    public void removeRow(int row) {}


    public void addElement(Object element) {
        int index = data.size();
        data.addElement(element);
        fireTableRowsInserted(index, index);
    }

    public void removeElement(int index) {
        data.removeElementAt(index);
        fireTableRowsDeleted(index, index);
    }

    public void changeElement(int index) {
        fireTableRowsUpdated(index, index);
    }

    public boolean elementIsReadable(Object element) {
        return true;
    }

    public void clear() {
        while (getRowCount() > 0) {
            removeRow(0);
        }
    }

    public void loadData() {
        Vector vector = getVector();
        data = vector;
    }

    public void refresh() {
        data.removeAllElements();
        loadData();
    }
}

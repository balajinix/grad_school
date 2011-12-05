package com.xhive.adminclient.treenodes;

import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedSwingWorker;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.Vector;

/**
 * Tree node with right pane
 */
public abstract class XhiveTableModelTreeNode extends XhiveExtendedTreeNode implements TableModel {

    // The default column names
    private static final String[] DEFAULT_COLUMN_NAMES = {"name", "description"};
    private final String[] columnNames;
    private Vector<TableModelListener> tableModelListeners = new Vector<TableModelListener>();

    protected XhiveTableModelTreeNode(XhiveDatabaseTree databaseTree, Object object) {
        this(databaseTree, object, DEFAULT_COLUMN_NAMES);
    }

    protected XhiveTableModelTreeNode(XhiveDatabaseTree databaseTree, Object object, String[] columnNames) {
        super(databaseTree, object);
        this.columnNames = columnNames;
    }

    // TableModel implementation and related methods
    @Override
    public String getExpandedIconName() {
        return XhiveResourceFactory.EXPANDED_FOLDER_ICON;
    }

    private Object getChildValueAtColumn(int rowIndex, int columnIndex) {
        Object childValues[] = ((XhiveExtendedTreeNode) getChildAt(rowIndex)).getPropertyValues();
        return columnIndex < childValues.length ? childValues[columnIndex] : "";
    }

    @Override
    public boolean hasContentsPane() {
        return true;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    public int getRowCount() {
        return getChildCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < getChildCount()) {
            if (getChildAt(rowIndex) instanceof XhiveExtendedTreeNode) {
                return getChildValueAtColumn(rowIndex, columnIndex);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return Object.class;
    }

    public void addTableModelListener(TableModelListener l) {
        tableModelListeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l) {
        tableModelListeners.remove(l);
    }

    public void fireTableDataChanged() {
        notifyTableModelListeners(new TableModelEvent(this, 0, getChildCount(), TableModelEvent.ALL_COLUMNS,
                                  TableModelEvent.INSERT));
    }

    public void fireTableRowsDeleted(int firstRow, int lastRow) {
        notifyTableModelListeners(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
                                  TableModelEvent.DELETE));
    }

    public void fireTableRowsInserted(int firstRow, int lastRow) {
        notifyTableModelListeners(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
                                  TableModelEvent.INSERT));
    }

    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        notifyTableModelListeners(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
                                  TableModelEvent.UPDATE));
    }

    private void notifyTableModelListeners(TableModelEvent tme) {
        for (int i = 0; i < tableModelListeners.size(); i++) {
            tableModelListeners.elementAt(i).tableChanged(tme);
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // Default no-op
    }


    protected XhiveExtendedTreeNode createChild(Object userObject1) {
        return null;
    }

    public XhiveExtendedTreeNode addChild(Object userObject1, int childIndex) {
        // Create the new child node
        XhiveExtendedTreeNode newChild = createChild(userObject1);
        // Update tree
        getTreeModel().insertNodeInto(newChild, this, childIndex);
        // Notify table model listeners
        fireTableRowsInserted(childIndex, childIndex);
        return newChild;
    }

    // Convenience method that adds the child at the end of the list
    public XhiveExtendedTreeNode addChild(Object userObject1) {
        return addChild(userObject1, getChildCount());
    }

    @Override
    public void doDelete(XhiveSessionIf session) {
        if (isDeletable() && confirmDeletion()) {
            XhiveTransactedSwingWorker worker = new XhiveTransactedSwingWorker(false) {
                                                    @Override
                                                    protected Object xhiveConstruct() throws Exception {
                                                        deleteAction(getSession());
                                                        return null;
                                                    }

                                                    @Override
                                                    protected void xhiveFinished(Object result) {
                                                        // Notify table model
                                                        int index = getParent().getIndex(XhiveTableModelTreeNode.this);
                                                        fireTableRowsDeleted(index, index);
                                                        // Notify tree node
                                                        getTreeModel().removeNodeFromParent(XhiveTableModelTreeNode.this);
                                                    }
                                                };
            worker.start();
        }
    }

    /* Called to update the view when import or deserialize has added stuff to the library or the
     * database. */
    void newStuffInLibrary(XhiveSessionIf session) {
        refresh(session);
        getTreeModel().nodeStructureChanged(this);
        fireTableDataChanged();
    }
}

package com.xhive.adminclient.treenodes;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.XhiveUpdateListener;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive tree node.
 */
public abstract class XhiveExtendedTreeNode extends XhiveTreeNode {

    private static final String[] DEFAULT_PROPERTY_NAMES = {"Name"};

    private AbstractTableModel propertiesTableModel;
    private String propertyValues[];
    private XhiveUpdateListener serializableContext = null;

    private XhiveAction propertiesAction = new XhiveTransactedAction("Properties",
                                           XhiveResourceFactory.getImageIcon(XhiveResourceFactory.PROPERTIES_ICON), "Change properties", 'p') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   performEdit(getSession());
                                                   update(getSession());
                                               }
                                           };

    private XhiveAction refreshAction = new XhiveTransactedAction("Refresh",
                                        XhiveResourceFactory.getImageIcon(XhiveResourceFactory.REFRESH_ICON),
                                        "Refresh contents", 'r') {
                                            @Override
                                            protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                                removeAllChildren();
                                                update(getSession());
                                                nodeChanged();
                                                expandInBackGround();
                                            }
                                        };


    protected XhiveExtendedTreeNode(XhiveDatabaseTree databaseTree, Object object) {
        super(databaseTree, object);
    }

    public void update(XhiveSessionIf session) {
        // Default no-op
    }


    protected void setPropertyValues(String[] values) {
        propertyValues = values;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public abstract String getName();

    public String[] getPropertyNames() {
        return DEFAULT_PROPERTY_NAMES;
    }

    public String[] getPropertyValues() {
        return (propertyValues != null ? propertyValues : new String[]{getName()});
    }

    @Override
    protected void nodeChanged() {
        super.nodeChanged();
        getPropertiesTableModel().fireTableDataChanged();
        TreeNode parent1 = getParent();
        if (parent1 instanceof XhiveTableModelTreeNode) {
            // If the parent is a table model tree node, then also update the table
            int index = getParent().getIndex(this);
            ((XhiveTableModelTreeNode) parent1).fireTableRowsUpdated(index, index);
        }
    }

    @Override
    public void performDoubleClick() {
        propertiesAction.actionPerformed(null);
        //
        //    XhiveTransactionWrapper wrapper = new XhiveTransactionWrapper() {
        //      protected Object transactedAction() throws Exception {
        //        performEdit(getSession());
        //        update(getSession());
        //        return null;
        //      }
        //    };
        //    wrapper.start();
    }


    @Override
    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        switch (category) {
        case MENU_CATEGORY_PROPERTIES:
            if (hasProperties()) {
                popupMenu.add(new JMenuItem(propertiesAction));
            }
            popupMenu.add(new JMenuItem(refreshAction));
            break;
        }
    }

    protected void performEdit(XhiveSessionIf session) {
        if (editProperties(session)) {
            nodeChanged();
        }
    }

    protected boolean editProperties(XhiveSessionIf session) {
        return false;
    }

    /**
     * Executes the delete on the object.
     */
    @Override
    public void deleteAction(XhiveSessionIf session) {
        // Default no-op
    }


    @Override
    protected void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        // Default no-op
    }


    public String getAsText(XhiveSessionIf session) throws Exception {
        return null;
    }

    public boolean hasContentsPane() {
        return false;
    }

    /**
     * Returns true if the node has serializable content (like documents, versions, and asmodels have)
     */
    public boolean hasSerializableContent() {
        return false;
    }

    public void setSerializableContext(XhiveUpdateListener context) {
        this.serializableContext = context;
    }

    protected XhiveUpdateListener getSerializableContext() {
        return serializableContext;
    }

    public boolean hasProperties() {
        return false;
    }

    public AbstractTableModel getPropertiesTableModel() {
        if (propertiesTableModel == null) {
            propertiesTableModel = new AbstractTableModel() {
                                       @Override
                                       public String getColumnName(int column) {
                                           switch (column) {
                                           case 0:
                                               return "Property name";
                                           case 1:
                                               return "Value";
                                           }
                                           return null;
                                       }

                                       public int getRowCount() {
                                           return getPropertyNames().length;
                                       }

                                       public int getColumnCount() {
                                           return 2;
                                       }

                                       public Object getValueAt(int rowIndex, int columnIndex) {
                                           if (columnIndex == 0) {
                                               return "<HTML><B>"+getPropertyNames()[rowIndex]+"</B></HTML>";
                                           } else if (columnIndex == 1) {
                                               return getPropertyValues()[rowIndex];
                                           }
                                           return null;
                                       }
                                   };
        }
        return propertiesTableModel;
    }

    public void getToolBarActions(String tabName, List<Action> actions) {
        // Default no-op
    }

}

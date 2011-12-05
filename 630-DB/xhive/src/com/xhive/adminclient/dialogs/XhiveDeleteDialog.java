package com.xhive.adminclient.dialogs;

import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.util.Vector;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for deleting the children of a dialog
 */
public class XhiveDeleteDialog extends XhiveTransactedDialog {

    protected JList itemsList;
    private XhiveLibraryIf library;

    public static int showDeleteLibraryChildren(XhiveSessionIf session, XhiveLibraryIf library) {
        XhiveDeleteDialog dialog = new XhiveDeleteDialog("Delete library children", session, library);
        return dialog.execute();
    }

    protected XhiveDeleteDialog(String title, XhiveSessionIf session, XhiveLibraryIf library) {
        super(title, session);
        this.library = library;
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new BorderLayout());
        itemsList = new JList();
        fieldsPanel.setBorder(new BorderUIResource.TitledBorderUIResource("Select children to remove"));
        fieldsPanel.add(new JScrollPane(itemsList), BorderLayout.CENTER);
        // layout hack
        fieldsPanel.add(new JLabel(" "), BorderLayout.WEST);
        fieldsPanel.add(new JLabel(" "), BorderLayout.EAST);
        fieldsPanel.add(new JLabel(" "), BorderLayout.SOUTH);
        return fieldsPanel;
    }

    protected void setFields() {
        Vector items = new Vector();
        XhiveLibraryChildIf item = (XhiveLibraryChildIf) library.getFirstChild();
        while (item != null) {
            items.add(new DeleteItem(item));
            item = (XhiveLibraryChildIf) item.getNextSibling();
        }
        itemsList.setListData(items);
    }

    protected boolean performAction() {
        Object[] selectedItems = itemsList.getSelectedValues();
        for (int i = 0; i < selectedItems.length; i++) {
            DeleteItem selectedItem = (DeleteItem) selectedItems[i];
            library.removeChild(selectedItem.item);
        }
        return true;
    }

    protected boolean fieldsAreValid() {
        return true;
    }

    private static class DeleteItem {
        private XhiveLibraryChildIf item;
        private String toString;

        public DeleteItem(XhiveLibraryChildIf item) {
            this.item = item;
            String result = item.getName();
            if (result == null) {
                result = "id:" + item.getId();
            }
            this.toString = result;
        }

        public String toString() {
            return " " + toString;
        }
    }
}

package com.xhive.adminclient.dialogs;

import com.xhive.dom.interfaces.XhiveCatalogIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.adminclient.layouts.StackLayout;
import com.xhive.adminclient.treenodes.XhiveASModelTreeNode;
import com.xhive.adminclient.treenodes.XhiveCatalogTreeNode;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;

import org.w3c.dom.as.ASModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Vector;

public class XhiveSelectASModelDialog extends XhiveTransactedDialog {

    private JList list;
    private XhiveLibraryIf library;

    private XhiveAction importModelsAction = new XhiveAction("Import AS Models",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.IMPORT_ICON),
            "Import AS Models", 'I') {
                protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                    if (XhiveImportDialog.showImportASModels(getSession(), library) == XhiveDialog.RESULT_OK) {
                        setFields();
                    }
                }
            };

    public XhiveSelectASModelDialog(XhiveSessionIf session, XhiveLibraryIf library, boolean singleSelect) {
        super("Select AS Model", session);
        this.library = library;
        list = new JList();
        list.setSelectionMode(singleSelect ? ListSelectionModel.SINGLE_SELECTION :
                              ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    protected void setFields() {
        Vector data = new Vector();
        for (Iterator i = library.getCatalog().getASModels(false); i.hasNext();) {
            data.add(((ASModel) i.next()).getHint());
        }
        list.setListData(data);
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new BorderLayout());
        fieldsPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        return fieldsPanel;
    }

    protected JPanel buildButtonPanel() {
        JPanel buttonPanel = super.buildButtonPanel();
        buttonPanel.add(new JButton(importModelsAction));
        return buttonPanel;
    }

    public static String showSelectASModelDialog(XhiveSessionIf session, XhiveLibraryIf library) {
        XhiveSelectASModelDialog dialog = new XhiveSelectASModelDialog(session, library, true);
        return (dialog.execute() == XhiveDialog.RESULT_OK ? dialog.getSelectedValue() : null);
    }

    public static Object[] showSelectASModelsDialog(XhiveSessionIf session, XhiveLibraryIf library) {
        XhiveSelectASModelDialog dialog = new XhiveSelectASModelDialog(session, library, false);
        return (dialog.execute() == XhiveDialog.RESULT_OK ? dialog.getSelectedValues() : null);
    }

    public String getSelectedValue() {
        return (String) list.getSelectedValue();
    }

    public Object[] getSelectedValues() {
        return (Object[]) list.getSelectedValues();
    }

    public boolean fieldsAreValid() {
        return (list.getSelectedValue() != null);
    }
}

/*
 * User: arnod
 * Date: Feb 12, 2003
 * Time: 4:46:27 PM
 *
 * X-Hive/TDS (c) X-Hive corporation
 */
package com.xhive.adminclient.dialogs;

import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveType;
import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.XhiveTextSearchPanel;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.treenodes.XhiveNodeTreeNode;
import com.xhive.adminclient.treenodes.XhiveDocumentRootTreeNode;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class XhiveBrowseDocumentDialog extends XhiveTransactedDialog {

    private XhiveDocumentIf document;

    public XhiveBrowseDocumentDialog(XhiveSessionIf session, XhiveDocumentIf document) {
        super(document.getFullPath(), session);
        this.document = document;
    }

    protected JPanel buildFieldsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        XhiveDatabaseTree resultTree = XhiveDatabaseTree.build(getSession(), new XhiveDocumentRootTreeNode(document), null);
        JScrollPane scrollPane = new JScrollPane(resultTree);
        panel.add(XhiveTextSearchPanel.createTextSearchPanel(scrollPane, getSession()), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(600, 500));

        // Dialog does not need the session anymore
        getSession().leave();

        return panel;
    }

    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent we) {
        if (we.getID() == WindowEvent.WINDOW_CLOSING) {
            try {
                performCancel();
            } catch (Exception e) {
                XhiveMessageDialog.showException(e);
            }
        }
        super.processWindowEvent(we);
    }

    protected void performOk() throws Exception {
        getSession().join();
        super.performOk();
    }

    protected void performCancel() {
        getSession().join();
        super.performCancel();
    }
}

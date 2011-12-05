package com.xhive.adminclient.dialogs;

import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.adminclient.layouts.StackLayout;
import com.xhive.adminclient.layouts.FormLayout;

import javax.swing.*;

class XhiveLibraryChildDialog extends XhiveTransactedDialog {

    protected JTextField nameField;
    protected JTextField descriptionField;

    public XhiveLibraryChildDialog(String title, XhiveSessionIf session) {
        super(title, session);
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new FormLayout());
        nameField = new JTextField();
        descriptionField = new JTextField();
        setPreferredWidthOf(nameField, 200);
        fieldsPanel.add(new JLabel("Name:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(new JLabel("Description:"));
        fieldsPanel.add(descriptionField);
        return fieldsPanel;
    }
}

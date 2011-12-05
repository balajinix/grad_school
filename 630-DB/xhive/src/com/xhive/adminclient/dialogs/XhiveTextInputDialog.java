package com.xhive.adminclient.dialogs;

import javax.swing.*;
import java.awt.*;

public class XhiveTextInputDialog extends XhiveDialog {

    private JTextField textField;

    public XhiveTextInputDialog(String title, String text) {
        super(title);
        textField = new JTextField(text);
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new BorderLayout());
        fieldsPanel.add(textField, BorderLayout.CENTER);
        return fieldsPanel;
    }

    public String getTextValue() {
        return textField.getText();
    }
}

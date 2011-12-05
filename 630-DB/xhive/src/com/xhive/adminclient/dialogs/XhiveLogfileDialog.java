package com.xhive.adminclient.dialogs;

import com.xhive.core.interfaces.XhiveFederationIf;
import com.xhive.adminclient.XhiveFederationAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for changing the keep log files option.
 */
public class XhiveLogfileDialog extends XhiveDialog {

    private JPasswordField superUserPasswordField;
    private JCheckBox keepLogFilesBox;
    private JLabel keepLabel;
    private JButton enableButton;

    public static void showKeepLogFileOption() {
        XhiveLogfileDialog dialog = new XhiveLogfileDialog();
        dialog.execute();
    }

    protected XhiveLogfileDialog() {
        super("Set keep-log-files option");
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        setGridPanel(fieldsPanel);

        superUserPasswordField = new JPasswordField();
        setPreferredWidthOf(superUserPasswordField, 200);
        keepLogFilesBox = new JCheckBox("Keep log files");

        enableButton = new JButton("Check password");
        enableButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               checkPasswordAction();
                                           }
                                       }
                                      );
        superUserPasswordField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        checkPasswordAction();
                    }
                }
                                                );

        addToGrid(true, new JLabel("Enter superuser password:"));
        addToGrid(false, superUserPasswordField);
        addToGrid(false, enableButton);

        keepLabel = new JLabel("keepLogFiles option:");
        addToGrid(true, keepLabel);
        addToGrid(false, keepLogFilesBox);

        return fieldsPanel;
    }

    protected void setFields() {
        super.setFields();
        enableDialog(false);
    }

    private void enableDialog(boolean enable) {
        keepLogFilesBox.setEnabled(enable);
        keepLabel.setEnabled(enable);
        enableButton.setEnabled(!enable);
        getOkButton().setEnabled(enable);
    }

    protected boolean performAction() throws Exception {
        new XhiveFederationAction(new String(superUserPasswordField.getPassword())) {
            public void performAction(XhiveFederationIf federation) throws Exception {
                federation.setKeepLogFiles(keepLogFilesBox.isSelected());
            }
        };
        return true;
    }

    private void checkPasswordAction() {
        try {
            new XhiveFederationAction(new String(superUserPasswordField.getPassword())) {
                public void performAction(XhiveFederationIf federation) throws Exception {
                    // Important part of this action is that it checks whether a connection could be made
                    keepLogFilesBox.setSelected(federation.getKeepLogFiles());
                }
            };
            enableDialog(true);
        } catch (Exception e) {
            XhiveMessageDialog.showException(this, e);
        }
    }
}


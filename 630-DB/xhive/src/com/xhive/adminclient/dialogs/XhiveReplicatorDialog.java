package com.xhive.adminclient.dialogs;

import javax.swing.*;

import com.xhive.adminclient.XhiveFederationAction;
import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.core.interfaces.XhiveFederationIf;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Iterator;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Xhive properties dialog.
 *
 */
public class XhiveReplicatorDialog extends XhiveDialog {

    private final boolean unregister;
    private JPasswordField superuserPasswordField;
    private JLabel replicatorIdLabel;
    private JComponent replicatorIdField;
    private JButton enableButton;

    public static void showRegister() {
        XhiveReplicatorDialog dialog = new XhiveReplicatorDialog(false);
        dialog.execute();
    }

    public static void showUnregister() {
        XhiveReplicatorDialog dialog = new XhiveReplicatorDialog(true);
        dialog.execute();
    }

    private XhiveReplicatorDialog(boolean unregister) {
        super(unregister ? "Unregister replicator" : "Register replicator");
        this.unregister = unregister;
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new FormLayout());
        setGridPanel(fieldsPanel);

        superuserPasswordField = new JPasswordField("");
        setPreferredWidthOf(superuserPasswordField, 200);
        if (unregister) {
            replicatorIdField = new JComboBox();
            replicatorIdField.setEnabled(false);
        } else {
            replicatorIdField = new JTextField();
        }
        setPreferredWidthOf(replicatorIdField, 200);

        enableButton = new JButton("Check password");
        replicatorIdLabel = new JLabel("Replicator id:");

        if (unregister) {
            enableButton.addActionListener(new ActionListener() {
                                               public void actionPerformed(ActionEvent e) {
                                                   checkPasswordAction();
                                               }
                                           }
                                          );
            superuserPasswordField.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            checkPasswordAction();
                        }
                    }
                                                    );
        }

        addToGrid(true, new JLabel("Superuser password:"));
        addToGrid(false, superuserPasswordField);
        if (unregister) {
            addToGrid(false, enableButton);
        }
        addToGrid(true, replicatorIdLabel);
        addToGrid(false, replicatorIdField);

        return fieldsPanel;
    }

    /**
     * Only used in unregister mode
     */
    private void checkPasswordAction() {
        try {
            new XhiveFederationAction(new String(superuserPasswordField.getPassword())) {
                public void performAction(XhiveFederationIf federation) throws Exception {
                    // Important part of this action is that it checks whether a connection could be made
                    JComboBox replicatorIds = (JComboBox) replicatorIdField;
                    replicatorIds.removeAllItems();
                    Iterator replicators = federation.getReplicators();
                    while (replicators.hasNext()) {
                        String replicatorId = (String) replicators.next();
                        replicatorIds.addItem(replicatorId);
                    }
                }
            };
            enableDialog(true);
        } catch (Exception e) {
            XhiveMessageDialog.showException(this, e);
        }
    }

    protected final boolean performAction() throws Exception {
        new XhiveFederationAction(new String(superuserPasswordField.getPassword())) {
            public void performAction(XhiveFederationIf federation) throws Exception {
                String id = getReplicatorIdField();
                if (unregister) {
                    federation.unregisterReplicator(id);
                } else {
                    federation.registerReplicator(id);
                }
            }
        };
        return true;
    }

    protected boolean fieldsAreValid() {
        return checkFieldLength(superuserPasswordField, 3, 8, "Expected a valid superuser password") &&
               checkField(replicatorIdField, !getReplicatorIdField().equals(""), "Replicator id is required");
    }

    protected void setFields() {
        super.setFields();
        enableDialog(!unregister);
    }

    private void enableDialog(boolean enable) {
        replicatorIdField.setEnabled(enable);
        replicatorIdLabel.setEnabled(enable);
        enableButton.setEnabled(!enable);
        getOkButton().setEnabled(enable);
    }

    private String getReplicatorIdField() {
        String replicatorId;
        if (replicatorIdField instanceof JTextField) {
            replicatorId = ((JTextField) replicatorIdField).getText();
        } else {
            replicatorId = (String) ((JComboBox) replicatorIdField).getSelectedItem();
            if (replicatorId == null) {
                replicatorId = "";
            }
        }
        return replicatorId.trim();
    }
}

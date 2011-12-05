package com.xhive.adminclient.dialogs;

import com.xhive.XhiveDriverFactory;
import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.XhiveFederationAction;
import com.xhive.core.interfaces.XhiveFederationIf;
import com.xhive.core.interfaces.XhiveSessionIf;

import javax.swing.*;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Xhive properties dialog.
 *
 */
public abstract class XhivePropertiesDialog extends XhiveDialog {

    private JPasswordField superuserPasswordField;

    public static void showChangeSuperuserPassword() {
        XhivePropertiesDialog dialog = new ChangeSuperuserPasswordDialog();
        dialog.execute();
    }

    public static void showChangeLicenseKey() {
        XhivePropertiesDialog dialog = new ChangeLicenseKeyDialog();
        dialog.execute();
    }

    XhivePropertiesDialog(String title) {
        super(title);
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new FormLayout());
        superuserPasswordField = new JPasswordField("");
        setPreferredWidthOf(superuserPasswordField, 200);
        fieldsPanel.add(new JLabel("Superuser password:"));
        fieldsPanel.add(superuserPasswordField);
        return fieldsPanel;
    }

    protected final boolean performAction() throws Exception {
        new XhiveFederationAction(new String(superuserPasswordField.getPassword())) {
            public void performAction(XhiveFederationIf federation) throws Exception {
                performActionImpl(federation);
            }
        };
        return true;
    }

    protected boolean fieldsAreValid() {
        return checkFieldLength(superuserPasswordField, 3, 8, "Expected a valid superuser password");
    }

    protected abstract void performActionImpl(XhiveFederationIf federation);
}

class ChangeLicenseKeyDialog extends XhivePropertiesDialog {

    private JTextField licenseKeyField;

    public ChangeLicenseKeyDialog() {
        super("Change license key");
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = super.buildFieldsPanel();
        licenseKeyField = new JTextField();
        fieldsPanel.add(new JLabel("New license key:"));
        fieldsPanel.add(licenseKeyField);
        return fieldsPanel;
    }

    protected boolean fieldsAreValid() {
        if (super.fieldsAreValid()) {
            return checkFieldNotEmpty(licenseKeyField, "New value should be filled in!");
        }
        return false;
    }

    protected void performActionImpl(XhiveFederationIf federation) {
        federation.setLicenseKey(licenseKeyField.getText());
    }
}

class ChangeSuperuserPasswordDialog extends XhivePropertiesDialog {

    private JPasswordField newPasswordField;
    private JPasswordField retypedPasswordField;

    public ChangeSuperuserPasswordDialog() {
        super("Change superuser password");
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = super.buildFieldsPanel();
        newPasswordField = new JPasswordField();
        retypedPasswordField = new JPasswordField("");
        fieldsPanel.add(new JLabel("New superuser password:"));
        fieldsPanel.add(newPasswordField);
        fieldsPanel.add(new JLabel("Retype password:"));
        fieldsPanel.add(retypedPasswordField);
        return fieldsPanel;
    }

    protected boolean fieldsAreValid() {
        if (super.fieldsAreValid()) {
            return checkFieldEquals(retypedPasswordField, new String(newPasswordField.getPassword()), "Password and retyped password should be identical") &&
                   checkFieldLength(newPasswordField, 3, 8, "New password should be at least 3 and at most 8 characters long!");
        }
        return false;
    }

    protected void performActionImpl(XhiveFederationIf federation) {
        federation.setSuperUserPassword(new String(newPasswordField.getPassword()));
    }
}

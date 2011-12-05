package com.xhive.adminclient.dialogs;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.xhive.adminclient.XhiveFederationAction;
import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.core.interfaces.XhiveFederationIf;
import java.util.ArrayList;
import java.util.StringTokenizer;



public class XhiveShutdownFederationDialog extends XhiveDialog {

    private JPasswordField superuserPasswordField;
    private JTextField replicatorIdsField;

    public static void showShutdown() {
        XhiveShutdownFederationDialog dialog = new XhiveShutdownFederationDialog();
        dialog.execute();
    }

    private XhiveShutdownFederationDialog() {
        super("Shutdown federation");
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new FormLayout());
        superuserPasswordField = new JPasswordField();
        setPreferredWidthOf(superuserPasswordField, 200);
        replicatorIdsField = new JTextField();
        fieldsPanel.add(new JLabel("Superuser password:"));
        fieldsPanel.add(superuserPasswordField);
        fieldsPanel.add(new JLabel("Replicator ids:"));
        fieldsPanel.add(replicatorIdsField);
        return fieldsPanel;
    }

    protected final boolean performAction() throws Exception {
        new XhiveFederationAction(new String(superuserPasswordField.getPassword())) {
            public void performAction(XhiveFederationIf federation) throws Exception {
                String text = replicatorIdsField.getText().trim();
                StringTokenizer tokenizer = new StringTokenizer(text, ",");
                ArrayList idlist = new ArrayList();
                while (tokenizer.hasMoreTokens()) {
                    idlist.add(tokenizer.nextToken());
                }
                String[] replicatorIds = (String[])idlist.toArray(new String[idlist.size()]);
                /* Cannot shutdown with open transaction. */
                getSession().commit();
                federation.shutdown(replicatorIds);
            }
        };
        return true;
    }

    protected boolean fieldsAreValid() {
        return checkFieldLength(superuserPasswordField, 3, 8, "Expected a valid superuser password");
    }
}

package com.xhive.adminclient.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.xhive.adminclient.XhiveFederationAction;
import com.xhive.core.interfaces.XhiveFederationFactoryIf;
import com.xhive.core.interfaces.XhiveFederationIf;

public class XhiveReplicateFederationDialog extends XhiveDialog {

    private JTextField federationTargetLocationField;
    private JPasswordField superuserPasswordField;
    private JCheckBox relativeDirOption;

    public static void showReplicateFederation() {
        XhiveReplicateFederationDialog dialog = new XhiveReplicateFederationDialog();
        dialog.execute();
    }

    private XhiveReplicateFederationDialog() {
        super("Replicate federation");
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        setGridPanel(fieldsPanel);

        federationTargetLocationField = new JTextField();
        setPreferredWidthOf(federationTargetLocationField, 350);
        superuserPasswordField = new JPasswordField();
        setPreferredWidthOf(superuserPasswordField, 200);
        relativeDirOption = new JCheckBox("Do not use original paths");

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               browseFile(federationTargetLocationField);
                                           }
                                       }
                                      );

        addToGrid(true, new JLabel("Federation target location:"));
        addToGrid(false, federationTargetLocationField);
        addToGrid(false, browseButton);

        addToGrid(true, new JLabel("Superuser password:"));
        addToGrid(false, superuserPasswordField);

        addToGrid(true, relativeDirOption);

        return fieldsPanel;
    }

    @Override
    protected final boolean performAction() throws Exception {
        new XhiveFederationAction(new String(superuserPasswordField.getPassword())) {
            @Override
            public void performAction(XhiveFederationIf federation) throws Exception {
                String bootstrap = federationTargetLocationField.getText();
                XhiveFederationFactoryIf.PathMapper mapper;
                if (relativeDirOption.isSelected()) {
                    mapper = new RelativeDirMapper();
                } else {
                    mapper = null;
                }
                federation.replicateFully(bootstrap, mapper);
            }
        };
        return true;
    }

    @Override
    protected boolean fieldsAreValid() {
        return checkFieldLength(superuserPasswordField, 3, 8, "Expected a valid superuser password") &&
               checkFieldNotEmpty(federationTargetLocationField, "Target location is required");
    }

    private void browseFile(JTextField field) {
        JFileChooser fileChooser = new JFileChooser(field.getText());
        fileChooser.setMultiSelectionEnabled(false);
        if (fileChooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
            field.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
}

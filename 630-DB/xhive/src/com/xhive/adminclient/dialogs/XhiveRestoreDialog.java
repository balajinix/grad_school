package com.xhive.adminclient.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.xhive.XhiveDriverFactory;
import com.xhive.adminclient.AdminProperties;
import com.xhive.core.interfaces.XhiveFederationFactoryIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for restoring.
 */
public class XhiveRestoreDialog extends XhiveDialog {

    private JTextField federationTargetLocationField;
    private JTextField importFileNameField;
    private JCheckBox relativeDirOption;

    public static void showRestore() {
        XhiveRestoreDialog dialog = new XhiveRestoreDialog();
        dialog.execute();
    }

    protected XhiveRestoreDialog() {
        super("Restore");
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        setGridPanel(fieldsPanel);

        federationTargetLocationField = new JTextField();
        setPreferredWidthOf(federationTargetLocationField, 350);
        importFileNameField = new JTextField();
        setPreferredWidthOf(importFileNameField, 350);
        importFileNameField.setText(AdminProperties.getProperty(AdminProperties.BACKUP_PATH));
        relativeDirOption = new JCheckBox("Do not use original paths");

        JButton browseButton1 = new JButton("Browse");
        browseButton1.addActionListener(new ActionListener() {
                                            public void actionPerformed(ActionEvent e) {
                                                browseFile(federationTargetLocationField);
                                            }
                                        }
                                       );
        JButton browseButton2 = new JButton("Browse");
        browseButton2.addActionListener(new ActionListener() {
                                            public void actionPerformed(ActionEvent e) {
                                                browseFile(importFileNameField);
                                            }
                                        }
                                       );

        addToGrid(true, new JLabel("Federation target location:"));
        addToGrid(false, federationTargetLocationField);
        addToGrid(false, browseButton1);

        addToGrid(true, new JLabel("File name of backup:"));
        addToGrid(false, importFileNameField);
        addToGrid(false, browseButton2);

        addToGrid(true, relativeDirOption);

        return fieldsPanel;
    }

    @Override
    protected boolean fieldsAreValid() {
        return checkFileField(federationTargetLocationField, "federation target")
               && checkFileField(importFileNameField, "backup file");
    }

    private boolean checkFileField(JTextField field, String fileName) {
        File outputFile = new File(field.getText().trim());
        File parentDir = outputFile.getParentFile();
        return checkField(field, parentDir != null, "Invalid directory for " + fileName) &&
               checkField(field, parentDir.isDirectory(), "Directory " +
                          parentDir.getAbsolutePath() + " does not exist") &&
               checkField(field, !outputFile.isDirectory(), fileName + " " +
                          outputFile.getAbsolutePath() + " must be a file, not a directory");
    }

    @Override
    protected boolean performAction() throws Exception {
        XhiveFederationFactoryIf federationFactory = XhiveDriverFactory.getFederationFactory();
        String filename = importFileNameField.getText().trim();
        AdminProperties.setProperty(AdminProperties.BACKUP_PATH, filename);
        FileInputStream inputStream = new FileInputStream(filename);

        String bootStrapFileName = federationTargetLocationField.getText().trim();

        XhiveFederationFactoryIf.PathMapper mapper = null;
        if (relativeDirOption.isSelected()) {
            mapper = new RelativeDirMapper();
        }
        federationFactory.restoreFederation(inputStream.getChannel(), bootStrapFileName, mapper);
        inputStream.close();
        XhiveBootstrapDialog.addPathToAdminProperties(bootStrapFileName);
        return true;
    }

    private void browseFile(JTextField field) {
        JFileChooser fileChooser = new JFileChooser(field.getText());
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (fileChooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
            field.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

}


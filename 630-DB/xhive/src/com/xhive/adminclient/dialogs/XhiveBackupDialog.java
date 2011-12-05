package com.xhive.adminclient.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.XhiveFederationAction;
import com.xhive.core.interfaces.XhiveFederationIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for backing up.
 */
public class XhiveBackupDialog extends XhiveDialog {

    private JPasswordField superUserPasswordField;
    private JTextField exportFileNameField;
    private JCheckBox standaloneOption;
    private JCheckBox incrementalOption;
    private JCheckBox keepLogFilesOption;

    public static void showBackup() {
        XhiveBackupDialog dialog = new XhiveBackupDialog();
        dialog.execute();
    }

    protected XhiveBackupDialog() {
        super("Backup");
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        setGridPanel(fieldsPanel);

        superUserPasswordField = new JPasswordField();
        setPreferredWidthOf(superUserPasswordField, 200);
        exportFileNameField = new JTextField();
        setPreferredWidthOf(exportFileNameField, 200);
        exportFileNameField.setText(AdminProperties.getProperty(AdminProperties.BACKUP_PATH));
        standaloneOption = new JCheckBox("Standalone");
        incrementalOption = new JCheckBox("Incremental");
        keepLogFilesOption = new JCheckBox("Keep log files");

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               browseFile();
                                           }
                                       }
                                      );
        incrementalOption.addActionListener(new ActionListener() {
                                                public void actionPerformed(ActionEvent e) {
                                                    if (incrementalOption.isSelected()) {
                                                        standaloneOption.setSelected(false);
                                                    }
                                                }
                                            }
                                           );
        standaloneOption.addActionListener(new ActionListener() {
                                               public void actionPerformed(ActionEvent e) {
                                                   if (standaloneOption.isSelected()) {
                                                       incrementalOption.setSelected(false);
                                                   }
                                               }
                                           }
                                          );

        addToGrid(true, new JLabel("Superuser password:"));
        addToGrid(false, superUserPasswordField);

        addToGrid(true, new JLabel("Output filename:"));
        addToGrid(false, exportFileNameField);

        addToGrid(false, browseButton);
        addToGrid(true, standaloneOption);
        addToGrid(true, incrementalOption);
        addToGrid(true, keepLogFilesOption);

        return fieldsPanel;
    }

    @Override
    protected boolean fieldsAreValid() {
        File outputFile = new File(exportFileNameField.getText().trim());
        File parentDir = outputFile.getParentFile();

        return checkField(exportFileNameField, parentDir != null, "Invalid directory for backup output") &&
               checkField(exportFileNameField, parentDir.isDirectory(), "Directory " +
                          parentDir.getAbsolutePath() + " does not exist") &&
               checkField(exportFileNameField, !outputFile.isDirectory(), "Output " +
                          outputFile.getAbsolutePath() + " must be a file, not a directory");
    }

    @Override
    protected boolean performAction() throws Exception {
        new XhiveFederationAction(new String(superUserPasswordField.getPassword())) {
            @Override
            public void performAction(XhiveFederationIf federation) throws Exception {
                String filename = exportFileNameField.getText().trim();
                AdminProperties.setProperty(AdminProperties.BACKUP_PATH, filename);
                FileOutputStream output = new FileOutputStream(filename);
                int options = 0;
                if (incrementalOption.isSelected()) {
                    options |= XhiveFederationIf.BACKUP_INCREMENTAL;
                }
                if (keepLogFilesOption.isSelected()) {
                    options |= XhiveFederationIf.BACKUP_KEEP_LOG_FILES;
                }
                if (standaloneOption.isSelected()) {
                    options |= XhiveFederationIf.BACKUP_STANDALONE;
                }
                federation.backup(output.getChannel(), options);
                output.close();
            }
        };
        return true;
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser(AdminProperties
                                   .getFile(AdminProperties.BACKUP_PATH).getParent());
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (fileChooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
            exportFileNameField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

}


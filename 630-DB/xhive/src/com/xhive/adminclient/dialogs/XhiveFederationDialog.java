package com.xhive.adminclient.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhiveFederationFactoryIf;
import com.xhive.core.interfaces.XhiveSessionIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for creating a new federation.
 */
public class XhiveFederationDialog extends XhiveDialog {

    private static final int MIN_PAGE_SIZE = 512;
    private static final int MAX_PAGE_SIZE = 1024 * 16;

    private JTextField bootstrapPathField;
    private JTextField logDirField;
    private JTextField pageSizeField;
    private JTextField licenseKeyField;
    private JPasswordField superuserPasswordField;
    private JPasswordField retypedPasswordField;

    public static void showCreateFederation() {
        XhiveFederationDialog dialog = new XhiveFederationDialog();
        dialog.execute();
    }

    protected XhiveFederationDialog() {
        super("Create federation");
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        setGridPanel(fieldsPanel);

        addToGrid(false, new JLabel("Note: You can create multiple databases in one federation,"), 3);
        addToGrid(true, new JLabel("it is usually not necessary to create multiple federations"), 3);

        bootstrapPathField = new JTextField();
        setPreferredWidthOf(bootstrapPathField, 200);
        addToGrid(true, new JLabel("Bootstrap path:"));
        addToGrid(false, bootstrapPathField);
        addToGrid(false, createBrowseButton(bootstrapPathField));

        logDirField = new JTextField();
        logDirField.setText("log");
        setPreferredWidthOf(logDirField, 200);
        addToGrid(true, new JLabel("Log directory:"));
        addToGrid(false, logDirField);
        addToGrid(false, createBrowseButton(logDirField));

        addToGrid(true, new JLabel("(Note: if relative path, log directory is relative to bootstrap path)"), 3);

        pageSizeField = new JTextField();
        pageSizeField.setText("8192");
        setPreferredWidthOf(pageSizeField, 200);
        addToGrid(true, new JLabel("Page size:"));
        addToGrid(false, pageSizeField);

        licenseKeyField = new JTextField();
        setPreferredWidthOf(licenseKeyField, 200);
        addToGrid(true, new JLabel("License key (optional):"));
        addToGrid(false, licenseKeyField);

        superuserPasswordField = new JPasswordField();
        setPreferredWidthOf(superuserPasswordField, 200);
        retypedPasswordField = new JPasswordField();
        setPreferredWidthOf(retypedPasswordField, 200);
        addToGrid(true, new JLabel("New superuser password:"));
        addToGrid(false, superuserPasswordField);
        addToGrid(true, new JLabel("Retype password:"));
        addToGrid(false, retypedPasswordField);
        return fieldsPanel;
    }

    private JComponent createBrowseButton(final JTextField textField) {
        final JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               chooseConfigDir(textField);
                                           }
                                       }
                                      );
        return browseButton;
    }

    @Override
    protected boolean fieldsAreValid() {
        boolean result = checkFieldNotEmpty(bootstrapPathField, "Bootstrap directory should be filled in")
                         && checkFieldNotEmpty(logDirField, "Log directory should be filled in")
                         && checkFieldIsLong(pageSizeField, false, "Page size should be a number")
                         && checkFieldLength(superuserPasswordField, 3, 8, "Password should be at least 3 and at most 8 characters long")
                         && checkFieldEquals(retypedPasswordField, new String(superuserPasswordField.getPassword()), "Password and retyped password should be identical");
        if (result) {
            int pageSize = Integer.valueOf(pageSizeField.getText()).intValue();
            result = checkField(pageSizeField, pageSize >= MIN_PAGE_SIZE && pageSize <= MAX_PAGE_SIZE, "Invalid page size amount");
        }
        return result;
    }

    private void chooseConfigDir(JTextField textField) {
        EFileChooser fileChooser = new EFileChooser("Choose a directory");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(EFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showDialog(this, "ok") == EFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String dir = file.getAbsolutePath();
            if (textField == bootstrapPathField) {
                textField.setText(dir + File.separator + "XhiveDatabase.bootstrap");
            } else {
                textField.setText(dir + File.separator + "log");
            }
        }
    }

    @Override
    protected boolean performAction() throws Exception {
        String bsFile = bootstrapPathField.getText().trim();
        String logDirectory = logDirField.getText().trim();
        int pagesize = Integer.valueOf(pageSizeField.getText().trim()).intValue();
        String passwd = new String(superuserPasswordField.getPassword());
        try {
            XhiveFederationFactoryIf ff = XhiveDriverFactory.getFederationFactory();
            ff.createFederation(bsFile, logDirectory, pagesize, passwd);

            String licenseKey = licenseKeyField.getText().trim();
            if (!licenseKey.equals("")) {
                // License key was filled in, so as long as we are here, set it on the federation
                XhiveDriverIf driver = XhiveDriverFactory.getDriver(bsFile);
                driver.init(200);
                XhiveSessionIf tempSession = driver.createSession();
                tempSession.connect("superuser", passwd, null);
                try {
                    tempSession.begin();
                    tempSession.getFederation().setLicenseKey(licenseKey);
                    tempSession.commit();
                    tempSession.disconnect();
                    tempSession.terminate();
                }
                finally {
                    if (tempSession.isOpen()) {
                        tempSession.rollback();
                    }
                    if (tempSession.isConnected()) {
                        tempSession.disconnect();
                    }
                    if (!tempSession.isTerminated()) {
                        tempSession.terminate();
                    }
                    // The driver.close is acceptable here because we just created this federation
                    driver.close();
                }
            }
        } catch (Exception e) {
            XhiveMessageDialog.showException(this, e);
        }
        XhiveBootstrapDialog.addPathToAdminProperties(bsFile);
        return true;
    }

}


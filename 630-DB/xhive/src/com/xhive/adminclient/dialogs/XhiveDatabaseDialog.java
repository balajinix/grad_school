package com.xhive.adminclient.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.DriverRegistry;
import com.xhive.adminclient.XhiveCancellation;
import com.xhive.adminclient.dialogs.clustering.XhiveFileDialog;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhiveFederationIf;
import com.xhive.core.interfaces.XhiveSessionIf;

import org.xml.sax.SAXException;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for creating, removing and connecting to a database.
 */
public abstract class XhiveDatabaseDialog extends XhiveDialog {

    public static void showDeleteDatabase(String currentDatabaseName) {
        XhiveDatabaseDialog dialog = new DeleteDatabaseDialog(currentDatabaseName);
        dialog.execute();
    }

    public static XhiveConnectionInfo showConnectDatabase() {
        ConnectDatabaseDialog dialog = new ConnectDatabaseDialog();
        return (dialog.execute() == RESULT_OK ? dialog.getCreatedConnectionInfo() : null);
    }

    public static void showCreateDatabase() {
        CreateDatabaseDialog dialog = new CreateDatabaseDialog();
        dialog.execute();
    }

    protected XhiveDatabaseDialog(String title) {
        super(title);
    }

    /**
     * @returns true if JAAS is used for authentication, false otherwise (for the normal case)
     * This return-value is passed back here because we have a driver-instance available here.
     */
    protected boolean fillinDatabaseNames(JComboBox box) {
        boolean usesJAAS = false;
        box.removeAllItems();
        XhiveDriverIf driver = AdminMainFrame.getDriver();
        DriverRegistry.registerDriverUser(driver, this);
        try {
            usesJAAS = driver.getSecurityConfig().usesJavaAuthentication();
            Iterator<String> dbNames = driver.getDatabaseNames();
            if (dbNames.hasNext()) {
                while (dbNames.hasNext()) {
                    box.addItem(dbNames.next());
                }
                getOkButton().setEnabled(true);
            } else {
                box.addItem("-Create a database first-");
                getOkButton().setEnabled(false);
            }
        }
        finally {
            DriverRegistry.unregisterDriverUser(driver, this);
        }
        return usesJAAS;
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        setGridPanel(fieldsPanel);

        JComponent databaseField = getDatabaseField();
        setPreferredWidthOf(databaseField, 200);
        addToGrid(false, new JLabel("Database name:"));
        addToGrid(false, databaseField);

        return fieldsPanel;
    }

    /**
     * Get the database field, creates it on first call
     */
    protected abstract JComponent getDatabaseField();

    protected abstract String getDatabaseFieldValue();

    @Override
    protected boolean fieldsAreValid() {
        return checkField(getDatabaseField(), !getDatabaseFieldValue().trim().equals(""),
                          "Database name should be filled in!");
    }
}

abstract class SuperuserDatabaseDialog extends XhiveDatabaseDialog {

    JPasswordField superuserPasswordField;

    public SuperuserDatabaseDialog(String title) {
        super(title);
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = super.buildFieldsPanel();
        superuserPasswordField = new JPasswordField();
        setPreferredWidthOf(superuserPasswordField, 200);
        addToGrid(true, new JLabel("Superuser password:"));
        addToGrid(false, superuserPasswordField);
        return fieldsPanel;
    }

    @Override
    protected boolean fieldsAreValid() {
        if (super.fieldsAreValid()) {
            return checkFieldNotEmpty(superuserPasswordField, "Superuser password should be filled in!");
        }
        return false;
    }
}

class DeleteDatabaseDialog extends SuperuserDatabaseDialog {

    private String currentDatabaseName;
    private JComboBox databaseField;

    public DeleteDatabaseDialog(String currentDatabaseName) {
        super("Delete database");
        this.currentDatabaseName = currentDatabaseName;
    }

    @Override
    protected boolean performAction() throws Exception {
        XhiveSessionIf session = AdminMainFrame.connectAsSuperuser(new String(superuserPasswordField.getPassword()));
        session.begin();
        try {
            XhiveFederationIf federation = session.getFederation();
            federation.deleteDatabase(getDatabaseFieldValue());
            session.commit();
        }
        finally {
            AdminMainFrame.disconnectFromSuperUser(session);
        }
        return true;
    }

    @Override
    protected JComponent getDatabaseField() {
        if (databaseField == null) {
            databaseField = new JComboBox();
        }
        return databaseField;
    }

    @Override
    protected String getDatabaseFieldValue() {
        return databaseField.getSelectedItem().toString();
    }

    @Override
    public void setFields() {
        fillinDatabaseNames(databaseField);
        databaseField.setSelectedItem(AdminProperties.getProperty("com.xhive.adminclient.database"));
    }

    @Override
    protected boolean fieldsAreValid() {
        if (super.fieldsAreValid()) {
            return checkField(databaseField,
                              (currentDatabaseName == null) || (!currentDatabaseName.equals(getDatabaseFieldValue())),
                              "Cannot delete the database that you are currently connected to!") &&
                   showConfirmation(this, "Are you sure you want to delete this database?");
        }
        return false;
    }
}

class CreateDatabaseDialog extends SuperuserDatabaseDialog {

    private JTextField databaseNameField;
    private JTextField pathField;
    private JTextField maxSizeField;
    private JTextField configFileField;
    private JPasswordField administratorPasswordField;
    private JPasswordField retypedPasswordField;
    private JRadioButton defaultConfigButton;
    private JRadioButton customConfigButton;


    public CreateDatabaseDialog() {
        super("Create database in federation " + AdminMainFrame.getCurrentBootstrapPath());
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = super.buildFieldsPanel();
        administratorPasswordField = new JPasswordField();
        setPreferredWidthOf(administratorPasswordField, 200);
        retypedPasswordField = new JPasswordField();
        setPreferredWidthOf(retypedPasswordField, 200);
        pathField = new JTextField();
        maxSizeField = new JTextField();
        setPreferredWidthOf(pathField, 200);
        setPreferredWidthOf(maxSizeField, 200);

        setPreferredWidthOf(getDatabaseField(), 200);

        addToGrid(true, new JLabel("Administrator password:"));
        addToGrid(false, administratorPasswordField);
        addToGrid(true, new JLabel("Retype password:"));
        addToGrid(false, retypedPasswordField);

        addToGrid(true, new JLabel("Path (optional):"));
        addToGrid(false, pathField);
        addToGrid(true, new JLabel("Max size of initial file (optional):"));
        addToGrid(false, maxSizeField);

        JPanel extraPanel = new JPanel(new BorderLayout());
        JPanel configPanel = new JPanel(new BorderLayout());
        defaultConfigButton = new JRadioButton("Default configuration");
        customConfigButton = new JRadioButton("Custom configuration");
        defaultConfigButton.setSelected(true);

        // Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(defaultConfigButton);
        group.add(customConfigButton);

        JPanel panel1 = new JPanel();
        panel1.add(defaultConfigButton);
        panel1.add(customConfigButton);

        configFileField = new JTextField();
        configFileField.setEnabled(false);
        final JButton browseButton = new JButton("Browse");
        browseButton.setMnemonic('B');
        browseButton.setEnabled(false);

        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(new JLabel("Config file:"), BorderLayout.WEST);
        panel2.add(configFileField, BorderLayout.CENTER);
        panel2.add(browseButton, BorderLayout.EAST);
        configPanel.add(panel1, BorderLayout.NORTH);
        configPanel.add(panel2, BorderLayout.SOUTH);

        extraPanel.add(fieldsPanel, BorderLayout.NORTH);
        extraPanel.add(configPanel, BorderLayout.SOUTH);

        ActionListener radioListener = new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               if (e.getSource() == customConfigButton && ((JRadioButton) e.getSource()).isSelected()) {
                                                   configFileField.setEnabled(true);
                                                   browseButton.setEnabled(true);
                                               } else {
                                                   configFileField.setEnabled(false);
                                                   browseButton.setEnabled(false);
                                               }
                                           }
                                       };
        defaultConfigButton.addActionListener(radioListener);
        customConfigButton.addActionListener(radioListener);

        browseButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               chooseConfigFile();
                                           }
                                       }
                                      );

        JPanel newFieldsPanel = new JPanel(new BorderLayout());
        newFieldsPanel.add(fieldsPanel, BorderLayout.CENTER);
        newFieldsPanel.add(extraPanel, BorderLayout.SOUTH);
        return newFieldsPanel;
    }

    @Override
    protected JComponent getDatabaseField() {
        if (databaseNameField == null) {
            databaseNameField = new JTextField();
        }
        return databaseNameField;
    }

    @Override
    protected String getDatabaseFieldValue() {
        return databaseNameField.getText();
    }

    private void chooseConfigFile() {
        EFileChooser fileChooser = new EFileChooser("Choose a configuration file");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(EFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(XhiveFilters.getXMLFileFilter());
        if (fileChooser.showDialog(this, "ok") == EFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            configFileField.setText(file.getAbsolutePath());
        }
    }

    @Override
    protected boolean performAction() throws Exception {
        XhiveSessionIf session = AdminMainFrame.connectAsSuperuser(new String(superuserPasswordField.getPassword()));
        session.begin();
        String configFileName = null;
        try {
            XhiveFederationIf federation = session.getFederation();
            if (!defaultConfigButton.isSelected()) {
                configFileName = configFileField.getText();
                federation.createDatabase(getDatabaseFieldValue(), new String(administratorPasswordField.getPassword()), configFileName, null);
            } else {
                String creationPath = null;
                if (!pathField.getText().trim().equals("")) {
                    creationPath = pathField.getText().trim();
                }
                long maxSize = XhiveFileDialog.createSizeFromString(maxSizeField.getText());
                federation.createDatabase(getDatabaseFieldValue(), new String(administratorPasswordField.getPassword()), creationPath, maxSize);
            }
            session.commit();
        } catch (SAXException se) {
            XhiveMessageDialog.showParseException(this, se, configFileName);
        } finally {
            AdminMainFrame.disconnectFromSuperUser(session);
            AdminProperties.setProperty("com.xhive.adminclient.database", getDatabaseFieldValue());
        }
        return true;
    }

    @Override
    protected boolean fieldsAreValid() {
        if (super.fieldsAreValid()) {
            return (checkFieldLength(administratorPasswordField, 3, 8, "Password should be at least 3 and at most 8 characters long") &&
                    checkFieldEquals(retypedPasswordField, new String(administratorPasswordField.getPassword()), "Password and retyped password should be identical")) &&
                   checkMaxSizeField(maxSizeField);
        }
        return false;
    }

    private boolean checkMaxSizeField(JTextField textField) {
        long size = XhiveFileDialog.createSizeFromString(textField.getText());
        if (size >= 0) {
            return true;
        } else {
            return checkField(textField, false, "Maximum size (if specified) should be a positive long");
        }
    }

}

class ConnectDatabaseDialog extends XhiveDatabaseDialog {

    private XhiveConnectionInfo createdConnectionInfo;

    private JComboBox databaseField;
    private JTextField userNameField;
    private JPasswordField passwordField;
    private JTextField cachePagesField;
    private JLabel bootStrapPath;
    private JCheckBox useJAASField;

    public ConnectDatabaseDialog() {
        super("Connect to database");
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = super.buildFieldsPanel();
        userNameField = new JTextField("Administrator");
        passwordField = new JPasswordField();
        cachePagesField = new JTextField();
        bootStrapPath = new JLabel();
        setPreferredWidthOf(passwordField, 200);
        setPreferredWidthOf(userNameField, 200);
        setPreferredWidthOf(cachePagesField, 200);
        bootStrapPath.setText(AdminMainFrame.getCurrentBootstrapPath());
        bootStrapPath.setForeground(Color.black);
        addToGrid(true, new JLabel("Username:"));
        addToGrid(false, userNameField);
        addToGrid(true, new JLabel("Password:"));
        addToGrid(false, passwordField);
        useJAASField = new JCheckBox("Use JAAS for authentication");
        useJAASField.setForeground(new JLabel("dummy").getForeground());
        addToGrid(true, useJAASField, 2);
        addToGrid(true, new JLabel("Cache pages:"));
        addToGrid(false, cachePagesField);
        addToGrid(true, new JLabel("Bootstrap path:"));
        addToGrid(false, bootStrapPath);

        JButton changeBootstrapButton = new JButton("Change");
        changeBootstrapButton.addActionListener(new ActionListener() {
                                                    public void actionPerformed(ActionEvent e) {
                                                        try {
                                                            XhiveBootstrapDialog.showSelectFederation();
                                                        } catch (XhiveCancellation cancel) {
                                                            // I do not know why this is not caught elsewhere, but does not matter
                                                        }

                                                        bootStrapPath.setText(AdminMainFrame.getCurrentBootstrapPath());
                                                        setFields();
                                                    }
                                                }
                                               );

        useJAASField.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               if (useJAASField.isSelected()) {
                                                   passwordField.setText("");
                                                   userNameField.setEnabled(false);
                                                   passwordField.setEnabled(false);
                                               } else {
                                                   userNameField.setEnabled(true);
                                                   passwordField.setEnabled(true);
                                               }
                                               pack();
                                           }
                                       }
                                      );

        addToGrid(false, changeBootstrapButton);
        return fieldsPanel;
    }

    @Override
    protected boolean performAction() throws Exception {
        createdConnectionInfo = new XhiveConnectionInfo(userNameField.getText(),
                                new String(passwordField.getPassword()),
                                getDatabaseFieldValue(), useJAASField.isSelected());
        AdminMainFrame.setCacheSize(Integer.valueOf(cachePagesField.getText()).intValue());
        AdminProperties.setProperty("com.xhive.adminclient.database", getDatabaseFieldValue());
        if (!useJAASField.isSelected()) {
            AdminProperties.setProperty("com.xhive.adminclient.username", userNameField.getText());
        }
        AdminProperties.setProperty("com.xhive.adminclient.maxcachepages", cachePagesField.getText());
        return true;
    }

    @Override
    protected JComponent getDatabaseField() {
        if (databaseField == null) {
            databaseField = new JComboBox();
        }
        return databaseField;
    }

    @Override
    protected String getDatabaseFieldValue() {
        return databaseField.getSelectedItem().toString();
    }

    @Override
    protected void setFields() {
        boolean jaasAllowed = fillinDatabaseNames(databaseField);
        databaseField.setSelectedItem(AdminProperties.getProperty("com.xhive.adminclient.database"));
        userNameField.setText(AdminProperties.getProperty("com.xhive.adminclient.username"));
        cachePagesField.setText(AdminProperties.getProperty("com.xhive.adminclient.maxcachepages"));
        if (jaasAllowed) {
            useJAASField.setEnabled(true);
            useJAASField.setVisible(true);
        } else {
            useJAASField.setSelected(false);
            useJAASField.setEnabled(false);
            useJAASField.setVisible(false);
        }
        pack();
    }

    @Override
    protected boolean fieldsAreValid() {
        if (super.fieldsAreValid()) {
            return ((useJAASField.isSelected() || checkFieldNotEmpty(userNameField, "User name should be filled in!")) &&
                    (useJAASField.isSelected() || checkFieldNotEmpty(passwordField, "Password should be filled in!")) &&
                    checkFieldIsInteger(cachePagesField, "Cache pages should be a number!"));
        }
        return false;
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_OPENED) {
            passwordField.requestFocus();
        }
    }

    protected XhiveConnectionInfo getCreatedConnectionInfo() {
        return createdConnectionInfo;
    }
}

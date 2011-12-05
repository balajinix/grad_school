package com.xhive.adminclient.dialogs;

import com.xhive.XhiveDriverFactory;
import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.DriverRegistry;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.error.XhiveException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for setting/ selecting a bootstrap path.
 * Precondition: the Adminclient is currently not connected to any database.
 */
public class XhiveBootstrapDialog extends XhiveDialog {

    private JComboBox bootstrapPathField;

    public static void showSelectFederation() {
        if (!AdminMainFrame.getInstance().isConnected()) {
            XhiveBootstrapDialog dialog = new XhiveBootstrapDialog();
            dialog.execute();
        } else {
            XhiveMessageDialog.showErrorMessage("Cannot select another federation when connected to a database");
        }
    }

    protected XhiveBootstrapDialog() {
        super("Select federation");
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new FlowLayout());
        bootstrapPathField = new JComboBox();
        bootstrapPathField.setEditable(true);
        setPreferredWidthOf(bootstrapPathField, 350);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               chooseBootstrapPath();
                                           }
                                       }
                                      );

        fieldsPanel.add(new JLabel("Newly selected bootstrap path"));
        fieldsPanel.add(bootstrapPathField);
        fieldsPanel.add(browseButton);

        return fieldsPanel;
    }

    @Override
    protected void setFields() {
        super.setFields();
        setBootstrapPaths();
    }

    @Override
    protected boolean fieldsAreValid() {
        Exception result = tryConnect(((String) bootstrapPathField.getSelectedItem()).trim());
        if (result != null) {
            XhiveMessageDialog.showException(this, result);
        }
        return result == null;
    }

    /**
     * @returns null if connection could be made to federation, the error otherwise
     */
    private Exception tryConnect(String path) {
        Exception result = null;
        XhiveDriverIf driver = null;
        try {
            driver = XhiveDriverFactory.getDriver(path);
            DriverRegistry.registerDriverUser(driver, this);
            if (!driver.isInitialized()) {
                driver.init(200);
            }
        } catch (XhiveException e) {
            result = e;
        } finally {
            if (driver != null) {
                DriverRegistry.unregisterDriverUser(driver, this);
            }
        }
        return result;
    }

    private void chooseBootstrapPath() {
        EFileChooser fileChooser = new EFileChooser("Choose a bootstrap file");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(EFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileFilter() {
                                               @Override
                                               public boolean accept(File f) {
                                                   return (f.isDirectory() || (f.isFile() && f.getName().endsWith(".bootstrap")));
                                               }

                                               @Override
                                               public String getDescription() {
                                                   return "*.bootstrap";
                                               }
                                           }
                                          );
        fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[1]);
        if (fileChooser.showDialog(this, "ok") == EFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            // See if the item is already there
            boolean contains = false;
            for (int i = 0; i < bootstrapPathField.getItemCount(); i++) {
                if (bootstrapPathField.getItemAt(i).equals(path)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                if (bootstrapPathField.getItemCount() > 4) {
                    bootstrapPathField.removeItemAt(4);
                }
                bootstrapPathField.insertItemAt(path, 0);
            }
            bootstrapPathField.setSelectedItem(path);
        }
    }

    /**
     * Fields are valid already did all the checking, so now it's just time
     * to perform the action.
     */
    @Override
    protected boolean performAction() throws Exception {
        String path = (String) bootstrapPathField.getSelectedItem();
        AdminMainFrame.setCurrentBootstrapPath(path);
        storeBootstrapPaths();
        return true;
    }

    private void storeBootstrapPaths() {
        int size = bootstrapPathField.getItemCount();
        if (size > 4) {
            size = 4;
        }
        AdminProperties.setProperty(AdminProperties.BOOTSTRAP_PATH + 0, (String) bootstrapPathField.getSelectedItem());
        for (int i = 0; i < size; i++) {
            AdminProperties.setProperty(AdminProperties.BOOTSTRAP_PATH + (i+1), (String) bootstrapPathField.getItemAt(i));
        }
    }

    private void setBootstrapPaths() {
        for (int i = 0; i < 4; i++) {
            String bootstrapPath = AdminProperties.getProperty(AdminProperties.BOOTSTRAP_PATH + i);
            if (bootstrapPath != null) {
                bootstrapPathField.addItem(bootstrapPath);
            }
        }
        String currentBootStrapPath = AdminMainFrame.getCurrentBootstrapPath();
        if (currentBootStrapPath != null) {
            // Seee if the item is already there
            boolean contains = false;
            for (int i = 0; i < bootstrapPathField.getItemCount(); i++) {
                if (bootstrapPathField.getItemAt(i).equals(currentBootStrapPath)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                bootstrapPathField.insertItemAt(currentBootStrapPath, 0);
            }
            bootstrapPathField.setSelectedItem(currentBootStrapPath);
        }
    }

    /**
     * Some other dialogs work with newly chosen paths, which from then on should show up in the list of
     * federations. They can call this method to accomplish that
     */
    public static void addPathToAdminProperties(String newPath) {
        // Insert this path before the previous paths
        for (int i = 3; i > 0; i--) {
            String bootstrapPath = AdminProperties.getProperty(AdminProperties.BOOTSTRAP_PATH + (i-1));
            if (bootstrapPath != null) {
                AdminProperties.setProperty(AdminProperties.BOOTSTRAP_PATH + i, bootstrapPath);
            }
        }
        AdminProperties.setProperty(AdminProperties.BOOTSTRAP_PATH + 0, newPath);
    }

}


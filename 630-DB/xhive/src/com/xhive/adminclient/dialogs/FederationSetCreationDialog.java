package com.xhive.adminclient.dialogs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.XhiveAction;
import com.xhive.federationset.interfaces.XhiveFederationSetFactory;

public class FederationSetCreationDialog extends XhiveDialog {

    private static final String PROPERTY_NAME = "com.xhive.adminclient.federationset.";
    private static final int SAVED_ITEMS = 8;

    private final boolean create;
    private JComboBox fileComboBox;

    public static void showDialog(boolean create) {
        new FederationSetCreationDialog(create).execute();
    }

    private FederationSetCreationDialog(boolean create) {
        super(create ? "Create federation set" : "Delete federation set");
        this.create = create;
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        setGridPanel(fieldsPanel);
        addToGrid(false, new JLabel("Federation set description file"));
        Vector<String> items = getComboBoxItems();
        fileComboBox = new JComboBox(items);
        fileComboBox.setEditable(true);
        setPreferredWidthOf(fileComboBox, 200);
        addToGrid(true, fileComboBox);
        final JButton browseButton = new JButton(new XhiveAction("Browse", 'B') {
                                         @Override
                                         protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                             browse();
                                         }
                                     }
                                                );
        addToGrid(false, browseButton);
        return fieldsPanel;
    }

    public static Vector<String> getComboBoxItems() {
        Vector<String> result = new Vector<String>();
        for (int i = 0; i < SAVED_ITEMS; ++i) {
            String propertyName = PROPERTY_NAME + String.valueOf(i);
            String propertyValue = AdminProperties.getProperty(propertyName);
            if (propertyValue != null) {
                result.add(propertyValue);
            }
        }
        return result;
    }

    private void browse() {
        browse(fileComboBox, this, create);
    }

    public static void browse(JComboBox fileComboBox, Component parent, boolean create) {
        EFileChooser fileChooser = new EFileChooser("Choose a file");
        fileChooser.setFileSelectionMode(create ? EFileChooser.FILES_AND_DIRECTORIES
                                         : EFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileFilter() {

                                               @Override
                                               public boolean accept(File f) {
                                                   return f.isDirectory() || f.getName().endsWith(".fds");
                                               }

                                               @Override
                                               public String getDescription() {
                                                   return "Federation sets (*.fds)";
                                               }
                                           }
                                          );
        if (fileChooser.showDialog(parent, "Ok") == EFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            fileComboBox.setSelectedItem(path);
        }
    }

    public static String saveState(JComboBox fileComboBox) {
        String path = (String)fileComboBox.getSelectedItem();
        if (path == null || path.equals("")) return null;
        // Remove from box if already present
        for (int i = 0; i < fileComboBox.getItemCount(); ++i) {
            if (path.equals(fileComboBox.getItemAt(i))) {
                fileComboBox.removeItemAt(i);
            }
        }
        // Then insert at start
        fileComboBox.insertItemAt(path, 0);
        fileComboBox.setSelectedIndex(0);
        // And save values for next time
        int n = Math.min(fileComboBox.getItemCount(), SAVED_ITEMS);
        for (int i = 0; i < n; ++i) {
            String propertyName = PROPERTY_NAME + String.valueOf(i);
            String propertyValue = (String)fileComboBox.getItemAt(i);
            AdminProperties.setProperty(propertyName, propertyValue);
        }
        return path;
    }

    @Override
    protected boolean performAction() throws IOException {
        String path = saveState(fileComboBox);
        if (path != null) {
            // Now do actual operation
            if (create) {
                XhiveFederationSetFactory.createFederationSet(path);
            } else {
                XhiveFederationSetFactory.deleteFederationSet(path);
            }
        }
        return true;
    }
}

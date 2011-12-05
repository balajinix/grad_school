package com.xhive.adminclient.dialogs;

import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.XhiveAction;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveCatalogIf;
import org.w3c.dom.as.ASModel;
import org.w3c.dom.as.DOMASWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for exporting ASModels (seperate class as it shares not so much with XhiveExportDialog.
 *
 */
public class XhiveExportASModelDialog extends XhiveTransactedDialog {

    private ASModel model;

    private JPanel fieldsPanel;
    private JTextField directoryField;
    private JTextField fileNameField;
    private JButton directoryButton;
    private JComboBox encodingType;
    private JLabel exportStatusLabel;

    private XhiveAction browseAction = new XhiveAction("Browse...") {
                                           protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                               showDirectoryChooser();
                                           }
                                       };

    public XhiveExportASModelDialog(XhiveSessionIf session, ASModel model) {
        super("Export AS Model", session);
        this.model = model;
    }

    protected JPanel buildFieldsPanel() {
        fieldsPanel = new JPanel();
        directoryField = new JTextField();
        directoryField.setText(AdminProperties.getProperty(AdminProperties.EXPORT_DIR));
        setPreferredWidthOf(directoryField, 200);

        encodingType = new JComboBox();
        encodingType.setEditable(true);
        encodingType.setPreferredSize(directoryField.getPreferredSize());
        encodingType.addItem("Original Encoding");
        encodingType.addItem("UTF-8");
        encodingType.addItem("US-ASCII");
        encodingType.addItem("ISO-8859-1");

        fileNameField = new JTextField();
        fileNameField.setText(determineFileName());
        setPreferredWidthOf(fileNameField, 200);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets.top = 5;
        gridBagConstraints.insets.left = 5;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        topPanel.add(new JLabel("Select a target directory:"), gridBagConstraints);
        gridBagConstraints.gridy = 1;
        topPanel.add(new JLabel("Select an encoding type:"), gridBagConstraints);
        gridBagConstraints.gridy = 2;
        topPanel.add(new JLabel("Override filename:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets.top = 5;
        topPanel.add(directoryField, gridBagConstraints);
        gridBagConstraints.gridx = 2;
        topPanel.add(new JButton(browseAction), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        topPanel.add(encodingType, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        topPanel.add(fileNameField, gridBagConstraints);

        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.add(topPanel);
        // Create a features panel with a prototype writer for intial values
        return fieldsPanel;
    }

    private String determineFileName() {
        String fileName = model.getLocation();
        if (fileName != null) {
            fileName = fileName.replace('\\', '/');
            if (fileName.indexOf('/') != -1) {
                fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            }
        }
        if (fileName == null) {
            fileName = "";
        }
        return fileName;
    }

    private JPanel buildProgressPanel() {
        JPanel progresstPanel = new JPanel(new BorderLayout());
        exportStatusLabel = new JLabel("Export");
        exportStatusLabel.setPreferredSize(new Dimension(400, 20));
        progresstPanel.add(exportStatusLabel, BorderLayout.CENTER);
        return progresstPanel;
    }

    // Workaround for bug 4295864
    // See http://developer.java.sun.com/developer/bugParade/bugs/4295864.html
    public void setVisible(boolean isVisible) {
        super.setVisible(isVisible);
        if (isResizable()) {
            setResizable(false);
        }
    }

    public static void showExportDialog(XhiveSessionIf session, ASModel model) {
        XhiveExportASModelDialog exportDialog = new XhiveExportASModelDialog(session, model);
        exportDialog.execute();
    }

    private void showDirectoryChooser() {
        JFileChooser fileChooser = new JFileChooser(AdminProperties.getFile(AdminProperties.EXPORT_DIR)
                                   .getParent());
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
            directoryField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    void lockDialog() {
        super.lockDialog();
        fieldsPanel.remove(fieldsPanel);
        fieldsPanel.add(buildProgressPanel());
        pack();
        resizeAndCenter();
    }

    protected boolean performAction() throws Exception {
        serializeModel(directoryField.getText(), fileNameField.getText().trim(), model);
        AdminProperties.setProperty(AdminProperties.EXPORT_DIR, directoryField.getText());
        return true;
    }

    private DOMASWriter getWriter(XhiveCatalogIf catalog) {
        DOMASWriter writer = catalog.createDOMASWriter();
        if (!((String) encodingType.getSelectedItem()).equals("Original Encoding")) {
            writer.setEncoding((String) encodingType.getSelectedItem());
        }
        return writer;
    }

    private boolean serializeModel(String dir, String fileName, ASModel model) {
        if (new File(dir).exists()) {
            String separator = System.getProperty("file.separator");
            exportStatusLabel.setText("Exporting " + dir + separator + model.getHint());
            FileOutputStream fo = null;
            try {
                if ((fileName == null) || fileName.equals("")) {
                    int i = 0;
                    boolean nameExists;
                    do {
                        fileName = "unnamed" + i++;
                        nameExists = new File(dir + separator + fileName).exists();
                    } while (nameExists);
                }
                fo = new FileOutputStream(dir + separator + fileName);
                DOMASWriter writer = getWriter(getSession().getDatabase().getRoot().getCatalog());
                writer.writeASModel(fo, model);
                fo.close();
            } catch (Exception e) {
                XhiveMessageDialog.showException(this, e);
            }
            return true;
        } else {
            XhiveMessageDialog.showErrorMessage(this, "The directory " + dir + " does not exist.");
            return false;
        }
    }
}

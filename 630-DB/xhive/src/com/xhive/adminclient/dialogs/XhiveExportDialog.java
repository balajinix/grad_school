package com.xhive.adminclient.dialogs;

import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.panes.XhiveDOMConfigurationPanel;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveBlobNodeIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSOutput;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for exporting documents.
 *
 */
public class XhiveExportDialog extends XhiveTransactedDialog {

    private XhiveLibraryChildIf libraryChild;

    private JPanel fieldsPanel;
    private XhiveDOMConfigurationPanel featuresPanel;
    private JTextField directoryField;
    private JComboBox encodingType;
    private JLabel exportStatusLabel;

    private XhiveAction browseAction = new XhiveAction("Browse...") {
                                           @Override
                                           protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                               showDirectoryChooser();
                                           }
                                       };

    public XhiveExportDialog(XhiveSessionIf session, XhiveLibraryChildIf libraryChild) {
        super("Export", session);
        this.libraryChild = libraryChild;
    }

    @Override
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

        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.add(topPanel);
        // Create a features panel with a prototype writer for intial values
        featuresPanel = XhiveDOMConfigurationPanel.getWriterPanel(getSession().getDatabase().getRoot().createLSSerializer().getDomConfig());
        fieldsPanel.add(featuresPanel);
        return fieldsPanel;
    }

    private JPanel buildProgressPanel() {
        JPanel progresstPanel = new JPanel(new BorderLayout());
        exportStatusLabel = new JLabel("Export");
        exportStatusLabel.setPreferredSize(new Dimension(400, 20));
        progresstPanel.add(exportStatusLabel, BorderLayout.CENTER);
        return progresstPanel;
    }

    private HashMap<String,Boolean> getFeatureMap() {
        return featuresPanel.getFeatureMap();
    }

    // Workaround for bug 4295864
    // See http://developer.java.sun.com/developer/bugParade/bugs/4295864.html
    @Override
    public void setVisible(boolean isVisible) {
        super.setVisible(isVisible);
        if (isResizable()) {
            setResizable(false);
        }
    }

    public static void showExportDialog(XhiveSessionIf session, XhiveLibraryChildIf libraryChild) {
        XhiveExportDialog exportDialog = new XhiveExportDialog(session, libraryChild);
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

    @Override
    void lockDialog() {
        super.lockDialog();
        fieldsPanel.remove(fieldsPanel);
        fieldsPanel.add(buildProgressPanel());
        pack();
        resizeAndCenter();
    }

    @Override
    protected boolean performAction() throws Exception {
        serializeNode(directoryField.getText(), libraryChild);
        AdminProperties.setProperty(AdminProperties.EXPORT_DIR, directoryField.getText());
        return true;
    }

    private LSSerializer getWriter(XhiveLibraryIf library, HashMap featureMap) {
        LSSerializer writer = library.createLSSerializer();
        // Copy settings from prototype
        for (Iterator i = featureMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            writer.getDomConfig().setParameter((String) entry.getKey(),
                                               ((Boolean)featureMap.get(entry.getKey())).booleanValue() ? Boolean.TRUE : Boolean.FALSE);
        }
        return writer;
    }

    private boolean serializeNode(String dir, XhiveLibraryChildIf libraryChild1) {
        if (new File(dir).exists()) {
            String separator = System.getProperty("file.separator");
            if (libraryChild1 instanceof XhiveLibraryIf) {
                XhiveLibraryChildIf child = (XhiveLibraryChildIf) libraryChild1.getFirstChild();
                while (child != null) {
                    File newDir = new File(dir + separator + libraryChild1.getName());
                    if (!newDir.exists()) {
                        newDir.mkdir();
                    }
                    serializeNode(newDir.getAbsolutePath(), child);
                    child = (XhiveLibraryChildIf) child.getNextSibling();
                }
            } else {
                exportStatusLabel.setText("Exporting " + dir + separator + libraryChild1.getName());
                //System.out.println("Exporting " + dir + separator + libraryChild.getName());
                FileOutputStream fo = null;
                try {
                    String name = libraryChild1.getName();
                    if (name == null) {
                        int i = 0;
                        boolean nameExists;
                        do {
                            name = "unnamed" + i++;
                            nameExists = new File(dir + separator + name).exists();
                        } while (nameExists);
                    }
                    fo = new FileOutputStream(dir + separator + name);
                    if (libraryChild1 instanceof XhiveBlobNodeIf) {
                        InputStream in = ((XhiveBlobNodeIf) libraryChild1).getContents();
                        byte[] buffer = new byte[(int) ((XhiveBlobNodeIf) libraryChild1).getSize()];
                        int length;
                        while ((length = in.read(buffer)) != -1) {
                            fo.write(buffer, 0, length);
                        }
                    } else {
                        LSSerializer writer = getWriter(getSession().getDatabase().getRoot(), getFeatureMap());
                        LSOutput output = getSession().getDatabase().getRoot().createLSOutput();
                        output.setByteStream(fo);
                        if (!((String) encodingType.getSelectedItem()).equals("Original Encoding")) {
                            output.setEncoding((String) encodingType.getSelectedItem());
                        }
                        writer.write(libraryChild1, output);
                    }
                    fo.close();
                } catch (Exception e) {
                    XhiveMessageDialog.showException(this, e);
                }
            }
            return true;
        } else {
            XhiveMessageDialog.showErrorMessage(this, "The directory " + dir + " does not exist.");
            return false;
        }
    }
}

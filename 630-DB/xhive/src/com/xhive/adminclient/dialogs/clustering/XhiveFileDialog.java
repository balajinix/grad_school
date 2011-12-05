package com.xhive.adminclient.dialogs.clustering;

import com.xhive.adminclient.dialogs.XhiveTransactedDialog;
import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.core.interfaces.XhiveFileIf;
import com.xhive.core.interfaces.XhiveSegmentIf;
import com.xhive.core.interfaces.XhiveSessionIf;

import javax.swing.*;

public abstract class XhiveFileDialog extends XhiveTransactedDialog {

    private static final String UNLIMITED = "unlimited";

    protected JTextField pathField;
    protected JTextField maxSizeField;

    protected XhiveFileDialog(String title, XhiveSessionIf session) {
        super(title, session);
    }

    long getCurrentFileSize() {
        return -1;
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new FormLayout());
        pathField = new JTextField("");
        maxSizeField = new JTextField("");
        setPreferredWidthOf(pathField, 200);
        fieldsPanel.add(new JLabel("Path (optional):"));
        fieldsPanel.add(pathField);
        fieldsPanel.add(new JLabel("Max size (bytes, optional):"));
        fieldsPanel.add(maxSizeField);
        return fieldsPanel;
    }

    public static XhiveFileIf showCreateFile(XhiveSessionIf session, XhiveSegmentIf segment) {
        CreateFileDialog dialog = new CreateFileDialog(session, segment);
        dialog.execute();
        return dialog.getCreatedFile();
    }

    public static int showEditFile(XhiveSessionIf session, XhiveFileIf file) {
        EditFileDialog dialog = new EditFileDialog(session, file);
        return dialog.execute();
    }

    protected boolean fieldsAreValid() {
        return checkMaxSizeField(maxSizeField);
    }

    private boolean checkMaxSizeField(JTextField textField) {
        long size = createSizeFromString(textField.getText());
        if (size >= 0) {
            return checkField(textField, (size == 0L) || (size >= getCurrentFileSize()),
                              "Size cannot be smaller than current file size (" + getCurrentFileSize() + ")");
        } else if (size == -1) {
            return checkField(textField, false, "Expected a long value for max size");
        } else {
            return checkField(textField, false, "Expected a positive long value for max size");
        }
    }

    /**
     * Returns -1 on an incorrect string
     */
    public static long createSizeFromString(String sizeString) {
        long multiplier = 1;
        sizeString = sizeString.trim();
        if (sizeString.equals("")) {
            return 0;
        }
        if (sizeString.equals(UNLIMITED)) {
            return 0;
        }
        sizeString = sizeString.toLowerCase();
        if (sizeString.endsWith("k")) {
            multiplier = 1024;
        }
        if (sizeString.endsWith("m")) {
            multiplier = 1024 * 1024;
        }
        if (sizeString.endsWith("g")) {
            multiplier = 1024 * 1024 * 1024;
        }
        if (multiplier != 1) {
            // last char was a multiplier
            sizeString = sizeString.substring(0, sizeString.length() - 1);
        }
        long value;
        try {
            value = Long.parseLong(sizeString);
        } catch (NumberFormatException e) {
            return -1;
        }
        value = value * multiplier;
        return value;
    }

    public static String convertSizeToString(long size) {
        if (size == 0L) {
            return UNLIMITED;
        }
        long multiplier = 1024 * 1024 * 1024;
        String[] multiplierStrings = {"K", "M", "G"};
        for (int i = 2; i >= 0; i--) {
            if ((size % multiplier) == 0) {
                return String.valueOf(size / multiplier) + multiplierStrings[i];
            }
            multiplier = multiplier / 1024;
        }
        return String.valueOf(size);
    }
}

class EditFileDialog extends XhiveFileDialog {

    private XhiveFileIf file;
    private long currentFileSize;

    public EditFileDialog(XhiveSessionIf session, XhiveFileIf file) {
        super("File properties", session);
        this.file = file;
        this.currentFileSize = file.getCurrentFileSize();
    }

    long getCurrentFileSize() {
        return currentFileSize;
    }

    protected void setFields() {
        pathField.setText(file.getFileName());
        maxSizeField.setText(convertSizeToString(file.getMaxFileSize()));
        pathField.setEnabled(false);
    }

    protected boolean performAction() throws Exception {
        file.setMaxFileSize(createSizeFromString(maxSizeField.getText()));
        return true;
    }

}

class CreateFileDialog extends XhiveFileDialog {

    private XhiveFileIf createdFile;
    private XhiveSegmentIf segment;

    public CreateFileDialog(XhiveSessionIf session, XhiveSegmentIf segment) {
        super("Add a new file", session);
        this.segment = segment;
    }

    protected boolean performAction() throws Exception {
        setWaitText("Creating new file");
        String path = pathField.getText().equals("") ? null : pathField.getText();
        String max = maxSizeField.getText().trim();
        long maxSize = createSizeFromString(max);
        createdFile = segment.addFile(path, maxSize);
        return true;
    }

    protected XhiveFileIf getCreatedFile() {
        return createdFile;
    }
}

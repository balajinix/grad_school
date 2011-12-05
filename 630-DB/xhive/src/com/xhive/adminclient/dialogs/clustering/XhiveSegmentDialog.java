package com.xhive.adminclient.dialogs.clustering;

import javax.swing.*;

import com.xhive.adminclient.dialogs.XhiveTransactedDialog;
import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSegmentIf;
import com.xhive.core.interfaces.XhiveSessionIf;

public class XhiveSegmentDialog extends XhiveTransactedDialog {

    private XhiveSegmentIf segment;

    private JTextField idField;
    private JTextField pathField;
    private JTextField maxSizeField;
    private JCheckBox tempOption;

    public XhiveSegmentDialog(XhiveSessionIf session) {
        super("Add a new segment", session);
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new FormLayout());
        idField = new JTextField("");
        pathField = new JTextField("");
        setPreferredWidthOf(pathField, 200);
        maxSizeField = new JTextField("");
        tempOption = new JCheckBox("Temporary data segment");
        fieldsPanel.add(new JLabel("Segment id:"));
        fieldsPanel.add(idField);
        fieldsPanel.add(new JLabel("Path (optional):"));
        fieldsPanel.add(pathField);
        fieldsPanel.add(new JLabel("Max size (bytes, optional):"));
        fieldsPanel.add(maxSizeField);
        fieldsPanel.add(tempOption);
        return fieldsPanel;
    }

    static public XhiveSegmentIf showCreateSegment(XhiveSessionIf session) {
        XhiveSegmentDialog dialog = new XhiveSegmentDialog(session);
        dialog.execute();

        return dialog.segment;
    }

    protected boolean performAction() throws Exception {
        String id = idField.getText();
        String path = pathField.getText().equals("") ? null : pathField.getText();
        String max = maxSizeField.getText().trim();
        long maxSize = max.equals("") ? 0L: Long.parseLong(max);
        boolean temporary = tempOption.isSelected();
        XhiveDatabaseIf database = getSession().getDatabase();
        segment = temporary ?
                  database.createTemporaryDataSegment(id, path, maxSize) :
                  database.createSegment(id, path, maxSize);
        return true;
    }

    protected boolean fieldsAreValid() {
        return (checkFieldNotEmpty(idField, "Expected an id") &&
                checkFieldIsLong(maxSizeField, true, "Expected an integer value for max size"));
    }
}

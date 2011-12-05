package com.xhive.adminclient.dialogs;

import java.util.Iterator;
import javax.swing.*;

import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSegmentIf;
import com.xhive.core.interfaces.XhiveSessionIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for editing the properties of a database.
 */
public class XhiveDatabasePropertiesDialog extends XhiveTransactedDialog {

    private static final String NONE_SELECTED = "<none selected>";
    private JTextField nameField;
    private JComboBox segmentIdChoice;
    private String previousSegmentId;

    public static int showDatabaseProperties(XhiveSessionIf session) {
        XhiveDatabasePropertiesDialog dialog = new XhiveDatabasePropertiesDialog("Database properties", session);
        return dialog.execute();
    }

    public XhiveDatabasePropertiesDialog(String title, XhiveSessionIf session) {
        super(title, session);
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new FormLayout());
        nameField = new JTextField();
        setPreferredWidthOf(nameField, 200);
        fieldsPanel.add(new JLabel("Name:"));
        fieldsPanel.add(nameField);
        nameField.setEnabled(false);

        segmentIdChoice = new JComboBox();
        segmentIdChoice.setEditable(false);
        setPreferredWidthOf(segmentIdChoice, 200);
        fieldsPanel.add(new JLabel("Temporary data segment:"));
        fieldsPanel.add(segmentIdChoice);
        return fieldsPanel;
    }

    public void setFields() {
        nameField.setText(getSession().getDatabase().getName());
        segmentIdChoice.addItem(NONE_SELECTED);
        Iterator segments = getSession().getDatabase().getSegments();
        while (segments.hasNext()) {
            XhiveSegmentIf segment = (XhiveSegmentIf) segments.next();
            segmentIdChoice.addItem(segment.getId());
        }
        segmentIdChoice.addItem(XhiveDatabaseIf.RAM_SEGMENT_NAME);
        previousSegmentId = getSession().getDatabase().getTemporaryDataSegment();
        if (previousSegmentId != null) {
            segmentIdChoice.setSelectedItem(previousSegmentId);
        } else {
            previousSegmentId = NONE_SELECTED;
        }
    }

    protected boolean fieldsAreValid() {
        return true;
    }

    protected boolean performAction() {
        String selectedSegmentId = (String) segmentIdChoice.getSelectedItem();
        if (!selectedSegmentId.equals(previousSegmentId)) {
            if (selectedSegmentId.equals(NONE_SELECTED)) {
                selectedSegmentId = null;
            }
            getSession().getDatabase().setTemporaryDataSegment(selectedSegmentId);
        }
        return true;
    }

}


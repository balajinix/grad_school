package com.xhive.adminclient.dialogs;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.layouts.StackLayout;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveSegmentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for editing the properties of a library.
 */
public abstract class XhiveLibraryDialog extends XhiveLibraryChildDialog {

    protected static final String NONE_SELECTED = "<none selected>";

    protected JCheckBox lockWithParentCheckBox;
    protected JCheckBox documentsDoNotLockWithParent;
    protected JComboBox segmentIdChoice;

    //  public static void main(String[] args) {
    //    final XhiveSessionIf session = AdminMainFrame.getDriver().createSession();
    //    session.connect("Administrator", "secret", "MyDatabase");
    //    // make sure session is joined to AWT-eventqueue thread because from
    //    // that thread the session will be used when pressing OK-button
    //    SwingUtilities.invokeLater(
    //        new Runnable() {
    //          public void run() {
    //            session.join();
    //          }
    //        }
    //    );
    //    session.begin();
    //
    //    showCreateLibrary(session, session.getDatabase().getRoot());
    //
    //    session.commit();
    //    System.exit(0);
    //  }

    public static XhiveLibraryIf showCreateLibrary(XhiveSessionIf session, XhiveLibraryIf parentLibrary) {
        CreateLibraryDialog dialog = new CreateLibraryDialog(session, parentLibrary);
        dialog.execute();
        return dialog.getCreatedLibrary();
    }

    public static int showEditLibrary(XhiveSessionIf session, XhiveLibraryIf editLibrary) {
        XhiveLibraryDialog dialog = new EditLibraryDialog(session, editLibrary);
        return dialog.execute();
    }

    public XhiveLibraryDialog(String title, XhiveSessionIf session) {
        super(title, session);
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = super.buildFieldsPanel();
        segmentIdChoice = new JComboBox();
        segmentIdChoice.setEditable(false);
        fieldsPanel.add(new JLabel("Child segment:"));
        fieldsPanel.add(segmentIdChoice);
        JPanel newFieldsPanel = new JPanel(new StackLayout());
        lockWithParentCheckBox = new JCheckBox("Lock with parent library");
        documentsDoNotLockWithParent = new JCheckBox("Documents do not lock with parent");
        newFieldsPanel.add(fieldsPanel);
        newFieldsPanel.add(lockWithParentCheckBox);
        newFieldsPanel.add(documentsDoNotLockWithParent);
        return newFieldsPanel;
    }

    void fillInSegments(XhiveSessionIf session) {
        segmentIdChoice.addItem(NONE_SELECTED);
        Iterator segments = session.getDatabase().getSegments();
        while (segments.hasNext()) {
            XhiveSegmentIf segment = (XhiveSegmentIf) segments.next();
            segmentIdChoice.addItem(segment.getId());
        }
    }
}

class EditLibraryDialog extends XhiveLibraryDialog {

    private XhiveLibraryIf editLibrary;
    private String previousSegmentId = NONE_SELECTED;

    EditLibraryDialog(XhiveSessionIf session, XhiveLibraryIf editLibrary) {
        super("Library properties", session);
        this.editLibrary = editLibrary;
    }

    public void setFields() {
        setWaitText("Please wait while updating library...");
        nameField.setText(editLibrary.getName());
        descriptionField.setText(editLibrary.getDescription());
        fillInSegments(getSession());
        if (editLibrary.getChildSegmentId() != null) {
            previousSegmentId = editLibrary.getChildSegmentId();
            segmentIdChoice.setSelectedItem(editLibrary.getChildSegmentId());
        }
        lockWithParentCheckBox.setSelected(editLibrary.locksWithParent());
        lockWithParentCheckBox.setEnabled(false);
        documentsDoNotLockWithParent.setSelected(((editLibrary.getOptions() & XhiveLibraryIf.DOCUMENTS_DO_NOT_LOCK_WITH_PARENT)
                == XhiveLibraryIf.DOCUMENTS_DO_NOT_LOCK_WITH_PARENT));
        documentsDoNotLockWithParent.setEnabled(false);
    }

    protected boolean performAction() {
        editLibrary.setName(nameField.getText());
        editLibrary.setDescription(descriptionField.getText());
        String selectedSegmentId = (String) segmentIdChoice.getSelectedItem();
        if (!selectedSegmentId.equals(previousSegmentId)) {
            if (selectedSegmentId.equals(NONE_SELECTED)) {
                selectedSegmentId = null;
            }
            editLibrary.setChildSegmentId(selectedSegmentId);
        }
        return true;
    }

    protected boolean fieldsAreValid() {
        boolean doesLibraryHasName = editLibrary.getName() != null;
        String libraryName = editLibrary.getName();
        boolean libraryAlreadyHasThisName = libraryName == null ? false : libraryName.equalsIgnoreCase(nameField.getText());
        XhiveLibraryIf ownerLibrary = editLibrary.getOwnerLibrary();
        boolean parentHasChildWithSameName = false;
        if (ownerLibrary != null) {
            parentHasChildWithSameName = (ownerLibrary.get(nameField.getText()) != null);
        }
        if (doesLibraryHasName && !libraryAlreadyHasThisName && parentHasChildWithSameName) {
            XhiveMessageDialog.showErrorMessage("Librarychild with name \"" + nameField.getText() + "\" already exists.");
            return false;
        }
        return true;
    }
}

class CreateLibraryDialog extends XhiveLibraryDialog {

    private XhiveLibraryIf createdLibrary;
    private XhiveLibraryIf parentLibrary;

    CreateLibraryDialog(XhiveSessionIf session, XhiveLibraryIf parentLibrary) {
        super("Append child library", session);
        this.parentLibrary = parentLibrary;
    }

    protected void setFields() {
        setWaitText("Please wait while creating library...");
        fillInSegments(getSession());
        lockWithParentCheckBox.setSelected(false);
        lockWithParentCheckBox.setEnabled(true);
        documentsDoNotLockWithParent.setSelected(false);
        documentsDoNotLockWithParent.setEnabled(true);
    }

    protected boolean performAction() {
        int properties = lockWithParentCheckBox.isSelected() ? XhiveLibraryIf.LOCK_WITH_PARENT : 0;
        properties = documentsDoNotLockWithParent.isSelected() ? properties | XhiveLibraryIf.DOCUMENTS_DO_NOT_LOCK_WITH_PARENT : properties;

        createdLibrary = parentLibrary.createLibrary(properties);

        createdLibrary.setName(nameField.getText());
        createdLibrary.setDescription(descriptionField.getText());
        String selectedSegmentId = (String) segmentIdChoice.getSelectedItem();
        if (!selectedSegmentId.equals(NONE_SELECTED)) {
            createdLibrary.setChildSegmentId(selectedSegmentId);
        }
        parentLibrary.appendChild(createdLibrary);
        return true;
    }

    protected boolean fieldsAreValid() {
        if (parentLibrary.get(nameField.getText()) != null) {
            XhiveMessageDialog.showErrorMessage("Librarychild with name \"" + nameField.getText() + "\" already exists.");
            return false;
        }
        return true;
    }

    protected XhiveLibraryIf getCreatedLibrary() {
        return createdLibrary;
    }
}


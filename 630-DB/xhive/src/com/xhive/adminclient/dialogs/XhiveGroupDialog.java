package com.xhive.adminclient.dialogs;

import com.xhive.core.interfaces.XhiveGroupIf;
import com.xhive.core.interfaces.XhiveGroupListIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.adminclient.layouts.FormLayout;

import javax.swing.*;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for adding and editing groups.
 */
public abstract class XhiveGroupDialog extends XhiveTransactedDialog {

    protected JTextField nameField;

    public static XhiveGroupIf showAddGroup(XhiveSessionIf session) {
        CreateGroupDialog dialog = new CreateGroupDialog(session);
        dialog.execute();
        return dialog.getCreatedGroup();
    }

    public XhiveGroupDialog(String title, XhiveSessionIf session) {
        super(title, session);
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new FormLayout());
        nameField = new JTextField("");
        setPreferredWidthOf(nameField, 200);
        fieldsPanel.add(new JLabel("Group name:"));
        fieldsPanel.add(nameField);
        return fieldsPanel;
    }

    protected XhiveGroupListIf getGroupList() {
        return getSession().getDatabase().getGroupList();
    }

    protected boolean checkNameExists(String name) {
        if (getGroupList().getGroup(nameField.getText()) != null) {
            showError("Group with name " + nameField.getText() + " already exists", nameField);
            return true;
        }
        return false;
    }

    protected boolean fieldsAreValid() {
        return checkFieldNotEmpty(nameField, "Group name should be filled in!");
    }
}

class CreateGroupDialog extends XhiveGroupDialog {

    private XhiveGroupIf createdGroup;

    public CreateGroupDialog(XhiveSessionIf session) {
        super("Add a group", session);
    }

    protected boolean performAction() {
        XhiveGroupListIf groupList = getGroupList();
        createdGroup = groupList.addGroup(nameField.getText());
        return true;
    }

    protected XhiveGroupIf getCreatedGroup() {
        return createdGroup;
    }

    protected boolean fieldsAreValid() {
        if (!checkNameExists(nameField.getText())) {
            return super.fieldsAreValid();
        }
        return false;
    }
}


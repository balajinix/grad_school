package com.xhive.adminclient.dialogs;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.adminclient.layouts.StackLayout;
import com.xhive.core.interfaces.XhiveGroupIf;
import com.xhive.core.interfaces.XhiveGroupListIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveUserIf;
import com.xhive.core.interfaces.XhiveUserListIf;

import javax.swing.DefaultListModel;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for adding, editing and deleting users.
 */
public abstract class XhiveUserDialog extends XhiveTransactedDialog {

    public static XhiveUserIf showAddUser(XhiveSessionIf session) {

        CreateUserDialog dialog = new CreateUserDialog(session);
        dialog.execute();
        return dialog.getCreatedUser();
    }

    public static int showEditUser(XhiveSessionIf session, XhiveUserIf user) {
        XhiveUserDialog dialog = new EditUserDialog(session, user);
        return dialog.execute();
    }

    // Returns the name of the new owner
    public static String showDeleteUser(XhiveSessionIf session, XhiveUserIf user) {
        DeleteUserDialog dialog = new DeleteUserDialog(session, user);
        if (dialog.execute() == XhiveDialog.RESULT_OK) {
            return dialog.getNewOwnerName();
        }
        return null;
    }

    protected XhiveUserDialog(String title, XhiveSessionIf session) {
        super(title, session);
    }

    protected XhiveUserListIf getUserList() {
        return getSession().getDatabase().getUserList();
    }

    protected boolean isAdminSession() {
        return getSession().getUser().getName().equals("Administrator");
    }
}

abstract class AddOrEditDialog extends XhiveUserDialog {

    protected JTextField nameField;

    protected JPasswordField newPasswordField;
    protected JPasswordField retypedPasswordField;

    protected DefaultListModel allGroupsListModel;
    protected DefaultListModel selectedGroupsListModel;

    protected JList allGroupsList;
    protected JList selectedGroupsList;

    private XhiveAction toAllGroupsAction = new XhiveAction("<<") {
                                                protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                                    moveGroups(selectedGroupsList, allGroupsList);
                                                }
                                            };

    private XhiveAction toSelectedGroupsAction = new XhiveAction(">>") {
                protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                    moveGroups(allGroupsList, selectedGroupsList);
                }
            };

    protected AddOrEditDialog(String title, XhiveSessionIf session) {
        super(title, session);
    }

    protected JPanel buildFieldsPanel() {

        JPanel fieldsPanel = new JPanel(new BorderLayout());

        fieldsPanel.add(buildTopPanel(), BorderLayout.NORTH);

        allGroupsListModel = new DefaultListModel();
        selectedGroupsListModel = new DefaultListModel();
        allGroupsList = new JList(allGroupsListModel);
        selectedGroupsList = new JList(selectedGroupsListModel);
        JScrollPane allGroupsScrollPane = new JScrollPane(allGroupsList);
        JScrollPane selectedGroupsScrollPane = new JScrollPane(selectedGroupsList);
        allGroupsScrollPane.setPreferredSize(new Dimension(120, 120));
        selectedGroupsScrollPane.setPreferredSize(new Dimension(120, 120));

        JPanel allGroupsPanel = new JPanel(new BorderLayout());
        allGroupsPanel.add(new JLabel("All groups:"), BorderLayout.NORTH);
        allGroupsPanel.add(allGroupsScrollPane, BorderLayout.CENTER);

        JPanel selectedGroupsPanel = new JPanel(new BorderLayout());
        selectedGroupsPanel.add(new JLabel("Selected groups:"), BorderLayout.NORTH);
        selectedGroupsPanel.add(selectedGroupsScrollPane, BorderLayout.CENTER);

        JPanel moveButtonsPanel = new JPanel(new StackLayout());

        boolean mayMoveGroups = isAdminSession();
        allGroupsList.setEnabled(mayMoveGroups);
        selectedGroupsList.setEnabled(mayMoveGroups);
        toSelectedGroupsAction.setEnabled(mayMoveGroups);
        toAllGroupsAction.setEnabled(mayMoveGroups);
        moveButtonsPanel.add(new JButton(toSelectedGroupsAction));
        moveButtonsPanel.add(new JButton(toAllGroupsAction));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1;

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        bottomPanel.add(allGroupsPanel, gridBagConstraints);
        gridBagConstraints.gridx = 2;
        bottomPanel.add(selectedGroupsPanel, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = gridBagConstraints.weighty = 0;
        bottomPanel.add(moveButtonsPanel, gridBagConstraints);


        fieldsPanel.add(bottomPanel, BorderLayout.CENTER);

        selectedGroupsList.addMouseListener(
            new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        moveGroups(selectedGroupsList, allGroupsList);
                    }
                }
            }
        );

        allGroupsList.addMouseListener(
            new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        moveGroups(allGroupsList, selectedGroupsList);
                    }
                }
            }
        );
        return fieldsPanel;
    }

    protected JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new FormLayout());
        nameField = new JTextField("");
        setPreferredWidthOf(nameField, 200);
        newPasswordField = new JPasswordField();
        retypedPasswordField = new JPasswordField();
        topPanel.add(new JLabel("User name:"));
        topPanel.add(nameField);
        addPasswordField(topPanel);
        topPanel.add(new JLabel("New password:"));
        topPanel.add(newPasswordField);
        topPanel.add(new JLabel("Retype new password:"));
        topPanel.add(retypedPasswordField);
        return topPanel;
    }

    protected abstract void addPasswordField(JPanel topPanel);

    protected XhiveGroupListIf getGroupList() {
        return getSession().getDatabase().getGroupList();
    }

    protected void setGroupLists(XhiveUserIf user) {
        allGroupsListModel.clear();
        selectedGroupsListModel.clear();

        XhiveGroupListIf groupList = getGroupList();

        for (Iterator i = groupList.iterator(); i.hasNext();) {
            allGroupsListModel.addElement(((XhiveGroupIf) i.next()).getName());
        }

        if (user != null) {
            for (Iterator i = user.groups(); i.hasNext();) {
                XhiveGroupIf group = (XhiveGroupIf) i.next();
                allGroupsListModel.removeElement(group.getName());
                selectedGroupsListModel.addElement(group.getName());
            }
        }
    }

    protected void addGroups(XhiveUserIf user, XhiveGroupListIf groupList) {
        Enumeration groups = selectedGroupsListModel.elements();

        while (groups.hasMoreElements()) {
            String groupName = (String) groups.nextElement();
            user.addGroup(groupList.getGroup(groupName));
        }
    }

    protected void moveGroups(JList fromList, JList toList) {
        int selectedItem = fromList.getSelectedIndex();
        DefaultListModel fromModel = (DefaultListModel) fromList.getModel();
        DefaultListModel toModel = (DefaultListModel) toList.getModel();

        if (selectedItem < 0) {
            showError("Please select one ore more groups first!", fromList);
        } else {
            int currentGroup = 0;

            while (currentGroup < fromModel.getSize()) {
                if (fromList.isSelectedIndex(currentGroup)) {
                    String selectedGroup = (String) fromModel.getElementAt(currentGroup);
                    fromModel.removeElementAt(currentGroup);
                    toModel.addElement(selectedGroup);
                } else {
                    currentGroup++;
                }
            }
        }
    }

    protected boolean checkNameExists(String name) {
        if (getUserList().getUser(nameField.getText()) != null) {
            showError("User with name " + nameField.getText() + " already exists", nameField);
            return true;
        }
        return false;
    }

    protected boolean fieldsAreValid() {
        if (checkFieldNotEmpty(nameField, "User name should be filled in")) {
            if (super.fieldsAreValid()) {
                if (newPasswordField.getPassword().length > 0) {
                    if (!checkFieldLength(newPasswordField, 3, 8, "Password should be at least 3 and at most 8 characters long!")) {
                        return false;
                    }
                }
                String newPassword = new String(newPasswordField.getPassword());
                return checkFieldEquals(retypedPasswordField, newPassword, "Password and retyped password should be identical!");
            }
        }
        return false;
    }
}

class CreateUserDialog extends AddOrEditDialog {

    private XhiveUserIf createdUser;

    protected CreateUserDialog(XhiveSessionIf session) {
        super("Add a user", session);
    }

    protected void addPasswordField(JPanel topPanel) {
        // Do nothing
    }


    protected void setFields() {
        super.setFields();
        setGroupLists(null);
    }

    protected boolean performAction() {
        XhiveUserListIf userList = getUserList();
        XhiveGroupListIf groupList = getGroupList();

        createdUser = userList.addUser(nameField.getText(), new String(newPasswordField.getPassword()));
        addGroups(createdUser, groupList);
        return true;
    }

    protected boolean fieldsAreValid() {
        if (super.fieldsAreValid()) {
            if (checkNameExists(nameField.getText())) {
                return false;
            }
            String newPassword = new String(newPasswordField.getPassword());
            return checkField(newPasswordField, !newPassword.equals(""), "Password should be filled in");
        }
        return false;
    }

    protected XhiveUserIf getCreatedUser() {
        return createdUser;
    }
}

class EditUserDialog extends AddOrEditDialog {
    // these dialogs now only appear when
    //     isAdminSession() || isOwnUser()

    protected boolean isOwnUser() {
        return getSession().getUser().getName().equals(user.getName());
    }

    private XhiveUserIf user;

    private JPasswordField oldPasswordField;

    protected EditUserDialog(XhiveSessionIf session, XhiveUserIf user) {
        super("Edit a user", session);
        this.user = user;
    }

    protected void addPasswordField(JPanel topPanel) {
        if (!isOwnUser()) {
            // implies isAdminUser().
            // dont ask Admin to produce *other* users passwords.
            oldPasswordField = new JPasswordField("null");
            oldPasswordField.setEnabled(false);
        } else {
            oldPasswordField = new JPasswordField("");
        }
        topPanel.add(new JLabel("Password:"));
        topPanel.add(oldPasswordField);
    }

    protected void setFields() {
        super.setFields();
        nameField.setText(user.getName());
        nameField.setEnabled(false);
        setGroupLists(user);
    }

    protected boolean performAction() {
        String newPassword = new String(newPasswordField.getPassword());
        String oldPassword = null;
        if (isOwnUser()) {
            oldPassword = new String(oldPasswordField.getPassword());
        } else {
            // implies isAdminUser()
        }


        if (!newPassword.equals("")) {
            user.setPassword(oldPassword, newPassword);
        }

        if (isAdminSession()) {
            removeGroups();
            addGroups(user, getGroupList());
        }

        return true;
    }

    private void removeGroups() {
        for (Iterator i = user.groups(); i.hasNext();) {
            i.next();
            i.remove();
        }
    }

    protected boolean fieldsAreValid() {
        if (super.fieldsAreValid()) {
            if (!nameField.getText().equals(user.getName())) {
                if (checkNameExists(nameField.getText())) {
                    return false;
                }
            }
            String newPassword = new String(newPasswordField.getPassword());
            boolean valid = (newPassword.equals("")) || (
                                checkFieldLength(oldPasswordField, 3, 8, "Password should be at least 3 and at most 8 characters long!") &&
                                checkFieldLength(newPasswordField, 3, 8, "New Password should be at least 3 and at most 8 characters long!"));
            // If passwords are entered, they must match
            valid &= checkFieldEquals(retypedPasswordField, newPassword, "New Password and retyped new password should be identical!");
            return valid;
        }
        return false;
    }
}

class DeleteUserDialog extends XhiveUserDialog {

    private XhiveUserIf user;

    private JComboBox newOwnerComboBox;

    protected DeleteUserDialog(XhiveSessionIf session, XhiveUserIf user) {
        super("Delete user " + user.getName(), session);
        this.user = user;
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new FormLayout());
        newOwnerComboBox = new JComboBox();
        fieldsPanel.add(new JLabel("New owner of objects:"));
        fieldsPanel.add(newOwnerComboBox);
        return fieldsPanel;
    }

    private void fillNewOwnerComboBox() {
        newOwnerComboBox.removeAllItems();

        XhiveUserListIf userList = getUserList();
        String currentUserName = user.getName();

        for (Iterator i = userList.iterator(); i.hasNext();) {
            XhiveUserIf userToAdd = (XhiveUserIf) i.next();

            if (!userToAdd.getName().equals(currentUserName)) {
                newOwnerComboBox.addItem(userToAdd.getName());
            }
        }
    }

    protected void setFields() {
        super.setFields();
        setWaitText("Please wait while deleting user...");
        fillNewOwnerComboBox();
    }

    protected void performOk() throws Exception {
        // This is overwritten so no thread is started, since the actual action is done by
        // the caller of this dialog.
        setResult(XhiveDialog.RESULT_OK);
        dispose();
    }

    public String getNewOwnerName() {
        return (String) newOwnerComboBox.getSelectedItem();
    }
}

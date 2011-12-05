package com.xhive.adminclient.dialogs;

import com.xhive.core.interfaces.XhiveAuthorityIf;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveGroupIf;
import com.xhive.core.interfaces.XhiveGroupListIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveUserIf;
import com.xhive.core.interfaces.XhiveUserListIf;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;


/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for editing authority.
 */
public class XhiveAuthorityDialog extends XhiveTransactedDialog {

    private static final int EDIT_AUTHORITY = 0;
    private static final String NONE = "-- None --";

    private XhiveAuthorityIf authority = null;

    private JComboBox ownerComboBox;
    private JComboBox groupComboBox;
    private JComboBox ownerAuthorityComboBox;
    private JComboBox groupAuthorityComboBox;
    private JComboBox otherAuthorityComboBox;

    private static final int AUTHORITIES[] = new int[]{
                XhiveAuthorityIf.NO_ACCESS,
                XhiveAuthorityIf.READ_ACCESS,
                XhiveAuthorityIf.READ_EXECUTE_ACCESS,
                XhiveAuthorityIf.READ_WRITE_ACCESS,
                XhiveAuthorityIf.READ_WRITE_EXECUTE_ACCESS
            };

    public static int showEditAuthority(XhiveSessionIf session, XhiveAuthorityIf authority) {
        XhiveAuthorityDialog dialog = new XhiveAuthorityDialog(session, "Change authority", authority);
        return dialog.execute();
    }

    public XhiveAuthorityDialog(XhiveSessionIf session, String title, XhiveAuthorityIf authority) {
        super(title, session);
        this.authority = authority;
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        ownerComboBox = new JComboBox();
        groupComboBox = new JComboBox();
        ownerAuthorityComboBox = new JComboBox();
        groupAuthorityComboBox = new JComboBox();
        otherAuthorityComboBox = new JComboBox();

        fillAuthorityComboBox(ownerAuthorityComboBox);
        fillAuthorityComboBox(groupAuthorityComboBox);
        fillAuthorityComboBox(otherAuthorityComboBox);

        addToGridBag(fieldsPanel, new JLabel("Owner:"), 0, 1, 0);
        addToGridBag(fieldsPanel, new JLabel("Group:"), 0, 2, 0);
        addToGridBag(fieldsPanel, new JLabel("Other:"), 0, 3, 0);
        addToGridBag(fieldsPanel, new JLabel("Select:"), 1, 0, 0);
        addToGridBag(fieldsPanel, new JLabel("Authority:"), 2, 0, 0);
        addToGridBag(fieldsPanel, ownerComboBox, 1, 1, 0.5);
        addToGridBag(fieldsPanel, groupComboBox, 1, 2, 0.5);
        addToGridBag(fieldsPanel, ownerAuthorityComboBox, 2, 1, 0.5);
        addToGridBag(fieldsPanel, groupAuthorityComboBox, 2, 2, 0.5);
        addToGridBag(fieldsPanel, otherAuthorityComboBox, 2, 3, 0.5);
        return fieldsPanel;
    }

    private int getAuthorityValue(JComboBox comboBox) {
        return AUTHORITIES[comboBox.getSelectedIndex()];
    }

    protected void addToGridBag(JPanel panel, Component component, int x, int y, double weight) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = x;
        gridBagConstraints.gridy = y;
        gridBagConstraints.weightx = weight;
        gridBagConstraints.insets = new Insets(2, 4, 2, 4);
        gridBagConstraints.fill = weight > 0 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        panel.add(component, gridBagConstraints);
    }

    protected void fillAuthorityComboBox(JComboBox comboBox) {
        comboBox.removeAllItems();
        comboBox.addItem("No access");
        comboBox.addItem("Read access");
        comboBox.addItem("Read and execute access");
        comboBox.addItem("Read and write access");
        comboBox.addItem("Read, write and execute access");
    }

    protected void fillOwnerComboBox() {
        XhiveUserListIf userList = getSession().getDatabase().getUserList();

        ownerComboBox.removeAllItems();

        for (Iterator i = userList.iterator(); i.hasNext();) {
            ownerComboBox.addItem(((XhiveUserIf) i.next()).getName());
        }
    }

    protected void fillGroupComboBox() {
        XhiveGroupListIf groupList = getSession().getDatabase().getGroupList();

        groupComboBox.removeAllItems();
        groupComboBox.addItem(NONE);

        for (Iterator i = groupList.iterator(); i.hasNext();) {
            groupComboBox.addItem(((XhiveGroupIf) i.next()).getName());
        }
    }

    private static int getAuthorityIndex(int authorityValue) {
        for (int i = 0; i < AUTHORITIES.length; i++) {
            if (authorityValue == AUTHORITIES[i]) {
                return i;
            }
        }
        return -1;
    }

    protected void setFields() {
        fillOwnerComboBox();
        fillGroupComboBox();

        setWaitText("Please wait while updating authority...");
        ownerAuthorityComboBox.setSelectedIndex(getAuthorityIndex(authority.getOwnerAuthority()));
        groupAuthorityComboBox.setSelectedIndex(getAuthorityIndex(authority.getGroupAuthority()));
        otherAuthorityComboBox.setSelectedIndex(getAuthorityIndex(authority.getOtherAuthority()));
        setComboBoxSelected(ownerComboBox, authority.getOwner() == null ? NONE: authority.getOwner().getName());

        XhiveGroupIf group = authority.getGroup();
        setComboBoxSelected(groupComboBox, group == null ? NONE : group.getName());
    }

    /**
     * Note, this may not select an item always, but then the default of the first item is correct.
     */
    private void setComboBoxSelected(JComboBox comboBox, String value) {
        int i = 0;
        while (i < comboBox.getItemCount()) {
            if (comboBox.getItemAt(i).equals(value)) {
                comboBox.setSelectedIndex(i);
                i = comboBox.getItemCount();
            } else {
                i++;
            }
        }
    }

    protected boolean performAction() {
        XhiveDatabaseIf database = getSession().getDatabase();
        XhiveUserListIf userList = database.getUserList();
        XhiveGroupListIf groupList = database.getGroupList();

        String groupName = (String) groupComboBox.getSelectedItem();

        authority.setOwner(userList.getUser((String) ownerComboBox.getSelectedItem()));
        authority.setGroup(groupName != NONE ? groupList.getGroup(groupName) : null);
        authority.setOwnerAuthority(getAuthorityValue(ownerAuthorityComboBox));
        authority.setGroupAuthority(getAuthorityValue(groupAuthorityComboBox));
        authority.setOtherAuthority(getAuthorityValue(otherAuthorityComboBox));
        return true;
    }
}

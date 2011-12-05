package com.xhive.adminclient.treenodes;

import java.util.Iterator;
import javax.swing.JPopupMenu;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;

import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactionWrapper;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.dialogs.XhiveUserDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveGroupIf;
import com.xhive.core.interfaces.XhiveNamedItemIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveUserIf;
import com.xhive.core.interfaces.XhiveUserListIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive named user tree node.
 *
 */
public class XhiveUserTreeNode extends XhiveExtendedTreeNode {

    private static final String[] EXTRA_PROPERTIES = new String[]{"Groups"};

    public XhiveUserTreeNode(XhiveDatabaseTree databaseTree, XhiveUserIf user) {
        super(databaseTree, user.getName());
    }

    public void update(XhiveSessionIf session) {
        updateValues(getUser(session));
    }

    public String getIconName() {
        return XhiveResourceFactory.USER_ICON;
    }

    private XhiveUserListIf getUserList(XhiveSessionIf session) {
        return session.getDatabase().getUserList();
    }

    private XhiveUserIf getUser(XhiveSessionIf session) {
        return getUserList(session).getUser(getName());
    }

    public String[] getPropertyNames() {
        String[] propertyNames = super.getPropertyNames();
        String[] newNames = new String[propertyNames.length + EXTRA_PROPERTIES.length];
        System.arraycopy(propertyNames, 0, newNames, 0, propertyNames.length);
        System.arraycopy(EXTRA_PROPERTIES, 0, newNames, propertyNames.length, EXTRA_PROPERTIES.length);
        return newNames;
    }

    protected void changeMenuItems(
        XhiveSessionIf session, JPopupMenu popupMenu
    ) {
        super.changeMenuItems(session, popupMenu);
        String sessionUserName = session.getUser().getName();
        boolean isAdminUser = sessionUserName.equals("Administrator");
        boolean isNodeUser = sessionUserName.equals(getName());
        AbstractButton absBut;
        absBut = getMenuItemByActionCommand(
                     popupMenu, "Delete"
                 );
        if (absBut != null) {
            absBut.setEnabled(absBut.isEnabled() && isAdminUser);
        }
        absBut = getMenuItemByActionCommand(
                     popupMenu, "Properties"
                 );
        if (absBut != null) {
            absBut.setEnabled(absBut.isEnabled() && (isAdminUser || isNodeUser));
        }
    }

    private void updateValues(XhiveUserIf user) {
        String groupNames = "";
        Iterator groups = ((XhiveUserIf) user).groups();
        while (groups.hasNext()) {
            groupNames += ", " + ((XhiveGroupIf) groups.next()).getName();
        }
        if (groupNames.length() > 0) {
            groupNames = groupNames.substring(1).trim();
        }
        setPropertyValues(new String[]{user.getName(), groupNames});
    }

    public String getName() {
        return (String)getUserObject();
    }

    public boolean hasProperties() {
        return true;
    }

    protected boolean editProperties(XhiveSessionIf session) {
        XhiveUserIf user = getUser(session);
        if (XhiveUserDialog.showEditUser(session, user) == XhiveDialog.RESULT_OK) {
            setUserObject(user.getName());
            updateValues(user);
            return true;
        }
        return false;
    }

    public void deleteAction(XhiveSessionIf session) {
        XhiveUserListIf userList = getUserList(session);
        userList.removeUser(getUser(session));
    }

    /**
     * Make sure deleting happens in a new session
     */
    public void doDelete(XhiveSessionIf session) {
        super.doDelete(null);
    }
}

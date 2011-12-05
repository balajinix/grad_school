package com.xhive.adminclient.treenodes;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.AbstractButton;
import javax.swing.tree.MutableTreeNode;

import java.awt.Component;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.dialogs.XhiveUserDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveUserIf;
import com.xhive.core.interfaces.XhiveUserListIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive userlist tree node.
 *
 */
public class XhiveUserListTreeNode extends XhiveTableModelTreeNode {

    private static final String[] COLUMN_NAMES = new String[]{"Name"};

    private XhiveAction addUserAction = new XhiveTransactedAction("Add user", 'u',
                                        XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                            protected void xhiveActionPerformed(ActionEvent e) {
                                                XhiveUserIf user = addUser(getSession());
                                                if (user != null) {
                                                    addChild(user);
                                                }
                                            }
                                        };

    public XhiveUserListTreeNode(XhiveDatabaseTree databaseTree, XhiveUserListIf userList) {
        super(databaseTree, null, COLUMN_NAMES);
        setDeletable(false);
        setHasChildren((userList.size() > 0));
    }

    protected Iterator getIterator(XhiveSessionIf session) {
        return session.getDatabase().getUserList().iterator();
    }

    protected void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        for (Iterator i = getIterator(session); i.hasNext();) {
            XhiveUserIf user = (XhiveUserIf)i.next();
            childList.add(createChild(user));
        }
    }

    protected XhiveExtendedTreeNode createChild(Object userObject) {
        return new XhiveUserTreeNode(getDatabaseTree(), (XhiveUserIf) userObject);
    }

    public String[] getPropertyValues() {
        return new String[]{getName(), "Contains all the users within the database"};
    }

    public String getIconName() {
        return XhiveResourceFactory.FOLDER_ICON;
    }

    public String getName() {
        return "Users";
    }

    protected XhiveUserIf addUser(XhiveSessionIf session) {
        return XhiveUserDialog.showAddUser(session);
    }

    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        super.getMenuItems(session, popupMenu, category);
        switch (category) {
        case MENU_CATEGORY_ADD:
            popupMenu.add(new JMenuItem(addUserAction));
            break;
        }
    }

    protected void changeMenuItems(
        XhiveSessionIf session, JPopupMenu popupMenu
    ) {
        super.changeMenuItems(session, popupMenu);
        String sessionUserName = session.getUser().getName();
        boolean isAdminUser = sessionUserName.equals("Administrator");
        AbstractButton absBut = getMenuItemByActionCommand(popupMenu, "Add user");
        if (absBut != null) {
            absBut.setEnabled(absBut.isEnabled() && isAdminUser);
        }
    }
}

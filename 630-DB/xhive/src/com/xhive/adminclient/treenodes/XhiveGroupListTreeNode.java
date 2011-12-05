package com.xhive.adminclient.treenodes;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.tree.MutableTreeNode;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.dialogs.XhiveGroupDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveGroupIf;
import com.xhive.core.interfaces.XhiveGroupListIf;
import com.xhive.core.interfaces.XhiveSessionIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive group list tree node.
 *
 */
public class XhiveGroupListTreeNode extends XhiveTableModelTreeNode {

    private static final String[] COLUMN_NAMES = new String[]{"Name"};

    private XhiveAction addAction = new XhiveTransactedAction("Add group", 'a',
                                    XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                        protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                            XhiveGroupIf group = addGroup(getSession());
                                            if (group != null) {
                                                addChild(group);
                                            }
                                        }
                                    };

    public XhiveGroupListTreeNode(XhiveDatabaseTree databaseTree, XhiveGroupListIf groupList) {
        super(databaseTree, null, COLUMN_NAMES);
        setDeletable(false);
        setHasChildren((groupList.size() > 0));
    }

    protected Iterator getIterator(XhiveSessionIf session) {
        return session.getDatabase().getGroupList().iterator();
    }

    protected void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        for (Iterator i = getIterator(session); i.hasNext();) {
            XhiveGroupIf group = (XhiveGroupIf)i.next();
            childList.add(createChild(group));
        }
    }

    public String[] getPropertyValues() {
        return new String[]{getName(), "Contains all the groups within the database"};
    }

    public String getIconName() {
        return XhiveResourceFactory.FOLDER_ICON;
    }

    public String getName() {
        return "Groups";
    }

    protected XhiveGroupIf addGroup(XhiveSessionIf session) {
        return XhiveGroupDialog.showAddGroup(session);
    }

    protected XhiveExtendedTreeNode createChild(Object userObject) {
        return new XhiveGroupTreeNode(getDatabaseTree(), (XhiveGroupIf) userObject);
    }

    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        super.getMenuItems(session, popupMenu, category);
        switch (category) {
        case MENU_CATEGORY_MISC:
            popupMenu.add(new JMenuItem(addAction));
            break;
        }
    }

    protected void changeMenuItems(
        XhiveSessionIf session, JPopupMenu popupMenu
    ) {
        super.changeMenuItems(session, popupMenu);
        String sessionUserName = session.getUser().getName();
        boolean isAdminUser = sessionUserName.equals("Administrator");
        AbstractButton absBut = getMenuItemByActionCommand(popupMenu, "Add group");
        if (absBut != null) {
            absBut.setEnabled(absBut.isEnabled() && isAdminUser);
        }
    }
}

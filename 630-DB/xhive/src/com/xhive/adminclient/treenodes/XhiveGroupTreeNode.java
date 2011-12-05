package com.xhive.adminclient.treenodes;

import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.dialogs.XhiveDialog;
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
 * Class implementing an Xhive named group tree node.
 *
 *
 */
public class XhiveGroupTreeNode extends XhiveExtendedTreeNode {

    public XhiveGroupTreeNode(XhiveDatabaseTree databaseTree, XhiveGroupIf group) {
        super(databaseTree, group.getName());
        updateValues(group);
    }

    private void updateValues(XhiveGroupIf group) {
        setPropertyValues(new String[]{
                              group.getName()
                          });
    }

    public String getName() {
        return (String) getUserObject();
    }

    /**
     * There is no point to edit a group anymore
     */
    public boolean hasProperties() {
        return false;
    }

    public void update(XhiveSessionIf session) {
        updateValues(getGroup(session));
    }

    public String getIconName() {
        return XhiveResourceFactory.GROUP_ICON;
    }

    private XhiveGroupListIf getGroupList(XhiveSessionIf session) {
        return session.getDatabase().getGroupList();
    }

    private XhiveGroupIf getGroup(XhiveSessionIf session) {
        return getGroupList(session).getGroup(getName());
    }

    public void deleteAction(XhiveSessionIf session) {
        XhiveGroupListIf groupList = getGroupList(session);
        XhiveGroupIf group = groupList.getGroup(getName());
        groupList.removeGroup(group);
    }

    /**
     * Make sure deleting happens in a new session
     */
    public void doDelete(XhiveSessionIf session) {
        super.doDelete(null);
    }
}

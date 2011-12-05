package com.xhive.adminclient.treenodes;

import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;

import java.util.ArrayList;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive database tree node.
 *
 */
public class XhiveQueryResultTreeNode extends XhiveTreeNode {

    public XhiveQueryResultTreeNode(XhiveDatabaseTree databaseTree, String string) {
        super(databaseTree, string);
        setDeletable(false);
    }

    public String getName() {
        return (String) getUserObject();
    }

    public String getIconName() {
        return null;
    }

    public void performDoubleClick() {}


    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {}


    public void deleteAction(XhiveSessionIf session) {}


    public void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {}

}

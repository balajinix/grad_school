package com.xhive.adminclient.treenodes;

import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.core.interfaces.XhiveAuthorityIf;
import com.xhive.core.interfaces.XhiveNamedItemIf;
import com.xhive.core.interfaces.XhiveSessionIf;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.tree.MutableTreeNode;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive named item list tree node.
 *
 */
abstract class XhiveNamedItemListTreeNode extends XhiveTableModelTreeNode {

    private static final String[] COLUMN_NAMES = new String[]{"Name", "Description", "Created", "Last modified"};

    public XhiveNamedItemListTreeNode(XhiveDatabaseTree databaseTree) {
        super(databaseTree, null, COLUMN_NAMES);
        setHasChildren(true);
    }

    protected void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        for (Iterator i = getIterator(session); i.hasNext();) {
            XhiveNamedItemIf namedItem = (XhiveNamedItemIf) i.next();

            XhiveAuthorityIf authority = namedItem.getAuthority();
            if (authority != null && authority.isReadable()) {
                childList.add(createChild(namedItem));
            }
        }
    }

    protected abstract Iterator getIterator(XhiveSessionIf session);
}

package com.xhive.adminclient.treenodes.clustering;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.XhiveType;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.dialogs.clustering.XhiveSegmentDialog;
import com.xhive.adminclient.treenodes.XhiveExtendedTreeNode;
import com.xhive.adminclient.treenodes.XhiveTableModelTreeNode;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSegmentIf;
import com.xhive.core.interfaces.XhiveSessionIf;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.ArrayList;

public class XhiveSegmentsTreeNode extends XhiveTableModelTreeNode {

    private static final String NAME = "Segments";
    private static final String[] PROPERTY_NAMES = {"Name"};
    private static final String[] COLUMN_NAMES = {"Id", "Free space"};

    private XhiveAction addSegmentAction = new XhiveTransactedAction("Add segment", 'a',
                                           XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhiveSegmentIf segment = XhiveSegmentDialog.showCreateSegment(getSession());
                                                   if (segment != null) {
                                                       addChild(segment);
                                                   }
                                               }
                                           };

    public XhiveSegmentsTreeNode(XhiveDatabaseTree databaseTree, XhiveDatabaseIf database) {
        super(databaseTree, null, COLUMN_NAMES);
        setDeletable(false);
        setHasChildren(true);
    }

    public String getIconName() {
        return XhiveResourceFactory.FOLDER_ICON;
    }

    public String[] getPropertyValues() {
        return new String[]{getName(), "Contains information about the segments in the database"};
    }

    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    public String getName() {
        return NAME;
    }

    protected void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        for (Iterator i = session.getDatabase().getSegments(); i.hasNext();) {
            XhiveSegmentIf location = (XhiveSegmentIf)i.next();
            childList.add(createChild(location));
        }
    }

    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        super.getMenuItems(session, popupMenu, category);
        switch (category) {
        case MENU_CATEGORY_ADD:
            popupMenu.add(new JMenuItem(addSegmentAction));
            break;
        }
    }

    protected XhiveExtendedTreeNode createChild(Object userObject) {
        return new XhiveSegmentTreeNode(getDatabaseTree(), (XhiveSegmentIf) userObject);
    }
}

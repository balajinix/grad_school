package com.xhive.adminclient.treenodes.clustering;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.XhiveType;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.dialogs.clustering.XhiveFileDialog;
import com.xhive.adminclient.treenodes.XhiveExtendedTreeNode;
import com.xhive.adminclient.treenodes.XhiveTableModelTreeNode;
import com.xhive.core.interfaces.XhiveFileIf;
import com.xhive.core.interfaces.XhiveSegmentIf;
import com.xhive.core.interfaces.XhiveSessionIf;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.ArrayList;

public class XhiveSegmentTreeNode extends XhiveTableModelTreeNode {

    private static final String[] PROPERTY_NAMES = {"id", "free space"};
    private static final String[] COLUMN_NAMES = {"filename", "maximum size", "current size"};

    private XhiveAction addFileAction = new XhiveTransactedAction("Add file", 'a',
                                        XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                            protected void xhiveActionPerformed(ActionEvent e) {
                                                XhiveFileIf file = XhiveFileDialog.showCreateFile(getSession(), getSegment(getSession()));
                                                if (file != null) {
                                                    addChild(file);
                                                }
                                            }
                                        };

    public XhiveSegmentTreeNode(XhiveDatabaseTree databaseTree, XhiveSegmentIf segment) {
        super(databaseTree, segment.getId(), COLUMN_NAMES);
        setDeletable(!segment.getDatabase().getDefaultSegment().equals(segment));
        setHasChildren(segment.getFiles().hasNext());
        updateValues(segment);
    }

    private void updateValues(XhiveSegmentIf segment) {
        setPropertyValues(new String[] {
                              segment.getId(),
                              String.valueOf(segment.getFreeSpace())
                          });
    }

    public void update(XhiveSessionIf session) {
        XhiveSegmentIf segment = getSegment(session);
        updateValues(segment);
    }

    public String getIconName() {
        return XhiveResourceFactory.FOLDER_ICON;
    }

    public String getName() {
        return (String) getUserObject();
    }

    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    private XhiveSegmentIf getSegment(XhiveSessionIf session) {
        return session.getDatabase().getSegment(getName());
    }

    protected void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        XhiveSegmentIf segment = getSegment(session);
        for (Iterator i = segment.getFiles(); i.hasNext();) {
            XhiveFileIf file = (XhiveFileIf) i.next();
            childList.add(createChild(file));
        }
    }

    protected XhiveExtendedTreeNode createChild(Object userObject) {
        return new XhiveFileTreeNode(getDatabaseTree(), (XhiveFileIf) userObject);
    }

    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        super.getMenuItems(session, popupMenu, category);
        switch (category) {
        case MENU_CATEGORY_ADD:
            popupMenu.add(new JMenuItem(addFileAction));
            break;
        }
    }

    public void deleteAction(XhiveSessionIf session) {
        XhiveSegmentIf segment = getSegment(session);
        segment.delete();
    }

    /**
     * Make sure deleting happens in a new session
     */
    public void doDelete(XhiveSessionIf session) {
        super.doDelete(null);
    }
}

package com.xhive.adminclient.treenodes;

import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.dialogs.XhiveBlobDialog;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveBlobNodeIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.versioning.interfaces.XhiveVersionSpaceIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive blob tree node.
 */
public class XhiveBlobTreeNode extends XhiveLibraryChildTreeNode {

    private XhiveAction makeVersionableAction = new XhiveTransactedAction("Make versionable",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.VERSIONED_DOCUMENT_ICON),
            "Create versionspace for document", 'M') {
                protected void xhiveActionPerformed(ActionEvent e) {
                    XhiveBlobNodeIf blob = getBlob(getSession());
                    blob.makeVersionable();
                    updateValues(blob);
                    // Notify the parent that a new versionspace was created
                    ((XhiveLibraryTreeNode) getParent()).addChild(blob.getXhiveVersion().getVersionSpace(), 0);
                    nodeChanged();
                }
            };

    private XhiveAction versionSpaceJumpAction = new XhiveTransactedAction("Go to version space",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.VERSIONSPACE_ICON),
            "Select version space of this versioned document in treeview", 'G') {
                protected void xhiveActionPerformed(ActionEvent e) {
                    XhiveBlobNodeIf blob = getBlob(getSession());
                    XhiveVersionSpaceIf versionspace = blob.getXhiveVersion().getVersionSpace();
                    XhiveLibraryTreeNode libraryNode = ((XhiveLibraryTreeNode) getParent());
                    int size = libraryNode.getChildCount();
                    for (int i = 0; i < size; i++) {
                        if (libraryNode.getChildAt(i) instanceof XhiveVersionSpaceTreeNode) {
                            XhiveVersionSpaceTreeNode versionSpaceTreeNode = (XhiveVersionSpaceTreeNode) libraryNode.getChildAt(i);
                            if (versionspace.equals(versionSpaceTreeNode.getVersionSpace(getSession()))) {
                                versionSpaceTreeNode.selectVersionNode(getSession(), blob.getXhiveVersion());
                                break;
                            }
                        } else if (! (libraryNode.getChildAt(i) instanceof XhiveCatalogTreeNode)) {
                            // Version spaces are all at the top, so no more matches possible
                            break;
                        }
                    }
                }
            };

    public XhiveBlobTreeNode(XhiveDatabaseTree databaseTree, XhiveBlobNodeIf blobNode) {
        super(databaseTree, blobNode);
    }

    private XhiveBlobNodeIf getBlob(XhiveSessionIf session) {
        return (XhiveBlobNodeIf) getLibraryChild(session);
    }

    public String getIconName() {
        return XhiveResourceFactory.BLOB_ICON;
    }

    public int getColumnCount() {
        // Unused
        return 0;
    }

    public String getColumnName(int columnIndex) {
        // Unused
        return null;
    }

    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        super.getMenuItems(session, popupMenu, category);
        switch (category) {
        case MENU_CATEGORY_VERSIONING:
            boolean versioned = getBlob(session).getXhiveVersion() != null;
            if (!versioned) {
                // Only possible to make versionable if not versioned
                popupMenu.add(new JMenuItem(makeVersionableAction));
            } else {
                popupMenu.add(new JMenuItem(versionSpaceJumpAction));
            }
            break;
        }
    }

    public boolean hasContentsPane() {
        return false;
    }

    protected boolean editLibraryChildProperties(XhiveSessionIf session, XhiveLibraryChildIf libraryChild) {
        return (XhiveBlobDialog.showEditBlobNode(session, (XhiveBlobNodeIf) libraryChild) == XhiveDialog.RESULT_OK);
    }

    public String[] getPropertyNames() {
        String[] standardNames = super.getPropertyNames();
        String[] propertyNames = new String[standardNames.length + 1];
        System.arraycopy(standardNames, 0, propertyNames, 0, standardNames.length);
        propertyNames[standardNames.length] = "Size";
        return propertyNames;
    }

    protected void updateValues(XhiveLibraryChildIf libraryChild) {
        super.updateValues(libraryChild);
        XhiveBlobNodeIf blob = (XhiveBlobNodeIf) libraryChild;
        String[] standardValues = super.getPropertyValues();
        String[] propertyValues = new String[standardValues.length + 1];
        System.arraycopy(standardValues, 0, propertyValues, 0, standardValues.length);
        DecimalFormat df = new DecimalFormat("#,###");
        propertyValues[standardValues.length] = df.format(blob.getSize()) + " Bytes";
        setPropertyValues(propertyValues);
    }
}

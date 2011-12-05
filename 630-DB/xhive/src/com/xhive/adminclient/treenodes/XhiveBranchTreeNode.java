package com.xhive.adminclient.treenodes;

import com.xhive.adminclient.ObjectFinder;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.versioning.interfaces.XhiveBranchIf;
import com.xhive.versioning.interfaces.XhiveVersionIf;
import com.xhive.versioning.interfaces.XhiveVersionSpaceIf;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Iterator;

public class XhiveBranchTreeNode extends XhiveTableModelTreeNode {

    private static final String[] COLUMN_NAMES = new String[]{"Id", "Branch", "Check in date", "Creator",
            "Checked out by", "Labels"};

    private String libraryId;
    private long versionSpaceId;

    public XhiveBranchTreeNode(XhiveDatabaseTree databaseTree, String libraryId, long versionSpaceId,
                               XhiveBranchIf branch) {
        super(databaseTree, branch.getId(), COLUMN_NAMES);
        this.libraryId = libraryId;
        this.versionSpaceId = versionSpaceId;
        setDeletable(false);
        setHasChildren(true);
    }

    protected String getBranchId() {
        return (String) getUserObject();
    }

    protected XhiveVersionSpaceIf getVersionSpace(XhiveSessionIf session) {
        return ObjectFinder.findVersionSpace(session, libraryId, versionSpaceId);
    }

    private XhiveBranchIf getBranch(XhiveSessionIf session) {
        for (Iterator i = getVersionSpace(session).getBranches(); i.hasNext();) {
            XhiveBranchIf branch = (XhiveBranchIf) i.next();
            if (branch.getId().equals(getBranchId())) {
                return branch;
            }
        }
        throw new RuntimeException("Branch with id " + getBranchId() + " can not be found in versionspace " + versionSpaceId);
    }

    protected XhiveExtendedTreeNode createChild(Object userObject) {
        if (userObject instanceof XhiveVersionIf) {
            return new XhiveDocumentVersionTreeNode(getDatabaseTree(), libraryId, (XhiveVersionIf) userObject);
        } else if (userObject instanceof XhiveBranchIf) {
            return new XhiveBranchTreeNode(getDatabaseTree(), libraryId, versionSpaceId, (XhiveBranchIf) userObject);
        }
        return null;
    }

    protected void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        XhiveBranchIf branch = getBranch(session);
        for (Iterator i = branch.getVersions(); i.hasNext();) {
            XhiveVersionIf version = (XhiveVersionIf) i.next();
            if (version.getBranch().equals(branch)) {
                // Only add documents in the same branch in the same
                childList.add(0, createChild(version));
            }
        }
    }

    /**
     * Find a version tree node that matches the version of a node, and
     * if found select that version in the user interface
     * @returns whether a matching node was found
     */
    public boolean selectVersionNode(XhiveSessionIf session, XhiveVersionIf version) {
        expand(session);
        ArrayList branchList = new ArrayList();
        boolean found = false;
        int length = getChildCount();
        int i = 0;
        while ((i < length) && (! found)) {
            if (getChildAt(i) instanceof XhiveBranchTreeNode) {
                // Recurse into it, later (to prevent to much expanding
                branchList.add(getChildAt(i));
            } else if (getChildAt(i) instanceof XhiveDocumentVersionTreeNode) {
                XhiveDocumentVersionTreeNode versionTreeNode = (XhiveDocumentVersionTreeNode) getChildAt(i);
                if (version.equals(versionTreeNode.getVersion(session))) {
                    getDatabaseTree().setSelectionPath(new TreePath(versionTreeNode.getPath()));
                    found = true;
                }
            }
            i++;
        }
        Iterator branches = branchList.iterator();
        while ((! found) && (branches.hasNext())) {
            found = ((XhiveBranchTreeNode) branches.next()).selectVersionNode(session, version);
        }
        return found;
    }

    public String getName() {
        return getBranchId();
    }

    public String getIconName() {
        return XhiveResourceFactory.FOLDER_ICON;
    }
}

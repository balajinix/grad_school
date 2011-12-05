package com.xhive.adminclient.treenodes;

import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.ObjectFinder;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.versioning.interfaces.XhiveVersionSpaceIf;

public class XhiveVersionSpaceTreeNode extends XhiveBranchTreeNode {

    private String name;

    protected XhiveVersionSpaceTreeNode(XhiveDatabaseTree databaseTree, String libraryId,
                                        XhiveVersionSpaceIf versionSpace) {
        super(databaseTree, libraryId, ObjectFinder.getVersionSpaceUniqueId(versionSpace),
              versionSpace.getVersionById("1.1").getBranch());
        this.name = versionSpace.getName();
        setDeletable(true);
    }

    public String getIconName() {
        return XhiveResourceFactory.VERSIONSPACE_ICON;
    }

    public String getExpandedIconName() {
        return getIconName();
    }

    public String getName() {
        return name;
    }

    public boolean confirmDeletion() {
        return XhiveDialog.showConfirmation("Are you sure you want to delete this version-space?");
    }

    public void deleteAction(XhiveSessionIf session) {
        getVersionSpace(session).remove();
    }
}

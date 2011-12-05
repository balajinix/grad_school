package com.xhive.adminclient.treenodes;

import com.xhive.adminclient.ObjectFinder;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.dialogs.XhiveEditXMLDialog;
import com.xhive.adminclient.dialogs.XhiveTextInputDialog;
import com.xhive.adminclient.dialogs.XhiveExportDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveUserIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.versioning.interfaces.XhiveBranchIf;
import com.xhive.versioning.interfaces.XhiveVersionIf;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class XhiveDocumentVersionTreeNode extends XhiveExtendedTreeNode {

    private static final String NONE = "-- None --";
    private static final String[] PROPERTY_NAMES = {"Id", "Branch", "Date", "Creator", "Checked out by", "Labels"};

    private long versionSpaceId;
    private String libraryId;

    private XhiveAction addLabelAction = new XhiveTransactedAction("Add label",
                                         XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                             protected void xhiveActionPerformed(ActionEvent e) {
                                                 XhiveTextInputDialog dialog = new XhiveTextInputDialog("Add label", "");
                                                 if (dialog.execute() == XhiveDialog.RESULT_OK) {
                                                     XhiveVersionIf version = getVersion(getSession());
                                                     version.addLabel(dialog.getTextValue());
                                                     updateValues(version);
                                                     nodeChanged();
                                                 }
                                             }
                                         };

    private XhiveAction removeLabelsAction = new XhiveTransactedAction("Remove all labels",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                protected void xhiveActionPerformed(ActionEvent e) {
                    XhiveVersionIf version = getVersion(getSession());
                    for (Iterator i = version.getLabels(); i.hasNext();) {
                        i.next();
                        i.remove();
                    }
                    updateValues(version);
                    nodeChanged();
                }
            };

    private XhiveAction checkOutAction = new XhiveTransactedAction("Check out/ Edit/ Checkin",
                                         XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                             protected void xhiveActionPerformed(ActionEvent e) {
                                                 VersionEditDialog dialog = new VersionEditDialog(getSession());
                                                 if (dialog.execute() == XhiveDialog.RESULT_CANCEL) {
                                                     getVersion(getSession()).abort();
                                                 }
                                             }
                                         };

    private XhiveAction abortCheckOutAction = new XhiveTransactedAction("Abort check out",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                protected void xhiveActionPerformed(ActionEvent e) {
                    XhiveVersionIf version = getVersion(getSession());
                    version.abort();
                    updateValues(version);
                    nodeChanged();
                }
            };

    private XhiveAction exportAction = new XhiveTransactedAction("Export",
                                       XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EXPORT_ICON)) {
                                           public void xhiveActionPerformed(ActionEvent e) {
                                               XhiveVersionIf version = getVersion(getSession());
                                               XhiveLibraryChildIf libChild = version.getAsLibraryChild();
                                               String name = version.getVersionSpace().getName();
                                               if (name.indexOf(":") != -1) {
                                                   name = "versionedDoc.xml";
                                               }
                                               libChild.setName(name);
                                               XhiveExportDialog.showExportDialog(getSession(), libChild);
                                           }
                                       };

    public XhiveDocumentVersionTreeNode(XhiveDatabaseTree databaseTree, String libraryId, XhiveVersionIf version) {
        super(databaseTree, version.getId());
        this.libraryId = libraryId;
        this.versionSpaceId = ObjectFinder.getVersionSpaceUniqueId(version.getVersionSpace());
        // TODO (ADQ) : This does not work correctly yet because the parents also need to be removed from the tree
        // if they are deleted.
        // Only deletable if this is the head of a branch
        //setDeletable(!version.getNextVersions().hasNext());
        setDeletable(false);
        setHasChildren(hasChildren(version));
        updateValues(version);
    }

    private static boolean hasChildren(XhiveVersionIf thisVersion) {
        for (Iterator<? extends XhiveVersionIf> i = thisVersion.getNextVersions(); i.hasNext();) {
            XhiveVersionIf version = i.next();
            if (!version.getBranch().equals(thisVersion.getBranch())) {
                return true;
            }
        }
        return false;
    }

    public void update(XhiveSessionIf session) {
        updateValues(getVersion(session));
    }

    private void updateValues(XhiveVersionIf version) {
        setPropertyValues(new String[]{
                              version.getId(),
                              version.getBranch().getId(),
                              version.getDate().toString(),
                              version.getCreator() == null ? NONE : version.getCreator().getName(),
                              getUserName(version.getCheckedOutBy()),
                              getVersionLabels(version.getLabels()),
                          });
    }

    public XhiveVersionIf getVersion(XhiveSessionIf session) {
        return ObjectFinder.findVersionSpace(session, libraryId, versionSpaceId).getVersionById(getName());
    }

    public String getIconName() {
        return XhiveResourceFactory.VERSIONED_DOCUMENT_ICON;
    }

    public String getExpandedIconName() {
        return getIconName();
    }

    public String getName() {
        return (String) getUserObject();
    }

    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    private static String getVersionLabels(Iterator i) {
        StringBuffer sb = new StringBuffer();
        while (i.hasNext()) {
            sb.append(i.next());
            if (i.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private static String getUserName(XhiveUserIf user) {
        return (user != null ? user.getName() : "");
    }

    protected XhiveExtendedTreeNode createChild(Object userObject1) {
        if (userObject1 instanceof XhiveVersionIf) {
            return new XhiveDocumentVersionTreeNode(getDatabaseTree(), libraryId, (XhiveVersionIf) userObject1);
        } else if (userObject1 instanceof XhiveBranchIf) {
            return new XhiveBranchTreeNode(getDatabaseTree(), libraryId, versionSpaceId, (XhiveBranchIf) userObject1);
        }
        throw new RuntimeException("Unexpected child type");
    }

    protected void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        XhiveVersionIf thisVersion = getVersion(session);
        for (Iterator<? extends XhiveVersionIf> i = thisVersion.getNextVersions(); i.hasNext();) {
            XhiveVersionIf version = i.next();
            if (!version.getBranch().equals(thisVersion.getBranch())) {
                // Other branch
                childList.add(createChild(version.getBranch()));
            }
        }
    }

    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        super.getMenuItems(session, popupMenu, category);
        switch (category) {
        case MENU_CATEGORY_VERSIONING:
                popupMenu.add(addLabelAction);
            XhiveVersionIf version = getVersion(session);
            if (version.getLabels().hasNext()) {
                popupMenu.add(removeLabelsAction);
            }
            if (version.getCheckedOutBy() == null) {
                // The document is not checked out
                if (version.getBranch().getHeadLibraryChild().getNodeType() == XhiveLibraryChildIf.DOCUMENT_NODE) {
                    // We can only edit documents
                    popupMenu.add(checkOutAction);
                }
            } else {
                // The document is checked out
                popupMenu.add(abortCheckOutAction);
            }
            break;
        case MENU_CATEGORY_MISC:
            popupMenu.add(new JMenuItem(exportAction));
            break;
        }
    }

    public boolean hasSerializableContent() {
        return true;
    }

    public String getAsText(XhiveSessionIf session) throws Exception {
        XhiveVersionIf version = getVersion(session);
        XhiveLibraryChildIf lc = version.getAsLibraryChild();
        if (lc.getNodeType() == XhiveLibraryChildIf.DOCUMENT_NODE) {
            return XhiveTreeNode.getAsText(session.getDatabase().getRoot(), lc);
        } else {
            return lc.getNodeName();
        }
    }

    private XhiveLibraryIf getLibrary(XhiveSessionIf session) {
        return ObjectFinder.findLibrary(session, libraryId);
    }

    public boolean confirmDeletion() {
        return XhiveDialog.showConfirmation("Delete branch?");
    }

    public void deleteAction(XhiveSessionIf session) {
        getVersion(session).getBranch().delete();
    }

    private Object createInsertOperation(XhiveVersionIf newVersion, XhiveVersionIf thisVersion) {
        XhiveExtendedTreeNode parentNode = (XhiveExtendedTreeNode) getParent();
        NodeInsertionOperation op = new NodeInsertionOperation();
        if (newVersion.getBranch().equals(thisVersion.getBranch())) {
            // Same branch add to parent
            op.index = parentNode.getIndex(this) + 1;
            op.targetNode = parentNode;
            op.newNode = createChild(newVersion);
        } else {
            // new branch, is a child
            op.index = 0;
            op.targetNode = this;
            op.newNode = createChild(newVersion.getBranch());
        }
        return op;
    }

    // TODO (ADQ) : If the edit version is part of a library then that should also be updated
    // TODO (ADQ) : if the document was stored without whitespace then the document can be beautified here, and stored
    // again without whitespaces so everything looks better
    class VersionEditDialog extends XhiveEditXMLDialog {

        public VersionEditDialog(XhiveSessionIf session) {
            super(session);
        }

        protected String getText(XhiveSessionIf session) throws Exception {
            XhiveVersionIf version = getVersion(session);
            XhiveDocumentIf doc = (XhiveDocumentIf) version.checkOut();
            return XhiveTreeNode.getAsText(session.getDatabase().getRoot(), doc);
        }

        protected String getDocumentTitle(XhiveSessionIf session) {
            XhiveVersionIf version = getVersion(session);
            return version.getVersionSpace().getName() + "[$" + version.getId() + "]";
        }

        protected XhiveLibraryIf getLibrary(XhiveSessionIf session) {
            return XhiveDocumentVersionTreeNode.this.getLibrary(session);
        }

        // This method is threaded, don't do any gui update's !
        protected Object  storeText(XhiveSessionIf session, final LSParser builder, final LSInput input) {
            XhiveVersionIf thisVersion = getVersion(getSession());
            XhiveVersionIf newVersion = thisVersion.checkIn(builder.parse(input));
            updateValues(thisVersion);
            // Add the new version to the tree
            // This will update the parent's child list
            return createInsertOperation(newVersion, thisVersion);
        }

        // This is called when store text is ready
        protected void storeTextFinished(Object result) {
            NodeInsertionOperation op = (NodeInsertionOperation) result;
            getTreeModel().insertNodeInto(op.newNode, op.targetNode, op.index);
            nodeChanged();
        }
    }

    static class NodeInsertionOperation {
        int index;
        XhiveTreeNode targetNode;
        XhiveTreeNode newNode;
    }
}

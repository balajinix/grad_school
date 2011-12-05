package com.xhive.adminclient.treenodes;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.MutableTreeNode;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.XhiveThreadedTransactedAction;
import com.xhive.adminclient.ObjectFinder;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.dialogs.XhiveDocumentDialog;
import com.xhive.adminclient.dialogs.XhiveImportDialog;
import com.xhive.adminclient.dialogs.XhiveLibraryDialog;
import com.xhive.adminclient.dialogs.XhiveMessageDialog;
import com.xhive.adminclient.dialogs.XhiveDeleteDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.*;
import com.xhive.versioning.interfaces.XhiveVersionSpaceIf;
import org.w3c.dom.Node;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive library tree node.
 *
 */
public class XhiveLibraryTreeNode extends XhiveLibraryChildTreeNode {

    private static final String[] COLUMN_NAMES = XhiveLibraryChildTreeNode.PROPERTY_NAMES;

    private static final String[] EXTRA_PROPERTIES = new String[]{"Child segment id"};

    private static String serializeCopyPath = null;

    private XhiveAction addDocumentAction = new XhiveTransactedAction(
                                                "Add new document", XhiveResourceFactory.getImageIcon(XhiveResourceFactory.DOCUMENT_ICON),
                                                "Add a new document", 'd') {
                                                @Override
                                                public void xhiveActionPerformed(ActionEvent e) {
                                                    XhiveDocumentIf newDocument = XhiveDocumentDialog.showCreateDocument(getSession(), getLibrary(getSession()));
                                                    if (newDocument != null) {
                                                        addChild(newDocument);
                                                    }
                                                }
                                            };

    private XhiveAction addLibraryAction = new XhiveTransactedAction(
                                               "Add library", XhiveResourceFactory.getImageIcon(XhiveResourceFactory.NEW_LIBRARY_ICON),
                                               "Add a new library", 'l') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhiveLibraryIf newLibrary = XhiveLibraryDialog.showCreateLibrary(getSession(), getLibrary(getSession()));
                                                   if (newLibrary != null) {
                                                       addChild(newLibrary);
                                                   }
                                               }
                                           };

    private XhiveAction deleteChildrenAction = new XhiveTransactedAction(
                "Delete children", XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON),
                "Delete (multiple) children of this library", 'C') {
                @Override
                protected void xhiveActionPerformed(ActionEvent e) {
                    if (XhiveDeleteDialog.showDeleteLibraryChildren(getSession(), getLibrary(getSession()))
                            == XhiveDialog.RESULT_OK) {
                        // Of course it is not really new stuff here, but deleted stuff
                        newStuffInLibrary(getSession());
                    }
                }
            };

    private XhiveAction importAction = new XhiveTransactedAction(
                                           "Import", XhiveResourceFactory.getImageIcon(XhiveResourceFactory.IMPORT_ICON),
                                           "Import data", 'P') {
                                           @Override
                                           protected void xhiveActionPerformed(ActionEvent e) {
                                               if (XhiveImportDialog.showImportFiles(getSession(), getLibrary(getSession())) == XhiveDialog.RESULT_OK) {
                                                   newStuffInLibrary(getSession());
                                               }
                                           }
                                       };

    private XhiveAction deserializeAction = new XhiveThreadedTransactedAction("Deserialize",
                                            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.DESERIALIZE_ICON)) {
                                                @Override
                                                protected void xhiveActionPerformed(ActionEvent e) throws IOException {
                                                    InputStream input = getDeserializationInputStream();
                                                    if (input != null) {
                                                        XhiveLibraryIf lib = getLibrary(getSession());
                                                        XhiveLibraryChildIf lc = lib.deserialize(input);
                                                        lib.appendChild(lc);
                                                        newStuffInLibrary(getSession());
                                                    }
                                                }
                                            };

    public static void setSerializeCopyPath(String libraryIdPath) {
        serializeCopyPath = libraryIdPath;
    }

    private XhiveAction serializePasteAction = new XhiveThreadedTransactedAction("Paste (deserialize)",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                @Override
                public void xhiveActionPerformed(ActionEvent e) throws Exception {
                    if (serializeCopyPath != null) {
                        XhiveLibraryIf target = (XhiveLibraryIf) getLibraryChild(getSession());
                        XhiveLibraryChildIf source = ObjectFinder.findLibraryChild(getSession(), serializeCopyPath);
                        File tempFile = getTempFile();
                        String messageBase = source.getFullPath() + " to " + target.getFullPath()
                                             + " (using tempfile '" + tempFile.getAbsolutePath() + "')";
                        String message1 = "Busy copying " + messageBase;
                        String message2 = "Busy pasting " + messageBase;
                        try {
                            AdminMainFrame.setStatus(message1);
                            FileOutputStream fos = new FileOutputStream(tempFile);
                            source.serialize(fos);
                            fos.close();
                            AdminMainFrame.setStatus(message2);
                            FileInputStream fis = new FileInputStream(tempFile);
                            XhiveLibraryChildIf lc = target.deserialize(fis);
                            if (lc.getName() != null) {
                                lc.setName(XhiveImportDialog.getUniqueName(target, lc.getName()));
                            }
                            target.appendChild(lc);
                            fis.close();
                            newStuffInLibrary(getSession());
                        }
                        finally {
                            if (tempFile.exists() && !tempFile.delete()) {
                                XhiveMessageDialog.showException(new Exception("Could not remove '" + tempFile.getAbsolutePath() + "'"));
                            }
                        }
                    }
                }
            };

    private File getTempFile() throws IOException {
        return File.createTempFile("xh_admin_serialize", ".tmp");
    }

    private XhiveAction addLocalCatalogAction = new XhiveTransactedAction(
                "Add local catalog", XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                @Override
                protected void xhiveActionPerformed(ActionEvent e) {
                    XhiveLibraryIf library = getLibrary(getSession());
                    library.addLocalCatalog();
                    // Adds the catalog as the first child
                    addChild(library.getCatalog(), 0);
                }
            };

    private XhiveAction removeLocalCatalogAction = new XhiveTransactedAction(
                "Remove local catalog", XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                @Override
                protected void xhiveActionPerformed(ActionEvent e) {
                    // The catalog is always the first child
                    ((XhiveTableModelTreeNode) getFirstChild()).doDelete(getSession());
                }
            };

    public XhiveLibraryTreeNode(XhiveDatabaseTree databaseTree, XhiveLibraryIf library) {
        super(databaseTree, library, COLUMN_NAMES);
        // The root library is not deletable
        setDeletable((library.getParentNode() != null));
        setHasChildren(hasChildren(library));
    }

    @Override
    public String[] getPropertyNames() {
        String[] propertyNames = super.getPropertyNames();
        String[] newNames = new String[propertyNames.length + EXTRA_PROPERTIES.length];
        System.arraycopy(propertyNames, 0, newNames, 0, propertyNames.length);
        System.arraycopy(EXTRA_PROPERTIES, 0, newNames, propertyNames.length, EXTRA_PROPERTIES.length);
        return newNames;
    }

    @Override
    protected void updateValues(XhiveLibraryChildIf libraryChild) {
        super.updateValues(libraryChild);
        XhiveLibraryIf library = (XhiveLibraryIf) libraryChild;
        String[] oldValues = getPropertyValues();
        String[] newValues = new String[oldValues.length + EXTRA_PROPERTIES.length];
        System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
        String childSegmentId = library.getChildSegmentId();
        if (childSegmentId == null) {
            childSegmentId = "<none>";
        }
        newValues[oldValues.length] = childSegmentId;
        setPropertyValues(newValues);
    }

    static InputStream getDeserializationInputStream() throws IOException {
        return getDeserializationInputStream("Deserialize");
    }

    static InputStream getDeserializationInputStream(String title) throws IOException {
        String path = AdminProperties.getProperty(AdminProperties.SERIALIZE_PATH);
        File directory = path == null ? null : new File(path).getParentFile();
        JFileChooser fileChooser = new JFileChooser(directory);
        if (fileChooser.showDialog(AdminMainFrame.getInstance(), title) ==
                JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            AdminProperties.setProperty(AdminProperties.SERIALIZE_PATH, file.getAbsolutePath());
            InputStream input = new BufferedInputStream(new FileInputStream(file));
            return input;
        } else {
            return null;
        }
    }

    private static boolean hasChildren(XhiveLibraryIf library) {
        if (library.hasLocalCatalog()) {
            return true;
        }
        if (library.getVersionSpaces().hasNext()) {
            return true;
        }
        return (library.getFirstChild() != null);
    }

    private XhiveLibraryIf getLibrary(XhiveSessionIf session) {
        return (XhiveLibraryIf) getLibraryChild(session);
    }

    @Override
    public String getIconName() {
        return XhiveResourceFactory.FOLDER_ICON;
    }

    @Override
    protected boolean editLibraryChildProperties(XhiveSessionIf session, XhiveLibraryChildIf libraryChild) {
        return (XhiveLibraryDialog.showEditLibrary(session, (XhiveLibraryIf) libraryChild) == XhiveDialog.RESULT_OK);
    }

    @Override
    protected XhiveExtendedTreeNode createChild(Object userObject1) {
        XhiveExtendedTreeNode treeNode = null;
        if (userObject1 instanceof XhiveCatalogIf) {
            treeNode = new XhiveCatalogTreeNode(getDatabaseTree(), getLibraryChildPath(), (XhiveCatalogIf) userObject1);
        } else if (userObject1 instanceof XhiveLibraryIf) {
            treeNode = new XhiveLibraryTreeNode(getDatabaseTree(), (XhiveLibraryIf) userObject1);
        } else if (userObject1 instanceof XhiveDocumentIf) {
            treeNode = new XhiveDocumentTreeNode(getDatabaseTree(), (XhiveDocumentIf) userObject1);
        } else if (userObject1 instanceof XhiveBlobNodeIf) {
            treeNode = new XhiveBlobTreeNode(getDatabaseTree(), (XhiveBlobNodeIf) userObject1);
        } else if (userObject1 instanceof XhiveVersionSpaceIf) {
            treeNode = new XhiveVersionSpaceTreeNode(getDatabaseTree(), getLibraryChildPath(),
                       (XhiveVersionSpaceIf) userObject1);
        }
        return treeNode;
    }

    @Override
    protected void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        // If there is a catalog, first insert a catalog node
        XhiveLibraryIf library = (XhiveLibraryIf) getLibraryChild(session);
        if (library.hasLocalCatalog()) {
            childList.add(createChild(library.getCatalog()));
        }
        for (Iterator i = library.getVersionSpaces(); i.hasNext();) {
            XhiveVersionSpaceIf versionSpace = (XhiveVersionSpaceIf) i.next();
            childList.add(createChild(versionSpace));
        }
        // Create list of children
        Node currentNode = library.getFirstChild();
        while (currentNode != null) {
            if (isReadable(currentNode)) {
                childList.add(createChild(currentNode));
            }
            currentNode = currentNode.getNextSibling();
        }
    }

    private static boolean isReadable(Node node) {
        if (node instanceof XhiveLibraryChildIf) {
            return ((XhiveLibraryChildIf) node).getAuthority().isReadable();
        }
        return true;
    }

    @Override
    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        XhiveLibraryIf library = getLibrary(session);
        switch (category) {
        case MENU_CATEGORY_AS:
            if (library.getParentNode() != null) {
                if (library.hasLocalCatalog()) {
                    popupMenu.add(new JMenuItem(removeLocalCatalogAction));
                } else {
                    popupMenu.add(new JMenuItem(addLocalCatalogAction));
                }
            }
            break;
        case MENU_CATEGORY_ADD:
            popupMenu.add(new JMenuItem(addDocumentAction));
            popupMenu.add(new JMenuItem(addLibraryAction));
            break;
        case MENU_CATEGORY_MISC:
            popupMenu.add(new JMenuItem(deleteChildrenAction));
            popupMenu.add(new JMenuItem(importAction));
            break;
        case MENU_CATEGORY_SERIALIZE:
            popupMenu.add(new JMenuItem(deserializeAction));
            break;
        case MENU_CATEGORY_SERIALIZE_CP:
            if (serializeCopyPath != null) {
                popupMenu.add(new JMenuItem(serializePasteAction));
            }
            break;
        }
        // super last because I want import on top
        super.getMenuItems(session, popupMenu, category);
    }

    @Override
    public void getToolBarActions(String tabName, List<Action> actions) {
        super.getToolBarActions(tabName, actions);
        actions.add(importAction);
    }
}

package com.xhive.adminclient.treenodes;

import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.BrowserLauncher;
import com.xhive.adminclient.ObjectFinder;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveThreadedTransactedAction;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.dialogs.XhiveAuthorityDialog;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.dialogs.XhiveExportDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveAuthorityIf;
import com.xhive.core.interfaces.XhiveGroupIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveUserIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import org.w3c.dom.Node;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive named item tree node.
 *
 *
 */
public abstract class XhiveLibraryChildTreeNode extends XhiveTableModelTreeNode {

    private static final String NONE = "-- None --";
    protected static final String[] PROPERTY_NAMES = new String[]{"Name", "Description", "Owner",
            "Owner access", "Group", "Group access",
            "Other access", "Created", "Last modified",
            "Full Path", "Options", "Segment id"};

    private String shortName;

    private XhiveAction xqueryAction = new XhiveAction("Execute XQuery",
                                       XhiveResourceFactory.getImageIcon(XhiveResourceFactory.SEARCH_ICON),
                                       "Execute XQuery", 'Q') {
                                           @Override
                                           protected void xhiveActionPerformed(ActionEvent e) {
                                               AdminMainFrame mainFrame = AdminMainFrame.getInstance();
                                               mainFrame.addXQueryTab(getLibraryChildPath(), false);
                                           }
                                       };

    private XhiveAction updateAction = new XhiveAction("Execute Update XQuery",
                                       XhiveResourceFactory.getImageIcon(XhiveResourceFactory.UPDATE_XQUERY_ICON),
                                       "Execute Update XQuery", 'U') {
                                           @Override
                                           protected void xhiveActionPerformed(ActionEvent e) {
                                               AdminMainFrame mainFrame = AdminMainFrame.getInstance();
                                               mainFrame.addXQueryTab(getLibraryChildPath(), true);
                                           }
                                       };

    private XhiveAction xpointerAction = new XhiveAction("Execute XPointer",
                                         XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                             @Override
                                             public void xhiveActionPerformed(ActionEvent e) {
                                                 AdminMainFrame mainFrame = AdminMainFrame.getInstance();
                                                 mainFrame.addXPointerTab(getLibraryChildPath());
                                             }
                                         };

    private XhiveAction exportAction = new XhiveTransactedAction("Export",
                                       XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EXPORT_ICON)) {
                                           @Override
                                           public void xhiveActionPerformed(ActionEvent e) {
                                               XhiveExportDialog.showExportDialog(getSession(), getLibraryChild(getSession()));
                                           }
                                       };

    private XhiveAction serializeAction = new XhiveThreadedTransactedAction("Serialize",
                                          XhiveResourceFactory.getImageIcon(XhiveResourceFactory.SERIALIZE_ICON)) {
                                              @Override
                                              public void xhiveActionPerformed(ActionEvent e) throws IOException {
                                                  String path = AdminProperties.getProperty(AdminProperties.SERIALIZE_PATH);
                                                  File directory = path == null ? null : new File(path).getParentFile();
                                                  JFileChooser fileChooser = new JFileChooser(directory);
                                                  if (fileChooser.showDialog(AdminMainFrame.getInstance(), "Serialize") ==
                                                          JFileChooser.APPROVE_OPTION) {
                                                      File file = fileChooser.getSelectedFile();
                                                      AdminProperties.setProperty(AdminProperties.SERIALIZE_PATH, file.getAbsolutePath());
                                                      OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
                                                      XhiveLibraryChildIf lc = getLibraryChild(getSession());
                                                      lc.serialize(output);
                                                  }
                                              }
                                          };

    private XhiveAction serializeCopyAction = new XhiveTransactedAction("Copy (serialize)",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                @Override
                public void xhiveActionPerformed(ActionEvent e) {
                    String path = getLibraryChildPath();
                    XhiveLibraryTreeNode.setSerializeCopyPath(path);
                }
            };

    private XhiveAction changeAuthorityAction = new XhiveTransactedAction(
                "Change authority", XhiveResourceFactory.getImageIcon(XhiveResourceFactory.AUTHORITY_ICON),
                "Change the access settings of this object", 'a') {
                @Override
                public void xhiveActionPerformed(ActionEvent e) {
                    XhiveLibraryChildIf libChild = getLibraryChild(getSession());
                    if (XhiveAuthorityDialog.showEditAuthority(getSession(), libChild.getAuthority()) ==
                            XhiveDialog.RESULT_OK) {
                        updateValues(libChild);
                        nodeChanged();
                    }
                }
            };

    private XhiveAction openInBrowser = new XhiveTransactedAction("Open in browser",
                                        XhiveResourceFactory.getImageIcon(XhiveResourceFactory.BROWSER_ICON)) {
                                            @Override
                                            protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                                // TODO (ADQ) : This does not work when the document has a dtd
                                                int port = AdminMainFrame.getHTTPD().getPort();
                                                String link = "http://" + InetAddress.getLocalHost().getHostName().toLowerCase() + ":" + port +
                                                              getFullPath(getLibraryChild(getSession()));
                                                BrowserLauncher.openURL(link);
                                            }
                                        };

    public XhiveLibraryChildTreeNode(XhiveDatabaseTree databaseTree, final XhiveLibraryChildIf libraryChild) {
        this(databaseTree, libraryChild, null);
    }

    public XhiveLibraryChildTreeNode(XhiveDatabaseTree databaseTree, final XhiveLibraryChildIf libraryChild,
                                     String[] columnNames) {
        super(databaseTree, getFullPath(libraryChild), columnNames);
        updateValuesForConstructor(libraryChild);
    }

    @Override
    public void update(XhiveSessionIf session) {
        updateValues(getLibraryChild(session));
    }

    /**
     * Can be overridden to make a lighter update possible when it is known not all properties are necessary.
     */
    protected void updateValuesForConstructor(XhiveLibraryChildIf libraryChild) {
        updateValues(libraryChild);
    }

    protected void updateValues(XhiveLibraryChildIf libraryChild) {
        // The name of the librarychild might be changed by the caller
        setUserObject(getFullPath(libraryChild));
        shortName = getShortName(libraryChild);
        XhiveAuthorityIf authority = libraryChild.getAuthority();
        XhiveGroupIf group = authority.getGroup();
        XhiveUserIf owner = authority.getOwner();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        setPropertyValues(new String[]{getName(),
                                       libraryChild.getDescription(),
                                       owner != null ? owner.getName() : NONE,
                                       getAccessString(authority.getOwnerAuthority()),
                                       group != null ? group.getName() : NONE,
                                       getAccessString(authority.getGroupAuthority()),
                                       getAccessString(authority.getOtherAuthority()),
                                       simpleDateFormat.format(libraryChild.getCreated()),
                                       simpleDateFormat.format(libraryChild.getLastModified()),
                                       getPathPropertyString(libraryChild),
                                       getOptionsString(libraryChild),
                                       libraryChild.getSegment().getId()
                                      });
    }

    private String getPathPropertyString(XhiveLibraryChildIf libChild) {
        String path = libChild.getFullPath();
        String idPath = getFullPath(libChild);
        if (path.equals(idPath)) {
            return path;
        } else {
            return path + " (id path = '" + idPath + "')";
        }
    }

    private static String getShortName(XhiveLibraryChildIf libraryChild) {
        String result = libraryChild.getName();
        if (result == null) {
            return "id:" + libraryChild.getId();
        }
        return result;
    }

    protected String getLibraryChildPath() {
        return (String) getUserObject();
    }

    public XhiveLibraryChildIf getLibraryChild(XhiveSessionIf session) {
        return ObjectFinder.findLibraryChild(session, getLibraryChildPath());
    }

    @Override
    public String getName() {
        return shortName;
    }

    @Override
    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    private static final boolean hasOption(int options, int option) {
        return ((options & option) == option);
    }

    private static String getOptionsString(XhiveLibraryChildIf libraryChild) {
        ArrayList<String> optionsList = new ArrayList<String>();
        int options = libraryChild.getOptions();
        // Though the constants are defined on the library interface, the lock with parent constant
        // is also relevant for documents, and it's no problem to use the documents do not lock with parent
        // constant here
        if (hasOption(options, XhiveLibraryIf.LOCK_WITH_PARENT)) {
            optionsList.add("LOCK_WITH_PARENT");
        }
        if (hasOption(options, XhiveLibraryIf.DOCUMENTS_DO_NOT_LOCK_WITH_PARENT)) {
            optionsList.add("KEY_SORTED");
        }
        return optionsList.toString();
    }

    protected static String getAccessString(int access) {
        String result = "";
        switch (access) {
        case XhiveAuthorityIf.NO_ACCESS:
            result = "-";
            break;
        case XhiveAuthorityIf.READ_ACCESS:
            result = "r";
            break;
        case XhiveAuthorityIf.READ_EXECUTE_ACCESS:
            result = "r, x";
            break;
        case XhiveAuthorityIf.READ_WRITE_ACCESS:
            result = "r, w";
            break;
        case XhiveAuthorityIf.READ_WRITE_EXECUTE_ACCESS:
            result = "r, w, x";
            break;
        }
        return result;
    }

    @Override
    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        super.getMenuItems(session, popupMenu, category);
        switch (category) {
        case MENU_CATEGORY_PROPERTIES:
            popupMenu.add(new JMenuItem(changeAuthorityAction));
            break;
        case MENU_CATEGORY_QUERY:
            popupMenu.add(new JMenuItem(xqueryAction));
            popupMenu.add(new JMenuItem(updateAction));
            popupMenu.add(new JMenuItem(xpointerAction));
            break;
        case MENU_CATEGORY_MISC:
            popupMenu.add(new JMenuItem(exportAction));
            if (AdminMainFrame.getHTTPD() != null && AdminMainFrame.getHTTPD().isRunning()) {
                popupMenu.add(new JMenuItem(openInBrowser));
            }
            break;
        case MENU_CATEGORY_SERIALIZE:
            popupMenu.add(new JMenuItem(serializeAction));
            break;
        case MENU_CATEGORY_SERIALIZE_CP:
            popupMenu.add(new JMenuItem(serializeCopyAction));
            break;
        }
    }

    @Override
    protected void changeMenuItems(XhiveSessionIf session, JPopupMenu popupMenu) {
        super.changeMenuItems(session, popupMenu);
        String sessionUserName = session.getUser().getName();
        XhiveAuthorityIf auth = getLibraryChild(session).getAuthority();
        String libChildOwnerName = auth.getOwner().getName();
        boolean isAdminUser = sessionUserName.equals("Administrator");
        boolean isOwner = sessionUserName.equals(libChildOwnerName);
        AbstractButton absBut = getMenuItemByActionCommand(popupMenu, "Change authority");
        if (absBut != null) {
            absBut.setEnabled(absBut.isEnabled() && (isAdminUser || isOwner));
        }
    }

    @Override
    public boolean hasProperties() {
        return true;
    }

    @Override
    protected final boolean editProperties(XhiveSessionIf session) {
        XhiveLibraryChildIf libraryChild= getLibraryChild(session);
        if (editLibraryChildProperties(session, libraryChild)) {
            // The name of the librarychild might be changed, so update the full path
            setUserObject(getFullPath(libraryChild));
            return true;
        }
        return false;
    }

    protected abstract boolean editLibraryChildProperties(XhiveSessionIf session, XhiveLibraryChildIf libraryChild);

    @Override
    public boolean confirmDeletion() {
        return XhiveDialog.showConfirmation("Are you sure you want to delete this node?");
    }

    @Override
    public void deleteAction(XhiveSessionIf session) {
        Node parentNode = getLibraryChild(session).getOwnerLibrary();
        Node thisNode = getLibraryChild(session);
        parentNode.removeChild(thisNode);
    }

    @Override
    public void getToolBarActions(String tabName, List<Action> actions) {
        super.getToolBarActions(tabName, actions);
        actions.add(xqueryAction);
        actions.add(updateAction);
    }
}

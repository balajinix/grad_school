package com.xhive.adminclient.treenodes;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.AbstractButton;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.tree.MutableTreeNode;

import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.AdminMainFrame;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.dialogs.XhiveDatabasePropertiesDialog;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.treenodes.clustering.XhiveSegmentsTreeNode;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive database tree node.
 *
 *
 */
public class XhiveDatabaseTreeNode extends XhiveTableModelTreeNode {

    private static final String[] COLUMN_NAMES = new String[]{"Name", "Description"};
    private static final String[] EXTRA_PROPERTIES = new String[]{"Temporary data segment"};

    private static final String SERIAL_USERS = "Serialize users and groups";
    private static final String DESERIAL_USERS = "Deserialize users and groups";
    private static final String DESERIAL_ROOT = "Deserialize root library";

    private XhiveAction deserialRootLibAction = new XhiveTransactedAction(
                DESERIAL_ROOT,
                XhiveResourceFactory.getImageIcon(XhiveResourceFactory.DESERIALIZE_ICON)) {
                @Override
                protected void xhiveActionPerformed(ActionEvent e) throws IOException {
                    InputStream input = XhiveLibraryTreeNode.getDeserializationInputStream(
                                            DESERIAL_ROOT
                                        );
                    if (input != null) {
                        XhiveSessionIf session = getSession();
                        XhiveDatabaseIf database = getDatabase(session);
                        database.deserializeRootLibrary(input);
                        // Refresh view.
                        newStuffInLibrary(session);
                    }
                }
            };

    private XhiveAction deserialUsersAction = new XhiveTransactedAction(
                DESERIAL_USERS,
                XhiveResourceFactory.getImageIcon(XhiveResourceFactory.DESERIALIZE_ICON)) {
                @Override
                protected void xhiveActionPerformed(ActionEvent e) throws IOException {
                    InputStream input = XhiveLibraryTreeNode.getDeserializationInputStream(
                                            DESERIAL_USERS
                                        );
                    if (input != null) {
                        XhiveSessionIf session = getSession();
                        XhiveDatabaseIf database = getDatabase(session);
                        database.deserializeUsers(input);
                        // Refresh view.
                        newStuffInLibrary(session);
                    }
                }
            };

    private XhiveAction SerialUsersAction = new XhiveTransactedAction(
                                                SERIAL_USERS,
                                                XhiveResourceFactory.getImageIcon(XhiveResourceFactory.SERIALIZE_ICON)) {
                                                @Override
                                                protected void xhiveActionPerformed(ActionEvent e) throws IOException {

                                                    String path = AdminProperties.getProperty(AdminProperties.SERIALIZE_PATH);
                                                    File directory = path == null ? null : new File(path).getParentFile();
                                                    JFileChooser fileChooser = new JFileChooser(directory);
                                                    if (fileChooser.showDialog(AdminMainFrame.getInstance(), SERIAL_USERS) ==
                                                            JFileChooser.APPROVE_OPTION) {
                                                        File file = fileChooser.getSelectedFile();
                                                        AdminProperties.setProperty(AdminProperties.SERIALIZE_PATH, file.getAbsolutePath());
                                                        OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
                                                        XhiveSessionIf session = getSession();
                                                        XhiveDatabaseIf database = getDatabase(session);
                                                        database.serializeUsers(output);
                                                    }
                                                }
                                            };

    public XhiveDatabaseTreeNode(String databaseName) {
        // Databasetree is set later
        super(null, databaseName, COLUMN_NAMES);
        setDeletable(false);
        // hasChildren needs to be false here, so is leaf returns false. Not sure why, but if this is true
        // the children won't show up in the tree
        setHasChildren(false);
    }

    private String getDatabaseName() {
        return (String) getUserObject();
    }

    private XhiveDatabaseIf getDatabase(XhiveSessionIf session) {
        return session.getDatabase();
    }

    @Override
    public String getIconName() {
        return XhiveResourceFactory.FOLDER_ICON;
    }

    @Override
    public String getName() {
        return getDatabaseName();
    }

    @Override
    public boolean hasProperties() {
        return true;
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
    public void update(XhiveSessionIf session) {
        XhiveDatabaseIf database = session.getDatabase();
        setPropertyValues(new String[]{database.getName(),
                                       database.getTemporaryDataSegment() != null ?
                                       database.getTemporaryDataSegment() :
                                       "<none specified (default)>"});
    }

    @Override
    protected void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        XhiveDatabaseIf database = getDatabase(session);
        if (session.getUser().isAdministrator()) {
            childList.add(new XhiveSegmentsTreeNode(getDatabaseTree(), database));
        }
        childList.add(new XhiveGroupListTreeNode(getDatabaseTree(), database.getGroupList()));
        childList.add(new XhiveUserListTreeNode(getDatabaseTree(), database.getUserList()));
        XhiveLibraryIf root = database.getRoot();
        if (root.getAuthority().isReadable()) {
            childList.add(new XhiveLibraryTreeNode(getDatabaseTree(), root));
        }
    }

    @Override
    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        super.getMenuItems(session, popupMenu, category);
        switch (category) {
        case MENU_CATEGORY_MISC:
            popupMenu.add(new JMenuItem(deserialRootLibAction));
            popupMenu.add(new JMenuItem(deserialUsersAction));
            popupMenu.add(new JMenuItem(SerialUsersAction));
            break;
        }
    }

    @Override
    protected boolean editProperties(XhiveSessionIf session) {
        if (XhiveDatabasePropertiesDialog.showDatabaseProperties(session) == XhiveDialog.RESULT_OK) {
            update(session);
            return true;
        }
        return false;
    }

    @Override
    protected void changeMenuItems(XhiveSessionIf session, JPopupMenu popupMenu) {
        super.changeMenuItems(session, popupMenu);
        String sessionUserName = session.getUser().getName();
        boolean isAdminUser = sessionUserName.equals("Administrator");
        String adminOnlyActions[] = { SERIAL_USERS, DESERIAL_USERS, DESERIAL_ROOT };
        for (int i = 0; i < adminOnlyActions.length; i++) {
            AbstractButton absBut = getMenuItemByActionCommand(popupMenu, adminOnlyActions[i]);
            if (absBut != null) {
                absBut.setEnabled(absBut.isEnabled() && isAdminUser);
            }
        }
    }

}

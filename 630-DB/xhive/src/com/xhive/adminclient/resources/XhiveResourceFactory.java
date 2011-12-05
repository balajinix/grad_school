package com.xhive.adminclient.resources;

import com.xhive.adminclient.dialogs.XhiveMessageDialog;

import javax.swing.*;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Resource Factory.
 *
 *
 */
public class XhiveResourceFactory {

    public final static String EMPTY_ICON = "empty.gif";
    public final static String AUTHORITY_ICON = "authority.gif";
    public final static String BLOB_ICON = "Blob.gif";
    public final static String CLOSE_FILE_ICON = "closeFile.gif";
    public final static String COLLAPSE_ALL_ICON = "collapseall.gif";
    public final static String CONNECT_ICON = "connect.gif";
    public final static String COPY_ICON = "copy.gif";
    public final static String DEBUG_ICON = "debug.gif";
    public final static String DELETE_ICON = "delete.gif";
    public final static String DISCONNECT_ICON = "disconnect.gif";
    public final static String DOCS_ICON = "docs.gif";
    public final static String DOCUMENT_ICON = "Document.gif";
    public final static String EXPAND_ALL_ICON = "expandall.gif";
    public final static String EXPANDED_FOLDER_ICON = "expandedfolder.gif";
    public final static String FOLDER_ICON = "Folder.gif";
    public final static String CATALOG_ICON = "catalog.gif";
    public final static String VERSIONSPACE_ICON = "versionspace.gif";
    public final static String FOLDER_1_UP_ICON = "Folder1up.gif";
    public final static String GC_ICON = "gc.gif";
    public final static String GROUP_ICON = "group.gif";
    public final static String HELP_ICON = "help.gif";
    public final static String IMPORT_ICON = "import.gif";
    public final static String EXPORT_ICON = "export.gif";
    public final static String DESERIALIZE_ICON = "deserialize.gif";
    public final static String SERIALIZE_ICON = "serialize.gif";
    public final static String BROWSER_ICON = "browser.gif";
    public final static String TREEVIEW_ICON = "treeview.gif";
    public final static String TREEVIEW_GRAYEDOUT_ICON = "treeview_grayed.gif";
    public final static String NEW_LIBRARY_ICON = "newlibrary.gif";
    public final static String OPEN_FILE_ICON = "openFile.gif";
    public final static String PARSE_ICON = "parse.gif";
    public final static String EDIT_ICON = "edit.gif";
    public final static String PROPERTIES_ICON = "properties.gif";
    public final static String REFRESH_ICON = "refresh.gif";
    public final static String RERUN_ICON = "rerun.gif";
    public final static String SEARCH_ICON = "search.gif";
    public final static String STOP17_ICON = "stop17.gif";
    public final static String USER_ICON = "user.gif";
    public final static String VALIDATE_ICON = "validate.gif";
    public final static String VERSIONED_DOCUMENT_ICON = "versioneddocument.gif";
    public final static String ASMODEL_ICON = "asmodel.gif";
    public final static String WARNING_ICON = "warning.gif";
    public final static String ERROR2_ICON = "error2.gif";
    public static final String UPDATE_XQUERY_ICON = "pen.gif";

    private static HashMap<String, ImageIcon> iconMap = new HashMap<String, ImageIcon>(50);

    public static ImageIcon getImageIcon(String name) {
        if (name != null) {
            ImageIcon icon = iconMap.get(name);
            if (icon == null) {
                icon = new ImageIcon(XhiveResourceFactory.class.getResource(name));
                iconMap.put(name, icon);
            }
            return icon;
        }
        return null;
    }

    public static InputStream getImageInputStream(String name) {
        InputStream inputstream = null;
        try {
            inputstream = XhiveResourceFactory.class.getResource(name).openStream();
        } catch (java.io.IOException e) {
            XhiveMessageDialog.showException(e);
        }
        return inputstream;
    }

    private static Image getImage(String name) {
        return Toolkit.getDefaultToolkit().getImage(XhiveResourceFactory.class.getResource(name));
    }


    public static Image getImageXSmall() {
        return getImage("x16.gif");
    }

    public static ImageIcon getIconSplash() {
        return getImageIcon("splash.png");
    }

    public static Image getImageSplash() {
        return getImage("splash.png");
    }

    public static ImageIcon getIconConnect() {
        return getImageIcon("connect.gif");
    }

    public static ImageIcon getIconError() {
        return getImageIcon("error.gif");
    }

    public static ImageIcon getIconFolder() {
        return getImageIcon("Folder.gif");
    }

    public static ImageIcon getIconFolder1Up() {
        return getImageIcon("Folder1up.gif");
    }

    public static ImageIcon getIconDocument() {
        return getImageIcon("Document.gif");
    }

    public static ImageIcon getIconStop() {
        return getImageIcon("Stop24.gif");
    }

    public static ImageIcon getIconGarbageCollect() {
        return getImageIcon("gc.gif");
    }

    public static ImageIcon getIconDisconnect() {
        return getImageIcon("disconnect.gif");
    }

    public static ImageIcon getIconSave() {
        return getImageIcon("closeFile.gif");
    }
}

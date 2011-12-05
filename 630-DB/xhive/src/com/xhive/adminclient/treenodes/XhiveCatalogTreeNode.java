package com.xhive.adminclient.treenodes;

import com.xhive.adminclient.ObjectFinder;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.dialogs.XhiveImportDialog;
import com.xhive.adminclient.dialogs.XhiveSelectASModelDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveCatalogIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import org.w3c.dom.as.ASModel;

import javax.swing.*;
import javax.swing.tree.MutableTreeNode;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class XhiveCatalogTreeNode extends XhiveTableModelTreeNode {

    private static final String[] PROPERTY_NAMES = {"Name", "Description", "Default AS model"};
    private static final String[] COLUMN_NAMES = new String[]{"Hint", "Location", "Usage", "Namespace aware"};

    private XhiveAction setDefaultASModel = new XhiveTransactedAction("Set default AS Model",
                                            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                                protected void xhiveActionPerformed(ActionEvent e) {
                                                    XhiveCatalogIf catalog = getCatalog(getSession());
                                                    String asModelHint = XhiveSelectASModelDialog.showSelectASModelDialog(getSession(), getLibrary(getSession()));
                                                    if (asModelHint != null) {
                                                        catalog.setDefaultASModel(catalog.getASModelById(asModelHint, false));
                                                        updateValues(catalog);
                                                        nodeChanged();
                                                    }
                                                }
                                            };

    private XhiveAction clearDefaultASModel = new XhiveTransactedAction("Clear default AS Model",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                protected void xhiveActionPerformed(ActionEvent e) {
                    XhiveCatalogIf catalog = getCatalog(getSession());
                    catalog.setDefaultASModel(null);
                    updateValues(catalog);
                    nodeChanged();
                }
            };

    private XhiveAction importASModels = new XhiveTransactedAction("Import AS Models",
                                         XhiveResourceFactory.getImageIcon(XhiveResourceFactory.IMPORT_ICON), "Import AS Models", 'I') {
                                             protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                                 if (XhiveImportDialog.showImportASModels(getSession(), getLibrary(getSession())) == XhiveDialog.RESULT_OK) {
                                                     refresh(getSession());
                                                     getTreeModel().nodeStructureChanged(XhiveCatalogTreeNode.this);
                                                     fireTableDataChanged();
                                                 }
                                             }
                                         };

    public XhiveCatalogTreeNode(XhiveDatabaseTree databaseTree, String libraryPath, XhiveCatalogIf catalog) {
        super(databaseTree, libraryPath, COLUMN_NAMES);
        setDeletable(!libraryPath.equals("/"));
        setHasChildren(hasChildren(catalog));
        updateValues(catalog);
    }

    private static boolean hasChildren(XhiveCatalogIf catalog) {
        for (Iterator i = catalog.getASModels(false); i.hasNext();) {
            ASModel asModel = (ASModel) i.next();
            if (asModel.getHint() != null) {
                return true;
            }
        }
        return false;
    }

    public void update(XhiveSessionIf session) {
        updateValues(getCatalog(session));
    }

    private void updateValues(XhiveCatalogIf catalog) {
        String defaultASModel = (catalog.getDefaultASModel() != null ? catalog.getDefaultASModel().getHint() : "");
        setPropertyValues(new String[]{"catalog",
                                       "The catalog contains dtd's and schema's",
                                       defaultASModel});
    }

    private XhiveLibraryIf getLibrary(XhiveSessionIf session) {
        return ObjectFinder.findLibrary(session, getLibraryPath());
    }

    private String getLibraryPath() {
        return (String) getUserObject();
    }

    private XhiveCatalogIf getCatalog(XhiveSessionIf session) {
        return getLibrary(session).getCatalog();
    }

    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    public String getIconName() {
        return XhiveResourceFactory.CATALOG_ICON;
    }

    public String getExpandedIconName() {
        return getIconName();
    }

    public String getName() {
        return "Catalog";
    }

    protected void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        XhiveCatalogIf catalog = getCatalog(session);
        for (Iterator i = catalog.getASModels(false); i.hasNext();) {
            ASModel asModel = (ASModel) i.next();
            if (asModel.getHint() != null) {
                childList.add(new XhiveASModelTreeNode(getDatabaseTree(), getLibrary(session), asModel));
            }
        }
    }

    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        super.getMenuItems(session, popupMenu, category);
        switch (category) {
        case MENU_CATEGORY_AS:
            popupMenu.add(new JMenuItem(importASModels));
            popupMenu.add(new JMenuItem(setDefaultASModel));
            if (getCatalog(session).getDefaultASModel() != null) {
                popupMenu.add(new JMenuItem(clearDefaultASModel));
            }
            break;
        }
    }

    public boolean confirmDeletion() {
        return XhiveDialog.showConfirmation("Are you sure you want to delete this catalog");
    }

    public void deleteAction(XhiveSessionIf session) {
        XhiveLibraryIf library = getLibrary(session);
        library.removeLocalCatalog();
    }
}

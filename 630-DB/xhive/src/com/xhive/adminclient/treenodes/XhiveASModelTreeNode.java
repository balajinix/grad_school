package com.xhive.adminclient.treenodes;

import com.xhive.adminclient.ObjectFinder;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.dialogs.XhiveTextInputDialog;
import com.xhive.adminclient.dialogs.XhiveExportASModelDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveCatalogIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import org.w3c.dom.as.ASModel;
import org.w3c.dom.as.DOMASWriter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

public class XhiveASModelTreeNode extends XhiveExtendedTreeNode {

    static final String[] PROPERTY_NAMES = new String[]{"Hint", "Location", "Schema type", "Default AS model"};

    private String libraryPath;
    private String name;

    private XhiveAction changeHint = new XhiveTransactedAction("Change hint",
                                     XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                         protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                             XhiveTextInputDialog dialog = new XhiveTextInputDialog("Change hint", getASModel(getSession()).getHint());
                                             if (dialog.execute() == XhiveDialog.RESULT_OK) {
                                                 ASModel model = getASModel(getSession());
                                                 XhiveCatalogIf catalog = getCatalog(getSession());
                                                 model.setHint(dialog.getTextValue());
                                                 updateValues(model, catalog);
                                                 nodeChanged();
                                             }
                                         }
                                     };

    private XhiveAction changeLocation = new XhiveTransactedAction("Change location",
                                         XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                             protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                                 XhiveTextInputDialog dialog = new XhiveTextInputDialog("Change location", getASModel(getSession()).getLocation());
                                                 if (dialog.execute() == XhiveDialog.RESULT_OK) {
                                                     ASModel model = getASModel(getSession());
                                                     XhiveCatalogIf catalog = getCatalog(getSession());
                                                     model.setLocation(dialog.getTextValue());
                                                     updateValues(model, catalog);
                                                     nodeChanged();
                                                 }
                                             }
                                         };

    private XhiveAction setDefaultASModel = new XhiveTransactedAction("Set default ASModel",
                                            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                                protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                                    ASModel model = getASModel(getSession());
                                                    XhiveCatalogIf catalog = getCatalog(getSession());
                                                    catalog.setDefaultASModel(model);
                                                    updateValues(model, catalog);
                                                    nodeChanged();
                                                }
                                            };

    private XhiveAction unsetDefaultASModel = new XhiveTransactedAction("Unset default ASModel",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                    XhiveCatalogIf catalog = getCatalog(getSession());
                    catalog.setDefaultASModel(null);
                    ASModel model = getASModel(getSession());
                    updateValues(model, catalog);
                    nodeChanged();
                }
            };

    private XhiveAction exportAction = new XhiveTransactedAction("Export",
                                       XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EXPORT_ICON)) {
                                           public void xhiveActionPerformed(ActionEvent e) {
                                               XhiveExportASModelDialog.showExportDialog(getSession(), getASModel(getSession()));
                                           }
                                       };

    public XhiveASModelTreeNode(XhiveDatabaseTree databaseTree, XhiveLibraryIf library, ASModel asModel) {
        super(databaseTree, new Long(asModel.getCatalogId()));
        libraryPath = getFullPath(library);
        updateValues(asModel, library.getCatalog());
    }

    public void update(XhiveSessionIf session) {
        updateValues(getASModel(session), getCatalog(session));
    }

    public void updateValues(ASModel model, XhiveCatalogIf catalog) {
        setPropertyValues(new String[]{model.getHint(), model.getLocation(),
                                       model.representsXMLSchema() ? "XML Schema" : "DTD",
                                       model.equals(catalog.getDefaultASModel()) ? "yes" : "no" });
        name = model.getHint();
    }

    private XhiveCatalogIf getCatalog(XhiveSessionIf session) {
        return ObjectFinder.findLibrary(session, libraryPath).getCatalog();
    }

    private ASModel getASModel(XhiveSessionIf session) {
        return ObjectFinder.findASModel(session, libraryPath, getId());
    }

    public String getName() {
        return (name != null ? name : "no public id");
    }

    private long getId() {
        return ((Long) getUserObject()).longValue();
    }

    public String getIconName() {
        return XhiveResourceFactory.ASMODEL_ICON;
    }

    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    public boolean confirmDeletion() {
        return XhiveDialog.showConfirmation("Are you sure you want to delete this ASModel");
    }

    public void deleteAction(XhiveSessionIf session) {
        getCatalog(session).removeASModel(getASModel(session));
    }

    public boolean hasSerializableContent() {
        return true;
    }

    public String getAsText(XhiveSessionIf session) throws Exception {
        DOMASWriter writer = (DOMASWriter) getCatalog(session).createLSSerializer();
        writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        String encoding = "UTF-8";
        writer.setEncoding(encoding);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writer.writeASModel(os, getASModel(session));
        return os.toString(encoding);
    }

    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        super.getMenuItems(session, popupMenu, category);
        switch (category) {
        case MENU_CATEGORY_AS:
            popupMenu.add(new JMenuItem(changeHint));
            popupMenu.add(new JMenuItem(changeLocation));
            ASModel model = getASModel(session);
            if (! model.representsXMLSchema()) {
                ASModel defaultModel = getCatalog(session).getDefaultASModel();
                if ((defaultModel == null) || (! defaultModel.equals(model))) {
                    popupMenu.add(new JMenuItem(setDefaultASModel));
                } else {
                    popupMenu.add(new JMenuItem(unsetDefaultASModel));
                }
            }
            popupMenu.add(new JMenuItem(exportAction));
            break;
        }
    }

    /**
     * Make sure deleting happens in a new session
     */
    public void doDelete(XhiveSessionIf session) {
        super.doDelete(null);
    }
}

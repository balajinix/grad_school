package com.xhive.adminclient.treenodes;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.ObjectFinder;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.dialogs.XhiveBrowseDocumentDialog;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.dialogs.XhiveDocumentDialog;
import com.xhive.adminclient.dialogs.XhiveEditXMLDialog;
import com.xhive.adminclient.dialogs.XhiveSelectASModelDialog;
import com.xhive.adminclient.panes.XhiveValidationResultPanel;
import com.xhive.adminclient.panes.XhiveXUpdatePanel;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveCatalogIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.versioning.interfaces.XhiveVersionIf;
import com.xhive.versioning.interfaces.XhiveVersionSpaceIf;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Node;
import org.w3c.dom.as.ASModel;
import org.w3c.dom.as.DocumentAS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive document tree node.
 */
public class XhiveDocumentTreeNode extends XhiveLibraryChildTreeNode {

    private static final String[] EXTRA_PROPERTIES = new String[]{"Active AS Model", "Encoding", "Versioned", "Version id"};

    private boolean isVersioned;

    // TODO (ADQ) : When shown in a query result this node should be expandable
    // TODO (ADQ) : Add extra versioning functions?
    // TODO (ADQ) : Add expand document function
    // TODO (ADQ) : If the document is part of a query result not all functions should work the same (make versionable gives a class cast exception)

    private XhiveAction validateAction = new XhiveAction("Validate/ Normalize",
                                         XhiveResourceFactory.getImageIcon(XhiveResourceFactory.VALIDATE_ICON),
                                         "Validate document (DTD) or Normalize with validation (XML Schema)", 'V') {
                                             @Override
                                             protected void xhiveActionPerformed(ActionEvent e) {
                                                 AdminMainFrame mainFrame = AdminMainFrame.getInstance();
                                                 mainFrame.addResultTab(new XhiveValidationResultPanel("Validation result", getLibraryChildPath()));
                                             }
                                         };

    private XhiveAction setASModelAction = new XhiveTransactedAction("Set AS model",
                                           XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhiveDocumentIf document = getDocument(getSession());
                                                   XhiveCatalogIf catalog = document.getOwnerLibrary().getCatalog();
                                                   Object[] asModelIds = XhiveSelectASModelDialog.showSelectASModelsDialog(getSession(),
                                                                         document.getOwnerLibrary());
                                                   // TODO (ADQ) : it is possible to add more schema's
                                                   if (asModelIds != null) {
                                                       ((DocumentAS) document).setActiveASModel(catalog.getASModelById((String) asModelIds[0], false));
                                                       updateValues(document);
                                                       nodeChanged();
                                                   }
                                               }
                                           };

    private XhiveAction addASModelAction = new XhiveTransactedAction("Add AS model",
                                           XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhiveDocumentIf document = getDocument(getSession());
                                                   XhiveCatalogIf catalog = document.getOwnerLibrary().getCatalog();
                                                   Object[] asModelIds = XhiveSelectASModelDialog.showSelectASModelsDialog(getSession(),
                                                                         document.getOwnerLibrary());
                                                   // TODO (ADQ) : it is possible to add more schema's
                                                   if (asModelIds != null) {
                                                       for (int i = 0; i < asModelIds.length; i++) {
                                                           ((DocumentAS) document).addAS(catalog.getASModelById((String) asModelIds[i], false));
                                                       }
                                                       updateValues(document);
                                                       nodeChanged();
                                                   }
                                               }
                                           };

    private XhiveAction clearASModelAction = new XhiveTransactedAction("Clear AS model",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                @Override
                protected void xhiveActionPerformed(ActionEvent e) {
                    XhiveDocumentIf document = getDocument(getSession());
                    // TODO (ADQ) : it is possible to add more schema's, should be able to remove only one.
                    ((DocumentAS) document).setActiveASModel(null);
                    updateValues(document);
                    nodeChanged();
                }
            };

    private XhiveAction makeVersionableAction = new XhiveTransactedAction("Make versionable",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.VERSIONED_DOCUMENT_ICON),
            "Create versionspace for document", 'M') {
                @Override
                protected void xhiveActionPerformed(ActionEvent e) {
                    XhiveDocumentIf document = getDocument(getSession());
                    document.makeVersionable();
                    updateValues(document);
                    // Notify the parent that a new versionspace was created
                    ((XhiveLibraryTreeNode) getParent()).addChild(document.getXhiveVersion().getVersionSpace(), 0);
                    nodeChanged();
                }
            };

    private XhiveAction versionSpaceJumpAction = new XhiveTransactedAction("Go to version space",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.VERSIONSPACE_ICON),
            "Select version space of this versioned document in treeview", 'G') {
                @Override
                protected void xhiveActionPerformed(ActionEvent e) {
                    XhiveDocumentIf document = getDocument(getSession());
                    XhiveVersionSpaceIf versionspace = document.getXhiveVersion().getVersionSpace();
                    XhiveLibraryTreeNode libraryNode = ((XhiveLibraryTreeNode) getParent());
                    int size = libraryNode.getChildCount();
                    for (int i = 0; i < size; i++) {
                        if (libraryNode.getChildAt(i) instanceof XhiveVersionSpaceTreeNode) {
                            XhiveVersionSpaceTreeNode versionSpaceTreeNode = (XhiveVersionSpaceTreeNode) libraryNode.getChildAt(i);
                            if (versionspace.equals(versionSpaceTreeNode.getVersionSpace(getSession()))) {
                                versionSpaceTreeNode.selectVersionNode(getSession(), document.getXhiveVersion());
                                break;
                            }
                        } else if (! (libraryNode.getChildAt(i) instanceof XhiveCatalogTreeNode)) {
                            // Version spaces are all at the top, so no more matches possible
                            break;
                        }
                    }
                }
            };

    private XhiveAction editAction = new XhiveTransactedAction("Edit as text",
                                     XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EDIT_ICON)) {
                                         @Override
                                         protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                             new DocumentEditDialog(getSession()).execute();
                                             nodeChanged();
                                         }

                                         @Override
                                         protected void postTransactionAction() {
                                             if (getSerializableContext() != null) {
                                                 getSerializableContext().fireUpdateEvent();
                                             }
                                         }
                                     };

    private XhiveAction browseDocumentAction = new XhiveTransactedAction("Browse document", 'b',
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.TREEVIEW_ICON)) {
                @Override
                protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                    new XhiveBrowseDocumentDialog(getSession(), getDocument(getSession())).execute();
                    XhiveDocumentIf document = getDocument(getSession());
                    updateValues(document, false);
                    nodeChanged();
                }

                @Override
                protected void postTransactionAction() {
                    if (getSerializableContext() != null) {
                        getSerializableContext().fireUpdateEvent();
                    }
                }
            };

    private XhiveAction executeXUpdateAction = new XhiveAction("Execute XUpdate",
            XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON),
            "Execute an XUpdate query", 'U') {
                @Override
                protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                    AdminMainFrame mainFrame = AdminMainFrame.getInstance();
                    mainFrame.addXUpdateTab(getLibraryChildPath());
                }
            };

    public XhiveDocumentTreeNode(XhiveDatabaseTree databaseTree, XhiveDocumentIf document) {
        super(databaseTree, document);
    }

    @Override
    public String getIconName() {
        return (isVersioned ? XhiveResourceFactory.VERSIONED_DOCUMENT_ICON : XhiveResourceFactory.DOCUMENT_ICON);
    }

    @Override
    protected boolean editLibraryChildProperties(XhiveSessionIf session, XhiveLibraryChildIf libraryChild) {
        return (XhiveDocumentDialog.showEditDocument(session, (XhiveDocumentIf) libraryChild) == XhiveDialog.RESULT_OK);
    }

    protected XhiveLibraryIf getLibrary(XhiveSessionIf session) {
        return getLibraryChild(session).getOwnerLibrary();
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
    protected void updateValuesForConstructor(XhiveLibraryChildIf libraryChild) {
        // Only perform update for common properties, to prevent document content lookups for schema info
        super.updateValues(libraryChild);
        XhiveDocumentIf document = (XhiveDocumentIf) libraryChild;
        isVersioned = document.getXhiveVersion() != null;
    }

    @Override
    protected void updateValues(XhiveLibraryChildIf libraryChild) {
        updateValues(libraryChild, true);
    }

    protected void updateValues(XhiveLibraryChildIf libraryChild, boolean updateContentToo) {
        super.updateValues(libraryChild);
        XhiveDocumentIf document = (XhiveDocumentIf) libraryChild;
        String[] oldValues = getPropertyValues();
        String[] newValues = new String[oldValues.length + EXTRA_PROPERTIES.length];
        System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
        ASModel asModel = ((DocumentAS) document).getActiveASModel();
        if (asModel != null) {
            if (asModel.getHint() != null && !asModel.getHint().equals("")) {
                if (asModel.representsXMLSchema()) {
                    // Get the schema-id instead
                    newValues[oldValues.length] = (String) document.getConfig().getParameter("xhive-schema-ids");
                } else {
                    newValues[oldValues.length] = asModel.getHint();
                }
            } else {
                newValues[oldValues.length] = "NO HINT SPECIFIED";
            }
        } else {
            newValues[oldValues.length] = "";
        }
        String encoding = document.getEncoding();
        newValues[oldValues.length + 1] = encoding != null ? encoding : "[DEFAULT : UTF-8]";
        XhiveVersionIf version = document.getXhiveVersion();
        if (version != null) {
            isVersioned = true;
            newValues[oldValues.length + 2] = "Yes";
            newValues[oldValues.length + 3] = version.getId();
        } else {
            isVersioned = false;
            newValues[oldValues.length + 2] = "No";
            newValues[oldValues.length + 3] = "N/A";
        }
        setPropertyValues(newValues);
        if ((getSerializableContext() != null) && updateContentToo) {
            getSerializableContext().fireUpdateEvent();
        }
    }

    @Override
    public int getColumnCount() {
        // Unused
        return 0;
    }

    @Override
    public String getColumnName(int columnIndex) {
        // Unused
        return null;
    }

    protected XhiveDocumentIf getDocument(XhiveSessionIf session) {
        return ObjectFinder.findDocument(session, getLibraryChildPath());
    }

    @Override
    public boolean hasSerializableContent() {
        return true;
    }

    @Override
    public String getAsText(XhiveSessionIf session) throws Exception {
        return getAsText(getDocument(session).getOwnerLibrary(), getDocument(session));
    }

    @Override
    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        super.getMenuItems(session, popupMenu, category);
        boolean versioned = getDocument(session).getXhiveVersion() != null;
        switch (category) {
        case MENU_CATEGORY_PROPERTIES:
            if (!versioned) {
                popupMenu.add(new JMenuItem(editAction));
            }
            popupMenu.add(new JMenuItem(browseDocumentAction));
            break;
        case MENU_CATEGORY_AS:
            if (((DocumentAS) getDocument(session)).getActiveASModel() != null) {
                // Only possible to validate if there is a asmodel associate with the
                // document
                popupMenu.add(new JMenuItem(validateAction));
                popupMenu.add(new JMenuItem(clearASModelAction));
            }
            popupMenu.add(new JMenuItem(setASModelAction));
            popupMenu.add(new JMenuItem(addASModelAction));
            break;
        case MENU_CATEGORY_VERSIONING:
            if (!versioned) {
                // Only possible to make versionable if not versioned
                popupMenu.add(new JMenuItem(makeVersionableAction));
            } else {
                popupMenu.add(new JMenuItem(versionSpaceJumpAction));
            }
            break;
        case MENU_CATEGORY_QUERY:
            if (XhiveXUpdatePanel.isAvailable()) {
                popupMenu.add(new JMenuItem(executeXUpdateAction));
            }
        }
    }

    protected void documentTreeNodeUpdate(XhiveSessionIf session) {
        updateValues(getLibraryChild(session), false);
    }

    @Override
    public boolean hasContentsPane() {
        return false;
    }

    class DocumentEditDialog extends XhiveEditXMLDialog {

        public DocumentEditDialog(XhiveSessionIf session) {
            super(session);
        }

        @Override
        protected XhiveLibraryIf getLibrary(XhiveSessionIf session) {
            return XhiveDocumentTreeNode.this.getLibrary(getSession());
        }

        @Override
        protected void setParserOptions(LSParser builder) {
            super.setParserOptions(builder);
            XhiveDocumentIf doc = getDocument(getSession());
            if (doc != null) {
                DOMConfiguration builderConfig = builder.getDomConfig();
                DOMConfiguration documentConfig = doc.getConfig();
                if (documentConfig.getParameter("schema-location") != null) {
                    builderConfig.setParameter("schema-type", documentConfig.getParameter("schema-type"));
                    builderConfig.setParameter("schema-location", documentConfig.getParameter("schema-location"));
                    builderConfig.setParameter("validate", documentConfig.getParameter("validate"));
                }
            }
        }

        // This method is threaded, don't do any gui update's !
        @Override
        protected Object storeText(XhiveSessionIf session, final LSParser builder, final LSInput input) {
            Node oldNode = getDocument(getSession());
            builder.parseWithContext(input, oldNode, LSParser.ACTION_REPLACE);
            documentTreeNodeUpdate(getSession());
            return null;
        }

        @Override
        protected void storeTextFinished(Object result) {
            nodeChanged();
        }

        @Override
        protected String getText(XhiveSessionIf session) throws Exception {
            return getAsText(session);
        }

        @Override
        protected String getDocumentTitle(XhiveSessionIf session) {
            return getDocument(session).getFullPath();
        }
    }
}

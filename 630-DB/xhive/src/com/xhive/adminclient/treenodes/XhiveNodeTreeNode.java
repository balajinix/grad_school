package com.xhive.adminclient.treenodes;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.dialogs.XhiveEditXMLDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveNodeIf;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.AbstractButton;
import javax.swing.tree.MutableTreeNode;

import java.awt.event.ActionEvent;

import java.util.ArrayList;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive Node (document-content) tree node.
 */
public class XhiveNodeTreeNode extends XhiveTreeNode {

    private static final String ELEMENT_COLOR = "#A52A2A"; // Same as in MSIE?
    private static final String TEXT_COLOR = "black";
    private static final String MARKUP_COLOR = "blue";

    private String name;
    private String toolTipText;

    /**
     * Since this has to use a databasetreesession, only initialize when actually used
     */
    private XhiveAction instantiatedEditAction = null;
    private XhiveAction getEditAction() {
        if (instantiatedEditAction == null) {
            instantiatedEditAction = new XhiveTransactedAction(getDatabaseTreeSession(), "Edit as text",
                                     XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EMPTY_ICON)) {
                                         protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                             new NodeEditDialog(getSession()).execute();
                                             refresh(getSession());
                                         }
                                     };
        }
        return instantiatedEditAction;
    }

    public XhiveNodeTreeNode(XhiveDatabaseTree databaseTree, Node node) {
        super(databaseTree, node);
        setName((XhiveNodeIf) node);
        setHasChildren(node.getFirstChild() != null);
        initTooltipText();
        setDeletable(!isTemporary());
    }

    public String getToolTipText() {
        return toolTipText;
    }

    private void initTooltipText() {
        XhiveNodeIf node = (XhiveNodeIf) getUserObject();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            StringBuffer stringBuffer = new StringBuffer();

            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                int length = attributes.getLength();
                for (int i = 0; i < length; i++) {
                    Attr attribute = (Attr) attributes.item(i);
                    TypeInfo typeInfo = ((XhiveNodeIf) attribute).getSchemaTypeInfo();
                    if (typeInfo != null && typeInfo.getTypeName() != null) {
                        stringBuffer.append("<B>Attribute Type for ");
                        stringBuffer.append(attribute.getName());
                        stringBuffer.append("</B>=<FONT COLOR='blue'>");
                        stringBuffer.append(typeInfo.getTypeNamespace());
                        stringBuffer.append("/");
                        stringBuffer.append(typeInfo.getTypeName());
                        stringBuffer.append("</FONT>");
                        if (i < length - 1) {
                            stringBuffer.append("<BR>");
                        }
                    }
                }
            }
            TypeInfo typeInfo = node.getSchemaTypeInfo();
            if (typeInfo != null && typeInfo.getTypeName() != null) {
                if (stringBuffer.length() > 0) {
                    stringBuffer.append("<BR>");
                }
                stringBuffer.append("<B>Element Type</B>=<FONT COLOR='blue'>");
                stringBuffer.append(typeInfo.getTypeNamespace());
                stringBuffer.append("/");
                stringBuffer.append(typeInfo.getTypeName());
                stringBuffer.append("</FONT>");
            }
            String sbString = stringBuffer.toString();
            if (!sbString.equals("")) {
                sbString = "<HTML>" + sbString + "</HTML>";
                toolTipText = sbString;
            }
        } else {
            toolTipText = null;
        }
    }

    private void setName(XhiveNodeIf node) {
        StringBuffer nameBuffer = new StringBuffer();

        switch (node.getNodeType()) {
        case Node.ELEMENT_NODE:
            nameBuffer.append("<HTML>");
            nameBuffer.append("<FONT COLOR='" + MARKUP_COLOR + "'>&lt;</FONT><FONT COLOR='" + ELEMENT_COLOR + "'>");
            nameBuffer.append(node.getNodeName());
            nameBuffer.append("</FONT>");
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                nameBuffer.append(' ');
                for (int i = 0; i < attributes.getLength(); i++) {
                    Attr attribute = (Attr) attributes.item(i);
                    nameBuffer.append((i > 0 ? " " : ""));
                    nameBuffer.append("<FONT COLOR='" + ELEMENT_COLOR + "'>");
                    nameBuffer.append(attribute.getName());
                    nameBuffer.append("</FONT><FONT COLOR='" + MARKUP_COLOR + "'>=\"</FONT><FONT COLOR='" + TEXT_COLOR + "'>");
                    nameBuffer.append(attribute.getValue());
                    nameBuffer.append("</FONT><FONT COLOR='" + MARKUP_COLOR + "'>\"</FONT>");
                }
            }
            nameBuffer.append("<FONT COLOR='blue'>");
            if (!node.hasChildNodes()) {
                nameBuffer.append('/');
            }
            nameBuffer.append("&gt;");
            nameBuffer.append("</FONT></HTML>");
            break;
        case Node.ENTITY_REFERENCE_NODE:
            nameBuffer.append('&');
            nameBuffer.append(node.getNodeName());
            nameBuffer.append(';');
            break;
        case Node.COMMENT_NODE:
            nameBuffer.append("<!--");
            nameBuffer.append(node.getNodeValue());
            nameBuffer.append("-->");
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            nameBuffer.append("<?");
            nameBuffer.append(node.getNodeValue());
            nameBuffer.append("?>");
            break;
        case Node.DOCUMENT_TYPE_NODE:
            nameBuffer.append("<!DOCTYPE ");
            nameBuffer.append(node.getNodeName());
            nameBuffer.append(">");
            break;
        default:
            nameBuffer.append(node.getNodeValue());
            break;
        }
        name = nameBuffer.toString();
    }

    private boolean isTemporary() {
        // Temporary documents do not have owner libraries, so check this here.
        return (getLibrary() == null);
    }

    private XhiveNodeIf getNode() {
        return (XhiveNodeIf) getUserObject();
    }

    protected XhiveDocumentIf getDocument() {
        return (XhiveDocumentIf) getNode().getOwnerDocument();
    }

    protected XhiveLibraryIf getLibrary() {
        return getDocument().getOwnerLibrary();
    }

    public String getName() {
        return name;
    }

    protected void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category) {
        switch (category) {
        case MENU_CATEGORY_PROPERTIES:
            // The document is only editable if it is not part of a tempory document.

            if (!isTemporary()) {
                popupMenu.add(new JMenuItem(getEditAction()));
            }
            break;
        }
    }

    protected void changeMenuItems(
        XhiveSessionIf session, JPopupMenu popupMenu
    ) {
        super.changeMenuItems(session, popupMenu);
        AbstractButton absBut;
        absBut = getMenuItemByActionCommand(
                     popupMenu, "Delete"
                 );
        if (absBut != null) {
            absBut.setEnabled(absBut.isEnabled() && !session.getReadOnlyMode());
        }
    }

    public void performDoubleClick() {
        if (isLeaf() && isTemporary()) {
            return;
        }
        getEditAction().actionPerformed(null);
    }

    public void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        // Create list of children
        Node currentNode = getNode().getFirstChild();
        while (currentNode != null) {
            childList.add(new XhiveNodeTreeNode(getDatabaseTree(), currentNode));
            currentNode = currentNode.getNextSibling();
        }
    }

    public void deleteAction(XhiveSessionIf session) {
        Node parentNode = getNode().getParentNode();
        if (parentNode != null) {
            parentNode.removeChild(getNode());
        }
    }

    public boolean openTransactionRequired() {
        return true;
    }

    class NodeEditDialog extends XhiveEditXMLDialog {

        public NodeEditDialog(XhiveSessionIf session) {
            super(session);
        }

        protected String getText(XhiveSessionIf session) throws Exception {
            return getAsText(getLibrary(session), getNode());
        }

        protected String getDocumentTitle(XhiveSessionIf session) {
            return getDocument().getFullPath() + "- " + getNode().getNodeName();
        }

        protected XhiveLibraryIf getLibrary(XhiveSessionIf session) {
            return XhiveNodeTreeNode.this.getLibrary();
        }

        // This method is threaded, don't do any gui update's !
        protected Object storeText(XhiveSessionIf session, final LSParser builder, final LSInput input) {
            Node result;
            Node oldNode = getNode();
            Node previousSibling = oldNode.getPreviousSibling();
            if (previousSibling == null) {
                Node parent = oldNode.getParentNode();
                builder.parseWithContext(input, oldNode, LSParser.ACTION_REPLACE);
                result = parent.getFirstChild();
            } else {
                builder.parseWithContext(input, oldNode, LSParser.ACTION_REPLACE);
                result = previousSibling.getNextSibling();
            }
            // This updates the text value of the tree node
            XhiveNodeTreeNode.this.setName((XhiveNodeIf) result);
            return result;
        }

        // This is called when store text is ready
        protected void storeTextFinished(Object result) {
            setUserObject(result);
            collapse();
            nodeChanged();
        }
    }
}

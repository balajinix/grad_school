package com.xhive.adminclient.treenodes;

import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.dialogs.XhiveMessageDialog;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.core.interfaces.XhiveSessionIf;

public class XhiveDocumentRootTreeNode extends XhiveNodeTreeNode {

    private String name;

    public XhiveDocumentRootTreeNode(XhiveDocumentIf document) {
        super(null, document);
        name = document.getName();
    }

    protected XhiveDocumentIf getDocument() {
        return (XhiveDocumentIf) getUserObject();
    }

    public String getIconName() {
        return XhiveResourceFactory.DOCUMENT_ICON;
    }

    public String toString() {
        return name;
    }

    public void doDelete(XhiveSessionIf session) {
        XhiveMessageDialog.showErrorMessage("You cannot remove root-nodes here");
    }

}

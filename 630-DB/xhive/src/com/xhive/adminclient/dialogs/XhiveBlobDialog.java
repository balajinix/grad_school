package com.xhive.adminclient.dialogs;

import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveBlobNodeIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for editing the properties of a document.
 */
public class XhiveBlobDialog extends XhiveLibraryChildDialog {

    private XhiveBlobNodeIf blobNode = null;

    public static int showEditBlobNode(XhiveSessionIf session, XhiveBlobNodeIf blobNode) {
        XhiveBlobDialog dialog = new XhiveBlobDialog(session, blobNode);
        return dialog.execute();
    }

    public XhiveBlobDialog(XhiveSessionIf session, XhiveBlobNodeIf blobNode) {
        super("Blob properties", session);
        this.blobNode = blobNode;
    }

    protected void setFields() {
        setWaitText("Please wait while updating blob properties...");
        nameField.setText(blobNode.getName());
        descriptionField.setText(blobNode.getDescription());
    }

    protected boolean performAction() {
        blobNode.setName(nameField.getText());
        blobNode.setDescription(descriptionField.getText());
        return true;
    }
}

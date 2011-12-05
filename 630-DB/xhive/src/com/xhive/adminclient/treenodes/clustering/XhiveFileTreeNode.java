package com.xhive.adminclient.treenodes.clustering;

import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveType;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.dialogs.clustering.XhiveFileDialog;
import com.xhive.adminclient.treenodes.XhiveExtendedTreeNode;
import com.xhive.core.interfaces.XhiveFileIf;
import com.xhive.core.interfaces.XhiveSegmentIf;
import com.xhive.core.interfaces.XhiveSessionIf;

import java.util.Iterator;

public class XhiveFileTreeNode extends XhiveExtendedTreeNode {

    private static final String[] PROPERTY_NAMES = {"filename", "maximum size", "current size"};

    private String fileId;
    private String segmentId;

    public XhiveFileTreeNode(XhiveDatabaseTree databaseTree, XhiveFileIf file) {
        super(databaseTree, file.getFileName());
        XhiveSegmentIf segment = file.getSegment();
        segmentId = segment.getId();
        fileId = getFileIdentifier(file);
        setDeletable(false);
        updateValues(file);
    }

    public boolean isDeletable() {
        return false;
    }

    private static String getFileIdentifier(XhiveFileIf file) {
        return file.getFileName();
    }

    public void update(XhiveSessionIf session) {
        updateValues(getFile(session));
    }

    private void updateValues(XhiveFileIf file) {
        long max = file.getMaxFileSize();
        String maxSize = max == 0L ? "0 (unlimited)" : XhiveFileDialog.convertSizeToString(max);
        setPropertyValues(new String[]{
                              file.getFileName(),
                              maxSize,
                              String.valueOf(file.getCurrentFileSize())
                          });
    }

    public String getIconName() {
        return XhiveResourceFactory.DOCUMENT_ICON;
    }

    public String getName() {
        return (String) getUserObject();
    }

    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    private XhiveFileIf getFile(XhiveSessionIf session) {
        XhiveSegmentIf segment = session.getDatabase().getSegment(segmentId);
        for (Iterator i = segment.getFiles(); i.hasNext();) {
            XhiveFileIf file = (XhiveFileIf) i.next();
            if (getFileIdentifier(file).equals(fileId)) {
                return file;
            }
        }
        throw new RuntimeException("File with name " + fileId + " can not be found in segment "+segmentId);
    }

    protected boolean editProperties(XhiveSessionIf session) {
        return (XhiveFileDialog.showEditFile(session, getFile(session)) == XhiveDialog.RESULT_OK);
    }

    public boolean hasProperties() {
        return true;
    }

}

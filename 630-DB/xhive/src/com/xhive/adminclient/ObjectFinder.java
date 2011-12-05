package com.xhive.adminclient;

import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.versioning.interfaces.XhiveVersionSpaceIf;
import com.xhive.versioning.interfaces.XhiveBranchIf;

import org.w3c.dom.as.ASModel;

import java.util.Iterator;

public class ObjectFinder {

    public static XhiveLibraryIf findLibrary(XhiveSessionIf session, String path) {
        XhiveLibraryChildIf libraryChild = findLibraryChild(session, path);
        if (!(libraryChild instanceof XhiveLibraryIf)) {
            throw new RuntimeException("Library child with path " + path + " is not a library");
        }
        return (XhiveLibraryIf) libraryChild;
    }

    public static XhiveDocumentIf findDocument(XhiveSessionIf session, String path) {
        XhiveLibraryChildIf libraryChild = findLibraryChild(session, path);
        if (!(libraryChild instanceof XhiveDocumentIf)) {
            throw new RuntimeException("Library child with path " + path + " is not a document");
        }
        return (XhiveDocumentIf) libraryChild;
    }

    public static XhiveLibraryChildIf findLibraryChild(XhiveSessionIf session, String path) {
        XhiveLibraryChildIf libraryChild = session.getDatabase().getRoot().getByPath(path);
        if (libraryChild == null) {
            throw new RuntimeException("Library child with path " + path + " can not be found");
        }
        return libraryChild;
    }

    /**
     * The unique id of the head document of the head-branch is used to identify the correct version space
     * now.
     */
    public static XhiveVersionSpaceIf findVersionSpace(XhiveSessionIf session, String libraryId, long id) {
        for (Iterator i = findLibrary(session, libraryId).getVersionSpaces(); i.hasNext();) {
            XhiveVersionSpaceIf vs = (XhiveVersionSpaceIf) i.next();
            if (getVersionSpaceUniqueId(vs) == id) {
                return vs;
            }
        }
        throw new RuntimeException("Versionspace with name " + id + " can not be found");
    }

    public static long getVersionSpaceUniqueId(XhiveVersionSpaceIf versionSpace) {
        for (Iterator<? extends XhiveBranchIf> i = versionSpace.getBranches(); i.hasNext();) {
            XhiveBranchIf branch = i.next();
            if (branch.getId().equals("1")) {
                return branch.getHeadLibraryChild().getId();
            }
        }
        throw new RuntimeException("Internal error, there should always be a branch with id '1'");
    }

    public static ASModel findASModel(XhiveSessionIf session, String libraryId, long id) {
        ASModel result = findLibrary(session, libraryId).getCatalog().getASModelByCatalogId(id);
        if (result == null) {
            throw new RuntimeException("ASModel child with id " + id + " can not be found in catalog of library "+libraryId);
        }
        return result;
    }
}

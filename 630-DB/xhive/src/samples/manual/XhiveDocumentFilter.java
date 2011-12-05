package samples.manual;

import com.xhive.dom.interfaces.XhiveLibraryIf;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This filter can be used in combination with a dom.traversal.TreeWalker to find
 * all documents in a tree. By using a treewalker, all descending nodes of
 * a document are rejected (skipped) during traversal. This is the fastest way
 * to find all documents in a library.
 *
 * Note that documents can be found on different levels in the subtree when libraries are nested.
 *
 */
public class XhiveDocumentFilter implements NodeFilter {

    public XhiveDocumentFilter() {}


    public short acceptNode(Node n) {
        if (n instanceof XhiveLibraryIf) {
            return FILTER_SKIP;
        }
        if (n instanceof Document) {
            return FILTER_ACCEPT;
        }
        return FILTER_REJECT;
    }
}

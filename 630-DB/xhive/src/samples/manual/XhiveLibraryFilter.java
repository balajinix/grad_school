package samples.manual;

import com.xhive.dom.interfaces.XhiveLibraryIf;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This filter can be used in combination with a dom.traversal.TreeWalker to find
 * all libraries in a tree. By using a treewalker, only library-nodes are travesed as
 * all other nodes are rejected rejected (skipped) during traversal. This is the fastest way
 * to find all descending libraries of a library.
 *
 */
public class XhiveLibraryFilter implements NodeFilter {

    public XhiveLibraryFilter() {}


    public short acceptNode(Node n) {
        if (n instanceof XhiveLibraryIf) {
            return FILTER_ACCEPT;
        }
        return FILTER_REJECT;
    }
}

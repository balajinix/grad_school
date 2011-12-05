package samples.manual;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This filter is used by class DomTraversal
 *
 */
public class SampleFilter implements NodeFilter {

    public short acceptNode(Node n) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            Element elem = (Element) n;

            if (elem.getNodeName().equals("title")) {
                return FILTER_SKIP;
            }

            if (elem.getNodeName().equals("list")) {
                return FILTER_REJECT;
            }
        }

        return FILTER_ACCEPT;
    }
}

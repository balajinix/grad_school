package samples.manual;

import com.xhive.index.interfaces.XhiveIndexNodeFilterIf;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Vector;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This filter index is used by class Indexing
 *
 */
public class SampleIndexFilter implements XhiveIndexNodeFilterIf {

    Vector v = new Vector();

    public SampleIndexFilter() {
        super();
    }

    public short acceptNode(Node n) {
        // add chapters to the index which have an even number
        if (n.getNodeName().equals("chapter") && (Integer.parseInt(((Element) n).getAttribute("number")) % 2 == 0)) {
            return FILTER_ACCEPT;
        }

        return FILTER_SKIP;
    }

    public Vector getKeys(Node n) {
        // use the titles of the chapters as key for this index
        v.removeAllElements();
        v.addElement(n.getFirstChild().getFirstChild().getNodeValue());
        return v;
    }
}

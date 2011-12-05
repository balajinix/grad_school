package samples.manual;

import com.xhive.index.interfaces.XhiveIndexNodeFilterIf;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.util.Vector;
import java.util.StringTokenizer;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This filter index is used by class XQueryExtensions
 *
 * It creates an index on all the words of all paragraphs.
 * Note: Normally, one would *not* use a context conditioned index for this kind of index
 * (but instead a full text index)
 *
 */
public class XQueryExtensionsIndexFilter implements XhiveIndexNodeFilterIf {

    Vector v = new Vector();

    public XQueryExtensionsIndexFilter() {
        super();
    }

    public short acceptNode(Node n) {
        // add chapters to the index which have an even number
        if (n.getNodeName().equals("para")) {
            return FILTER_ACCEPT;
        }
        return FILTER_SKIP;
    }

    /**
     * Use the words of the first text-nodes of this paragraph
     */
    public Vector getKeys(Node n) {
        v.removeAllElements();

        Node child = n.getFirstChild();
        while (child != null) {
            if (child instanceof Text) {
                StringTokenizer words = new StringTokenizer(child.getNodeValue(), " ,.!;:\t\r\n");
                while (words.hasMoreTokens()) {
                    v.addElement(words.nextToken());
                }
            }
            child = child.getNextSibling();
        }

        return v;
    }
}

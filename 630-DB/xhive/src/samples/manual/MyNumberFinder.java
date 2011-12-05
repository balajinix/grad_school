package samples.manual;

import com.xhive.dom.interfaces.XhiveFunctionIf;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This function object is used in sample FunctionObjects.java
 *
 */

public class MyNumberFinder implements XhiveFunctionIf {

    public MyNumberFinder() {}


    public boolean test(Node node) {
        // we only want to process nodes which have an attribute with name "number"

        return node.getNodeType() == Node.ELEMENT_NODE && ((Element) node).hasAttribute("number");
    }

    public void process(Node node) {
        // do something with the Node

        String indentation = "";
        String elementName = ((Element) node).getTagName();
        if (elementName.equals("article")) {
            indentation = "  ";
        }

        System.out.println(indentation + elementName + " " + ((Element) node).getAttribute("number"));
    }

    public boolean isDone(Node node) {
        return false;
    }
}

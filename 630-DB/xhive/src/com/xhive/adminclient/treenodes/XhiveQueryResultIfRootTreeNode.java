package com.xhive.adminclient.treenodes;

import com.xhive.core.interfaces.XhiveLocationIteratorIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLocationIf;
import com.xhive.dom.interfaces.XhivePointIf;
import com.xhive.dom.interfaces.XhiveRangeIf;
import com.xhive.query.interfaces.XhiveQueryResultIf;

import org.w3c.dom.Node;

import java.util.ArrayList;

import javax.swing.tree.MutableTreeNode;

public class XhiveQueryResultIfRootTreeNode extends XhiveQueryResultTreeNode {

    private XhiveQueryResultIf queryResult;

    public XhiveQueryResultIfRootTreeNode(XhiveQueryResultIf queryResult) {
        super(null, "Query result");
        this.queryResult = queryResult;
    }

    public void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
        short resultType = queryResult.getType();
        switch (resultType) {
        case XhiveQueryResultIf.LOCATIONSET:
            XhiveLocationIteratorIf locationIterator = queryResult.getLocationSetValue();
            while (locationIterator.hasNext()) {
                XhiveLocationIf location = locationIterator.next();
                Node node = null;
                short type = location.getLocationType();
                switch (type) {
                case XhiveLocationIf.POINT:
                    node = ((XhivePointIf) location).getContainerNode();
                    break;
                case XhiveLocationIf.RANGE:
                    node = ((XhiveRangeIf) location).getStartPoint().getContainerNode();
                    break;
                default:
                    node = (Node) location;
                }
                childList.add(new XhiveNodeTreeNode(getDatabaseTree(), node));
            }
            break;
        case XhiveQueryResultIf.BOOLEAN:
            childList.add(new XhiveQueryResultTreeNode(getDatabaseTree(), String.valueOf(queryResult.getBooleanValue())));
            break;
        case XhiveQueryResultIf.NUMBER:
            childList.add(new XhiveQueryResultTreeNode(getDatabaseTree(), String.valueOf(queryResult.getNumberValue())));
            break;
        case XhiveQueryResultIf.STRING:
            childList.add(new XhiveQueryResultTreeNode(getDatabaseTree(), queryResult.getStringValue()));
            break;
        }
    }
}

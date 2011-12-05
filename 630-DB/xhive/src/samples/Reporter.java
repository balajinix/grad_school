package samples;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveAuthorityIf;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveGroupIf;
import com.xhive.core.interfaces.XhiveLocationIteratorIf;
import com.xhive.core.interfaces.XhiveNodeIteratorIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveUserIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveLocationIf;
import com.xhive.dom.interfaces.XhivePointIf;
import com.xhive.dom.interfaces.XhiveRangeIf;
import com.xhive.index.interfaces.XhiveIndexIf;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import java.util.Iterator;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive General
 *
 * [DESCRIPTION]
 * This helper class can be used to show specific database details.
 *
 *
 */
public class Reporter {

    public void showAllUsers(XhiveDatabaseIf database) {
        System.out.println("\n*Show all users of database " + database.getName());
        for (Iterator i = database.getUserList().iterator(); i.hasNext();) {
            XhiveUserIf user = (XhiveUserIf) i.next();
            System.out.println("   User Name        = " + user.getName());
        }
    }

    public void showAllGroups(XhiveDatabaseIf database) {
        System.out.println("\n*Show all groups of database " + database.getName());
        for (Iterator i = database.getGroupList().iterator(); i.hasNext();) {
            XhiveGroupIf group = (XhiveGroupIf) i.next();
            System.out.println("   Group Name        = " + group.getName());
        }
    }

    public void showAllUsersOfGroup(XhiveGroupIf group) {
        System.out.println("\n*Show all users of group " + group.getName());
        for (Iterator i = group.users(); i.hasNext();) {
            XhiveUserIf user = (XhiveUserIf) i.next();
            System.out.println("   User Name = " + user.getName());
        }
    }

    public void showAllGroupsOfUser(XhiveUserIf user) {
        System.out.println("\n*Show all groups of user " + user.getName());
        for (Iterator i = user.groups(); i.hasNext();) {
            XhiveGroupIf group = (XhiveGroupIf) i.next();
            System.out.println("   Group Name = " + group.getName());
        }
    }

    public void showAllDocumentsOfUser(XhiveUserIf user, XhiveDatabaseIf database) {
        System.out.println("\n*Show documents of user " + user.getName());
        XhiveLibraryIf libNode = database.getRoot();
        DocumentTraversal docTravers = XhiveDriverFactory.getDriver().getDocumentTraversal();
        NodeFilter documentFilter = new samples.manual.XhiveDocumentFilter();

        TreeWalker walker = docTravers.createTreeWalker(libNode, NodeFilter.SHOW_ALL, documentFilter, false);
        XhiveDocumentIf doc;
        while ((doc = (XhiveDocumentIf) walker.nextNode()) != null) {
            if (doc.getAuthority().getOwner().equals(user)) {
                System.out.println("   Document Id = " + doc.getId());
            }
        }
    }

    public void showAuthority(XhiveAuthorityIf auth) {
        XhiveGroupIf group = auth.getGroup();
        int ownerAuth = auth.getOwnerAuthority();
        int groupAuth = auth.getGroupAuthority();
        int otherAuth = auth.getOtherAuthority();
        System.out.println("   Owner           = " + auth.getOwner().getName());
        if (group != null) {
            System.out.println("   Group           = " + group.getName());
        } else {
            System.out.println("   Group = null");
        }

        System.out.println("   Owner authority = " + ownerAuth);
        System.out.println("   Group authority = " + groupAuth);
        System.out.println("   Other authority = " + otherAuth);
    }

    public void showAuthority(Document doc) {
        System.out.println("\n*Show authority of document with Id " + ((XhiveDocumentIf) doc).getId());
        XhiveAuthorityIf auth = ((XhiveDocumentIf) doc).getAuthority();
        showAuthority(auth);
    }

    /**
     * Show the node name and node value of all nodes in de location set
     * This function should only be used when the user expects the location-iterator only
     * to contain nodes. (no points or ranges)
     * If the location-set containes other location-types than nodes, use the function <code>showLocations</code>
     */
    public void showNodeSet(XhiveLocationIteratorIf nodes) {
        System.out.println("\n*Show all nodes in nodeset");

        while (nodes.hasNext()) {
            Node node = (Node) nodes.next();
            System.out.println("     Node name = " + node.getNodeName() + " Node value = " + node.getNodeValue());
        }
    }

    /**
     * Show the node name and node value of all nodes in de XPath node set
     */
    public void showLocations(XhiveLocationIteratorIf locations) {
        System.out.println("\n*Show all locations in locationset");

        while (locations.hasNext()) {
            XhiveLocationIf location = locations.next();
            short locationType = location.getLocationType();
            switch (locationType) {
            case XhiveLocationIf.POINT:
                XhivePointIf point = (XhivePointIf) location;
                System.out.println("     Locator type = point");
                System.out.println("       Container node = " + point.getContainerNode().getNodeName());
                System.out.println("       Index          = " + point.getIndex() + "\n");
                break;
            case XhiveLocationIf.RANGE:
                XhiveRangeIf range = (XhiveRangeIf) location;
                System.out.println("     Locator type = range");
                XhivePointIf startPoint = range.getStartPoint();
                System.out.println("     Startpoint :");
                System.out.println("       Container node = " + startPoint.getContainerNode().getNodeName());
                System.out.println("       Index          = " + startPoint.getIndex());
                XhivePointIf endPoint = range.getEndPoint();
                System.out.println("     Endpoint :");
                System.out.println("       Container node = " + endPoint.getContainerNode().getNodeName());
                System.out.println("       Index          = " + endPoint.getIndex());
                break;
            default :
                Node node = (Node) location;
                System.out.println("     Locator type = node");
                System.out.println("     Node name = " + node.getNodeName() + " Node value = " + node.getNodeValue() + "\n");
            }
        }
    }

    /**
     * Show the keys of an index
     */
    public void showKeys(XhiveIndexIf index) {
        System.out.println("\nShow keys of  index \"" + index.getName() + "\"");
        for (Iterator i = index.getKeys(); i.hasNext();) {
            System.out.println("  key = " + (String) i.next());
        }
    }

    /**
     * Show the nodes by key of an index
     */
    public void showNodesByKey(XhiveIndexIf index, String key) {
        System.out.println("\n*Show all nodes of index \"" + index.getName() + "\" by key \"" + key + "\"");
        XhiveNodeIteratorIf nodeIter = index.getNodesByKey(key);
        if (nodeIter != null) {
            while (nodeIter.hasNext()) {
                System.out.println("  node = " + nodeIter.next());
            }
        }
    }


    /**
     * Show all descending nodes in document-order by using the Traversal.
     *
     */
    public void showAllNodes(Node rootNode) {
        System.out.println("\n*Show all descending nodes of node " + rootNode.getNodeName());
        DocumentTraversal docTravers = XhiveDriverFactory.getDriver().getDocumentTraversal();

        TreeWalker walker = docTravers.createTreeWalker(rootNode, NodeFilter.SHOW_ALL, null, false);
        Node node;
        while ((node = walker.nextNode()) != null) {
            System.out.println("     Node name = " + node.getNodeName() + " Node value = " + node.getNodeValue());
        }
    }

    /**
     * Show all nodes in the iterator.
     *
     */
    public void showAllNodes(Iterator iterator) {
        System.out.println("\n*Show all nodes of iterator");

        while (iterator.hasNext()) {
            Node node = (Node) iterator.next();
            System.out.println("     Node name = " + node.getNodeName() + " Node value = " + node.getNodeValue());
        }
    }

    /**
     * Show all databases
     */
    public void showAllDatabases(XhiveSessionIf session) {
        System.out.println("\n*Show All Databases");
        for (Iterator i = session.getFederation().getDatabaseNames(); i.hasNext();) {
            System.out.println("     Database = " + i.next());
        }
    }

    /**
     * Show all libraries of the database
     */
    public void showAllLibraries(XhiveDatabaseIf database) {
        System.out.println("\n*Show All Libraries of database " + database.getName());

        XhiveLibraryIf root = database.getRoot();

        System.out.println("     Root Library = " + root.getName());
        DocumentTraversal docTravers = XhiveDriverFactory.getDriver().getDocumentTraversal();
        NodeFilter libraryFilter = new samples.manual.XhiveLibraryFilter();

        TreeWalker walker = docTravers.createTreeWalker(root, NodeFilter.SHOW_ALL, libraryFilter, false);
        XhiveLibraryIf lib;
        while ((lib = (XhiveLibraryIf) walker.nextNode()) != null) {
            System.out.println("     Library = " + lib.getName());
        }
    }

    public void showAllDocuments(XhiveDatabaseIf database) {
        System.out.println("\n*Show documents of database " + database.getName());
        XhiveLibraryIf libNode = database.getRoot();
        DocumentTraversal docTravers = XhiveDriverFactory.getDriver().getDocumentTraversal();
        NodeFilter documentFilter = new samples.manual.XhiveDocumentFilter();

        TreeWalker walker = docTravers.createTreeWalker(libNode, NodeFilter.SHOW_ALL, documentFilter, false);
        XhiveDocumentIf doc;
        while ((doc = (XhiveDocumentIf) walker.nextNode()) != null) {
            System.out.println("   Document Id = " + doc.getId());
        }
    }

    public void showAllIndexes(XhiveLibraryChildIf libChild) {
        System.out.println("\n*Show indexes of library child " + libChild.getName());
        for (Iterator i = libChild.getIndexList().iterator(); i.hasNext();) {
            System.out.println("   Index name = " + ((XhiveIndexIf) i.next()).getName());
        }
    }

    public long countAllFilesInLibrary(XhiveLibraryIf lib) {
        long total = 0;
        DocumentTraversal docTravers = XhiveDriverFactory.getDriver().getDocumentTraversal();
        NodeFilter documentFilter = new samples.manual.XhiveDocumentFilter();

        TreeWalker walker = docTravers.createTreeWalker(lib, NodeFilter.SHOW_ALL, documentFilter, false);
        while (walker.nextNode() != null) {
            total++;
        }
        return total;
    }


    public long countAllFilesInDatabase(XhiveDatabaseIf database) {
        long total = 0;

        XhiveLibraryIf libNode = database.getRoot();
        DocumentTraversal docTravers = XhiveDriverFactory.getDriver().getDocumentTraversal();
        NodeFilter documentFilter = new samples.manual.XhiveDocumentFilter();

        TreeWalker walker = docTravers.createTreeWalker(libNode, NodeFilter.SHOW_ALL, documentFilter, false);
        while (walker.nextNode() != null) {
            total++;
        }
        return total;
    }
}

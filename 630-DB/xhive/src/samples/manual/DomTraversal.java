package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;

import java.io.File;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to traverse documents using DOM Traversal
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.DomTraversal
 */
public class DomTraversal {

    public static void main(String[] args) {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        // create a session
        XhiveDriverIf driver = XhiveDriverFactory.getDriver();
        driver.init(1024);
        XhiveSessionIf session = driver.createSession();

        try {

            // open a connection to the database
            session.connect(administratorName, administratorPassword, databaseName);

            // create a Reporter for some nice output
            Reporter rep = new Reporter();

            // begin the transaction
            session.begin();

            // get a handle to the database
            XhiveDatabaseIf united_nations_db = session.getDatabase();

            // get a handle to the root library
            XhiveLibraryIf rootLibrary = united_nations_db.getRoot();

            // get/create the "UN Charter" library
            XhiveLibraryIf charterLib = DataLoader.createLibrary(united_nations_db, rootLibrary, "UN Charter", session);

            // load the sample documents
            DataLoader.storeDocuments(united_nations_db, charterLib, session);

            // retrieve the document to traverse
            Document resultGetDocument = (Document)charterLib.get("UN Charter - Chapter 1");

            // get a handle to the DocumentTraversal implementation
            DocumentTraversal docTraversal = XhiveDriverFactory.getDriver().getDocumentTraversal();

            // use a NodeIterator without a NodeFilter
            System.out.println("\n#NodeIterator without a NodeFilter:");
            NodeIterator iter = docTraversal.createNodeIterator(resultGetDocument, NodeFilter.SHOW_ALL, null, false);
            Node node;

            int i = 1;
            while ((node = iter.nextNode()) != null) {
                System.out.println(i++ + "   Node Name = " + node.getNodeName());
            }

            // use a NodeIterator with a NodeFilter
            NodeFilter sampleFilter = new SampleFilter();

            System.out.println("\n#NodeIterator with a NodeFilter:");
            iter = docTraversal.createNodeIterator(resultGetDocument, NodeFilter.SHOW_ALL, sampleFilter, false);

            int j = 1;
            while ((node = iter.nextNode()) != null) {
                System.out.println(j++ + "   Node Name = " + node.getNodeName());
            }

            // use a TreeWalker with a NodeFilter
            System.out.println("\n#TreeWalker with a NodeFilter:");
            TreeWalker walker = docTraversal.createTreeWalker(resultGetDocument, NodeFilter.SHOW_ALL, sampleFilter, false);

            int k = 1;
            while ((node = walker.nextNode()) != null) {
                System.out.println(k++ + "   Node Name = " + node.getNodeName());
            }

            session.commit();

        } catch (Exception e) {

            System.out.println("DomTraversal sample failed: ");
            e.printStackTrace();

        } finally {

            // disconnect and remove the session
            if (session.isOpen()) {
                session.rollback();
            }
            if (session.isConnected()) {
                session.disconnect();
            }

            driver.close();
        }
    }
}

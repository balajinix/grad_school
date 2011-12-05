package samples.manual;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.index.interfaces.XhiveCCIndexIf;
import com.xhive.index.interfaces.XhiveIndexListIf;
import com.xhive.query.interfaces.XhiveXQueryValueIf;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Iterator;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how some of the extra functionality of XQuery provided by X-Hive/DB
 * can be used.
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.XQueryExtensions
 *
 */
public class XQueryExtensions {

    private XhiveLibraryIf charterLib;

    public static void main(String[] args) {
        new XQueryExtensions().run();
    }

    private void run() {

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

            // begin the transaction
            session.begin();

            // get a handle to the database
            XhiveDatabaseIf united_nations_db = session.getDatabase();

            // get a handle to the root library
            XhiveLibraryIf rootLibrary = united_nations_db.getRoot();

            // get/create the "UN Charter" library
            charterLib = DataLoader.createLibrary(united_nations_db, rootLibrary, "UN Charter", session);

            // load the sample documents
            DataLoader.storeDocuments(united_nations_db, charterLib, session);

            // execute queries

            runQuery(charterLib, "Standard XQuery",
                     "//para[contains(text(), 'distinction')]");
            runQuery(charterLib, "Make use of X-Hive full text function",
                     "//para[xhive:fts(text(), 'distinction')]");
            runQuery(charterLib, "Make use of user defined function",
                     "//para[xhive:java('samples.manual.XQueryContainsWordFunction', text(), 'distinction')]");
            prepareCCIIndex(charterLib.getIndexList());
            runQuery(charterLib, "Make use of context conditioned index",
                     "xhive:get-nodes-by-key('/UN Charter', 'word index', 'distinction')");

            session.commit();

        } catch (Exception e) {

            System.out.println("XQueryExtensions sample failed: ");
            e.printStackTrace();

        } finally {

            if (session.isOpen()) {
                session.rollback();
            }

            // disconnect and remove the session
            if (session.isConnected()) {
                session.disconnect();
            }

            driver.close();
        }
    }

    /**
     * Create a String from a result-type code
     */
    private String getResultType(XhiveXQueryValueIf result) {
        if (result.isNode()) {
            return "Node";
        } else {
            int type = result.getNodeType();
            switch (type) {
            case XhiveXQueryValueIf.BOOLEAN:
                return "Boolean";
            case XhiveXQueryValueIf.STRING:
                return "String";
            case XhiveXQueryValueIf.INTEGER:
            case XhiveXQueryValueIf.INT:
            case XhiveXQueryValueIf.FLOAT:
            case XhiveXQueryValueIf.DOUBLE:
                return "Number";
            }
            return "type other";
        }
    }

    /**
     * Run a given query on a given context, and output the result.
     */
    private void runQuery(XhiveLibraryChildIf contextNode, String description, String queryString) {
        // Execute the query
        System.out.println("\n\n#type of query: " + description);
        System.out.println("#        query: " + queryString);
        Iterator results = null;
        results = contextNode.executeXQuery(queryString);

        // Process the results
        System.out.println("#query result:");
        while (results.hasNext()) {
            XhiveXQueryValueIf result = (XhiveXQueryValueIf) results.next();
            System.out.println(" Result type = " + getResultType(result));

            if (result.isNode()) {
                Node node = result.asNode();
                System.out.println(node);
            } else {
                String value = result.asString();
                System.out.println(value);
            }
        }
    }

    /**
     * Create a context conditioned index for use from a query.
     * See the Indexing sample for more information on context conditioned indexes.
     */
    private void prepareCCIIndex(XhiveIndexListIf indexList) {
        String indexName = "word index";
        XhiveCCIndexIf index = (XhiveCCIndexIf)indexList.getIndex(indexName);
        if (index == null) {
            // If the index does not exist yet, create and fill it
            index = indexList.addNodeFilterIndex("samples.manual.XQueryExtensionsIndexFilter", indexName);
            // Index all documents
            Node node = (Document)charterLib.getFirstChild();
            while (node != null) {
                if (node instanceof Document) {
                    index.indexDocument((Document)node);
                }
                node = node.getNextSibling();
            }
        }
    }

}

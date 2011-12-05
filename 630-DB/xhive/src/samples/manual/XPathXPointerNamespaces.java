package samples.manual;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveLocationIteratorIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveLocationIf;
import com.xhive.query.interfaces.XhiveQueryResultIf;
import com.xhive.xpath.interfaces.XhiveXPathContextIf;

import org.w3c.dom.Node;

import java.io.File;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how namespaces can be used with XPath and XPointer within X-Hive/DB.
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.XPathXPointerNamespaces
 *
 */
public class XPathXPointerNamespaces {

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

            // begin the transaction
            session.begin();

            // get a handle to the database
            XhiveDatabaseIf united_nations_db = session.getDatabase();

            // get a handle to the root library
            XhiveLibraryIf rootLibrary = united_nations_db.getRoot();

            // get/create the "UN Charter" library
            XhiveLibraryIf charterLib = DataLoader.createLibrary(united_nations_db, rootLibrary, "UN Charter", session);

            String fileName = "publish2HTML.xsl";

            // parse the XSL source file, which contains namesspaces
            XhiveDocumentIf nsDocument = (XhiveDocumentIf) DataLoader.storeDocument(charterLib,
                                         SampleProperties.baseDir + fileName,
                                         false,
                                         fileName);

            // Query Using XPath

            // Create an XPathContext
            XhiveXPathContextIf xpathContext = nsDocument.createXPathContext();

            // Add a namespace declaration for the xsl-namespace
            // Note that the prefix does not have to match
            xpathContext.addNamespaceBinding("ns", "http://www.w3.org/1999/XSL/Transform");

            // Execute the query
            String theQuery = "descendant::ns:template/@match";
            System.out.println("#running query: " + theQuery);
            XhiveQueryResultIf result = nsDocument.executeXPathQuery(theQuery, xpathContext);

            outputResult(result);

            // Query Using XPointer
            theQuery = "xmlns(ns=http://www.w3.org/1999/XSL/Transform) xpointer(descendant::ns:template/@match)";
            System.out.println("#running query: " + theQuery);
            result = nsDocument.executeXPointerQuery(theQuery);

            outputResult(result);

            session.commit();

        } catch (Exception e) {

            System.out.println("XPathXPointerNamespaces sample failed: ");
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


    private static void outputResult(XhiveQueryResultIf result) {
        // Process the results
        if (result != null) {
            System.out.println("#query result:");
            short resultType = result.getType();
            System.out.println(" Result type = " + getResultType(resultType));

            if (resultType == XhiveQueryResultIf.LOCATIONSET) {

                XhiveLocationIteratorIf resultNodeSet = result.getLocationSetValue();
                System.out.println(" Number of nodes = " + resultNodeSet.size() + "\n");

                XhiveLocationIf resultNode;

                while (resultNodeSet.hasNext()) {
                    resultNode = resultNodeSet.next();

                    if (resultNode.getLocationType() == Node.ATTRIBUTE_NODE) {
                        System.out.println(" " + ((Node) resultNode).getNodeValue());
                    }
                }
            }
        }
    }


    public static String getResultType(short type) {
        switch (type) {
        case XhiveQueryResultIf.LOCATIONSET:
            return "Location set";
        case XhiveQueryResultIf.BOOLEAN:
            return "Boolean";
        case XhiveQueryResultIf.STRING:
            return "String";
        case XhiveQueryResultIf.NUMBER:
            return "Number";
        }
        return "non valid type";
    }
}

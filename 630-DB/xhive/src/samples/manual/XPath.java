package samples.manual;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveLocationIteratorIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveLocationIf;
import com.xhive.query.interfaces.XhiveQueryResultIf;

import org.w3c.dom.Node;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how XPath can be used within X-Hive/DB
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.XPath
 *
 */
public class XPath {

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

            // load the sample documents
            DataLoader.storeDocuments(united_nations_db, charterLib, session);

            // Execute the query
            String theQuery = "descendant::chapter/title";
            System.out.println("#running query: " + theQuery);
            XhiveQueryResultIf result = charterLib.executeXPathQuery(theQuery);

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

                        if (resultNode.getLocationType() == Node.ELEMENT_NODE) {
                            System.out.println(" " + ((Node) resultNode).getFirstChild().getNodeValue());
                        }
                    }
                }
            }

            session.commit();

        } catch (Exception e) {

            System.out.println("XPath sample failed: ");
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

package samples.manual;

import samples.Reporter;

import java.util.Iterator;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.query.interfaces.XhiveXQueryValueIf;

import org.w3c.dom.Node;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how XQuery can be used within X-Hive/DB
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.XQuery
 *
 */
public class XQuery {

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

            // Create a query (find all the short chapter titles)
            String theQuery =
                "for $i in document('/UN Charter')//chapter/title \n" +
                "where string-length($i) lt 16 \n" +
                "return <chaptertitle>{ $i/node() }</chaptertitle>";

            // Execute the query (place the results in the new document)
            System.out.println("#running query:\n" + theQuery);
            Iterator result = rootLibrary.executeXQuery(theQuery);

            // Process the results
            while (result.hasNext()) {
                // Get the next value from the result sequence
                XhiveXQueryValueIf value = (XhiveXQueryValueIf)result.next();

                // Print this value
                System.out.println(value.toString());
            }

            session.commit();

        } catch (Exception e) {

            System.err.println("XQuery sample failed: ");
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

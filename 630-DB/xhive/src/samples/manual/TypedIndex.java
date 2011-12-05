package samples.manual;

import samples.Reporter;

import java.util.Iterator;
import java.io.File;

import com.xhive.XhiveDriverFactory;
import com.xhive.index.interfaces.XhiveIndexIf;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.query.interfaces.XhiveXQueryValueIf;
import org.w3c.dom.ls.LSParser;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how typed information from indexes and PSVI can be used in XQuery.
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.TypedIndex
 *
 */
public class TypedIndex {

    private static final String DOCUMENT_NAME = "un_charter_editor_info.xml";

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

            //if (charterLib.nameExists(DOCUMENT_NAME)) {
            //  charterLib.removeChild(charterLib.get(DOCUMENT_NAME));
            //}


            if (! charterLib.nameExists(DOCUMENT_NAME)) {
                String fileName = SampleProperties.baseDir + DOCUMENT_NAME;
                // Store the Query data sample
                LSParser parser = charterLib.createLSParser();

                // Note: These options would not be necessary if the query would use constants, e.g. '...>= xf:date("2003-04-08")'
                parser.getDomConfig().setParameter("validate", Boolean.TRUE);
                parser.getDomConfig().setParameter("xhive-psvi", Boolean.TRUE);

                XhiveDocumentIf newDoc = (XhiveDocumentIf) parser.parseURI(new File(fileName).toURL().toString());
                newDoc.setName(DOCUMENT_NAME);
                charterLib.appendChild(newDoc);

                // Note: In most use cases, you would not actually put the index on the document, but rather on a library
                // Note: TYPE_DATE is used so that we can use the field in an XQuery using dates
                // Note: KEY_SORTED is used so that the index can be used in lesser then and larger then type of queries
                newDoc.getIndexList().addValueIndex("date index", null, "date", null, null,
                                                    XhiveIndexIf.TYPE_DATE | XhiveIndexIf.KEY_SORTED);
            }

            XhiveDocumentIf contextDocument = (XhiveDocumentIf) charterLib.get(DOCUMENT_NAME);

            // Create a query (find the titles of documents approved after document chapter 9 was last approved)
            String theQuery =
                "(# xhive:index-debug stdout #) { \n" +
                "let $chapter9 := //chapter[@id='/UN Charter/UN Charter - Chapter 9'] \n" +
                "for $doc in //chapter \n" +
                "where $doc/date >= $chapter9/date \n" +
                "return document($doc/@id)/chapter/title }";

            // Execute the query (place the results in the new document)
            System.out.println("#running query:\n" + theQuery + "\n");
            Iterator result = contextDocument.executeXQuery(theQuery);

            System.out.println("#result: (Note how the debug output shows the index is used)\n");

            // Process the results
            while (result.hasNext()) {
                // Get the next value from the result sequence
                XhiveXQueryValueIf value = (XhiveXQueryValueIf)result.next();

                // Print this value
                System.out.println(value.toString());
            }

            session.commit();

        } catch (Exception e) {

            System.err.println("TypedIndex sample failed: ");
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

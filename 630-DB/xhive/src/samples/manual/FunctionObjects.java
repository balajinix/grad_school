package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import org.w3c.dom.Document;

import java.io.File;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to create and use function objects
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.FunctionObjects
 *
 */
public class FunctionObjects {

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
            Document chapter5Document = (Document)charterLib.get("UN Charter - Chapter 5");

            MyNumberFinder numberFinder = new MyNumberFinder();

            System.out.println("# traverse the charter library with traverseDocumentOrder:");
            charterLib.traverseDocumentOrder(numberFinder);

            System.out.println("# traverse the charter library with traverseBreadthFirst:");
            charterLib.traverseBreadthFirst(numberFinder);

            System.out.println("# traverse \"UN Charter - Chapter 5\" with traverseDocumentOrder:");
            ((XhiveDocumentIf) chapter5Document).traverseDocumentOrder(numberFinder);

            System.out.println("# traverse \"UN Charter - Chapter 5\" with traverseReverseDocumentOrder:");
            ((XhiveDocumentIf) chapter5Document).traverseReverseDocumentOrder(numberFinder);

            session.commit();

        } catch (Exception e) {

            System.out.println("FunctionObjects sample failed: ");
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

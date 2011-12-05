package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import org.w3c.dom.Document;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import java.io.File;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to store documents
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.StoreDocuments
 *
 */
public class StoreDocuments {

    public static void main(String[] args) {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        // the number of files parsed
        int numFiles = SampleProperties.numFiles;

        // the name of the files to store/append
        String fileName1 = "../src/samples/data/manual/un_charter_preamble.xml";
        String fileName2 = "../src/samples/data/manual/un_charter_introductory_note.xml";

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

            //show all documents - before
            System.out.println("\nBefore:");
            System.out.println(" the number of documents is: " + rep.countAllFilesInDatabase(united_nations_db));

            //create a DOMBuilder
            LSParser builder = charterLib.createLSParser();

            // parse a new document
            Document firstDocument = builder.parseURI(new File(fileName1).toURL().toString());

            String firstDocumentName = "UN Charter - Preamble";

            // if it doesn't exist yet: store it
            if (!(charterLib.nameExists(firstDocumentName))) {
                // add the new document to the "UN Charter" library
                charterLib.appendChild(firstDocument);

                // give the new document a name
                ((XhiveDocumentIf) firstDocument).setName(firstDocumentName);
            } else {
                firstDocument = (Document) charterLib.get(firstDocumentName);
            }

            //show all documents - after first document
            System.out.println("\nAfter (1):");
            System.out.println(" the number of documents is: " + rep.countAllFilesInDatabase(united_nations_db));

            // parse a new second document
            Document secondDocument = builder.parseURI(new File(fileName2).toURL().toString());

            String secondDocumentName = "UN Charter - Introductory Note";

            // if it doesn't exist yet: store it
            if (!(charterLib.nameExists(secondDocumentName))) {
                // add the second document before the first document in "UN Charter" library
                charterLib.insertBefore(secondDocument, firstDocument);

                // give the second document a name
                ((XhiveDocumentIf) secondDocument).setName(secondDocumentName);
            }

            //show all documents - after second document
            System.out.println("\nAfter (2):");
            System.out.println(" the number of documents is: " + rep.countAllFilesInDatabase(united_nations_db));

            session.commit();

        } catch (Exception e) {

            System.out.println("StoreDocuments sample failed: ");
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

package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.error.XhiveException;

import org.w3c.dom.Document;
import org.w3c.dom.ls.LSParser;

import java.io.File;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to use sessions and transactions
 * and perform some persistent operations within a transaction
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.UseSessions
 *
 */
public class UseSessions {

    public static void main(String[] args) {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        // the name of the files to store
        String baseFileName = SampleProperties.baseFileName;

        // the number of files parsed
        int numFiles = SampleProperties.numFiles;

        // create a session
        XhiveDriverIf driver = XhiveDriverFactory.getDriver();
        driver.init(1024);
        XhiveSessionIf session = driver.createSession();

        try {

            // open a connection to the database
            session.connect(administratorName, administratorPassword, databaseName);

            // create a Reporter for some nice output
            Reporter rep = new Reporter();

            //
            // first example of creating a transaction:
            // parse a number of (existing) documents
            //

            try {

                // begin a transaction (1)
                session.begin();

                // get a handle to the database
                XhiveDatabaseIf united_nations_db = session.getDatabase();

                // get a handle to the root library
                XhiveLibraryIf rootLibrary = united_nations_db.getRoot();

                //show all documents - before
                System.out.println("\nBefore:");
                rep.showAllDocuments(united_nations_db);

                LSParser parser = rootLibrary.createLSParser();
                // parse documents and add them to the root library
                for (int i = 1; i <= numFiles; i++) {
                    String fileName = baseFileName + i + ".xml";
                    String documentName = "CreateTransactionSample - UN Charter - Chapter " + i;
                    DataLoader.storeDocument(rootLibrary, fileName, parser, documentName);
                }

                //show all documents - after
                System.out.println("\nAfter:");
                rep.showAllDocuments(united_nations_db);

                // commit the changes
                session.commit();

            } catch (XhiveException xe) {

                // Be sure to rollback the transaction on failure
                if (session.isOpen()) {
                    System.out.println("\nError detected, rollback...");
                    session.rollback();
                }

            }

            //
            // second example of creating a transaction:
            // again, parse a number of documents but this time
            // it will fail, and a rollback() will be occur
            //

            try {

                // begin a transaction (2)
                session.begin();

                // get a handle to the database
                XhiveDatabaseIf united_nations_db = session.getDatabase();

                // get a handle to the root library
                XhiveLibraryIf rootLibrary = united_nations_db.getRoot();

                // parse documents and add them to the root library
                Document newDocument = null;
                for (int i = 1; i <= numFiles; i++) {
                    newDocument = rootLibrary.parseDocument(new File(baseFileName + i + ".xml").toURL(),
                                                            XhiveLibraryIf.PARSER_NAMESPACES_ENABLED | XhiveLibraryIf.PARSER_NO_VALIDATION);
                    rootLibrary.appendChild(newDocument);
                    ((XhiveDocumentIf) newDocument).setName("Sample CreateTransaction - FAILURE - UN Charter - Chapter " + i);
                }

                // the next newDocument does not exist, this will throw an
                // exception and cause a rollback of the transaction. None of the
                // files with prefix "Sample CreateTransaction - FAILURE ..." in
                // their name, will be stored
                System.out.println("\nTrying to parse a non-existent document will cause an error:");
                newDocument = rootLibrary.parseDocument(new File(baseFileName + "ERROR" + ".xml").toURL(),
                                                        XhiveLibraryIf.PARSER_NAMESPACES_ENABLED | XhiveLibraryIf.PARSER_NO_VALIDATION);
                rootLibrary.appendChild(newDocument);
                ((XhiveDocumentIf) newDocument).setName("Sample CreateTransaction - FAILURE - UN Charter - Chapter ERROR");

                // commit the changes
                session.commit();

            } catch (XhiveException xe) {

                // Be sure to rollback the transaction on failure
                if (session.isOpen()) {
                    System.out.println("\nError detected, rollback...");
                    session.rollback();
                }

            }

            try {

                // begin a transaction (3)
                session.begin();

                // get a handle to the database
                XhiveDatabaseIf united_nations_db = session.getDatabase();

                //show all documents - after
                System.out.println("\nAfter:");
                rep.showAllDocuments(united_nations_db);

                session.commit();

            } catch (XhiveException xe) {

                throw xe;
            }

        } catch (Exception e) {

            System.out.println("UseSessions sample failed: ");
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

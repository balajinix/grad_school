package samples.manual;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveCatalogIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import org.w3c.dom.Document;
import org.w3c.dom.as.ASModel;
import samples.Reporter;

import java.io.File;
import java.util.Iterator;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to parse documents with validation
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.ParseDocumentsWithValidation
 *
 */
public class ParseDocumentsWithValidation {

    public static void main(String[] args) {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        // the number of files parsed
        int numFiles = SampleProperties.numFiles;

        // the name of the files to store/append
        String fileName = "../src/samples/data/manual/un_charter_chapter1_withDTD.xml";
        String documentName = "Chapter 1 (DTD)";

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

            // parse a new document with validation (if it doesn't exist yet)
            Document document = DataLoader.storeDocument(charterLib, fileName, true, documentName);

            // retrieve the catalog of the "UN Charter" library
            XhiveCatalogIf unCharterCatalog = charterLib.getCatalog();

            // get the content models that exist in the root library catalog
            Iterator iter = unCharterCatalog.getASModels(true);
            ASModel asModel;
            while (iter.hasNext()) {
                asModel = (ASModel) iter.next();
                System.out.println(" asModel = " + asModel.getLocation());
            }

            session.commit();

        } catch (Exception e) {

            System.out.println("ParseDocumentsWithValidation sample failed: ");
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

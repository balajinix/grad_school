package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.util.interfaces.XhiveTransformerIf;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to convert XML documents into HTML documents
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.Publish2HTML
 *
 */
public class Publish2HTML {

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

            // parse the XSL source file
            String xslFileName = SampleProperties.baseDir + "publish2HTML.xsl";
            Document xslDocument = charterLib.createLSParser().parseURI(new File(xslFileName).toURL().toString());

            // get a handle to the Transformer implementation
            XhiveTransformerIf transformer = XhiveDriverFactory.getDriver().getTransformer();

            // retrieve the document to publish
            Document firstDocument = (Document) charterLib.get("UN Charter - Chapter 1");

            String result = transformer.transformToString(firstDocument, xslDocument);
            System.out.println(result);

            session.commit();

        } catch (Exception e) {

            System.out.println("Publish2HTML sample failed: ");
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

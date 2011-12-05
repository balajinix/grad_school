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
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import java.io.File;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to store and serialize documents with DOM's Load/ Save standard
 * We show off one particular feature of Load/Save, namely the possibility to omit
 * namespace declarations attributes during loading (so those attributes do not
 * appear in the resulting document), after which namespace declarations are introduced
 * during serialization (perhaps with an unwanted effect here, as prefixes are renamed
 * and there are now namespace declarations on each element).
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.DOMLoadSave
 *
 */
public class DOMLoadSave {

    public static void main(String[] args) {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        // the name of the files to store/append
        String fileName = "../src/samples/data/manual/un_bodies.xml";

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

            System.out.println("# Load a document");

            // Parse a new document, using a DOMBuilder
            LSParser parser = charterLib.createLSParser();
            parser.getDomConfig().setParameter("namespace-declarations", Boolean.FALSE);
            Document document = parser.parseURI(new File(fileName).toURL().toString());

            // IMPORTANT NOTE: We do not store the document in this sample, one would use charterLib.appendChild(document) for that

            System.out.println("# 'Save' a document (to string here)");

            // Serialize a document (to String), using a DOMWriter
            LSSerializer serializer = charterLib.createLSSerializer();
            serializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            String output = serializer.writeToString(document);

            // Display the output on the screen
            System.out.println(output);

            session.commit();

        } catch (Exception e) {

            System.out.println("DOMLoadSave sample failed: ");
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

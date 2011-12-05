package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.xlink.interfaces.XhiveSimpleLinkIf;
import com.xhive.dom.xlink.interfaces.XhiveXLinkUtilIf;

import org.w3c.dom.Document;

import java.io.File;
import java.util.Iterator;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how XLink works
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.XLink
 *
 */
public class XLink {

    public static void main(String[] args) {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        // the name of the files to store
        String linkFileName = SampleProperties.baseDir + "un_bodies.xml";
        String linkDocumentName = "UN bodies";

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

            XhiveDocumentIf linkDocument = (XhiveDocumentIf) DataLoader.storeDocument(charterLib, linkFileName, false, linkDocumentName);

            XhiveXLinkUtilIf util = XhiveDriverFactory.getDriver().getXLinkUtil();

            System.out.println("\n#Show all simple links\n");
            // Gets an Iterator with all simple links
            for (Iterator i = util.getLinks(linkDocument); i.hasNext();) {
                // note that simple and extended links may be found. In this case, only simple links are found.
                XhiveSimpleLinkIf link = (XhiveSimpleLinkIf) i.next();
                System.out.println("\n   Element name = " + link.getNodeName());
                System.out.println("   Title = " + link.getTitle());
            }

            System.out.println("\n#Expand the document\n");
            // First create a document where the links can be expanded to.
            Document tempDoc = united_nations_db.getRoot().createDocument(null, "expanded_link_doc", null);

            // Expand the document into the tempDoc
            tempDoc = util.expandDocument(linkDocument, tempDoc, true);
            System.out.println(tempDoc);

            System.out.println("\n#Generate the URI of a specific node\n");
            // Another convenience method: generate the URI of a specific node
            // in this case: the URI of the root element of document "UN Charter - Chapter 4"
            String uri = util.generateURI(((XhiveDocumentIf) charterLib.get("UN Charter - Chapter 4")).getDocumentElement());
            System.out.println("  URI = " + uri);

            session.commit();

        } catch (Exception e) {

            System.out.println("XLink sample failed: ");
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

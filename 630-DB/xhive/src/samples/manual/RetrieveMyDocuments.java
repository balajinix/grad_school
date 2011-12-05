package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.error.XhiveException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.util.Iterator;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to retrieve documents
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.RetrieveDocuments
 *
 */
public class RetrieveMyDocuments {

    public static void main(String[] args) {

        // the name of the database
        String databaseName = "apriori";

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
            XhiveLibraryIf charterLib = DataLoader.createLibrary(united_nations_db, rootLibrary, "apriorilib", session);

            // load the sample documents
            DataLoader.storeDocuments(united_nations_db, charterLib, session);

            // retrieve the documents thru DOM operations
            System.out.println("\n#Retrieving documents thru DOM operations...");

            // count the number of children of the "UN Charter" library
            int nrChildren = 0;
            if (charterLib.hasChildNodes()) {
                Node n = charterLib.getFirstChild();
                while (n != null) {
                    nrChildren++;
                    n = n.getNextSibling();
                }
            }
            System.out.println("library \"apriori\" has " + nrChildren + " children");

            // retrieve the documents by ID
            //System.out.println("\n#Retrieving documents by ID...");
            //int anId = 10;
            //Node child = charterLib.get(anId);
            //System.out.println("document with ID = " + anId + " in \"apriori\" has name: " + ((XhiveLibraryChildIf) child).getName());

            // retrieve the documents by name
            System.out.println("\n#Retrieving documents by name...");
            String documentName = "transactions";
            Document docRetrievedByName = (Document) charterLib.get(documentName);
            System.out.println("the ID of the document with name \"" + documentName + "\" is: " + ((XhiveLibraryChildIf) docRetrievedByName).getId());

            // retrieve the name of the document by executeFullPathXPointerQuery (FPXPQ)
            //System.out.println("\n#Retrieving documents by executeFullPathXPointerQuery...");
            //Document docRetrievedByFPXPQ = (Document)rootLibrary.getByPath("/UN Charter/UN Charter - Chapter 2");
            //System.out.println(docRetrievedByFPXPQ.toString());

            // use FPXPQ with a relative path
            //System.out.println("\n#Retrieving documents by executeFullPathXPointerQuery - using relative path...");
            // create a new sub library of UN Charter
            //XhiveLibraryIf newLib = charterLib.createLibrary(XhiveLibraryIf.LOCK_WITH_PARENT);
            //newLib.setName("sub library of UN Charter");
            //charterLib.appendChild(newLib);

            // execute the FullPathXPointerQuery relative to the new sub library
            //docRetrievedByFPXPQ = (Document) newLib.getByPath("../UN Charter - Chapter 3");
            //System.out.println(docRetrievedByFPXPQ.toString());

            // and delete the sub library
            //charterLib.removeChild(newLib);

            session.commit();

        } catch (Exception e) {

            System.out.println("RetrieveDocuments sample failed: ");
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

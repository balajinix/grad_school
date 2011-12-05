package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveNodeIteratorIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveNodeIf;
import com.xhive.index.interfaces.XhiveCCIndexIf;
import com.xhive.index.interfaces.XhiveIndexIf;
import com.xhive.index.interfaces.XhiveIndexListIf;

import org.w3c.dom.Document;

import java.io.File;
import java.util.Iterator;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to index documents
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.CCIndex
 */
public class CCIndex {

    public static void main(String[] args) {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        // the number of files parsed
        int numFiles = SampleProperties.numFiles;

        // the name of the files to store
        String baseFileName = SampleProperties.baseFileName;

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

            // get the index list that belongs to the root library
            XhiveIndexListIf indexList = rootLibrary.getIndexList();

            // create an XhiveIndexIf object
            String indexName = "Test Index";
            XhiveCCIndexIf index = (XhiveCCIndexIf)indexList.getIndex(indexName);
            if (index != null) {

                // remove existing index first
                System.out.println("removing index");
                indexList.removeIndex(index);
            }

            index = (XhiveCCIndexIf)indexList.addNodeFilterIndex("samples.manual.SampleIndexFilter", indexName);

            // load the sample documents
            DataLoader.storeDocuments(united_nations_db, charterLib, session);

            // index the documents
            for (int i = 1; i <= numFiles; i++) {
                String newDocumentName = "UN Charter - Chapter " + i;
                Document newDocument = (Document) charterLib.get(newDocumentName);
                ((XhiveCCIndexIf)index).indexDocument(newDocument);
            }

            // show all keys
            System.out.println("\n#show all keys in the index");
            Iterator keyIter = index.getKeys();
            while (keyIter.hasNext()) {
                String key = (String) keyIter.next();
                System.out.println(key);
            }

            // retrieve a document using the index
            System.out.println("\n#retrieve the document with chapter title \"AMENDMENTS\" using the index");
            XhiveNodeIteratorIf nodesFound = index.getNodesByKey("AMENDMENTS");
            while (nodesFound.hasNext()) {
                XhiveNodeIf docFound = (XhiveNodeIf)nodesFound.next();
                System.out.println(docFound.toXml());
            }

            session.commit();

        } catch (Exception e) {

            System.out.println("CCIndex sample failed: ");
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

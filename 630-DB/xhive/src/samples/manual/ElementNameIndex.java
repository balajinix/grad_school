package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.index.interfaces.XhiveIndexIf;
import com.xhive.index.interfaces.XhiveIndexListIf;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveNodeIteratorIf;
import com.xhive.core.interfaces.XhiveLocationIteratorIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;

import java.io.File;
import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to create and use element name indexes and selected element name indexes.
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.ElementNameIndex
 *
 */
public class ElementNameIndex {

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

            // open a connection
            session.connect(administratorName, administratorPassword, databaseName);

            // create a Reporter for some nice output
            Reporter rep = new Reporter();

            // begin the trans
            session.begin();

            // get a handle to the database
            XhiveDatabaseIf db = session.getDatabase();

            // get a handle to the root library
            XhiveLibraryIf rootLibrary = db.getRoot();

            //sample document: family.xml
            String fileName = "../src/samples/data/family.xml";
            String documentName = "family";

            //If document "family' is not yet added to the library, parse "family.xml" and add the document to the library
            XhiveDocumentIf document = (XhiveDocumentIf) DataLoader.storeDocument(rootLibrary, fileName, false, documentName);

            String elementIndexName = "Element name Index";
            String selectedElementIndexName = "Selected element name index";

            //add the element name index to the indexlist of the document if the index is not found
            XhiveIndexListIf indexList = document.getIndexList();
            XhiveIndexIf elementNameIndex = indexList.getIndex(elementIndexName);
            if (elementNameIndex == null) {
                elementNameIndex = indexList.addElementNameIndex(elementIndexName);
            }


            //show all keys of the element name index
            rep.showKeys(elementNameIndex);

            //Get all elements with name = "NAME"
            String key = "NAME";
            rep.showNodesByKey(elementNameIndex, key);

            //Remove the index
            indexList.removeIndex(elementNameIndex);

            //Add a selected element name index
            String[] names = {"NAME", "BORN", "WIFE"};
            XhiveIndexIf selectedElementNameIndex = indexList.addElementNameIndex(selectedElementIndexName, names);

            //show all keys of the element name index
            rep.showKeys(selectedElementNameIndex);

            //Remove the index
            indexList.removeIndex(selectedElementNameIndex);

            //sample document: checkouts.xml, this file contains namespaces
            String fileNameNS = "../src/samples/data/manual/checkouts.xml";
            String documentNameNS = "checkouts";


            //If document "checkouts' is not yet added to the library, parse "checkouts.xml" and add the document to the library
            XhiveDocumentIf documentNS = (XhiveDocumentIf) DataLoader.storeDocument(rootLibrary, fileNameNS, false, documentNameNS);

            //add the element value index to the indexlist of the document if the index is not found
            indexList = documentNS.getIndexList();
            elementNameIndex = indexList.getIndex(elementIndexName);
            if (elementNameIndex == null) {
                elementNameIndex = indexList.addElementNameIndex(elementIndexName);
            }


            //show all keys of the element name index
            rep.showKeys(elementNameIndex);

            //Get the elemets of name "editor" with namespace "http://www.x-hive.com"
            key = "http://www.x-hive.com checkout";
            rep.showNodesByKey(elementNameIndex, key);

            //Remove the index
            indexList.removeIndex(elementNameIndex);

            //Add a selected element name index
            String[] namesNS = {"http://www.x-hive.com chapter", "http://www.x-hive.com owner"};
            selectedElementNameIndex = indexList.addElementNameIndex(selectedElementIndexName, namesNS);
            rep.showNodesByKey(selectedElementNameIndex, "http://www.x-hive.com chapter");

            //Remove the index
            indexList.removeIndex(selectedElementNameIndex);

            session.commit();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ElementNameIndex sample failed: " + e);

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

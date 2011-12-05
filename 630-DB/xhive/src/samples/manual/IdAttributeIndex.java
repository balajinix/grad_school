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
 * This sample shows how to create and use an id attribute index.
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.IdAttributeIndex
 *
 */
public class IdAttributeIndex {

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

            String fileName = "../src/samples/data/family.xml";
            String documentName = "family";

            //If document "family' is not yet added to the library, parse "family.xml" and add the document to the library
            XhiveDocumentIf document = (XhiveDocumentIf) DataLoader.storeDocument(rootLibrary, fileName, false, documentName);

            String indexName = "ID Attribute Index";

            //add the ID attribute index to the indexlist of the document if the index is not found
            XhiveIndexListIf indexList = document.getIndexList();
            XhiveIndexIf index = indexList.getIndex(indexName);
            if (index == null) {
                index = indexList.addIdAttributeIndex(indexName);
            }

            //show all indexes of the document
            rep.showAllIndexes(document);

            //show all keys of the index
            System.out.println("\nShow keys if ID Attribute index");
            for (Iterator i = index.getKeys(); i.hasNext();) {
                System.out.println(   "Key: " + (String)i.next());
            }

            //Get the element of key = "p3"
            String key = "p3";
            System.out.println("\nShow the element with id = " + key);

            System.out.println("\nGet the element by using the index");
            XhiveNodeIteratorIf nodeIter = index.getNodesByKey(key);
            if (nodeIter.hasNext()) {
                System.out.println("  Element = " + nodeIter.next());
            }

            System.out.println("\nGet the element by using DOM function getElementById");
            Element element = document.getElementById(key);
            System.out.println("   Element = " + element);

            String xpathExpression = "id('p3')";
            System.out.println("\nGet the element by executing the XPath expression : " + xpathExpression);
            XhiveLocationIteratorIf locationIter = document.executeXPathQuery(xpathExpression).getLocationSetValue();
            if (locationIter.hasNext()) {
                System.out.println("  Element = " + locationIter.next());
            }

            String fullXPointerExpression = documentName + "#p3";
            System.out.println("\nGet the element by executing the full XPointer expression : " + fullXPointerExpression);
            Iterator iter = rootLibrary.executeFullPathXPointerQuery(fullXPointerExpression);
            if (iter.hasNext()) {
                System.out.println("  Element = " + (Node)iter.next());
            }

            session.commit();

        } catch (Exception e) {

            System.out.println("IdAttributeIndex sample failed: ");
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

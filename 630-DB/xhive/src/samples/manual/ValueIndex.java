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
 * This sample shows how to create and use a value index.
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.ValueIndex
 *
 */
public class ValueIndex {

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
            XhiveDocumentIf document = (XhiveDocumentIf)DataLoader.storeDocument(rootLibrary, fileName, false, documentName);

            String nameIndexName = "NAME Elements Value Index";
            String idIndexName = "Elements By ID Value Index";
            String personByFatherIndexName = "PERSON Elements By FATHER Value Index";

            //add the element value index to the indexlist of the document if the index is not found
            XhiveIndexListIf indexList = document.getIndexList();
            XhiveIndexIf nameIndex = indexList.getIndex(nameIndexName);
            if (nameIndex == null) {
                //The index stores elements of name "NAME" by element value
                nameIndex = indexList.addValueIndex(nameIndexName, null, "NAME", null, null);
            }

            //add the attribute value index to the indexlist of the document if the index is not found
            XhiveIndexIf idIndex = indexList.getIndex(idIndexName);
            if (idIndex == null) {
                //The index stores elements by element attribute value. The attribute has name "IDREF" and the
                //elementname is not defined.
                idIndex = indexList.addValueIndex(idIndexName, null, null, null, "ID");
            }

            //add the attribute value index to the indexlist of the document if the index is not found
            XhiveIndexIf personByFatherIndex = indexList.getIndex(personByFatherIndexName);
            if (personByFatherIndex == null) {
                //The index stores elements by element attribute value. The attribute has name "IDREF" and the
                //elementname is not defined.
                personByFatherIndex = indexList.addValueIndex(personByFatherIndexName, null, "PERSON", null, "FATHER");
            }


            //show all indexes of the document
            rep.showAllIndexes(document);

            //show all keys of the element value index
            rep.showKeys(nameIndex);

            //show all keys of the attribute value index
            rep.showKeys(idIndex);

            //show all keys of the attribute value by element name index
            rep.showKeys(personByFatherIndex);

            //Get the "PERSON elements with FATHER = "p2"
            String key = "p2";
            rep.showNodesByKey(personByFatherIndex,  key);

            session.commit();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("IdAttributeIndex sample failed: " + e);

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

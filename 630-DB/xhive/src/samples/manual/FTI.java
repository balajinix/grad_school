package samples.manual;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhiveNodeIteratorIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.index.interfaces.XhiveIndexIf;
import com.xhive.index.interfaces.XhiveIndexListIf;
import com.xhive.query.interfaces.XhiveXQueryValueIf;

import java.util.Iterator;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to use full text indexing in X-Hive/DB
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.FTI
 */
public class FTI {

    public static void main(String[] args) {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        // Get the driver
        XhiveDriverIf driver = XhiveDriverFactory.getDriver();
        driver.init(1024);

        // create a session
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

            // get/create the "UN Charter" library, and fill it with documents
            XhiveLibraryIf charterLib = DataLoader.createLibrary(united_nations_db, rootLibrary, "UN Charter", session);
            DataLoader.storeDocuments(united_nations_db, charterLib, session);

            String indexName = "full-text";

            // Create a full text index on the para element, using the default analyzer, indexing all text in
            // the descendent text nodes of the para element (this is necessary, since the para element can have children).
            // Also support phrase searches, asjust tokens to lowercase and filter stop words
            XhiveIndexListIf indexList = charterLib.getIndexList();
            XhiveIndexIf index = indexList.getIndex(indexName);
            if (index == null) {
                indexList.addFullTextIndex(indexName, null, "para", null, null, null,
                                           XhiveIndexIf.FTI_GET_ALL_TEXT | XhiveIndexIf.FTI_SUPPORT_PHRASES | XhiveIndexIf.FTI_SA_ADJUST_TO_LOWERCASE |
                                           XhiveIndexIf.FTI_SA_FILTER_ENGLISH_STOP_WORDS);
            }

            // Sample of a phrase query
            executeQuery(charterLib, "//para[xhive:fts(., '\" The Trusteeship Council shall consist of\"')]");

            // Sample query using boolean operator
            executeQuery(charterLib, "//para[xhive:fts(., ' regulations AND assembly')]");

            // Sample query using a wildcard
            executeQuery(charterLib, "//para[xhive:fts(., 'reg*s AND assembly')]");

            session.commit();

        } catch (Exception e) {

            System.out.println("FTI sample failed: ");
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

    private static void executeQuery(XhiveLibraryIf library, String query) {
        System.out.println("#running query: " + query);
        for (Iterator result = library.executeXQuery(query); result.hasNext();) {
            XhiveXQueryValueIf value = (XhiveXQueryValueIf) result.next();
            System.out.println(value.asString());
        }
    }
}

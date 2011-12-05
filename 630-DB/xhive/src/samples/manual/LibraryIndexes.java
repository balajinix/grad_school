package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.index.interfaces.XhiveIndexIf;
import com.xhive.index.interfaces.XhiveIndexListIf;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to use library id and library name indexes.
 * These index types improve performance and scalability of XhiveLibraryIf functions
 * get(long id) and get(String name).
 *
 * [DEPENDENCIES / PRE-REQUISITES 
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.LibraryIndexes
 *
 */
public class LibraryIndexes {

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
            XhiveDatabaseIf united_nations_db = session.getDatabase();

            // get a handle to the root library
            XhiveLibraryIf rootLibrary = united_nations_db.getRoot();

            //the root library is created with both id index and name index
            //show all indexes of the root library
            rep.showAllIndexes(rootLibrary);

            // create a library (unless it already exists)
            XhiveLibraryIf library = null;

            if (rootLibrary.nameExists("Publications")) {
                library = (XhiveLibraryIf) rootLibrary.get("Publications");
            } else {
                // create a library
                library = rootLibrary.createLibrary(XhiveLibraryIf.LOCK_WITH_PARENT);

                // give the new library a name
                library.setName("Publications");

                // append the new libary to its parent
                rootLibrary.appendChild(library);
            }

            //Show all indexes of the new library
            System.out.println("\nBefore: ");
            rep.showAllIndexes(library);

            //get the index list of the library
            XhiveIndexListIf indexList = library.getIndexList();

            //add a library id index to the library
            String idIndexName = "Library ID Index";
            XhiveIndexIf idIndex = indexList.addLibraryIdIndex(idIndexName);

            //show all existing indexes - after
            System.out.println("\n#After:");
            rep.showAllIndexes(library);

            //remove the indexes from the index list
            indexList.removeIndex(idIndex);

            idIndex = indexList.addLibraryIdIndex(idIndexName, XhiveIndexIf.KEY_SORTED);

            //remove the index from the index list
            indexList.removeIndex(idIndex);

            session.commit();

        } catch (Exception e) {

            System.out.println("LibraryIndexes sample failed: ");
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

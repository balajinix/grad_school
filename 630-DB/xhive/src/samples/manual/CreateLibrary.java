package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to create a new library
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.CreateLibrary
 *
 */
public class CreateLibrary {

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

            //show all existing libraries - before
            System.out.println("\n#Before:");
            rep.showAllLibraries(united_nations_db);

            // get a handle to the root library
            XhiveLibraryIf rootLibrary = united_nations_db.getRoot();

            // create a library (unless it already exists)
            XhiveLibraryIf newLibA = null;

            if (rootLibrary.nameExists("Publications")) {
                newLibA = (XhiveLibraryIf) rootLibrary.get("Publications");
            } else {
                // create a library
                newLibA = rootLibrary.createLibrary();

                // give the new library a name
                newLibA.setName("Publications");

                // append the new libary to its parent
                rootLibrary.appendChild(newLibA);
            }

            // create a library which is a sublibrary of newLibA (unless it already exists)
            if (!(newLibA.nameExists("General Info"))) {

                // create a library which is a sublibrary of newLibA
                // and is in the same locking context
                XhiveLibraryIf newLibA1 = newLibA.createLibrary(XhiveLibraryIf.LOCK_WITH_PARENT);

                // give the new library a name
                newLibA1.setName("General Info");

                // append the new libary to its parent
                newLibA.appendChild(newLibA1);
            }


            //show all existing libraries - after
            System.out.println("\n#After:");
            rep.showAllLibraries(united_nations_db);

            session.commit();

        } catch (Exception e) {

            System.out.println("CreateLibrary sample failed: ");
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

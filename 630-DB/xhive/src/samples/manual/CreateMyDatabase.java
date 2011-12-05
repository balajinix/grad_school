package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveFederationIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to create a new database
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.CreateDatabase
 *
 */
public class CreateMyDatabase {

    public static void main(String[] args) {

        // the name and password of the superuser
        String superUserName = SampleProperties.superuserName;
        String superUserPassword = "abc123";

        // the name of the database
        String databaseName = "630";

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

		System.out.println("\n#USername " + SampleProperties.superuserName);
		System.out.println("\n#Password " + SampleProperties.superuserPassword);
		System.out.println("\n#Password " + SampleProperties.administratorName);
		System.out.println("\n#Password " + SampleProperties.administratorPassword);

        // create a session
        XhiveDriverIf driver = XhiveDriverFactory.getDriver();
        driver.init(1024);
        XhiveSessionIf session = driver.createSession();

        try {

            // Only the superuser can create databases
            // The databasename parameter should be null, or empty
            session.connect(superUserName, superUserPassword, null);

            // create a Reporter for some nice output
            Reporter rep = new Reporter();

            // begin the trans
            session.begin();

            // show all existing databases - before
            System.out.println("\n#Before:");
            rep.showAllDatabases(session);

            // The federation object is used to manage databases
            XhiveFederationIf federation = session.getFederation();

            if (federation.hasDatabase(databaseName)) {
                System.out.println("\n#Database already exists: " + databaseName);
            } else {
                System.out.println("\n#Create new database with name " + databaseName);

                // create the database
                // (null configuration file (means default configuration),
                // print debug output to console)
                federation.createDatabase(databaseName, administratorPassword, null, System.out);
            }

            //show all existing databases - after
            System.out.println("\n#After:");
            rep.showAllDatabases(session);

            session.commit();

        } catch (Exception e) {

            System.out.println("CreateMyDatabase sample failed: ");
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

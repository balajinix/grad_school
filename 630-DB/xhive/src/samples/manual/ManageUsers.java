package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveGroupIf;
import com.xhive.core.interfaces.XhiveGroupListIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveUserIf;
import com.xhive.core.interfaces.XhiveUserListIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to manage users and groups within X-Hive/DB
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
public class ManageUsers {

    public static void main(String[] args) {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        String userName = "sampleUser";
        String userPassword = "password";
        String groupName = "sampleGroup";

        // create a session
        XhiveDriverIf driver = XhiveDriverFactory.getDriver();
        driver.init(1024);
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

            XhiveUserListIf userList = united_nations_db.getUserList();
            XhiveGroupListIf groupList = united_nations_db.getGroupList();

            System.out.println("\n#Add user " + userName);
            // create a new user... (unless it already exists)
            if (!userList.hasUser(userName)) {
                userList.addUser(userName, userPassword);
            }

            System.out.println("\n#Add group " + groupName);
            // create a new group...  (unless it already exists)
            if (!groupList.hasGroup(groupName)) {
                groupList.addGroup(groupName);
            }

            System.out.println("\n#Add user " + userName + " to group " + groupName);

            // add user to group...  (unless it is already a member)
            XhiveUserIf user = userList.getUser(userName);
            XhiveGroupIf group = groupList.getGroup(groupName);
            if (!group.isMember(user)) {
                user.addGroup(group);
            }

            // show results...
            Reporter reporter = new Reporter();
            reporter.showAllUsers(united_nations_db);
            reporter.showAllGroups(united_nations_db);
            reporter.showAllUsersOfGroup(groupList.getGroup(groupName));
            reporter.showAllGroupsOfUser(userList.getUser(userName));
            reporter.showAllDocumentsOfUser(session.getUser(), united_nations_db);

            session.commit();

        } catch (Exception e) {

            System.out.println("ManageUsers sample failed: ");
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

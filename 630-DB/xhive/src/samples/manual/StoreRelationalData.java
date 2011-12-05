package samples.manual;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.util.interfaces.XhiveCSVFileLoaderIf;
import org.w3c.dom.Document;
import samples.Reporter;

import java.io.FileInputStream;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to load non-XML data into the database
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.StoreRelationalData
 *
 */
public class StoreRelationalData {

    public static void main(String[] args) {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        // the name of the external file to import
        String fileName = "../src/samples/data/manual/un_members.csv";

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

            XhiveDatabaseIf database = session.getDatabase();
            XhiveLibraryIf rootLibrary = database.getRoot();

            // get the XhiveCSVLoader
            FileInputStream input = new FileInputStream(fileName);
            XhiveCSVFileLoaderIf loader = XhiveDriverFactory.getDriver().getCSVFileLoader(input);

            // Set the options to load the data with
            loader.setSeparator(',');
            loader.setEscape('\\');
            loader.setEnclose('"');
            loader.setHeadersIncl(true);
            loader.setRowName("member");
            loader.setColumnNames(new String[]{"name", "admission_date", "additional_note"});
            loader.setColumn2Attribute(new boolean[]{false, false, false});

            // Create a document that will serve as target for the data
            Document un_members_doc = rootLibrary.createDocument(null, "UN_members", null);

            // Set document(-element) as the target for the load
            loader.setTargetDocument(un_members_doc, un_members_doc.getDocumentElement());

            // Perform the actual load
            loader.loadCSVData();

            System.out.println("\n#XML output of imported file:\n" + un_members_doc + "\n");

            // store the document in the root library
            rootLibrary.appendChild(un_members_doc);

            session.commit();

        } catch (Exception e) {

            System.out.println("StoreRelationalData sample failed: ");
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

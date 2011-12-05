package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.versioning.interfaces.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.io.File;
import java.util.Calendar;
import java.util.Iterator;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to use versioning in X-Hive/DB
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.Versioning
 *
 */
public class Versioning {

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

            // get/create the "UN Daily Briefings" library
            XhiveLibraryIf briefingLib = DataLoader.createLibrary(united_nations_db, rootLibrary, "UN Daily Briefings", session);

            String docName = "briefing.xml";
            String parsedFileName = SampleProperties.baseDir + docName;

            // load the sample document
            Document doc = DataLoader.storeDocument(briefingLib, parsedFileName, false, docName);

            // make document "briefing.xml" versionable
            if ( ((XhiveDocumentIf)doc).getXhiveVersion() == null ) {
                System.out.println("\n#make doc versionable...");
                XhiveVersionIf firstVersion = ((XhiveDocumentIf)doc).makeVersionable();
                firstVersion.addLabel("original version");
            } else {
                System.out.println("\n#doc already versioned...");
            }

            // get the last version of this document
            XhiveVersionIf lastVersion = ((XhiveDocumentIf)doc).getXhiveVersion();

            // show information about this version
            showVersionInfo(lastVersion);

            // do a check out of the last version
            Document lastVersionDoc = lastVersion.checkOut();

            // add an element to the document
            Element rootElement = lastVersionDoc.getDocumentElement();
            Element newItem = lastVersionDoc.createElement("item");
            String now = Calendar.getInstance().getTime().toString();
            Text itemText = lastVersionDoc.createTextNode(now + " - NEW ITEM");
            rootElement.appendChild(newItem);
            newItem.appendChild(itemText);

            // show document before check in
            System.out.println("\n#before check in:\n" + doc.toString());

            // do a check in of the latest, edited version
            lastVersion.checkIn(lastVersionDoc);

            // show document after check in
            System.out.println("\n#after check in:\n" + doc.toString());

            // retrieve the versionspace of the document
            XhiveVersionSpaceIf versionSpace = ((XhiveDocumentIf)doc).getXhiveVersion().getVersionSpace();

            // get version 1.1 of the document
            XhiveVersionIf version1_1 = versionSpace.getVersionById("1.1");
            Document doc1_1 = version1_1.getAsDocument();
            System.out.println("\n#version 1.1:\n" + doc1_1.toString());

            session.commit();

        } catch (Exception e) {

            System.out.println("Versioning sample failed: ");
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


    public static void showVersionInfo(XhiveVersionIf version) {

        System.out.println("id           : " + version.getId());
        System.out.println("creation date: " + version.getDate().toString());
        Iterator labels = version.getLabels();
        if (labels.hasNext()) {
            System.out.println("label        : " + labels.next());
        }
        System.out.println("created by   : " + version.getCreator().getName());
    }


}


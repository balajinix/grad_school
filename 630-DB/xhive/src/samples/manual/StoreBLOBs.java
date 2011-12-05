package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveBlobNodeIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;


/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to store BLOBs
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.StoreBLOBs
 *
 */
public class StoreBLOBs {

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

            // get/create the "UN Charter" library
            XhiveLibraryIf charterLib = DataLoader.createLibrary(united_nations_db, rootLibrary, "UN Charter", session);

            String imgFileName = "un_flags.gif";
            String imgName = "Flags of UN members";
            XhiveBlobNodeIf img = null;

            if ( !charterLib.nameExists(imgName) ) {

                // create a BLOB node
                img = charterLib.createBlob();

                // set the contents and name of the BLOB node
                img.setContents(new FileInputStream(SampleProperties.baseDir + imgFileName));
                img.setName(imgName);

                // append the BLOB node to the library
                charterLib.appendChild(img);
            } else {

                // BLOB node already exists, retrieve it:
                img = (XhiveBlobNodeIf)charterLib.get(imgName);
            }

            // retrieve the contents of the BLOB node
            InputStream in = img.getContents();

            // show size of the BLOB node
            System.out.println( "size of image \"" + imgName + "\": " + img.getSize() + " bytes");

            // output the image to a new file
            FileOutputStream out = new FileOutputStream(SampleProperties.baseDir + "copy_of_" + imgFileName);
            byte[] buffer = new byte[(int)img.getSize()];
            int length;
            while((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            out.close();
            in.close();

            System.out.println("image \"copy_of_" + imgFileName + "\" has been created in directory \"" + SampleProperties.baseDir + "\"");
            session.commit();


        } catch (Exception e) {

            System.out.println("StoreBLOBs sample failed: ");
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

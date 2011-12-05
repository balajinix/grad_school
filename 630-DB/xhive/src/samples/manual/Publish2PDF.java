package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.util.interfaces.XhiveFormatterIf;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to publish XML documents stored in X-Hive/DB as PDF files
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.Publish2PDF
 *
 */
public class Publish2PDF {

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

            // load the sample documents
            DataLoader.storeDocuments(united_nations_db, charterLib, session);

            // parse the XSL source file
            String xslFileName = SampleProperties.baseDir + "publish2PDF.xsl";
            Document xslDocument = charterLib.createLSParser().parseURI(new File(xslFileName).toURL().toString());

            // retrieve the document to publish
            Document firstDocument = (Document) charterLib.get("UN Charter - Chapter 1");

            XhiveFormatterIf formatter = XhiveDriverFactory.getDriver().getFormatter();

            File pdfFile = new File(SampleProperties.baseDir + "output.pdf");

            // format the XML document as PDF using the parsed XSL document to a file
            FileOutputStream os = new FileOutputStream(pdfFile);
            formatter.formatAsPDFToStream(rootLibrary, firstDocument, xslDocument, os);
            os.close();

            System.out.println("Written PDF ouput to: " + pdfFile.getAbsolutePath());

            session.commit();

        } catch (Exception e) {

            System.out.println("Publish2PDF sample failed: ");
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

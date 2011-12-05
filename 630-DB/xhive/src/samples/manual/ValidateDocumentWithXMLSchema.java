package samples.manual;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.as.ASModel;

import java.io.File;
import java.util.Iterator;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to validate documents with xml schema
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.ValidateDocumentWithXMLSchema
 *
 */
public class ValidateDocumentWithXMLSchema {


    public static void main(String[] args) {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        // the name of the files to store/append
        final String fileName = "../src/samples/data/manual/personal-schema.xml";

        //the xml schema uri
        final String schemaTypeURI = "http://www.w3.org/2001/XMLSchema";

        // the name of the library where the document is stored
        final String libraryName = "library";



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
            XhiveDatabaseIf database = session.getDatabase();

            // get a handle to the root library
            XhiveLibraryIf rootLibrary = database.getRoot();

            // get the library. If the library is not found, create a new library.
            XhiveLibraryIf library = (XhiveLibraryIf)rootLibrary.get(libraryName);
            if (library == null) {
                library = rootLibrary.createLibrary();
                rootLibrary.appendChild(library);
                library.setName(libraryName);
            }

            // parse a document with XML schema and add the document to the library

            LSParser builder = library.createLSParser();
            DOMConfiguration config = builder.getDomConfig();
            // set validation to true to enable validated parsing
            config.setParameter("validate", Boolean.TRUE);
            // store psvi information. This setting is only required when nodes must store psvi information.
            config.setParameter("xhive-psvi", Boolean.TRUE);
            // in order to set a schema-location, the schema type must be set
            config.setParameter("schema-type", schemaTypeURI);
            // set the schema by using the schema location parameter. This setting is already defined in the document by
            // the noNamespaceSchemaLocation attribute, so this parameter is only set to show how to use it.
            config.setParameter("schema-location", "personal.xsd");


            System.out.println("# Load a document");

            // parse the document
            XhiveDocumentIf document = (XhiveDocumentIf) builder.parseURI(new File(fileName).toURL().toString());

            System.out.println("\n# Append the document to the library");

            // append the document to the library
            library.appendChild(document);

            // get the normalization configuration of the document to set normalization settings
            config = document.getConfig();

            config.setParameter("validate", Boolean.TRUE);

            config.setParameter("error-handler", new SimpleDOMErrorPrinter());

            System.out.println("\n# Normalize document, no errors should occur");
            // the document is valid, so, during validation, the errorhandler should not print any errors
            document.normalizeDocument();

            // get the schemaIds of the ASModels attached to the document
            String schemaIds = (String)config.getParameter("xhive-schema-ids");
            System.out.println("\n# The schema ids of the models used is: " + schemaIds);

            // make the document invalid by removing the id attribute of the first person element

            // get a person element
            Element person = (Element) document.getElementsByTagNameNS(null, "person").item(5);

            System.out.println("\n# Make the document invalid by removing the id attribute of the first \"person\" element");
            // make the person element invalid by removing its id attribute
            person.removeAttributeNode(person.getAttributeNodeNS(null, "id"));

            System.out.println("\n# Normalize document again, now errors should be printed");
            // the document is no longer valid, so, during validation, the errorhandler should print errors
            document.normalizeDocument();



            session.commit();

        } catch (Exception e) {

            System.out.println("ValidateDocumentWithXMLSchema sample failed: ");
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

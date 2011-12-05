package samples.manual;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import org.apache.xerces.xs.AttributePSVI;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.ItemPSVI;
import org.apache.xerces.xs.XSTypeDefinition;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSParser;

import java.io.File;


/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to access psvi information of elements and attributes
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.PSVI
 *
 */
public class PSVI {

    public static void main(String[] args) {
        PSVI psvi = new PSVI();
        psvi.runSample();
    }

    private String getStringValue (short validity) {
        switch (validity) {
        case ItemPSVI.VALIDITY_INVALID   :
            return "invalid";
        case ItemPSVI.VALIDITY_NOTKNOWN  :
            return "unknown";
        case ItemPSVI.VALIDITY_VALID     :
            return "valid";
        }
        return "";
    }

    public void runSample() {

        // the name of the database
        String databaseName = SampleProperties.databaseName;

        // the name and password of the administrator of the database
        String administratorName = SampleProperties.administratorName;
        String administratorPassword = SampleProperties.administratorPassword;

        // the name of the files to store/append
        String fileName = "../src/samples/data/manual/personal-schema.xml";

        // the name of the library where the document is stored
        String libraryName = "library";



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

            LSParser parser = library.createLSParser();
            DOMConfiguration config = parser.getDomConfig();
            // set validation to true to enable validated parsing
            config.setParameter("validate", Boolean.TRUE);
            // store psvi information
            config.setParameter("xhive-psvi", Boolean.TRUE);

            System.out.println("# Load a document");

            // parse the document
            Document document = parser.parseURI(new File(fileName).toURL().toString());

            System.out.println("\n# Append the document to the library");

            // append the document to the library
            library.appendChild(document);

            Element email = (Element) document.getElementsByTagNameNS(null, "email").item(0);

            // get the ElementPSVI of the first element with tagname "email" by casting the element to ElementPSVI
            ElementPSVI elemPsvi = (ElementPSVI) email;

            // get the type definition of the element
            XSTypeDefinition elemTypeDef = elemPsvi.getTypeDefinition();

            // print the type name and type namespace of element email
            System.out.println("\n# The type name of element \"email\" is \"" + elemTypeDef.getName()
                               + "\" of namespace \"" + elemTypeDef.getNamespace() + "\"");

            // print the validity of the element
            System.out.println("\n# The validity of element \"email\" is \"" + getStringValue(elemPsvi.getValidity()) + "\"");

            // get the first person element
            Element person = (Element) document.getElementsByTagNameNS(null, "person").item(0);

            // get the attribute psvi of attr with name id
            AttributePSVI attrPsvi = (AttributePSVI) person.getAttributeNodeNS(null, "id");

            // get the type definition of the attribute
            XSTypeDefinition attrTypeDef = attrPsvi.getTypeDefinition();

            // print the type name and type namespace of attribute id
            System.out.println("\n# The type name of attribute \"id\" is \"" + attrTypeDef.getName()
                               + "\" of namespace \"" + attrTypeDef.getNamespace() + "\"");

            // print the validity of the attribute
            System.out.println("\n# The validity of attribute \"id\" is \"" + getStringValue(attrPsvi.getValidity()) + "\"");

            session.commit();

        } catch (Exception e) {

            System.out.println("PSVI sample failed: ");
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

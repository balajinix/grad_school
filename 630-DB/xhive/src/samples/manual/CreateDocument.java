package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to create an XML document from scratch
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.CreateDocument
 *
 */
public class CreateDocument {

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
            XhiveDatabaseIf database = session.getDatabase();

            // get a handle to the root library
            XhiveLibraryIf rootLibrary = database.getRoot();
            System.out.println("\n#Create a new Document");
            DOMImplementation impl = rootLibrary;

            // create the document
            DocumentType docType = impl.createDocumentType("events", "publicId", "systemId");
            // (You can also create a document with a null document type!)
            Document eventsDocument = impl.createDocument(null, "events", docType);

            // get a handle to the root element of the document
            Element rootElement = eventsDocument.getDocumentElement();

            // add a comment to the document before the root element
            System.out.println("\n#Add a comment to node " + eventsDocument.getNodeName());
            Comment comment = eventsDocument.createComment("this document contains UN events");
            eventsDocument.insertBefore(comment, rootElement);

            // add a new element to root element
            System.out.println("\n#Add an element to node " + rootElement.getNodeName());
            Element eventElement = eventsDocument.createElement("event");
            rootElement.appendChild(eventElement);

            // add text value to the element
            System.out.println("\n#Add a text to node " + eventElement.getNodeName());
            Text eventText = eventsDocument.createTextNode("UNICEF, Executive Board, annual session");
            eventElement.appendChild(eventText);

            // add an attribute to the element
            System.out.println("\n#Add an attribute to element " + eventElement.getTagName());
            eventElement.setAttribute("occurrence", "year");

            // add a new element to event
            System.out.println("\n#Add an element  to node " + eventElement.getNodeName());
            Element dateElement = eventsDocument.createElement("date");
            eventElement.appendChild(dateElement);

            // add text value to the date element
            System.out.println("\n#Add a text to node " + dateElement.getNodeName());
            Text dateText = eventsDocument.createTextNode("4-8 June, 2001");
            dateElement.appendChild(dateText);

            //add to the (root) library
            rootLibrary.appendChild(eventsDocument);

            // show all descending nodes of the new document in document order...
            rep.showAllNodes(eventsDocument);

            // show the document
            System.out.println("\n#XML output of imported file:\n" + eventsDocument + "\n");

            // store the newly created document in the root library of the database
            // and give it a name
            String eventsDocumentName = "UN - events June 2001";

            if (!(rootLibrary.nameExists(eventsDocumentName))) {
                rootLibrary.appendChild(eventsDocument);
                ((XhiveDocumentIf) eventsDocument).setName(eventsDocumentName);
            }

            session.commit();


        } catch (Exception e) {

            System.out.println("CreateDocument sample failed: ");
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

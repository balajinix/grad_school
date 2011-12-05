package samples.manual;

import samples.Reporter;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import org.w3c.dom.Document;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.Iterator;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to retrieve parts of documents
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.RetrieveDocumentParts
 *
 */
public class RetrieveDocumentParts {

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

            // get the first document of the charter library
            Node firstDocument = charterLib.getFirstChild();

            // show all elements of the first document
            showChildren(firstDocument, 0);

            // get the value of attribute "number" of child "chapter"
            Node chapterNode = firstDocument.getFirstChild();
            String attributeValue = ((Element) chapterNode).getAttribute("number");
            System.out.println("\nattribute \"number\" of element \"chapter\" has value: " + attributeValue);

            // retrieve the value of attribute number of all articles using executeFullPathXPointerQuery
            System.out.println("\n#Retrieving value of attribute \"number\" via executeFullPathXPointerQuery...");
            Iterator articleNumbers = rootLibrary.executeFullPathXPointerQuery("/UN Charter/UN Charter - Chapter 1#xpointer(/chapter/article/@number)");
            while (articleNumbers.hasNext()) {
                String number = ((Attr)articleNumbers.next()).getValue();
                System.out.println(number);
            }

            // retrieve a single document using executeFullPathXPointerQuery
            String sampleLibName = "/UN Charter";
            String sampleDocName = "UN Charter - Chapter 5";
            String sampleDocPath = sampleLibName + "/" + sampleDocName;
            Document resultGetDocument = (Document)rootLibrary.getByPath(sampleDocPath);
            System.out.println("\n# executeFullPathXPointerQuery(\"" + sampleDocPath + "\") returns: ");
            System.out.println("document with ID = " +
                               ((XhiveLibraryChildIf) resultGetDocument).getId() +
                               ", name: " +
                               ((XhiveLibraryChildIf) resultGetDocument).getName());

            // retrieve all titles within the sample document
            String queryXPointer = "#xpointer(/descendant::title)";
            Iterator resultNodes = rootLibrary.executeFullPathXPointerQuery(sampleDocPath + queryXPointer);
            System.out.println("\n# executeFullPathXPointerQuery(\"" + sampleDocPath + queryXPointer + "\") returns: ");
            while (resultNodes.hasNext()) {
                Node resultNode = (Node) resultNodes.next();
                System.out.println(resultNode.getFirstChild().getNodeValue());
            }

            // retrieve all titles of sections within the sample document
            queryXPointer = "#xpointer(/chapter/section/title[1])";
            resultNodes = rootLibrary.executeFullPathXPointerQuery(sampleDocPath + queryXPointer);
            System.out.println("\n# executeFullPathXPointerQuery(\"" + sampleDocPath + queryXPointer + "\") returns: ");
            while (resultNodes.hasNext()) {
                Node resultNode = (Node) resultNodes.next();
                System.out.println(resultNode.getFirstChild().getNodeValue());
            }

            // retrieve the first paragraph of UN article #68
            queryXPointer = "#xpointer(/descendant::article[@number='68']/para[1])";

            // note that we only specify the library path and not the document name:
            resultNodes = rootLibrary.executeFullPathXPointerQuery(sampleLibName + queryXPointer);
            System.out.println("\n# executeFullPathXPointerQuery(\"" + sampleLibName + queryXPointer + "\") returns: ");
            while (resultNodes.hasNext()) {
                Node resultNode = (Node) resultNodes.next();
                System.out.println(resultNode.getFirstChild().getNodeValue());
            }

            session.commit();

        } catch (Exception e) {

            System.out.println("RetrieveDocumentParts sample failed: ");
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


    public static void showChildren(Node theNode, int level) {

        // some output formatting
        String indentation = "";
        for (int i = 0; i < level; i++) {
            indentation += "\t";
        }

        // check whether the node has any children
        if (theNode.hasChildNodes()) {

            // if so: get the first one
            Node n = theNode.getFirstChild();

            int j = 1;

            // as long as there are children...
            while (n != null) {

                // and child is of type 'element'...
                if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    // show the element...
                    System.out.println(indentation + "child " + j++ + " is a " + n.getNodeName());

                    // and try to find the children of this element (recursively)
                    showChildren(n, level + 1);
                }

                // get next child
                n = n.getNextSibling();
            }
        }
    }

}


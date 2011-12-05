package samples.manual;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.xlink.interfaces.XhiveArcIf;
import com.xhive.dom.xlink.interfaces.XhiveExtendedLinkIf;
import com.xhive.dom.xlink.interfaces.XhiveLinkBaseIf;
import com.xhive.dom.xlink.interfaces.XhiveLocatorIf;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSParser;

import java.io.File;
import java.util.Iterator;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to use the Dom-XLink API to handle linkbases and extended links.
 *
 * A linkbase (checkouts.xml) is used to store check-out information of document-parts.
 * The linkbase only maintains 'current' checkout information, no check-out' history is stored.
 *
 * The linkbase contains checkout-information about 3 UN documents, containing one chapter 
 * per document. For every document, checkout information is stored in the linkbase.
 * Therefore, the check-out information of each chapter is stored in a separate xlink extended link.
 *
 * The linkbase only uses exended links with 'remote resources'. All links are 'third-part' links.
 * Checkout information involves the following 'remote resources' :
 * (1) the current editor
 * (2) the 'checked-out' document-part
 * (3) the owner of the chapter.
 * This information is stored in xlink locators.
 * The 'check-out' relation between editor and document-part is stored in xlink arcs.
 *
 * The personal information about editors and owners of document(parts) is stored in staff.xml.
 *
 * This samples shows:
 * (1) Parse and store data used in this sample
 * (2) Retrieve check-out information from the linkbase by using the Dom XLink API
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated 
 *   in sample CreateDatabase.java
 *
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.DomLinkBase
 *
 */
public class DomLinkBase {

    private String fileLocation = SampleProperties.baseDir;
    private String staffFilePath = fileLocation + "staff.xml";
    private String checkoutLinkBasePath = fileLocation + "checkouts.xml";
    private String unFilePath = fileLocation + "un_charter_chapter";
    private String unDocName = "UN_Chapter";
    private String linkBaseName = "Checkout_Linkbase";
    private String staffDocName = "staff";
    private static final int numDocs = 3;

    public void runSample() {

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

            // begin the transaction
            session.begin();

            // get a handle to the database
            XhiveDatabaseIf united_nations_db = session.getDatabase();

            // get a handle to the root library
            XhiveLibraryIf rootLibrary = united_nations_db.getRoot();

            // get/create the "DomLinkBase" library
            XhiveLibraryIf library = DataLoader.createLibrary(united_nations_db, rootLibrary, "DomLinkBase", session);

            //////////////////////////////////////////////////////////////////////////////////
            //                                  Store data                                  //
            //////////////////////////////////////////////////////////////////////////////////

            System.out.println("\n#Parse and store " + numDocs + " un-docs");

            //Parse and store the un-data.
            Document document;
            LSParser builder = library.createLSParser();
            for (int k = 1; k <= numDocs; k++) {
                String fullDocName = unDocName + k;
                if (library.get(fullDocName) == null) {
                    document = builder.parseURI(new File(unFilePath + k + ".xml").toURL().toString());
                    // Append it to the library
                    library.appendChild(document);
                    ((XhiveDocumentIf) document).setName(fullDocName);
                }
            }

            System.out.println("\n#Parse and store linkbase");

            XhiveLinkBaseIf linkBase = (XhiveLinkBaseIf) library.get(linkBaseName);
            if (linkBase == null) {
                linkBase = (XhiveLinkBaseIf) builder.parseURI(new File(checkoutLinkBasePath).toURL().toString());
                library.appendChild(linkBase);
            }

            System.out.println("\n#Parse and store staff document");

            if (library.get(staffDocName) == null) {
                document = builder.parseURI(new File(staffFilePath).toURL().toString());
                library.appendChild(document);
                ((XhiveDocumentIf) document).setName(staffDocName);
            }

            //////////////////////////////////////////////////////////////////////////////////
            //                                  Read link information                       //
            //////////////////////////////////////////////////////////////////////////////////

            System.out.println("\n#Show all editors\n");

            //Gets an Iterator with all extended links
            for (Iterator i = linkBase.getLinksBy("type", "extended"); i.hasNext();) {
                XhiveExtendedLinkIf link = (XhiveExtendedLinkIf) i.next();
                System.out.println("       Title = " + link.getTitle());
                //Gets an Iterator with all locators
                for (Iterator j = link.getLocators(); j.hasNext();) {
                    XhiveLocatorIf locator = (XhiveLocatorIf) j.next();
                    if (locator.getRole().equals("CurrentEditor")) {
                        System.out.println("               *Editor = " + locator.getLabel());
                    }
                }
            }

            System.out.println("\n\n#Print the 'checked-out' document-parts of Chapter 2\n");

            //Gets an Iterator with links of title "Chapter 2"
            for (Iterator i = linkBase.getLinksByTitle("Chapter 2"); i.hasNext();) {
                XhiveExtendedLinkIf link = (XhiveExtendedLinkIf) i.next();
                for (Iterator j = link.getLocators(); j.hasNext();) {
                    //Gets an Iterator with all locators
                    XhiveLocatorIf locator = (XhiveLocatorIf) j.next();
                    if (locator.getRole().equals("DocumentPart")) {
                        System.out.println("\n**Document Part with label " + locator.getLabel() + " = \n" + (Node) locator.getReferencedResources().next());
                    }
                }
            }

            System.out.println("\n\n#Show the labels of the Document Parts checked out by Margaret Martin\n");

            //Gets an Iterator with all extended links
            for (Iterator i = linkBase.getLinksBy("type", "extended"); i.hasNext();) {
                XhiveExtendedLinkIf link = (XhiveExtendedLinkIf) i.next();
                System.out.println("       Title = " + link.getTitle());
                //Gets an Iterator with all arcs
                for (Iterator j = link.getArcs(); j.hasNext();) {
                    XhiveArcIf arc = (XhiveArcIf) j.next();
                    if (arc.getFrom().equals("Margaret Martin")) {
                        for (Iterator m = arc.getEndingResources(); m.hasNext();) {
                            XhiveLocatorIf locator = (XhiveLocatorIf) m.next();
                            System.out.println("               *Checked out resource = " + locator.getLabel());
                        }
                    }
                }
            }

            System.out.println("\n\n#Print the personal information of all editors of chapter 2");

            //Gets an Iterator with links of title "Chapter 2"
            for (Iterator i = linkBase.getLinksByTitle("Chapter 2"); i.hasNext();) {
                XhiveExtendedLinkIf link = (XhiveExtendedLinkIf) i.next();
                //Gets an Iterator with all locators
                for (Iterator j = link.getLocators(); j.hasNext();) {
                    XhiveLocatorIf locator = (XhiveLocatorIf) j.next();
                    if (locator.getRole().equals("CurrentEditor")) {
                        System.out.println("\n**Editor with label " + locator.getLabel() + " = \n" + (Node) locator.getReferencedResources().next());
                    }
                }
            }

            session.commit();

        } catch (Exception e) {

            System.out.println("DomLinkBase sample failed: ");
            e.printStackTrace();

        } finally {
            if (session.isOpen()) {
                session.rollback();
            }
            if (session.isConnected()) {
                session.disconnect();
            }
        }

        driver.close();
    }

    public static void main(String[] args) {
        DomLinkBase sample = new DomLinkBase();
        sample.runSample();
    }
}

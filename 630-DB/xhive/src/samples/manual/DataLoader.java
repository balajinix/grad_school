package samples.manual;

import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.error.XhiveException;

import org.w3c.dom.Document;
import org.w3c.dom.ls.LSParser;

import java.io.File;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This class loads the XML documents which are used in 
 * several samples.
 *
 */

public class DataLoader {

    public static XhiveLibraryIf createLibrary(XhiveDatabaseIf database, XhiveLibraryIf parent, String libraryName, XhiveSessionIf session) {

        // create a library (unless it already exists)
        XhiveLibraryIf newLib = null;
        if (parent.nameExists(libraryName)) {
            newLib = (XhiveLibraryIf) parent.get(libraryName);
        } else {
            // create a library
            newLib = parent.createLibrary();

            // give the new library a name
            newLib.setName(libraryName);

            // append the new libary to its parent
            parent.appendChild(newLib);
        }

        return newLib;
    }

    /**
     * Parse document and add document to the library if the document does not yet exist
     */
    public static Document storeDocument(XhiveLibraryIf library, String fileName, boolean validate, String documentName)
    throws Exception {
        LSParser parser = library.createLSParser();
        parser.getDomConfig().setParameter("validate", (validate ? Boolean.TRUE : Boolean.FALSE));
        return storeDocument(library, fileName, parser, documentName);
    }

    public static Document storeDocument(XhiveLibraryIf library, String fileName, LSParser builder, String documentName)
    throws Exception {
        Document doc = (Document)library.get(documentName);
        if (doc == null) {
            doc = builder.parseURI(new File(fileName).toURL().toString());
            library.appendChild(doc);
            ((XhiveDocumentIf)doc).setName(documentName);
        }
        return doc;

    }

    public static void storeDocuments(XhiveDatabaseIf database, XhiveLibraryIf library, XhiveSessionIf session) {

        // parse documents and add them to the charter library
        // (if they don't exist yet)
        Document newDocument = null;
        String parsedFileName = "";
        String newDocumentName = "";

        LSParser parser = library.createLSParser();
        for (int i = 1; i <= SampleProperties.numFiles; i++) {
            parsedFileName = SampleProperties.baseFileName + i + ".xml";
            newDocumentName = "UN Charter - Chapter " + i;

            try {
                newDocument = storeDocument(library, parsedFileName, parser, newDocumentName);
            } catch (Exception e) {

                if (session.isOpen()) {
                    session.rollback();
                }
            }
        }
    }
}

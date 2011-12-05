package samples.manual;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDatabaseIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

import java.io.File;
import java.net.MalformedURLException;
import org.w3c.dom.DOMException;

/**
 *
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * This sample shows how to store documents, where an entire directory structure is imported.
 * It does not use the sample framework, but instead traverses an entire directory structure.
 * Users can use this sample as a starting point for their own data-loaders.
 * Features include:
 *  - Recursive subdirectories loading,
 *  - Load only files with certain extensions,
 *  - Replace existing documents,
 *  - Check whether files are newer than documents in database (to prevent unnecessary loading).
 *
 * Please note that you can also use the import function of the administrator client for such
 * tasks.
 *
 * [DEPENDENCIES / PRE-REQUISITES]
 * - a database must be present, this database is generated
 *   in sample CreateDatabase.java
 
 * [RUNNING]
 * When using ant, start sample with command:
 * xhive-ant run-sample -Dname=manual.LoadDirectory
 *
 */
public class LoadDirectory {

    // Information on what to load and where to store it
    private String SRC_DIR_NAME = "../src/samples/data";
    private String TARGET_LIBRARY_NAME = "LoadDirectoryLibrary";
    private String DATABASE_NAME = SampleProperties.databaseName;
    private String USER_NAME = SampleProperties.administratorName;
    private String PASSWORD = SampleProperties.administratorPassword;

    // Storing options
    private boolean RECURSE_SUBDIRS = true;               // Enter into sub-directories
    private boolean REPLACE_EXISTING_DOCS = true;         // Replace documents with same name, or do not overwrite
    private String[] FILE_EXTENSIONS = {"xml", "xsl"};    // What file-extensions to load (null means attempt all
    private int LIBRARY_CREATION_OPTIONS = 0;                               // What options are used for the creation of sub-libraries
    private int CHECKPOINT_EVERY_N_DOCUMENTS = 25;                          // Checkpoint every N documents loaded, -1 means no checkpoints

    private int numDocsLoaded = 0;

    public static void main(String[] args) {
        new LoadDirectory().runSample();
    }

    private void runSample() {
        // create a session
        XhiveDriverIf driver = XhiveDriverFactory.getDriver();
        driver.init(1024);
        XhiveSessionIf session = driver.createSession();

        try {
            // open a connection to the database
            session.connect(USER_NAME, PASSWORD, DATABASE_NAME);

            // begin the transaction
            session.begin();

            // get a handle to the database
            XhiveDatabaseIf united_nations_db = session.getDatabase();

            // get a handle to the root library
            XhiveLibraryIf rootLibrary = united_nations_db.getRoot();

            // get/create the target-library
            XhiveLibraryIf targetLib = (XhiveLibraryIf) rootLibrary.get(TARGET_LIBRARY_NAME);
            if (targetLib == null) {
                // Library does not exist yet, create it
                targetLib = rootLibrary.createLibrary();
                targetLib.setName(TARGET_LIBRARY_NAME);
                rootLibrary.appendChild(targetLib);
            }

            File directory = new File(SRC_DIR_NAME);
            storeDocuments(session, targetLib, directory);

            session.commit();

        } catch (Exception e) {

            System.out.println("LoadDirectory sample failed: ");
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

    /**
     * Recursive method to store the files and sub-directories of 'dir' into 'targetLib'.
     */
    private void storeDocuments(XhiveSessionIf session, XhiveLibraryIf targetLib, File dir) {

        System.out.println("*Processing directory: " + dir.getAbsolutePath());
        File[] dirContents = dir.listFiles();
        if (dirContents == null) {
            dirContents = new File[0];
        }
        for (int i = 0; i < dirContents.length; i++) {
            File dirChild = dirContents[i];
            String dirChildName = dirChild.getName();
            if (!dirChild.isDirectory()) {
                // Is a regular file
                boolean nameExists = targetLib.nameExists(dirChildName);
                // Only add if correct extension...
                boolean doParseFile = extensionMatches(dirChildName, FILE_EXTENSIONS);
                // ... and not in database yet (or overwrite) ...
                doParseFile &= (!nameExists) || REPLACE_EXISTING_DOCS;
                // ... and if file is newer than document (if it exists)
                doParseFile &= fileIsNewer((XhiveDocumentIf) targetLib.get(dirChildName), dirChild);
                if (doParseFile) {
                    try {
                        XhiveDocumentIf newDoc = (XhiveDocumentIf) targetLib.createLSParser().parseURI(dirChild.toURL().toString());
                        numDocsLoaded++;
                        if (nameExists) {
                            // Remove existing document
                            targetLib.removeChild(targetLib.get(dirChildName));
                        }
                        newDoc.setName(dirChildName);
                        targetLib.appendChild(newDoc);
                        System.out.println("  Added " + dirChild.getName());
                    } catch (MalformedURLException mue) {
                        // 'never happens' (URLs are generated)
                        mue.printStackTrace();
                    }
                    catch (DOMException e) {
                        System.out.println("  Error: Parsing of " + dirChild.getAbsolutePath() + " failed because of: " + e.getMessage());
                    }
                } else {
                    // Do not parse file
                    System.out.print("  Skipped " + dirChild.getName());
                    // Reason?
                    if (! fileIsNewer((XhiveDocumentIf) targetLib.get(dirChildName), dirChild)) {
                        System.out.print("  (document in database is newer)");
                    } else if (! extensionMatches(dirChildName, FILE_EXTENSIONS)) {
                        System.out.print("  (file does not have correct extension)");
                    } else if (nameExists && (! REPLACE_EXISTING_DOCS)) {
                        System.out.print("  (document already in database)");
                    }
                    System.out.println();
                }

            } else {

                // Is a subdirectory
                if (RECURSE_SUBDIRS) {
                    // Check whether a sublibrary with this name already exists
                    XhiveLibraryIf subLib;
                    if (targetLib.nameExists(dirChildName)) {
                        subLib = (XhiveLibraryIf) targetLib.get(dirChildName);
                    } else {
                        // create new library
                        subLib = targetLib.createLibrary(LIBRARY_CREATION_OPTIONS);
                        subLib.setName(dirChildName);
                        targetLib.appendChild(subLib);
                    }
                    // Recursive load of sub-directory
                    storeDocuments(session, subLib, dirChild);
                }
            }

            // Perform a checkpoint?
            if ((CHECKPOINT_EVERY_N_DOCUMENTS != -1) && ((numDocsLoaded % CHECKPOINT_EVERY_N_DOCUMENTS) == 0)) {
                session.checkpoint();
            }
        }
    }

    /**
     * Helper-routine for storeDocuments, check whether the extension of a filename
     * matches the list of extensions to be parsed.
     */
    private boolean extensionMatches(String fileName, String[] allowedExtensions) {
        if (allowedExtensions == null) {
            // parse all extensions
            return true;
        } else {
            fileName = fileName.toLowerCase();
            for (int i = 0; i < allowedExtensions.length; i++) {
                if (fileName.endsWith(allowedExtensions[i])) {
                    return true;
                }
            }
            // No extension matches
            return false;
        }
    }

    /**
     * Helper-routine for storeDocuments, check whether a file to be loaded
     * is newer than the last-modified-date of an existing document.
     */
    private boolean fileIsNewer(XhiveDocumentIf existingDoc, File file) {
        if (existingDoc == null) {
            // No document yet, so file is definitely newer
            return true;
        } else {
            // Which was last changed?
            return (file.lastModified() > existingDoc.getLastModified().getTime());
        }
    }
}

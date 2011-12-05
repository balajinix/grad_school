package com.xhive.ftpserver;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.dom.interfaces.XhiveBlobNodeIf;
import com.xhive.dom.interfaces.XhiveCatalogIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveNodeIf;
import com.xhive.dom.interfaces.XhiveSchemaDocIf;
import com.xhive.error.XhiveException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.as.ASDOMBuilder;
import org.w3c.dom.as.ASModel;
import org.w3c.dom.as.DocumentAS;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 *
 * [DESCRIPTION]
 * By implementing a FTP server, this sample code shows how easy it is to perform
 * manipulations to an X-Hive database via the X-Hive API. The FTP interface
 * provided by this sample enables easy storage and retrieval of documents in a database and allows
 * FTP enabled editors like XML Spy to edit documents.
 *
 * When using ant, start sample with command (from the xhive\bin directory):
 *  xhive-ant run-ftpserver -Ddbname=DatabaseName
 *
 */
public class XhiveFtpServer extends Thread {

    // Configuration variables

    /** Default FTP connection port */
    private static final int CONNECT_PORT = 21;
    /** The display name of the catalog */
    private static final String CATALOG_DIR_NAME = "xhive-catalog";
    /** File extensions which are treated as XML documents (rather than BLOBs) */
    private static final String[] XML_EXTENSIONS = {"xml", "xsl", "xsd"};
    /** Parsing options */
    private static final int PARSE_OPTIONS =
        XhiveLibraryIf.PARSER_NO_VALIDATION | XhiveLibraryIf.PARSER_NAMESPACES_ENABLED |
        XhiveLibraryIf.PARSER_KEEP_WHITESPACES;
    /** Whether to show catalogs in the database */
    public static boolean USE_CATALOGS = true;
    /** Output directory listing in Unix or DOS format */
    private static final boolean DIRECTORY_LISTING_UNIX = false;

    private String DATABASE_NAME = null;
    /** Maintain a pool of sessions that can be reused */
    private XhiveDriverIf driver = null;
    private Stack unusedSessions;

    // Start the program.

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Please supply a databasename");
        } else {
            String databaseName = args[0];
            // For ant integration
            if (databaseName.equals("${dbname}")) {
                System.out.println("Please supply a databasename, use -Ddbname=DatabaseName");
                System.exit(0);
            }
            XhiveFtpServer server = new XhiveFtpServer(databaseName);
            ServerSocket listenSocket = server.initializeServer();
            server.waitForConnections(listenSocket);
        }
    }

    public XhiveFtpServer(String databaseName) {
        unusedSessions = new Stack();
        this.DATABASE_NAME = databaseName;
    }

    // Set up a socket listener and return it.

    private ServerSocket initializeServer() {
        ServerSocket listenSocket = null;
        String hostName = "";

        try {
            listenSocket = new ServerSocket(CONNECT_PORT);
        } catch (IOException e) {
            System.err.println("Exception during socket creation: " + e);
            System.exit(-1);
        }

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            hostName = "Could_not_determine_hostname";
        }

        System.out.println("***  X-Hive FTP Server started  ***");
        System.out.println("");
        System.out.println("Hostname:          " + hostName);
        System.out.println("Port number:       " + CONNECT_PORT);
        System.out.println("Database:          " + DATABASE_NAME);
        System.out.println("Directory listing: " + (DIRECTORY_LISTING_UNIX ? "Unix" : "DOS"));
        System.out.println("");
        System.out.println("Use CTRL-C to stop the application");
        System.out.println("");
        System.out.println("***  Event log  ***");
        System.out.println("");

        return listenSocket;
    }

    // Process incoming Ftp-connections.

    private void waitForConnections(ServerSocket listenSocket) {
        int connectionNumber = 0;

        try {
            while (true) {
                // handle a connection
                Socket clientSocket = listenSocket.accept();
                FTPClient c = new FTPClient(clientSocket, connectionNumber++);
            }
        } catch (IOException e) {
            System.err.println("Exception while listening for connections: " + e);
            System.exit(-1);
        }
    }

    /*****************************************************************/

    // Inner class that handles the FTP connections.

    class FTPClient extends Thread {

        // FTP variables
        private int connectionId;
        private Socket controlSocket;
        private Socket dataSocket;
        private ServerSocket serverSocket;
        private InetAddress clientAddress;
        private InetAddress localAddress;
        private BufferedReader in;
        private PrintWriter out;

        // X-Hive database variables
        private XhiveSessionIf session;
        private boolean connected = false;

        // Directory vs Library navigating variables
        private String currentDirectory = "";
        private String renameFromLibraryName;
        private String renameFromNodeName;

        // User variables
        private String user = "";
        private String password = "";

        // ****  Functions for handling FTP Commands  ****

        // Initialize the connection

        public FTPClient(Socket clientSocket, int id) {

            // Get a session from the unused pool, or create a new one
            synchronized (unusedSessions) {
                if (unusedSessions.empty()) {
                    log("Created new session");
                    if (driver == null) {
                        driver = XhiveDriverFactory.getDriver();
                        driver.init(200);
                    }
                    session = driver.createSession();
                    session.leave();
                } else {
                    session = (XhiveSessionIf) unusedSessions.pop();
                }
            }

            controlSocket = clientSocket;
            connectionId = id;

            try {
                clientAddress = controlSocket.getInetAddress();
                localAddress = InetAddress.getLocalHost();

                in = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
                out = new PrintWriter(controlSocket.getOutputStream(), true);

                out.println("220 *** Welcome to the Xhive FTP server. ***\r");
                log("New connection from " + clientAddress);

            } catch (IOException e) {
                logException(e);
                controlSocket = null;
            }

            if (controlSocket != null)
                this.start();
        }

        // Handle one FTP-session.

        public void run() {
            try {
                boolean done = false;

                while (!done) {
                    // ********  Process commandline  *******************************************************

                    String line = in.readLine();

                    if (line == null) {
                        break;
                    }

                    if (line.length() >= 4 && line.charAt(1) == '\u00F4' && line.charAt(3) == '\u00F2') {
                        line = line.substring(4);
                    }

                    int index = line.indexOf(" ");
                    String command, argument;

                    if (index != -1) {
                        command = line.substring(0, index);
                        argument = line.substring(line.indexOf(" ") + 1).replace('\\', '/');

                        if (argument.length() > 1 && argument.substring(argument.length() - 1).equals("/")) {
                            argument = argument.substring(0, argument.length() - 1);
                        }
                    } else {
                        command = line;
                        argument = "";
                    }

                    command = command.toUpperCase();

                    // ****  Show commandline for debug purposes, mask password  ****

                    if (!command.equals("PASS")) {
                        log("CMD: " + line);

                        if (!command.equals("RNTO")) {
                            renameFromLibraryName = null;
                            renameFromNodeName = null;
                        }
                    } else {
                        log("CMD: PASS *password not shown*");
                    }

                    // ********  Execute requested command  **************************************************

                    // ****  Access-Control commands  ****

                    if (session.isConnected()) {
                        sessionBegin();
                    }

                    if (command.equals("USER")) {
                        user = argument;
                        out.println("331 User okay, password needed");
                    } else if (command.equals("PASS")) {
                        password = argument;
                        if (connectToDatabase(user, password)) {
                            out.println("230-Welcome to the X-Hive FTP Server.");
                            out.println("230-You are connected to database:");
                            out.println("230- " + DATABASE_NAME);
                            out.println("230 User " + user + " logged in.");
                        } else {
                            out.println("530 User not logged in.");
                        }
                    }

                    // ****  Document manipulation commands  ****

                    else if (command.equals("RETR")) {
                        String documentName = extractFileNameFromFile(argument);
                        XhiveLibraryIf library = getLibraryFromFile(argument);

                        handleRetrieve(library, documentName);
                    } else if (command.equals("STOR")) {
                        out.println("150 Binary data connection");
                        String documentName = extractFileNameFromFile(argument);
                        XhiveLibraryIf library = getLibraryFromFile(argument);

                        handleStore(library, documentName);
                    } else if (command.equals("DELE")) {
                        String documentName = extractFileNameFromFile(argument);
                        XhiveLibraryIf library = getLibraryFromFile(argument);

                        if (handleDelete(library, documentName)) {
                            out.println("250 Deleted " + documentName);
                        } else {
                            out.println("550 " + documentName + ": No such file or no permission");
                        }
                    } else if (command.equals("RNFR")) {
                        boolean result = true;

                        try {
                            renameFromLibraryName = getAbsolutePath(extractPathFromFile(argument));
                            renameFromNodeName = extractFileNameFromFile(argument);
                            sessionCommit();
                        } catch (Exception e) {
                            result = false;
                            sessionRollback();
                        }

                        if (result) {
                            out.println("350 Source exists, ready for destination name");
                        } else {
                            out.println("550 Source does not exist.");
                        }
                    } else if (command.equals("RNTO")) {
                        XhiveLibraryIf renameToLibrary = getLibraryFromFile(argument);
                        XhiveLibraryIf renameFromLibrary = getLibrary(renameFromLibraryName);
                        Node renameFromNode = null;
                        if (renameFromLibrary != null) {
                            if (renameFromLibrary instanceof XhiveCatalogIf) {
                                renameFromNode = (Node) catalogGetSchemaDocument(renameFromLibrary, renameFromNodeName);
                            } else {
                                renameFromNode = renameFromLibrary.get(renameFromNodeName);
                            }
                        }
                        String renameToName = extractFileNameFromFile(argument);

                        if (handleRename(renameFromLibrary, renameFromNode, renameToLibrary, renameToName)) {
                            out.println("250 Command okay");
                        } else {
                            out.println("553 Destination already exists or source not found");
                        }
                    }

                    // ****  Directory manipulation commands  ****

                    else if (command.equals("CWD")) {
                        if (changeDirectory(argument)) {
                            out.println("250 Command succesful");
                        } else {
                            out.println("550 Cannot find directory " + argument + ".");
                        }
                    } else if (command.equals("CDUP")) {
                        changeDirectory("..");
                        out.println("250 CWD command succesful");
                    } else if (command.equals("PWD") || command.equals("XPWD")) {
                        out.println("257 \"/" + currentDirectory + "\" is current directory");
                    } else if (command.equals("MKD") || command.equals("XMKD")) {
                        String libraryName = extractFileNameFromFile(argument);
                        XhiveLibraryIf library = getLibraryFromFile(argument);

                        if (handleMakeLibrary(library, libraryName)) {
                            out.println("257 Directory " + libraryName + " created.");
                        } else {
                            out.println("550 Directory " + libraryName + " could not be created.");
                        }
                    } else if (command.equals("RMD") || command.equals("XRMD")) {
                        String libraryName = extractFileNameFromFile(argument);
                        XhiveLibraryIf library = getLibraryFromFile(argument);

                        if (handleRemoveLibrary(library, libraryName)) {
                            out.println("250 Directory " + libraryName + " removed.");
                        } else {
                            out.println("550 Directory " + libraryName + " could not be removed or permission denied.");
                        }
                    } else if (command.equals("LIST")) {
                        XhiveLibraryIf libraryToList = getLibrary(currentDirectory);
                        String fileName = "";

                        if (!argument.equals("")) {
                            String absolutePath = getAbsolutePath(argument);
                            libraryToList = getLibrary(absolutePath);

                            if (libraryToList == null) {
                                libraryToList = getLibrary(extractPathFromFile(absolutePath));
                                fileName = extractFileNameFromFile(absolutePath);
                            }

                        }

                        Vector entries = getListEntries(libraryToList, fileName);
                        if (entries != null) {
                            try {
                                out.println("150 ASCII data");
                                PrintWriter out2 = new PrintWriter(dataSocket.getOutputStream(), true);

                                for (int i = 0; i < entries.size(); i++) {
                                    out2.println((String) entries.elementAt(i));
                                }
                                out.println("226 transfer complete");
                                dataSocket.close();

                            } catch (IOException e) {
                                logException(e);
                            }
                        } else {
                            out.println("450 Could not find directory " + argument);
                        }
                    }

                    // ****  Miscellaneous commands  ****

                    else if (command.equals("TYPE")) {
                        out.println("200 Type set");
                    } else if (command.equals("QUIT")) {
                        out.println("Good Bye");
                        done = true;
                    } else if (command.equals("ABOR")) {
                        out.println("426 Transfer aborted abnormally");
                        out.println("226 Transfer aborted");
                        dataSocket.close();
                    } else if (command.equals("SYS")) {
                        out.println("500 SYS not understood");
                    } else if (command.equals("PORT")) {
                        argument = argument.replace(',', '.');
                        index = argument.lastIndexOf('.', argument.lastIndexOf('.') - 1);
                        String host = argument.substring(0, index);
                        argument = argument.substring(index + 1);
                        index = argument.indexOf('.');
                        int ip = 256 * Integer.parseInt(argument.substring(0, index)) +
                                 Integer.parseInt(argument.substring(index + 1));

                        dataSocket = new Socket(host, ip);
                        out.println("200 PORT command successful");
                    } else if (command.equals("PASV")) {
                        serverSocket = new ServerSocket(0);
                        int port = serverSocket.getLocalPort();
                        String ipAddress = localAddress.getHostAddress().replace('.', ',');
                        out.println("227 Entering Passive Mode (" + ipAddress + "," + port / 256 + "," + port % 256 + ").");
                        dataSocket = serverSocket.accept();
                    }

                    // ****  Command is not implemented  ****

                    else {
                        out.println("502 Command not implemented");
                    }

                    if (session.isOpen()) {
                        session.commit();
                    }

                } // after quit
            }
            catch (SocketException e) {}

            catch (Exception e) {
                logException(e);
            }

            try {
                controlSocket.close();

                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (Exception e) {}


            disconnectFromDatabase();

            log("Connection closed");
        }

        // ****  Functions that handle the FTP commands  ****

        private boolean handleRetrieve(XhiveLibraryIf library, String documentName) {
            boolean isCatalog = library instanceof XhiveCatalogIf;
            boolean result = true;

            try {
                if (!isCatalog) {
                    // get the document
                    int docId = -1;

                    try {
                        docId = Integer.parseInt(documentName);
                        log("Using document '" + documentName + "' as id");
                    } catch (Exception e) {
                        docId = -1;
                    }

                    Node document;
                    if (docId == -1) {
                        document = library.get(documentName);
                    } else {
                        document = library.get(docId);
                    }

                    switch (document.getNodeType()) {
                    case Node.DOCUMENT_NODE:
                        // Try to make doctypes information match catalog

                        if (USE_CATALOGS) {
                            ((XhiveDocumentIf) document).fixupDoctypeIds(CATALOG_DIR_NAME);
                        }

                        out.println("150 Binary data connection");
                        LSSerializer writer = library.createLSSerializer();
                        LSOutput output = library.createLSOutput();
                        output.setByteStream(dataSocket.getOutputStream());
                        writer.write(document, output);
                        break;
                    case XhiveNodeIf.BLOB_NODE:
                        XhiveBlobNodeIf blobDocument = (XhiveBlobNodeIf) document;
                        InputStream docStream = blobDocument.getContents();

                        out.println("150 Binary data connection");
                        OutputStream outS = dataSocket.getOutputStream();
                        byte[] buffer = new byte[2048];
                        int length;
                        while ((length = docStream.read(buffer)) != -1) {
                            outS.write(buffer, 0, length);
                        }
                        break;
                    default:
                        log("Unknown document being retrieved");
                    }
                } else {
                    out.println("150 Binary data connection");
                    String docText = catalogSerializeSchema(library, documentName);
                    PrintWriter outS = new PrintWriter(dataSocket.getOutputStream(), true);
                    outS.println(docText);
                    outS.close();
                }

                out.println("226 transfer complete");
                dataSocket.close();

                sessionCommit();

            } catch (Exception e) {
                result = false;
                sessionRollback();

                try {
                    if (dataSocket != null) {
                        out.println("550 " + documentName + ": document could not be found.");
                        dataSocket.close();
                    }
                } catch (Exception ee) {
                    logException(ee);
                }
            }

            return result;
        }

        private boolean handleStore(XhiveLibraryIf library, String documentName) {
            boolean isCatalog = library instanceof XhiveCatalogIf;
            boolean result = true;

            if (!documentName.equals("")) {
                try {

                    if (isCatalog) {
                        // Always threat content as a DTD or XML Schema (based on extension), if it fails, it fails...
                        boolean isXMLSchema = documentName.endsWith(".xsd");
                        XhiveCatalogIf catalog = (XhiveCatalogIf) library;
                        LSInput source = catalog.createLSInput();
                        source.setByteStream(dataSocket.getInputStream());
                        ASDOMBuilder builder = (ASDOMBuilder) catalog.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
                        String schemaType = isXMLSchema ? ASDOMBuilder.XML_SCHEMA_SCHEMA_TYPE : ASDOMBuilder.DTD_SCHEMA_TYPE;
                        if (isXMLSchema) {
                            // Name set before parsing
                            source.setSystemId(documentName);
                        }
                        ASModel schema = builder.parseASInputSource(source, schemaType);
                        if (!isXMLSchema) {
                            // name set after parsing
                            catalog.setSystemId(documentName, schema);
                            // The next call is questionable, but for our FTP-purposes, it is better
                            // when the DTD has a public ID
                            catalog.setPublicId(documentName, schema);
                        }
                    } else {
                        XhiveLibraryChildIf newDocument;
                        if (isXMLDocument(documentName)) {
                            // Parse the document
                            InputSource input = new InputSource(new DocTypeCorrectingReader(
                                                                    new InputStreamReader(dataSocket.getInputStream()), library.getCatalog(), this));

                            newDocument = (XhiveDocumentIf) library.parseDocument(input,
                                          PARSE_OPTIONS);
                        } else {
                            newDocument = library.createBlob();
                            ((XhiveBlobNodeIf) newDocument).setContents(dataSocket.getInputStream());
                        }

                        Node oldDocument = (Node) library.get(documentName);

                        if (oldDocument != null) {
                            // replaceChild is not used here, as the combination removeChild/ appendChild is quicker for indexes.
                            library.removeChild(oldDocument);
                            library.appendChild(newDocument);
                        } else {
                            library.appendChild(newDocument);
                        }

                        // Set the new name
                        newDocument.setName(documentName);
                    }
                    out.println("226 transfer complete");
                    dataSocket.close();

                    sessionCommit();

                } catch (Exception e) {
                    logException(e);
                    sessionRollback();

                    result = false;
                    try {
                        String errMessage = "451 Storage failed";
                        if (e.getMessage().equals("May not alter catalog")) {
                            errMessage = "553 " + documentName + ": Permission denied, may not alter catalog.";
                        }
                        out.println(errMessage);

                        if (dataSocket != null) {
                            dataSocket.close();
                        }
                    } catch (Exception ee) {
                        logException(ee);
                    }
                }
            } else {
                result = false;
            }

            return result;
        }

        /**
         * Determine from the filename, whether we are dealing with an XML document here
         */
        private boolean isXMLDocument(String documentName) {
            // look for an extension
            if (documentName.lastIndexOf('.') != -1) {
                String extension = documentName.substring(documentName.lastIndexOf('.') + 1).toLowerCase();
                for (int i = 0; i < XML_EXTENSIONS.length; i++) {
                    if (extension.equals(XML_EXTENSIONS[i])) {
                        // XML extension, so assume XML document
                        return true;
                    }
                }
            }
            // In all other cases, assume it is not an XML document
            return false;
        }

        private boolean handleDelete(XhiveLibraryIf library, String docName) {
            boolean isCatalog = library instanceof XhiveCatalogIf;

            boolean result = true;

            try {

                // remove the document
                Node document;
                if (isCatalog) {
                    document = (Node) catalogGetSchemaDocument(library, docName);
                } else {
                    document = library.get(docName);
                }
                library.removeChild(document);

                sessionCommit();

            } catch (Exception e) {
                logException(e);
                result = false;
                sessionRollback();
            }

            return result;
        }

        private boolean handleMakeLibrary(XhiveLibraryIf library, String libName) {
            boolean result = true;

            if (!libName.equals("")) {
                try {

                    // add the library
                    XhiveLibraryIf newLibrary = library.createLibrary();
                    newLibrary.setName(libName);
                    library.appendChild(newLibrary);

                    sessionCommit();

                } catch (Exception e) {
                    logException(e);
                    result = false;
                    sessionRollback();
                }
            } else {
                result = false;
            }

            return result;
        }

        private boolean handleRemoveLibrary(XhiveLibraryIf library, String libName) {
            if (USE_CATALOGS && libName.equals(CATALOG_DIR_NAME)) {
                // Do not actually remove the catalog, but lie about it
                // for recursive removals
                return true;
            }

            boolean result = true;

            try {

                // remove the library
                XhiveLibraryIf libraryToRemove = getChildLibrary(library, libName);
                library.removeChild(libraryToRemove);

                sessionCommit();

            } catch (Exception e) {
                result = false;

                sessionRollback();
                logException(e);
            }

            return result;
        }

        private boolean handleRename(XhiveLibraryIf renameFromLibrary, Node renameFromNode,
                                     XhiveLibraryIf renameToLibrary, String renameToName) {
            boolean result = true;

            if (!renameToName.equals("")) {
                try {

                    if (renameFromLibrary != renameToLibrary) {
                        //renameToLibrary.appendChild(renameFromLibrary.removeChild(renameFromNode));
                        throw new Exception("Renaming between libraries is not supported");
                    }
                    if (renameFromNode == null) {
                        throw new Exception("Source node could not be found");
                    }

                    if (renameFromNode instanceof XhiveSchemaDocIf) {
                        ((XhiveSchemaDocIf) renameFromNode).setSystemId(renameToName);
                    } else if (renameFromNode.getNodeType() == XhiveNodeIf.LIBRARY_NODE) {
                        ((XhiveLibraryIf) renameFromNode).setName(renameToName);
                    } else if (renameFromNode.getNodeType() == Node.DOCUMENT_NODE) {
                        ((XhiveDocumentIf) renameFromNode).setName(renameToName);
                    } else if (renameFromNode.getNodeType() == XhiveNodeIf.BLOB_NODE) {
                        ((XhiveBlobNodeIf) renameFromNode).setName(renameToName);
                    }

                    sessionCommit();
                } catch (Exception e) {
                    log("Renaming failed because of: " + e.getMessage());
                    logException(e);
                    result = false;
                    sessionRollback();
                }
            } else {
                result = false;
            }

            return result;
        }

        private Vector getListEntries(XhiveLibraryIf library, String fileName) {
            boolean isCatalog = library instanceof XhiveCatalogIf;

            Vector libraryEntries = new Vector();
            Vector documentEntries = new Vector();

            String libraryPrefix;
            String documentPrefix;
            SimpleDateFormat sdf;

            if (DIRECTORY_LISTING_UNIX) {
                libraryPrefix = "drw-r--r--   1 xhive   xhive     1 ";
                documentPrefix = "-rw-r--r--   1 xhive   xhive  1024 ";
                sdf = new SimpleDateFormat("MMM dd HH:mm ");
            } else {
                libraryPrefix = "       <DIR>         ";
                documentPrefix = "                1024 ";
                sdf = new SimpleDateFormat("MM-dd-yy  hh:mma");
            }

            try {
                if (fileName.equals("")) {

                    if (USE_CATALOGS && (!isCatalog)) {
                        // Add an entry for the catalog
                        if (DIRECTORY_LISTING_UNIX) {
                            libraryEntries.addElement(libraryPrefix + "jan 01 00:00 " + CATALOG_DIR_NAME);
                        } else {
                            libraryEntries.addElement("01-01-01  00:00AM" + libraryPrefix + CATALOG_DIR_NAME);
                        }
                    }

                    // Show all libraries in current library
                    Node node = library.getFirstChild();

                    while (node != null) {
                        if (node instanceof XhiveLibraryChildIf) {
                            XhiveLibraryChildIf libraryChild = (XhiveLibraryChildIf) node;

                            String lastModified = sdf.format(libraryChild.getLastModified());
                            boolean isLibrary = node instanceof XhiveLibraryIf;

                            String prefix = isLibrary ? libraryPrefix : documentPrefix;
                            Vector entries = isLibrary ? libraryEntries : documentEntries;
                            String name = isCatalog ? catalogGetSystemIdName((XhiveDocumentIf) node) : libraryChild.getName();
                            String wholePrefix = DIRECTORY_LISTING_UNIX ? prefix + lastModified : lastModified + prefix;

                            if (name == null) {
                                name = libraryChild.getId() + "";
                            }

                            entries.addElement(wholePrefix + name);
                        }

                        node = node.getNextSibling();
                    }
                } else {
                    Node node = library.get(fileName);

                    if (node != null) {
                        XhiveDocumentIf document = (XhiveDocumentIf) node;
                        String lastModified = sdf.format(document.getLastModified());
                        String prefix = DIRECTORY_LISTING_UNIX ? documentPrefix + lastModified : lastModified + documentPrefix;
                        documentEntries.add(prefix + document.getName());
                    }
                }
            } catch (Exception e) {
                logException(e);
            }

            sessionCommit();

            Vector entries = new Vector();
            entries.addAll(libraryEntries);
            entries.addAll(documentEntries);

            return entries;
        }

        // Functions for navigating through the database structure
        // by using directory path names

        private String parentDirectory(String directory) {
            String parent;
            int index = directory.lastIndexOf('/');

            if (index == -1) {
                parent = "";
            } else {
                parent = directory.substring(0, index);
            }

            return parent;
        }

        private String getAbsolutePath(String relativePath) {
            String absolutePath = "";

            // Check for root indicator '/'
            if (relativePath.startsWith("/")) {
                absolutePath = "";
                relativePath = relativePath.substring(1);
            } else {
                absolutePath = currentDirectory;
            }

            // Walk through relative path to change absolute path anologously
            StringTokenizer tokenizer = new StringTokenizer(relativePath, "/");

            while (tokenizer.hasMoreTokens()) {
                String directory = tokenizer.nextToken();

                if (!directory.equals(".")) {
                    if (directory.equals("..")) {
                        absolutePath = parentDirectory(absolutePath);
                    } else {
                        if (absolutePath != "") {
                            absolutePath += '/';
                        }

                        absolutePath += directory;
                    }
                }
            }

            return absolutePath;
        }

        private boolean changeDirectory(String directory) {
            String newDirectory = getAbsolutePath(directory);

            XhiveLibraryIf newLibrary = getLibrary(newDirectory);


            boolean successful = newLibrary != null;

            sessionCommit();

            if (successful) {
                currentDirectory = newDirectory;
            }

            return successful;
        }

        private String extractPathFromFile(String file) {
            int index = file.lastIndexOf("/");
            String path = "";

            if (index != -1) {
                path = file.substring(0, index);
            }

            return path;
        }

        private String extractFileNameFromFile(String file) {
            int index = file.lastIndexOf("/");
            String fileName = file;

            if (index != -1) {
                fileName = file.substring(index + 1);
            }

            return fileName;
        }

        // ****  Functions for retrieving libraries from the database based on their pathnames  ****

        private XhiveLibraryIf getLibrary(String directory) {
            StringTokenizer tokenizer = new StringTokenizer(directory, "/");
            String name = null;

            XhiveLibraryIf library = session.getDatabase().getRoot();

            while (tokenizer.hasMoreTokens() && library != null) {
                name = tokenizer.nextToken();
                library = getChildLibrary(library, name);
            }

            return library;
        }

        private XhiveLibraryIf getChildLibrary(XhiveLibraryIf library, String childName) {
            XhiveLibraryIf childLibrary = null;
            Node node = library.getFirstChild();
            String name;

            while (childLibrary == null && node != null) {
                if (node.getNodeType() == XhiveNodeIf.LIBRARY_NODE &&
                        ((name = ((XhiveLibraryIf) node).getName()) != null) && name.equals(childName)) {
                    childLibrary = (XhiveLibraryIf) node;
                }

                node = node.getNextSibling();
            }

            // Exception for catalogs
            if ((childLibrary == null) && (childName != null)) {
                if (USE_CATALOGS && childName.equals(CATALOG_DIR_NAME)) {
                    childLibrary = (XhiveLibraryIf) library.getCatalog();
                }
            }

            return childLibrary;
        }

        private XhiveLibraryIf getLibraryFromFile(String file) {
            XhiveLibraryIf library = getLibrary(getAbsolutePath(extractPathFromFile(file)));

            return library;
        }

        // ****  Functions to connect to the database  ****

        public void sessionBegin() {
            session.join();
            session.begin();
        }

        private void sessionCommit() {
            session.commit();
            session.leave();
        }

        private void sessionRollback() {
            session.rollback();
            session.leave();
        }

        private boolean connectToDatabase(String user, String password) {
            if (connected) {
                session.join();
                session.disconnect();
                session.leave();
            }

            try {
                session.join();
                session.connect(user, password, DATABASE_NAME);
                connected = true;
                session.begin();
                currentDirectory = "";
                session.commit();
                session.leave();
            } catch (XhiveException e) {
                connected = false;
                log(e.getMessage());
            }

            return connected;
        }

        private void disconnectFromDatabase() {
            if (session != null) {
                if (session.isOpen()) {
                    session.rollback();
                }
                if (session.isConnected()) {
                    session.disconnect();
                }
                if (session.isJoined()) {
                    session.leave();
                }
                synchronized (unusedSessions) {
                    unusedSessions.push(session);
                }
            }
            session = null;
            connected = false;
        }


        // ****  Miscellaneous functions  ****

        public void log(String logText) {
            System.out.println(connectionId + ":  " + logText);
        }

        public void logException(Exception e) {
            System.out.println(connectionId + ":  Exception occurred: " + e.getMessage());
            e.printStackTrace(System.out);
        }

        // ****  Catalog related functions ****

        /**
         * Determine a schema-document to use in the catalog, and return that
         * as a String for output.
         * @throws Exception if the document could not be found.
         * @param schemaName Should be the name of the DTD or schema without any path information
         */
        private String catalogSerializeSchema(XhiveLibraryIf catalog, String schemaName) throws Exception {
            XhiveSchemaDocIf schemaDoc = catalogGetSchemaDocument(catalog, schemaName);
            if (schemaDoc == null) {
                throw new Exception("Schema with name " + schemaName + " could not be found");
            }
            String preAmble = "";
            if (!schemaDoc.representsXMLSchema()) {
                String pubId = schemaDoc.getPublicId();
                if (pubId != null) {
                    preAmble = "<!-- This DTD has public id \"" + pubId + "\" -->\n";
                    //        preAmble += "\n\nTo create a new document in X-Hive/DB based on this dtd";
                    //        preAmble += "\nin the right directory using our FTP-server, use doctype";
                    //        preAmble += "\n   <!DOCTYPE rootElement PUBLIC \"" + pubId + "\"\">\n";
                    //        preAmble += "-->\n\n";
                }

            }
            return preAmble + schemaDoc.serializeToString();
        }

        /**
         * Determine a Schema-document to use in the catalog, and return that document
         * @param schemaName Should be the name of the DTD or schema without any path information
         * @return The DomSchemaDocument
         */
        private XhiveSchemaDocIf catalogGetSchemaDocument(XhiveLibraryIf catalog, String schemaName) {
            Document schemaDoc = (Document) catalog.getFirstChild();

            while (schemaDoc != null) {
                // Determine the filename for matching
                String systemId = ((XhiveSchemaDocIf) schemaDoc).getSystemId();
                if (systemId.endsWith(schemaName)) {
                    return (XhiveSchemaDocIf) schemaDoc;
                }

                // go to the next document
                schemaDoc = (Document) schemaDoc.getNextSibling();
            }
            // Could not be found
            return null;
        }

        /**
         * Returns the last 'part' (filename) of the system id if that can be
         * found, or null otherwise
         */
        private String catalogGetSystemIdName(Document doc) {
            XhiveSchemaDocIf schemaDoc = (XhiveSchemaDocIf) doc;
            if (schemaDoc.representsXMLSchema()) {
                return schemaDoc.getSchemaId();
            } else {
                String systemId = schemaDoc.getSystemId();
                String lastPart = null;
                if (systemId != null) {
                    StringTokenizer systemIdParts = new StringTokenizer(systemId, "/\\");
                    while (systemIdParts.hasMoreTokens()) {
                        lastPart = systemIdParts.nextToken();
                    }
                }
                return lastPart;
            }
        }

    } // end of class FTPClient

    /**
     * This class is a Reader-filter, that changes the document type to match the information
     * in the catalog.
     */
    class DocTypeCorrectingReader extends Reader {
        Reader in = null;
        XhiveCatalogIf catalog;
        FTPClient ftpClient;

        static final int SEARCHING_DOCTYPE = 0;
        static final int SKIPPING_WHITESPACE1 = 1;
        static final int SKIPPING_ROOT_ELEMENT = 2;
        static final int SKIPPING_WHITESPACE2 = 3;
        static final int MATCHING_SYSTEM = 4;
        static final int CORRECTING = 5;
        static final int FORWARDING = 6;

        static final String DOCTYPE = "<!DOCTYPE";
        static final String SYSTEM = "SYSTEM";

        int status = SEARCHING_DOCTYPE;
        int charactersMatched = 0;
        char character;
        boolean endOfStream = false;

        String correctingBuffer = "";

        public DocTypeCorrectingReader(Reader in, XhiveCatalogIf catalog, FTPClient ftpClient) {
            super();

            this.in = in;
            this.catalog = catalog;
            this.ftpClient = ftpClient;
        }

        public int read(char[] cbuf, int off, int len) throws IOException {
            int i = 0;
            char character;

            while (i < len && !endOfStream) {
                character = readCharacter();

                if (!endOfStream) {
                    cbuf[off + i] = character;
                    i++;
                }
            }

            if (endOfStream && i == 0) {
                i = -1;
            }

            return i;
        }

        public char readCharacter() throws IOException {
            char[] characters = new char[1];
            int charactersRead;

            if (status == CORRECTING) {
                if (correctingBuffer.length() != 0) {
                    character = correctingBuffer.charAt(0);
                    correctingBuffer = correctingBuffer.substring(1);
                } else {
                    status = FORWARDING;
                }
            }

            if (status != CORRECTING) {
                charactersRead = in.read(characters, 0, 1);
                character = characters[0];
                endOfStream = charactersRead == -1 || charactersRead == 0;
            }

            if (!endOfStream) {
                switch (status) {
                case SEARCHING_DOCTYPE:
                    matchDocType(character);
                    break;
                case SKIPPING_WHITESPACE1:
                    skip(character, true, SKIPPING_ROOT_ELEMENT);
                    break;
                case SKIPPING_ROOT_ELEMENT:
                    skip(character, false, SKIPPING_WHITESPACE2);
                    break;
                case SKIPPING_WHITESPACE2:
                    if (skip(character, true, CORRECTING)) {
                        correct(character);
                        character = correctingBuffer.charAt(0);
                        correctingBuffer = correctingBuffer.substring(1);
                    }
                    break;
                }
            }

            return character;
        }

        public boolean skip(char character, boolean whitespace, int nextStatus) {
            boolean switched = false;

            if (Character.isWhitespace(character) != whitespace) {
                status = nextStatus;
                switched = true;
            }

            return switched;
        }

        public void close() throws IOException {
            in.close();
        }

        void matchDocType(char character) {
            if (Character.toUpperCase(character) == DOCTYPE.charAt(charactersMatched)) {
                charactersMatched++;

                if (charactersMatched == DOCTYPE.length()) {
                    status = SKIPPING_WHITESPACE1;
                }
            } else {
                charactersMatched = 0;
            }
        }

        void correct(char character) throws IOException {
            char[] characters = new char[5];
            in.read(characters, 0, 5);
            correctingBuffer = character + new String(characters, 0, 5);

            if (correctingBuffer.toUpperCase().equals(SYSTEM)) {
                int charactersRead = in.read(characters, 0, 1);

                while (charactersRead == 1 && Character.isWhitespace(characters[0])) {
                    correctingBuffer += characters[0];
                    charactersRead = in.read(characters, 0, 1);
                }

                correctingBuffer += characters[0];

                if (characters[0] == '"') {
                    charactersRead = in.read(characters, 0, 1);

                    while (charactersRead == 1 && characters[0] != '"') {
                        correctingBuffer += characters[0];
                        charactersRead = in.read(characters, 0, 1);
                    }

                    correctingBuffer += characters[0];
                    correctingBuffer = replaceSystem(correctingBuffer);
                }
            }
        }

        public String replaceSystem(String str) {
            int index = correctingBuffer.indexOf("\"");
            String systemId = correctingBuffer.substring(index + 1, correctingBuffer.length() - 1);

            String lastPart = null;
            StringTokenizer systemIdParts = new StringTokenizer(systemId, "/\\");

            while (systemIdParts.hasMoreTokens()) {
                lastPart = systemIdParts.nextToken();
            }

            XhiveSchemaDocIf dtd = ftpClient.catalogGetSchemaDocument((XhiveLibraryIf) catalog, lastPart);

            if (dtd != null) {
                str = "PUBLIC \"" + dtd.getPublicId() + "\" \"" + dtd.getSystemId() + "\"";
            }

            return str;
        }
    }

} // end of class XhiveFtpServer

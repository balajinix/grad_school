package com.xhive.adminclient.httpd;

import com.xhive.adminclient.dialogs.XhiveMessageDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.AdminMainFrame;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveBlobNodeIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.XhiveDriverFactory;

import org.w3c.dom.Node;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSSerializerFilter;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Adminclient
 *
 * [DESCRIPTION]
 * This is the very simple integrated webserver used in the administrator client
 *
 */
public class AdminHTTPD {

    // http-server variables
    public static final int DEFAULT_HTTP_PORT = 2183;
    public static final int ACCEPT_TIMEOUT = 4000;
    public static final int MAX_CONNECTIONS = 10;
    // This is the default MiMe-type Apache uses as well.
    private static final String DEFAULT_MIMETYPE = "text/plain";
    private boolean connectionRunning = false;
    private ServerSocket listenSocket;
    private Hashtable mimeTypes;
    private String hostName;
    private int port;
    private boolean serverRunning;


    /**
     * Set up a socket listener.
     */
    public AdminHTTPD() {
        port = DEFAULT_HTTP_PORT;
        hostName = "";

        mimeTypes = new Hashtable();
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("doc", "application/msword");
        mimeTypes.put("xls", "application/msexcel");
        mimeTypes.put("ppt", "application/powerpoint");
        mimeTypes.put("mp3", "audio/x-mp3");

        try {
            int connectionAttempts = 0;
            boolean connected = false;
            while (!connected && connectionAttempts < MAX_CONNECTIONS) {
                try {
                    listenSocket = new ServerSocket(port);
                    connected = true;
                } catch (BindException e) {
                    port++;
                    connectionAttempts++;
                }
            }
        } catch (Exception e) {
            XhiveMessageDialog.showException(e);
        }

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            hostName = "Could_not_determine_hostname";
            XhiveMessageDialog.showErrorMessage("Could_not_determine_hostname: " + e);
        }
    }

    /**
     * Start up the webserver
     */
    public void startHTTPServer() {
        //    System.out.println("Server started: " + "http://" + hostName.toLowerCase() + ":" + port + "/");
        serverRunning = true;
        waitForConnections(listenSocket);
    }

    /**
     * Stop the webserver
     */
    public void stopHTTPServer() {
        serverRunning = false;
        // Session is terminated in the listener thread
    }

    /**
     * Get port the server is running on
     */
    public int getPort() {
        return port;
    }

    public boolean isRunning() {
        return serverRunning;
    }

    /**
     * Process incoming HTTP-connections.
     */
    private void waitForConnections(ServerSocket listenSocket) {
        try {
            listenSocket.setSoTimeout(ACCEPT_TIMEOUT);
            while (serverRunning) {
                // handle a connection
                try {
                    Socket clientSocket = listenSocket.accept();
                    if (serverRunning) {
                        HTTPConnection c = new HTTPConnection(clientSocket);
                    } else {
                        clientSocket.close();
                    }
                } catch (InterruptedIOException e) {
                    // We have set a timeout, so that this occurs is okay
                }

            }
            listenSocket.close();
        } catch (IOException e) {
            try {
                XhiveMessageDialog.showErrorMessage("Exception while listening for connections: " + e);
            } catch (Throwable t) {
                // do nothing (in some cases XhiveDialog class could not be
                // found because application is shutting down)
            }

        }
    }

    /**
     * Called by HTTPConnection threads, only one thread at
     * a time may use the transaction.
     */
    protected synchronized void connectionStart() {
        // wait until connection is free
        /*
            // Disabled to allow a proper error about dtd not found to be displayed in browser
            // Dtd get fetched in CONCURRENT sessions
         
            while (connectionRunning) {
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                // never happens
              }
            }
        */
        connectionRunning = true;
    }

    /**
     * @see connectionStart
     */
    protected void connectionStop() {
        connectionRunning = false;
    }

    /**
     * Output a standard HTTP response header to the web-client
     * @param contentType - the HTTP-MIME type of output
     */
    public static void printHTTPHeader(PrintStream httpOut, String contentType, String code) {
        httpOut.println(code);
        httpOut.println("Date: " + (new Date()).toString());
        httpOut.println("Server: X- Hive WebServer/sample");
        httpOut.println("MIME-version: 1.0");
        httpOut.println("Content-type: " + contentType);
        // empty line after header
        httpOut.println("");
    }

    /*****************************************************************/

    /**
     * Inner class that handles the HTTP connections, by responding
     * to certain GET requests
     */
    class HTTPConnection extends Thread {

        private Socket client;
        /** Inputstream which connects to the web-client */
        private BufferedReader httpIn;
        /** Outputstream which connects to the web-client */
        private PrintStream httpOut;

        /**
         * Constructor, try to get input and output-streams for
         * the HTTP-connection, and start the thread if that
         * was succesful.
         */
        public HTTPConnection(Socket client_socket) {

            this.client = client_socket;

            try {
                httpIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                httpOut = new PrintStream(client.getOutputStream());
            } catch (IOException e) {
                XhiveMessageDialog.showErrorMessage("Could not connect socket streams: " + e);
                try {
                    client.close();
                } catch (IOException e2) {
                    // do nothing
                }

                client = null;
            }

            if (client != null)
                this.start();
        }

        /**
         * Thread.run, call handleRequest after reserving the database
         * transaction.
         */
        public void run() {
            int result;
            connectionStart();
            try {
                handleRequest();
                httpOut.close();
            } catch (Exception e) {
                XhiveMessageDialog.showException(e);
            } finally {
                try {
                    client.close();
                } catch (IOException e2) {
                    XhiveMessageDialog.showErrorMessage("Could not close connection: " + e2);
                }
                connectionStop();
            }
        }

        /**
         * Read rest of request-input-buffer, and throw away the
         * contents.
         */
        void emptyInput() throws IOException {
            char in[] = new char[4096];
            int result = httpIn.read(in, 0, 4096);
        }

        private String unescape(String request) {
            int index;
            while ((index = request.indexOf("%20")) != -1) {
                request = request.substring(0, index) + " " + request.substring(index + "%20".length());
            }
            return request;
        }

        /**
         * Determine how to handle the request to the HTTP-server
         * by examining the GET-request.
         */
        private void handleRequest() throws IOException {
            // Determine GET-command
            String request = httpIn.readLine();
            emptyInput();
            request = request.substring("GET ".length() + 1, request.indexOf(" HTTP/"));
            request = unescape(request);

            // The AdminHTTPDhandler processes the actual request
            doGet(httpIn, httpOut, request);
        }

        private void doGet(BufferedReader httpIn, PrintStream httpOut, String request) {
            XhiveSessionIf session = AdminMainFrame.getSession(true);
            session.join();
            session.begin();
            try {
                if (request.equalsIgnoreCase("Document.gif")) {
                    doGetImage(httpOut, "Document.gif");
                } else if (request.equalsIgnoreCase("Folder.gif")) {
                    doGetImage(httpOut, "Folder.gif");
                } else if (request.equalsIgnoreCase("Blob.gif")) {
                    doGetImage(httpOut, "Blob.gif");
                } else {
                    XhiveLibraryChildIf libraryChild = session.getDatabase().getRoot().getByPath(request);
                    if (libraryChild == null) {
                        AdminHTTPD.printHTTPHeader(httpOut, "text/html", "HTTP/1.1 404 Not Found");
                    } else {
                        if (libraryChild instanceof XhiveDocumentIf) {
                            AdminHTTPD.printHTTPHeader(httpOut, "text/xml", "HTTP/1.0 200 OK");
                            LSSerializer writer = ((XhiveLibraryIf) libraryChild.getOwnerLibrary()).createLSSerializer();
                            writer.getDomConfig().setParameter("entities", Boolean.FALSE);
                            LSOutput output = ((XhiveLibraryIf) libraryChild.getOwnerLibrary()).createLSOutput();
                            output.setByteStream(httpOut);
                            // Read-only, so should be okay to change the encoding
                            output.setEncoding("UTF-8");
                            // Filter out doctype
                            writer.setFilter(new LSSerializerFilter() {
                                                 public int getWhatToShow() {
                                                     return LSSerializerFilter.SHOW_ALL;
                                                 }

                                                 public short acceptNode(Node n) {
                                                     if (n.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
                                                         return LSSerializerFilter.FILTER_REJECT;
                                                     } else {
                                                         return LSSerializerFilter.FILTER_ACCEPT;
                                                     }
                                                 }
                                             }
                                            );
                            writer.write(libraryChild, output);
                        } else if (libraryChild instanceof XhiveBlobNodeIf) {
                            String name = libraryChild.getName();
                            String mimeType = null;
                            if ((name != null) && (name.indexOf('.') != -1)) {
                                mimeType = (String) mimeTypes.get(name.substring(name.lastIndexOf('.') + 1));
                            }
                            if (mimeType == null) {
                                mimeType = DEFAULT_MIMETYPE;
                            }
                            AdminHTTPD.printHTTPHeader(httpOut, mimeType, "HTTP/1.0 200 OK");
                            InputStream in = ((XhiveBlobNodeIf) libraryChild).getContents();
                            byte[] buffer = new byte[2048];
                            int length;
                            while ((length = in.read(buffer)) != -1) {
                                httpOut.write(buffer, 0, length);
                            }
                        } else {
                            AdminHTTPD.printHTTPHeader(httpOut, "text/html", "HTTP/1.0 200 OK");
                            httpOut.println("<html>");
                            httpOut.println("  <body>");
                            httpOut.println("    <table>");
                            if (libraryChild.getParentNode() != null) {
                                httpOut.println("      <tr><td>" + getHref((XhiveLibraryChildIf) libraryChild.getParentNode()) + "<img src='/Folder.gif' style='border: none'></a></td><td>" + getHref((XhiveLibraryChildIf) libraryChild.getParentNode()) + " .. </td></tr>");
                            }
                            XhiveLibraryChildIf childIterator = (XhiveLibraryChildIf) libraryChild.getFirstChild();
                            while (childIterator != null) {
                                httpOut.println("<tr><td>");
                                String name = childIterator.getName();
                                if (name == null) {
                                    name = childIterator.getFullPath();
                                }
                                if (childIterator instanceof XhiveDocumentIf) {
                                    httpOut.println(getHref(childIterator) + "<img src='/Document.gif' style='border: none'></a></td><td>" + getHref(childIterator) + name);
                                } else if (childIterator instanceof XhiveBlobNodeIf) {
                                    httpOut.println(getHref(childIterator) + "<img src='/Blob.gif' style='border: none'></a></td><td>" + getHref(childIterator) + name);
                                } else {
                                    httpOut.println(getHref(childIterator) + "<img src='/Folder.gif' style='border: none'></a></td><td>" + getHref(childIterator) + name);
                                }
                                httpOut.println("</td></tr>");
                                childIterator = (XhiveLibraryChildIf) childIterator.getNextSibling();
                            }
                            httpOut.println("    </table>");
                            httpOut.println("  </body>");
                            httpOut.println("</html>");
                        }
                    }
                }
                session.commit();
            } catch (Exception e) {
                XhiveMessageDialog.showException(e);
            } finally {
                if (session.isOpen()) {
                    session.rollback();
                }
                session.leave();
                AdminMainFrame.returnSession(session);
            }
        }
    }

    private String getHref(XhiveLibraryChildIf libraryChild) {
        return "<a href=\"http://" + hostName.toLowerCase() + ":" + port + libraryChild.getFullPath() + "\" style='font-family: Verdana; font-size: 8pt'>";
    }

    private void doGetImage(PrintStream httpOut, String image) throws Exception {
        String mimeType = (String) mimeTypes.get("gif");
        AdminHTTPD.printHTTPHeader(httpOut, mimeType, "HTTP/1.0 200 OK");
        InputStream in = XhiveResourceFactory.getImageInputStream(image);
        byte[] buffer = new byte[2048];
        int length;
        while ((length = in.read(buffer)) != -1) {
            httpOut.write(buffer, 0, length);
        }
    }
}

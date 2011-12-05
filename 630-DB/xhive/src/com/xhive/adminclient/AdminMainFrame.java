package com.xhive.adminclient;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;

import com.xhive.XhiveDriverFactory;
import com.xhive.adminclient.dialogs.FederationSetCreationDialog;
import com.xhive.adminclient.dialogs.XhiveAboutDialog;
import com.xhive.adminclient.dialogs.XhiveBackupDialog;
import com.xhive.adminclient.dialogs.XhiveBootstrapDialog;
import com.xhive.adminclient.dialogs.XhiveCloseConnectionDialog;
import com.xhive.adminclient.dialogs.XhiveConnectionInfo;
import com.xhive.adminclient.dialogs.XhiveDatabaseDialog;
import com.xhive.adminclient.dialogs.XhiveFederationDialog;
import com.xhive.adminclient.dialogs.XhiveLogfileDialog;
import com.xhive.adminclient.dialogs.XhiveMessageDialog;
import com.xhive.adminclient.dialogs.XhivePropertiesDialog;
import com.xhive.adminclient.dialogs.XhiveReplicateFederationDialog;
import com.xhive.adminclient.dialogs.XhiveReplicatorDialog;
import com.xhive.adminclient.dialogs.XhiveRestoreDialog;
import com.xhive.adminclient.dialogs.XhiveServerDialog;
import com.xhive.adminclient.dialogs.XhiveShutdownFederationDialog;
import com.xhive.adminclient.httpd.AdminHTTPD;
import com.xhive.adminclient.panes.XhiveResultPanel;
import com.xhive.adminclient.panes.XhiveXPointerPanel;
import com.xhive.adminclient.panes.XhiveXQueryPanel;
import com.xhive.adminclient.panes.XhiveXUpdatePanel;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.treenodes.XhiveLibraryTreeNode;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhivePageCacheIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.error.XhiveException;
import com.xhive.federationset.interfaces.XhiveFederationSetFactory;

/**
 * (c) 2001-2004 X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Main administration program frame.
 *
 */
public class AdminMainFrame extends JFrame {

    public static boolean DEBUGGING = Boolean.getBoolean("debugging");

    private static AdminMainFrame mainFrame = null;
    private static String currentBootstrapPath = null;
    private static XhiveDriverIf driver = null;
    private static XhivePageCacheIf pageCache =
        XhiveDriverFactory.getFederationFactory().createPageCache(1024);

    // Contains the username, password and database name that where used by the user to connect
    private XhiveConnectionInfo connectionInfo;

    private JToolBar contextToolBar;
    private JPanel explorerPanel;
    private JLabel connectedLabel;
    private static JLabel statusLabel;
    private JProgressBar memoryUsageBar;
    private XhiveMenuItem startServer;
    private XhiveMenuItem stopServer;

    private JTabbedPane resultTabbedPane;
    private JSplitPane splitPane;

    private XhiveDatabaseExplorer databaseExplorer;

    private int oldDividerSize;

    /**
     * Only in use when JAAS is used
     */
    private CachedDialogCallbackHandler callbackHandler = null;

    /**
     * Session pool, plus an indication of what session is currently linked to this action, plus
     * a list for all created sessions to keep track of sessions that may not be returned to the pool.
     */
    private static Stack<XhiveSessionIf> sessionPool = new Stack<XhiveSessionIf>();
    private static ThreadLocal<XhiveSessionIf> sessionForThread = new ThreadLocal<XhiveSessionIf>();
    private static ArrayList<XhiveSessionIf> createdSessions = new ArrayList<XhiveSessionIf>();

    private AdminHTTPD httpServer;

    private final Collection<PerformanceStatisticsFrame> statisticsFrames = new LinkedList<PerformanceStatisticsFrame>();

    // Action objects

    // TODO (ADQ) : These icons are to big (or the other are to small)
    private XhiveAction connectAction = new XhiveAction("Connect",
                                        XhiveResourceFactory.getIconConnect(), "Connect to a database", 'C') {
                                            @Override
                                            protected void xhiveActionPerformed(ActionEvent e) {
                                                XhiveConnectionInfo connectionInfo1 = XhiveDatabaseDialog.showConnectDatabase();
                                                if (connectionInfo1 != null) {
                                                    disconnect();
                                                    connectSession(connectionInfo1);
                                                }
                                            }
                                        };

    private XhiveAction disconnectAction = new XhiveAction("Disconnect",
                                           XhiveResourceFactory.getIconDisconnect(), "Disconnect from database", 'D') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   disconnect();
                                               }
                                           };

    private XhiveAction debugAction = new XhiveAction("Debug", XhiveResourceFactory.getIconConnect(), "Debug", 'u') {
                                          @Override
                                          protected void xhiveActionPerformed(ActionEvent e) {
                                              connectSession(new XhiveConnectionInfo("Administrator", "secret", "MyDatabase", false));
                                          }
                                      };

    private JPanel buildExplorerPanel() {
        explorerPanel = new JPanel(new BorderLayout());
        return explorerPanel;
    }

    private JPanel buildStatusBar() {
        int height = 20;
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray), BorderFactory.createEmptyBorder(0, 5, 0, 5));
        connectedLabel = new JLabel("Not connected");
        connectedLabel.setMaximumSize(new Dimension(200, height));
        connectedLabel.setBorder(border);
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        memoryUsageBar = new JProgressBar(0, 100);
        Font font = memoryUsageBar.getFont();
        memoryUsageBar.setFont(new Font(font.getFamily(), font.getStyle(), font.getSize() - 2));
        memoryUsageBar.setBorderPainted(false);
        memoryUsageBar.setStringPainted(true);
        JPanel memoryUsagePanel = new JPanel(new GridLayout());
        memoryUsagePanel.setBorder(BorderFactory.createLineBorder(Color.gray));

        memoryUsagePanel.setMaximumSize(new Dimension(100, height));
        memoryUsagePanel.add(memoryUsageBar);
        JButton gcButton = new JButton(XhiveResourceFactory.getIconGarbageCollect());
        gcButton.addMouseListener(
            new XhiveMouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    System.gc();
                }
            }
        );
        gcButton.setBorder(BorderFactory.createLineBorder(Color.gray));
        gcButton.setFocusPainted(false);
        gcButton.setMaximumSize(new Dimension(height, height));

        statusLabel = new JLabel();
        statusLabel.setBorder(border);
        statusLabel.setMaximumSize(new Dimension(2000, height));

        statusBar.add(connectedLabel);
        statusBar.add(Box.createHorizontalStrut(2));
        statusBar.add(statusLabel);
        statusBar.add(Box.createHorizontalStrut(2));
        statusBar.add(memoryUsagePanel);
        statusBar.add(Box.createHorizontalStrut(2));
        statusBar.add(gcButton);
        return statusBar;
    }

    private void enableMemoryStatusUpdate() {
        Action setMemoryUsageBar = new XhiveAction() {
                                       @Override
                                       public void xhiveActionPerformed(ActionEvent e) {
                                           if (memoryUsageBar != null) {
                                               Runtime runtime = Runtime.getRuntime();
                                               long totalMemory = runtime.totalMemory();
                                               long totalMemoryUsed = runtime.totalMemory() - runtime.freeMemory();
                                               memoryUsageBar.setValue((int) ((totalMemoryUsed * 100) / totalMemory));
                                               memoryUsageBar.setString((totalMemoryUsed / (1024 * 1024)) + "M of " + (totalMemory / (1024 * 1024)) + "M");
                                           }
                                       }
                                   };
        new Timer(1000, setMemoryUsageBar).start();
    }

    public AdminMainFrame() {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);

        // JDK1.4's default color for labels has changed to black
        Color color = new Color(102, 102, 153);
        UIManager.put("Label.foreground", new ColorUIResource(color));

        int frameTop = AdminProperties.getInt("com.xhive.adminclient.top");
        int frameLeft = AdminProperties.getInt("com.xhive.adminclient.left");
        int frameWidth = AdminProperties.getInt("com.xhive.adminclient.width");
        int frameHeight = AdminProperties.getInt("com.xhive.adminclient.height");

        setLocation(frameLeft, frameTop);
        setSize(frameWidth, frameHeight);

        updateTitle(XhiveDriverFactory.getBootstrapFileName());
        setIconImage(XhiveResourceFactory.getImageXSmall());

        setJMenuBar(buildMainMenu());

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(buildExplorerPanel());
        oldDividerSize = splitPane.getDividerSize();
        splitPane.setDividerSize(0);

        contentPane.add(buildToolBar(), BorderLayout.NORTH);
        contentPane.add(splitPane, BorderLayout.CENTER);
        contentPane.add(buildStatusBar(), BorderLayout.SOUTH);

        enableMemoryStatusUpdate();
        // while not connected:
        setButtonsEnabled(false);

        UIManager.put("FileView.directoryIcon", XhiveResourceFactory.getIconFolder());
        UIManager.put("FileView.fileIcon", XhiveResourceFactory.getIconDocument());
        UIManager.put("FileChooser.upFolderIcon", XhiveResourceFactory.getIconFolder1Up());
    }

    private void updateTitle(String bootStrapPath) {
        setTitle("X-Hive/DB Administrator - " + bootStrapPath);
    }

    private JMenuBar buildMainMenu() {
        JMenuBar menuBar = new JMenuBar();
        // Database menu
        JMenu menuDatabase = menuBar.add(new XhiveMenu("Database", 'a'));
        menuDatabase.add(new XhiveMenuItem(connectAction));
        menuDatabase.add(new XhiveMenuItem(disconnectAction));
        menuDatabase.addSeparator();
        menuDatabase.add(new XhiveMenuItem(new XhiveAction("Create database", 'r') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   // TODO (ADQ) : If exception(ERROR) occurs, this is only shown on command line...After that application hangs
                                                   XhiveDatabaseDialog.showCreateDatabase();
                                               }
                                           }
                                          ));
        menuDatabase.add(new XhiveMenuItem(new XhiveAction("Delete database", 'D') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   String currentDatabaseName = null;
                                                   if (connectionInfo != null) {
                                                       currentDatabaseName = connectionInfo.getDatabaseName();
                                                   }
                                                   XhiveDatabaseDialog.showDeleteDatabase(currentDatabaseName);
                                               }
                                           }
                                          ));
        menuDatabase.addSeparator();
        menuDatabase.add(new XhiveMenuItem(new XhiveAction("Exit", 'X') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   exit();
                                               }
                                           }
                                          ));
        // Settings &amp; federation menu
        JMenu menuSettings = menuBar.add(new XhiveMenu("Settings/ Federation", 'S'));
        menuSettings.add(new XhiveMenuItem(new XhiveAction("Select active federation", 'a') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhiveBootstrapDialog.showSelectFederation();
                                               }
                                           }
                                          ));
        menuSettings.addSeparator();
        menuSettings.add(new XhiveMenuItem(new XhiveAction("Change superuser password", 'p') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhivePropertiesDialog.showChangeSuperuserPassword();
                                               }
                                           }
                                          ));
        menuSettings.add(new XhiveMenuItem(new XhiveAction("Change license key", 'l') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhivePropertiesDialog.showChangeLicenseKey();
                                               }
                                           }
                                          ));
        menuSettings.addSeparator();
        menuSettings.add(new XhiveMenuItem(new XhiveAction("Change keep-log-file option", 'k') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhiveLogfileDialog.showKeepLogFileOption();
                                               }
                                           }
                                          ));
        menuSettings.add(new XhiveMenuItem(new XhiveAction("Backup", 'B') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhiveBackupDialog.showBackup();
                                               }
                                           }
                                          ));
        menuSettings.add(new XhiveMenuItem(new XhiveAction("Restore", 'R') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhiveRestoreDialog.showRestore();
                                               }
                                           }
                                          ));
        menuSettings.addSeparator();
        menuSettings.add(new XhiveMenuItem(new XhiveAction("Create federation", 'f') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhiveFederationDialog.showCreateFederation();
                                               }
                                           }
                                          ));
        menuSettings.addSeparator();
        menuSettings.add(new XhiveMenuItem(new XhiveAction("Close client connection", 'C') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   XhiveCloseConnectionDialog.showCloseConnection();
                                               }
                                           }
                                          ));
        startServer = new XhiveMenuItem(new XhiveAction("Start server in adminclient", 'S') {
                                            @Override
                                            protected void xhiveActionPerformed(ActionEvent e) {
                                                boolean serverRunning = XhiveServerDialog.showStopStartServer(getCurrentBootstrapPath());
                                                updateMenuServerRunning(serverRunning);
                                            }
                                        }
                                       );
        menuSettings.add(startServer);
        stopServer = new XhiveMenuItem(new XhiveAction("Stop adminclient server", 'S') {
                                           @Override
                                           protected void xhiveActionPerformed(ActionEvent e) {
                                               boolean serverRunning = XhiveServerDialog.showStopStartServer(null);
                                               updateMenuServerRunning(serverRunning);
                                           }
                                       }
                                      );
        menuSettings.add(stopServer);
        stopServer.setVisible(false);
        menuSettings.add(new XhiveMenuItem(new XhiveAction("Performance statistics", 'P') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   final PerformanceStatisticsFrame frame = new PerformanceStatisticsFrame(getDriver());
                                                   statisticsFrames.add(frame);
                                                   frame.addWindowListener(new WindowAdapter() {
                                                                               @Override
                                                                               public void windowClosed(WindowEvent event) {
                                                                                   statisticsFrames.remove(event.getWindow());
                                                                               }
                                                                           }
                                                                          );
                                                   frame.showPerformanceStatistics(AdminMainFrame.this);
                                               }
                                           }
                                          ));
        // Replication menu
        JMenu menuReplication = menuBar.add(new XhiveMenu("Replication", 'R'));
        menuReplication.add(new XhiveMenuItem(new XhiveAction("Register replicator", 'R') {
                                                  @Override
                                                  protected void xhiveActionPerformed(ActionEvent e) {
                                                      XhiveReplicatorDialog.showRegister();
                                                  }
                                              }
                                             ));
        menuReplication.add(new XhiveMenuItem(new XhiveAction("Unregister replicator", 'U') {
                                                  @Override
                                                  protected void xhiveActionPerformed(ActionEvent e) {
                                                      XhiveReplicatorDialog.showUnregister();
                                                  }
                                              }
                                             ));
        menuReplication.add(new XhiveMenuItem(new XhiveAction("Replicate federation", 'f') {
                                                  @Override
                                                  protected void xhiveActionPerformed(ActionEvent e) {
                                                      XhiveReplicateFederationDialog.showReplicateFederation();
                                                  }
                                              }
                                             ));
        menuReplication.add(new XhiveMenuItem(new XhiveAction("Shutdown federation", 'S') {
                                                  @Override
                                                  protected void xhiveActionPerformed(ActionEvent e) {
                                                      XhiveShutdownFederationDialog.showShutdown();
                                                  }
                                              }
                                             ));
        // Federation sets menu
        JMenu menuFS = menuBar.add(new XhiveMenu("Federation sets", 'F'));
        menuFS.add(new XhiveMenuItem(new XhiveAction("Create federation set", 'C') {
                                         @Override
                                         protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                             FederationSetCreationDialog.showDialog(true);
                                         }
                                     }
                                    ));
        menuFS.add(new XhiveMenuItem(new XhiveAction("Delete federation set", 'D') {
                                         @Override
                                         protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                             FederationSetCreationDialog.showDialog(false);
                                         }
                                     }
                                    ));
        menuFS.addSeparator();
        menuFS.add(new XhiveMenuItem(new XhiveAction("Modify federation set", 'M') {
                                         @Override
                                         protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                             new FederationSetFrame(AdminMainFrame.this);
                                         }
                                     }
                                    ));
        // Help menu
        JMenu menuHelp = menuBar.add(new XhiveMenu("Help", 'H'));
        menuHelp.add(new XhiveMenuItem(new XhiveAction("About...", 'A') {
                                           @Override
                                           protected void xhiveActionPerformed(ActionEvent e) {
                                               XhiveAboutDialog.showAbout();
                                           }
                                       }
                                      ));
        return menuBar;
    }

    private void updateMenuServerRunning(boolean serverRunning) {
        if (serverRunning) {
            stopServer.setText("Stop adminclient server for " + getCurrentBootstrapPath());
        }
        startServer.setVisible(!serverRunning);
        stopServer.setVisible(serverRunning);
    }

    private JComponent buildToolBar() {
        JPanel toolBarPanel = new JPanel(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        toolBarPanel.add(toolBar, BorderLayout.WEST);

        toolBar.setFloatable(false);
        // TODO (ADQ) : This is only available in JDK1.4
        //toolBar.setRollover(true);
        if (DEBUGGING) {
            toolBar.add(debugAction);
        }
        toolBar.add(connectAction);
        toolBar.add(disconnectAction);
        contextToolBar = new JToolBar();
        contextToolBar.setFloatable(false);
        //contextToolBar.setRollover(true);
        toolBarPanel.add(contextToolBar, BorderLayout.CENTER);
        return toolBarPanel;
    }

    public static void setStatus(String status) {
        statusLabel.setText(status);
    }

    public static void clearStatus() {
        statusLabel.setText("");
    }

    public boolean exit() {
        for (Iterator<PerformanceStatisticsFrame> itr = new LinkedList<PerformanceStatisticsFrame>(statisticsFrames).iterator(); itr.hasNext();) {
            PerformanceStatisticsFrame frame = itr.next();
            frame.disposeFrameOnExit();
        }
        if (disconnect()) {
            if (driver != null) {
                if (driver.isInitialized()) {
                    // Somehow the disconnect did not close the driver
                    System.out.println("Unclean close");
                    driver.close();
                    driver = null;
                }
            }
            if (! maximized()) {
                AdminProperties.setProperty("com.xhive.adminclient.top", getY());
                AdminProperties.setProperty("com.xhive.adminclient.left", getX());
                AdminProperties.setProperty("com.xhive.adminclient.height", getHeight());
                AdminProperties.setProperty("com.xhive.adminclient.width", getWidth());
            }
            AdminProperties.store();
            System.out.println("Thank you for using X-Hive/DB");
            System.exit(0);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tries to determine whether the window is currently maximize.
     * In JDK 1.4, one may be able to do this with getState() == JFrame.MAXIMIZED_BOTH
     */
    private boolean maximized() {
        // Apparently, there is no way to do this in JDK 1.3, so we fake it
        int realWidth = getWidth();
        int screenWidth = getToolkit().getScreenSize().width;
        return (Math.abs(realWidth - screenWidth) < 10) &&
               (getX() <= 0) && (getY() <= 0);
    }

    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent we) {
        if (we.getID() == WindowEvent.WINDOW_CLOSING) {
            try {
                if (!exit()) {
                    return;
                }
            } catch (Exception e) {
                XhiveMessageDialog.showException(e);
            }
        }
        super.processWindowEvent(we);
    }

    private void setButtonsEnabled(boolean enabled) {
        disconnectAction.setEnabled(enabled);
    }

    private void closeResultWindows() {
        if (resultTabbedPane != null) {
            while (resultTabbedPane != null && resultTabbedPane.getComponentCount() > 0) {
                XhiveResultPanel resultPanel = (XhiveResultPanel) resultTabbedPane.getComponentAt(0);
                // Will remove it from the tabbed pane, and will remove tabbedpane from screen and set it to null if
                // it was the last panel.
                resultPanel.close();
            }
        }
    }

    private void checkRunningThreads() {
        Thread[] threads = new Thread[Thread.activeCount()];
        int threadCount = Thread.enumerate(threads);
        for (int i = 0; i < threadCount; i++) {
            if (threads[i].getName().equalsIgnoreCase("xhiveThread")) {
                if (XhiveMessageDialog.showConfirmation("A Thread is still active, are you sure you want to disconnect?")) {
                    break;
                }
            }
        }
    }

    public boolean isConnected() {
        String currentDatabaseName = null;
        if (connectionInfo != null) {
            currentDatabaseName = connectionInfo.getDatabaseName();
        }
        return (currentDatabaseName != null);
    }

    private boolean disconnect() {
        if (isConnected()) {
            // Close all result windows
            closeResultWindows();
            // Check if there are still threads running
            checkRunningThreads();
            if (databaseExplorer != null) {
                explorerPanel.removeAll();
                explorerPanel.repaint();
                databaseExplorer = null;
                // Remove all buttons from the context tool bar
                contextToolBar.removeAll();
                contextToolBar.repaint();
            }
            setButtonsEnabled(false);
            connectedLabel.setText("Not connected");

            if (httpServer != null) {
                httpServer.stopHTTPServer();
                httpServer = null;
            }

            // Terminate and remove all sessions from the pool
            for (Iterator<XhiveSessionIf> i = sessionPool.iterator(); i.hasNext();) {
                XhiveSessionIf session = i.next();
                if (session.isOpen()) {
                    session.join();
                    session.rollback();
                    session.leave();
                }
                createdSessions.remove(session);
                i.remove();
            }

            try {
                if (createdSessions.size() > 0) {
                    XhiveMessageDialog.showErrorMessage("Unfinished sessions remain, attempting clean-up");
                    // Terminate and remove all sessions created that are not in the pool
                    for (Iterator<XhiveSessionIf> i = createdSessions.iterator(); i.hasNext();) {
                        XhiveSessionIf session = i.next();
                        if (session.isOpen()) {
                            session.join();
                            session.rollback();
                            session.leave();
                        }
                        i.remove();
                    }
                }
                if (driver != null) {
                    DriverRegistry.unregisterDriverUser(driver, this);
                    driver = null;
                }
            }
            finally {
                createdSessions.clear();
            }

            if (callbackHandler != null) {
                callbackHandler.clearLoginCache();
            }

            this.connectionInfo = null;

        }
        return true;
    }

    private void connectSession(final XhiveConnectionInfo connectionInfo1) {
        try {
            this.connectionInfo = connectionInfo1;
            // Extra item, we start with a new stack of sessions now
            // Should not be necessary, but sometimes not all sessions are properly deleted above.
            sessionPool.clear();
            createdSessions.clear();
            // Created a new session
            XhiveTransactionWrapper wrapper = new XhiveTransactionWrapper(null, true) {
                                                  @Override
                                                  protected Object transactedAction() throws Exception {
                                                      XhiveSessionIf session = getSession();
                                                      DriverRegistry.registerDriverUser(session.getDriver(), AdminMainFrame.this);
                                                      databaseExplorer = new XhiveDatabaseExplorer(session, contextToolBar);
                                                      explorerPanel.add(databaseExplorer, BorderLayout.CENTER);
                                                      connectedLabel.setText("Connected as " + session.getUser().getName());
                                                      setButtonsEnabled(true);
                                                      startHTTPD();
                                                      XhiveLibraryTreeNode.setSerializeCopyPath(null);
                                                      return null;
                                                  }
                                              };
            wrapper.start();
        } catch (Throwable t) {
            // Admin tool must not think it is connected after connect fails
            this.connectionInfo = null;
            XhiveMessageDialog.showThrowable(t);
        }
    }

    private void startHTTPD() {
        Thread httpd = new Thread() {
                           @Override
                           public void run() {
                               httpServer = new AdminHTTPD();
                               httpServer.startHTTPServer();
                           }
                       };
        httpd.start();
    }

    private void initResultTabbedPane() {
        if (resultTabbedPane == null) {
            resultTabbedPane = new JTabbedPane();
            splitPane.setBottomComponent(resultTabbedPane);
            // TODO (ADQ) : Remember last position
            splitPane.setDividerLocation(0.60);
            splitPane.setDividerSize(oldDividerSize);
        }
    }

    public void addResultTab(XhiveResultPanel resultPanel) {
        initResultTabbedPane();
        resultTabbedPane.addTab(resultPanel.getName(), resultPanel.getIcon(), resultPanel);
        resultTabbedPane.setSelectedComponent(resultPanel);
    }

    public void addXUpdateTab(String documentPath) {
        initResultTabbedPane();
        addResultTab(new XhiveXUpdatePanel(resultTabbedPane, documentPath));
    }

    public void addXQueryTab(String libraryChildPath, boolean allowUpdates) {
        initResultTabbedPane();
        addResultTab(new XhiveXQueryPanel(resultTabbedPane, libraryChildPath, allowUpdates));
    }

    public void addXPointerTab(String libraryChildPath) {
        initResultTabbedPane();
        addResultTab(new XhiveXPointerPanel(resultTabbedPane, libraryChildPath));
    }

    public void removeResultTab(JPanel panel) {
        resultTabbedPane.remove(panel);
        if (resultTabbedPane.getComponentCount() == 0) {
            // Last one turn out the light
            splitPane.setBottomComponent(null);
            splitPane.setDividerSize(0);
            resultTabbedPane = null;
        }
    }

    public XhiveDatabaseExplorer getDatabaseExplorer() {
        return databaseExplorer;
    }

    public static void setCurrentBootstrapPath(String path) {
        if (getInstance().isConnected()) {
            XhiveMessageDialog.showException(new Exception("Should not be able to set bootstrap path when connected"));
        }
        currentBootstrapPath = path;
        driver = null;
        if (AdminMainFrame.getInstance() != null) {
            AdminMainFrame.getInstance().updateTitle(currentBootstrapPath);
        }
    }

    public static String getCurrentBootstrapPath() {
        if (currentBootstrapPath == null) {
            currentBootstrapPath = XhiveDriverFactory.getBootstrapFileName();
        }
        return currentBootstrapPath;
    }

    public static XhiveDriverIf getDriver() {
        if (driver == null || !driver.isInitialized()) {
            try {
                driver = XhiveFederationSetFactory.getFederation(getCurrentBootstrapPath(), pageCache);
            } catch (IOException ioe) {
                throw new XhiveException(XhiveException.IO_ERROR, ioe);
            }
        }
        return driver;
    }

    public static AdminMainFrame build() {
        mainFrame = new AdminMainFrame();
        return mainFrame;
    }

    public static void setCacheSize(int size) {
        if (size != pageCache.getCachePages()) {
            pageCache = XhiveDriverFactory.getFederationFactory().createPageCache(size);
        }
    }

    public static XhiveSessionIf connectAsSuperuser(String password) {
        XhiveDriverIf driver1 = getDriver();
        XhiveSessionIf session = driver1.createSession();
        String superuser = driver1.getStringProperty("com.xhive.core.superusername");
        session.connect(superuser, password, null);
        DriverRegistry.registerDriverUser(driver1, getInstance());
        return session;
    }

    public static void disconnectFromSuperUser(XhiveSessionIf session) {
        if (session.isOpen()) {
            session.rollback();
        }
        if (session.isConnected()) {
            session.disconnect();
        }
        if (!session.isTerminated()) {
            session.terminate();
        }
        if (sessionPool.isEmpty()) {
            // Was only session, close driver
            DriverRegistry.unregisterDriverUser(driver, getInstance());
            driver = null;
        }
    }

    public static XhiveSessionIf getSession(boolean readOnly) {
        synchronized (AdminMainFrame.getInstance()) {
            return AdminMainFrame.getInstance().createConnectedSession(readOnly, true);
        }
    }

    /**
     * Get a session for use in a thread not shared by others.
     */
    public static XhiveSessionIf getSeperateSessionForThread(boolean readOnly) {
        synchronized (AdminMainFrame.getInstance()) {
            return AdminMainFrame.getInstance().createConnectedSession(readOnly, false);
        }
    }

    public static AdminHTTPD getHTTPD() {
        return mainFrame.httpServer;
    }

    /**
     * If you do not register the session for the thread (which is the default),
     * the returned session will not be found for future requests from the same thread.
     * When set to false, it will also not use the session currently bound to the thread
     */
    private XhiveSessionIf createConnectedSession(boolean readOnly, boolean registerForThread) {
        XhiveSessionIf session = sessionForThread.get();
        if ((session != null) && registerForThread) {
            return session;
        }
        boolean justCreated = false;
        if (sessionPool.size() > 0) {
            session = sessionPool.pop();
        } else {
            XhiveDriverIf driver1 = getDriver();
            session = driver1.createSession("adminclient");
            session.setWaitOption(connectionInfo.getWait());
            justCreated = true;
        }
        XhiveConnectionInfo connectionInfo1 = mainFrame.connectionInfo;
        if (! session.isConnected()) {
            session.join();
            connect(session, connectionInfo1);
            session.leave();
        }
        if (registerForThread) {
            sessionForThread.set(session);
        }
        // Wait with adding to the created sessions only after a succesful connect
        if (justCreated) {
            createdSessions.add(session);
        }
        // default is not readOnly
        if (readOnly) {
            setReadOnlyMode(session, true);
            //      System.out.println("readonly");
        }
        else {
            //      System.out.println("readwrite");
        }

        return session;
    }

    private void connect(XhiveSessionIf session, XhiveConnectionInfo connectionInfo1) {
        if (!connectionInfo1.usesJAAS()) {
            // default case
            session.connect(connectionInfo1.getUserName(), connectionInfo1.getPassword(), connectionInfo1.getDatabaseName());
        } else {
            // Use JAAS, with Swing component
            if (callbackHandler == null) {
                callbackHandler = new CachedDialogCallbackHandler(this);
            }
            session.connect(connectionInfo1.getDatabaseName(), callbackHandler);
        }
    }

    public static void returnSession(XhiveSessionIf session) {
                                 synchronized (AdminMainFrame.getInstance()) {
                                     Stack<XhiveSessionIf> sessionPool1 = getInstance().getSessionPool();
                                     sessionForThread.set(null);
                                     if (! sessionPool1.contains(session)) {
                                         setReadOnlyMode(session, false);
                                         sessionPool1.push(session);
                                     } else {
                                         //        System.out.println("Error in returning session");
                                         //        Thread.dumpStack();
                                     }

                                 }
                             }

                             private static void setReadOnlyMode(XhiveSessionIf session, boolean readOnly) {
                                 session.join();
                                 session.setReadOnlyMode(readOnly);
                                 session.leave();
                             }

                             private Stack<XhiveSessionIf> getSessionPool() {
                                 return sessionPool;
                             }

                             public static AdminMainFrame getInstance() {
                                 return mainFrame;
                             }

                         }

                         class XhiveMenu extends JMenu {

                             public XhiveMenu(String text, char mnemonic) {
                                 super();
                                 setText(text);
                                 setFont(new java.awt.Font("Dialog", 0, 11));
                                 setMnemonic(mnemonic);
                             }
                         }

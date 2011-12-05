package com.xhive.adminclient.dialogs;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.DriverRegistry;
import com.xhive.core.interfaces.XhiveDriverIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for starting a dedicated server within the Adminclient.
 */
public class XhiveServerDialog extends XhiveDialog {

    private static ServerSocket serverSocket;
    private static XhiveDriverIf serverDriver;

    private final String bootstrapPath;
    private JTextField portField;
    private JTextField cacheSizeField;
    private JTextField addressField;
    private JCheckBox addressCheckBox;

    public static boolean showStopStartServer(String bootstrapPath) {
        boolean serverRunning;
        if (serverSocket != null) {
            stopServer();
            serverRunning = false;
        } else {
            XhiveServerDialog dialog = new XhiveServerDialog(bootstrapPath);
            dialog.execute();
            serverRunning = dialog.serverSuccesfullyStarted();
        }
        return serverRunning;
    }

    private boolean serverSuccesfullyStarted() {
        return serverSocket != null;
    }

    protected XhiveServerDialog(String bootstrapPath) {
        super("Start server");
        this.bootstrapPath = bootstrapPath;
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        setGridPanel(fieldsPanel);

        JLabel bootstrapPathField = new JLabel(bootstrapPath);
        bootstrapPathField.setForeground(Color.black);
        portField = new JTextField();
        cacheSizeField = new JTextField();
        addressField = new JTextField();
        addressCheckBox = new JCheckBox("only from this host");
        addressCheckBox.addActionListener(new ActionListener() {
                                              public void actionPerformed(ActionEvent e) {
                                                  if (addressCheckBox.isSelected()) {
                                                      addressField.setText("localhost");
                                                  } else {
                                                      addressField.setText("");
                                                  }
                                              }
                                          }
                                         );

        setPreferredWidthOf(portField, 100);
        setPreferredWidthOf(cacheSizeField, 100);
        setPreferredWidthOf(addressField, 100);

        addToGrid(true, new JLabel("Bootstrap path:"));
        addToGrid(false, bootstrapPathField);
        addToGrid(true, new JLabel("Port:"));
        addToGrid(false, portField);
        addToGrid(true, new JLabel("Cache size:"));
        addToGrid(false, cacheSizeField);
        addToGrid(true, new JLabel("Server address (optional):"));
        JPanel addressFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        addressFieldPanel.add(addressField);
        addressFieldPanel.add(addressCheckBox);
        addToGrid(false, addressFieldPanel);

        return fieldsPanel;
    }

    @Override
    protected void setFields() {
        super.setFields();
        portField.setText("1221");
        cacheSizeField.setText(AdminProperties.getProperty("com.xhive.adminclient.maxcachepages"));
    }

    @Override
    protected boolean fieldsAreValid() {
        return checkFieldIsInteger(portField, "Port must be a number")
               && (!cacheSizeField.isEnabled() || checkFieldIsInteger(cacheSizeField, "Cache size must be a number"));
    }

    @Override
    protected boolean performAction() throws Exception {
        String address = addressField.getText().trim();
        if (address.equals("")) {
            address = null;
        }
        int port = Integer.valueOf(portField.getText()).intValue();
        int cacheSize = Integer.valueOf(cacheSizeField.getText()).intValue();
        startServer(address, port, cacheSize);
        return serverSuccesfullyStarted();
    }

    private static void startServer(String address, int port, int cacheSize) throws Exception {
        if ("*".equals(address)) {
            address = null;
        }
        final ServerSocket socket;
        if (address == null) {
            socket = new ServerSocket(port);
        } else {
            InetAddress inetAddress = InetAddress.getByName(address);
            socket = new ServerSocket(port, 0, inetAddress);
        }
        if (cacheSize != -1) {
            AdminMainFrame.setCacheSize(cacheSize);
        }
        XhiveDriverIf driver = AdminMainFrame.getDriver();
        driver.startListenerThread(socket);
        DriverRegistry.registerDriverUser(driver, socket);
        serverSocket = socket;
        serverDriver = driver;
    }

    private static void stopServer() {
        ServerSocket s = serverSocket;
        if (s != null) {
            DriverRegistry.unregisterDriverUser(serverDriver, s);
            serverSocket = null;
            serverDriver = null;
            try {
                s.close();
            } catch (IOException ioe) {
                // Ignore
            }

        }
    }
}

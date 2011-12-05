package com.xhive.adminclient.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.ListSelectionModel;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.DriverRegistry;
import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhiveFederationIf;
import com.xhive.core.interfaces.XhiveSessionIf;

public class XhiveCloseConnectionDialog extends XhiveDialog {

    private JPasswordField superuserPasswordField;
    private XhiveDriverIf driver;
    /* If ok, the first dialog will leave a superuser connection session here. */
    private XhiveSessionIf session;

    public static void showCloseConnection() {
        XhiveCloseConnectionDialog dialog1 = new XhiveCloseConnectionDialog();
        try {
            if (dialog1.execute() == RESULT_OK) {
                XhiveCloseConnectionDialog2 dialog2 = new XhiveCloseConnectionDialog2(dialog1.session);
                dialog2.execute();
            }
        }
        finally {
            XhiveSessionIf session = dialog1.session;
            if (session != null) {
                session.join();
                session.disconnect();
                session.terminate();
                DriverRegistry.unregisterDriverUser(dialog1.driver, session);
            }
        }
    }

    private XhiveCloseConnectionDialog() {
        super("Close remote connection");
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new FormLayout());
        superuserPasswordField = new JPasswordField();
        setPreferredWidthOf(superuserPasswordField, 200);
        fieldsPanel.add(new JLabel("Superuser password:"));
        fieldsPanel.add(superuserPasswordField);
        return fieldsPanel;
    }

    @Override
    protected final boolean performAction() {
        driver = AdminMainFrame.getDriver();
        session = driver.createSession();
        DriverRegistry.registerDriverUser(driver, session);
        String superUserPassword = new String(superuserPasswordField.getPassword());
        session.connect("superuser", superUserPassword, null);
        session.leave();
        return true;
    }

    @Override
    protected boolean fieldsAreValid() {
        return checkFieldLength(superuserPasswordField, 3, 8, "Expected a valid superuser password");
    }
}

class XhiveCloseConnectionDialog2 extends XhiveDialog {

    private final XhiveSessionIf session;
    private List<SocketAddress> connections;
    private JList connectionsListBox;

    XhiveCloseConnectionDialog2(XhiveSessionIf ses) {
        super("Close remote connection");
        session = ses;
    }

    @Override
    protected JPanel buildFieldsPanel() {
        AdminMainFrame frame = AdminMainFrame.getInstance();
        try {
            /* For some reason, even resolving 127.0.0.1 to localhost takes ridiculously long on this
             * HP-UX machine (like a few seconds per address). */
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            session.join();
            connections = session.getFederation().getRemoteConnections();
            session.leave();
            int size = connections.size();
            String[] connectionStrings = new String[size];
            for (int i = 0; i < size; ++i) {
                SocketAddress address = connections.get(i);
                if (address instanceof InetSocketAddress) {
                    /* Resolve address to host name.  This will make it appear in the toString result of the
                     * socket address. */
                    ((InetSocketAddress)address).getHostName();
                }
                connectionStrings[i] = address.toString();
            }
            connectionsListBox = new JList(connectionStrings);
            connectionsListBox.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Remote connections"), BorderLayout.NORTH);
            panel.add(connectionsListBox, BorderLayout.CENTER);
            return panel;
        }
        finally {
            frame.setCursor(null);
        }
    }

    @Override
    protected final boolean performAction() {
        session.join();
        XhiveFederationIf federation = session.getFederation();
        int[] select = connectionsListBox.getSelectedIndices();
        for (int i = 0; i < select.length; ++i) {
            SocketAddress address = connections.get(select[i]);
            federation.closeRemoteConnection(address);
        }
        session.leave();
        return true;
    }
}

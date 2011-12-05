package com.xhive.adminclient;

import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhiveFederationIf;
import com.xhive.core.interfaces.XhiveSessionIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Wrapper for action on a federation
 */
public abstract class XhiveFederationAction {

    private String superUserPassword;
    private XhiveSessionIf session;

    public XhiveFederationAction(String superUserPassword) throws Exception {
        this.superUserPassword = superUserPassword;
        run();
    }

    private void run() throws Exception {
        XhiveDriverIf driver = AdminMainFrame.getDriver();
        session = driver.createSession();
        DriverRegistry.registerDriverUser(driver, this);
        try {
            session.connect("superuser", superUserPassword, null);
            session.begin();
            XhiveFederationIf federation = session.getFederation();
            performAction(federation);
            if (session.isOpen()) {
                session.commit();
            }
        }
        finally {
            if (session.isOpen()) {
                session.rollback();
            }
            if (session.isConnected()) {
                session.disconnect();
            }
            if (!session.isTerminated()) {
                session.terminate();
            }
            DriverRegistry.unregisterDriverUser(driver, this);
        }
    }

    public abstract void performAction(XhiveFederationIf federation) throws Exception;

    protected final XhiveSessionIf getSession() {
        return session;
    }
}


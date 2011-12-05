package com.xhive.adminclient;

import com.xhive.adminclient.dialogs.XhiveMessageDialog;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.error.XhiveException;

public abstract class XhiveTransactionWrapper {

    private XhiveSessionIf session;

    public XhiveTransactionWrapper(XhiveSessionIf session, boolean readOnly) {
        if (session != null) {
            this.session = session;
        } else {
            this.session = AdminMainFrame.getSession(readOnly);
        }
    }

    protected XhiveSessionIf getSession() {
        // If we throw exception here, more clear what was going on
        if (session == null) {
            throw new XhiveException(XhiveException.INTERNAL_ERROR, "getSession called when no session available");
        }
        return session;
    }

    public Object start() {
        Object result = null;
        boolean wasJoined = session.isJoined();
        if (!wasJoined) {
            session.join();
        }
        boolean wasOpened = session.isOpen();
        try {
            if (!wasOpened) {
                session.begin();
            }
            try {
                result = transactedAction();
                if (!wasOpened) {
                    if (! (result instanceof XhiveCancellation)) {
                        session.commit();
                    } else {
                        // Cancellation means rollback
                        session.rollback();
                    }
                }
                postTransactionAction();
            } catch (Throwable t) {
                // TODO (ADQ) : What if the transaction was already opened?
                if (session.isOpen()) {
                    session.rollback();
                }
                XhiveMessageDialog.showThrowable(t);
            }
        }
        finally {
            if (!wasJoined) {
                session.leave();
            }
            if (!wasOpened) {
                // Othogonal with XhiveTransactedSwingWorker::empty constructor
                returnSession();
            }
        }
        return result;
    }

    private void returnSession() {
                           AdminMainFrame.returnSession(session);
                           session = null;
                       }

                       protected abstract Object transactedAction() throws Exception;

    /**
     * Can be overridden in subclasses
     */
    protected void postTransactionAction() {
    }

}

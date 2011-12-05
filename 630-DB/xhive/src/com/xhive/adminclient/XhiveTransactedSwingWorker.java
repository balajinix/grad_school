package com.xhive.adminclient;

import com.xhive.core.interfaces.XhiveSessionIf;

/**
 * Swing worker that starts and ends a transaction
 */
public abstract class XhiveTransactedSwingWorker extends XhiveSwingWorker {

    private XhiveSessionIf session = null;
    private boolean readOnly = false;

    public XhiveTransactedSwingWorker(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public XhiveTransactedSwingWorker(XhiveSessionIf session, boolean readOnly) {
        this.session = session;
        this.readOnly = readOnly;
    }

    protected XhiveSessionIf getSession() {
        return session;
    }

    @Override
    protected boolean isTransactionOpened() {
        return session.isOpen();
    }

    @Override
    protected void join() {
        session.waitedJoin();
    }

    @Override
    protected void leave() {
        session.leave();
    }

    @Override
    protected void returnSession() {
                             AdminMainFrame.returnSession(session);
                         }

                         @Override
                         protected void begin() {
                             session.begin();
                         }

                         @Override
                         protected void commit() {
                             session.commit();
                         }

                         @Override
                         protected void rollback() {
                             session.rollback();
                         }

                         @Override
                         public Object construct() {
                             //    System.out.println("What transacted swing worker am I? " + getClass().getName());
                             if (session == null) {
                                 session = AdminMainFrame.getSession(readOnly);
                             }
                             return super.construct();
                         }
                     }

package com.xhive.adminclient;

import com.xhive.adminclient.dialogs.SwingWorker;
import com.xhive.adminclient.dialogs.XhiveMessageDialog;
import com.xhive.error.XhiveException;

/**
 * Swing worker that catches exceptions and shows them in the event thread. It also cals transaction event
 * handler, but these are ignored in this class.
 */
public abstract class XhiveSwingWorker extends SwingWorker {

    public XhiveSwingWorker() {}


    protected void lockEnvironment() {}


    protected void unlockEnvironment() {}


    protected boolean isTransactionOpened() {
        return false;
    }

    protected void join() {}


    protected void leave() {}


    protected void begin() {}


    protected void commit() {}


    protected void rollback() {}


    public Object construct() {
        join();
        boolean wasOpened = isTransactionOpened();
        try {
            if (!wasOpened) {
                begin();
            }
            try {
                Object result = xhiveConstruct();
                if (!wasOpened) {
                    commit();
                }
                return result;
            } catch (Throwable t) {
                // If the transaction was already opened, we still need to rollback here because the
                // error may have been serious.
                // TODO (ADQ) : Make sure the above situation is handled correctly!
                if (isTransactionOpened() && rollbackRequired(t)) {
                    rollback();
                }
                return t;
            }
        }
        finally {
            leave();
            if (!wasOpened) {
                // Othogonal with XhiveTransactedSwingWorker::empty constructor
                returnSession();
            } else {
                //        System.out.println("not returned");
            }

        }
    }

    protected void returnSession() {
                         }


                         public final void finished() {
                             Object result = get();
                             if (result instanceof Throwable) {
                                 handleException((Throwable) result);
                             } else {
                                 xhiveFinished(result);
                             }
                             unlockEnvironment();
                         }

                         protected void handleException(Throwable t) {
                             XhiveMessageDialog.showThrowable(t);
                         }

                         protected abstract Object xhiveConstruct() throws Throwable;

    protected void xhiveStart() {
        lockEnvironment();
    }

    protected void xhiveFinished(Object result) {}


    protected boolean rollbackRequired(Throwable t) {
        return true;
    }

    public void start() {
        xhiveStart();
        super.start();
    }
}

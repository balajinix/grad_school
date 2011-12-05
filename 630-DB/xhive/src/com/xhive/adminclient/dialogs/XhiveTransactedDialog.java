package com.xhive.adminclient.dialogs;

import com.xhive.adminclient.XhiveSwingWorker;
import com.xhive.core.interfaces.XhiveSessionIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 *
 * Standard dialog used for Xhive dialogs which have
 * to perform the Ok action in a transaction.
 *
 * The dialogs expect a transaction will be actived by the
 * called, and will also be ended by the caller.
 *
 * The dialog will only assure the action is ran in a joined
 * thread
 */
// TODO (ADQ) : What if an exception occurs, and the session needs to be rollbacked
public abstract class XhiveTransactedDialog extends XhiveDialog {

    private XhiveSessionIf session = null;

    public XhiveTransactedDialog(String title, XhiveSessionIf session) {
        super(title);
        this.session = session;
    }

    protected XhiveSessionIf getSession() {
        return session;
    }

    protected void performOk() throws Exception {
        XhiveSwingWorker worker = new XhiveDialogSwingWorker(this) {
                                      protected void xhiveStart() {
                                          super.xhiveStart();
                                          // Dialogs are expected to be called from within a transaction. The actual action associated with
                                          // a dialog is executed from within another thread, so first we leave the calling thread.
                                          session.leave();
                                      }

                                      protected Object xhiveConstruct() throws Exception {
                                          // and then we join the worker thread
                                          session.join();
                                          try {
                                              return super.xhiveConstruct();
                                          }
                                          finally {
                                              // No matter what happens leave the worker thread again
                                              session.leave();
                                          }
                                      }

                                      protected void xhiveFinished(Object result) {
                                          super.xhiveFinished(result);
                                          // Join caller thread again
                                          session.join();
                                      }

                                      // When an exception occurred xhiveFinished is not called, so override it here
                                      // and join the caller thread again.
                                      protected void handleException(Throwable t) {
                                          super.handleException(t);
                                          // Join caller thread again
                                          session.join();
                                      }
                                  };
        worker.start();
    }
}

package com.xhive.adminclient;

import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.error.XhiveException;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class XhiveTransactedAction extends XhiveAction {

    private XhiveTransactionWrapper wrapper = null;
    // initialSession is almost always null
    private XhiveSessionIf initialSession = null;
    private boolean readOnly = false;

    public XhiveTransactedAction(boolean readOnly) {
        super();
        this.readOnly = readOnly;
    }

    public XhiveTransactedAction(String name) {
        super(name);
    }

    public XhiveTransactedAction(String name, Icon icon) {
        super(name, icon);
    }

    public XhiveTransactedAction(XhiveSessionIf session, String name, Icon icon) {
        super(name, icon);
        this.initialSession = session;
    }

    public XhiveTransactedAction(String name, int mnemonic) {
        super(name, mnemonic);
    }

    public XhiveTransactedAction(String name, int mnemonic, Icon icon) {
        super(name, mnemonic, icon);
    }

    public XhiveTransactedAction(String name, Icon icon, String hint, int mnemonic) {
        super(name, icon, hint, mnemonic);
    }

    protected XhiveSessionIf getSession() {
        if (wrapper != null) {
            return wrapper.getSession();
        } else {
            throw new XhiveException(XhiveException.INTERNAL_ERROR, "getSession called when no session available in " + this.getClass().getName());
        }
    }

    // TODO (ADQ) : Block stuff?
    public void actionPerformed(final ActionEvent e) {
        //    System.out.println("What kind of transacted action am I? " + getClass().getName());
        // initialSession is almost always null
        wrapper = new XhiveTransactionWrapper(initialSession, readOnly) {
                      protected Object transactedAction() throws Exception {
                          try {
                              xhiveActionPerformed(e);
                          } catch (XhiveCancellation e) {
                              return e;
                          }
                          return null;
                      }

                      protected void postTransactionAction() {
                          XhiveTransactedAction.this.postTransactionAction();
                      }
                  };
        wrapper.start();
    }

    /**
     * Can be overridden in subclasses, for actions that must take place
     * after the transaction has completed.
     */
    protected void postTransactionAction() {
    }

}

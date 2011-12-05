package com.xhive.adminclient;

import javax.swing.*;
import java.awt.event.ActionEvent;

// TODO: The original goal here was to have a status dialog while the action was going
// instead of only a status message change.
public abstract class XhiveThreadedTransactedAction extends XhiveTransactedAction {

    private String message;

    public XhiveThreadedTransactedAction(String name, Icon icon) {
        super(name, icon);
        this.message = name;
    }

    public void actionPerformedSuper(ActionEvent e) {
        super.actionPerformed(e);
    }

    public final void actionPerformed(final ActionEvent e) {
        AdminMainFrame.setStatus("Busy in background with: " + message + "...");
        XhiveSwingWorker worker = new XhiveSwingWorker() {
                                      protected Object xhiveConstruct() throws Exception {
                                          actionPerformedSuper(e);
                                          return Boolean.TRUE;
                                      }

                                      protected void xhiveFinished(Object result) {
                                          super.xhiveFinished(result);
                                          AdminMainFrame.setStatus("");
                                      }
                                  };
        worker.start();
    }

}

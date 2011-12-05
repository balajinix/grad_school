package com.xhive.adminclient;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import com.xhive.adminclient.dialogs.XhiveMessageDialog;

public abstract class XhiveAction extends AbstractAction {

    public XhiveAction() {
        super();
    }

    public XhiveAction(String name) {
        super(name);
    }

    public XhiveAction(String name, int mnemonic) {
        super(name);
        putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
    }

    public XhiveAction(String name, int mnemonic, Icon icon) {
        super(name, icon);
        putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
    }

    public XhiveAction(String name, Icon icon) {
        super(name, icon);
    }

    public XhiveAction(String name, Icon icon, String hint, int mnemonic) {
        super(name, icon);
        putValue(Action.SHORT_DESCRIPTION, hint);
        putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
    }

    public void actionPerformed(ActionEvent e) {
        try {
            //      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
            xhiveActionPerformed(e);
        } catch (XhiveCancellation cancellation) {
            // No need to perform any action
        }

        catch (Exception exception) {
            XhiveMessageDialog.showException(exception);
            //      exception.printStackTrace();
        }
        finally {
            //      setCursor(null);
        }

    }

    protected abstract void xhiveActionPerformed(ActionEvent e) throws Exception;
}

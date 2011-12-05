package com.xhive.adminclient.dialogs;

import com.xhive.adminclient.XhiveSwingWorker;

public class XhiveDialogSwingWorker extends XhiveSwingWorker {

    private XhiveDialog dialog;

    XhiveDialogSwingWorker(XhiveDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    protected Object xhiveConstruct() throws Exception {
        return new Boolean(dialog.performAction());
    }

    @Override
    protected void lockEnvironment() {
        super.lockEnvironment();
        dialog.lockDialog();
    }

    @Override
    protected void unlockEnvironment() {
        dialog.unlockDialog();
        super.unlockEnvironment();
    }

    @Override
    protected void handleException(Throwable t) {
        // If an exception occurs, then we set the exception property on the dialog. The dialog
        // will then throw the exception as a result of the execute method.
        dialog.setActionException(t);
        dialog.dispose();
    }

    @Override
    protected void xhiveFinished(Object result) {
        if (((Boolean) result).booleanValue()) {
            dialog.performActionFinished();
            dialog.setResult(XhiveDialog.RESULT_OK);
            dialog.dispose();
        }
    }
}

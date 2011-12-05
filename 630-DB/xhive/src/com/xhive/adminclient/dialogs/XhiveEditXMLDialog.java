package com.xhive.adminclient.dialogs;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveSwingWorker;
import com.xhive.adminclient.XhiveTextArea;
import com.xhive.adminclient.panes.XhiveEditPanel;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSInput;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.io.Reader;
import java.io.StringReader;

public abstract class XhiveEditXMLDialog extends XhiveTransactedDialog implements DocumentListener {

    private XhiveTextArea textArea;

    // These objects are only used when the save action is executed. They are used to pass
    // parameters that cannot be passed as a parameter of a method call
    private Object storeTextResult;
    private LSParser builder;

    private XhiveAction saveAction = new XhiveAction("Save changes", XhiveResourceFactory.getIconSave(),
                                     "Save changes", 's') {
                                         public void xhiveActionPerformed(ActionEvent e) throws Exception {
                                             // Session must be joined again (see SetFields)
                                             getSession().join();
                                             XhiveLibraryIf library = getLibrary(getSession());
                                             builder = library.createLSParser();
                                             setParserOptions(builder);
                                             // Try to look for an internal subset, if so, set some options to keep the internal subset
                                             // (that can always be overruled by the user)
                                             String text = textArea.getText().toLowerCase();
                                             if ((text.indexOf("!doctype") != -1) && (text.indexOf("[") != -1)) {
                                                 builder.getDomConfig().setParameter("xhive-store-schema", Boolean.TRUE);
                                                 builder.getDomConfig().setParameter("xhive-store-schema-only-internal-subset", Boolean.TRUE);
                                             }
                                             // This feature was basically introduced to be used here, allow for optimized save
                                             XhiveDomBuilderConfigurationDialog dialog = new XhiveDomBuilderConfigurationDialog(builder);
                                             if (dialog.execute() == XhiveDialog.RESULT_OK) {
                                                 performOk();
                                             }
                                         }
                                     };

    protected void setParserOptions(LSParser builder) {
        // Empty, can be overridden if you want to set extra options
    }


    public XhiveEditXMLDialog(XhiveSessionIf session) {
        super("Edit", session);
        // Because of session-join problems do not allow closing of dialog
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    protected JPanel buildFieldsPanel() {
        XhiveEditPanel editPanel = new XhiveEditPanel(XhiveTextArea.TYPE_XML, true);
        textArea = editPanel.getTextArea();
        textArea.getActionMap().put("Save", saveAction);
        textArea.getInputMap().put(KeyStroke.getKeyStroke("control S"), "Save");
        return editPanel;
    }

    protected JPanel buildButtonPanel() {
        JPanel panel = super.buildButtonPanel();
        okButton.setAction(saveAction);
        return panel;
    }

    protected void setFields() {
        final XhiveSessionIf session = getSession();
        setTitle(getDocumentTitle(session));
        textArea.setEnabled(false);
        // Session no longer in use in this thread
        // This leave is here to prevent the session being joined in two sessions (the
        // second one is the worker started below)
        session.leave();
        XhiveSwingWorker worker = new XhiveSwingWorker() {
                                      protected void join() {
                                          session.join();
                                      }

                                      protected void leave() {
                                          session.leave();
                                      }

                                      protected Object xhiveConstruct() throws Exception {
                                          return getText(getSession());
                                      }

                                      protected void xhiveFinished(Object result) {
                                          textArea.setText((String) result);
                                          textArea.setEnabled(true);
                                          textArea.select(0, 0);
                                          textArea.getDocument().addDocumentListener(XhiveEditXMLDialog.this);
                                          textArea.requestFocus();
                                      }
                                  };
        worker.start();
    }

    protected abstract XhiveLibraryIf getLibrary(XhiveSessionIf session);

    // This method is called from another thread
    protected abstract Object storeText(XhiveSessionIf session, final LSParser builder, LSInput input);

    // This is called when store text is ready
    protected abstract void storeTextFinished(Object result);

    protected abstract String getText(XhiveSessionIf session) throws Exception;

    protected abstract String getDocumentTitle(XhiveSessionIf session);

    protected boolean performAction() throws Exception {
        XhiveLibraryIf library = getLibrary(getSession());
        final Reader reader = new StringReader(textArea.getText());
        final LSInput input = library.createLSInput();
        input.setCharacterStream(reader);
        try {
            storeTextResult = storeText(getSession(), builder, input);
        } catch (Exception e) {
            XhiveMessageDialog.showException(this, e);
            return false;
        }
        return true;
    }

    /**
     * SBO: performOk is not overridden because join already happens in the save action
     */
    protected void performCancel() {
        // Session must be joined again (see SetFields)
        getSession().join();
        super.performCancel();
    }

    protected void performActionFinished() {
        storeTextFinished(storeTextResult);
    }

    public void insertUpdate(DocumentEvent e) {
        saveAction.setEnabled(true);
    }

    public void removeUpdate(DocumentEvent e) {
        saveAction.setEnabled(true);
    }

    public void changedUpdate(DocumentEvent e) {
        saveAction.setEnabled(true);
    }
}

package com.xhive.adminclient.dialogs;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.xhive.adminclient.layouts.StackLayout;
import com.xhive.adminclient.panes.XhiveDOMConfigurationPanel;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for editing the properties of a document.
 */
public abstract class XhiveDocumentDialog extends XhiveLibraryChildDialog {

    protected JTextField encodingField;

    public static XhiveDocumentIf showCreateDocument(XhiveSessionIf session, XhiveLibraryIf library) {
        CreateDocumentDialog dialog = new CreateDocumentDialog(session, library);
        dialog.execute();
        return dialog.getNewDocument();
    }

    public static int showEditDocument(XhiveSessionIf session, XhiveDocumentIf document) {
        XhiveDocumentDialog dialog = new EditDocumentDialog(session, document);
        return dialog.execute();
    }

    protected XhiveDocumentDialog(String title, XhiveSessionIf session) {
        super(title, session);
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = super.buildFieldsPanel();
        encodingField = new JTextField();
        fieldsPanel.add(new JLabel("Encoding"));
        fieldsPanel.add(encodingField);
        return fieldsPanel;
    }
}

class EditDocumentDialog extends XhiveDocumentDialog {

    private XhiveDocumentIf document;

    EditDocumentDialog(XhiveSessionIf session, XhiveDocumentIf document) {
        super("Document properties", session);
        this.document = document;
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = super.buildFieldsPanel();
        JPanel newFieldsPanel = new JPanel(new StackLayout());
        newFieldsPanel.add(fieldsPanel);
        newFieldsPanel.add(XhiveDOMConfigurationPanel.getNormalizePanel(document.getConfig()));
        return newFieldsPanel;
    }

    @Override
    protected void setFields() {
        setWaitText("Please wait while updating document...");
        nameField.setText(document.getName());
        descriptionField.setText(document.getDescription());
        if (document.getEncoding() != null) {
            encodingField.setText(document.getEncoding());
        }
    }

    @Override
    protected boolean performAction() {
        document.setName(nameField.getText());
        document.setDescription(descriptionField.getText());
        if (encodingField.getText().equals("")) {
            document.setEncoding(null);
        } else {
            document.setEncoding(encodingField.getText());
        }
        return true;
    }

    @Override
    protected boolean fieldsAreValid() {
        if (document.getOwnerLibrary().get(nameField.getText()) != null &&
                !document.getName().equalsIgnoreCase(nameField.getText())) {
            XhiveMessageDialog.showErrorMessage("Librarychild with name \"" + nameField.getText() + "\" already exists.");
            return false;
        }
        return true;
    }
}

class CreateDocumentDialog extends XhiveDocumentDialog {

    protected JTextField documentElementField;
    protected JTextField namespaceField;

    private XhiveDocumentIf newDocument;
    private XhiveLibraryIf library;

    CreateDocumentDialog(XhiveSessionIf session, XhiveLibraryIf library) {
        super("Create document", session);
        this.library = library;
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = super.buildFieldsPanel();
        documentElementField = new JTextField();
        namespaceField = new JTextField();
        fieldsPanel.add(new JLabel("Document element:"));
        fieldsPanel.add(documentElementField);
        fieldsPanel.add(new JLabel("Namespace URI:"));
        fieldsPanel.add(namespaceField);
        return fieldsPanel;
    }

    @Override
    protected void setFields() {
        setWaitText("Please wait while creating document...");
    }

    @Override
    protected boolean performAction() {
        String namespace = namespaceField.getText();
        if (namespace.equals("")) {
            namespace = null;
        }
        newDocument = (XhiveDocumentIf) library.createDocument(namespace, documentElementField.getText(), null);
        newDocument.setName(nameField.getText());
        newDocument.setDescription(descriptionField.getText());
        String encoding = encodingField.getText().trim();
        if (! encoding.equals("")) {
            newDocument.setEncoding(encoding);
        }
        library.appendChild(newDocument);
        return true;
    }

    @Override
    protected boolean fieldsAreValid() {
        if (library != null && library.get(nameField.getText()) != null) {
            XhiveMessageDialog.showErrorMessage("Librarychild with name \"" + nameField.getText() + "\" already exists.");
            return false;
        }
        return checkFieldNotEmpty(documentElementField, "You should specify a document element!");
    }

    protected XhiveDocumentIf getNewDocument() {
        return newDocument;
    }
}

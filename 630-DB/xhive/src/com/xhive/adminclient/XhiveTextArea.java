package com.xhive.adminclient;

import com.xhive.adminclient.jeditor.REXMLTypes;
import com.xhive.util.jeditor.EditorDocument;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;

public class XhiveTextArea extends JEditorPane {

    private static final int MAX_HIGHLIGHT_SIZE = 250000;

    private static final boolean DEBUG_GC = false;
    private static int textAreaCount = 0;

    public static final short TYPE_NONE = 0;
    public static final short TYPE_XML = 1;
    public static final short TYPE_XQUERY = 2;

    private short highlightType = TYPE_NONE;
    private UndoManager undo;

    public XhiveTextArea(short highlightType) {
        super();
        if (DEBUG_GC) {
            System.out.println("--created textArea " + (++textAreaCount));
        }
        this.highlightType = highlightType;
        if (highlightType == TYPE_XML) {
            setEditorKit(new REXMLTypes.XMLKit());
        } else if (highlightType == TYPE_XQUERY) {
            setEditorKit(new REXMLTypes.XQueryKit());
        }

        //    setTabSize(2);
        setEditable(true);
        setFont(new Font("Courier New", Font.PLAIN, 12));

        undo = new UndoManager();
        javax.swing.text.Document doc = getDocument();

        doc.addUndoableEditListener(
            new UndoableEditListener() {
                public void undoableEditHappened(UndoableEditEvent evt) {
                    undo.addEdit(evt.getEdit());
                }
            }
        );

        getActionMap().put("Undo",
                           new AbstractAction("Undo") {
                               public void actionPerformed(ActionEvent evt) {
                                   try {
                                       if (undo.canUndo()) {
                                           undo.undo();
                                       }
                                   } catch (CannotUndoException e) {}

                               }
                           }
                          );

        getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

        getActionMap().put("Redo",
                           new AbstractAction("Redo") {
                               public void actionPerformed(ActionEvent evt) {
                                   try {
                                       if (undo.canRedo()) {
                                           undo.redo();
                                       }
                                   } catch (CannotRedoException e) {}

                               }
                           }
                          );

        getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");

        addMouseListener(
            new XhiveMouseListener() {
                public void popupRequested(MouseEvent e) {
                    JPopupMenu popupMenu = new JPopupMenu("Edit");
                    popupMenu.add(
                        new XhiveAction("Cut") {
                            public void xhiveActionPerformed(ActionEvent e) {
                                cut();
                            }
                        }
                    );
                    popupMenu.add(
                        new XhiveAction("Copy") {
                            public void xhiveActionPerformed(ActionEvent e) {
                                copy();
                            }
                        }
                    );
                    popupMenu.add(
                        new XhiveAction("Paste") {
                            public void xhiveActionPerformed(ActionEvent e) {
                                paste();
                            }
                        }
                    );
                    popupMenu.show(XhiveTextArea.this, e.getX(), e.getY());
                }
            }
        );

    }

    // used to make sure user can't undo to "retrieving data..." part
    public void discardAllEdits() {
        if (undo != null) {
            undo.discardAllEdits();
        }
    }

    protected void finalize() throws Throwable {
        if (DEBUG_GC) {
            System.out.println("--gc'd textArea " + (--textAreaCount));
        }
        super.finalize();
    }

    public void setText(String text) {
        EditorDocument editorDocument = null;
        if (getEditorKit() instanceof REXMLTypes.XhiveStyledEditorKit) {
            editorDocument = ((REXMLTypes.XhiveStyledEditorKit) getEditorKit()).getDefaultDocument();
        }
        // Do not attempt to syntax highlight really big text-area's
        if (text != null) {
            //      System.out.println("text.length() = " + text.length());
            if ((text.length() > MAX_HIGHLIGHT_SIZE) && (highlightType != TYPE_NONE)) {
                if (editorDocument != null) {
                    editorDocument.setHighlightAllowed(false);
                }
            }
        }
        if (text == null) {
            text = "";
        }
        if (editorDocument != null) {
            editorDocument.setTextSettingMode(true);
        }
        super.setText(text);
        if (editorDocument != null) {
            editorDocument.setTextSettingMode(false);
        }
    }

    public OutputStream getOutputStream() {
        return new TextAreaOutputStream();
    }

    class TextAreaOutputStream extends OutputStream {

        protected void insertLater(final String text) {
            Runnable runnable = new Runnable() {
                                    public void run() {
                                        //          insert(text, getText().length());
                                        setText(getText() + text);
                                    }
                                };
            SwingUtilities.invokeLater(runnable);
        }

        public void write(int b) throws IOException {
            throw new RuntimeException("Dont know what to do");
        }

        public void write(byte b[]) throws IOException {
            insertLater(new String(b, "UTF-8"));
        }

        public void write(byte b[], int off, int len) throws IOException {
            insertLater(new String(b, off, len, "UTF-8"));
        }
    }
}


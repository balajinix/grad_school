package com.xhive.adminclient.dialogs;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.resources.XhiveResourceFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for showing errors.
 */
public class XhiveMessageDialog extends XhiveDialog {

    public static final int RESULT_SKIP = 2;
    public static final int RESULT_SKIP_ALL = 3;

    protected JLabel messageLabel;
    protected JPanel messagePanel;
    protected JList messageList;


    public static void showThrowable(Throwable throwable) {
        XhiveMessageDialog dialog = new ExceptionDialog(throwable);
        dialog.execute();
    }

    public static void showThrowable(Dialog owner, Throwable throwable) {
        XhiveMessageDialog dialog = new ExceptionDialog(owner, throwable);
        dialog.execute();
    }

    public static void showException(Exception exception) {
        XhiveMessageDialog dialog = new ExceptionDialog(exception);
        dialog.execute();
    }

    public static void showException(Dialog owner, Exception exception) {
        XhiveMessageDialog dialog = new ExceptionDialog(owner, exception);
        dialog.execute();
    }

    public static int showParseException(Dialog owner, Exception exception, String documentName) {
        XhiveMessageDialog dialog = new ParseExceptionDialog(owner, exception, documentName);
        return dialog.execute();
    }

    public static void showErrorMessage(String message) {
        XhiveMessageDialog dialog = new ErrorDialog(message, null);
        dialog.execute();
    }

    public static void showErrorMessage(Dialog owner, String message) {
        XhiveMessageDialog dialog = new ErrorDialog(owner, message, null);
        dialog.execute();
    }

    public static void showErrorMessage(String message, String longMessage) {
        XhiveMessageDialog dialog = new ErrorDialog(message, longMessage);
        dialog.execute();
    }

    public XhiveMessageDialog(Dialog owner, String title) {
        super(owner, title);
    }

    public XhiveMessageDialog(String title) {
        super(title);
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new BorderLayout());
        messageLabel = new JLabel("No message", XhiveResourceFactory.getIconError(), SwingConstants.LEFT);
        messageLabel.setIconTextGap(16);
        fieldsPanel.add(messageLabel, BorderLayout.NORTH);

        messagePanel = new JPanel(new BorderLayout());
        messagePanel.setVisible(false);
        messageList = new JList();
        messageList.setFont(new Font("Courier New", Font.PLAIN, 12));
        JScrollPane messageScrollPane = new JScrollPane(messageList);
        messageScrollPane.setPreferredSize(new Dimension(600, 200));
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);

        JPanel messageButtonPanel = new JPanel();
        messageButtonPanel.add(new JButton(new XhiveAction("Copy to console", 'o') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   copyToConsole();
                                               }
                                           }
                                          ));
        messageButtonPanel.add(new JButton(new XhiveAction("Copy", 'C') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) {
                                                   copyToClipboard();
                                               }
                                           }
                                          ));
        messagePanel.add(messageButtonPanel, BorderLayout.SOUTH);


        fieldsPanel.add(messagePanel, BorderLayout.CENTER);
        return fieldsPanel;
    }


    protected static String formatMessage(String message) {
        message = replaceString(message, "\t", "    ");
        message = replaceString(message, "\r\n", "\n");
        message = replaceString(message, "\r", "\n");
        return message;
    }

    public static String replaceString(String text, String repl, String with) {
        if (text == null) {
            return null;
        }

        StringBuffer buffer = new StringBuffer(text.length());
        int start = 0;
        int end = 0;
        while ((end = text.indexOf(repl, start)) != -1) {
            buffer.append(text.substring(start, end)).append(with);
            start = end + repl.length();

        }
        buffer.append(text.substring(start));

        return buffer.toString();
    }

    protected void setListText(JList list, String message) {
        message = formatMessage(message);

        DefaultListModel listModel = new DefaultListModel();

        StringTokenizer stringTokenizer = new StringTokenizer(message, "\n");

        while (stringTokenizer.hasMoreTokens()) {
            listModel.addElement(stringTokenizer.nextToken());
        }

        list.setModel(listModel);
    }

    private void copyToClipboard() {
        Clipboard clipboard = getToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(copyTo()), null);
    }

    private void copyToConsole() {
        System.out.println(copyTo());
    }

    private String copyTo() {
        String stackTrace = "";
        DefaultListModel listModel = (DefaultListModel) messageList.getModel();

        for (int i = 0; i < listModel.getSize(); i++) {
            stackTrace += ((String) listModel.elementAt(i)) + '\n';
        }

        return stackTrace;
    }
}

class ExceptionDialog extends XhiveMessageDialog {

    private boolean detailed = false;
    private Throwable throwable;

    private JButton detailsButton;

    @Override
    protected JPanel buildButtonPanel() {
        JPanel buttonPanel = super.buildButtonPanel();
        cancelButton.setVisible(false);
        detailsButton = new JButton(new XhiveAction("Details", 'D') {
                                        @Override
                                        protected void xhiveActionPerformed(ActionEvent e) {
                                            detailed = !detailed;
                                            setFields();
                                            resizeAndCenter();
                                        }
                                    }
                                   );
        buttonPanel.add(detailsButton);
        return buttonPanel;
    }

    public ExceptionDialog(Throwable throwable) {
        super("Exception message");
        this.throwable = throwable;
    }

    public ExceptionDialog(Dialog owner, Throwable throwable) {
        super(owner, "Exception message");
        this.throwable = throwable;
    }

    public ExceptionDialog(Dialog owner, String title, Throwable throwable) {
        super(owner, title);
        this.throwable = throwable;
    }

    @Override
    public void setFields() {
        String message = throwable.getMessage();
        if (message == null) {
            message = throwable.getClass().getName();
        }
        if (detailed) {
            setListText(messageList, getStackTrace(throwable));
            messagePanel.setVisible(true);
        } else {
            if (message.indexOf("\n") > 0) {
                setListText(messageList, message);
                messagePanel.setVisible(true);
            } else {
                messageLabel.setText(message);
                messagePanel.setVisible(false);
            }
        }
        detailsButton.setText(detailed ? "No details" : "Details");
    }

    private String getStackTrace(Throwable t) {
        StringWriter stackTrace = new StringWriter();
        t.printStackTrace(new PrintWriter(stackTrace, true));
        return stackTrace.toString();
    }
}

class ParseExceptionDialog extends ExceptionDialog {

    private JButton skipAllButton;
    private JLabel documentLabel;
    private String documentName;

    private XhiveAction skipAction = new XhiveAction("Skip", 'S') {
                                         @Override
                                         protected void xhiveActionPerformed(ActionEvent e) {
                                             setResult(RESULT_SKIP);
                                             dispose();
                                         }
                                     };

    private XhiveAction skipAllAction = new XhiveAction("Skip all", 'a') {
                                            @Override
                                            protected void xhiveActionPerformed(ActionEvent e) {
                                                setResult(RESULT_SKIP_ALL);
                                                dispose();
                                            }
                                        };

    public ParseExceptionDialog(Dialog owner, Throwable throwable, String documentName) {
        super(owner, "Parse exception on " + documentName, throwable);
        this.documentName = documentName;
    }

    @Override
    protected JPanel buildButtonPanel() {
        JPanel buttonPanel = super.buildButtonPanel();
        okButton.setVisible(false);
        cancelButton.setVisible(true);
        buttonPanel.add(new JButton(skipAllAction), 0);
        buttonPanel.add(new JButton(skipAction), 0);
        return buttonPanel;
    }
}

class ErrorDialog extends XhiveMessageDialog {

    private String shortMessage;
    private String longMessage;

    public ErrorDialog(Dialog owner, String shortMessage, String longMessage) {
        super(owner, "Xhive error message");
        this.shortMessage = shortMessage;
        this.longMessage = longMessage;
    }

    public ErrorDialog(String shortMessage, String longMessage) {
        super("Xhive error message");
        this.shortMessage = shortMessage;
        this.longMessage = longMessage;
    }

    @Override
    protected void setFields() {
        messageLabel.setText(shortMessage);
        if (longMessage != null) {
            messagePanel.setVisible(true);
            setListText(messageList, longMessage);
        }
    }
}


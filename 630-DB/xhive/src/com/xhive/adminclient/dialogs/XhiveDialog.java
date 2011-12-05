package com.xhive.adminclient.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveCancellation;
import com.xhive.adminclient.XhiveSwingWorker;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.error.XhiveException;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Standard dialog used for Xhive dialogs
 */
public abstract class XhiveDialog extends JDialog {

    public static final int RESULT_OK = 0;
    public static final int RESULT_CANCEL = 1;

    /**
     * Executing a dialog may result in an exception. If an exception occurs in the performAction method
     * it is caught and rethrown as a result of the excute method. It needs to be stored here because
     * the method is thrown in a different thread, and can not propagate directly to the calling thread.
     */
    private Throwable actionException;

    private int result = RESULT_OK;
    private String waitText;
    private Vector<Component> disabledComponents;

    protected JButton okButton;
    protected JButton cancelButton;

    private XhiveAction okAction = new XhiveAction("OK", 'o') {
                                       @Override
                                       public void xhiveActionPerformed(ActionEvent e) throws Exception {
                                           if (fieldsAreValid()) {
                                               performOk();
                                           }
                                       }
                                   };

    private XhiveAction cancelAction = new XhiveAction("Cancel", 'c') {
                                           @Override
                                           protected void xhiveActionPerformed(ActionEvent e) {
                                               result = RESULT_CANCEL;
                                               try {
                                                   performCancel();
                                               }
                                               finally {
                                                   dispose();
                                               }
                                           }
                                       };

    protected XhiveDialog(String title) {
        super(AdminMainFrame.getInstance(), title);
        init(title);
    }

    protected XhiveDialog(Dialog owner, String title) {
        super(owner);
        init(title);
    }

    protected void setResult(int result) {
        this.result = result;
    }

    private void init(String title) {
        setTitle(title);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        disabledComponents = new Vector<Component>();
    }

    // Build is done before execute, so the session is available in transacted dialogs.
    private void build() {
        Container contentPane = getContentPane();

        // Content panel is necessary for border
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        contentPanel.add(buildFieldsPanel(), BorderLayout.CENTER);
        JPanel buttonPanel = buildButtonPanel();
        if (buttonPanel != null) {
            contentPanel.add(buildButtonPanel(), BorderLayout.SOUTH);
        }
        contentPane.add(contentPanel);
        getRootPane().setDefaultButton(okButton);
    }

    protected JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        okButton = new JButton(okAction);
        cancelButton = new JButton(cancelAction);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    protected JButton getOkButton() {
        return okButton;
    }

    protected abstract JPanel buildFieldsPanel();

    public int execute() {
        build();
        setFields();
        resizeAndCenter();
        setVisible(true);
        // If an exception occurred it needs to be rethrown here.
        if (actionException != null) {
            if (actionException instanceof RuntimeException) {
                throw (RuntimeException) actionException;
            } else {
                // I Hate checked exceptions :(
                throw new XhiveException(XhiveException.INVALID_USE, actionException);
            }
        }
        return result;
    }

    void setActionException(Throwable t) {
        actionException = t;
    }

    void lockDialog() {
        if (waitText != null && !waitText.equals("")) {
            AdminMainFrame.setStatus(waitText);
        }
        setComponentsEnabled(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    void unlockDialog() {
        if (waitText != null && !waitText.equals("")) {
            AdminMainFrame.clearStatus();
        }
        setComponentsEnabled(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * This method is called when the cancel button is pressed
     * (Used to be nothing, but because of bug 647 it was decided it is better to send a signal here
     * so it can be sensed elsewhere cancel was clicked).
     */
    protected void performCancel() {
        // Simply throwing the exception does not work
        setActionException(new XhiveCancellation());
    }

    /**
     * This method is called when the ok button is pressed
     */
    protected void performOk() throws Exception {
        XhiveSwingWorker worker = new XhiveDialogSwingWorker(this);
        worker.start();
    }

    private void setComponentsEnabled(boolean enabled) {
        setComponentsEnabledRecursive(getComponents(), enabled);

        if (enabled) {
            disabledComponents.clear();
        }
    }

    private void setComponentsEnabledRecursive(Component[] components, boolean enabled) {
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JButton ||
                    components[i] instanceof JCheckBox ||
                    components[i] instanceof JComboBox ||
                    components[i] instanceof JRadioButton ||
                    components[i] instanceof JTextArea ||
                    components[i] instanceof JPasswordField ||
                    components[i] instanceof JTree ||
                    components[i] instanceof JList ||
                    components[i] instanceof JLabel ||
                    components[i] instanceof JTextField) {
                if (!enabled) {
                    if (components[i].isEnabled()) {
                        components[i].setEnabled(false);
                        disabledComponents.add(components[i]);
                    }
                } else {
                    if (disabledComponents.contains(components[i])) {
                        components[i].setEnabled(true);
                    }
                }
            }

            if (components[i] instanceof Container) {
                setComponentsEnabledRecursive(((Container) components[i]).getComponents(), enabled);
            }
        }
    }

    protected void setFields() {
        // Default no-op
    }

    // Should return true if successful

    protected boolean performAction() throws Exception {
        return true;
    }

    protected void performActionFinished() {
        // Default no-op
    }


    protected boolean fieldsAreValid() {
        return true;
    }


    /**
     * Because of bug 698, after pack the dialogs are made bigger a few pixels.
     * pack calls this method.
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.setSize(size.getWidth() + 20.0, size.getHeight() + 10.0);
        return size;
    }

    public Dimension getPreferredSizeOriginal() {
        return super.getPreferredSize();
    }

    public void resizeAndCenter() {
        pack();

        Dimension Ownerdimension = new Dimension(getOwner().getWidth(), getOwner().getHeight());
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) (getOwner().getX() + (Ownerdimension.getWidth() - getWidth()) / 2);
        int y = (int) (getOwner().getY() + (Ownerdimension.getHeight() - getHeight()) / 2);
        x = (x > 0) ? x : 0;
        y = (y > 0) ? y : 0;
        x = ((x + getWidth()) < screenDimension.getWidth()) ? x : (int) (screenDimension.getWidth() - getWidth());
        y = ((y + getHeight()) < screenDimension.getHeight()) ? y : (int) (screenDimension.getHeight() - getHeight());
        setLocation(x, y);
    }

    protected boolean checkField(JComponent component, boolean condition, String errorMessage) {
        if (component.isVisible() && !condition) {
            showError(errorMessage, component);
            return false;
        } else {
            return true;
        }
    }

    protected boolean checkFieldNotEmpty(JTextField textField, String errorMessage) {
        return checkField(textField, !textField.getText().trim().equals(""), errorMessage);
    }

    protected boolean checkAreaNotEmpty(JTextArea textArea, String errorMessage) {
        return checkField(textArea, !textArea.getText().trim().equals(""), errorMessage);
    }

    protected boolean checkFieldLength(JTextField textField, int minimumLength, int maximumLength, String errorMessage) {
        return checkField(textField, inInterval(textField.getText().trim().length(), minimumLength, maximumLength), errorMessage);
    }

    protected boolean checkFieldEquals(JTextField textField, String value, String errorMessage) {
        return checkField(textField, textField.getText().trim().equals(value), errorMessage);
    }

    protected boolean checkFieldNotEquals(JTextField textField, String value, String errorMessage) {
        boolean condition;
        if (value == null) {
            condition = true;
        } else {
            condition = ! value.equals(textField.getText().trim());
        }
        return checkField(textField, condition, errorMessage);
    }

    protected boolean checkFieldIsInteger(JTextField textField, String errorMessage) {
        boolean result1 = true;

        try {
            Integer.parseInt(textField.getText());
        } catch (NumberFormatException e) {
            result1 = false;
        }

        return checkField(textField, result1, errorMessage);
    }

    protected boolean checkFieldIsLong(JTextField textField, boolean optional, String errorMessage) {
        boolean result1 = true;
        try {
            String value = textField.getText().trim();
            if (!optional || !value.equals("")) {
                Long.parseLong(textField.getText());
            }
        } catch (NumberFormatException e) {
            result1 = false;
        }
        return checkField(textField, result1, errorMessage);
    }

    protected boolean checkFieldIsFloat(JTextField textField, String errorMessage) {
        boolean result1 = true;

        try {
            Float.parseFloat(textField.getText());
        } catch (NumberFormatException e) {
            result1 = false;
        }

        return checkField(textField, result1, errorMessage);
    }

    protected boolean inInterval(int value, int startInterval, int endInterval) {
        return (value >= startInterval) && (value <= endInterval);
    }

    protected void showError(String message, Component componentToFocus) {
        XhiveMessageDialog.showErrorMessage(this, message);
        if (componentToFocus != null) {
            componentToFocus.requestFocus();
        }
    }

    protected static void setPreferredWidthOf(JComponent component, int preferredWidth) {
        Dimension preferredSize = component.getPreferredSize();
        preferredSize.setSize(preferredWidth, (int) preferredSize.getHeight());
        component.setPreferredSize(preferredSize);
    }

    protected static void setPreferredHeightOf(JComponent component, int preferredHeight) {
        Dimension preferredSize = component.getPreferredSize();
        preferredSize.setSize((int) preferredSize.getWidth(), preferredHeight);
        component.setPreferredSize(preferredSize);
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            escapeKeyPressed();
        }
        super.processKeyEvent(e);
    }

    protected void escapeKeyPressed() {
        dispose();
    }

    protected void setWaitText(String text) {
        waitText = text;
    }

    /**
     * GridLayout utility items
     */
    private GridBagConstraints gridBagConstraints = null;
    private JPanel gridPanel = null;
    protected void setGridPanel(JPanel panel) {
        gridPanel = panel;
        gridPanel.setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets.top = 5;
        gridBagConstraints.insets.left = 5;
        gridBagConstraints.gridx = -1;
        gridBagConstraints.gridy = 0;
    }
    protected void addToGrid(boolean newRow, Component c) {
        if (newRow) {
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy++;
        } else {
            gridBagConstraints.gridx++;
        }
        gridPanel.add(c, gridBagConstraints);
    }
    protected void addToGrid(boolean newRow, Component c, int gridWidth) {
        gridBagConstraints.gridwidth = gridWidth;
        addToGrid(newRow, c);
        gridBagConstraints.gridwidth = 1;
    }




    public static boolean showConfirmation(String message) {
        return showConfirmation(AdminMainFrame.getInstance(), message);
    }

    public static boolean showConfirmation(Component parent, String message) {
        return (JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                                              JOptionPane.YES_NO_OPTION,
                                              JOptionPane.QUESTION_MESSAGE, XhiveResourceFactory.getIconStop()) == JOptionPane.YES_OPTION);
    }
}

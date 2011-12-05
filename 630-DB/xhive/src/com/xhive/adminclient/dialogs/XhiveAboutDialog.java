package com.xhive.adminclient.dialogs;

import com.xhive.adminclient.XhiveSplashWindow;
import com.xhive.adminclient.resources.XhiveResourceFactory;

import javax.swing.*;
import java.awt.*;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * About dialog.
 *
 */
public class XhiveAboutDialog extends XhiveDialog {

    public static void showAbout() {
        XhiveAboutDialog dialog = new XhiveAboutDialog("About X-Hive/DB Administrator");
        dialog.execute();
    }

    public XhiveAboutDialog(String title) {
        super(title);
        setResizable(false);
    }

    protected JPanel buildFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new BorderLayout());
        JLabel imageLabel = new JLabelWithVersion(XhiveResourceFactory.getIconSplash());
        fieldsPanel.add(imageLabel, BorderLayout.CENTER);
        return fieldsPanel;
    }

    protected JPanel buildButtonPanel() {
        JPanel buttonPanel = super.buildButtonPanel();
        cancelButton.setVisible(false);
        return buttonPanel;
    }

    public Dimension getPreferredSize() {
        return getPreferredSizeOriginal();
    }

    private class JLabelWithVersion extends JLabel {

        Font font = null;

        private JLabelWithVersion(Icon icon) {
            super(icon);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            XhiveSplashWindow.paintVersion(g, font);
        }
    }
}

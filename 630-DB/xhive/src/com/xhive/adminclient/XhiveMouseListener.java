package com.xhive.adminclient;

import com.xhive.adminclient.dialogs.XhiveMessageDialog;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Mouse listener for doubleclick and popup menu
 *
 */
public class XhiveMouseListener extends MouseAdapter {

    public void mouseClicked(MouseEvent e) {
        try {
            if (e.getClickCount() == 2) {
                xhiveMouseDoubleClicked(e);
            } else {
                xhiveMouseClicked(e);
            }
        } catch (Exception exception) {
            XhiveMessageDialog.showException(exception);
        }
    }

    public void xhiveMouseClicked(MouseEvent e) throws Exception {}


    public void xhiveMouseDoubleClicked(MouseEvent e) throws Exception {}


    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            try {
                popupRequested(e);
            } catch (Exception exception) {
                XhiveMessageDialog.showException(exception);
            }
        }
    }

    public void popupRequested(MouseEvent e) {}

}

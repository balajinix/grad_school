package com.xhive.adminclient;

import javax.swing.*;

public class XhiveMenuItem extends JMenuItem {

    public XhiveMenuItem(XhiveAction action) {
        super(action);
        // TODO (ADQ) : How to change default font settings
        // popup menu's will also be adjusted
        setFont(new java.awt.Font("Dialog", 0, 11));
    }
}

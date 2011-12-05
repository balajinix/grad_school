package com.xhive.adminclient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class XhiveButton extends JButton {

    public XhiveButton(Action action) {
        super();
        setFont(new java.awt.Font("Dialog", 0, 9));
        // If this is not set all the buttons get a different size
        setMaximumSize(new Dimension(45, 45));
        setMinimumSize(new Dimension(45, 45));
        setAction(action);
        setVerticalTextPosition(SwingConstants.BOTTOM);
        setHorizontalTextPosition(SwingConstants.CENTER);
    }

    //  public XhiveButton(String text, Icon icon, String tooltip, char mnemonic, Action action) {
    //    this(text, icon, tooltip, mnemonic);
    //    addActionListener(action);
    //  }

    public XhiveButton(String text, char mnemonic, Action action) {
        super(text);
        setMnemonic(mnemonic);
        setAction(action);
    }
}

package com.xhive.adminclient;

import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.treenodes.XhiveTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class XhiveTreeCellRenderer extends DefaultTreeCellRenderer {

    private static Font idFont;
    private static Font defaultFont;

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        if (defaultFont == null) {
            defaultFont = tree.getFont();
        }

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof XhiveTreeNode) {
            XhiveTreeNode treeNode = (XhiveTreeNode) value;
            setIcon(XhiveResourceFactory.getImageIcon(treeNode.getIconName()));
            if (expanded) {
                setIcon(XhiveResourceFactory.getImageIcon(treeNode.getExpandedIconName()));
            }
            if (getText() != null && getText().startsWith("id:")) {
                if (idFont == null) {
                    idFont = defaultFont.deriveFont(Font.ITALIC, getFont().getSize() - 1);
                }
                setFont(idFont);
            } else {
                setFont(defaultFont);
            }
        }
        return this;
    }
}

package com.xhive.adminclient;

import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.treenodes.XhiveExtendedTreeNode;
import com.xhive.adminclient.treenodes.XhiveTreeNode;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class XhiveTableCellRenderer extends DefaultTableCellRenderer {

    private static Font idFont;
    private static Font defaultFont;

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (column == 0) {
            XhiveExtendedTreeNode treeNode = (XhiveExtendedTreeNode) table.getModel();
            TreeNode child = treeNode.getChildAt(row);
            if (child instanceof XhiveTreeNode) {
                setIcon(XhiveResourceFactory.getImageIcon(((XhiveTreeNode) child).getIconName()));
            }
        }

        if (getText() != null && getText().startsWith("id:")) {
            if (idFont == null) {
                defaultFont = table.getFont();
                idFont = table.getFont().deriveFont(Font.ITALIC, getFont().getSize() - 1);
            }
            setFont(idFont);
        } else {
            if (defaultFont == null) {
                defaultFont = table.getFont();
            }
            setFont(defaultFont);
        }
        return this;
    }
}

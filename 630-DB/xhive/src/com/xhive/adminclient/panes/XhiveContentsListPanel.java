package com.xhive.adminclient.panes;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.XhiveTableCellRenderer;
import com.xhive.adminclient.treenodes.XhiveExtendedTreeNode;
import com.xhive.adminclient.treenodes.XhiveTableModelTreeNode;
import com.xhive.core.interfaces.XhiveSessionIf;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class XhiveContentsListPanel extends XhiveTablePanel {

  public XhiveContentsListPanel(JTabbedPane parent, XhiveExtendedTreeNode node) {
    super(parent, node);
  }

  private XhiveExtendedTreeNode getChildAt(int index) {
    return (XhiveExtendedTreeNode) getSelectedNode().getChildAt(index);
  }

  protected void handleDeleteEvent(ActionEvent e) {
    // TODO (ADQ) : Allow multiple selected rows
    XhiveExtendedTreeNode nodeToDelete = getChildAt(getTable().getSelectedRow());
    nodeToDelete.doDelete(AdminMainFrame.getSession(false));
  }

  protected void handleDoubleClickEvent(int selectedRow) {
    if (selectedRow >= 0) {
      XhiveExtendedTreeNode treeNode = (XhiveExtendedTreeNode) getChildAt(selectedRow);
      treeNode.performDoubleClick();
    }
  }

  protected JPopupMenu getPopupMenu() {
    XhiveExtendedTreeNode treeNode = getChildAt(getTable().getSelectedRow());
    return treeNode.getPopupMenu(getTable().getActionMap());
  }

  private void setNode(XhiveTableModelTreeNode treeNode) {
    getTable().setModel(treeNode);
    if (getTable().getColumnModel().getColumnCount() > 0) {
      getTable().getColumnModel().getColumn(0).setCellRenderer(new XhiveTableCellRenderer());
    }
    resizeTableColumnWidth();
  }

  protected Object createContent(XhiveSessionIf session) {
    return getSelectedNode().getChildrenToAdd(session);
  }

  protected void createContentFinished(Object result) {
    getSelectedNode().addChildren((ArrayList) result);
    setNode((XhiveTableModelTreeNode) getSelectedNode());
  }
}

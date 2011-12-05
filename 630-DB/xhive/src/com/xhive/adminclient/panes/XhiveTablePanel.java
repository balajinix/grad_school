package com.xhive.adminclient.panes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveMouseListener;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.treenodes.XhiveExtendedTreeNode;

abstract class XhiveTablePanel extends XhiveExplorerRightPanel {

  private JTable table;

  protected XhiveAction deleteAction;

  // This does not need to be a transacted action since doDelete handles its own transaction
  // Placed in a method since buildPanel is called from the constructor of a superclass
  private XhiveAction initDeleteAction() {
    Icon icon;
    if (this instanceof XhiveContentsListPanel) {
      icon = XhiveResourceFactory.getImageIcon(XhiveResourceFactory.DELETE_ICON);
    } else {
      // No icon in indexlist panel
      icon = null;
    }
    deleteAction = new XhiveAction("Delete", 'd', icon) {
      @Override
      protected void xhiveActionPerformed(ActionEvent e) {
        handleDeleteEvent(e);
      }
    };
    return deleteAction;
  }


  protected XhiveTablePanel(JTabbedPane parent, XhiveExtendedTreeNode node) {
    super(parent, node);
  }

  @Override
  protected void buildPanel() {
    table = buildTable();
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.white);
    add(scrollPane, BorderLayout.CENTER);
  }

  protected JTable buildTable() {
    JTable table1 = new JTable();
    table1.setShowHorizontalLines(false);
    table1.setShowVerticalLines(false);
    table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table1.setIntercellSpacing(new Dimension(0, 0));
    table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    // Register key event handlers
    table1.getActionMap().put("Delete", initDeleteAction());
    table1.getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "Delete");

    table1.addMouseListener(new XhiveMouseListener() {
      @Override
      public void xhiveMouseDoubleClicked(MouseEvent e) {
        handleDoubleClickEvent(getTable().getSelectedRow());
      }

      @Override
      public void popupRequested(MouseEvent e) {
        int popupRow = getTable().rowAtPoint(new Point(e.getX(), e.getY()));
        int[] selectedRows = getTable().getSelectedRows();
        boolean popupRowSelected = false;
        for (int rowCount = 0; rowCount < selectedRows.length; rowCount++) {
          if (selectedRows[rowCount] == popupRow) {
            popupRowSelected = true;
          }
        }
        if (!popupRowSelected) {
          getTable().setRowSelectionInterval(popupRow, popupRow);
        }
        getPopupMenu().show(getTable(), e.getX(), e.getY());
      }
    });
    return table1;
  }

  protected abstract void handleDeleteEvent(ActionEvent e);

  protected abstract void handleDoubleClickEvent(int selectedRow);

  protected abstract JPopupMenu getPopupMenu();

  protected final JTable getTable() {
    return table;
  }

  protected void resizeTableColumnWidth() {
    JTable table1 = getTable();
    TableModel model = table1.getModel();
    TableColumnModel colModel = table1.getColumnModel();
    for (int i = 0; i < model.getColumnCount(); i++) {
      TableColumn column = colModel.getColumn(i);

      int longestCell = 0;

      for (int j = 0; j < (model.getRowCount() < 100 ? model.getRowCount() : 100); j++) {
        Object value = model.getValueAt(j, i);
        if (value == null) {
          continue;
        }

        Component cell = table1.getDefaultRenderer(model.getColumnClass(i)).getTableCellRendererComponent(table1, value, false, false, j, i);

        int width = cell.getPreferredSize().width;
        if (width > longestCell) {
          longestCell = width;
        }
      }

      TableCellRenderer cellRenderer = table1.getDefaultRenderer(column.getClass());
      Component headerComp = cellRenderer.getTableCellRendererComponent(table1, column.getHeaderValue(), false, false, 0, 0);

      int headerWidth = headerComp.getPreferredSize().width;
      column.setPreferredWidth(Math.max(headerWidth, longestCell) + 22);
    }
  }
}

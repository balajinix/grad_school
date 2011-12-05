package com.xhive.adminclient.panes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.XhiveTransactionWrapper;
import com.xhive.adminclient.dialogs.XhiveTransactedDialog;
import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.adminclient.treenodes.XhiveDocumentVersionTreeNode;
import com.xhive.adminclient.treenodes.XhiveExtendedTreeNode;
import com.xhive.adminclient.treenodes.XhiveLibraryChildTreeNode;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.versioning.interfaces.XhiveVersionIf;

public class MetadataPanel extends XhiveExplorerRightPanel implements TableModelListener {

  private static final String[] COLUMN_NAMES = new String[] { "Key", "Value" };

  private JPanel buttonPanel;
  private JTable table;
  private DefaultTableModel tableModel;
  private boolean modifyable;

  private final XhiveAction addAction = new XhiveTransactedAction("Add", 'a') {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) {
      Map<String, String> md = getMetadata(getSession());
      MetadataDialog.showAddDialog(getSession(), md);
      updateTable(md);
    }
  };

  private final XhiveAction deleteAction = new XhiveTransactedAction("Delete", 'd') {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) {
      Map<String, String> md = getMetadata(getSession());
      int[] selectedRows = table.getSelectedRows();
      for (int i = 0; i < selectedRows.length; ++i) {
        Object key = tableModel.getValueAt(selectedRows[i], 0);
        md.remove(key);
      }
      updateTable(md);
    }
  };

  public MetadataPanel(JTabbedPane parent, XhiveExtendedTreeNode node) {
    super(parent, node);
    add(buildButtonBar(), BorderLayout.NORTH);
  }

  private JPanel buildButtonBar() {
    buttonPanel = new JPanel();
    buttonPanel.setVisible(false);
    buttonPanel.add(new JButton(addAction));
    buttonPanel.add(new JButton(deleteAction));
    return buttonPanel;
  }

  @Override
  protected void buildPanel() {
    JTable table1 = buildTable();
    JScrollPane scrollPane = new JScrollPane(table1);
    scrollPane.getViewport().setBackground(Color.white);
    add(scrollPane, BorderLayout.CENTER);
  }

  private JTable buildTable() {
    table = new JTable();
    tableModel = new DefaultTableModel(new String[0][2], COLUMN_NAMES) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return modifyable && column == 1;
      }
    };
    table.setModel(tableModel);
    tableModel.addTableModelListener(this);
    return table;
  }

  public void tableChanged(TableModelEvent event) {
    if (event.getType() == TableModelEvent.UPDATE) {
      final int row = event.getFirstRow();
      if (row != TableModelEvent.HEADER_ROW) {
        final String key = (String)tableModel.getValueAt(row, 0);
        final String value = (String)tableModel.getValueAt(row, 1);
        new XhiveTransactionWrapper(null, false) {
          @Override
          protected Object transactedAction() {
            Map<String, String> md = getMetadata(getSession());
            md.put(key, value);
            return null;
          }
        }.start();
      }
    }
  }

  private Map<String, String> getMetadata(XhiveSessionIf session) {
    XhiveExtendedTreeNode selectedNode = getSelectedNode();
    XhiveLibraryChildIf lc;
    if (selectedNode instanceof XhiveDocumentVersionTreeNode) {
      XhiveVersionIf version = ((XhiveDocumentVersionTreeNode)getSelectedNode())
          .getVersion(session);
      lc = version.getAsLibraryChild();
      modifyable = false;
    } else {
      lc = ((XhiveLibraryChildTreeNode)getSelectedNode()).getLibraryChild(session);
      modifyable = lc.getXhiveVersion() == null;
    }
    return lc.getMetadata();
  }

  private static String[][] getValues(Map<String, String> md) {
    int n = md.size();
    String[][] values = new String[n][];
    Iterator<Map.Entry<String, String>> itr = md.entrySet().iterator();
    for (int i = 0; i < n; ++i) {
      Map.Entry<String, String> entry = itr.next();
      values[i] = new String[] { entry.getKey(), entry.getValue() };
    }
    Arrays.sort(values, new Comparator<String[]>() {
      public int compare(String[] o1, String[] o2) {
        return o1[0].compareTo(o2[0]);
      }
    });
    return values;
  }

  private void updateTable(String[][] values) {
    /* We're not interested in changes we make ourselves. */
    tableModel.removeTableModelListener(this);
    tableModel.setDataVector(values, COLUMN_NAMES);
    tableModel.addTableModelListener(this);
  }

  private void updateTable(Map<String, String> md) {
    updateTable(getValues(md));
  }

  @Override
  protected Object createContent(XhiveSessionIf session) {
    return getValues(getMetadata(session));
  }

  @Override
  protected void createContentFinished(Object result) {
    buttonPanel.setVisible(modifyable);
    updateTable((String[][])result);
  }

  private static class MetadataDialog extends XhiveTransactedDialog {

    private final Map<String, String> md;

    private JTextField keyField;
    private JTextField valueField;

    private MetadataDialog(XhiveSessionIf s, Map<String, String> m) {
      super("Add metadata field", s);
      md = m;
    }

    static void showAddDialog(XhiveSessionIf session, Map<String, String> md) {
      MetadataDialog dialog = new MetadataDialog(session, md);
      dialog.execute();
    }

    @Override
    protected JPanel buildFieldsPanel() {
      JPanel fieldsPanel = new JPanel(new FormLayout());
      keyField = new JTextField();
      valueField = new JTextField();
      setPreferredWidthOf(keyField, 200);
      fieldsPanel.add(new JLabel("Key:"));
      fieldsPanel.add(keyField);
      fieldsPanel.add(new JLabel("Value:"));
      fieldsPanel.add(valueField);
      return fieldsPanel;
    }

    @Override
    protected boolean performAction() {
      String key = keyField.getText();
      String value = valueField.getText();
      md.put(key, value);
      return true;
    }
  }
}

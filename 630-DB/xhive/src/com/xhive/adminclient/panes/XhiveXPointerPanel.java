package com.xhive.adminclient.panes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.adminclient.tablemodels.XhiveFunctionBindingsTableModel;
import com.xhive.adminclient.tablemodels.XhiveNamespaceDeclarationsTableModel;
import com.xhive.adminclient.tablemodels.XhiveTableModel;
import com.xhive.adminclient.treenodes.XhiveQueryResultIfRootTreeNode;
import com.xhive.adminclient.treenodes.XhiveTreeNode;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.query.interfaces.XhiveQueryResultIf;
import com.xhive.xpath.interfaces.XhiveXPathContextIf;

public class XhiveXPointerPanel extends XhiveQueryPanel {

  // Keeps track of the number of query panels opened to assign them a name
  private static int queryCount = 0;

  private JPanel namespaceTab;
  private JPanel functionBindingsTab;
  private XhiveNamespaceDeclarationsTableModel namespaceTableModel;
  private XhiveFunctionBindingsTableModel functionBindingsTableModel;

  public XhiveXPointerPanel(JTabbedPane owner, String context) {
    super(owner, context, "XPointer", queryCount++, false);
    add(buildContentPanel(), BorderLayout.CENTER);
    add(buildToolBar(), BorderLayout.WEST);
    setQueryResultToolButtonsEnabled(false);
  }

  @Override
  protected JComponent buildContentPanel() {
    JTabbedPane tabbedPane = (JTabbedPane) super.buildContentPanel();
    namespaceTableModel = new XhiveNamespaceDeclarationsTableModel();
    functionBindingsTableModel = new XhiveFunctionBindingsTableModel();
    tabbedPane.addTab("Namespace bindings",
        new BindingsPanel(namespaceTableModel, "prefix", "uri"));
    tabbedPane.addTab("Function bindings",
        new BindingsPanel(functionBindingsTableModel, "Method name", "Class name"));
    return tabbedPane;
  }

  @Override
  protected Object executeQuery(XhiveLibraryChildIf context, String query) throws Throwable {
    XhiveXPathContextIf xpathContext = context.createXPathContext();
    namespaceTableModel.addNameSpacesContexts(xpathContext);
    functionBindingsTableModel.addFunctionBindings(xpathContext);
    return context.executeXPointerQuery(query, xpathContext);
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  protected XhiveTreeNode buildResultRootNode(long timeTaken, Object result) {
    return new XhiveQueryResultIfRootTreeNode((XhiveQueryResultIf) result);
  }

  @Override
  protected String getDefaultQueryPropertyName() {
    return "com.xhive.adminclient.xpointer";
  }
}

class BindingsPanel extends JPanel {

  private JTable bindingsTable;
  private String bindingKeyName;
  private String bindingValueName;

  private XhiveAction addAction = new XhiveAction("Add") {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      performAdd();
    }
  };

  private XhiveAction deleteAction = new XhiveAction("Delete") {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      performRemove();
    }
  };

  private XhiveAction clearAction = new XhiveAction("Clear") {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      performClear();
    }
  };

  public BindingsPanel(TableModel model, String bindingKeyName, String bindingValueName) {
    super(new BorderLayout());
    this.bindingKeyName = bindingKeyName;
    this.bindingValueName = bindingValueName;

    JPanel commonButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    commonButtonPanel.add(new JButton(addAction));
    commonButtonPanel.add(new JButton(deleteAction));
    commonButtonPanel.add(new JButton(clearAction));

    bindingsTable = new JTable(model);
    JScrollPane tableScrollPane = new JScrollPane(bindingsTable);
    tableScrollPane.getViewport().setBackground(Color.white);
    add(commonButtonPanel, BorderLayout.NORTH);
    add(tableScrollPane, BorderLayout.CENTER);
  }

  private XhiveTableModel getTableModel() {
    return (XhiveTableModel) bindingsTable.getModel();
  }

  protected void performAdd() throws Exception {
    XhiveTableModel tableModel = getTableModel();
    AddBindingDialog addDialog = new AddBindingDialog("Add binding", bindingKeyName, bindingValueName);
    if (addDialog.execute() == XhiveDialog.RESULT_OK) {
      tableModel.addRow(new String[]{addDialog.keyField.getText(), addDialog.valueField.getText()});
    }
    bindingsTable.revalidate();
  }

  protected void performRemove() {
    XhiveTableModel tableModel = getTableModel();
    int i = 0;
    while (i < tableModel.getRowCount()) {
      if (bindingsTable.isRowSelected(i)) {
        tableModel.removeRow(i);
      } else {
        i++;
      }
    }
    bindingsTable.revalidate();
  }

  protected void performClear() {
    getTableModel().clear();
    bindingsTable.revalidate();
  }
}

class AddBindingDialog extends XhiveDialog {

  JTextField keyField;
  JTextField valueField;
  private String bindingKeyName;
  private String bindingValueName;

  public AddBindingDialog(String title, String bindingKeyName, String bindingValueName) {
    super(title);
    this.bindingKeyName = bindingKeyName;
    this.bindingValueName = bindingValueName;
  }

  @Override
  protected JPanel buildFieldsPanel() {
    JPanel fieldsPanel = new JPanel(new FormLayout());
    fieldsPanel.add(new JLabel(bindingKeyName));
    keyField = new JTextField();
    fieldsPanel.add(keyField);
    fieldsPanel.add(new JLabel(bindingValueName));
    valueField = new JTextField();
    fieldsPanel.add(valueField);
    return fieldsPanel;
  }
}

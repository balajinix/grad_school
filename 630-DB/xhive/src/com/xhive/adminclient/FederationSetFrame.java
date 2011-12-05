package com.xhive.adminclient;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.xhive.adminclient.dialogs.FederationSetCreationDialog;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.dialogs.XhiveMessageDialog;
import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.federationset.interfaces.XhiveFederationSetFactory;
import com.xhive.federationset.interfaces.XhiveFederationSetIf;

class FederationSetFrame extends JFrame implements TableModelListener {

    private static final Vector<String> COLUMN_NAMES = new Vector<String>(Arrays.asList(new String[] {
                "Name", "Value" }));

    private boolean useNestedSets;
    private JComboBox fileComboBox;
    private JTable mapTable;
    private DefaultTableModel tableModel;

    FederationSetFrame(Component parent) throws IOException {
        super();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(XhiveResourceFactory.getImageXSmall());
        // Now build the contents of this frame
        Container contentPane = getContentPane();
        contentPane.add(createNorthBorder(), BorderLayout.NORTH);
        contentPane.add(createSouthBorder(), BorderLayout.SOUTH);
        mapTable = createTable();
        JScrollPane scrollpane = new JScrollPane(mapTable);
        contentPane.add(scrollpane, BorderLayout.CENTER);
        refreshTable();
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private JPanel createNorthBorder() {
        JRadioButton fedButton = new JRadioButton(new XhiveAction("Federations", 'F') {
                                     @Override
                                     protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                         useNestedSets = false;
                                         refreshTable();
                                     }
                                 }
                                                 );
        fedButton.setSelected(true);
        JRadioButton setButton = new JRadioButton(new XhiveAction("Nested federation sets", 'N') {
                                     @Override
                                     protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                         useNestedSets = true;
                                         refreshTable();
                                     }
                                 }
                                                 );
        ButtonGroup group = new ButtonGroup();
        group.add(fedButton);
        group.add(setButton);
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
        buttonPanel.add(fedButton);
        buttonPanel.add(setButton);
        JPanel panel = new JPanel();
        panel.add(buttonPanel);
        panel.add(new JButton(new XhiveAction("Add", 'A') {
                                  @Override
                                  protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                      // Add a new table entry
                                      new AddDialog().execute();
                                      refreshTable();
                                  }
                              }
                             ));
        panel.add(new JButton(new XhiveAction("Remove", 'R') {
                                  @Override
                                  protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                      // Remove currently selected table entry
                                      Map<String, String> map = getCurrentMap();
                                      int[] selectedRows = mapTable.getSelectedRows();
                                      for (int i = 0; i < selectedRows.length; ++i) {
                                          Object key = tableModel.getValueAt(selectedRows[i], 0);
                                          map.remove(key);
                                      }
                                      refreshTable();
                                  }
                              }
                             ));
        return panel;
    }

    private JPanel createSouthBorder() {
        Vector<String> items = FederationSetCreationDialog.getComboBoxItems();
        fileComboBox = new JComboBox(items);
        fileComboBox.setEditable(true);
        fileComboBox.setPreferredSize(new Dimension(200, (int)fileComboBox.getPreferredSize()
                                      .getHeight()));
        fileComboBox.addActionListener(new XhiveAction() {
                                           @Override
                                           protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                               FederationSetCreationDialog.saveState(fileComboBox);
                                               refreshTable();
                                           }
                                       }
                                      );
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(fileComboBox, BorderLayout.CENTER);
        JButton browseButton = new JButton(new XhiveAction("Browse", 'B') {
                                               @Override
                                               protected void xhiveActionPerformed(ActionEvent e) throws Exception {
                                                   FederationSetCreationDialog.browse(fileComboBox, FederationSetFrame.this, false);
                                               }
                                           }
                                          );
        panel.add(browseButton, BorderLayout.EAST);
        return panel;
    }

    private JTable createTable() {
        JTable table = new JTable();
        tableModel = new DefaultTableModel(new Vector<Vector<String>>(), COLUMN_NAMES) {
                         @Override
                         public boolean isCellEditable(int row, int column) {
                             return column == 1;
                         }
                     };
        table.setModel(tableModel);
        tableModel.addTableModelListener(this);
        return table;
    }

    public void tableChanged(TableModelEvent event) {
        if (event.getType() == TableModelEvent.UPDATE) {
            int row = event.getFirstRow();
            if (row != TableModelEvent.HEADER_ROW) {
                final String name = (String)tableModel.getValueAt(row, 0);
                final String value = (String)tableModel.getValueAt(row, 1);
                try {
                    getCurrentMap().put(name, value);
                } catch (Exception e) {
                    XhiveMessageDialog.showException(e);
                }
            }
        }
    }

    private Map<String, String> getCurrentMap() throws IOException {
        String path = (String)fileComboBox.getSelectedItem();
        XhiveFederationSetIf fs = XhiveFederationSetFactory.getFederationSet(path, null);
        Map<String, String> map = useNestedSets ? fs.getFederationSetMap() : fs.getFederationMap();
        return map;
    }

    private void refreshTable() {
        Vector<Vector<String>> values = new Vector<Vector<String>>();
        try {
            Map<String, String> map = getCurrentMap();
for (Map.Entry<String, String> entry : map.entrySet()) {
                Vector<String> v = new Vector<String>(2);
                v.add(entry.getKey());
                v.add(entry.getValue());
                values.add(v);
            }
        } catch (Exception e) {
            XhiveMessageDialog.showException(e);
        }
        tableModel.setDataVector(values, COLUMN_NAMES);
    }

    private class AddDialog extends XhiveDialog {

        private JTextField nameField;
        private JTextField valueField;

        private AddDialog() {
            super(useNestedSets ? "Add nested federation set" : "Add federation");
        }

        @Override
        protected JPanel buildFieldsPanel() {
            JPanel fieldsPanel = new JPanel(new FormLayout());
            nameField = new JTextField();
            valueField = new JTextField();
            setPreferredWidthOf(nameField, 200);
            fieldsPanel.add(new JLabel("Name:"));
            fieldsPanel.add(nameField);
            fieldsPanel.add(new JLabel("Value:"));
            fieldsPanel.add(valueField);
            return fieldsPanel;
        }

        @Override
        protected boolean performAction() throws IOException {
            String name = nameField.getText();
            String value = valueField.getText();
            Map<String, String> map = getCurrentMap();
            map.put(name, value);
            return true;
        }
    }
}

package com.xhive.adminclient;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveDriverIf;

final class PerformanceStatisticsFrame extends JFrame {

    private static final String[] columnNames = new String[] { "Name", "Value" };
    private static final Class<?>[] columnClasses = new Class<?>[] { String.class, Number.class };

    private final XhiveDriverIf driver;
    private final WindowListener listener = new WindowAdapter() {
                                                @Override
                                                public void windowClosed(WindowEvent e) {
                                                    stopAutoRefresh();
                                                    DriverRegistry.unregisterDriverUser(driver, this);
                                                }
                                            };
    private final Action refreshAction = new XhiveAction("Refresh") {
                                             @Override
                                             protected void xhiveActionPerformed(ActionEvent e) {
                                                 refreshTableContents();
                                             }
                                         };
    private final Action closeAction = new XhiveAction("Close", 'C') {
                                           @Override
                                           protected void xhiveActionPerformed(ActionEvent e) {
                                               dispose();
                                           }
                                       };
    private final SpinnerNumberModel spinnerModel =
        new SpinnerNumberModel(0, 0, Integer.MAX_VALUE / 1000, 1);
    private String[] names;
    private Number[] values;
    private final AbstractTableModel tableModel = new AbstractTableModel() {
                public int getRowCount() {
                    return names.length;
                }
                public int getColumnCount() {
                    return columnNames.length;
                }
                @Override
                public String getColumnName(int column) {
                    return columnNames[column];
                }
                @Override
                public Class<?> getColumnClass(int column) {
                    return columnClasses[column];
                }
                public Object getValueAt(int row, int column) {
                    switch (column) {
                    case 0:
                            return names[row];
                    case 1:
                        return values[row];
                    default:
                        return null;
                    }
                }
            };
    private Timer refreshTimer;

    PerformanceStatisticsFrame(XhiveDriverIf driver) {
        super("Performance statistics - " + driver.getFederationBootFileName());
        this.driver = driver;
        DriverRegistry.registerDriverUser(driver, this);
        addWindowListener(listener);
    }

    void showPerformanceStatistics(Component parent) {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // Escape key closes window
        JComponent contentPane = (JComponent)getContentPane();
        Object key = closeAction.getValue(Action.NAME);
        InputMap inputMap = contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), key);
        ActionMap actionMap = contentPane.getActionMap();
        actionMap.put(key, closeAction);
        // Now build the contents of this frame
        setIconImage(XhiveResourceFactory.getImageXSmall());
        JTable table = createTable();
        JScrollPane scrollpane = new JScrollPane(table);
        contentPane.add(scrollpane, BorderLayout.CENTER);
        contentPane.add(createButtonbar(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private JTable createTable() {
        refreshTableContents();
        JTable table = new JTable(tableModel);
        table.setFocusable(false);
        return table;
    }

    private JPanel createButtonbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel autolabel = new JLabel("Auto refresh (seconds): ");
        panel.add(autolabel);
        JSpinner autospinner = new JSpinner(spinnerModel);
        // Make the number field a little smaller
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)autospinner.getEditor();
        JFormattedTextField autotext = editor.getTextField();
        autotext.setColumns(5);
        autospinner.addChangeListener(new ChangeListener() {
                                          public void stateChanged(ChangeEvent event) {
                                              int value = spinnerModel.getNumber().intValue();
                                              setAutoRefreshTime(value);
                                          }
                                      }
                                     );
        panel.add(autospinner);
        JButton refreshbutton = new JButton(refreshAction);
        panel.add(refreshbutton);
        getRootPane().setDefaultButton(refreshbutton);
        JButton resetbutton = new JButton(new XhiveAction("Reset", 'R') {
                                              @Override
                                              protected void xhiveActionPerformed(ActionEvent e) {
                                                  driver.getStatistics().clear();
                                                  refreshTableContents();
                                              }
                                          }
                                         );
        panel.add(resetbutton);
        JButton closebutton = new JButton(closeAction);
        panel.add(closebutton);
        return panel;
    }

    private void refreshTableContents() {
        try {
            Map stats = driver.getStatistics();
            int n = stats.size();
            names = new String[n];
            values = new Number[n];
            Iterator itr = stats.entrySet().iterator();
            for (int i = 0; i < n; ++i) {
                Map.Entry entry = (Map.Entry)itr.next();
                names[i] = (String)entry.getKey();
                values[i] = (Number)entry.getValue();
            }
            tableModel.fireTableDataChanged();
        } catch (RuntimeException e) {
            // Setting the value to 0 will generate an event that stops the timer
            spinnerModel.setValue(new Integer(0));
            throw e;
        }
    }

    private void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
            refreshTimer = null;
        }
    }

    private void setAutoRefreshTime(int seconds) {
        stopAutoRefresh();
        if (seconds != 0) {
            refreshTimer = new Timer(seconds * 1000, refreshAction);
            refreshTimer.start();
        }
    }

    void disposeFrameOnExit() {
        // Remove listener and call it explicitly. Otherwise the event will just be queued and not
        // called quickly enough.
        removeWindowListener(listener);
        dispose();
        listener.windowClosed(null);
    }
}

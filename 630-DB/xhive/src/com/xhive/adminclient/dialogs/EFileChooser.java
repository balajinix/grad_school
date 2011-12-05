package com.xhive.adminclient.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import com.xhive.adminclient.AdminProperties;

/**
 * A wrapper for JFileChooser with accessories for
 * directory history and file preview.
 * (E)nhancedFilechooser.
 *
 * @author	Klaus Berg
 */

public class EFileChooser extends JFileChooser {

    private static Vector<String> comboModel;
    private static FileOutputStream fos;
    private static ObjectOutputStream oos;
    private static FileInputStream fis;
    private static ObjectInputStream ois;
    private static String dirFile;

    private TextPreviewer previewer;
    private PreviewAndHistoryPanel previewAndHistoryPanel;
    private MyComboBox combo;
    private PreviewAndHistoryPanel.ComboItemListener comboItemListener;

    private final class PreviewAndHistoryPanel extends JPanel {

        public PreviewAndHistoryPanel() {
            setPreferredSize(new Dimension(250, 250));
            setBorder(BorderFactory.createEtchedBorder());
            setLayout(new BorderLayout());

            combo = new MyComboBox(comboModel);
            comboItemListener = new ComboItemListener();
            combo.addItemListener(comboItemListener);
            combo.registerKeyboardAction(new DeleteKeyListener("ONE"),
                                         KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false),
                                         JComponent.WHEN_IN_FOCUSED_WINDOW);
            combo.registerKeyboardAction(new DeleteKeyListener("ALL"),
                                         KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,
                                                                InputEvent.SHIFT_MASK, false),
                                         JComponent.WHEN_IN_FOCUSED_WINDOW);
            combo.setRenderer(new ComboBoxRendererWithTooltips());
            add(combo, BorderLayout.NORTH);

            previewer = new TextPreviewer();
            add(previewer, BorderLayout.CENTER);
        }

        private final class DeleteKeyListener implements ActionListener {
            String action_;

            DeleteKeyListener(String action) {
                action_ = action;
            }

            public void actionPerformed(ActionEvent e) {
                if (action_.equals("ONE")) {
                    combo.removeItemAt(combo.getSelectedIndex());
                } else {
                    combo.removeAllItems();
                }
            }
        }

        /**
         * We use an ItemListener imstead of an ActionListener because an
         * action event is also generated if the DEL or SHIFT+DEL button
         * is pressed in order to delete an item resp. all items.
         */
        private final class ComboItemListener implements ItemListener {
            String dir;

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    selectNewDirectory();
                }
            }

            private void selectNewDirectory() {
                dir = (String) combo.getSelectedItem();
                setCurrentDirectory(new File(dir));
                JLabel label = new JLabel(dir);
                label.setFont(combo.getFont());
                if (label.getPreferredSize().width > combo.getSize().width) {
                    combo.setToolTipText(dir);
                } else {
                    combo.setToolTipText(null);
                }
            }
        }

        /**
         * Display a tooltip for the cell if needed.
         *
         * Note :
         * When JComboBox is located near the border of a Frame, the tooltip
         * doesn't display outside the frame due to current Swing limitations.
         */
        class ComboBoxRendererWithTooltips extends BasicComboBoxRenderer {

            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                setFont(list.getFont());
                setText((value == null) ? "" : value.toString());
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                    if (index >= 0) {
                        if (this.getPreferredSize().width > combo.getSize().width) {
                            list.setToolTipText(comboModel.elementAt(index));
                        } else {
                            list.setToolTipText(null);
                        }
                    }
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                return this;
            }
        }

    }

    class TextPreviewer extends JComponent {

        private JTextArea textArea = new JTextArea();
        private JScrollPane scroller = new JScrollPane(textArea);
        private char[] buff = new char[1000];
        private Color bg;

        public TextPreviewer() {

            try {
                textArea.setEditable(false);
                if ((bg = UIManager.getColor("TextArea.background")) != null)
                    textArea.setBackground(bg);
                else
                    textArea.setBackground(Color.white);
                setBorder(BorderFactory.createEtchedBorder());
                setLayout(new BorderLayout());
                add(scroller, BorderLayout.CENTER);
            } catch (NullPointerException np) {
                // layout can throw exceptions sometimes: ignore
            }

        }

        public void initTextArea(File file) {
            textArea.setText(contentsOfFile(file));
            textArea.setCaretPosition(0);
        }

        public void clear() {
            textArea.setText("");
        }

        private String contentsOfFile(File file) {
            if (file == null) {
                return "";
            }
            if (file.getName().equals("")) {
                return "";
            }
            String s = new String();
            FileReader reader = null;
            try {
                reader = new FileReader(file);
                int nch = reader.read(buff, 0, buff.length);
                if (nch != -1) {
                    s = new String(buff, 0, nch);
                }
            } catch (IOException iox) {
                s = "";
            }
            try {
                reader.close();
            } catch (Exception ex) {
                // ignore
            }

            return s;
        }
    }

    public EFileChooser(String title) {
        comboModel = new Vector<String>();
        if (AdminProperties.getProperty("com.xhive.adminclient.dir1") != null) {
            comboModel.add(AdminProperties.getProperty("com.xhive.adminclient.dir1"));
        }
        if (AdminProperties.getProperty("com.xhive.adminclient.dir2") != null) {
            comboModel.add(AdminProperties.getProperty("com.xhive.adminclient.dir2"));
        }
        if (AdminProperties.getProperty("com.xhive.adminclient.dir3") != null) {
            comboModel.add(AdminProperties.getProperty("com.xhive.adminclient.dir3"));
        }
        if (AdminProperties.getProperty("com.xhive.adminclient.dir4") != null) {
            comboModel.add(AdminProperties.getProperty("com.xhive.adminclient.dir4"));
        }
        if (AdminProperties.getProperty("com.xhive.adminclient.dir5") != null) {
            comboModel.add(AdminProperties.getProperty("com.xhive.adminclient.dir5"));
        }
        if (AdminProperties.getProperty("com.xhive.adminclient.dir6") != null) {
            comboModel.add(AdminProperties.getProperty("com.xhive.adminclient.dir6"));
        }
        if (AdminProperties.getProperty("com.xhive.adminclient.dir7") != null) {
            comboModel.add(AdminProperties.getProperty("com.xhive.adminclient.dir7"));
        }
        if (AdminProperties.getProperty("com.xhive.adminclient.dir8") != null) {
            comboModel.add(AdminProperties.getProperty("com.xhive.adminclient.dir8"));
        }
        setDialogTitle(title);
        setMultiSelectionEnabled(false);
        setPreferredSize(new Dimension(500, 350));
        previewAndHistoryPanel = new PreviewAndHistoryPanel();
        JPanel choicePanel = new JPanel(new BorderLayout());
        choicePanel.add(previewAndHistoryPanel, BorderLayout.CENTER);
        setAccessory(choicePanel);
        addPropertyChangeListener(new PropertyChangeListener() {
                                      public void propertyChange(PropertyChangeEvent e) {
                                          if (e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                                              previewer.clear();
                                              File dir = (File) e.getNewValue();
                                              if (dir == null || dir.getName().equals("")) {
                                                  return;
                                              }
                                              String pathname = dir.getAbsolutePath();
                                              int i;
                                              boolean found = false;
                                              for (i = 0; i < comboModel.size(); i++) {
                                                  String dirname = comboModel.elementAt(i);
                                                  if (dirname.equals(pathname)) {
                                                      found = true;
                                                      break;
                                                  }
                                              }
                                              if (found) {
                                                  combo.setSelectedIndex(i);
                                              }
                                          }
                                          if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                                              File f = (File) e.getNewValue();
                                              showFileContents(f);
                                              insertDirectory(f);
                                          }
                                      }
                                  }
                                 );
    }

    protected void showFileContents(File f) {
        previewer.initTextArea(f);
    }

    private void saveDirectoryEntries() {
        if (combo.getItemCount() > 0) {
            AdminProperties.setProperty("com.xhive.adminclient.dir1", (String) combo.getItemAt(0));
        }
        if (combo.getItemCount() > 1) {
            AdminProperties.setProperty("com.xhive.adminclient.dir2", (String) combo.getItemAt(1));
        }
        if (combo.getItemCount() > 2) {
            AdminProperties.setProperty("com.xhive.adminclient.dir3", (String) combo.getItemAt(2));
        }
        if (combo.getItemCount() > 3) {
            AdminProperties.setProperty("com.xhive.adminclient.dir4", (String) combo.getItemAt(3));
        }
        if (combo.getItemCount() > 4) {
            AdminProperties.setProperty("com.xhive.adminclient.dir5", (String) combo.getItemAt(4));
        }
        if (combo.getItemCount() > 5) {
            AdminProperties.setProperty("com.xhive.adminclient.dir6", (String) combo.getItemAt(5));
        }
        if (combo.getItemCount() > 6) {
            AdminProperties.setProperty("com.xhive.adminclient.dir7", (String) combo.getItemAt(6));
        }
        if (combo.getItemCount() > 7) {
            AdminProperties.setProperty("com.xhive.adminclient.dir8", (String) combo.getItemAt(7));
        }
    }

    private void insertDirectory(File file) {
        if (file == null || file.getName().equals("")) {
            return;
        }
        String pathname = file.getAbsolutePath();
        int pos = pathname.lastIndexOf(System.getProperty("file.separator"));
        String dir = pathname.substring(0, pos);
        if (comboModel.contains(dir)) {
            return;
        } else {
            comboModel.addElement(dir);
            Collections.sort(comboModel);
            combo.revalidate();
            combo.setSelectedItem(dir);
        }
    }

    /**
     * This inner class is used to set the UI to MyComboBoxUI
     * Unlike JButton, JComboBox has no public setUI() method,
     * so we have to go this way using the protected setUI()
     * method of JComponent.
     */
    class MyComboBox extends JComboBox {

        public MyComboBox(Vector items) {
            super(items);
            setUI(new MyComboBoxUI());
        }
    }

    /**
     * Modified UI for JComboBox.
     */
    class MyComboBoxUI extends BasicComboBoxUI {

        private MyComboBoxUI() {
            super();
        }

        /**
         * Creates an implementation of the ComboPopup interface.
         * Returns an instance of MyComboPopup.
         */
        @Override
        protected ComboPopup createPopup() {
            return new MyComboPopup(comboBox);
        }

        class MyComboPopup extends BasicComboPopup {

            protected JList list_;
            protected JComboBox comboBox_;
            protected boolean hasEntered_;

            MyComboPopup(JComboBox combo) {
                super(combo);
                list_ = list;
                comboBox_ = comboBox;
                hasEntered_ = hasEntered;
            }

            /**
             * Creates the mouse listener that is returned by ComboPopup.getMouseListener().
             * Returns an instance of MyComboPopup$MyInvocationMouseHandler.
             */
            @Override
            protected MouseListener createMouseListener() {
                return new MyInvocationMouseHandler();
            }

            @Override
            protected MouseListener createListMouseListener() {
                return new MyListMouseHandler();
            }

            @Override
            protected MouseEvent convertMouseEvent(MouseEvent e) {
                return super.convertMouseEvent(e);
            }

            @Override
            protected void updateListBoxSelectionForEvent(MouseEvent anEvent, boolean shouldScroll) {
                super.updateListBoxSelectionForEvent(anEvent, shouldScroll);
            }

            @Override
            protected void stopAutoScrolling() {
                super.stopAutoScrolling();
            }

            @Override
            protected void delegateFocus(MouseEvent e) {
                super.delegateFocus(e);
            }

            @Override
            protected void togglePopup() {
                super.togglePopup();
            }

            @Override
            public void hide() {
                super.hide();
            }

            class MyInvocationMouseHandler extends MouseAdapter {

                @Override
                public void mousePressed(MouseEvent e) {

                    if (!SwingUtilities.isLeftMouseButton(e))
                        return;

                    if (!comboBox_.isEnabled())
                        return;

                    delegateFocus(e);
                    togglePopup();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    int oldSelectedIndex = comboBox_.getSelectedIndex();
                    Component source = (Component) e.getSource();
                    Dimension size = source.getSize();
                    Rectangle bounds = new Rectangle(0, 0, size.width - 1, size.height - 1);
                    if (!bounds.contains(e.getPoint())) {
                        MouseEvent newEvent = convertMouseEvent(e);
                        Point location = newEvent.getPoint();
                        Rectangle r = new Rectangle();
                        list_.computeVisibleRect(r);
                        if (r.contains(location)) {
                            updateListBoxSelectionForEvent(newEvent, false);
                            int index = list_.getSelectedIndex();
                            comboBox_.setSelectedIndex(index);
                            if (index == oldSelectedIndex) {
                                comboItemListener.selectNewDirectory();
                            }
                        }
                        hide();
                    }
                    hasEntered_ = false;
                    stopAutoScrolling();
                }

            }

            /**
             * This listener hides the popup when the mouse is released in the list.
             */
            protected class MyListMouseHandler extends MouseAdapter {

                @Override
                public void mouseReleased(MouseEvent e) {
                    comboItemListener.selectNewDirectory();
                    comboBox_.setSelectedIndex(list_.getSelectedIndex());
                    hide();
                }
            }
        }
    }

    @Override
    public void approveSelection() {
        super.approveSelection();
        saveDirectoryEntries();
    }

    @Override
    public void cancelSelection() {
        super.cancelSelection();
        saveDirectoryEntries();
    }
}


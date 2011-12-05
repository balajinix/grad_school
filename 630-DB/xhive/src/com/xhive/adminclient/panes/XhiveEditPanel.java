package com.xhive.adminclient.panes;

import com.xhive.adminclient.XhiveTextArea;
import com.xhive.adminclient.XhiveTextSearchPanel;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.border.Border;
import java.awt.*;

public class XhiveEditPanel extends JPanel implements CaretListener {

  private XhiveTextArea textArea;
  private JLabel cursorPositionLabel;
  private JLabel statusLabel;

  public XhiveEditPanel(short highlightType, boolean showTextSearch) {
    super(new BorderLayout());

    JPanel statusBar = buildStatusBar();

    JScrollPane scrollPane = new JScrollPane(buildTextArea(highlightType));
    scrollPane.setPreferredSize(new Dimension(600, 400));
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    if (showTextSearch) {
      add(XhiveTextSearchPanel.createTextSearchPanel(textArea), BorderLayout.NORTH);
    }
    add(scrollPane, BorderLayout.CENTER);
    add(statusBar, BorderLayout.SOUTH);
  }

  private XhiveTextArea buildTextArea(short highlightType) {
    textArea = new XhiveTextArea(highlightType);
//    textArea.setLineWrap(true);
//    textArea.setWrapStyleWord(true);
    textArea.addCaretListener(this);
    textArea.moveCaretPosition(0);
    return textArea;
  }

  private JPanel buildStatusBar() {
    JPanel statusBar = new JPanel();
    statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
    statusBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

    Border border = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray),
        BorderFactory.createEmptyBorder(0, 5, 0, 5));

    statusLabel = new JLabel("");
    statusLabel.setBorder(border);
    statusLabel.setMaximumSize(new Dimension(2000, 20));

    cursorPositionLabel = new JLabel("");
    cursorPositionLabel.setBorder(border);
    cursorPositionLabel.setMaximumSize(new Dimension(150, 20));

    statusBar.add(statusLabel);
    statusBar.add(Box.createHorizontalStrut(2));
    statusBar.add(cursorPositionLabel);

    return statusBar;
  }

  /**
   * Shows position in text area
   */
  public void caretUpdate(CaretEvent e) {
/*
    try {
      int row = textArea.getLineOfOffset(e.getDot());
      int col = e.getDot() - textArea.getLineStartOffset(row);

      String position = String.valueOf(row + 1) + ":" + String.valueOf(col + 1);
      cursorPositionLabel.setText(position);
    } catch (BadLocationException e1) {
    }
*/
  }

  public XhiveTextArea getTextArea() {
    return textArea;
  }

  public JLabel getStatusLabel() {
    return statusLabel;
  }
}

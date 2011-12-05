package com.xhive.adminclient.panes;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.resources.XhiveResourceFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class XhiveResultPanel extends JPanel {

  private XhiveAction runAction = new XhiveAction("Run",
      XhiveResourceFactory.getImageIcon(XhiveResourceFactory.RERUN_ICON),
      "Run", 'R') {
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      runAction();
    }
  };

  private XhiveAction closeAction = new XhiveAction("Close",
      XhiveResourceFactory.getImageIcon(XhiveResourceFactory.DELETE_ICON),
      "Close", 'C') {
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      closeAction();
    }
  };

  public XhiveResultPanel() {
    super(new BorderLayout());
  }

  public void close() {
    // Remove myself from mainframe
    AdminMainFrame mainFrame = AdminMainFrame.getInstance();
    mainFrame.removeResultTab(XhiveResultPanel.this);
  }

  protected JToolBar buildLeftToolBar() {
    return null;
  }

  protected JToolBar buildRightToolBar() {
    JToolBar toolBar = new JToolBar();
    toolBar.setOrientation(JToolBar.VERTICAL);
    //toolBar2.setRollover(true);
    toolBar.setFloatable(false);
    toolBar.add(closeAction);
    toolBar.add(runAction);
    return toolBar;
  }

  protected JComponent buildToolBar() {
    JToolBar leftBar = buildLeftToolBar();
    if (leftBar != null) {
      JPanel panel = new JPanel(new GridLayout(0, 2));
      panel.add(leftBar);
      panel.add(buildRightToolBar());
      return panel;
    } else {
      return buildRightToolBar();
    }
  }

  protected void setToolButtonsEnabled(boolean value) {
    closeAction.setEnabled(value);
    runAction.setEnabled(value);
  }

  protected abstract JComponent buildContentPanel();

  protected abstract void runAction();

  protected void closeAction() {
    close();
  }

  public abstract Icon getIcon();
}

package com.xhive.adminclient.panes;

import com.xhive.adminclient.XhiveTransactedSwingWorker;
import com.xhive.adminclient.treenodes.XhiveExtendedTreeNode;
import com.xhive.core.interfaces.XhiveSessionIf;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Base class for panels that should show up at the right side of the database explorer.
 * For each node selection change at the left new panels will be created. Panel content is only created
 * once at the first tab state changed event.
 */
public abstract class XhiveExplorerRightPanel extends JPanel implements ChangeListener {

  private boolean contentCreated = false;
  private JTabbedPane parent;
  private XhiveExtendedTreeNode node;

  protected XhiveExplorerRightPanel(JTabbedPane parent, XhiveExtendedTreeNode node) {
    super(new BorderLayout());
    this.node = node;
    this.parent = parent;
    buildPanel();
    parent.addChangeListener(this);
  }

  public void fireUpdateEvent() {
    contentCreated = false;
    stateChanged(null);
  }

  public void stateChanged(ChangeEvent e) {
    if (parent.getSelectedComponent() == this && !contentCreated) {
      contentCreated = true;
      XhiveTransactedSwingWorker worker = new XhiveTransactedSwingWorker(true) {
        protected Object xhiveConstruct() throws Exception {
          return createContent(getSession());
        }

        protected void xhiveFinished(Object result) {
          createContentFinished(result);
        }
      };
      worker.start();
    }
  }

  protected XhiveExtendedTreeNode getSelectedNode() {
    return node;
  }

  protected abstract void buildPanel();

  protected abstract Object createContent(XhiveSessionIf session) throws Exception;

  protected abstract void createContentFinished(Object result);
}

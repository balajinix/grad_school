package com.xhive.adminclient.panes;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.ObjectFinder;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTextArea;
import com.xhive.adminclient.XhiveTextSearchPanel;
import com.xhive.adminclient.XhiveTransactedSwingWorker;
import com.xhive.adminclient.XhiveTransactionWrapper;
import com.xhive.adminclient.dialogs.EFileChooser;
import com.xhive.adminclient.dialogs.XhiveFilters;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.treenodes.XhiveNodeTreeNode;
import com.xhive.adminclient.treenodes.XhiveQueryResultTreeNode;
import com.xhive.adminclient.treenodes.XhiveTreeNode;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;

import org.w3c.dom.Node;

public abstract class XhiveQueryPanel extends XhiveResultPanel {

  private final XhiveSessionIf session;
  private final String context;
  private final JTabbedPane owner;
  private File currentFile;

  protected JTabbedPane resultTabbedPane;
  private JScrollPane resultTreeScrollPane;
  private XhiveEditPanel queryEditPanel;
  protected XhiveDatabaseTree resultTree;

  private XhiveAction openQueryAction = new XhiveAction("Open",
      XhiveResourceFactory.getImageIcon(XhiveResourceFactory.OPEN_FILE_ICON),
      "Open query", 'O') {

    private String readQuery(File file) throws Exception {
      StringBuffer sb = new StringBuffer();
      LineNumberReader lir = new LineNumberReader(new FileReader(file));
      String line = lir.readLine();
      while (line != null) {
        if (sb.length() == 0) {
          sb.append(line);
        } else {
          sb.append("\n" + line);
        }
        line = lir.readLine();
      }
      return sb.toString();
    }

    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      EFileChooser fileChooser = getFileChooser("Open XQuery");
      if (fileChooser.showDialog(AdminMainFrame.getInstance(), "Open query") == EFileChooser.APPROVE_OPTION) {
        currentFile = fileChooser.getSelectedFile();
        getQueryEditPanel().getTextArea().setText(readQuery(currentFile));
        // When a quer is opened then this is the selected tab
        owner.setTitleAt(owner.getSelectedIndex(), currentFile.getName());
        saveQueryAction.setEnabled(true);
      }
      AdminProperties.setProperty(AdminProperties.XQUERY_DIR, fileChooser.getCurrentDirectory());
    }
  };

  private XhiveAction saveQueryAction = new XhiveAction("Save",
      XhiveResourceFactory.getImageIcon(XhiveResourceFactory.CLOSE_FILE_ICON),
      "Save query", 's') {

    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      if (currentFile != null) {
        saveQuery(currentFile, getQuery());
      }
    }
  };

  private XhiveAction saveQueryAsAction = new XhiveAction("Save as",
      XhiveResourceFactory.getImageIcon(XhiveResourceFactory.CLOSE_FILE_ICON),
      "Save query as", 'a') {

    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      // Only save if a name was chosen
      EFileChooser fileChooser = getFileChooser("Save XQuery");
      if (fileChooser.showDialog(AdminMainFrame.getInstance(), "Save query") == EFileChooser.APPROVE_OPTION) {
        currentFile = fileChooser.getSelectedFile();
        saveQuery(currentFile, getQuery());
        saveQueryAction.setEnabled(true);
      }
      AdminProperties.setProperty(AdminProperties.XQUERY_DIR, fileChooser.getCurrentDirectory());
    }
  };

  private XhiveAction copyAction = new XhiveAction("",
      XhiveResourceFactory.getImageIcon(XhiveResourceFactory.COPY_ICON),
      "Copy results to clipboard", 0) {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      serializeResultToClipBoard();
    }
  };

  // TODO (ADQ) : Might want to do this in a thread?
  private XhiveAction expandAllAction = new XhiveAction("",
      XhiveResourceFactory.getImageIcon(XhiveResourceFactory.EXPAND_ALL_ICON),
      "Expand result one level", 0) {

    private void expandNode(XhiveDatabaseTree resultTree1, XhiveTreeNode node,
        ArrayList<XhiveTreeNode> expandedNodes) {
      for (int i = 0; i < node.getChildCount(); i++) {
        XhiveTreeNode child = (XhiveTreeNode)node.getChildAt(i);
        if (child.notYetOpenedReadOnly()) {
          child.expand(getSession());
          expandedNodes.add(child);
        } else {
          // Check for not yet opened nodes below
          if (child.getChildCount() > 0) {
            // IntelliJ fun: remove expandedNodes in arguments below to get infinite validation loop
            expandNode(resultTree1, child, expandedNodes);
          }
        }
      }
    }

    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      ArrayList<XhiveTreeNode> expandedNodes = new ArrayList<XhiveTreeNode>();
      session.join();
      try {
        XhiveDatabaseTree resultTree1 = getResultTree();
        if ((resultTree1 != null)) {
          expandNode(resultTree1, resultTree1.getRoot(), expandedNodes);
        }
      } finally {
        session.leave();
      }
      // Only after session is left above, can we savely expand these in the GUI
      for (int i = 0; i < expandedNodes.size(); i++) {
        XhiveTreeNode child = expandedNodes.get(i);
        resultTree.fireTreeExpanded(new TreePath(child.getPath()));
      }
    }
  };

  // TODO (ADQ) : Might want to do this in a thread?
  private XhiveAction collapseAllAction = new XhiveAction("",
      XhiveResourceFactory.getImageIcon(XhiveResourceFactory.COLLAPSE_ALL_ICON),
      "Collapse all nodes", 0) {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      // TODO (ADQ) : This does not yet work
      XhiveDatabaseTree resultTree1 = getResultTree();
      if ((resultTree1 != null)) {
        int count = resultTree1.getRoot().getChildCount();
        for (int i = 0; i < count; i++) {
          XhiveTreeNode node = (XhiveTreeNode) resultTree1.getRoot().getChildAt(i);
          if (!node.isLeaf()) {
            node.collapse();
            resultTree1.fireTreeCollapsed(new TreePath(node.getPath()));
          }
        }
      }
    }
  };

  private XhiveAction cancelAction = new XhiveAction(null,
      XhiveResourceFactory.getImageIcon(XhiveResourceFactory.STOP17_ICON),
      "Cancel query", 'q') {
      @Override
      protected void xhiveActionPerformed(ActionEvent e) throws Exception {
        setEnabled(false);
        getSession().interrupt();
      }
    };
  
  private static EFileChooser getFileChooser(String title) {
    EFileChooser fileChooser = new EFileChooser(title);
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileSelectionMode(EFileChooser.FILES_ONLY);
    fileChooser.addChoosableFileFilter(XhiveFilters.getXQueryFileFilter());
    fileChooser.setCurrentDirectory(AdminProperties.getFile(AdminProperties.XQUERY_DIR));
    return fileChooser;
  }

  private static void saveQuery(File file, String query) throws Exception {
    FileWriter fw = new FileWriter(file);
    fw.write(query);
    fw.close();
  }

  public XhiveQueryPanel(JTabbedPane owner, String context, String tabName, int queryCount,
      boolean allowUpdates) {
    this.owner = owner;
    this.session = AdminMainFrame.getSeperateSessionForThread(!allowUpdates);
    this.context = context;
    saveQueryAction.setEnabled(false);
    queryCount++;
    setName(tabName + String.valueOf(queryCount));
  }

  public void disableCancelButton() {
    cancelAction.setEnabled(false);
  }

  protected String getContext() {
    return context;
  }

  protected abstract String getDefaultQueryPropertyName();

  @Override
  protected JComponent buildContentPanel() {
    queryEditPanel = new XhiveEditPanel(XhiveTextArea.TYPE_XQUERY, false);
    queryEditPanel.getTextArea().setText(AdminProperties.getProperty(getDefaultQueryPropertyName()));
    resultTreeScrollPane = new JScrollPane(null);
    queryEditPanel.getStatusLabel().setText("Context: " + getContext());
    resultTabbedPane = new JTabbedPane();
    resultTabbedPane.addTab("Query", null, queryEditPanel, "Query");
    JPanel resultTreePanel = new JPanel(new BorderLayout());
    resultTreePanel.add(XhiveTextSearchPanel.createTextSearchPanel(resultTreeScrollPane, getSession()),
        BorderLayout.NORTH);
    resultTreePanel.add(resultTreeScrollPane, BorderLayout.CENTER);
    resultTabbedPane.addTab("Result tree", null, resultTreePanel, "Result as tree");
    resultTabbedPane.setEnabledAt(1, false);
    return resultTabbedPane;
  }

  @Override
  protected JToolBar buildLeftToolBar() {
    JToolBar toolBar = new JToolBar();
    toolBar.setOrientation(JToolBar.VERTICAL);
    // TODO (ADQ) : This is only available in JDK1.4
    //toolBar1.setRollover(true);
    toolBar.setFloatable(false);
    toolBar.add(openQueryAction);
    toolBar.add(saveQueryAction);
    toolBar.add(saveQueryAsAction);
    return toolBar;
  }

  @Override
  protected JToolBar buildRightToolBar() {
    JToolBar toolBar = super.buildRightToolBar();
    toolBar.add(expandAllAction);
    toolBar.add(collapseAllAction);
    toolBar.add(copyAction);
    cancelAction.setEnabled(false);
    toolBar.add(cancelAction);
    return toolBar;
  }

  protected JTabbedPane getResultTabbedPane() {
    return resultTabbedPane;
  }

  protected XhiveEditPanel getQueryEditPanel() {
    return queryEditPanel;
  }

  protected XhiveDatabaseTree getResultTree() {
    return resultTree;
  }

  protected String getQuery() {
    return getQueryEditPanel().getTextArea().getText();
  }

  /**
   * Overridden in XhiveXQueryPanel
   */
  protected boolean isResultTreeEnabled() {
    return true;
  }

  protected XhiveSessionIf getSession() {
    return session;
  }

  @Override
  public void close() {
    session.join();
    if (session.isOpen()) {
      session.commit();
    }
    session.disconnect();
    session.leave();
    AdminMainFrame.getInstance().returnSession(session);
    super.close();
  }

  protected void commitSession() {
    if (session != null) {
      session.join();
      if (session.isOpen()) {
        session.commit();
      }
      session.leave();
    }
  }

  @Override
  protected void runAction() {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    session.join();
    if (session.isOpen()) {
      // Always use a new transaction for a new query, so the result will be more up to date
      session.commit();
    }
    session.begin();
    session.leave();

    setToolButtonsEnabled(false);
    setQueryResultToolButtonsEnabled(false);
    final String query = getQuery();
    cancelAction.setEnabled(true);

    XhiveTransactedSwingWorker worker = new XhiveTransactedSwingWorker(session, true) {

      @Override
      protected Object xhiveConstruct() throws Throwable {
        queryStarted();
        resultTree = null;
        long t = System.currentTimeMillis();
        XhiveLibraryChildIf libraryChild = ObjectFinder.findLibraryChild(session, getContext());
        AdminProperties.setProperty(getDefaultQueryPropertyName(), queryEditPanel.getTextArea().getText());
        Object result = executeQuery(libraryChild, query);
        long timeTaken = System.currentTimeMillis() - t;
        resultTree = XhiveDatabaseTree.build(session, buildResultRootNode(timeTaken, result), XhiveQueryPanel.this);
        return resultTree;
      }

      @Override
      protected void xhiveFinished(final Object result) {
        // Need to join again, this is in another thread then the xhiveConstruct method (namely
        // the event thread).
        session.join();
        try {
          resultTabbedPane.setSelectedIndex(0);
          if (isResultTreeEnabled()) {
            resultTreeScrollPane.setViewportView((XhiveDatabaseTree) result);
            resultTabbedPane.setEnabledAt(1, true);
            resultTabbedPane.setSelectedIndex(1);
          } else {
            resultTabbedPane.setSelectedIndex(2);
            if (XhiveQueryPanel.this instanceof ChangeListener) {
              ((ChangeListener)XhiveQueryPanel.this).stateChanged(null);
            }
            resultTree = null;
          }
          enableButtons();
        } finally {
          session.leave();
          if (! isResultTreeEnabled()) {
            commitSession();
          }
        }
      }

        private void enableButtons() {
          cancelAction.setEnabled(false);
          setToolButtonsEnabled(true);
          setQueryResultToolButtonsEnabled(true);
          queryFinished();
          setCursor(null);      // Use parent cursor
        }
        
      @Override
      protected boolean rollbackRequired(Throwable t) {
        return XhiveQueryPanel.this.rollbackRequired(t);
      }

      @Override
      protected void handleException(Throwable t) {
        super.handleException(t);
        enableButtons();
      }
    };
    worker.start();
  }

  protected void serializeResultToClipBoard() {
    String serializedResult = getQueryResultAsText();
    Clipboard clipboard = getToolkit().getSystemClipboard();
    clipboard.setContents(new StringSelection(serializedResult), null);
  }

  private static String serializeResultTreeToString(XhiveDatabaseTree resultTree) {
    StringBuffer serializedResult = new StringBuffer();
    if ((resultTree != null)) {
      int count = resultTree.getRoot().getChildCount();
      for (int i = 0; i < count; i++) {
        XhiveTreeNode node = (XhiveTreeNode) resultTree.getRoot().getChildAt(i);
        if (node instanceof XhiveQueryResultTreeNode) {
          serializedResult.append(node.toString());
        } else if (node instanceof XhiveNodeTreeNode) {
          Node containedNode = (Node) node.getUserObject();
          if (containedNode != null) {
            serializedResult.append(containedNode.toString());
          }
        }
        serializedResult.append("\n");
      }
    }
    return serializedResult.toString();
  }

  protected String getQueryResultAsText() {
    XhiveTransactionWrapper wrapper = new XhiveTransactionWrapper(session, true) {
      @Override
      protected Object transactedAction() throws Exception {
        return serializeResultTreeToString(resultTree);
      }
    };
    return (String) wrapper.start();
  }

  @Override
  protected void setToolButtonsEnabled(boolean value) {
    super.setToolButtonsEnabled(value);
    //closeAction.setEnabled(value);
    openQueryAction.setEnabled(value);
    saveQueryAction.setEnabled(currentFile == null ? false : value);
    saveQueryAsAction.setEnabled(value);
  }

  protected void setQueryResultToolButtonsEnabled(boolean value) {
    expandAllAction.setEnabled(value);
    collapseAllAction.setEnabled(value);
    copyAction.setEnabled(value);
  }

  protected abstract Object executeQuery(XhiveLibraryChildIf context1, String query) throws Throwable;

  protected abstract XhiveTreeNode buildResultRootNode(long timeTaken, Object result);


  protected void queryStarted() {
    queryEditPanel.setEnabled(false);
    resultTabbedPane.setEnabledAt(1, false);
  }

  protected void queryFinished() {
    queryEditPanel.setEnabled(true);
  }

  protected boolean rollbackRequired(Throwable t) {
    return true;
  }
}

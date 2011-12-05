package com.xhive.adminclient.panes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.MutableTreeNode;

import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveTextArea;
import com.xhive.adminclient.dialogs.XhiveMessageDialog;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.treenodes.XhiveDocumentTreeNode;
import com.xhive.adminclient.treenodes.XhiveLibraryTreeNode;
import com.xhive.adminclient.treenodes.XhiveNodeTreeNode;
import com.xhive.adminclient.treenodes.XhiveQueryResultTreeNode;
import com.xhive.adminclient.treenodes.XhiveTreeNode;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveNodeIf;
import com.xhive.error.XhiveException;
import com.xhive.error.XhiveInterruptedException;
import com.xhive.query.interfaces.XhiveXQueryValueIf;

import org.w3c.dom.Node;

public class XhiveXQueryPanel extends XhiveQueryPanel implements ChangeListener {

  private static final String LOCAL_DEBUG_HEAD = 
  "(# xhive:index-debug stdout #)\n" +
  "(# xhive:queryplan-debug stdout #)\n" +
  "(# xhive:pathexpr-debug stdout #)\n";

  private static final char LOCAL_DEBUG_OPEN = '{';
  private static final String LOCAL_DEBUG_STRING = LOCAL_DEBUG_HEAD + LOCAL_DEBUG_OPEN;
  private static final char LOCAL_DEBUG_CLOSE = '}';
  private static final String GLOBAL_DEBUG_STRING =
  "declare option xhive:index-debug 'stdout';\n" +
  "declare option xhive:queryplan-debug 'stdout';\n" +
  "declare option xhive:pathexpr-debug 'stdout';\n";

  // Keeps track of the number of query panels opened to assign them a name
  private static int queryCount = 0;

  private final boolean allowUpdates;
  private XhiveEditPanel resultEditPanel;
  private XhiveTextArea queryDebugTextArea;
  private String resultDebugText;

  private boolean resultTreeEnabled = true;
  private JToggleButton enableTreeButton;

  private XhiveAction disableResultTreeAction = new XhiveAction(null,
      XhiveResourceFactory.getImageIcon(XhiveResourceFactory.TREEVIEW_ICON),
      "Do not use result tree (query transaction closes immediately)", 'T') {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      if (resultTabbedPane.getSelectedIndex() == 1) {
        resultTabbedPane.setSelectedIndex(2);
      }
      resultTabbedPane.setEnabledAt(1, false);
      resultTree = null;
      commitSession();

    }
  };

  @Override
  protected boolean isResultTreeEnabled() {
    return !allowUpdates && enableTreeButton.isSelected();
  }

  private XhiveAction toggleDebugOutputAction = new XhiveAction(null,
      XhiveResourceFactory.getImageIcon(XhiveResourceFactory.DEBUG_ICON),
      "Toggle debug output", 'D') {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      XhiveTextArea textArea = getQueryEditPanel().getTextArea();
      String selected = textArea.getSelectedText();
      String text = textArea.getText();
      if (selected == null || selected.length() == 0) {
        int index = text.indexOf(GLOBAL_DEBUG_STRING);
        if (index == -1) {
          textArea.setText(GLOBAL_DEBUG_STRING + textArea.getText());
        } else {
          String t1 = text.substring(0, index) + text.substring(index + GLOBAL_DEBUG_STRING.length());
          textArea.setText(t1);
        }
      } else {
        int selectStart = textArea.getSelectionStart();
        int selectEnd = textArea.getSelectionEnd();
        int index = text.lastIndexOf(LOCAL_DEBUG_STRING, selectStart);
        if (index == -1) {
          textArea.replaceSelection(LOCAL_DEBUG_STRING + selected + LOCAL_DEBUG_CLOSE);
        } else {
          if (selectStart != index + LOCAL_DEBUG_STRING.length() ||
              selectEnd >= text.length() ||
              text.charAt(selectEnd) != LOCAL_DEBUG_CLOSE
          ) {
            XhiveMessageDialog.showErrorMessage(
              "Cannot switch off debug info for selected expression"
            );
            return;
          }
          int exprStart = index + LOCAL_DEBUG_STRING.length();
          int rightIndex = exprStart;
          int balance = 0;
          while (rightIndex != selectEnd - 1) {
            if (text.charAt(rightIndex) == LOCAL_DEBUG_OPEN) {
              balance++;
            } else if (text.charAt(rightIndex) == LOCAL_DEBUG_CLOSE) {
              balance--;
            }
            rightIndex++;
          }
          if (balance != 0) {
            XhiveMessageDialog.showErrorMessage(
              "Curly braces not balanced within selected expression." +
              "Switch off debug info anyway?"
            );
          }
          String t1 = text.substring(0, index);
          String t2 = text.substring(selectStart, selectEnd);
          String t3 = text.substring(selectEnd + 1);
          textArea.setText(t1 + t2 + t3);
        }
      }
    }
  };

  public XhiveXQueryPanel(JTabbedPane owner, String libraryChildPath, boolean allowUpdates) {
    super(owner, libraryChildPath, allowUpdates ? "Update XQuery" : "XQuery", queryCount++,
        allowUpdates);
    this.allowUpdates = allowUpdates;
    add(buildContentPanel(), BorderLayout.CENTER);
    add(buildToolBar(), BorderLayout.WEST);
    setQueryResultToolButtonsEnabled(false);
  }

  @Override
  public Icon getIcon() {
    return XhiveResourceFactory.getImageIcon(allowUpdates ? XhiveResourceFactory.UPDATE_XQUERY_ICON
        : XhiveResourceFactory.SEARCH_ICON);
  }

  @Override
  protected JComponent buildContentPanel() {
    JTabbedPane tabbedPane = (JTabbedPane) super.buildContentPanel();
    resultEditPanel = new XhiveEditPanel(XhiveTextArea.TYPE_NONE, true);
    queryDebugTextArea = new XhiveTextArea(XhiveTextArea.TYPE_NONE);
    queryDebugTextArea.setEditable(false);
    queryDebugTextArea.setForeground(Color.gray);
    tabbedPane.addTab("Result text", null, resultEditPanel, "Result as text");
    tabbedPane.addTab("Query debug", XhiveResourceFactory.getImageIcon(XhiveResourceFactory.DEBUG_ICON),
        new JScrollPane(queryDebugTextArea), "Query debug output");
    tabbedPane.setEnabledAt(2, false);
    tabbedPane.setEnabledAt(3, false);
    tabbedPane.addChangeListener(this);
    return tabbedPane;
  }

  @Override
  protected JToolBar buildRightToolBar() {
    JToolBar toolBar2 = super.buildRightToolBar();
    if (!allowUpdates) {
      enableTreeButton = new JToggleButton(disableResultTreeAction);
      enableTreeButton.setSelected(true);
      toolBar2.add(enableTreeButton);
    }
    toolBar2.add(new JButton(toggleDebugOutputAction));
    return toolBar2;
  }

  @Override
  protected String getQueryResultAsText() {
    XhiveTextArea resultTextArea = resultEditPanel.getTextArea();
    if (! resultTextArea.getText().equals("")) {
      // Use whatever is in the text-area
      return resultTextArea.getText();
    } else {
      // Let the super-class handle it
      return super.getQueryResultAsText();
    }
  }

  @Override
  protected void setToolButtonsEnabled(boolean value) {
    super.setToolButtonsEnabled(value);
    disableResultTreeAction.setEnabled(value);
    toggleDebugOutputAction.setEnabled(value);
  }
  
  public void stateChanged(ChangeEvent e) {
    if (getResultTabbedPane().getSelectedComponent() == resultEditPanel) {
      if (getResultTree() != null) {
        updateResultText();
      }
    }
  }

  private void updateResultText() {
    XhiveTextArea resultTextArea = resultEditPanel.getTextArea();
    if (resultTextArea.getText().equals("")) {
      resultTextArea.setText(getQueryResultAsText());
      resultTextArea.setCaretPosition(0);
    }
  }

  @Override
  protected void runAction() {
    XhiveTextArea resultTextArea = resultEditPanel.getTextArea();
    resultTextArea.setText("");
    super.runAction();
  }

  @Override
  protected Object executeQuery(XhiveLibraryChildIf context, String query) {
    // No need to join, this is done by the transacted swing worker
    PrintStream oldSout = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      System.setOut(new PrintStream(baos));
      // First get all results, to exclude the time that is used to build the results tree
      ArrayList<XhiveXQueryValueIf> results = new ArrayList<XhiveXQueryValueIf>();
      try {
        Iterator<? extends XhiveXQueryValueIf> queryResultIterator = context.executeXQuery(query);
        while (queryResultIterator.hasNext()) {
          results.add(queryResultIterator.next());
        }
      } catch (XhiveInterruptedException xie) {
        /* Do nothing, show any results up to now normally. */
      }
      return results;
    } finally {
      System.setOut(oldSout);
      resultDebugText = baos.toString();
    }
  }

  @Override
  protected XhiveTreeNode buildResultRootNode(long timeTaken, Object result) {
    ArrayList results = (ArrayList) result;
    String title = "Query result in " + timeTaken;
    if (results.size() > 1) {
      title = title + " ms (" + results.size() + " results)";
    } else if (results.size() == 1) {
      title = title + " ms (1 result)";
    } else {
      title = title + " ms (no results)";
    }
    return new XhiveXQueryResultRootTreeNode(results, title);
  }

  @Override
  protected void queryStarted() {
    super.queryStarted();
    resultTabbedPane.setEnabledAt(2, false);
    resultTabbedPane.setEnabledAt(3, false);
  }

  @Override
  protected void queryFinished() {
    super.queryFinished();
    JTabbedPane tabbedPane = getResultTabbedPane();
    if (tabbedPane.getSelectedComponent() == resultEditPanel) {
      updateResultText();
    } else {
      resultEditPanel.getTextArea().setText("");
    }
    queryDebugTextArea.setText(resultDebugText);
    setToolButtonsEnabled(true);
    tabbedPane.setEnabledAt(2, true);
    tabbedPane.setEnabledAt(3, true);
  }


  @Override
  protected boolean rollbackRequired(Throwable t) {
    if (t instanceof XhiveException) {
      XhiveException xe = (XhiveException) t;
      // No rollback required in case of an XQuery parse error
      // TODO (ADQ) : Check which errors are ok to continue
      if (xe.getErrorCode() == XhiveException.XQUERY_PARSE_ERROR ||
          xe.getErrorCode() == XhiveException.XQUERY_UNKNOWN_FUNCTION) {
// TODO (ADQ) : Exception should contain line and column no
//        String message = xe.getMessage();
//        int lineIndex = message.indexOf("line ");
//        if (lineIndex != -1) {
//          String lineString = message.substring(lineIndex + 5);
//          lineString = lineString.substring(0, lineString.indexOf(":"));
//          int line = Integer.valueOf(lineString).intValue();
//          XhiveTextArea textArea = getQueryEditPanel().getTextArea();
//          try {
//            textArea.setCaretPosition(textArea.getLineStartOffset(line));
//          } catch (BadLocationException e) {
//          }
//        }
        return false;
      }
    }
    return true;
  }

  @Override
  protected String getDefaultQueryPropertyName() {
    return "com.xhive.adminclient.xquery";
  }

}

class XhiveXQueryResultRootTreeNode extends XhiveQueryResultTreeNode {

  private ArrayList results;

  public XhiveXQueryResultRootTreeNode(ArrayList results, String title) {
    super(null, title);
    this.results = results;
  }

  @Override
  public String getIconName() {
    return XhiveResourceFactory.FOLDER_ICON;
  }

  XhiveTreeNode createChildNode(XhiveXQueryValueIf value) {
    switch (value.getNodeType()) {
      case Node.ELEMENT_NODE:
        return new XhiveNodeTreeNode(getDatabaseTree(), value.asNode());
      case Node.DOCUMENT_NODE:
        return new XhiveDocumentTreeNode(getDatabaseTree(), (XhiveDocumentIf) value.asNode());
      case XhiveNodeIf.LIBRARY_NODE:
        return new XhiveLibraryTreeNode(getDatabaseTree(), (XhiveLibraryIf) value.asNode());
    }
    return new XhiveQueryResultTreeNode(getDatabaseTree(), value.toString());
  }

  @Override
  public void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList) {
    // Show results tree
    try {
      for (Iterator i = results.iterator(); i.hasNext();) {
        XhiveXQueryValueIf value = (XhiveXQueryValueIf) i.next();
        childList.add(createChildNode(value));
      }
    } catch (XhiveInterruptedException e) {
      /* Do nothing, show any results build up to now normally. */
    }
  }

}

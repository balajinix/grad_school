package com.xhive.adminclient.panes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.ObjectFinder;
import com.xhive.adminclient.XhiveTransactedSwingWorker;
import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveNodeIf;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.as.ASModel;
import org.w3c.dom.as.DocumentAS;

public class XhiveValidationResultPanel extends XhiveResultPanel {

  private String documentPath = null;
  private JScrollPane resultScrollPane;

  public XhiveValidationResultPanel(String title, String documentPath) {
    super();
    this.documentPath = documentPath;
    add(buildContentPanel(), BorderLayout.CENTER);
    add(buildToolBar(), BorderLayout.WEST);
    setName(title);
    runAction();
  }

  @Override
  public Icon getIcon() {
    return XhiveResourceFactory.getImageIcon(XhiveResourceFactory.VALIDATE_ICON);
  }

  @Override
  protected JComponent buildContentPanel() {
    resultScrollPane = new JScrollPane();
    return resultScrollPane;
  }

  private JTree buildResultTree() {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Results for: " + documentPath);
    JTree errorTree = new JTree(new DefaultTreeModel(rootNode));
    errorTree.setCellRenderer(new ErrorTreeCellRenderer());
    errorTree.putClientProperty("JTree.lineStyle", "Angled");
    errorTree.setLargeModel(true);
    errorTree.setExpandsSelectedPaths(true);
    errorTree.setShowsRootHandles(true);
    return errorTree;
  }

  @Override
  protected void runAction() {
    final XhiveSessionIf session = AdminMainFrame.getSeperateSessionForThread(false);
    XhiveTransactedSwingWorker worker = new XhiveTransactedSwingWorker(session, false) {
      @Override
      protected Object xhiveConstruct() throws Exception {
        ArrayList<DefaultMutableTreeNode> errorTreeNodes = new ArrayList<DefaultMutableTreeNode>();
        XhiveDocumentIf document = ObjectFinder.findDocument(session, documentPath);
        try {
          document.getConfig().setParameter("error-handler", new ErrorCollector(errorTreeNodes, session.createTemporaryDocument()));
          doRunAction(document);
        } finally {
          // Clear errorhandler
          document.getConfig().setParameter("error-handler", null);
        }
        return errorTreeNodes;
      }

      @Override
      protected void xhiveFinished(Object result) {
        JTree resultTree = buildResultTree();
        resultScrollPane.setViewportView(resultTree);
        ArrayList errorTreeNodes = (ArrayList) result;
        // Add all error tree nodes to the tree
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) resultTree.getModel().getRoot();
        if (errorTreeNodes.size() > 0) {
          for (int i = 0; i < errorTreeNodes.size(); i++) {
            root.add((MutableTreeNode) errorTreeNodes.get(i));
          }
        } else {
          root.add(new DefaultMutableTreeNode(ErrorCollector.ALL_OK));
        }
        resultTree.expandPath(new TreePath(root));
      }
    };
    worker.start();
  }

  protected void doRunAction(XhiveDocumentIf document) {
    ASModel model =  ((DocumentAS) document).getActiveASModel();
    if (model != null) {
      if (model.representsXMLSchema()) {
        // normalize with validation, but restore original setting
        Boolean validate = (Boolean)document.getDomConfig().getParameter("validate");
        document.getDomConfig().setParameter("validate", Boolean.TRUE);
        document.normalizeDocument();
        document.getDomConfig().setParameter("validate", validate);
      } else {
        document.validate();
      }
    }
  }
}

class ErrorCollector implements DOMErrorHandler {

  static final String ERROR_STRING = "ERROR";
  static final String WARNING_STRING = "WARNING";
  static final String FATAL_ERROR_STRING = "FATAL ERROR";
  static final String ALL_OK = "No error or warnings ocurred";

  private ArrayList<DefaultMutableTreeNode> errors;
  /**
   * Please note that it is not allowed to use this document
   * should the transaction no longer be open. It is used to serialize the error nodes.
   */
  private Document tempDocument;

  ErrorCollector(ArrayList<DefaultMutableTreeNode> errors, Document tempDocument) {
    this.errors = errors;
    this.tempDocument = tempDocument;
  }

  private String getSeverityString(int value) {
    switch (value) {
      case DOMError.SEVERITY_WARNING:
        return WARNING_STRING;
      case DOMError.SEVERITY_ERROR:
        return ERROR_STRING;
      case DOMError.SEVERITY_FATAL_ERROR:
        return FATAL_ERROR_STRING;
    }
    return "";
  }

  // TODO (ADQ) : Do something with location information?
  public boolean handleError(DOMError error) {
    // String value van be overwritten later
    String message = getSeverityString(error.getSeverity()) + ": " + error.getMessage();
    if ((error.getLocation() != null) && (error.getLocation().getRelatedNode() != null)) {
      Node errorNode = error.getLocation().getRelatedNode();
      if (errorNode instanceof Text) {
        errorNode = errorNode.getParentNode();
      }
      if (errorNode instanceof Attr) {
        errorNode = ((Attr) errorNode).getOwnerElement();
      }

      if (errorNode instanceof Element) {
        // Import is necessary because we don't want to add the whole subtree
        XhiveNodeIf outputElement = (XhiveNodeIf) tempDocument.importNode(errorNode, false);
        message = getSeverityString(error.getSeverity()) +  " (for node " + outputElement.toXml() + "): " + error.getMessage();
      }
    }
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(message);
    errors.add(node);
    error.getSeverity();
    Throwable t = (Throwable) error.getRelatedException();
    if (t != null) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      t.printStackTrace(new PrintStream(stream));
      DefaultMutableTreeNode exceptionNode = new DefaultMutableTreeNode("Exception");
      node.add(exceptionNode);
      exceptionNode.add(new DefaultMutableTreeNode("<html><pre> " + stream.toString() + "</pre></html>"));
    }
    return false;
  }
}

class ErrorTreeCellRenderer extends DefaultTreeCellRenderer {

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                boolean leaf, int row, boolean hasFocus1) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus1);
    String text = (String) ((DefaultMutableTreeNode) value).getUserObject();
    if (text.startsWith(ErrorCollector.WARNING_STRING)) {
      setIcon(XhiveResourceFactory.getImageIcon(XhiveResourceFactory.WARNING_ICON));
    } else if (text.startsWith(ErrorCollector.ERROR_STRING)) {
      setIcon(XhiveResourceFactory.getImageIcon(XhiveResourceFactory.ERROR2_ICON));
    } else if (text.startsWith(ErrorCollector.FATAL_ERROR_STRING)) {
      setIcon(XhiveResourceFactory.getImageIcon(XhiveResourceFactory.ERROR2_ICON));
    } else if (text.startsWith(ErrorCollector.ALL_OK)) {
      setIcon(XhiveResourceFactory.getImageIcon(XhiveResourceFactory.VALIDATE_ICON));
    } else {
      setIcon(null);
    }
    return this;
  }
}



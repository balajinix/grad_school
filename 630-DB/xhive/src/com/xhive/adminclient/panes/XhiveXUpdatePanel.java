package com.xhive.adminclient.panes;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

import com.xhive.adminclient.treenodes.XhiveDocumentRootTreeNode;
import com.xhive.adminclient.treenodes.XhiveTreeNode;
import com.xhive.dom.interfaces.XhiveDocumentIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;

import org.w3c.dom.Node;

public class XhiveXUpdatePanel extends XhiveQueryPanel {

  private static final String XUPDATE_QUERY_IMPL_CLASS = "org.xmldb.xupdate.lexus.XUpdateQueryImpl";

  // Keeps track of the number of query panels opened to assign them a name
  private static int queryCount = 0;

  public XhiveXUpdatePanel(JTabbedPane owner, String documentPath) {
    super(owner, documentPath, "XUpdate", queryCount++, true);
    add(buildToolBar(), BorderLayout.WEST);
    add(buildContentPanel(), BorderLayout.CENTER);
    setQueryResultToolButtonsEnabled(false);
  }

  @Override
  protected Object executeQuery(XhiveLibraryChildIf context, String query) throws Throwable {
    // Use reflection to execute the query, because by default the jar is not in the classpath.
    System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
        "com.xhive.xupdate.XhiveXPathQueryFactory");
    Class clazz = Class.forName(XUPDATE_QUERY_IMPL_CLASS);
    Object o = clazz.newInstance();
    Method setQString = clazz.getMethod("setQString", new Class[]{String.class});
    try {
      //xq.setQString(((XhiveDocumentIf) updateDoc).toXml());
      setQString.invoke(o, new Object[]{getQuery()});
      //xq.execute(inputDoc);
      Method execute = clazz.getMethod("execute", new Class[]{Node.class});
      execute.invoke(o, new Object[]{context});
      // Return the updated document
      return context;
    } catch (InvocationTargetException ite) {
      throw ite.getTargetException();
    }
  }

  @Override
  protected XhiveTreeNode buildResultRootNode(long timeTaken, Object result) {
    return new XhiveDocumentRootTreeNode((XhiveDocumentIf) result);
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  public static boolean isAvailable() {
    try {
      Class.forName(XUPDATE_QUERY_IMPL_CLASS);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  @Override
  protected String getDefaultQueryPropertyName() {
    return "com.xhive.adminclient.xupdate";
  }
}

package com.xhive.adminclient.tablemodels;

import com.xhive.adminclient.dialogs.XhiveMessageDialog;
import com.xhive.xpath.interfaces.XhiveXPathContextIf;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Table model for Xhive namespace declarations.
 *
 */
public class XhiveNamespaceDeclarationsTableModel extends XhiveTableModel {

    protected Properties keys;
    protected String namespacePropertiesFile;

    public XhiveNamespaceDeclarationsTableModel() {
        columns = new String[]{"Prefix", "URI"};
        namespacePropertiesFile = System.getProperty("user.home") + System.getProperty("file.separator") + ".xhive.admin.namespaces";

        keys = new Properties();
        try {
            keys.load(new BufferedInputStream(new FileInputStream(namespacePropertiesFile)));
        } catch (IOException e) {}


        loadData();
    }

    /*
      public Object getValueAt(Object element, int column) {
        String result = null;
     
        try {
          result = "";
     
          switch (column) {
            case 0:
              result = (String) ((Object[]) element)[0];
              break;
            case 1:
              result = (String) ((Object[]) element)[1];
              break;
          }
        } catch (Exception e) {
          XhiveMessageDialog.showException(e);
        }
        return result;
      }
    */

    public void addRow(Object[] row) {
        keys.setProperty((String) row[0], (String) row[1]);
        addElement(row);
    }

    public void removeRow(int row) {
        keys.remove(getValueAt(row, 0));
        removeElement(row);
    }

    public Vector getVector() {
        Enumeration enumeration = keys.keys();
        Vector properties = new Vector();
        while (enumeration.hasMoreElements()) {
            String property = (String) enumeration.nextElement();
            properties.add(new Object[]{property, keys.getProperty(property)});
        }
        return properties;
    }

    public void save() {
        try {
            keys.store(new BufferedOutputStream(new FileOutputStream(namespacePropertiesFile)), "Created by Xhive/DB 3.0 Adminclient");
        } catch (IOException e) {
            XhiveMessageDialog.showException(e);
        }
    }

    public void addNameSpacesContexts(XhiveXPathContextIf xpathContext) {
        Enumeration enumeration = keys.keys();
        while (enumeration.hasMoreElements()) {
            String prefix = (String) enumeration.nextElement();
            xpathContext.addNamespaceBinding(prefix, keys.getProperty(prefix));
        }
    }

    public boolean hasKey(String key) {
        if (keys.getProperty(key) == null) {
            return false;
        } else {
            return true;
        }
    }
}

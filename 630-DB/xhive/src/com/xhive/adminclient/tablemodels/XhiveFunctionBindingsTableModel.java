package com.xhive.adminclient.tablemodels;

import com.xhive.xpath.interfaces.XhiveXPathContextIf;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Table model for Xhive function bindings.
 *
 *
 */
public class XhiveFunctionBindingsTableModel extends XhiveNamespaceDeclarationsTableModel {

    public XhiveFunctionBindingsTableModel() {
        columns = new String[]{"Method name", "Class name"};
        namespacePropertiesFile = System.getProperty("user.home") + System.getProperty("file.separator") + ".xhive.admin.functions";

        keys = new Properties();
        try {
            keys.load(new BufferedInputStream(new FileInputStream(namespacePropertiesFile)));
        } catch (IOException e) {}


        loadData();
    }

    public void addFunctionBindings(XhiveXPathContextIf xpathContext) {
        Enumeration enumeration = keys.keys();
        while (enumeration.hasMoreElements()) {
            String method = (String) enumeration.nextElement();
            xpathContext.addFunctionBinding(method, keys.getProperty(method));
        }
    }
}

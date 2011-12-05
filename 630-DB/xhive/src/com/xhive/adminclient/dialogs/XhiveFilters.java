package com.xhive.adminclient.dialogs;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.HashMap;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 */
public class XhiveFilters {

    private static HashMap filters = new HashMap();

    static {
        filters.put("xml", new XhiveFilter("xml", "XML file"));
        filters.put("xsl", new XhiveFilter("xsl", "XSL file"));
        filters.put("dtd", new XhiveFilter("dtd", "Document type definition"));
        filters.put("xsd", new XhiveFilter("xsd", "XML Schema definition"));
        filters.put("xql", new XhiveFilter("xql", "XQuery file"));
        filters.put("jar", new XhiveFilter("jar", "Jar file"));
    }

    public static FileFilter getXMLFileFilter() {
        return getFilter("xml");
    }

    public static FileFilter getXSLFileFilter() {
        return getFilter("xsl");
    }

    public static FileFilter getJarFileFilter() {
        return getFilter("jar");
    }

    public static FileFilter getXQueryFileFilter() {
        return getFilter("xql");
    }

    public static FileFilter getFilter(String extension) {
        return (FileFilter) filters.get(extension);
    }
}

class XhiveFilter extends FileFilter {

    private String extension;
    private String description;

    public XhiveFilter(String extension, String description) {
        this.extension = extension;
        this.description = description + ", *." + extension;
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String ext = getExtension(f);
        if (ext.equalsIgnoreCase(extension)) {
            return true;
        }
        return false;
    }

    private String getExtension(File f) {
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            return s.substring(i + 1).toLowerCase();
        }
        return "";
    }

    public String getDescription() {
        return description;
    }
}

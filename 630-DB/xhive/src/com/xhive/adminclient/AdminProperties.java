package com.xhive.adminclient;

import com.xhive.error.XhiveException;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

public class AdminProperties {

    public static final String XQUERY_DIR = "com.xhive.adminclient.querydir";
    public static final String IMPORT_DIR = "com.xhive.adminclient.importdir";
    public static final String EXPORT_DIR = "com.xhive.adminclient.exportdir";
    public static final String SERIALIZE_PATH = "com.xhive.adminclient.serializedir";
    public static final String EXPLORER_DIVIDER_LOCATION = "com.xhive.adminclient.dividerlocation";
    public static final String BOOTSTRAP_PATH = "com.xhive.adminclient.bootstrap";
    public static final String BACKUP_PATH = "com.xhive.adminclient.backupdir";


    private static Properties properties;
    private static HashMap<String, String> defaultValues;
    private static File adminPropertiesFile;

    static {
        String file = System.getProperty("user.home") + System.getProperty("file.separator") + ".xhive.admin.properties";
        adminPropertiesFile = new File(file);
        properties = new Properties();
        try {
            if (adminPropertiesFile.exists()) {
                properties.load(new BufferedInputStream(new FileInputStream(adminPropertiesFile)));
            }
        } catch (IOException e) {
            // Ignore, do not use saved properties
        }
    }

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            value = getDefaultValue(key);
        }
        return value;
    }

    public static String getDefaultValue(String key) {
        initDefaults();
        return defaultValues.get(key);
    }

    public static int getInt(String key) throws XhiveException {
        return Integer.parseInt(getProperty(key));
    }

    public static File getFile(String key) throws XhiveException {
        return new File(getProperty(key));
    }

    public static void setProperty(String key, String value) throws XhiveException {
        properties.setProperty(key, value);
    }

    public static void setProperty(String key, int value) throws XhiveException {
        properties.setProperty(key, String.valueOf(value));
    }

    public static void setProperty(String key, File value) throws XhiveException {
        properties.setProperty(key, value.getAbsolutePath());
    }

    private static void initDefaults() {
        if (defaultValues == null) {
            defaultValues = new HashMap<String, String>();
            defaultValues.put("com.xhive.adminclient.top", "10");
            defaultValues.put("com.xhive.adminclient.left", "10");
            defaultValues.put("com.xhive.adminclient.height", "500");
            defaultValues.put("com.xhive.adminclient.width", "800");
            defaultValues.put("com.xhive.adminclient.querydialog.top", "10");
            defaultValues.put("com.xhive.adminclient.querydialog.left", "10");
            defaultValues.put("com.xhive.adminclient.querydialog.height", "500");
            defaultValues.put("com.xhive.adminclient.querydialog.width", "800");
            defaultValues.put("com.xhive.adminclient.database", "");
            defaultValues.put("com.xhive.adminclient.maxcachepages", "1024");
            defaultValues.put("com.xhive.adminclient.username", "Administrator");
            defaultValues.put(XQUERY_DIR, System.getProperty("user.dir"));
            defaultValues.put(IMPORT_DIR, System.getProperty("user.dir"));
            defaultValues.put(EXPORT_DIR, System.getProperty("user.dir"));
            defaultValues.put(BACKUP_PATH, System.getProperty("user.dir"));
            defaultValues.put(EXPLORER_DIVIDER_LOCATION, "200");
            defaultValues.put("com.xhive.adminclient.docfilters", "true/*.xml/1/true/*.xsl/1/true/*.*/0");
            defaultValues.put("com.xhive.adminclient.asfilters", "true/*.dtd/3/true/*.xsd/4");
            defaultValues.put("com.xhive.adminclient.searchterm", "");
        }
    }

    protected static void store() {
        try {
            properties.store(new BufferedOutputStream(new FileOutputStream(adminPropertiesFile)), "Created by Xhive/DB 3.0 Adminclient");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

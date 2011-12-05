/*
 * User: arnod
 * Date: Feb 20, 2003
 * Time: 4:56:39 PM
 *
 * X-Hive/TDS (c) X-Hive corporation
 */
package com.xhive.adminclient.dialogs;

import com.xhive.core.interfaces.XhiveSessionIf;

public class XhiveConnectionInfo {

    private String userName;
    private String password;
    private String databaseName;
    private boolean usesJAAS;
    private int wait;

    public XhiveConnectionInfo(String userName, String password, String databaseName, boolean usesJAAS) {
        this.userName = userName;
        this.password = password;
        this.databaseName = databaseName;
        this.usesJAAS = usesJAAS;
        this.wait = 1;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public int getWait() {
        return wait;
    }

    public boolean usesJAAS() {
        return usesJAAS;
    }
}

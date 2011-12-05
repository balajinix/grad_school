package com.xhive.adminclient;

import com.xhive.adminclient.dialogs.XhiveMessageDialog;
import com.xhive.core.interfaces.XhiveDriverIf;

import java.util.HashMap;


/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Very simple class to deal with the very hard task of determining when drivers may be closed.
 *
 */
public class DriverRegistry {

    private static final boolean DEBUG = false;

    /** XhiveDriverIf -> Integer (numUsers) */
    private static HashMap driverUsers = new HashMap();

    public synchronized static void registerDriverUser(XhiveDriverIf driver, Object user) {
        if (DEBUG) {
            System.out.println("register " + user.hashCode());
        }
        if (!driverUsers.containsKey(driver)) {
            driverUsers.put(driver, new Integer(0));
        }
        int currentUse = ((Integer) driverUsers.get(driver)).intValue();
        driverUsers.put(driver, new Integer(currentUse + 1));
    }

    public synchronized static void unregisterDriverUser(XhiveDriverIf driver, Object user) {
        if (DEBUG) {
            System.out.println("unregister " + user.hashCode());
        }
        if (driverUsers.containsKey(driver)) {
            int currentUse = ((Integer) driverUsers.get(driver)).intValue();
            currentUse--;
            if (currentUse == 0) {
                driverUsers.remove(driver);
                // Nobody uses this driver anymore, it may be closed
                if (driver.isInitialized()) {
                    if (DEBUG) {
                        System.out.println("driver.close()");
                    }
                    try {
                        driver.close();
                    } catch (Exception e) {
                        XhiveMessageDialog.showErrorMessage("Could not properly close current driver: " + e.getMessage());
                    }
                }
            } else {
                // Only lower current users
                driverUsers.put(driver, new Integer(currentUse));
            }
        } else {
            // Is this an error?
            //      Thread.dumpStack();
            //      System.out.println("unsynchronized unregisterDriverUser");
        }

    }
}

package com.xhive.adminclient;

import com.xhive.adminclient.resources.XhiveResourceFactory;

/**
 * (c) 2001-2003 X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Program (main) for administrator interface.
 *
 */
public class Admin {

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Shouldn't happen
        }

    }

    public static void main(String[] args) {
        XhiveSplashWindow splashWindow = null;
        if (!Boolean.getBoolean("nosplash")) {
            splashWindow = new XhiveSplashWindow(462, 426, XhiveResourceFactory.getImageSplash());
            splashWindow.setVisible(true);
            // ADQ: Give the splashscreen some time to show up.
            sleep(1500);
        }
        AdminMainFrame frame = AdminMainFrame.build();
        if (splashWindow != null) {
            // Wait for sleeper to finish
            splashWindow.dispose();
        }
        frame.setVisible(true);
        frame.validate();
    }
}

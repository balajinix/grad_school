package com.xhive.adminclient;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Exception thrown when cancel is clicked in dialogs, basically an identifier that a rollback is needed
 */

public class XhiveCancellation extends RuntimeException {

    public XhiveCancellation() {
        this("Cancel clicked");
    }

    public XhiveCancellation(String message) {
        super(message);
    }
}

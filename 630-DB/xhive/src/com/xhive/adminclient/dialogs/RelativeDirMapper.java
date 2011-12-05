package com.xhive.adminclient.dialogs;

import java.io.IOException;

import com.xhive.core.interfaces.XhiveFederationFactoryIf;

class RelativeDirMapper implements XhiveFederationFactoryIf.PathMapper {

    public String getPath(String originalPath, String databaseName, String segmentId)
    throws IOException {
        if (databaseName != null) {
            return null;
        } else {
            // Log file, put in relative logs directory
            return "logs";
        }
    }
}

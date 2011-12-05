package com.xhive.adminclient;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public class XhiveInputStreamWithProgressIndication extends InputStream {

    private InputStream source;
    private JProgressBar progressBar;

    public XhiveInputStreamWithProgressIndication(InputStream source, JProgressBar progressBar) {
        this.source = source;
        this.progressBar = progressBar;
    }

    void processProgress(final int b) {
        SwingUtilities.invokeLater(new Runnable() {
                                       public void run() {
                                           progressBar.setValue(progressBar.getValue() + b);
                                       }
                                   }
                                  );
    }

    public int read() throws IOException {
        processProgress(1);
        return source.read();
    }

    public int read(byte[] b) throws IOException {
        int read = source.read(b);
        processProgress(read);
        return read;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int read = source.read(b, off, len);
        processProgress(read);
        return read;
    }
}

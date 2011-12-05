package com.xhive.adminclient;

import javax.swing.*;
import java.io.IOException;
import java.io.Reader;

public class XhiveReaderWithProgressIndication extends Reader {

    private Reader source;
    private JProgressBar progressBar;

    public XhiveReaderWithProgressIndication(Reader source, JProgressBar progressBar) {
        this.source = source;
        this.progressBar = progressBar;
    }

    void processProgress(int b) {
        progressBar.setValue(progressBar.getValue() + b);
    }

    public int read() throws IOException {
        processProgress(1);
        return source.read();
    }

    public int read(char cbuf[]) throws IOException {
        int read = source.read(cbuf);
        processProgress(read);
        return read;
    }

    public int read(char cbuf[], int off, int len) throws IOException {
        int read = source.read(cbuf, off, len);
        processProgress(read);
        return read;
    }

    public void close() throws IOException {
        source.close();
    }
}

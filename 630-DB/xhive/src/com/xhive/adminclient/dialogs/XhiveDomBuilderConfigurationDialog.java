package com.xhive.adminclient.dialogs;

import com.xhive.adminclient.panes.XhiveDOMConfigurationPanel;

import org.w3c.dom.ls.LSParser;

import javax.swing.*;
import java.awt.*;

public class XhiveDomBuilderConfigurationDialog extends XhiveDialog {

    private LSParser parser;

    public XhiveDomBuilderConfigurationDialog(LSParser builder) {
        super("Configure DOM Builder");
        this.parser = builder;
    }

    protected JPanel buildFieldsPanel() {
        return XhiveDOMConfigurationPanel.getBuilderPanel(parser.getDomConfig());
    }
}

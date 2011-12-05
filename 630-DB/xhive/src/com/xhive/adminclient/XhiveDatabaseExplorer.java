package com.xhive.adminclient;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.xhive.adminclient.panes.MetadataPanel;
import com.xhive.adminclient.panes.XhiveContentsListPanel;
import com.xhive.adminclient.panes.XhiveExplorerRightPanel;
import com.xhive.adminclient.panes.XhiveIndexListPanel;
import com.xhive.adminclient.treenodes.XhiveDatabaseTreeNode;
import com.xhive.adminclient.treenodes.XhiveDocumentTreeNode;
import com.xhive.adminclient.treenodes.XhiveDocumentVersionTreeNode;
import com.xhive.adminclient.treenodes.XhiveExtendedTreeNode;
import com.xhive.adminclient.treenodes.XhiveLibraryChildTreeNode;
import com.xhive.adminclient.treenodes.XhiveLibraryTreeNode;
import com.xhive.core.interfaces.XhiveSessionIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Database explorer panel.
 *
 */
public class XhiveDatabaseExplorer extends JSplitPane implements TreeSelectionListener, ChangeListener {

    private static final String PROPERTIES_TAB_LABEL = "Properties";
    private static final String CONTENTS_TAB_LABEL = "Contents";
    private static final String TEXT_TAB_LABEL = "Text";
    private static final String INDEX_TAB_LABEL = "Indexes";
    private static final String METADATA_TAB_LABEL = "Metadata";

    private JToolBar contextToolBar;
    private XhiveDatabaseTree explorerTree;
    private JTabbedPane tabbedPane;

    public XhiveDatabaseExplorer(XhiveSessionIf session, JToolBar contextToolBar) {
        super(HORIZONTAL_SPLIT, true);
        this.contextToolBar = contextToolBar;
        setLeftComponent(buildLeftPanel(session));
        setRightComponent(new JTabbedPane());
        setDividerLocation(AdminProperties.getInt(AdminProperties.EXPLORER_DIVIDER_LOCATION));
        setResizeWeight(0);
    }

    // This method is called with an active transaction
    private JPanel buildLeftPanel(XhiveSessionIf session) {
        final JPanel leftPanel = new JPanel(new BorderLayout());
        // Use a transaction wrapper because this is necessary to open the first node, and select
        // the root library
        explorerTree = XhiveDatabaseTree.build(session,
                                               new XhiveDatabaseTreeNode(session.getDatabase().getName()), null);
        explorerTree.addTreeSelectionListener(XhiveDatabaseExplorer.this);
        leftPanel.add(new JScrollPane(explorerTree), BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(300, 300));
        TreePath path = new TreePath(((DefaultMutableTreeNode) explorerTree.getRoot().getLastChild()).getPath());
        // Select the root library
        explorerTree.setSelectionPath(path);
        return leftPanel;
    }

    public XhiveExtendedTreeNode getSelectedTreeNode() {
        return (XhiveExtendedTreeNode) explorerTree.getLastSelectedPathComponent();
    }

    public XhiveDatabaseTree getExplorerTree() {
        return explorerTree;
    }

    @Override
    public void setDividerLocation(int location) {
        super.setDividerLocation(location);
        if (location > 0) {
            AdminProperties.setProperty(AdminProperties.EXPLORER_DIVIDER_LOCATION, location);
        }
    }

    /**
     * This method is called when the right panel needs to be adjusted to
     * a new selection in the left panel
     */
    private void updateRightPanel(XhiveSessionIf session, XhiveExtendedTreeNode selectedNode) {
        selectedNode.update(session);
        String selectedTabName = null;
        if (tabbedPane != null) {
            // Remember what the current selected tab was, using its name.
            selectedTabName = getSelectedTabName(tabbedPane);
            // Temporarily remove the listener, so we don't get irrelavant
            // events while adjusting the tabs
            tabbedPane.removeChangeListener(this);
        }
        tabbedPane = new JTabbedPane();
        if (selectedNode.hasContentsPane()) {
            tabbedPane.addTab(CONTENTS_TAB_LABEL, new XhiveContentsListPanel(tabbedPane, selectedNode));
        }
        tabbedPane.addTab(PROPERTIES_TAB_LABEL, new PropertiesPanel(tabbedPane, selectedNode));
        if (selectedNode instanceof XhiveLibraryTreeNode || selectedNode instanceof XhiveDocumentTreeNode) {
            tabbedPane.addTab(INDEX_TAB_LABEL, new XhiveIndexListPanel(tabbedPane, selectedNode));
        }
        if (selectedNode.hasSerializableContent()) {
            SerializedContentPanel serializedContent = new SerializedContentPanel(tabbedPane, selectedNode);
            selectedNode.setSerializableContext(serializedContent);
            tabbedPane.addTab(TEXT_TAB_LABEL, serializedContent);
        }
        if (selectedNode instanceof XhiveLibraryChildTreeNode || selectedNode instanceof XhiveDocumentVersionTreeNode) {
            tabbedPane.addTab(METADATA_TAB_LABEL, new MetadataPanel(tabbedPane, selectedNode));
        }
        // Start listening to changes again
        tabbedPane.addChangeListener(this);
        changeTabSelection(tabbedPane, selectedTabName);
        stateChanged(null);
        // We store the original divider location and set it again afterwards, otherwise Swing
        // resizes (poorly) the tab
        int previousDividerLocation = getDividerLocation();
        Component previousPane = getRightComponent();
        if (previousPane instanceof JTabbedPane) {
            cleanupTabbedPane((JTabbedPane) previousPane);
            remove(previousPane);
        }
        setRightComponent(tabbedPane);
        setDividerLocation(previousDividerLocation);
    }

    /**
     * This attempts to 'dislodge' some components to enable garbage collection.
     * The most vital call is gc() on PropertiesPanels.
     */
    private void cleanupTabbedPane(JTabbedPane pane) {
        Component[] comps = pane.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof ChangeListener) {
                pane.removeChangeListener((ChangeListener) comps[i]);
            }
        }
        for (int i = 0; i < comps.length; i++) {
            pane.remove(comps[i]);
            if (comps[i] instanceof PropertiesPanel) {
                ((PropertiesPanel) comps[i]).gc();
            }
        }
    }

    private static String getSelectedTabName(JTabbedPane tabbedPane) {
        int selectedIndex = tabbedPane.getSelectedIndex();
        String selectedTabName = null;
        if (selectedIndex != -1) {
            selectedTabName = tabbedPane.getTitleAt(selectedIndex);
        }
        return selectedTabName;
    }

    private static int changeTabSelection(JTabbedPane tabbedPane, String selectedTabName) {
        if (selectedTabName != null) {
            // A Tab was already selected. Try to select the same tab, if possible
            int index = tabbedPane.indexOfTab(selectedTabName);
            if (index != -1) {
                tabbedPane.setSelectedIndex(index);
            } else {
                tabbedPane.setSelectedIndex(0);
            }
        } else {
            // No tab was selected. Just select first tab
            tabbedPane.setSelectedIndex(0);
        }
        return tabbedPane.getSelectedIndex();
    }

    // Tab change listener
    public void stateChanged(ChangeEvent e) {
        updateToolBar(getSelectedTreeNode());
    }

    private void updateToolBar(final XhiveExtendedTreeNode selectedNode) {
        // Remove all components from the context tool bar
        contextToolBar.removeAll();
        if (selectedNode != null) {
            ArrayList<Action> actions = new ArrayList<Action>();
            selectedNode.getToolBarActions(null, actions);// tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
            if (actions != null) {
                for (int i = 0; i < actions.size(); i++) {
                    contextToolBar.add(actions.get(i));
                }
            }
            contextToolBar.repaint();
        }
    }

    // TreeSelectionListener
    public void valueChanged(TreeSelectionEvent e) {
        final DefaultMutableTreeNode expandedNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
        /**
         * A short delay is used so the right panel does not styart to flicker when the keyboard is
         * used to run through the treenodes in the left panel.
         * An actual update will only be done if the selected node equals the expanded node,
         * and this is only the case if the selection has stabalized (is still the same when the
         * time event occurs)
         */
        Action updateDelayAction = new XhiveTransactedAction(true) {
                                       @Override
                                       public void xhiveActionPerformed(ActionEvent e1) {
                                           // Check if the selected node is still the same of that when the event occurred
                                           if (getSelectedTreeNode() == expandedNode) {
                                               updateRightPanel(getSession(), getSelectedTreeNode());
                                               updateToolBar(getSelectedTreeNode());
                                           }
                                       }
                                   };
        Timer timer = new Timer(300, updateDelayAction);
        timer.start();
        timer.setRepeats(false);
    }

    private static class SerializedContentPanel extends XhiveExplorerRightPanel implements XhiveUpdateListener {

        private XhiveTextArea textArea;

        SerializedContentPanel(JTabbedPane parent, XhiveExtendedTreeNode node) {
            super(parent, node);
        }

        @Override
        protected void buildPanel() {
            textArea = new XhiveTextArea(XhiveTextArea.TYPE_XML);
            textArea.setEditable(false);
            textArea.setForeground(Color.gray);
            add(XhiveTextSearchPanel.createTextSearchPanel(textArea), BorderLayout.NORTH);
            add(new JScrollPane(textArea), BorderLayout.CENTER);
        }

        @Override
        protected Object createContent(XhiveSessionIf session) throws Exception {
            // The text are output stream uses invoke later to insert text, so it is safe to use it here
            return getSelectedNode().getAsText(session);
        }

        @Override
        protected void createContentFinished(Object result) {
            textArea.setText((String) result);
            textArea.select(0, 0);
            textArea.discardAllEdits();
        }

    }

    private static class PropertiesPanel extends XhiveExplorerRightPanel {

        private JPanel propertiesPanel;
        private JTable propertiesTable;

        PropertiesPanel(JTabbedPane parent, XhiveExtendedTreeNode node) {
            super(parent, node);
        }

        /**
         * For garbage collection, it is some how vital that this gets unregistered.
         * No idea why.
         */
        private void gc() {
            propertiesTable.getModel().removeTableModelListener(propertiesTable);
        }

        @Override
        protected void buildPanel() {
            propertiesTable = new JTable();
            JScrollPane scrollPane = new JScrollPane(propertiesTable);
            scrollPane.getViewport().setBackground(Color.white);
            propertiesTable.setShowHorizontalLines(false);
            propertiesTable.setShowVerticalLines(false);
            propertiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            propertiesTable.setIntercellSpacing(new Dimension(0, 0));
            add(scrollPane, BorderLayout.CENTER);
        }

        @Override
        protected Object createContent(XhiveSessionIf session) {
            return null;
        }

        @Override
        protected void createContentFinished(Object result) {
            TableModel model = getSelectedNode().getPropertiesTableModel();
            propertiesTable.setModel(model);
        }
    }
}

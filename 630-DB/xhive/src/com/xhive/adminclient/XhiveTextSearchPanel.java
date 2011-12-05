package com.xhive.adminclient;

import com.xhive.adminclient.dialogs.SwingWorker;
import com.xhive.adminclient.treenodes.XhiveTreeNode;
import com.xhive.core.interfaces.XhiveSessionIf;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Simple Search 'bar' for text-area's and database tree's
 *
 */
public abstract class XhiveTextSearchPanel extends JPanel {

    private static final String SEARCH_PROPERTY_NAME = "com.xhive.adminclient.searchterm";

    public static XhiveTextSearchPanel createTextSearchPanel(JTextComponent textComponent) {
        return new TextAreaSearchPanel(textComponent);
    }

    public static XhiveTextSearchPanel createTextSearchPanel(JScrollPane treeScrollPane, XhiveSessionIf session) {
        return new DatabaseTreeSearchPanel(treeScrollPane, session);
    }

    private JTextField searchField;
    private JButton searchButton;
    private JLabel status;

    public XhiveTextSearchPanel() {
        super();

        setLayout(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(10);
        searchField.setText(AdminProperties.getProperty(SEARCH_PROPERTY_NAME));
        searchButton = new JButton("Search");
        status = new JLabel("");
        add(new JLabel("Text search:"));
        add(searchField);
        add(searchButton);
        add(status);
        searchButton.addActionListener(createSearchActionListener(false));
        searchField.addActionListener(createSearchActionListener(true));
    }

    private ActionListener createSearchActionListener(final boolean highlightField) {
        ActionListener action = new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        final String searchText = searchField.getText().trim();
                                        searchField.setEnabled(false);
                                        searchButton.setEnabled(false);
                                        status.setText("");
                                        AdminProperties.setProperty(SEARCH_PROPERTY_NAME, searchText);
                                        SwingWorker worker = new SwingWorker() {
                                                                 private boolean found = false;

                                                                 public Object construct() {
                                                                     found = performSearch(searchText);
                                                                     return null;
                                                                 }

                                                                 public void finished() {
                                                                     searchField.setEnabled(true);
                                                                     searchButton.setEnabled(true);
                                                                     if (!found) {
                                                                         status.setText("'" + searchText + "' not found");
                                                                     }
                                                                     if (highlightField) {
                                                                         SwingUtilities.invokeLater(new Runnable() {
                                                                                                        public void run() {
                                                                                                            searchField.requestFocus();
                                                                                                        }
                                                                                                    }
                                                                                                   );
                                                                     }
                                                                 }
                                                             };
                                        worker.start();
                                    }
                                };
        return action;
    }

    protected abstract boolean performSearch(final String searchText);


    // End of generic part

    /**
     * Text-search for text-area's
     */
    private static class TextAreaSearchPanel extends XhiveTextSearchPanel {

        /**
         * The text component that can be searched
         */
        private JTextComponent searchableComponent;

        public TextAreaSearchPanel(JTextComponent searchableComponent) {
            super();
            this.searchableComponent = searchableComponent;
        }

        protected boolean performSearch(final String searchText) {
            boolean found = false;
            if (!searchText.equals("")) {
                int startLookingPoint;
                if (searchableComponent.getSelectionEnd() == searchableComponent.getSelectionStart()) {
                    // Nothing selected, start from top
                    startLookingPoint = 0;
                } else {
                    // Look beyond current selection
                    startLookingPoint = searchableComponent.getSelectionEnd();
                }
                String textToSearch = searchableComponent.getText();
                final int foundLocation = textToSearch.indexOf(searchText, startLookingPoint);
                if (foundLocation != -1) {
                    found = true;
                    // Select text found, but in other thread
                    SwingUtilities.invokeLater(new Runnable() {
                                                   public void run() {
                                                       // Reset start, otherwise we cannot always set the end
                                                       searchableComponent.setSelectionStart(0);
                                                       searchableComponent.setSelectionEnd(foundLocation + searchText.length());
                                                       searchableComponent.setSelectionStart(foundLocation);
                                                       searchableComponent.getCaret().setSelectionVisible(true);
                                                   }
                                               }
                                              );
                }
            }
            return found;
        }

    }

    /**
     * Text-search for XhiveDatabaseTree's
     */
    private static class DatabaseTreeSearchPanel extends XhiveTextSearchPanel {

        /**
         * The scrollpane that contains the database tree can be searched
         */
        private JScrollPane treeScrollPane;
        private XhiveSessionIf session;

        /**
         * Keep a list of nodes that were expanded while searching, so that they can be collapsed if
         * opened during search but not used during selection.
         */
        private ArrayList nodesExpandedDuringPerformSearch;

        // 'cached' during each call to performSearch
        private String searchText;
        private XhiveDatabaseTree databaseTree;

        public DatabaseTreeSearchPanel(JScrollPane treeScrollPane, XhiveSessionIf session) {
            super();
            this.treeScrollPane = treeScrollPane;
            this.session = session;
        }

        private XhiveSessionIf getSession() {
            return session;
        }

        protected boolean performSearch(String searchText) {
            // init variables
            this.searchText = searchText;
            this.databaseTree = (XhiveDatabaseTree) treeScrollPane.getViewport().getView();
            nodesExpandedDuringPerformSearch = new ArrayList();
            boolean found = false;
            if ((this.databaseTree != null) && !searchText.equals("")) {
                XhiveTreeNode currentTreeNode = null;
                session.join();
                try {
                    while ((!found) && ((currentTreeNode = getNextNode(currentTreeNode)) != null)) {
                        if (currentTreeNode.getUserObject() instanceof Node) {
                            Node node = (Node) currentTreeNode.getUserObject();
                            found = searchIndividualNode(node);
                        } else {
                            // Treat as string (rare case)
                            String text = currentTreeNode.getUserObject().toString();
                            found = contains(text);
                        }
                    }
                }
                finally {
                    nodesExpandedDuringPerformSearch = null;
                    if (session.isJoined()) {
                        session.leave();
                    }
                }
                // Only after session is left above, can we savely expand the found node in the GUI
                if (found) {
                    TreePath treePath = new TreePath(currentTreeNode.getPath());
                    databaseTree.setSelectionPath(treePath);
                    databaseTree.scrollPathToVisible(treePath);
                }
            }
            this.databaseTree = null;
            return found;
        }

        private XhiveTreeNode getStartNode() {
            XhiveTreeNode result = databaseTree.getSelectedTreeNode();
            if (result == null) {
                result = databaseTree.getRoot();
            } else {
                // User interface item, never actually start at the selected node, but start at the next one
                result = getNextNode(result);
            }
            return result;
        }

        private XhiveTreeNode getNextNode(XhiveTreeNode currentTreeNode) {
            if (currentTreeNode == null) {
                return getStartNode();
            }
            Object userObject = currentTreeNode.getUserObject();
            if ( !(userObject instanceof Node) ||
                    ((Node)userObject).getNodeType() != Node.TEXT_NODE
               ) {
                if (currentTreeNode.notYetOpenedReadOnly()) {
                    currentTreeNode.expand(getSession());
                    nodesExpandedDuringPerformSearch.add(currentTreeNode);
                }
                if (currentTreeNode.getChildCount() != 0) {
                    // Proceed with first child
                    return (XhiveTreeNode) currentTreeNode.getChildAt(0);
                }
            }
            // We are still here, so currentTreeNode subtree fully processed
            // Check for siblings
            if (currentTreeNode.getNextSibling() != null) {
                return getNextSiblingWithCollapse(currentTreeNode);
            } else {
                // We are still here, so this node fully processed, and father also fully processed
                while (currentTreeNode.getParent() instanceof XhiveTreeNode) {
                    XhiveTreeNode parentTreeNode = (XhiveTreeNode) currentTreeNode.getParent();
                    collapseNode(currentTreeNode);
                    if (parentTreeNode.getNextSibling() != null) {
                        return getNextSiblingWithCollapse(parentTreeNode);
                    } else {
                        // Parent also has no siblings, so move up again
                        currentTreeNode = parentTreeNode;
                    }
                }
                // So all parents are fully processed too, we are done
                return null;
            }
        }

        private XhiveTreeNode getNextSiblingWithCollapse(XhiveTreeNode treeNode) {
            XhiveTreeNode nextSibling = (XhiveTreeNode) treeNode.getNextSibling();
            collapseNode(treeNode);
            return nextSibling;
        }

        private void collapseNode(XhiveTreeNode treeNode) {
            //      System.out.println("nodesExpandedDuringPerformSearch.size() = " + nodesExpandedDuringPerformSearch.size());
            if (nodesExpandedDuringPerformSearch.contains(treeNode)) {
                // Collapse previous node, as no search result was found in it and it was opened
                // by this routine
                nodesExpandedDuringPerformSearch.remove(treeNode);
                treeNode.collapse();
            }
        }

        private boolean contains(String text) {
            if (text != null) {
                return text.indexOf(searchText) != -1;
            } else {
                return false;
            }
        }

        private boolean searchIndividualNode(Node node) {
            boolean found = false;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // look at element name
                if (contains(node.getNodeName())) {
                    found = true;
                } else {
                    NamedNodeMap attrs = node.getAttributes();
                    int length = attrs.getLength();
                    // loop may do to much, but not a performance bottleneck if found here
                    for (int i = 0; i < length; i++) {
                        Node attr = attrs.item(i);
                        if (contains(attr.getNodeName()) || contains(attr.getNodeValue())) {
                            found = true;
                        }
                    }
                }
            } else {
                // For all non-element nodes, we only look at the node-value
                String value = node.getNodeValue();
                if (contains(value)) {
                    found = true;
                }
            }
            return found;
        }
    }

}


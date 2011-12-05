package com.xhive.adminclient;

import com.xhive.adminclient.resources.XhiveResourceFactory;
import com.xhive.adminclient.treenodes.XhiveTreeNode;
import com.xhive.adminclient.panes.XhiveQueryPanel;
import com.xhive.core.interfaces.XhiveSessionIf;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Database tree.
 */
public class XhiveDatabaseTree extends JTree implements TreeExpansionListener, TreeModelListener {

    private XhiveSessionIf session;

    // This does not need to be a transacted action since doDelete handles its own transaction
    private XhiveAction deleteAction = new XhiveAction("Delete",
                                       XhiveResourceFactory.getImageIcon(XhiveResourceFactory.DELETE_ICON),
                                       "Delete this object", 'd') {
                                           protected void xhiveActionPerformed(ActionEvent e) {
                                               final XhiveTreeNode selectedNode = getSelectedTreeNode();
                                               if (selectedNode != null) {
                                                   selectedNode.doDelete(getSession());
                                               }
                                           }
                                       };

    /**
     * The notifyOfCancelPanel is a bit of a hack, but it was really necessary to be able to disable
     * cancelling once addChildren begins.
     */
    public static XhiveDatabaseTree build(XhiveSessionIf session, XhiveTreeNode rootNode,
                                          XhiveQueryPanel notifyOfCancelPanel) {
        XhiveDatabaseTree tree = new XhiveDatabaseTree(session, rootNode);
        ToolTipManager.sharedInstance().registerComponent(tree);
        rootNode.setDatabaseTree(tree);
        // rootNode.expand(session) == getChildrenToAdd + addChildren
        ArrayList list = rootNode.getChildrenToAdd(session);
        if (notifyOfCancelPanel != null) {
            notifyOfCancelPanel.disableCancelButton();
        }
        rootNode.addChildren(list);
        tree.expandPath(new TreePath(rootNode.getPath()));
        return tree;
    }

    private XhiveDatabaseTree(XhiveSessionIf session, XhiveTreeNode rootNode) {
        super(new DefaultTreeModel(rootNode));
        this.session = session;
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new XhiveTreeCellRenderer());
        putClientProperty("JTree.lineStyle", "Angled");
        setLargeModel(true);
        setExpandsSelectedPaths(true);
        setShowsRootHandles(true);
        addTreeExpansionListener(this);
        getModel().addTreeModelListener(this);
        addMouseListener(new XhiveMouseListener() {
                             public void xhiveMouseDoubleClicked(MouseEvent e) {
                                 XhiveTreeNode treeNode = getSelectedTreeNode();

                                 if (treeNode != null && treeNode.isLeaf()) {
                                     treeNode.performDoubleClick();
                                 }
                             }

                             public void popupRequested(MouseEvent e) {
                                 addSelectionRow(getClosestRowForLocation(e.getX(), e.getY()));
                                 XhiveTreeNode treeNode = getSelectedTreeNode();

                                 if (treeNode != null) {
                                     getPopupMenu(treeNode).show(XhiveDatabaseTree.this,
                                                                 e.getX(), e.getY());
                                 }
                             }
                         }
                        );
        // Register key event handlers
        getActionMap().put("Delete", deleteAction);
        getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "Delete");
    }

    private JPopupMenu getPopupMenu(XhiveTreeNode treeNode) {
        JPopupMenu result;
        if (treeNode.openTransactionRequired()) {
            result = treeNode.getPopupMenu(getSession(), getActionMap());
        } else {
            result = treeNode.getPopupMenu(getActionMap());
        }
        return result;
    }

    public XhiveTreeNode getSelectedTreeNode() {
        return (XhiveTreeNode) getLastSelectedPathComponent();
    }

    public XhiveTreeNode getRoot() {
        return (XhiveTreeNode) getModel().getRoot();
    }

    public XhiveSessionIf getSession() {
        return session;
    }

    /**
     * Make sure expansion is threaded and updating the tree model
     * only occurs within the event dispatching thread.
     */
    public void treeExpanded(TreeExpansionEvent event) {
        final XhiveTreeNode expandedNode = (XhiveTreeNode) event.getPath().getLastPathComponent();
        expandedNode.expandInBackGround(getSession());
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        XhiveTreeNode collapsedNode = (XhiveTreeNode) event.getPath().getLastPathComponent();
        collapsedNode.collapse();
    }

    public void treeNodesChanged(TreeModelEvent e) {}


    public void treeNodesInserted(TreeModelEvent e) {}


    public void treeNodesRemoved(TreeModelEvent e) {
        XhiveTreeNode selectedNode = getSelectedTreeNode();
        int newSelectionIndex = -1;
        int[] indices = e.getChildIndices();
        Object[] deletedNodes = e.getChildren();
        for (int i = 0; i < deletedNodes.length; i++) {
            if (deletedNodes[i] == selectedNode) {
                // The selected node is deleted, See what's next to be selected
                newSelectionIndex = indices[i]++;
                XhiveTreeNode parent = (XhiveTreeNode) e.getTreePath().getLastPathComponent();
                if (newSelectionIndex < parent.getChildCount()) {
                    // There is a next sibling, make that the new selection
                    DefaultMutableTreeNode nextChild = (DefaultMutableTreeNode) parent.getChildAt(newSelectionIndex);
                    setSelectionPath(new TreePath(nextChild.getPath()));
                } else {
                    // There is no next sibling, select the parent
                    setSelectionPath(e.getTreePath());
                }
                break;
            }
        }
    }

    public void treeStructureChanged(TreeModelEvent e) {}


    public String getToolTipText(MouseEvent evt) {
        if (getRowForLocation(evt.getX(), evt.getY()) == -1) return null;
        TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
        return ((XhiveTreeNode) curPath.getLastPathComponent()).getToolTipText();
    }

}

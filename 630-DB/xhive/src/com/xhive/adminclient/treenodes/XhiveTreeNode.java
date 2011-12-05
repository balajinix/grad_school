package com.xhive.adminclient.treenodes;

import java.awt.Component;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import com.xhive.adminclient.AdminMainFrame;
import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.XhiveDatabaseTree;
import com.xhive.adminclient.XhiveTransactedSwingWorker;
import com.xhive.adminclient.XhiveTransactionWrapper;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.dom.interfaces.XhiveNodeIf;
import com.xhive.error.XhiveException;

import org.w3c.dom.ls.LSSerializer;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Class implementing an Xhive tree node.
 *
 */

public abstract class XhiveTreeNode extends DefaultMutableTreeNode {

    protected final static int MENU_CATEGORY_PROPERTIES = 0;
    protected final static int MENU_CATEGORY_AS = 1;
    protected final static int MENU_CATEGORY_QUERY = 2;
    protected final static int MENU_CATEGORY_VERSIONING = 3;
    protected final static int MENU_CATEGORY_MISC = 4;
    protected final static int MENU_CATEGORY_ADD = 5;
    protected final static int MENU_CATEGORY_SERIALIZE = 6;
    protected final static int MENU_CATEGORY_SERIALIZE_CP = 7;
    private final static int[] MENU_CATEGORIES = {MENU_CATEGORY_PROPERTIES, MENU_CATEGORY_QUERY, MENU_CATEGORY_MISC,
            MENU_CATEGORY_SERIALIZE, MENU_CATEGORY_SERIALIZE_CP,
            MENU_CATEGORY_ADD, MENU_CATEGORY_VERSIONING, MENU_CATEGORY_AS};

    private XhiveDatabaseTree databaseTree;
    private boolean isDeletable = true;
    private boolean hasChildren = false;

    /**
     * Initial adding of the nodes is done in different threads. That may
     * cause a concurrency issue when two threads decide to add nodes at the same time.
     * Therefore, this boolean controls whether adding of nodes is already occuring in another
     * thread.
     */
    private boolean addingInOtherThread = false;

    protected XhiveTreeNode(XhiveDatabaseTree databaseTree, Object object) {
        super(object);
        this.databaseTree = databaseTree;
    }

    protected void setDeletable(boolean deletable) {
        isDeletable = deletable;
    }

    protected void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public String getIconName() {
        return null;
    }

    public String getExpandedIconName() {
        return null;
    }

    public void setDatabaseTree(XhiveDatabaseTree databaseTree) {
        this.databaseTree = databaseTree;
    }

    protected XhiveSessionIf getDatabaseTreeSession() {
        // TODO (ADQ) : Fix me
        // This really is a hack. Have to find a betetr sollution for this!
        // Root tree nodes do not yet have a pointer to the database tree, so here
        // we'll just return the session of the main frame. This works, but needs
        // a cleaner sollution
        return (getDatabaseTree() != null ? getDatabaseTree().getSession() : AdminMainFrame.getSession(false));
    }

    protected XhiveDatabaseTree getDatabaseTree() {
        return databaseTree;
    }

    public DefaultTreeModel getTreeModel() {
        return (DefaultTreeModel) databaseTree.getModel();
    }

    @Override
    public String toString() {
        return getName();
    }

    protected abstract String getName();

    protected void nodeChanged() {
        getTreeModel().nodeChanged(this);
    }

    public abstract void performDoubleClick();

    public JPopupMenu getPopupMenu(final ActionMap actionMap) {
        return getPopupMenu(null, actionMap);
    }
    public JPopupMenu getPopupMenu(final XhiveSessionIf session, final ActionMap actionMap) {
        XhiveTransactionWrapper wrapper = new XhiveTransactionWrapper(session, true) {
                                              @Override
                                              protected Object transactedAction() throws Exception {
                                                  JPopupMenu popupMenu = new JPopupMenu();
                                                  for (int i = 0; i < MENU_CATEGORIES.length; i++) {
                                                      int oldItemCount = popupMenu.getComponentCount();
                                                      getMenuItems(getSession(), popupMenu, MENU_CATEGORIES[i]);
                                                      if (popupMenu.getComponentCount() > oldItemCount) {
                                                          popupMenu.addSeparator();
                                                      }
                                                  }
                                                  if (isDeletable()) {
                                                      popupMenu.add(new JMenuItem(actionMap.get("Delete")));
                                                  }
                                                  if (popupMenu.getComponentCount() > 0) {
                                                      // If the last child is a separator then remove it
                                                      JComponent lastChild = (JComponent) popupMenu.getComponent(popupMenu.getComponentCount() - 1);
                                                      if (lastChild.getClass() == JPopupMenu.Separator.class) {
                                                          popupMenu.remove(lastChild);
                                                      }
                                                  }
                                                  changeMenuItems(getSession(), popupMenu);
                                                  return popupMenu;
                                              }
                                          };
        return (JPopupMenu) wrapper.start();
    }

    protected void changeMenuItems(
        XhiveSessionIf session, JPopupMenu popupMenu
    ) {
        // intended to allow subclasses to disable
        // some menu item action commands,
        // e.g. for non Administrator users
    }


    protected AbstractButton getMenuItemByActionCommand(
        JPopupMenu popupMenu, String actionCommand
    ) {
        Component[] cp = popupMenu.getComponents();
        for (int i = 0; i < cp.length; i++) {
            if (!(cp[i] instanceof AbstractButton)) {
                continue;
            }
            AbstractButton absBut = (AbstractButton) cp[i];
            String actionCmd = absBut.getActionCommand();
            if (actionCmd == null) {
                continue;
            }
            if (actionCmd.equals(actionCommand)) {
                return absBut;
            }
        }
        return null;
    }

    protected abstract void getMenuItems(XhiveSessionIf session, JPopupMenu popupMenu, int category);

    /**
     * Executes the delete on the object. Might return false because in some cases the user is asked
     * to confirm the deletion first
     */
    public abstract void deleteAction(XhiveSessionIf session);

    protected abstract void buildChildList(XhiveSessionIf session, ArrayList<MutableTreeNode> childList);

    public synchronized ArrayList<MutableTreeNode> getChildrenToAdd(XhiveSessionIf session) {
        if (addingInOtherThread) {
            try {
                // Wait, with a maximum time to take into account bugs
                this.wait(20000);
            } catch (InterruptedException e) {
                // If interrupted, simply continue
            }

        }
        ArrayList<MutableTreeNode> childList = new ArrayList<MutableTreeNode>();
        if (getChildCount() == 0 || notYetOpened()) {
            buildChildList(session, childList);
        }
        if (childList.size() > 0) {
            addingInOtherThread = true;
        }
        return childList;
    }

    public synchronized void addChildren(ArrayList<MutableTreeNode> children1) {
        for (int i = 0; i < children1.size(); i++) {
            add(children1.get(i));
        }
        if (children1.size() > 0) {
            // Only notify if children were added
            // Note: This call can be very processor intensive, e.g. when showing a large query result
            // One of the few (only?) X-Hive/DB API calls that gets called during this processing is
            // XhiveTreeCellRenderer::getTreeCellRendererComponent.
            getTreeModel().nodeStructureChanged(this);
        }
        addingInOtherThread = false;
        this.notifyAll();
    }

    protected void refresh(XhiveSessionIf session) {
        removeAllChildren();
        expand(session);
    }

    public void expandInBackGround() {
        expandInBackGround(null);
    }

    /**
     * Overriden in XhiveNodeTreeNode
     */
    public boolean openTransactionRequired() {
        return false;
    }

    public void expandInBackGround(XhiveSessionIf session) {
        if (! openTransactionRequired()) {
            // Except when elements are involved, we do not use an open session but start a new one.
            session = null;
        }
        XhiveTransactedSwingWorker worker = new XhiveTransactedSwingWorker(session, true) {
                                                @Override
                                                protected Object xhiveConstruct() {
                                                    return getChildrenToAdd(getSession());
                                                }

                                                @SuppressWarnings("unchecked")
                                                @Override
                                                protected void xhiveFinished(Object result) {
                                                    addChildren((ArrayList<MutableTreeNode>)result);
                                                }
                                            };
        worker.start();
    }

    /**
     * If the node has no children yet this method will built the child list.
     */
    public final void expand(XhiveSessionIf session) {
        addChildren(getChildrenToAdd(session));
    }

    /**
     * ADQ: This construction is to fix what maybe a bug in swing. If a treeNode is not a Leaf but all it's
     * children are removed then it apears as being a leaf. To solve this a temporary tree node is inserted
     * of the type DefaultMutableTreeNode
     */
    private boolean notYetOpened() {
        if (getChildCount() == 1) {
            if (getFirstChild().getClass() == DefaultMutableTreeNode.class) {
                // This is here
                removeAllChildren();
                return true;
            }
        }
        return false;
    }

    /**
     * Readonly variant of last routine
     */
    public boolean notYetOpenedReadOnly() {
        if (children == null) {
            return true;
        }
        if (getChildCount() == 1) {
            if (getFirstChild().getClass() == DefaultMutableTreeNode.class) {
                return true;
            }
        }
        return false;
    }

    public void collapse() {
        // Remove all children to save memory
        removeAllChildren();
        // See comments add wasOpendedBefore for explanation
        add(new DefaultMutableTreeNode());
        nodeChanged();
    }

    public boolean isDeletable() {
        return isDeletable;
    }

    public boolean confirmDeletion() {
        return true;
    }

    /**
     * TODO (SBO): Deletion is still problematic, because the nodes that use deletion
     * can be in such different states of transaction.
     * Perhaps
     * if (!openTransactionRequired()) {
     *  session = null;
     * }
     * could be added to the beginning?
     */
    public void doDelete(XhiveSessionIf session) {
        if (isDeletable() && confirmDeletion()) {
            XhiveTransactedSwingWorker worker = new XhiveTransactedSwingWorker(session, false) {
                                                    @Override
                                                    protected Object xhiveConstruct() throws Exception {
                                                        deleteAction(this.getSession());
                                                        return null;
                                                    }

                                                    @Override
                                                    protected void xhiveFinished(Object result) {
                                                        // Notify tree node
                                                        getTreeModel().removeNodeFromParent(XhiveTreeNode.this);
                                                    }

                                                    @Override
                                                    protected boolean rollbackRequired(Throwable t) {
                                                        // This just happens to match openTransactionRequired, only because it is
                                                        // only overridden in XhiveNodeTreeNode
                                                        return !openTransactionRequired();
                                                    }
                                                };
            worker.start();
        }
    }

    @Override
    public final boolean isLeaf() {
        if (super.isLeaf()) {
            return !hasChildren;
        }
        return false;
    }

    public String getToolTipText() {
        return null;
    }

    protected static String getAsText(XhiveLibraryIf library, XhiveNodeIf node) throws Exception {
        LSSerializer writer = library.createLSSerializer();
        // TODO (ADQ) : Make this configurable (serialization settings) in the GUI
        String serializationProperties = AdminProperties.getProperty("com.xhive.adminclient.serializationproperties");
        if (serializationProperties == null) {
            // Use defaults. Do not unset namespaces, or documents generated by code that sets URIs and
            // prefixes, but no explicit namespace declaration attributes, will not be namespace
            // wellformed.
            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            writer.getDomConfig().setParameter("entities", Boolean.TRUE);
        } else {
            // Parse properties
            StringTokenizer st = new StringTokenizer(serializationProperties, ", ");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                String tokenValues[] = splitLSParam(token, '=');
                writer.getDomConfig().setParameter(tokenValues[0], new Boolean(tokenValues[1]));
            }
        }
        return writer.writeToString(node);
    }

    private static String[] splitLSParam(String str, char token) {
        int index = str.indexOf(token);
        if (index != -1) {
            String result[] = new String[2];
            result[0] = str.substring(0, index);
            result[1] = str.substring(index + 1);
            return result;
        } else {
            throw new XhiveException(XhiveException.INVALID_PARAMETER, "Expected '" + str + "' to be of format 'X"
                                     + token + "Y'");
        }
    }

    /**
     * getFullPath for treenodes, paths with names cause problems for identification in case names
     * are changed (at a higher level), so this routine uses only id's.
     */
    protected static String getFullPath(XhiveLibraryChildIf libChild) {
        XhiveLibraryIf library = libChild.getOwnerLibrary();
        if (library == null) return "/";
        StringBuffer path = new StringBuffer();
        insertId(path, libChild);
        XhiveLibraryIf parent;
        while ((parent = library.getOwnerLibrary()) != null) {
            insertId(path, library);
            library = parent;
        }
        return path.toString();
    }

    private static void insertId(StringBuffer buf, XhiveLibraryChildIf lc) {
        buf.insert(0, lc.getId());
        buf.insert(0, "id:");
        buf.insert(0, '/');
    }
}

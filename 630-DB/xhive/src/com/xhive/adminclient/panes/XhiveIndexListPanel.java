package com.xhive.adminclient.panes;

import com.xhive.adminclient.ObjectFinder;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveTransactedAction;
import com.xhive.adminclient.XhiveTransactedSwingWorker;
import com.xhive.adminclient.dialogs.XhiveAuthorityDialog;
import com.xhive.adminclient.dialogs.XhiveDialog;
import com.xhive.adminclient.dialogs.XhiveIndexDialog;
import com.xhive.adminclient.dialogs.XhiveMessageDialog;
import com.xhive.adminclient.treenodes.XhiveExtendedTreeNode;
import com.xhive.adminclient.treenodes.XhiveLibraryChildTreeNode;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveNodeIf;
import com.xhive.index.interfaces.XhiveIndexIf;
import com.xhive.index.interfaces.XhiveIndexListIf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class XhiveIndexListPanel extends XhiveTablePanel {

  private static final String[] COLUMN_NAMES = new String[]{"Name", "Index type", "Description", "options"};

  private static final int[] LIBRARY_INDEXES = {
    XhiveIndexIf.LIBRARY_ID_INDEX,
    XhiveIndexIf.LIBRARY_NAME_INDEX,
    XhiveIndexIf.ID_ATTRIBUTE_INDEX,
    XhiveIndexIf.VALUE_INDEX,
    XhiveIndexIf.ELEMENT_NAME_INDEX,
    XhiveIndexIf.SELECTED_ELEMENT_NAMES_INDEX,
    XhiveIndexIf.FULL_TEXT_INDEX,
    XhiveIndexIf.METADATA_VALUE_INDEX,
    XhiveIndexIf.METADATA_FULL_TEXT_INDEX,
    XhiveIndexIf.PATH_VALUE_INDEX,
  };

  private static final int[] DOCUMENT_INDEXES = {
    XhiveIndexIf.ID_ATTRIBUTE_INDEX,
    XhiveIndexIf.VALUE_INDEX,
    XhiveIndexIf.ELEMENT_NAME_INDEX,
    XhiveIndexIf.SELECTED_ELEMENT_NAMES_INDEX,
    XhiveIndexIf.FULL_TEXT_INDEX,
    XhiveIndexIf.METADATA_VALUE_INDEX,
    XhiveIndexIf.METADATA_FULL_TEXT_INDEX,
    XhiveIndexIf.PATH_VALUE_INDEX,
  };

  private String libraryChildPath;
  private int[] allowedIndexes;

  private XhiveAction addAction = new XhiveTransactedAction("Add", 'a') {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) {
      XhiveIndexListIf indexList = getIndexList(getSession());
      XhiveIndexDialog.showAddIndexesDialog(getSession(), indexList, allowedIndexes);
      updateIndexList(getIndexInfoArray(indexList));
    }
  };

  private XhiveAction changeAction = new XhiveTransactedAction("Change", 'r') {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) throws Exception {
      int[] selectedRows = getTable().getSelectedRows();
      if (selectedRows.length > 0) {
        XhiveIndexIf index = getIndex(getSession(), getTable().getSelectedRow());
        XhiveIndexListIf indexList = getIndexList(getSession());
        XhiveIndexDialog.showEditIndexDialog(getSession(), indexList, allowedIndexes, index.getName());
        updateIndexList(getIndexInfoArray(indexList));
      } else {
        XhiveMessageDialog.showErrorMessage("No index is selected");
      }
    }
  };

  private XhiveAction changeAuthorityAction = new XhiveTransactedAction("Change authority", 'c') {
    @Override
    protected void xhiveActionPerformed(ActionEvent e) {
      XhiveAuthorityDialog.showEditAuthority(getSession(), getIndex(getSession(),
          getTable().getSelectedRow()).getAuthority());
    }
  };

  public XhiveIndexListPanel(JTabbedPane parent, XhiveExtendedTreeNode node) {
    super(parent, node);
    add(buildButtonBar(), BorderLayout.NORTH);
  }

  private XhiveIndexListIf getIndexList(XhiveSessionIf session) {
    return ObjectFinder.findLibraryChild(session, libraryChildPath).getIndexList();
  }

  private XhiveIndexIf getIndex(XhiveSessionIf session, int rowIndex) {
    return getIndexList(session).getIndex((String) getTable().getModel().getValueAt(rowIndex, 0));
  }

  private JComponent buildButtonBar() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(new JButton(addAction));
    buttonPanel.add(new JButton(changeAction));
    buttonPanel.add(new JButton(deleteAction));
    return buttonPanel;
  }

  @Override
  protected void handleDeleteEvent(ActionEvent e) {
    final ArrayList<String> indexesToDelete = new ArrayList<String>();
    int[] selectedRows = getTable().getSelectedRows();
    if (selectedRows.length > 0) {
      for (int i = selectedRows.length-1; i >= 0; i--) {
        String indexName = (String) getTable().getValueAt(selectedRows[i], 0);
        if (XhiveDialog.showConfirmation("Are you sure you want to delete index: " + indexName + " ?")) {
          indexesToDelete.add(indexName);
        }
      }
      if (indexesToDelete.size() > 0) {
        XhiveTransactedSwingWorker worker = new XhiveTransactedSwingWorker(false) {
          @Override
          protected Object xhiveConstruct() throws Exception {
            XhiveIndexListIf indexList = getIndexList(getSession());
            for (int i = 0; i < indexesToDelete.size(); i++) {
              XhiveIndexIf index = indexList.getIndex(indexesToDelete.get(i));
              indexList.removeIndex(index);
            }
            return getIndexInfoArray(indexList);
          }

          @Override
          protected void xhiveFinished(Object result) {
            updateIndexList((Object[][]) result);
          }
        };
        worker.start();
      }
    } else {
      XhiveMessageDialog.showErrorMessage("No index is selected");
    }
  }

  @Override
  protected void handleDoubleClickEvent(int selectedRow) {
    // No need to do anything
  }

  @Override
  protected JPopupMenu getPopupMenu() {
    JPopupMenu popupMenu = new JPopupMenu();
    if (getTable().getSelectedRowCount() <= 1) {
      popupMenu.add(new JMenuItem(addAction));
//      popupMenu.add(new JMenuItem(rebuildAction));
      popupMenu.add(new JMenuItem(changeAuthorityAction));
    }
    popupMenu.add(new JMenuItem(deleteAction));
    return popupMenu;
  }

  private Object[][] getIndexInfoArray(XhiveIndexListIf indexList) {
    Object[][] indexInfo = new Object[indexList.size()][];
    int j = 0;
    for (Iterator<? extends XhiveIndexIf> i = indexList.iterator(); i.hasNext();) {
      indexInfo[j++] = getTableValues(i.next());
    }
    return indexInfo;
  }

  private void updateIndexList(Object[][] data) {
    DefaultTableModel tableModel = new DefaultTableModel(data, COLUMN_NAMES);
    getTable().setModel(tableModel);
    resizeTableColumnWidth();
  }

  private Object[] getTableValues(XhiveIndexIf index) {
    return new Object[]{index.getName(), XhiveIndexDialog.getIndexName(index.getType()),
                        String.valueOf(index), getOptionsString(index)};
  }

  private static final boolean hasOption(int options, int option) {
    return ((options & option) == option);
  }

  private static String getOptionsString(XhiveIndexIf index) {
    ArrayList<String> optionsList = new ArrayList<String>();
    int options = index.getOptions();
    if (hasOption(options, XhiveIndexIf.KEY_SORTED)) {
      optionsList.add("KEY_SORTED");
    }
    if (hasOption(options, XhiveIndexIf.UNIQUE_KEYS)) {
      optionsList.add("UNIQUE_KEYS");
    }
    if (hasOption(options, XhiveIndexIf.CONCURRENT)) {
      optionsList.add("CONCURRENT");
    }
    if (hasOption(options, XhiveIndexIf.FTI_GET_ALL_TEXT)) {
      optionsList.add("FTI_GET_ALL_TEXT");
    }
    if (hasOption(options, XhiveIndexIf.FTI_INCLUDE_ATTRIBUTES)) {
      optionsList.add("FTI_INCLUDE_ATTRIBUTES");
    }
    if (hasOption(options, XhiveIndexIf.FTI_SUPPORT_PHRASES)) {
      optionsList.add("FTI_SUPPORT_PHRASES");
    }
    if (hasOption(options, XhiveIndexIf.FTI_SA_ADJUST_TO_LOWERCASE)) {
      optionsList.add("FTI_SA_ADJUST_TO_LOWERCASE");
    }
    if (hasOption(options, XhiveIndexIf.FTI_SA_FILTER_ENGLISH_STOP_WORDS)) {
      optionsList.add("FTI_SA_FILTER_ENGLISH_STOP_WORDS");
    }
    return optionsList.toString();
  }

  @Override
  protected Object createContent(XhiveSessionIf session) {
    XhiveLibraryChildIf libraryChild = ((XhiveLibraryChildTreeNode) getSelectedNode()).
        getLibraryChild(session);
    switch (libraryChild.getNodeType()) {
      case XhiveNodeIf.DOCUMENT_NODE:
        allowedIndexes = DOCUMENT_INDEXES;
        break;
      case XhiveNodeIf.LIBRARY_NODE:
        allowedIndexes = LIBRARY_INDEXES;
        break;
    }
    this.libraryChildPath = libraryChild.getFullPath();
    return getIndexInfoArray(libraryChild.getIndexList());
  }

  @Override
  protected void createContentFinished(Object result) {
    updateIndexList((Object[][]) result);
  }
}

package com.xhive.adminclient.panes;

import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.dialogs.EFileChooser;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class XhiveFileListPanel extends JPanel {

  private EFileChooser fileChooser;
  private JList filesList;
  private DefaultListModel filesListModel;
  private FileFilter[] fileFilters = new FileFilter[0];
  private JLabel undefinedStatus;
  private boolean updatingUndefinedStatus = true;

  public XhiveFileListPanel() {
    super(new BorderLayout());
    // File list
    filesListModel = new DefaultListModel();
    filesList = new JList(filesListModel);
    filesList.setPreferredSize(new Dimension(300, 300));

    addUpdateStatusListener();

    JPanel fileListPanel = new JPanel(new BorderLayout());
    undefinedStatus = new JLabel("");
    undefinedStatus.setForeground(Color.red);
    fileListPanel.add(new JScrollPane(filesList), BorderLayout.CENTER);
    fileListPanel.add(undefinedStatus, BorderLayout.SOUTH);

    // File list button panel
    JPanel filesButtonPanel = new JPanel(new FlowLayout());
    filesButtonPanel.add(new JButton(new XhiveAction("Add", 'A') {
      @Override
      protected void xhiveActionPerformed(ActionEvent e) {
        addFiles();
      }
    }));
    filesButtonPanel.add(new JButton(new XhiveAction("Delete", 'D') {
      @Override
      protected void xhiveActionPerformed(ActionEvent e) {
        removeFiles();
      }
    }));
    filesButtonPanel.add(new JButton(new XhiveAction("Clear", 'C') {
      @Override
      protected void xhiveActionPerformed(ActionEvent e) {
        filesListModel.clear();
      }
    }));

    // Files panel
    add(fileListPanel, BorderLayout.CENTER);
    add(filesButtonPanel, BorderLayout.SOUTH);

    fileChooser = new EFileChooser("Select files to add");
    fileChooser.setMultiSelectionEnabled(true);
    fileChooser.setFileSelectionMode(EFileChooser.FILES_AND_DIRECTORIES);
    fileChooser.setCurrentDirectory(AdminProperties.getFile(AdminProperties.IMPORT_DIR));
  }

  private void addUpdateStatusListener() {
    filesListModel.addListDataListener(new ListDataListener() {
      public void intervalAdded(ListDataEvent e) {
        updateUndefinedStatus();
      }
      public void intervalRemoved(ListDataEvent e) {
        updateUndefinedStatus();
      }
      public void contentsChanged(ListDataEvent e) {
        updateUndefinedStatus();
      }
    });
  }

  private void updateUndefinedStatus() {
    if (updatingUndefinedStatus && (undefinedStatus != null)) {
      undefinedStatus.setText("");
      int size = filesListModel.size();
      for (int i = 0; i < size; i++) {
        File f = ((FileListWrapper) filesListModel.get(i)).getFile();
        if (!filterAvailable(f) && f.exists()) {
          undefinedStatus.setText("Filters need to be added for one or more entries, otherwise they are ignored");
          break;
        }
      }
    }
  }


  private void fireListChanged() {
    if (filesList != null) {
//      filesList.setModel(new DefaultListModel());
//      filesList.setModel(filesListModel);
    }
    updateUndefinedStatus();
  }

  public void setFileChooserFilters(FileFilter[] filters) {
    fileFilters = filters;
    fileChooser.resetChoosableFileFilters();
    for (int i = 0; i < filters.length; i++) {
      fileChooser.addChoosableFileFilter(filters[i]);
      fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[0]);
    }
    fireListChanged();
  }

  public int getFileCount() {
    return filesListModel.getSize();
  }

  public Object[] getFileList() {
    int size = filesListModel.size();
    Object[] list = new Object[size];
    for (int i = 0; i < size; i++) {
      list[i] = ((FileListWrapper) filesListModel.get(i)).getFile();
    }
    return list;
  }

  private void addFiles() {
    if (fileChooser.showDialog(this, "Add") == EFileChooser.APPROVE_OPTION) {
      File[] files = fileChooser.getSelectedFiles();

      if ((files.length == 1) && (!files[0].exists())) {
        // Bug work-around, in case a directory is double clicked and then add is clicked,
        // the file returned is the one just selected. So select the parent instead
        File parentFile = files[0].getParentFile();
        if (files[0].getName().equals(parentFile.getName())) {
          files[0] = parentFile;
        }
      }

      updatingUndefinedStatus = false;
      try {
        for (int i = 0; i < files.length; i++) {
          // Double entries are allowed on purpose
          filesListModel.addElement(new FileListWrapper(files[i]));
        }
      } finally {
        updatingUndefinedStatus = true;
      }
      updateUndefinedStatus();
    }
    AdminProperties.setProperty(AdminProperties.IMPORT_DIR, fileChooser.getCurrentDirectory());
  }

  private void removeFiles() {
    int currentFile = 0;

    while (currentFile < filesListModel.getSize()) {
      if (filesList.isSelectedIndex(currentFile)) {
        filesListModel.removeElementAt(currentFile);
      } else {
        currentFile++;
      }
    }
  }

  private boolean filterAvailable(File f) {
    if (f.isDirectory()) {
      return true;
    }
    int i = 0;
    while (i < fileFilters.length) {
      if (fileFilters[i].accept(f)) {
        return true;
      }
      i++;
    }
    return false;
  }

  /**
   * File-list wrapper that indicates when a file will be caught by a filter
   * in the dialog
   */
  private class FileListWrapper {
    private File file;

    public FileListWrapper(File file) {
      this.file = file;
    }

    public File getFile() {
      return file;
    }

    @Override
    public String toString() {
      String text = file.toString();
      if (! file.exists()) {
        text = "[FILE DOES NOT EXIST] " + text;
      } else if (! filterAvailable(file)) {
        text = "[NO FILTER DEFINED] " + text;
      }
      return text;
    }
  }

}

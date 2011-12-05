package com.xhive.adminclient.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.AbstractTableModel;

import com.xhive.adminclient.AdminProperties;
import com.xhive.adminclient.XhiveAction;
import com.xhive.adminclient.XhiveInputStreamWithProgressIndication;
import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.adminclient.layouts.StackLayout;
import com.xhive.adminclient.panes.XhiveDOMConfigurationPanel;
import com.xhive.adminclient.panes.XhiveFileListPanel;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.dom.interfaces.XhiveBlobNodeIf;
import com.xhive.dom.interfaces.XhiveCatalogIf;
import com.xhive.dom.interfaces.XhiveLibraryChildIf;
import com.xhive.dom.interfaces.XhiveLibraryIf;
import com.xhive.error.XhiveException;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Node;
import org.w3c.dom.as.ASDOMBuilder;
import org.w3c.dom.as.ASModel;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for importing documents into the database.
 */
public class XhiveImportDialog extends XhiveTransactedDialog {

    private XhiveLibraryIf library;

    private JPanel fieldsPanel;
    protected JTabbedPane tabbedPane;

    // Controls in features panel
    private XhiveDOMConfigurationPanel featuresPanel;

    private String filterPropertyName;

    // Controls in sources panel
    private XhiveFileListPanel fileListPanel;
    protected JCheckBox flatten;
    protected JCheckBox lockWithParentCheckBox;
    protected JCheckBox documentsDoNotLockWithParent;
    protected JCheckBox overWriteFiles;
    protected JCheckBox pruneEmptyDirectories;
    protected JPanel optionsPanel;

    // Controls in filters panel
    private JTable filterTable;
    private FilterTableModel filterTableModel;

    // Controls in progress panel
    private JPanel progressTab;
    private JLabel currentFileLabel;
    private JLabel currentProgressLabel;
    private JProgressBar currentProgressBar;
    private JLabel totalProgressLabel;
    private JProgressBar totalProgressBar;

    private boolean skipAll;
    private int totalFilesStored;
    private int totalFiles;

    public static int showImportFiles(XhiveSessionIf session, XhiveLibraryIf library) {
        String propertyName = "com.xhive.adminclient.docfilters";
        XhiveImportDialog dialog = new XhiveImportDialog("Import into library " + library.getFullPath(),
                                   session, library, propertyName);
        return dialog.execute();
    }

    public static int showImportASModels(XhiveSessionIf session, XhiveLibraryIf library) {
        String propertyName = "com.xhive.adminclient.asfilters";
        XhiveImportDialog dialog = new ImportASModelsDialog("Import into catalog of library " + library.getFullPath(),
                                   session, library, propertyName);
        return dialog.execute();
    }

    XhiveImportDialog(String title, XhiveSessionIf session, XhiveLibraryIf library, String filterPropertyName) {
        super(title, session);
        this.library = library;
        this.filterPropertyName = filterPropertyName;
    }

    private ArrayList<XhiveFileFilter> constructFilterFromPropertyString(String propertyName) {
        ArrayList<XhiveFileFilter> filters = new ArrayList<XhiveFileFilter>();
        String filtersString = AdminProperties.getProperty(propertyName);
        StringTokenizer filtersTokens = new StringTokenizer(filtersString, "/");
        while (filtersTokens.hasMoreTokens()) {
            filters.add(constructFilter(filtersTokens.nextToken(), filtersTokens.nextToken(), filtersTokens.nextToken()));
        }
        return filters;
    }

    private XhiveFileFilter constructFilter(String enabledString, String pattern, String storageTypeIndex) {
        int storageType = Integer.parseInt(storageTypeIndex);
        boolean enabled = Boolean.valueOf(enabledString).booleanValue();
        return new XhiveFileFilter(enabled, pattern, storageType);
    }

    private void storeFilters(String filterPropertyName1) {
        String propertyValue = "";
        ArrayList filterList = filterTableModel.getFilters();
        for (int i = 0; i < filterList.size(); i++) {
            XhiveFileFilter filter = (XhiveFileFilter) filterList.get(i);
            propertyValue += "/" + filter.enabled;
            propertyValue += "/" + filter.filter;
            propertyValue += "/" + filter.getStorageType();
        }
        // Remove leading and trailing /'s
        while (propertyValue.startsWith("/")) {
            propertyValue = propertyValue.substring(1);
        }
        while (propertyValue.endsWith("/")) {
            propertyValue = propertyValue.substring(0, propertyValue.length() - 1);
        }
        // Set the property in the default properties
        AdminProperties.setProperty(filterPropertyName1, propertyValue);
    }

    private boolean hasXMLFilters() {
        ArrayList filters = filterTableModel.getFilters();
        for (int i = 0; i < filters.size(); i++) {
            XhiveFileFilter filter = (XhiveFileFilter) filters.get(i);
            if (filter.getStorageType() == XhiveFileFilter.STORAGE_TYPE_DOCUMENT) {
                return true;
            }
        }
        return false;
    }

    protected JPanel buildFieldsPanel() {
        fieldsPanel = new JPanel(new BorderLayout());
        featuresPanel = XhiveDOMConfigurationPanel.getBuilderPanel(library.createLSParser().getDomConfig());
        // Tabs
        tabbedPane = new JTabbedPane();
        JPanel filterPanel = buildFiltersPanel(constructFilterFromPropertyString(filterPropertyName));
        tabbedPane.add("Select sources", buildSourcesPanel());
        tabbedPane.add("Filters", filterPanel);
        if (hasXMLFilters()) {
            // Only show this pannel if documents can be stored
            tabbedPane.addTab("Parser configuration", featuresPanel);
        }
        fieldsPanel.setLayout(new BorderLayout());
        fieldsPanel.add(tabbedPane, BorderLayout.CENTER);
        return fieldsPanel;
    }

    private FileFilter[] getFileFilters() {
        ArrayList<XhiveFileFilter> filters = filterTableModel.getFilters();
        ArrayList<XhiveFileFilter> fileFilters = new ArrayList<XhiveFileFilter>();
        for (int i = 0; i < filters.size(); i++) {
            XhiveFileFilter f = filters.get(i);
            if ((f != null) && (f.getStorageType() != XhiveFileFilter.STORAGE_TYPE_NO_STORAGE) && f.enabled) {
                fileFilters.add(f);
            }
        }
        FileFilter[] filterArray = new FileFilter[fileFilters.size()];
        return fileFilters.toArray(filterArray);
    }

    private JPanel buildSourcesPanel() {
        JPanel sourcesPanel = new JPanel(new BorderLayout());

        fileListPanel = new XhiveFileListPanel();
        fileListPanel.setFileChooserFilters(getFileFilters());
        fileListPanel.setBorder(new BorderUIResource.TitledBorderUIResource("Files and directories to import"));

        optionsPanel = new JPanel();
        optionsPanel.setBorder(new BorderUIResource.TitledBorderUIResource("Library settings"));
        lockWithParentCheckBox = new JCheckBox("Lock newly created libraries with parent (if possible)", false);
        documentsDoNotLockWithParent = new JCheckBox("Documents do not lock with parent");
        overWriteFiles = new JCheckBox("Overwrite existing documents with same name");
        pruneEmptyDirectories = new JCheckBox("Prune empty libraries");
        flatten = new JCheckBox("Flatten library structure");
        flatten.addChangeListener(new ChangeListener() {
                                      public void stateChanged(ChangeEvent e) {
                                          lockWithParentCheckBox.setEnabled(!flatten.isSelected());
                                          documentsDoNotLockWithParent.setEnabled(!flatten.isSelected());
                                          overWriteFiles.setEnabled(!flatten.isSelected());
                                          pruneEmptyDirectories.setEnabled(!flatten.isSelected());
                                      }
                                  }
                                 );
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.add(flatten);
        optionsPanel.add(lockWithParentCheckBox);
        optionsPanel.add(documentsDoNotLockWithParent);
        optionsPanel.add(overWriteFiles);
        optionsPanel.add(pruneEmptyDirectories);

        sourcesPanel.add(fileListPanel, BorderLayout.CENTER);
        sourcesPanel.add(optionsPanel, BorderLayout.SOUTH);

        return sourcesPanel;
    }

    private JPanel buildFiltersPanel(ArrayList fileFilters) {
        JPanel filtersPanel = new JPanel(new BorderLayout());

        filterTableModel = new FilterTableModel();
        for (int i = 0; i < fileFilters.size(); i++) {
            filterTableModel.add((XhiveFileFilter) fileFilters.get(i));
        }

        filterTable = new JTable(filterTableModel);
        filterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(new JButton(new XhiveAction("Reset", 'r') {
                                        public void xhiveActionPerformed(ActionEvent e) {
                                            AdminProperties.setProperty(filterPropertyName, AdminProperties.getDefaultValue(filterPropertyName));
                                            ArrayList filters = constructFilterFromPropertyString(filterPropertyName);
                                            filterTableModel.clear();
                                            for (int i = 0; i < filters.size(); i++) {
                                                filterTableModel.add((XhiveFileFilter) filters.get(i));
                                            }
                                            fileListPanel.setFileChooserFilters(getFileFilters());
                                        }
                                    }
                                   ));
        buttonPanel.add(new JButton(new XhiveAction("Move up", 'u') {
                                        public void xhiveActionPerformed(ActionEvent e) {
                                            filterTableModel.moveUp(filterTable.getSelectedRow());
                                        }
                                    }
                                   ));
        buttonPanel.add(new JButton(new XhiveAction("Move down", 'd') {
                                        public void xhiveActionPerformed(ActionEvent e) {
                                            filterTableModel.moveDown(filterTable.getSelectedRow());
                                        }
                                    }
                                   ));
        buttonPanel.add(new JButton(new XhiveAction("Add", 'A') {
                                        public void xhiveActionPerformed(ActionEvent e) {
                                            AddFilterDialog addDialog = new AddFilterDialog("Add filter");
                                            if (addDialog.execute() == XhiveDialog.RESULT_OK) {
                                                filterTableModel.add(new XhiveFileFilter(true, addDialog.filterExpressionField.getText(),
                                                                     addDialog.storageTypeComboBox.getSelectedIndex()));
                                            }
                                            fileListPanel.setFileChooserFilters(getFileFilters());
                                        }
                                    }
                                   ));
        buttonPanel.add(new JButton(new XhiveAction("Delete", 'D') {
                                        public void xhiveActionPerformed(ActionEvent e) {
                                            filterTableModel.removeRow(filterTable.getSelectedRow());
                                            fileListPanel.setFileChooserFilters(getFileFilters());
                                        }
                                    }
                                   ));
        buttonPanel.add(new JButton(new XhiveAction("Clear", 'C') {
                                        public void xhiveActionPerformed(ActionEvent e) {
                                            filterTableModel.clear();
                                            fileListPanel.setFileChooserFilters(getFileFilters());
                                        }
                                    }
                                   ));

        // Files panel
        JScrollPane tableScrollPane = new JScrollPane(filterTable);
        tableScrollPane.getViewport().setBackground(Color.white);
        filtersPanel.add(tableScrollPane, BorderLayout.CENTER);
        filtersPanel.add(buttonPanel, BorderLayout.SOUTH);

        return filtersPanel;
    }

    public JPanel buildProgressPanel() {
        progressTab = new JPanel(new StackLayout());
        currentFileLabel = new JLabel("Current file");
        progressTab.add(currentFileLabel);
        currentProgressLabel = new JLabel("Current progress");
        progressTab.add(currentProgressLabel);
        currentProgressBar = new JProgressBar();
        progressTab.add(currentProgressBar);
        currentProgressBar.setPreferredSize(new Dimension(350, (int) currentProgressBar.getPreferredSize().getHeight()));
        totalProgressLabel = new JLabel("Total progress");
        progressTab.add(totalProgressLabel);
        totalProgressBar = new JProgressBar();
        progressTab.add(totalProgressBar);
        totalProgressBar.setPreferredSize(new Dimension(350, (int) totalProgressBar.getPreferredSize().getHeight()));
        return progressTab;
    }

    protected boolean fieldsAreValid() {
        return (checkField(fileListPanel, fileListPanel.getFileCount() > 0, "Please select at least one source to import from"));
    }

    private Object[] getSelectedFiles() {
        return fileListPanel.getFileList();
    }

    //  protected void performOk() {
    //    switchToProgressView();
    //    // Start import
    //    performThreadedAction(true, true);
    //  }

    private void switchToProgressView() {
        fieldsPanel.remove(tabbedPane);
        fieldsPanel.add(buildProgressPanel());
        pack();
        resizeAndCenter();
    }

    void lockDialog() {
        super.lockDialog();
        switchToProgressView();
    }

    protected boolean performAction() throws Exception {
        storeFilters(filterPropertyName);
        countFiles(getSelectedFiles());
        int libraryOptions = lockWithParentCheckBox.isSelected() ? XhiveLibraryIf.LOCK_WITH_PARENT : 0;
        libraryOptions |= documentsDoNotLockWithParent.isSelected() ? XhiveLibraryIf.DOCUMENTS_DO_NOT_LOCK_WITH_PARENT : 0;
        int result = parseFiles(library, libraryOptions,
                                flatten.isSelected(),
                                overWriteFiles.isSelected(),
                                pruneEmptyDirectories.isSelected(),
                                getSelectedFiles());
        if (result != XhiveMessageDialog.RESULT_CANCEL) {
            return (result != XhiveMessageDialog.RESULT_CANCEL);
        } else {
            // An exception is thrown here because this forces a rollback in the framework
            // Not totally elegant, but looks okay in the program
            throw new RuntimeException("Parsing canceled by user");
        }
    }

    /**
     * Counts the total number of files to import and sets the size of the totalProgressBar
     * in the background.
     */
    protected void countFiles(final Object[] files) {
        final Dialog dialog = this;

        Thread runner = new Thread("xhiveThread") {
                            public void run() {
                                try {
                                    for (int i = 0; i < files.length; i++) {
                                        countFiles((File) files[i]);
                                    }
                                } catch (Exception e) {
                                    XhiveMessageDialog.showException(dialog, e);
                                }
                            }
                        };
        runner.start();
    }

    private void countFiles(final File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    countFiles(files[i]);
                }
            }
        } else {
            XhiveFileFilter fileFilter = getFileFilter(file);
            if (fileFilter != null && fileFilter.getStorageType() != XhiveFileFilter.STORAGE_TYPE_NO_STORAGE) {
                // The file is only processed if a filter applies to it
                totalFiles++;
                totalProgressBar.setMaximum(totalProgressBar.getMaximum() + (int) file.length());
            }
        }
    }

    protected int parseFiles(XhiveLibraryIf library1, int libraryOptions, boolean flatten1, boolean overwrite,
                             boolean prune, Object[] files) throws Exception {
        int result = XhiveMessageDialog.RESULT_OK;
        for (int i = 0; i < files.length && result != XhiveMessageDialog.RESULT_CANCEL; i++) {
            File file = (File) files[i];
            if (file.isDirectory()) {
                File[] filesArray = file.listFiles();
                if (filesArray == null) {
                    XhiveMessageDialog.showErrorMessage(this, "Can't read dir " + file.getAbsolutePath() + ", skipping...");
                } else {
                    XhiveLibraryIf newLibrary = library1;
                    if (!flatten1) {
                        String libraryName = file.getName();
                        newLibrary = (XhiveLibraryIf) library1.get(libraryName);
                        if (newLibrary == null) {
                            newLibrary = library1.createLibrary(libraryOptions);
                            newLibrary.setName(libraryName);
                        }
                    }
                    result = parseFiles(newLibrary, libraryOptions, flatten1, overwrite, prune, filesArray);
                    if (newLibrary.getParentNode() == null && newLibrary != library1) {
                        if (newLibrary.getFirstChild() != null || !prune) {
                            library1.appendChild(newLibrary);
                        }
                    }
                }
            } else {
                result = parseFile(library1, file, overwrite);
            }
        }
        return result;
    }

    private int parseFile(final XhiveLibraryIf library1, final File file, boolean overwrite) throws Exception {
        int result = XhiveMessageDialog.RESULT_OK;
        XhiveFileFilter fileFilter = getFileFilter(file);
        if (fileFilter != null && fileFilter.getStorageType() != XhiveFileFilter.STORAGE_TYPE_NO_STORAGE) {
            final JProgressBar bar = getTotalProgressBar();
            // Remind current value so it can be corrected if an error occurs.
            final int lastValue = bar.getValue();
            // Update labels
            final String filePath = file.getAbsolutePath();
            final String libraryPath = library1.getFullPath();
            SwingUtilities.invokeLater(new Runnable() {
                                           public void run() {
                                               currentFileLabel.setText(filePath);
                                               // Make sure there are never 0 files to do
                                               int currentTotal = (totalFiles == 0 ? 1 : totalFiles);
                                               currentProgressLabel.setText("Storing file " + (totalFilesStored + 1) + " of " + currentTotal + " into library \"" + libraryPath + "\"");
                                           }
                                       }
                                      );
            try {
                String name = shortUrl(file.toURL());
                Node existingChild = library1.get(name);
                if (existingChild != null) {
                    if (overwrite) {
                        library1.removeChild(existingChild);
                    } else {
                        name = getUniqueName(library1, name);
                    }
                }
                XhiveLibraryChildIf libraryChild = null;
                switch (fileFilter.getStorageType()) {
                case XhiveFileFilter.STORAGE_TYPE_DOCUMENT:
                    LSInput inputSource = library1.createLSInput();
                    inputSource.setByteStream(getProgressInputStream(file));
                    inputSource.setSystemId(file.toURL().toString());
                    LSParser builder = getBuilder(library1, featuresPanel);
                    libraryChild = (XhiveLibraryChildIf) builder.parse(inputSource);
                    break;
                case XhiveFileFilter.STORAGE_TYPE_BLOB:
                    XhiveBlobNodeIf blob = library1.createBlob();
                    libraryChild = blob;
                    blob.setContents(getProgressInputStream(file));
                    break;
                case XhiveFileFilter.STORAGE_TYPE_XSD:
                case XhiveFileFilter.STORAGE_TYPE_DTD:
                    XhiveCatalogIf catalog = library1.getCatalog();
                    LSInput inputSource2 = library1.createLSInput();
                    inputSource2.setByteStream(getProgressInputStream(file));
                    inputSource2.setSystemId(file.toURL().toString());
                    ASDOMBuilder builder2 = (ASDOMBuilder) library1.getCatalog().createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
                    String schemaType = fileFilter.getStorageType() == XhiveFileFilter.STORAGE_TYPE_DTD ?
                                        ASDOMBuilder.DTD_SCHEMA_TYPE : ASDOMBuilder.XML_SCHEMA_SCHEMA_TYPE;
                    builder2.getDomConfig().setParameter("validate", Boolean.TRUE);
                    builder2.getDomConfig().setParameter("error-handler", new DOMErrorHandler() {
                                                             public boolean handleError(DOMError error) {
                                                                 if (error.getSeverity() != DOMError.SEVERITY_WARNING) {
                                                                     if (error.getRelatedException() != null) {
                                                                         throw new XhiveException(XhiveException.PARSE_ERROR, (Exception) error.getRelatedException(), error.getMessage());
                                                                     } else {
                                                                         throw new XhiveException(XhiveException.PARSE_ERROR, error.getMessage());
                                                                     }
                                                                 }
                                                                 // In case of warning, continue
                                                                 return true;
                                                             }
                                                         }
                                                        );

                    ASModel model = builder2.parseASInputSource(inputSource2, schemaType);
                    model.setLocation(file.toURL().toString());
                    catalog.addASModel(model);
                    break;
                }
                if (fileFilter.getStorageType() != XhiveFileFilter.STORAGE_TYPE_DTD &&
                        fileFilter.getStorageType() != XhiveFileFilter.STORAGE_TYPE_XSD) {
                    libraryChild.setName(name);
                    library1.appendChild(libraryChild);
                }
            } catch (Exception e) {
                // Correct progress bar position
                SwingUtilities.invokeLater(new Runnable() {
                                               public void run() {
                                                   bar.setValue(lastValue + (int) file.length());
                                               }
                                           }
                                          );
                if (!skipAll) {
                    result = XhiveMessageDialog.showParseException(this, e, file.getAbsolutePath());
                    skipAll = (result == XhiveMessageDialog.RESULT_SKIP_ALL);
                }
            }
            totalFilesStored++;
        }
        return result;
    }

    private LSParser getBuilder(XhiveLibraryIf library1, XhiveDOMConfigurationPanel featuresPanel1) {
        LSParser builder = library1.createLSParser();
        // Copy settings from prototype (order is important)
        HashMap<String,Boolean> featureMap = featuresPanel1.getFeatureMap();
        String featureNames[] = featuresPanel1.getFeatureList();
        for (int i = 0; i < featureNames.length; i++) {
            String key = featureNames[i];
            builder.getDomConfig().setParameter(key, featureMap.get(key));
        }
        if (featuresPanel1.getConfig().getParameter(XhiveDOMConfigurationPanel.PARAMETER_SCHEMA_TYPE) != null) {
            builder.getDomConfig().setParameter(XhiveDOMConfigurationPanel.PARAMETER_SCHEMA_TYPE,
                                                featuresPanel1.getConfig().getParameter(XhiveDOMConfigurationPanel.PARAMETER_SCHEMA_TYPE));
        }
        if (featuresPanel1.getConfig().getParameter(XhiveDOMConfigurationPanel.PARAMETER_SCHEMA_LOCATION) != null) {
            builder.getDomConfig().setParameter(XhiveDOMConfigurationPanel.PARAMETER_SCHEMA_LOCATION,
                                                featuresPanel1.getConfig().getParameter(XhiveDOMConfigurationPanel.PARAMETER_SCHEMA_LOCATION));
        }
        return builder;
    }

    private String shortUrl(URL url) {
        String urlString = url.toString();
        if (urlString.charAt(urlString.length() - 1) == '/') {
            urlString = urlString.substring(0, urlString.length() - 1);
        }
        int index = urlString.lastIndexOf('/');
        if (index > 0) {
            urlString = urlString.substring(index + 1);
        }
        return urlString;
    }

    public static String getUniqueName(XhiveLibraryIf library, String suggestedName) {
        if (library.nameExists(suggestedName)) {
            String beforeDot, fromDot;
            int indexOfDot = suggestedName.indexOf(".");

            if (indexOfDot >= 0) {
                beforeDot = suggestedName.substring(0, indexOfDot);
                fromDot = suggestedName.substring(indexOfDot);
            } else {
                beforeDot = suggestedName;
                fromDot = "";
            }

            int copy = 1;
            String newName;

            do {
                newName = beforeDot + " (" + copy + ")" + fromDot;
                copy++;
            } while (library.nameExists(newName));

            return newName;
        } else {
            return suggestedName;
        }
    }

    private JProgressBar getTotalProgressBar() {
        return totalProgressBar;
    }

    private InputStream getProgressInputStream(File file) throws FileNotFoundException {
        // Reset the current progress bar
        currentProgressBar.setValue(0);
        currentProgressBar.setMaximum((int) file.length());
        // Wrap the stream twice, once for the current progress, and once for the
        InputStream is = new XhiveInputStreamWithProgressIndication(new FileInputStream(file), currentProgressBar);
        return new XhiveInputStreamWithProgressIndication(is, totalProgressBar);
    }

    private XhiveFileFilter getFileFilter(File file) {
        ArrayList filters = filterTableModel.getFilters();
        for (int i = 0; i < filters.size(); i++) {
            XhiveFileFilter filter = (XhiveFileFilter) filters.get(i);
            if (filter.appliesTo(file)) {
                return filter;
            }
        }
        return null;
    }


    class FilterTableModel extends AbstractTableModel {

        private final String[] COLUMN_NAMES = new String[]{"enabled", "file filter", "Storage type"};

        private ArrayList<XhiveFileFilter> filters;

        FilterTableModel() {
            this.filters = new ArrayList<XhiveFileFilter>();
        }

        public int getRowCount() {
            return filters.size();
        }

        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 0);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            XhiveFileFilter filter = filters.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return new Boolean(filter.enabled);
            case 1:
                return filter.filter;
            case 2:
                return XhiveFileFilter.STORAGE_TYPE_NAMES[filter.storageType];
            }
            return null;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            XhiveFileFilter filter = filters.get(rowIndex);
            switch (columnIndex) {
            case 0:
                filter.enabled = ((Boolean) aValue).booleanValue();
                fileListPanel.setFileChooserFilters(getFileFilters());
                break;
            }
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        public void add(XhiveFileFilter filter) {
            boolean added = false;
            if (!isGenericFilter(filter)) {
                // This is a specific filter, make sure it is inserted before any general filters.
                for (int i = 0; i < filters.size(); i++) {
                    if (isGenericFilter(filters.get(i))) {
                        // Add filter before generic one
                        filters.add(i, filter);
                        added = true;
                        break;
                    }
                }
            }
            if (!added) {
                // Add to the end if no proper place to place it for
                filters.add(filter);
            }
            fireTableRowsInserted(filters.size(), filters.size());
        }

        private boolean isGenericFilter(XhiveFileFilter filter) {
            String filterString = filter.filter;
            return (filterString != null && (filterString.equals("*.*") || filterString.equals("*")));
        }

        public void removeRow(int row) {
            if (row >= 0) {
                filters.remove(row);
                fireTableRowsDeleted(row, row);
            } else {
                XhiveMessageDialog.showErrorMessage("No filter is selected");
            }
        }

        public void moveUp(int row) {
            if (row > 0) {
                XhiveFileFilter old = filters.get(row - 1);
                filters.set(row - 1, filters.get(row));
                filters.set(row, old);
                fireTableDataChanged();
            }
        }

        public void moveDown(int row) {
            if (row < filters.size() - 1) {
                XhiveFileFilter old = filters.get(row + 1);
                filters.set(row + 1, filters.get(row));
                filters.set(row, old);
                fireTableDataChanged();
            }
        }

        public void clear() {
            filters.clear();
            fireTableDataChanged();
        }

        public ArrayList<XhiveFileFilter> getFilters() {
            return filters;
        }
    }

    static class XhiveFileFilter extends FileFilter {

        final static String[] STORAGE_TYPE_NAMES = {"No storage", "XML Document", "Blob", "Dtd", "XML Schema"};

        /**
         * Note: You may not simply change the values of these constants,
         * as they are used in the preferences.
         */
        static final int STORAGE_TYPE_NO_STORAGE = 0;
        static final int STORAGE_TYPE_DOCUMENT = 1;
        static final int STORAGE_TYPE_BLOB = 2;
        static final int STORAGE_TYPE_DTD = 3;
        static final int STORAGE_TYPE_XSD = 4;

        boolean enabled;
        String filter;
        int storageType;

        XhiveFileFilter(boolean enabled, String filter, int storageType) {
            this.enabled = enabled;
            this.filter = filter;
            this.storageType = storageType;
        }

        public boolean accept(File f) {
            return (appliesTo(f) || f.isDirectory());
        }

        public String getDescription() {
            return STORAGE_TYPE_NAMES[storageType] + ", " + filter;
        }

        boolean appliesTo(File file) {
            return wildcardEquals(filter, 0, file.getName().toLowerCase(), 0);
        }

        int getStorageType() {
            return storageType;
        }

        private static final char WILDCARD_STRING = '*';
        private static final char WILDCARD_CHAR = '?';

        /**
         * This code was copied from Apache lucene
         *
         * Determines if a word matches a wildcard pattern.
         * <small>Work released by Granta Design Ltd after originally being done on
         * company time.</small>
         */
        public final boolean wildcardEquals(String pattern, int patternIdx,
                                            String string, int stringIdx) {
            for (int p = patternIdx; ; ++p) {
                for (int s = stringIdx; ; ++p, ++s) {
                    // End of string yet?
                    boolean sEnd = (s >= string.length());
                    // End of pattern yet?
                    boolean pEnd = (p >= pattern.length());

                    // If we're looking at the end of the string...
                    if (sEnd) {
                        // Assume the only thing left on the pattern is/are wildcards
                        boolean justWildcardsLeft = true;

                        // Current wildcard position
                        int wildcardSearchPos = p;
                        // While we haven't found the end of the pattern,
                        // and haven't encountered any non-wildcard characters
                        while (wildcardSearchPos < pattern.length() && justWildcardsLeft) {
                            // Check the character at the current position
                            char wildchar = pattern.charAt(wildcardSearchPos);
                            // If it's not a wildcard character, then there is more
                            // pattern information after this/these wildcards.

                            if (wildchar != WILDCARD_CHAR &&
                                    wildchar != WILDCARD_STRING) {
                                justWildcardsLeft = false;
                            } else {
                                // Look at the next character
                                wildcardSearchPos++;
                            }
                        }

                        // This was a prefix wildcard search, and we've matched, so
                        // return true.
                        if (justWildcardsLeft) {
                            return true;
                        }
                    }

                    // If we've gone past the end of the string, or the pattern,
                    // return false.
                    if (sEnd || pEnd) {
                        break;
                    }

                    // Match a single character, so continue.
                    if (pattern.charAt(p) == WILDCARD_CHAR) {
                        continue;
                    }

                    //
                    if (pattern.charAt(p) == WILDCARD_STRING) {
                        // Look at the character beyond the '*'.
                        ++p;
                        // Examine the string, starting at the last character.
                        for (int i = string.length(); i >= s; --i) {
                            if (wildcardEquals(pattern, p, string, i)) {
                                return true;
                            }
                        }
                        break;
                    }
                    if (pattern.charAt(p) != string.charAt(s)) {
                        break;
                    }
                }
                return false;
            }
        }
    }

    class AddFilterDialog extends XhiveDialog {

        JTextField filterExpressionField;
        JComboBox storageTypeComboBox;

        public AddFilterDialog(String title) {
            super(title);
        }

        protected JPanel buildFieldsPanel() {
            JPanel fieldsPanel1 = new JPanel(new FormLayout());
            fieldsPanel1.add(new JLabel("filter expression"));
            filterExpressionField = new JTextField("*.");
            fieldsPanel1.add(filterExpressionField);
            fieldsPanel1.add(new JLabel("storage type"));
            storageTypeComboBox = new JComboBox();
            for (int i = 0; i < XhiveFileFilter.STORAGE_TYPE_NAMES.length; i++) {
                storageTypeComboBox.addItem(XhiveFileFilter.STORAGE_TYPE_NAMES[i]);
            }
            fieldsPanel1.add(storageTypeComboBox);
            return fieldsPanel1;
        }

        protected boolean fieldsAreValid() {
            String text = filterExpressionField.getText();
            if (text.indexOf("/") != -1) {
                XhiveMessageDialog.showErrorMessage("You may not use the '/' character in filter expressions");
                return false;
            }
            return super.fieldsAreValid() && checkFieldNotEmpty(filterExpressionField, "You should specify a filter expression");
        }
    }

    static class ImportASModelsDialog extends XhiveImportDialog {

        ImportASModelsDialog(String title, XhiveSessionIf session, XhiveLibraryIf library, String propertyName) {
            super(title, session, library, propertyName);
        }

        protected void setFields() {
            flatten.setEnabled(false);
            lockWithParentCheckBox.setEnabled(false);
            documentsDoNotLockWithParent.setEnabled(false);
            overWriteFiles.setEnabled(false);
            pruneEmptyDirectories.setEnabled(false);
            optionsPanel.setVisible(false);
        }
    }

}


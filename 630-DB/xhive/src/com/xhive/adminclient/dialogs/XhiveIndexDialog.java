package com.xhive.adminclient.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventListener;
import java.util.StringTokenizer;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.xhive.adminclient.layouts.FormLayout;
import com.xhive.adminclient.layouts.StackLayout;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.index.interfaces.XhiveIndexIf;
import com.xhive.index.interfaces.XhiveIndexListIf;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Dialog for adding indexes.
 *
 */
public class XhiveIndexDialog extends XhiveTransactedDialog {

    private static final int EDIT_INDEX = 0;
    private static final int ADD_INDEX = 1;

    private int dialogType;

    private JTextField indexName;
    private JTextField elementURI;
    private JTextField elementName;
    private JTextField attributeURI;
    private JTextField attributeName;
    private JTextField indexPath;
    private JTextField selectedElementNames;
    private JTextField analyzerClassName;
    private JTextField metadataKeyName;
    private JLabel elementURILabel;
    private JLabel elementNameLabel;
    private JLabel attributeURILabel;
    private JLabel attributeNameLabel;
    private JLabel indexPathLabel;
    private JLabel selectedElementNamesLabel;
    private JLabel analyzerClassNameLabel;
    private JLabel metadataKeyNameLabel;
    private JComboBox indexTypeCombo;
    private JCheckBox uniqueKeys;
    private JCheckBox keySorted;
    private JCheckBox concurrent;
    private JCheckBox indexAllText;
    private JCheckBox includeAttributes;
    private JCheckBox supportPhrases;
    private JCheckBox saToLowerCase;
    private JCheckBox saFilterStopWords;
    private XhiveIndexListIf indexList;
    private int[] allowedIndexes;
    private String originalIndexName;
    private JComboBox valueIndexTypeCombo;
    private JLabel valueIndexTypeLabel;

    public XhiveIndexDialog(String title, XhiveSessionIf session, XhiveIndexListIf indexList, int[] allowedIndexes,
                            String indexName, int dialogType) {
        super(title, session);
        this.dialogType = dialogType;
        this.indexList = indexList;
        this.allowedIndexes = allowedIndexes;
        this.originalIndexName = indexName;
    }

    public static int showAddIndexesDialog(XhiveSessionIf session, XhiveIndexListIf indexList, int[] allowedIndexes) {
        XhiveIndexDialog dialog = new XhiveIndexDialog("Add index", session, indexList, allowedIndexes, null,
                                  ADD_INDEX);
        return dialog.execute();
    }

    public static int showEditIndexDialog(XhiveSessionIf session, XhiveIndexListIf indexList, int[] allowedIndexes, String indexName) {
        XhiveIndexDialog dialog = new XhiveIndexDialog("Add index", session, indexList, allowedIndexes, indexName,
                                  EDIT_INDEX);
        return dialog.execute();
    }

    @Override
    protected JPanel buildFieldsPanel() {
        JPanel editPanel = new JPanel(new FormLayout());

        editPanel.add(new JLabel("Index type:"));
        indexTypeCombo = new JComboBox();
        for (int i = 0; i < allowedIndexes.length; i++) {
            indexTypeCombo.addItem(getIndexName(allowedIndexes[i]));
        }
        indexTypeCombo.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    setFields(getIndexType((String) indexTypeCombo.getSelectedItem()));
                }
            }
        );
        editPanel.add(indexTypeCombo);

        editPanel.add(new JLabel("Index name:"));
        indexName = new JTextField();
        editPanel.add(indexName);

        valueIndexTypeLabel = new JLabel("Value index Type:");
        editPanel.add(valueIndexTypeLabel);
        valueIndexTypeCombo = new JComboBox();
        editPanel.add(valueIndexTypeCombo);
        valueIndexTypeCombo.addItem(getValueIndexTypeName(XhiveIndexIf.TYPE_STRING));
        valueIndexTypeCombo.addItem(getValueIndexTypeName(XhiveIndexIf.TYPE_INT));
        valueIndexTypeCombo.addItem(getValueIndexTypeName(XhiveIndexIf.TYPE_LONG));
        valueIndexTypeCombo.addItem(getValueIndexTypeName(XhiveIndexIf.TYPE_FLOAT));
        valueIndexTypeCombo.addItem(getValueIndexTypeName(XhiveIndexIf.TYPE_DOUBLE));
        valueIndexTypeCombo.addItem(getValueIndexTypeName(XhiveIndexIf.TYPE_DATE));
        valueIndexTypeCombo.addItem(getValueIndexTypeName(XhiveIndexIf.TYPE_DATE_TIME));
        valueIndexTypeCombo.addItem(getValueIndexTypeName(XhiveIndexIf.TYPE_DAY_TIME_DURATION));
        valueIndexTypeCombo.addItem(getValueIndexTypeName(XhiveIndexIf.TYPE_YEAR_MONTH_DURATION));

        elementURILabel = new JLabel("Element URI:");
        editPanel.add(elementURILabel);
        elementURI = new JTextField();
        editPanel.add(elementURI);

        elementNameLabel = new JLabel("Element name:");
        editPanel.add(elementNameLabel);
        elementName = new JTextField();
        editPanel.add(elementName);

        attributeURILabel = new JLabel("Attribute URI:");
        editPanel.add(attributeURILabel);
        attributeURI = new JTextField();
        editPanel.add(attributeURI);

        attributeNameLabel = new JLabel("Attribute name:");
        editPanel.add(attributeNameLabel);
        attributeName = new JTextField();
        editPanel.add(attributeName);

        indexPathLabel = new JLabel("Index path");
        editPanel.add(indexPathLabel);
        indexPath = new JTextField();
        editPanel.add(indexPath);

        selectedElementNamesLabel = new JLabel("Element name(s)");
        editPanel.add(selectedElementNamesLabel);
        selectedElementNames = new JTextField();
        selectedElementNames.setToolTipText("Format: [namespaceURI] eltname, [namespaceURI2] eltname2..."
                                            + " (e.g.: title, graphic)");
        editPanel.add(selectedElementNames);

        metadataKeyNameLabel = new JLabel("Metadata key name:");
        editPanel.add(metadataKeyNameLabel);
        metadataKeyName = new JTextField();
        editPanel.add(metadataKeyName);

        analyzerClassNameLabel = new JLabel("Analyzer class");
        editPanel.add(analyzerClassNameLabel);
        analyzerClassName = new JTextField();
        editPanel.add(analyzerClassName);

        JPanel checkBoxPanel = new JPanel(new StackLayout());
        keySorted = new JCheckBox("Key sorted");
        checkBoxPanel.add(keySorted);
        uniqueKeys = new JCheckBox("Unique keys");
        checkBoxPanel.add(uniqueKeys);
        uniqueKeys.addActionListener(new ActionListener() {
                                         public void actionPerformed(ActionEvent dummy) {
                                             concurrent.setEnabled(
                                                 !uniqueKeys.isSelected()
                                             );
                                         }
                                     }
                                    );
        concurrent = new JCheckBox("Concurrent index");
        checkBoxPanel.add(concurrent);
        concurrent.addActionListener(new ActionListener() {
                                         public void actionPerformed(ActionEvent dummy) {
                                             uniqueKeys.setEnabled(
                                                 !concurrent.isSelected() &&
                                                 (
                                                     getIndexType((String) indexTypeCombo.getSelectedItem()) !=
                                                     XhiveIndexIf.FULL_TEXT_INDEX
                                                 )
                                             );
                                         }
                                     }
                                    );
        indexAllText = new JCheckBox("Index all text", false);
        checkBoxPanel.add(indexAllText);
        includeAttributes = new JCheckBox("Include attributes", false);
        checkBoxPanel.add(includeAttributes);
        supportPhrases = new JCheckBox("Support phrases", false);
        checkBoxPanel.add(supportPhrases);
        saToLowerCase = new JCheckBox("convert terms to lowercase (only for standardanalyzer)", false);
        checkBoxPanel.add(saToLowerCase);
        saFilterStopWords = new JCheckBox("filter english stop words (only for standardanalyzer)", false);
        checkBoxPanel.add(saFilterStopWords);

        // Defaults
        saFilterStopWords.setSelected(true);
        saToLowerCase.setSelected(true);
        supportPhrases.setSelected(true);

        JPanel fieldsPanel = new JPanel(new BorderLayout());

        fieldsPanel.add(editPanel, BorderLayout.NORTH);
        fieldsPanel.add(checkBoxPanel, BorderLayout.SOUTH);

        setFields(getIndexType((String) indexTypeCombo.getSelectedItem()));
        setWaitText("Adding index...");
        if (originalIndexName != null) {
            setIndex(originalIndexName);
        }
        return fieldsPanel;
    }

    @Override
    protected boolean performAction() throws Exception {
        if (dialogType == EDIT_INDEX && indexList.getIndex(originalIndexName) != null) {
            indexList.removeIndex(indexList.getIndex(originalIndexName));
        }
        switch (getIndexType((String) indexTypeCombo.getSelectedItem())) {
        case XhiveIndexIf.LIBRARY_ID_INDEX:
            indexList.addLibraryIdIndex(indexName.getText(), getOptions());
            break;
        case XhiveIndexIf.LIBRARY_NAME_INDEX:
            indexList.addLibraryNameIndex(indexName.getText(), getOptions());
            break;
        case XhiveIndexIf.ID_ATTRIBUTE_INDEX:
            indexList.addIdAttributeIndex(indexName.getText(), getOptions());
            break;
        case XhiveIndexIf.VALUE_INDEX:
            indexList.addValueIndex(indexName.getText(), elementURI.getText(), elementName.getText(),
                                    attributeURI.getText(), attributeName.getText(), getOptions());
            break;
        case XhiveIndexIf.ELEMENT_NAME_INDEX:
            indexList.addElementNameIndex(indexName.getText(), getOptions());
            break;
        case XhiveIndexIf.SELECTED_ELEMENT_NAMES_INDEX:
            StringTokenizer tokenizer = new StringTokenizer(selectedElementNames.getText(), ",");
            String[] elements = new String[tokenizer.countTokens()];
            while (tokenizer.hasMoreTokens()) {
                elements[tokenizer.countTokens() - 1] = tokenizer.nextToken().trim();
            }
            indexList.addElementNameIndex(indexName.getText(), elements, getOptions());
            break;
        case XhiveIndexIf.FULL_TEXT_INDEX:
            indexList.addFullTextIndex(indexName.getText(), elementURI.getText(), elementName.getText(),
                                       attributeURI.getText(), attributeName.getText(), analyzerClassName.getText(),
                                       getOptions());
            break;
        case XhiveIndexIf.METADATA_VALUE_INDEX:
            String keyName1 = metadataKeyName.getText();
            if (keyName1 != null && keyName1.length() == 0) keyName1 = null;
            indexList.addMetadataValueIndex(indexName.getText(), keyName1, getOptions());
            break;
        case XhiveIndexIf.METADATA_FULL_TEXT_INDEX:
            String keyName2 = metadataKeyName.getText();
            if (keyName2 != null && keyName2.length() == 0) keyName2 = null;
            indexList.addMetadataFullTextIndex(indexName.getText(), keyName2, analyzerClassName.getText(), getOptions());
            break;
        case XhiveIndexIf.PATH_VALUE_INDEX:
            String path = indexPath.getText();
            indexList.addPathValueIndex(indexName.getText(), path, getOptions());
            break;
        }
        return true;
    }

    private final boolean hasOption(int options, int option) {
        return ((options & option) == option);
    }

    protected void setIndex(String name) {
        XhiveIndexIf index = indexList.getIndex(name);
        indexTypeCombo.setSelectedItem(getIndexName(index.getType()));
        indexName.setText(index.getName());
        int options = index.getOptions();
        keySorted.setSelected(hasOption(options, XhiveIndexIf.KEY_SORTED));
        uniqueKeys.setSelected(hasOption(options, XhiveIndexIf.UNIQUE_KEYS));
        concurrent.setSelected(hasOption(options, XhiveIndexIf.CONCURRENT));
        supportPhrases.setSelected(hasOption(options, XhiveIndexIf.FTI_SUPPORT_PHRASES));
        indexAllText.setSelected(hasOption(options, XhiveIndexIf.FTI_GET_ALL_TEXT));
        includeAttributes.setSelected(hasOption(options, XhiveIndexIf.FTI_INCLUDE_ATTRIBUTES));
        saFilterStopWords.setSelected(hasOption(options, XhiveIndexIf.FTI_SA_FILTER_ENGLISH_STOP_WORDS));
        saToLowerCase.setSelected(hasOption(options, XhiveIndexIf.FTI_SA_ADJUST_TO_LOWERCASE));
        String definition = index.toString();
        // Analyze the definition String
        switch (index.getType()) {
        case XhiveIndexIf.FULL_TEXT_INDEX:
            String ftiDefinition = definition.substring(definition.lastIndexOf('[') + 1, definition.lastIndexOf(']'));
            StringTokenizer ftiDefinitionTokens = new StringTokenizer(ftiDefinition, ",");
            analyzerClassName.setText(getNextTokenValue(ftiDefinitionTokens));
            // no break-->handling of full text indexes is partially the same as that of value indexes
        case XhiveIndexIf.VALUE_INDEX:
            // Strip [  .]

            definition = definition.substring(definition.indexOf('[') + 1, definition.indexOf(']'));
            StringTokenizer definitionTokens = new StringTokenizer(definition, ",");
            elementURI.setText(getNextTokenValue(definitionTokens));
            elementName.setText(getNextTokenValue(definitionTokens));
            attributeURI.setText(getNextTokenValue(definitionTokens));
            attributeName.setText(getNextTokenValue(definitionTokens));
            valueIndexTypeCombo.setSelectedItem(getValueIndexTypeName(index.getOptions() & XhiveIndexIf.TYPE_MASK));
            break;
        case XhiveIndexIf.SELECTED_ELEMENT_NAMES_INDEX:
            // Strip [...]

            definition = definition.substring(definition.indexOf('[') + 1, definition.indexOf(']'));
            selectedElementNames.setText(definition.replace('\"', ' ').trim());
            break;
        case XhiveIndexIf.PATH_VALUE_INDEX:
            definition = definition.substring(definition.indexOf('[') + 1, definition.lastIndexOf(']'));
            indexPath.setText(definition);
            break;
        default:
            break;
        }
    }

    private String getNextTokenValue(StringTokenizer definitionTokens) {
        // Get the next token
        String value = definitionTokens.nextToken();
        // Trim spaces
        value = value.trim();
        if (value.equals("null")) {
            return null;
        }
        // Trim quotes
        value = value.replace('\"', ' ');
        value = value.trim();
        return value;
    }

    private static final String NODE_FILTER_INDEX_NAME = "Node filter index";
    private static final String LIBRARY_ID_INDEX_NAME = "Library Id index";
    private static final String LIBRARY_NAME_INDEX_NAME = "Library name index";
    private static final String ID_ATTRIBUTE_INDEX_NAME = "Attribute Id index";
    private static final String VALUE_INDEX_NAME = "Value index";
    private static final String ELEMENT_NAME_INDEX_NAME = "Element name index";
    private static final String SELECTED_ELEMENT_NAMES_INDEX = "Selected element name index";
    private static final String FULL_TEXT_INDEX_NAME = "Full text index";
    private static final String METADATA_VALUE_INDEX_NAME = "Metadata value index";
    private static final String METADATA_FULL_TEXT_INDEX_NAME = "Metadata full text index";
    private static final String PATH_VALUE_INDEX_NAME = "Path value index";
    private static final String UNKNOWN_INDEX_NAME = "Unknown index type";

    private static final String STRING_VALUE_INDEX_TYPE = "String";
    private static final String INT_VALUE_INDEX_TYPE = "Int";
    private static final String LONG_VALUE_INDEX_TYPE = "Long";
    private static final String FLOAT_VALUE_INDEX_TYPE = "Float";
    private static final String DOUBLE_VALUE_INDEX_TYPE = "Double";
    private static final String DATE_VALUE_INDEX_TYPE = "Date";
    private static final String DATE_TIME_VALUE_INDEX_TYPE = "Date Time";
    private static final String DAY_TIME_DURATION_VALUE_INDEX_TYPE = "Day Time Duration";
    private static final String YEAR_MONTH_DURATION_VALUE_INDEX_TYPE = "Year Month Duration";
    private static final String UNKNOWN_VALUE_INDEX_TYPE = "Unknown value index type";

    private static String getValueIndexTypeName(int valueIndexType) {
        switch (valueIndexType) {
        case XhiveIndexIf.TYPE_STRING:
            return STRING_VALUE_INDEX_TYPE;
        case XhiveIndexIf.TYPE_INT:
            return INT_VALUE_INDEX_TYPE;
        case XhiveIndexIf.TYPE_LONG:
            return LONG_VALUE_INDEX_TYPE;
        case XhiveIndexIf.TYPE_FLOAT:
            return FLOAT_VALUE_INDEX_TYPE;
        case XhiveIndexIf.TYPE_DOUBLE:
            return DOUBLE_VALUE_INDEX_TYPE;
        case XhiveIndexIf.TYPE_DATE:
            return DATE_VALUE_INDEX_TYPE;
        case XhiveIndexIf.TYPE_DATE_TIME:
            return DATE_TIME_VALUE_INDEX_TYPE;
        case XhiveIndexIf.TYPE_DAY_TIME_DURATION:
            return DAY_TIME_DURATION_VALUE_INDEX_TYPE;
        case XhiveIndexIf.TYPE_YEAR_MONTH_DURATION:
            return YEAR_MONTH_DURATION_VALUE_INDEX_TYPE;
        default:
            return UNKNOWN_VALUE_INDEX_TYPE;
        }
    }

    private static int getValueIndexType(String valueIndexTypeName) {
        if (valueIndexTypeName.equals(STRING_VALUE_INDEX_TYPE)) {
            return XhiveIndexIf.TYPE_STRING;
        } else if (valueIndexTypeName.equals(INT_VALUE_INDEX_TYPE)) {
            return XhiveIndexIf.TYPE_INT;
        } else if (valueIndexTypeName.equals(LONG_VALUE_INDEX_TYPE)) {
            return XhiveIndexIf.TYPE_LONG;
        } else if (valueIndexTypeName.equals(FLOAT_VALUE_INDEX_TYPE)) {
            return XhiveIndexIf.TYPE_FLOAT;
        } else if (valueIndexTypeName.equals(DOUBLE_VALUE_INDEX_TYPE)) {
            return XhiveIndexIf.TYPE_DOUBLE;
        } else if (valueIndexTypeName.equals(DATE_VALUE_INDEX_TYPE)) {
            return XhiveIndexIf.TYPE_DATE;
        } else if (valueIndexTypeName.equals(DATE_TIME_VALUE_INDEX_TYPE)) {
            return XhiveIndexIf.TYPE_DATE_TIME;
        } else if (valueIndexTypeName.equals(DAY_TIME_DURATION_VALUE_INDEX_TYPE)) {
            return XhiveIndexIf.TYPE_DAY_TIME_DURATION;
        } else if (valueIndexTypeName.equals(YEAR_MONTH_DURATION_VALUE_INDEX_TYPE)) {
            return XhiveIndexIf.TYPE_YEAR_MONTH_DURATION;
        } else {
            return -1;
        }
    }

    public static String getIndexName(int indexType) {
        switch (indexType) {
        case XhiveIndexIf.NODE_FILTER_INDEX:
            return NODE_FILTER_INDEX_NAME;
        case XhiveIndexIf.LIBRARY_ID_INDEX:
            return LIBRARY_ID_INDEX_NAME;
        case XhiveIndexIf.LIBRARY_NAME_INDEX:
            return LIBRARY_NAME_INDEX_NAME;
        case XhiveIndexIf.ID_ATTRIBUTE_INDEX:
            return ID_ATTRIBUTE_INDEX_NAME;
        case XhiveIndexIf.VALUE_INDEX:
            return VALUE_INDEX_NAME;
        case XhiveIndexIf.ELEMENT_NAME_INDEX:
            return ELEMENT_NAME_INDEX_NAME;
        case XhiveIndexIf.SELECTED_ELEMENT_NAMES_INDEX:
            return SELECTED_ELEMENT_NAMES_INDEX;
        case XhiveIndexIf.FULL_TEXT_INDEX:
            return FULL_TEXT_INDEX_NAME;
        case XhiveIndexIf.METADATA_VALUE_INDEX:
            return METADATA_VALUE_INDEX_NAME;
        case XhiveIndexIf.METADATA_FULL_TEXT_INDEX:
            return METADATA_FULL_TEXT_INDEX_NAME;
        case XhiveIndexIf.PATH_VALUE_INDEX:
            return PATH_VALUE_INDEX_NAME;
        default:
            return UNKNOWN_INDEX_NAME;
        }
    }

    private static int getIndexType(String indexName) {
        if (indexName.equals(NODE_FILTER_INDEX_NAME)) {
            return XhiveIndexIf.NODE_FILTER_INDEX;
        } else if (indexName.equals(LIBRARY_ID_INDEX_NAME)) {
            return XhiveIndexIf.LIBRARY_ID_INDEX;
        } else if (indexName.equals(LIBRARY_NAME_INDEX_NAME)) {
            return XhiveIndexIf.LIBRARY_NAME_INDEX;
        } else if (indexName.equals(ID_ATTRIBUTE_INDEX_NAME)) {
            return XhiveIndexIf.ID_ATTRIBUTE_INDEX;
        } else if (indexName.equals(VALUE_INDEX_NAME)) {
            return XhiveIndexIf.VALUE_INDEX;
        } else if (indexName.equals(ELEMENT_NAME_INDEX_NAME)) {
            return XhiveIndexIf.ELEMENT_NAME_INDEX;
        } else if (indexName.equals(SELECTED_ELEMENT_NAMES_INDEX)) {
            return XhiveIndexIf.SELECTED_ELEMENT_NAMES_INDEX;
        } else if (indexName.equals(FULL_TEXT_INDEX_NAME)) {
            return XhiveIndexIf.FULL_TEXT_INDEX;
        } else if (indexName.equals(METADATA_VALUE_INDEX_NAME)) {
            return XhiveIndexIf.METADATA_VALUE_INDEX;
        } else if (indexName.equals(METADATA_FULL_TEXT_INDEX_NAME)) {
            return XhiveIndexIf.METADATA_FULL_TEXT_INDEX;
        } else if (indexName.equals(PATH_VALUE_INDEX_NAME)) {
            return XhiveIndexIf.PATH_VALUE_INDEX;
        } else {
            return -1;
        }
    }

    private int getOptions() {
        int result = 0;
        int type = getIndexType((String)indexTypeCombo.getSelectedItem());
        if (keySorted.isSelected()) {
            result |= XhiveIndexIf.KEY_SORTED;
        }
        if (uniqueKeys.isSelected()) {
            result |= XhiveIndexIf.UNIQUE_KEYS;
        }
        if (concurrent.isSelected()) {
            result |= XhiveIndexIf.CONCURRENT;
        }
        if (type == XhiveIndexIf.VALUE_INDEX || type == XhiveIndexIf.METADATA_VALUE_INDEX) {
            result |= getValueIndexType((String) valueIndexTypeCombo.getSelectedItem());
        }
        if (type == XhiveIndexIf.FULL_TEXT_INDEX) {
            if (indexAllText.isSelected()) {
                result |= XhiveIndexIf.FTI_GET_ALL_TEXT;
            }
            if (includeAttributes.isSelected()) {
                result |= XhiveIndexIf.FTI_INCLUDE_ATTRIBUTES;
            }
        }
        if (type == XhiveIndexIf.FULL_TEXT_INDEX || type == XhiveIndexIf.METADATA_FULL_TEXT_INDEX) {
            if (supportPhrases.isSelected()) {
                result |= XhiveIndexIf.FTI_SUPPORT_PHRASES;
            }
            if (saToLowerCase.isSelected()) {
                result |= XhiveIndexIf.FTI_SA_ADJUST_TO_LOWERCASE;
            }
            if (saFilterStopWords.isSelected()) {
                result |= XhiveIndexIf.FTI_SA_FILTER_ENGLISH_STOP_WORDS;
            }
        }
        return result;
    }

    private void setFields(int indexType) {
        boolean libraryIndex = false;
        boolean valueIndex = false;
        boolean elemAndAttr = false;
        boolean pathIndex = false;
        boolean selectedElementNameIndex = false;
        boolean fullTextIndex = false;
        boolean metadataIndex = false;
        switch (indexType) {
        case XhiveIndexIf.LIBRARY_ID_INDEX:
        case XhiveIndexIf.LIBRARY_NAME_INDEX:
            libraryIndex = true;
            break;
        case XhiveIndexIf.ELEMENT_NAME_INDEX:
        case XhiveIndexIf.ID_ATTRIBUTE_INDEX:
            break;
        case XhiveIndexIf.VALUE_INDEX:
            valueIndex = true;
            elemAndAttr = true;
            break;
        case XhiveIndexIf.SELECTED_ELEMENT_NAMES_INDEX:
            selectedElementNameIndex = true;
            break;
        case XhiveIndexIf.FULL_TEXT_INDEX:
            elemAndAttr = true;
            fullTextIndex = true;
            break;
        case XhiveIndexIf.METADATA_VALUE_INDEX:
            valueIndex = true;
            metadataIndex = true;
            break;
        case XhiveIndexIf.METADATA_FULL_TEXT_INDEX:
            fullTextIndex = true;
            metadataIndex = true;
            break;
        case XhiveIndexIf.PATH_VALUE_INDEX:
            pathIndex = true;
            break;
        }

        if (dialogType == ADD_INDEX) {
            indexName.setText(getIndexName(indexType));
        }

        elementURI.setVisible(elemAndAttr);
        elementURILabel.setVisible(elemAndAttr);
        elementName.setVisible(elemAndAttr);
        elementNameLabel.setVisible(elemAndAttr);
        attributeURI.setVisible(elemAndAttr);
        attributeURILabel.setVisible(elemAndAttr);
        attributeName.setVisible(elemAndAttr);
        attributeNameLabel.setVisible(elemAndAttr);

        indexPathLabel.setVisible(pathIndex);
        indexPath.setVisible(pathIndex);

        metadataKeyNameLabel.setVisible(metadataIndex);
        metadataKeyName.setVisible(metadataIndex);

        valueIndexTypeLabel.setVisible(valueIndex);
        valueIndexTypeCombo.setVisible(valueIndex);

        uniqueKeys.setEnabled(!fullTextIndex);
        keySorted.setEnabled(!fullTextIndex);
        //keySorted.setSelected(fullTextIndex);
        // Sort keys always by default
        keySorted.setSelected(true);
        concurrent.setVisible(!libraryIndex);
        indexAllText.setVisible(fullTextIndex && !metadataIndex);
        includeAttributes.setVisible(fullTextIndex && !metadataIndex);
        if (analyzerClassName.getText().equals("")) {
            supportPhrases.setVisible(fullTextIndex);
            saFilterStopWords.setVisible(fullTextIndex);
        }
        saToLowerCase.setVisible(fullTextIndex);
        analyzerClassNameLabel.setVisible(fullTextIndex);
        analyzerClassName.setVisible(fullTextIndex);
        selectedElementNames.setVisible(selectedElementNameIndex);
        selectedElementNamesLabel.setVisible(selectedElementNameIndex);
        pack();
    }

    private void setSelected(JCheckBox checkBox, boolean selected) {
        EventListener[] listeners = checkBox.getListeners(ItemListener.class);
        for (int i = 0; i < listeners.length; i++) {
            checkBox.removeItemListener((ItemListener) listeners[i]);
        }
        checkBox.setSelected(selected);
        for (int i = 0; i < listeners.length; i++) {
            checkBox.addItemListener((ItemListener) listeners[i]);
        }
    }
}

package com.xhive.adminclient.panes;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.plaf.BorderUIResource;

import com.xhive.adminclient.dialogs.XhiveMessageDialog;

import org.w3c.dom.DOMConfiguration;

public class XhiveDOMConfigurationPanel extends JPanel implements ActionListener {

  private static String[] normalizeProperties = new String[]{"cdata-sections", "comments",
                                                           "datatype-normalization",
                                                           "entities", "infoset", "namespaces",
                                                           "namespace-declarations",
                                                           "split-cdata-sections", "validate", "validate-if-schema",
                                                           "element-content-whitespace"};

  private static String[] writerProperties = new String[]{"cdata-sections", "comments",
                                                        "datatype-normalization", "discard-default-content",
                                                        "entities", "infoset", "namespaces",
                                                        "namespace-declarations",
                                                        "split-cdata-sections", "validate", "validate-if-schema",
                                                        "element-content-whitespace", "format-pretty-print",
                                                        "xml-declaration"};

  private static String[] builderProperties = new String[]{"cdata-sections", "comments",
                                                         "datatype-normalization",
                                                         "entities", "infoset", "namespaces",
                                                         "namespace-declarations",
                                                         "split-cdata-sections", "validate", "validate-if-schema",
                                                         "element-content-whitespace",
                                                         "charset-overrides-xml-encoding",
                                                         "supported-media-types-only",
                                                         "xhive-store-schema", "xhive-store-schema-only-internal-subset",
                                                         "xhive-ignore-catalog", "xhive-psvi",
                                                         "xhive-sync-features"};

  public static final String PARAMETER_SCHEMA_TYPE           = "schema-type";
  public static final String PARAMETER_SCHEMA_LOCATION       = "schema-location";

  private DOMConfiguration configuration;
  private String[] propertyNames;
  private JCheckBox checkBoxes[];
  private JTextField schemaLocationField;
  private JRadioButton xmlSchemaButton;
  private JRadioButton dtdButton;

  public XhiveDOMConfigurationPanel(String propertiesTitle, String[] featureNames, DOMConfiguration configuration) {
    this.propertyNames = featureNames;
    this.configuration = configuration;
    init(propertiesTitle);
  }

  public static XhiveDOMConfigurationPanel getNormalizePanel(DOMConfiguration config) {
    return new XhiveDOMConfigurationPanel("DOM normalizeDocument properties", normalizeProperties, config);
  }

  public static XhiveDOMConfigurationPanel getWriterPanel(DOMConfiguration config) {
    return new XhiveDOMConfigurationPanel("LSSerializer properties", writerProperties, config);
  }

  public static XhiveDOMConfigurationPanel getBuilderPanel(DOMConfiguration config) {
    return new XhiveDOMConfigurationPanel("LSParser properties", builderProperties, config);
  }

  public HashMap<String,Boolean> getFeatureMap() {
    HashMap<String,Boolean> featureMap = new HashMap<String,Boolean>();
    for (int i = 0; i < propertyNames.length; i++) {
      featureMap.put(propertyNames[i], Boolean.valueOf(checkBoxes[i].isSelected()));
    }
    return featureMap;
  }

  public String[] getFeatureList() {
    return propertyNames;
  }

  public DOMConfiguration getConfig() {
    return configuration;
  }

  /**
   * Options are updated automatic when a feature changes
   */
  public void actionPerformed(ActionEvent event) {
    try {
      // Find the nr of the updated check box
      int index = -1;
      for (int i = 0; i < propertyNames.length; i++) {
        if (event.getSource() == checkBoxes[i]) {
          index = i;
          break;
        }
      }
      // Update the feature
      if (index != -1) {
        configuration.setParameter(propertyNames[index], checkBoxes[index].isSelected() ? Boolean.TRUE : Boolean.FALSE);
      } else {
        if (event.getSource() == dtdButton) {
          configuration.setParameter(PARAMETER_SCHEMA_TYPE, dtdButton.getActionCommand());
        }
        if (event.getSource() == xmlSchemaButton) {
          configuration.setParameter(PARAMETER_SCHEMA_TYPE, xmlSchemaButton.getActionCommand());
        }
      }
      // Update the state of the other checkboxes
    } catch (Exception e) {
      XhiveMessageDialog.showException(e);
    } finally {
      // Update the state of all the checkboxes
      for (int i = 0; i < propertyNames.length; i++) {
        checkBoxes[i].setSelected(configuration.getParameter(propertyNames[i]) == Boolean.TRUE);
      }
    }
  }

  protected void init(String propertiesTitle) {
    JPanel propertiesPanel = new JPanel(new BorderLayout());
    propertiesPanel.setBorder(new BorderUIResource.TitledBorderUIResource(propertiesTitle));
    JPanel panel1 = new JPanel();
    panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
    JPanel panel2 = new JPanel();
    panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
    setLayout(new BorderLayout());
    propertiesPanel.add(panel1, BorderLayout.WEST);
    propertiesPanel.add(panel2, BorderLayout.EAST);
    checkBoxes = new JCheckBox[propertyNames.length];
    int firstHalf = propertyNames.length / 2;
    add(propertiesPanel, BorderLayout.CENTER);
    createCheckBoxes(panel1, 0, firstHalf);
    createCheckBoxes(panel2, firstHalf, propertyNames.length);
    add(createParameterPanel(), BorderLayout.SOUTH);
  }

  private void createCheckBoxes(Container container, int start, int end) {
    for (int i = start; i < end; i++) {
      boolean value = configuration.getParameter(propertyNames[i]) == Boolean.TRUE;
      checkBoxes[i] = new JCheckBox(propertyNames[i], value);
      // Check if the feature can be changed
      checkBoxes[i].setEnabled(configuration.canSetParameter(propertyNames[i], value ? Boolean.FALSE : Boolean.TRUE));
      checkBoxes[i].addActionListener(this);
      container.add(checkBoxes[i]);
    }
  }

  private JPanel createParameterPanel() {
    JPanel parameterPanel = new JPanel(new BorderLayout());
    JPanel schemaTypePanel = new JPanel();
    schemaTypePanel.setLayout(new BoxLayout(schemaTypePanel, BoxLayout.Y_AXIS));
    schemaTypePanel.setBorder(new BorderUIResource.TitledBorderUIResource("schema-type"));
    xmlSchemaButton = new JRadioButton("http://www.w3.org/2001/XMLSchema");
    xmlSchemaButton.addActionListener(this);
    dtdButton = new JRadioButton("http://www.w3.org/TR/REC-xml");
    dtdButton.addActionListener(this);

    ButtonGroup group = new ButtonGroup();
    group.add(xmlSchemaButton);
    group.add(dtdButton);
    schemaTypePanel.add(xmlSchemaButton);
    schemaTypePanel.add(dtdButton);
    parameterPanel.add(schemaTypePanel, BorderLayout.NORTH);
    JPanel schemaLocationPanel = new JPanel(new BorderLayout());
    schemaLocationPanel.setBorder(new BorderUIResource.TitledBorderUIResource("schema-location"));
    schemaLocationField = new JTextField();
    schemaLocationPanel.add(schemaLocationField, BorderLayout.CENTER);
    schemaLocationField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent ke) {
        try {
          String schemaLocation = schemaLocationField.getText();
          configuration.setParameter(PARAMETER_SCHEMA_LOCATION, schemaLocation);
        } catch (Exception e) {
          XhiveMessageDialog.showException(e);
        }
      }
    });
    parameterPanel.add(schemaLocationPanel, BorderLayout.SOUTH);
    String schemaType = (String) configuration.getParameter(PARAMETER_SCHEMA_TYPE);
    if (xmlSchemaButton.getActionCommand().equals(schemaType)) {
      xmlSchemaButton.setSelected(true);
    } else if (dtdButton.getActionCommand().equals(schemaType)) {
      dtdButton.setSelected(true);
    }
    schemaLocationField.setText((String) configuration.getParameter(PARAMETER_SCHEMA_LOCATION));
    return parameterPanel;
  }

}

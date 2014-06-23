package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.PivotConfiguration;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

public class QAStepExportWizard extends Dialog implements QAStepExportWizardView {


//    private WizardModel wizardModel;
//    private WizardController wizardController;

    private QAStepExportWizardPresenter presenter;
        
    private JPanel cardPanel;
    private CardLayout cardLayout;
            
    private JButton backButton;
    private JButton nextButton;
    private JButton finishButton;
    private JButton cancelButton;
    private List<JPanel> wizardModel;
    private int returnCode;
    private JCheckBox csvFormat;
    private JCheckBox shapeFileFormat;
    
    private MessagePanel messagePanel;
    private TextArea rowFilter;   
    private int panelPosition = 0;
    private List<ProjectionShapeFile> projectionShapeFileList = new ArrayList<ProjectionShapeFile>();
    private ComboBox projectionShapeFile;

    private JCheckBox pivotData;

    private ListWidget pivotAvailableFields;
    private ListWidget pivotRowLabels;
    private ListWidget pivotExtraFields;
    private ListWidget pivotColumnLabel;
    private ListWidget pivotValue;
    private ComboBox pivotSummarizeBy;

    private String[] validCountyColumns = new String[] { "fips", "region_cd" };
    private String[] validStateColumns = new String[] { "fipsst" };
    private String[] validPointColumns = new String[] { "longitude", "latitude", "lon", "lat", "xloc", "yloc" };

    private Map<String,Location> columnnNameListLocationMap = new LinkedHashMap<String,Location>();

    private ProjectionShapeFile[] projectionShapeFiles;

    private boolean canceled = false;

    private boolean isShapefileCapable;

    private boolean ignoreShapeFileFunctionality;
    
    private enum Location {
        AVAILABLE_FIELDS, 
        ROW_LABEL,
        EXTRA_FIELD, 
        COLUMN_LABEL, 
        VALUE
    }
    
    
    public QAStepExportWizard(EmfConsole parent) {
        super("Export QA Step Results 2 " , parent);
        super.setSize(new Dimension(550, 450));
        super.center();
        setModal(true);
        this.wizardModel = new ArrayList<JPanel>();
    }

    public void display(QAStepResult qaStepResult) {
        //load projection shapefiles objects into baseShapeFileMap
        String[] availableColumns = null;
        String type = "";
        try {
            isShapefileCapable = presenter.isShapeFileCapable(qaStepResult);
            ignoreShapeFileFunctionality = presenter.ignoreShapeFileFunctionality();
            projectionShapeFiles = presenter.getProjectionShapeFiles();
            availableColumns = presenter.getTableColumns("emissions." + qaStepResult.getTable());
            //determine the appropriate base shapefile/projection to use
            type = getProjectionType(availableColumns);
            //load list location map to help keep track of which list contains which columns
            for (String column : availableColumns) 
                columnnNameListLocationMap.put(column, Location.AVAILABLE_FIELDS);
        } catch (EmfException e) {
            e.printStackTrace();
            messagePanel.setMessage(e.getMessage());
        }
        for (ProjectionShapeFile projectionShapeFile : projectionShapeFiles) {
            if (projectionShapeFile.getType().equals(type)) {
                projectionShapeFileList.add(projectionShapeFile);
            }
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(mainPanel());

        panel.add(getButtonPanel());
        super.getContentPane().add(panel);
        
        
        
        registerWizardPanel(step1Panel(availableColumns));
        
//        registerWizardPanel(filterPanel(new String[] {"fips", "plantid", "pointid", "stackid", "segment", "scc", "poll", "ann_emis"}));
//        registerWizardPanel(pivotPanel(new String[] {"fips", "plantid", "pointid", "stackid", "segment", "scc", "poll", "ann_emis"}));
        registerWizardPanel(pivotPanel(availableColumns));
        registerWizardPanel(chooseGeometryPanel());
        
        super.display();

//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//
//        panel.add(mainPanel());
//        panel.add(buttonsPanel());
//
//        super.getContentPane().add(panel);
//        super.display();
    }

    public void observe(QAStepExportWizardPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel mainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(1, 1));
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.NORTH);
        panel.add(new Label("empty", "  "), BorderLayout.LINE_START);
        panel.add(getCardPanel(),BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel step1Panel(String[] availableColumns) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(exportTypesPanel(), BorderLayout.NORTH);
        panel.add(filterPanel(availableColumns), BorderLayout.CENTER);
        return panel;
    }

    private String getProjectionType(String[] columns) {
        boolean hasFipsCol = false;
        boolean hasFipsStCol = false;
        boolean hasLatCol = false;
        boolean hasLonCol = false;
        boolean hasLatitudeCol = false;
        boolean hasLongitudeCol = false;
        boolean hasXLocCol = false;
        boolean hasYLocCol = false;

        String colName = "";
        for (int i = 0; i < columns.length; i++) {
            colName = columns[i].toLowerCase();
            if (colName.equals("fips") || colName.equals("region_cd")) {
                hasFipsCol = true;
            } else if (colName.equals("fipsst")) {
                hasFipsStCol = true;
            } else if (colName.equals("longitude")) {
                hasLongitudeCol = true;
            } else if (colName.equals("latitude")) {
                hasLatitudeCol = true;
            } else if (colName.equals("lon")) {
                hasLonCol = true;
            } else if (colName.equals("lat")) {
                hasLatCol = true;
            } else if (colName.equals("xloc")) {
                hasXLocCol = true;
            } else if (colName.equals("yloc")) {
                hasYLocCol = true;
            }
        }
        if (
                (hasLongitudeCol && hasLatitudeCol) || (hasLonCol && hasLatCol) || (hasXLocCol && hasYLocCol)
                    )
            return "point";
        else if (hasFipsCol)
            return "county";
        else if (hasFipsStCol)
            return "state";
        return "";
    }
    
    private void toggleButtons() {
        if (csvFormat.isSelected() && !shapeFileFormat.isSelected()){
            finishButton.setEnabled(true);
            nextButton.setEnabled(false);
        } else if (!csvFormat.isSelected() && !shapeFileFormat.isSelected()){
            finishButton.setEnabled(false);
            nextButton.setEnabled(false);
        } else if (csvFormat.isSelected() && shapeFileFormat.isSelected()){
            finishButton.setEnabled(false);
            nextButton.setEnabled(true);
        } else if (!csvFormat.isSelected() && shapeFileFormat.isSelected()){
            finishButton.setEnabled(false);
            nextButton.setEnabled(true);
        }
    }
    
    private JPanel exportTypesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Export Format:"));
        csvFormat = new JCheckBox("CSV");
        if (!isShapefileCapable || ignoreShapeFileFunctionality)
            csvFormat.setSelected(true);
        else
            csvFormat.setSelected(false);
        csvFormat.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                toggleButtons();
            }
        });
        panel.add(csvFormat,BorderLayout.NORTH);

        
        JPanel shapeFilePanel = new JPanel(new BorderLayout());
        shapeFileFormat = new JCheckBox("ShapeFile");
        shapeFileFormat.setSelected(false);
        if (!isShapefileCapable || ignoreShapeFileFunctionality)
            shapeFileFormat.setEnabled(false);
        shapeFileFormat.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                toggleButtons();
                if (shapeFileFormat.isSelected()){
                    pivotData.setEnabled(true);
                } else {
                    pivotData.setEnabled(false);
                }
            }
        });
        shapeFilePanel.add(shapeFileFormat, BorderLayout.WEST);
        
        pivotData = new JCheckBox("Pivot Data (i.e., make pollutant columns)");
        pivotData.setSelected(false);
        pivotData.setEnabled(false);
        shapeFilePanel.add(pivotData, BorderLayout.CENTER);

        panel.add(shapeFilePanel,BorderLayout.SOUTH);

    
        return panel;
    }
    
    private JPanel chooseGeometryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new Label("<html>Output Shapefile Template:<br/><br/>The QA Step output/result was found to contain the county fips code.  The folowing base shape files are available for use when creating your custom shapefile.</html>"), BorderLayout.NORTH);
        projectionShapeFile = new ComboBox(projectionShapeFileList.toArray(new ProjectionShapeFile[0]));
        if (projectionShapeFile.getItemCount() > 0)
            projectionShapeFile.setSelectedIndex(0);
        panel.add(projectionShapeFile, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel filterPanel(String[] availableColumns) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Output Filter:"));

        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel avaialbleColumnsPanel = new JPanel();
        avaialbleColumnsPanel.setLayout(new BorderLayout());
        
        ListWidget avaialbleColumnsCB = new ListWidget(availableColumns);
//        if (avaialbleColumnsCB.getItemCount() > 0)
//            avaialbleColumnsCB.setSelectedIndex(0);
        avaialbleColumnsCB.setEnabled(false);
        JScrollPane avaialbleColumnsScrollPane = new JScrollPane(avaialbleColumnsCB);
        avaialbleColumnsScrollPane.setPreferredSize(new Dimension(20, 100));
                                        
        rightPanel.add(new Label("<html>Available Columns<br/>to use in Row<br/>Filter:</html>"), BorderLayout.NORTH);
        rightPanel.add(avaialbleColumnsScrollPane, BorderLayout.CENTER);
        
        // Row Filter
        rowFilter = new TextArea("rowFilter", "", 35, 2);
        rowFilter.setToolTipText("<html>SQL WHERE clause used to filter QA Step output.<br/>For example to filter on a certain state,<br/>substring(fips,1,2) = '37'<br/>or<br/>fips like '37%'<html>");
        JScrollPane rowArea = new JScrollPane(rowFilter, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        leftPanel.add(new Label("Row Filter: "), BorderLayout.NORTH);
        leftPanel.add(rowArea,BorderLayout.CENTER);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        
        return panel;
    }

    protected ImageIcon createImageIcon(String path) {
        URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }
        messagePanel.setError("Could not find file: " + path);
        return null;
    }

    private JPanel pivotPanel(String[] availableColumns) {



        JPanel pivotAvailableFieldsPanel = new JPanel();
        pivotAvailableFieldsPanel.setLayout(new BoxLayout(pivotAvailableFieldsPanel, BoxLayout.Y_AXIS));

        pivotAvailableFields = new ListWidget(availableColumns);
        pivotAvailableFields.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane pivotAvailableFieldsPane = new JScrollPane(pivotAvailableFields);
        pivotAvailableFieldsPane.setPreferredSize(new Dimension(150, 75));
        pivotAvailableFields.setToolTipText("The column pollutants/species of the report.  Press select Add button to add to list.  Press Remove button to remove from the list.  To move the order of the columns, select the appropriate item and then press the Up or Down button to move the item.");
//        if (pollList != null && pollList.length > 0) {
//            for (String poll : pollList) {
//                leftPollListWidget.addElement(poll);
//            }
//        }
        pivotAvailableFieldsPanel.add(new JLabel("<html>Fields for Report:</html>"));
        pivotAvailableFieldsPanel.add(pivotAvailableFieldsPane);
//        Button removeLeftPollButton = new Button("Row Label>>", addRowLabelAction());
//        removeLeftPollButton.setMargin(new Insets(1, 2, 1, 2));
//        leftPollListPanel.add(removeLeftPollButton);

        JPanel mainButtonPanel = new JPanel();
        mainButtonPanel.setLayout(new BoxLayout(mainButtonPanel, BoxLayout.Y_AXIS));
        Button addRowLabelButton = new AddButton("Row Label>>", addRowLabelAction());
        addRowLabelButton.setMargin(new Insets(1, 2, 1, 2));
        Button addExtraFieldsButton = new AddButton("Extra Fields>>", addExtraFieldsAction());
        addExtraFieldsButton.setMargin(new Insets(1, 2, 1, 2));
        Button addColumnLabelButton = new AddButton("Column Label>>", addColumnLabelAction());
        addColumnLabelButton.setMargin(new Insets(1, 2, 1, 2));
        Button addValueButton = new AddButton("Value>>", addValueAction());
        addValueButton.setMargin(new Insets(1, 2, 1, 2));
        mainButtonPanel.add(addRowLabelButton);
        mainButtonPanel.add(addExtraFieldsButton);
        mainButtonPanel.add(addColumnLabelButton);
        mainButtonPanel.add(addValueButton);        
        mainButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel rowLabelsPanel = new JPanel();
        rowLabelsPanel.setLayout(new BoxLayout(rowLabelsPanel, BoxLayout.Y_AXIS));
        pivotRowLabels = new ListWidget(new String[] {});
        pivotRowLabels.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane pivotRowLabelsPane = new JScrollPane(pivotRowLabels);
        pivotRowLabelsPane.setPreferredSize(new Dimension(150, 75));
        pivotRowLabels.setToolTipText("The pollutants/species to exclude from the report.  Press select Exclude button to remove from the list.");
        rowLabelsPanel.add(new JLabel("<html>Row Labels:</html>"));
        rowLabelsPanel.add(pivotRowLabelsPane);
        Button removeRowLabelButton = new RemoveButton("Remove", removeRowLabelAction());
        removeRowLabelButton.setMargin(new Insets(1, 2, 1, 2));
        rowLabelsPanel.add(removeRowLabelButton);

        JPanel extraFieldsPanel = new JPanel();
        extraFieldsPanel.setLayout(new BoxLayout(extraFieldsPanel, BoxLayout.Y_AXIS));
        pivotExtraFields = new ListWidget(new String[] {});
        pivotExtraFields.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane pivotExtraFieldsPane = new JScrollPane(pivotExtraFields);
        pivotExtraFieldsPane.setPreferredSize(new Dimension(150, 75));
        extraFieldsPanel.add(new JLabel("<html>Extra Fields:</html>"));
        extraFieldsPanel.add(pivotExtraFieldsPane);
        Button removeExtraFieldButton = new RemoveButton("Remove", removeExtraFieldAction());
        removeExtraFieldButton.setMargin(new Insets(1, 2, 1, 2));
        extraFieldsPanel.add(removeExtraFieldButton);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        pivotColumnLabel = new ListWidget(new String[] {});
        pivotColumnLabel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane pivotColumnLabelPane = new JScrollPane(pivotColumnLabel);
        pivotColumnLabelPane.setPreferredSize(new Dimension(150, 20));
        Button removeColumnLabelButton = new AddButton("Remove", removeColumnLabelAction());
        removeColumnLabelButton.setMargin(new Insets(1, 2, 1, 2));
        pivotValue = new ListWidget(new String[] {});
        pivotValue.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane pivotValuePane = new JScrollPane(pivotValue);
        pivotValuePane.setPreferredSize(new Dimension(150, 20));
        Button removeValueButton = new AddButton("Remove", removeValueAction());
        removeValueButton.setMargin(new Insets(1, 2, 1, 2));
        pivotSummarizeBy = new ComboBox(new String[] {"Sum", "Count", "Avg", "Max", "Min"});
        rightPanel.add(new JLabel("<html>Column Label:</html>"));
        rightPanel.add(pivotColumnLabelPane);
        rightPanel.add(removeColumnLabelButton);
        rightPanel.add(new JLabel("<html>Value:</html>"));
        rightPanel.add(pivotValuePane);        
        rightPanel.add(removeValueButton);
        rightPanel.add(new JLabel("<html>Summarize Value By:</html>"));

        JPanel pivotSummarizeByPanel = new JPanel();
        pivotSummarizeByPanel.setLayout(new BorderLayout());
        pivotSummarizeByPanel.add(pivotSummarizeBy, BorderLayout.NORTH);

        
        rightPanel.add(pivotSummarizeByPanel);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


        
        //JPanel container = new JPanel(new FlowLayout());
        JPanel container = new JPanel();
        //invPanel.setMaximumSize( new Dimension(1500,30));
        container.setLayout( new BoxLayout(container, BoxLayout.X_AXIS));

        container.add(pivotAvailableFieldsPanel);
        container.add(mainButtonPanel);
        container.add(rowLabelsPanel);
        container.add(extraFieldsPanel);
        container.add(rightPanel);
        
//        container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        container.setBorder(BorderFactory.createTitledBorder("Pivot Configuration:"));

        return container;
    
    
    
    
    }

    private Action removeExtraFieldAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addToAvailableFields(pivotExtraFields);
            }
        };
    }

    private Action removeValueAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addToAvailableFields(pivotValue);
            }
        };
    }

    private Action removeColumnLabelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addToAvailableFields(pivotColumnLabel);
            }
        };
    }

    private void addToAvailableFields(ListWidget listWidget) {
        for (Object column : listWidget.getSelectedValues()) {
            columnnNameListLocationMap.put((String)column, Location.AVAILABLE_FIELDS);
            listWidget.removeElement(column);
            populateAvaibleFieldsList();
        }
    }

    private void addToOtherList(ListWidget listWidget, Location location) {
        for (Object column : pivotAvailableFields.getSelectedValues()) {
            columnnNameListLocationMap.put((String)column, location);
            pivotAvailableFields.removeElement(column);
        }
        listWidget.removeAllElements();
        for (Map.Entry<String, Location> entry : columnnNameListLocationMap.entrySet()) {
            if (entry.getValue() == location)
                listWidget.addElement(entry.getKey());
        }
    }

    private void populateAvaibleFieldsList() {
        
        pivotAvailableFields.removeAllElements();
        for (Map.Entry<String, Location> entry : columnnNameListLocationMap.entrySet()) {
            if (entry.getValue() == Location.AVAILABLE_FIELDS)
                pivotAvailableFields.addElement(entry.getKey());
        }
    }
    
    private Action removeRowLabelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addToAvailableFields(pivotRowLabels);
            }
        };
    }

    private Action addValueAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addToOtherList(pivotValue, Location.VALUE);
            }
        };
    }

    private Action addColumnLabelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addToOtherList(pivotColumnLabel, Location.COLUMN_LABEL);
            }
        };
    }

    private Action addRowLabelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addToOtherList(pivotRowLabels, Location.ROW_LABEL);
            }
        };
    }

    private Action addExtraFieldsAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addToOtherList(pivotExtraFields, Location.EXTRA_FIELD);
            }
        };
    }

    private JPanel getButtonPanel() {

         JPanel buttonPanel = new JPanel();
         Box buttonBox = new Box(BoxLayout.X_AXIS);

         backButton = new JButton(new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 clearMsg();
                 
                 //if going to pivot page, make sure user has the Pivot Checkbox selected, if not then
                 //skip this page in the wizard
                 if (panelPosition - 1 == 1 && shapeFileFormat.isSelected() && !pivotData.isSelected()) {
                     panelPosition--;
                 }
                 
                 if (panelPosition - 1 != 0) {
                     backButton.setEnabled(true);
                 } else {
                     backButton.setEnabled(false);
                 }
                 nextButton.setEnabled(true);
                 finishButton.setEnabled(false);
                 panelPosition--;
                 setCurrentPanel(panelPosition);
//                 exportFile();
             }
         });
         backButton.setText("Back");
         backButton.setEnabled(false);
         nextButton = new JButton(new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 clearMsg();

                 //if going to pivot page, make sure user has the Pivot Checkbox selected, if not then
                 //skip this page in the wizard
                 if (panelPosition + 1 == 1  && shapeFileFormat.isSelected() && !pivotData.isSelected()) {
                     panelPosition++;

                     //repopulate projection drop down to represent new columns/fields that were defined in the 
                     //pivoted version of this resultset, for example, might sum up to the state level, when 
                     //report is county based
                     List<String> pivotReportFieldList = new ArrayList<String>();
                     for (Map.Entry<String, Location> entry : columnnNameListLocationMap.entrySet()) {
                         pivotReportFieldList.add(entry.getKey());
                     }
                     String projectionType = getProjectionType(pivotReportFieldList.toArray(new String[0]));
                     repopulateProjectionShapefiles(projectionType);
                 }
                 
                 //if leaving pivot page, perform some basic validation to make sure all fields are populated 
                 //with the correct data
                 else if (panelPosition + 1 == 2) {
                     
                     if (pivotRowLabels.getAllElements().length == 0) {
                         messagePanel.setError("Missing row label fields.");
                         return;
                     }
                     
                     if (pivotColumnLabel.getAllElements().length == 0) {
                         messagePanel.setError("Missing column label.");
                         return;
                     }
                     
                     if (pivotValue.getAllElements().length == 0) {
                         messagePanel.setError("Missing value field.");
                         return;
                     }
                     
                     //repopulate projection drop down to represent new columns/fields that were defined in the 
                     //pivoted version of this resultset, for example, might sum up to the state level, when 
                     //report is county based
                     List<String> pivotReportFieldList = new ArrayList<String>();
                     for (Map.Entry<String, Location> entry : columnnNameListLocationMap.entrySet()) {
                         if (entry.getValue() == Location.ROW_LABEL
                                 || entry.getValue() == Location.EXTRA_FIELD
                                 || entry.getValue() == Location.COLUMN_LABEL
                                 || entry.getValue() == Location.VALUE)
                             pivotReportFieldList.add(entry.getKey());
                     }
                     String projectionType = getProjectionType(pivotReportFieldList.toArray(new String[0]));
                     repopulateProjectionShapefiles(projectionType);
//                     setRenderer(new ComboBoxRenderer(defaultLabel));

                     //no issues then goto to next panel in wizard...
                 }
                 
                 

                 if (panelPosition + 1 == wizardModel.size() - 1) {
                     nextButton.setEnabled(false);
                     finishButton.setEnabled(true);
                 } else {
                     nextButton.setEnabled(true);
                     finishButton.setEnabled(false);
                 }
                 backButton.setEnabled(true);
                 panelPosition++;
                 setCurrentPanel(panelPosition);
//                 exportFile();
             }
         });
         nextButton.setText("Next");
         nextButton.setEnabled(false);
         
         cancelButton = new CancelButton(new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 canceled = true;
                 close();
             }
         });
         finishButton = new JButton(new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 finish();
             }
         });
         finishButton.setText("Finish");
         if (!isShapefileCapable || ignoreShapeFileFunctionality)
             finishButton.setEnabled(true);
         else            
             finishButton.setEnabled(false);

//         backButton.addActionListener(wizardController);
//         nextButton.addActionListener(wizardController);
//         cancelButton.addActionListener(wizardController);

         buttonPanel.setLayout(new BorderLayout());
         buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

         buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10))); 
         buttonBox.add(backButton);
         buttonBox.add(Box.createHorizontalStrut(10));
         buttonBox.add(nextButton);
         buttonBox.add(Box.createHorizontalStrut(30));
         buttonBox.add(finishButton);
         buttonBox.add(Box.createHorizontalStrut(30));
         buttonBox.add(cancelButton);
         buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);

         return buttonPanel;
     }    

    private void repopulateProjectionShapefiles(String projectionType) {
        //repopulate projection drop down to represent new columns/fields that were defined in the 
        //pivoted version of this resultset, for example, might sum up to the state level, when 
        //qa report is point based
        projectionShapeFileList.clear();
        for (ProjectionShapeFile projectionShapeFileItem : projectionShapeFiles) {
            if (projectionShapeFileItem.getType().equals(projectionType)) {
                projectionShapeFileList.add(projectionShapeFileItem);
            }
        }
        projectionShapeFile.removeAllItems();
//        projectionShapeFile.resetModel(projectionShapeFileList.toArray(new ProjectionShapeFile[0]));
        projectionShapeFile.setModel(new DefaultComboBoxModel(projectionShapeFileList.toArray(new ProjectionShapeFile[0])));
//        setRenderer(new ComboBoxRenderer(defaultLabel));

    }
    
    private JPanel getCardPanel() {
        
        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10))); 

        cardLayout = new CardLayout(); 
        cardPanel.setLayout(cardLayout);
        return cardPanel;
//        this.getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);


    }
    private void clearMsg() {
        this.messagePanel.clear();
    }
    
    private void registerWizardPanel(JPanel panel) {
        cardPanel.add(panel, (wizardModel.size()) + ""); 
        wizardModel.add(panel);
    }

    void setBackButtonEnabled(boolean b) {
        backButton.setEnabled(b);
    }
    void setNextButtonEnabled(boolean b) {
        nextButton.setEnabled(b);
    }

    private void setCurrentPanel(Object id) {

        // Code omitted

//        WizardPanelDescriptor oldPanelDescriptor =
//            wizardModel.getCurrentPanelDescriptor();
//
//        if (oldPanelDescriptor != null)
//            oldPanelDescriptor.aboutToHidePanel();
//
//        wizardModel.setCurrentPanel(id);
//
//        wizardModel.getCurrentPanelDescriptor().
//            AboutToDisplayPanel();

        cardLayout.show(cardPanel, id.toString());

//        wizardModel.getCurrentPanelDescriptor().
//            DisplayingPanel();
     
    }

    public void finish() {
        clearMsg();
        setVisible(false);
        dispose();
    }

    public boolean shouldCreateCSV(){
        return csvFormat.isSelected();
    }
    
    public boolean shouldCreateShapeFile(){
        return shapeFileFormat.isSelected();
    }
    
    private String[] convertToStringArray(Object[] columns) {
        String[] strColumns = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            strColumns[i] = (String)columns[i];
        }
        return strColumns;
    }
    
    public PivotConfiguration getPivotConfiguration() {
        
        //make sure and return null when not dealing with 
        if (!shapeFileFormat.isSelected() || !pivotData.isSelected())
            return null;
        
        PivotConfiguration pivotConfiguration = new PivotConfiguration();
        
        pivotConfiguration.setRowLabels(convertToStringArray(pivotRowLabels.getAllElements()));
        pivotConfiguration.setExtraFields(convertToStringArray(pivotExtraFields.getAllElements()));
        pivotConfiguration.setColumnLabels(convertToStringArray(pivotColumnLabel.getAllElements()));
        pivotConfiguration.setValues(convertToStringArray(pivotValue.getAllElements()));
        pivotConfiguration.setSummarizeValueBy((String)pivotSummarizeBy.getSelectedItem());
        
        return pivotConfiguration;
    }
    
    public ProjectionShapeFile getProjectionShapeFile() {
        return (ProjectionShapeFile)projectionShapeFile.getSelectedItem();
    }

    public String getRowFilter() {
        return rowFilter.getText();
    }

    public boolean isCanceled() {
        return canceled;
    }
}

package gov.epa.emissions.framework.client.meta.qa.comparedatasetsprogram;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.AddRemoveDatasetVersionWidget;
import gov.epa.emissions.framework.client.meta.qa.EditQAEmissionsPresenter;
import gov.epa.emissions.framework.client.meta.qa.EditQAEmissionsView;
import gov.epa.emissions.framework.client.meta.qa.EditQAStepWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetVersion;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class CompareDatasetsQAProgamWindow extends DisposableInteralFrame implements EditQAEmissionsView {
    
    private AddRemoveDatasetVersionWidget datasetWidgetBase;
    private AddRemoveDatasetVersionWidget datasetWidgetCompare;
    
    private EmfConsole parentConsole;
    
    private JPanel layout;
    
    private EditQAEmissionsPresenter presenter;
    
    private EmfSession session;
    
    private SingleLineMessagePanel messagePanel;
    
    private DatasetVersion[] baseDatasetVersions;
    
    private DatasetVersion[] compareDatasetVersions;
    
    private TextArea groupByExpressionsTextField;
    
    private TextArea aggregateExpressionsTextField;
    
    private TextArea matchingExpressionsTextField;
    
    private TextArea whereFilterTextField;
    
    private TextArea bSuffixTextField;
    private TextArea cSuffixTextField;
    
    private String groupByExpressions;
    
    private String aggregateExpressions;
    
    private String matchingExpressions;

    private String program;
    
    private ComboBox joinTypes;
    
    private String joinType;
    private String whereFilter;
    private String baseSuffix;
    private String compareSuffix;
    
    private DatasetType defaultDatasetType = null;
        
    public CompareDatasetsQAProgamWindow(DesktopManager desktopManager, String program, 
            EmfSession session, DatasetVersion[] baseDatasetVersions, DatasetVersion[] compareDatasetVersions, 
            String groupByExpressions, String aggregateExpressions, String matchingExpressions, 
            String joinType, String whereFilter, String baseSuffix, String compareSuffix) {
        
        super("Emissions Inventories Editor", new Dimension(680, 600), desktopManager);
        this.program = program; 
        this.session = session;
        this.baseDatasetVersions = baseDatasetVersions;
        this.compareDatasetVersions = compareDatasetVersions;
        this.groupByExpressions = groupByExpressions;
        this.aggregateExpressions = aggregateExpressions;
        this.matchingExpressions = matchingExpressions;
        this.joinType = joinType;
        this.whereFilter = whereFilter;
        this.baseSuffix = baseSuffix;
        this.compareSuffix = compareSuffix;
    }


    public void display(EmfDataset dataset, QAStep qaStep) {
        this.defaultDatasetType = dataset.getDatasetType();
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
        try {
            this.getContentPane().add(createLayout(dataset));
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void observe(EditQAEmissionsPresenter presenter) {
        this.presenter = presenter;
    }
    
    private JPanel createLayout(EmfDataset dataset) throws EmfException {
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        JPanel content = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
       
        layoutGenerator.addLabelWidgetPair("Base datasets:", baseDatasetVersionWidget(), content);
        layoutGenerator.addLabelWidgetPair("Compare datasets:", compareDatasetVersionWidget(), content);

        this.groupByExpressionsTextField = new TextArea("Group By Expressions", this.groupByExpressions, 40, 4);
        this.groupByExpressionsTextField.setToolTipText("<html>Group By Expressions"
                + "<br/><br/>This is a new line character delimited list of expressions that will be part of the group by expression when performing the comparison analysis."
                + "<br/>The expressions must contain valid column(s) from either the base or comparison datasets.  If the aggregate expression exists only in the base"
                + "<br/>or compare dataset, then a Mapping Expression must be specified in order for a proper mapping can happen for the comparison analysis (i.e., substring(fips,1,2)=substring(region_cd,1,2))."
                + "<br/>Also, when an aggregrate expression contains any sort of functional operation (i.e., string concatenation --> fipsst || fipscounty),"
                + "<br/>then this expression will also need to be mapped as part of the mapping expressions.  The group by expressions can be aliased by adding the \"AS ALIAS\" clause to the expression."
                + "<br/>The expression can also contain SQL functions such as substring."
                + "<br/><br/>Sample Group By Expressions:"
                + "<br/><br/>scc AS scc_code<br/>substring(fips,1,2) as fipsst<br/>"
                + "<br/>or"
                + "<br/><br/>fipsst || fipscounty as fips<br/>substring(scc,1,5) as scc_lv5</html>");
        ScrollableComponent scrollableComment = ScrollableComponent.createWithVerticalScrollBar(this.groupByExpressionsTextField);
        scrollableComment.setPreferredSize(new Dimension(450, 105));
        layoutGenerator.addLabelWidgetPair("Group By Expressions:", scrollableComment, content);

        this.aggregateExpressionsTextField = new TextArea("Aggregate Expressions", this.aggregateExpressions, 40, 4);
        this.aggregateExpressionsTextField.setToolTipText("<html>Aggregate Expressions"
                + "<br/><br/>This is a new line character delimited list of expressions that will be aggregated across the specified above group by expressions."
                + "<br/>The expressions must contain valid column(s) from either the base or comparison datasets.  If the aggregate expression exists only"
                + "<br/>in the base or compare dataset, then a Mapping Expression must be specified in order for a proper mapping to happen during the comparison"
                + "<br/>analysis."
                + "<br/><br/>Sample Aggregate Expression:"
                + "<br/>ann_emis<br/>avd_emis</html>");
        ScrollableComponent scrollableComment2 = ScrollableComponent.createWithVerticalScrollBar(this.aggregateExpressionsTextField);
        scrollableComment2.setPreferredSize(new Dimension(450, 105));
        layoutGenerator.addLabelWidgetPair("Aggregate Expressions:", scrollableComment2, content);

        this.matchingExpressionsTextField = new TextArea("Matching Expressions", this.matchingExpressions, 40, 4);
        this.matchingExpressionsTextField.setToolTipText("<html>Matching Expressions"
                + "<br/><br/>This is a new line character delimited list of mapping expressions that help cross reference what base dataset expression maps to what comparison dataset expression."
                + "<br/>The mapping expression contains three parts, the base dataset expression, the equals \"=\" character, and the cross reference comparison dataset expression (i.e., base_expression=comparison_expression)."
                + "<br/>The left hand part of the expression is for the base dataset and the right hand side is for the comparison dataset.  The mapping expressions must contain valid column(s)"
                + "<br/>from either the base or comparison datasets."
                + "<br/><br/>Sample Mapping Expression:"
                + "<br/><br/>substring(fips,1,2)=substring(region_cd,1,2)<br/>scc=scc_code<br/>ann_emis=emis_ann<br/>avd_emis=emis_avd</html>");
        ScrollableComponent scrollableComment3 = ScrollableComponent.createWithVerticalScrollBar(this.matchingExpressionsTextField);
        scrollableComment3.setPreferredSize(new Dimension(450, 105));
        layoutGenerator.addLabelWidgetPair("Matching Expressions:", scrollableComment3, content);

        joinTypeCombo();
        layoutGenerator.addLabelWidgetPair("Join Type:", joinTypes, content);
        
        this.whereFilterTextField = new TextArea("Row Filter", this.whereFilter, 40, 4);
        this.whereFilterTextField.setToolTipText("<html>Row Filter"
                + "<br/><br/>This is a SQL WHERE clause that is used to filter both the base and compare dataset(s)."
                + "<br/>The expressions in the WHERE clause must contain valid column(s) from either the base or comparison datasets.  If the expression exists only"
                + "<br/>in the base or compare dataset, then a Mapping Expression must be specified in order for a proper mapping to happen during the comparison"
                + "<br/>analysis."
                + "<br/><br/>Sample Row Filter:"
                + "<br/><br/>For example to filter on a certain state and scc codes,<br/>substring(fips,1,2) = '37' and SCC_code in ('10100202','10100203')<br/>or<br/>fips like '37%' and  and SCC_code like '101002%'</html>");
        ScrollableComponent scrollableComment4 = ScrollableComponent.createWithVerticalScrollBar(this.whereFilterTextField);
        scrollableComment4.setPreferredSize(new Dimension(450, 105));
        layoutGenerator.addLabelWidgetPair("Where Filter:", scrollableComment4, content);
        
        this.bSuffixTextField = new TextArea("Base Suffix", this.baseSuffix, 40, 4);
        ScrollableComponent scrollableComment5 = ScrollableComponent.createWithVerticalScrollBar(this.bSuffixTextField);
        scrollableComment5.setAutoscrolls(true);
        layoutGenerator.addLabelWidgetPair("Base Field Suffix :", scrollableComment5, content);
        
        this.cSuffixTextField = new TextArea("Compare Suffix", this.compareSuffix, 40, 4);
        ScrollableComponent scrollableComment6 = ScrollableComponent.createWithVerticalScrollBar(this.cSuffixTextField);
        scrollableComment5.setAutoscrolls(true);
        layoutGenerator.addLabelWidgetPair("Compare Field Suffix :", scrollableComment6, content);
        
        layoutGenerator.makeCompactGrid(content, 9, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad*/
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(content);
        layout.add(buttonPanel());
        
        return layout;
    }
    
    private void joinTypeCombo() {
        String [] values= new String[]{"FULL OUTER JOIN ","LEFT OUTER JOIN ", "RIGHT OUTER JOIN ", "INNER JOIN "};
        joinTypes = new ComboBox("Not Selected", values);
        joinTypes.setPreferredSize(new Dimension(450, 25));
        joinTypes.setToolTipText("<html>Default join type is FULL OUTER JOIN. <br> " +
        		"INNER JOIN:  only rows that satisfy join conditions. <br>"+
                "LEFT OUTER JOIN: the joined table always has at least one row for each row in the first table. <br>" + 
                "RIGHT OUTER JOIN: the joined table always has at least one row for each row in the second table. <br>" + 
                "FULL OUTER JOIN: the joined table always has at least one row for each row in both tables. </html>");
               
        if(!(joinType==null) && (joinType.trim().length()>0))
            joinTypes.setSelectedItem(joinType);
        
        joinTypes.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                joinTypes.getSelectedItem();
            }
        });
    }
    
    private JPanel baseDatasetVersionWidget() throws EmfException {
        datasetWidgetBase = new AddRemoveDatasetVersionWidget(false, 0, this, parentConsole, session);
        datasetWidgetBase.setPreferredSize(new Dimension(350,220));
        datasetWidgetBase.setDefaultDatasetType(this.defaultDatasetType);
        List<DatasetVersion> datasetVersions = new ArrayList<DatasetVersion>();
        if(baseDatasetVersions != null && baseDatasetVersions.length > 0) {
            
            for (DatasetVersion datasetVersion : baseDatasetVersions) {
                datasetVersions.add(datasetVersion);
            }
        }
        datasetWidgetBase.setDatasetVersions(datasetVersions.toArray(new DatasetVersion[0]));
        return datasetWidgetBase;
    }
    
    private JPanel compareDatasetVersionWidget() throws EmfException {
        datasetWidgetCompare = new AddRemoveDatasetVersionWidget(false, 0, this, parentConsole, session);
        datasetWidgetCompare.setPreferredSize(new Dimension(350,220));
        datasetWidgetCompare.setDefaultDatasetType(this.defaultDatasetType);
        List<DatasetVersion> datasetVersions = new ArrayList<DatasetVersion>();
        if(compareDatasetVersions != null && compareDatasetVersions.length > 0) {
            
            for (DatasetVersion datasetVersion : compareDatasetVersions) {
                datasetVersions.add(datasetVersion);
            }
        }
        datasetWidgetCompare.setDatasetVersions(datasetVersions.toArray(new DatasetVersion[0]));
        return datasetWidgetCompare;
    }
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }
    
    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                disposeView();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (datasetWidgetBase.getDatasetVersions().length == 0) {
                    messagePanel.setError("Please choose base datasets to compare with.");
                    return;
                }
                if (datasetWidgetCompare.getDatasetVersions().length == 0) {
                    messagePanel.setError("Please choose compare datasets to compare with.");
                    return;
                }
                if (groupByExpressionsTextField.getText().trim().length() == 0) {
                    messagePanel.setError("Please specify GROUP BY expression(s).");
                    return;
                }
                if (aggregateExpressionsTextField.getText().trim().length() == 0) {
                    messagePanel.setError("Please specify aggregrate expression(s).");
                    return;
                }
                
                
/*sample program arguments        

-base
ptipm_cap2005v2_nc_sc_va|0
-compare
$DATASET
-groupby
scc
substring(fips,1,2)
-aggregate
ann_emis
avd_emis
-matching
substring(fips,1,2)=substring(region_cd,1,2)
scc=scc_code
ann_emis=emis_ann
avd_emis=emis_avd
-join
outer
-where
substring(fips,1,2)='37'
-base_field_suffix
2007
-compare_field_suffix
2010
*/
                StringBuilder programArguments = new StringBuilder();
                //base tag
                programArguments.append(EditQAStepWindow.BASE_TAG + "\n");
                for (Object datasetVersion : datasetWidgetBase.getDatasetVersions()) {
                    programArguments.append(((DatasetVersion)datasetVersion).getDataset().getName() + "|" + ((DatasetVersion)datasetVersion).getVersion().getVersion() + "\n");
                }
                //compare tag
                programArguments.append(EditQAStepWindow.COMPARE_TAG + "\n");
                for (Object datasetVersion : datasetWidgetCompare.getDatasetVersions()) {
                    programArguments.append(((DatasetVersion)datasetVersion).getDataset().getName() + "|" + ((DatasetVersion)datasetVersion).getVersion().getVersion() + "\n");
                }
                //group by tag
                programArguments.append(EditQAStepWindow.GROUP_BY_EXPRESSIONS_TAG + "\n");
                programArguments.append(groupByExpressionsTextField.getText() + "\n");
                //aggregate tag
                programArguments.append(EditQAStepWindow.AGGREGATE_EXPRESSIONS_TAG + "\n");
                programArguments.append(aggregateExpressionsTextField.getText() + "\n");
                //match by tag
                programArguments.append(EditQAStepWindow.MATCHING_EXPRESSIONS_TAG + "\n");
                programArguments.append(matchingExpressionsTextField.getText()+"\n");
               
                //table join tag
                programArguments.append(EditQAStepWindow.JOIN_TYPE_TAG + "\n");
                programArguments.append(joinTypes.getSelectedItem()==null? "":joinTypes.getSelectedItem().toString()+"\n");

                //table filter tag
                programArguments.append(EditQAStepWindow.WHERE_FILTER_TAG + "\n");
                programArguments.append(whereFilterTextField.getText() + "\n");
                
                //base suffix tag
                programArguments.append(EditQAStepWindow.BASE_SUFFIX_TAG + "\n");
                programArguments.append(bSuffixTextField.getText() + "\n");
                
                //compare suffix tag
                programArguments.append(EditQAStepWindow.COMPARE_SUFFIX_TAG + "\n");
                programArguments.append(cSuffixTextField.getText() + "\n");

                presenter.updateProgramArguments(programArguments.toString());
                dispose();
                disposeView();
            }
        };
    }

}

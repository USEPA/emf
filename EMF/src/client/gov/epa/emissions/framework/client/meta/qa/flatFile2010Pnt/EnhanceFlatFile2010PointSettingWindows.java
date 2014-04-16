package gov.epa.emissions.framework.client.meta.qa.flatFile2010Pnt;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import gov.epa.emissions.commons.data.DatasetType;
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

public class EnhanceFlatFile2010PointSettingWindows extends DisposableInteralFrame implements EditQAEmissionsView{
    
    private AddRemoveDatasetVersionWidget datasetWidgetFlatFile2010Point;
    private AddRemoveDatasetVersionWidget datasetWidgetSupportingSmokeFlatFile;
    private AddRemoveDatasetVersionWidget datasetWidgetSupportingFlatFile2;
    private JCheckBox chkMultiNEI_UNIQUE_ID;
    private JCheckBox chkMultiFRS_ID;
    private TextArea whereFilterTextField;
    
    private String program;
    private EmfSession session;
    private DatasetVersion[] flatFile2010PointDatasetVersions;
    private DatasetVersion[] supportingSmokeFlatFileDatasetVersions;
    private DatasetVersion[] supportingFlatFileDatasetVersions2;
    private String whereFilter;
    private boolean multiNEI;
    private boolean multiFRS;
    
    private EmfConsole parentConsole;
    private SingleLineMessagePanel messagePanel;
    private JPanel layout;
    
    private EditQAEmissionsPresenter presenter;
    
    public EnhanceFlatFile2010PointSettingWindows(DesktopManager desktopManager, String program, 
            EmfSession session, 
            DatasetVersion[] flatFile2010PointDatasetVersions, 
            DatasetVersion[] supportingSmokeFlatFileDatasetVersions, 
            DatasetVersion[] supportingFlatFileDatasetVersions2,
            boolean multiNEI, boolean multiFRS, String whereFilter) {

        super("Emissions Inventories Editor", new Dimension(650, 600), desktopManager);
        this.program = program; 
        this.session = session;
        this.flatFile2010PointDatasetVersions = flatFile2010PointDatasetVersions;
        this.supportingSmokeFlatFileDatasetVersions = supportingSmokeFlatFileDatasetVersions;
        this.supportingFlatFileDatasetVersions2 = supportingFlatFileDatasetVersions2;
        this.multiNEI = multiNEI;
        this.multiFRS = multiFRS;
        this.whereFilter = whereFilter;
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
        try {
            this.getContentPane().add(createLayout(dataset));
        } catch (EmfException e) {
            e.printStackTrace();
        }
        
    }
    private Component createLayout(EmfDataset dataset) throws EmfException{
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        JPanel content = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        layoutGenerator.addLabelWidgetPair(DatasetType.FLAT_FILE_2010_POINT + ":", flatFile2010PointVersionWidget(), content);
        layoutGenerator.addLabelWidgetPair("Supporting Data:", supportingSmokeFlatFileVersionWidget(), content);
        layoutGenerator.addLabelWidgetPair("Alternate Facility:",supportingFlatFileVersionWidget2(), content);

        chkMultiNEI_UNIQUE_ID = new JCheckBox();
        chkMultiNEI_UNIQUE_ID.setSelected( this.multiNEI);
        layoutGenerator.addLabelWidgetPair("Multiple NEI Unique IDs:", chkMultiNEI_UNIQUE_ID, content);
        
        chkMultiFRS_ID = new JCheckBox();
        chkMultiFRS_ID.setSelected( this.multiFRS);
        layoutGenerator.addLabelWidgetPair("Multiple FRS IDs:", chkMultiFRS_ID, content);
        
        this.whereFilterTextField = new TextArea("Row Filter", this.whereFilter, 40, 4);
        this.whereFilterTextField.setToolTipText("<html>Row Filter"
                + "<br/><br/>This is a SQL WHERE clause that is used to filter only the " + DatasetType.FLAT_FILE_2010_POINT + ", not the newly created output -- for example don't allow to filter on newly created columns."
                + "<br/>The expressions in the WHERE clause must contain valid column(s) from either the FF10 dataset."
                + "<br/><br/>Sample Row Filter:"
                + "<br/><br/>For example to filter on a certain state and scc codes,<br/>substring(region_cd,1,2) = '37' and scc in ('10100202','10100203')<br/>or<br/>region_cd like '37%' and  and scc like '101002%'</html>");
        ScrollableComponent scrollableComment4 = ScrollableComponent.createWithVerticalScrollBar(this.whereFilterTextField);
        scrollableComment4.setPreferredSize(new Dimension(450, 105));
        layoutGenerator.addLabelWidgetPair("Where Filter:", scrollableComment4, content);

        layoutGenerator.makeCompactGrid(content, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad*/
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(content);
        layout.add(buttonPanel());
        
        return layout;
    }
    
    private JPanel flatFile2010PointVersionWidget() throws EmfException {
        DatasetType[] datasetTypesToInclude = new DatasetType[1];
        datasetTypesToInclude[0] = session.getLightDatasetType("Flat File 2010 Point");

        datasetWidgetFlatFile2010Point = new AddRemoveDatasetVersionWidget(false, 1, this, parentConsole, session);
        datasetWidgetFlatFile2010Point.setPreferredSize(new Dimension(350,220));
        datasetWidgetFlatFile2010Point.setDatasetTypesToInclude(datasetTypesToInclude);
        List<DatasetVersion> datasetVersions = new ArrayList<DatasetVersion>();
        if(flatFile2010PointDatasetVersions != null && flatFile2010PointDatasetVersions.length > 0) {
            
            for (DatasetVersion datasetVersion : flatFile2010PointDatasetVersions) {
                datasetVersions.add(datasetVersion);
            }
        }
        datasetWidgetFlatFile2010Point.setDatasetVersions(datasetVersions.toArray(new DatasetVersion[0]));
        return datasetWidgetFlatFile2010Point;
    }
    
    private JPanel supportingSmokeFlatFileVersionWidget() throws EmfException {
        DatasetType[] datasetTypesToInclude = new DatasetType[1];
        datasetTypesToInclude[0] = session.getLightDatasetType("Comma Separated Values (CSV)");
        
        datasetWidgetSupportingSmokeFlatFile = new AddRemoveDatasetVersionWidget(false, 1, this, parentConsole, session);
        datasetWidgetSupportingSmokeFlatFile.setPreferredSize(new Dimension(350,220));
        List<DatasetVersion> datasetVersions = new ArrayList<DatasetVersion>();
        datasetWidgetSupportingSmokeFlatFile.setDatasetTypesToInclude(datasetTypesToInclude);
        if(supportingSmokeFlatFileDatasetVersions != null && supportingSmokeFlatFileDatasetVersions.length > 0) {
            
            for (DatasetVersion datasetVersion : supportingSmokeFlatFileDatasetVersions) {
                datasetVersions.add(datasetVersion);
            }
        }
        datasetWidgetSupportingSmokeFlatFile.setDatasetVersions(datasetVersions.toArray(new DatasetVersion[0]));
        return datasetWidgetSupportingSmokeFlatFile;
    }   
    
    private JPanel supportingFlatFileVersionWidget2() throws EmfException {
        DatasetType[] datasetTypesToInclude = new DatasetType[1];
        datasetTypesToInclude[0] = session.getLightDatasetType("Comma Separated Values (CSV)");
        
        datasetWidgetSupportingFlatFile2 = new AddRemoveDatasetVersionWidget(false, 1, this, parentConsole, session);
        datasetWidgetSupportingFlatFile2.setPreferredSize(new Dimension(350,220));
        List<DatasetVersion> datasetVersions = new ArrayList<DatasetVersion>();
        datasetWidgetSupportingFlatFile2.setDatasetTypesToInclude(datasetTypesToInclude);
        if(supportingFlatFileDatasetVersions2 != null && supportingFlatFileDatasetVersions2.length > 0) {
            
            for (DatasetVersion datasetVersion : supportingFlatFileDatasetVersions2) {
                datasetVersions.add(datasetVersion);
            }
        }
        datasetWidgetSupportingFlatFile2.setDatasetVersions(datasetVersions.toArray(new DatasetVersion[0]));
        return datasetWidgetSupportingFlatFile2;
    }    

    public void observe(EditQAEmissionsPresenter presenter) {
        this.presenter = presenter;
       
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
                if (datasetWidgetFlatFile2010Point.getDatasetVersions().length == 0) {
                    messagePanel.setError("Please choose Flat File 2010 Point dataset.");
                    return;
                }
                if (datasetWidgetSupportingSmokeFlatFile.getDatasetVersions().length == 0) {
                    messagePanel.setError("Please choose Supporting Smoke Flat File dataset.");
                    return;
                }
                if (datasetWidgetSupportingFlatFile2.getDatasetVersions().length == 0) {
                    messagePanel.setError("Please choose the second Supporting Flat File dataset.");
                    return;
                }
                /*sample program arguments
                 
                -ff10p
                dataset_name|version
                -ssff
                dataset_name|version
                -fac
                dataset_name|version
                -where
                ann_emis=1000
                -manyfrs
                true
                -manyneiid
                true
                */
                
                StringBuilder programArguments = new StringBuilder();
                //Flat File 2010 Point dataset
                programArguments.append(QAStep.FF10P_TAG + "\n");
                for (Object datasetVersion : datasetWidgetFlatFile2010Point.getDatasetVersions()) {
                    programArguments.append(((DatasetVersion)datasetVersion).getDataset().getName() + "|" + ((DatasetVersion)datasetVersion).getVersion().getVersion() + "\n");
                }
                //Supporting Smoke Flat File dataset
                programArguments.append(QAStep.SSFF_TAG + "\n");
                for (Object datasetVersion : datasetWidgetSupportingSmokeFlatFile.getDatasetVersions()) {
                    programArguments.append(((DatasetVersion)datasetVersion).getDataset().getName() + "|" + ((DatasetVersion)datasetVersion).getVersion().getVersion() + "\n");
                }
                //Supporting Flat File dataset for NEI and FRS
                programArguments.append(QAStep.FAC_TAG + "\n");
                for (Object datasetVersion : datasetWidgetSupportingFlatFile2.getDatasetVersions()) {
                    programArguments.append(((DatasetVersion)datasetVersion).getDataset().getName() + "|" + ((DatasetVersion)datasetVersion).getVersion().getVersion() + "\n");
                }
                
                programArguments.append(QAStep.MANYNEIID_TAG + "\n");
                programArguments.append(chkMultiNEI_UNIQUE_ID.isSelected() + "\n");
                
                programArguments.append(QAStep.MANYFRS_TAG + "\n");
                programArguments.append(chkMultiFRS_ID.isSelected() + "\n");

                programArguments.append(QAStep.WHERE_FILTER_TAG + "\n");
                programArguments.append(whereFilterTextField.getText() + "\n");

                presenter.updateProgramArguments(programArguments.toString());
                dispose();
                disposeView();
            }
        };
    }                
}

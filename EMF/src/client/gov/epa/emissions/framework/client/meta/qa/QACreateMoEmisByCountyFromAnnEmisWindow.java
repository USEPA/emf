package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

public class QACreateMoEmisByCountyFromAnnEmisWindow extends DisposableInteralFrame implements EditQAEmissionsView {
    
    protected AddRemoveDatasetWidget datasetWidget;
    
    protected EmfConsole parentConsole;
    
    protected JPanel layout;
    
    protected EditQAEmissionsPresenter presenter1;
    
    protected ListWidget temporalListWidget;
    
    protected ListWidget smkRptDatasetsListWidget;
    
    protected EmfSession session;
    
    protected SingleLineMessagePanel messagePanel;
    
    private EmfDataset temporalProfile;
        
    private EmfDataset[] smkRpts;
    
    private DatasetType smkRptDST;
    
    private DatasetType temporalProfileDST;

    private Integer year;

    private TextField yearTextField;
    
    public QACreateMoEmisByCountyFromAnnEmisWindow(DesktopManager desktopManager, 
            String program, 
            EmfSession session, 
            EmfDataset temporal, 
            EmfDataset [] smkRptDatasets,
            Integer year) {
        
        super("Create monthly emissions by county from annual emissions", new Dimension(800, 300), desktopManager);
        this.session = session;
        this.temporalProfile = temporal;
        this.smkRpts = smkRptDatasets;
        this.year = year;
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        try {
            this.temporalProfileDST = presenter1.getDatasetType(DatasetType.temporalProfile);
            this.smkRptDST = presenter1.getDatasetType(DatasetType.smokeReportCountyMoncodeAnnual);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
        this.getContentPane().add(createLayout(dataset));
    }

    public void observe(EditQAEmissionsPresenter presenter1) {
        this.presenter1 = presenter1;
    }
    
    private JPanel createLayout(EmfDataset dataset) {
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        messagePanel = new SingleLineMessagePanel();
        messagePanel.setMaximumSize( new Dimension(10000,30));
        layout.add(messagePanel);
        
        JPanel label = new JPanel(new BorderLayout(10, 10));
        label.add(new JLabel("Select one or more reports by county and monthly profile"), BorderLayout.WEST);
        label.setMaximumSize( new Dimension(10000, 30));
        layout.add(label);
        
        boolean boxlayout = true;
        if ( boxlayout) {
            int w = 280, h = 30;
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("Temporal Profile:", w,h));
            panel.add( temporalProfilePanel(dataset));
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            layout.add( panel);
            
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("Smkreport county-moncode annual Datasets:", w,h));
            panel.add( smokeReportDatasetsPanel());
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            layout.add( panel);
            
            yearTextField = new TextField("year", year != null ? year + "" : "", 40);
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            emptyPanel.setPreferredSize( new Dimension(75,30));
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("Year:", w,h));
            panel.add( yearTextField);
            panel.add( emptyPanel);
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            panel.setMaximumSize( new Dimension(10000,30));
            layout.add( panel);
            
        } else {

            JPanel content = new JPanel(new SpringLayout());
            SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

            layoutGenerator.addLabelWidgetPair("Temporal Profile:", temporalProfilePanel(dataset), content);
            layoutGenerator.addLabelWidgetPair("Smkreport county-moncode annual Datasets:",  smokeReportDatasetsPanel(), content);
            yearTextField = new TextField("year", year != null ? year + "" : "", 40);
            layoutGenerator.addLabelWidgetPair("Year:", yearTextField, content);

            layoutGenerator.makeCompactGrid(content, 3, 2, // rows, cols
                    5, 5, // initialX, initialY
                    10, 10);// xPad, yPad*/
            
            
            layout.add(content);
        }
        
        layout.add(buttonPanel());
        
        return layout;
    }
    
    private JPanel temporalProfilePanel(EmfDataset dataset) {
        
        temporalListWidget = new ListWidget(new EmfDataset[0]);
        if(!(temporalProfile == null))
            setDatasetsFromStepWindow(temporalListWidget, new EmfDataset[] { temporalProfile });
        
        JScrollPane pane = new JScrollPane(temporalListWidget);
        pane.setPreferredSize(new Dimension(450, 25));
        temporalListWidget.setToolTipText("The Temporal Profile dataset.  Press select button to choose from a list.");
       
        Button addButton = new AddButton("Select", addTemporalProfileAction() );
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(addButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setPreferredSize( new Dimension(75,30));
        
        //JPanel container = new JPanel(new FlowLayout());
        
        JPanel container = new JPanel();
        //invPanel.setMaximumSize( new Dimension(1500,30));
        container.setLayout( new BoxLayout(container, BoxLayout.X_AXIS));

        container.add(pane);
        container.add(buttonPanel);
        
        return container;
    }
    
    private JPanel smokeReportDatasetsPanel() {
        
        smkRptDatasetsListWidget = new ListWidget(new EmfDataset[0]);
        if(!(smkRpts==null))
            setDatasetsFromStepWindow(smkRptDatasetsListWidget, smkRpts);
        
        JScrollPane pane = new JScrollPane(smkRptDatasetsListWidget);
        pane.setPreferredSize(new Dimension(450, 50));
        smkRptDatasetsListWidget.setToolTipText("The Smkreport county-moncode annual datasets.  Press select button to choose from a list.");
//        speciationProfileWeightDatasetsListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        Button addButton = new AddButton("Select", addSmkRptDatasetsAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(addButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setPreferredSize( new Dimension(75,30));
        
        //JPanel container = new JPanel(new FlowLayout());
        
        JPanel container = new JPanel();
        //invPanel.setMaximumSize( new Dimension(1500,30));
        container.setLayout( new BoxLayout(container, BoxLayout.X_AXIS));

        container.add(pane);
        container.add(buttonPanel);
        
        return container;
    }
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        panel.setMaximumSize( new Dimension(10000,30));
        return panel;
    }
    
    private Action addTemporalProfileAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(temporalListWidget, new DatasetType[] {temporalProfileDST}, temporalProfileDST, true);
            }
        };
    }
  
    private Action addSmkRptDatasetsAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(smkRptDatasetsListWidget, new DatasetType[] {smkRptDST}, smkRptDST, false);
            }
        };
    }
    
    protected Action cancelAction() {
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
                //validate dataset selections
                Object temporalProfile = getTemporalProfileDataset();
                Object[] smkRpts = getSmkRptDatasets();
                Integer year = null;
                String errors = "";

                if (temporalProfile == null) {
                    errors = "Missing temporal profile dataset. ";
                }
                if (smkRpts == null || smkRpts.length == 0) {
                    errors += "Missing SMOKE Report Dataset(s). ";
                }
                if (yearTextField.getText().trim().length() == 0) {
                    errors += "Missing year. ";
                } else {
                    try {
                        year = Integer.parseInt(yearTextField.getText().trim());
                    } catch (NumberFormatException ex) {
                        errors += "The year needs to be in a number format (i.e., 2002). ";
                    }
                }
                
                if (errors.length() > 0) {
                    messagePanel.setError(errors);
                    return;
                }
                presenter1.updateDatasets(temporalProfile, 
                        smkRpts,
                        year);
                dispose();
                disposeView();
            }
        };
    }
    
    protected void doAddWindow(ListWidget listWidget, DatasetType[] datasetTypes, DatasetType defaultDatasetType, boolean selectSingle) {
        try {
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole);
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypes);
            presenter.display(defaultDatasetType, selectSingle);
            if (view.shouldCreate())
                setDatasets(listWidget, presenter.getDatasets());
        } catch (Exception e) {
             messagePanel.setError(e.getMessage());
        }
    }
    
    protected void setDatasetsFromStepWindow(ListWidget listWidget, EmfDataset[] datasets){
        listWidget.removeAll();
        for (int i = 0; i < datasets.length; i++) {
            listWidget.addElement(datasets[i]);
        }
    }
    
    protected void setDatasets(ListWidget listWidget, EmfDataset [] datasets) {
        listWidget.removeAllElements();
        for (int i = 0; i < datasets.length; i++) {
           //System.out.println(" Inv dataset is: " + datasets[i]);
            listWidget.addElement(datasets[i]);
        }
        
    }
    
    protected Object getTemporalProfileDataset() {
        return (temporalListWidget.getAllElements() != null && temporalListWidget.getAllElements().length > 0) ? temporalListWidget.getAllElements()[0] : null;
    }
   
    protected Object[] getSmkRptDatasets() {
        return (smkRptDatasetsListWidget.getAllElements() != null && smkRptDatasetsListWidget.getAllElements().length > 0) ? smkRptDatasetsListWidget.getAllElements() : null;
    }
    
    protected JPanel createLabelInPanel( String lbl, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setSize(width, height);
        panel.setMinimumSize( new Dimension(width, height));
        panel.setMaximumSize( new Dimension(width, height));
        panel.setPreferredSize( new Dimension(width, height));
        
        JLabel label = new JLabel( lbl);
        label.setSize(width, height);
        label.setMinimumSize( new Dimension(width, height));
        label.setMaximumSize( new Dimension(width, height));   
        label.setPreferredSize( new Dimension(width, height));
        panel.add( label);
        
        return panel;
    }    
}

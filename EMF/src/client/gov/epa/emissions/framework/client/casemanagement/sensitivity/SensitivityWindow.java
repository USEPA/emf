package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditor;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class SensitivityWindow extends DisposableInteralFrame implements SensitivityView {
    private SensitivityPresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private JRadioButton newRadioButton;

    private JRadioButton existRadioButton;

    private ButtonGroup buttonGroup;

    private ComboBox senName;

    private ComboBox jobGroup;

    private TextField senCaseAbrev;

    private EmfConsole parentConsole;

    private Case parentCase, selectedTem;

    private List<CaseCategory> categories = new ArrayList<CaseCategory>();

    private List<Case> templateCases;

    private JList grids, sectors;

    private CaseManagerPresenter parentPresenter;

    private ComboBox senTypeCombox;

    private ComboBox categoryCombox;

    private JTextArea information;

    private Dimension preferredSize = new Dimension(300, 20);

    public SensitivityWindow(DesktopManager desktopManager, EmfConsole parentConsole, List<CaseCategory> categories) {
        super("Sensitivity", new Dimension(540, 640), desktopManager);
        super.setName(title);
        this.parentConsole = parentConsole;
        this.categories.addAll(categories);
        this.categories.remove(new CaseCategory("All"));
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void observe(SensitivityPresenter presenter, CaseManagerPresenter parentPresenter) {
        this.presenter = presenter;
        this.parentPresenter = parentPresenter;
    }

    public void display(Case case1) {
        super.setLabel("Add Sensitivity for Case: " + case1.getName());
        layout.removeAll();
        doLayout(case1);
        super.display();
        super.resetChanges();

    }

    private void doLayout(Case parentCase) {
        this.parentCase = parentCase;
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createTopPanel(parentCase));
        layout.add(createCasePanel(parentCase));
        layout.add(createSenPanel(parentCase));
        layout.add(createButtonsPanel());
    }

    private JPanel createTopPanel(Case case1) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        Label caseNameNAbbr = new Label("Case Name", case1.getName() + " (" + case1.getAbbreviation() + ")");
        layoutGenerator.addLabelWidgetPair("Parent Case:", caseNameNAbbr, panel);

        layoutGenerator.addLabelWidgetPair("Sensitivity Case:", newOrExistRadios(), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel createCasePanel(Case case1) {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Case"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        senName = new ComboBox(new Case[0]);
        senName.setPreferredSize(preferredSize);
        senName.setEditable(true);
        addChangeable(senName);
        senName.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    messagePanel.clear();
                    updateCaseInfo();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Name:", senName, panel);

        senCaseAbrev = new TextField("CaseAbbreviation", 27);
        addChangeable(senCaseAbrev);
        layoutGenerator.addLabelWidgetPair("Abbreviation:", senCaseAbrev, panel);

        categoryCombox = new ComboBox("Select One", categories.toArray(new CaseCategory[0]));
        categoryCombox.setPreferredSize(preferredSize);
        categoryCombox.setSelectedItem(getSenTemCategory("Sensitivity"));
        layoutGenerator.addLabelWidgetPair("Case Category: ", categoryCombox, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createSenPanel(Case case1) {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sensitivity"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        CaseCategory category = getSenTemCategory("Sensitivity Template");
        try {
            getAllSenTemplateCases(category);
        } catch (EmfException e) {
            messagePanel.setError("Couldn't get Senstivity Template Cases: " + e.getMessage());
        }

        senTypeCombox = new ComboBox("Select One", templateCases.toArray(new Case[0]));
        senTypeCombox.setPreferredSize(preferredSize);
        senTypeCombox.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                refreshSensInfo();
            }
        });
        layoutGenerator.addLabelWidgetPair("Sensitivity Type:", senTypeCombox, panel);

        jobGroup = new ComboBox(new String[] { "" });
        jobGroup.setPreferredSize(preferredSize);
        jobGroup.setEditable(true);
        addChangeable(jobGroup);
        jobGroup.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
            }
        });
        layoutGenerator.addLabelWidgetPair("Information: ", builtTemplateDesc(), panel);
        layoutGenerator.addLabelWidgetPair("Job Group:", jobGroup, panel);
        layoutGenerator.addLabelWidgetPair("Region: ", buildGridsPanel(), panel);
        layoutGenerator.addLabelWidgetPair("Sector Filter: ", buildSectorsPanel(), panel);
        // layoutGenerator.addLabelWidgetPair("Sensitivity Jobs: ", buildjobsPanel(), panel);
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(15);
        layout.setVgap(10);
        container.setLayout(layout);

        Button setJobsButton = new Button("Select Jobs", setJobsAction(this));
        container.add(setJobsButton);
        setJobsButton.setMnemonic('S');
        container.add(new CancelButton(closeAction()));
        getRootPane().setDefaultButton(setJobsButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private CaseCategory getSenTemCategory(String name) {
        for (CaseCategory cat : categories) {
            if (cat.getName().trim().equalsIgnoreCase(name))
                return cat;
        }
        return null;
    }

    private void getAllSenTemplateCases(CaseCategory category) throws EmfException {
        this.templateCases = new ArrayList<Case>();
        templateCases.addAll(Arrays.asList(presenter.getCases(category)));
        Collections.sort(templateCases);
    }

    private JScrollPane builtTemplateDesc() {
        information = new JTextArea();
        information.setWrapStyleWord(true);
        information.setLineWrap(true);
        information.setEditable(false);
        information.setText("");
        JScrollPane scrollPane = new JScrollPane(information);
        scrollPane.setPreferredSize(new Dimension(300, 80));
        return scrollPane;
    }

    private JScrollPane buildGridsPanel() {
        grids = new JList();
        grids.setListData(parentCase.getRegions());
        JScrollPane scrollPane = new JScrollPane(grids, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(300, 100));
        return scrollPane;
    }

    private JScrollPane buildSectorsPanel() {
        sectors = new JList();
        sectors.setListData(new Sector[] {});
        sectors.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(sectors, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(300, 100));
        return scrollPane;
    }

    private void refreshSensInfo() {
        selectedTem = (Case) senTypeCombox.getSelectedItem();
        if (selectedTem == null) {
            information.setText("");
            sectors.setListData(new Sector[] {});
            return;
        }
        information.setText(selectedTem.getDescription());
        refreshSectors(parentCase.getId(), selectedTem.getId());
    }

    private void refreshSectors(int parentCaseId, int templateCaseId) {
        sectors.setListData(getCommonSectors());
    }

    private Sector[] getCommonSectors() {
        List<Sector> commonSectors = new ArrayList<Sector>();
        List<Sector> tSectors = Arrays.asList(selectedTem.getSectors());
        commonSectors.add(new Sector("Select All", "Select All"));
        for (Sector pSector : parentCase.getSectors()) {
            if (tSectors.contains(pSector))
                commonSectors.add(pSector);
        }
        return commonSectors.toArray(new Sector[0]);
    }

    private JPanel newOrExistRadios() {
        newRadioButton = new JRadioButton("Create new case");
        newRadioButton.setSelected(true);
        existRadioButton = new JRadioButton("Add to existing case");
        buttonGroup = new ButtonGroup();
        buttonGroup.add(newRadioButton);
        newRadioButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                newRadioButtonAction();
            }
        });
        buttonGroup.add(existRadioButton);
        existRadioButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                try {
                    existRadioButtonAction();
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        });

        JPanel radioPanel = new JPanel();
        radioPanel.add(newRadioButton);

        radioPanel.add(existRadioButton);
        return radioPanel;
    }

    public void editAction(CaseJob[] selectedJobs) {
        messagePanel.clear();
        messagePanel.setMessage("Server is processing sensitivity case...");
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // messagePanel.setMessage("Server is processing sensitivity case...");

        try {
            // validateFields();
            Case sensitivityCase = null;

            if (newRadioButton.isSelected())
                sensitivityCase = presenter.doSave(parentCase.getId(),
                        ((Case) senTypeCombox.getSelectedItem()).getId(), jobIds(selectedJobs), getJobGroup(),
                        setSensitivityCase());
            else
                sensitivityCase = presenter.addSensitivities(parentCase.getId(), ((Case) senTypeCombox
                        .getSelectedItem()).getId(), jobIds(selectedJobs), getJobGroup(), (Case) senName
                        .getSelectedItem(), (GeoRegion)grids.getSelectedValue());

            if (sensitivityCase == null) {
                messagePanel.setError("Failed processing sensitivity case.");
                return;
            }

            resetChanges();

            CaseEditor view = new CaseEditor(parentConsole, presenter.getSession(), desktopManager);
            parentPresenter.doEdit(view, sensitivityCase);
            disposeView();
            // messagePanel.clear();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private String getJobGroup() {
        return (String) jobGroup.getSelectedItem();
    }

    private void newRadioButtonAction() {
        senName.setEnabled(true);
        senName.removeAllItems();
        senName.clear();
        senName.resetModel(new Case[0]);
        senName.setEditable(true);
        senName.validate();
        senCaseAbrev.setEditable(true);
        senCaseAbrev.setText("");
        categoryCombox.setEnabled(true);
        jobGroup.removeAllItems();
        jobGroup.resetModel(new String[] { "" });
        jobGroup.validate();
    }

    private void existRadioButtonAction() throws EmfException {
        senName.removeAllItems();
        senName.resetModel(presenter.getSensitivityCases(parentCase.getId()));
        senName.setEditable(false);
        senName.validate();

        Case selected = (Case) senName.getSelectedItem();
        senCaseAbrev.setEditable(false);
        senCaseAbrev.setText(selected == null ? "" : selected.getAbbreviation().getName());
        categoryCombox.setSelectedItem(selected == null ? 0 : selected.getCaseCategory());
        categoryCombox.setEnabled(false);
    }

    private void updateCaseInfo() throws EmfException {
        Object selected = senName.getSelectedItem();

        if (selected == null || selected instanceof String) {
            if (existRadioButton.isSelected()) {
                senCaseAbrev.setText("");
                categoryCombox.setSelectedIndex(0);
            }

            return;
        }

        Case selectedCase = (Case) selected;

        if (selectedCase != null) {
            senCaseAbrev.setText(selectedCase.getAbbreviation().getName());
            categoryCombox.setSelectedItem(selectedCase.getCaseCategory());
            jobGroup.removeAllItems();
            jobGroup.resetModel(presenter.getJobGroups(selectedCase));
            senName.validate();
        }
    }

    private Case setSensitivityCase() {
        Case sensitivityCase = new Case();
        sensitivityCase.setName(senName.getSelectedItem().toString());
        sensitivityCase.setAbbreviation(new Abbreviation(senCaseAbrev.getText()));
        sensitivityCase.setCaseCategory((CaseCategory) categoryCombox.getSelectedItem());
        
        GeoRegion rg = (GeoRegion)grids.getSelectedValue();
        
        if (rg != null)
            sensitivityCase.setRegions(new GeoRegion[]{rg});
        
        return sensitivityCase;
    }

    private void validateFields() throws EmfException {
        if (senName.getSelectedItem() == null || senCaseAbrev.getText().trim().isEmpty())
            throw new EmfException("Please specify a name and abbreviation. ");

        validateJobGroup(getJobGroup());

        if (senTypeCombox.getSelectedItem() == null)
            throw new EmfException("Please specify sensitivity type. ");
        
        if (grids.getModel() == null || grids.getModel().getSize() == 0)
            throw new EmfException("Please exit and set regions in the parent case.");
        
        if (grids.getSelectedValue() == null)
            throw new EmfException("Please select a specific region for the sensitivity case.");
        
        if (grids.getSelectedValues().length > 1)
            throw new EmfException("Please select only one region for the sensitivity case.");
    }

    private void validateJobGroup(String group) throws EmfException {
        if (group == null || group.trim().isEmpty())
            return;

        for (int i = 0; i < group.length(); i++) {
            if (!Character.isLetterOrDigit(group.charAt(i)) && (group.charAt(i) != '_'))
                throw new EmfException("Job group must contain only letters, digits, and underscores. ");
        }
    }

    private int[] jobIds(CaseJob[] jobs) throws EmfException {
        int jobsNumber = jobs.length;

        if (jobsNumber == 0)
            throw new EmfException("Please select one or more jobs.");

        int[] selectedIndexes = new int[jobsNumber];
        for (int i = 0; i < jobsNumber; i++)
            selectedIndexes[i] = jobs[i].getId();

        return selectedIndexes;
    }

    public void windowClosing() {
        doClose();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                doClose();
            }
        };

        return action;
    }

    private Action setJobsAction(final SensitivityWindow view) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                try {
                    validateFields();
                    CaseJob[] filteredJobs = presenter.getCaseJobs(selectedTem, getSelectedRegion(), getSelectedSectors());
                    if (filteredJobs == null || filteredJobs.length == 0) {
                        messagePanel.setMessage("There is no jobs for the selected region and sectors");
                        return;
                    }
                    JobsChooserDialog dialog = new JobsChooserDialog(view, parentConsole);
                    dialog.display(filteredJobs);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }

            }
        };

        return action;
    }

    public void setAction(CaseJob[] selectedJobs) {
        messagePanel.clear();
        messagePanel.setMessage("Server is processing sensitivity case...");
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            Case sensCase = null;
            List<CaseInput> existingInputs = null;
            List<CaseParameter> existingParas = null;
            int temCaseId = ((Case) senTypeCombox.getSelectedItem()).getId();

            if (newRadioButton.isSelected())
                sensCase = presenter.doSave(parentCase.getId(), temCaseId, jobIds(selectedJobs), getJobGroup(),
                        setSensitivityCase());
            else {
                existingInputs = Arrays.asList(presenter.getCaseInput(((Case) senName.getSelectedItem()).getId(),
                        new Sector("All", "All"), false));
                existingParas = Arrays.asList(presenter.getCaseParameters(((Case) senName.getSelectedItem()).getId(),
                        new Sector("All", "All"), false));
                sensCase = presenter.addSensitivities(parentCase.getId(), temCaseId, jobIds(selectedJobs),
                        getJobGroup(), (Case) senName.getSelectedItem(), (GeoRegion)grids.getSelectedValue());
            }
            if (sensCase == null) {
                messagePanel.setError("Failed processing sensitivity case.");
                return;
            }

            resetChanges();
            setCaseView(sensCase, existingInputs, existingParas);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void doClose() {
        if (shouldDiscardChanges())
            presenter.doClose();
    }

    private void setCaseView(Case newCase, List<CaseInput> existingInputs, List<CaseParameter> existingParas)
            throws EmfException {
        if (newCase == null)
            throw new EmfException("The new sensitivity case is null.");

        String title = "Sensitivity Wizard: " + newCase.getName();
        presenter.doDisplaySetCaseWindow(newCase, title, parentConsole, desktopManager, parentPresenter,
                existingInputs, existingParas);
        presenter.doClose();
    }
    
    private GeoRegion getSelectedRegion() {
        return (GeoRegion) grids.getSelectedValue();
    }

    private Sector[] getSelectedSectors() {
        List<Sector> list = new ArrayList<Sector>(sectors.getSelectedValues().length);
        for (int i = 0; i < sectors.getSelectedValues().length; i++) {
            Sector sector = (Sector) sectors.getSelectedValues()[i];
            if (!sector.getName().equalsIgnoreCase("Select All"))
                list.add((Sector) sectors.getSelectedValues()[i]);
        }
        return list.toArray(new Sector[0]);
    }
}

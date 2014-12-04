package gov.epa.emissions.framework.client.tempalloc.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.DefaultEmfSession.ObjectCacheType;
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class TemporalAllocationSummaryTab extends JPanel implements TemporalAllocationTabView {
    
    private TemporalAllocation temporalAllocation;

    private ManageChangeables changeablesList;

    private TextField name;

    private TextArea description;
    
    private EditableComboBox projectsCombo;
    
    private JLabel runStatus, startDate, completionDate;

    private EmfSession session;

    private MessagePanel messagePanel;
    
    private TemporalAllocationPresenter presenter;
    
    public TemporalAllocationSummaryTab(TemporalAllocation temporalAllocation, EmfSession session, 
            ManageChangeables changeablesList, SingleLineMessagePanel messagePanel,
            TemporalAllocationPresenter presenter) {
        super.setName("summary");
        this.temporalAllocation = temporalAllocation;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.presenter = presenter;
    }
    
    public void setTemporalAllocation(TemporalAllocation temporalAllocation) {
        this.temporalAllocation = temporalAllocation;
    }
    
    public void display() {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createMainSection(), ""), BorderLayout.CENTER);
        panel.add(getBorderedPanel(createResultsSection(), "Results"), BorderLayout.SOUTH);
        super.add(panel, BorderLayout.NORTH);
    }

    private JPanel createMainSection() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panel);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description()), panel);
        if (presenter.isEditing()) {
            layoutGenerator.addLabelWidgetPair("Project:", projects(), panel);
        } else {
            JLabel viewLabel = new JLabel();
            if (temporalAllocation.getProject() != null) {
                viewLabel.setText(temporalAllocation.getProject().getName());
            }
            layoutGenerator.addLabelWidgetPair("Project:", viewLabel, panel);
        }
        
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panel);
        layoutGenerator.addLabelWidgetPair("Creator:", creator(), panel);

        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        
        return panel;
    }
    
    private JPanel createResultsSection() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        runStatus = new JLabel("");
        runStatus.setBackground(Color.white);
        
        startDate = new JLabel("");
        startDate.setBackground(Color.white);

        completionDate = new JLabel("");
        completionDate.setBackground(Color.white);
        
        updateResultsSection();

        layoutGenerator.addLabelWidgetPair("Run Status:", runStatus, panel);
        layoutGenerator.addLabelWidgetPair("Start Date:", startDate, panel);
        layoutGenerator.addLabelWidgetPair("Completion Date:", completionDate, panel);
     
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    private void updateResultsSection() {
        String status = temporalAllocation.getRunStatus();
        runStatus.setText(status);
        
        String labelText;
        if (temporalAllocation.getStartDate() == null) {
            labelText = status;
        } else {
            labelText = CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(temporalAllocation.getStartDate());
        }
        startDate.setText(labelText);
        
        if (status.indexOf("Finished") == -1) {
            labelText = status;
        } else {
            labelText = CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(temporalAllocation.getCompletionDate());
        }
        completionDate.setText(labelText);
    }

    private JPanel getBorderedPanel(JPanel component, String border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }

    private JLabel lastModifiedDate() {
        return createLeftAlignedLabel(temporalAllocation.getLastModifiedDate() != null ? CustomDateFormat.format_MM_DD_YYYY_HH_mm(temporalAllocation.getLastModifiedDate()) : "");
    }

    private JLabel creator() {
        return createLeftAlignedLabel(temporalAllocation.getCreator() != null ? temporalAllocation.getCreator().getName() : "");
    }

    private TextArea description() {
        description = new TextArea("description", temporalAllocation.getDescription() != null ? temporalAllocation.getDescription() : "", 40, 3);
        changeablesList.addChangeable(description);
        description.setEditable(presenter.isEditing());

        return description;
    }

    private TextField name() {
        name = new TextField("name", 40);
        name.setText(temporalAllocation.getLastModifiedDate() != null ? temporalAllocation.getName() : "");
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);
        name.setEditable(presenter.isEditing());

        return name;
    }

    private Project[] getProjects() {
        return session.getProjects();
    }
    
    private EditableComboBox projects() {
        projectsCombo = new EditableComboBox(getProjects());

        if (!(this.session != null && this.session.user() != null && this.session.user().isAdmin())) {
            projectsCombo.setEditable(false);
        }

        projectsCombo.setSelectedItem(temporalAllocation.getProject());
        projectsCombo.setPreferredSize(name.getPreferredSize());
        changeablesList.addChangeable(projectsCombo);

        return projectsCombo;
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel label = new JLabel(name);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }
    
    public void save() throws EmfException {
        messagePanel.clear();
        if (name.getText().trim().length() ==0)
            throw new EmfException("The name is missing.");

        temporalAllocation.setName(name.getText());
        temporalAllocation.setDescription(description.getText());
        updateProject();
    }

    private void updateProject() {
        Object selected = projectsCombo.getSelectedItem();
        if (selected instanceof String) {
            String projectName = ((String) selected).trim();
            if (projectName.length() > 0) {
                Project project = project(projectName); // checking for duplicates
                temporalAllocation.setProject(project);
                if (project.getId() == 0) {
                    session.getObjectCache().invalidate(ObjectCacheType.PROJECTS_LIST);
                }
            }
        } else if (selected instanceof Project) {
            temporalAllocation.setProject((Project) selected);
        }
    }

    private Project project(String projectName) {
        return new Projects(session.getProjects()).get(projectName);
    }
    
    public void refresh() {
        updateResultsSection();
    }
}

package gov.epa.emissions.framework.client.tempalloc.editor;

import java.awt.BorderLayout;
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
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class TemporalAllocationSummaryTab extends JPanel implements TemporalAllocationTabView {
    
    private TemporalAllocation temporalAllocation;

    private ManageChangeables changeablesList;

    private TextField name;

    private TextArea description;
    
    private EditableComboBox projectsCombo;

    private EmfSession session;

    private MessagePanel messagePanel;
    
    public TemporalAllocationSummaryTab(TemporalAllocation temporalAllocation, EmfSession session, ManageChangeables changeablesList, SingleLineMessagePanel messagePanel) {
        super.setName("summary");
        this.temporalAllocation = temporalAllocation;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
    }
    
    public void setTemporalAllocation(TemporalAllocation temporalAllocation) {
        this.temporalAllocation = temporalAllocation;
    }
    
    public void display() {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createMainSection(), ""), BorderLayout.CENTER);
        super.add(panel, BorderLayout.NORTH);
    }

    private JPanel createMainSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JPanel panelTop = new JPanel(new SpringLayout());
        // panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panelTop);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description()), panelTop);
        layoutGenerator.addLabelWidgetPair("Project:", projects(), panelTop);
        
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panelTop);
        layoutGenerator.addLabelWidgetPair("Creator:", creator(), panelTop);
        layoutGenerator.makeCompactGrid(panelTop, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        panel.add(panelTop);
        
        return panel;
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

        return description;
    }

    private TextField name() {
        name = new TextField("name", 40);
        name.setText(temporalAllocation.getLastModifiedDate() != null ? temporalAllocation.getName() : "");
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

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
            }
        } else if (selected instanceof Project) {
            temporalAllocation.setProject((Project) selected);
        }
    }

    private Project project(String projectName) {
        return new Projects(session.getProjects()).get(projectName);
    }
}

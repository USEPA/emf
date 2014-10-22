package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import java.awt.BorderLayout;
import java.awt.Dimension;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class StrategyGroupNotesTab extends JPanel implements StrategyGroupTabView {

    private StrategyGroup strategyGroup;

    private ManageChangeables changeablesList;

    private TextField name;

    private TextArea notes;

    private EmfSession session;

    private MessagePanel messagePanel;
    
    public StrategyGroupNotesTab(StrategyGroup strategyGroup, EmfSession session, ManageChangeables changeablesList, SingleLineMessagePanel messagePanel) {
        super.setName("notes");
        this.strategyGroup = strategyGroup;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
    }
    
    public void setStrategyGroup(StrategyGroup strategyGroup) {
        this.strategyGroup = strategyGroup;
    }
    
    public void display() {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panel);
        layoutGenerator.addLabelWidgetPair("Notes:", new ScrollableComponent(notes()), panel);

        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        super.add(panel, BorderLayout.NORTH);
    }

    private TextField name() {
        name = new TextField("name", 40);
        name.setText(strategyGroup.getName());
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

        return name;
    }

    private TextArea notes() {
        notes = new TextArea("notes", strategyGroup.getNotes() != null ? strategyGroup.getNotes() : "", 40, 20);
        changeablesList.addChangeable(notes);

        return notes;
    }
    
    public void save() throws EmfException {
        messagePanel.clear();
        if (name.getText().trim().length() ==0)
            throw new EmfException("The name is missing.");
        
        strategyGroup.setName(name.getText());
        strategyGroup.setNotes(notes.getText());
    }

}

package gov.epa.emissions.framework.client.tempalloc.editor;

import java.awt.BorderLayout;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationOutput;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.JPanel;

public class TemporalAllocationOutputTab extends JPanel implements TemporalAllocationTabView {
    
    private TemporalAllocation temporalAllocation;

    private ManageChangeables changeablesList;

    private EmfSession session;

    private MessagePanel messagePanel;
    
    private EmfConsole parentConsole;
    
    public TemporalAllocationOutputTab(TemporalAllocation temporalAllocation, EmfSession session, 
            ManageChangeables changeablesList, SingleLineMessagePanel messagePanel, 
            EmfConsole parentConsole) {
        super.setName("output");
        this.temporalAllocation = temporalAllocation;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
    }
    
    public void setTemporalAllocation(TemporalAllocation temporalAllocation) {
        this.temporalAllocation = temporalAllocation;
    }
    
    public void display() {
        super.setLayout(new BorderLayout());
        super.add(tablePanel(), BorderLayout.CENTER);
    }
    
    private JPanel tablePanel() {
        TemporalAllocationOutput[] outputs = null;
        try {
            outputs = session.temporalAllocationService().getTemporalAllocationOutputs(temporalAllocation);
        } catch (Exception e) {
            //
        }
        TemporalAllocationOutputTableData tableData = new TemporalAllocationOutputTableData(outputs);
        SelectableSortFilterWrapper table = new SelectableSortFilterWrapper(parentConsole, tableData, null);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(table);
        return tablePanel;
    }
    
    public void save() {
        
    }
}

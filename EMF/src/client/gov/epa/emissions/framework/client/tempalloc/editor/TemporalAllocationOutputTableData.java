package gov.epa.emissions.framework.client.tempalloc.editor;

import java.util.ArrayList;
import java.util.List;

import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationOutput;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

public class TemporalAllocationOutputTableData extends AbstractTableData {
    
    private List rows;

    public TemporalAllocationOutputTableData(TemporalAllocationOutput[] temporalAllocationOutputs) {
        this.rows = createRows(temporalAllocationOutputs);
    }
    
    private List createRows(TemporalAllocationOutput[] temporalAllocationOutputs) {
        List rows = new ArrayList();
        for (int i = 0; i < temporalAllocationOutputs.length; i++) {
            addRow(rows, temporalAllocationOutputs[i]);
        }
        return rows;
    }

    private void addRow(List rows, TemporalAllocationOutput temporalAllocationOutput) {
        Object[] values = { temporalAllocationOutput.getType().getName(), 
                temporalAllocationOutput.getOutputDataset().getName() };
        Row row = new ViewableRow(temporalAllocationOutput, values);
        rows.add(row);
    }

    public String[] columns() {
        return new String[] { "Result Type", "Result Dataset" };
    }
    
    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }
}
package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class SectorScenarioOutputTableData extends AbstractTableData {

    private List rows;

    public SectorScenarioOutputTableData(SectorScenarioOutput[] sectorScenarioOutputs) {
        this.rows = createRows(sectorScenarioOutputs);
    }

    private List createRows(SectorScenarioOutput[] sectorScenarioOutputs) {
        List rows = new ArrayList();
        for (int i = 0; i < sectorScenarioOutputs.length; i++) {
            addRow(rows, sectorScenarioOutputs[i]);
        }
        return rows;
    }

    private void addRow(List rows, SectorScenarioOutput sectorScenarioOutput) {
        Object[] values = values(sectorScenarioOutput);
        Row row = new ViewableRow(sectorScenarioOutput, values);
        rows.add(row);
    }

    private Object[] values(SectorScenarioOutput result) {
        EmfDataset outputDataset = result.getOutputDataset();
        EmfDataset invDataset = result.getInventoryDataset();
        Object[] values = { 
                result.getType().getName(),
                //result.getgetRecordCount() == null ? 0 : result.getRecordCount(), 
                outputDataset != null ? outputDataset.getName() : "", 
                result.getRunStatus(), 
                
                format(result.getStartDate()),
                format(result.getCompletionDate()),
                invDataset == null || invDataset.getSectors() == null  || invDataset.getSectors().length == 0 ? "" : invDataset.getSectors()[0].getName(),
                invDataset == null? "":invDataset.getName()
                };
        return values;
    }

    public String[] columns() {
        return new String[] { 
                "Result Type", 
                //"Record Count", 
                "Result Dataset", 
                "Status", 
                "Start Time", 
                "Completion Time",
                "Input Inventory Sector",
                "Input Inventory Dataset" 
                };
    }

    public Class getColumnClass(int col) {
//        if ( col == 1)
//            return Integer.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }
}
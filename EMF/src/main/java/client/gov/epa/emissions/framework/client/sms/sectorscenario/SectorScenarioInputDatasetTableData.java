package gov.epa.emissions.framework.client.sms.sectorscenario;

import gov.epa.emissions.framework.services.sms.SectorScenarioInventory;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SectorScenarioInputDatasetTableData extends AbstractTableData {

    private List rows;

    public SectorScenarioInputDatasetTableData(SectorScenarioInventory[] sectorScenarioInventorys) {
        rows = createRows(sectorScenarioInventorys);
    }

    private List createRows(SectorScenarioInventory[] sectorScenarioInventories) {
        List rows = new ArrayList();
        for (int i = 0; i < sectorScenarioInventories.length; i++) {
            Row row = row(sectorScenarioInventories[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(SectorScenarioInventory sectorScenarioInventory) {
        Object[] values = { sectorScenarioInventory.getDataset().getDatasetType().getName(), sectorScenarioInventory.getDataset().getName(), sectorScenarioInventory.getVersion() //, sectorScenarioInventory.getExportSector(), sectorScenarioInventory.getExportEECS()
                          };
        return new ViewableRow(sectorScenarioInventory, values);
    }

    public String[] columns() {
        return new String[] { "Type", "Dataset", "Version" //, "Export Sector", "Export EECS" 
                            };
    }

    public Class getColumnClass(int col) {
        if (col == 2)
        {    
            return Integer.class;
        }
        if ( col == 3 || col == 4)
        {
            return Boolean.class;
        }

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        if ( col >= 3) 
        {
            return true;
        }
        return false;
    }

    public void add(SectorScenarioInventory[] SectorScenarioInventories) {
        for (int i = 0; i < SectorScenarioInventories.length; i++) {
            Row row = row(SectorScenarioInventories[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public SectorScenarioInventory[] sources() {
        List sources = sourcesList();
        return (SectorScenarioInventory[]) sources.toArray(new SectorScenarioInventory[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(SectorScenarioInventory sectorScenarioInventory) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            SectorScenarioInventory source = (SectorScenarioInventory) row.source();
            if (source == sectorScenarioInventory) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(SectorScenarioInventory[] sectorScenarioInventories) {
        for (int i = 0; i < sectorScenarioInventories.length; i++)
            remove(sectorScenarioInventories[i]);

        refresh();
    }
}
package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ResultTable {
    
    private DataTable delegate;
    
    public ResultTable(String table, Datasource datasource) throws EmfException {
        EmfDataset dataset = new EmfDataset();
        
        String newName = table;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);
        
        this.delegate = new DataTable(dataset, datasource);
    }
    
    public String name() {
        return delegate.name();
    }
    
    public void create(String table, TableFormat tableFormat) throws Exception {
        delegate.create(table, tableFormat);
    }

    public void create(TableFormat tableFormat) throws Exception {
        delegate.create(name(), tableFormat);
    }

    public void drop(String table) throws Exception {
        delegate.drop(table);
    }

    public void drop() throws Exception {
        delegate.drop();
    }

    public boolean exists(String table) throws Exception {
        return delegate.exists(table);
    }
    
}

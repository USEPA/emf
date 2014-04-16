package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;

import java.util.ArrayList;
import java.util.List;

public class FixedColumnsWithLineNoColDataLoader extends FixedColumnsDataLoader {

    private TableFormat tableFormat;
    
    private long count = 1;
    
    public FixedColumnsWithLineNoColDataLoader(Datasource datasource, TableFormat tableFormat) {
        super(datasource, tableFormat);
        this.tableFormat = tableFormat;
    }

    protected String[] data(Dataset dataset, Record record) {
        List data = new ArrayList();

        if (tableFormat instanceof VersionedTableFormat)
            addVersionData(data, dataset.getId(), 0);
        else
            data.add("" + dataset.getId());

        data.add("" + count++);// lineNumber
        data.addAll(record.tokens());

        massageNullMarkers(data);

        return (String[]) data.toArray(new String[0]);
    }

}

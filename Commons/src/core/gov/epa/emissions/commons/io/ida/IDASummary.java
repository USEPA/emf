package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.io.importer.SummaryTable;

public class IDASummary implements SummaryTable {

    private Dataset dataset;

    public IDASummary(Dataset dataset) {
        this.dataset = dataset;
    }

    public void createSummary() {
        InternalSource internalSource = dataset.getInternalSources()[0];
        String table = internalSource.getTable();
        InternalSource summarySource = summarySource(table);
        dataset.setSummarySource(summarySource);
    }

    private InternalSource summarySource(String table) {
        InternalSource summarySource = new InternalSource();
        summarySource.setType("Summary Table");
        summarySource.setTable(table);
        summarySource.setSource("TODO: ");
        return summarySource;
    }

}

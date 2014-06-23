package gov.epa.emissions.framework.client.fast.analyzer;

import gov.epa.emissions.framework.client.fast.AbstractMPSDTTableData;
import gov.epa.emissions.framework.services.fast.FastAnalysis;

public class FastAnalysisTableData extends AbstractMPSDTTableData<FastAnalysis> {

    private static final String[] COLUMNS = { "Name", "Description", "Start Time", "End Time", "Status" };

    public static final String DEFAULT_TIME = "N/A";

    public FastAnalysisTableData(FastAnalysis[] analyses) {
        super(analyses);
    }

    public String[] columns() {
        return COLUMNS;
    }

    @Override
    protected String[] createRowValues(FastAnalysis analysis) {
        return new String[] { this.getValueWithDefault(analysis.getName()),
                this.getValueWithDefault(analysis.getDescription()), this.format(analysis.getStartDate()),
                this.format(analysis.getCompletionDate()), this.getValueWithDefault(analysis.getRunStatus()) };
    }
}

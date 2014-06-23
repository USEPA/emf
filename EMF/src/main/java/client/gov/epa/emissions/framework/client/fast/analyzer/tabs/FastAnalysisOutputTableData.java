package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.framework.client.fast.AbstractMPSDTTableData;
import gov.epa.emissions.framework.services.fast.FastAnalysisOutput;
import gov.epa.emissions.framework.services.fast.Grid;

public class FastAnalysisOutputTableData extends AbstractMPSDTTableData<FastAnalysisOutput> {

    private static final String[] COLUMNS = { "Result Type", "Result", "Status", "Start Time", "Completion Time" };

    public FastAnalysisOutputTableData(FastAnalysisOutput[] outputs) {
        super(outputs);
    }

    public String[] columns() {
        return COLUMNS;
    }

    @Override
    protected String[] createRowValues(FastAnalysisOutput output) {

        return new String[] { this.getValueWithDefault(output.getType().getName()),
                this.getValueWithDefault(output.getOutputDataset().getName()), output.getRunStatus(),
                this.format(output.getStartDate()), this.format(output.getCompletionDate()) };
    }

    protected String getGridNameWithDefault(Grid grid) {
        return grid == null || grid.getName() == null || grid.getName().trim().length() == 0 ? DEFAULT_VALUE : grid
                .getName().trim();
    }
}

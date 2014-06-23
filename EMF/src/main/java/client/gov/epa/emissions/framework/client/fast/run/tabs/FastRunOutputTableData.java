package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.framework.client.fast.AbstractMPSDTTableData;
import gov.epa.emissions.framework.services.fast.FastRunOutput;
import gov.epa.emissions.framework.services.fast.Grid;

public class FastRunOutputTableData extends AbstractMPSDTTableData<FastRunOutput> {

    private static final String[] COLUMNS = { "Result Type", "Result", "Status", "Start Time", "Completion Time",
            "Grid" };

    public FastRunOutputTableData(FastRunOutput[] outputs) {
        super(outputs);
    }

    public String[] columns() {
        return COLUMNS;
    }

    @Override
    protected String[] createRowValues(FastRunOutput output) {

        return new String[] { this.getValueWithDefault(output.getType().getName()),
                this.getValueWithDefault(output.getOutputDataset().getName()), output.getRunStatus(),
                this.format(output.getStartDate()), this.format(output.getCompletionDate()),
                this.getGridNameWithDefault(output.getGrid()) };
    }

    protected String getGridNameWithDefault(Grid grid) {
        return grid == null || grid.getName() == null || grid.getName().trim().length() == 0 ? DEFAULT_VALUE : grid
                .getName().trim();
    }
}

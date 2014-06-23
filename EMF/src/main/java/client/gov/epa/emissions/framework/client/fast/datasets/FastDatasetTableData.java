package gov.epa.emissions.framework.client.fast.datasets;

import gov.epa.emissions.framework.client.fast.AbstractMPSDTTableData;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;

import java.util.List;

public class FastDatasetTableData extends AbstractMPSDTTableData<FastDatasetWrapper> {

    private static final String[] COLUMNS = { "Dataset", "Added Date", "Type", "Base Non-Point Dataset",
            "Gridded SMOKE Dataset", "Grid" };

    public static final String DEFAULT_TIME = "N/A";

    public FastDatasetTableData(FastDatasetWrapper[] datasets) {
        super(datasets);
    }

    public FastDatasetTableData(List<FastDatasetWrapper> datasets) {
        this(datasets.toArray(new FastDatasetWrapper[0]));
    }

    public String[] columns() {
        return COLUMNS;
    }

    @Override
    protected String[] createRowValues(FastDatasetWrapper wrapper) {

        String[] rowValues = new String[0];

        rowValues = new String[] { this.getValueWithDefault(wrapper.getName()), this.format(wrapper.getAddedDate()),
                wrapper.getType(), this.getValueWithDefault(wrapper.getBaseNonPointName()),
                this.getValueWithDefault(wrapper.getGriddedSMOKEName()),
                this.getValueWithDefault(wrapper.getGridName()) };

        return rowValues;
    }
}

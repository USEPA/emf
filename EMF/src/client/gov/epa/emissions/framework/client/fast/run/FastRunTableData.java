package gov.epa.emissions.framework.client.fast.run;

import gov.epa.emissions.framework.client.fast.AbstractMPSDTTableData;
import gov.epa.emissions.framework.services.fast.FastRun;

public class FastRunTableData extends AbstractMPSDTTableData<FastRun> {

    private static final String[] COLUMNS = { "Name", "Description", "Start Time", "End Time", "Status",
            "Inventory Table", "Inventory Table Version", "Species Mapping", "Species Mapping Version",
            "Transfer Coefficients", "Transfer Coefficients Version", "Domain Population", "Domain Population Version",
            "Cancer Risk", "Cancer Risk Version" };

    public static final String DEFAULT_TIME = "N/A";

    public FastRunTableData(FastRun[] runs) {
        super(runs);
    }

    public String[] columns() {
        return COLUMNS;
    }

    @Override
    protected String[] createRowValues(FastRun run) {
        return new String[] { this.getValueWithDefault(run.getName()), this.getValueWithDefault(run.getDescription()),
                this.format(run.getStartDate()), this.format(run.getCompletionDate()),
                this.getValueWithDefault(run.getRunStatus()), run.getInvTableDataset().getName(),
                Integer.toString(run.getInvTableDatasetVersion()), run.getSpeciesMapppingDataset().getName(),
                Integer.toString(run.getSpeciesMapppingDatasetVersion()),
                run.getTransferCoefficientsDataset().getName(),
                Integer.toString(run.getTransferCoefficientsDatasetVersion()),
                run.getDomainPopulationDataset().getName(), Integer.toString(run.getDomainPopulationDatasetVersion()),
                run.getCancerRiskDataset().getName(), Integer.toString(run.getCancerRiskDatasetVersion()) };
    }
}

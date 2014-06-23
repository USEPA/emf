package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.framework.client.fast.Command;
import gov.epa.emissions.framework.services.data.EmfDataset;

public abstract class DatasetCommand implements Command {

    private EmfDataset dataset;

    public EmfDataset getDataset() {
        return dataset;
    }

    public void setDataset(EmfDataset dataset) {
        this.dataset = dataset;
    }

    public void postExecute() {
        /*
         * no-op
         */
    }
}

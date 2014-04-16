package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.client.EmfSession;

public class PollutantSelectionPresenter {

    private PollutantTableData tableData;
    private EditControlStrategyPollutantsTab parentView;
    private Pollutant[] pollutants;

    public PollutantSelectionPresenter(EditControlStrategyPollutantsTab parentView, PollutantSelectionView view, 
            EmfSession session, Pollutant[] pollutants) {
        this.parentView = parentView;
        this.pollutants = pollutants;
    }

    public void display(PollutantSelectionView view) throws Exception {
        view.observe(this);
        this.tableData = new PollutantTableData(pollutants);
        view.display(tableData);

    }

    public void doAdd(Pollutant[] pollutants) {
        parentView.add(pollutants);
    }

}
